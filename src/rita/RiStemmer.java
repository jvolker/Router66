package rita;

import java.text.DecimalFormat;

import processing.core.PApplet;

import rita.support.*;
import rita.support.ifs.RiStemmerIF;

// TODO: test with word-list (+stems) from here:
// http://snowball.tartarus.org/algorithms/porter/diffs.txt


// TODO: load rules files as class resources ***********
// TODO: pkg correct rita.dict in jar file

/**
 * A simple set of stemmers for extracting base roots from a word by 
 * removing prefixes and suffixes. For example, the words 'run', 'runs',  
 * and 'running' all have "run" as their stem.<pre>
    String[] tests = { "run", "runs", "running" };
    RiStemmer stem = new RiStemmer(this);
    for (int i = 0; i < tests.length; i++)
      System.out.println(stem.stem(tests[i]));
 * </pre>
 * This class provides a # of implementations, each specified by a type constant.<br>
 * For example, to use the Lancaster (or Paice-Husk) algorithm instead of the Porter (the default),
 * create the stemmer as follows:
 * <pre>
 * RiStemmer stem = new RiStemmer(this, LANCASTER_STEMMER);
 * </pre>
 * For a comparison of the various algorithms, see <br>
 *   http://www.comp.lancs.ac.uk/computing/research/stemming/Links/algorithms.htm
 */
public class RiStemmer extends RiObject implements RiStemmerIF
{
  /** 
   * Type constant for Pling stemmer     
   * @invisible
   */
  public static final int PLING_STEMMER = 1;
  
  /** 
   * Type constant for Porter stemmer     
   * @invisible
   */
  public static final int PORTER_STEMMER = 2;
  
  /** 
   * Type constant for Lancaster stemmer  
   * @invisible
   */
  public static final int LANCASTER_STEMMER = 3;
  
  /** 
   * Type constant for Pacie-Husk stemmer 
   * @invisible
   */
  public static final int LOVINS_STEMMER = 4;
  
  private static final int DEFAULT_STEMMER = PORTER_STEMMER;

  private RiStemmerIF stemmer;

  /**
   * @invisible
   */
  public RiStemmer()
  {
    this(null);
  }
  
    
  /**
   * Creates a default stemmer
   */
  public RiStemmer(PApplet p)
  {
    this(p, DEFAULT_STEMMER);
  }
  
  /**
   * @invisible
   */
  public RiStemmer(PApplet p, int stemmerType)
  {
    super(p);
    switch (stemmerType) {
      case PLING_STEMMER:
        stemmer = new PlingStemmer();
        break;
      case PORTER_STEMMER:
        stemmer = new PorterStemmer();
        break;
      case LANCASTER_STEMMER:
        stemmer = new LancasterStemmer();
        break;
      case LOVINS_STEMMER:
        stemmer = new LovinsStemmer();
        break;
      default: 
        throw new RiTaException("Unexpected stemmer type: "+stemmerType);
    }
  }
    
  /** 
   * Returns the concrete stemmer (delegate) object that actually does the work
   * @invisible 
   */
  public RiStemmerIF getStemmer() {
    return stemmer;
  }

/*  
  // returns the correcvt stemmer for the pos
  protected RiStemmerIF getStemmer(String word) {

    return isPossibleNoun(word) ? nounStemmer : genericStemmer;
  }*/
  
/*  private boolean isPossibleNoun(String word)
  {
    boolean checkBestOnly = true; // hmm
    
    boolean result = false;
    String[] posTags = null;
    if (checkBestOnly) {
      posTags = tagger.tag(new String[]{word});
      return RiPosTagger.isNoun(posTags[0]);
    }
    
    // try all pos possibilities for noun
    posTags = lexicon.getPosEntries(word);
    if (posTags != null) {
      for (int i = 0; i < posTags.length; i++) {
        if (RiPosTagger.isNoun(posTags[i])) {
          result = true;
          break;
        }
      }
    }
    // none found, use tagger's rules
    else 
    {
      posTags = tagger.tag(new String[]{word});
      result = RiPosTagger.isNoun(posTags[0]);  
    }
    return result;
  }*/

  /**
   * Extracts base roots from a word by lower-casing it, then removing prefixes and suffixes. 
   * For example, the words 'run', 'runs', 'ran', and 'running' all have "run" as their stem.
   */
  public String stem(String word)
  {
    //RiStemmerIF rs = getStemmer(word);
    return stemmer.stem(word.toLowerCase());
  }
  
  /**
   * Extracts base roots from a word by removing prefixes and suffixes, using the POS specified as context. 
   * For example, the words 'run', 'runs', 'ran', and 'running' all have "run"
   * as their stem
  public String stem(String word, String pos)
  {
    return stemmer.stem(s) 
    RiPosTagger.isNoun(pos) ? 
        nounStemmer.stem(word) : genericStemmer.stem(word);
  }*/

  private static void testUnchanging(RiStemmer stemmer)
  {
    stemmer.test("locomote", "locomote");
    stemmer.test("idle", "idle");
    stemmer.test("juvenile", "juvenile");
    stemmer.test("ingenue", "ingenue");
    stemmer.test("service", "service");
    stemmer.test("creature", "creature");
    stemmer.test("device", "device");
    stemmer.test("lagerphone", "lagerphone");
    stemmer.test("force", "force");
    stemmer.test("desire", "desire");
    stemmer.test("province", "province");
    stemmer.test("signalise", "signalise");
    stemmer.test("formulate", "formulate");
    stemmer.test("cognise", "cognise");
    stemmer.test("communicate", "communicate");
    stemmer.test("tangle", "tangle");
    stemmer.test("motorcycle", "motorcycle");
    stemmer.test("synchronise", "synchronise");
    stemmer.test("admeasure", "admeasure");
    stemmer.test("gauge", "gauge");
    stemmer.test("intertwine", "intertwine");
    stemmer.test("precede", "precede");
    stemmer.test("situate", "situate");
    stemmer.test("automobile", "automobile");
    stemmer.test("enumerate", "enumerate");
    stemmer.test("determine", "determine");
    stemmer.test("disagree", "disagree");
    stemmer.test("agree", "agree");
    stemmer.test("mobile", "mobile");
    stemmer.test("machine", "machine");
    stemmer.test("locate", "locate");
    stemmer.test("hearse", "hearse");
    stemmer.test("translate", "translate");
    stemmer.test("endure", "endure");
    stemmer.test("secure", "secure");
    stemmer.test("straddle", "straddle");
    stemmer.test("desire", "desire");
    stemmer.test("populate", "populate");
    stemmer.test("cringle", "cringle");
    stemmer.test("corroborate", "corroborate");
    stemmer.test("substantiate", "substantiate");
  }
  
  private void test(String expected, String... tests) {
    for (int i = 0; i < tests.length; i++)
      System.out.println(test(expected, tests[i]));
  }
  
  private static void mainC(String[] args)
  {
    exitOnFail = true;

    //String[] tests = {"idles"};
    RiStemmer stemmer = new RiStemmer();
    stemmer.test("run",       "run", "runs", "ran", "running");
    stemmer.test("quick",     "quicker", "quickly");
    testUnchanging(stemmer);
  }
  
  private static float runTests(RiStemmer stemmer, String[] data)
  {
    int fails = 0;
    for (int i = 0; i < data.length; i++) {
      String[] parts = data[i].split("\\s+");
      if (!stemmer.test(parts[1] ,parts[0]))
        fails++;
    }
    return (data.length-fails)/(float)data.length;
  }
  

  protected boolean test(String expected, String test) {
    String key = /*clean*/(test.toLowerCase());
    String val = stem(key);
    //System.out.println("lookup.put(\""+key+"\", "+"\""+val+"\");");
    if (!val.equals(expected)) {
      //System.err.println("    FAIL: stem('"+test+"') returned '"+ val+"', expecting '"+expected+"'");
      //System.err.print("      Stemmers:  Pling(n)='"+new PlingStemmer().stem(test)+"'");
      //System.err.print(", Porter(*)='"+new PorterStemmer().stem(test)+"'");
      if (exitOnFail) System.exit(1);
      return false;
    }
    return true;
  }
  
  private static boolean exitOnFail = false;
  
  public static void main(String[] args)
  {
    exitOnFail = false;
    DecimalFormat DF = new DecimalFormat("#.#");
    
    RiStemmer stemmer = null;
    
/*    String[] tests = { "run", "runs", "ran", "running" };
    //stemmer = new RiStemmer(null, LOVINS_STEMMER);
    for (int i = 0; i < tests.length; i++)
      System.out.println(stemmer.stem(tests[i]));
    
    if (1==1) System.exit(1);*/
 
    String[] data = RiTa.loadStrings(null, "diffs.txt");
    
     // ------------------------------------------------
    stemmer = new RiStemmer(null, PORTER_STEMMER);
    System.err.println("\nTesting stemmer-class="+stemmer.getStemmer());
    System.err.println(stemmer.getStemmer()+" result="+DF.format(runTests(stemmer, data)*100)+"%");
    // ------------------------------------------------
    stemmer = new RiStemmer(null, PLING_STEMMER);
    System.err.println("\nTesting stemmer-class="+stemmer.getStemmer());
    System.err.println(stemmer.getStemmer()+" result="+DF.format(runTests(stemmer, data)*100)+"%");
    // ------------------------------------------------
    stemmer = new RiStemmer(null, LOVINS_STEMMER);
    System.err.println("\nTesting stemmer-class="+stemmer.getStemmer());
    System.err.println(stemmer.getStemmer()+" result="+DF.format(runTests(stemmer, data)*100)+"%");
    // ------------------------------------------------
    stemmer = new RiStemmer(null, LANCASTER_STEMMER);
    System.err.println("\nTesting stemmer-class="+stemmer.getStemmer());
    System.err.println(stemmer.getStemmer()+" result="+DF.format(runTests(stemmer, data)*100)+"%");
    // ------------------------------------------------
  }

}// end
