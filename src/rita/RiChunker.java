package rita;

import java.util.List;

import processing.core.PApplet;
import rita.support.ifs.RiChunkerIF;
import rita.support.me.MaxEntChunker;
import rita.support.remote.RemoteChunker;

/* TODO:
   add method that takes a RiString an tags it with the chunk-type,
   add to IF too, and also RiParser.
*/

/**
 * A simple and lightweight implementation of a phrase chunker for non-recursive syntactic 
 * elements (e.g., noun-phrases, verb-phrases, etc.) using the Penn conventions (shown below). 
 * 
 *<pre>
    String sent = "The boy ran over dog";  
    RiChunker chunker = new RiChunker(this);    
    String chunks = chunker.chunk(sent);</pre>     
 
 * The full tag set follows:
 * <ul>
 * <li>adjp   = adjective phrase  
 * <li>advp   = adverb phrase       
 * <li>conjp  = conjunction phrase  
 * <li>intj   = interjection        
 * <li>lst    = list marker         
 * <li>np     = noun phrase
 * <li>pp     = prepositional phrase
 * <li>prt    = particle
 * <li>sbar   = clause introduced by a subordinating conjunction
 * <li>ucp    = unlike coordinated phrase
 * <li>vp     = verb phrase
 * <li>o      = independent phrase
 * </ul><br>
 * Note: to use this object, you must first download the rita 
 * statistical models (rita.me.models.zip) and unpack them into 
 * the 'rita' directory in your processing sketchbook, or into
 * the data directory for you sketch. You can also specify an 
 * alternative directory (an absolute path) for the models via 
 * RiTa.setModelDir();<p> 
 * 
 * Based closely on the OpenNLP maximum entropy chunker.<p>
 *   
 * For more info see: Berger & Della Pietra's paper 
 * 'A Maximum<br> Entropy Approach to Natural Language Processing',
 * which<br>provides a good introduction to the maxent framework. 
 * 
 * @see RiTaServer 
 * @invisible
 */
public class RiChunker extends RiObject implements RiChunkerIF
{
  protected RiChunkerIF delegate;
  
  /**
   * @invisible
   */
  public RiChunker() {
    this(null);    
  }
  
  public RiChunker(PApplet pApplet) {
    super(pApplet);    
    if (RiTa.isServerEnabled())
      delegate = new RemoteChunker(MaxEntChunker.class);
    else
      delegate = MaxEntChunker.getInstance(pApplet);    
  }

  /**
   * Performs pos-tagging (and word tokenizing) to prepare a sentence 
   * for chunking, then returns a String of chunks inline, in the following
   * format(for input 'The boy ran over dog'):<p>
   *   (np The/dt boy/nn) (vp ran/vbd) (pp over/in) (np the/dt dog/nn)
   */
  public String tagAndChunk(String sentence)
  {
    return this.delegate.tagAndChunk(sentence);
  }
  

  /**
   * Use supplied part-of-speech tags to do chunking,
   * then returning chunk data inline, in following
   * format (for input 'The boy ran over dog'):<p>
   *   (np The/dt boy/nn) (vp ran/vbd) (pp over/in) (np the/dt dog/nn)
   */
  public String chunk(String[] arrayOfTokens, String[] arrayOfTags)
  {
    return this.delegate.chunk(arrayOfTokens, arrayOfTags);
  }
  
  /**
   * Use supplied part-of-speech tags to do chunking,
   * then returning chunk data inline, in following
   * format (for input 'The boy ran over dog'):<p>
   *   (np The/dt boy/nn) (vp ran/vbd) (pp over/in) (np the/dt dog/nn)
   */
  public String chunk(List listOfTokens, List listOfTags)
  {
    return this.delegate.chunk(listOfTokens, listOfTags);
  }

  /**
   * Returns an array of adjective phrases
   * found in the last chunking operation.
   */
  public String[] getAdjPhrases()
  {
    return this.delegate.getAdjPhrases();
  }

  /**
   * Returns an array of adverb phrases
   * found in the last chunking operation.
   */
  public String[] getAdvPhrases()
  {
    return this.delegate.getAdvPhrases();
  }

  /**
   * @invisible
   */
  public String[] getChunkData()
  {
    return this.delegate.getChunkData();
  }

  /**
   * Returns an array of noun phrases
   * found in the last chunking operation.
   */
  public String[] getNounPhrases()
  {
    return this.delegate.getNounPhrases();
  }

  /**
   * Returns an array of prepositions
   * found in the last chunking operation.
   */
  public String[] getPrepPhrases()
  {
    return this.delegate.getPrepPhrases();
  }

  /**
   * Returns the array of verb phrases
   * found in the last chunking operation.
   */
  public String[] getVerbPhrases()
  {
    return this.delegate.getVerbPhrases();
  }
  
  public static void main(String[] args)
  {
    //RiPosTagger.DEFAULT_POS_TAGGER = RiPosTagger.BRILL_POS_TAGGER;
    
    RiTa.useServer();
    
    RiTa.setModelDir("/Users/dhowe/Desktop/models/");
    
    long time = System.currentTimeMillis();
    String sent = "The boy ran over the dog";  
   // sent = "The doctor treated dogs";
    RiChunker chunker = new RiChunker();    
    for (int i = 0; i < 1; i++) 
      System.out.println(i+") "+chunker.tagAndChunk(sent));      
    System.out.println(RiTa.elapsed(time)+"s");
    String[] s = chunker.getChunkData();  
    for (int i = 0; i < s.length; i++)
      System.out.println(i+") "+s[i]);
    String[] toks = sent.split(" ");
    String[] tags = RiPosTagger.getInstance().tag(toks);
    System.out.println(chunker.chunk(asList(toks), asList(tags)));
  }

}// end
