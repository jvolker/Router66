package rita.support;

import java.io.PrintStream;
 
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import processing.core.PApplet;




import rita.RiTa;
import rita.RiTaException;
import rita.support.ifs.RiMarkovIF;
import rita.support.ifs.RiTokenizerIF;
import rita.support.remote.RiRemotable;

/*
 * BUG: 
 * w' myTestFile2.txt:
 *   'Then the chief clerk called Good morning, I sit before the green window and the open crack in the door. Wilson.'
 *   'On the quiet and open morning, the cats sit before the green window and the open crack in the door. Wilson, who called me silly.
 * w' kafka.txt:
 *   ', in the entire flat and especially in the kitchen and just reads the paper or studies train timetables.'
 *   ', so don't keep trying to do it - although that much dampness also made Gregor ill and he lay flat on the couch, of course, moving it an'
 inch.
 */
/**
 * Represents a Markov chain (or n-Gram) model for probabilistic text generation. 
 */
public class MarkovModel extends RiRemotable implements RiMarkovIF
{   
  private static final String SS_DELIM = "D=l1m";
  
  public static String SS_REGEX = "\"?[A-Z][a-z\"',;`-]*";
  private static final boolean DEFAULT_ALLOW_DUPLICATES = true;

  /** constant for default value of ignore-case */
  public static final boolean DEFAULT_IGNORE_CASE = false;
  private static final int MAX_PROB_MISSES = 100;
  
  /** constant for max # of tries for a generation */
  public static int MAX_GENERATION_ATTEMPTS = 1000;

  private RiTextNode root;
  private Stack pathTrace; 
  private Set sentenceList;  
  public List sentenceStarts;  
  private RiTokenizerIF tokenizer;
  
  private int nFactor, wordsPerFile, wordCount;
  private int minSentenceLength=6, maxSentenceLength=35; 
  
  private boolean removeQuotations = true;    
  private boolean recognizeSentences = true;
  private boolean useSmoothing, addSpaces = true;
  private boolean ignoreCase, allowDuplicates, profile=true;

  private boolean printIgnoredText = false;
  public static int maxDuplicatesToSkip = 10000;
  private int skippedDups = 0;
  
  /**
   * Construct a sentence-generating Markov chain (or n-gram) model 
   * and set its n-Factor and ignoreCase flag 
   */
  public MarkovModel(PApplet parent, int nFactor, boolean ignoreCase) {
    super(parent);        
    this.nFactor = nFactor;
    this.ignoreCase = ignoreCase;   
    allowDuplicates = DEFAULT_ALLOW_DUPLICATES;
    this.tokenizer = new PennWordTokenizer(false); // don't split contractions 
    this.root = TextNode.createRoot(ignoreCase);
  }
  
  public boolean isPrintingIgnoredText() {
    return printIgnoredText;
  }

  public void setPrintIgnoredText(boolean printIgnoredText) {
    this.printIgnoredText = printIgnoredText;
  }

  /**
   * Construct a sentence-generating Markov chain (or n-gram) model 
   * and set its n-Factor and ignoreCase flag 
   */
  public static MarkovModel createRemote(Map params)
  {    
    int n = Integer.parseInt((String)params.get("nFactor"));
    boolean ic = DEFAULT_IGNORE_CASE;
    if (params.containsKey("ignoreCase")) 
      ic = Boolean.parseBoolean((String)params.get("ignoreCase"));     
    return new MarkovModel(null, n, ic);    
  }

  // METHODS ----------------------------------------------------------

  /**
   * Creates a new RegexTokenizer from the supplied regular expression
   * and uses it when adding subsequent data to the model.
   */
  public void setTokenizerRegex(String regex) {
    setTokenizer(new RegexTokenizer(regex)); 
  }
  
  /**
   * Determines whether calls to generateSentence(s) will return 
   * sentences that exist (character-for-character) in the input text.<p>
   * Note: The trade-off here is between ensuring novel outputs
   * and a potential slow-down due to rejected outputs (b/c they
   * exist in the input text.)  
   */
  public void setAllowDuplicates(boolean allow)
  {
    if (allowDuplicates && !allow && root != null && root.hasChildren())
      throw new RiTaException("Illegal attempt to set " +
      	"allowDuplicates=false after adding data.\n  " +
        "        Call this method before add any data.\n");
    this.allowDuplicates = allow;
  }

  public boolean isAllowingDuplicates()
  {
    return this.allowDuplicates;
  }
  
  /**
   * Continues generating tokens until a token matches 'regex', assuming
   * the length of the output is between min and maxLength (inclusive).
   */
  public String generateTokensUntil(String regex, int minLength, int maxLength)
  { 
    boolean bug = false;
    
    int tries = 0, maxTries = 100;
    List tokens = new ArrayList();       
    OUT: while (++tries < maxTries) {
      System.out.println("  TRY # "+tries+"--------------------");
      RiTextNode mn = root.selectChild();
      if (mn == null || mn.getToken() == null)
        continue OUT;
      tokens.add(mn);
      while (tokens.size() < minLength) {     
        mn = nextNode(tokens);
        if (mn == null || mn.getToken() == null) { // hit the end
          //System.err.println("FAILED: "+tokens);
          System.out.println("  NULL TOKEN after: "+tokens);
          tokens.clear(); // start over
          continue OUT;
        }        
        tokens.add(mn); 
      }
      // minLength is ok, look for an ender
      //System.out.println("  GOT MIN-LENGTH: "+tokens);
      
      String tok = mn.getToken();
      System.out.println("    CHECKING: "+mn);
      if (tok.matches(regex)) {
        System.out.println("    OK (after "+tries+")\n--------------------");
        break;
      }
      if (tokens.size() > maxLength) {
        System.out.println("    GIVING UP: "+tokens+"\n--------------------");
        tokens.clear();
        continue;
      }
    }
    
    //System.out.println("tokens="+tokens.size());
    
    // uh-oh, looks like we failed...
    if (tries >= maxTries) 
      onGenerationIncomplete(tries, tokens.size());
    
    String result = "";   
    for (Iterator i = tokens.iterator(); i.hasNext();)
    {
      RiTextNode tn = (RiTextNode) i.next();        
      if (tn.getToken() == null)  {
        continue;
      }
      result += tn.getToken();
      if (i.hasNext() && addSpaces)
        result += " ";      
    }
    return result;
  }
  
  /**
   * Generates a string of <pre>length</pre> tokens from the model.
   */
  public String generateTokens(int targetNumber)
  {       
    int tries = 0, maxTries = 100;
    List tokens = new ArrayList();       
    OUT: while (++tries < maxTries) {
      RiTextNode mn = root.selectChild();
      if (mn == null || mn.getToken() == null)
        continue OUT;
      tokens.add(mn);
      while (tokens.size() < targetNumber) {     
        mn = nextNode(tokens);
        if (mn == null || mn.getToken() == null) { // hit the end
          //System.err.println("FAILED: "+tokens);
          tokens.clear(); // start over
          continue OUT;
        }        
        tokens.add(mn);        
      }
      break;
    }
    
    //System.out.println("tokens="+tokens.size());
    
    // uh-oh, looks like we failed...
    if (tokens.size() < targetNumber) 
      onGenerationIncomplete(tries, tokens.size());
    
    String result = "";   
    for (Iterator i = tokens.iterator(); i.hasNext();)
    {
      RiTextNode tn = (RiTextNode) i.next();        
      if (tn.getToken() == null)  {
        continue;
      }
      result += tn.getToken();
      if (i.hasNext() && addSpaces)
        result += " ";      
    }
    return result;
  }

  private void onGenerationIncomplete(int tries, int successes) {
    System.err.println("\n[WARN] MarkovModel failed to complete after "+tries+" tries\n       Giving up after only "+successes+" successful generations...\n");
    //throw new RiTaException("Unable to generate the requested length after "+MAX_GENERATION_ATTEMPTS+" tries");after
    
  }

  // Methods -------------------------------------------------------
  private Set loadedFiles = new HashSet();
  /**  
   * Load a text file into the model -- if using Processing,
   * the file should be in the sketch's data folder. 
   * @param fileName name of file to load
   * @param multiplier weighting for tokens in the file;<br>  
   * a weight of 3 is equivalent to loading that file 3 times and gives
   * each token 3x the probability of being chosen during generation.
   */
  public void loadFile(String fileName, int multiplier) 
  {      
    long done, start = System.currentTimeMillis();  
    if (loadedFiles.contains(fileName)) {     
      if (!RiTa.SILENT)System.out.println("[INFO] Attempt to reload file: "+fileName+" ignored...");
      return;
    }
    loadedFiles.add(fileName);
    String contents = RiTa.loadString(_pApplet, fileName);
    
    if (profile) {
      done = System.currentTimeMillis()-start;
      if (!RiTa.SILENT)System.out.println("[INFO] Loaded '"+fileName+"' ("+
        contents.length()+" chars) in "+done/1000d+"s");
      start = System.currentTimeMillis();
    }
    
    this.loadText(contents, multiplier);
    
    if (profile) {
      done = System.currentTimeMillis()-start;
      if (!RiTa.SILENT)
        System.out.println("[INFO] Loaded data into model in "+done/1000d+"s");
    }
  }  

  public void loadFile(String fileName) 
  {
    this.loadFile(fileName, 1);    
  }
  
  public void loadText(String rawText) 
  {
    this.loadText(rawText, 1);
  }
  
  /** 
   * Load a String into the model, splitting the text first into sentences,
   * then into words, according to the current regular expression. 
   * 
   * @param multiplier Weighting for tokens in the String <br>  
   * 
   * A weight of 3 is equivalent to loading the text 3 times and gives
   * each token 3x the probability of being chosen during generation.
   */
  public void loadText(String rawText, int multiplier) 
  {       
    if (recognizeSentences) {
      String[] sents= RiTa.splitSentences(rawText);
//for (int i = 0; i < sents.length; i++)
//  System.out.println(i+") "+sents[i]);
      loadSentences(sents, multiplier);       
    }
    else {       
      loadTokens(tokenizer.tokenize(rawText), multiplier);      
    }
  }
  
  /**
   * Returns the # of words loaded into the model
   */
  public int getWordCount()
  {
    return wordCount;
  }
  
  /**
   * Loads an array of tokens (or words) into the model; each 
   * element in the array must be a single token for proper 
   * construction of the model. 
   * @param multiplier Weighting for tokens in the array <br>
   */
  public void loadTokens(String[] tokens, int multiplier, boolean addSpacesInBetween)
  {
    setAddSpaces(addSpacesInBetween);
    String[] toAdd;
    wordCount += tokens.length;
    for (int k = 0; k < tokens.length; k++)
    {
      toAdd = new String[nFactor];
      for (int j = 0; j < toAdd.length; j++)
      {
        if ((k+j) < tokens.length)   
          toAdd[j] = (tokens[k+j] != null) ? tokens[k+j] : null;
        else 
          toAdd[j] = null;
      }      
      
      // hack to deal with multiplier...
      for (int j = 0; j < multiplier; j++)
        addSequence(toAdd);
    }
  }
  
  public void loadTokens(String[] tokens, int multiplier)
  {
    loadTokens(tokens, multiplier, false);
  }
  
  protected void addSequence(String[] toAdd)
  {
    //System.out.println(Util.asList(toAdd));
    RiTextNode node = root;          
    for (int i = 0; i < toAdd.length; i++)
      if (node.getToken() != null)        
        node = node.addChild(toAdd[i], useSmoothing ? 2 : 1);
  }
  
  /**
   * Outputs a String representing the models probability tree using
   * the supplied print stream (or System.out).<p>
   * 
   * NOTE: this method will block for potentially long periods of time
   * on large models. 
   * 
   * @param printStream where to send the output (default=System.out)
   * @param sort whether the tree is first sorted (by frequency) 
   * before being output
   */
  public void printTree(PrintStream printStream, boolean sort)
  {
    printStream.println(root.asTree(sort));
  }  
  public void printTree(boolean sort) { printTree(System.out, sort); }
  public void printTree(PrintStream pw) { printTree(pw, false); }
  public void printTree() { printTree(System.out, false); }

  
  /**
   * Returns the TextNode representing the root of the model's tree,
   * so that it can be (manually) navigated.
   */
  public RiTextNode getRoot() {
    return root;
  }

  /**
   * Returns the current n-value for the model
   */
  public int getNFactor()  {
    return this.nFactor;
  } 

  /**
   * Returns whether (add-1) smoothing is enabled for the model
   */
  public boolean isSmoothing()  {
    return this.useSmoothing;
  }

  /** 
   * Toggles whether (add-1) smoothing is enabled for the model.
   * Should be called before any data loading is done. 
   */
  public void setUseSmoothing(boolean useSmoothing){
    if (this.root.hasChildren()) 
      throw new RiTaException("Invalid state: setUseSmoothing() "
        + "must be called before any data is added to the model");
    this.useSmoothing = useSmoothing;
  }  
  
  protected String nextToken(String[] tokens)
  { 
    RiTextNode node = this.nextNode(tokens);    
    return node == null ? null : node.getToken();
  }
  
  protected RiTextNode nextNode(List previousTokens)
  { 
    // Follow the seed path down the tree
    int firstLookupIdx = Math.max(0, previousTokens.size()-(nFactor-1));     
    RiTextNode tn = (RiTextNode)previousTokens.get(firstLookupIdx++);
    RiTextNode node = root.lookup(tn);    
    for (int i = firstLookupIdx; i < previousTokens.size(); i++) {
      if (node != null)
        node = node.lookup((RiTextNode)previousTokens.get(i));
    }
    // Now select the next node
    RiTextNode result = selectChild(node, true);
    return result;
  }

  protected RiTextNode nextNode(String[] seed)
  { 
    // Follow the seed path down the tree
    int firstLookupIdx = Math.max(0, seed.length-(nFactor-1));         
    RiTextNode node = root.lookup(seed[firstLookupIdx++]);    
    for (int i = firstLookupIdx; i < seed.length; i++) {
      if (node != null)
        node = node.lookup(seed[i]);
    }
    // Now select the next node
    RiTextNode result = selectChild(node, true);
    return result;
  }
  
  /** 
   * Returns all possible next words (or tokens), ordered by probability, for the given
   * seed array, or null if none are found. <p>Note: seed arrays of any size (>0) may 
   * be input, but only the last n-1 elements will be considered.   
   */
  public String[] getCompletions(String[] seed)
  { 
    if (seed == null || seed.length == 0) {
      System.out.println("[WARN] Null (or zero-length) seed passed to getCompletions()");
      return null;
    }
    int firstLookupIdx = Math.max(0, seed.length-(nFactor-1));         
    RiTextNode node = root.lookup(seed[firstLookupIdx++]);    
    for (int i = firstLookupIdx; i < seed.length; i++) {
      if (node == null) return null;
      node = node.lookup(seed[i]);
    }
    if (node == null) return null;
    
    Collection c = node.getChildMap().values();
    if (c == null || c.size()<1) return null;
    RiTextNode[] nodes = new RiTextNode[c.size()];
    nodes = (RiTextNode[])c.toArray(nodes);
    Arrays.sort(nodes);
    String[] result = new String[nodes.length];
    for (int i = 0; i < result.length; i++)
      result[i] = nodes[i].getToken();
    return result;
  }

  protected RiTextNode selectChild(RiTextNode tn, boolean useProb) {
    return (tn == null) ? null : tn.selectChild(useProb);
  }  
  
  /**
   * Returns true if the model contains the token
   * in any position, else false.
   */
  public boolean containsChar(String token)
  {     
    return root.lookup(token) != null;
  }
    
  /**
   * Returns the raw (unigram) probability for 
   * a token in the model, or 0 if it does not exist
   */
  public float getProbability(String token)
  {
    if (root == null) 
      throw new RiTaException("Model not initialized: root is null!");
    RiTextNode tn = root.lookup(token);
    if (tn == null) return 0;
    else 
      return tn.getProbability();
  }
  
  /** 
   * Returns the probability of obtaining
   * a sequence of k character tokens were k <= nFactor,
   * e.g., if nFactor = 3, then valid lengths
   * for the String <code>tokens</code> are 1, 2 & 3.
   */
  public float getProbability(String[] tokens)
  {  
    RiTextNode tn = findNode(tokens);
    if (tn == null) return 0;
    else return tn.getProbability();
  }
  
  /**
   * Returns an unordered list of possible words <i>w</i> that complete
   * an n-gram consisting of: pre[0]...pre[k], <i>w</i>, post[k+1]...post[n].
   * As an example, the following call:
   * <pre>
   * getCompletions(new String[]{ "the" }, new String[]{ "ball" })
   * </pre>
   * will return all the single words that occur between 'the' and 'ball'
   * in the current model (assuming n > 2), e.g., ['red', 'big', 'bouncy']).
   * <p> 
   * Note: For this operation to be valid, (pre.length + post.length)
   * must be strictly less than the model's nFactor, otherwise an 
   * exception will be thrown. 
   */
  public String[] getCompletions(String[] pre, String[] post)
  { 
    if (pre == null || pre.length >= nFactor)
      throw new RiTaException("Invalid pre array: "+RiTa.asList(pre));
    
    int postLen = post == null ? 0 : post.length;    
    if (pre.length + postLen > nFactor) {
      throw new RiTaException("Sum of pre.length" +
          " && post.length must be < N, was "+(pre.length+postLen));        
    }
    
    RiTextNode tn = findNode(pre);
    if (tn == null) return null;
    
    List result = new ArrayList();
    Collection nexts = tn.getChildNodes();
    for (Iterator it = nexts.iterator(); it.hasNext();)
    {
      RiTextNode node = (RiTextNode) it.next();
      String[] test = appendToken(pre, node.getToken());
      if (test == null) continue;
      for (int i = 0; i < postLen; i++)
        test = appendToken(test, post[i]); 
      if (findNode(test) != null)
        result.add(node.getToken());      
    }        
    return strArr(result);    
  }
  
  /** 
   * Returns the full set of possible next tokens (as a HashMap: 
   * String -> Float (probability)) given an array of tokens 
   * representing the path down the tree (with length less than n).  
   * If the input array length is not less than n, or the path cannot be 
   * found, or the endnode has no children, null is returned.<p>
   * 
   * Note: As the returned Map represents the full set of possible next 
   * tokens, the sum of its probabilities will always be equal 1.
   *   
   * @see #getProbability(String) 
   */
  public Map getProbabilities(String[] path)
  { 
    Map probs = new HashMap();
    
    if (path.length ==0 || path.length >= nFactor)      
      return null;
    
    RiTextNode tn = findNode(path);
    if (tn == null) return null;
    
    Collection nexts = tn.getChildNodes();
    for (Iterator iter = nexts.iterator(); iter.hasNext();)
    {
      RiTextNode node = (RiTextNode) iter.next();      
      if (node != null) {
        String tok = node.getToken();            
        float prob = getProbability(appendToken(path, tok));
        probs.put(tok, new Float(prob));
      }      
    }
    return probs;     
  }
  
  /* not used - to util? push pop peek?
  static String[] prependToken(String token, String[] path) 
  {
    String[] fullPath = new String[path.length+1];
    System.arraycopy(path, 0, fullPath, 1, path.length);
    fullPath[0] = token;
    return fullPath;
  }*/
  
  static String[] appendToken(String[] path, String token) 
  {
    String[] fullPath = new String[path.length+1];
    System.arraycopy(path, 0, fullPath, 0, path.length);
    fullPath[fullPath.length-1] = token;
    return fullPath;
  }

  /**
   * Traverses the tree and returns the node 
   * at the end of <code>path</code>, or null 
   * if the full path does not exist
   */
  protected RiTextNode findNode(String[] path) 
  {
    //System.out.print("RiMarkov.findNode("+Util.asList(path)+")");
    if (path == null || path.length <1) 
      return null;
    RiTextNode[] nodes = nodesOnPath(path);
    RiTextNode tf = nodes != null ? nodes[nodes.length-1] : null;
    //System.out.println(" :: "+tf);
    return tf;
  }

  /**
   * Traverses the tree and returns the node 
   * at the end of <code>path</code>, or null 
   * if the full path does not exist. Will
   * matches on the last token plus a sentence
   * end if <code>allowSentenceEnds</code> is true;
   */
  //protected TextNodeIF findNode(String[] path, boolean allowSentenceEnds) 
  
  /**
   * Return the nodes on the <code>path</code>
   * or null if the full path does not exist
   */
  protected RiTextNode[] nodesOnPath(String[] path) 
  {    
    int numNodes = Math.min(path.length, nFactor-1);
    int firstLookupIdx = Math.max(0, path.length-(nFactor-1));         
    RiTextNode node = (RiTextNode)root.lookup(path[firstLookupIdx++]);    
    if (node == null) return null;
    
    int idx = 0;  // found at least one good node
    RiTextNode[] nodes = new RiTextNode[numNodes];    
    nodes[idx++] = node; 
    for (int i = firstLookupIdx; i < path.length; i++) {       
      node = node.lookup(path[i]);
      if (node == null) return null;
      nodes[idx++] = node;
    }
    return nodes;
  }  

  protected boolean validSentenceStart(String word)//, String previousWord)
  {      
    if (!recognizeSentences) return true;  
    
    if (word.matches(SS_REGEX)) { 
      //if (previousWord == null || !Util.isAbbreviation(previousWord))      
        return true;
    }    
    return false;
  }
  
  protected String clean(String sentence) {
    if (isRemovingQuotations()) {
      sentence = sentence.replaceAll("[\"��]", "");
      sentence = sentence.replaceAll("['`��] ", "");
      sentence = sentence.replaceAll(" ['`��]", "");
    }
    sentence = sentence.replaceAll("\\s+", " ");
    return sentence.trim();
  }
  
  /**
   * Loads an array of sentences into the model; each 
   * element in the array must be a single sentence for
   * proper parsing.
   */
  public void loadSentences(String[] sentences, int multiplier)
  {    
    List allWords = new ArrayList();
    
    if (sentenceStarts == null)
      sentenceStarts = new ArrayList();
    
    //String lastWord = null;
    
    // do the cleaning/splitting first ---------------------
    for (int i = 0; i < sentences.length; i++)
    {
      String sentence = clean(sentences[i]);
      if (!allowDuplicates) {
        if (sentenceList == null)
          sentenceList = new HashSet();
        sentenceList.add(sentence);
      }
      String[] tokens = tokenizer.tokenize(sentence);
      wordCount += tokens.length;
      if (!validSentenceStart(tokens[0])){
        if (printIgnoredText)
          System.out.println("[WARN] Skipping (bad sentence start): "+RiTa.asList(tokens));
        continue;
      }              
      allWords.add(SS_DELIM+tokens[0]); // awful hack
      int j = 1;
      for (; j < tokens.length; j++)  
        allWords.add(tokens[j]);  
      //lastWord = tokens[tokens.length-1];
    }
    
    // ------------------------------------------------
      
    String[] words, toAdd;    
    wordsPerFile += allWords.size();
    words = (String[])allWords.toArray(new String[allWords.size()]);
    for (int i = 0; i < words.length; i++)
    {      
      toAdd = new String[nFactor]; // use arraycopy?
      for (int j = 0; j < nFactor; j++)
      {
        if ((i + j) < words.length)
          toAdd[j] = words[i + j];
      }
      
      // hack to deal with multiplier...
      for (int j = 0; j < multiplier; j++)
        addSentenceSequence(toAdd);
    }
    
//  System.out.println("Starts: "+sentenceStarts); 
    
    //System.out.println("[INFO] Processing complete: "+wordsPerFile+" words.");
  }
  
  protected RiTextNode getSentenceStart()
  {
    if (sentenceStarts == null || sentenceStarts.size()<1)
      return null;
    int idx = (int)(Math.random()*sentenceStarts.size());
    String txt = (String)sentenceStarts.get(idx);
    return root.lookup(txt);
  }
  
  /**
   * @deprecated use generateSentence() instead
   */
  public String generate()
  {     
    return this.generateSentence();
  }
  
  /**
   * Generates a sentence from the model.<p>
   * Note: multiple sentences generated by this method WILL NOT follow 
   * the model across sentence boundaries; thus the following two calls 
   * are NOT equivalent:
   * <pre>
     String[] results = markov.generateSentences(10);
               and
     for (int i = 0; i < 10; i++)
       results[i] = markov.generateSentence();
     </pre>
   * The latter will create 10 sentences with no explicit relationship 
   * between one and the next; while the former will follow probabilities 
   * from one sentence (across a boundary) to the next.  
   */
  public String generateSentence() { 
    return generateSentences(1)[0];
  }
  
  /**
   * Generates some # (one or more) of sentences from the model.<P>
   * Note: multiple sentences generated by this method WILL follow 
   * the model across sentence boundaries; thus the following two calls 
   * are NOT equivalent:
   * <pre>
     String[] results = markov.generateSentences(10);
               and
     for (int i = 0; i < 10; i++)
       results[i] = markov.generateSentence();
     </pre>
   * The latter will create 10 sentences with no explicit relationship 
   * between one and the next; while the former will follow probabilities 
   * from one sentence (across a boundary) to the next.                  
   */
  public String[] generateSentences(int numSentences)
  {
    if (!recognizeSentences) 
      throw new RiTaException("Illegal state: attempt to call " +
        "generateSentences() after setting generateSentences=false");
                        
    StringBuilder s = new StringBuilder(32);
    String[] result = new String[numSentences];
    int counter=0, totalTries=0, wordsInSentence=1;
    
    // find a token to start from
    RiTextNode mn = (recognizeSentences) ? getSentenceStart() : root.selectChild();    
    if (mn == null)
      throw new RiTaException("Unable to find start node! genSen="+recognizeSentences);
    s.append(mn.getToken()+" ");      
    
    int tries = 0;
    while (counter < numSentences) 
    {
      if (wordsInSentence >= getMaxSentenceLength() || mn == null) { // too long, start over
        //System.out.println("MarkovModel.generateSentences() L:: toolong");
        wordsInSentence = 0;
        s.delete(0, s.length());
      } 
      
      if (mn.isLeaf()) {
        mn = tracePathFromRoot(mn);
        continue;
      }
            
      mn = nextNode(mn);        
      if (mn.isSentenceStart()) 
      {         
        if (wordsInSentence >= getMinSentenceLength()) 
        {
          String candidate = checkPunctuation(s.toString());          
          if (candidate != null && validateSentence(candidate)) 
          {
            // got one, store and reset the counters
            result[counter++] = candidate;
            totalTries += tries;
            tries = 0;
          }
          else {
            System.out.println("MarkovModel.generateSentences() L:: reject");
            candidate = null;
          }
        }
        wordsInSentence = 0;
        s.delete(0, s.length());
      }
      wordsInSentence++;
      s.append(mn.getToken()+" ");
      
      //System.out.println("adding: "+mn);

      if (++tries >= MAX_GENERATION_ATTEMPTS) {
        totalTries += tries;
        onGenerationIncomplete(totalTries, counter);       
        for (int i = 0; i < result.length; i++)
          if (result[i]==null) result[i] = QQ; 
        break; // give-up
      }        
    }           

    return result; 
  }  

  private boolean validateSentence(String sent)
  {
    String[] tokens = sent.split(" ");
    String first = tokens[0], last = tokens[tokens.length-1];
    if (first.matches("\\W.*")) {
      if (printIgnoredText && (!RiTa.SILENT))
        System.out.println("[INFO] Skipping: bad first char in '"+sent+"'");
      return false;
    }      
    if (RiTa.isAbbreviation(last)) {
      System.out.println("Bad last token: '"+last+"' in:\n  "+sent);   
      return false;
    }
    if (!allowDuplicates) 
    {
      if (!recognizeSentences)
        System.err.println("[WARN] Invalid state:" +
          " allowDuplicates must be true when not generating sentences");
      
      if (sentenceList.contains(sent)) {
        if (++skippedDups == maxDuplicatesToSkip) {
          System.err.println("[WARN] Hit skip-maximum after skipping "+
            maxDuplicatesToSkip+" duplicates, now allowing duplicates!");
          allowDuplicates = true;
        }
        return false;
      }
    }
    return true;
  }

  private String checkPunctuation(String sent)
  {    
    sent = RiTa.upperCaseFirst(RiTa.chomp(sent));
    
    if (SENTENCE_ENDING_GAP == null) 
      SENTENCE_ENDING_GAP = Pattern.compile(SE_GAP_REGEX);
    
    // close gaps at the end of a sentence, e.g. "dog ."
    Matcher m = SENTENCE_ENDING_GAP.matcher(sent);
    if (m.matches()) {     
      int x = m.start(1), y = m.end(1);
      sent = sent.substring(0, x) + sent.substring(y);
      m = SENTENCE_ENDING_GAP.matcher(sent);
    }    
    
    if (PUNCTUATION_GAP == null)
      PUNCTUATION_GAP = Pattern.compile(PUNCT_GAP_REGEX); 
    
    sent = removeGroup(sent, PUNCTUATION_GAP);

    return sent;
  }

  /**
   * Expects a regex with one capturing group and (repeatedly) 
   * removes the group until there is no match 
   */
  private String removeGroup(String sent, Pattern pat)
  {
    Matcher m = pat.matcher(sent);
    while (m.matches()) {     
      int x = m.start(1), y = m.end(1);
      sent = sent.substring(0, x) + sent.substring(y);
      m = pat.matcher(sent);
    }
    return sent;
  }

  /**  
   * Chooses the next node (probabalistically) from the model. 
   * @param current - Node that is the parent of the returned node
   */
  protected RiTextNode nextNodeORig(RiTextNode current)
  {  
    double selector=0, pTotal=0;
    Collection nodes = current.getChildNodes();
    while (true) {
      pTotal = 0;
      selector = Math.random();      
      for (Iterator it = nodes.iterator(); it.hasNext();)
      {
        RiTextNode child = (RiTextNode) it.next();            
        pTotal += child.getProbability();
        if (current.isRoot() && (recognizeSentences && !child.isSentenceStart()))
          continue;
        if (selector < pTotal)
          return child;
      }  
      throw new RuntimeException  // should never happen
        ("PROB. MISS"+current+ " total="+pTotal+" selector="+selector);
    }
  }
  
  protected RiTextNode nextNode(RiTextNode current)
  {         
    int attempts = 0;
    double selector, pTotal=0;
    Collection nodes = current.getChildNodes();
    while (true) {
        pTotal = 0;
        selector = Math.random();   
        //System.out.println("current="+current+", selector="+selector);
        for (Iterator it = nodes.iterator(); it.hasNext();)
        {
          RiTextNode child = (RiTextNode) it.next(); 
          //System.out.println("child="+child);
          pTotal += child.getProbability();
          //System.out.println("pTotal="+pTotal);
          if (current.isRoot() && (recognizeSentences && !child.isSentenceStart())) {
            //System.out.println("continuing...");
            continue;
          }
          if (selector < pTotal) {
            //System.out.println("returning "+child+"\n====================");
            return child;
          }
          //System.out.println("selector >= pTotal\n====================");
        }
        attempts++; 
        System.err.println("[WARN] Prob. miss (#"+attempts+") in MarkovModel.nextNode()."
        		+ " Make sure there are a sufficient\n       # of sentences"
        		+ " in the model that are longer than your minSentenceLength.");
        if (attempts == MAX_PROB_MISSES)
          throw new RuntimeException  // should never happen
            ("PROB. MISS"+current+ " total="+pTotal+" selector="+selector);  
      }      
  }
  
  private static Pattern SENTENCE_ENDING_GAP, PUNCTUATION_GAP;
  
  protected RiTextNode tracePathFromRoot(RiTextNode node)
  {
    if (pathTrace == null) pathTrace = new Stack();
    node.pathFromRoot(pathTrace);
    pathTrace.pop(); // ignore the first element
    RiTextNode mn = root;    
    while (!pathTrace.isEmpty()) {
      String search = (String)pathTrace.pop();
      mn = mn.lookup(search);
    }     
    return mn;
  }
  
  protected void addSentenceSequence(String[] toAdd)
  {
// System.out.println(Util.asList(toAdd));
    RiTextNode node = root;          
    for (int i = 0; i < toAdd.length; i++) 
    {
      if (toAdd[i] == null) continue;
// System.out.println("  "+i+") "+toAdd[i]);
      if (node.getToken() != null)   {
        String add = toAdd[i];         
        if (add.startsWith(SS_DELIM)) {
          add = add.substring(SS_DELIM.length()); // awful (use-ristring)
          RiTextNode parent = node;
          node = node.addChild(add, useSmoothing ? 2 : 1);
          node.setIsSentenceStart(true);
          if (parent.isRoot()) { 
            sentenceStarts.add(node.getToken());            
// System.out.println("adding Starter: "+node.getToken()+ " "+toAdd[i+1]);
          }                  
        }
        else
          node = node.addChild(add, useSmoothing ? 2 : 1);
      }
    }
  }
  
  /**
   * Returns whether the model will attempt to recognize (english-like) sentences
   * in the input text (default=true).
   */
  public boolean isRecognizingSentences()
  {
    return this.recognizeSentences;
  }

  /**
   * Sets whether the model will try to recognize 
   * (english-like) sentences in its input (default=true).
   */
  public void setRecognizeSentences(boolean ignoreSentences)
  {
    this.recognizeSentences = ignoreSentences;
  }
  
  /**
   * Tells the model to ignore (english-like) sentences in its input 
   * and treat all text tokens the same.
   */
  public void disableSentenceProcessing()
  {
    this.recognizeSentences = false;
  }

  public boolean isIgnoringCase()
  {
    return this.ignoreCase;
  }
  
  public RiTokenizerIF getTokenizer()
  {
    return this.tokenizer;
  }
  public void setTokenizer(RiTokenizerIF tokenizer)
  {
    this.tokenizer = tokenizer;
  }
  public void destroy() {
    root = null;
  }  
  public void loadSentences(String[] sentences) {
    loadSentences(sentences, 1);
  }
  public void loadTokens(String[] tokens) {
    loadTokens(tokens, 1);
  }
  public void loadTokens(char[] tokens) {
    String[] s = new String[tokens.length];
    for (int i = 0; i < tokens.length; i++) 
      s[i] = Character.toString(tokens[i]);    
    loadTokens(s, 1);
  }
  
  private static final String SE_GAP_REGEX = ".+[a-z](\\s+)[\\.\\?\\!]";
  private static final String PUNCT_GAP_REGEX = "[^-]+?(\\s+)[\"'\\?\\!\\.,:;`].+";
  
  // NEED TO DEAL with DASHES HERE!!!!
  
  //private static final String PUNCT_GAP_REGEX = ".+?[a-z](\\s+)[\"'\\?\\!\\.,:;`].+";

  /** Sets the minimum # of words allowed in a generated sentence (default=6) */
  public void setMinSentenceLength(int minSentenceLength) {
    this.minSentenceLength = minSentenceLength;
  }

  /** Returns the minimum # of words allowed in a generated sentence */
  public int getMinSentenceLength() {
    return minSentenceLength;
  }
  
  /** Sets the maximum # of words allowed in a generated sentence (default=6) */
  public void setMaxSentenceLength(int maxSentenceLength) {
    this.maxSentenceLength = maxSentenceLength;
  }

  /** Returns the maximum # of words allowed in a generated sentence */
  public int getMaxSentenceLength() {
    return maxSentenceLength;
  }
  
  /** Tells the model whether to ignore various quotations types in the input   */
  public void setRemoveQuotations(boolean removeQuotations) {
    this.removeQuotations = removeQuotations;
  }

  /** Tells whether the model is ignoring quotations found in the input  (default=true) */
  public boolean isRemovingQuotations() {
    return removeQuotations;
  }
  
  public void setAddSpaces(boolean addSpaces)
  {
    this.addSpaces = addSpaces;
  }

  public static void main(String[] args)
  {  
    String test = ("Last Wednesday we decided to visit the zoo. " +
        "We arrived before the fog had lifted, cashed " +
        "in our passes and entered. We walked toward the first exhibits." +
        " I looked up at one of the giraffes as it stared back at me. I stepped " +
        "nervously to the next area. One of the lions gazed at me as he " +
        "lazed in the shade while the others napped. One of my friends first " +
        "knocked then banged on the tempered glass in front of the monkey’s " +
        "cage. They howled and screamed at us as we hurried to other exhibit" +
        " where we stopped and gawked at plumed birds. After we rested, we " +
        "walked by the petting zoo where we petted wooly sheep who only " +
        "glanced at us but the goats butted each other and nipped our clothes" +
        " when we ventured too near their closed pen. Later, our tired group " +
        "nudged their way through the crowded paths and exited the turnstiled" +
        " gate. Our car bumped, jerked and swayed as we dozed during the " +
        "relaxed ride home.");

    MarkovModel mm = new MarkovModel(null, 5, false);
    mm.setRecognizeSentences(false);
    char[] tokens = RiTa.loadString(null, "tate.txt").toCharArray();
    mm.loadTokens(tokens);
    mm.printTree();
    
    //if (1==1) return;
    
    String[] strs = mm.generateSentences(10);    
    for (int i = 0; i < strs.length; i++)
      System.out.println(i+") "+strs[i]);
    System.out.println("\nGeneration took "+RiTa.elapsed()+"s");
    
    //if (1==1) return;

    System.out.println("1] p(One) = "+mm.getProbability("One"));
    System.out.println("2] p(the | giraffes) = "+mm.getProbability(new String[]{ "the", "giraffes" }));
    System.out.println("3] map(the | before) = "+mm.getProbabilities(new String[]{  "before", "the" }));    
    System.out.println("4] next(the | before) = "+RiTa.asList(mm.getCompletions(new String[]{ "before", "the" })));
    System.out.println("5] next(of | one) = "+RiTa.asList(mm.getCompletions(new String[]{ "One", "of" })));
    System.out.println("6] map(of | one) = "+mm.getProbabilities(new String[]{  "One", "of" }));    
    System.out.println("7] getCompletions(walked ? the) = "+RiTa.asList
      ((mm.getCompletions(new String[]{ "walked" }, new String[]{ "the" }))));
    
  }

}// end

