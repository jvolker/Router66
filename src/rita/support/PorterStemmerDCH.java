package rita.support;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import processing.core.PApplet;
import rita.RiObject;
import rita.support.ifs.RiStemmerIF;

// TODO: test with word-list (+stems) from here:
// http://snowball.tartarus.org/algorithms/porter/diffs.txt

/**
 * A simple stemmer for extracting base roots from a word by removing 
 * prefixes and suffixes. For example, the words 'run', 'runs', 'ran', 
 * and 'running' all have "run" as the root.
 * <pre>
    String[] tests = { "run", "runs", "ran", "running" };
    RiStemmer stem = new RiStemmer(this);
    for (int i = 0; i < tests.length; i++)
      System.out.println(stem.stem(tests[i]));
 * </pre>
 * 
 * Based on Martin Porter's stemmer algorithm detailed here:
 *   http://tartarus.org/~martin/PorterStemmer/
 */
public class PorterStemmerDCH extends RiObject implements RiStemmerIF
{
  private static boolean DBUG = false;
  
  protected static Map cache;
  protected static boolean cacheEnabled = true;
  
  private String input;
  private char[] buffer;
  
  // offset into buffer
  private int startOffset;
  
  // offset to end of stemmed word
  private int endOffset; 
  
  // start/end buffer indexes
  private int a, b;  
  
  /* unit of size whereby buffer is increased */
  private static final int INC = 50;
  
  
  static {
    cache = new HashMap();
    // some irregulars
    cache.put("ran", "run");
    cache.put("crisis", "crisis");
    cache.put("crises", "crisis");
    cache.put("corpora", "corpus");
/*    
    cache.put("memory", "memory");
    cache.put("memories", "memory");
    cache.put("allegory", "allegory");
    cache.put("allegories", "allegory");
    cache.put("analogies", "analogy");
    cache.put("analogy", "analogy");
    
    cache.put("ambulance", "ambulance");
    cache.put("ambulances", "ambulance");*/
  }
  
  /**
   * @invisible
   */
  public PorterStemmerDCH() {
    this(null);
  }
  
  public PorterStemmerDCH(PApplet pApplet) {
    super(pApplet);
    buffer = new char[INC];
    startOffset = 0;
    endOffset = 0;
  }

  /**
   * Returns whethe the cache is enabled
   * @invisible
   */
  public static boolean isCacheEnabled()
  {
    return cacheEnabled;
  }

  /**
   * Sets whether the cache is enabled and duplicate
   * requests are returned immediately rather than
   * re-computed (default=true).
   * @invisible
   */
  public static void setCacheEnabled(boolean enableCache)
  {
    cacheEnabled = enableCache;
  }
  
  /**
   * Add a character to the word being stemmed. When you are finished adding
   * characters, you can call stem(void) to stem the word.
   * @invisible
   */
  private void add(char ch)
  {
    if (startOffset == buffer.length)
    {
      char[] new_b = new char[startOffset + INC];
      for (int c = 0; c < startOffset; c++)
        new_b[c] = buffer[c];
      buffer = new_b;
    }
    buffer[startOffset++] = ch;
  }

  /**
   * Adds wLen characters to the word being stemmed contained in a portion of a
   * char[] array. This is like repeated calls of add(char ch), but faster.
   *  @invisible
   */
  private void add(char[] w, int wLen)
  {
    if (startOffset + wLen >= buffer.length)
    {
      char[] new_b = new char[startOffset + wLen + INC];
      for (int c = 0; c < startOffset; c++)
        new_b[c] = buffer[c];
      buffer = new_b;
    }
    for (int c = 0; c < wLen; c++)
      buffer[startOffset++] = w[c];
  }

  /**
   * After a word has been stemmed, it can be retrieved by toString(), or a
   * reference to the internal buffer can be retrieved by getResultBuffer and
   * getResultLength (which is generally more efficient.)
   * @invisible
   */
  private String resultToString()
  {
    return new String(buffer, 0, endOffset);
  }

  /**
   * Returns the length of the word resulting from the stemming process.
   * @invisible
   */
  private int getResultLength()
  {
    return endOffset;
  }

  /**
   * Returns a reference to a character buffer containing the results of the
   * stemming process. You also need to consult getResultLength() to determine
   * the length of the result.
   * @invisible
   */
  private char[] getResultBuffer()
  {
    return buffer;
  }

  /* cons(i) is true <=> b[i] is a consonant. */

  private final boolean isConsonant(int c)
  {
    switch (buffer[c])
    {
      case 'a':
      case 'e':
      case 'i':
      case 'o':
      case 'u':
        return false;
      case 'y':
        return (c == 0) ? true : !isConsonant(c - 1);
      default:
        return true;
    }
  }

  /*
   * m() measures the number of consonant sequences between 0 and j. if c is a
   * consonant sequence and v a vowel sequence, and <..> indicates arbitrary
   * presence,
   * 
   * <c><v> gives 0 <c>vc<v> gives 1 <c>vcvc<v> gives 2 <c>vcvcvc<v> gives 3
   * ....
   */
  private final int measure()
  {
    int n = 0;
    int x = 0;
    while (true)
    {
      if (x > a)
        return n;
      if (!isConsonant(x))
        break;
      x++;
    }
    x++;
    while (true)
    {
      while (true)
      {
        if (x > a)
          return n;
        if (isConsonant(x))
          break;
        x++;
      }
      x++;
      n++;
      while (true)
      {
        if (x > a)
          return n;
        if (!isConsonant(x))
          break;
        x++;
      }
      x++;
    }
  }

  /* vowelinstem() is true <=> 0,...j contains a vowel */

  private final boolean vowelInStem()
  {
    int x;
    for (x = 0; x <= a; x++)
      if (!isConsonant(x))
        return true;
    return false;
  }

  /* doublec(j) is true <=> j,(j-1) contain a double consonant. */

  private final boolean isDoubleConsonant(int x)
  {
    if (x < 1)
      return false;
    if (buffer[x] != buffer[x - 1])
      return false;
    return isConsonant(x);
  }

  /*
   * cvc(i) is true <=> i-2,i-1,i has the form consonant - vowel - consonant and
   * also if the second c is not w,x or y. this is used when trying to restore
   * an e at the end of a short word. e.g.
   * 
   * cav(e), lov(e), hop(e), crim(e), but snow, box, tray.
   * 
   */

  private final boolean isConsVowelCons(int x)
  {
    if (x < 2 || !isConsonant(x) || isConsonant(x - 1) || !isConsonant(x - 2))
      return false;
    {
      int ch = buffer[x];
      if (ch == 'w' || ch == 'x' || ch == 'y')
        return false;
    }
    return true;
  }

  private final boolean ends(String s)
  {
    int l = s.length();
    int o = b - l + 1;
    if (o < 0)
      return false;
    for (int x = 0; x < l; x++)
      if (buffer[o + x] != s.charAt(x))
        return false;
    a = b - l;
    return true;
  }

  /*
   * setto(s) sets (j+1),...k to the characters in the string s, readjusting k.
   */

  private final void setTo(String s)
  {
    int l = s.length();
    int o = a + 1;
    for (int x = 0; x < l; x++)
      buffer[o + x] = s.charAt(x);
    b = a + l;
  }

  /* r(s) is used further down. */
  private final void condSetTo(String s)
  {
    if (measure() > 0) setTo(s);
  }

  /*
   * step1() gets rid of plurals and -ed or -ing.
   * 
   * caresses -> caress   
   * ponies -> poni  
   * ties -> ti   
   * caress -> caress  
   * cats -> cat
   * feed -> feed 
   * agreed -> agree 
   * disabled -> disable
   * 
   * matting -> mat 
   * mating -> mate 
   * meeting -> meet 
   * milling -> mill 
   * messing -> mess
   */
  private final void step1()
  {
    if (buffer[b] == 's')
    {      
      if (ends("sses")) {
        b -= 2;
      }
      else if (ends("ies")) {
        setTo("i");;
      }
      else if (buffer[b - 1] != 's') {
        b--;
//System.out.println("RiStemmer2.step1.4() "+_j+"..."+_k);
      }
    }
    if (ends("eed"))
    {
      if (measure() > 0)
        b--;
    } 
    else if ((ends("ed") || ends("ing")) && vowelInStem())
    {
      b = a;
      if (ends("at"))
        setTo("ate");
      else if (ends("bl"))
        setTo("ble");
      else if (ends("iz"))
        setTo("ize");
      else if (isDoubleConsonant(b))
      {
        b--;
        {
          int ch = buffer[b];
          if (ch == 'l' || ch == 's' || ch == 'z')
            b++;
        }
      } else if (measure() == 1 && isConsVowelCons(b))
        setTo("e");
    }
  }

  /* step2() turns terminal y to i when there is another vowel in the stem. */
  
  private final void step2()
  {
    if (ends("y") && vowelInStem())
      buffer[b] = 'i';
  }
  /*
   * Problems:
   *   Memory,allegory, analogy etc.
   */

  /*
   * step3() maps double suffixes to single ones. so -ization ( = -ize plus
   * -ation) maps to -ize etc. note that the string before the suffix must give
   * m() > 0.
   */
  private final void step3()
  {
    if (b == 0) return; /* For Bug 1 */
    
    switch (buffer[b - 1])
    {
      case 'a':
        if (ends("ational"))
        {
          condSetTo("ate");
          break;
        }
        if (ends("tional"))
        {
          condSetTo("tion");
          break;
        }
        break;
      case 'c':
        if (ends("enci"))
        {
          condSetTo("ence");
          break;
        }
        if (ends("anci"))
        {
          condSetTo("ance");
          break;
        }
        break;
      case 'e':
        if (ends("izer"))
        {
          condSetTo("ize");
          break;
        }
        break;
      case 'l':
        if (ends("bli"))
        {
          condSetTo("ble");
          break;
        }
        if (ends("alli"))
        {
          condSetTo("al");
          break;
        }
        if (ends("entli"))
        {
          condSetTo("ent");
          break;
        }
        if (ends("eli"))
        {
          condSetTo("e");
          break;
        }
        if (ends("ousli"))
        {
          condSetTo("ous");
          break;
        }
        break;
      case 'o':
        if (ends("ization"))
        {
          condSetTo("ize");
          break;
        }
        if (ends("ation"))
        {
          condSetTo("ate");
          break;
        }
        if (ends("ator"))
        {
          condSetTo("ate");
          break;
        }
        break;
      case 's':
        if (ends("alism"))
        {
          condSetTo("al");
          break;
        }
        if (ends("iveness"))
        {
          condSetTo("ive");
          break;
        }
        if (ends("fulness"))
        {
          condSetTo("ful");
          break;
        }
        if (ends("ousness"))
        {
          condSetTo("ous");
          break;
        }
        break;
      case 't':
        if (ends("aliti"))
        {
          condSetTo("al");
          break;
        }
        if (ends("iviti"))
        {
          condSetTo("ive");
          break;
        }
        if (ends("biliti"))
        {
          condSetTo("ble");
          break;
        }
        break;
      case 'g':
        if (ends("logi"))
        {
          condSetTo("log");
          break;
        }
    }
  }

  /* step4() deals with -ic-, -full, -ness etc. similar strategy to step3. */

  private final void step4()
  {
    switch (buffer[b])
    {
      case 'e':
        if (ends("icate"))
        {
          condSetTo("ic");
          break;
        }
        if (ends("ative"))
        {
          condSetTo("");
          break;
        }
        if (ends("alize"))
        {
          condSetTo("al");
          break;
        }
        break;
      case 'i':
        if (ends("iciti"))
        {
          condSetTo("ic");
          break;
        }
        break;
      case 'l':
        if (ends("ical"))
        {
          condSetTo("ic");
          break;
        }
        if (ends("ful"))
        {
          condSetTo("");
          break;
        }
        break;
      case 's':
        if (ends("ness"))
        {
          condSetTo("");
          break;
        }
        break;
    }
  }

  /* step5() takes off -ant, -ence etc., in context <c>vcvc<v>. */

  private final void step5()
  {
    if (b == 0) return; 
    
    switch (buffer[b - 1])
    {
      case 'a':
        if (ends("al"))
          break;
        return;
      case 'c':
        if (ends("ance"))
          break;
        if (ends("ence"))
          break;
        return;
      case 'e':
        if (ends("er"))
          break;
        return;
      case 'i':
        if (ends("ic"))
          break;
        return;
      case 'l':
        if (ends("able"))
          break;
        if (ends("ible"))
          break;
        return;
      case 'n':
        if (ends("ant"))
          break;
        if (ends("ement"))
          break;
        if (ends("ment"))
          break;
        /* element etc. not stripped before the m */
        if (ends("ent"))
          break;
        return;
      case 'o':
        if (ends("ion") && a >= 0 && (buffer[a] == 's' || buffer[a] == 't'))
          break;
        /* j >= 0 fixes Bug 2 */
        if (ends("ou"))
          break;
        return;
        /* takes care of -ous */
      case 's':
        if (ends("ism"))
          break;
        return;
      case 't':
        if (ends("ate")) {
          System.out.println("ate -> "+resultToString());          
          break;
        }
        if (ends("iti"))
          break;
        return;
      case 'u':
        if (ends("ous"))
          break;
        return;
      case 'v':
        if (ends("ive"))
          break;
        return;
      case 'z':
        if (ends("ize"))
          break;
        return;
      default:
        return;
    }
    if (measure() > 1)
      b = a;
  }

  /* step6() removes a final -e if measure() > 1. */

  private final void step6()  // example?
  {
    a = b;
    if (buffer[b] == 'e')
    {
      int a = measure();
      if (a > 1 || (a == 1 && !isConsVowelCons(b - 1)))
        b--;
    }
    if (buffer[b] == 'l' && isDoubleConsonant(b) && measure() > 1)
      b--;
  }

  /**
   * Stem the word placed into the Stemmer buffer through calls to add().
   * You can retrieve the result with getResultLength()/getResultBuffer()
   * or toString().
   */
  private void stem()
  {    
    //System.out.println("Start: "+input);
    
    if (input.endsWith("ate")) { // added: dch
      this.buffer = this.input.toCharArray();
      endOffset = input.length();
      startOffset = 0;
      return;
    }    
    
    b = startOffset - 1;
    if (b > 1)
    {
      step1();      
      step2();
      step3();
      step4();
      step5();      
      step6();      
    }    
    endOffset = b + 1;
    startOffset = 0;
    //System.out.println("  "+resultToString());
  }

  /**
   * Test program for demonstrating the Stemmer. It reads text from a a list of
   * files, stems each word, and writes the result to standard output. Note that
   * the word stemmed is expected to be in lower case: forcing lower case must
   * be done outside the Stemmer class. Usage: Stemmer file-name file-name ...
   * @invisible 
   */
  private static void test(String[] files)
  {
    char[] w = new char[501];
    PorterStemmerDCH s = new PorterStemmerDCH();
    for (int i = 0; i < files.length; i++)
      try
      {
        FileInputStream in = new FileInputStream(files[i]);
        try
        {
          while (true)

          {
            int ch = in.read();
            if (Character.isLetter((char) ch))
            {
              int j = 0;
              while (true)
              {
                ch = Character.toLowerCase((char) ch);
                w[j] = (char) ch;
                if (j < 500)
                  j++;
                ch = in.read();
                if (!Character.isLetter((char) ch))
                {
                  /* to test add(char ch) */
                  for (int c = 0; c < j; c++)
                    s.add(w[c]);

                  /* or, to test add(char[] w, int j) */
                  /* s.add(w, j); */

                  s.stem();
                  {
                    String u;

                    /* and now, to test toString() : */
                    u = s.toString();

                    /* to test getResultBuffer(), getResultLength() : */
                    /*
                     * u = new String(s.getResultBuffer(), 0,
                     * s.getResultLength());
                     */

                    System.out.print(u);
                  }
                  break;
                }
              }
            }
            if (ch < 0)
              break;
            System.out.print((char) ch);
          }
        }
        catch (IOException e)
        {
          System.out.println("error reading " + files[i]);
          break;
        }
      }
      catch (FileNotFoundException e)
      {
        System.out.println("file " + files[i] + " not found");
        break;
      }
  }  
  
  /**
   * The basic stemming method: extracts base roots from a word by removing prefixes and suffixes. 
   * For example, the words 'run', 'runs', 'ran', and 'running' all have "run" as the root.
   * @param toStem 
   */
  public String stem(String toStem)
  {
    this.input = toStem.toLowerCase();
    
    // check the cache no matter what (for irregulars)
    String result = (String)cache.get(input);
    
    if (result == null || result.length()<1) 
    {
      char[] chars = this.input.toCharArray();
      this.add(chars, chars.length);
      this.stem();    
      result = resultToString();
      
      // added - dch
      if (input.equals(result + 'e'))
        result = input;
    }
    
    if (cacheEnabled) { 
      cache.put(input, result);
      if (DBUG)System.err.println("STEMMED: "+input+" -> "+result);
    }
    
    return result;
  }
  
  protected String clean(String str)
  {
    int last = str.length();
    String temp = "";
    for (int x = 0; x < last; x++) {
      if (Character.isLetterOrDigit(str.charAt(x)))
        temp += str.charAt(x);
    }
    return temp;
  }
  
  protected String testStem(String str) {
    String key = /*clean*/(str.toLowerCase());
    String val = stem(key);
    System.out.println("lookup.put(\""+key+"\", "+"\""+val+"\");");
    return val;
  }
  
  public static void main(String[] args)
  {
    String[] tests = { "run", "runs", "ran", "running" };
    //String[] tests = {"idles"};
    PorterStemmerDCH stem = new PorterStemmerDCH();
    for (int i = 0; i < tests.length; i++)
    {
      System.out.println(stem.stem(tests[i]));
    }
    //if (1==1) return;
    stem.testStem("locomote");
    stem.testStem("idle");  
    stem.testStem("juvenile");
    stem.testStem("ingenue");
    stem.testStem("service");
    stem.testStem("creature");
    stem.testStem("device");
    stem.testStem("lagerphone");      
    stem.testStem("force");
    stem.testStem("desire");
    stem.testStem("province");
    stem.testStem("signalise");
    stem.testStem("formulate");
    stem.testStem("cognise");
    stem.testStem("communicate");
    stem.testStem("tangle");
    stem.testStem("motorcycle");
    stem.testStem("synchronise");
    stem.testStem("admeasure");
    stem.testStem("gauge");
    stem.testStem("intertwine");
    stem.testStem("precede");
    stem.testStem("situate");
    stem.testStem("automobile");
    stem.testStem("enumerate");
    stem.testStem("determine");
    stem.testStem("disagree");
    stem.testStem("agree");
    stem.testStem("mobile");
    stem.testStem("machine");
    stem.testStem("locate");
    stem.testStem("hearse");
    stem.testStem("translate");
    stem.testStem("endure");
    stem.testStem("secure");
    stem.testStem("straddle");
    stem.testStem("desire");
    stem.testStem("populate");
    stem.testStem("cringle");
    stem.testStem("corroborate");
    stem.testStem("substantiate");
  }

}// end
