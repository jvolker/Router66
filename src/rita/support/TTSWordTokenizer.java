package rita.support;

import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;
import rita.RiObject;
import rita.support.ifs.RiTokenizerIF;

import com.sun.speech.freetts.Tokenizer;
import com.sun.speech.freetts.en.TokenizerImpl;
import com.sun.speech.freetts.en.us.USEnglish;

/**
 * Text-To-Speech tokenizer which ignores punctuation
 */
public class TTSWordTokenizer extends RiObject implements RiTokenizerIF
{
  protected Tokenizer delegate;
  
  public TTSWordTokenizer() {
    this(null);
  }
  
  public TTSWordTokenizer(PApplet p) {
    super(p);
    this.delegate = new TokenizerImpl();
    delegate.setWhitespaceSymbols(USEnglish.WHITESPACE_SYMBOLS);
    delegate.setSingleCharSymbols(USEnglish.SINGLE_CHAR_SYMBOLS);
    delegate.setPrepunctuationSymbols(USEnglish.PREPUNCTUATION_SYMBOLS);
    delegate.setPostpunctuationSymbols(USEnglish.PUNCTUATION_SYMBOLS);   
  }
  
  /**
   * Tokenizes the String according to  OAK conventions
   * and stores the result as a List in <code>result</code>
   */
  public void tokenize(String sentence, List result) 
  {
    delegate.setInputText(sentence);
    while (delegate.hasMoreTokens())
      result.add(delegate.getNextToken().getWord());
  }
  
  public String[] tokenize(String sentence)
  {
    delegate.setInputText(sentence);
    List result = new ArrayList();
    tokenize(sentence, result);    
    return (String[]) result.toArray(new String[result.size()]);
  }
  
  public static void main(String[] args)
  {
    String sent = "This is the fastest, most perfect car: it does 0-60 in four seconds!";
    TTSWordTokenizer tk = new TTSWordTokenizer();
    String[] words = tk.tokenize(sent);
    System.out.println(asList(words));
  }
  
}// end
