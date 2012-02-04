// $Id: RiTaException.java,v 1.2 2011/05/26 13:57:59 dhowe Exp $

package rita;

import java.io.*;

/**
 * Simple tagged RuntimeException for library errors
 * @invisible
 */
public class RiTaException extends RuntimeException
{
  private static final String STATIC = "(static)";
  private static final String CLASS = "[CLASS]";
  private static final String ERROR = "[ERROR]";
  protected static final String SPC = " ", QQ = "", CR = "\n"; 

  public RiTaException() { super(); }

  public RiTaException(String message)
  {
    super(tagMessage(null, message));
  }
  
  public RiTaException(String message, boolean prependMsg)
  {
    super(tagMessage(null, message, prependMsg));
  }

  public RiTaException(Throwable cause)
  {
    super(cause);
  }
  
  public RiTaException(String message, Throwable cause)
  {
    super(tagMessage(null, message), cause);
  }
  
  public RiTaException(Object thrower, Throwable cause) 
  {
     super(tagMessage(thrower),cause); 
  }
  
  public RiTaException(Object thrower, String message, Throwable cause)
  {
    super(tagMessage(thrower, message), cause);
  }
  
  public RiTaException(Object thrower, String message)
  {
    super(tagMessage(thrower, message));
  }
  
  private static String tagMessage(Object thrower)
  {
    String msg = QQ;
    if (thrower != null) 
      msg += CR+CLASS+SPC+((thrower instanceof Class) 
      ? (thrower+SPC+STATIC) : (thrower.getClass()+QQ));  
    return msg;
  }

  private static String tagMessage(Object thrower, String msg)
  {
    return tagMessage(thrower, msg, true);
  }
  
  private static String tagMessage(Object thrower, String msg, boolean prepend)
  {    
    String versInfo = "RiTa.version ["+RiTa.VERSION+"] ";
    if (msg == null) return versInfo;
    if (msg.startsWith(ERROR+SPC))
      msg = msg.substring(ERROR.length()+1);// tmp   
    if (prepend) 
      msg = CR+SPC+SPC+ERROR+SPC+msg;
    msg += tagMessage(thrower);   
    return msg +" / "+versInfo;
  }

  public static String stackToString(Throwable aThrowable) {
    final Writer result = new StringWriter();
    final PrintWriter printWriter = new PrintWriter(result);
    aThrowable.printStackTrace(printWriter);
    return result.toString();
  }
  
  public static void main(String[] args)
  {
    throw new RiTaException("test error", new RuntimeException("root cause"));
  }
  

}// end
