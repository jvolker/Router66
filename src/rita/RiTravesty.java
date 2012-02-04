package rita;

import java.io.PrintStream;
import java.util.*;

import processing.core.PApplet;
import rita.support.*;


/**
 * Represents a Markov chain (or n-Gram) model that treats each character
 * as a separate token (a la the original Travesty program). Provides a 
 * range of methods (see examples below) to query the model for probabilites 
 * and/or completions.
 * <br> 
   <pre>
   
    RiTravesty rm = new RiTravesty(this, 5);
    
    rm.setEndOfSequenceChar(' ');                // treats each word as separate input
    
    rm.loadFile("myTestFile.txt");               // load the file (w' no multiplier)
        
    rm.printTree();                              // prints the model as a tree
    
                            ---------------------------
     
    println("p(g)="+rm.getProbability("g"));     // the probability of 'g'
    
    println("p(X|g)="+rm.getProbabilities("g")); // probabilities for next letters(X) after g, p(X|g) 
    
    String comp = rm.getCompletion("gr");        // returns a prob. random completion 
     
    String[] comps = rm.getCompletions("gr");    // returns all possible completions
 * </pre>
 * @author dhowe
 * @invisible
 */
public class RiTravesty extends RiObject
{   
  /** @invisible */
  public static final char NULL_CHAR = (char)-1;
  
  protected int nFactor;  
  protected boolean useSmoothing;    
  protected static TextNode ROOT;
  protected char endOfSequenceChar;
  protected boolean profile;
 
  /**
   * Construct a Markov (or n-gram) model and set its n-factor 
   */
  public RiTravesty(PApplet parent, int nFactor) {
  	super(parent);
    this.nFactor = nFactor;
    this.endOfSequenceChar = NULL_CHAR;
    RiTravesty.ROOT = TextNode.createRoot(false);
    if (!RiTa.SILENT)System.out.println("\n[INFO] Model created with N="+nFactor);
  }
  
  // Methods -------------------------------------------------------

  /**  
   * Load a text file into the model -- if using Processing, the file 
   * should be in the sketch's data folder. 
   * @param fileName name of file to load
   * @param multiplier weighting for tokens in the file;<br>  
   * a weight of 3 is equivalent to loading that file 3 times and gives
   * each token 3x the probability of being chosen on a call to generate().
   */
  public void loadFile(String fileName, int multiplier) 
  {  
    long done, start = System.currentTimeMillis();  

    String[] data = RiTa.loadStrings(_pApplet, fileName);
    String contents = RiTa.join(data, " "); 
    
    if (profile)
    {
      System.err.print("[INFO] Loaded file: "+fileName);
      done = System.currentTimeMillis()-start;
      System.err.println(" in "+done/1000d+"s");
    }
    
    start = System.currentTimeMillis(); 
    this.loadCharData(contents, multiplier);
    
    if (profile)
    {
      System.err.print("[INFO] Loaded data into model");
      done = System.currentTimeMillis()-start;
      System.err.println(" in "+done/1000d+"s");
    }
  }  
  
  public void loadFile(String fileName) 
  {
    this.loadFile(fileName, 1);    
  }
  
  /** 
   * Load a String into the model, treating each character as separate entity 
   * 
   * @param multiplier Weighting for tokens in the String <br>  
   * A weight of 3 is equivalent to loading that file 3 times and gives
   * each token 3x the probability of being chosen on a call to generate().
   */
  public void loadCharData(String rawText, int multiplier) 
  {
    //System.err.println("RiTravesty.loadCharData: "+rawText);
    char toAdd[] = null;  
    for (int k = 0; k < rawText.length(); k++)
    {
      if (rawText.charAt(k) == endOfSequenceChar) 
        continue;
      
      toAdd = new char[nFactor];
      boolean endSequence = false;
      for (int j = 0; j < toAdd.length; j++)
      {
        //System.err.println("charAt("+(k+j)+")");
        if (!endSequence && (k+j) < rawText.length()) {  
          char c = rawText.charAt(k+j);
          if (c == endOfSequenceChar) {
            endSequence = true;
            toAdd[j] = NULL_CHAR;
          }
          else 
            toAdd[j] = c;           
        }
        else 
          toAdd[j] = NULL_CHAR;
      }      
      //System.err.println(Util.asList(toAdd));
      addSequence(toAdd);
    }
  }
  
  protected void addSequence(char[] toAdd)
  {
    //System.err.println(Util.asList(toAdd));
    TextNode node = ROOT;          
    for (int i = 0; i < toAdd.length; i++)
      if (node.getToken().charAt(0) != NULL_CHAR)
        node = node.addChild(toAdd[i], useSmoothing ? 2 : 1);
  }
  
/*  protected void addSequence(String[] toAdd)
  {
    //System.err.println(Util.asList(toAdd));
    TextNode node = ROOT;          
    for (int i = 0; i < toAdd.length; i++) {
      String token = node.getToken();
      if (token.charAt(0) != NULL_CHAR)
        node = node.addChild(toAdd[i], useSmoothing ? 2 : 1);
    }
  }*/

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
    printStream.println(ROOT.asTree(sort));
  }  
  public void printTree(boolean sort) { printTree(System.out, sort); }
  public void printTree(PrintStream pw) { printTree(pw, false); }
  public void printTree() { printTree(System.out, false); }


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
   * Toggles whether (add-1) smoothing is enabled for the model
   */
  public void setUseSmoothing(boolean useSmoothing){
    this.useSmoothing = useSmoothing;
  }  
  
  protected char nextChar(String chars)
  { 
    TextNode node = this.nextNode(chars.toCharArray());    
    return node == null ? NULL_CHAR : node.getToken().charAt(0);
  }
  
  protected TextNode nextNode(String chars)
  { 
    return nextNode(chars.toCharArray());
  }
    
  protected TextNode nextNode(char[] seed)
  { 
    // Follow the seed path down the tree
    int firstLookupIdx = Math.max(0, seed.length-(nFactor-1));         
    TextNode node = ROOT.lookup(seed[firstLookupIdx++]);    
    for (int i = firstLookupIdx; i < seed.length; i++) {
      if (node != null)
        node = node.lookup(seed[i]);
    }
    // Now select the next node
    TextNode result = selectChild(node, true);
    return result;
  }

  protected TextNode selectChild(TextNode tn, boolean useProb) {
    return (tn == null) ? null : tn.selectChild(useProb);
  }  
  
  /**
   * Returns true if the model contains the token
   * in any position, else false.
   */
  public boolean containsChar(char token)
  {     
    return ROOT.lookup(token) != null;
  }
    
  /**
   * Returns the raw (unigram) probability for 
   * a token in the model, or 0 if it does not exist
   */
  public float getProbability(char token)
  {
    TextNode tn = ROOT.lookup(token);
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
  public float getProbability(String tokens)
  {  
    TextNode tn = findNode(tokens);
    if (tn == null) return 0;
    else return tn.getProbability();
  }
  
  /** 
   * Returns the full set of possible next tokens
   * (as a HashMap: String -> Float (probability)) 
   * given an array of tokens representing the path
   * down the tree (with length less than n).  
   * If the input array length is not less than n, 
   * or the path cannot be found, or the endnode 
   * has no children, null is returned.<p>
   * As the returned Map represents the full set of 
   * possible next tokens, the sum of its probabilities
   * will alwyas equal 1.
   *   
   * @see #getProbability(String) 
   */
  public Map getProbabilities(String path)
  { 
    Map probs = new HashMap();
    if (path.length()==0 || path.length() >= nFactor)
      return null;
    TextNode tn = findNode(path);
    if (tn == null) return null;
    Collection nexts = tn.getChildNodes();
    for (Iterator iter = nexts.iterator(); iter.hasNext();)
    {
      TextNode node = (TextNode) iter.next();
      if (node != null) {
        String tok = node.getToken();
        String fullPath = path + tok;
        float prob = getProbability(fullPath);
        //System.err.println("p("+fullPath+")="+prob);
        probs.put(tok, new Float(prob));
      }      
    }
    return probs;     
  }

  /**
   * Return the nodes on the <code>path</code>
   * or null if the full path does not exist
   */
  protected TextNode findNode(String path) 
  {
    if (path == null || path.length()<1) return null;
    TextNode[] nodes = nodesOnPath(path.toCharArray());
    return nodes != null ? nodes[nodes.length-1] : null;
  }
  
  /**
   * Return the nodes on the <code>path</code>
   * or null if the full path does not exist
   */
  protected TextNode[] nodesOnPath(char[] path) 
  {    
    int numNodes = Math.min(path.length, nFactor-1);
    int firstLookupIdx = Math.max(0, path.length-(nFactor-1));         
    TextNode node = ROOT.lookup(path[firstLookupIdx++]);    
    if (node == null) return null;
    
    int idx = 0;  // found at least one good node
    TextNode[] nodes = new TextNode[numNodes];    
    nodes[idx++] = node; 
    for (int i = firstLookupIdx; i < path.length; i++) {       
      node = node.lookup(path[i]);
      if (node == null) return null;
      nodes[idx++] = node;
    }
    return nodes;
  }
  
  
  /**
   * Chooses a single completion (if there are more than 1)
   * based upon probabilistic random choices.
   */
  public String getCompletion(String start) {
    char next; 
    while ((next = nextChar(start)) != NULL_CHAR)
      start += next;
    return start;
  }
  
  /**
   * Returns all completions (in the input) for a given substring
   // BROKEN??
  public String[] getCompletions(String start) {
    TextNode next = findNode(start);
    if (next == null) return null;
    List result = new LinkedList();
    this.getCompletions(start, next, result);
    return (String[])result.toArray(new String[result.size()]);
  }  */
  
  protected void getCompletions(String start, TextNode current, List soFar) {
    System.err.println("getCompletions("+start+","+current+", "+soFar+")");
    Collection children = current.getChildNodes();
    if (children == null || children.size() < 1) { // at a leaf
      TextNode tn = findNode(start);
      if (tn != null) getCompletions(start, tn, soFar);
      return;
    }
    for (Iterator i = children.iterator(); i.hasNext();)
    {      
      TextNode tn = (TextNode) i.next();
      //System.err.println("  child: "+tn);
      char c = tn.getToken().charAt(0);
      if (c==NULL_CHAR) {
        //System.err.println("  found ? adding "+(start));
        soFar.add(start);
      }
      else
        getCompletions(start+c, tn, soFar);
    }      
  }

  /**
   * Returns the character marking the end of an input sequence;
   */
  public char getEndOfSequenceChar()
  {
    return this.endOfSequenceChar;
  }

  /**
   * Sets the character to mark the end of an input sequence.
   * To parse each word token separately, for example, set this 
   * to {@link Character} a space - default is Character.EOF. 
   * @param endOfSequenceChar
   */
  public void setEndOfSequenceChar(char endOfSequenceChar)
  {
    this.endOfSequenceChar = endOfSequenceChar;
  }
    
  public static void main(String[] args)
  {    
    /*RiPosTagger.setDefaultTagger(RiPosTagger.MAXENT_POS_TAGGER);
    String[] tokens = Util.tokenize(CrimeAndPunish.firstParagraph); 
    String[] pos = Util.posTag(tokens);
    for (int i = 0; i < pos.length; i++)
    {
      System.out.println(tokens[i]+" -> "+pos[i]);
    }
    if (1==1) return;*/
    
    RiTravesty rm = new RiTravesty(null,5);
    //rm.setEndOfSequenceChar(' ');
    rm.loadFile("tate.txt");
    rm.printTree();         
     
   //System.out.println("p(g)="+rm.getProbability("g"));
    //System.out.println("p(o|g)="+rm.getProbabilities("ga"));
    //System.out.println(rm.generateUntil(" ", 3));
    //String[] comps = rm.getCompletions("g");
/*    
    String[] comps = rm.getCompletions("g");
    System.out.println("RESULT: "+RiTa.asList(comps));  */
  }

  /*public String generateUntil(String end, int minLen)
  {
    System.out.println("RiTravesty.generateUntil("+end+")");
    StringBuilder sb = new StringBuilder();
    TextNode tn = getFirstCharBesides(end);
    System.out.println("first: "+tn);
    int tries = 0;
    while (true) {
      if (tn.getToken().equals(end)) {
        if (sb.length() >= minLen) {
          System.out.println("GIVE UP");
          tries++;
          break;
        }
        
        sb.delete(0, sb.length());
        tn = getFirstCharBesides(end);
      }
      else {
        sb.append(tn.getToken());
        tn = nextNode(tn.getToken());
        System.out.println("next:  "+tn);
      }
    }
//    System.out.println("1: "+tn);
    return sb.toString();
  }*/

/*  private TextNode getFirstCharBesides(String end)
  {
    TextNode tn = ROOT.selectChild(true);
    while (tn.getToken().equals(end)) 
      tn = ROOT.selectChild(true);
    return tn;
  }*/

}// end

