package rita.support;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

import processing.core.PApplet;
import rita.*;
import rita.support.ifs.RiLexiconIF;

import com.sun.speech.freetts.en.us.CMULexicon;
import com.sun.speech.freetts.lexicon.LetterToSound;

/**
 * Provides an implementation of a user-customizable Lexicon using CMU-style
 * pronunciation tags and Penn-style part-of-speech tags.
 * <p>
 * 
 * Note: this is a support class, public access is provided through
 * rita.RiLexicon.
 * <p>
 * The implementation also allows users to define their own addenda that will be
 * addenda, it values will be added to the system addenda, overriding any
 * existing elements in the system addenda.
 */
public class RiLexiconImpl implements RiLexiconIF
{
  public static final String DATA_DELIM = "\\|";
  static final String DEFAULT_LEXICON = "rita_dict.txt";

  private static final String CMUDICT_LTS_TXT = "cmudict04_lts.txt";
  private static String DEFAULT_USER_ADDENDA_FILE = "rita_addenda.txt";
  private static final boolean LOAD_USER_ADDENDA = true;
  private static final String CMUDICT_COMMENT = "#";
  private static final String LEXICON_DELIM = ":";
  private static final String PHONE_DELIM = "[- ]";
  private static final String SPC = " ";

  // members ====================================

  private String dictionaryFile;
  private Map lexicalData;
  private boolean loaded;
  private boolean lazyLoadLTS;
  private LetterToSound letterToSound;
  private URL letterToSoundURL;

  private PApplet _pApplet; // remove?

  // statics ====================================

  private static RiLexiconImpl instance;

  /**
   * @deprecated
   * @invisible
   */
  public static RiLexiconImpl getInstance()
  {
    return getInstance(null, DEFAULT_LEXICON);
  }

  /**
   * Creates, loads and returns the singleton lexicon instance.
   * <p>
   */
  public static RiLexiconImpl getInstance(PApplet p)
  {
    return getInstance(p, DEFAULT_LEXICON);
  }

  /**
   * Creates, loads and returns the singleton lexicon instance.
   * <p>
   */
  public static RiLexiconImpl getInstance(PApplet p, String pathToLexicon)
  {
    if (instance == null)
    {
      try
      {
        long start = System.currentTimeMillis();
        instance = new RiLexiconImpl(p, pathToLexicon);
        instance.load(p);
        int addenda = instance.getAddendaCount();
        if (!RiTa.SILENT)
          System.out.println("[INFO] Loaded " + instance.size() + 
            "(" + addenda + ") lexicon in " + RiTa.elapsed(start) + "s");
      }
      catch (Throwable e)
      {
        throw new RiTaException(e);
      }
    }
    return instance;
  }

  // constructors ====================================
  /**
   * Constructs an unloaded instance of the lexicon.
   * <p>
   * 
   * @param p
   */
  private RiLexiconImpl(PApplet p, String basename)
  {
    this._pApplet = p;
    this.dictionaryFile = basename;
  }

  // methods ====================================

  /**
   * Returns the raw data (as a Map) used in the lexicon. Modifications to this
   * Map will be immediately reflected in the lexicon.
   */
  public Map getLexicalData()
  {
    return lexicalData;
  }

  /**
   * Sets the raw data (a Map) used in the lexicon, replacing all default words
   * and features.
   */
  public void setLexicalData(Map lexicalData)
  {
    this.lexicalData = lexicalData;
  }

  /**
   * Returns the number of user addenda items added to the lexicon
   */
  public int getAddendaCount()
  {
    return addendaCount;
  }

  private int addendaCount = 0;

  /**
   * Determines if this lexicon is loaded.
   * 
   * @return <code>true</code> if the lexicon is loaded
   */
  public boolean isLoaded()
  {
    return loaded;
  }

  /**
   * Loads the data into this lexicon. If the
   * 
   * @throws IOException
   *           if errors occur during loading
   */
  public void load() throws IOException
  {
    this.load(null);
  }

  /**
   * Loads the data into this lexicon.
   */
  private void load(PApplet p) throws IOException
  {
    // System.out.println("RiTaLexicon.load("+dict+")");
    if (dictionaryFile == null)
      throw new RiTaException("No dictionary path specified!");

    InputStream is = null;
    if (dictionaryFile.equals(DEFAULT_LEXICON)) {
     // is = CMULexicon.class.getResourceAsStream(dictionaryFile);
      is = RiTa.openStream(CMULexicon.class, dictionaryFile);
    }
    else {
     is = RiTa.openStream(p,dictionaryFile);
     //new FileInputStream(new File(""));
    }
      
    if (is == null)
      throw new RiTaException("Unable to load lexicon from: " + CMULexicon.class + "." + dictionaryFile);
    
    
    lexicalData = createLexicon(is);

    if (LOAD_USER_ADDENDA)
      addAddendaEntries(p, DEFAULT_USER_ADDENDA_FILE, lexicalData);

    this.addCustomizations(lexicalData);
    this.pruneNonsenseWords(lexicalData);

    loaded = true;
    // if (letterToSoundURL == null)
    // letterToSoundURL = new
    // URL("jar:file:/Users/dhowe/Documents/eclipse-workspace/RiTa/src/jars/freetts121d.jar!/com/sun/speech/freetts/en/us/cmudict04_lts.txt");

    if (!lazyLoadLTS)
      getLTSEngine();
  }

  private LetterToSound getLTSEngine()
  {
    if (letterToSound == null)
    {
      letterToSoundURL = CMULexicon.class.getResource(CMUDICT_LTS_TXT);
      try
      {
        letterToSound = new RiLetterToSound(letterToSoundURL);
      }
      catch (IOException e)
      {
        throw new RiTaException(e);
      }
    }
    return letterToSound;
  }

  /*
   * private static InputStream getInputStream(URL url) throws IOException { if
   * (url.getProtocol().equals("file")) return new
   * FileInputStream(url.getFile()); else return url.openStream(); }
   */

  private int addToMap(InputStream is, Map lexicon) throws IOException
  {
    int num = 0;
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    String line = reader.readLine();
    while (line != null)
    {
      if (!line.startsWith(CMUDICT_COMMENT))
      {
        line = line.trim();
        if (line.length() > 0)
        {
          parseAndAdd(lexicon, line);
          num++;
        }
      }
      line = reader.readLine();
    }
    reader.close();
    reader = null;
    return num;
  }

  private void addAddendaEntries(PApplet p, String fileName, Map compiledMap)
  {
    InputStream is = null;
    try
    {
      is = RiTa.openStream(p, fileName);
      if (is == null)
        throw new RiTaException("Null input stream for addenda file: " + fileName);
    }
    catch (Throwable e)
    {
      // this is the default (when the user hasn't
      // provided an addenda file), just return...
      return;
    }

    try
    {
      this.addendaCount = addToMap(is, compiledMap);
      if (addendaCount > 0)
        if (!RiTa.SILENT) System.out.println("[INFO] Loaded " + addendaCount + " entries from user addenda file");
    }
    catch (Throwable e)
    {
      // System.out.println("[WARN] User-addenda file not"
      // + " in expected location: data/" + fileName);
      throw new RiTaException(e);
    }
  }

  /**
   * Reads the given input stream as lexicon data and returns the results in a
   * <code>Map</code>.
   */
  private Map createLexicon(InputStream is) throws IOException
  {
    Map lexicon = new LinkedHashMap(34000 * 4 / 3);
    addToMap(is, lexicon);
    return lexicon;
  }

  /**
   * Creates a word from the given input line and add it to the lexicon. Returns
   * true if the word was not a duplicate, else false
   * 
   * @param lexicon
   *          the lexicon
   * @param line
   *          the input text
   */
  private void parseAndAdd(Map lexicon, String line)
  {
    String[] parts = line.split(LEXICON_DELIM);
    if (parts == null || parts.length != 2)
      throw new RiTaException("Illegal entry: " + line);
    lexicon.put(parts[0], parts[1].trim());
  }

  /**
   * Gets the phone list (+ stresses) for a given word. If a phone list cannot
   * be found, returns <code>null</code>. The <code>partOfSpeech</code> is
   * optional, but <code>null</code> always matches.
   */
  public String[] getPhones(String word, String partOfSpeech)
  {
    return getPhones(word, null/* partOfSpeech */, true);
  }// @interface

  /**
   * Gets the phone list (+ stresses) for a given word. If a phone list cannot
   * be found, <code>null</code> is returned. The <code>partOfSpeech</code> is
   * optional, but <code>null</code> always matches.
   */
  public String[] getPhones(String word, String partOfSpeech, boolean useLTS)
  {
    return getPhones(word, useLTS);// phoneStr.split(Featured.PHONEME_BOUNDARY);
  }// @interface

  /**
   * Gets the phoneme list for a given word, either via lookup or, if not found,
   * (and <code>useLTS</code> is true), generated via the letter-to-sound
   * engine, else null.
   */
  public String getPhonemes(String word, boolean useLTS)
  {

    Map m = getFeatures(word);

    if (m != null) // check the lexicon first
      return (String) m.get(Featured.PHONEMES);

    if (useLTS)
    { // try using the lts engine
      String[] tmp = getPhones(word, true);
      if (tmp != null)
        return stripStresses(RiTa.join(tmp, Featured.PHONEME_BOUNDARY));
    }

    return null;
  }

  /**
   * Gets the phoneme list for a given word, either via lookup or, if not found,
   * (and <code>useLTS</code> is true), generated via the letter-to-sound
   * engine, else null.
   */
  public String[] getPhonemeArr(String word, boolean useLTS)
  {

    String[] res = null;
    Map m = getFeatures(word);

    if (m != null)
    { // check the lexicon first
      String s = (String) m.get(Featured.PHONEMES);
      if (s != null)
        res = s.split(Featured.PHONEME_BOUNDARY);
    }
    else
    {
      if (useLTS)
      { // try using the lts engine
        String[] tmp = getPhones(word, true);
        if (tmp != null)
          res = stripStresses(tmp);
      }
    }
    return res;
  }

  String[] stripStresses(String[] phonesAndStresses)
  {
    // String phonesOnly = "";
    // System.out.println("RiTaLexicon.stripStresses("+RiTa.asList(phonesAndStresses)+")");
    for (int i = 0; i < phonesAndStresses.length; i++)
    {
      String syl = phonesAndStresses[i];
      char c = syl.charAt(syl.length() - 1);
      if (c == RiLexicon.STRESSED)
      {
        syl = syl.substring(0, syl.length() - 1);
        phonesAndStresses[i] = syl;
      }
    }
    return phonesAndStresses;
  }

  String stripStresses(String phonesAndStresses)
  {
    StringBuilder phonesOnly = new StringBuilder();
    // System.out.println("RiTaLexicon.stripStresses("+RiTa.asList(phonesAndStresses)+")");
    for (int i = 0; i < phonesAndStresses.length(); i++)
    {
      char c = phonesAndStresses.charAt(i);
      if (c != RiLexicon.STRESSED)
        phonesOnly.append(c);
    }
    return phonesOnly.toString();
  }

  /**
   * Gets a phone list (+ stresses) for a word from a given lexicon. If a phone
   * list cannot be found, returns <code>null</code>.
   * 
   * @return the list of phones for word or <code>null</code>
   */
  private String[] getPhones(String word, boolean useLTS)
  {
    // System.out.println("RiTaLexicon.getPhones("+word+") -> "+lookupRaw(word));
    String[] phones = null;
    String raw = lookupRaw(word);
    if (raw != null)
    {
      String[] o = raw.split(DATA_DELIM);
      if (o.length != 2)
        throw new RiTaException("Invalid lexicon entry: " + raw);
      phones = o[0].trim().split(PHONE_DELIM); // BOTH PHONE DELIMS!
      // System.out.println(word+": "+RiTa.asList(phones));
      return phones;
    }

    if (phones == null && useLTS)
      phones = getLTSEngine().getPhones(word, null);
    // System.out.println(word+": "+RiTa.asList(phones));
    return phones;
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
    lexicalData.remove(word);// + fixPartOfSpeech(partOfSpeech));
  }

  public int size()
  {
    if (lexicalData == null)
    {
      System.err.println("NULL compiled Map!");
      return -1;
    }
    return this.lexicalData.size();
  }

  public Set getWords()
  {
    return lexicalData.keySet();
  }

  /**
   * @invisible
   */
  public String lookupRaw(String word)
  {
    return (String) lexicalData.get(word.toLowerCase());
  }

  /**
   * @invisible
   */
  /*
   * public String lookupPhonemesAndStresses(String word) { String data =
   * lookupRaw(word); if (data == null) return null; String[] s =
   * data.split(DATA_DELIM); if (s == null || s.length != 2) throw new
   * RiTaException("invalid lexicon entry: "+word+" / "+s[0]); return s[0]; }
   */

  /*
   * private String lookupPhonemesAndStresses(String word) { String data =
   * lookupRaw(word); if (data == null) return null; String[] s =
   * data.split(LEXICON_DELIM); if (s == null || s.length != 2) throw new
   * RiTaException("invalid lexicon entry: "+word+" / "+s[0]); return s[0]; }
   */

  /*
   * private String lookupPOS(String word) { String data = lookupRaw(word); if
   * (data == null) return null; String[] s = data.split(DATA_DELIM); if (s ==
   * null || s.length != 2) throw new
   * RiTaException("invalid lexicon entry: "+word); return s[1]; }
   */
  
  public Iterator iterator()
  {
    return lexicalData.keySet().iterator();
  }

  RiRandomIterator randomIterator = null;

  public Iterator randomIterator()
  {
    if (randomIterator == null)
      randomIterator = new RiRandomIterator(lexicalData.keySet());
    else
      randomIterator.reset();
    return randomIterator;
  }

  public Iterator randomPosIterator(String pos)
  {
    return new RiRandomIterator(getWordsWithPos(pos));
  }

  public Iterator posIterator(String pos)
  {
    return getWordsWithPos(pos).iterator();
  }

  public Set keySet()
  {
    return lexicalData.keySet();
  }

  public Set getWords(String regex)
  {
    Set s = new TreeSet();
    Pattern p = Pattern.compile(/* DOT_STAR+ */regex/* +DOT_STAR */);
    for (Iterator iter = iterator(); iter.hasNext();)
    {
      String str = ((String) iter.next());
      if (p.matcher(str).matches())
        s.add(str);
    }
    return s;
  }

  // what does this do exactly?
  // all words with the given pos, or all words where it is the first?????
  public Set getWordsWithPos(String pos)
  {
    if (!RiPos.isPennTag(pos))
      throw new RiTaException("Pos '" + pos + "' is not a recognized part-of-speech" 
          + " tag. Check the list in the documentation for rita.RiPosTagger");
    Set s = new TreeSet();
    String posSpc = pos + " ";
    for (Iterator iter = iterator(); iter.hasNext();)
    {
      String str = ((String) iter.next());
      String allpos = getPosStr(str);
      // System.out.print(str+": "+allpos);
      if (allpos.startsWith(posSpc) || allpos.equals(pos))
      {
        s.add(str);
        // System.out.println(" -> ADD!");
      }
      // else System.out.println();

    }
    return s;
  }

  // Caching --------------------------------------------
  private static boolean cacheEnabled = true, debugCache = false;
  private static Map featureCache;

  private void addToFeatureCache(String word, Map m)
  {
    if (featureCache == null)
      featureCache = new HashMap();
    featureCache.put(word, m);
  }

  private Map checkFeatureCache(String word)
  {
    if (featureCache == null)
      return null;
    Map m = (Map) featureCache.get(word);
    if (debugCache && m != null)
      System.out.println("Using cache for: " + word);
    return m;
  }

  public Map getFeatures(String word)
  {
    Map m = null;
    if (cacheEnabled)
      m = checkFeatureCache(word);

    if (m != null)
      return m; // return cache hit

    String dataStr = lookupRaw(word);
    if (dataStr == null)
      return null;

    String[] data = dataStr.split(DATA_DELIM);
    if (data == null || data.length != 2)
    {
      throw new RiTaException("Invalid lexicon entry: " + word + " -> '" + dataStr + "'");
      // System.out.println("invalid lexicon entry: "+word+" -> '"+dataStr+"'");
      // return null;
    }

    StringBuilder phones = new StringBuilder();
    StringBuilder stresses = new StringBuilder();
    StringBuilder syllables = new StringBuilder();
    String[] phonesAndStresses = data[0].split(SPC);
    for (int i = 0; i < phonesAndStresses.length; i++)
    {
      String syl = phonesAndStresses[i];
      boolean stressed = false;
      for (int j = 0; j < syl.length(); j++)
      {
        char c = syl.charAt(j);
        if (c == '1')
        {
          stressed = true;
        }
        else
        {
          // add phones and syls
          phones.append(c);
          syllables.append(c);
        }
      }

      // add the stress for each syllable
      stresses.append(stressed ? RiLexicon.STRESSED : RiLexicon.UNSTRESSED);

      if (i < phonesAndStresses.length - 1)
      {
        phones.append(Featured.PHONEME_BOUNDARY);
        syllables.append(Featured.SYLLABLE_BOUNDARY);
        stresses.append(Featured.SYLLABLE_BOUNDARY);
      }
    }

    m = new HashMap(8); // create feature-map
    m.put(Featured.SYLLABLES, syllables.toString());
    m.put("poslist", data[1].trim());
    m.put(Featured.PHONEMES, phones.toString());
    m.put(Featured.STRESSES, stresses.toString());

    if (cacheEnabled) // add to cache
      addToFeatureCache(word, m);

    return m;
  }

  /*
   * private Map featuresFromRules(String word) { String[] phones =
   * letterToSound.getPhones(word, null);
   * System.out.println("phones:"+RiTa.asList(phones)); Map m = new HashMap(8);
   * String phoneStr = "";
   * 
   * //m.put(Featured.PHONEMES, phoneStr); //m.put(Featured.STRESSES, phoneStr);
   * 
   * return m; }
   */
  /**
   * returns a '0' (no-stress) or 1 (stressed) for each phoneme
   * 
   * public String[] getRawStresses(String word) { String phones =
   * lookupPhonemesAndStresses(word); if (phones == null) return null; String[]
   * p = phones.split(Featured.PHONEME_BOUNDARY);
   * 
   * for (int i = 0; i
   * < p
   * .length; i++) { String s = p[i]; char c = s.charAt(s.length()-1);
   * System.err.println("Checking: "+s+"/"+c); if (c == RiLexicon.STRESSED) p[i]
   * = "1"; else p[i] = "0"; } return p; } static final char[] STRESS_MARKS = {
   * RiLexicon.STRESSED, RiLexicon.UNSTRESSED };
   */

  public void addAddendum(String word, String pos, String[] phones)
  {
    // lexMap.put(word, phones.joi)
    throw new RiTaException("addAddendum not implemented...");
  }

  /**
   * Determines if the currentPhone represents a new syllable boundary.
   * 
   * @param syllablePhones
   *          the phones in the current syllable so far
   * @param wordPhones
   *          the phones for the whole word
   * @param currentWordPhone
   *          the word phone in question
   * 
   * @return <code>true</code> if the word phone in question is on a syllable
   *         boundary; otherwise <code>false</code>.
   */
  public boolean isSyllableBoundary(List syllablePhones, String[] wordPhones, int currentWordPhone)
  {
    boolean ib = false;
    if (currentWordPhone >= wordPhones.length)
    {
      ib = true;
    }
    else if (RiPhone.isSilence(wordPhones[currentWordPhone]))
    {
      ib = true;
    }
    else if (!RiPhone.hasVowel(wordPhones, currentWordPhone))
    { // rest of word
      ib = false;
    }
    else if (!RiPhone.hasVowel(syllablePhones))
    { // current syllable
      ib = false;
    }
    else if (RiPhone.isVowel(wordPhones[currentWordPhone]))
    {
      ib = true;
    }
    else if (currentWordPhone == (wordPhones.length - 1))
    {
      ib = false;
    }
    else
    {
      int p, n, nn;
      p = RiPhone.getSonority((String) syllablePhones.get(syllablePhones.size() - 1));
      n = RiPhone.getSonority(wordPhones[currentWordPhone]);
      nn = RiPhone.getSonority(wordPhones[currentWordPhone + 1]);
      if ((p <= n) && (n <= nn))
      {
        ib = true;
      }
      else
      {
        ib = false;
      }
    }
    // System.out.println("RiTaLexicon.isSyllableBoundary("+
    // syllablePhones+", "+RiTa.asList(wordPhones)+", "+currentWordPhone+") -> "+ib);
    return ib;
  }

  public static boolean isCaching()
  {
    return cacheEnabled;
  }

  public void preloadFeatures()
  {
    cacheEnabled = true; // ?
    long start = System.currentTimeMillis();
    for (Iterator iterator = iterator(); iterator.hasNext();)
      getFeatures((String) iterator.next());
    if (!RiTa.SILENT)
      System.out.println("[INFO] Created and cached features in " + RiTa.elapsed(start) + "s");
  }

  public String getRawPhones(String word)
  {
    return getRawPhones(word, false);
  }

  public String getRawPhones(String word, boolean useLTS)
  {
    String data = lookupRaw(word);
    if (data == null)
    {
      // try LTS here?
      if (useLTS)
      {
        RiPhrase rp = new RiPhrase(_pApplet, word);
        System.out.println("LTS: " + rp);
      }
      return null;
    }
    String[] both = data.split(DATA_DELIM);
    return both[0].trim();
  }

  public String getPosStr(String word)
  { // cache? nah
    String data = lookupRaw(word);
    if (data == null)
      return null;
    String[] both = data.split(DATA_DELIM);
    return both[1].trim();
  }

  public String getPosStrOld(String word)
  {
    Map m = getFeatures(word); // OPT: dont need features here, just pos-choices
    if (m == null)
    {
      // System.err.println("[WARN] No features for: "+word);
      return null;
    }
    String pl = (String) m.get("poslist");
    if (pl == null)
      System.err.println("[WARN] No pos-list for: " + word);
    return pl;
  }

  public String[] getPosArr(String word)
  {
    String pl = getPosStr(word);
    return (pl == null) ? null : pl.split(SPC);
  }

  static void test()
  {
    int posMissess = 0;

    RiLexiconImpl lex = RiLexiconImpl.getInstance();
    for (Iterator iterator = lex.iterator(); iterator.hasNext();)
    {
      String word = (String) iterator.next();
      Map x = RiPhrase.createFeatureMap(null, word);
      Map y = lex.getFeatures(word);

      String xPh = (String) x.get(Featured.PHONEMES);
      String yPh = (String) y.get(Featured.PHONEMES);
      boolean mm = !(xPh.equals(yPh));

      if (false && !mm)
      {
        String xStr = (String) x.get(Featured.STRESSES);
        String yStr = (String) y.get(Featured.STRESSES);
        mm = !(xStr.equals(yStr));
      }

      if (!mm)
      {
        String xSyl = (String) x.get(Featured.SYLLABLES);
        String ySyl = (String) y.get(Featured.SYLLABLES);
        mm = !(xSyl.equals(ySyl));
      }

      if (!mm)
      {
        String xPos = (String) x.get(Featured.POS);
        String yPos = (String) y.get("poslist");
        boolean pm = yPos.indexOf(xPos) < 0;
        if (pm)
        {
          if (word.endsWith("ing"))
          {
            System.out.println(word + " =? " + xPos);
            posMissess++;
          }
        }
      }

      if (mm)
      {
        // if (!x.toString().equals(y.toString())) {
        // System.err.println("MISMATCH: "+word+"\n  LTS="+x+"\n  LEX="+y);
        // return;
      }

    }
    System.out.println("POS-MISSES=" + posMissess);
  }

  private void pruneNonsenseWords(Map m)
  {
    m.remove("arbs");
    m.remove("inti");
    m.remove("silvas");
    m.remove("doi");
    m.remove("doo");
    m.remove("ler");
    m.remove("mor");
    m.remove("ahs");
    m.remove("arb");
    m.remove("ast");
    m.remove("bam");
    m.remove("bel");
    m.remove("bon");
    m.remove("com");
    m.remove("cul");
    m.remove("das");
    m.remove("dea");
    m.remove("del");
    m.remove("der");
    m.remove("des");
    m.remove("dey");
    m.remove("duo");
    m.remove("ein");
    m.remove("ell");
    m.remove("est");
    m.remove("fee");
    m.remove("fer");
    m.remove("gee");
    m.remove("gen");
    m.remove("han");
    m.remove("hon");
    m.remove("ifs");
    m.remove("jai");
    m.remove("jua");
    m.remove("lak");
    m.remove("len");
    m.remove("lui");
    m.remove("mah");
    m.remove("mai");
    m.remove("mee");
    m.remove("mei");
    m.remove("mon");
    m.remove("mor");
    m.remove("och");
    m.remove("pas");
    m.remove("pol");
    m.remove("psi");
    m.remove("qua");
    m.remove("que");
    m.remove("qui");
    m.remove("raj");
    m.remove("rep");
    m.remove("roi");
    m.remove("rot");
    m.remove("rue");
    m.remove("ruh");
    m.remove("sup");
    m.remove("sur");
    m.remove("tae");
    m.remove("tam");
    m.remove("tat");
    m.remove("und");
    m.remove("ups");
    m.remove("von");
    m.remove("vos");
    m.remove("vue");
    m.remove("wee");
    m.remove("wei");
    m.remove("wil");
    m.remove("yea");
    m.remove("yow");
    m.remove("zim");
  }

  private void addCustomizations(Map m)
  {
    m.put("_a", "ey1 | dt"); // this is so ugly (why?)
    m.put("a", "ey1 | dt"); // to be consistent with^
    m.put("first", "f-er1-s-t | jj");
    m.put("zero", "z-ih1-r ow | jj nn");
    m.put("one", "w-ah1-n | jj nn");
    m.put("two", "t-uw1 | jj nn");
    m.put("three", "t-iy1 | jj nn");
    m.put("four", "f-ao1-r | jj nn");
    m.put("five", "f-ay1-v | jj nn");
    m.put("six", "s-ih1-k-s | jj nn");
    m.put("seven", "s-eh1-v ax-n | jj nn");
    m.put("eight", "ey1-t | jj nn");
    m.put("nine", "f-ay1-n | jj nn");
    m.put("ten", "t-ay1-n | jj nn");
    m.put("jumps", "jh-ah-m-p-s | vbz nns");
    // ... more?
  }

  public static void main(String[] args)
  {

    // test(); if (1==1) return;
    String test = "1";
    test = "a";
    // RiSpeech rs = new RiSpeech();

    RiLexiconImpl lex = RiLexiconImpl.getInstance();

    // lex.preloadFeatures();
    /*
     * System.out.println(lex.lookupRaw(test));
     * System.out.println(lex.getPhonemes(test, true));
     * System.out.println(RiTa.asList(lex.getPhonemeArr(test, true)));
     */
    // rs.speak(test);

  }

}// end
