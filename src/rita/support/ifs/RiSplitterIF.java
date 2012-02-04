package rita.support.ifs;


/**
 * Simple interface for all sentence-splitters
 * @author dhowe
 */
public interface RiSplitterIF 
{
	/** Splits <code>text</code> into a String[] of sentences */
	public abstract String[] splitSentences(String text);
  
}// end