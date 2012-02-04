package rita.support.ifs;

import java.util.Collection;

public interface RiWordnetIF {

  /**
   * Returns up to <code>maxResults</code> full anagram matches 
   * for the specified <code>word</code> and <code>pos</code><p>
   * Example: 'table' returns 'bleat' (but not 'tale').
   */
  public abstract String[] getAnagrams(String word, String posStr,
      int maxResults);

  /**
   * Returns all full anagram matches 
   * for the specified <code>word</code> and <code>pos</code><p>
   * Example: 'table' returns 'bleat' (but not 'tale').
   * @param word
   * @param posStr
   */
  public abstract String[] getAnagrams(String word, String posStr);

  /**
   * Returns up to <code>maxResults</code> 
   * of the specified <code>pos</code>
   *    where each contains the given <code>word</code><p> 
   * Example: 'table' returns 'bleat' (but not 'tale').
   * @param word
   * @param posStr
   * @param maxResults
   */
  public abstract String[] getContains(String word, String posStr,
      int maxResults);

  /**
   * Returns all 'contains' matches for the specified <code>word</code> and <code>pos</code><p>
   * Example: 'table' returns 'bleat' (but not 'tale').
   * @param word
   * @param posStr
   */
  public abstract String[] getContains(String word, String posStr);

  /**
   * Returns up to <code>maxResults</code> of the specified <code>pos</code>
   *   ending with the given <code>word</code>.<p> 
   * Example: 'table' returns 'turntable' & 'uncomfortable'
   * @param word
   * @param posStr
   * @param maxResults
   */
  public abstract String[] getEndsWith(String word, String posStr,
      int maxResults);

  /**
   * Returns up to <code>maxResults</code> of the specified <code>pos</code>
   *   ending with the given <code>word</code>.<p> 
   * Example: 'table' returns 'turntable' & 'uncomfortable'
   * @param word
   * @param posStr
   */
  public abstract String[] getEndsWith(String word, String posStr);

  /**
   * Returns up to <code>maxResults</code> of the specified <code>pos</code>
   *   starting with the given <code>word</code>.<p> 
   * Example: 'turn' returns 'turntable'
   * @param word
   * @param posStr
   * @param maxResults
   */
  public abstract String[] getStartsWith(String word, String posStr,
      int maxResults);

  /**
   * Returns up to <code>maxResults</code> of the specified <code>pos</code>
   *   starting with the given <code>word</code>.<p>
   * Example: 'turn' returns 'turntable'
   * @param word
   * @param posStr
   */
  public abstract String[] getStartsWith(String word, String posStr);

  /**
   * Returns up to <code>maxResults</code> of the specified <code>pos</code>
   *    matching the the given regular expression <code>pattern</code>.<p> 
   * @param pattern
   * @param posStr
   * @param maxResults
   * @see java.util.regex.Pattern
   */
  public abstract String[] getRegexMatch(String pattern, String posStr,
      int maxResults);

  /**
   * Returns up to <code>maxResults</code> of the specified <code>pos</code>
   * Example: 'table' returns 'turntable' & 'uncomfortable'
   * @param pattern
   * @param posStr
   * @see java.util.regex.Pattern
   */
  public abstract String[] getRegexMatch(String pattern, String posStr);

  /**
   * Returns up to <code>maxResults</code> of the specified <code>pos</code>
   *   that match the soundex code of the given <code>word</code>.<p> 
   * @param pattern
   * @param posStr
   * @param maxResults
   */
  public abstract String[] getSoundsLike(String pattern, String posStr,
      int maxResults);

  /**
   * Returns up to <code>maxResults</code> of the specified <code>pos</code>
   *   that match the soundex code of the given <code>word</code>.
   * @param pattern
   * @param posStr
   */
  public abstract String[] getSoundsLike(String pattern, String posStr);

  /**
   * Returns up to <code>maxResults</code> of the specified <code>pos</code>
   *   matching  a wildcard <code>pattern</code>,<br>
   * with * '*' equals any number of characters, <br>
   * and '?' equals any single character.<p>
   * Example: 't?le' returns (tale,tile,tole)<br>
   * Example: 't*le' returns (tatumble, turtle, tussle, etc.)<br>
   * Example: 't?le*' returns (telex, tile,tilefish,tile,talent, tiles, etc.)<br>
   * @param pattern
   * @param posStr
   * @param maxResults
   */
  public abstract String[] getWildcardMatch(String pattern, String posStr,
      int maxResults);

  /**
   * Returns up to <code>maxResults</code> of the specified <code>pos</code>
   * matching  a wildcard <code>pattern</code>,<br>
   * with '*' representing any number of characters, <br>
   * and '?' equals any single character..<p>
   * Example: 't?le' returns (tale,tile,tole)<br>
   * Example: 't*le' returns (tatumble, turtle, tussle, etc.)<br>
   * Example: 't?le*' returns (telex, tile,tilefish,tile,talent, tiles, etc.)<br>
   * @param pattern
   * @param posStr
   */
  public abstract String[] getWildcardMatch(String pattern, String posStr);

  /**
   * Return up to <code>maxResults</code> instances of specified 
   * <code>posStr</code> matching the filter specified with <code>filterFlag</code><p>
   * Filter types include:
   * <pre>
        RiWordnet.EXACT_MATCH
        RiWordnet.ENDS_WITH
        RiWordnet.STARTS_WITH
        RiWordnet.ANAGRAMS 
        RiWordnet.CONTAINS_ALL
        RiWordnet.CONTAINS_SOME  
        RiWordnet.CONTAINS
        RiWordnet.SIMILAR_TO
        RiWordnet.SOUNDS_LIKE
        RiWordnet.WILDCARD_MATCH
        RiWordnet.REGEX_MATCH                
   * </pre>
   * @param filterFlag
   * @param word
   * @param pos
   * @param maxResults
   * @invisible
   */
  public abstract String[] filter(int filterFlag, String word, String pos, int maxResults);

  /**
   * @invisible
   * Return all instances of specified <code>posStr</code>
   *  matching the filter specified with <code>filterFlag</code>.<p>
   * Filter types include:
   * <pre>
        RiWordnet.EXACT_MATCH
        RiWordnet.ENDS_WITH
        RiWordnet.STARTS_WITH
        RiWordnet.ANAGRAMS 
        RiWordnet.CONTAINS_ALL
        RiWordnet.CONTAINS_SOME  
        RiWordnet.CONTAINS
        RiWordnet.SIMILAR_TO
        RiWordnet.SOUNDS_LIKE
        RiWordnet.WILDCARD_MATCH
        RiWordnet.REGEX_MATCH                
   * </pre>ss  
   * @param word
   * @param pos
   * @param filterFlag
   */
  public abstract String[] filter(int filterFlag, String word, String pos);

  /**
   * Return up to <code>maxResults</code> instances of specified 
   * matching ANY of the filters specified with <code>filterFlags</code>.<p>
   * Filter types include:
   * <pre>
        RiWordnet.EXACT_MATCH
        RiWordnet.ENDS_WITH
        RiWordnet.STARTS_WITH
        RiWordnet.ANAGRAMS 
        RiWordnet.CONTAINS_ALL
        RiWordnet.CONTAINS_SOME  
        RiWordnet.CONTAINS
        RiWordnet.SIMILAR_TO
        RiWordnet.SOUNDS_LIKE
        RiWordnet.WILDCARD_MATCH
        RiWordnet.REGEX_MATCH                
   * </pre>
   * @param filterFlags
   * @param words
   * @param pos
   * @param maxResults
   * @invisible
   */
  public abstract String[] orFilter(int[] filterFlags, String[] words, String pos,
      int maxResults);

  /**
   * @invisible
   * Return all instances of specified <code>posStr</code>
   *  matching ANY of the filters specified with <code>filterFlags</code>.<p>
   * Filter types include:
   * <pre>
        RiWordnet.EXACT_MATCH
        RiWordnet.ENDS_WITH
        RiWordnet.STARTS_WITH
        RiWordnet.ANAGRAMS 
        RiWordnet.CONTAINS_ALL
        RiWordnet.CONTAINS_SOME  
        RiWordnet.CONTAINS
        RiWordnet.SIMILAR_TO
        RiWordnet.SOUNDS_LIKE
        RiWordnet.WILDCARD_MATCH
        RiWordnet.REGEX_MATCH                
   * </pre>
   * @param word
   * @param pos
   * @param filterFlag
   */
  public abstract String[] orFilter(int[] filterFlag, String[] word, String pos);

  /**
   * @invisible
   */
  public abstract void setWordnetHome(String wordnetHome);

  /**
   * Returns String[] of unique ids, one for each 'sense' of <code>word</code>
   * with <code>pos</code>, or null if none are found.<p> A Wordnet
   * 'sense' refers to a specific Wordnet meaning and maps 1-1 
   * to the concept of synsets. Each 'sense' of a word exists 
   * in a different synset. <p> For more info, see: 
        http://wordnet.princeton.edu/man/wngloss.7WN.html
   */
  public abstract int[] getSenseIds(String word, String posStr);

  /** 
   * Returns full gloss for !st sense of 'word' 
   * with 'pos' or null if not found
   */
  public abstract String getGloss(String word, String pos);

  /** 
   * Returns glosses for all senses of 'word' 
   * with 'pos', or null if not found
   */
  public abstract String[] getAllGlosses(String word, String pos);

  /**
   * Returns full gloss for word with unique <code>senseId</code>, or null if
   * not found
   */
  public abstract String getGloss(int senseId);

  /**
   * Returns description for word with unique <code>senseId</code>, 
   * or null if not found
   */
  public abstract String getDescription(int senseId);

  /** Returns description for <code>word</code> with <code>pos</code> or null if not found */
  public abstract String getDescription(String word, String pos);

  /**
   * Returns all examples for 1st sense of <code>word</code> with <code>pos</code>,
   * or null if not found
   */
  public abstract String[] getExamples(CharSequence word, CharSequence pos);

  /**
   * Return a random example from the set of examples from all senses
   * of <code>word</code> with <code>pos</code>, assuming they contain
   * <code>word</code>, or else null if not found
   */
  public abstract String getAnyExample(CharSequence word, CharSequence pos);

  /**
   * Returns examples for word with unique <code>senseId</code>, or null if
   * not found
   */
  public abstract String[] getExamples(int senseId);

  /**
   *  Returns examples for all senses of <code>word</code> with <code>pos</code>
   *  if they contain the <code>word</code>, else null 
   *  if not found   
   */
  public abstract String[] getAllExamples(CharSequence word, CharSequence pos);

  /**
   * Returns an unordered String[] containing the 
   * synset, hyponyms, similars, alsoSees, 
   * and coordinate terms (checking each in order),
   * or null if not found.
   */
  public abstract String[] getAllSynonyms(int senseId, int maxResults);

  public abstract String[] getAllSynonyms(int id);

  /**
   * Returns an unordered String[] containing the 
   * synset, hyponyms, similars, alsoSees, 
   * and coordinate terms (checking each in order) 
   * for all senses of <code>word</code> with <code>pos</code>, 
   * or null if not found
   */
  public abstract String[] getSynonyms(String word, String posStr,
      int maxResults);

  /**
   * Returns an unordered String[] containing the 
   * synset, hyponyms, similars, alsoSees, 
   * and coordinate terms (checking each in order) 
   * for all senses of <code>word</code> with <code>pos</code>, 
   * or null if not found
   */
  public abstract String[] getSynonyms(String word, String posStr);

  /**
   * Returns an unordered String[] containing the 
   * synset, hyponyms, similars, alsoSees, 
   * and coordinate terms (checking each in order) 
   * for all senses of <code>word</code> with <code>pos</code>, 
   * or null if not found
   */
  public abstract String[] getAllSynonyms(String word, String posStr,
      int maxResults);

  public abstract String[] getAllSynonyms(String word, String posStr);

  /**
   * Returns String[] of Common Parents for 1st senses of words with specified
   * pos' or null if not found
   */
  public abstract String[] getCommonParents(String word1, String word2,
      String pos);

  /**
   * Returns String[] of words in synset for first sense of <code>word</code>
   * with <code>pos</code>, or null if not found. <P>Note: original word is 
   * excluded by default.
   * @see #getSynset(String, String, boolean)
   */
  public abstract String[] getSynset(String word, String pos);

  /**
   * Returns String[] of words in synset for first sense of <code>word</code>
   * with <code>pos</code>, or null if not found.
   */
  public abstract String[] getSynset(String word, String pos, boolean includeOriginal);

  /**
   * Returns String[] of Synsets for unique id <code>id</code> or null if not found.
   */
  public abstract String[] getSynset(int id);

  /**
   * Returns String[] of words in each synset for all senses of
   * <code>word</code> with <code>pos</code>, or null if not found
   */
  public abstract String[] getAllSynsets(String word, String posStr);

  /**
   * Return the # of senses (polysemy) for a given word/pos.
   * A 'sense' refers to a specific Wordnet meaning and maps 1-1 
   * to the concept of synsets. Each 'sense' of a word exists 
   * in a different synset. <p> For more info, see
   * http://wordnet.princeton.edu/man/wngloss.7WN.html.
   * 
   * @return # of senses or -1 if not found
   */
  public abstract int getSenseCount(String word, String pos);

  // ANTONYMS ------------
  /**
   * Returns String[] of Antonyms for the 1st sense of <code>word</code> with <code>pos</code> or null
   * if not found<br>
   * Holds for adjectives only (?)
   */
  public abstract String[] getAntonyms(String word, String pos);

  /**
   * Returns String[] of Antonyms for the specified id, or null
   * if not found<br>
   * Holds for adjectives only (?)
   */
  public abstract String[] getAntonyms(int id);

  /**
   * Returns String[] of Antonyms for the 1st sense of <code>word</code> with <code>pos</code> or null
   * if not found<br>
   * Holds for adjectives only (?)
   */
  public abstract String[] getAllAntonyms(String word, String pos);

  /*
   * Returns String[] of Antonyms for the 1st sense of 'word' with specified pos
   * private List getAntonymsAtIndex(IndexWord idw, int index) throws
   * JWNLException { if (idw == null) return null;
   * 
   * Synset[] synsets = idw.getSenses(); if (synsets == null || synsets.length <=
   * 0) return null;
   * 
   * List l = new ArrayList(); PointerUtils pu = PointerUtils.getInstance();
   * PointerTargetNodeList nodeList = pu.getAntonyms(synsets[index]);
   * getLemmaSet(nodeList, l);
   * 
   * return l == null || l.size() < 1 ? null : l; }
   */
  // HYPERNYMS -- direct
  /**
   * Returns Hypernym String[] for all senses of <code>word</code> with <code>pos</code> or null if
   * not found
   * <p>
   * X is a hyponym of Y if there exists an is-a relationship between X and Y.<br>
   * That is, if X is a subtype of Y. <br>
   * Or, for xample, if X is a species of the genus Y. <br>
   * X is a hypernym of Y is Y is a hyponym of X. <br>
   * Holds between: nouns and nouns & verbs and verbs<br>
   * Examples:
   * <ul>
   * <li>artifact is a hyponym of object
   * <li>object is a hypernym of artifact
   * <li>carrot is a hyponym of herb
   * <li>herb is a hypernym of carrot
   * </ul>
   */
  public abstract String[] getHypernyms(String word, String posStr);

  /**
   * Returns Hypernym String[] for id, or null if not found
   * <p>
   * X is a hyponym of Y if there exists an is-a relationship between X and Y.<br>
   * That is, if X is a subtype of Y. <br>
   * Or, for example, if X is a species of the genus Y. <br>
   * X is a hypernym of Y is Y is a hyponym of X. <br>
   * Holds between: nouns and nouns & verbs and verbs<br>
   * Examples:
   * <ul>
   * <li>artifact is a hyponym of object
   * <li>object is a hypernym of artifact
   * <li>carrot is a hyponym of herb
   * <li>herb is a hypernym of carrot
   * </ul>
   */
  public abstract String[] getHypernyms(int id);

  /**
   * Returns an ordered String[] of hypernym-synsets (each a semi-colon
   * delimited String) up to the root of Wordnet for the 1st sense of the word,
   * or null if not foundsssssss
   */
  public abstract String[] getAllHypernyms(String word, String posStr);

  /**
   * Returns an ordered String[] of hypernym-synsets (each a semi-colon
   * delimited String) up to the root of Wordnet for the <code>id</code>,
   * or null if not found 
   */
  public abstract String[] getHypernymTree(int id);

  /**
   * Returns Hyponym String[] for 1st sense of <code>word</code> with 
   * <code>pos</code> or null if not found
   * <p>
   * X is a hyponym of Y if there exists an is-a relationship between X and Y.<br>
   * That is, if X is a subtype of Y. <br>
   * Or, for xample, if X is a species of the genus Y. <br>
   * X is a hypernym of Y is Y is a hyponym of X. <br>
   * Holds between: nouns and nouns & verbs and verbs<br>
   * Examples:
   * <ul>
   * <li>artifact is a hyponym of object
   * <li>object is a hypernym of artifact
   * <li>carrot is a hyponym of herb
   * <li>herb is a hypernym of carrot
   * </ul>
   */
  public abstract String[] getHyponyms(String word, String posStr);

  /**
   * Returns Hyponym String[] for id, or null if not
   * found
   * <p>
   * X is a hyponym of Y if there exists an is-a relationship between X and Y.<br>
   * That is, if X is a subtype of Y. <br>
   * Or, for xample, if X is a species of the genus Y. <br>
   * X is a hypernym of Y is Y is a hyponym of X. <br>
   * Holds between: nouns and nouns & verbs and verbs<br>
   * Examples:
   * <ul>
   * <li>artifact is a hyponym of object
   * <li>object is a hypernym of artifact
   * <li>carrot is a hyponym of herb
   * <li>herb is a hypernym of carrot
   * </ul>
   */
  public abstract String[] getHyponyms(int id);

  /**
   * Returns an unordered String[] of hyponym-synsets (each a colon-delimited
   * String), or null if not found
   */
  public abstract String[] getAllHyponyms(String word, String posStr);

  /**
   * Returns an unordered String[] of hyponym-synsets (each a colon-delimited
   * String) representing all paths to leaves in the ontology (the full hyponym tree),
   * or null if not found <p>
   */
  public abstract String[] getHyponymTree(int id);

  public abstract boolean isAdjective(String word);

  public abstract boolean isAdverb(String word);

  public abstract boolean isVerb(String word);

  public abstract boolean isNoun(String word);

  /**
   * Returns an array of all stems, or null if not found
   * 
   * @param query
   * @param pos
   */
  public abstract String[] getStems(String query, CharSequence pos);

  /**
   * Returns true if 'word' exists with 'pos' and is equal (via String.equals())
   * to any of its stem forms, else false;
   */
  public abstract boolean isStem(String word, CharSequence pos);

  /**
   * Checks the existence of a 'word' in the ontology
   * 
   * @param word
   */
  public abstract boolean exists(String word);

  /**
   * Check each word in 'words' and removes those that don't exist in the
   * ontology.
   * <p>
   * Note: destructive operation
   * 
   * @invisible
   * 
   * @param words
   */
  public abstract void removeNonExistent(Collection words);

  /** 
   * Returns an array of all parts-of-speech ordered according to 
   * their polysemy count, returning the pos with the most different 
   * senses in the first position, etc. 
   * @return String[], one element for each part of speech 
   * ("a" = adjective, "n" = noun, "r" = adverb, "v" = verb),
   * or null if not found.
   */
  public abstract String[] getPos(String word);

  /**
   * @return String from ("a" = adjective, "n" = noun,
   *  "r" = adverb, "v" = verb),
   * or null if not found.
   */
  public abstract String getPos(int id);

  /**
   * Returns most-common pos according to polysemy count, returning
   * the pos with the most different senses. 
   * 
   * @return single-char String for the most common part of speech 
   * ("a" = adjective, "n" = noun, "r" = adverb, "v" = verb), 
   * or null if not found.
   */
  public abstract String getBestPos(String word);

  /**
   * Returns a random example from a random word w' <code>pos</code>
   * 
   * @return random example
   */
  public abstract String getRandomExample(CharSequence pos);

  /**
   * Returns <code>numExamples</code> random examples from random words w'
   * <code>pos</code>
   * 
   * @return random examples
   */
  public abstract String[] getRandomExamples(CharSequence pos, int numExamples);

  /**
   * Returns <code>count</code> random words w' <code>pos</code>
   * 
   * @return String[] of random words
   */
  public abstract String[] getRandomWords(CharSequence pos, int count);

  /**
   * Returns a random stem with <code>pos</code> and a max length
   * of <code>this.maxCharsPerWord</code>.
   * 
   * @return random word
   */
  public abstract String getRandomWord(CharSequence pos);

  /**
   * Returns a random word with <code>pos</code> and a maximum of
   * <code>maxChars</code>.
   * 
   * @return a random word or null if none is found
   */
  public abstract String getRandomWord(CharSequence pos, boolean stemsOnly,
      int maxChars);

  /**
   * Prints the full hyponym tree to System.out (primarily for debugging). 
   * @param senseId
   */
  public abstract void printHyponymTree(int senseId);

  /**
   * Prints the full hypernym tree to System.out (primarily for debugging). 
   * @param senseId
   */
  public abstract void printHypernymTree(int senseId);

  /**
   * Returns the min distance between any two senses for the 2 words
   * in the wordnet tree (result normalized to 0-1) with specified pos, 
   * or 1.0 if either is not found
   */
  public abstract float getDistance(String lemma1, String lemma2, String pos);

  /**
   * Returns array of whole-to-part relationships for 1st sense of word/pos, or
   * null if not found<br>
   * X is a meronym of Y if Y has X as a part.<br>
   * X is a holonym of Y if X has Y as a part. That is, if Y is a meronym of X.
   * <br>
   * Holds between: Nouns and nouns<br>
   * Returns part,member, and substance meronyms<br>
   * Example: arm -> [wrist, carpus, wrist-joint, radiocarpal-joint...]
   * 
   * @param query
   * @param pos
   */
  public abstract String[] getMeronyms(String query, String pos);

  /**
   * Returns array of whole-to-part relationships for id, or
   * null if not found<br>
   * X is a meronym of Y if Y has X as a part.<br>
   * X is a holonym of Y if X has Y as a part. That is, if Y is a meronym of X.
   * <br>
   * Holds between: Nouns and nouns<br>
   * Returns part,member, and substance meronyms<br>
   * Example: arm -> [wrist, carpus, wrist-joint, radiocarpal-joint...]
   */
  public abstract String[] getMeronyms(int id);

  /**
   * Returns array of whole-to-part relationships for all senses of word/pos, or
   * null if not found<br>
   * X is a meronym of Y if Y has X as a part.<br>
   * X is a holonym of Y if X has Y as a part. That is, if Y is a meronym of X.
   * <br>
   * Holds between: Nouns and nouns<br>
   * Returns part,member, and substance meronyms<br>
   * Example: arm -> [wrist, carpus, wrist-joint, radiocarpal-joint...]
   * 
   * @param query
   * @param pos
   */
  public abstract String[] getAllMeronyms(String query, String pos);

  /**
   * Returns part-to-whole relationships for 1st sense of word/pos, or none if
   * not found<br>
   * X is a meronym of Y if Y has X as a part.<br>
   * X is a holonym of Y if X has Y as a part. That is, if Y is a meronym of X.
   * <br>
   * Holds between: nouns and nouns<br>
   * Returns part, member, and substance holonyms<br>
   * Example: arm -> [body, physical-structure, man, human...]
   * 
   * @param query
   * @param pos
   */
  public abstract String[] getHolonyms(String query, String pos);

  /**
   * Returns part-to-whole relationships for 1st sense of word/pos, or none if
   * not found<br>
   * X is a meronym of Y if Y has X as a part.<br>
   * X is a holonym of Y if X has Y as a part. That is, if Y is a meronym of X.
   * <br>
   * Holds between: nouns and nouns<br>
   * Returns part, member, and substance holonyms<br>
   * Example: arm -> [body, physical-structure, man, human...]
   * 
   */
  public abstract String[] getHolonyms(int id);

  /**
   * Returns part-to-whole relationships for all sense of word/pos, or none if
   * not found<br>
   * X is a meronym of Y if Y has X as a part.<br>
   * X is a holonym of Y if X has Y as a part. That is, if Y is a meronym of X.
   * <br>
   * Holds between: nouns and nouns<br>
   * Returns part, member, and substance holonyms<br>
   * Example: arm -> [body, physical-structure, man, human...]
   * 
   * @param query
   * @param pos
   */
  public abstract String[] getAllHolonyms(String query, String pos);

  /**
   * Returns coordinate terms for 1st sense of word/pos, or null if not found<br>
   * X is a coordinate term of Y if there exists a term Z which is the hypernym
   * of both X and Y.<br>
   * Examples:
   * <ul>
   * <li>blackbird and robin are coordinate terms (since they are both a kind
   * of thrush)
   * <li>gun and bow are coordinate terms (since they are both weapons)
   * <li>fork and spoon are coordinate terms (since they are both cutlery, or
   * eating utensils)
   * <li>hat and helmet are coordinate terms (since they are both a kind of
   * headgear or headdress)
   * </ul>
   * Example: arm -> [hind-limb, forelimb, flipper, leg, crus, thigh, arm...]<br>
   * Holds btwn nouns/nouns and verbs/verbs
   * 
   * @param query
   * @param pos
   */
  public abstract String[] getCoordinates(String query, String pos);

  /**
   * Returns String[] of Coordinates for the specified id, or null
   * if not found<br>
   */
  public abstract String[] getCoordinates(int id);

  /**
   * Returns coordinate terms for all sense of word/pos, or null if not found<br>
   * X is a coordinate term of Y if there exists a term Z which is the hypernym
   * of both X and Y.<br>
   * Examples:
   * <ul>
   * <li>blackbird and robin are coordinate terms (since they are both a kind
   * of thrush)
   * <li>gun and bow are coordinate terms (since they are both weapons)
   * <li>fork and spoon are coordinate terms (since they are both cutlery, or
   * eating utensils)
   * <li>hat and helmet are coordinate terms (since they are both a kind of
   * headgear or headdress)
   * </ul>
   * Example: arm -> [hind-limb, forelimb, flipper, leg, crus, thigh, arm...]<br>
   * Holds btwn nouns/nouns and verbs/verbs
   * 
   * @param query
   * @param pos
   */
  public abstract String[] getAllCoordinates(String query, String pos);

  /**
   * Returns verb group for 1st sense of verb or null if not found<br>
   * Example: live -> [dwell, inhabit]<br>
   * Holds for verbs
   * 
   * @param query
   * @param pos
   */
  public abstract String[] getVerbGroup(String query, String pos);

  /**
   * Returns verb group for id, or null if not found<br>
   * Example: live -> [dwell, inhabit]<br>
   * Holds for verbs
   */
  public abstract String[] getVerbGroup(int id);

  /**
   * Returns verb group for all senses of verb or null if not found<br>
   * Example: live -> [dwell, inhabit]<br>
   * Holds for verbs
   * 
   * @param query
   * @param pos
   */
  public abstract String[] getAllVerbGroups(String query, String pos);

  /**
   * Returns derived terms for 1st sense of word/pos or null if not found<br>
   * Holds for adverbs <br>
   * Example: happily -> [jubilant, blithe, gay, mirthful, merry, happy]
   * 
   * @param query
   * @param pos
   */
  public abstract String[] getDerivedTerms(String query, String pos);

  /**
   * Returns derived terms for the id, or null if not found<br>
   * Holds for adverbs <br>
   * Example: happily -> [jubilant, blithe, gay, mirthful, merry, happy]
   */
  public abstract String[] getDerivedTerms(int id);

  /**
   * Returns derived terms forall senses of word/pos or null if not found<br>
   * Holds for adverbs <br>
   * Example: happily -> [jubilant, blithe, gay, mirthful, merry, happy]
   * 
   * @param query
   * @param pos
   */
  public abstract String[] getAllDerivedTerms(String query, String pos);

  /**
   * Returns also-see terms for 1st sense of word/pos or null if not found<br>
   * Holds for nouns (?) & adjectives<br>
   * Example: happy -> [cheerful, elated, euphoric, felicitous, joyful,
   * joyous...]
   * 
   * @param query
   * @param pos
   */
  public abstract String[] getAlsoSees(String query, String pos);

  /**
   * Returns also-see terms for seseId or null if not found<br>
   * Holds for nouns (?) & adjectives<br>
   * Example: happy -> [cheerful, elated, euphoric, felicitous, joyful,
   * joyous...]
   * 
   * @param senseId
   */
  public abstract String[] getAlsoSees(int senseId);

  /**
   * Returns also-see terms for all senses ofword/pos or null if not found<br>
   * Holds for nouns (?) & adjectives<br>
   * Example: happy -> [cheerful, elated, euphoric, felicitous, joyful,
   * joyous...]
   * 
   * @param query
   * @param pos
   */
  public abstract String[] getAllAlsoSees(String query, String pos);

  /**
   * Returns nominalized terms for 1st sense of word/pos or null if not found<br>
   * Refers to the use of a verb or an adjective as a noun. Holds for nouns,
   * verbs & adjecstives(?)<br>
   * Example: happiness(n) -> [happy, unhappy]<br>
   * happy(a) -> [happiness, felicity]<br>
   * 
   * @param query
   * @param pos
   */
  public abstract String[] getNominalizations(String query, String pos);

  /**
   * Returns nominalized terms for id,  or null if not found<br>
   * Refers to the use of a verb or an adjective as a noun. Holds for nouns,
   * verbs & adjecstives(?)<br>
   * Example: happiness(n) -> [happy, unhappy]<br>
   * happy(a) -> [happiness, felicity]<br>
   */
  public abstract String[] getNominalizations(int id);

  /**
   * Returns nominalized terms for all sense of word/pos or null if not found<br>
   * Refers to the use of a verb or an adjective as a noun. Holds for nouns,
   * verbs & adjecstives(?)<br>
   * Example: happiness(n) -> [happy, unhappy]<br>
   * happy(a) -> [happiness, felicity]<br>
   * 
   * @param query
   * @param pos
   */
  public abstract String[] getAllNominalizations(String query, String pos);

  /**
   * Returns similar-to list for first sense of word/pos or null if not found<br>
   * Holds for adjectives<br>
   * Example:<br>
   * happy(a) -> [blessed, blissful, bright, golden, halcyon, prosperous...]<br>
   * 
   * @param query
   * @param pos
   */
  public abstract String[] getSimilar(String query, String pos);

  /**
   * Returns similar-to list for id, or null if not found<br>
   * Holds for adjectives<br>
   * Example:<br>
   * happy(a) -> [blessed, blissful, bright, golden, halcyon, prosperous...]<br>
   */
  public abstract String[] getSimilar(int id);

  /**
   * Returns similar-to list for all sense of word/pos or null if not found<br>
   * Holds for adjectives<br>
   * Example:<br>
   * happy(a) -> [blessed, blissful, bright, golden, halcyon, prosperous...]<br>
   * 
   * @param query
   * @param pos
   */
  public abstract String[] getAllSimilar(String query, String pos);

  public abstract boolean isIgnoringCompoundWords();

  public abstract void ignoreCompoundWords(boolean ignoreCompoundWords);

  public abstract boolean isIgnoringUpperCaseWords();

  public abstract void ignoreUpperCaseWords(boolean ignoreUpperCaseWords);

}