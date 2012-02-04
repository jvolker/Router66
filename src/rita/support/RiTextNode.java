package rita.support;

import java.util.*;

public interface RiTextNode {

  /**
   * If the <code>newToken</code> does not exist as a child, creates a new 
   * child node with a frequency of 1 (e.g., else increments the existing 
   * child node's frequency by 1.   
   * @param newToken
   * @return the child node
   */
  public abstract RiTextNode addChild(String newToken);

  public abstract RiTextNode addChild(char c, int initialCount);

  /**
   * If the <code>newToken</code> does not exist as a child, creates a new 
   * child node with <code>initialCount</code> as its frequency (e.g., 
   * for smoothing), else increments the existing child nodes frequency
   * by 1   
   * @param newToken
   * @param initialCount
   * @return the child node
   */
  public abstract RiTextNode addChild(String newToken, int initialCount);

  public abstract String getToken();

  public abstract int getCount();

  public abstract int increment();

  public abstract String toString();

  //public String toString() {  return toString(-1); }

  public abstract boolean isRoot();

  public abstract void pathFromRoot(Stack result);

  public abstract int uniqueCount();

  public abstract float getProbability();

  public abstract Iterator childIterator();

  /** Returns number of children of this node */
  public abstract int numChildren();

  // order by frequency
  public abstract int compareTo(Object o);

  /** 
   * Does a lookup on the children of this node and returns 
   * any nodes that match <code>tokenToLookup</code>, else returns null.
   */
  public abstract RiTextNode lookup(String tokenToLookup);

  /** 
   * Does a lookup on the children of this node and returns 
   * any nodes that match <code>tokenToLookup</code>, else returns null.
   */
  public abstract RiTextNode lookup(RiTextNode tokenToLookup);

  /** 
   * Does a lookup on the children of this node and returns 
   * any nodes that match <code>charToLookup</code>, else returns null.
   */
  public abstract RiTextNode lookup(char charToLookup);

  public abstract Collection getChildNodes();

  public abstract String asTree(boolean sort);

  public abstract boolean isLeaf();

  public abstract void setIgnoreCase(boolean b);

  public abstract boolean isIgnoringCase();

  public abstract RiTextNode selectChild();

  public abstract RiTextNode selectChild(boolean probabalisticSelect);

  public abstract RiTextNode selectChild(String regex, boolean probabalisticSelect);

  /**
   * Returns a List of all children matching 
   * the supplied regular expression.
   * @param regex
   * @return a List of matching children
   * or null if none are found.
   */
  public abstract List getChildNodes(String regex);

  /**
   * Return true if the node has at least one child
   * matching the given regular expression (if one is supplied).
   */
  public abstract boolean hasChildren(String regex);

  public abstract boolean hasChildren();

  public abstract Map getChildMap();

  public abstract boolean isSentenceStart();

  public abstract void setIsSentenceStart(boolean isSentenceStart);

  public abstract void setCount(int initialCount); // new

}