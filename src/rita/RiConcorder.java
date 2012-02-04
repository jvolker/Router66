package rita;

import java.util.*;

import processing.core.PApplet;
import rita.support.*;

/**
 * Maintains a simple word frequency table for a set of input data<pre>
    RiConcorder ric = new RiConcorder(this);
    ric.setIgnoreCase(false);
    ric.setIgnoreStopWords(false);
    ric.setIgnorePunctuation(false);
    ric.loadFile("myTestFile.txt");
    ric.dump();
    String[] mostCommon = ric.getMostCommonTokens(5);
    print(mostCommon);
    </pre>
 * @author dhowe 
 * @invisible
 */
public class RiConcorder extends RiObject
{  
  protected static boolean DEFAULT_IGNORE_CASE = false;  
  
  protected boolean ignoreStopWords = false;  
  protected boolean ignorePunctuation = false;       
    
  protected int totalWordCount;
  protected RiTokenizer tokenizer;
  protected String[] wordsToIgnore;
  protected TextNode root; 
  
  protected List sorted; // yuk
      
  /**
   * Constructs a new RiConcorder using the specified 
   * <code>tokenizer</code> ands loads it with the data 
   * in <code>fileName(s)</code>.
   */
  public RiConcorder(PApplet pApplet, String[] fileNames, RiTokenizer tokenizer) {
    super(pApplet);
    this.root = TextNode.createRoot(DEFAULT_IGNORE_CASE);
    if (fileNames != null) this.loadFiles(fileNames);
    this.tokenizer = tokenizer;
  }
  
  /**
   * Constructs a new RiConcorder ands loads
   * it with the data in <code>fileName</code>.
   */
  public RiConcorder(PApplet pApplet, String fileName) {
    this(pApplet, new String[]{fileName});
  }
  
  /**
   * Constructs a new RiConcorder ands loads
   * it with the data in <code>fileName</code>.
   */
  public RiConcorder(PApplet pApplet, String[] fileNames) {
    this(pApplet, fileNames, RiTokenizer.getInstance(pApplet));
  }
  
  /**
   * Constructs a new RiConcorder using
   * the specified <code>tokenizer</code>
   */
  public RiConcorder(PApplet pApplet, RiTokenizer tokenizer) {
    this(pApplet, null, tokenizer);
  }
  
  /**
   * Constructs a new RiConcorder
   */
  public RiConcorder(PApplet pApplet) {
    this(pApplet, (String[])null);
  }  
  
  
  /**
   * Constructs a new RiConcorder
   * @invisible
   */
  public RiConcorder() {
    this(null, (String[])null);
  } 
  
  /**
   * Constructs a new RiConcorder using
   * the specified <code>tokenizer</code>
   * @invisible
   */
  public RiConcorder(RiTokenizer tokenizer) {
    this(null, null, tokenizer);
  }

  // ========================= METHODS =============================
  
  /**
   * Loads the data from the files into a single frequency table
   */
  public void loadFiles(String[] fileNames) {
    sorted = null;
    for (int f = 0; f < fileNames.length; f++) {
      String[] lines = RiTa.loadStrings(_pApplet, fileNames[f]);
      for (int i = 0; i < lines.length; i++)
        addLine(lines[i]);
    }    
  } 
    
  /**
   * Tells the model to ignore this set of words
   */
  public void setWordsToIgnore(String[] wordsToIgnore)
  {      
    sorted = null;
    this.ignoreStopWords = true;
    this.wordsToIgnore = wordsToIgnore;
  }
  
  /**
   * Add the data from a single line into the frequency table
   */
  public void addLine(String line)
  {       
    sorted = null;
    String[] words = null;
    if (line == null || line.length()<1)
      return;
    words = tokenizer.tokenize(line);
    if (words == null || words.length<1)
      throw new RiTaException("Error parsing line: "+line);    
    this.addWords(words);
  }

  /**
   * Returns the # of occurences of <code>word</code>
   * or 0 if the word does not exist in the table.
   */
  public int getCount(String word)
  {
    int count = 0;
    TextNode wf = root.lookup(word);
    if (wf != null) count = wf.getCount();
    return count;
  }
  
  /**
   * Returns the normalized frequency (probability) of <code>word</code>,
   * 1 if it is the only word in the model, 0 if it does not exist.
   */
  public float getProbability(String word)
  {
    TextNode wf = root.lookup(word);
    return wf != null ? wf.getProbability() : 0;
  }

  /**
   * Returns the <code>numberToReturn</code> words with the highest frequency.
   * If there are less than <code>numberToReturn</code> words then all items
   * are returned.
   */
  protected TextNode[] mostCommonTokenNodes(int numberToReturn)
  {
    TextNode[] result = new TextNode[Math.min(numberToReturn, root.numChildren())];
    if (sorted == null) createSort();
    for (int i = 0; i < result.length; i++){
      result[i] = (TextNode)sorted.get(i);}
    return result;
  }
  
  /**
   * Returns the <code>numberToReturn</code> words with the highest frequency.
   * If there are less than <code>numberToReturn</code> words then all items
   * are returned.
   */
  public String[] getMostCommonTokens(int numberToReturn)
  {
    String[] result = new String[Math.min(numberToReturn, root.numChildren())];
    if (sorted == null) createSort();
    for (int i = 0; i < result.length; i++)
      result[i] = ((TextNode)sorted.get(i)).getToken();
    return result;
  }
  
  /**
   * Returns the <code>numberToReturn</code> words with the highest frequency.
   * If there are less than <code>numberToReturn</code> words then all items
   * are returned.
   */
  public String[] getLeastCommonTokens(int numberToReturn)
  {
    String[] result = new String[Math.min(numberToReturn, root.numChildren())];
    if (sorted == null) createSort();
    for (int i = 0; i < result.length; i++)
      result[i] = ((TextNode)sorted.get(root.numChildren()-i-1)).getToken();
    return result;
  }
  
  /**
   * Returns the <code>numberToReturn</code> words with the lowest frequency.
   * If there are less than <code>numberToReturn</code> words then all items
   * are returned.
   */
  protected TextNode[] getLeastCommonNodes(int numberToReturn)
  {
    TextNode[] result = new TextNode[Math.min(numberToReturn, root.numChildren())];
    if (sorted == null) createSort();
    int count = result.length;
    for (int i = 0; i < count; i++){
      result[i] = (TextNode)sorted.get(root.numChildren()-i-1);}
    return result;
  }
  
  
  private void createSort()
  {
    if (sorted == null)
      sorted = new ArrayList(root.numChildren());
    sorted.addAll(root.getChildNodes());
    Collections.sort(sorted);
  }

  /**
   * Returns the total # of entries in the model.  
   */
  public int totalCount()
  { 
    return  totalWordCount;
  }
  
  /**
   * Returns the # of unique words in the model.  
   */
  public int uniqueCount()
  { 
    return root.numChildren();
  }
  
  /**
   * Adds the <code>words</code>to the model, incrementing
   * their counts (and the total-count) for each.
   */
  public void addWords(String[] words)
  {
    sorted = null;
    if (words == null || words.length < 1)
      return;
    for (int i = 0; i < words.length; i++)
      addWord(words[i]);
  }
  
  /**
   * Adds a single word to the model with a count of 1 if
   * it does not yet exist, else increments its count by 1.
   */
  public void addWord(String word)
  {           
    sorted = null;
    if (word == null) return;
    
    word = word.trim();       
        
    if (ignorePunctuation)
      word = RiTa.trimPunctuation(word);

    if (word.length() < 1) return;
      
    // skip single characters not letters or numbers
    if (ignorePunctuation && word.length() == 1) {    
      if (!Character.isLetterOrDigit(word.charAt(0))) {
        //System.err.println("SKIPPING: "+word);
        return;
      }
    }           
    
    if (ignoreStopWords) {
      if (RiTa.contains(word, RiTa.STOP_WORDS, isIgnoringCase()))
        return;
    }
    
    if (wordsToIgnore != null) {
      if (RiTa.contains(word, wordsToIgnore, isIgnoringCase()))
        return;
    }
    
    if (word.indexOf(' ')>-1) 
      throw new RiTaException("addWord(String) accepts " +
        "only single words, but received phrase: '"+word+"'");
        
    // add new word to the model
    root.addChild(word);
    
    totalWordCount++;
  }
  
  /**
   * True if the concordance contains <code>word</code>, else false
   */
  public boolean contains(String word)
  {
    return root.lookup(word) != null;
  }

  /**
   * Loads the data from the file into a frequency table
   */
  public void loadFile(String fileName)
  {
    sorted = null;
    this.loadFiles(new String[]{ fileName });    
  }
  
  /**
   * Clears the model, resets variables, and prepares it for reloading
   * with new data
   */
  public void clear()
  {
    totalWordCount = 0;
    root.getChildNodes().clear();
    sorted = null;
  }
  
  /** @invisible                      */
  public void dump()
  {
    float total = 0;
    
    if (sorted == null) createSort();
    for (Iterator i = sorted.iterator(); i.hasNext();) {
      TextNode wf = (TextNode) i.next();
      float f = getProbability(wf.getToken());
      System.out.println(wf+ " -> "+f);      
      total += f;
    }    
    System.out.println("\nModel.wordCount="+totalWordCount+" probability sums to: "+total);
  }    
    
  
  /**
   * Returns whether the model is ignoring case by considering
   * all words as lowerCase (default=false)
   */
  public boolean isIgnoringCase()
  {
    return root.isIgnoringCase();
  }

  /**
   * Sets whether the model should ignore case (default=false), 
   * treating all tokens as lower-case
   */
  public void setIgnoreCase(boolean ignoreCase)
  {
    root.setIgnoreCase(ignoreCase);
  }

  /**
   * Returns whether the model is ignoring stopWords  (default = false)
   * @see RiTa#STOP_WORDS
   */
  public boolean isIgnoringStopWords()
  {
    return this.ignoreStopWords;
  }

  /**
   * Sets whether the model should ignore stopWords  (default = false)
   * @see RiTa#STOP_WORDS
   */
  public void setIgnoreStopWords(boolean ignoreStopWords)
  {
    this.ignoreStopWords = ignoreStopWords;
  }

  /**
   * Returns whether the model is ignoring punctuation  (default = true)
   * @see RiTa#STOP_WORDS
   */
  public boolean isIgnoringPunctuation()
  {
    return this.ignorePunctuation;
  }

  /**
   * Sets whether the model should ignore punctuation (default = true)
   */
  public void setIgnorePunctuation(boolean ignore)
  {
    this.ignorePunctuation = ignore;
  }
  
  public static void main(String[] args)
  {
    //RiTokenizer rt = RiTokenizer.getInstance();//"\\b");
    RiConcorder ric = new RiConcorder((PApplet)null);//,rt);
    ric.addWords(new String[] {"hello","hello","goodbye"} );
    ric.dump();
    if (1==1) return;
    ric.setIgnoreCase(false);
    ric.setWordsToIgnore(new String[]{"I"});
    ric.setIgnoreStopWords(false);
    ric.setIgnorePunctuation(true);
    String[] lines = RiTa.loadStrings(null,"bang.txt");
    for (int i = 0; i < lines.length; i++)
      ric.addLine(lines[i]);
    ric.dump();
    System.out.println(Arrays.asList(ric.getMostCommonTokens(5)));      
  }
  
}//
