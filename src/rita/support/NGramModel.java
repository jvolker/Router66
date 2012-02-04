package rita.support;

import java.io.PrintStream;
import java.util.*;

import processing.core.PApplet;
import rita.*;

// simplified implementation of markov-chain

/**
 * Represents a Markov chain (or n-Gram) model for probabilistic text generation. 
 * @invisible
 */
public class NGramModel extends RiObject implements NGramIF
{ 
  protected int nFactor;
  protected TextNode root;

  /**
   * Construct a Markov chain (or n-gram) model 
   * and set its n-Factor and ignoreCase flag 
   */
  public NGramModel(PApplet parent, int nFactor, boolean ignoreCase) {
    super(parent);        
    this.nFactor = nFactor;
    this.root = TextNode.createRoot(false);
  }
  
  public NGramModel(PApplet parent, int nFactor)
  { 
    this(parent, nFactor, false);
  }

  
  /**
   * Continues to generate tokens until the regex is matched, or generation fails.
   */
  public String generateUntil(String regex, int minLength, int maxLength)
  { 
    boolean dbug = false;
    
    int tries = 0, maxTries = 100;
    List tokens = new ArrayList();     

    OUT: while (++tries < maxTries) 
    {
      if (dbug) System.out.println("  TRY # "+tries+"--------------------");
      TextNode mn = getStartToken();
      if (mn == null || mn.getToken() == null)
        continue OUT;
      tokens.add(mn);
      
      // make sure we have enough
      while (tokens.size() < minLength) {     
        mn = nextNode(tokens);
        if (mn == null || mn.getToken() == null) { // hit the end
          //System.err.println("FAILED: "+tokens);
          if (dbug)System.out.println("  NULL TOKEN after: "+tokens);
          tokens.clear(); // start over
          continue OUT;
        }        
        tokens.add(mn); 
      }
      
      // length is ok, check for a regex match
      String tok = mn.getToken();
      if (dbug) System.out.println("    CHECKING: "+mn);
      if (tok.matches(regex)) {
        if (dbug) System.out.println("    OK (after "+tries+")\n--------------------");
        break;
      }
      
      // too many, bail
      if (tokens.size() > maxLength) {
        if (dbug) System.out.println("    GIVING UP: "+tokens+"\n--------------------");
        tokens.clear();
        continue;
      }
    }
    // uh-oh, we failed
    if (tries >= maxTries) 
      onGenerationIncomplete(tries, tokens.size());
    
    return tokensToString(tokens, true);
  }

  protected TextNode getStartToken()
  {
    return root.selectChild();
  }

  protected String tokensToString(List tokens, boolean addSpaces)
  {
    String result = "";   
    for (Iterator i = tokens.iterator(); i.hasNext();)
    {
      TextNode tn = (TextNode) i.next();        
      if (tn.getToken() == null)  {
        continue;
      }
      result += tn.getToken();
      if (i.hasNext() && addSpaces )
        result += " ";      
    }
    return result;
  }
  
  /* (non-Javadoc)
   * @see rita.support.NGramModelIF#generateTokens(int)
   */
  public String generateTokens(int targetNumber)
  {       
    int tries = 0, maxTries = 100;
    List tokens = new ArrayList();       
    OUT: while (++tries < maxTries) {
      TextNode mn = getStartToken();
      if (mn == null || mn.getToken() == null)
        continue OUT;
      tokens.add(mn);
      while (tokens.size() < targetNumber) {     
        mn = nextNode(tokens);
        if (mn == null || mn.getToken() == null) { // hit the end
          tokens.clear(); // start over
          continue OUT;
        }        
        tokens.add(mn);        
      }
      break;
    }
    // uh-oh, looks like we failed...
    if (tokens.size() < targetNumber) 
      onGenerationIncomplete(tries, tokens.size());
    
    return tokensToString(tokens, true);
  }

  protected void onGenerationIncomplete(int tries, int successes) {
    System.err.println("\n[WARN] MarkovModel failed to complete after "
        +tries+" tries\n       Giving up after only "+successes+" successful generations...\n");
  }

  /**
   * Loads an array of tokens (or words) into the model; each 
   * element in the array must be a single token for proper 
   * construction of the model. 
   * @param multiplier Weighting for tokens in the array <br>
   */
  public void loadTokens(String[] tokens, int multiplier)
  {
    String[] toAdd;
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
      for (int j = 0; j < multiplier; j++) {
        TextNode node = root;          
        for (int i = 0; i < toAdd.length; i++)
          if (node.getToken() != null)        
            node = node.addChild(toAdd[i]);
      }
    }
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

  
  /* (non-Javadoc)
   * @see rita.support.NGramModelIF#getRoot()
   */
  public TextNode getRoot() {
    return root;
  }

  /* (non-Javadoc)
   * @see rita.support.NGramIF#getNFactor()
   */
  public int getNFactor()  {
    return this.nFactor;
  } 
  
  protected TextNode nextNode(List previousTokens)
  { 
    // Follow the seed path down the tree
    int firstLookupIdx = Math.max(0, previousTokens.size()-(nFactor-1));     
    TextNode tn = (TextNode)previousTokens.get(firstLookupIdx++);
    TextNode node = rootLookup(tn);   
    for (int i = firstLookupIdx; i < previousTokens.size(); i++) {
      if (node != null)
        node = node.lookup((TextNode)previousTokens.get(i));
    }
    // Now select the next node
    TextNode result = selectChild(node, true);
    return result;
  }

  protected TextNode rootLookup(TextNode tn)
  {
    return root.lookup(tn); 
  }
  
  protected TextNode rootLookup(String tn)
  {
    return root.lookup(tn); 
  }

  protected TextNode nextNode(String[] seed)
  { 
    // Follow the seed path down the tree
    int firstLookupIdx = Math.max(0, seed.length-(nFactor-1));         
    TextNode node = rootLookup(seed[firstLookupIdx++]);    
    for (int i = firstLookupIdx; i < seed.length; i++) {
      if (node != null)
        node = node.lookup(seed[i]);
    }
    // Now select the next node
    TextNode result = selectChild(node, true);
    return result;
  }
  
  /* (non-Javadoc)
   * @see rita.support.NGramIF#getCompletions(java.lang.String[])
   */
  /* (non-Javadoc)
   * @see rita.support.NGramModelIF#getCompletions(java.lang.String[])
   */
  public String[] getCompletions(String[] seed)
  { 
    if (seed == null || seed.length == 0) {
      System.out.println("[WARN] Null (or zero-length) seed passed to getCompletions()");
      return null;
    }
    int firstLookupIdx = Math.max(0, seed.length-(nFactor-1));         
    TextNode node = rootLookup(seed[firstLookupIdx++]);    
    for (int i = firstLookupIdx; i < seed.length; i++) {
      if (node == null) return null;
      node = node.lookup(seed[i]);
    }
    if (node == null) return null;
    
    Collection c = node.getChildMap().values();
    if (c == null || c.size()<1) return null;
    TextNode[] nodes = new TextNode[c.size()];
    nodes = (TextNode[])c.toArray(nodes);
    Arrays.sort(nodes);
    String[] result = new String[nodes.length];
    for (int i = 0; i < result.length; i++)
      result[i] = nodes[i].getToken();
    return result;
  }

  protected TextNode selectChild(TextNode tn, boolean useProb) {
    return (tn == null) ? null : tn.selectChild(useProb);
  }  
  
  /* (non-Javadoc)
   * @see rita.support.NGramIF#getProbability(java.lang.String)
   */
  /* (non-Javadoc)
   * @see rita.support.NGramModelIF#getProbability(java.lang.String)
   */
  public float getProbability(String singleToken)
  {
    if (root == null) 
      throw new RiTaException("Model not initialized: root is null!");
    TextNode tn = rootLookup(singleToken);
    if (tn == null) return 0;
    else 
      return tn.getProbability();
  }
  
  /* (non-Javadoc)
   * @see rita.support.NGramIF#getProbability(java.lang.String[])
   */
  /* (non-Javadoc)
   * @see rita.support.NGramModelIF#getProbability(java.lang.String[])
   */
  public float getProbability(String[] tokens)
  {  
    TextNode tn = findNode(tokens);
    if (tn == null) return 0;
    else return tn.getProbability();
  }
  
  /* (non-Javadoc)
   * @see rita.support.NGramIF#getCompletions(java.lang.String[], java.lang.String[])
   */
  /* (non-Javadoc)
   * @see rita.support.NGramModelIF#getCompletions(java.lang.String[], java.lang.String[])
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
    
    TextNode tn = findNode(pre);
    if (tn == null) return null;
    
    List result = new ArrayList();
    Collection nexts = tn.getChildNodes();
    for (Iterator it = nexts.iterator(); it.hasNext();)
    {
      TextNode node = (TextNode) it.next();
      String[] test = appendToken(pre, node.getToken());
      if (test == null) continue;
      for (int i = 0; i < postLen; i++)
        test = appendToken(test, post[i]); 
      if (findNode(test) != null)
        result.add(node.getToken());      
    }        
    return strArr(result);    
  }
  
  /* (non-Javadoc)
   * @see rita.support.NGramIF#getProbabilities(java.lang.String[])
   */
  /* (non-Javadoc)
   * @see rita.support.NGramModelIF#getProbabilities(java.lang.String[])
   */
  public Map getProbabilities(String[] path)
  { 
    Map probs = new HashMap();
    
    if (path.length ==0 || path.length >= nFactor)      
      return null;
    
    TextNode tn = findNode(path);
    if (tn == null) return null;
    
    Collection nexts = tn.getChildNodes();
    for (Iterator iter = nexts.iterator(); iter.hasNext();)
    {
      TextNode node = (TextNode) iter.next();      
      if (node != null) {
        String tok = node.getToken();            
        float prob = getProbability(appendToken(path, tok));
        probs.put(tok, new Float(prob));
      }      
    }
    return probs;     
  }
  
  protected static String[] appendToken(String[] path, String token) 
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
  protected TextNode findNode(String[] path) 
  {
    //System.out.print("RiMarkov.findNode("+Util.asList(path)+")");
    if (path == null || path.length <1) 
      return null;
    int numNodes = Math.min(path.length, nFactor-1);
    int firstLookupIdx = Math.max(0, path.length-(nFactor-1));         
    TextNode node = (TextNode)rootLookup(path[firstLookupIdx++]);    
    if (node == null) return null;
    
    int idx = 0;  // found at least one good node
    TextNode[] nodes = new TextNode[numNodes];    
    nodes[idx++] = node; 
    for (int i = firstLookupIdx; i < path.length; i++) {       
      node = node.lookup(path[i]);
      if (node == null) return null;
      nodes[idx++] = node;
    }
    TextNode tf = nodes != null ? nodes[nodes.length-1] : null;
    //System.out.println(" :: "+tf);
    return tf;
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

  public static void mainX(String[] args)
  {  
    String[] test3 =
    {
      "in nn to vb nn vbd to vb rb in dt nn cc nn nn.",
      "prp$ nn jj to vb dt jj nn vbn dt nn nn in dt nn in nn prp vbz jj to vb wrb nn vbz rb nn.",
      "dt nn vbn in dt nn nn in dt jj nn cc dt jjr nn nn md vbp vbn nn.",
      "prp vbd rb vbn dt nn in dt vbg in nnp in wdt nnp vbg vbd dt jj nn vbn in dt nn cc nn nn vb to vb dt nn in vbg prp nn.",
      "nn nn vbd prp$ nn nn.",
      "nn ex vbd jjr nns prp$ nn md vbp vbd in nn.",
      "prp$ nn md vbp nn dt nns rb dt nn.",
      "nn in nn nns wp prp vbd nn wrb vbd prp to nn?",
      "prp vbd nn in nn jj cc nn in nns in vbn nnp nn nnp nns in nn dt nn nn cc dt jj nn nn in dt jj vbn nn nn vbd in nn jj rb to vb in cc vb in in dt jj nn.",
      "ex vbd dt nn in nn in dt jj nn in dt nn.",
      "prp vbd rb in dt nn in in dt jj nn nn nn dt jj nn nn nn.",
      "dt jjs nns vbd in dt nns nn nn.",
      "prp rb vbn dt nn in rb prp vbd prp wrb prp$ nn vbd prp cc prp vbd in in nn.",
      "rb prp vbd prp in dt nn wp vbd vb to jj in dt nns to nn cc in vbg vbd nn nn.",
      "prp rb vbd dt nn in dt nn wp vbd vbn in dt nn in nn in dt nn nn in prp$ nn jj in prp md vb jj in rb jj nn in prp nn.",
      "dt nn vbd dt in dt nn vbg in dt jj nn in dt jj nn cc vbd prp jj nn in dt jj nn in dt nn nn in nnp.",
      "wp vbd jj in dt nn vbd dt prp$ vbz cc prp$ nn cc rb dt jj jj nn in dt nn vbd rb jj in dt nn.",
      "prp vbd rb vbd dt nn cc vbd in prp rb in nn cc vbd vbd to vb prp rb wrb nn vbn in dt jj nns vbg in nn.",
      "nn nn vbn dt nn prp$ nn vbd in dt nn.",
      "dt nns wp vbd vb to vb dt nn vbn nn vbg in nn.",
      "prp vbd in nn vbn dt nn in dt nn cc prp vbd vbn nn.",
      "prp vbd to prp cc nn nnp jj rb prp$ nn in nn cc prp nn vb dt jj nn.",
      "prp vbd nn nn cc rb vbd rb nn in jj nn in ex nn dt nn.",
      "nn in jjs prp nn vb in nn prp nn.",
      "ex vbd nn jj in dt nn.",
      "prp vbd dt jj nn to vb nn dt nn to vb cc vb in cc dt nn to nn nn dt nn to vb nn jj nns in nn nns cc nns in vbg nn dt nn to vb to nn nn cc prp md vb nn.",
      "in prp vbn rb in nn in vbd prp$ jj nn in prp rb vbd dt nns nn cc nn.",
      "dt prp vbn in jj in nn nn in prp$ jj nn vbd vbg vbn nn nns in jj in nn rb rb vbg dt vbn jj rb to dt nn rb vbg prp in in dt nn cc in dt jj nn to dt nn cc rb vbg prp in in nn cc vbg prp in vbg prp nn.",
      "in in dt jj nn nn prp vbd nn.",
      "wrb dt nn vbd vbd in dt nn cc in nn prp md vb in nns in nn in nn vbg dt nn vbg nn vbg nn vbg in nns in nn cc nns wrb prp vbd nn vbg dt nn jj in nns nn cc rb nn.",
      "wp jj prp$ nn cc vbn prp$ nn in dt nn vbd dt vbn to vb nn in nn.",
      "prp vbd prp vbd in in jj to vb dt nn in prp vbd to vb nn.",
      "ex vbd to vb dt nn in jj jj nn cc prp vbd dt jjr in dt jj nn wdt in prp$ vbn nn prp md vbp nn in vbn prp nn dt nn jjr cc nn.",
      "prp md vbp vbn prp nn cc in prp$ nn.",
      "in jj nn prp md vb in dt nn dt jj nn nn nn cc rbr cc rb dt dt nn md vb prp to nn.",
      "dt jj nn nn in jj nns cc nn jj nn nn cc nn jj nn nn vbg prp nn rb rb in vbg dt nn prp vbn in ex vbd nn jj in nn in in prp md vbp nn in dt nn prp nn rb dt nn.",
      "rb jj rb jj cc jj nns vbd vbn in nn in dt nn prp vbd dt nn in nn.",
      "rb prp md vbp nn prp in dt nn in dt nn to dt nn vbn dt nn dt jj nn cc dt nn dt nn dt nn.",
      "prp vbd nnp vbd dt nn wrb vbd nnp vb dt dt nn?",
      "in prp vbd in in nn.",
      "dt nns prp$ nn jj nn.",
      "prp nn vb rb rb nn prp vbd to vb in cc nn prp$ nn.",
      "prp md vbp vb ex rb in dt vbg in dt nn cc dt nn in prp$ nn cc nns in dt nn nn.",
      "prp vbn prp$ jj nns nn cc rb nnp vbg in dt dt jj nn.",
      "wp md prp vbp in prp$ nn?",
      "in prp vbd prp nn dt nn nns in dt nn.",
      "cc prp vbd prp nn prp in nn nn vbg prp$ nns in nn.",
      "rb prp vbd nn in prp vbd jj to vb rb nn prp vbd vbn in prp vbd vb nn cc dt vbn dt nn nn vbg jjr nns in dt nn.",
      "rb prp vbd jj in vbg nns cc vbg nn vbg in prp$ nns in jj nn.",
      "cc prp vbn prp in vbg prp vbd vbn ex nn prp vbd dt nn to dt nn cc prp vbd prp$ nns to nn prp dt vbg nn.",
      "prp vbd in dt nn in prp vbd to vbp in nn rb dt nn vbd dt nn.",
      "prp vbd dt nn in wdt dt nn nn vbd dt nn nn prp rb jj in dt nn jj in dt jj nn vbd nn.",
      "prp vbd dt nn to prp$ cc vbn prp vbd jjr vbn to vb in to nn in dt nns rb vbn in dt nn.",
      "prp vbd jj nn cc nn in dt vbg in dt nn cc vbn prp nn.",
      "nn prp vbd rb vbg nn.",
      "rb in prp vbd in nn nn prp nn vbd jj in nn jj in dt jj nn nns in prp$ nn nn.",
      "nn vbp dt nns dt vbg in nn prp nn in prp$ nns nn.",
      "in prp rb vb prp vb in dt nn in prp$ nns in jj nn prp rb vbd jj wrb prp vbd to vb nn.",
      "prp vbd rb in dt nn rb to vb wrb dt nn cc prp$ nn vbd jj cc vbn nn dt nn in dt nn.",
      "jj nn nn in prp vbd to vbg nns in wdt nn vbd dt nn in dt nn nn vbd in dt nns in dt nn cc nn in nn vbd dt nn in nn.",
      "prp vbn nns cc nns to nn nn nn jj nn nn rb vbn prp to dt nns in nn.",
      "prp vbn dt nn in jj nn dt vbg dt nn dt jj vbg dt jj cc vbg dt nn vbg nn.",
      "prp vbd nn vbd nn nns to dt nn jj nns to dt nn cc rb in to nn wdt vbd nn prp rb vbd nn to vb in vbg nn.",
      "dt nn in dt nns dt nn vbd rb dt jj nn cc rb dt jj nn nn.",
      "rb prp vbd nn rb prp vbd nn.",
      "rb prp vbn prp in wrb prp vbn in dt nn cc rb prp vbn in dt nn cc rb in nn in dt nn in dt nn in in dt nn jjr rb wrb prp vbd nn.",
      "prp vbd prp$ jjs to vb dt nns in prp in nn nn vb nn.",
      "rb prp vbd vbn cc rb prp vbd vb nn.",
      "prp vbd rb jj in prp nn in prp vbd prp cc prp vbd prp nn in dt nn.",
      "in wdt nn prp vbd in nn vbd prp$ nn vbd prp nn vbn dt nn to dt nn in vbd nn.",
      "dt nn vbd dt jj vb rb in dt nn in dt jj nn in nn nn nn cc nn.",
      "prp vbd rb in dt nn in nn cc nn in prp vbd prp$ nn to nn wrb prp vbd wrb dt vbn vbd nn in in dt nn.",
      "nn in nn rb in dt nn nn in dt nn vbg rb to vb nn in nn.",
      "vbg vbd jj nn prp vbd nn nn.",
      "dt vbn dt jj nn cc nn to prp$ nn nn cc vbd jj to vb prp$ nn.",
      "prp vbd in dt vbg in nn vbg prp to nn cc vb nn.",
      "dt nns vbd jjr jj in prp$ nn in dt nn in dt nn rb vbd jjr in dt nn.",
      "in jj nn prp md vb dt nn in in dt nn cc nn in nns cc nn vbg to vb dt jj nnp.",
      "prp vbd rb jj in nn nn cc vbd vbg dt nn nn rb rbr cc rb nn vb in in nn cc vb nn in dt nn.",
      "dt nn vbd rb vbn in prp$ nn in vbg vbn nn.",
      "nns dt nn prp nn cc vb dt nn to prp$ nn.",
      "rb nnp vb jj to vb in dt nn nn prp vbd in prp$ vbg nn.",
      "rb prp vbd in md vb dt nn to vb vbd in nn in dt nn.",
      "cc wp in dt nn vbd nn?",
      "in prp vbd nn prp$ vbg vbd dt nn cc vbd to vb nn.",
      "prp vbd prp vbd dt vbg in dt in nn cc nn nn.",
      "prp md vb rb jjr nn wrb prp nn jj wdt prp vbd nn cc md vb nn.",
      "prp vbd jjr in wrb prp vbd nn vbd nn prp rb vbd to vbg dt nn cc vbg dt nn nns in in nn prp vbd dt dt nn in dt nn cc rb in nn prp vbd vbg nn.",
      "prp vbn to nn nn nn nn nn in prp$ nn cc rb nn rb prp vbd prp jj in nn cc prp vbd vbg nn.",
      "wrb dt nn rb vbd nn prp md vb cc vb prp in vbg dt nn nn vb cc vb prp$ in dt nn in dt nn vbg wrb prp vbd nn nn.",
      "prp vbn dt nns in dt nns in prp$ nn vbg prp dt nn cc prp rb vbn prp$ vbg nn in dt nn cc vbd prp$ nn nns in dt nn nn rb vbg prp nn.",
      "wrb prp vbd in dt vbg in dt nn prp vbd jjr nn to prp$ nn cc in nn prp vbd nn.",
      "nn prp vbn to vb in nn in dt nn cc vb to dt nns in dt nn rb ex vbd prp$ nn.",
      "cc wp rb vbd ex to vb nn?",
      "prp vbd prp vbd vbn vbg rb in to vb jj to vb in nn cc prp md nn prp$ nn in dt jj nn vbg prp$ nn in prp vbd nn jjr in dt nn in dt jj nn.",
      "wrb prp vbd in rb prp vbn dt nn in nn to vb jj in dt nn prp vbd dt nns in to vb in nn.",
      "prp vbd dt nn in nnps nn nn in wdt prp nn rb dt nn cc dt nn in dt nn.",
      "nn prp vbd jj to nn dt nn.",
      "nn prp vbd vbn in nn nn nn nn cc nn nn.",
      "ex vbd nns in md vb nns rb nn in jj rb jj nn nn.",
      "prp vbd dt nn in dt nn wp vbd vbn dt nn in dt jj jj cc wp rb vbd dt nns in vbg dt nn in in jj nn.",
      "prp vbn nnps nn to dt nn nn cc vbd prp in dt nn.",
      "prp vbn dt nn in nnp in wdt dt jj nn vbz in vbg dt nn cc dt nn rb nn nn in wrb to vb dt nn in nn.",
      "nn nn vbz vbn in dt nn cc vbz rb jj in prp$ jj nn to vb nn cc rb vbz jj nns wdt vb in to vb nn.",
      "jj nns in jj nns nn nn nnps nn.",
      "nn rb vbz prp dt nns nn jj in jj nn cc nnp nn cc nnp vbn in prp$ nn vbz nnp in dt nn in nn cc rb vbz dt nns nn vbg dt nnp.",
      "rb in dt jj nn dt jj nn nn prp nn vbg prp$ nns rb to dt nn in dt nn nn cc dt nn md rb nn.",
      "prp vbd nn in jj nn jj cc nn nn in rb dt jj nn in dt jj nn.",
      "rb dt nn md vb nn vbg in dt jj nn.",
      "dt nn md vb in dt jj nn in dt nn in dt nn in nnp vbg in nn in in to vb in prp dt nn in prp$ nn.",
      "in jj nn nns in jj nn md vb in nns in dt nn jj cc nn cc nns md vb nn vbg in nn.",
      "prp$ nns vbd in dt nn vbd in dt nn vbd in in dt nn vbn nn vbd wrb prp vbd nns in nn vbn in dt nns cc dt nn vbd nn.",
      "rb cc nn jj md vb cc vb vbd in in dt nn cc nn nn wrb vbp prp vb in vbg prp in nn rb vbg to vb in nn?",
      "nn dt dt nn cc prp$ nns rb vbd to vb in nn cc prp vbd nn.",
      "prp vbn dt nn nn in wdt dt dt nns cc vbz vbp nn in rb in dt nn cc dt dt wp vb to vb dt nns in dt nns wp vbd nn.",
      "cc prp vbd dt jj nn cc prp nn in prp vbd in in dt jj nn in nn rb vb rb in dt nn.",
      "dt nn in dt vbn nn vbd dt vbn nn in dt nn nn in nn nn cc prp$ nn in dt jj nn nn in jj nn.",
      "vbd prp rb to vbp in nn nn prp in nn vb dt nn nn?",
      "nn rb vb in in nn cc vbd vbn vbg prp$ nn in nn nn.",
      "jj nns vbd vbn in nns cc nns vbd vbg prp nn.",
      "jj in prp nn in nn vbg vbz nn.",
      "jj in dt nn vbd nn vbg rb rb vbn in nns cc nn.",
      "prp md vb in to prp$ nns in nn in dt jj nns nn.",
      "prp$ nns rb vbd in cc rb nn.",
      "rb nn in nn vbp nn prp nn cc nn in rb nn.",
      "rb prp vbd dt nn in dt nn in dt nn vbg jj nn.",
      "jj in dt vbn nns vbn prp to dt nn in dt nn in vbd vbg dt nn in nns cc nns in in to vb prp$ nn cc prp vbd prp nn vbg prp in jj nn.",
      "in jj nn in dt nn in jj nn nns nn.",
      "prp$ jj jj nns vbd to vb prp dt nn in vbn prp dt nn in vbg nn.",
      "in prp$ nn vbd in dt nn in nn prp nn nn md rb vbp to vb prp nn.",
      "dt nn prp vbd in dt jj jj vbg in dt jj nn in dt nn in in dt nn cc jj nn.",
      "rb cc nn dt nn vbn nn nn nn.",
      "prp vbd in dt nns wp vbn to vb prp nn.",
      "prp rb vbd wrb dt nns nn.",
      "nn dt nn vbd prp jj nn in dt nn in jj cc nn vbd dt jj nn nn.",
      "in nn dt nn vbn nn.",
      "dt vbg nn vbd in in nn cc vbd dt nn vbg rb in prp$ jj nn in nn.",
      "nn vbd rb vbn in nn.",
      "prp md in rb vbp rb nn.",
      "prp vbd to vb in dt nn in nn in dt nn in dt jj nn vbg in in dt jj nn vbg dt jj nn cc nn dt nn wp nn?",
      "prp vbd in dt nn jjr rb wrb dt nn vbd in dt nn in prp vbd rb to vb nns cc nns in nn.",
      "in jj nn prp md vb in cc vb in dt nns in dt nn vbg prp$ nn wdt vbd to vbp nn to vbp in jj nn nn cc nn.",
      "prp vbd to vb in nnps jj nn in nn vbd prp$ nn nn cc vbd prp$ nn.",
      "prp nn dt jj nn md vb vbn in dt nn cc rb vbn nn.",
      "prp vbd dt nn in dt nn nn vbn in dt nn in nn nns cc nn vbg vbn rb wp dt nn vbd cc wp prp$ nn vbd nn vbd in jj nn cc vbd nn.",
      "prp vbn to vb in nnps nn to dt nn in dt nn nn jjr cc jjr nn vbg nn.",
      "prp vbn dt nnp in wdt nn nn cc rb vbd in nn.",
      "to vb nnp in nn nnp vbd prp in dt nn wdt rb rb prp nn vbg in in prp$ prp md rb vb prp$ nn in in dt nn.",
      "prp nn cc vbn dt nn nnp in dt nns prp$ nn.",
      "prp vbd prp$ nns nn vbg in dt nn in vbg prp rb dt nn vbd in prp nn rb in dt nn prp vbd rb rb nn.",
      "rb nnp vbd nn prp nn.",
      "in in rb nns dt nn md vb dt jj nn dt nn vbd rb rb nn.",
      "prp vbd vbd in nns wp vbd in in dt nns in nns rb vbd in in nn in dt nn in prp$ nn.",
      "dt jj md vb vbd in nns in jj nn dt jj nn vbg dt vbn nn.",
      "cc to vb dt nn nn.",
      "in nn in vbd dt jj nn to nns cc nn.",
      "jj nn vbz to nn prp nn cc nns wrb prp vb vbg nn.",
      "dt nn in dt nn dt nn in prp$ nn vbd rb nn vbg in nn nn vb prp in dt nn vb prp in nn.",
      "nn rb nn dt nn in dt nn in dt nn nn prp nn vbd dt nn in vbd vbn rb dt nn in nn vbn in in nn.",
      "prp vbd rb cc rb prp vbd nn.",
      "prp vbn dt dt nn rb dt nn in dt nn cc in rb dt nn to dt nn cc wp dt nn rb prp$ jjs nn vbd nn nnp nn nn dt nn.",
      "in nn prp rb vbd dt md in nn in prp in nns to dt nn vbg nn in prp vbd in dt jj nn in dt jj nn.",
      "in dt nn prp vbd rb dt jj nn in nn.",
      "prp vbd dt nn in in nn nns cc nn cc wrb prp nn vbp wrb to vb dt nn prp vbn vbz nn.",
      "ex vbd dt nn in jj nns vbg nn vbg dt jj jj in dt nn nn in dt rb nn nn vbn to nn prp$ nn in dt jj in vbg in dt nnps nnp cc ex vbd dt nn jjr jj nn.",
      "nns rb jj nn rb nn vbd dt nn.",
      "prp vbn prp dt jj nn in nn rb vbd dt nn to nn to vbp dt nn cc vb dt nn.",
      "prp vbd in wp prp vbd vbg vbd nn in dt nns vbd vbg dt nn nn.",
      "prp vbd in nnp prp$ nn in dt nn nn jj nns in dt nn nn in nn.",
      "nns dt vbg in nn prp nn vbg dt nn in dt jj nn.",
      "dt nn vbd vbg in in dt nn wrb dt nn nn.",
      "nn vbp nn vbd prp$ nn prp nn.",
      "nns dt nn prp nn cc vbd prp$ nn to vb prp nn.",
      "prp vbn ex vbd nn jj in vbg nn rb to nn nn cc in dt jj nn nn cc nn prp vbd rb in jj in prp nn cc rb prp vbd nn vbg nns to nns cc dt nn in jj nns to vb prp$ nn in dt jj vbg nn in prp$ nn.",
      "prp vbd dt nn in dt nn wp vbz prp$ nn to dt jj to vbp dt nn.",
      "nn nn vbn cc rb nn.",
      "dt nn vbz vbn rb in dt nn cc dt nn vbz dt jj cc vbn nn in dt nn.",
      "dt nn vbz to vb rb in prp nn nn cc prp vbz to nn.",
      "wrb prp vbz dt nn in nn prp vbz jj jj nn.",
      "nn in dt nn prp vbz in nn in dt nn vbz prp in dt nn vbg jj cc vbn to dt nns in nn.",
      "prp vbz in in nn nns in dt nn in nn rb dt jj in nn.",
      "dt nn vbz dt nn nn vbg dt nn jj in dt nns in nn cc vbg prp$ nns nn in dt nn.",
      "dt jjs nn vbz in prp nn vb nn.",
      "nn wp md prp vbp in dt dt nn?",
      "dt nn vbd vbg dt uh nn.",
      "nn rb vbd to vb dt nn cc dt nn in dt nn vbd vbn nn.",
      "prp vbd in in prp nn vb prp$ jj nns in nn prp$ nn prp$ nn.",
      "dt nn vbd rb jj in dt jj nns wp vb in to vb dt nn in prp cc nnps nnp.",
      "dt nn vbd vbg in dt nn vbg in dt jj nn vbn in in nn rb vbg dt nn in prp$ nn cc prp vbd in prp$ nn rb dt nn in nn cc nn to vb nn.",
      "ex vbd dt nn in dt nn cc prp vbd nn prp$ nn nn.",
      "prp$ nn vbd vbd in dt nn nn in dt nn.",
      "prp md vb dt nns nn nn in nn prp md rb vb dt nn in prp$ vbg nn.",
      "prp vbd to dt nn cc vbd in dt nn.",
      "dt nn wp vbd vbn prp$ nn vbg in prp$ nns cc nn vbd in to vb wp vbd vbn nn.",
      "prp$ nn nn vbd vbg nn cc ex vbd nn in prp$ nn.",
      "prp vbd vbg in prp vbd in in dt nn in dt nn prp vbd in dt nn md vb dt nn dt jj nn in prp vbd dt jj dt nn vbd nn.",
      "nns dt nn in nn nn prp nn.",
      "dt nns nns vbd in dt nns nn.",
      "dt nn vbd dt nn nn.",
      "prp vbn dt nn vbn in in to nn rb rb vbd nn.",
      "vbn nn prp vbd dt jj nns in dt nn cc vbd dt nn in dt jj nn vbn in in nnp in dt nn vbd vbd nn wp vbz in dt nn jj in dt jj nnp jj jj nns vbn in nn nns nn cc vbz in nn.",
      "prp vbd prp in dt nns in prp$ nn cc prp rb vbn nn prp$ nn vbg in prp$ nn.",
      "prp vbd in prp vbd in nn cc vbd rb to vb dt nn in dt vbg nn.",
      "nns rb in nn prp nn.",
      "prp rb vb vbd cc rb dt nn nn.",
      "prp vbd in in prp vbd dt nn in dt nn in nnp dt nnp jj nnp cc dt dt nn vbd jj cc nn rb dt nn dt nn vbg nn nns vbg in in nn nns vbg in vbn nn prp md vb dt jj jj nn in prp$ nn cc prp rb vbd dt nn md vb in in prp md rb nn.",
      "prp dt jj vbd ex md vb dt nn in dt nn nn nns vbp dt nn prp nn in nn cc nn.",
      "nn dt nn vbd nn dt nn vbd vbg in dt nn cc nn nns vbd dt nn nn nn.",
      "prp vbd in cc vbd nn nn vbg jj rb in rb to vb nn.",
      "prp vbd dt nn in nnps cc dt nnp cc dt nn vb prp in dt nn nn.",
      "in in prp$ nn prp nn.",
      "wp vbp nn vbd to nn prp nn.",
      "dt nn vbd in cc vbd dt nns nn.",
      "in nn prp$ nns vbn in nn.",
      "prp vbd dt jj nn jj in jj nns in jj nn.",
      "dt nns vbd vbd cc nns vbd in in vbn nn.",
      "dt nn vbd rb in dt nn vbg nn dt nns vbd in prp$ in dt vbg in nn.",
      "prp vbd in to nn cc vbd in dt nn vbd nn.",
      "jj nns vbd dt in in vbn nn vbn cc nn.",
      "nns vbd vbg nn nns in dt jj in vbn nn vbg to vb dt jj nnp.",
      "prp nn vbd nn in prp nn dt nn md vb to dt nn cc prp rb vbd ex in dt nn in dt jj jj nn rb prp$ nns vbg in dt nn vbg in dt nn cc dt jj nn.",
      "prp vbd in in nn vbd jj rb jj nns in prp$ nn cc dt nn vbd vbn in cc vbn prp in jj nn.",
      "nns nn to vb dt nn prp vbd dt nn.",
      "nn vb dt jj nn prp nn cc prp nn vb prp in dt nns vbd nn.",
      "prp vbd vbg dt nn in in dt nn nn.",
      "cc rb in dt nn dt nn vbd vbg in in dt nn cc prp vbd ex vbd dt nn in dt nn.",
      "prp vbd rb in dt nn cc in prp vbd nn dt nn rb vbd in dt nn cc nn.",
      "prp vbd in in nn dt nn nn.",
      "prp vbd prp$ nn cc prp nn vb jj nn.",
      "wp vbd prp to vb dt jj nn in prp$ nn?",
      "wp vbd prp to vb nn?",
      "prp md in rb rb vbp vbd dt nn in dt jj nn.",
      "cc prp vbd vbn cc in vbd dt nn to nn.",
      "cc rb prp vbd in dt nn nn.",
    };
         
    NGramModel mm = new NGramModel(null, 5);
    for (int i = 0; i < test3.length; i++)
    {
      String[] toks = test3[i].split(" ");
      //System.out.println("added: "+RiTa.asList(toks));
      mm.loadTokens(toks, 1);  
    }
    
    for (int i = 0; i < 10; i++)
    {
      String toks = mm.generateUntil("[^.?]+[\\.\\?]", 6, 10);
      System.out.println(i+") " +toks);
    }    
  }
  
  public static void main(String[] args)
  {
    NGramModel mm = new NGramModel(null, 3);
    String data = RiTa.loadString(null, "kafka.txt");
    char[] tokens = data.toCharArray();
    mm.loadTokens(tokens);
    mm.printTree();
  }


}// end

