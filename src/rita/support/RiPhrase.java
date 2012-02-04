package rita.support;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import processing.core.PApplet;
import rita.*;

import com.sun.speech.freetts.*;

/*
 * TODO: implement multi-utterance phrases
 *       store/handle punctuation
 */
/**
 * Utility object for representing phrases which 
 * can be sent to the speech engine.
 * @invisible
 */
public class RiPhrase extends Featured implements CharSequence, RiCharSequence
{  
  private static final String SPC = " ";

  private static RiFreeTTSEngine speechEngine;
  
  private PApplet pApplet;  
  private Utterance delegate;   
  private String[] words;
  
  // --------------------- Constructors ----------------------------
  
  public RiPhrase() 
  {     
    this(null, null, null);
  } 
   
  public RiPhrase(String phrase) throws RiTaException 
  {   
    this(null, phrase);
  }  
  
  public RiPhrase(PApplet p, CharSequence phrase) throws RiTaException 
  {   
    this(p, createDelegate(p, phrase), phrase);
  }
  
  public RiPhrase(PApplet p, Utterance utterance, CharSequence phrase) 
  {  
//System.out.println("RiPhrase.RiPhrase("+utterance+", "+phrase+")");
    this.pApplet = p;
    this.delegate = utterance;
    this.words = phrase.toString().split(SPC); 
    this.features = createFeatureMap(utterance, phrase);    
    this.addFeature(ID, Integer.toString(getId()));
  }
  
  // ----------------------- Methods ------------------------------
  
  /**
   * Returns an RiString (String + Feature-Map) representation of the phrase
   */
  public RiString getRiString()
  {    
    RiString fs = new RiString(getText());
    if (features != null) {
      for (Iterator i = features.keySet().iterator(); i.hasNext();) {
        String key = (String) i.next();
        fs.addFeature(key, (String)features.get(key));
      }
    }
    return fs;
  }    
  
  public Utterance getDelegate()
  {
    return this.delegate;
  }

  protected static Utterance createDelegate(PApplet p, CharSequence phrase) throws RiTaException
  {       
//System.out.println("RiPhrase.createDelegate("+phrase+")");
    
    if (phrase==null || phrase.length()==0) 
      throw new RiTaException("[WARN] Attempt to create "+
          "an utterance from null or zero-length String: \""+phrase+"\"");    
    List l = null;
    try {
      l = getSpeechEngine(p).textToUtterance(phrase.toString().trim());
    }
    catch (RuntimeException e) {
      throw new RiTaException("[ERROR] processing '"+phrase+"'", e);
    }
    
    if (l==null) {
      throw new IllegalArgumentException("[ERROR] Unable " +
        "to create utterance from:\n["+phrase+"] result=null");
    }
    else if (l.size() > 1)  {
      throw new RiTaException("Attempt to create "+
        "utterance from multiple sentences:\n  ["+phrase+"] count="+l.size()+l);
    }        
    return (Utterance)l.get(0);
  }

  private static RiFreeTTSEngine getSpeechEngine(PApplet p)
  {
    if (speechEngine == null) 
      speechEngine = new RiFreeTTSEngine(p, null, null);
    return speechEngine;
  }
  
  public static Map createFeatureMap(PApplet p, CharSequence phrase)
  {
    Utterance utt = createDelegate(p, phrase);
    return createFeatureMap(utt, phrase);
  }  
  
  private static Map createFeatureMap(Utterance u, CharSequence phrase)
  {            
    //System.out.println("RiPhrase.createFeatureMap("+phrase+")");u.dump("  ");
    if (u == null) { 
      System.err.println("[WARN] Null Utterance!");
      return null;
    }
    
    Map result = new HashMap();
    addFeature(result, MUTABLE, "true");     
    addFeature(result, TEXT, phrase);
        
    /* if (!phrase.toString().contains(" ")) { // single words    
         if (singleWordLookup(phrase, result))
           return result; // found it  }*/   
    
    try {
      // Add the tokens
      List tokens = new LinkedList();
      Relation token = u.getRelation(Relation.TOKEN);
      Item i = token.getHead(); 
      while (i != null) {  
        tokens.add(i.toString().trim());
        i = i.getNext();
      }     
      addFeature(result, TOKENS, asFeature(tokens));
 
      // Grab stress, syllable, & phone features
      StringBuilder stresses = new StringBuilder();
      StringBuilder phonemes = new StringBuilder();
      StringBuilder syllables = new StringBuilder();

      // Per-word holders for features (reused) 
      StringBuilder sylbuf = new StringBuilder(8);
      StringBuilder phones = new StringBuilder(8);
      StringBuilder stress = new StringBuilder(8);
          
      Item item = u.getRelation(Relation.SYLLABLE_STRUCTURE).getHead();            
      while (item != null) // each word
      {            
        String lemma = item.toString().trim();
//System.err.println("LEMMA: '"+lemma+"'");
        if (lemma.length()<1) {
          item = item.getNext();
          continue;        
        }
        
        if (item.hasDaughters()) 
        {
          Item d = item.getDaughter();        
          int daughterCount = 1;

          while (d != null) // each syllable 
          {         
            int k = 1;
            Item dd = d.getDaughter();
            while (dd != null)  // each phoneme 
            {
              // add the syllable and phone data
              sylbuf.append(dd);
              sylbuf.append(Featured.PHONEME_BOUNDARY);           
              phones.append(dd);
              phones.append(Featured.PHONEME_BOUNDARY);            
              dd = d.getNthDaughter(k++);          
            }
            
            // add the stress data
            stress.append(d.getFeatures().getString("stress"));
            stress.append(Featured.SYLLABLE_BOUNDARY);             
            
            // and the syllable boundary
            sylbuf.deleteCharAt(sylbuf.length()-1);
            sylbuf.append(Featured.SYLLABLE_BOUNDARY);
            d = item.getNthDaughter(daughterCount++);
          }
        }
        // ------------------------------------------------------------
        // TODO: this can be optimized by checking the lexicon feature   
        // cache for these 2 features instead of generating them 
        // ------------------------------------------------------------
        //System.err.println("BUF: '"+sylbuf+"'");
        
        sylbuf.deleteCharAt(sylbuf.length()-1);
        syllables.append(sylbuf);
        syllables.append(Featured.WORD_BOUNDARY);

        phones.deleteCharAt(phones.length()-1);
        phonemes.append(phones);
        phonemes.append(Featured.WORD_BOUNDARY);
 
        stress.deleteCharAt(stress.length()-1);
        stresses.append(stress);
        stresses.append(Featured.WORD_BOUNDARY);
                   
        //if (RiTaLexicon.isCaching()) // cache each new word (LTS) here?
          //RiTaLexicon.addToFeatureMap();
        
        sylbuf.delete(0, sylbuf.length());
        phones.delete(0, phones.length());
        stress.delete(0, stress.length());
        
        item = item.getNext();
      }   
      
      stresses.deleteCharAt(stresses.length()-1);
      addFeature(result, STRESSES, stresses.toString());
      
      phonemes.deleteCharAt(phonemes.length()-1);
      addFeature(result, PHONEMES, phonemes.toString());//p=asFeature(phonemes));
      
      syllables.deleteCharAt(syllables.length()-1);
      addFeature(result, SYLLABLES, /*asFeature*/(syllables.toString()));    
      
      String[] toks = new String[tokens.size()];  
      addPosFeature((String[])tokens.toArray(toks), result);
    } 
    catch (Throwable e) {
      throw new RiTaException("Unable to create feature map " +
      		"for: '"+phrase+"'! Perhaps a bad character?\n"+e+"\n");
    }    
      
    return result;
  }

  // not-used at moment
  private static boolean singleWordLookup(CharSequence phrase, Map result) {
    Map lookup = RiLexiconImpl.getInstance().getFeatures(phrase.toString());
    if (lookup != null) {
      //System.out.println("adding features: ");
      for (Iterator it = lookup.keySet().iterator(); it.hasNext();) {
        String key = (String) it.next();
        result.put(key, lookup.get(key));       
        //System.out.println("  key="+key);
      }
      String poslist = (String)lookup.get("poslist");        
      addFeature(result, POS, poslist.split(Featured.WORD_BOUNDARY)[0]);
      addFeature(result, TOKENS, phrase);
      return true;
    }
    return false;
  }

/*  private static void addToFeatureList(List featureList, StringBuilder feature)
  {
    if (feature == null || feature.length() < 1) return;
    String fs = feature.substring(0, feature.length()-1);
    featureList.add(fs);
    feature.delete(0, feature.length());
    feature=null;
  }*/
  
  // not-used
  private void validatePhones(List phonemes)
  {
    if (phonemes == null || phonemes.size()==1) return;
    //System.err.println("Phrase.validatePhones("+phonemes+")");
    for (Iterator i = phonemes.iterator(); i.hasNext();)
    {
      String phoneStr = (String) i.next();
      if (phoneStr == null || phoneStr.length() == 0)
        continue;
      String[] phones = phoneStr.split(":");
      for (int j = 0; j < phones.length; j++){
        //System.out.println("phone: "+phone);
        if (!RiPhone.isPhoneme(phones[j]))
          throw new RiTaException("Invalid Phoneme: '"+phones[j]+"' in "+getText());
      }
    }
  }   
  
  // FINISH THIS METHOD
  /** 
   * Replaces all instances of the specified word with 'newWord'
   * @return the # of words successfully replaced 
  public int replaceWord(String wordToReplace,  String newWord)
  {    
    return true;
  }*/  
  
  /**
   * Replaces the word at index 'wordNum' with 'newWord'
   * As usual, the first word is at index 0.
   * @param newWord
   * @param wordNum
   * @return true if the replacement was successful
   */
  public boolean replaceWordAt(int wordNum, String newWord)
  {
    if (wordNum < 0 || newWord == null) return false;
    
    String punc = "";
    Matcher m = punct.matcher(words[wordNum]);
    if (m.matches()) {
      punc = m.group(0); 
    }
    
    words[wordNum] = newWord + punc;
    
    this.rebuildFromTokens();
    
    return true;
  }
  static Pattern punct = Pattern.compile("\\p{Punct}$");
  
  /**
   * Replaces any word matching the 'pos' of 'newWord' 
   * (provided by the default tagger) with 'newWord'
   * starting at a random location in the string.
   * @param newWord
   * @deprecated
   * @return the index of the replaced word on success or -1 on failure

  public int replaceAny(String newWord)
  {
    String pos = PosTagger.getInstance().tag(newWord);
    return this.replaceAny(newWord, pos);
  }   */
  
  /**
   * Replaces any word matching the 'pos' of 'newWord' 
   * (provided by the default tagger) with 'newWord'
   * starting at a random location in the string.
   * @param newWord
   * @param wordIdx
   * @return the index of the replaced word on success or -1 on failure
   * @deprecated

  public int replaceAny(String newWord, int wordIdx)
  {
    String pos = PosTagger.getInstance().tag(newWord);
    return this.replaceAny(newWord, pos, wordIdx);
  }   */
  

  /**
   * Replaces a single word with the given 'pos' by 'newWord'
   *  starting at a random location in the string.
   * @param newWord
   * @param pos
   * @return true if the replacement was successful
   */
  public int replaceAny(String pos, String newWord)
  {
    String[] toks = getWords();
    int randomStartIdx = (int)(Math.random()*toks.length);
    return this.replaceAny(randomStartIdx, newWord, pos);
  }
  
  /**
   * Replaces any word with the given 'pos' by 'newWord'
   * starting at word at 'wordIdx' in the string.
   * @param newWord
   * @param wordIdx
   * @param pos
   * @return true if the replacement was successful
   */
  public int replaceAny(int wordIdx, String pos, String newWord)
  {    
    if (newWord == null || pos == null)
      throw new IllegalArgumentException("null arg");
    
    //System.out.print("PReplace: "+newWord+"/"+pos+") in: "+toString()+" - ");
    
    int counter = 0;
    String[] toks = getTokenArray();
    for (; counter < toks.length; counter++)
    {
      String tok = getWordAt(wordIdx);

      if ((getPosAt(wordIdx).equalsIgnoreCase(pos)) &&
          (!tok.equalsIgnoreCase(newWord))) 
      {
        if (replaceWordAt(wordIdx, newWord)) {
          //System.out.println(" OK!");
          return wordIdx;
        }
        //System.out.println("  Fail: "+tok.lemma);
      }
      if (++wordIdx == toks.length) wordIdx = 0;
    }
    //System.out.println("FAILED!");
    return -1;
  }

  public RiPhrase copy()
  {
    if (1==1) throw new RuntimeException("re-implement me!!!");
    RiPhrase p = new RiPhrase();
    p.delegate = delegate;
    //p.id = id;
    return p;
  }

  private void rebuildFromTokens() throws RiTaException 
  {            
    String s = RiTa.join(getWords(), WORD_BOUNDARY);
    this.delegate = createDelegate(pApplet, s);
    this.features = createFeatureMap(delegate, s);//, tokens, punct);
  }

  public int syllableCount()
  {    
    return getSyllables().split(Featured.WORD_BOUNDARY).length;    
  }

  /**
   * Returns the index of the last token matching <code>word</code>
   * or -1 if not found
   */
  public int lastIndexOf(String word)
  {
    int j = -1;    
    String[] toks = getTokenArray();
    for (int i = 0; i < toks.length; i++) {
      if (toks.equals(word))
        j = i;
    }
    return j;
  }
  
  
  // ------------------ Delegate Methods ---------------------
  
  /**
   * @param obj
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj)
  {
    return getWords().equals(((RiPhrase)obj).getWords());
  }

  /**
   * @param speakable
   * @see com.sun.speech.freetts.Utterance#setSpeakable(com.sun.speech.freetts.FreeTTSSpeakable)
   */
  public void setSpeakable(FreeTTSSpeakable speakable)
  {
    this.delegate.setSpeakable(speakable);
  }
  
 /* public void dumpRelations()
  {
    this.delegate.dumpRelations(id+"");
  }*/

  public void dumpFeatures()
  {
    System.out.println(getFeatureString());
  }
  
  public String getFeatureString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("-------------------------------------");
    sb.append("\nPhrase:    '"+this.getText()+"'");      
    sb.append("\nTokens:    '"+getFeature(TOKENS)+"'");
    sb.append("\nStresses:  '"+this.getStresses()+"'");
    sb.append("\nPhonemes:  '"+this.getPhonemes()+"'");
    sb.append("\nSyllables: '"+this.getSyllables()+"'");
    sb.append("\nPosTags:   '"+getFeature(POS)+"'");
    return sb.toString();
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {    
    return getText();
  }
  
  public String getText()
  {
    return getFeature(TEXT);
  }

  /* (non-Javadoc)
   * @see rita.nlg.RiWordSequence#wordAt(int)
   */
  public String getWordAt(int i)
  {
    String[] toks = getTokenArray();
    return i >= toks.length ? null : toks[i];    
  }
  
  /**
   * Returns closest token to 'pos' of char in Phrase
   */ 
  public String wordAtCharPos(int pos)
  {    
    return getWordAt(wordIdxAtCharPos(pos));
  }
  
  /**
   * Returns index of the word closest to <code>position</code> in the Phrase
   */ 
  public int wordIdxAtCharPos(int position)
  {
    if (position < 0 || position > charCount()-1) return -1;
    
    //int idx = 0;   
    int offset = -1;    
    String[] toks = getWords(); // getTokens?
    for (int i = 0; i < toks.length; i++)   {
      int startPos = offset;
      int endPos = (offset + toks[i].length());     
      if (position >= startPos && position <= endPos)
        return i;     
      offset = endPos + 1;
    }
    
    return -1;
  }

  /* (non-Javadoc)
   * @see rita.nlg.RiWordSequence#wordCount()
   */
  public int getWordCount()
  {
    return getTokenArray().length;
  }
  
  /*
   * Returns index of all tokens matching word
   * or null if none are found
   */
  public int[] indexOf(String word) // TODO: OPT
  {    
    List l = new LinkedList();
    String[] toks = getTokenArray();
    for (int i = 0; i < toks.length; i++)   {
      if (toks[i].equalsIgnoreCase(word))
        l.add(new Integer(i));
    }
    
    if (l.size() == 0) return null;
    
    int idx = 0;
    int[] result = new int[l.size()];
    for (Iterator iter = l.iterator(); iter.hasNext();)
      result[idx++] = ((Integer) iter.next()).intValue();
    
    return result;
  }
  
  /**
   * Returns index of first token matching word
   * or -1 if not found
   */
  public int firstIndexOf(String word)
  {
    String[] toks = getTokenArray();
    for (int i = 0; i < toks.length; i++)   {
      //System.out.println("  testing: "+tok);
      if (toks[i].equals(word))
        return i;
    }
    return -1;
  }
  
  public int charCount()
  {
    return getText().length();
  } 

  public String[] getWords()
  { 
    return words;
  }
    
  public String getPosStr()
  {  
    String posStr = getFeature(POS);
    if (posStr == null)       
      posStr = addPosFeature();      
    return posStr;
  }

  public String addPosFeature()
  {  
    //List l = RiTa.asList();
    return addPosFeature(getTokenArray(), features);
  }
  /*
  public static String addPosFeature(List toks, Map featureMap)
  {
    List tags = RiTa.posTag(toks);
    String posStr = RiTa.join(tags, WORD_BOUNDARY).toLowerCase();
    addFeature(featureMap, POS, posStr);
    return posStr;
  }*/
  
  public static String addPosFeature(String[] toks, Map featureMap)
  {
    String[] tags = RiTa.posTag(toks);
    String posStr = RiTa.join(tags, WORD_BOUNDARY).toLowerCase();
    addFeature(featureMap, POS, posStr);
    return posStr;
  }
  
  /* (non-Javadoc)
   * @see rita.nlg.RiWordSequence#getPos()
   */
  public String[] getPos()
  {          
    return getPosStr().split(WORD_BOUNDARY);
  }
  
  /* (non-Javadoc)
   * @see rita.nlg.RiWordSequence#posAt(int)
   */
  public String getPosAt(int wordIdx)
  {
    String[] pos = getPos();
    if (pos == null || pos.length==0 || wordIdx >= pos.length)
      return null;
    return pos[wordIdx];
  }
  
  public String getStresses() 
  {
    return getFeature(STRESSES);   
  }
  
  public String getSyllables()
  {
    return getFeature(SYLLABLES);
  }

  public String getPhonemes()
  {
    return getFeature(PHONEMES);
  }
  
  public String[] getPhonemeArray()
  {
    return getFeature(PHONEMES).split(WORD_BOUNDARY);
  }  
  
  public String getTokens()
  {   
    return getFeature(TOKENS);   
  }
  
  public String[] getTokenArray()
  {   
    return getFeature(TOKENS).split(WORD_BOUNDARY);   
  }
  
  public boolean isMutable()
  {
    String mutable = getFeature("mutable");
    if (mutable != null && mutable.equals("false"))
      return false;
    return true;
  }

  public void setMutable(boolean mutable)
  {
    this.addFeature("mutable", mutable ? "true" : "false");
  }
/*
  public void speak()
  {    
    getSpeechEngine(pApplet).speak(toString());
  }*/
  
  /**
   * Returns a String representing the pos for the first index of 
   * <code>word</code> in the phrase, or null if there is no such word.
   * @see #firstIndexOf(String)
   */
  public String getPos(String word)
  {
    int idx = firstIndexOf(word);
    if (idx < 0) return null;
    String[] pos = this.getPos();
    if (pos == null || pos.length==0)
      return null;
    return pos[idx];
  }
  
  public char charAt(int index)
  {
    return getText().charAt(index);
  }

  /**
   * Returns the number of characters in the phrase
   */
  public int length()
  {
    return getText().length();
  }

  public CharSequence subSequence(int start, int end)
  {
    return getText().subSequence(start, end);
  }

  public boolean insertWordAt(String newWord, int wordIdx)
  {
    throw new RuntimeException("Oops, unimplemented for "+getClass());
  }
  
  public int numWords()
  {
    return getTokenArray().length;
  }

  // ----------------------------------------------------------------
  
  public static void main(String[] args) throws Exception
  {
    // alone once more
        
    String text = "The boy ran after the fat Blaupunkt.";
    text = "Hello from RiTa";
    //text = "hello";
    //text = "blaupunkt";
    //text = "The dog";
    //RiPosTagger.setDefaultTagger(RiPosTagger.MAXENT_POS_TAGGER);
    //ext = "type a phrase here";
    
    //RiLexiconImpl rl = RiLexiconImpl.getInstance();
    //long start = System.currentTimeMillis();
    RiPhrase r = new RiPhrase("a");//The boy ate. He asked why.");
    System.out.println(r.getFeatures());
    ///RiTa.pElapsed(start);
    /*r.replaceWordAt(1, "girl");
    //r.replaceWord("girl", "cat");
    r.dumpFeatures();
    //r.speak();
    r = new RiPhrase("deliverance");
    r.dumpFeatures();*/
    
  }

}// end
