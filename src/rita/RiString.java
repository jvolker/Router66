package rita;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.regex.Pattern;

import org.omg.CORBA._PolicyStub;

import processing.core.PApplet;

import rita.support.*;

/** 
 * RiTa's version of the Java String object (both implement 
 * the CharSequence interface) with support for 'features';
 * key-value pairs that contain additional information about
 * the object. For example, you can add part-of-speech, phonemes, 
 * syllables, and stress features to a RiString object as follows:
    <pre>    RiString rs = new RiString("The dog was white");
    RiAnalyzer ra = new RiAnalyzer();
    ra.analyze(rs);
    
    System.out.println(rs.getFeature("syllables"));
    System.out.println(rs.getFeature("phonemes"));    
    System.out.println(rs.getFeature("stresses"));
    System.out.println(rs.getFeature("pos"));</pre>
 */
public class RiString extends Featured implements RiCharSequence
{  
  protected String delegate = "";  // null?
   
  /**
   * Included only for consistency's sake
   * @invisible 
   */
  public RiString(PApplet p, CharSequence s)  { 
    this(s);
  }
  
  public RiString(CharSequence s) 
  { 
    if (s != null)
      this.delegate = s.toString();    
    else
      if (!RiTa.SILENT) System.out.println("[WARN] Null value passed  "  +
        "to 'new RiString()', converted to empty String");
  }
  
  
  public static void delete(RiString text)
  {
    if (text != null) {
      if (text.features!=null)
        text.features.clear();
    }
  }
  
  // Methods ==================================================
  
  /**
   * Sets the current text to this String
   */
  public void setText(String _text) {
    this.delegate = _text;
  }   
  
/*  
  public static RiString copy(RiString rs) {
    RiString rs2 = new RiString(rs.getText());
    rs.copyFeaturesInto(rs2);
    return rs2;
  }*/

  /**
   * Returns the index of the first token matching <code>word</code>
   * or -1 if not found
   */
  public int firstIndexOf(String word)
  {
    String[] words = getWords();
    for (int k = 0; k < words.length; k++)
    {
      String tok = (String)words[k];
      if (tok.equals(word))
        return k;
    }
    return -1;
  }


  /**
   * Returns the # of words in the object
   * according to the default WordTokenizer.
   * @see #getWords()
   */
  public int getWordCount()
  {
    return getWords().length;
  }

   /* using a call to this.split(" ").<p> Note: this may not
   * give the same result as , which
   * uses the default word tokenizer instead of a split() call. */
  
  /**
   * Returns the array of words in the object,
   * via a call to RiTa.tokenize().
   * @see RiTa#tokenize(String)
   */
  public String[] getWords()
  {
    return RiTa.tokenize(delegate);
  }
  
  /**
   * Inserts <code>newWord</code> at <code>wordIdx</code>
   * and shifts each subsequent word accordingly. Returns true
   * if the replace was succesful, or false if the index does not exist. 
   */
  public boolean insertWordAt(String newWord, int wordIdx)
  {
    String[] words = getWords();
    if (wordIdx < 0 || wordIdx >= words.length)
      return false;
    words[wordIdx] = newWord;
    setText(RiTa.join(words));
    return true;
  }

  /**
   * Replaces the word at <code>wordIdx</code>
   * with <code>newWord</code>. Returns true
   * if the replace was succesful, or false
   * if the index does not exist. 
   */
  public boolean replaceWordAt(String newWord, int wordIdx)
  {
    String[] words = getWords();
    if (wordIdx < 0 || wordIdx >= words.length)
      return false;
    words[wordIdx] = newWord;
    setText(RiTa.join(words));
    return true;
  }  


  /**
   * Utility method to do regex replacement on a String
   * @param patternStr regex
   * @param fullStr String to check
   * @param replaceStr String to insert
   * @see Pattern 
   */
  public static String regexReplace(String patternStr, String fullStr, String replaceStr)
  {
    return Regex.getInstance().replace(patternStr, fullStr, replaceStr);
  }
  
  /**
   * Utility method to test whether a String partially matches a regex pattern.
   * @param patternStr regex String
   * @param fullStr String to check
   * @see Pattern 
   */
  public static boolean regexMatch(String patternStr, String fullStr)
  {
    return Regex.getInstance().test(patternStr, fullStr);
  } 

  /**
   * Randomly chooses from all words matching <code>pos</code>
   * and replaces a random one with <code>newWord</code>. 
   * Returns the replaced word, or null if if no substitution 
   * could be made.
   * @invisible
   */
  public String replaceByPos(String newWord, String pos) {
    List l = null;
    String[] allpos = getPosArr(false);
    String[] words = getWords();
    if (allpos == null || allpos.length < 1) return null;
    for (int i = 0; i < allpos.length; i++) {
      if (words[i].equals(newWord)) continue;
       if (allpos[i].equalsIgnoreCase(pos)) {
         if (l == null)
           l = new LinkedList();
         l.add(new Integer(i));
       }
    }
    if (l == null || l.size() < 1)
      return null;
    Integer rand = (Integer)l.get((int)(Math.random()*l.size()));
    int wordIdx = rand.intValue();
    String old = getWordAt(wordIdx); 
    if (replaceWordAt(newWord, wordIdx))
      return old;
    return null;
  }
  
  /**
   * Returns the word at <code>wordIdx</code> using the default WordTokenizer. 
   */
  public String getWordAt(int wordIdx) {
    String[] words = RiTa.tokenize(delegate.toString());
    if (words == null || words.length==0 || wordIdx >= words.length)
      return null;
    return words[wordIdx];
  }

  /**
   * Returns the part-of-speech at <code>wordIdx</code> using the default 
   * WordTokenizer & PosParser... 
   */
  public String getPosAt(int wordIdx) {
    return getPosAt(wordIdx, false);
  }
  
  /**
   * Returns the part-of-speech at <code>wordIdx</code> using the default 
   * WordTokenizer & PosParser... 
   */
  public String getPosAt(int wordIdx, boolean useWordNetTags) {
    String[] pos = getPosArr(useWordNetTags);
    if (pos == null || pos.length==0 || wordIdx >= pos.length)
      return null;
    return pos[wordIdx];
  }
  
  /**
   * Returns the part-of-speech for either the whole phrase or 
   * the first instance of <code>word</code> in the text, using 
   * the default WordTokenizer & PosParser, or null if the 
   * <code>word</code> is not found.    
  public String getPos(String word) {
    int i = firstIndexOf(word);
    if (i < 0) return null;
    return getPosAt(i);
  }*/
  
  /**
   * Returns an array of part-of-speech tags, one per word, using the default 
   * WordTokenizer & PosParser... 
   */
  public String[] getPosArr(boolean useWordNetTags) {
    // or just create a Phrase?
    if (hasFeature(POS)) 
      return getFeature(POS).split(WORD_BOUNDARY);
    String[] words = RiTa.tokenize(delegate.toString());
    String[] tag =  RiTa.posTag(words);
    for (int i = 0; i < tag.length; i++) {
      if (tag[i] == null)// || tag[i].equals(Pos.UNKNOWN.getTag()));
        throw new RiTaException("Unable to parse pos for word: " + words[i]);
      if (useWordNetTags)
       tag[i] = RiPosTagger.toWordNet(tag[i]);  
    }
    return tag;
  } 
  
  /**
   * Returns an String of part-of-speech tags, 1 per word, using the default 
   * WordTokenizer & PosParser, delimited by <code>delim</code>.
   * @invisible
   */
  public String getPos(String delim, boolean useWordNetTags) {
    return RiTa.join(getPosArr(useWordNetTags), delim);
  }
  
  /**
   * Returns an String of part-of-speech tags, 1 per word, using the default 
   * WordTokenizer & PosParser, delimited by <code>delim</code>.
   * @invisible
   */
  public String getPos(String delim){
    return getPos(delim, false);
  }
  
  /**
   * Returns an String of part-of-speech tags, 1 per word, using the default 
   * WordTokenizer & PosParser, delimited by spaces.
   */
  public String getPos() {
    return getPos(false);
  }
  
  
  public String getPos(boolean useWordNetTags) {
    return RiTa.join(getPosArr(useWordNetTags), Featured.WORD_BOUNDARY);
  }

  /**
   * Replaces all instances of <code>oldText</code> with 
   * <code>newText</code> in the object. 
   */
  public void replace(String oldText, String newText) {
     String txt = getText();
     txt = txt.replaceAll(oldText, newText);
     setText(txt);
  }

    /**
   * Splits the RiString as per <code>String.split()</code>, then checks for 
   * any features with the same number of elements as the resulting String[] and adds 
   * the appropriate feature to each newly created RiString in the array.<br>
   * Example: <br>
   *   'only a handful of responses' / {chunk=noun-phrase}, {pos=rb dt nn in nns} ->
   *   <ul> 
   *   <li>  'only' / {pos=rb}
   *   <li>  'a' / {pos=dt}
   *   <li>  'handful' / {pos=nn}
   *   <li>  'of' / {pos=in}
   *   <li>  'responses' / {pos=nns}
   *   </ul>  
   * @see RiConstants#WORD_BOUNDARY
   */
  public RiString[] split()
  {
    return this.split(RiTa.SPC);
  }
  
  /**
   * Splits the RiString as per <code>String.split(regex)</code>, then checks for 
   * any features with the same number of elements as the resulting String[] and adds 
   * the appropriate feature to each individual RiString.<br>
   * Example: <br>
   *   'only a handful of responses' / {chunk=noun-phrase}, {pos=rb dt nn in nns} ->
   *   <ul> 
   *   <li>  'only' / {pos=rb}
   *   <li>  'a' / {pos=dt}
   *   <li>  'handful' / {pos=nn}
   *   <li>  'of' / {pos=in}
   *   <li>  'responses' / {pos=nns}
   *   </ul>  
   * @see RiConstants#WORD_BOUNDARY
   */
  public RiString[] split(String regex)
  {
    String[] s = delegate.split(regex);
    RiString[] fs = new RiString[s.length];
    for (int i = 0; i < fs.length; i++) {
      fs[i] = new RiString(s[i]);
//System.out.println("RiString.split() : "+fs[i].getFeature(ID));      
      for (Iterator iter = getAvailableFeatures().iterator(); iter.hasNext();) {
        String fkey = (String) iter.next();  
        String feature = getFeature(fkey);        
        // only add per-word features if they match in #
        if (feature.indexOf(WORD_BOUNDARY) > -1) 
        {
          String[] wordFeatures = feature.split(WORD_BOUNDARY);
          if (wordFeatures.length == fs.length) 
             fs[i].addFeature(fkey, wordFeatures[i]);          
        }
      }
    }
    return fs;
  }  

  public void setString(String newWord)
  {
    this.delegate = newWord; 
  }
  
  /*
   * TODO: 
   *   money: handle {around, $, 22} as "around $22" instead of "around$ 22"
   *   contractions: handle "is n't" as "isn't" or 'is not"
   */
  public void setString(CharSequence[] words)
  {
    // sanity check..
    if (words == null || words.length == 0)
      throw new RuntimeException("Illegal argument: "+words);

    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < words.length; i++) 
    {
      if (words[i] == null) 
        throw new RuntimeException("Null word: idx="+i);
      
      buf.append(words[i].toString());

      if ((i + 1 < words.length && (words[i + 1] != null) 
        && words[i + 1].length() == 1)) 
      {
        // no space before punctuation
        if (isPunct(words[i + 1])) {
          buf.append(words[i + 1]);
          ++i; // no space after hyphen
          if (i + 1 < words.length && words[i].equals("-")) {
            buf.append(words[i + 1]);
            i++;
          }
        }
      }
      buf.append(RiTa.SPC);
    }

    setString(buf.toString().trim());
  }
  
  private boolean isPunct(CharSequence full) {
    if (full == null || full.length() > 1) return false;        
    int idx = (RiTa.PUNCTUATION.indexOf(full.charAt(0)));        
    return idx > -1;
  }
  
  
  public FeaturedIF join(RiString[] fstrs, String regex)
  {
    // handle the features here too (or probably just clear?) 
    if (1==1) throw new RuntimeException("NOT YET IMPLEMENTED!");
    return null;    
  }

  public void dump() {
    System.out.println(toString()+" / "+getFeatures());    
  }  
  
  /**
   * Creates and returns a copy of <code>riString</code>
   */
  public static RiString copy(RiString riString)
  {
    RiString rs2 = new RiString(riString.toString());
    for (Iterator i = riString.getFeatures().keySet().iterator(); i.hasNext();) {
        String key = (String) i.next();
      rs2.addFeature(key, riString.getFeature(key));      
    }
    return rs2;     
  }
  
  /**
   * Creates and returns a copy of this object
   */
  public RiString copy() 
  { 
    return copy(this);
  }
    
  public static RiString[] fromStrings(String[] s) 
  { 
    RiString[] result = new RiString[s.length];
    for (int i = 0; i < s.length; i++) 
      result[i] = new RiString(s[i]);
    return result;
  }
  
  public static RiString[] fromCollection(Collection c) 
  { 
    int idx = 0;
    RiString[] result = new RiString[c.size()];
    for (Iterator i = c.iterator(); i.hasNext();) { 
      Object o = i.next();
      if (!(o instanceof CharSequence))
        throw new RiTaException("Illegal type: "+o.getClass());       
      result[idx++] = new RiString(o==null?null:o.toString());
    }
    return result;
  }

  // delegate methods -- auto-generated
  
  public char charAt(int index)
  {
    return this.delegate.charAt(index);
  }

  public int compareTo(Object arg0)
  {
    return this.delegate.compareTo((String)arg0);
  }

  public int compareTo(String anotherString)
  {
    return this.delegate.compareTo(anotherString);
  }

  public int compareToIgnoreCase(String str)
  {
    return this.delegate.compareToIgnoreCase(str);
  }

  public String concat(String str)
  {
    setText(this.delegate.concat(str));
    return getText();
  }

  public boolean contains(CharSequence s)
  {
    if (s == null) return false;
    return this.delegate.indexOf(s.toString())>-1;
  }

  public boolean contentEquals(StringBuilder sb)
  {
    return this.delegate.contentEquals(sb);
  }

  public boolean endsWith(String suffix)
  {
    return this.delegate.endsWith(suffix);
  }

  public boolean equals(Object anObject)
  {
    if (delegate == null) return false;
    return this.delegate.equals(anObject);
  }

  public boolean equalsIgnoreCase(String anotherString)
  {
    if (delegate == null) return false;
    return this.delegate.equalsIgnoreCase(anotherString);
  }

  public byte[] getBytes()
  {
    return this.delegate.getBytes();
  }

  public byte[] getBytes(String charsetName) throws UnsupportedEncodingException
  {
    return this.delegate.getBytes(charsetName);
  }

  public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin)
  {
    this.delegate.getChars(srcBegin, srcEnd, dst, dstBegin);
  }

  public int hashCode()
  {
    return this.delegate.hashCode();
  }

  public int indexOf(int ch, int fromIndex)
  {
    return this.delegate.indexOf(ch, fromIndex);
  }

  public int indexOf(int ch)
  {
    return this.delegate.indexOf(ch);
  }

  public int indexOf(String str, int fromIndex)
  {
    return this.delegate.indexOf(str, fromIndex);
  }

  public int indexOf(String str)
  {
    return this.delegate.indexOf(str);
  }

  public String intern()
  {
    return this.delegate.intern();
  }

  public int lastIndexOf(int ch, int fromIndex)
  {
    return this.delegate.lastIndexOf(ch, fromIndex);
  }

  public int lastIndexOf(int ch)
  {
    return this.delegate.lastIndexOf(ch);
  }

  public int lastIndexOf(String str, int fromIndex)
  {
    return this.delegate.lastIndexOf(str, fromIndex);
  }

  public int lastIndexOf(String str)
  {
    return this.delegate.lastIndexOf(str);
  }

  public int length()
  {    
    return delegate == null ? 0 : delegate.length();
  }

  public boolean matches(String regex)
  {
    return this.delegate.matches(regex);
  }

  public boolean regionMatches(boolean ignoreCase, int toffset, String other, int ooffset, int len)
  {
    return this.delegate.regionMatches(ignoreCase, toffset, other, ooffset, len);
  }

  public boolean regionMatches(int toffset, String other, int ooffset, int len)
  {
    return this.delegate.regionMatches(toffset, other, ooffset, len);
  }

  public String replace(char oldChar, char newChar)
  {
    setText(this.delegate.replace(oldChar, newChar));
    return getText();
  }

  public String replace(CharSequence target, CharSequence replacement)
  {
    setText(this.delegate.replaceAll
      (target.toString(), replacement.toString()));
    return getText();
  }

  public String replaceAll(String regex, String replacement)
  {
    if (delegate == null) return null;
    String replaced = this.delegate.replaceAll(regex, replacement);
    setText(replaced);
    return getText();
  }

  public String replaceFirst(String regex, String replacement)
  {
    setText(this.delegate.replaceFirst(regex, replacement));
    return getText();
  }
  
  /**
   * Replaces the character at 'idx' with 'replaceWith'.
   * If the specified 'idx' is less than xero, or beyond 
   * the length of the current text, there will be no effect.
   * Returns true if the replacement was made  
   */
  public boolean replaceCharAt(int idx, String replaceWith)
  {   
    if (idx < 0 || idx >= length()) 
      return false;
    
    String s = getText();     
    String beg = s.substring(0, idx);
    String end = s.substring(idx+1);
    String s2 = null;
    if (replaceWith != null) 
      s2 = beg + replaceWith + end;
    else
      s2 = beg + end;
    
    if (s2.equals(s)) // no change
      return false;
    
    setText(s2);
    
    return true;
  }

  public boolean startsWith(String prefix, int toffset)
  {
    return this.delegate.startsWith(prefix, toffset);
  }

  public boolean startsWith(String prefix)
  {
    return this.delegate.startsWith(prefix);
  }

  public CharSequence subSequence(int beginIndex, int endIndex)
  {
    setText((String)this.delegate.subSequence(beginIndex, endIndex));
    return getText();
  }

  public String substring(int beginIndex, int endIndex)
  {
    setText(this.delegate.substring(beginIndex, endIndex));
    return getText();
  }

  public String substring(int beginIndex)
  {
    setText( this.delegate.substring(beginIndex));
    return getText();
  }

  public char[] toCharArray()
  {
    return this.delegate.toCharArray();
  }

  public String toLowerCase()
  {
    setText(this.delegate.toLowerCase());
    return getText();
  }

  public String toLowerCase(Locale locale)
  {
    setText( this.delegate.toLowerCase(locale));
        return getText();
  }

  public String toString()
  {
    return this.delegate == null ? "" : delegate;
  }

  public String getText()
  {
    return this.delegate;
  }
  
  public String toUpperCase()
  {
    setText( this.delegate.toUpperCase());
        return getText();
  }

  public String toUpperCase(Locale locale)
  {
    setText( this.delegate.toUpperCase(locale));
        return getText();
  }

  public String trim()
  {
    setText(this.delegate.trim());
        return getText();
  }

  public static void main(String[] args)
  {
    RiString rs = new RiString("the white rabbit");
    System.out.println(rs.getAvailableFeatures());
    System.out.println(rs.getId());
    System.out.println(rs.getFeature(ID));   
    System.out.println(rs.getFeatures());    
    RiAnalyzer ra = new RiAnalyzer();
    ra.analyze(rs);
    System.out.println(rs+": "+rs.getFeatures());
    RiString[] rss = rs.split();
    for (int i = 0; i < rss.length; i++) {
      System.out.println(rss[i]+": "+rss[i].getFeatures());
    }
    rs.clearFeatures();
    //rss = rs.split();
    System.out.println(rs+": "+rs.getFeatures());
    for (int i = 0; i < rss.length; i++) {
      rss[i].clearFeatures();
      System.out.println(rss[i]+": "+rss[i].getFeatures());
    }
    //RiChunker rc = new RiChunker();
    //rc.chunk(listOfTokens, listOfTags);
    //System.out.println("punct:"+rs.isPunct(","));    
/*  System.out.println(rs.getFeature(Featured.SYLLABLES));
    System.out.println(rs.getFeature(Featured.PHONEMES));    
    System.out.println(rs.getFeature(Featured.STRESSES));
    System.out.println(rs.getFeature(Featured.POS));*/
  }
  
}// end
