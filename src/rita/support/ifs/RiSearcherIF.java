package rita.support.ifs;

public interface RiSearcherIF {

  /** 
   * Returns the bigram coherence for the word pair where
   * coherence(w1, w2) = bigram(w1 + w2)/(count(w1) + count(w2))
   * [from Gervas]
   */
  public abstract float getBigram(String word1, String word2);

  /**
   * Returns the product of the count of the query and the # of words. 
   */
  public abstract float getWeightedUnigram(String query);

  /**
   * Returns the product of the count of the query and the # of words. 
   */
  public abstract float getWeightedUnigram(String[] words);

  /**
   * Returns the product of the avg value of all bigram pairs 
   * and the min bigram value in the sentence. Equivalent to (
   * but more efficient than): getBigramAvg(s) * getBigramMin(s)
   */
  public abstract float getWeightedBigram(String[] sentence);

  /**
   * Returns the avg value of all bigram pairs in
   * the sentence. 
   */
  public abstract float getBigramAvg(String[] sentence);

  /**
   * Returns the min value of all bigram pairs in
   * the sentence. 
   */
  public abstract float getBigramMin(String[] sentence);

  /**
   * Returns the number of hits for the search query.
   * @param query The string to be searched for.
   * @return The number of hits returned for the search query.
   */
  public abstract int getCount(String query);

  /**
   * Sets the user-agent for subsequent requests
   */
  public abstract String getUserAgent();

  /**
   * Returns the current user-agent
   */
  public abstract void setUserAgent(String userAgent);

  /**
   * Sets the cookie string for subsequent requests 
   */
  public abstract void setCookie(String cookie);

}