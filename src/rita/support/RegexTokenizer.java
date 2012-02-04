package rita.support;

import java.util.*;
import java.util.regex.Pattern;

import processing.core.PApplet;
import rita.RiObject;
import rita.RiTa;
import rita.support.ifs.RiTokenizerIF;

/**
 * Simple tokenizer for user-supplied regular expressions. <P>
 * Note: defaults to splitting on white-space characters('\s');
 */
public final class RegexTokenizer extends RiObject implements RiTokenizerIF
{
  public static final String DEFAULT_REGEX = "\\s";//[ ,;:()\\[\\]]";
  private static final String SPC = " ";
  
  protected Pattern regex;
  protected boolean trimSpaces = false;
  protected boolean returnDelims = false;
  
  public RegexTokenizer(PApplet p, String regex) {
    super(p);
    setRegex(regex == null ? DEFAULT_REGEX : regex);
  }
  
  public RegexTokenizer(PApplet p, Pattern regex) {
    super(p);
    setRegex(regex);
  }
  
  public RegexTokenizer(String regex) {
    this(null, regex);
  }
  
  public RegexTokenizer(PApplet p) {
    this(p, DEFAULT_REGEX);
  }
  
  
  public RegexTokenizer() {
    this(null, DEFAULT_REGEX);
  }
  
  /**
   * Splits the String into sentences according to the regular expression 
   */
  public String[] split(String text)
  {
    return tokenize(text);
  }
  
  /**
   * Tokenizes the String according to the supplied regular expression
   * and stores the result as a List in <code>result</code>
   */
  public void tokenize(String words, List result) 
  {
    String[] tokens = RiTa.split(words, regex, returnDelims);
    for (int i = 0; i < tokens.length; i++)
      if (tokens[i].length()>0) {
        if (trimSpaces && tokens[i].equals(SPC))
          continue;
        result.add(tokens[i]);
      }
  }
  
	/**
	 * Tokenizes the String according to the supplied regular expression.
	 */
	public String[] tokenize(String words) 
  {
    List l = new ArrayList();
    tokenize(words, l);
    return (String[])l.toArray(new String[l.size()]);
  } 


  /**
   * Returns the regular expression used for tokenizing
   */
  public String getRegex()
  {
    return this.regex.pattern();
  }

  /**
   * Sets the regular expression to be used for tokenizing
   */
  public void setRegex(Pattern regex)
  {
    this.regex = regex;
  }

  /**
   * Sets the regular expression to be used for tokenizing
   */
  public void setRegex(String regex)
  {
    if (regex != null)
      this.regex = Pattern.compile(regex);
  }

  /**
   * Whether spaces are trimmed from each ends of tokens
   */
  public boolean isTrimmingSpaces()
  {
    return this.trimSpaces;
  }

  /**
   * Sets whether spaces are trimmed from each ends of tokens
   */
  public void setTrimSpaces(boolean trimSpaces)
  {
    this.trimSpaces = trimSpaces;
  }

  /**
   * Whether delimiters are returned as tokens or ignored  
   */
  public boolean isReturningDelims()
  {
    return this.returnDelims;
  }
  
  /**
   * Sets whether delimiters should be returned as tokens or ignored  
   */
  public void setReturnDelims(boolean returnDelims)
  {
    this.returnDelims = returnDelims;
  }
  
  public static void main(String[] args)
  {
    String[] texts = { "Good time, great taste (that's why this is our place).", "McDonald's - it cannot happen" , "McDonald's, it cannot happen"  };

    String paragraph  = "This isn't the greatest example sentence " +
    "in the world because I've seen better. Neither is this one. " +
    "T.D. Waterhouse is not bad, though.";
    
    RegexTokenizer res = new RegexTokenizer(".");
    List l = asList(res.split(paragraph));
    for (Iterator i = l.iterator(); i.hasNext();)
      System.out.println("'"+i.next()+"'");      
  }

}// end