package rita.support.me;

import java.util.*;

import processing.core.PApplet;
import rita.*;
import rita.support.ifs.RiChunkerIF;

/**
 * Simple chunker that finds non-recursive syntactic 'chunks' 
 * such as noun-phrases, using the Penn conventions (shown below). 
 * <br><ul>
 * <li>adjp   adjective phrase  
 * <li>advp   adverb phrase       
 * <li>conjp  conjunction phrase  
 * <li>intj   interjection        
 * <li>lst    list marker         
 * <li>np     noun phrase
 * <li>pp     prepositional phrase
 * <li>prt    particle
 * <li>sbar   clause introduced by a subordinating conjunction
 * <li>ucp    unlike coordinated phrase
 * <li>vp     verb phrase
 * <li>o      independent phrase
 * </ul><br>
 * Primarily just a wrapper for the OpenNLP(http://opennlp.sourceforge.net) chunker 
 * with some minor modifications/simplifications.<p>  
 * For more info see: Berger & Della Pietra's paper:
 * 
 * 'A Maximum Entropy Approach to Natural Language Processing',
 * which provides a good introduction to the maxent framework. 
 */
public class MaxEntChunker extends RiObjectME implements RiChunkerIF
{ 
  protected static final String CHUNK_MODEL = "chunker.bin.gz";
  
  public static final String NOUN_PHRASE = "np";
  public static final String VERB_PHRASE = "vp";
  public static final String PREP_PHRASE = "pp";
  public static final String SBAR_PHRASE = "sbar";
  public static final String ADJ_PHRASE  = "adjp";
  public static final String ADV_PHRASE  = "advp";
  public static final String PRT_PHRASE  = "prt";  
  public static final String IND_PHRASE  = "o"; // indep.
    
  static opennlp.maxent.MaxentModel model;  
  static opennlp.tools.chunker.Chunker delegate;
  static boolean fixIndependentPhrases = true;
  
  protected String[] chunkData, posTags, tokens;  
  
  private static MaxEntChunker instance;
  
  public static MaxEntChunker getInstance() {
    return getInstance(null);
  }
  
  public static MaxEntChunker getInstance(PApplet p) {
    if (instance == null)
      instance = new MaxEntChunker(p);
    return instance;
  }
      
  public MaxEntChunker() { this(null); }

  public MaxEntChunker(PApplet p) {
    super(p);
    loadModelData(p);
    instance = this;
  }
  
  // Methods =============================================

  public static MaxEntChunker createRemote(Map params)
  {        
    setModelDir(params);
    return new MaxEntChunker();    
  }
  
  private void loadModelData(PApplet p)
  {
    try
    {
      long start = System.currentTimeMillis();
      if (model == null) 
      {
        if (LOAD_FROM_MODEL_DIR) 
        {
          String mdir = getModelDir();          
          
          // if absolute only try the actual path
          if (RiTa.isAbsolutePath(mdir))  
          {
            model = OpenNLPUtil.getFileAsModel(p, mdir+CHUNK_MODEL, false);            
          }
          else // lets try some other spots
          {
            // if we are in P5, try the default spot
            if (p != null && !RiTa.isAbsolutePath(mdir)) 
              model = OpenNLPUtil.getFileAsModel(p, RiTa.libPath(p)+"rita/models/"+CHUNK_MODEL, false);
            
            // try the current model directory
            if (model == null)
              model = OpenNLPUtil.getFileAsModel(p, mdir+CHUNK_MODEL);
            
            // try the chunker sub-directory 
            if (model == null  && getModelDir().indexOf("chunker")<0)              
              model = OpenNLPUtil.getFileAsModel(p, mdir+"chunker/"+CHUNK_MODEL);
          }
        }
        else
          model = OpenNLPUtil.getResourceAsModel(RiText.class, CHUNK_MODEL);
      }
      if (delegate == null && !RiTa.isServerEnabled()) {
        delegate = new opennlp.tools.lang.english.TreebankChunker(model);
        if (!RiTa.SILENT)System.out.println("[INFO] Loaded chunk data in "+RiTa.elapsed(start)+"s");
      }
      
    }
    catch (Exception e)
    {
      throw new RiTaException("Unable to create chunker: ",e);
    }        
  }
  
  protected String getChunkStr()
  {   
    String[] chunks = getChunkData();
    StringBuilder buf = new StringBuilder(64);
    for (int i = 0; i < chunks.length; i++)
    {
      if (i > 0 && !chunks[i].startsWith("i-"))// && !chunks[ci - 1].equals(IND_PHRASE))
        buf.append(RP);
      
      if (chunks[i].startsWith("b-"))
        buf.append(SPC +LP+ chunks[i].substring(2).toLowerCase());
        
      else if (chunks[i].equals(IND_PHRASE))  // added 
        chunks[i] = handleIndPhrase(buf, tokens[i], posTags[i]);

      buf.append(SPC + tokens[i] + FS + posTags[i]);
    }
    // if (!chunks[chunks.length - 1].equals(IND_PHRASE))
    buf.append(RP);    
    
//for (int i = 0; i < chunks.length; i++)System.out.println(i+") "+chunks[i]);
    
    return buf.toString().trim();
  }

  private String handleIndPhrase(StringBuilder buf, String word, String pos)
  {
    String type = IND_PHRASE;
    if (fixIndependentPhrases) {
      if (pos.startsWith("nn"))
        type = "np";
      else if (pos.startsWith("vb"))
        type = "vp";
      buf.append(SPC + LP + type);
System.out.println("[****] Fixed iPhrase (o "+word+FS+pos+") -> ("+type+" "+word+FS+pos+")");      
    }
    else
      buf.append(SPC + LP+ IND_PHRASE);
    return type;
  }
    
  public String chunk(List words, List postags)
  {
   // checkServerState(this, "chunk(words, tags)");
    return chunk(strArr(words), strArr(postags));
  }

  public String[] getNounPhrases() { return getChunksByType(NOUN_PHRASE); }
  public String[] getVerbPhrases() { return getChunksByType(VERB_PHRASE); }  
  public String[] getPrepPhrases() { return getChunksByType(PREP_PHRASE); }
  public String[] getAdjPhrases()  { return getChunksByType(ADJ_PHRASE);  }
  public String[] getAdvPhrases()  { return getChunksByType(ADV_PHRASE);  }
  
/*  
  public static String[] getNounPhrases(String chunkStr) { return getChunksByType(chunkStr, NOUN_PHRASE); }
  public static String[] getVerbPhrases(String chunkStr) { return getChunksByType(chunkStr, VERB_PHRASE); }  
  public static String[] getPrepPhrases(String chunkStr) { return getChunksByType(chunkStr, PREP_PHRASE); }
  public static String[] getAdjPhrases(String chunkStr)  { return getChunksByType(chunkStr, ADJ_PHRASE);  }
  public static String[] getAdvPhrases(String chunkStr)  { return getChunksByType(chunkStr, ADV_PHRASE);  }  
  
  protected static String[] getChunksByType(String chunkData, String chunkType) {
    throw new RiTaException("Implement me!");
  }*/
  
  protected String[] getChunksByType(String chunkType) {
    //checkServerState(this, "getChunksByType("+chunkType+")");
    List results = new ArrayList();
    String[] cd = getChunkData();
    String phrase = "";
    for (int i = 0; i < cd.length; i++)
    {
      if (cd[i].equals(IND_PHRASE)) continue;
      
      String chunkTag = cd[i].substring(2);
      
      if (chunkTag.equals(chunkType)) {
        if (cd[i].startsWith("b-")) { 
          addResult(results, phrase);
          phrase = tokens[i]+ SPC;
        }
        else
          phrase += tokens[i]+SPC;
      }
    }    
    addResult(results, phrase);
    return strArr(results);
  }

  private void addResult(List results, String phrase)
  {
    if (phrase.length()>0)          
      results.add(phrase.trim());
  }

  private static String[] lc(String[] words)
  {
    for (int i = 0; i < words.length; i++)
    {
      if (words[i] == null)
        words[i]=QQ;
      else
        words[i] = words[i].toLowerCase();
    }
    return words;
  }

  public String chunk(String[] words, String[] tags)
  {
    this.tokens = words;
    this.posTags = tags;     
    this.chunkData = lc(delegate.chunk(tokens, posTags));
    //System.out.println("RiChunker.chunk() : "+Util.asList(chunkData));
    // ================ TMP-TESTS ==================
    for (int i = 0; i < chunkData.length; i++)
    {
      if (chunkData[i].equals(IND_PHRASE)) continue;
      if ("np|vp|pp|adjp|prt|sbar|advp".indexOf(chunkData[i].substring(2))<0)
        throw new RiTaException("Unexpected chunk type: "+chunkData[i]);
    }
    // ============================================= */
    return getChunkStr();
  }
  
  /**
   * Utility method that uses the default word tokenizer & 
   * pos-tagger to prepare a sentence for chunking, then
   * returns  the sentence String w' chunk-data inline
   * @param sentence
   */
  public String tagAndChunk(String sentence)
  {
    this.tokens = RiTa.tokenize(sentence);
    //System.out.println("TOKS: "+asList(tokens));
    if (!RiPosTagger.taggerExists())
      RiPosTagger.setDefaultTagger(RiPosTagger.MAXENT_POS_TAGGER);
    
    this.posTags = RiTa.posTag(tokens);
    //System.out.println("TAGS: "+asList(posTags));
    return this.chunk(tokens, posTags);
  }

  public String[] getChunkData()
  {
    //checkServerState(this, "getChunkData()");
    if (chunkData == null)
      throw new RiTaException("chunk() must be called before getChunkData()");
    return this.chunkData;
  }
  
  
  public void destroy() {
    model = null;
    delegate = null;
  }
  
  public static void main(String[] args)
  {
    //RiTa.setModelDir("/Users/dhowe/Desktop/me-models/chunker");
    RiPosTagger.setDefaultTagger(RiPosTagger.BRILL_POS_TAGGER);    
    long time = System.currentTimeMillis();
    String sent = "The boy ran over the dog";  
    sent = "The doctor treated dogs";
    MaxEntChunker chunker = new MaxEntChunker();    
    for (int i = 0; i < 1; i++) 
      System.out.println(i+") "+chunker.tagAndChunk(sent));      
    System.out.println(RiTa.elapsed(time)+"s");
    String[] s = chunker.getChunkData();  
    for (int i = 0; i < s.length; i++)
      System.out.println(i+") "+s[i]);
  }

}// end