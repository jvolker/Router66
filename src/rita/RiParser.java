package rita;

import processing.core.PApplet;

import rita.support.ifs.RiParserIF;
import rita.support.me.MaxEntParser;
import rita.support.remote.RemoteParser;

/**
 * Tree-based parser for recursive syntactic annotations, e.g.,
 * noun-phrases, using the Penn conventions.<br> 
 * 
 * An example:
 *   <pre>
     String s = "The black cat crossed my path.";
     RiParser parser = new RiParser();
     String result = parser.parse(s);
     System.out.println(result);</pre> 
 * 
 * Note: to use this object, first download the rita statistical
 * models (rita.me.models.zip) and unpack them into the 'rita'
 * directory in your libraries directory within your processing sketchbook,
 * e.g., $SKETCH_PAD/libraries/rita/models. 
 * You may also specify an alternative directory (an absolute path) for the
 * models via RiTa.setModelDir();<p> 
 * 
 * This object is most useful when used with the RiTaServer
 * as it can take significant time to load the necessary
 * statisical models.<p>
 * 
 * <pre>
    RiTa.useServer(portNumber);
    RiTa.setModelDir("/models");
    String s = "The black cat crossed my path.";
    RiParser rp = new RiParser();     
    System.out.println(rp.parse(s));</pre>  
 *
 * Primarily just a wrapper for the OpenNLP(http://opennlp.sourceforge.net) parser 
 * with some minor modifications/simplifications.<p>  
 *
 * For more info see: Berger & Della Pietra's paper: 
 * 'A Maximum<br> Entropy Approach to Natural Language Processing',
 * which<br>provides a good introduction to the maxent framework. 
 * <p>
 *  The full tag set follows:
<ul>
<li>S - simple declarative clause, i.e. one that is not introduced by a (possible
 empty) subordinating conjunction or a wh-word and that does not exhibit
 subject-verb inversion.
<li>SBAR - Clause introduced by a (possibly empty) subordinating conjunction.
<li>SBARQ - Direct question introduced by a wh-word or a wh-phrase. Indirect
 questions and relative clauses should be bracketed as SBAR, not SBARQ.
<li>SINV - Inverted declarative sentence, i.e. one in which the subject follows
 the tensed verb or modal.
<li>SQ - Inverted yes/no question, or main clause of a wh-question, following the
 wh-phrase in SBARQ.
<li>Phrase Level
<li>ADJP - Adjective Phrase.
<li>ADVP - Adverb Phrase.
<li>CONJP - Conjunction Phrase.
<li>FRAG - Fragment.
<li>INTJ - Interjection. Corresponds approximately to the part-of-speech tag UH.
<li>LST - List marker. Includes surrounding punctuation.
<li>NAC - Not a Constituent; used to show the scope of certain prenominal
 modifiers within an NP.
<li>NP - Noun Phrase.
<li>NX - Used within certain complex NPs to mark the head of the NP. Corresponds
 very roughly to N-bar level but used quite differently.
<li>PP - Prepositional Phrase.
<li>PRN - Parenthetical.
<li>PRT - Particle. Category for words that should be tagged RP.
<li>QP - Quantifier Phrase (i.e. complex measure/amount phrase); used within NP.
<li>RRC - Reduced Relative Clause.
<li>UCP - Unlike Coordinated Phrase.
<li>VP - Vereb Phrase.
<li>WHADJP - Wh-adjective Phrase. Adjectival phrase containing a wh-adverb, as in
 how hot.
<li>WHAVP - Wh-adverb Phrase. Introduces a clause with an NP gap. May be null
 (containing the 0 complementizer) or lexical, containing a wh-adverb such as how
 or why.
<li>WHNP - Wh-noun Phrase. Introduces a clause with an NP gap. May be null
 (containing the 0 complementizer) or lexical, containing some wh-word, e.g. who,
 which book, whose daughter, none of which, or how many leopards.
<li>WHPP - Wh-prepositional Phrase. Prepositional phrase containing a wh-noun
 phrase (such as of which or by whose authority) that either introduces a PP gap
 or is contained by a WHNP.
<li>X - Unknown,simple
</ul>
 * @see RiTaServer 
 * @invisible
 */
public class RiParser extends RiObject implements RiParserIF
{
  protected RiParserIF delegate;
  
  /**
   * Note: when using this constructor, the Processing 'sketchpad' directory
   * will NOT be checked for models.
   * @see RiParser#RiParser(PApplet)
   * @invisible
   */
  public RiParser() {
    this(null);    
  }
  
  public RiParser(PApplet pApplet) {
    super(pApplet);    
    if (RiTa.isServerEnabled())
      delegate = new RemoteParser(MaxEntParser.class);
    else
      delegate = MaxEntParser.getInstance(pApplet);    
  }
  
  /**
   * Returns the String of the most probable parse using Penn 
   * Treebank-style formatting with or without indents 
   * depending on the paramater <code>indent</code>.
   */
  public String parse(String text, boolean indent) {
    if (delegate instanceof MaxEntParser)
    {
      return ((MaxEntParser) delegate).parse(RiTa.stripPunctuation(text), indent);
    } 
    throw new RiTaException("Method unavailable via " +
      "RiTaServer, use RiParser.parse(String) instead.");
  }
   
  /**
   * Returns the String of the most probable parse 
   * using Penn Treebank-style formatting. 
   */
  public String parse(String text) {
    return delegate.parse(RiTa.stripPunctuation(text));
  } 

  public static void main(String[] args)
  {
    //RiPosTagger.DEFAULT_POS_TAGGER = RiPosTagger.BRILL_POS_TAGGER;    
    RiTa.useServer(4444);
    RiTa.setModelDir("/Users/dhowe/Desktop/models/");
    String sent = "The boy, a child really, ran over the dog";  
    //sent = "The doctor, a man of compassion, treated dogs.";
    //sent = "The doctor treated dogs.";
    RiParser rp = new RiParser();     
    System.out.println(rp.parse(sent));
  }

}
