package rita.support;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.regex.Pattern;

import processing.core.PApplet;
import rita.*;
import rita.support.ifs.RiLexiconIF;

import com.sun.speech.freetts.en.us.CMULexicon;
import com.sun.speech.freetts.lexicon.LetterToSound;
import com.sun.speech.freetts.lexicon.Lexicon;
import com.sun.speech.freetts.util.Utilities;

/**
 * Provides an implementation of a user-customizable Lexicon using
 * CMU-style pronunciation tags and penn-style part-of-speech tags.
 <p>
 * The implementation also allows users to define their own addenda that will be
 * used in addition to the system addenda. If the user defines their own
 * addenda, it values will be added to the system addenda, overriding any
 * existing elements in the system addenda. 
 */
public class RiCMULexicon implements RiLexiconIF
{  
  private static final String SPC = " ";
  private static String DEFAULT_USER_ADDENDA_FILE = "rita_addenda.txt";
  private static final String LEXICON_DELIM = "\t";
  static final String DEFAULT_LEXICON = "cmudict04";//"cmulex";
  private static final boolean LOAD_USER_ADDENDA = true;
  private static final String CMUDICT_COMMENT = "***";
  private static final boolean DEFAULT_USE_BINARY = false;
  
  /**
   * If true, the phone string is replaced with the phone array in the hashmap
   * when the phone array is loaded. The side effects of this are quicker
   * lookups, but more memory usage and a longer startup time.
   
  private boolean tokenizeOnLoad = false;*/

  /**
   * If true, the phone string is replaced with the phone array in the hashmap
   * when the phone array is first looked up. The side effects Set by
   * cmufilelex.tokenize=lookup.
   */
  //private boolean tokenizeOnLookup = false;

  /**
   * Magic number for binary Lexicon files.
   */
  private final static int MAGIC = 0xBABB1E;

  /**
   * Current binary file version.
   */
  private final static int VERSION = 1;

  /**
   * URL for the compiled form.
   */
  private URL compiledURL;

  /**
   * URL for the addenda.   
  private URL addendaURL;*/

  /**
   * URL for the letter to sound rules.
   */
  private URL letterToSoundURL;
  
  /**
   * Loaded State of the lexicon
   */
  private boolean loaded = false;

  /**
   * Type of lexicon to load
   */
  private boolean binary = true;

  /**
   * No phones for this word.
   */
  //final static private String[] NO_PHONES = new String[0];

  /**
   * Temporary place holder.
   */
  private char charBuffer[] = new char[128];
  /**
   * The addenda.
   */
  //private Map addenda;

  /**
   * The compiled lexicon.
   */
  private Map compiled;

  /**
   * The LetterToSound rules.
   */
  private LetterToSound letterToSound = null;

  /**
   * Parts of Speech.  // remove!
   */
  //public static List partsOfSpeech = new ArrayList();

  /**
   * A static directory of compiledURL URL objects and associated already-loaded
   * compiled Map objects. This is used to share the immutable compiled lexicons
   * between lexicon instances. As the addenda can be changed using
   * <code>addAddendum()</code> and <code>removeAddendum</code>, each
   * lexicon instance has its own addenda.
   */
  //private static Map loadedCompiledLexicons;


  /**
   * Use the new IO package?
   */
  private boolean useNewIO = Utilities.getProperty(
      "com.sun.speech.freetts.useNewIO", "true").equals("true");

  /**
   * Vowels
   */
  static final private String VOWELS = "aeiou";

  /**
   * Glides/Liquids
   */
  static final private String GLIDES_LIQUIDS = "wylr";

  /**
   * Nasals
   */
  static final private String NASALS = "nm";

  /**
   * Voiced Obstruents
   */
  static final private String VOICED_OBSTRUENTS = "bdgjlmnnnrvwyz";

  
  // statics ====================================
  private static RiCMULexicon instance;

  public static RiCMULexicon getInstance() 
  {        
    return getInstance(null, DEFAULT_LEXICON);
  }
  
  /**
   * Creates, loads and returns the singleton lexicon instance.<p>
   */ 
  public static RiCMULexicon getInstance(PApplet p) 
  {        
    return getInstance(p, DEFAULT_LEXICON);
  }
  
  /**
   * Creates, loads and returns the singleton lexicon instance.<p>
   */ 
  public static RiCMULexicon getInstance(PApplet p, String basename) 
  {    
    if (instance == null) {
      try {        
        long start = System.currentTimeMillis();
        instance = new RiCMULexicon(basename, DEFAULT_USE_BINARY);       
        instance.load(p);
        int addenda = instance.getAddendaCount(); 
        if (!RiTa.SILENT)System.out.println("[INFO] Loaded "+instance.size() + "("
          + addenda + ") lexicon entries in "+RiTa.elapsed(start)+"s");
      }
      catch (Throwable e) {
        throw new RiTaException(e);
      }   
    }
    return instance;
  }
  
  // constructors ====================================
 
  /**
   * Constructs an unloaded instance of the lexicon.<p>
   * @throws MalformedURLException 
   */ 
  private RiCMULexicon(String basename, boolean useBinaryIO) throws MalformedURLException 
  {
    setLexiconParameters(getURL(basename,"_compiled"),
      getURL(basename,"_addenda"), getURL(basename,"_lts"), useBinaryIO);
    
  }

  
  /**
   * Returns the number of user addenda items added to the lexicon
   */
  public int getAddendaCount() { 
    return addendaCount; 
  }
  private int addendaCount = 0;
  
  private static final Map overrides = new HashMap();  
  static {
    overrides.put("offical", null);
    overrides.put("blog","b l ao g\tnn vbg");
    overrides.put("legible","l eh g ax b ax l\tjj");
  }

  /**
   * Sets the lexicon parameters
   * 
   * @param compiledURL
   *          a URL pointing to the compiled lexicon
   * @param addendaURL
   *          a URL pointing to lexicon addenda
   * @param letterToSoundURL
   *          a URL pointing to the LetterToSound to use
   * @param binary
   *          if <code>true</code>, the input streams are binary; otherwise,
   *          they are text.
   */
  private void setLexiconParameters(URL compiledURL, URL addendaURL,
      URL letterToSoundURL, boolean binary)
  {
    this.compiledURL = compiledURL;
   // this.addendaURL = addendaURL;
    this.letterToSoundURL = letterToSoundURL;
    this.binary = binary;
  }

  /**
   * Determines if this lexicon is loaded.
   * 
   * @return <code>true</code> if the lexicon is loaded
   */
  public boolean isLoaded()  { return loaded; }

  /**
   * Loads the data into  this lexicon. If the
   * 
   * @throws IOException
   *           if errors occur during loading
   */
  public void load() throws IOException { this.load(null); }
  
  /**
   * Loads the data into this lexicon. 
   */
  private void load(PApplet p) throws IOException
  {
    if (!RiTa.SILENT)System.out.println("RiCMULexicon.load("+compiledURL+")");
    if (compiledURL == null) throw new RiTaException("No lexicon URL");

    InputStream compiledIS = getInputStream(compiledURL);
    if (compiledIS == null)
      throw new RiTaException("Can't load lexicon from " + compiledURL);
    
    if (compiled != null)
      throw new RiTaException("recreating compiled map: " + compiledURL);
    
    compiled = createLexicon(compiledIS, binary, 20000);
    if (compiled == null)
      throw new RiTaException
        ("Can't create Lexicon from "+compiledURL+" binary="+binary);
    compiledIS.close();
    
    if (LOAD_USER_ADDENDA)
      addAddendaEntries(p, DEFAULT_USER_ADDENDA_FILE, compiled);  
        
    loaded = true;
    letterToSound = new RiLetterToSound(letterToSoundURL, binary);
  }  

  private static InputStream getInputStream(URL url) throws IOException {
    if (url.getProtocol().equals("file"))
      return new FileInputStream(url.getFile());
    else 
      return url.openStream();
  }
  
  private int addToMap(InputStream is, Map lexicon) throws IOException
  {
    int num = 0;
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    String line = reader.readLine();
    while (line != null) {
      if (!line.startsWith(CMUDICT_COMMENT)) {
        parseAndAdd(lexicon, line);
        num++;
      }
      line = reader.readLine();
    }
    reader.close();
    reader = null;
    for (Iterator iterator = overrides.keySet().iterator(); iterator.hasNext();) {
      String key = (String) iterator.next();
      String data = (String)overrides.get(key);
      if (lexicon.containsKey(key)) {
        if (data == null) {
          if (RiLexicon.VERBOSE_WARNINGS)
            System.err.println("REMOVING: "+key);
          lexicon.remove(key);
        }
        else
          lexicon.put(key, data);
      }
    }
    //if (!overrides.containsKey(parts[0])) 
      
      //lexicon.put(parts[0], parts[1].trim()+LEXICON_DELIM+parts[2].trim());
    //else
     //System.err.println("SKIPPING: "+parts[0]);
    //if (parts[0].equals("rpm"))
      //System.out.println(parts[0]+": '"+parts[1]+"' "+LEXICON_DELIM+" '"+parts[2]+"'");
    
    
      //String d ="";
      //lexicon.put(parts[0], overrides.get(parts[0]));
      //System.out.println("SKIPPING: "+parts[0]);
    //}
    //else
    

    return num;
  }
  
  private void addAddendaEntries(PApplet p, String fileName, Map compiledMap)
  {
    InputStream is = null;
    try  {
      is = RiTa.openStream(p, fileName);
      if (is == null) throw new RiTaException
        ("Null input stream for addenda file: "+fileName);
    } catch (Throwable e) {
      // this is the default (when the user hasn't 
      // provided an addenda file), just return...
      return;
    }
      
    try  { 
      this.addendaCount = addToMap(is, compiledMap);
      if (addendaCount > 0 && !RiTa.SILENT)
        System.out.println("[INFO] Loaded "+addendaCount
          + " entries from user addenda file");
    }
    catch (Throwable e) {  
      //System.out.println("[WARN] User-addenda file not"
        //+ " in expected location: data/" + fileName);
      throw new RiTaException(e);
    }
  }
  
  /**
   * Reads the given input stream as lexicon data and returns the results in a
   * <code>Map</code>.
   * 
   * @param is
   *          the input stream
   * @param binary
   *          if <code>true</code>, the data is binary
   * @param estimatedSize
   *          the estimated size of the lexicon
   * 
   * @throws IOException
   *           if errors are encountered while reading the data
   */
  private Map createLexicon(InputStream is, boolean useBinary, int estimatedSize)
      throws IOException
  {
    if (useBinary)
    {
      if (useNewIO && is instanceof FileInputStream)
      {        
        FileInputStream fis = (FileInputStream) is;
        return loadMappedBinaryLexicon(fis, estimatedSize);
      } 
      else
      {
        //System.err.println("[WARN] JarFile: not using NEW_IO! useNewIO="+useNewIO);//+compiledURL);
        DataInputStream dis = new DataInputStream(new BufferedInputStream(is));
        return loadBinaryLexicon(dis, estimatedSize);
      }
    } 
    else
    {
      Map lexicon = new LinkedHashMap(estimatedSize * 4 / 3);
      addToMap(is, lexicon);
      return lexicon;
    }
  }

 

  /**
   * Creates a word from the given input line and add it to the lexicon.
   * Returns true if the word was not a duplicate, else false
   * 
   * @param lexicon the lexicon
   * @param line  the input text
   */
  private void parseAndAdd(Map lexicon, String line)
  {
    if (line == null || line.length() < 1) return;      
        
    String[] parts = line.split(LEXICON_DELIM);
    if (parts == null || parts.length != 3)
      throw new RiTaException("Illegal entry: "+line);
    lexicon.put(parts[0], parts[1].trim()+LEXICON_DELIM+parts[2].trim());
  }
  
  /**
   * Creates a word from the given input line and add it to the lexicon.
   * Returns true if the word was not a duplicate, else false
   * 
   * @param lexicon the lexicon
   * @param line  the input text
   */
  private boolean oldParseAndAdd(Map lexicon, String line, boolean test)
  {
    if (line == null || line.length() <1 ) return false;
    
    String phones = null;
    StringTokenizer tokenizer = new StringTokenizer(line, "\t");
    
    String wordAndPos = tokenizer.nextToken();
    
    //else System.err.println("RiLexiconImpl.parseAndAdd("+line+")");

    boolean isDup = test && lexicon.containsKey(wordAndPos);
    
    if (isDup) 
      System.err.println("[WARN] overwriting previous entry: "+wordAndPos);

    //String word = wordAndPos.substring(0, wordAndPos.length() - 1);
    String pos = wordAndPos.substring(wordAndPos.length() - 1);

    // remove parts of speech from the lexicon!!    
/*    if (!partsOfSpeech.contains(pos))
      partsOfSpeech.add(pos);   */  

    if (tokenizer.hasMoreTokens())
      phones = tokenizer.nextToken();

    /*if ((phones != null) && (tokenizeOnLoad))
      lexicon.put(wordAndPos, getPhones(phones));
    else 
    if (phones == null)
      lexicon.put(wordAndPos, NO_PHONES);
    else*/
      
    lexicon.put(wordAndPos, phones);

    return !isDup;
  }

  /**
   * Gets the phone list for a given word. If a phone list cannot be found,
   * returns <code>null</code>. The format is lexicon dependent. If the part
   * of speech does not matter, pass in <code>null</code>.
   */
  public String[] getPhones(String word, String partOfSpeech) {
    return getPhones(word, null/*partOfSpeech*/, true);
  }

  /**
   * Gets the phone list for a given word. If a phone list cannot be found,
   * <code>null</code> is returned. The <code>partOfSpeech</code> is
   * implementation dependent, but <code>null</code> always matches.
   */
  public String[] getPhones(String word, String partOfSpeech, boolean useLTS)
  {
    String[] phones = getPhones(compiled, word);
    
    if (useLTS && phones == null && letterToSound != null) {
      //if (1==1) throw new RuntimeException("LTS: '"+word+"' compiled="+compiled.get(word));
      phones = letterToSound.getPhones(word, partOfSpeech);
    }
    
    return phones;
  }

  /**
   * Gets a phone list for a word from a given lexicon. If a phone list cannot
   * be found, returns <code>null</code>.
   * 
   * @param lexicon
   * @param word  
   * 
   * @return the list of phones for word or <code>null</code>
   */
  private String[] getPhones(Map lexicon, String word)
  {
    String value = (String)lexicon.get(word);
    if (value == null) return null;
    String[] phonesAndPos = value.split(LEXICON_DELIM);   
    if (phonesAndPos == null || phonesAndPos.length != 2)
      throw new RiTaException("bad lexicon entry for: "+word);
    
    return phonesAndPos[0].split(SPC);    
  }
  
  /*
    if (value instanceof String[])
    {
      return (String[]) value;
    }
    else if (value instanceof String)
    {
      String[] phoneArray;
      phoneArray = getPhones((String) value);
      if (tokenizeOnLookup)
      {
        lexicon.put(word, phoneArray);
      }
      return phoneArray;
    } 
    else
    {
      return null;
    }
  }*/

  /**
   * Turns the phone <code>String</code> into a <code>String[]</code>,
   * using " " as the delimiter. 
   * @param phones 
   * @return the phones split into an array
   
  private String[] getPhones(String phones)
  {
    ArrayList phoneList = new ArrayList();
    StringTokenizer tokenizer = new StringTokenizer(phones, SPC);
    while (tokenizer.hasMoreTokens())
    {
      phoneList.add(tokenizer.nextToken());
    }
    return (String[]) phoneList.toArray(new String[0]);
  }*/

  /**
   * Adds a word to the addenda. 
   */
  public void addAddendum(String word, String[] phones)
  {
    //String pos = fixPartOfSpeech(partOfSpeech);
/*    if (!partsOfSpeech.contains(pos)) 
      partsOfSpeech.add(pos);*/
    compiled.put(word, phones);
  }

  /**
   * Removes a word from the lexicon.
   * 
   * @param word
   *          the word to remove
   * @param partOfSpeech
   *          the part of speech
   */
  public void removeAddendum(String word, String partOfSpeech)
  {
    compiled.remove(word);// + fixPartOfSpeech(partOfSpeech));
  }

  /**
   * Outputs a string to a data output stream.
   * 
   * @param dos
   *          the data output stream
   * @param s
   *          the string to output
   * 
   * @throws IOException
   *           if errors occur during writing
   */
  private void outString(DataOutputStream dos, String s) throws IOException
  {
    dos.writeByte((byte) s.length());
    for (int i = 0; i < s.length(); i++)
    {
      dos.writeChar(s.charAt(i));
    }
  }

  /**
   * Inputs a string from a DataInputStream. This method is not re-entrant.
   * 
   * @param dis
   *          the data input stream
   * 
   * @return the string
   * 
   * @throws IOException
   *           if errors occur during reading
   */
  private String getString(DataInputStream dis) throws IOException
  {
    int size = dis.readByte();
    for (int i = 0; i < size; i++)
    {
      charBuffer[i] = dis.readChar();
    }
    return new String(charBuffer, 0, size);
  }

  /**
   * Inputs a string from a DataInputStream. This method is not re-entrant.
   * 
   * @param bb
   *          the input byte buffer
   * 
   * @return the string
   * 
   * @throws IOException
   *           if errors occur during reading
   */
  private String getString(ByteBuffer bb) throws IOException
  {
    int size = bb.get();
    for (int i = 0; i < size; i++)
    {
      charBuffer[i] = bb.getChar();
    }
    return new String(charBuffer, 0, size);
  }

  /**
   * Dumps a binary form of the database. This method is not thread-safe.
   * 
   * <p>
   * Binary format is:
   * 
   * <pre>
   *  MAGIC
   *  VERSION
   *  (int) numPhonemes
   *  (String) phoneme0
   *  (String) phoneme1
   *  (String) phonemeN
   *  (int) numEntries
   *  (String) nameWithPOS 
   *  (byte) numPhonemes
   *  phoneme index 1
   *  phoneme index 2
   *  phoneme index n
   * </pre>
   * 
   * <p>
   * Strings are formatted as: <code>(byte) len char0 char1 charN</code>
   * 
   * <p>
   * Limits: Strings: 128 chars
   * <p>
   * Limits: Strings: 128 phonemes per word
   * 
   * @param lexicon
   *          the lexicon to dump
   * @param path
   *          the path to dump the file to
  
  private void dumpBinaryLexicon(Map lexicon, String path)
  {
    try
    {
      FileOutputStream fos = new FileOutputStream(path);
      DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(fos));
      List phonemeList = findPhonemes(lexicon);

      dos.writeInt(MAGIC);
      dos.writeInt(VERSION);
      dos.writeInt(phonemeList.size());

      for (int i = 0; i < phonemeList.size(); i++)
      {
        outString(dos, (String) phonemeList.get(i));
      }

      dos.writeInt(lexicon.keySet().size());
      for (Iterator i = lexicon.keySet().iterator(); i.hasNext();)
      {
        String key = (String) i.next();
        outString(dos, key);
        String[] phonemes = getPhones(lexicon, key);
        dos.writeByte((byte) phonemes.length);
        for (int index = 0; index < phonemes.length; index++)
        {
          int phonemeIndex = phonemeList.indexOf(phonemes[index]);
          if (phonemeIndex == -1)
          {
            throw new Error("Can't find phoneme index");
          }
          dos.writeByte((byte) phonemeIndex);
        }
      }
      dos.close();
    } catch (FileNotFoundException fe)
    {
      throw new Error("Can't dump binary database " + fe.getMessage());
    } catch (IOException ioe)
    {
      throw new Error("Can't write binary database " + ioe.getMessage());
    }
  } */

  /**
   * Loads the binary lexicon from the given InputStream. This method is not
   * thread safe.
   * 
   * @param is
   *          the InputStream to load the database from
   * @param estimatedSize
   *          estimate of how large the database is
   * 
   * @return a <code>Map</code> containing the lexicon
   * 
   * @throws IOException
   *           if an IO error occurs
   */
  private Map loadMappedBinaryLexicon(FileInputStream is, int estimatedSize)
      throws IOException
  {
    FileChannel fc = is.getChannel();

    MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, (int) fc
        .size());
    bb.load();
    int size = 0;
    int numEntries = 0;
    List phonemeList = new ArrayList();

    // we get better performance for some reason if we
    // just ignore estimated size
    //
    // Map lexicon = new HashMap();
    Map lexicon = new LinkedHashMap(estimatedSize * 4 / 3);

    if (bb.getInt() != MAGIC)
    {
      throw new Error("bad magic number in lexicon");
    }

    if (bb.getInt() != VERSION)
    {
      throw new Error("bad version number in lexicon");
    }

    size = bb.getInt();
    for (int i = 0; i < size; i++)
    {
      String phoneme = getString(bb);
      phonemeList.add(phoneme);
    }
    numEntries = bb.getInt();

    for (int i = 0; i < numEntries; i++)
    {
      String wordAndPos = getString(bb);
      String pos = Character.toString(wordAndPos
          .charAt(wordAndPos.length() - 1));
      
/*      if (!partsOfSpeech.contains(pos))
      {
        partsOfSpeech.add(pos);
      }*/

      int numPhonemes = bb.get();
      String[] phonemes = new String[numPhonemes];

      for (int j = 0; j < numPhonemes; j++)
      {
        phonemes[j] = (String) phonemeList.get(bb.get());
      }
      lexicon.put(wordAndPos, phonemes);
    }
    fc.close();
    return lexicon;
  }

  /**
   * Loads the binary lexicon from the given InputStream. This method is not
   * thread safe.
   * 
   * @param is
   *          the InputStream to load the database from
   * @param estimatedSize
   *          estimate of how large the database is
   * 
   * @return a <code>Map</code> containing the lexicon
   * 
   * @throws IOException
   *           if an IO error occurs
   */
  private Map loadBinaryLexicon(InputStream is, int estimatedSize)
      throws IOException
  {
    DataInputStream dis = new DataInputStream(new BufferedInputStream(is));
    int size = 0;
    int numEntries = 0;
    List phonemeList = new ArrayList();

    // we get better performance for some reason if we
    // just ignore estimated size
    //
    Map lexicon = new LinkedHashMap();

    if (dis.readInt() != MAGIC)
    {
      throw new Error("bad magic number in lexicon");
    }

    if (dis.readInt() != VERSION)
    {
      throw new Error("bad version number in lexicon");
    }

    size = dis.readInt();
    for (int i = 0; i < size; i++)
    {
      String phoneme = getString(dis);
      phonemeList.add(phoneme);
    }
    numEntries = dis.readInt();

    for (int i = 0; i < numEntries; i++)
    {
      String wordAndPos = getString(dis);
      String pos = Character.toString(wordAndPos
          .charAt(wordAndPos.length() - 1));
      
//      if (!partsOfSpeech.contains(pos))
//      {
//        partsOfSpeech.add(pos);
//      }

      int numPhonemes = dis.readByte();
      String[] phonemes = new String[numPhonemes];

      for (int j = 0; j < numPhonemes; j++)
      {
        phonemes[j] = (String) phonemeList.get(dis.readByte());
      }
      lexicon.put(wordAndPos, phonemes);
    }
    dis.close();
    return lexicon;
  }

  /**
   * Dumps this lexicon (just the compiled form). Lexicon will be dumped to two
   * binary files PATH_compiled.bin and PATH_addenda.bin
   * 
   * @param path
   *          the root path to dump it to
   
  public void dumpBinary(String path)
  {
    String compiledPath = path + "_compiled.bin";
    //String addendaPath = path + "_addenda.bin";

    dumpBinaryLexicon(compiled, compiledPath);
    //dumpBinaryLexicon(addenda, addendaPath);
  }*/

  /**
   * Returns a list of the unique phonemes in the lexicon.
   * 
   * @param lexicon
   *          the lexicon of interest
   * 
   * @return list the unique set of phonemes
   
  private List findPhonemes(Map lexicon)
  {
    List phonemeList = new ArrayList();
    for (Iterator i = lexicon.keySet().iterator(); i.hasNext();)
    {
      String key = (String) i.next();
      String[] phonemes = getPhones(lexicon, key);
      for (int index = 0; index < phonemes.length; index++)
      {
        if (!phonemeList.contains(phonemes[index]))
        {
          phonemeList.add(phonemes[index]);
        }
      }
    }
    return phonemeList;
  }*/

  /**
   * Tests to see if this lexicon is identical to the other for debugging
   * purposes.
   * 
   * @param other
   *          the other lexicon to compare to
   * 
   * @return true if lexicons are identical
   
  public boolean compare(RiLexiconImpl other)
  {
    return //compare(addenda, other.addenda) && 
    compare(compiled, other.compiled);
  }*/

  /**
   * Determines if the two lexicons are identical for debugging purposes.
   * 
   * @param lex
   *          this lex
   * @param other
   *          the other lexicon to chd
   * 
   * @return true if they are identical   
  private boolean compare(Map lex, Map other)
  {
    for (Iterator i = lex.keySet().iterator(); i.hasNext();)
    {
      String key = (String) i.next();
      String[] thisPhonemes = getPhones(lex, key);
      String[] otherPhonemes = getPhones(other, key);
      if (thisPhonemes == null)
      {
        System.out.println(key + " not found in this.");
        return false;
      } 
      else if (otherPhonemes == null)
      {
        System.out.println(key + " not found in other.");
        return false;
      } 
      else if (thisPhonemes.length == otherPhonemes.length)
      {
        for (int j = 0; j < thisPhonemes.length; j++)
        {
          if (!thisPhonemes[j].equals(otherPhonemes[j]))
          {
            return false;
          }
        }
      } else
      {
        return false;
      }
    }
    return true;
  }*/

  /**
   * Fixes the part of speech if it is <code>null</code>. The default
   * representation of a <code>null</code> part of speech is the number "0".

  static private String fixPartOfSpeech(String partOfSpeech)
  {
    return (partOfSpeech == null) ? "0" : partOfSpeech;
  }   */
  

  private static URL getURL(String basename, String suffix, boolean useBinaryIO)
  {    
    String file = basename + suffix + (useBinaryIO ? ".bin" : ".txt");

    URL lexUrl = null;
    try {
     // RiTa.pcwd();
      lexUrl = new URL("file:"+file);
      System.out.println("URL: "+lexUrl);
    } catch (MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    if (lexUrl == null) 
      System.err.println("Rita.Lexicon: Error! No lexicon at: "+file);    
    return lexUrl;
  }
  
  private static URL getURL(String basename, String suffix)
  {     
    return getURL(basename, suffix, false);
  }

/*  // Specific to format of CMULexicon
  private String stripPos(String wordAndPos)
  {
    if (wordAndPos.charAt(wordAndPos.length()-1) == '0')
      wordAndPos = wordAndPos.substring(0,wordAndPos.length()-1);
    
    return wordAndPos;
  }*/
  
  /*public Map getLexiconMap()
  {
    return this.compiled;
  }*/
  

  public int size()
  {   
    if (compiled==null) {
      System.err.println("NULL compiled Map!");
      return -1;
    }
    return this.compiled.size();
  }
  
  
  /**  
   * Determines if the currentPhone represents a new syllable
   * boundary.
   *
   * @param syllablePhones the phones in the current syllable so far
   * @param wordPhones the phones for the whole word
   * @param currentWordPhone the word phone in question
   *
   * @return <code>true</code> if the word phone in question is on a
   *     syllable boundary; otherwise <code>false</code>.
   */
  public boolean isSyllableBoundary(List syllablePhones,
                                    String[] wordPhones,
                                    int currentWordPhone) {
      boolean ib = false;
      if (currentWordPhone >= wordPhones.length) {
        //System.out.println("TRUE1 "+syllablePhones);
          ib = true;
      } else if (isSilence(wordPhones[currentWordPhone])) {
          ib =  true;
          //System.out.println("TRUE2 "+syllablePhones);
      } else if (!hasVowel(wordPhones, currentWordPhone)) { // rest of word 
          ib =  false;
      } else if (!hasVowel(syllablePhones)) { // current syllable
          ib =  false;
      } else if (isVowel(wordPhones[currentWordPhone])) {
          ib =  true;
          //System.out.println("TRUE3 "+syllablePhones);
      } else if (currentWordPhone == (wordPhones.length - 1)) {
          ib =  false;
      } else {
          int p, n, nn;
          p = getSonority((String) syllablePhones.get(syllablePhones.size() - 1));
          n = getSonority(wordPhones[currentWordPhone]);
          nn = getSonority(wordPhones[currentWordPhone + 1]);
          if ((p <= n) && (n <= nn)) {
              ib =  true;
                   //System.out.println("TRUE4 "+syllablePhones);
          } else {
              ib =  false;
          }
      }
      System.out.println("RiCMULexicon.isSyllableBoundary("+
        syllablePhones+", "+RiTa.asList(wordPhones)+", "+currentWordPhone+") -> "+ib);
      return ib;
  }
  
  /**
   * Determines if the given phone represents a silent phone.
   *
   * @param phone the phone to test
   *
   * @return <code>true</code> if the phone represents a silent
   *    phone; otherwise <code>false</code>. 
   */
  static private boolean isSilence(String phone) {
      return phone.equals("pau");
  }

  /**
   * Determines if there is a vowel in the remainder of the array, 
   * starting at the given index.
   *
   * @param phones the set of phones to check
   * @param index start checking at this index
   *
   * @return <code>true</code> if a vowel is found; 
   *    otherwise <code>false</code>. 
   */
  static private boolean hasVowel(String[] phones, int index) {
      for (int i = index; i < phones.length; i++) {
          if (isVowel(phones[i])) {
              return true;
          }
      }
      return false;
  }
  
  /**
   * Determines if there is a vowel in given list of phones.
   *
   * @param phones the list of phones
   *
   * @return <code>true</code> if a vowel is found; 
   *    otherwise <code>false</code>. 
   */
  static private boolean hasVowel(List phones) {
      for (int i = 0; i < phones.size(); i++) {
          if (isVowel((String) phones.get(i))) {
              return true;
          }
      }
      return false;
  }
  
  /**
   * Determines if the given phone is a vowel
   *
   * @param phone the phone to test
   *
   * @return <code>true</code> if phone is a vowel
   *    otherwise <code>false</code>. 
   */
  static private boolean isVowel(String phone) {
      return VOWELS.indexOf(phone.substring(0,1)) != -1;
  }

  /**
   * Determines the sonority for the given phone.
   * 
   * @param phone the phone of interest
   * 
   * @return an integer that classifies phone transitions
   */
  private static int getSonority(String phone) {
      if (isVowel(phone) || isSilence(phone)) {
          return 5;
      } else if (GLIDES_LIQUIDS.indexOf(phone.substring(0,1)) != -1) {
          return 4; 
      } else if (NASALS.indexOf(phone.substring(0,1)) != -1) {
          return 3;
      } else if (VOICED_OBSTRUENTS.indexOf(phone.substring(0,1)) != -1) {
          return 2;
      } else {
          return 1;
      }
  }       

  public Set getWords() { return compiled.keySet();  }
  
  /**
   * @invisible
   */
  public String lookupRaw(String word) {
    return (String)compiled.get(word.toLowerCase()); 
  }
  
  /**
   * @invisible
   */
  public String lookupPhonemesAndStresses(String word) {
    String data = lookupRaw(word);  
    if (data == null) return null;
    String[] s = data.split(LEXICON_DELIM);
    if (s == null || s.length != 2)
      throw new RiTaException("invalid lexicon entry: "+word+" / "+s[0]);
    return s[0];
  }
  
 /* private String lookupPhonemesAndStresses(String word) {
    String data = lookupRaw(word);  
    if (data == null) return null;
    String[] s = data.split(LEXICON_DELIM);
    if (s == null || s.length != 2)
      throw new RiTaException("invalid lexicon entry: "+word+" / "+s[0]);
    return s[0];
  }*/
  
  private String lookupPOS(String word) {
    String data = lookupRaw(word);
    if (data == null) return null;
    String[] s = data.split(LEXICON_DELIM);
    if (s == null || s.length != 2)
      throw new RiTaException("invalid lexicon entry: "+word);
    return s[1];
  }
    
  public Iterator iterator() { 
    return compiled.keySet().iterator(); 
  }
  
  public Set keySet() { 
    return compiled.keySet(); 
  }
  
  public Iterator iterator(boolean randomStartOffset) { 
    if (randomStartOffset) {
      HashMap s = new HashMap();
      s.keySet();
      return null;
    }
    else 
      return compiled.keySet().iterator();
  }

  public Set getWords(String regex)
  {
    Set s = new TreeSet();
    Pattern p = Pattern.compile(regex);
    for (Iterator iter = iterator(); iter.hasNext();) {
      String str = ((String) iter.next());
      if (p.matcher(str).matches()) 
        s.add(str);
    }
    return s;
  }

  public String[] getPosArr(String word)
  {
    String pos = lookupPOS(word);
    if (pos == null) return null;
    return pos.split(SPC);
  } 
  
  public String[] getPhones(String word)
  {
    String phones = lookupPhonemesAndStresses(word);
    if (phones == null) return null;   
    String[] p = phones.split(SPC);
    for (int i = 0; i < p.length; i++) {
      if (RiTa.lastCharMatches(p[i], STRESS_MARKS)) 
        p[i] = p[i].substring(0,p[i].length()-1);
    }
    return p;
  }
  
  /**
   * returns a '-' (denoting no mark) or 0 (unstressed) or 1 (stressed) for each phoneme
   */
  public String[] getRawStresses(String word)
  {
    String phones = lookupPhonemesAndStresses(word);
    if (phones == null) return null;   
    String[] p = phones.split(SPC);
    
    for (int i = 0; i < p.length; i++) {

      if (RiTa.lastCharMatches(p[i],STRESS_MARKS)) 
        p[i] = p[i].substring(p[i].length()-1);
      else
        p[i] = "0";
    }
    return p;
  }  
  static final char[] STRESS_MARKS = { RiLexicon.STRESSED, RiLexicon.UNSTRESSED };

  public void addAddendum(String word, String pos, String[] phones)
  {
    this.addAddendum(word, phones);
  } 
    
  int writeLexicon(String fname) throws IOException
  {
    Map overrides = new HashMap();
    overrides.put("offical", null);
    overrides.put("blog","b-l-ao1-g | nn vbg");
    overrides.put("legible","l-eh1-g ax-b ax-l | jj");
    overrides.put("kwh",null); // mph  th vs
    overrides.put("mpg",null);
    overrides.put("rpm",null);
    overrides.put("th",null);
    overrides.put("vs",null);
    
    //BrillPosTagger ft = BrillPosTagger.getInstance();   
    boolean writeToFile = true;
    boolean writeFullData = true;
    
    FileWriter fw = null;
    if (writeToFile) fw = new FileWriter(fname);
    
    int count = 0; int errs=0;    
    WORDS: for (Iterator i = iterator();/* count<50000 &&*/ i.hasNext();)
    {
      String word = (String) i.next();   

      //String pos = RiTa.join(tags);//tags.length >1 ? RiTa.join(tags) : tags[0];
      String pos = handleReadPos(word);
      String line = null, syls = null;
      if (!writeFullData) 
      {
        syls = RiTa.join(getPhones(word), Featured.WORD_BOUNDARY);
        if (syls == null) {
          System.out.println("No syllable data for: "+word);
          //syls = lex.lookup(word.toLowerCase());
          continue;
        }
        line = word+"\t"+syls+"\t"+pos.toLowerCase()+" \n";
      }
      else 
      {
        String ps = lookupPhonemesAndStresses(word);
        syls = getSyllables(word);
        
        
        int k = 0;
        String data = "";
        for (int j = 0; j < ps.length(); j++) {
          char c = ps.charAt(j);          
          if (c != '1') { 
            if (c == ' ') {            
              char d = syls.charAt(k);
              if (d=='-')
                c = '-';
              else if (d!=' ') {
                //errs += word+" ";
                //throw new RuntimeException(word+") unexpected character in d: "+d);
                //System.err.println(word+") unexpected character in d: "+ps+" "+errs);
                continue WORDS;
              }
            }            
            k++;
          }
          data += c;
        }
        if (overrides.containsKey(word)) {
          String val = (String) overrides.get(word);
          if (val == null) continue WORDS;
          line = word+": "+val+"\n";
        }
        line = word+": "+data+" | "+pos.toLowerCase()+"\n";        
      }
      if (line.indexOf("|") != line.lastIndexOf("|"))
          System.out.println("ERROR!!! "+line+" tags="+pos);
      
      if (writeToFile)
        fw.write(line);
      else {
        if (count%1000==0)
          ;//System.out.print(line);
      }
      
      //if (pos.indexOf(",")>=0)
        //System.out.println("FIX: "+word+" -> "+pos);
      //if (count % 1000==0) System.out.println(count);
    }
    if (writeToFile) {
      fw.flush(); 
      
      fw.close();
      System.out.println("Wrote "+RiTa.cwd()+"/"+fname);
    }
    return count;
  }
  
  private String handleReadPos(String word) {
      List stags = new ArrayList();
      String[] tags = getPosArr(word);
      for (int j = 0; j < tags.length; j++) {
        if (tags[j].contains("|")) {
          //System.err.println("handling weird '|' for: "+word);
          String[] extras = tags[j].split("\\|");
          for (int k = 0; k < extras.length; k++) {
            
            String tag = extras[k].trim();
            if (!stags.contains(tag))
              stags.add(tag);
          }
          //throw new RiTaException
          //System.out.println("[WARN] "+word+": "+tags[j]+" tags="+RiTa.asList(tags));
        }
        else {
            String tag = tags[j].trim();
            if (!stags.contains(tag))
              stags.add(tag);
        }
      }
      String pos = "";
      for (Iterator iterator = stags.iterator(); iterator.hasNext();) {
        pos += iterator.next();
        if (iterator.hasNext())
          pos += " ";
      }
      //String pos = RiTa.join(tags);
      if (pos.contains("|"))
        throw new RiTaException("ERROR!!! "+word+": "+pos+" tags="+RiTa.asList(tags));
      
      //System.out.println(word+" returning: "+pos);
      return pos; 
  }

  private String getSyllables(String word) {  
    Map features = RiPhrase.createFeatureMap(null, word);
    return (String)features.get(Featured.SYLLABLES);
    //String fStr = (String)features.get(Featured.SYLLABLES);
    //return fStr.split(Featured.SYLLABLE_BOUNDARY);
  }
  
  public static void writeLexicon2(RiCMULexicon lex) throws IOException
  {
    Map m = new HashMap();
    FileWriter fw = new FileWriter("new_dict.txt");
    for (Iterator iterator = lex.iterator(); iterator.hasNext();) {
      String type = (String) iterator.next();
      String data = lex.lookupRaw(type);
      String[] sa = data.split("\t");
      String pos = lex.handleReadPos(type);
      if (!sa[1].equals(pos))
        System.out.println("FIX) "+type+": "+sa[1]+" -> "+pos);
      m.put(type.trim(), pos.trim());
    }
    int count = 0;
    RiLexiconImpl rl = RiLexiconImpl.getInstance();
    for (Iterator iterator = lex.iterator(); iterator.hasNext(); count++) {
      String type = (String) iterator.next();
      type = type.trim();
      String data = rl.lookupRaw(type);
      if (data == null) {
        //System.out.println("skipping "+type);
        continue;
      }
      String[] sa = data.split("\\|");
      if (sa == null || sa.length!=2)
        throw new RuntimeException("invalid lex entry: "+type);
      String tags = (String)m.get(type);
      String line = type+": "+sa[0].trim()+" | "+tags.trim()+"\n";      
      if (line.indexOf("|") != line.lastIndexOf("|"))
        throw new RuntimeException("ERROR!!! "+line+" tags="+tags);      
      fw.write(line);
      if (count%1000==0)
       System.out.print(line);
    }
    fw.flush();
    fw.close();
  }
  
  public static void main(String[] args) {
    RiCMULexicon lex = RiCMULexicon.getInstance();
    
    //lex.writeLexicon("new-lexicon.txt");
    //System.out.println(lex.getSyllables(test));
    
    //System.out.println(RiTa.asList(lex.getPhones(test)));
    //System.out.println(RiTa.asList(lex.getRawStresses(test)));
    //System.out.println(RiTa.asList(lex.getPOSs(test)));
    System.out.println(RiTa.asList(lex.getPhones("blaupunkt", null, true)));
    System.out.println();
    //[b, l, ao1, p, ah1, ng, k, t]
    //[b, l, ao1, p, ah1, ng, k, t]
  }

  
}// end
