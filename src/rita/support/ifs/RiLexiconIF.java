package rita.support.ifs;

import com.sun.speech.freetts.lexicon.Lexicon;

public interface RiLexiconIF extends Lexicon
{
  public String[] getPosArr(String word);
  /*public int size();

  public Set getWords();
  
  public Iterator iterator();
  
  public Map getLexiconMap();
  
  public String lookupRaw(String word);
  
  public String lookupPhonemes(String word);
  
  public String lookupPOS(String word);

  public Set getWords(String regex);

                        //remove ref to PApplet from here?
  public void addAddendaEntries(processing.core.PApplet p, String addendaFile, Map lexMap);

  public int getAddendaCount();*/

}
