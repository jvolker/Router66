package rita;

import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;

import rita.support.*;
import rita.support.ifs.RiTaggerIF;
import rita.support.me.MaxEntTagger;
import rita.support.remote.RemotePosTagger;

/**
 * Simple pos-tagger for the RiTa libary using the Penn tagset. Use <code>RiPosTagger.setDefaultTagger(type);</code> 
 *  to specify a (faster/lighter) transformation-based tagger, or the (usually more accurate) 
 *  maximum-entryopy tagger<pre>
    RiPosTagger tagger = new RiPosTagger(this);
    String s = "The teenage boy, stricken with fear, cried sadly like a little baby";
    
    String[] words = RiTa.tokenize(s);
    String[] tags = tagger.tag(words);
    
    for (int i = 0; i < sents.length; i++) 
    {
        System.out.println(sents[i]);
    }   
    //    OR     
    System.out.println(tagger.tagInline(s));
 * </pre>
 * The full Penn part-of-speech tag set:
 * <ul>
 * <li><b><code>cc</code> </b> coordinating conjunction
 * <li><b><code>cd</code> </b> cardinal number
 * <li><b><code>dt</code> </b> determiner
 * <li><b><code>ex</code> </b> existential there
 * <li><b><code>fw</code> </b> foreign word
 * <li><b><code>in</code> </b> preposition/subord. conjunction
 * <li><b><code>jj</code> </b> adjective
 * <li><b><code>jjr</code> </b> adjective, comparative
 * <li><b><code>jjs</code> </b> adjective, superlative
 * <li><b><code>ls</code> </b> list item marker
 * <li><b><code>md</code> </b> modal
 * <li><b><code>nn</code> </b> noun, singular or mass
 * <li><b><code>nns</code> </b> noun, plural
 * <li><b><code>nnp</code> </b> proper noun, singular
 * <li><b><code>nnps</code> </b> proper noun, plural
 * <li><b><code>pdt</code> </b> predeterminer
 * <li><b><code>pos</code> </b> possessive ending
 * <li><b><code>prp</code> </b> personal pronoun
 * <li><b><code>prp$</code> </b>i possessive pronoun
 * <li><b><code>rb</code> </b> adverb
 * <li><b><code>rbr</code> </b> adverb, comparative
 * <li><b><code>rbs</code> </b> adverb, superlative
 * <li><b><code>rp</code> </b> particle
 * <li><b><code>sym</code> </b> symbol (mathematical or scientific)
 * <li><b><code>to</code> </b> to
 * <li><b><code>uh</code> </b> interjection
 * <li><b><code>vb</code> </b> verb, base form
 * <li><b><code>vbd</code> </b> verb, past tense
 * <li><b><code>vbg</code> </b> verb, gerund/present participle
 * <li><b><code>vbn</code> </b> verb, past participle
 * <li><b><code>vbp</code> </b> verb, non-3rd ps. sing. present
 * <li><b><code>vbz</code> </b> verb, 3rd ps. sing. present
 * <li><b><code>wdt</code> </b> wh-determiner
 * <li><b><code>wp</code> </b> wh-pronoun
 * <li><b><code>wp$</code> </b> possessive wh-pronoun
 * <li><b><code>wrb</code> </b> wh-adverb
 * <li><b><code>#</code> </b> pound sign
 * <li><b><code>$</code> </b> dollar sign
 * <li><b><code>.</code> </b> sentence-final punctuation
 * <li><b><code>,</code> </b> comma
 * <li><b><code>:</code> </b> colon, semi-colon
 * <li><b><code>(</code> </b> left bracket character
 * <li><b><code>)</code> </b> right bracket character
 * <li><b><code>"</code> </b> straight double quote
 * <li><b><code>`</code> </b> left open single quote
 * <li><b><code>"</code> </b> left open double quote
 * <li><b><code>"</code> </b> right close single quote
 * <li><b><code>"</code> </b> right close double quote
 * <li><b><code>-</code> </b> dash
 * </ul>
 *  
 * Note: to use maximum-entry tagger, you must first download 
 * the rita statistical models (rita.me.models.zip) package
 * and unpack the zip into the "rita" directory in your 
 * processing sketchbook, or the data directory for your sketch. <p>
 * You can also specify an alternative directory (an absolute path) 
 * for the models via RiTa.setModelDir();<pre>
 * Then call RiPosTagger.setDefaultTagger(RiPosTagger.MAXENT_POS_TAGGER);</pre>
 */ 
public class RiPosTagger extends RiObject
{      
  protected static int DEFAULT_POS_TAGGER = BRILL_POS_TAGGER;
  
  protected static boolean COMPARE_TAGGERS = false;
  
  protected static RiTaggerIF delegate = null;
  protected static RiTaggerIF test = null; // tmp
  
  protected static int currentType= -1;
  
  /**
   * @invisible
   * @deprecated
   */
  public RiPosTagger() {
    this(null);
  }
  
  public RiPosTagger(PApplet pApplet) {
    this(pApplet, DEFAULT_POS_TAGGER);     
  }
  
  public RiPosTagger(PApplet p, int taggerType) 
  {
    super(p);             

//System.out.println("RiPosTagger.RiPosTagger(server="+RiTa.isServerEnabled()+", brill="+(taggerType==BRILL_POS_TAGGER)+")");
    
    DEFAULT_POS_TAGGER = taggerType;
    
    if (currentType != taggerType && delegate != null) 
      throw new RiTaException("Only one pos-tagger type allowed at a time!"
        + " An instance of "+delegate+" was already created..."); 
    
    currentType = taggerType;
    
    if (delegate == null) 
    {
      if (RiTa.isServerEnabled()) taggerType = MAXENT_POS_TAGGER;
      
      if (taggerType==MAXENT_POS_TAGGER) 
      {
        System.out.println("RiPosTagger.RiPosTagger(MAX_ENT)");
        if (RiTa.isServerEnabled()) {
          delegate = new RemotePosTagger(MaxEntTagger.class);
        }
        else 
        {
          try {
            delegate = MaxEntTagger.getInstance(p);
          }
          catch (Exception e) {
            System.err.println("[WARN] RiPosTagger: Unable to " +
              "load the MaxEntTagger, reverting to the BrillTagger!\n      "+e.getMessage());
            taggerType = BRILL_POS_TAGGER;              
          }
        }
      }      
      
      if (delegate == null) {
        if (taggerType==BRILL_POS_TAGGER) 
          delegate =  BrillPosTagger.getInstance(p);
        else
          throw new RiTaException("RiPosTagger -> unknown tagger type: "+taggerType);
      }
      
    }

    instance = this; // hack
  }  
  
 
  /** @invisible */
  public static RiPosTagger getInstance() {
    return getInstance(null);
  } private static RiPosTagger instance;
  
  /** @invisible */
  public static RiPosTagger getInstance(PApplet p) {
    if (instance == null)
      instance = new RiPosTagger();
    return instance;
  }
  
  /**
   * Takes an array of words and of tags and returns a 
   * combined String of the form:
   *  <pre>"The/dt doctor/nn treated/vbd dogs/nns"</pre>
   * assuming a "/" as <code>delimiter</code>. 
   * @invisible
   */
  public static String inlineTags(String[] tokenArray, String[] tagArray, String delimiter) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < tagArray.length; i++)
      sb.append(tokenArray[i] + delimiter + tagArray[i] + " ");
    return sb.toString().trim();
  }
    
  /**
   * Tags the array of words (as usual) with a part-of-speech from the Penn tagset, 
   * then returns the corresponding part-of-speech for WordNet from the set
   * { "n" (noun), "v"(verb), "a"(adj), "r"(adverb), "-"(other) } as a String. 
   * @see #tag
   */
  public String[] tagForWordNet(String[] tokenArray)
  { 
    String[] tags = tag(tokenArray);
    System.out.println("RiPosTagger.tagForWordNet() = "+RiTa.asList(tags));
    for (int i = 0; i < tags.length; i++) 
      tags[i] = toWordNet(tags[i]);
    return tags;
  }
  
  /**
   * Tags the array of words (as usual) with a part-of-speech from the Penn tagset, 
   * then returns the corresponding part-of-speech for WordNet from the set
   * { "n" (noun), "v"(verb), "a"(adj), "r"(adverb), "-"(other) } as a String. 
   * @see #tagForWordNet(String[])
   * @invisible
   */
  public String[] tagForWordNet(FeaturedIF[] tokenArray)
  { 
    String[] tags = tag(tokenArray);
    for (int i = 0; i < tags.length; i++) 
      tags[i] = toWordNet(tags[i]);
    return tags;
  }
  
  /**
   * Tags a single word with a part-of-speech from the Penn tagset, 
   * then returns the corresponding part-of-speech for WordNet from the set
   * { "n" (noun), "v"(verb), "a"(adj), "r"(adverb), "-"(other) } as a String. 
   * @see #tagForWordNet(String[])
   * @invisible 
  public String tagWordForWordNet(FeaturedIF featured)
  { 
    String tag = tagWordForWordNet(featured.toString());
    // set feature here?
  }*/
  
  /**
   * Tags a single word with a part-of-speech from the Penn tagset, 
   * then returns the corresponding part-of-speech for WordNet from the set
   * { "n" (noun), "v"(verb), "a"(adj), "r"(adverb), "-"(other) } as a String. 
   * @see #tagForWordNet(String[])
   * @see #tag(String[])
   */
  public String tagWordForWordNet(String word)
  {     
    if (word.indexOf(' ')>=0)
      throw new RiTaException("Expecting a single word"
        + " (w' no spaces), but found: '"+word+"'");
    String[] tags = tag(new String[]{word});
    if (tags==null || tags.length != 1)
      return null;
    if (tags[0]==null || tags[0].length()<1)
      return null;
    return toWordNet(tags[0]);
  }
  
  /**
   * Converts a part-of-speech String from the Penn tagset to the corresponding part-of-speech 
   * for WordNet from the set { "n" (noun), "v"(verb), "a"(adj), "r"(adverb), "-"(other) } as a String. 
   * If the pos is not found in the penn set, it is returned unchanged.
   * @see #tag(String[])
   */
  public static String toWordNet(String pos)
  {     
    if (pos==null || pos.length()<1) 
      return null;
    if (isNoun(pos))      return "n";
    if (isVerb(pos))      return "v";
    if (isAdverb(pos))    return "r";
    if (isAdjective(pos)) return "a";
    return "-";
  } 
  

  /**
   * Takes an array of words and of tags and returns a 
   * combined String of the form:
   *  <pre>    "The/dt doctor/nn treated/vbd dogs/nns"</pre>
   * @invisible
   */
  public static String inlineTags(String[] tokenArray, String[] tagArray) {
    return inlineTags(tokenArray, tagArray, "/");   // default delim
  }    
  
  /**
   * Takes a String of words and tags in the format:
   *  <pre>     The/dt doctor/nn treated/vbd dogs/nns</pre>
   * returns an array of the part-of-speech tags.
   * @param wordsAndTags
   */
  public static String[] parseTagString(String wordsAndTags) {
    List tags = new ArrayList();
    String[] data = wordsAndTags.split(" ");
    for (int i = 0; i < data.length; i++)
    {
      String[] tmp = data[i].split("/");
      if (tmp == null || tmp.length != 2)
        throw new RiTaException("Bad tag format: "+data[i]);
      tags.add(tmp[1]);
    }
    return RiTa.strArr(tags);
  }
  
  /**
   * Returns true if <code>pos</code> is a verb
   */
  public static boolean isVerb(String pos)
  {
    return RiPos.isVerb(pos);
  }
  /**
   * Returns true if <code>partOfSpeech</code> is a noun
   */
  public static boolean isNoun(String partOfSpeech)
  {
    return RiPos.isNoun(partOfSpeech);
  }
  /**
   * Returns true if <code>pos</code> is an adverb
   */
  public static boolean isAdverb(String pos)
  {
    return RiPos.isAdverb(pos);
  }
  /**
   * Returns true if <code>pos</code> is an adjective
   */
  public static boolean isAdjective(String pos)
  {
    return RiPos.isAdj(pos);
  }
  
  /** Returns a String List of the most probably tags 
  public List tag(List tokens)
  {

    List l = delegate.tag(tokens);
    if (COMPARE_TAGGERS) {      
      List l2 = getTestTagger().tag(tokens);
      boolean ok = true;
      for (int i = 0; i < l.size(); i++)
      {
        if (!l.get(i).equals(l2.get(i))) {
          ok = false;
          break;
        }
      }
      if (!ok) System.out.println("POS_MISMATCH:\n  MAXENT: "+l+"\n  BRILL:  "+l2);
    }
    return l;
  }*/
  
  /** 
   * Tags each token with the appropriate POS (as a feature),
   * then returns a String array of the assigned tags. 
   */
  public String[] tag(FeaturedIF[] tokenArray)
  {    
    String[] words = Featured.toStrings(tokenArray);
    String[] tags = tag(words); 
    for (int i = 0; i < tokenArray.length; i++)
      tokenArray[i].addFeature(Featured.POS, tags[i]);
    return tags;
  }
  
  /** Returns a String array of the most probably tags */
  public String[] tag(String[] tokenArray)
  {
    String[] l = delegate.tag(tokenArray);
    if (COMPARE_TAGGERS) {      
      String[] l2 = getTestTagger().tag(tokenArray);
      boolean ok = true;
      for (int i = 0; i < l.length; i++)
      {
        if (!l[i].equals(l2[i])) {
          ok = false;
          break;
        }
      }
      if (!ok) System.out.println("POS_MISMATCH:\n  BRILL: "+asList(l)+"\n  MAXENT:  "+asList(l2));
    }
    return l;
  }
  
  private RiTaggerIF getTestTagger()
  {
    if (test == null) 
      test = MaxEntTagger.getInstance();
    return test;
  }

  /** Returns a String with pos-tags notated inline in the format:
   *  <pre>    "The/dt doctor/nn treated/vbd dogs/nns"</pre>
   */
  public String tagInline(String[] tokens)
  {
    return delegate.tagInline(tokens); 
  }
  
  /** 
   * Sets the default tagger type for the application
   * @invisible 
   */
  public static void setDefaultTagger(int taggerType)
  {
    DEFAULT_POS_TAGGER = taggerType;
  }

  /** 
   * Returns true if the tagger has been already created
   * @invisible
   */
  public static boolean taggerExists()
  {
    return instance != null;
  }

  /** 
   * Tokenizes the input sentence using the defaultTokenizer
   * and returns a String with pos-tags notated inline 
   */
  public String tagInline(String sentence)
  {
    return delegate.tagInline(sentence);
  } 
  
  /** 
   * Loads a file, splits the input into sentences and returns 
   * a single String[] with all the pos-tags from the text. 
   */
  public String[] tagFile(String fileName)
  {
    return delegate.tagFile(fileName);
  }
  
  private static void testLocal2()
  {
    //RiPosTagger.setDefaultTagger(RiPosTagger.MAXENT_POS_TAGGER);
    RiPosTagger.COMPARE_TAGGERS = true;
    RiPosTagger tagger = new RiPosTagger();
    String[] s = RiTa.tokenize("The teenage boy, stricken with fear, cried sadly like a little baby");
    //s = tagger.tag(s);
    //for (int i = 0; i < s.length; i++)
     //System.out.println(i+") "+s[i]);
/*    RiPosTagger.COMPARE_TAGGERS = true;        
    System.out.println(tagger.tagInline(s));*/
    System.out.println(asList(tagger.tag(s)));
  }

  private static void testLocal1()
  {    
    //RiTa.setModelDir("/Users/dhowe/Desktop/me-models/tagger");
    
    //RiPosTagger.setDefaultTagger(RiPosTagger.MAXENT_POS_TAGGER);

    RiPosTagger tagger = new RiPosTagger();
    String s = "The teenage boy, stricken with fear, cried sadly like a little baby";
    System.out.println(tagger.tagInline(s));
    //System.out.println(tagger.tagInline(RiTa.tokenize(s)));
    System.out.println(RiTa.asList(tagger.tag(RiTa.tokenize(s))));
    System.out.println(RiTa.asList(tagger.tagForWordNet(RiTa.tokenize(s))));
    System.out.println(tagger.tagWordForWordNet("wrestle"));
  }
  
  private static void testRemote()
  {        
    RiTa.useServer();
    testLocal1();
  }
  
  public static void main(String[] args) {
    
    //RiTa.setModelDir("/Users/dhowe/Desktop/models/");
    //testLocal1();
    String s  = "The first to suggest a remedy"; 
    s = "to";
    /*
    RiPosTagger tagger = new RiPosTagger(null, RiConstants.MAXENT_POS_TAGGER);
    System.out.println(RiTa.asList(tagger.tag(RiTa.tokenize(s))));*/
    
    RiPosTagger tagger = new RiPosTagger(null);//, RiConstants.BRILL_POS_TAGGER);
    System.out.println(RiTa.asList(tagger.tag(RiTa.tokenize(s))));
    //testRemote();
  }
}// end
