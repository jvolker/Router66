package rita;

import java.io.*;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import processing.core.PApplet;
import rita.*;
import rita.support.MultiMap;
import rita.support.grammar.RuleList;
import rita.support.ifs.RiGrammarIF;

// TODO: exec mechanism??

/**   
 * Implementation of a (probabilistic) context-free grammar (with specific 
 * literary extensions) that performs generation from user-specified grammars.<pre> 
    RiGrammar rg = new RiGrammar(this, "mygrammar.g");
    String result = rg.expand();
    System.out.println(result);</pre>
 *   
 * RiTa grammar files are plain text files (generally ending with the '.g'
 * extension and residing in the 'data' folder) that follow the format below 
 * (&lt; and &gt; are optional):
 *  <pre> 
      &lt;start&gt; => &lt;rule1&gt; | &lt;rule2&gt; | &lt;rule3&gt;
 
      &lt;rule2&gt; => terminal1 | terminal2 | &lt;rule1&gt;  

      &lt;rule3&gt; => terminal3 | terminal4 \
      terminal5 | terminal6
      
      # this is a comment 
    }
    ...</pre>   
 *   
 * <b>Primary methods of interest:</b>
 * 
 * <ul>
 * <li><code>expand()</code> which simply begins at the &lt;start&gt; state and 
 * generates a string of terminals from the grammar.<p>
 * 
 * <li><code>expandFrom(String)</code> which begins with the argument
 * String (which can consist of both non-terminals and terminals,) 
 * and expands from there. Notice that <code>expand()</code> is simply
 * a convenient version of <code>expandFrom("&lt;start&gt;");</code>.<p>
 * 
 * <li><code>expandWith(String, String)</code> takes 2 String arguments, the 1st 
 * (a terminal) is guaranteed to be substituted for the 2nd (a non-terminal). Once this 
 * substitution is made, the algorithm then works backwards (up the tree from the leaf)
 * ensuring that the terminal (terminal1) appears in the output string. 
 * For example, with the grammar fragment above, one might call:<p>
 * 
 * <pre>
 *      grammar.expandWith(terminal1, "&lt;rule2&gt;");
 * </pre>
 * 
 * assuring not only that <code>&lt;rule2&gt;</code>will be used at least 
 * once in the generation process, but that when it is, it will be replaced 
 * by the terminal "hello".
 *</ul>
 *
 *<b>Other items of note:</b><ul>
 *
 *<li>Grammar files should be plain-text files placed in the 'data' directory of a Processing sketch, 
 *along with images, fonts, and other resources.<p>
 *
 *<li>A RiGrammar object will assign (by default) equal weights to all choices in a rule. 
 *  One can adjust the weights by adding 'probabilities' as follows: (in the rule below,
 * 'rule1' will be chosen half as often as the 2 other choices).
 * <pre>    
   &lt;start&gt; => &lt;rule1&gt; [0.5] | &lt;rule2&gt; | &lt;rule3&gt;
 </pre> 
   <li>The RiGrammar object supports callbacks, from your grammar, back into your Java code.
 * To generate a callback, add a method call in your grammar, surrounded by back-ticks, as follows:
 * <pre>   
 *       &lt;rule2&gt; => \
 *       The cat ran after the `getRhyme("cat");` | \
 *       The &lt;noun&gt; ran after the `pluralize(&lt;noun&gt;);` 
 *     }</pre>
 *     
 * Any number of arguments may be passed in a callback, but for each call,
 * there must be a corresponding method (with the same number and type or
 * arguments) in the sketch, e.g.,
 * 
 * <pre>
 *    String pluralize(String s) {
 *      ...
 *    }
 * </pre>
 * 
 * @author dhowe 
 * @invisible
 */
public class RiGrammarX extends RiObject implements RiGrammarIF
{
  public static final String PROB_PATTERN = "(.*[^ ]) *\\[([^]]+)\\](.*)";

  public static final String EXEC_CHAR  = "`";
  public static final String EXEC_POST = ");"+EXEC_CHAR; // semi-colon optional?
  
  public static final String ENCODING = "UTF-8", START = "<start>";
  
  private PApplet myParent;
  
  /** @invisible */
  public RuleList rules;
  public int maxIterations = 100;
  public String startRule = START;
  public Pattern probabilityPattern;
  private Object callbackReceiver;
  private String fileName;
  
  /** @invisible */
  public RiGrammarEditor editor;

  /**
   * New/experimental version of RiGrammar with simpler syntax...
   * @invisible
   */
  public RiGrammarX(PApplet parent)
  {
    this(parent, null);
  }
  
  
  public RiGrammarX(PApplet parent, String grammarFileName) {
    super(parent);
    this.fileName = grammarFileName;
    this.rules = new RuleList();
    MultiMap mp = new MultiMap(parent, grammarFileName);
    setGrammarFromProps(mp);
  }
  
  public String expandWith(String literalString, String ruleName)
  {
    // make sure the rule exists in the grammar
    if (!hasRule(ruleName))
      throw new RiTaException(ruleName+" not found in current rules: "+rules);
    
    return "Not yet implemented...";
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
    if (editor == null) {
      editor = new RiGrammarEditor(getPApplet(), this);
    }
    editor.setSize(width, height);
    editor.setVisible(true);
    return editor;
  }

  public void addRule(String name, String rule)
  {
    addRule(name, rule, 1);
  }

  public String getRule(String name)
  {
    return rules.getRule(name);
  }

  public boolean hasRule(String name)
  {
    return rules.hasRule(name);
  }

  // returns null if no expansion can be found
  String expandRule(String production)
  {
    for (Iterator it = rules.iterator(); it.hasNext();)
    {
      String name = (String) it.next();
      //System.out.println("  search->'"+name+"'");
     
      int idx = production.indexOf(name);
      if (idx >= 0)  
      {
        String pre = production.substring(0, idx);
        String expanded = getRule(name);
        String post = production.substring(idx+name.length());
        return pre + expanded + post;
      }
    }
    
    /*
     
    Here we check for an exec() call..
     
    int idx = production.indexOf(backTickedCall);
    if (idx >= 0)  
    {
      String pre = production.substring(0, idx);
      String post = production.substring(idx+name.length());
    
      if so, do it and substitute the result:
      -------------------------

         Object o = getCallbackHandler();
         if (o == null)
           throw new RiTaException("No valid callback handler found!");
         CallbackHandler ch = new CallbackHandler(o);
         String expanded = ch.exec();
        
         return pre + expanded + post;
         
    */
    
    return null;
  }

  public String expandFrom(String rule)
  {
    if (!rules.hasRule(rule))
      throw new RiTaException("Definition not found: "+rule+"\nRules:\n"+rules);
    
    int iterations = 0;
    while (++iterations < maxIterations)
    {
      String next = expandRule(rule);
      if (next == null) break;
      rule = next;
    }
    
    if (iterations >= maxIterations) 
      System.out.println("[WARN] max number of iterations reached: "+maxIterations);
    
    //System.out.println("# Iterations="+iterations);
    
    return rule;  
  }
  
  private Object getCallbackHandler()
  {
    return callbackReceiver == null ? getPApplet() : callbackReceiver;
  }
  
  public void setCallbackHandler(Object handler)
  {
    this.callbackReceiver = handler;
  }

  public String expand()
  {
    return expandFrom(START);
  }

  public void reset()
  {
    rules.clear();
  }

  public String toString()
  {
    return rules.toString();
  }
  
  public void setGrammarFromString(String grammarRulesAsString)
  {  
    MultiMap m = new MultiMap();
    m.loadFromString(grammarRulesAsString);
    this.setGrammarFromProps(m);
  }

  public void setGrammarFromProps(MultiMap grammarRules) 
  {
    this.rules.clear();

    for (Iterator iterator = grammarRules.keySet().iterator(); iterator.hasNext();)
    {
      String key = (String) iterator.next();
      String[] rules = (String[]) grammarRules.get(key);
      for (int j = 0; j < rules.length; j++)
      {
        addRule(key, rules[j]);
      }
    }
  }


  public void addRule(String key, String rule, float prob)
  {
    //System.out.println("RiGrammarX.addRule("+key+", "+rule+")");
    
    //String[] parts = rule.split("\\|");
    String[] parts = rule.split("\\s*\\|\\s*"); 
    for (int i = 0; i < parts.length; i++)
    {
      String part = parts[i];
      float weight = prob;
      if (part != null && part.trim().length() > 0)
      {
        if (probabilityPattern == null)
        {
          probabilityPattern = Pattern.compile(PROB_PATTERN);
        }
        Matcher m = probabilityPattern.matcher(part);
        if (m.matches())
        {
          if (m.groupCount() == 3)
          {
            String probStr = m.group(2);
            // nothing after the weight is allowed
            part = m.group(1) + m.group(3);
            String ignored = m.group(3);
 /*           if (ignored != null && ignored.trim().length() > 0) {
              System.err.println("[WARN] Ignoring characters '"+ignored+
                "' after probability tag ["+probStr+"]\n  for rule: "+part);
            }*/
            weight = Float.parseFloat(probStr);
          }
          else
          {
            System.err.println("[WARN] Invalid rule: " + part + " -> " + m.groupCount());
          }
        }
        // System.out.println("addRule("+key+","+ part+","+prob+");");
        rules.addRule(key, part, weight);
      }
    }
  }
 
  
  public void setGrammarFromProperties(Properties grammarRules) 
  {
    this.rules.clear();
    
    String key, rule;
    for (Map.Entry<Object, Object> propItem : grammarRules.entrySet())
    {
      key = (String) propItem.getKey();
      rule = (String) propItem.getValue();
      addRule(key, rule);
    }
  }


  public String getGrammarFileName()
  {
    return fileName;
  }
  
  public static void mainLSys(String[] args)
  {
    RiGrammarX grammar = new RiGrammarX(null);
    grammar.addRule("<start>", "X");
    grammar.addRule("X", "+ F X - - F Y +",2f);
    grammar.addRule("X", "c", .01f);
    grammar.addRule("Y", "- F + + F - ");
    int num = 0, tries = 1000;
    for (int i = 0; i < tries; i++)
    {
      String gr = grammar.getRule("X");
      if (gr.equals("c")) num++;
     // System.out.println(i+", "+gr);
    }
    String result = grammar.expand();
    System.out.println("NumP=" + num/(float) tries);
  }
   
  public static void main(String[] args) throws IOException
  {
/*    if (1==1) {
      String rule = "asd | dfg | dfg";
      String[] parts = rule.split("(\\s*)\\|(\\s*)"); 
      for (int i = 0; i < parts.length; i++)
      {
        System.out.println(i+") '"+parts[i]+"'");
      }
      return;
    }*/
    if (1==1) {
      RiGrammarX g = new RiGrammarX(null, "poem3.g");
      System.out.println();
      for (int i = 0; i < 10; i++)
      {
        System.out.println(i+") "+g.expandFrom("<x>"));
      }
    }
    else {
      RiGrammarX g = new RiGrammarX(null);//, "poem2.g");
      g.addRule("<x>", "<y> [2.1] | <z> check <z>", 2);
      g.addRule("<x>", "<end>", 1.5f);
      g.addRule("<y>", "Y <end>", .2f);
      g.addRule("<z>", "Z <end>", .8f);
      g.addRule("<end>", "THE END");
      System.out.println();
      System.out.println(g);
      System.out.println();
      for (int i = 0; i < 10; i++)
      {
        System.out.println(i+") "+g.expandFrom("<x>"));
      }
    }
  }

}// end
