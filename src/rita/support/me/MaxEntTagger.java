package rita.support.me;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import opennlp.maxent.MaxentModel;
import opennlp.tools.postag.POSDictionary;

import processing.core.PApplet;
import rita.*;
import rita.support.RiPos;
import rita.support.ifs.RiTaggerIF;
import rita.support.remote.RiRemotable;

/**
 * Simple pos-tagger for the RiTa libary using the Penn tagset<p>
 *
 * Based closely on the OpenNLP maximum entropy tagger.<p>  
 * 
 * For more info see: Berger & Della Pietra's paper 
 * 'A Maximum Entropy Approach to Natural Language Processing',
 * which provides a good introduction to the maxent framework. 
 * <p>
 * The full Penn tag set follows:
 * <ol>
 * <li><b><code>CC</code> </b> Coordinating conjunction
 * <li><b><code>CD</code> </b> Cardinal number
 * <li><b><code>DT</code> </b> Determiner
 * <li><b><code>EX</code> </b> Existential there
 * <li><b><code>FW</code> </b> Foreign word
 * <li><b><code>IN</code> </b> Preposition/subord. conjunction
 * <li><b><code>JJ</code> </b> Adjective
 * <li><b><code>JJR</code> </b> Adjective, comparative
 * <li><b><code>JJS</code> </b> Adjective, superlative
 * <li><b><code>LS</code> </b> List item marker
 * <li><b><code>MD</code> </b> Modal
 * <li><b><code>NN</code> </b> Noun, singular or mass
 * <li><b><code>NNS</code> </b> Noun, plural
 * <li><b><code>NNP</code> </b> Proper noun, singular
 * <li><b><code>NNPS</code> </b> Proper noun, plural
 * <li><b><code>PDT</code> </b> Predeterminer
 * <li><b><code>POS</code> </b> Possessive ending
 * <li><b><code>PRP</code> </b> Personal pronoun
 * <li><b><code>PRP$</code> </b> Possessive pronoun
 * <li><b><code>RB</code> </b> Adverb
 * <li><b><code>RBR</code> </b> Adverb, comparative
 * <li><b><code>RBS</code> </b> Adverb, superlative
 * <li><b><code>RP</code> </b> Particle
 * <li><b><code>SYM</code> </b> Symbol (mathematical or scientific)
 * <li><b><code>TO</code> </b> to
 * <li><b><code>UH</code> </b> Interjection
 * <li><b><code>VB</code> </b> Verb, base form
 * <li><b><code>VBD</code> </b> Verb, past tense
 * <li><b><code>VBG</code> </b> Verb, gerund/present participle
 * <li><b><code>VBN</code> </b> Verb, past participle
 * <li><b><code>VBP</code> </b> Verb, non-3rd ps. sing. present
 * <li><b><code>VBZ</code> </b> Verb, 3rd ps. sing. present
 * <li><b><code>WDT</code> </b> wh-determiner
 * <li><b><code>WP</code> </b> wh-pronoun
 * <li><b><code>WP$</code> </b> Possessive wh-pronoun
 * <li><b><code>WRB</code> </b> wh-adverb
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
public class MaxEntTagger extends RiObjectME implements RiTaggerIF
{
  private static final boolean DBUG_CREATES = false;
  
  private static final String POS_TAG_MODEL = "tag.bin.gz";
  private static final String POS_TAG_DICT = "tagdict";
   
  // singleton model resources
  private static opennlp.maxent.MaxentModel model;
  private static opennlp.tools.postag.TagDictionary tagdict;
  
  //  singleton delegate object (static?)
  private static opennlp.tools.postag.POSTagger delegate;
  
  private static MaxEntTagger instance;
  
  public static MaxEntTagger getInstance() {
    return getInstance(null);
  }
  
  public static MaxEntTagger getInstance(PApplet p) {
    if (instance == null)
      instance = new MaxEntTagger(p);
    return instance;
  }
  
  private MaxEntTagger() {
    this(null);    
  }
  
  public MaxEntTagger(PApplet p) {
    super(p);
    loadModelData(p);
    instance = this;
  }
    
  // METHODS ====================================================
    
  public static RiRemotable createRemote(Map params) {
    setModelDir(params);
    return new MaxEntTagger();
  }

  private void loadModelData(PApplet p)
  {
    if(DBUG_CREATES)System.out.println("MaxEntPosTagger.loadModelData(p="+(!(p==null))+")");
    long start = System.currentTimeMillis();
    
    try
    {
      if (tagdict == null)
        tagdict = loadTagDict(p);

      if (tagdict != null && model == null) { 
        model = loadTagModel(p);
      }
      
      if (delegate == null) 
      {        
        if (tagdict == null)
          throw new RiTaException(" Unable to load the '"+POS_TAG_DICT+"' file"+ERROR_MSG, false);
        
        if (model == null)        
          throw new RiTaException(" Unable to load the tag model: "+POS_TAG_MODEL+ERROR_MSG, false);
        
        delegate = new opennlp.tools.postag.POSTaggerME(model, tagdict);
        
        if (!RiTa.SILENT)System.out.println("[INFO] Loaded tagger(me) data in "+RiTa.elapsed(start)+"s");
      }
    }
    catch (IOException e)
    {
      throw new RiTaException("Unable to create pos-tagger: ",e);
    }
  }

  private POSDictionary loadTagDict(PApplet p) throws IOException 
  {   
    if(DBUG_CREATES)System.out.println("[DBUG] MaxEntPosTagger loading tagdict...");
    
    BufferedReader br = null;      
    if (LOAD_FROM_MODEL_DIR) 
    {
      String mDir = getModelDir();
      
      // if absolute only try the actual path
      if (RiTa.isAbsolutePath(mDir))
      { 
        if (DBUG_CREATES)System.out.println("[DBUG] Trying '"+mDir+POS_TAG_MODEL+"'");
        br = OpenNLPUtil.getFileAsBufferedReader(p, mDir+POS_TAG_DICT, false);
      }
      else // do some guessing...
      {              
        // if we are in P5, try the default spot
        if (p != null && !RiTa.isAbsolutePath(mDir)) {
          String test = RiTa.libPath(p)+"rita/models/"+POS_TAG_DICT;
          if (DBUG_CREATES)System.out.println("[DBUG] Trying '"+test+"' LIB_PATH="+RiTa.libPath(p));
          br = OpenNLPUtil.getFileAsBufferedReader(p, test, false);
        }
        
        // now try the current model directory
        if (br==null) {     
          if (DBUG_CREATES)System.out.println("[DBUG] Trying '"+mDir+POS_TAG_MODEL+"'");
          br = OpenNLPUtil.getFileAsBufferedReader(p, mDir+POS_TAG_DICT);
        }
        
        // now try the tagger sub-directory 
        if (br==null && getModelDir().indexOf("tagger")<0) { 
          mDir += "tagger/";
          if (DBUG_CREATES)System.out.println("[DBUG] Trying '"+mDir+POS_TAG_MODEL+"'");
          br = OpenNLPUtil.getFileAsBufferedReader(p, mDir+POS_TAG_DICT);
        }
        
        // last chance, replace chunker w' tagger
        if (br==null) {                 
          String hackTest = "chunker/tagger/";
          if (mDir.endsWith(hackTest)) {
            mDir = mDir.substring(0, mDir.length()-hackTest.length())+"tagger/";
            if (DBUG_CREATES)System.out.println("[DBUG] Trying '"+mDir+POS_TAG_MODEL+"'");
            br = OpenNLPUtil.getFileAsBufferedReader(p, mDir+POS_TAG_DICT);
          }
        }  
      }
    }
    else
      br = OpenNLPUtil.getResourceAsBufferedReader(RiPosTagger.class, POS_TAG_DICT); 
    
    if (br != null) 
      return new opennlp.tools.postag.POSDictionary(br, true);
    
    return null;    
  }
  
  private MaxentModel loadTagModel(PApplet p) 
  {
    if(DBUG_CREATES)System.out.println("[DBUG] MaxEntPosTagger loading model");
    
    MaxentModel mm = null;    
    if (LOAD_FROM_MODEL_DIR) {
      String mDir = getModelDir();
      
      // if absolute only try the actual path
      if (RiTa.isAbsolutePath(mDir)) {
        if (DBUG_CREATES)System.out.println("[DBUG] Trying '"+mDir+POS_TAG_MODEL+"'");
        return OpenNLPUtil.getFileAsModel(p, mDir+POS_TAG_MODEL, false);
      }
      
      // if we are in P5, try the default spot
      if (p != null && !RiTa.isAbsolutePath(mDir)) { 
        String test = RiTa.libPath(p)+"rita/models/"+POS_TAG_MODEL;
        if (DBUG_CREATES)System.out.println("[DBUG] Trying default '"+test+"' LIB_PATH="+RiTa.libPath(p));
        mm = OpenNLPUtil.getFileAsModel(p, test, false);
      }
      
      // now try the current model directory
      if (mm==null) {
        if (DBUG_CREATES)System.out.println("[DBUG] Trying '"+mDir+POS_TAG_MODEL+"'");
        mm = OpenNLPUtil.getFileAsModel(p, mDir+POS_TAG_MODEL);
      }
      
      // now try the tagger sub-directory 
      if (mm==null && getModelDir().indexOf("tagger")<0) {        
        mDir += "tagger/";
        if (DBUG_CREATES)System.out.println("[DBUG] Trying '"+mDir+POS_TAG_MODEL+"'");
          mm = OpenNLPUtil.getFileAsModel(p, mDir+POS_TAG_MODEL);          
      }
      
      // last chance, replace chunker w' tagger
      if (mm==null) {
        String hackTest = "chunker/tagger/";
        if (mDir.endsWith(hackTest)) {
          mDir = mDir.substring(0, mDir.length()-hackTest.length())+"tagger/";
          if (DBUG_CREATES)System.out.println("[DBUG] Trying '"+mDir+POS_TAG_MODEL+"'");
          mm = OpenNLPUtil.getFileAsModel(p,mDir+POS_TAG_MODEL);
        }
      }
      if (DBUG_CREATES && mm == null) 
        System.err.println("[WARN] Unable to load tag model: "+POS_TAG_MODEL);             
    }
    else
      mm = OpenNLPUtil.getResourceAsModel(RiPosTagger.class, POS_TAG_MODEL);
    
    return mm;
  }

  public List tag(List tokens)
  {
    return delegate.tag(tokens);
  }
  
  public String tag(String sentence)
  {
    return this.tagInline(RiTa.tokenize(sentence));
  }
  
  public String[] tag(String[] tokens)
  {    
    return delegate.tag(tokens);
  }

  public String tagInline(String[] tokens)
  {
    return RiPosTagger.inlineTags(tokens, tag(tokens));
  }  
  
  public String tagInline(String toTag)
  {
   /* if (RiTa.isServerEnabled()) 
      return proxy.exec(this,"tagInline",toTag); */   
    return tagInline(RiTa.tokenize(toTag));
  }

  public void destroy() {
    model = null;
    delegate = null;
    instance = null;
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
  
  public String[] tagFile(String fileName) {
    throw new RiTaException("tagFile(String) " +
      "is not (as yet) implemented for the statistical tagger");
  }
    
  public static void main(String[] args)
  {
    RiObjectME.setModelDir("/Users/dhowe/Desktop/models/");
    
    String[] toks = {"This,","unfortunately",",","is","your","sad","life","."};
    MaxEntTagger tagger = new MaxEntTagger();
    String[] tags = tagger.tag(toks);
    System.out.println("TAGS: "+RiTa.asList(tags));
    String[] toks2 = {"This","is","your","happy","life","."};
    
    MaxEntTagger tagger2 = new MaxEntTagger();   
    String[] tags2 = tagger2.tag(toks2);
    System.out.println("TAGS: "+RiTa.asList(tags2));
  }


  
}// end
