package rita.support;

import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

import processing.core.PApplet;
import rita.*;
import rita.support.ifs.RiLexiconIF;
import rita.support.ifs.RiTaggerIF;

/*
 * TODO: 
 *   BAD TAG EXAMPLES:
 *      'The quick brown fox jumped over the frightened(vbn) dog.'
 */

/**
 * Simple transformation-based pos-tagger for the RiTa libary using the Penn tagset<p>
 *
 * Uses the Brill data set with a minimal subset of the original 
 * context-sensitive transformations (plus some custom additions.) <p>
 * 
 * For more info see: Brill (1995) 'Unsupervised Learning 
 * of Disambiguation Rules for Part of Speech Tagging' 
 * <p>
 * The full Penn tag set follows:
 * <ol>
 * <li><b><code>cc</code> </b> Coordinating conjunction
 * <li><b><code>cd</code> </b> Cardinal number
 * <li><b><code>dt</code> </b> Determiner
 * <li><b><code>ex</code> </b> Existential there
 * <li><b><code>fw</code> </b> Foreign word
 * <li><b><code>in</code> </b> Preposition/subord. conjunction
 * <li><b><code>jj</code> </b> Adjective
 * <li><b><code>jjr</code> </b> Adjective, comparative
 * <li><b><code>jjs</code> </b> Adjective, superlative
 * <li><b><code>ls</code> </b> List item marker
 * <li><b><code>md</code> </b> Modal
 * <li><b><code>nn</code> </b> Noun, singular or mass
 * <li><b><code>nns</code> </b> Noun, plural
 * <li><b><code>nnp</code> </b> Proper noun, singular
 * <li><b><code>nnps</code> </b> Proper noun, plural
 * <li><b><code>pdt</code> </b> Predeterminer
 * <li><b><code>pos</code> </b> Possessive ending
 * <li><b><code>prp</code> </b> Personal pronoun
 * <li><b><code>prp$</code> </b> Possessive pronoun
 * <li><b><code>rb</code> </b> Adverb
 * <li><b><code>rbr</code> </b> Adverb, comparative
 * <li><b><code>rbs</code> </b> Adverb, superlative
 * <li><b><code>rp</code> </b> Particle
 * <li><b><code>sym</code> </b> Symbol (mathematical or scientific)
 * <li><b><code>to</code> </b> to
 * <li><b><code>uh</code> </b> Interjection
 * <li><b><code>vb</code> </b> Verb, base form
 * <li><b><code>vbd</code> </b> Verb, past tense
 * <li><b><code>vbg</code> </b> Verb, gerund/present participle
 * <li><b><code>vbn</code> </b> Verb, past participle
 * <li><b><code>vbp</code> </b> Verb, non-3rd ps. sing. present
 * <li><b><code>vbz</code> </b> Verb, 3rd ps. sing. present
 * <li><b><code>wdt</code> </b> wh-determiner
 * <li><b><code>wp</code> </b> wh-pronoun
 * <li><b><code>wp$</code> </b> Possessive wh-pronoun
 * <li><b><code>wrb</code> </b> wh-adverb
 * <li><b><code>#</code> </b> Pound sign
 * <li><b><code>$</code> </b> Dollar sign
 * <li><b><code>.</code> </b> Sentence-final punctuation
 * <li><b><code>,</code> </b> Comma
 * <li><b><code>:</code> </b> Colon, semi-colon
 * <li><b><code>(</code> </b> Left bracket character
 * <li><b><code>)</code> </b> Right bracket character
 * <li><b><code>"</code> </b> Straight double quote
 * <li><b><code>`</code> </b> Left open single quote
 * <li><b><code>"</code> </b> Left open double quote
 * <li><b><code>'</code> </b> Right close single quote
 * <li><b><code>"</code> </b> Right close double quote
 * <li><b><code>-</code> </b> Right close double quote
 * </ol> 
 */
public class BrillPosTagger implements RiTaggerIF 
{
  static final String ZIP_LEX = "taglex.brill.dat";
  static final String TEXT_LEX = "taglex.brill";  
  
  static final boolean USE_RITA_LEXICON = true;
  static final boolean LOAD_FROM_ZIP = false;    
  static final boolean DBUG_CREATES = false;
  static final boolean DBUG_INFO = true;
  
  private static Map dictionary;  
  private static RiLexiconIF lexicon;
  private static BrillPosTagger instance;
  
  private PApplet pApplet; 
  
  /**
   * invisible
   * deprecated
  public static BrillPosTagger getInstance() {
    return getInstance(null);
  } */
  
  
  public static BrillPosTagger getInstance(PApplet p) {
    if (instance == null)
      instance = new BrillPosTagger(p);
    return instance;
  }
  
  private BrillPosTagger() { this(null); }
  
  private BrillPosTagger(PApplet p) 
  {
    this.pApplet = p;
    
    if (DBUG_CREATES)
      System.err.println("BrillPosTagger.BrillPosTagger()");
    
    long start = System.currentTimeMillis();
    
    String[] lines = null;
    if (USE_RITA_LEXICON) {
      lexicon = RiLexiconImpl.getInstance(p);
    }
    else 
    {
      if(DBUG_CREATES)System.err.println("BrillPosTagger.loading file-data");
      URL url = RiText.class.getResource(TEXT_LEX);
      lines = RiTa.loadStrings(url);
      //}
      if (dictionary == null) {
        dictionary = new HashMap(110000, .9f);
        for (int i = 0; i < lines.length; i++)
          parseLine(lines[i]);      
        if (DBUG_INFO) System.out.println("[INFO] Loaded pos data: '"
          + TEXT_LEX + "' -> " + dictionary.size() + " entries "+RiTa.elapsed(start)+"s");
      }
    }
  }
  
  /** 
   * Loads a file, splits the input into sentences 
   * and returns a String[] of the most probably tags. 
   */
  public String[] tagFile(String fileName) {
    List result = new ArrayList();
    String text = RiTa.loadString(pApplet, fileName);
    
    String[] sents = RiTa.splitSentences(text);
    for (int i = 0; i < sents.length; i++) {
      //System.out.println("BrillPosTagger: "+sen);
      String[] words = RiTa.tokenize(sents[i]);
      tag(words, result);
    }
    
    String[] tmp = new String[result.size()];
    return (String[])result.toArray(tmp);
  }
  public boolean isVerb(String pos) {
    return RiPos.in(pos, RiPos.PENN_VERBS);
  }
  public boolean isNoun(String pos) {
    return RiPos.in(pos, RiPos.PENN_NOUNS);
  }
  public boolean isAdverb(String pos) {
    return RiPos.in(pos,RiPos.PENN_ADV);
  }
  public boolean isAdjective(String pos) {
    return RiPos.in(pos, RiPos.PENN_ADJ);
  }
  
  /**
   * Returns the part(s)-of-speech from the Penn tagset for a single word
   * @param word String
   * @see #tag(String[])
   * @return String (or String[])
   */
  public String tag(String word)
  {
    word = word.trim(); // ???
    if (word.indexOf(" ")>-1) // check for/strip punct too?
      throw new RiTaException("Method accepts only single words!");
    String[] result = tag(new String[]{word});
    if (result == null || result.length < 1)
      return null;
    return result[0];
  }
  
  /**
   * Tags the word (as usual) with a part-of-speech from the Penn tagset, 
   * then returns the corresponding part-of-speech for WordNet from the
   * set { 'n', 'v', 'a', 'r' } as a String. 
   * @param word
   * @see #tag
   */
  public String tagForWordNet(String word)
  { 
    String pos = tag(word);
    if (pos==null || pos.length()<1) return null;        
    if (pos.equals("n") || isNoun(pos))      return "n";
    if (pos.equals("v") || isVerb(pos))      return "v";
    if (pos.equals("r") || isAdverb(pos))    return "r";
    if (pos.equals("a") || isAdjective(pos)) return "a";    
    return null; 
  } 
  
  /**
   * Returns an array of parts-of-speech from the Penn tagset
   * each corresponding to one word of input.
   * @param words String[]
   */
  public void tag(String[] words, List result)
  {
    //System.out.println("BrillPosTagger.tag("+Util.asList(words)+")");
    String[] tmp = tag(words);    
    for (int j = 0; j < tmp.length; j++) 
      result.add(tmp[j]); 
  }
  
  /**
   * Returns an array of parts-of-speech from the Penn tagset
   * each corresponding to one word of input.
   * @param words String[]
   * @return String[]
   */
  public String[] tag(String[] words)
  {
    //System.out.println("BrillPosTagger.tag("+Util.asList(words)+")");

    String[] result = new String[words.length];
    String[][] choices = new String[words.length][];
    for (int i = 0, size = words.length; i < size; i++)
    {
      String word = words[i];
      String[] data = lookup(word);
//System.out.println(words[i]+" -> "+RiTa.asList(data));
      if (data == null || data.length == 0) {
        if (word.length() == 1) {
          result[i] = Character.isDigit(word.charAt(0)) ? "cd" : word;             
        }
        else
          result[i] = "nn";
        choices[i] = null;
      }
      else {
        result[i] = data[0];
        choices[i] = data;
      }
    }
    
    // adjust pos according to transformation rules
    this.applyContext(words, result, choices);
    
    return result;
  }

  public String[] lookup(String word)
  {
    //System.out.println("BrillPosTagger.lookup("+word+")");    
    String[] data = null;
    if (USE_RITA_LEXICON) 
    {      
      data = lexicon.getPosArr(word);      
    }
    else {
      data = (String[])dictionary.get(word);
      if (data == null) { // try lowercase (remove?) 
        data = (String[]) dictionary.get(word.toLowerCase());
        if (data != null)
          System.out.println("[INFO] Brill-Lexicon: found lowercase match! "+word);
      }
    }
    return data;
  }
  
  static final Pattern number = Pattern.compile("[0-9\\.][0-9\\.]*");
  
  public static final boolean PRINT_CUSTOM_TAGS = false;

  /**
   * Applies a customized subset of the Brill transformations
   */
  protected void applyContext(String[] words, String[] result, String[][] choices)
  {    
    //System.out.println("RiPosTagger.applyContext("+Arrays.asList(words)+","+Arrays.asList(result)+")");
    
    // Apply transformations 
    for (int i = 0; i < words.length; i++)
    {
      //System.err.println(i+") preTransform: "+words[i]+" -> "+result[i]);
      //if (i>0) System.out.println("previous="+result[i-1]);
      
      // transform 1: DT, {VBD | VBP | VB} --> DT, NN
      if (i > 0 && result[i - 1].equals("dt"))
      {
        if (result[i].startsWith("vb")) {
          if (PRINT_CUSTOM_TAGS) System.out.println("BrillPosTagger: changing verb to noun: "+words[i]);
          result[i] = "nn";
        }
        
        // transform 1: DT, {RB | RBR | RBS} --> DT, {JJ | JJR | JJS}
        else if (result[i].startsWith("rb"))   {// added -dch
          if (PRINT_CUSTOM_TAGS)
            System.out.print("BrillPosTagger:  custom tagged '"+words[i]+"', "+result[i]);                    
          result[i] = "jj";
          if (result[i].length()>2)  
            result[i] += result[i].charAt(2);
          if (PRINT_CUSTOM_TAGS) System.out.println(" -> "+result[i]);
        }
      }
      // transform 2: convert a noun to a number (cd)
      // if it is all digits and/or a decimal "." 
      if (result[i].startsWith("n") && choices[i] == null)
      {
        if (isNum(words[i])) result[i] = "cd"; // mods: dch (add choice check above)
      }
        
      // transform 3: convert a noun to a past participle if words[i] ends with "ed"
      if (result[i].startsWith("n") && words[i].endsWith("ed"))
        result[i] = "vbn";
      
      // transform 4: convert any type to adverb if it ends in "ly";
      if (words[i].endsWith("ly"))
        result[i] = "rb";
      
      // transform 5: convert a common noun (NN or NNS) to a adjective if it ends with "al"
      if (result[i].startsWith("nn") && words[i].endsWith("al"))
        result[i] = "jj";
      
      // transform 6: convert a noun to a verb if the preceeding word is "would"
      if (i > 0 && result[i].startsWith("nn")
          && words[i - 1].equalsIgnoreCase("would"))
        result[i] = "vb";
      
      // transform 7: if a word has been categorized as a common noun and it ends
      // with "s", then set its type to plural common noun (NNS)
      if (result[i].equals("nn") && words[i].endsWith("s"))
        result[i] = "nns";
        
      // transform 8: convert a common noun to a present participle verb (i.e., a gerund)
      if (result[i].startsWith("nn") && words[i].endsWith("ing")) {
        // fix here -- add check on choices for any verb: eg 'morning'
        if (hasTag(choices[i], "vb"))
          result[i] = "vbg";
        else if (PRINT_CUSTOM_TAGS)
          System.out.println("[INFO] um tagged '"+words[i]+"' as "+result[i]);
      }
      
      // transform 9(dch): convert common nouns to proper nouns when they start w' a capital and are not a sentence start
      if (i > 0 && result[i].startsWith("nn") && words[i].length()>1 && Character.isUpperCase(words[i].charAt(0))) {
        //System.out.print("[INFO] Custom tagged '"+words[i]+"', "+result[i]);
        result[i] = result[i].endsWith("s") ? "nnps" : "nnp";
        //System.out.println(" -> "+result[i]);
      }
      
      // transform 10(dch): convert plural nouns (which are also 3sg-verbs) to 3sg-verbs when followed by an adverb (jumps, dances) 
      if (i < result.length-1 && result[i].equals("nns") && result[i+1].startsWith("rb") && hasTag(choices[i], "vbz")) {
        //System.out.print("CUSTOM_TAG_TRANSFORM: "+result[i]);
        result[i] = "vbz";
        //System.out.println(" -> "+result[i]);
      }
    }
  }

  private static boolean isNum(String word) 
  {
    for (int j = 0; j < word.length(); j++) {
      char c = word.charAt(j);
      if (! (Character.isDigit(c) || c=='.' || c=='-')) {  
        return false;
      }
    }
    return true;
  }
  
  private boolean hasTag(/*String word, */String[] choices, String tag)
  {    
    String choiceStr = RiTa.join(choices);
    //System.err.println("RiPosTagger.canBeVerb("+word+","+choiceStr+")");
    boolean hasTag = choiceStr.indexOf(tag)>-1;
    //System.err.println("VERB! "+verb);
    return hasTag;
  }

  // TODO: optimize...
  private void parseLine(String line)
  {    
    String[] words = line.split(" ");
    String[] tmp = new String[words.length - 1];
    System.arraycopy(words, 1, tmp, 0, tmp.length);
    //System.out.println("put("+words[0]+","+Arrays.asList(tmp)+")");
    dictionary.put(words[0], tmp);
  }

/*  public List tag(List tokens) {
    
    tag(toks);
    String[] toks = (String[])tokens.toArray(new String[tokens.size()]);
    return Arrays.asList();
  }  */ 
  
  /*public static void mainXX(String[] args)
  {
    RiLexicon lex = new RiLexicon();
    BrillPosTagger ft = new BrillPosTagger();
    RiWordnet rw = new RiWordnet();
    int count = 0;
    for (Iterator i = lex.iterator(); i.hasNext();) {
      String word = (String) i.next();  
      if (rw.exists(word)) 
        if (++count % 1000==0)
          System.out.println(count);        
    }
    System.out.println("MATCHES: "+count);    
  }*/
  
  /** Returns a String with pos-tags notated inline */
  public String tagInline(String[] tokens) { 
    return RiPosTagger.inlineTags(tokens, tag(tokens));
  }
  
 public String tagInline(String sentence) {
    
    int count = 0;
    int cursor = sentence.length()-1;
    char last = sentence.charAt(cursor);
    while (RiTa.PUNCTUATION.indexOf(last)>-1) {
        last = sentence.charAt(--cursor);
        count++;
    }
    String end = " ";
    int lastPunct = sentence.length()-count;
    end += sentence.substring(lastPunct);
    sentence = sentence.substring(0,lastPunct);
    
    return (tagInline(sentence.split(" "))+end).trim();
  }
 
  /** @invisible */
  public static void tests(String[] args)
  {
    boolean failed = false;   
    BrillPosTagger ft = new BrillPosTagger();
    for (int i = 0; i < tests.length; i+=2)
    {
      String[] words = RiTa.tokenize(tests[i]);
      String tags = RiTa.join(ft.tag(words));
      String expected = tests[i+1]; 
      if (!tags.equals(expected )) {
        System.err.println("FAILED: expected '"+expected+"'  -> "
          +RiTa.asList(words)+"\n        but got: '"+tags+"'");
        failed = true;
      }
//break;
    }       
    if (!failed)
      System.out.println("\n       All Tests OK!\n");
  }
    
  static String[] tests = {
     "I run to school.",                "nn vb to nn .",
     "I went for a run.",               "nn vbd in dt nn .",
     //"Red is a beautiful color",      "nn vbz dt jj nn",   // fails!
     "The little boy jumps quickly over the great fence and dances happily.",
                                        "dt jj nn vbz rb in dt jj nn cc vbz rb .",
     "The little boy jumps quickly",    "dt jj nn vbz rb",
     "The little boy dances happily",   "dt jj nn vbz rb",
     "The little boy jumped 3 times",   "dt jj nn vbd cd nns",
     "The little boy jumps",            "dt jj nn vbz",      
     "The little boy jumps wildly",     "dt jj nn vbz rb",   
     "The little boy jumps rope",       "dt jj nn vbz nn",      
     "I woke up early that morning",    "nn vbd in rb in nn",      
  };
  
  public static void main(String[] args)
  {
    BrillPosTagger ft = new BrillPosTagger();
    System.out.println(RiTa.asList(ft.tag("is it nourishing".split(" "))));
    System.out.println(ft.tag("gets"));
    if (1==1) return;
    //String[] test = {"small", "modest", "young"};
    String[] test = {"gets", "nourishing", "young"};
    for (int i = 0; i < test.length; i++)
    {
      System.out.println(i+") "+ft.tag(test[i]));
    }
    if (1==1) return;
    tests(args);
    RiLexicon rl = new RiLexicon();
  
    System.out.println(rl.getFeatures("jumps"));    
    ///RiTaLexicon rl = RiTaLexicon.getInstance();
    //System.out.println(rl.getFeatures("jumps"));

   // System.out.println("dog="+ft.tag("dog"));    
    String[] s = RiTa.tokenize("One morning, the man, stricken with fear, cried sadly like 2 little babies");
   // System.out.println(RiTa.asList(s));
    String[] tags = ft.tag(s);
    System.out.println(RiTa.asList(tags));
    
  }
}// end

