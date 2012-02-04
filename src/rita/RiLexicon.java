package rita;

import java.util.*;

import org.omg.CORBA.portable.Delegate;

import processing.core.PApplet;
import rita.support.*;

/**
 * RiLexicon represents the core 'dictionary' (or lexicon) for the RiTa tools. It contains ~35,000 
 * words augmented with phonemic and syllabic data, as well as a list of valid parts-of-speech 
 * for each. The lexicon can be extended and/or customized for additional
 * words, usages, or pronunciations.<P>
 *  
 * Additionally the lexicon is equipped with implementations of a variety of matching 
 * algorithms (min-edit-distance, soundex, anagrams, alliteration, rhymes, looks-like, etc.) 
 * based on combinations of letters, syllables and phonemes. An example use:
 * <pre>    RiLexicon lex = new RiLexicon(this);
    String[] similars = lex.similarBySound("cat");
    String[] rhymes  = lex.getSimpleRhymes("cat");
    // etc.</pre>
 * <P>
 * Note: If you wish to modify or customize the lexicon (e.g., add words, or change pronunciations) 
 * you can do so by editing the 'rita_addenda.txt' file, found in $SKETCH_DIR/libraries/rita folder
 * and placing the modifed version in the 'data' folder of your sketch. 
 * 
 * @author dhowe
 */

/*
 * WORDS TO ADD:
 *    misdirect
 *    unempty
 *    orature
 *    surfaceless
 *    syntagm
 *    diegesis
 *    poeticize
 *    formlessness
 *    leftmost
 *    teleportation
 *    downwards
 *    upwards ?
 *    aurature
 *    vertiginous
 *    turd/turds
 *    suppository
 *    gunwales
 *    voyaging
 *    flapped
 *    stupefied
 *    feinted
 *    disgorged
 *    half
 *    vulva
 *    haloed
 *    christ
 *    goodnatured
 *    testicles
 *    both
 *    watermelons
 *    splattering
 *    godliness
 *    swatted
 *    bobbed
 *    shooing
 *    chlorinated
 *    outhouses
 *    shat
 *    fifty
 *    foxhole
 *    scarecrow/s
 *    farting
 *    crumbs
 *    eucharist
 *    oilcloth
 *    explicating
 *    diapered
 *    to-fuck
 *    pee
 *    poplars
 *    enemas
 *    woodpile
 *    soaped
 *    cookstove
 *    epigram
 *    reptiles
 *    subsisted
 *    gonads
 *    muskrats
 *    squirrels 
 *    checkered
 *    unwrapped
 *    jews
 *    
 *    Columbus
 *    Jesus
 *    Coover
 *    Beckett
 *    Mallarme
 *    
 *    "as," is not stripped
 */

/*
 * TODO:
 *    -- LexicalScan for anagrams (anemic <--> cinema)
 *    -- LexicalScan for exact character (different-order) match     
 *    -- LexicalScan to match stresses (different-order) match  
 *    -- LexicalScan to match constraints: stress and alliteration
 *    
 *    TEST 
 *    -- test iterator(regex) - random iterator 
 *    -- add pos-iterators: fill-in randomPosIterator(String), posIterator(String) 
 *    -- backoff constraints if # is too small for all similarBy
 *        
 *    ---------
 *    
 *    -- make it possible for user to supply their own lexicon file? prob. too hard for user
 
 *    -- LexicalScan for exact character (different-order) match     
 *    -- LexicalScan to match stresses (different-order) match  
 *    -- LexicalScan to match constraints: stress and alliteration
 *     
 *    -- Add new partialMatch (super-string only)
 *    
 *    -- Comparator for sorting by length sortByLength(Collection c, int input);
 *    
 *    -- Add regular expression support?
 */
public class RiLexicon extends RiObject
{       
  static final int DEFAULT_MATCH_MIN_LENGTH = 4;

  static final int CHARACTERS_MATCH = 1;
  
  //private static final int MIN_DESIRED_RESULT_SIZE = 10; // tmp    // add setters and getters for these?
  //private static final int MAX_DESIRED_RESULT_SIZE = 20; // tmp
  
  /**  @invisible    */
  public static final char STRESSED   = '1';
  
  /**  @invisible    */
  public static final char UNSTRESSED = '0';
  
  /**  @invisible    */
  public static boolean VERBOSE_WARNINGS=false;
  
  private MinEditDist minEditDist;
  
  private RiLexiconImpl lexicon;
  
  /**  @invisible    */
  public RiLexicon()
  {
    this(null);
  }
  
  public RiLexicon(PApplet pApplet)
  {
  	super(pApplet); 
    this.lexicon = RiLexiconImpl.getInstance(pApplet);      
  }
  
  public RiLexicon(PApplet pApplet, String lexiconFile)
  {
    super(pApplet); 
    this.lexicon = RiLexiconImpl.getInstance(pApplet, lexiconFile);      
  }
  
  // public methods ----------------------------------------------------
  
  /**
   * Returns true if the word is a 'stop' (or 'closed-class') word
   * else false. See http://en.wikipedia.org/wiki/Stop_words
   */
  public boolean isStopWord(String word) {
    return RiTa.isClosedClass(word);
  }
  
  /** 
   * Returns a random word from the lexicon with the specified syllable-count 
   * or null if no such word exists.
   */
  public String getRandomWordWithSyllableCount(int syllableCount)
  {
    return getRandomWordWithSyllableCount(null, syllableCount);
  }
  
  /** 
   * Returns a random word from the lexicon with the specified part-of-speech and syllable-count, or null if no such word exists.
   * @see RiPosTagger
   */
  public String getRandomWordWithSyllableCount(String pos, int syllableCount)
  {
    Map lookup = lexicon.getLexicalData();
    Iterator it = (pos == null) ? lexicon.randomIterator() : lexicon.randomPosIterator(pos);
    
    while (it.hasNext()) {
      String s = (String)it.next();
      String data = (String) lookup.get(s);
      String sylStr = data.split("\\|")[0].trim();
      if (sylStr.split(" ").length == syllableCount) 
        return s;
    }
    return null;
  }
  
  /** 
   * Returns a random word from the lexicon with the specified part-of-speech and target-length, 
   * or null if no such word exists.
   * @see RiPosTagger
   */
  public String getRandomWord(String pos, int targetLength) {
    Iterator it = (pos == null) ? lexicon.randomIterator() : lexicon.randomPosIterator(pos);
    if (targetLength == 0) 
      return (String) it.next();
    while (it.hasNext()) {
      String s = (String)it.next();
      if (s.length() == targetLength) 
        return s;
    }
    return null;
  }
    
  /** Returns a random word from the lexicon */
  public String getRandomWord() {  return getRandomWord(0);  }
  
  /**
   * Returns a random word from the lexicon 
   * with the specified part-of-speech
   * @see RiPosTagger
   */
  public String getRandomWord(String pos) {  return getRandomWord(pos, 0);  }
  
  
  /**
   * Returns a random word from the lexicon with the specified target-length (where length>0),
   * or null if no such word exists.
   */
  public String getRandomWord(int targetLength) {
    return getRandomWord(null, targetLength);
  }
  
  /**
   * Returns true if the first stressed consonant of the two words match, else false. <P>
   * Note: returns true if wordA.equals(wordB) and false if either (or both) are null;
   */
  public boolean isAlliteration(String wordA, String wordB) {
    if (wordA != null && wordB != null)  {
      if (wordB.equals(wordA)) return true;
      String fcA = firstConsonant(firstStressedSyllable(wordA));      
      String fcB = firstConsonant(firstStressedSyllable(wordB));  
      // System.out.println(fcA+" ?= "+fcB);
      if (fcA != null && fcB != null && fcA.equals(fcB)) 
        return true;
    }
    return false;
  }
  
  
  /**
   * Returns true if the two words rhyme (that is, if their final stressed phoneme
   * and all following phonemes are identical) else false. Note: returns false 
   * if wordA.equals(wordB) or if either (or both) are null; 
   * <p>
   * Note: at present doesn't use letter-to-sound engine if either word 
   * is not found in the lexicon, but instead just returns false. TODO  
   */
  public boolean isRhyme(String wordA, String wordB) 
  {
//System.out.println("RiLexicon.isRhyme('"+wordA+"' ?= '"+wordB+"')");
    if (wordA != null && wordB != null) {
      if (wordB.equalsIgnoreCase(wordA)) return false;
      String lspA = lastStressedPhoneToEnd(wordA);      
      String lspB = lastStressedPhoneToEnd(wordB);  
      if (lspA != null && lspB != null && lspA.equals(lspB)) 
        return true;
    }
    return false;
  } 
  
  /** @invisible
  public boolean isRhyme2(String wordA, String wordB) {
    String[] sylA = getSyllables(wordA); 
    String[] sylB = getSyllables(wordB);   
    
    String lastA = lastSyllable(wordA); 
    String lastB = lastSyllable(wordB);   
    if (minEditDist == null)
      minEditDist = new MinEditDist();    
    int med = minEditDist.computeRaw(lastA, lastB);  
    System.out.println("SYLS: "+lastA+" / "+lastB+" med="+med);
    if (med < 2)
      return true;    
    return false;
  } */

  private static String firstConsonant(String rawPhones) {
    if (rawPhones != null) {
      String[] phones = rawPhones.split(PHONEME_BOUNDARY);
      if (phones != null) {
        for (int j = 0; j < phones.length; j++) {
          if (RiPhone.isConsonant(phones[j]))
            return phones[j];
        }
      }
    }
    return null;
  }

  /** @invisible */
  private boolean isSoundex(String a, String b, int maxDist) {  
    throw new RuntimeException("IMPLEMENT ME"); 
  }
  
  /**
   * Returns true if <code>orig</code> is a substring of
   * <code>toCheck</code>.
   */
  public boolean isSubstring(String orig, String toCheck) {
    return orig.indexOf(toCheck) >= 0;
  }
    
  /**
   * Returns true if <code>orig</code> is a superstring of
   * <code>toCheck</code>.
   */
  public boolean isSuperstring(String orig, String toCheck) {
    return toCheck.indexOf(orig) >= 0;
  }
  
  /**
   * Returns true if <code>orig</code> is a sub or super-string of
   * <code>toCheck</code>.
   */
  public boolean isContaining(String orig, String toCheck) {
    return isSubstring(orig, toCheck) || isSuperstring(orig, toCheck);
  }
  
  /**
   * Returns an iterator over the words in lexicon beginning
   * at a random offset. 
   */
  public Iterator randomIterator() { return lexicon.randomIterator();  }
  
  /** Utility method that returns a random-iterator over the specified set.  */
  public static Iterator randomIterator(Set s) { return new RiRandomIterator(s); }

  /**
   * Returns an iterator over the words in lexicon, for the supplied part-of-speech
   * beginning at a random offset. 
   * @see rita.support.RiPos
   * <p>
   * Note: this method will create a new iterator each time it is called 
   */
  public Iterator randomPosIterator(String pos) { 
    return lexicon.randomPosIterator(pos);
  }
  
  /**
   * Returns an iterator over the words in lexicon, for the supplied part-of-speech
   * @see rita.support.RiPos
   */
  public Iterator posIterator(String pos) { 
     return lexicon.posIterator(pos);
  }

  
  /**
   * Returns an iterator over the words in the lexicon matching the 
   * supplied regular expression beginning from a random offset. 
   * <p>
   * Note: this method will create a new iterator each time it is called
   */
  public Iterator randomIterator(String regex) 
  { 
    return new RiRandomIterator(getWords(regex));  
  }
  
  /**
   * Returns an iterator over the words in lexicon
   */
  public Iterator iterator() { return lexicon.iterator(); }
  
  /**
   * Returns an iterator over the words in lexicon
   * matching the supplied regular expression.
   */
  public Iterator iterator(String regex) { return getWords(regex).iterator(); }

  /** 
   * Returns the set of words in the lexicon (including those from user-addenda) 
   * that match the supplied regular expression. For example, getWords("ee"); 
   * returns 661 words with 2 or more consecutive e's, while getWords("ee.*ee");
   * returns exactyl 2: 'freewheeling' and 'squeegee'.
   */
  public Set getWords(String regex)
  {
    return lexicon.getWords(regex);
  }
  
  /** 
   * Returns the full set of words in the lexicon (including those from user-addenda) 
   */
  public Set getWords()
  {
    return lexicon.getWords();
  }

  /**
   * Returns the rhymes for a given word or null if none found<p>
   * Two words rhyme if their final stressed vowel and all
   * following phonemes are identical.
   */
  public String[] getRhymes(String input)
  {
    Set result = new HashSet();   
    getRhymes(input, result);
    if (result.size()==0) return null;
    return SetOp.toStringArray(result); 
  }

    
  /*
   * In the specific sense, two words rhyme if their final stressed vowel and all following sounds are identical;
   * 
   *   masculine: a rhyme in which the stress is on the final syllable of the words. (rhyme, sublime, crime)
   *   feminine: a rhyme in which the stress is on the penultimate (second from last) syllable of the words. (picky, tricky, sticky, icky)
   *   dactylic: a rhyme in which the stress is on the antepenultimate (third from last) syllable ('cacophonies", "Aristophanes")
   *
   * In the general sense, "rhyme" can refer to various kinds of phonetic similarity between words, and to the use of such similar-sounding words in organizing verse. Rhymes in this general sense are classified according to the degree and manner of the phonetic similarity:
   *
   *   syllabic: a rhyme in which the last syllable of each word sounds the same but does not necessarily contain vowels. (cleaver, silver, or pitter, patter)
   *   imperfect: a rhyme between a stressed and an unstressed syllable. (wing, caring)
   *   semirhyme: a rhyme with an extra syllable on one word. (bend, ending)
   *   oblique (or slant): a rhyme with an imperfect match in sound. (green, fiend; one, thumb)
   *   assonance: matching vowels. (shake, hate) Assonance is sometimes used to refer to slant rhymes.
   *   consonance: matching consonants. (rabies, robbers)
   *   half rhyme (or sprung rhyme): matching final consonants. (bent, ant)
   */
  
  /**
   * Returns the rhymes for a given word or null if none found.<P>
   * Two words rhyme if their final stressed vowel and all
   * following phonemes are identical.
   * @invisible
   */
  public void getRhymes(String input, Set result) 
  {
    String lss = lastStressedPhoneToEnd(input);
    for (Iterator it = iterator(); it.hasNext();) 
    {      
      String cand = (String)it.next();
      
      if (cand.equals(input)) continue;
      
      String chck = lexicon.getRawPhones(cand);
       if (chck != null && chck.endsWith(lss))
         result.add(cand);         
    }
  }

  /*private void getSimpleAlliterations(String input, Set result, boolean ignoredFlag)
  {
    String[] tPhones = getPhones(input);
    if (tPhones == null) return;      
    String tPhone1 = tPhones[0];
    this.similarBySound(input, result);
    for (Iterator i = result.iterator(); i.hasNext();)
    {
      String candidate = (String) i.next();
      String[] phones = getPhones(candidate);
      if (phones == null || phones.length==0)
        continue;
      String cPhone1 = phones[0];
      if  (!cPhone1.equals(tPhone1))
        i.remove();
    }    
    
    //System.out.println("INITIAL_SIZE: "+result.size());
    if (result.size() < MIN_DESIRED_RESULT_SIZE) {
      Set tmp = new HashSet(); 
      System.out.println("Backing off constraints...");
      getAlliterations(input, tmp);    
      if (tmp != null) result.addAll(tmp);
    }
    //System.out.println("FINAL_SIZE: "+result.size());
  }*/
    
  /**
   * Finds alliterations by comparing the phonemes of the input string 
   * to those of each word in the lexicon
   */
  public String[] getAlliterations(String input)
  {
		Set result = new HashSet();   
		getAlliterations(input, result);
    if (result.size()==0) return null;
		return SetOp.toStringArray(result); 
  }
  
  /**
   * Finds alliterations by comparing the phonemes of the input string 
   * to those of each word in the lexicon
   * @invisible 
   */ 
  public void getAlliterations(String input, Set result)
  {
    this.getAlliterations(input, result, DEFAULT_MATCH_MIN_LENGTH);
  }
  
  /**
   * Finds alliterations by comparing the phonemes of the input string 
   * to those of each word in the lexicon
   * @invisible
   */ 
  public void getAlliterations(String input, Set result, int minLength)
  {
    for (Iterator iterator = iterator(); iterator.hasNext();) {
      String cand = (String) iterator.next();
      if (isAlliteration(input, cand))
        addResult(input, result, cand, minLength);           
    }     
  }
  
  /**
   * First calls similarBySound(), then filters 
   * the result set by the algorithm used in similarByLetter();
   * (useful when similarBySound() returns too large a result set)  
   * 
   * @see #similarByLetter(String) 
   * @see #similarBySound(String)
   */
  public String[] similarBySoundAndLetter(String input)
  {
		Set result = new HashSet();   
		similarBySoundAndLetter(input, result);
    if (result.size()==0) return null;
		return SetOp.toStringArray(result); 
  }
  
  /** @invisible */
  private int similarBySoundAndLetter(String input, Set result) 
  {
    Set tmp = new TreeSet();
    int min = this.similarBySound(input, tmp);    
    this.similarByLetter(tmp, input, result);
    
    result.remove(input); // not the same word   
    
    return min;
  }
  
  /**
   * Compares the phonemes of the input String to those of each word in the 
   * lexicon,  returning the set of closest matches as a String[].
   */
  public String[] similarBySound(String input)
  {
		Set result = new HashSet();   
		similarBySound(input, result);
    if (result.size()==0) return null;
		return SetOp.toStringArray(result); 
  }
  
  /** @invisible */
  public int similarBySound(String input, Set result)
  { 
    if (result == null) 
      throw new IllegalArgumentException("Null Arg: result[Collection](3)");

    int idx = 0;
    int minVal = Integer.MAX_VALUE;
    String[] targetPhones = lexicon.getPhonemeArr(input, true);
    if (targetPhones == null) return -1;
    
    if (minEditDist == null)
      minEditDist = new MinEditDist();        
    
//System.out.println("TARGET: "+RiTa.asList(targetPhones));
    
    for (Iterator i = lexicon.iterator(); i.hasNext(); idx++)
    {
      String candidate = (String)i.next();
      String[] phones = lexicon.getPhonemeArr(candidate, false);
      //System.out.println("TEST: "+RiTa.asList(targetPhones));
      
      int med = minEditDist.computeRaw(phones, targetPhones);  
      /*if (phones.length == 3 && med ==2)
        System.out.println(RiTa.asList(phones)+" -> "+med);*/
      if (med == 0) continue; // same phones     
      
      // we found something even closer
      if (med < minVal) {
        if (checkResult(input, result, candidate, 3)) {
          minVal = med;
          result.clear();
          result.add(candidate);
        }
      }  
      // we have another best to add
      else if (med == minVal) {
        addResult(input, result,  candidate, 3);
      }
    }

    return minVal;
  }

  // -------------
  
  /**
   * Compares the characters of the input string (using a version of the min-edit distance algorithm)
   * to each word in the lexicon, returning the set of closest matches.
   */
  public String[] similarByLetter(String input)
  {   
		Set result = new HashSet();   
		similarByLetter(input, result);
    if (result.size()==0) return null;
		return SetOp.toStringArray(result); 
  }
  
  /** 
   * Compares the characters of the input string (using a version of the min-edit distance algorithm)
   * to each word in the lexicon, adding the set of closest matches to <code>result</code>.
   * @invisible 
   */
  public int similarByLetter(String input, Set result)
  { 
    return this.similarByLetter(lexicon.getWords(), input, result);
  }  
  
  /** 
   * Compares the characters of the input string (using a version of the min-edit distance algorithm)
   * to each word in the lexicon, adding the set of closest matches to <code>result</code>.
   * If 'preserveLength' is true, the method will favor words of the same length as the input. 
   * @invisible 
   */
  public int similarByLetter(String input, Set result, boolean preserveLength)
  { 
    return this.similarByLetter(lexicon.getWords(), input, result, 1, preserveLength);
  }
  
  /** 
   * 
   * Compares the characters of the input string (using a version of the min-edit distance algorithm)
   * to each word in the lexicon, adding the set of closest matches to <code>result</code>,
   * considering all matches where the edit distance >= 'minMed'.<p>
   * @invisible 
   */
  public int similarByLetter(String input, Set result, int minMed)
  { 
    return this.similarByLetter(lexicon.getWords(), input, result, minMed);
  }
  
  /** 
   * 
   * Compares the characters of the input string (using a version of the min-edit distance algorithm)
   * to each word in the lexicon, adding the set of closest matches to <code>result</code>, 
   * considering all matches where the edit distance >= 'minMed'.<p>
   * If 'preserveLength' is true, the method will favor words of the same length as the input. 
   */
  public int similarByLetter(String input, Set result, int minMed, boolean preserveLength)
  { 
    return this.similarByLetter(lexicon.getWords(), input, result, minMed, preserveLength);
  }
    
    
  /**  
   * Compares the characters of the input string (using a version of the min-edit distance algorithm)
   * to each word in the lexicon, adding the set of closest matches to <code>result</code>. 
   * @invisible 
   */
  int similarByLetter(Collection candidates, String input, Collection result)
  { 
    return similarByLetter(candidates, input, result, 1);
  }
    
  /**  
   * Compares the characters of the input string (using a version of the min-edit distance algorithm)
   * to each word in the lexicon, adding the set of closest matches to <code>result</code>
   * @invisible 
   */
  int similarByLetter(Collection candidates, String input, Collection result, int minMed)
  { 
    return similarByLetter(candidates, input, result, minMed, false);
  }
  
  /**  
   * Compares the characters of the input string (using a version of the min-edit distance algorithm)
   * to each word in the lexicon, adding the set of closest matches to <code>result</code>.
   * If 'preserveLength' is true, the method will favor words of the same length as the input.
   * @invisible 
   */
  int similarByLetter(Collection candidates, String input,
      Collection result, int minMed, boolean preserveLength)
  { 
    if (result == null) 
      throw new IllegalArgumentException("Null Arg: result[Collection](3)");
    
    int minVal = Integer.MAX_VALUE;

    if (minEditDist == null) minEditDist = new MinEditDist();

    for (Iterator i = candidates.iterator(); i.hasNext();)
    {
      String candidate = (String)i.next();  

      if (preserveLength && candidate.length() != input.length())
        continue;
                
      if (candidate.equalsIgnoreCase(input))
        continue;
      
      // System.out.println("testing: "+candidate);
      
      int med = minEditDist.computeRaw(candidate, input);     

      if (med == 0) continue; // same word

      // we found something even closer
      if (med >= minMed && med < minVal) {
        if (checkResult(input, result, candidate, 2)) {
          //System.out.println("Found "+candidate+", med="+med+" -> "+result);
          minVal = med;
          result.clear();
          result.add(candidate);
        }
      }  
      // we have another best to add
      else if (med == minVal) {        
        addResult(input, result, candidate, 2);
        //System.out.println("  Adding "+candidate+", med="+med);
      }
    }        

    //System.out.println("Min: "+minVal+" -> "+result);
    return minVal;
  }
  
  /**
   * Returns list of words with the lowest joint (summed) edit distance 
   * for the two words. The list is ordered by min-edit-distance to the 
   * first term. The algorithm relaxes its constraints until at least 
   * 'minResultCount' words have been found. 
   * @invisible
   
  public Set newJointSimilarByLetter(String current, String target, int maxJumpSize)
  {   
    // get all the words within maxJumpSize(starts at 1) steps to current, 
    // then order by distance to target.
    
    // WORKING HERE!!!!
    
    return null;
  }*/
  
  /**
   * Returns the set of words with the lowest joint (summed) edit distance
   * for the two words. 
   * @invisible

  private int jointSimilarByLetter(String input1, String input2, Collection result)
  { 
    return jointSimilarByLetter(input1, input2, result, 1, 0);
  }   */
  
  /**
   * Returns list of words with the lowest joint (summed) edit distance 
   * for the two words. The list is ordered by min-edit-distance to the 
   * first term. The algorithm relaxes its constraints until at least 
   * 'minResultCount' words have been found. 
   * @invisible

  public Set jointSimilarByLetter(String input1, String input2, int minResultCount, int maxStepSz)
  {     
    Set result = new TreeSet(new StringCompare(input1));
    jointSimilarByLetter(input1, input2, result, 1, minResultCount, maxStepSz);
    return result;
  }   */
  
  class StringCompare implements Comparator {
    private String target;
    public StringCompare(String word) {
      this.target = word;
    }
    public int compare(Object o1, Object o2) {
      int med1 = minEditDist.computeRaw((String)o1, target);
      int med2 = minEditDist.computeRaw((String)o2, target);      
      return med1 < med2 ? -1 : 1;
    }    
  }
  
  private int jointSimilarByLetter
    (String input1, String input2, Collection result, int minMed, int minResultCount, int maxStepSz)
  { 
//System.out.println("RiLexicon.jointSimilarByLetter(med="+minMed+" minSize="+minResultCount+")");
    if (result == null)  throw new IllegalArgumentException("Null Arg: result[Collection]");
    
    int med = jointSimilarByLetter(input1, input2, result, minMed, maxStepSz);        
//System.out.println("  med="+med+" -> "+result);
    while (result.size() < minResultCount) {
       Set tmp = new HashSet(); // relax & retry
       jointSimilarByLetter(input1, input2, tmp, ++med, maxStepSz);
//System.out.println("  med="+med+" -> "+tmp);
       for (Iterator it = tmp.iterator(); it.hasNext();) {
         Object o = it.next();
         //if (result instanceof Set || !result.contains(o))
         result.add(o);
       }
//System.out.println("  result.size="+result.size());
    }
//System.out.println("  returning: "+result);
    return med;
  }
  
  public Set singleLetterSubtitutions(String input)
  {
    //List result = new ArrayList();
    Set result = new HashSet();
    for (int i = 0; i < input.length(); i++)
    {
      String pre = input.substring(0, i);
      char c = input.charAt(i);
      String post = input.substring(i + 1);

      for (int j = 0; j < 26; j++)
      {        
        char sub = (char)(j+97);
        if (sub == c) continue;
        String test = pre+sub+post;
        System.out.println("trying: "+test);
        //if (contains(test) && !result.contains(test))
        if (this.contains(test)) 
          result.add(test); 
      }      
    }    
    return result;
  }
  
  public Set singleLetterInsertions(String input)
  {
    //List result = new ArrayList();
    Set result = new HashSet();
    for (int i = 0; i < input.length(); i++)
    {
      String pre = input.substring(0, i);
      String post = input.substring(i);
      for (int j = 0; j < 26; j++)
      {        
        char sub = (char)(j+97);
        String test = pre+sub+post;
        if (this.contains(test)) 
          result.add(test); 
      }      
    }    
    return result;
  }
    
  public Set singleLetterDeletes(String input)
  {
    Set result = new HashSet();
    for (int i = 0; i < input.length(); i++)
    {
      String pre = input.substring(0, i);
      String post = input.substring(i + 1);
      String test = pre+post;  
      if (this.contains(test)) 
        result.add(test); 
    }    
    return result;
  }

  private int jointSimilarByLetter(String input1, String input2, Collection result, int minMed, int maxStepSz)
  {
    int minVal = Integer.MAX_VALUE;

    if (minEditDist == null) minEditDist = new MinEditDist();

    for (Iterator i = iterator(); i.hasNext();)
    {
      String candidate = (String)i.next();  
      
      // make sure words are at least 2 chars
      if (candidate.length() < 2) continue;
      
      // make sure we only vary by 1 unit of length
      if (Math.abs(candidate.length()-input1.length())>1)
        continue;
      
      int med1 = minEditDist.computeRaw(candidate, input1); 
      
      // make sure we don't exceed maxStepSz
      if (med1 > maxStepSz) continue;
      
      int med2 = minEditDist.computeRaw(candidate, input2);     
      
      if (med1 == 0 || med2 == 0) continue; // same word

      int med = med1 + med2;  // joint (sum) med
      
      // we found something even closer
      if (med < minVal && med >= minMed) 
      {
        // do we need all this?
/*        if (candidate.equals(input1+"s")  
          || candidate.equals(input2+"s")  
          || candidate.equals(input1+"es")
          || candidate.equals(input2+"es"))
        {
          continue;
        }*/
/*        if (input1.equals(candidate+"s")  
          || input2.equals(candidate+"s")  
          || input1.equals(candidate+"es")
          || input2.equals(candidate+"es"))
        {
          continue;
        }*/
          //System.out.println("Found "+candidate+", med="+med+" -> "+result);
//System.out.println(candidate+" med="+med+" (med1="+med1+" med2="+med2+")");
        minVal = med;
        result.clear();
        result.add(candidate);
 
      }  
      // we have another best to add
      else if (med == minVal) {        
        result.add(candidate);
        //System.out.println("  Adding "+candidate+", med="+med);
      }
    }
    return minVal;
  }
  
  /**
   * Returns valid words (in lexicon) using both substring and superstring matching.<p>
   * This method, CONTAINS(K), is equivalent to UNION( SUB(K), SUPER(K) ).
   */
  public String[] containingStringsByLetter(String input) 
  {
  	Set result = new HashSet();
    this.containingStringsByLetter(input, result, DEFAULT_MATCH_MIN_LENGTH);
    if (result.size()==0) return null;
    return SetOp.toStringArray(result);
  }
  
  /**
   * Returns valid words (in lexicon) using both substring and superstring matching.
   * This method CONTAINS(K) = UNION(SUB(K), SUPER(K)).
   */
  private void containingStringsByLetter(String input, Set result, int minLength) 
  {
    if (minLength < 0) 
      minLength = DEFAULT_MATCH_MIN_LENGTH;

    //List partials = getPartialCandidates(input, minLength);
    for (Iterator j = lexicon.iterator(); j.hasNext();)
    {
      String candidate = (String)j.next();
      
      if (candidate.length() < minLength) continue;
      
      // accepts either sub or super strings of input
      if (RiTa.contains(candidate, input) || RiTa.contains(input, candidate)) {
        //System.out.println("MATCH: contains1: "+candidate);
        addResult(input, result, candidate, 0);
      }
    }
    result.remove(input);       
  }
  // --------
  
  /**
   * Returns all valid substrings of the input word in the lexicon 
   */
  public String[] substringsByLetter(String input) 
  {
    return this.substringsByLetter(input, DEFAULT_MATCH_MIN_LENGTH); 
  }
  
  /**
   * Returns all valid substrings of the input word in the lexicon
   * of length at least <code>minLength</code> 
   */
  public String[] substringsByLetter(String input, int minLength) 
  {
    Set result = new HashSet();
    this.substringsByLetter(input, result, minLength);
    if (result.size()==0) return null;
    return SetOp.toStringArray(result); 
  }
 	
  /** @invisible */
  public void substringsByLetter(String input, Set result) 
  {
    this.substringsByLetter(input, result, DEFAULT_MATCH_MIN_LENGTH);
  }
  
  /** @invisible */
  private void substringsByLetter(String input, Set result, int minLength) 
  {         
    if (minLength < 0) 
      minLength = DEFAULT_MATCH_MIN_LENGTH;

    //List partials = getPartialCandidates(input, minLength);
    for (Iterator j = lexicon.iterator(); j.hasNext();)
    {
      String candidate = (String)j.next();
      
      if (candidate.length() < minLength) continue;
      
      if (RiTa.contains(input, candidate)) 
        addResult(input, result, candidate, 0);
    }
  }
  
  // --------
	
  /**
   * Returns all valid superstrings of the input word in the lexicon 
   */
  public String[] superstringsByLetter(String input) 
  {
  	Set result = new HashSet();   
    this.superstringsByLetter(input, result);
    if (result.size()==0) return null;
    return SetOp.toStringArray(result); 
  }
	
  /** @invisible */
  public void superstringsByLetter(String input, Set result) 
  {
    this.superstringsByLetter(input, result, DEFAULT_MATCH_MIN_LENGTH);
  }
  
  /** @invisible 
  private void superstringsByLetter(String input, Set result, boolean ignoredFlag) 
  {
    this.superstringsByLetter(input, result, DEFAULT_MATCH_MIN_LENGTH, ignoredFlag);
  }*/
  
  /** @invisible */
  private void superstringsByLetter(String input, Set result, int minLength) 
  {   
    if (minLength < 0) 
      minLength = DEFAULT_MATCH_MIN_LENGTH;
    
    for (Iterator j = lexicon.iterator(); j.hasNext();)
    {
      String candidate = (String)j.next();
      if (candidate.length() < minLength) continue;
      if (RiTa.contains(candidate, input))  
        addResult(input, result, candidate, 0);
    }
  }
  
  
/*  
  public String getFirstStressedPhone(String word)
  {            
    int idx = 0;
    boolean foundStress = false;
    String[] stresses = getStresses(word);
    for (; idx < stresses.length; idx++) {
      System.out.println("testing: "+stresses[idx]);
      if (stresses[idx].equals(Featured.STRESSES)) {
        foundStress = true;
        break;
      }
    }
    if (!foundStress) {
      System.out.println("[WARN] No Stressed phonemes in word: "+word);
      return null;
    }
    else
      System.out.println("stress: "+stresses[idx]+" idx="+idx);

    return getStressedPhone(getSyllables(word), idx);
  }

  
  public String getLastStressedPhone(String s)
  {      
    //String[] syllables = 
    String[] stresses = getStresses(s);
    int idx = stresses.length-1;
    boolean foundStress = false;
    for (; idx >= 0; idx--) {
      //System.out.println("testing: "+stresses[idx]);
      if (stresses[idx].equals(Featured.STRESSES)) {
        foundStress = true;
        break;
      }
    }
    if (!foundStress)
      throw new RiTaException("No Stressed phonemes in word: "+s);

    return getStressedPhone(getSyllables(s), idx);
  }

  private String getStressedPhone(String word, int idx)
  {
    String[] syllables = getPhones(word, true, false);
    System.out.println(Arrays.asList(syllables));
    //String stressedSyl = syllables[idx];
    String stressedSyl = getStressedPhone(syllables, 1);
    System.out.println(stressedSyl);
    return null;
  }*/

  
  // end publics ---------------------------------------------------------
  
  /*public void getAllAlliterations(String input, Set result)
  {
    String targetPhone = getPhones(input)[0];
    //System.out.println(input+": "+targetPhone);

    int count=0;
    Map phonemeMap = lexicon.getCompiledMap();
    for (Iterator i = phonemeMap.keySet().iterator(); i.hasNext();count++)
    {
      String test = (String) i.next();
      String[] phones = getPhones(test, false);
      if (phones == null) {
        System.out.println("  "+test+": NULL");
        continue;        
      }
      String testPhone = phones[0];
      //System.out.println("  "+test+": "+testPhone);
      //if (count==100) return;
      if  (targetPhone.equals(testPhone))
        addResult(result, test);
    }
  }
  */
    
     /* //System.out.println(candidate);
      for (Iterator i = partials.iterator(); i.hasNext();) {
        String partial = stripPos((String)i.next());
        //System.out.println("    "+partial);
        if (candidate.equals(partial)) {
          System.out.println("MATCH: contains1");
          addResult(result, candidate);
        }
        if (candidate.contains(input)) {
          System.out.println("MATCH: contains2");
          addResult(result, candidate);
        }
      }
    }
  }*/
 
 /* private static List getPartialCandidates(String word, int minLength)
  {
    List candidates = new LinkedList();
    for (int j = minLength+1; j < word.length(); j++) {
      for (int i = 0; i+j <= word.length(); i++)
      {
        String s = word.substring(i, i+j);
        candidates.add(s);
      }
    } 
    return candidates;
  }*/
  /*

/*  private String getStressedPhone(String syllable)
  {    
    String s = syllable.split(Phrase.SYLLABLE_BOUNDARY)[0];
    System.out.println("LexiconLookup.getStressedPhone("+syllable+") = "+s);
    return s;
  }*/
/*  
  private String getStressedPhone(String[] syllables, int idx)
  {
    String stressedSyl = syllables[idx];
    return getStressedPhone(stressedSyl);
  }
  */
/*
  public String firstStressedPhoneS(String s)
  {  
    String[] phones = getPhones(s, false);

    for (int i = 0; i < phones.length; i++)
    {
      System.out.println(phones[i]);
      if (phones[i].endsWith(STRESSES))
        return phones[i];
    }
    throw new PException("No Stressed phonemes in word");
  }*/
  
  /*public void getFullAlliterations(String input, Set result)
  {
    String stressedPhone = firstStressedPhone(input);
    System.out.println("stressedPhone="+stressedPhone);
    String candidate, phone;// phones, phoneSet[];
    for (Iterator i = phonemeMap.keySet().iterator(); i.hasNext();)
    {
      candidate = (String)i.next();
      candidate = stripPos(candidate);
      //System.out.print("testing: "+candidate+" : '");
      //phones = (String)phonemeMap.get(word);
      //phoneSet = phones.split(Nlg.SPC);
      phone = firstStressedPhone(candidate);
      //System.out.println(phone+"'");

      
      if (phone == null) continue;
      
      if (phone.equals(stressedPhone)) 
        addResult(result, candidate);
    }
  }*/
  // ------------------------ privates ------------------------------
  
  /**remove?
   * Returns valid words (in lexicon) using a variety of criteria specified by 'searchType'
   * with length at least 'minLength' characters 
   */
  private void lexiconScan(String input, Set result, int searchType) 
  {
    this.lexiconScan(input, result, searchType, DEFAULT_MATCH_MIN_LENGTH); 
  }
  
  /**   remove?
   * Returns valid words (in lexicon) using a variety of criteria specified by 'searchType'
   * with length at least 'minLength' characters 
   */
  private void lexiconScan(String input, Set result, int searchType, int minLength) 
  {
    //if (minLength < 0) minLength = DEFAULT_MATCH_MIN_LENGTH;
    
    LEX: for (Iterator j = lexicon.iterator(); j.hasNext();)
    {
      String candidate = ((String)j.next());
      //int targetLength = input.length();
      if (candidate.length() < minLength)
        continue;
      
      switch (searchType) 
      {
        case CHARACTERS_MATCH:
          for (int i = 0; i < candidate.length(); i++)
          {
            char c = candidate.charAt(i);
            if (input.indexOf(c) < 0) 
              continue LEX;
          } 
          addResult(input, result, candidate, minLength);
          break;
          
          
/*               TODO: Implement!
          case EXACT_CHARACTER_MATCH: //scramble 

          if (targetLength != candidate.length()) 
            continue;
          for (int i = 0; i < candidate.length(); i++)
          {
            char c = candidate.charAt(i);
            if (input.indexOf(c) < 0) 
              continue LEX;
          } 
          addResult(result, candidate);
          break;*/

        default:
          break;     
      }
    
    } // end LEX   
  }

  private boolean checkResult(String input, Collection result, String candidate, int minLength)
  {
    if (candidate.length()<minLength) {
      //System.out.println("RiLexicon.addResult(False::BAD_LENGTH) -> "+candidate);
      return false;
    }    
    if (candidate.equals(input) || 
      candidate.equals(input+"s") || 
      candidate.equals(input+"es")) 
    {
      //System.out.println("RiLexicon.addResult(False::INPUT) -> "+candidate);
      return false;
    }   
    return true;
  }  
  
  private boolean addResult(String input, Collection result, String candidate, int minLength)
  {
    if (checkResult(input, result, candidate, minLength)) {
      result.add(candidate);
      return true;
    }
    return false;
  }
    
  /**
   * Return the list of possible parts-of-speech
   * for the <code>word</code> , or null if
   * not found.
   */
  public String[] getPosEntries(String word)
  {
    return lexicon.getPosArr(word);
  }
  
  /*
   * Includes the last stressed syllable and all subsequent phonemes
   * in the form 'syl1.ph1 syl1.ph2/syl1.ph1 syl1.ph2/'
   */  
  private String lastStressedPhoneToEnd(String word) 
  {
    String raw = lexicon.getRawPhones(word);
    if (raw == null) return null;
    //System.out.print("'"+raw+"' -> ");
    int idx = raw.lastIndexOf(STRESSED);
    //System.out.print("idx="+idx);
    if (idx < 0) return null; 
    char c = raw.charAt(--idx);
    //System.out.print("\n  chk:"+idx+"="+c);
    while (c != '-' && c != ' ') {
      if (--idx < 0) {
        //System.out.println(raw);
        return raw; // single-stressed syllable
      }
      //System.out.print("  chk:"+idx+"="+raw.charAt(idx));
      c = raw.charAt(idx);      
    }    
    String res = raw.substring(idx+1);
    //System.out.println(res);
    return res;
  }
  
  /*
   * Includes the last stressed syllable and all subsequent phonemes
   * in the form 'syl1.ph1 syl1.ph2/syl1.ph1 syl1.ph2/'
   */  
  private String lastStressedSyllableToEnd(String word) 
  {
    String raw = lexicon.getRawPhones(word);
    //System.out.print("'"+raw+"' -> ");
    int idx = raw.lastIndexOf(STRESSED);
    //System.out.print("idx="+idx);
    if (idx < 0) return null; 
    char c = raw.charAt(--idx);
    //System.out.print("\n  chk:"+idx+"="+c);
    while (c != ' ') {
      if (--idx < 0) {
        //System.out.println(raw);
        return raw; // single-stressed syllable
      }
      //System.out.print("  chk:"+idx+"="+raw.charAt(idx));
      c = raw.charAt(idx);      
    }    
    String res = raw.substring(idx).trim();
    //System.out.println(res);
    return res;
  }
    
/*  private String lastStressedSyllable(String word) 
  {
    String raw = lastStressedSyllableToEnd(word);    
    if (raw == null) return raw;
    int idx = raw.indexOf(' ');     // syllable-boundary 
    return (idx < 0) ? raw : raw.substring(0,idx);    
  }*/
  
  private String firstStressedSyllable(String word) 
  {
    String raw = lexicon.getRawPhones(word);
    //System.out.println(word + "-> raw='"+raw+"'");
    int idx = -1; 
    
    if (raw != null)
      idx = raw.indexOf(STRESSED);
    else
      System.out.println("[WARN] No stress data for '"+word+"'");
    //System.out.print("idx="+idx);
    
    if (idx < 0) return null; // no stresses
    
    char c = raw.charAt(--idx);
    //System.out.print("\n  chk:"+idx+"="+c);
    while (c != ' ') {
      if (--idx < 0) {
        // single-stressed syllable
        idx = 0;
        break;
      }
      //System.out.print("  chk:"+idx+"="+raw.charAt(idx));
      c = raw.charAt(idx);      
    }    
    String firstToEnd = idx == 0 ? raw : raw.substring(idx).trim();
    //System.out.println("\nfirstToEnd="+firstToEnd);
    idx = firstToEnd.indexOf(' ');
    //System.out.println("idx="+idx);
    String res =  idx < 0 ? firstToEnd : firstToEnd.substring(0, idx); 
    //System.out.println(res);
    return res;
  } 
  
/* TODO:
 
  "aphaeresis" -> loss of one or more sounds from the beginning of a word 
  "syncope"    -> loss of one or more sounds from the middle of a word
  "apocope"    -> loss of one or more sounds from the end of a word

  In phonetics these terms are often but not always limited to the loss of an unstressed vowel:

  The loss of an unstressed vowel:
    * English [a]cute > cute
    * English [E]gyptian > Gyptian > Gypsy
    * English [a]mend > mend
    * English [e]scape + goat > scapegoat
    * Old French evaniss- > English vanish
    * English esquire > squire
 */
    
  /**
   * Returns valid words (in lexicon) using aphaeresis and apocope  
   
  public Set getPartialMatchesBySound(String word) {
    return getPartialMatchesBySound(word, 2);
  }  
  /**
   * Returns valid words (in lexicon) using aphaeresis and apocope 
   * with length greater than minLength 
   *//*
  public Set getPartialMatchesBySound(String word, int minLength) {
    throw new RuntimeException("implement me!");
  // return getPartialCandidates(word, minLength);       
  }
  
*/
  /** @invisible */
  public static void tests(String[] args) throws InterruptedException
  {    
    System.err.println("RiLexicon.main()");
   // RiCMULexicon lex = (RiCMULexicon)RiCMULexicon.getInstance(null);
    RiLexicon lex = new RiLexicon(null);
    //System.out.println(RiTa.asList(lex.getSyllableArr("elevator")));
    /*Set s = lex.getWords();    
    int i = 0;    
    long start = System.currentTimeMillis();
    Iterator it = s.iterator(); 
    while (it.hasNext()) {
      it.next();
      i++;
    }    
    System.err.println("Loop found "+i+" items in "+Util.elapsed(start));*/
    
/*  if (1==1) return;
    
    Set result = new TreeSet(); int min=0;    
    
    result.clear();
    min = lex.similarBySound("letter", result, true);
    System.out.println("SimilarBySound: "+result);   
    System.out.println("MED: "+min); 
    System.out.println(timer());
    System.out.println("------------------------------------------------");
    
    if (1==1) return;
    
    String input = "oblique";    
    System.out.println(timer());
    System.out.println("------------------------------------------------");

    if (1==2) {
      System.out.println("Syllables:     "+Arrays.asList(lex.getSyllables(input)));
      System.out.println("Stresses:      "+Arrays.asList(lex.getStresses(input)));
      System.out.println("StressedSyl:   "+lex.getFirstStressedSyllable(input));
      System.out.println("StressedPhone: "+lex.getFirstStressedPhone(input));
      System.out.println("StressedSyl:   "+lex.getLastStressedSyllable(input));
      System.out.println("StressedPhone: "+lex.getLastStressedPhone(input));
    }
    
    result.clear();
    lex.substringsByLetter(input, result, true);
    System.out.println("\nSubstringsByLetter: "+result);
    System.out.println(timer());
    System.out.println("------------------------------------------------");
        
    result.clear();
    lex.superstringsByLetter(input, result, true);
    System.out.println("SuperStringsByLetter: "+result);
    System.out.println(timer());
    System.out.println("------------------------------------------------");
    
    result.clear();
    lex.containingStringsByLetter(input, result, true);
    System.out.println("ContainingStringsByLetter: "+result);
    System.out.println(timer());
    System.out.println("------------------------------------------------");       
    
    result.clear();
    min = lex.similarBySound(input, result, true);
    System.out.println("SimilarBySound: "+result);   
    System.out.println("MED: "+min); 
    System.out.println(timer());
    System.out.println("------------------------------------------------");
    
    result.clear();
    med.getAlliterations(input, result);  // BROKEN: earth??
    System.out.println("Alliterations: "+result);      
    System.out.println("------------------------------------------------");

    result.clear();  
    min = lex.similarByLetter(input, result, true);
    System.out.println("SimilarByLetter"+result);    
    System.out.println("MED: "+min);  
    System.out.println(timer());
    System.out.println("------------------------------------------------");   
        
    result.clear();
    min = lex.similarBySoundAndLetter(input, result, true);
    System.out.println("SimilarBySound/Letter: "+result);   
    System.out.println("MED: "+min);   
    System.out.println(timer());
    System.out.println("------------------------------------------------");
    
    result.clear();
    lex.getSimpleRhymes(input, result);
    System.out.println("SimpleRhymes: "+result); 
    System.out.println(timer());
    System.out.println("------------------------------------------------");

    result.clear();
    lex.getSimpleAlliterations(input, result);
    System.out.println("SimpleAlliterations: "+result);  
    System.out.println(timer());
    System.out.println("------------------------------------------------");         

    // ----------------------- SCAN FUNCTIONS -----------------------------
    result.clear(); 
    lex.lexiconScan(input, result, CHARACTERS_MATCH);
    System.out.println("CharacterMatch: "+result);         
    System.out.println(timer());
    System.out.println("------------------------------------------------");*/
  }   
  
  String getSyllables(String word)
  {
    Map m = lexicon.getFeatures(word);  // lts?
    if (m == null) return null;
    return (String)m.get(Featured.SYLLABLES);
  }
  
  String getPhonemes(String word)
  {
    Map m = lexicon.getFeatures(word);  // lts?
    if (m == null) return null;
    return (String)m.get(Featured.PHONEMES);
  }
  
  public Map getFeatures(String word) {
    return lexicon.getFeatures(word);
  }
    
  // this needs to be optimized!
  /**
   * Use this method to preload the Lexicon with feature data (stress, syllables, pos, phones, etc).
   * Increases the initialization time but speeds up all subsequent lookups by an order of magnitude.
   * Useful when doing many lookups over the course of a program, especially with the RiTaServer. Example:<pre>
        RiLexicon lex = new RiLexicon();
        lex.preloadFeatures();
        // use the lexicon</pre> 
   */
  public void preloadFeatures() {
    lexicon.preloadFeatures();
  }
    /*cacheEnabled = true; // ?
    long start = System.currentTimeMillis();
    for (Iterator iterator = iterator(); iterator.hasNext();) 
      getFeatureMap((String) iterator.next());          *
      re/
    System.out.println("[INFO] Created and cached features in "+RiTa.elapsed(start)+"s");
  }*/
  
  // fix tags not starting with 0 ??
  // fix pos-tags containing ,s  ??
  //public static void main(String[] args) throws IOException
  //{ 
    //RiLexicon lex = new RiLexicon();
    //lex.preloadFeatures();
    //System.out.println(lex.writeLexicon("test.lex.txt")+" items");
    //System.out.println(lex.getFeatureMap("legible"));
    /*if (1==1) return;
    
    int i = 0;
    String[] s,r;
    
    lex.preloadFeatures();
    for (Iterator iterator = lex.randomIterator(); iterator.hasNext();i++) {
      String word = (String)iterator.next();
      String lss = lex.lastStressedSyllable(word).replace('-', ' ');
      System.out.println(word+" -> "+lex.postLastStressedSyllable((RiCMULexicon)lex.getPhonemeStr(word),lss));
      
      
      if (i > 100) break; // BREAKS HERE!!!!
      
      
    }
    if (1==1) return;
    long time = System.currentTimeMillis();
     lex.getRhymes("abrupt");
     System.out.println(RiTa.elapsed(time));
         time = System.currentTimeMillis();
     lex.getRhymes("abrupt");
     System.out.println(RiTa.elapsed(time));
    for (Iterator iterator = lex.randomIterator(); iterator.hasNext();i++) {
      String word = (String)iterator.next();
      System.out.println(word+" -> "+lex.lexicon.lookupRaw(word)+"\t"+lex.getSyllableStr(word));
      if (i > 100) break;
    }
    //
    for (Iterator iterator = lex.randomIterator(); iterator.hasNext();i++) {
      String word = (String)iterator.next();
      System.out.println(lex.lastStressedSyllable(word) + " ## "+lex.lexicon.lookupRaw(word));
      //System.out.println(word+": "+lex.getStressStr(word)+" # "+lex.getSyllableStr(word)+" ["+lex.lexicon.lookupRaw(word)+"]");
      //if (i==100)
        //break;
      if (i==100) {        
        System.out.println(i+" done...");
        break;
      }
    }
    
    
    
    for (Iterator it = lex.iterator(); i < 100 && it.hasNext();i++) {
      String word = (String) it.next();
      String lss = lex.lastStressedSyllable(word);
      System.out.println(i+") "+word+" -> "+lss);
    }
    String[] s = lex.similarBySound("align");    
    System.out.println("SIM: "+RiTa.asList(s));
    

    String[] r  = lex.getSimpleRhymes("align");
    System.out.println("RHYME: "+RiTa.asList(r));
    
    System.out.println("MALIGN: "+lex.contains("malign"));
    s = lex.similarBySound("cat");
    System.out.println("SIM: "+RiTa.asList(s));
    r  = lex.getSimpleRhymes("cat");
    System.out.println("RHYME: "+RiTa.asList(r));
    //System.out.println("last: "+lex.lastSyllable("abode"));
    
    for (int j = 0; j < s.length; j++) {
      boolean r1 = lex.isRhyme("cat", s[j]);
      
      if (!r1 && RiTa.contains(r, s[j]) ||
        (r1 && !RiTa.contains(r, s[j])))
      {
        String lastA = lex.lastPhone("cat"); 
        String lastB = lex.lastPhone(s[j]);        
        System.out.println("=================================");
        System.out.println("cat && "+s[j]+":");
        System.out.println("  "+lastA+" / "+lastB);
        System.out.println("    -> "+r1);      
      }
    }
    System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n");
    String[] fails = {"boy", "cut", "cot", "apricot", "hello", "cataract", "attic"};
    for (int j = 0; j < fails.length; j++) {
      boolean r1 = lex.isRhyme("cat", fails[j]);
      if (r1) {
        String lastA = lex.lastPhone("cat"); 
        String lastB = lex.lastPhone(fails[j]);        
        System.out.println("=================================");
        System.out.println("cat && "+fails[j]+":");
        System.out.println("  "+lastA+" / "+lastB);
        System.out.println("    -> "+r1);
      }
    }    
    for (int j = 0; j < s.length; j++) {
      boolean r1 = lex.isRhyme1("cat", s[j]);
      boolean r2 = lex.isRhyme2("cat", s[j]);
      if (r1 != r2) {
        String lastA = lex.lastPhone("cat"); 
        String lastB = lex.lastPhone(s[j]);        
        System.out.println("=================================");
        System.out.println(lastA+" / "+lastB);
        System.out.println("\ncat 1) "+s[j]+" : "+r1);      
        System.out.println("    2) "+s[j]+" : "+r2);
        System.out.println("=================================");
      }
    }
    System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
    String[] fails = {"boy", "cut", "cot", "apricot", "hello", "cataract", "attic"};
    for (int j = 0; j < fails.length; j++) {
      boolean r1 = lex.isRhyme1("cat", fails[j]);
      boolean r2 = lex.isRhyme2("cat", fails[j]);
      if (r1 || r2) {
        System.out.println("\ncat 1) "+fails[j]+" : "+r1);      
        System.out.println("    2) "+fails[j]+" : "+r2);
      }
    }
    
    //String[] s = lex.getSimpleRhymes("cat");
    RiAnalyzer ra = new RiAnalyzer();
    System.out.println("mat="+lex.contains("mat"));
    System.out.println("mat="+lex.getPhonemeStr("mat"));
    System.out.println("cat="+lex.getPhonemeStr("cat"));
    System.out.println("kit="+lex.getPhonemeStr("kit"));
    System.out.println("cot="+lex.getPhonemeStr("cot"));
    System.out.println("antibody="+RiTa.asList(lex.getStresses("antibody")));
    System.out.println("antibody="+RiTa.asList(lex.getSyllables("antibody")));
    System.out.println("antibody="+lex.getPhonemeStr("antibody"));
    System.out.println("antibody="+RiTa.asList(lex.getPhones("antibody")));
    System.out.println("-----------------------------");
    System.out.println("antibody="+RiTa.asList(lex.lexicon.getPhones("antibody")));
    System.out.println("-----------------------------");    
    ra.analyze("antibody");
    System.out.println("antibody="+ra.getStresses());
    System.out.println("antibody="+ra.getPhonemes());
    System.out.println("antibody="+ra.getSyllables());
    
    //String[] s = lex.similarBySound("cat");
    //String[] s = lex.getSimpleRhymes("cat");
    //System.out.println(RiTa.asList(s));
    Map m = lex.getFeatureMap("deliverance");
    System.out.println(m);
    System.out.println(m.get("syllables"));
    //writeLexicon(lex, "src/bin/freetts-build/com/sun/speech/freetts/en/us/cmudict04_compiled.txt");    
*/  
  
  /**
   * Returns true if the word exists in the lexicon
   */
  public boolean contains(String word)
  {
    return lexicon.lookupRaw(word) != null;
  }
    public static void testRhymes() 
  {   
    RiTest[] tests = {
      new RiTest("brow","now", true),
      new RiTest("snow","know", true),
    };
    RiLexicon lex = new RiLexicon();
    for (int i = 0; i < tests.length; i++) {
      boolean b = false;
      if (b=lex.isRhyme(tests[i].a(),tests[i].b())==tests[i].value())
       System.out.println((i+1)+"... Ok");
      else {
        System.out.println((i+1)+"... ERROR! "+tests[i]+" but returns "+b);
        System.exit(1);
      }
    }        
  }
   
    
  /**
   * Returns  
   * @param word
   */
  public String getPosStr(String word) {
    return lexicon.getPosStr(word);
  }
 
  /**
   *  Returns the raw data (as a Map) used in the lexicon, allowing for
   *  deletion or modification of existing lexical entires. Modifications 
   *  to this Map will be immediately reflected in all operations on the lexicon.   
   */
  public Map getLexicalData()
  {
    return lexicon.getLexicalData();
  }

  /** 
   * Sets the raw data to be used in the lexicon, replacing
   * all default words and features with those specified in the map. 
   * When using this method, be sure to exactly match the format
   * as specified rita_addenda.txt, e.g., <pre>
##############################################################################
#### FORMAT##<word>: <phone1> <phone2> ... <phoneN> | <pos1> <pos2> ... <posN>
##############################################################################

blog: b-l-ao-g  | nn vbg
cepstral: k-eh1-p s-t-r-ax-l  | nnp
freetts:  f-r-iy1 t-iy t-iy eh-s  | nnp
jsapi:  jh-ey s-ae1-p iy  | nnp
   * </pre>
   */
  public void setLexicalData(Map lexicalData)
  {
    lexicon.setLexicalData(lexicalData);
  }
  
  /**
   * @invisible
   */
  public static void testAllits() 
  {   
    RiTest[] tests = {
      new RiTest("festival","flinch", true),
      new RiTest("fern","flinch", true),
      new RiTest("festival","flinching", true),      
      new RiTest("","flinching", false),
      new RiTest(null,"flinching", false),
      new RiTest("instantly","begun", false),
      new RiTest("your", "or", false),
      new RiTest("and","now", false),
      new RiTest("nothing","instantly", false),      
      new RiTest("arbor","ardent", true),
    };
    RiLexicon lex = new RiLexicon();
    for (int i = 0; i < tests.length; i++) {
      boolean b = false;
      if (b=lex.isAlliteration(tests[i].a(),tests[i].b())==tests[i].value())
       System.out.println((i+1)+"... Ok");
      else {
        System.out.println((i+1)+"... ERROR! "+tests[i]+" but returns "+b);
        System.exit(1);
      }
    }        
  }
  
  public static void mainX(String[] args) 
  { 
    //testRhymes();
    //if (1==1) return;
    RiLexicon lex = new RiLexicon();
    RiStemmer rs = new RiStemmer();
    Iterator it2 = lex.iterator("(?:.*)[aeiou](.*)y");
    while (it2.hasNext())
    {
      String s= (String) it2.next();
      System.out.println(s+": "+rs.stem(s));      
    }
    if (1==1) return;
    //m.remove("more");
    String input1 = "god";
    System.out.println(lex.singleLetterInsertions(input1));
    
    String input2 = "fired";

    // WORKING HERE: ****************
    
   /* System.out.println(); 
    //Set result = new HashSet();
    Set set = lex.jointSimilarByLetter(input1, input2, 5, 2);
    System.out.println("RESULTS");
    for (Iterator it = set.iterator(); it.hasNext();)
    {
      String x = (String) it.next();
      System.out.println(x);      
    }*/
    
    if (1==1) return;
    
    testAllits();
        
    int cnt = 0;
    String[] s = null;
    String word = "first";
    lex = new RiLexicon();
    for (Iterator it = lex.iterator(); it.hasNext();) { 
       word = (String) it.next();
       System.out.print(".");
       if (++cnt%50==0) System.out.println();
       if (lex.similarByLetter(word).length<1)
         throw new RiTaException("No similarByLetter for: "+word);
    }
    
    if (1==1) return;
    
    System.out.println("caseload: "+RiTa.asList(lex.similarByLetter("caseload")));
    System.out.println("horizon: "+RiTa.asList(lex.similarByLetter("horizon")));
    
    if (1==1) return; 
    
    for (Iterator it = lex.randomIterator("ee"); it.hasNext();) 
      System.out.println(it.next());
      
    //for (int i = 0; i < 20; i++)       
      //System.out.println(lex.getRandomWord("nn"));
    
    if (1==1) return;
    lex.preloadFeatures();
    long start = System.currentTimeMillis();
    
    s = lex.similarBySound(word);
    
    for (int i = 0; s!=null && i < s.length; i++)
        System.out.println(i+") "+word+" && "+s[i]);
        
    RiTa.pElapsed(start);
    start = System.currentTimeMillis();
    s = lex.similarByLetter(word);
    RiTa.pElapsed(start);

    if (1==1) {
       
      s = lex.getAlliterations(word);
      //System.out.println(RiTa.asList(s));
      for (int i = 0; s!=null && i < s.length; i++) {
        System.out.println(i+") elevator && "+s[i]);//" isAllit="+lex.isAlliteration(word, s[i]));
        //System.out.println(i+") "+s[i]+" isRhyme="+lex.isRhyme(word, s[i]));
        //System.out.println(i+") "+s[i]+"s isRhyme="+lex.isRhyme(word+"s", s[i]));
      }
      RiTa.pElapsed(start);
      start = System.currentTimeMillis();
      s = lex.getAlliterations(word);
      RiTa.pElapsed(start);
      if (1==1) return;
      s = lex.getRhymes(word);
      //System.out.println(RiTa.asList(s));
      for (int i = 0; s!=null && i < s.length; i++) {
        //System.out.println(i+") "+s[i]+" isRhyme="+lex.isRhyme(word, s[i]));
        System.out.println(i+") "+s[i]+"s isRhyme="+lex.isRhyme(word+"s", s[i]));
      }
      RiTa.pElapsed(start);
      if (1==1) return;
    }
    else 
    {  
      String x = "";
      for (Iterator it = lex.iterator(); it.hasNext();) {
        word = (String) it.next();
        System.out.print(lex.lexicon.lookupRaw(word)+" -> ");
        System.out.println(x=lex.firstStressedSyllable(word));//
        if (x!=null && x.indexOf(' ')>=0)
          throw new RiTaException();
        //lex.lastStressedSyllableToEnd(word);     
      }
    }
    if (1==1) return;
    
    for (Iterator it = lex.iterator(); it.hasNext();) {
      word = (String) it.next();
      //System.out.println(lex.lexicon.lookupRaw(word));
      System.out.println(lex.getRhymes(word));//
      //lex.lastStressedSyllableToEnd(word);      
    }
    RiTa.pElapsed(start);
    
    if (1==1) return;
    
    lex.preloadFeatures();
    System.out.println(lex.getSyllables(word));
    System.out.println(lex.getFeatures(word));
    
    System.out.println("Rhymes="+RiTa.asList(lex.getRhymes(word)));
    RiTa.pElapsed(start);
  }
  
  public static void main(String[] args)
  {
    String pos = "vbn";
    RiLexicon lex = new RiLexicon(null);
    System.out.println(lex.getLexicalData().get("diapers"));
    for (int i = 0; i < 10; i++)
    {
      System.out.println(i+"a) "+ lex.getRandomWord(6));
      System.out.println(i+"b) "+ lex.getRandomWord(pos, 6));
      System.out.println(i+"c) "+ lex.getRandomWordWithSyllableCount(3));
      System.out.println(i+"d) "+ lex.getRandomWordWithSyllableCount(pos, 3));
      System.out.println();
    }
    
    /*RiLexicon lex = new RiLexicon(null, "aspellutf8combined.txt");
    for (Iterator it = lex.iterator("aba.*"); it.hasNext();)
      System.out.println(it.next());
    System.out.println("ababilló="+lex.getPhonemes("ababilló"));*/
    }
  
}// end
 