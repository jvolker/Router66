package rita;

import java.io.ByteArrayInputStream;


import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import processing.core.PApplet;
import rita.support.grammar.Definition;
import rita.support.grammar.Grammar;
import rita.support.ifs.RiGrammarIF;

/*TODO:
 Handle unicode in applets: 
   -- http://download.oracle.com/javase/tutorial/i18n/text/convertintro.html 
   -- http://www.ssec.wisc.edu/~tomw/java/unicode.html
 add noDups flag for generateX
 */

/**   
 * Implementation of a (probabilistic) context-free grammar (with specific 
 * literary extensions) that performs generation from user-specified grammars.<pre> 
    RiGrammar rg = new RiGrammar(this, "mygrammar.g");
    System.out.println(rg.expand());</pre>
 *   
 * RiTa grammar files are plain text files (generally ending with the '.g'
 * extension and residing in the 'data' folder) that follow the format below:
 *  <pre>    {
      &lt;start&gt;
      &lt;rule1&gt; | &lt;rule2&gt; | &lt;rule3&gt;
    }

    {
      &lt;rule2&gt;
      terminal1 | 
      terminal2 | &lt;rule1&gt;
      # this is a comment 
    }
    ...</pre>   
 * <b>Primary methods of interest:</b>
 * <ul>
 * <li><code>expand()</code> which simply begins at the &lt;start&gt; state and 
 * generates a string of terminals from the grammar.<p>
 * <li><code>expandFrom(String)</code> which begins with the argument
 * String (which can consist of both non-terminals and terminals,) 
 * and expands from there. Notice that <code>expand()</code> is simply
 * a convenient version of <code>expandFrom("&lt;start&gt;");</code>.<p>
 * <li><code>expandWith(String, String)</code> takes 2 String arguments, the 1st 
 * (a terminal) is guaranteed to be substituted for the 2nd (a non-terminal). Once this 
 * substitution is made, the algorithm then works backwards (up the tree from the leaf)
 * ensuring that the terminal (terminal1) appears in the output string. 
 * For example, with the grammar fragment above, one might call:<p>
 * <pre>
 *      grammar.expandWith(terminal1, "&lt;rule2&gt;");
 * </pre>
 * assuring not only that <code>&lt;rule2&gt;</code>will be used at least 
 * once in the generation process, but that when it is, it will be replaced 
 * by the terminal "hello".
 *</ul>
 *<b>Other items of note:</b><ul>
 *<li>Grammar files should be plain-text files placed in the 'data' directory of a Processing sketch, 
 *along with images, fonts, and other resources.<p>
 *
 *<li>A RiGrammar object will assign (by default) equal weights to all choices in a rule. 
 *One can adjust the weights by adding 'multipliers' as follows: (in the rule below,
 * 'terminal1' will be chosen twice as often as the 2 other choices.
 * <pre>   {
     &lt;rule2&gt;
     [2] terminal1 | 
     terminal2 | &lt;rule1&gt; 
   }</pre> 
   <li>The RiGrammar object supports callbacks, from your grammar, back into your Java code.
 * To generate a callback, add a method call in your grammar, surrounded by back-ticks, as follows:
 * <pre>   
 *     {
 *       &lt;rule2&gt;
 *       The cat ran after the `getRhyme("cat");` |
 *       The &lt;noun&gt; ran after the `pluralize(&lt;noun&gt;);` 
 *     }</pre>
 *     
 * Any number of arguments may be passed in a callback, but for each call,
 * there must be a corresponding method(with the same number and type or
 * arguments) in the sketch, e.g.,
 * 
 * <pre>
 *    String pluralize(String s) {
 *      ...
 *    }
 * </pre>
 * 
 * @author dhowe 
 */
public class RiGrammar extends RiObject implements RiGrammarIF
{ 
  /** @invisible */
  public static final String DEFAULT_LINEBREAK = "&break;";
  
  /** @invisible */
  public static final String DEFAULT_INDENT= "";    
  
  static final boolean DBUG = false;
    
  private static final String GT = ">", LT = "<";
  
  /** @invisible                */
  public Grammar grammar;

  private RiGrammarEditor grammaEditor; 
  
    /** @invisible                */
  public RiGrammar() 
  {
    this(null, (String)null);
  }
  
  /** @invisible                */
  public RiGrammar(String grammarFileName) 
  {
    this(null, grammarFileName);
  }

  /**
   * Specify a grammar file conforming to the example above.<br>
   * To specify a rule multiplier, prepend it with the desired
   * # in square brackets:
   * <pre>
   * {
   *   <rule>
   *   <choice1> |  
   *   [2] <choice2> | <choice3>
   * }
   * </pre>
   * In the above case, <choice2> will be selected 50% of 
   * the time, while <choice1> and <choice3> will each be
   * selected 25% of the time.
   * 
   * @param grammarFileName grammar file in the 'data' directory 
   */
  public RiGrammar(PApplet parent, String grammarFileName) 
  {
    super(parent);
    if (grammarFileName != null) {      
      //InputStream is = RiTa.openStream(parent, grammarFileName);
      InputStream is = null;
      if (parent == null)
        is = RiTa.openStream(parent, grammarFileName);
      else {        
        is = parent.createInput(grammarFileName);
      }
      if (is == null) 
        throw new RiTaException("Null input stream for file: "+grammarFileName);
      this.grammar = new Grammar(parent, is, grammarFileName);
    }
  }  
  
  public String getGrammarFileName() {
    return grammar.getGrammarFileName();
  }
  
  /**
   * Initialize a RiGrammar object with no grammar data.
   */
  public RiGrammar(PApplet parent) 
  {
    this(parent, (String)null);
  }
  
  // Methods ====================================================
  
  /**
   * @deprecated
   * @invisible
   * @see #setGrammarFromString(String)
   */
  public void setGrammar(String grammarFileAsString)
  { 
    this.setGrammarFromString(grammarFileAsString);
  }
  
  /**
   * Resets the contents of the current buffer 
   * @invisible
   */
  public void setBuffer(CharSequence buffer) {
    grammar.setBuffer(buffer);
  }
  
  /**
   * Return all the text generated since the last call to any of the expand() methods.
   */
  public String getBuffer() {
    return grammar.getBuffer();
  }
  
  /**
   * Initializes a grammar from a String containing the rules (rather than a file),
   * replacing any existing grammar. 
   */
  public void setGrammarFromString(String grammarRulesAsString)
  {  
    grammar = new Grammar(getPApplet(), new ByteArrayInputStream
      (grammarRulesAsString.getBytes()), grammarRulesAsString);    
  }
  
  /**
   * Loads a grammar from the file specified by <code>grammarFileName</code>,
   * replacing any existing grammar file.
   */
  public void loadGrammarFile(String grammarFileName)
  {  
    grammar = new Grammar(getPApplet(), RiTa.openStream
      (getPApplet(), grammarFileName), grammarFileName);    
  }
  
  /**
   * Prints the definition map to the console. 
   */
  public void dumpDefinitions()
  {  
    grammar.dumpDefinitions();
  }
  
  /**
   * Returns a Map<String,String> of the current Definitions, with names as keys 
   * and String-representations of each Definition as values. 
   */
  public Map getDefinitions()
  {  
    return grammar.getDefinitions();
  }
  
  /**
   * Gets a production definition by name
   */
  public Definition getDefinition(String name)
  {
    return grammar.getDefinition(verifyName(name));
  }
  
  /**
   * Adds a production definition by name, replacing
   * the existing one it if it exists.
   * @deprecated
   * @invisible
   * @see RiGrammar#setDefinition(String, String)
   */
  public void addDefinition(String name, String def)
  {
    name = verifyName(name);
    Vector v = parseDefinitionString(def);
    grammar.addDefinition(name, v);
  }
  
  /**
   * Adds a production definition by name, replacing
   * the existing one it if it exists.
   */
  public void setDefinition(String name, String def)
  {
    name = verifyName(name);
    Vector v = parseDefinitionString(def);
    grammar.addDefinition(name, v);
  }
  
  private String verifyName(String name)
  {
    if (!name.startsWith(LT)) name = LT + name;
    if (!name.endsWith(GT)) name += GT;
    return name;
  }
  
  private Vector parseDefinitionString(String def)
  {
    Vector v = new Vector();
    String[] strs = def.split("\\|");
    for (int i = 0; i < strs.length; i++) {
      Vector words = new Vector();
      String[] wordArr = strs[i].split(" ");
      for (int j = 0; j < wordArr.length; j++) {
        if (wordArr[j] != null && wordArr[j].length()>0)
          words.addElement(wordArr[j].trim());
      }
      if (words.size()>0)
        v.addElement(words);
    }
    return v;
  }

  /**
   * Expands a grammar from its '&lt;start&gt;' symbol 
   * one or more times.
   * @param numTimes # of times to do the expansion  
   * @return String[] one element for each expansion
   */
  public String[] expand(int numTimes)
  {
    String s[] = new String[numTimes];
    for (int i = 0; i < numTimes; i++) {
      s[i] = grammar.expand(false);
    }
    return s; 
  }
  
  /**
   * Expands a grammar from its '&lt;start&gt;' symbol 
   */
  public String expand()
  {    
    return grammar.expand(false);
  }  
  
  /**
   * Expands a grammar from its '&lt;start&gt;' symbol
   * using a temporary buffer when <code>preserveBuffer</code> 
   * is true.
   * @param preserveBuffer when true, will use a temporary buffer and leave the current
   * buffer unchanged
   */
  public String expand(boolean preserveBuffer)
  {    
    return grammar.expand(preserveBuffer);
  }     

  /**
   * Expands the grammar from the given symbol, using a temporary buffer
   * when <code>preserveBuffer</code> is true.
   * @param preserveBuffer when true, will use a temporary buffer and leave the current
   * buffer unchanged 
   */
  public String expandFrom(String toExpand, boolean preserveBuffer)
  {
    return grammar.expand(toExpand, preserveBuffer);
  }   
  
  /**
   * Expands the grammar, starting from the given symbol.<br>
   * RiGrammar.expand() is equivalent to RiGrammar.expandFrom('<start>').  
   */
  public String expandFrom(String toExpand)
  {
    return grammar.expand(toExpand, false);
  }
  
  /**
   * Expands the grammar after replacing an instance of the non-terminal 
   * <code>productionName</code> with the terminal in <code>literalString</code>.<P>
   * This method guarantees that <code>literalString</code> will be present 
   * in the generated output, assuming at least one instance of <code>productionName</code>  
   * exists in the grammar.
   * 
   * @param preserveBuffer when true, will use a temporary buffer and leave the current
   * buffer unchanged
   * 
   * @exception RiTaException if <code>productionName</code> is not found
   * in the grammar.
   */  
  public String expandWith(String literalString, String productionName)
  {    
    return grammar.expandWith(literalString, productionName, false);
  }
  public String expandWith(String literalString, String productionName, boolean preserveBuffer)
  {    
    return grammar.expandWith(literalString, productionName, preserveBuffer);
  }  
  
  /** 
   * Tells the grammar whether or not to add spaces
   * between the terminals that are output (default=true).
   */
  public void setIncludeSpaces(boolean includeSpaces)
  {    
    this.grammar.setInsertSpaces(includeSpaces);
  }
  
  /** 
   * Tells the grammar what String to use for line-breaks
   * default is '&break;'.
   * @invisible
   */
  public void setLineBreakCharacter(String linebreak)
  {
    this.grammar.setLineBreak(linebreak);
  }
 
  
  /**
   * The exec() mechanism is now enabled by default so this method has no effect.
   * To disable execs, you can call RiGrammar.disableExec().
   * @invisible
   * @deprecated
   * @see #disableExec()
   */
  public static void setExecEnabled() {
    Grammar.setExecEnabled(true);
  }
  
  /**
   * Call if you want to disable the exec() mechanism for callbacks (they are enabled by default). 
   * Useful if you want to include backticks or method calls as terminals 
   * in your grammar.<p>
   * Note: this must be called before loading a grammar file.
   * @invisible
   */
  public static void disableExec() {
    Grammar.setExecEnabled(false);
  }
  
  /**
   * Returns the state of the execEnabled flag. If this is false,
   * callbacks in the grammar will be ignored.
   * @invisible
   */
  public static boolean isExecEnabled() {
    return Grammar.isExecEnabled();
  }

/*  *//**
   * Sets the object which will receive callbacks from the grammar via the 'exec'
   * mechanism (defaults to the PApplet passed in to the constructor).<P>
   * @see RiGrammar#setExecEnabled()
   * @invisible
   *//*
  public void setCallbackHandler(Object callbackHandler) {
    this.grammar.setCallbackHandler(callbackHandler);    
  }
    
  *//**
   * Returns the current callback handler for 'exec' calls
   * @invisible
   *//*
  public Object getCallbackHandler() {
    return grammar.getCallbackHandler();
  }
  */
  
  public void closeGrammarEditor() {
    if (grammaEditor != null)
      grammaEditor.setVisible(false);
  }
  
  /**
   * Provides a live, editable view of a RiGrammar text file
   * that can be dynamically loaded into a sketch without
   * stopping and restarting it. 
   */
  public RiGrammarEditor openGrammarEditor() {
      return openGrammarEditor(800, 600);
  }
  
  /**
   * Provides a live, editable view of a RiGrammar text file
   * that can be dynamically loaded into a sketch without
   * stopping and restarting it. 
   */
  public RiGrammarEditor openGrammarEditor(int width, int height) {
    if (grammaEditor == null) {
      grammaEditor = new RiGrammarEditor(getPApplet(), this);
    }
    grammaEditor.setSize(width, height);
    grammaEditor.setVisible(true);
    return grammaEditor;
  }
  
  /** @invisible  */
  public static void mainX(String[] xxx) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException 
  {
    /*Test t = new Test();
    Method me = t.getClass().getMethod("methodMissing",  new Class[]{String.class, Object[].class});
    /Object[] ob = {"name",new Object[]{"test"}};
    System.out.println(me.invoke(t, ob));
    if (1==1) return;*/
    PApplet cb = new PApplet() {
      public String noargs() {
        System.out.println("Main.noargs()");
        return "(noargs)";
      }
      public String appendColor(String s) {
        //System.out.println("Main.appendColor("+s+")");
        return "red "+s;
      }
      public String appendColors(String s,String t) {
        //System.out.println("Main.appendColors("+s+","+t+")");
        return "red "+s+" or a blue "+t;
      }
      public String args(String s) {
        System.out.println("Main.args("+s+")");
        return "(args-"+s+")";
        //return "<object>";        
      }
      public String args(String s, String t) {
        //System.out.println("args("+s+",'"+t+"')");
        return "args";
      }
      public Object methodMissing(String name, Object[] o) {
        //System.out.println("Main.methodMissing("+o+")");
        return "(Main.methodMissing"+RiTa.asList(o)+")";
      }
    };
    /*Method m = cb.getClass().getMethod("methodMissing",  new Class[]{String.class, Object[].class});
    Object[] o = {"mm", new Object[]{"test"}};
    System.out.println(m.invoke(cb, o));
    if (1==1) return;*/
    
    RiGrammar rg = new RiGrammar();
    rg.setGrammarFromString("");
    for (int i = 0; i < 20; i++)
      System.out.println(rg.expand());//With("runs","<verb>"));
    
   /* 
    Grammar g = rg.grammar;
    g.dumpDefinitions();
    Definition verbDef = rg.getDefinition("<verb>");
    verbDef.clearRules();
    verbDef.addRule(new String[]{"sighs","<adverb>"}, 2);
    verbDef.addRule(new String[]{"appears","like", "a", "<object>"});
    System.out.println(verbDef);*/
    //rg.setDefinition("<verb>", new Definition());
    
/*    RiGrammar p = new RiGrammar(null, "examples/poem.g");
    for (int i = 0; i < 10; i++)
      System.out.println(p.expand());
    p.grammar.dumpDefinitions();
    System.out.println(p.grammar.getDefinition("<verb>"));*/
    
    //p.dumpDefinitions();

  /*  String[] expandTokens = { "dog" ,    "cats",   // NN,NNS
                              "licked", // "believing",  // VBD, VBG
                              "filthy" , "roughly" // JJ, RB 
                            };
    System.out.println(p.expand());*/
    //String[] expandTokens = {"I","lick","the","apple"};
//    System.out.println("--------------------------------------");
//    for (int i = 0; i < expandTokens.length; i++)  {     
//      System.out.println("*** "+expandTokens[i]+" -> "+PosTagger.getInstance().tag(expandTokens[i]));
//      for (int j = 0; j < 10; j++) 
//        System.out.println(p.expandFrom(expandTokens[i])); // NN
//      System.out.println("--------------------------------------");
//    }    
  }
    
  public static void main(String[] args) {
    
    String target = "sighs";
    RiGrammar rg = new RiGrammar("poem.g");
    Map defs = rg.getDefinitions();
    for (Iterator iterator = defs.keySet().iterator(); iterator.hasNext();)
    {
      String rule = (String) iterator.next();
      String def = (String) defs.get(rule);
      if (def.contains(target))
        System.out.println("found "+target+" in rule: "+rule);
    }
    //System.out.println("\n\n"+rg.getDefinitions());
    /*//rg.loadGrammarFile("poem.g");
    for (int i = 0; i < 10; i++) {
     //System.out.println(i+") "+rg.expand());
     System.out.println(i+") "+rg.expandWith("wan ", "<adverb>")); 
    }*/
  }

  
}// end

