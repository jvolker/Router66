package rita.support.dyn;

import java.util.Collection;

import rita.support.ifs.RiWordnetIF;
import rita.support.remote.RiWordnetSupport;

/**
 * A Wordnet implementation that allows clients to dynamically 
 * load and calls methods from the RiTa.Wordnet package at runtime 
 * if it is available. All methods are auto-generated. 
 */
public class RiWordnetImpl extends RiDynamicObject implements RiWordnetIF
{     
  private static RiWordnetImpl instance;

  public static RiWordnetImpl getInstance() throws ClassNotFoundException
  {
    if (instance == null) {
      Class c = Class.forName(RiWordnetSupport.RITA_WORDNET_CLASS);
//System.out.println("RiWordnetImpl.getInstance() -> created "+c);      
      instance = new RiWordnetImpl(c);
    }
    return instance;
  }
  
  //RiWordnetIF delegate;  (auto-gen)
  private RiWordnetImpl(Object dynamicProxy) {
    super(dynamicProxy, RiWordnetIF.class);//getClass().getInterfaces());
  }
  
  public static void main(String[] args)
  {        
    //RiWordnetImpl rw = new RiWordnetImpl(Class.forName("rita.wordnet.RiWordnet"));
/*    String[] s = rw.getSynonyms("dog", "n");
    for (int i = 0; i < s.length; i++)
      System.out.println(i+"] "+s[i]);   */
  }
  
  // ------------ interface methods (auto-generated) -------------
  
  public boolean exists(String word) {
    return ((RiWordnetIF)delegate).exists(word);
  }

  public String[] filter(int filterFlag, String word, String pos, int maxResults) {
    return ((RiWordnetIF)delegate).filter(filterFlag, word, pos, maxResults);
  }

  public String[] filter(int filterFlag, String word, String pos) {
    return ((RiWordnetIF)delegate).filter(filterFlag, word, pos);
  }

  public String[] getAllAlsoSees(String query, String pos) {
    return ((RiWordnetIF)delegate).getAllAlsoSees(query, pos);
  }

  public String[] getAllAntonyms(String word, String pos) {
    return ((RiWordnetIF)delegate).getAllAntonyms(word, pos);
  }

  public String[] getAllCoordinates(String query, String pos) {
    return ((RiWordnetIF)delegate).getAllCoordinates(query, pos);
  }

  public String[] getAllDerivedTerms(String query, String pos) {
    return ((RiWordnetIF)delegate).getAllDerivedTerms(query, pos);
  }

  public String[] getAllExamples(CharSequence word, CharSequence pos) {
    return ((RiWordnetIF)delegate).getAllExamples(word, pos);
  }

  public String[] getAllGlosses(String word, String pos) {
    return ((RiWordnetIF)delegate).getAllGlosses(word, pos);
  }

  public String[] getAllHolonyms(String query, String pos) {
    return ((RiWordnetIF)delegate).getAllHolonyms(query, pos);
  }

  public String[] getAllHypernyms(String word, String posStr) {
    return ((RiWordnetIF)delegate).getAllHypernyms(word, posStr);
  }

  public String[] getAllHyponyms(String word, String posStr) {
    return ((RiWordnetIF)delegate).getAllHyponyms(word, posStr);
  }

  public String[] getAllMeronyms(String query, String pos) {
    return ((RiWordnetIF)delegate).getAllMeronyms(query, pos);
  }

  public String[] getAllNominalizations(String query, String pos) {
    return ((RiWordnetIF)delegate).getAllNominalizations(query, pos);
  }

  public String[] getAllSimilar(String query, String pos) {
    return ((RiWordnetIF)delegate).getAllSimilar(query, pos);
  }

  public String[] getAllSynonyms(int senseId, int maxResults) {
    return ((RiWordnetIF)delegate).getAllSynonyms(senseId, maxResults);
  }

  public String[] getAllSynonyms(int id) {
    return ((RiWordnetIF)delegate).getAllSynonyms(id);
  }

  public String[] getAllSynonyms(String word, String posStr, int maxResults) {
    return ((RiWordnetIF)delegate).getAllSynonyms(word, posStr, maxResults);
  }

  public String[] getAllSynonyms(String word, String posStr) {
    return ((RiWordnetIF)delegate).getAllSynonyms(word, posStr);
  }

  public String[] getAllSynsets(String word, String posStr) {
    return ((RiWordnetIF)delegate).getAllSynsets(word, posStr);
  }

  public String[] getAllVerbGroups(String query, String pos) {
    return ((RiWordnetIF)delegate).getAllVerbGroups(query, pos);
  }

  public String[] getAlsoSees(int senseId) {
    return ((RiWordnetIF)delegate).getAlsoSees(senseId);
  }

  public String[] getAlsoSees(String query, String pos) {
    return ((RiWordnetIF)delegate).getAlsoSees(query, pos);
  }

  public String[] getAnagrams(String word, String posStr, int maxResults) {
    return ((RiWordnetIF)delegate).getAnagrams(word, posStr, maxResults);
  }

  public String[] getAnagrams(String word, String posStr) {
    return ((RiWordnetIF)delegate).getAnagrams(word, posStr);
  }

  public String[] getAntonyms(int id) {
    return ((RiWordnetIF)delegate).getAntonyms(id);
  }

  public String[] getAntonyms(String word, String pos) {
    return ((RiWordnetIF)delegate).getAntonyms(word, pos);
  }

  public String getAnyExample(CharSequence word, CharSequence pos) {
    return ((RiWordnetIF)delegate).getAnyExample(word, pos);
  }

  public String getBestPos(String word) {
    return ((RiWordnetIF)delegate).getBestPos(word);
  }

  public String[] getCommonParents(String word1, String word2, String pos) {
    return ((RiWordnetIF)delegate).getCommonParents(word1, word2, pos);
  }

  public String[] getContains(String word, String posStr, int maxResults) {
    return ((RiWordnetIF)delegate).getContains(word, posStr, maxResults);
  }

  public String[] getContains(String word, String posStr) {
    return ((RiWordnetIF)delegate).getContains(word, posStr);
  }

  public String[] getCoordinates(int id) {
    return ((RiWordnetIF)delegate).getCoordinates(id);
  }

  public String[] getCoordinates(String query, String pos) {
    return ((RiWordnetIF)delegate).getCoordinates(query, pos);
  }

  public String[] getDerivedTerms(int id) {
    return ((RiWordnetIF)delegate).getDerivedTerms(id);
  }

  public String[] getDerivedTerms(String query, String pos) {
    return ((RiWordnetIF)delegate).getDerivedTerms(query, pos);
  }

  public String getDescription(int senseId) {
    return ((RiWordnetIF)delegate).getDescription(senseId);
  }

  public String getDescription(String word, String pos) {
    return ((RiWordnetIF)delegate).getDescription(word, pos);
  }

  public float getDistance(String lemma1, String lemma2, String pos) {
    return ((RiWordnetIF)delegate).getDistance(lemma1, lemma2, pos);
  }

  public String[] getEndsWith(String word, String posStr, int maxResults) {
    return ((RiWordnetIF)delegate).getEndsWith(word, posStr, maxResults);
  }

  public String[] getEndsWith(String word, String posStr) {
    return ((RiWordnetIF)delegate).getEndsWith(word, posStr);
  }

  public String[] getExamples(CharSequence word, CharSequence pos) {
    return ((RiWordnetIF)delegate).getExamples(word, pos);
  }

  public String[] getExamples(int senseId) {
    return ((RiWordnetIF)delegate).getExamples(senseId);
  }

  public String getGloss(int senseId) {
    return ((RiWordnetIF)delegate).getGloss(senseId);
  }

  public String getGloss(String word, String pos) {
    return ((RiWordnetIF)delegate).getGloss(word, pos);
  }

  public String[] getHolonyms(int id) {
    return ((RiWordnetIF)delegate).getHolonyms(id);
  }

  public String[] getHolonyms(String query, String pos) {
    return ((RiWordnetIF)delegate).getHolonyms(query, pos);
  }

  public String[] getHypernyms(int id) {
    return ((RiWordnetIF)delegate).getHypernyms(id);
  }

  public String[] getHypernyms(String word, String posStr) {
    return ((RiWordnetIF)delegate).getHypernyms(word, posStr);
  }

  public String[] getHypernymTree(int id) {
    return ((RiWordnetIF)delegate).getHypernymTree(id);
  }

  public String[] getHyponyms(int id) {
    return ((RiWordnetIF)delegate).getHyponyms(id);
  }

  public String[] getHyponyms(String word, String posStr) {
    return ((RiWordnetIF)delegate).getHyponyms(word, posStr);
  }

  public String[] getHyponymTree(int id) {
    return ((RiWordnetIF)delegate).getHyponymTree(id);
  }

  public String[] getMeronyms(int id) {
    return ((RiWordnetIF)delegate).getMeronyms(id);
  }

  public String[] getMeronyms(String query, String pos) {
    return ((RiWordnetIF)delegate).getMeronyms(query, pos);
  }

  public String[] getNominalizations(int id) {
    return ((RiWordnetIF)delegate).getNominalizations(id);
  }

  public String[] getNominalizations(String query, String pos) {
    return ((RiWordnetIF)delegate).getNominalizations(query, pos);
  }

  public String getPos(int id) {
    return ((RiWordnetIF)delegate).getPos(id);
  }

  public String[] getPos(String word) {
    return ((RiWordnetIF)delegate).getPos(word);
  }

  public String getRandomExample(CharSequence pos) {
    return ((RiWordnetIF)delegate).getRandomExample(pos);
  }

  public String[] getRandomExamples(CharSequence pos, int numExamples) {
    return ((RiWordnetIF)delegate).getRandomExamples(pos, numExamples);
  }

  public String getRandomWord(CharSequence pos, boolean stemsOnly, int maxChars) {
    return ((RiWordnetIF)delegate).getRandomWord(pos, stemsOnly, maxChars);
  }

  public String getRandomWord(CharSequence pos) {
    return ((RiWordnetIF)delegate).getRandomWord(pos);
  }

  public String[] getRandomWords(CharSequence pos, int count) {
    return ((RiWordnetIF)delegate).getRandomWords(pos, count);
  }

  public String[] getRegexMatch(String pattern, String posStr, int maxResults) {
    return ((RiWordnetIF)delegate).getRegexMatch(pattern, posStr, maxResults);
  }

  public String[] getRegexMatch(String pattern, String posStr) {
    return ((RiWordnetIF)delegate).getRegexMatch(pattern, posStr);
  }

  public int getSenseCount(String word, String pos) {
    return ((RiWordnetIF)delegate).getSenseCount(word, pos);
  }

  public int[] getSenseIds(String word, String posStr) {
    return ((RiWordnetIF)delegate).getSenseIds(word, posStr);
  }

  public String[] getSimilar(int id) {
    return ((RiWordnetIF)delegate).getSimilar(id);
  }

  public String[] getSimilar(String query, String pos) {
    return ((RiWordnetIF)delegate).getSimilar(query, pos);
  }

  public String[] getSoundsLike(String pattern, String posStr, int maxResults) {
    return ((RiWordnetIF)delegate).getSoundsLike(pattern, posStr, maxResults);
  }

  public String[] getSoundsLike(String pattern, String posStr) {
    return ((RiWordnetIF)delegate).getSoundsLike(pattern, posStr);
  }

  public String[] getStartsWith(String word, String posStr, int maxResults) {
    return ((RiWordnetIF)delegate).getStartsWith(word, posStr, maxResults);
  }

  public String[] getStartsWith(String word, String posStr) {
    return ((RiWordnetIF)delegate).getStartsWith(word, posStr);
  }

  public String[] getStems(String query, CharSequence pos) {
    return ((RiWordnetIF)delegate).getStems(query, pos);
  }

  public String[] getSynonyms(String word, String posStr, int maxResults) {
    return ((RiWordnetIF)delegate).getSynonyms(word, posStr, maxResults);
  }

  public String[] getSynonyms(String word, String posStr) {
    return ((RiWordnetIF)delegate).getSynonyms(word, posStr);
  }

  public String[] getSynset(int id) {
    return ((RiWordnetIF)delegate).getSynset(id);
  }

  public String[] getSynset(String word, String pos, boolean includeOriginal) {
    return ((RiWordnetIF)delegate).getSynset(word, pos, includeOriginal);
  }

  public String[] getSynset(String word, String pos) {
    return ((RiWordnetIF)delegate).getSynset(word, pos);
  }

  public String[] getVerbGroup(int id) {
    return ((RiWordnetIF)delegate).getVerbGroup(id);
  }

  public String[] getVerbGroup(String query, String pos) {
    return ((RiWordnetIF)delegate).getVerbGroup(query, pos);
  }

  public String[] getWildcardMatch(String pattern, String posStr, int maxResults) {
    return ((RiWordnetIF)delegate).getWildcardMatch(pattern, posStr, maxResults);
  }

  public String[] getWildcardMatch(String pattern, String posStr) {
    return ((RiWordnetIF)delegate).getWildcardMatch(pattern, posStr);
  }

  public void ignoreCompoundWords(boolean ignoreCompoundWords) {
    ((RiWordnetIF)delegate).ignoreCompoundWords(ignoreCompoundWords);
  }

  public void ignoreUpperCaseWords(boolean ignoreUpperCaseWords) {
    ((RiWordnetIF)delegate).ignoreUpperCaseWords(ignoreUpperCaseWords);
  }

  public boolean isAdjective(String word) {
    return ((RiWordnetIF)delegate).isAdjective(word);
  }

  public boolean isAdverb(String word) {
    return ((RiWordnetIF)delegate).isAdverb(word);
  }

  public boolean isIgnoringCompoundWords() {
    return ((RiWordnetIF)delegate).isIgnoringCompoundWords();
  }

  public boolean isIgnoringUpperCaseWords() {
    return ((RiWordnetIF)delegate).isIgnoringUpperCaseWords();
  }

  public boolean isNoun(String word) {
    return ((RiWordnetIF)delegate).isNoun(word);
  }

  public boolean isStem(String word, CharSequence pos) {
    return ((RiWordnetIF)delegate).isStem(word, pos);
  }

  public boolean isVerb(String word) {
    return ((RiWordnetIF)delegate).isVerb(word);
  }

  public String[] orFilter(int[] filterFlags, String[] words, String pos,
      int maxResults) {
    return ((RiWordnetIF)delegate).orFilter(filterFlags, words, pos, maxResults);
  }

  public String[] orFilter(int[] filterFlag, String[] word, String pos) {
    return ((RiWordnetIF)delegate).orFilter(filterFlag, word, pos);
  }

  public void printHypernymTree(int senseId) {
    ((RiWordnetIF)delegate).printHypernymTree(senseId);
  }

  public void printHyponymTree(int senseId) {
    ((RiWordnetIF)delegate).printHyponymTree(senseId);
  }

  public void removeNonExistent(Collection words) {
    ((RiWordnetIF)delegate).removeNonExistent(words);
  }

  public void setWordnetHome(String wordnetHome) {
    ((RiWordnetIF)delegate).setWordnetHome(wordnetHome);
  }

}// end
