package rita.support;

import com.sun.speech.freetts.FeatureSet;
import com.sun.speech.freetts.Item;
import com.sun.speech.freetts.Relation;
import com.sun.speech.freetts.Utterance;

/**
 * Helper class to add words and breaks into a Relation object.
 */
public class RiWordRelation
{
  private Relation relation;
  private RiTokenToWords tokenToWords;

  private RiWordRelation(Relation parentRelation, RiTokenToWords tokenToWords) {
    this.relation = parentRelation;
    this.tokenToWords = tokenToWords;
  }

  /**
   * Creates a WordRelation object with the given utterance and TokenToWords.
   * 
   * @param utterance the Utterance from which to create a Relation
   * @param tokenToWords the TokenToWords object to use
   * 
   * @return a WordRelation object
   */
  public static RiWordRelation createWordRelation(Utterance utterance,
      RiTokenToWords tokenToWords)
  {
    Relation relation = utterance.createRelation(Relation.WORD);
    return new RiWordRelation(relation, tokenToWords);
  }

  /**
   * Adds a break as a feature to the last item in the list.
   */
  public void addBreak()
  {
    Item wordItem = (Item) relation.getTail();
    if (wordItem != null)
    {
      FeatureSet featureSet = wordItem.getFeatures();
      featureSet.setString("break", "1");
    }
  }

  /**
   * Adds a word as an Item to this WordRelation object.
   * 
   * @param word the word to add
   */
  public void addWord(String word)
  {
    Item tokenItem = tokenToWords.getTokenItem();
    if (tokenItem == null)
      throw new IllegalStateException("NULL TokenItem!");
    Item wordItem = tokenItem.createDaughter();
    FeatureSet featureSet = wordItem.getFeatures();
    featureSet.setString("name", word);
    relation.appendItem(wordItem);
  }

  /**
   * Sets the last Item in this WordRelation to the given word.
   * 
   * @param word the word to set
   */
  public void setLastWord(String word)
  {
    Item lastItem = relation.getTail();
    FeatureSet featureSet = lastItem.getFeatures();
    featureSet.setString("name", word);
  }

  /**
   * Returns the last item in this WordRelation.
   * 
   * @return the last item
   */
  public Item getTail()
  {
    return relation.getTail();
  }
}
