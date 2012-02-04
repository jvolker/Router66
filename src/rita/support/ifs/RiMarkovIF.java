package rita.support.ifs;

import java.util.Map;

public interface RiMarkovIF
{
  /**
   * Creates a new RegexTokenizer from the supplied regular expression
   * and uses it when adding subsequent data to the model.
   */
  public abstract void setTokenizerRegex(String regex);

  /**
   * Generates a string of <pre>length</pre> tokens from the model.
   */
  public abstract String generateTokens(int length);

  /**  
   * Load a text file into the model -- if using Processing,
   * the file should be in the sketch's data folder. 
   * @param fileName name of file to load
   * @param multiplier weighting for tokens in the file;<br>  
   * a weight of 3 is equivalent to loading that file 3 times and gives
   * each token 3x the probability of being chosen during generation.
   */
  public abstract void loadFile(String fileName, int multiplier);

  public abstract void loadFile(String fileName);

  public abstract void loadText(String rawText);

  public boolean isPrintingIgnoredText();
  
  public void setPrintIgnoredText(boolean printIgnoredText);
  
  /** 
   * Load a String into the model, splitting the text first into sentences,
   * then into words, according to the current regular expression. 
   * 
   * @param multiplier Weighting for tokens in the String <br>  
   * 
   * A weight of 3 is equivalent to loading this text 3 times and gives
   * each token within 3x the probability of being chosen on a call to 
   * generate().
   */
  public abstract void loadText(String rawText, int multiplier);

  /**
   * Returns the # of words loaded into the model
   */
  public abstract int getWordCount();

  /**
   * Loads an array of tokens (or words) into the model; each 
   * element in the array must be a single token for proper 
   * construction of the model.
   */
  public abstract void loadTokens(String[] tokens);

  /**
   * Loads an array of tokens (or words) into the model; each 
   * element in the array must be a single token for proper 
   * construction of the model.
   * @param multiplier Weighting for tokens in the String <br>  
   * 
   * A weight of 3 is equivalent to loading these tokens 3 times and gives
   * each token 3x the probability of being chosen during generation.
   */
  public abstract void loadTokens(String[] tokens, int multiplier);

  
  /**
   * Returns the current n-value for the model
   */
  public abstract int getNFactor();
  
  /**
   * Returns the TextNode representing the root of the model's tree,
   * so that it can be (manually) navigated.
  public TextNodeIF getRoot();   */

  /**
   * Returns whether (add-1) smoothing is enabled for the model
   */
  public abstract boolean isSmoothing();

  /** 
   * Toggles whether (add-1) smoothing is enabled for the model.
   * Should be called before any data loading is done. 
   */
  public abstract void setUseSmoothing(boolean useSmoothing);

  /** 
   * Returns all possible next words (or tokens), ordered by probability, for the given
   * seed array, or null if none are found. <p>Note: seed arrays of any size may 
   * be input, but only the last n-1 elements will be considered.   
   */
  public abstract String[] getCompletions(String[] seed);

  /**
   * Returns the raw (unigram) probability for 
   * a token in the model, or 0 if it does not exist
   */
  public abstract float getProbability(String token);

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

  /**
   * Loads an array of sentences into the model; each 
   * element in the array must be a single sentence for
   * proper parsing.
   */
  public abstract void loadSentences(String[] sentences);
  
  /**
   * Loads an array of sentences into the model; each 
   * element in the array must be a single sentence for
   * proper parsing.
   * 
   * @param multiplier Weighting for this set of sentences<br>  
   * 
   * A weight of 3 is equivalent to loading these sentences 3 times and gives
   * each token 3x the probability of being chosen during generation.
   */
  public abstract void loadSentences(String[] sentences, int multiplier);

  /**
   * @deprecated use generateSentence() instead
   */
  public abstract String generate();

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
  public abstract String generateSentence();

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
  public abstract String[] generateSentences(int numSentences);

  /**
   * Returns whether the model will attempt to recognize (english-like) sentences
   * in the input text (default=true).
   */
  public abstract boolean isRecognizingSentences();

  /**
   * Sets whether the model will try to recognize 
   * (english-like) sentences in its input (default=true).
   */
  public abstract void setRecognizeSentences(boolean ignoreSentences);

  /**
   * Tells the model to ignore (english-like) sentences in its input 
   * and treat all text tokens the same.
   */
  public abstract void disableSentenceProcessing();

  public abstract boolean isIgnoringCase();
  
  /**
   * Determines whether calls to generateSentence(s) will return 
   * sentences that exist (character-for-character) in the input text.<p>
   * Note: The trade-off here is between ensuring novel outputs
   * and a potential slow-down due to rejected outputs (b/c they
   * exist in the input text.)     
   */
  public void setAllowDuplicates(boolean allow);
  
  /** Tells the model whether to ignore various quotations types in the input   */
  public void setRemoveQuotations(boolean removeQuotations);

  /** Tells whether the model is ignoring quotations found in the input  (default=true) */
  public boolean isRemovingQuotations();

  public boolean isAllowingDuplicates();

  public abstract void setMinSentenceLength(int minSentenceLength);

  public abstract int getMinSentenceLength();

  public abstract void setMaxSentenceLength(int maxSentenceLength);

  public abstract int getMaxSentenceLength();

  public abstract void loadTokens(char[] tokens);
  
  public abstract void setAddSpaces(boolean addSpacesBetweenTokens);

}// end