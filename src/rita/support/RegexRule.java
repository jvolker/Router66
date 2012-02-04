/*   
 *	   =================    
 *     Acknowledgements:
 *     =================
 *     This library contains a re-implementation of some rules derived from SimpleNLG MorphG package
 *     by Guido Minnen, John Carroll and Darren Pearce. You can find more information about MorphG
 *     in the following reference:
 *     	Minnen, G., Carroll, J., and Pearce, D. (2001). Applied Morphological Processing of English.
 *     		Natural Language Engineering 7(3): 207--223.
 *     Thanks to John Carroll (University of Sussex) for permission to re-use the MorphG rules. 
 */

package rita.support;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Adapted from the SnlgPatternRule class in the SimpleNLG package
 */
public class RegexRule //implements Comparable 
{
	/** The Constant EXCEPTION. */
	public static final int EXCEPTION = 0;

	/** The Constant GENERIC. */
	public static final int GENERIC = 1;

	/** The Constant DEFAULT. */
	public static final int DEFAULT = 2;

	/** The left hand side. */
	private Matcher leftHandSide;

	/** The left hand string. */
	private String leftHandString;

	/** The offset. */
	private int offset;

	/** The suffix. */
	private String suffix;

	/**
	 * Instantiates a new pattern action rule.
	 * 
	 * @param regex
	 *            the regex
	 * @param truncate
	 *            the truncate
	 * @param suff
	 *            the suff
	 */
	public RegexRule(String regex, int truncate, String suff, int notused) {
    this(regex, truncate, suff);
	}
	

  /**
   * Instantiates a new pattern action rule.
   * 
   * @param regex
   *            the regex
   * @param truncate
   *            the truncate
   * @param suff
   *            the suff
   */
	public RegexRule(String regex, int truncate, String suff) {
  	leftHandSide = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher("\\w+");
    leftHandString = regex;
    offset = truncate;
    suffix = suff;
	}

	 /** The type. */
  //private Integer type = RegexRule.EXCEPTION;
  
  public String toString() {
    return suffix+" "+leftHandString+" "+leftHandSide;
  }

  
/*	*//**
	 * Instantiates a new pattern action rule.
	 * 
	 * @param regex
	 *            the regex
	 * @param truncate
	 *            the truncate
	 * @param suff
	 *            the suff
	 * @param newType
	 *            the new type
	 *//*
	public RegexRule(String regex, int truncate, String suff,
			int newType) {

		if ((newType == RegexRule.EXCEPTION)
				|| (newType == RegexRule.DEFAULT)
				|| (newType == RegexRule.GENERIC))
			type = newType;
		else
			throw new IllegalArgumentException(
					"Type of PatternActionRule is 0 (Exception), "
							+ "1 (Generic) or 2 (Default)");

		// set the leftHandSide to a matcher for "word". To be reset
		// every time the rule is applied. Saves garbage from creation
		// of new matchers each time
		leftHandSide = Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
				.matcher("word");
		leftHandString = regex;
		offset = truncate;
		suffix = suff;
	}*/

/*	*//**
	 * Sets the type.
	 * 
	 * @param newType
	 *            the new type
	 *//*
	public void setType(int newType) {

		if ((newType == RegexRule.EXCEPTION)
				|| (type == RegexRule.DEFAULT)
				|| (type == RegexRule.GENERIC))
			type = newType;
		else
			throw new IllegalArgumentException(
					"Type of PatternActionRule is 0 (Exception), "
							+ "1 (Generic) or 2 (Default)");
	}*/

/*	*//**
	 * Gets the type.
	 * 
	 * @return the type
	 *//*
	public int getType() {
		return type;
	}
*/
	/**
	 * Gets the left hand side.
	 * 
	 * @return the left hand side
	 */
	public String getLeftHandSide() {
		return leftHandString;
	}

	/**
	 * Applies.
	 * 
	 * @param word
	 *            the word
	 * 
	 * @return true, if successful
	 */
	public boolean applies(String word) {
		word = word.trim();
		leftHandSide = leftHandSide.reset(word);
		return leftHandSide.find();
	}

	/**
	 * Fire.
	 * 
	 * @param word
	 *            the word
	 * 
	 * @return the string
	 */
	public String fire(String word) {
		word = word.trim();
		return truncate(word) + suffix;
	}

	/**
	 * Analyse.
	 * 
	 * @param word
	 *            the word
	 * 
	 * @return true, if successful
	 */
	public boolean analyse(String word) {

		if ((suffix != "") && word.endsWith(suffix))
			return true;

		return false;
	}
/*
	
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 
	public int compareTo(Object rule) {
		return type.compareTo(((RegexRule)rule).getType());
	}*/

	/**
	 * Truncate.
	 * 
	 * @param word
	 *            the word
	 * 
	 * @return the string
	 */
	private String truncate(String word) {

		if (offset == 0)
			return word;

		StringBuffer buffer = new StringBuffer(word);
		int i = 1;
		while (i <= offset) {
			buffer.deleteCharAt(buffer.length() - 1);
			i++;
		}

		return buffer.toString();
	}

  /*public int compareTo(Object o)
  {
    throw new RuntimeException("compareTo unimplemented for "+getClass()+"...");
  }*/

}
