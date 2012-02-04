package rita.support;

import java.util.Map;

public interface NGramIF
{
  /**
   * Continues generating tokens until a token matches 'regex', assuming
   * the length of the output is between min and maxLength (inclusive).
   */
  public abstract String generateUntil(String regex, int minLength, int maxLength);

  /**
   * Generates a string of <pre>length</pre> tokens from the model.
   */
  public abstract String generateTokens(int targetNumber);

  /**
   * Returns the current n-value for the model
   */
  public abstract int getNFactor();

  /** 
   * Returns all possible next words (or tokens), ordered by probability, for the given
   * seed array, or null if none are found. <p>Note: seed arrays of any size (>0) may 
   * be input, but only the last n-1 elements will be considered.   
   */
  public abstract String[] getCompletions(String[] seed);

  /**
   * Returns the raw (unigram) probability for 
   * a token in the model, or 0 if it does not exist
   */
  public abstract float getProbability(String singleToken);

  /** 
   * Returns the probability of obtaining
   * a sequence of k character tokens were k <= nFactor,
   * e.g., if nFactor = 3, then valid lengths
   * for the String <code>tokens</code> are 1, 2 & 3.
   */
  public abstract float getProbability(String[] tokens);

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
  public abstract String[] getCompletions(String[] pre, String[] post);

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
  public abstract Map getProbabilities(String[] path);

}