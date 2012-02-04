package rita.support.me;

import java.io.IOException;
import java.util.*;

import opennlp.tools.lang.english.TreebankParser;
import opennlp.tools.parser.*;
import opennlp.tools.util.Span;

import processing.core.PApplet;

import rita.RiTa;
import rita.RiTaException;
import rita.support.ifs.RiParserIF;

// TODO:  Add penn parse tags to docs
/*
 * Clause Level (from http://bulba.sdsu.edu/jeanette/thesis/PennTags.html)

  S - simple declarative clause, i.e. one that is not introduced by a (possible empty) subordinating conjunction or a wh-word and that does not exhibit subject-verb inversion.
  SBAR - Clause introduced by a (possibly empty) subordinating conjunction.
  SBARQ - Direct question introduced by a wh-word or a wh-phrase. Indirect questions and relative clauses should be bracketed as SBAR, not SBARQ.
  SINV - Inverted declarative sentence, i.e. one in which the subject follows the tensed verb or modal.
  SQ - Inverted yes/no question, or main clause of a wh-question, following the wh-phrase in SBARQ.
  Phrase Level
  ADJP - Adjective Phrase.
  ADVP - Adverb Phrase.
  CONJP - Conjunction Phrase.
  FRAG - Fragment.
  INTJ - Interjection. Corresponds approximately to the part-of-speech tag UH.
  LST - List marker. Includes surrounding punctuation.
  NAC - Not a Constituent; used to show the scope of certain prenominal modifiers within an NP.
  NP - Noun Phrase.
  NX - Used within certain complex NPs to mark the head of the NP. Corresponds very roughly to N-bar level but used quite differently.
  PP - Prepositional Phrase.
  PRN - Parenthetical.
  PRT - Particle. Category for words that should be tagged RP.
  QP - Quantifier Phrase (i.e. complex measure/amount phrase); used within NP.
  RRC - Reduced Relative Clause.
  UCP - Unlike Coordinated Phrase.
  VP - Vereb Phrase.
  WHADJP - Wh-adjective Phrase. Adjectival phrase containing a wh-adverb, as in how hot.
  WHAVP - Wh-adverb Phrase. Introduces a clause with an NP gap. May be null (containing the 0 complementizer) or lexical, containing a wh-adverb such as how or why.
  WHNP - Wh-noun Phrase. Introduces a clause with an NP gap. May be null (containing the 0 complementizer) or lexical, containing some wh-word, e.g. who, which book, whose daughter, none of which, or how many leopards.
  WHPP - Wh-prepositional Phrase. Prepositional phrase containing a wh-noun phrase (such as of which or by whose authority) that either introduces a PP gap or is contained by a WHNP.
  X - Unknown, 
 */
public class MaxEntParser extends RiObjectME implements RiParserIF
{
  private static final boolean DBUG_LOADS = true;

  protected static Parser delegate;   
  
  protected Parse[] parses;
  protected String[] tokens;

  private static MaxEntParser instance;
  
  public static MaxEntParser getInstance() {
    return getInstance(null);
  }
  
  public static MaxEntParser getInstance(PApplet p) {
    if (instance == null)
      instance = new MaxEntParser(p);
    return instance;
  }    
  
  MaxEntParser() { this(null); }
  
  public MaxEntParser(PApplet p) {
    super(p);
    if (delegate == null)
      loadModelData(p);
    instance = this;
  }
  
  // METHODS ====================================================
  
  public static MaxEntParser createRemote(Map params)
  {
//System.out.println("MaxEntParser.createRemote("+params+")"); 
    setModelDir(params);
    return new MaxEntParser();    
  }

  public void destroy() {
    delegate = null;
    instance = null;
  }
  
  // LOADS: build.bin.gz, check.bin.gz, tag.bin.gz, tagdict, chunk.bin.gz, head_rules
  private void loadModelData(PApplet p)
  {
    if (!RiTa.SILENT)System.out.println("[INFO] Loading parser (this may a while)...");
    
    long start = System.currentTimeMillis();
    boolean useTagDict = true;
    boolean useCaseInsensitiveTagDict = false;
    int beamSize = AbstractBottomUpParser.defaultBeamSize;
    double advancePercentage = AbstractBottomUpParser.defaultAdvancePercentage;
    String mdir = getModelDir();
    
    try
    {      
      // if absolute only try the actual path
      if (RiTa.isAbsolutePath(mdir)) {
        delegate = initDelegate(useTagDict, useCaseInsensitiveTagDict, 
            beamSize, advancePercentage, mdir); 
      }
      else  // lets try some other spots 
      {
        // if we are in P5, try the default spot
        if (p != null && !RiTa.isAbsolutePath(mdir))  {
          delegate = initDelegate(useTagDict, useCaseInsensitiveTagDict, beamSize, 
            advancePercentage, RiTa.libPath(p)+"rita/models/");
        }
               
        // lets look in the data directory
        String[] guesses = RiTa.getDataDirectoryGuesses();
        for (int j = 0; delegate==null && j < guesses.length; j++) 
        {
          String fName = mdir;
          if (guesses[j].length() > 0)       
            fName = guesses[j] + RiTa.OS_SLASH + fName;    
          
          if (DBUG_LOADS) System.out.print("[INFO] Trying "+fName+"...");          
          delegate = initDelegate
            (useTagDict, useCaseInsensitiveTagDict, beamSize, advancePercentage, fName);
          if (DBUG_LOADS) System.out.println(delegate==null?" failed":" OK");
          
          if (delegate == null) 
          {
            fName += "parser"+RiTa.OS_SLASH;
            if (DBUG_LOADS) System.out.print("[INFO] Trying "+fName+"...");
            delegate = initDelegate          
              (useTagDict, useCaseInsensitiveTagDict, beamSize, advancePercentage,fName);
            if (DBUG_LOADS) System.out.println(delegate==null?" failed":" OK");               
          }
        }
      }
    }
    catch (OutOfMemoryError e) {
        throw new RiTaException("Out of memory! Increase the" +
          " memory size in the Processing preferences\n          or pass" +
          " -Xmx384m as an argument to Java (replace 384 with the # of MBs" +
          "\n          you want to use), or use the RiTaServer instead...");                          
    }
    if (delegate == null)
      throw new RiTaException("[ERROR] Unable to create parser Parser instance!"+ERROR_MSG);
    
    if (DBUG_LOADS && (!RiTa.SILENT))
      System.out.println("[INFO] Loaded parser in "+RiTa.elapsed(start)+"s");;
  }

  private Parser initDelegate(boolean useTagDict, boolean useCaseInsensitiveTagDict, 
    int beamSize, double advancePercentage, String mDir) 
  {
    try {
      return TreebankParser.getParser(mDir, useTagDict, 
        useCaseInsensitiveTagDict, beamSize, advancePercentage);
    } catch (IOException e) {      
      return null;
    }
  }

  /**
   * Returns the String of the most probable parse 
   * using Penn Treebank-style formatting. 
   */
  public String parse(String text)
  {   
    return parse(text, false);
  }    
  
  /**
   * Returns the String of the most probable parse using Penn 
   * Treebank-style formatting with or without indents 
   * depending on the paramater <code>indent</code>.
   */
  public String parse(String text, boolean indent)
  {
    Parse[] ps = getParses(text, 1);
    if (ps == null || ps.length<1)
      return null;
    return parseToString(ps[0], indent);
  }

  public Parse[] getParses(String text, int numParses)
  {
//System.out.println("Parser.getParses()");
    if (RiTa.endsWith(text, new char[] {'!','.','?'}))
      text = text.substring(0, text.length()-1);
    
    this.tokens = RiTa.tokenize(text);
    
    Parse p = new Parse(text, new Span(0, text.length()), 
      AbstractBottomUpParser.INC_NODE, 1, null); 

    int start = 0;
    for (int i = 0; i < tokens.length; i++)
    {
      Span sp = new Span(start, start +  tokens[i].length());
      Parse p2 = new Parse(text, sp, AbstractBottomUpParser.TOK_NODE, 0, i);
      try {
        p.insert(p2);
      }
      catch (InternalError e)
      {
        throw new RiTaException("Illegal insertion of " +
          "item (punctuation perhaps?) not contained in " +
          "the sentence: '"+tokens[i]+"'\n  Msg: '"+e.getMessage()+"'");
      }
      start +=  tokens[i].length() + 1;
    }   
    if (numParses == 1) {
      this.parses = new Parse[] { delegate.parse(p)};
    }
    else {
      this.parses = delegate.parse(p, numParses);
    }
    return parses;
  }

  public static String parseToString(Parse p, boolean indent)
  {
    StringBuilder sb = new StringBuilder(128);
    parseToString(p, sb, indent ? "":null, null);    
    return sb.toString().trim();  // hack
  }
  
  /**
   * Appends the specified string buffer with a string representation of this parse.
   * @param sb A string buffer into which the parse string can be appended. 
   */
  public static void parseToString(Parse p, StringBuilder sb, String indent, String toClose) {

    //System.out.println("CALL: "+p.getText()+": "+p.getChildCount());
    Span span = p.getSpan();
    String type = p.getType().toLowerCase();
    List parts = p.getParts();
    String text = p.getText();
    int start = span.getStart();
    
    if (parts.size() == 1) {
      Parse oc = (Parse)parts.get(0);
      if (oc.getChildCount()==1)          
        toClose = oc.getType(); 
    }
    
    if (!type.equalsIgnoreCase(AbstractBottomUpParser.TOK_NODE)) {
      sb.append("(").append(type);
      
      if (DBUG)System.out.print("1: '("+type);
      
      if (type.equals("TOP") || parts.size()>1) {
        if (indent != null) {          
          indent += "--";      
          //System.out.println("INCR indent="+indent.length());
          sb.append("\n").append(indent);
          if (DBUG)System.out.println("\\n"+indent+"' [lc="+lc+"]");
          lc += "\\n"+indent;
        }
        else {
          sb.append(" ");
          if (DBUG)System.out.println(" ' [lc="+lc+"]");
          lc += "("+type+" ";
        }
      }
      else {
        //noBreak = true;
        sb.append(" ");        
        if (DBUG)System.out.println(" ' [lc="+lc+"]");
        //System.out.println(" SKIP_BR: '("+type+" '");
        lc += "("+type+" ";
      }
    }     
    
    for (Iterator i = parts.iterator(); i.hasNext();) {
      Parse c = (Parse)i.next();
      Span s = c.getSpan();
      if (start < s.getStart()) {
        //String toAdd = text.substring(start, s.getStart());
        if (DBUG)System.out.println("2: '' [lc="+lc+"]");
        lc = "";
      }      
      parseToString(c, sb, indent, toClose);
      start = s.getEnd();
    }
    if (start < span.getEnd()) {
      String toAdd = text.substring(start, span.getEnd());
      if (!toAdd.equals(" ")) {
        sb.append(toAdd);
        if (DBUG)System.out.println("3: '"+toAdd+"' [lc="+lc+"]");
        lc = toAdd;
      }
    }
    //boolean skipDelete = false;
    if (!type.equalsIgnoreCase(AbstractBottomUpParser.TOK_NODE)) 
    {     
      if (lc.startsWith(")")) {
        if (indent != null) {          
          
          //System.out.println("DECR indent="+indent.length());          
          if (indent.length()>1) {
            if (!lc.equals(") ")) {
              indent = indent.substring(0, indent.length()-2);
              //sofar(sb);
              sb.delete(sb.length()-2, sb.length());
              //sofar(sb);
              //System.out.println("del2: "+s3);
            }
          }
        }         
        else 
          sb.delete(sb.length()-1, sb.length());        
      }            
      sb.append(")");      
      if (DBUG)System.out.println("4: ')'       lc='"+lc+"'"+" kids="+p.getChildCount()+" "+type+" close="+toClose);//  kids="+p.getChildCount() + " nobreak="+noBreak);
      if (indent != null) {
        if (toClose==null || !toClose.equals(type)) {
          sb.append("\n").append(indent);
          lc = ")\\n"+indent;
        }
        toClose = null;
      }
      else {        
        lc = ") ";
        sb. append(" ");
      }      
      if (DBUG)System.out.println("5: '\\n"+indent+"'     lc='"+lc+"'");//  kids="+p.getChildCount() + " nobreak="+noBreak);      
    }
  } static String lc = ""; static boolean DBUG=false;
  
  public static void main(String[] args)
  {
    String sent = "The doctor, a man of compassion, treated dogs"; // why does this fail?
    sent = "The black cat crossed my path";
    String parse = "(TOP (S (NP (DT The) (NN doctor)) (VP (VBD treated) (NP (NNS dogs)))))";
    
    if (1==1) {
      //RiTa.setModelDir("/Users/dhowe/Desktop/me-models/parser");
      MaxEntParser parser = new MaxEntParser();
      System.out.println(parser.parse(sent));
    }
    else {       
      //String s = "(TOP (S (NP (DT The) (JJ black) (NN cat)) (VP (VBD crossed) (NP (NP (DT the) (NN path)) (PP (IN of) (NP (DT the) (JJ large) (NN car))))))";
      System.out.println(parse);
      Parse p = Parse.parseParse(parse);      
      System.out.println(parseToString(p, true));
      System.out.println("===========================");
    }
  }


}// end
