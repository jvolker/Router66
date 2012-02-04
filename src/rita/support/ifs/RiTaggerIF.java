package rita.support.ifs;


/**
 * Simple interface for all pos-taggers
 * @author dhowe
 */
public interface RiTaggerIF 
{
  /* Returns a String List of the most probably tags */
  //public abstract List tag(List tokens);
  
  /** 
   * Loads a file, splits the input into sentences 
   * and returns a String[] of the most probably tags. 
   */
  public abstract String[] tagFile(String fileName);
  
	/** Returns a String array of the most probably tags */
	public abstract String[] tag(String[] tokens);

  /** Returns a String with pos-tags notated inline */
  public abstract String tagInline(String[] tokens);
  
  /** Returns a String with pos-tags notated inline */
  public abstract String tagInline(String sentence);

  /** Returns true if <code>word</code> is a verb. */
  public abstract boolean isVerb(String word);
  
  /** Returns true if <code>word</code> is a noun. */
  public abstract boolean isNoun(String word);  

  /** Returns true if <code>word</code> is an adverb. */
  public abstract boolean isAdverb(String word);  

  /** Returns true if <code>word</code> is an adjective. */
  public abstract boolean isAdjective(String word);

}// end