package rita;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import processing.core.PApplet;
import rita.support.RegexRule;

/**
 * A simple pluralizer for nouns. Pass it a stemmed noun
 * (see RiStemmer) and it will return the plural form.
 * Uses a combination of letter-based rules and a lookup 
 * table of irregular exceptions, e.g., 'appendix' -> 'appendices'
 * <pre>
    RiPluralizer rp = new RiPluralizer();
    RiStemmer rs = new RiStemmer();
    
    String stem = rs.stem("dogs");
    System.out.println(rp.pluralize(stem));
    // returns "dogs"
    
    stem = rs.stem("appendix");
    System.out.println(rp.pluralize(stem));
    // returns "appendices"<pre>
     
    <br>
    Note: this implementation is based closely on rules found in the MorphG package,
    further described here:<p>
      Minnen, G., Carroll, J., and Pearce, D. (2001). Applied Morphological Processing of English.
      Natural Language Engineering 7(3): 207--223.    
 */
public class RiPluralizer extends RiObject {
    
  // privates --------------------
  private Matcher wordMatcher;
  private RegexRule[] rules;
  private RegexRule defaultRule;
//  private RiStemmer stemmer;
  
  // constructors ----------------
  public RiPluralizer() {
    this(null);
  }
  
  public RiPluralizer(PApplet pApplet) {
    super(pApplet);
    this.defaultRule = DEFAULT_PLURAL_RULE;
    this.wordMatcher = Pattern.compile(ANY_STEM).matcher("blablabla");
    this.rules = PLURAL_RULES;
    //Arrays.sort(rules);
  }
  
  // statics ----------------
  private static List MODALS = Arrays.asList(new String[] 
    { "shall", "would", "may", "might", "ought", "should" });

  private static final String ANY_STEM = "^((\\w+)(-\\w+)*)(\\s((\\w+)(-\\w+)*))*$";
  private static final String C = "[bcdfghjklmnpqrstvwxyz]";
  private static final String VL = "[lraeiou]";
  
	private static final RegexRule DEFAULT_PLURAL_RULE = new RegexRule(ANY_STEM, 0, "s", 2);

	private static final RegexRule[] PLURAL_RULES = new RegexRule[] {
			new RegexRule("^(piano|photo|solo|ego|tobacco|cargo|golf|grief)$", 0,"s"),
			new RegexRule("^(wildlife)$", 0, "s"),
			new RegexRule(C + "o$", 0, "es"),
			new RegexRule(C + "y$", 1, "ies"),
			new RegexRule("([zsx]|ch|sh)$", 0, "es"),
			new RegexRule(VL + "fe$", 2, "ves"),
			new RegexRule(VL + "f$", 1, "ves"),
			new RegexRule("(eu|eau)$", 0, "x"),
			new RegexRule("(man|woman)$", 2, "en"),

			new RegexRule("money$", 2, "ies"),
			new RegexRule("person$", 4, "ople"),
			new RegexRule("motif$", 0, "s"),
			new RegexRule("^meninx|phalanx$", 1, "ges"),
			new RegexRule("(xis|sis)$", 2, "es"),
			new RegexRule("schema$", 0, "ta"),
			new RegexRule("^bus$", 0, "ses"),
			new RegexRule("child$", 0, "ren"),
			new RegexRule("^(curi|formul|vertebr|larv|uln|alumn|signor|alg)a$", 0,"e"),
			new RegexRule("^corpus$", 2, "ora"),
			new RegexRule("^(maharaj|raj|myn|mull)a$", 0, "hs"),
			new RegexRule("^aide-de-camp$", 8, "s-de-camp"),
			new RegexRule("^apex|cortex$", 2, "ices"),
			new RegexRule("^weltanschauung$", 0, "en"),
			new RegexRule("^lied$", 0, "er"),
			new RegexRule("^tooth$", 4, "eeth"),
			new RegexRule("^[lm]ouse$", 4, "ice"),
			new RegexRule("^foot$", 3, "eet"),
			new RegexRule("femur", 2, "ora"),
			new RegexRule("goose", 4, "eese"),
			new RegexRule("(human|german|roman)$", 0, "s"),
			new RegexRule("(crisis)$", 2, "es"),
			new RegexRule("^(monarch|loch|stomach)$", 0, "s"),
			new RegexRule("^(taxi|chief|proof|ref|relief|roof|belief)$", 0, "s"),
			new RegexRule("^(co|no)$", 0, "'s"),

			// Latin stems
			new RegexRule("^(memorandum|bacterium|curriculum|minimum|"
					+ "maximum|referendum|spectrum|phenomenon|criterion)$", 2,"a"),
			new RegexRule("^(appendix|index|matrix)", 2, "ices"),
			new RegexRule("^(stimulus|alumnus)$", 2, "i"),

			// Null Plural
			new RegexRule("^(Bantu|Bengalese|Bengali|Beninese|Boche|bonsai|"
							+ "Burmese|Chinese|Congolese|Gabonese|Guyanese|Japanese|Javanese|"
							+ "Lebanese|Maltese|Olympics|Portuguese|Senegalese|Siamese|Singhalese|"
							+ "Sinhalese|Sioux|Sudanese|Swiss|Taiwanese|Togolese|Vietnamese|aircraft|"
							+ "anopheles|apparatus|asparagus|barracks|bellows|bison|bluefish|bob|bourgeois|"
							+ "bream|brill|butterfingers|carp|catfish|chassis|clothes|chub|cod|codfish|"
							+ "coley|contretemps|corps|crawfish|crayfish|crossroads|cuttlefish|dace|dice|"
							+ "dogfish|doings|dory|downstairs|eldest|earnings|economics|electronics|finnan|"
							+ "firstborn|fish|flatfish|flounder|fowl|fry|fries|works|globefish|goldfish|"
							+ "grand|gudgeon|gulden|haddock|hake|halibut|headquarters|herring|hertz|horsepower|"
							+ "goods|hovercraft|hundredweight|ironworks|jackanapes|kilohertz|kurus|kwacha|ling|lungfish|"
							+ "mackerel|means|megahertz|moorfowl|moorgame|mullet|nepalese|offspring|pampas|parr|(pants$)|"
							+ "patois|pekinese|penn'orth|perch|pickerel|pike|pince-nez|plaice|precis|quid|rand|"
							+ "rendezvous|revers|roach|roux|salmon|samurai|series|seychelles|seychellois|shad|"
							+ "sheep|shellfish|smelt|spacecraft|species|starfish|stockfish|sunfish|superficies|"
							+ "sweepstakes|swordfish|tench|tennis|tope|triceps|trout|tuna|tunafish|tunny|turbot|trousers|"
							+ "undersigned|veg|waterfowl|waterworks|waxworks|whiting|wildfowl|woodworm|"
							+ "yen|aries|pisces|forceps|lieder|jeans|physics|mathematics|news|odds|politics|remains|"
							+ "surroundings|thanks|statistics|goods|aids)$", 0, "", 0) 
			};

	/**
	 * Returns the regular or irregular plural form of <code>noun</code>. Note: this method requires
	 * a pre-stemmed noun (see RiStemmer) for proper function.
	 * @see rita.RiStemmer
   */
  public String pluralize(String noun) 
  {
    wordMatcher.reset(noun);

    if (!wordMatcher.matches())
      return noun;

    if (MODALS.contains(noun))
      return noun;

    String result = null;
    for (int i = 0; i < PLURAL_RULES.length; i++) {         
      RegexRule currentRule = PLURAL_RULES[i];
      if (currentRule.applies(noun)) {
        //System.out.print("applying rule "+i+" -> ");
        result = currentRule.fire(noun);
        //System.out.println(result);
        break;
      }
    }

    if ((result == null) && (defaultRule != null)) 
      result = defaultRule.fire(noun);

    return result;
  }  
 /* 
  *//** 
   * Delegates to the default stemmer to return 
   * whether the specified word is plural. 
   *//*
  public boolean isPlural(String s) {
    return getStemmer().isPlural(s);
  }*/

	/*private RiStemmer getStemmer()
  {
	  if (stemmer == null)
	    stemmer = new RiStemmer(getPApplet());
    return stemmer;
  }
*/
  public static void mainX(String[] args) {
    RiPluralizer rp = new RiPluralizer();
    RiStemmer rs = new RiStemmer();
    String stem = rs.stem("corpora");
    System.out.println("stem: "+stem);
    System.out.println(rp.pluralize(stem));
    stem = rs.stem("appendixes");
    System.out.println(rp.pluralize(stem));
    System.out.println();
    String s = "ambulance";
    stem = rs.stem(s);
    
    System.out.println("stem="+stem);
   // System.out.println(rp.isPlural(stem));
    System.out.println(rp.pluralize(stem));
    System.out.println();
    //System.out.println(rp.isPlural(s));
    System.out.println(rp.pluralize(s));


    //System.out.println("stem: "+stem);
    //System.out.println(rp.pluralize(stem));
  }
  
  public static void main(String[] args)
  {
    System.out.println(plural("dog"));
  }
  
  static String plural(String n) 
  {
    RiPluralizer rp = new RiPluralizer();
    RiStemmer rs = new RiStemmer();
    String stem = rs.stem(n);
    System.out.println("stem="+stem);
    String result = rp.pluralize(n);
    System.out.println("result="+result);
    return n;
  }
	
}// end
