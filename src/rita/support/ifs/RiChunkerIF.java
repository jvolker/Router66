package rita.support.ifs;

import java.util.List;

/**
 * Simple interface for text chunking
 * @author dhowe
 */
public interface RiChunkerIF
{
  /** Returns a String of chunks inline */  
  public abstract String chunk(List tokens, List tags);
  
  /** Returns a String of chunks inline */
  public abstract String chunk(String[] tokens, String[] tags);
  
  /**
   * Performs word tokenizing & pos-tagging to prepare a sentence 
   * for chunking, then returns a String of chunks inline
   */
  public String tagAndChunk(String sentence);
  
  public String[] getChunkData();  
  
  public String[] getNounPhrases();
  
  public String[] getVerbPhrases();  
  
  public String[] getPrepPhrases();
  
  public String[] getAdjPhrases();
  
  public String[] getAdvPhrases();
  
}// end
