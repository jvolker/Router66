package rita.support.grammar;

import rita.*;

public class CallbackHandler
{
  public Object callbackHandler = null;
  
  public CallbackHandler(Object callbackParent) {
    this.callbackHandler = callbackParent;
  }
  
  public String exec(RiGrammarX grammar, String term)
  {
    term = term.substring(RiGrammarX.EXEC_CHAR.length());

    String methodNm = null;
    int parenIdx = term.indexOf("()");

    if (parenIdx > -1)
    {
      term = term.substring(0, parenIdx);
      methodNm = stripQuotes(term);
      // System.out.println("INVOKE="+methodNm);
      return RiTa.invoke(callbackHandler, methodNm).toString();
    }

    parenIdx = term.indexOf('(');
    if (parenIdx < 0)
    {
      throw new RiTaException("Invalid exec string: "
          + "does not contain open-paren, str=" + term);
    }
    methodNm = term.substring(0, parenIdx);

    // System.out.println("METH="+methodNm);

    String argsStr = term.substring(parenIdx + 1);
    int closeIdx = argsStr.indexOf(RiGrammarX.EXEC_POST);
    if (closeIdx < 0)
      throw new RiTaException("Invalid exec string: " + "does not end with "
          + RiGrammarX.EXEC_POST + " str=" + argsStr);
    argsStr = argsStr.substring(0, closeIdx);

    // System.out.println("ARGS="+argsStr);

    String[] strs = argsStr.split(",");
    Object[] args = new Object[strs.length];

    for (int i = 0; i < strs.length; i++)
    {
      strs[i] = strs[i].trim();
      if (!strs[i].startsWith("\""))
      {
        if (grammar.hasRule(strs[i]))
        {
          StringBuilder tmp = new StringBuilder();
       
          
        // ------------------------------------------------          
        //NEED TO RE-ADD expandrule BELOW!
        // ------------------------------------------------
          
          
          args[i] = grammar;//.expandRule(strs[i]);   
          //= tmp.toString().trim();
        }
        else
        {
          // handle primitives
          try
          {
            args[i] = Boolean.parseBoolean(strs[i]);
          }
          catch (Exception e) {}
          
          try
          {
            args[i] = Integer.parseInt(strs[i]);
          }
          catch (Exception e) {}
          
          try
          {
            args[i] = Float.parseFloat(strs[i]);
          }
          catch (Exception e) {}
          
          try
          {
            args[i] = Long.parseLong(strs[i]);
          }
          catch (Exception e) {}
          
        }
      }
      else
        args[i] = stripQuotes(strs[i]).trim();
    }
    // System.out.println("INVOKE: "+methodNm+"("+RiTa.asList(args)+")");
    Object result = RiTa.invoke(callbackHandler, methodNm, args);
    return result == null ? "" : result.toString();
  }

  private static String stripQuotes(String term)
  {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < term.length(); i++)
    {
      char c = term.charAt(i);
      if (c != '\'' && c != '\"' && c != '`') // smart-quotes?
        sb.append(c);

    }
    return sb.toString();
  }
  
}
