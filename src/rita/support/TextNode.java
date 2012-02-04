package rita.support;

import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rita.*;

/**
 * A node in a graph containing text and some # of child TextNodes  
 */
public class TextNode implements Comparable, RiProbable, RiTextNode
{
  static DecimalFormat formatter = new DecimalFormat(".###");
  
  /** the # of tokens processed */
  public static int totalTokens;
  
  /** the # of non-root nodes */
  public static int totalNodes;
  
  private int count=0;  
  protected Map children;
  protected TextNode parent;
  protected String token, lookup;
  protected boolean ignoreCase, isSentenceStart;    
  
  public static TextNode createRoot(boolean ignoreCase) 
  {    
    TextNode tn =  new TextNode(null, null);
    tn.ignoreCase = ignoreCase;
    //System.out.println("ignoreCase="+tn.ignoreCase);
    return tn;
  }  

  TextNode(TextNode parent, String token) 
  {    
    this.token = token;    
    this.parent = parent;  
    
    if (parent != null)
      this.ignoreCase = parent.ignoreCase;
    
    if (ignoreCase && token != null) {
      String lc = token.toLowerCase();
      if (!lc.equals(token))
        lookup = lc;
    }
//System.out.println("NEW: "+this);    
  }
  
  /* (non-Javadoc)
   * @see rita.support.TextNodeIF#addChild(java.lang.String)
   */
  public TextNode addChild(String newToken) 
  {      
    return this.addChild(newToken, 1);
  } 
  
  /* (non-Javadoc)
   * @see rita.support.TextNodeIF#addChild(char, int)
   */
  public TextNode addChild(char c, int initialCount) 
  {     
    return this.addChild(Character.toString(c), initialCount);
  }
  
  /* (non-Javadoc)
   * @see rita.support.TextNodeIF#addChild(java.lang.String, int)
   */
  public TextNode addChild(String newToken, int initialCount) 
  { 
    // create the child map for this Node
    if (children == null) 
      children = new HashMap();     
    
    TextNode node = lookup(newToken);

    //  add first instance of this token 
    if (node == null) {
      String key = getLookupKey(newToken);
      children.put(key, node = new TextNode(this, newToken));  
      node.count = initialCount;
    }
    else {         
      node.increment(); // up the frequency
    }
    
    return node;
  }

  /* (non-Javadoc)
   * @see rita.support.TextNodeIF#getToken()
   */
  public String getToken()
  {
    return isRoot() ? "ROOT" : token;
  }

  /* (non-Javadoc)
   * @see rita.support.TextNodeIF#getCount()
   */
  public int getCount()
  {
    return count;
  }

  /* (non-Javadoc)
   * @see rita.support.TextNodeIF#increment()
   */
  public int increment()
  {
    totalTokens++;
    count = count + 1;
    return count;
  }
   
  /* (non-Javadoc)
   * @see rita.support.TextNodeIF#toString()
   */
  public String toString()
  {
    String result = "["+getToken()+"]";//lookup;//"("+isSentenceStart+")";
    /*if (!getWord().equals("ROOT")) { 
      result += " (" + count + "," + 
        formatter.format(getProbability()) + "%)"; 
    }*/
    return result;    
  }
  //public String toString() {  return toString(-1); }
  
  /* (non-Javadoc)
   * @see rita.support.TextNodeIF#isRoot()
   */
  public boolean isRoot()
  {
    return parent == null;
  }
  
  /* (non-Javadoc)
   * @see rita.support.TextNodeIF#pathFromRoot(java.util.Stack)
   */
  public void pathFromRoot(Stack result)
  {       
    TextNode mn = this;
    if (result == null)
      result = new Stack();
    while (true) {
      if (mn.isRoot()) break;
      result.push(mn.getToken());      
      mn = (TextNode)mn.parent;          
    }   
  }
  
  /* (non-Javadoc)
   * @see rita.support.TextNodeIF#uniqueCount()
   */
  public int uniqueCount()
  {
    if (children == null)
      return 0;
    return children.size();
  }

  //   total count for all children at this level    
  int siblingCount()
  {
    if (isRoot()) {
      System.err.println("WARN: Sibling count on ROOT!");
      return 1;
    }
    
    if (parent == null)
      throw new RuntimeException("Null parent for: "+token);
    
    int sum = 0;
    for (Iterator i = parent.childIterator(); i.hasNext();) {
      TextNode node = (TextNode)i.next();
      if (node != null) sum += node.getCount();
    } 
    return sum;
  }
  
  /* (non-Javadoc)
   * @see rita.support.TextNodeIF#getProbability()
   */
  public float getProbability() {
    return count/(float)siblingCount();
  }
  
  /* (non-Javadoc)
   * @see rita.support.TextNodeIF#childIterator()
   */
  public Iterator childIterator()
  {
    if (children == null)
      children = new HashMap();
    return children.values().iterator();
  }

  /* (non-Javadoc)
   * @see rita.support.TextNodeIF#numChildren()
   */
  public int numChildren()
  {
    return (children==null) ? 0 : children.size();
  }

  // order by frequency
  /* (non-Javadoc)
   * @see rita.support.TextNodeIF#compareTo(java.lang.Object)
   */
  public int compareTo(Object o)
  {
    float nf1 = getCount();
    float nf2 = ((TextNode) o).getCount();
    if (nf1 == nf2) // return lex-order on ties (?)
      return token.compareTo(((TextNode) o).getToken());
    return nf1 < nf2 ? 1 : -1;
  }

  /* (non-Javadoc)
   * @see rita.support.TextNodeIF#lookup(java.lang.String)
   */
  public TextNode lookup(String tokenToLookup)
  {
   // // System.out.println("TextNode.lookup("+tokenToLookup+")");
    if (children == null || tokenToLookup == null || tokenToLookup.length()<1) 
      return null;    
    String key = getLookupKey(tokenToLookup);    
    return (TextNode)children.get(key);
  }
  
  /* (non-Javadoc)
   * @see rita.support.TextNodeIF#lookup(rita.support.TextNode)
   */
  public TextNode lookup(RiTextNode tokenToLookup)
  {   
    if (tokenToLookup == null) return null;
    return (TextNode)children.get(getLookupKey(tokenToLookup.getToken()));    
  }
  
  /* (non-Javadoc)
   * @see rita.support.TextNodeIF#lookup(char)
   */
  public TextNode lookup(char charToLookup)
  {
    if (children == null || charToLookup == RiTravesty.NULL_CHAR) 
      return null;
    return (TextNode)children.get(getLookupKey(charToLookup));
  }

  private String getLookupKey(char c)
  {
    char key = c;
    if (ignoreCase) 
      key = Character.toLowerCase(c);
    return Character.toString(key);
  }
  
  private String getLookupKey(String newToken)
  {
    String key = newToken;
    if (ignoreCase) 
      key = newToken.toLowerCase();
    return key;
  }

  /* (non-Javadoc)
   * @see rita.support.TextNodeIF#getChildNodes()
   */
  public Collection getChildNodes()
  {
    if (children == null) return null;
    return children.values();
  }
  
  private String childrenToString
    (TextNode mn, String str, int depth, boolean sort) 
  {
    List l = new ArrayList(mn.children.values());    
    if (sort) Collections.sort(l);
    Iterator i =  l.iterator();
    if (i == null) return str;
    TextNode node = null;
    String indent = "\n";
    for (int j = 0; j < depth; j++) 
      indent += "  ";
    while (i.hasNext()) 
    {
      node = (TextNode)i.next();
      if (node == null) break;
      String tok = node.getToken();      
      if (tok != null) {         
        if (tok.equals("\n"))
          tok = "\\n";
        else if (tok.equals("\r"))
          tok = "\\r";
        else if (tok.equals("\t"))
          tok = "\\t";
        else if (tok.equals("\r\n"))
          tok = "\\r\\n";
      }
      str += indent +"'"+tok+"'";
      if (node.count == 0) 
        throw new RuntimeException("ILLEGAL FREQ: "+node.count+" -> "+mn.token+","+node.token);
      
      if (!node.isRoot())
        str += " ["+node.count + ",p=" +formatter.format
          (node.getProbability()) + "]->{"; 
      //if (node.isSentenceStart) 
        //str += "[START]";
      if (node.getChildNodes() != null)
        str = childrenToString(node, str, depth+1, sort);  
      else str += "}";
    }
    indent = "\n";
    for (int j = 0; j < depth-1; j++) 
      indent += "  ";
    
    return str+indent+"}";
  }
  
  /* (non-Javadoc)
   * @see rita.support.TextNodeIF#asTree(boolean)
   */
  public String asTree(boolean sort) 
  {
    String s = getToken()+" ";
    if (!isRoot()) 
      s+= "("+count+")->"; 
    s += "{";
    if (!isLeaf())
      return childrenToString(this, s, 1, sort);
    return s + "}";
  }
  
  /* (non-Javadoc)
   * @see rita.support.TextNodeIF#isLeaf()
   */
  public boolean isLeaf()
  {    
    return children == null || children.size() == 0;
  }
  
  /* (non-Javadoc)
   * @see rita.support.TextNodeIF#setIgnoreCase(boolean)
   */
  public void setIgnoreCase(boolean b) {
    if (!isRoot()) throw new RiTaException
      ("Illegal to set the ignore-case flag on any Node but the root");
    this.ignoreCase = true;    
  }
  
  /* (non-Javadoc)
   * @see rita.support.TextNodeIF#isIgnoringCase()
   */
  public boolean isIgnoringCase()
  {
    return ignoreCase;
  }
  
  /* (non-Javadoc)
   * @see rita.support.TextNodeIF#selectChild()
   */
  public TextNode selectChild() 
  {
    return this.selectChild(true);
  }
  
  /* (non-Javadoc)
   * @see rita.support.TextNodeIF#selectChild(boolean)
   */
  public TextNode selectChild(boolean probabalisticSelect) 
  {
    return this.selectChild((String)null, probabalisticSelect);
  }
  
  /* (non-Javadoc)
   * @see rita.support.TextNodeIF#selectChild(java.lang.String, boolean)
   */
  public TextNode selectChild(String regex, boolean probabalisticSelect) {
    if (children == null) return null;
    Collection c = (regex != null) ? getChildNodes(regex) : children.values();
    return selectChild(c, probabalisticSelect);
  }
  
  protected TextNode selectChild(Collection c, boolean probabalisticSelect) 
  {
    return (TextNode)RiTa.select(c, probabalisticSelect);
  }

  /**
   * Returns a List of all children matching 
   * the supplied regular expression.
   * @param regex

  public List getChildren(String regex)
  {
    Matcher m = null;
    List tmp = new LinkedList();
    Pattern p = Pattern.compile(regex);     
    for (Iterator i = childIterator(); i.hasNext();)
    {
      TextNode tn = (TextNode) i.next();
      m = p.matcher(tn.getWord());
      if (m.matches())
        tmp.add(tn.getWord());      
    }
    return tmp;
  }   */
  
  /* (non-Javadoc)
   * @see rita.support.TextNodeIF#getChildNodes(java.lang.String)
   */
  public List getChildNodes(String regex)
  {
    Matcher m = null;
    List tmp = null;
    if (children == null || children.size()==0)
      return null;
    Pattern p = Pattern.compile(regex);    
    for (Iterator i = childIterator(); i.hasNext();)
    {
      TextNode tn = (TextNode) i.next();
      m = p.matcher(tn.getToken());
      if (m.matches()) {
        if (tmp == null) 
          tmp = new LinkedList();
        tmp.add(tn);    
      }
    }
    return tmp;
  }    

  /* (non-Javadoc)
   * @see rita.support.TextNodeIF#hasChildren(java.lang.String)
   */
  public boolean hasChildren(String regex)
  {
    return getChildNodes(regex).size() > 0;
  }
  
  /* (non-Javadoc)
   * @see rita.support.TextNodeIF#hasChildren()
   */
  public boolean hasChildren()
  {
    return children != null && children.size() > 0;
  }

  /* (non-Javadoc)
   * @see rita.support.TextNodeIF#getChildMap()
   */
  public Map getChildMap()
  {
    return children;
  }

  /* (non-Javadoc)
   * @see rita.support.TextNodeIF#isSentenceStart()
   */
  public boolean isSentenceStart()
  {
    return this.isSentenceStart;
  }

  /* (non-Javadoc)
   * @see rita.support.TextNodeIF#setIsSentenceStart(boolean)
   */
  public void setIsSentenceStart(boolean isSentenceStart)
  {
    this.isSentenceStart = isSentenceStart;
  }
  
  public void setCount(int initialCount) {
    this.count = initialCount;
  }
  
  /**
   *  To satisfy the RiProbable interface; simply returns the count here
   */
  public float getRawValue()
  {
    return getCount();
  }
  
  public static void main(String[] args)
  {
    TextNode rt = createRoot(true);
    rt.addChild("I");
    rt.addChild("i");
    System.out.println(rt.asTree(true));  
  }

}// end
