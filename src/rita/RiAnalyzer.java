package rita;

import java.util.*;

import processing.core.PApplet;
import rita.support.*;

/**
 * Analyzes String phrases, annotating each phrase and 
 * each contained word with 'feature' data. Default features 
 * include: word-boundaries, pos, phonemes, stresses, syllables, etc.
 * <p> 
 * <pre>
    String text = "The boy jumped over the wild dog.";
    RiAnalyzer ra = new RiAnalyzer(this);
    ra.analyze(text);
    
    String phonemes = ra.getPhonemes();   
    String stresses = ra.getStresses();
    String syllables = ra.getSyllables();
    String partsOfSpeech = ra.getPos();</pre>
        
    Note: RiString (and RiText) objects can also be analyzed:<pre>
      RiString rt = new RiString("The boy ran to the store.");
      ra.analyze(rt);
      String phonemes = rt.getPhonemes();</pre>
    
    And additional (custom) features can be added by the user    
    by creating a subclass and overriding the analyze method as
    follows:<pre>
      RiAnalyzer ra = new RiAnalyzer(this) {
        public void analyze(RiText rt) {
          super.analyze(rt);
          // add custom features here
          rt.addFeature("featureName", "featureValue");
        }
      };</pre>
 *            
 * @author dhowe
 */
public class RiAnalyzer extends RiObject 
{
  /** @invisible                 */
  protected RiPhrase[] delegates;
  protected boolean cacheEnabled;
  protected int callCount; 
  protected Map cache; 
  
  /** Default constructor for RiAnalyzer */
  public RiAnalyzer(PApplet pApplet) 
  {
    this(pApplet, false);
  }
  
  /** @invisible                 */
  public RiAnalyzer(boolean enableCaching) 
  {
    this(null, enableCaching);
  }
  
  /** @invisible                 */
  public RiAnalyzer() 
  {
    this(null, false);
  }
  
  /**
   * Constructor for RiAnalyzer that specifies a specific
   * PosTagger type to use, e.g., RiPosTagger.MAXENT_POS_TAGGER
   * and a flag to indicate whether to enable the cache.
   */
  public RiAnalyzer(PApplet pApplet, int taggerType, boolean enableCaching) 
  {
    this(pApplet, (RiPhrase[])null, taggerType, enableCaching);    
  }

  /** @invisible                 */
  public RiAnalyzer(PApplet p, RiPhrase[] phrase, int taggerType, boolean enableCaching) 
  {   
    super(p);   
    RiPosTagger.setDefaultTagger(taggerType);
    this.delegates = phrase;
    this.cacheEnabled = enableCaching;
  }  
  
  /**
   * Constructor for RiAnalyzer with boolean specifying whether 
   * to enable the cache.
   */
  public RiAnalyzer(PApplet pApplet, boolean enableCaching) {
    this(pApplet, RiPosTagger.DEFAULT_POS_TAGGER, enableCaching);
  }

  
  // Methods ====================================================
  
  /**
   * Returns the number of non-cached lookups made by this object so far
   * @see #setCacheEnabled(boolean)
   */
  public int getCallCount()
  {
    return callCount;
  }
  
  /**
   * Returns the rhyme scheme for a given set of lines.<p>
   * Note: assumes all rhymes are end-rhymes, that is, happening 
   * on the last word of the lines.
   */
  public String rhymeScheme(String[] lines)
  {
    throw new RuntimeException("unimplemented");
  }
  
  /**
   * @invisible
   */
  public static void setDefaultTagger(int taggerType)
  {
    RiPosTagger.setDefaultTagger(taggerType);
  }
  
	protected static RiPhrase[] createDelegates(PApplet p, String phrase)
	{    
	  if (!phrase.contains(" "))  {// hack for single word
	    RiPhrase[] rp = { new RiPhrase(p, phrase) };
	    //System.out.println("rp: "+rp[0].getFeatures());
	    return rp;
	  }
	    
    String[] sentences = RiTa.splitSentences(phrase);
//System.out.println("SENT: "+asList(sentences));
    RiPhrase[] phrases = new RiPhrase[sentences.length];
    for (int i = 0; i < sentences.length; i++) 
      phrases[i] = new RiPhrase(p, sentences[i]);     
		return phrases;
	}

  /**
   * Returns the Set of available features  
   */
  public Set getAvailableFeatures()
  {
    return this.getFeatures(delegates).getAvailableFeatures();
  }

  /**
   * Returns the feature specified by <code>name</code>.<P>
   * Note: <code>getFeature("pos")</code> is equivalent to <code>getPos()</code>.   
   */
  public String getFeature(String featureName)
  {
    return getFeatures(delegates).getFeature(featureName);
  }

  /**
   * Returns a String containing all phonemes for the input text, 
   * delimited by semi-colons, e.g., "dh:ax:d:ao:g:r:ae:n:f:ae:s:t",
   * or null if no text has been input.
   */
  public String getPhonemes()
  {
    return getFeatures(delegates).getFeature(Featured.PHONEMES);
  }
  
  /**
   * Returns the pos for the word at <code>wordIdx</code>
   * @param wordIdx
   */
  public String getPosAt(int wordIdx)
  {
    return getPos().split(Featured.WORD_BOUNDARY)[wordIdx];
  }
  
  /**
   * Returns the phonemes for the word at <code>wordIdx</code>
   * @param wordIdx
   */
  public String getPhonemesAt(int wordIdx)
  {
    return getPhonemes().split(Featured.WORD_BOUNDARY)[wordIdx];
  }     
  
  /**
   * Returns the stresses for the word at <code>wordIdx</code>
   * @param wordIdx
   */
  public String getStressesAt(int wordIdx)
  {
    return getStresses().split(Featured.WORD_BOUNDARY)[wordIdx];
  }   
  
  /**
   * Returns the word at index <code>wordIdx</code>
   * @param wordIdx
   */
  public String tokenAt(int wordIdx)
  {
    String[] toks = getTokens();
    return wordIdx >= toks.length ? null : toks[wordIdx];
  }   
  
  /**
   * Returns a Map (of String key-value pairs) of all the features
   * for the word at the specified word-index. 
   * @param wordIdx 
   */
  public Map getFeatures(int wordIdx)
  {
    Map m = getFeatures(), wordMap = new HashMap(); 
    for (Iterator i = m.keySet().iterator(); i.hasNext();)
    {
      String key = (String) i.next();
      String val = (String)m.get(key);
      String[] vals = val.split(Featured.WORD_BOUNDARY);
      if (vals == null || vals.length == 0) 
        continue;
      //System.out.println("VAL0: "+vals[0]);
      try
      {
        wordMap.put(key, (vals.length==1) ? val : vals[wordIdx]);
      }
      catch (RuntimeException e)
      {
        System.err.println("FAILED (wordIdx="+wordIdx+") "+RiTa.asList(delegates)+" "+e.getMessage());
        e.printStackTrace();
      }     
    }
    return wordMap;
  }

  private RiPhrase delegateForIdx(int wordIdx) 
  {
    if (wordIdx < 0 || wordIdx > wordCount()-1)
      throw new RiTaException("Index out of bounds: "+wordIdx);
    
    int maxLen = delegates[0].numWords();
    for (int i = 1; i < delegates.length; i++)
    {
      if (wordIdx < maxLen) 
        return delegates[i-1];
      maxLen += delegates[i].length();
    }
    return delegates[delegates.length-1];
  }
    
  /**
   * Returns a String containing all pos tags for the input text, 
   * delimited by semi-colons, e.g., "dt:nn:vbd:rb",
   * or null if no text has been input.
   */
  public String getPos()
  {
    return getFeatures(delegates).getFeature(Featured.POS);
  }
  
  /**
   * Returns a String representing the pos 
   * for the first index of <code>word</code> in the current text,
   * or null if there is no such word.
   * @param word
   * @see #wordIdx(String)
  public String getPos(String word)
  {
    if (delegates == null)
      delegates = createDelegates(_pApplet, word);
    return getPos()
  }   */  
  
  /**
   * Returns a String containing the stresses for each syllable of the input text, 
   * delimited by semi-colons, e.g., "0:1:0:1", with 1's meaning
   * 'stressed', and 0's meaning 'unstressed', or null if no text has been input.
   */
  public String getStresses()
  {
    return getFeatures(delegates).getFeature(Featured.STRESSES);
  }
  
  /**
   * Returns a String containing the phonemes for each syllable of each word 
   * of the input text, delimited by dashes for the first index of <code>word</code> 
   * in the current text,
   * or null if there is no such word.
   * @param word
   * @see #wordIdx(String)
   * @see #getSyllables()   
  public String getSyllables(String word)
  {
    return getFeatures(delegates).getFeature(Featured.STRESSES);
  }*/    
  
  /**
   * Returns a String containing the phonemes for each syllable of each word 
   * of the input text, delimited by dashes (phonemes) and semi-colons (words),
   * e.g., "dh-ax:d-ao-g:r-ae-n:f-ae-s-t" for the 4 syllables of the phrase 
   * 'The dog ran fast', or null if no text has been input.
   */
  public String getSyllables()
  {
    return getFeatures(delegates).getFeature(Featured.SYLLABLES);
  }

  /**
   * Returns an array of the words (no punctuation) in the current text,  
   * or null if no text has been input.
   */
  public String[] getTokens()
  {
    List l = new ArrayList();
    for (int i = 0; i < delegates.length; i++) {
      String[] toks = delegates[i].getTokenArray();
// XXXXXXXXXXXXXXX
      for (int j = 0; j < toks.length; j++)
        l.add(toks[j]);      
    }      
    return strArr(l);
  }

  /**
   * Returns a String containing each words in the current text, 
   * delimited by semi-colons (words) e.g., "The:dog:ran:fast", 
   * or null if no text has been input.

  public String getWords()
  {
    return getFeatures(delegates).getFeature(Featured.TOKENS);
  }   */


  /** 
   * Returns the # of words in the current text
   */
  public int wordCount()
  {
    return getTokens().length;
  } 

  /** 
   * Returns the (1st) index of <code>word</code>
   * or -1 if not found 
   */
  public int firstIdx(String word)
  {
    int idx = 0;
    for (int i = 0; i < delegates.length; i++)
    {
      String[] toks = delegates[i].getTokenArray();
      for (int j = 0; j < toks.length; j++)
      {
        if (toks[j].equals(word))
          return idx;
        idx++;
      }      
    }
    return -1; 
  }  
  
  /**
   * Returns the index of the last token matching <code>word</code>
   * or -1 if not found
   *
   */
  public int lastIdx(String word)
  {
    int result = -1;
    String[] toks = getTokens();
    for (int j = 0; j < toks.length; j++)
      if (toks[j].equals(word))
        result = j;  
    return result; 
  }     
  

  /** 
   * Returns the last analyzed text
   */
  public String getText()
  {
    return getFeatures(delegates).getText(); 
  }
  
  /**
   * @invisible
   * @deprecated
   * @see #analyze(String)
   */
  public void setText(String text)
  {
    this.analyze(text); 
  }    
  
  /**
   * Sets <code>text</code> as the current phrase 
   * and analyzes it, so that subsequent calls to 
   * methods like getPos(), getPhonemes(), getSyllables(),
   * etc. will return immediately.
   */
  public void analyze(String text)
  {
    if (cacheEnabled) {
      if (cache == null) cache = new HashMap();      
      this.delegates = (RiPhrase[])cache.get(text);    
      if (delegates != null) return;
    }    
    this.delegates = createDelegates(_pApplet, text);    
    if (cacheEnabled) cache.put(text, delegates);
    this.callCount++;
  } 
  
  /**
   * Sets <code>text</code> as the current phrase 
   * and analyzes it, so that subsequent calls to 
   * methods like getPos(), getPhonemes(), getSyllables(),
   * etc. will return immediately.
   */
  public void analyze(CharSequence text)
  {
    this.analyze(text.toString());
  }
  
  /**
   * Sets the text contained by <code>rt</code> as the current phrase 
   * and analyzes it, so that subsequent calls to methods like getPos(), 
   * getPhonemes(), getSyllables(), etc. will return immediately.
   */
  public void analyze(RiCharSequence rcs)
  {
    this.analyze(rcs.getText());
    Map m = getFeatures();
    for (Iterator i = m.keySet().iterator(); i.hasNext();) {
      String key = (String) i.next();      
      rcs.addFeature(key, (String)m.get(key));
//System.out.println("adding "+key+"="+m.get(key));
    }
  } 

  /**
   * Returns a String representation of the feature list for the last analyzed text 
   */ 
  public String getFeatureString()
  {
    StringBuilder sb = new StringBuilder();
    Map m = getFeatures(delegates).getFeatures();
    for (Iterator iter = m.keySet().iterator(); iter.hasNext();)
    {
      String key = (String) iter.next();
      sb.append(key+": "+m.get(key)+"\n");      
    }    
    return sb.toString();
  }
  
  /**
   * Prints the features of the last analyzed text to System.out
   */ 
  public void dumpFeatures()
  {
    System.out.println(getFeatureString());
  }
  
  /**
   * Analyzes an array of each RiTexts, setting the appropriate
   * features for each element assuming that each holds a single word. 
   * Often used in conjunction with RiText.createWords().
   * @param rts
   * @see RiText#createWords(PApplet, String, float, float)
   */
  public void analyze(RiText[] rts)
  {
    String[] text = new String[rts.length];
    for (int i = 0; i < rts.length; i++)
      text[i] = rts[i].getText();
    String full = RiTa.join(text, ' ');
    //System.out.println("full: "+full);
    analyze(full);
    int j = 0;
    Map f = getFeatures(delegates).getFeatures();    
    for (Iterator i = f.keySet().iterator(); i.hasNext(); j++)
    {
      String key = (String) i.next();
      String val = (String)f.get(key);     
      String[] feat = val.split(Featured.WORD_BOUNDARY);      
      //System.out.println(key+": "+val);
//    System.out.println(key+": "+Util.asList(feat)+"\n");      
      for (int k = 0; k < feat.length; k++) {
        if (feat.length > 1)  
          rts[k].addFeature(key, feat[k]);
        else
          rts[k].addFeature(key, feat[0]);
      }   
    }
  }
  

 /**
  * Returns a Map (of String key-value pairs) of all the features
  * for the last analyzed phrase
  */
  public Map getFeatures()
  {
    FeaturedIF fif = getFeatures(delegates);
    Map m = fif.getFeatures();
//    System.out.println("M: "+m);
    return m;    
  }
      
  private FeaturedIF getFeatures(RiPhrase[] rips)
  {
    Featured featureMap = new Featured();
    for (int i = 0; i < rips.length; i++) {
      Map m = rips[i].getFeatures();
      for (Iterator it = m.keySet().iterator(); it.hasNext();) {
        String key = (String) it.next();    
        if (key.equals("mutable") || key.equals(ID)) 
          continue;
        featureMap.appendFeature(key, (String)m.get(key));
      }
    }
    return featureMap;
  }

  /**
   * @invisible
   */
  public boolean isCacheEnabled()
  {
    return this.cacheEnabled;
  }

  /**
   * @invisible
   */
  public void setCacheEnabled(boolean cacheEnabled)
  {
    this.cacheEnabled = cacheEnabled;
  }
  
  /** @invisible                   */
  public static void example(String[] args)
  {   
    String text = "The boy jumped over the wild dog.";
    RiAnalyzer ra = new RiAnalyzer(null);
    ra.analyze(text);
    String phonemes = ra.getPhonemes();   
    String stresses = ra.getStresses();
    String syllables = ra.getSyllables();
    String pos = ra.getPos();
  }
  
  /** @invisible                   */
  public static void main(String[] args)
  {
    String text = "The dog ran faster than the other dog.  But the other dog was prettier.";
    text = "The boy woke from a troubled sleep, wondering if he was alone " +
    "in the house. Such a thing had never occurred to him before, but things had " +
    "changed at home in the last few weeks. That was for sure.";
    text = "The dog ran faster than you. He asked why.";// Why was the boy sad?";
    RiAnalyzer ra = new RiAnalyzer();
    ra.analyze(text);
    ra.dumpFeatures();
    System.out.println(ra.getAvailableFeatures());
    if (1==1) return;
    RiText rt = new RiText(null, "The red dog");
    //rt.addFeature("myfeatureName", "myFeatureValue");
    ra.analyze(rt);
    System.out.println(rt.getFeatures());
    

  /* 
    System.out.println(ra.lastIdx("dog")+"\n");
    int idx = 13;
    System.out.println(ra.delegateForIdx(idx));
    System.out.println(ra.wordAt(idx));    
    System.out.println(ra.getPosAt(idx));
    System.out.println(ra.getPhonemesAt(idx));
    System.out.println(ra.getStressesAt(idx));*/
  }

}// end

