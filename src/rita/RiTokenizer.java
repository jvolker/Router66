package rita;

import java.util.List;

import processing.core.PApplet;
import rita.support.*;
import rita.support.ifs.RiTokenizerIF;

/**
 * A simple tokenizer for word boundaries with regular expression
 * support for custom-tokenizing.
 * 
 * @author dhowe
 */
public class RiTokenizer extends RiObject implements RiTokenizerIF
{  
  protected static final int REGEX_TOKENIZER = 2;
  protected static final int FREE_TTS_TOKENIZER = 3;
  protected static final int PENN_WORD_TOKENIZER = 4;
   
  //protected static final int OAK_WORD_TOKENIZER = 5;   
  //protected static final int MAXENT_TOKENIZER = 7;
  
  protected static int DEFAULT_TOKENIZER = PENN_WORD_TOKENIZER;

  protected static RiTokenizerIF delegate;
  
  /** @invisible    */  
  public RiTokenizer() {
    this(null, DEFAULT_TOKENIZER);    
  }
  
  public RiTokenizer(PApplet pApplet) {
    this(pApplet, DEFAULT_TOKENIZER);    
  }
  
  /** @invisible    */
  public RiTokenizer(int type) {
    this(null, type);    
  }
  
  public RiTokenizer(PApplet pApplet, int type) {
    super(pApplet); 
    delegate = createDelegate(pApplet, type);
    instance = this;
  }
  
  // =========== static (singleton) instances ============
   
  protected static RiTokenizer instance;
    

  /**
   *  @invisible
   */
  public static RiTokenizer getInstance() {
    return getInstance(null, DEFAULT_TOKENIZER);
  }
  
  /**
   *  @invisible
   */
  public static RiTokenizer getInstance(PApplet p) {
    return getInstance(p, DEFAULT_TOKENIZER);
  }
  
  /**
   *  @invisible
   */
  public static RiTokenizer getInstance(int type) {
    return getInstance(null, type);
  }

  /**
   *  @invisible
   */
  public static RiTokenizer getInstance(PApplet p, int type) {
    if (instance == null)
      instance = new RiTokenizer(p, type);
    return instance;
  }
  
  // --------------------------------------------------------------
  
  private static RiTokenizerIF createDelegate(PApplet p, int type)
  {
    RiTokenizerIF previous = delegate;    
    switch (type) {
      case PENN_WORD_TOKENIZER:
        if (delegate == null || (!(delegate instanceof PennWordTokenizer))) 
          delegate = new PennWordTokenizer(p);          
        break;
      case FREE_TTS_TOKENIZER:
        if (delegate == null || (!(delegate instanceof TTSWordTokenizer)))
          delegate = new TTSWordTokenizer(p);
        break;
      case REGEX_TOKENIZER:     
        if (delegate == null || (!(delegate instanceof RegexTokenizer)))
          delegate = new RegexTokenizer(p);          
        break;
      default:
        throw new RiTaException("Unexpected Tokenizer Type: "+type);
    }
    if (previous != null && previous.getClass() != delegate.getClass())
      System.out.println("[WARN] Creating multiple word tokenizer types!");
    return delegate;
  }

  /**
   * Tokenizes the sentence into an array of words.
   */
  public String[] tokenize(String sentence)
  {
    return delegate.tokenize(sentence);
  }


  /**
   * Tokenizes the sentence into an array of words
   * and adds them to the result List
   * @invisible
   */
  public void tokenize(String sentence, List result)
  {
    delegate.tokenize(sentence, result);
  }
  
  /**
   * Sets the regular expression for tokenization
   */
  public void setRegex(String regex)
  {
    if (delegate == null)
      delegate = createDelegate(null, REGEX_TOKENIZER);
    
    if (delegate instanceof RegexTokenizer) {
      RegexTokenizer rt = (RegexTokenizer) delegate;
      rt.setRegex(regex);      
    }
    else 
      throw new RiTaException("Cannot specify a regex unless using "+
        "a RegexTokenizer, but the current tokenizer is: "+delegate);
  }
  
  public static void main(String[] args) {
    
  }

}// end
