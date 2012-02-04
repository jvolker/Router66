 // $Id: RiTa.java,v 1.15 2011/11/11 03:43:58 dhowe Exp $

package rita;

// JAVA 1.6 ONLY ====================================
//import java.awt.Desktop;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import processing.core.PApplet;
import processing.core.PGraphics;
import rita.support.EntityLookup;
import rita.support.FeaturedIF;
import rita.support.RiConstants;
import rita.support.RiProbable;
import rita.support.RiSplitter;
import rita.support.UnicodeInputStream;
import rita.support.Xmlable;
import rita.support.dyn.RiDynamicObject;
import rita.support.dyn.RiMessagable;
import rita.support.me.RiObjectME;
import rita.support.remote.RiClientStub;
import rita.support.remote.RiMethodMissing;
  
/*
 * REFACTOR:
 *   Fix RiStemmer, switch on noun | notNoun
 *   Remove stat models
 *   Add rotateTo() behavior;
 *   
 *   Fix methods on RiTaEvent (src/type/data)
 *   Remove extraneous methods in RiText
 *   Get rid of Processing style docs?  
 */

/* NEXT:
 * 
 *    ELIMINATE ALL BUT ONE OF ID_GEN
 *    
 *    Create JointMarkovModel (Pos/Word)
 *    Finish RiRealizer from SimpleNlg
 *    Fill out RiSubstitute with a filter interface (flags)
 *       
 * NEXT:      
 *   Add a RiSubstitute replaceWord(int type) method that handles
 *     word boundaries, caps, and compounds.. (see SubstituteTest).

 * -- Change dragging/selecting to work as a behavior (?) 
 * 
 * BEHAVIORS  
 * -- FallIn, FallOut, SpinLetters... 
 * -- IF for combining text behaviors? e.g., SpinFall(Spin+Fall)? 
 * 
 * $CVSHeader: RiTa/src/rita/RiTa.java,v 1.15 2011/11/11 03:43:58 dhowe Exp $
 */

/** 
 * A range of constants and (static) utility functions for the rita package
 */
public abstract class RiTa implements RiConstants
{
  /** 
   * The current version number for the RiTa package.<p>
   * This is the # output to the console when the first RiTa class is loaded. 
   */
  public static final String VERSION = "130";
  
  /**
   * Disables any console output from the RiTa packages...
   */
  public static boolean SILENT = false;    
  
  /** @invisible */
  private static long millisOffset = System.currentTimeMillis();
  
  // RiTa server ==========================================================  
  
  private static boolean serverEnabled;

  /** @invisible */   
  public static final int DEFAULT_SERVER_PORT = 4444;

  /** @invisible */
  public static int port = DEFAULT_SERVER_PORT;

  public static String textEncoding = "UTF-8";
  
  /** @invisible */
  public static final String QQ="";
  /** @invisible */
  public static final String BN="\n";
  /** @invisible */
  public static final String DOT=".";
  /** @invisible */
  public static final String COMMA=",";
  
  /** @invisible */
  public static final String SPC=" ";

  /** @invisible */
  public static String OS_SLASH="/", LINE_BREAK="\n";
  static {
    try {
      OS_SLASH = System.getProperty("file.separator");
    } 
    catch (Throwable e) {
      OS_SLASH = "/";
    }    
    try {      
      LINE_BREAK  = System.getProperty("line.separator");
    } 
    catch (Throwable e) {
      LINE_BREAK = "\n";
    }
  }
  
  // Server methods ======================================================
  
  /**
   * Clears all objects from the RiTaServer so that they can be reloaded
   */
  public static void refreshServer() {
    if (!isServerEnabled())
      throw new RiTaException("Server is not enabled! call RiTa.useServer() first");
    RiClientStub.getProxy().refreshServer();
  }   
   
  /**
   * Enables the RiTaServer on <code>host</code> (default=localhost) 
   * using <code>port</code> (default=4444). Note: one must still
   * start the server process (via the scripts provided). 
   */
  public static void useServer(String host, int port) {
    serverEnabled = true;
    RiClientStub.serverHost = host;
    RiClientStub.serverPort = port;
  }
  
  /**
   * Enables the RiTaServer on <code>port</code> (default=4444) 
   */
  public static void useServer(int port) {
    serverEnabled = true;
    RiClientStub.serverPort = port;
  }
     
  /**
   * Enables the RiTaServer on localhost using the default port
   */
  public static void useServer() {
    serverEnabled = true;
  }
  
  /**
   * Disables server processing for RiTa. Note: This does not kill the
   * server process itself if one is running.
   */
  public static void disableServer() {
    serverEnabled = false;
  }
  
  /** 
   * Returns whether RiTa will attempt to use the server for processing. 
   * Note: this does say mean that a server
   * process is actually running (or not).
   */
  public static boolean isServerEnabled() {
    return serverEnabled;
  }
  
  // Utility methods ======================================================

  
  /**
   * Takes a screenshot of the specified width and height (starting at x,y), writes it to a
   * JPG file, and returns the generated BufferedImage. Note: pass a null 'outfile' to 
   * skip writing the output image to a file. 
   * @invisible
   */
  public static BufferedImage captureScreen(String outFileName, int x, int y, int width, int height) {
    try
    {
      return captureScreen(outFileName, x, y, width, height, new Robot());
    }
    catch (Exception e)
    {
      throw new RiTaException(e);
    }
  }
  
  /**
   * Takes a screenshot of the specified width and height (starting in the upper left-hand corner)
   * @return 
   * @invisible
   */
  public static BufferedImage captureScreen(String outFileName, int width, int height) 
  {
    return captureScreen(outFileName, 0, 0, width, height);
  }
  
  /**
   * Takes a screenshot of the specified width and height (starting in the upper left-hand corner) 
   * @invisible
   */
  public static BufferedImage captureScreen(String outFileName) 
  {
    Dimension d = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
    return captureScreen(outFileName, 0, 0, (int)d.getWidth(), (int)d.getHeight());
  }

  // JAVA 1.6 ONLY ====================================
  /**
   * Converts a String containing UTF-X characters to their ASCII simplifications, e.g.
   * ü -> u, and é -> e.
   * @invisible
  public static String toAsci(String s)
  {
    return java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
  } */
  
  
  // JAVA 1.6 ONLY ====================================
  /**
   * Loads the URL in the default browser, waits 5 sec, then takes a screenshot 
   * of the desktop, and writes it to 'outfile' 
   * @invisible
 
  public static BufferedImage captureScreen(String outFileName, URL url) 
  {
    // allow time for browser & page to load..
    Robot robot = null;
    try
    {
      Desktop.getDesktop().browse(url.toURI());
      robot = new Robot();
    }
    catch (Exception e)
    {
      throw new RiTaException(e);
    }
    robot.delay(5000);
    Dimension d = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
    // send browser to full-screen?
    robot.keyRelease( KeyEvent.VK_F11 );
    return captureScreen(outFileName, 0, 0, (int)d.getWidth(), (int)d.getHeight(), robot);
  }  */
  
  private static BufferedImage captureScreen(String outFileName, int x, int y, int width, int height, Robot r) 
  {
    BufferedImage bi = null;
    try
    {
      if (outFileName != null && !outFileName.endsWith(".jpg")) 
        outFileName += ".jpg";
      // Capture the area of the screen defined by the rectangle
      bi = r.createScreenCapture(new Rectangle(x, y, width, height));
      if (outFileName != null)
        ImageIO.write(bi, "jpg", new File(outFileName));
    }
    catch (Exception e)
    {
      throw new RiTaException(e);
    }
    return bi;
  }
  
  // ----------------------------------------------------------
  
  /**
   * Packs an array of floats (1,2,3, or 4 elements) into a single integer. 
   * @invisible
   */
  public int pack(PApplet pApplet, float[] c) {
    switch (c.length) {
    case 1:
      return pApplet.color(c[0], c[0], c[0]);
    case 2:
      return pApplet.color(c[0], c[0], c[0], c[3]);
    case 3:
      return pApplet.color(c[0], c[1], c[2]);
    case 4:
      return pApplet.color(c[0], c[1], c[2], c[3]);
    default:
      throw new RiTaException("Illegal color value");
    }
  }
  
 /**
  * @invisible
  */
  public static int pack(int a, int r, int g, int b)
  {
    if (a > 255) a = 255; else if (a < 0) a = 0;
    if (r > 255) r = 255; else if (r < 0) r = 0;
    if (g > 255) g = 255; else if (g < 0) g = 0;
    if (b > 255) b = 255; else if (b < 0) b = 0;
    return (a << 24) | (r << 16) | (g << 8) | b;
  }
  
  /**
   * Unpacks a integer into an array of floats (size 4)
   * representing (a,r,g,b) color values
   * @invisible
   */
  public static int[] unpack(int pix)
  {
    int a = (pix >> 24) & 0xff;
    int r = (pix >> 16) & 0xff;
    int g = (pix >> 8) & 0xff;
    int b = (pix) & 0xff;
    return new int[] { a, r, g, b };
  }
  
 /**
  * @invisible
  */
  public static String escapeXml(String string) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0, len = string.length(); i < len; i++) {
        char c = string.charAt(i);
        switch (c) {
          case '\'':
            sb.append("&apos;");
            break;
          case '&':
              sb.append("&amp;");
              break;
          case '<':
              sb.append("&lt;");
              break;
          case '>':
              sb.append("&gt;");
              break; 
          case '"':
              sb.append("&quot;");
              break;
          default:
            sb.append(c);
        }
    }
    return sb.toString();
  }
  /**
  * @invisible
  */ 
  public static String unescapeXml(String xmlStr) {
    xmlStr = xmlStr.replaceAll("&apos;", "'");
    xmlStr = xmlStr.replaceAll("&amp;", "&");
    xmlStr = xmlStr.replaceAll("&lt;", "<");
    xmlStr = xmlStr.replaceAll("&gt;", ">");
    xmlStr = xmlStr.replaceAll("&quot;", "\"");
    return xmlStr;
  }  

  // Callback timer methods =====================================
  //private static RiText timerParent; 

  public static RiTextBehavior setCallbackTimer(Object pApplet, String timerName, float startOffset, float duration) {
    return setCallbackTimer(pApplet, timerName, startOffset, duration, true);
  }
  public static RiTextBehavior setCallbackTimer(Object pApplet, String timerName, float duration) {
    return setCallbackTimer(pApplet, timerName, duration, true);
  }
  public static RiTextBehavior setCallbackTimer(Object pApplet, float duration, boolean repeat) {
    return setCallbackTimer(pApplet, null, duration, repeat);
  }  
  public static RiTextBehavior setCallbackTimer(Object pApplet, float startOffset, float duration) {
    return setCallbackTimer(pApplet, null, startOffset, duration, true);
  }
  public static RiTextBehavior setCallbackTimer(Object pApplet, float startOffset, float duration, boolean repeat) {
    return setCallbackTimer(pApplet, null, startOffset, duration, repeat);
  }
  public static RiTextBehavior setCallbackTimer(Object pApplet, float duration) {
    return setCallbackTimer(pApplet, null, duration, true);
  }  
  public static RiTextBehavior setCallbackTimer(Object pApplet, String timerName, float duration, boolean repeat) {
    return setCallbackTimer(pApplet, timerName, 0, duration, repeat);
  }
  
  /*
   * Creates and returns a new  behavior that interpolates 
   * between 2 values, 'start' and 'target', and generates 
   * a callback to pApplet.onRiTaEvent(RiTaEvent) when finished.
   * @param pApplet The parent applet to call back to
   * @param start the start value
   * @param target the target value
   * @param startOffset Time before the timer starts (in seconds) 
   * @param duration The timer's period (in seconds)  
   * @see RiLerpBehavior#getValue()
   * @invisible
   */

  /**
   * Creates and returns a new callback timer which will
   * generate callbacks to pApplet.onRiTaEvent(RiTaEvent) 
   * according to the parameters specified.
   * @param pApplet The parent applet to call back to
   * @param timerName The name associated with this timer (optional)
   * @param startOffset Time before the timer starts (in seconds) 
   * @param duration The timer's period (in seconds)  
   * @param repeat Whether the timer fires just once, or repeats indefinitely (default=true)
   */
  public static RiTextBehavior setCallbackTimer(Object pApplet, String timerName, 
    float startOffset, float duration, boolean repeat) 
  {
    RiTimer rt = new RiTimer(pApplet, startOffset, duration, timerName, repeat);
    return rt.rtb;
  }   
  
  /** Resets the callback timer with the specified name */
  public static RiTextBehavior resetCallbackTimer
    (PApplet pApplet, String timerName, float startOffset, float duration)
  {
      return resetCallbackTimer(pApplet, timerName, startOffset, duration, true);
  }
  
  /** Resets the callback timer with the specified name */
  public static RiTextBehavior resetCallbackTimer(Object pApplet, String timerName, float startOffset, float duration, boolean repeat) {
      stopCallbackTimer(timerName);
      return setCallbackTimer(pApplet, timerName, startOffset, duration, repeat);
  }
    
  /** 
   * Pauses the callback timer with the specified name for 'pauseTime' seconds
   * @invisible 
   */
  public static void pauseCallbackTimer(Object pApplet, String timerName, float pauseTime) {
   RiTextBehavior tb = RiTa.getCallbackTimer(timerName);
   if (tb != null) {
     tb.pause(pauseTime);
   }
   else 
     System.out.println("[WARN] Unable to find timer with name: "+timerName);
  }
  
  /** 
   * Pauses (or unpauses) the callback timer with the specified name
   * @invisible  
   */
  public static void pauseCallbackTimer(Object pApplet, String timerName, boolean paused) {
   RiTextBehavior tb = getCallbackTimer(timerName);
   if (tb != null) {
     tb.setPaused(paused);
   }
   else 
     System.out.println("[WARN] Unable to find timer with name: "+timerName);
  }
  
  /** Stops all callback timers currently running */
  public static void stopCallbackTimers() {
    List l = RiTextBehavior.findByType(TIMER);
    for (Iterator iterator = l.iterator(); iterator.hasNext();)
    {
      RiTextBehavior rtb = (RiTextBehavior) iterator.next();
      rtb.stop();
    }
/*  if (timerParent != null) 
      timerParent.removeBehaviors();*/
  }
  
  /** Stops the callback timer with the specified name */
  public static void stopCallbackTimer(String timerName) {
    RiTextBehavior.findByName(timerName).stop();
  }
  
  /** Returns the callback timer with the specified name */
  private static RiTextBehavior getCallbackTimer(String timerName) {
    return RiTextBehavior.findByName(timerName);
  }

  // Sample loading methods ====================================

  public static RiSample loadSample(PApplet p, String sampleFileName) {
    return loadSample(p, sampleFileName, RiSample.MINIM, false);
  }    
  public static RiSample loadSample(PApplet p, String sampleFileName, int playerType) {
    return loadSample(p, sampleFileName, playerType, false);
  }  
  /**
   * Convenience method that loads a RiSample object and (if 'setLooping' is true)
   * starts it looping. 
   * 
   * @see RiTa#MINIM
   * @see RiTa#ESS
   * @see RiTa#SONIA 
   * 
   * @see #loadSample(PApplet, String, int)
   * @see #loadSample(PApplet, String)
   */
  public static RiSample loadSample(PApplet p, String sampleFileName, int playerType, boolean setLooping) 
  {
    RiSample sample = null;
    switch (playerType) {
      case RiSample.MINIM:
        sample = RiSample.create(p, RiSample.MINIM_SAMPLE_PLAYER);
        break;
      case RiSample.SONIA:
        sample = RiSample.create(p, RiSample.SONIA_SAMPLE_PLAYER);
        break;
      case RiSample.ESS:
        sample = RiSample.create(p, RiSample.ESS_SAMPLE_PLAYER);
        break;
      default:
        throw new RuntimeException("[ERROR] Invalid playerType: " + playerType);
    }
    try {
      if (setLooping)
        sample.loop(sampleFileName);
      else
        sample.load(sampleFileName);
    }
    catch (Exception e) {
      throw new RiTaException("Unable to load sample: "
        + sampleFileName + " from " + RiTa.cwd());
    }    
    return sample;
  }
  
  public static RiSample loopSample(PApplet p, String sampleFileName) {
    return loadSample(p, sampleFileName, RiSample.MINIM, true);
  } 
  /**
   * Convenience method that loads a RiSample object and starts it looping. 
   * 
   * @see RiTa#MINIM
   * @see RiTa#ESS
   * @see RiTa#SONIA 
   * 
   * @see #playSample(PApplet, String, int)
   */
  public static RiSample loopSample(PApplet p, String sampleFileName, int playerType) {
    return loadSample(p, sampleFileName, playerType, true);
  }
  
  public static RiSample playSample(PApplet p, String sampleFileName) {
    return playSample(p, sampleFileName, RiSample.MINIM);
  }
  /**
   * Convenience method that loads a RiSample object and
   * starts it playing. 
   * 
   * @see RiTa#MINIM
   * @see RiTa#ESS
   * @see RiTa#SONIA 
   * 
   * @see #playSample(PApplet, String, int)
   */
  public static RiSample playSample(PApplet p, String sampleFileName, int playerType) {
    RiSample rs = loadSample(p, sampleFileName, playerType, false);
    rs.play();
    return rs;
  }
    
  /** @invisible  */
  public static Map stringToMap(String parameters)
  {
//System.out.println("RiTa.stringToMap("+parameters+")");    
    Map m = new HashMap();
    String[] keyVals = parameters.split("&");
    for (int i = 0; i < keyVals.length; i++)
    {
      String[] kv = keyVals[i].split("=");
      if (kv == null ||kv.length != 2)
        throw new RiTaException("Invalid pair: "+keyVals[i]);      
      m.put(kv[0], kv[1]);      
    }
    //System.out.println("INIT-MAP: "+m);
    return m;
  }
  
  /** @invisible  */
  public static String mapToString(Map m)
  {
    StringBuilder sb = new StringBuilder(64);
    for (Iterator i = m.keySet().iterator(); i.hasNext();)
    {
      Object key = i.next(); // check if String?
      Object o = (Object) m.get(key);
      sb.append(key+"="+o);
      if (i.hasNext())
        sb.append("&");
    }   
//System.out.println("RiTa.mapToString("+m+") :: "+sb);
    return sb.toString();    
  }  
  
  /**
   * An alternative to {@link String#split(String)} that optionally
   * returns the delimiters.
   */
  public static String[] split(String toSplit, Pattern regexPattern, boolean returnDelims)
  {
    if (!returnDelims) return regexPattern.split(toSplit);
    
    int index = 0;
    List matchList = new ArrayList();
    Matcher m = regexPattern.matcher(toSplit);
    while (m.find())
    {
      String match = toSplit.subSequence(index, m.start()).toString();
      matchList.add(match);
      matchList.add(toSplit.subSequence(m.start(), m.end()).toString());
      index = m.end();
    }

    if (index == 0) return new String[] { toSplit };

    matchList.add(toSplit.subSequence(index, toSplit.length()).toString());

    int resultSize = matchList.size();
    while (resultSize > 0 && matchList.get(resultSize - 1).equals(""))
      resultSize--;

    return (String[]) matchList.subList(0, resultSize).toArray(new String[resultSize]);
  }

  
  /**
   * Trims null entries off the end of an array. Returns a new array consisting 
   * of the elements from 0 to the last non-null element.
   */
  public static Object[] trim(Object[] input)
  {
    int trimmedLength = 0;
    for (int i = input.length-1; i >= 0; i--)
    {
      if (input[i] != null) {
        trimmedLength = i + 1;
        break;
      }
    }
    String[] output = new String[trimmedLength];
    if (trimmedLength > 0)
      System.arraycopy(input, 0, output, 0, output.length);
    return output;
  }
  
  /**
   * Trims whitespace from both ends of <code>token</code> 
   */
  public static String trimEnds(String token)
  {
    boolean gotChar = false;
    token = token.trim();
    char[] c = token.toCharArray();
    token = "";
    for (int i = 0; i < c.length; i++)
    {
      if (gotChar || !Character.isSpaceChar(c[i])) {
        token += c[i];
        gotChar = true;
      }
    }
    return token;
  }
  
  /**
   * Trims punctuation from each side of the <code>token</code> 
   * (does not trim whitespace or internal punctuation).
   */ 
  public static String trimPunctuation(String token)
  {
    // Note: needs to handle byte-order marks...
    if (punctPattern == null) 
      punctPattern = Pattern.compile(PUNCT_PATT, Pattern.CASE_INSENSITIVE);
  
    Matcher m = punctPattern.matcher(token);
    boolean match = m.find();
    if (!match || m.groupCount() < 1) {
      System.err.println("[WARN] RiTa.trimPunctuation(): invalid regex state for String "
          + "\n       '" + token + "', perhaps an unexpected byte-order mark?");
      return token;
    }
     
    return m.group(1);
  }
  static Pattern punctPattern = null;
  //final static String PATT = "^(?:[\\p{Punct}’'`‘”“]*)((?:.)|(?:[\\w ].*?[\\w ]))(?:[\\p{Punct}’‘”“]*)$";
 
  final static int[] BOM = { 0xEF, 0xBB, 0xBF };
        
  /** Returns a String representing the current OS ["mac", "windows", "linux" ] */
  public static String getOS() {    
    String os = System.getProperty("os.name");
    for (int i = 0; i < OSs.length; i++)
      if (os.toLowerCase().indexOf(OSs[i]) >= 0)
        return OSs[i];
    System.err.println("[WARN] Undefined-OS: "+os);
    return OS_UNDEFINED;
  }

  /** @invisible */
  public static final String MAC = "mac";
  /** @invisible */
  public static final String LINUX = "linux";
  /** @invisible */
  public static final String WINDOWS = "windows";
  /** @invisible */
  public static final String OS_UNDEFINED = "undefined";
  
  /** @invisible */
  private static final String[] OSs = { WINDOWS, MAC, LINUX };
    
  /**
   * Returns a String representation of Exception and stacktrace
   *  (only elements with line numbers)
   */  
  public static String exceptionToString(Throwable e) {
    if (e == null) return "null";
    StringBuilder s = new StringBuilder(e+"\n");
    StackTraceElement[] stes = e.getStackTrace();
    for (int i = 0; i < stes.length; i++)
    {
      String ste = stes[i].toString();
      if (ste.matches(".*[0-9]+\\)"))
        s.append("    "+ste+RiTa.BN);
    }
    return s.toString();
  }  
  
  /** 
   * Calls 'methodName' on 'callee' Object with args via reflection
   * @return return value of method or null on error.
   * @invisible
   */
  public static Object invoke(Object callee, 
    String methodName, Class[] argTypes, Object[] args)
  { 
    Method m = null;
    try
    {
//System.out.println("INVOKE: "+callee.getClass()+"."+methodName+"(types="+asList(argTypes)+", vals="+asList(args)+")");      
      m = findMethod(callee, methodName, argTypes, args, true);
    }
    catch (RiTaException e) 
    {      
      if (e instanceof RiMethodMissing) {
        if (callee instanceof RiMessagable) {
          RiMessagable rdo = (RiMessagable) callee;
          return rdo.methodMissing(methodName, args);
        }
        else { 
          //System.out.println("TRYING DYNAMIC METHOD-MISSING");
          m = findMethodMissing(callee, argTypes, args, (RiMethodMissing)e);
          if (m != null) {
            try {
              Object[] params = new Object[]{methodName, args};
              //System.out.println("RiTa.invoke("+callee+"."+params+")");
              return m.invoke(callee, params);              
            } 
            catch (Exception e1) {
              throw new RiTaException(e1);
            }
          }
        }         
      }
      //else if (RiDynamicType.class)
      throw e;
    }    
    return invoke(callee, m, args);
  }
  /** 
   * Calls 'method' on 'object' via reflection, passing 'args'
   * and inferring the Class[] of argument types by calls to 
   * {@link Class#getClass()} for each arg.
   * 
   * @return return value of method or null on error.
   * @invisible
   */
  public static Object invoke(Object callee, String methodName, Object[] args)
  { 
    if (args == null) return invoke(callee, methodName);    
  	Class[] argTypes = new Class[args.length];
  	for (int i = 0; i < args.length; i++)  {
			argTypes[i] = args[i].getClass();  
      if (argTypes[i]==Integer.class)
        argTypes[i] = Integer.TYPE;
      else if (argTypes[i]==Boolean.class)
        argTypes[i] = Boolean.TYPE;
      else if (argTypes[i]==Float.class)
        argTypes[i] = Float.TYPE;
      else if (argTypes[i]==Double.class)
        argTypes[i] = Double.TYPE;
      else if (argTypes[i]==Character.class)
        argTypes[i] = Character.TYPE; 
  	}
    return invoke(callee, methodName, argTypes, args);
  }   
  /** 
   * Calls 'methodName' on 'callee' Object via reflection
   * @invisible
   * @return return value of method or null on error.
   */
  public static Object invoke(Object callee, String methodName)
  { 
    return invoke(callee, methodName, null, null);    
  }
  
  /**
   * Fires a RiTaEvent as a callback to the parent which is
   * generally (but now always) a PApplet
   * @invisible
   */
  public static boolean fireEvent(Object pApplet, RiTaEvent rte) {
    if (pApplet==null) return false;
    try
    {    
      invoke(pApplet, "onRiTaEvent", 
        new Class[] { RiTaEvent.class }, new Object[]{ rte });
    }
    catch (RiTaException e) {
      // no error if the method doesnt exist...
      return false;
    }
    return true;
  }

  static boolean SHOW_INVOKE_WARNINGS = true;

  private static Method findMethodMissing(Object callee, Class[] argTypes, Object[] args, RiMethodMissing e) 
  {
    Method m = null;
    try {
//System.out.println("TRYING REFLECTED METHOD-MISSING INVOKE");
      m = callee.getClass().getMethod("methodMissing",  new Class[]{String.class, Object[].class});
    }
    catch (Exception f) {
      return null;
    }
    return m;
  }

  /** @invisible */
  public static Method findMethod(Object callee, String methodName, Class[] argTypes, Object[] args, boolean isPublic)
  {
    Method m = null;
    try 
    {
      if (callee instanceof Class) {  // static method
        if (isPublic) {
          m = ((Class)callee).getMethod(methodName,  argTypes);
        }
        else
          m = ((Class)callee).getDeclaredMethod(methodName,  argTypes);
      }
      else                       // non-static method
      {
        if (isPublic) 
        {
          m = callee.getClass().getMethod(methodName, argTypes);
        }
        else
          m = callee.getClass().getDeclaredMethod(methodName, argTypes);
      }
    } 
    catch (Exception e) {
      throw new RiMethodMissing(callee, methodName, args, argTypes);       
    }    
    //if (!isPublic) m.setAccessible(true);  // re-add??    
    return m;
  }

  private static Object invoke(Object callee, Method m, Object[] args)
  {
    try 
    {
//System.out.println("INVOKE: "+callee+"."+m.getName()+"("+asList(args)+")");
      return m.invoke(callee, args);
    } 
    catch (Throwable e)
    {
      Throwable cause = e.getCause();
      while (cause != null) {
         e = cause;
         cause = e.getCause();
      }
      if (SHOW_INVOKE_WARNINGS) { 
        System.err.println("[WARN] Invoke error on "+RiTa.shortName
          (callee)+"."+m.getName()+"("+asList(args)+")\n  "+exceptionToString(e));
      }
      throw new RiTaException(e);      
    }
  }
  
  /** 
   * Calls inaccessible (default, private, or protected) 'methodName' 
   * on  'callee' Object with 'args' via reflection.
   * @invisible
   * @return return value of method or null on error.
   */
  public static Object invokeHidden(Object callee, String methodName, Class[] argTypes, Object[] args)
  {    
    Method m = null;
    try {
      m = findMethod(callee, methodName, argTypes, args, false);
    }
    catch (RiTaException e) {      
      if (e instanceof RiMethodMissing) 
      {
        if (callee instanceof RiMessagable) {      
          RiMessagable rdo = (RiMessagable) callee;
          return rdo.methodMissing(methodName, args);
        }
        else 
          m = findMethodMissing(callee, argTypes, args, (RiMethodMissing)e);
      }
      throw e;
    } 
    return invoke(callee, m, args);
  }
  
  /**
   * Removes a random element from a List, maintaining the ordering
   * @return null if List is null or empty, else removed item
   * @invisible
   */
  public static Object removeRandom(List list)
  { 
    if (list == null || list.size()==0) return null;
    int rand = (int)(Math.random()*list.size());
    return list.remove(rand);
  }
  
  /**
   * Removes a random element from a Collection, maintaining the ordering
   * @return null if List is null or empty, else removed item
   */
  public static Object removeRandom(Collection c)
  { 
    Object o = random(c);
    c.remove(o);
    return o;
  }
  
  /**
   * Returns a random element from a Collection
   * @return null if List is null or empty, else random item
   */
  public static Object random(Collection c/*, boolean removeItem*/) {
    if (c == null || c.isEmpty()) return null;
    int rand = (int)(Math.random()*c.size());
    Object result = null;
    Iterator it = c.iterator();
    for (int i = 0; i <= rand; i++)
      result = it.next();
    return result;
  }
  
  /**
   * Returns a random element from a List
   * @return null if List is null or empty, else random item
   */
  public static Object random(List list)
  { 
    if (list == null || list.size()==0) return null;
    int rand = (int)(Math.random()*list.size());
    return list.get(rand);
  }

  
  /**
   * Returns a random element from a List
   * @return null if List is null or empty, else random item
   */
  public static Object random(Object[] list)
  { 
    if (list == null || list.length==0) return null;
    int rand = (int)(Math.random()*list.length);
    return list[rand];
  }
  
  /** @invisible */  // TEST THIS
  public static /*Object[] */ void shuffle(Object[] items)
  { 
    List tmp = new LinkedList();
    for (int i = 0; i < items.length; i++)
      tmp.add(items[i]);
    Collections.shuffle(tmp);
    int idx = 0;
    for (Iterator i = tmp.iterator(); i.hasNext(); idx++)
    {
      items[idx] = i.next();      
    }
    //return (Object[])tmp.toArray(new Object[tmp.size()]);
  }

  /**
   * Returns a randomly ordered array
   * of unique integers from 0 to <code>numElements</code> -1.
   * The size of the array will be <code>numElements</code>. 
   */
  public static int[] randomOrdering(int numElements)
  { 
    int[] result = new int[numElements];
    List tmp = new LinkedList();
    for (int i = 0; i < result.length; i++)
      tmp.add(new Integer(i));
    Collections.shuffle(tmp);
    int idx = 0;
    for (Iterator iter = tmp.iterator(); iter.hasNext(); idx++)
      result[idx] = ((Integer)iter.next()).intValue();
    return result;
  }

  /** Returns a formatted String from a float[]    */
  public static String toString(float[] floats) 
  {
    String s = "[";
    for (int i = 0; i < floats.length; i++) {
      s += floats[i];
      if (i < floats.length - 1)
        s += ",";
    }
    return s + ']';
  }
 
  // test this
  /**
   * Returns a shuffled view of the key list for a map
   */
  public static List shuffleKeys(final Map m) 
  {  
  	if (m instanceof SortedMap) 
  		throw new RiTaException("Unable to shuffle a sortedMap!");

    List keys = new LinkedList();
    for (Iterator i = m.keySet().iterator(); i.hasNext();)     	 
			keys.add(i.next());
    
    Collections.shuffle(keys);
    
    return keys;
  }
  
  /** Joins a list using space  as a delimiter. */
  public static String join(List tokenList) 
  {  
  	return join(tokenList, " ");
  }
  
  /** Returns true if all elements of the 2 lists are equal,
   * and have the same ordering, 
   * testing the elements in each with {@link Object#equals(Object)}
   *  */
  public static boolean equals(List l1, List l2) 
  {
    if (l1==null && l2==null || l1 == l2) 
      return true;
    
    if (l1 == null && l2 != null|| l1 != null && l2 == null) 
      return false;    
    
  	if (l1.size() != l2.size()) return false;
  	
  	for (int i = 0; i < l1.size(); i++) {			
      String a = (String)l1.get(i);
      String b = (String)l2.get(i);
      if (!a.equals(b))
      	return false;
  	}
		return true;
	}

  /** @invisible */
  public static float constrain(float amt, float min, float max) {
    return Math.min(max, Math.max(min, amt));
  }
  
  /** @invisible */
  public static int constrain(int amt, int min, int max) {
    return Math.min(max, Math.max(min, amt));
  }
  
  /**
   * Verifies that every character in <code>word</code> is lowerCase
   */
  public static boolean isLowerCase(String word) {
    for (int j = 0; j < word.length(); j++)
    {
      char c = word.charAt(j);
      if (!Character.isLowerCase(c))
        return false;
    }
    return true;
  }
  
  /**
   * Returns true if all characters are uppercase letters
   */
  public static boolean isUpperCase(String word) {
    for (int j = 0; j < word.length(); j++) {
      char c = word.charAt(j);
      if (!Character.isUpperCase(c))
        return false;
    }
    return true;
  }
  
  /** Returns input String with XML/HTML entities replaced */
  public static String replaceEntities(String input)
  {    
    return EntityLookup.getInstance().unescape(input);
  }
  
  /** @invisible */
  public static String shortName(Class c)
  {
    String name = c.getName();    
    int idx = name.lastIndexOf(".");
    return name.substring(idx+1);
  }
  
  /** @invisible */
  public static String shortName(Object c)
  {
  	return shortName(c.getClass());   
  }
  
  /** @invisible */
  public static void pcwd() {
    System.out.println(cwd());
  }
  
  /** Returns a String holding the current working directory */
  public static String cwd() {
    
    String cwd = "UNKNOWN";
    try {
      cwd = System.getProperty("user.dir");
    }
    catch (Exception e) {
      System.out.println("[WARN] Unable to determine current directory!");
    }
    return cwd;
  }
  
  /** @invisible */
  public static void pls() {
    System.out.println(ls());
  }
  
  /** @invisible */
  public static String ls() {
    return join( new File(System.getProperty("user.dir")).list());    
  }
  
  /**
   * Returns input String stripped of HTML markup
   * @param input String containing HTML markup
   * @return input stripped of HTML markup
   */
  public static String stripMarkup(String input)
  {
    if (!(contains(input, '<'))) return input;

    StringBuilder content = new StringBuilder();
    boolean inTag = false;

    for (int j = 0; j < input.length(); j++)
    {
      char c = input.charAt(j);
      if (c == '<')
      {
        inTag = true;
        continue;
      }
      else if (c == '>')
      {
        inTag = false;
        continue;
      }
      if (!inTag)
        content.append(c);
    }
    return chomp(content.toString());
    // was stripCRs
  }
  
  /**
   * Returns true if <code>word</code> is a close-class word.
   */
  public static boolean isClosedClass(String word)
  {
    for (int i = 0; i < CLOSED_CLASS_WORDS.length; i++)
      if (word.equalsIgnoreCase(CLOSED_CLASS_WORDS[i]))
        return true;
    return false;    
  }

  private static boolean isIn(char c, String s) {
    return s.indexOf(c) >= 0;
  }
  
  /**
   * Returns true if 'currentWord' is the final word of a sentence. <p>
   * This is a simplified version of the OAK/JET sentence splitter method.
   */
  public static boolean isSentenceEnd(String currentWord, String nextWord)
  {
    //System.out.println("RiTa.isSentenceEnd("+currentWord+", "+nextWord+")");
    
    if (currentWord == null) return false;
    
    int cWL = currentWord.length();
    
    // token is a mid-sentence abbreviation (mainly, titles) --> middle of sent
    if (RiTa.isAbbreviation(currentWord))
      return false;
    
    if (cWL > 1 && isIn(currentWord.charAt(0), "`'\"([{<")
        && RiTa.isAbbreviation(currentWord.substring(1)))
      return false;

    if (cWL > 2 && ((currentWord.charAt(0) == '\'' 
      && currentWord.charAt(1) == '\'') || (currentWord.charAt(0) == '`' 
      && currentWord.charAt(1) == '`')) && RiTa.isAbbreviation(currentWord.substring(2)))
    {
      return false;
    }
    
    char currentToken0 = currentWord.charAt(cWL - 1);
    char currentToken1 = (cWL > 1) ? currentWord.charAt(cWL - 2) : ' ';
    char currentToken2 = (cWL > 2) ? currentWord.charAt(cWL - 3) : ' ';
    
    int nTL = nextWord.length();
    char nextToken0 = nextWord.charAt(0);
    char nextToken1 = (nTL > 1) ? nextWord.charAt(1) : ' ';
    char nextToken2 = (nTL > 2) ? nextWord.charAt(2) : ' ';

    // nextToken does not begin with an upper case,
    // [`'"([{<] + upper case, `` + upper case, or < -> middle of sent.
    if (!  (Character.isUpperCase(nextToken0) 
        || (Character.isUpperCase(nextToken1) && isIn(nextToken0, "`'\"([{<"))
        || (Character.isUpperCase(nextToken2) && ((nextToken0 == '`' && nextToken1 == '`') 
        || (nextToken0 == '\'' && nextToken1 == '\'')))
        ||  nextWord.equals("_") || nextToken0 == '<'))
      return false;

    // ends with ?, !, [!?.]["'}>)], or [?!.]'' -> end of sentence
    if (currentToken0 == '?'
        || currentToken0 == '!'
        || (isIn(currentToken1, "?!.") && isIn(currentToken0, "\"'}>)"))
        || (isIn(currentToken2, "?!.") && currentToken1 == '\'' && currentToken0 == '\''))
      return true;
      
    // last char not "." -> middle of sentence
    if (currentToken0 != '.') return false;

    // Note: wont handle Q. / A. at start of sentence, as in a news wire
    //if (startOfSentence && (currentWord.equalsIgnoreCase("Q.") 
      //|| currentWord.equalsIgnoreCase("A.")))return true; 
    
    // single upper-case alpha + "." -> middle of sentence
    if (cWL == 2 && Character.isUpperCase(currentToken1))
      return false;

    // double initial (X.Y.) -> middle of sentence << added for ACE
    if (cWL == 4 && currentToken2 == '.'
        && (Character.isUpperCase(currentToken1) && Character
            .isUpperCase(currentWord.charAt(0))))
      return false;

    // U.S. or U.N. -> middle of sentence
    //if (currentToken.equals("U.S.") || currentToken.equals("U.N."))
      //return false; // dch
      
    //f (Util.isAbbreviation(currentToken)) return false;
    
    // (for XML-marked text) next char is < -> end of sentence
    if (nextToken0 == '<')
      return true;
    
    return true;
  }
  
  /**
   * Returns true if <code>sentence</code> starts with a question sword.
   */  
  public static boolean isQuestion(String[] sentence)
  {
    for (int i = 0; i < QUESTION_STARTS.length; i++)
      if (sentence[0].equalsIgnoreCase(QUESTION_STARTS[i]))
        return true;
    return false;
  }
  
  /**
   * Returns true if <code>sentence</code> starts with a w-question word,
   * eg (who,what,why,where,when,etc.)
   */  
  public static boolean isW_Question(String[] sentence)
  {
    for (int i = 0; i < W_QUESTION_STARTS.length; i++)
      if (sentence[0].equalsIgnoreCase(W_QUESTION_STARTS[i]))
        return true;
    return false;
  }

  /**
   * Returns true if <code>c</code> is a vowel
   */
  public static boolean isVowel(char c) 
  {
    return Character.isLetter(c) && VOWELS.indexOf(c)>-1;
  }
  
  /**
   * Returns an array without the closed-class words 
   */
  public static String[] getOpenClassWords(String[] words)
  {
    if (words == null) throw new IllegalArgumentException
      ("getOpenClassWords -> Null argument");

    List result = new ArrayList();
    for (int i = 0; i < words.length; i++) { 
      if (!isClosedClass(words[i])) 
        result.add(words[i]);
    }  
    return (String[])result.toArray(new String[result.size()]);
  }

  public static String toString(char[] c) {
    StringBuilder buf = new StringBuilder(c.length);
    buf.append("[");
    for (int i = 0; i < c.length; i++)
      buf.append(c[i] + ",");
    return buf.substring(0, buf.length() - 1) + "]";
  }
  
  static Set abbreviations = new HashSet(64);
  //static Set monocaseAbbreviations = new HashSet(64);

  static {// titles
    abbreviations.add("Adm.");
    abbreviations.add("Capt.");
    abbreviations.add("Cmdr.");
    abbreviations.add("Col.");
    abbreviations.add("Dr.");
    abbreviations.add("Gen.");
    abbreviations.add("Gov.");
    abbreviations.add("Lt.");
    abbreviations.add("Maj.");
    abbreviations.add("Messrs.");
    abbreviations.add("Mr.");
    abbreviations.add("Mrs.");
    abbreviations.add("Ms.");
    abbreviations.add("Prof.");
    abbreviations.add("Rep.");
    abbreviations.add("Reps.");
    abbreviations.add("Rev.");
    abbreviations.add("Sen.");
    abbreviations.add("Sens.");
    abbreviations.add("Sgt.");
    abbreviations.add("Sr.");
    abbreviations.add("St.");

    // abbreviated first names
    // abbreviations.add("Alex."); // dch
    abbreviations.add("Benj.");
    abbreviations.add("Chas.");
    
    // abbreviated months
    abbreviations.add("Jan.");
    abbreviations.add("Feb.");
    abbreviations.add("Mar.");
    abbreviations.add("Apr.");
    abbreviations.add("Mar.");
    abbreviations.add("Jun.");
    abbreviations.add("Jul.");
    abbreviations.add("Aug.");
    abbreviations.add("Sept.");
    abbreviations.add("Oct.");
    abbreviations.add("Nov.");
    abbreviations.add("Dec.");

    // other abbreviations
    abbreviations.add("a.k.a.");
    abbreviations.add("c.f.");
    abbreviations.add("i.e.");
    abbreviations.add("e.g.");
    abbreviations.add("vs.");
    abbreviations.add("v.");

    Set tmp = new HashSet(64);
    Iterator it = abbreviations.iterator();
    while (it.hasNext())
      tmp.add(((String) it.next()).toLowerCase());
    abbreviations.addAll(tmp);
  }

   /** Returns true if 'input' is an abbreviation */
  public static boolean isAbbreviation(String input)
  {
    return abbreviations.contains(input); // case??
  }

  /** Verifies the first char is upperCase and the rest are lowerCase */
  public static boolean isTitleCase(String word)
  {
    // Make sure we start the 1st is upper-case
    if (!Character.isUpperCase(word.charAt(0))) 
      return false;

    // And the rest are lowercase
    for (int i = 1; i < word.length(); i++) 
      if (!Character.isLowerCase(word.charAt(i))) 
         return false;
    
    return true;
  }

  /** Verifies only the absence of lowercase letters     */
  public static boolean isAllCaps(String word)
  {
    for (int i = 0; i < word.length(); i++) {
      char c = word.charAt(i);
      if (Character.isLetter(c) && !Character.isUpperCase(c))
        return false;
    }
    return true;
  }
  
  /** Verifies all chars are are capital letters */
  public static boolean allCaps(String word)
  {
    for (int i = 0; i < word.length(); i++) 
      if (Character.isUpperCase(word.charAt(i)))
        return false;
    return true;
  }

  /** Verifies the absence of uppercase letters     */
  public static boolean noCaps(String word)
  {
    for (int i = 0; i < word.length(); i++) {
      char c = word.charAt(i);
      if (Character.isLetter(c) && !Character.isLowerCase(c))
        return false;
    }
    return true;
  }
  
  /** Verifies all characters are lowercase & letters      */
  public static boolean allLowerCase(String word)
  {
    for (int i = 0; i < word.length(); i++) 
      if (!Character.isLowerCase(word.charAt(i)))
        return false;
    return true;
  }
  
  /** Verifies all characters in all (non-null) words are lowercase letters  */
  public static boolean allLowerCase(String[] words)
  {
    for (int j = 0; j < words.length; j++) {
      if (words[j] == null) continue;
      for (int i = 0; i < words[j].length(); i++) {
        if (!Character.isLowerCase(words[j].charAt(i)))
          return false;        
      }
    }
    return true;
  }
  
  /** Verifies all characters in all words are lowercase letters  */
  public static boolean allLetters(String[] words)
  {
    for (int j = 0; j < words.length; j++) 
      for (int i = 0; i < words[j].length(); i++) 
        if (!Character.isLetter(words[j].charAt(i)))
          return false;
    return true;
  }
  
  /**
   * Concatenates the array 'input' into a single String, spearated by 'delim'
   */
  public static String join(String[] input, char delim)
  {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < input.length; i++) {
      sb.append(input[i]);
      if (i < input.length-1) 
        sb.append(delim);
    }
    return sb.toString();
  }
  /**
   * Concatenates the list 'input' into a single String, spearated by 'delim'
   */
  public static String join(List input, String delim)
  {    
    StringBuilder sb = new StringBuilder();
    if (input != null) {
      for (Iterator i = input.iterator(); i.hasNext();) {
        sb.append(i.next());      
        if (i.hasNext())
          sb.append(delim);
      }
    }
    return sb.toString();
  }
  
  /** @invisible */
  public static boolean contains(String full, String search)
  {
    if (full == null) return false; 
    return (full.indexOf(search) > -1);
  }

  /** counts the # of times 'toMatch' appears in input */
  public static int count(String input, String toMatch)
  {
    int count = 0;
    int idx = input.indexOf(toMatch);
    // StringBuilder tmp = new StringBuilder(text);
    if (idx < 0)
      return 0;
    // we've found one, lets keep going
    while (idx >= 0) {
      count++;
      input = input.substring(idx + toMatch.length(), input.length());
      idx = input.indexOf(toMatch);
    }
    return count;
  }
  /** counts the # of times char c appears in input */
  public static int count(String input, char toMatch)
  {
    int count = 0;
    for (int i = 0; i < input.length(); i++)
      if (input.charAt(i) == toMatch)
        count++;
    return count;
  }
  
  /**
   * Joins Array of String into space-delimited String.
   * @param full - Array of Strings to be joined
   * @return String containing elements of String[] or ""
   */
  public static String join(Object[] full)
  {
    return join(full, SPC);
  }

  /**
   * Joins Array of String into delimited String.
   * @param full - Array of Strings to be joined
   * @param delim - Delimiter to parse elements in resulting String
   * @return String containing elements of String[] or "" if null
   */
  public static String join(Object[] full, String delim)
  {
    StringBuilder result = new StringBuilder();
    if (full != null) {
      for (int index = 0; index < full.length; index++) {
        if (index == full.length - 1)
          result.append(full[index]);
        else
          result.append(full[index] + delim);
      }
    }
    return result.toString();
  }

	/**
	 * Loads a properties-format (key=value) File 
	 * from the 'data' directory (if the path is relative)
	 * and parses it into a Map  
	 */
	public static Map loadPropertyFile(PApplet p, String fName) 
	{  
    //System.out.println("loadPropertyFile("+fName+")");
    
		String[] strings = loadStrings(p,fName);		
    
		Map m = new HashMap();
	  for (int i = 0; i < strings.length; i++) {
	  	if (strings[i].startsWith("#")) continue;
			String[] conf = strings[i].split("=");
			if (conf.length==2) 
			  m.put(conf[0], conf[1]);
	  }
	  return m;
	}

  private static String[] loadStringsLocal(String name)
  {   
    InputStream is = openStreamLocal(name);   
    // UnicodeInputStream uis = new UnicodeInputStream(is);
    return loadStrings(is, 100);
  }

    /** @invisible */
  public static String[] getDataDirectoryGuesses() {  return guesses; }
  
  private static String[] includedFiles = new String[] { "addenda.txt", "bin.gz" };
  private static boolean isIncluded(String fname) {
    for (int i = 0; i < includedFiles.length; i++) {
      if (fname.endsWith(includedFiles[i]))
        return true;
    }
    return false;
  }  
  

  /**
   * Returns the path to the 'libraries' directory in the Processing sketchbook 
   * @invisible  
   */
  public static String libPath(PApplet p) {
    if (p == null) return null;
    String sp = p.sketchPath;
//System.out.println("Processing.SketchDir="+sp);   
    String search = OS_SLASH+"Processing"+OS_SLASH;
    int idx = sp.indexOf(search);
    if (idx < 0) {
      System.out.println("[WARN] Unable to determine sketchbook directory!");
      return null;
    }
    sp = sp.substring(0, idx+1);
    sp += "Processing"+OS_SLASH+"libraries"+OS_SLASH;
//System.out.println("Processing.LibDir="+sp);
    return sp;
  }
  
  private static String[] guesses = { "src"+OS_SLASH+"data", "data", "" };
  private static InputStream openStreamLocal(String streamName) // need to handle URLs here..
  {
//System.out.println("RiTa.openStreamLocal("+streamName+")");
    
    if (!SILENT && !printedNullParentWarning) { // hack, just print this once
      System.err.println( "[WARN] Null PApplet passed to RiTa when loading '"
        + streamName + "'\n       If you are using Processing, this may cause problems...");
      printedNullParentWarning = true;
    }    
    
    try // check for url first  (from PApplet)
    {
      URL url = new URL(streamName);
      return url.openStream();
    } catch (MalformedURLException mfue) {
      // not a url, that's fine
    } catch (FileNotFoundException fnfe) {
      // Java 1.5 likes to throw this when URL not available.
      // http://dev.processing.org/bugs/show_bug.cgi?id=403
    } catch (Throwable e) 
    {
      throw new RiTaException("Throwable in openStreamLocal()",e);
    }     
    
    InputStream is = null;
    
    for (int i = 0; i < guesses.length; i++) {
    	String guess = streamName;
      if (guesses[i].length() > 0) { 
        if (RiTa.isAbsolutePath(guess)) continue;
        guess = guesses[i] + OS_SLASH + guess;
      }
   
      boolean isDefaultFile = isIncluded(guess);       
      if (!isDefaultFile && !RiTa.SILENT) 
        System.out.print("[INFO] Trying "+guess);
      
    	try {
    		is = new FileInputStream(guess);
        if (!isDefaultFile&& !RiTa.SILENT) System.out.println("... OK");
    	} 
    	catch (FileNotFoundException e) {
        if (!isDefaultFile&& !RiTa.SILENT) System.out.println("... failed");
    	}
    	if (is != null) break;
    }
    
    if (is == null) // last try with classloader... 
    {
      // Using getClassLoader() prevents java from converting dots
      // to slashes or requiring a slash at the beginning.
      // (a slash as a prefix means that it'll load from the root of
      // the jar, rather than trying to dig into the package location)
      ClassLoader cl = RiTa.class.getClassLoader();

      // by default, data files are exported to the root path of the jar.
      // (not the data folder) so check there first.
      if (!RiTa.SILENT)
        System.out.print("[INFO] Trying data/" + streamName+" as resource");
      
      is = cl.getResourceAsStream("data/" + streamName);
      if (is != null) {
        String cn = is.getClass().getName();
        // this is an irritation of sun's java plug-in, which will return
        // a non-null stream for an object that doesn't exist. like all good
        // things, this is probably introduced in java 1.5. awesome!
        // http://dev.processing.org/bugs/show_bug.cgi?id=359
        if (!cn.equals("sun.plugin.cache.EmptyInputStream")) {
          if (!RiTa.SILENT) System.out.println("... OK");
          return is;
        }
      }
      if (!RiTa.SILENT)System.out.println("... failed");
    }
    
    if (is == null) 
      throw new RiTaException("Unable to create stream for: "+streamName);
    
    return is;
  }	
  static boolean printedNullParentWarning = false;
  
  /**
   * Opens a URL (line-by-line) and reads the 
   * contents into a String[], one line per array element
   * @return Contents of the URL stream as a String
   */
  public static String[] loadStrings(URL url)
  {  
    try
    {
      return PApplet.loadStrings(url.openStream());
    } 
    catch (IOException e)
    {
      throw new RiTaException("unable to open url: "+url);
    } 
  }
  
  /*
   * (Only used as backup method)
   * Loads a File by name and reads the  contents into a single String
   * @return Contents of the file as String
   */
  private static String loadStringOld(PApplet pApplet, String filename) {
    byte[] bytes = null;
    if (pApplet != null) {
      // ok, we're good w' papplet
      bytes = pApplet.loadBytes(filename);
    }
    else  {// uh-oh, who knows?
      bytes = PApplet.loadBytes(openStreamLocal(filename));
    }    
    if (bytes == null)
      throw new RiTaException("The file '"+filename+"' is missing or inaccessible, " +
        "make sure the URL is valid or that the file has been added to your data " +
        "folder and is readable.");    
    return new String(bytes);
  }
    
  /**
   * Loads a File by name and reads the 
   * contents into a single String
   * 
   * @return Contents of the file as String
   */
  public static String loadString(PApplet pApplet, String fileName) {
    if (pApplet != null) {
      String[] lines = loadStrings(pApplet, fileName);
      return join(lines, BN);
    }
    return loadStringOld(pApplet, fileName);
  }

  /**
   * Same as pApplet.loadStrings but correctly handles UTF-8 characters.
   */
  public static String[] loadStrings(PApplet pApplet, String fileName)
  {
   String[] lines = null;
    if (pApplet != null) {
      lines = pApplet.loadStrings(fileName);
    }
    else // uh-oh, who knows?
      lines = loadStringsLocal(fileName);
    
    if (lines == null)
      throw new RiTaException("The file '"+fileName+"' is missing or inaccessible, " +
        "make sure the URL is valid or that the file has been added to your data " +
        "folder and is readable.");
 
    return lines;
    
  }

  /**
   * Returns a map of key-value pairs in an XML string with format:
   *      <... key1='value1' key2='value2' ... />
   * @invisible 
   */
  public static Map propertiesFromXml(String xml)
  {
    String[] parts = xml.split(" ");
    Map props = new HashMap();
    for (int i = 0; i < parts.length; i++)
    {
      int idx = parts[i].indexOf("='");
      if (idx>0) { 
        String key = parts[i].substring(0,idx);
        int idx2 = parts[i].lastIndexOf("'");
        if (idx2 < 0) throw new RiTaException("Bad XML: "+xml);
        String val = parts[i].substring(idx+2,idx2);
        props.put(key, val);
      }
    }
    return props;
  }  
  
  /**
   * Opens an InputStream to the specified filename  
   * @invisible 
   */
  public static InputStream openStream(Class resourceDir, String fileName) 
  {
    return resourceDir.getResourceAsStream(fileName);
  }
  
  /**
   * Opens an InputStream for the specified file 
   */
  public static InputStream openStream(PApplet p, String fileName) 
  {
    System.err.println("openStream("+p+", "+fileName+")");
  	InputStream is = null;
    try
    {
      if (p != null) {
        is =  p.createInput(fileName);
      }
      else 
        is = openStreamLocal(fileName);
      
      if (is == null) throw new RiTaException();
    }
    catch (RiTaException e) {
      throw new RiTaException("Unable to open stream: "+fileName+" with pApplet="+p+" pwd="+cwd());
    }
    return is;//new UnicodeInputStream(is);
  } 

  private static String[] loadStrings(InputStream input, int numLines) {
    if (input == null) throw new RiTaException("Null input stream!");
    try {
      BufferedReader reader =
        new BufferedReader(new InputStreamReader(input, textEncoding));

      String lines[] = new String[numLines];
      int lineCount = 0;
      String line = null;
      while ((line = reader.readLine()) != null) {
        if (lineCount == lines.length) {
          String temp[] = new String[lineCount << 1];
          System.arraycopy(lines, 0, temp, 0, lineCount);
          lines = temp;
        }
        lines[lineCount++] = line;
      }
      reader.close();

      if (lineCount == lines.length) {
        return lines;
      }

      // resize array to appropriate amount for these lines
      String output[] = new String[lineCount];
      System.arraycopy(lines, 0, output, 0, lineCount);
      return output;

    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  
  /**
   * Returns true if 'search' is found in the String, else false.
   * @invisible
   */
  public static boolean contains(String full, char search, boolean ignoreCase) {
    boolean result = contains(full, search);
    if (!result && ignoreCase) {
      if (Character.isLowerCase(search))
        result = contains(full, Character.toUpperCase(search));
      else
        result = contains(full, Character.toLowerCase(search));
    }
    return result;
  }
  
  /**
   * @invisible
   */
  public static boolean contains(String full, char search) {
    if (full == null || full.length()!=1) return false;    
    return (full.indexOf(Character.toString(search)) > -1);
  }
  

  /**
   * Return true if full is a substring of any element in search
   * @invisible    
  public static boolean contains(String full, String[] search, boolean ignoreCase)
  {
    if (full == null || search == null || search.length ==0)
      return false; //?
    for (int i = 0; i < search.length; i++)
      if (ignoreCase) {
        if (full.equalsIgnoreCase(search[i]))
          return true;
      }
      else {
        if (full.equals(search[i]))
          return true;
      }
    return false;
  }*/

  /** @invisible */
  public static boolean endsWith(String full, char[] search) {
    if (full == null || search == null || search.length ==0)
      return false; //?
    for (int i = 0; i < search.length; i++) {
      if (full.charAt(full.length() - 1) == search[i])
        return true;
    }
    return false;
  }
  
  /**
   * Returns true if 'input' ends with a punctuation character
   * @see #PUNCTUATION 
   */
  public static boolean endsWithPunctuation(String input) {
    if (input == null || input.length()<1) return false;
    char c = input.charAt(input.length()-1);
    return (PUNCTUATION.indexOf(c) > -1);      
  }
  
  
  public static String stripPunctuation(String phrase) {
    return stripPunctuation(phrase, null);
  }
  
  public static void stripPunctuation(RiText phrase) {
    String word = stripPunctuation(phrase.getText(), null);
    phrase.setText(word);
  }
  
  /**
   * Strips any punctuation characters from the String
   * @see #PUNCTUATION
   */
  public static String stripPunctuation(String phrase, char[] charsToIgnore)
  {
    if (phrase == null || phrase.length()<1) 
      return "";
    
    StringBuilder sb = new StringBuilder();
    OUTER: for (int i = 0; i < phrase.length(); i++) {
      char c = phrase.charAt(i);
      //System.out.println("char: "+c+" "+Character.valueOf(c));
      if (charsToIgnore != null)  {
        for (int j = 0; j < charsToIgnore.length; j++) {
          if (c == charsToIgnore[j]) {
            sb.append(c);
            continue OUTER;
          }
        }
      }
      if (PUNCTUATION.indexOf(c) < 0)
        sb.append(c);
    }
    return sb.toString();
  }
  
  private static final String ALL_QUOTES = "\"“”’‘`'"; // 7 ?
 
  /** @invisible */
  public static final String PUNCTUATION = ALL_QUOTES+"\",;:!?)([].#\"\\!@$%&}<>|+=-_\\/*{^"; // add quotes?
  
  final static String PUNCT_PATT = "^(?:[\\p{Punct}"+ALL_QUOTES+"]*)((?:.)|(?:[\\w ].*?[\\w ]))(?:[\\p{Punct}"+ALL_QUOTES+"]*)$";

/*  private static Set suffixes2 = new HashSet();
  private static Set suffixes3 = new HashSet();  
  static {
    suffixes2.add("'s");
    suffixes2.add("'m");
    suffixes2.add("'d");
    suffixes2.add("'S");
    suffixes2.add("'M");
    suffixes2.add("'D");
    suffixes3.add("'re");
    suffixes3.add("'ve");
    suffixes3.add("n't");
    suffixes3.add("'ll");
    suffixes3.add("'RE");
    suffixes3.add("'VE");
    suffixes3.add("N'T");
    suffixes3.add("'LL");
  }*/
  
  /** @invisible */
  private static final String[] W_QUESTION_STARTS = {
    "Was", "What", "When", "Where", "How", "Which",
    "Why", "Who", "Will", 
  };  
  private static final String[] QUESTION_STARTS = {
    "Was", "What", "When", "Where", "How", "Which", "If",  
    "Who", "Is", "Could", "Might", "Will", "Does", "Why", 
  };  
  /** @invisible */
  public static final String[] CLOSED_CLASS_WORDS = { ".", ",", "THE", 
      "AND", "A", "OF", "\"", "IN", "I", ":", "YOU", "IS", "TO",
      "THAT", ")", "(", "IT", "FOR", "ON", "!", "HAVE", "WITH", "?",
      "THIS", "BE", "...", "NOT", "ARE", "AS", "WAS", "BUT", "OR", "FROM",
      "MY", "AT", "IF", "THEY", "YOUR", "ALL", "HE", "BY", "ONE",
      "ME", "WHAT", "SO", "CAN", "WILL", "DO", "AN", "ABOUT", "WE", "JUST",
      "WOULD", "THERE", "NO", "LIKE", "OUT", "HIS", "HAS", "UP", "MORE", "WHO",
      "WHEN", "DON'T", "SOME", "HAD", "THEM", "ANY", "THEIR", "IT'S", "ONLY",
      ";", "WHICH", "I'M", "BEEN", "OTHER", "WERE", "HOW", "THEN", "NOW",
      "HER", "THAN", "SHE", "WELL", "ALSO", "US", "VERY", "BECAUSE",
      "AM", "HERE", "COULD", "EVEN", "HIM", "INTO", "OUR", "MUCH",
      "TOO", "DID", "SHOULD", "OVER", "WANT", "THESE", "MAY", "WHERE", "MOST",
      "MANY", "THOSE", "DOES", "WHY", "PLEASE", "OFF", "GOING", "ITS", "I'VE",
      "DOWN", "THAT'S", "CAN'T", "YOU'RE", "DIDN'T", "ANOTHER", "AROUND",
      "MUST",  "FEW", "DOESN'T", "EVERY", "YES", "EACH", "MAYBE",
      "I'LL", "AWAY", "DOING", "OH", "ELSE", "ISN'T", "HE'S", "THERE'S", "HI",
      "WON'T", "OK", "THEY'RE", "YEAH", "MINE", "WE'RE", "WHAT'S", "SHALL",
      "SHE'S", "HELLO", "OKAY", "HERE'S", "-", "LESS"
  };
  
  /** @invisible */
  public static String[] STOP_WORDS = new String[] {
    "a", "about", "above", "across", "after", "afterwards", "again", "against",
    "all", "almost", "alone", "along", "already", "also", "although", "always", "am",
    "among", "amongst", "amoungst", "amount", "an", "and", "another", "any",
    "anyhow", "anyone", "anything", "anyway", "anywhere", "are", "around",
    "as", "at", "back", "be", "became", "because", "become", "becomes",
    "becoming", "been", "before", "beforehand", "behind", "being", "below",
     "beside", "besides", "between", "beyond", "bill", "both", "bottom", "but",
    "by", "call", "can", "cannot", "cant", "co", "computer", "con", "could",
    "couldnt", "cry", "de", "describe", "detail", "do", "done", "does", "down",
    "due", "during", "each", "eg", "eight", "either", "eleven", "else", "elsewhere",
    "empty", "enough", "etc", "even", "ever", "every", "everyone", "everything",
    "everywhere", "except", "few", "fifteen", "fify", "fill", "find",
    "fire", "first", "five", "for", "former", "formerly", "forty", "found",
    "four", "from", "front", "full", "further", "get", "give", "go",
    "had", "has", "hasnt", "have", "he", "hence", "her", "here", "hereafter",
    "hereby", "herein", "hereupon", "hers", "herself", "him", "himself", "his",
    "how", "however", "hundred", "i", "ie", "if", "in", "inc", "indeed", 
    "interest", "into", "is", "it", "its", "itself", "keep", "last", "latter",
    "latterly", "least", "less", "ltd", "made", "many", "may", "me", 
    "meanwhile", "might", "mill", "mine", "more", "moreover", "most", "mostly",
    "move", "much", "must", "my", "myself", "name", "namely", "neither", 
    "never", "nevertheless", "next", "nine", "no", "nobody", "none", "noone",
    "nor", "not", "nothing", "now", "nowhere", "of", "off", "often", "on", 
    "once", "one", "only", "onto", "or", "other", "others", "otherwise", "our",
    "ours", "ourselves", "out", "over", "own", "part", "per", "perhaps", 
    "please", "put", "rather", "re", "same", "see", "seem", "seemed", "seeming",
    "seems", "serious", "several", "she", "should", "show", "side", "since", 
    "sincere", "six", "sixty", "so", "some", "somehow", "someone", "something",
    "sometime", "sometimes", "somewhere", "still", "such", "system", "take", 
    "ten", "than", "that", "the", "their", "them", "themselves", "then", 
    "thence", "there", "thereafter", "thereby", "therefore", "therein", 
    "thereupon", "these", "they", "thick", "thin", "third", "this", "those",
    "though", "three", "through", "throughout", "thru", "thus", "to", 
    "together", "too", "top", "toward", "towards", "twelve", "twenty", "two",
    "un", "under", "until", "up", "upon", "us", "very", "via", "was", "we", 
    "well", "were", "what", "whatever", "when", "whence", "whenever", "where", 
    "whereafter", "whereas", "whereby", "wherein", "whereupon", "wherever", 
    "whether", "which", "while", "whither", "who", "whoever", "whole", "whom", 
    "whose", "why", "will", "with", "within", "without", "would", "yet", 
    "you", "your", "yours", "yourself", "yourselves", 
  }; 

  /**
   * Returns true if the word is a so-called 'stop-word'
   * @see #STOP_WORDS
   */
  public static boolean isStopWord(String keyword)
  {
    if (keyword == null || keyword.length()<1)
      return false;
    for (int i = 0; i < STOP_WORDS.length; i++)
      if (STOP_WORDS[i].equalsIgnoreCase(keyword))
        return true;
    return false;
  }   

  private static final String VOWELS = "aeiuo";

  /** @invisible */
  public static List asList(Set s)
  {
    List l = new ArrayList();
    if (s == null) return l;
    l.addAll(s);
    return l;
  }
  /** @invisible */
  public static List asList(int[] ints)
  {
    List l = new ArrayList();
    if (ints == null) return l;
    for (int i = 0; i < ints.length; i++)
      l.add(new Integer(ints[i]));
    return l;
  }
  /** @invisible */
  public static List asList(char[] chars)
  {
    List l = new ArrayList();
    if (chars == null) return l;
    for (int i = 0; i < chars.length; i++)
      l.add(new Character(chars[i]));
    return l;
  }
  /** @invisible */
  public static List asList(long[] longs)
  {
    List l = new ArrayList();
    if (longs == null) return l;
    for (int i = 0; i < longs.length; i++)
      l.add(new Long(longs[i]));
    return l;
  }
  /** @invisible */
  public static List asList(float[] floats)
  { 
    List l = new ArrayList();
    if (floats == null) return l;
    for (int i = 0; i < floats.length; i++)
      l.add(new Float(floats[i]));
    return l;
  }
  /** @invisible */
  public static List asList(Object[] o)
  {    
    if (o == null)
      return new ArrayList();
    return Arrays.asList(o);
  }
  
  /** @invisible */
  public static final float[] unhex(int hexColor){
    // note: not handling alphas...
    int r = hexColor >> 16;
    int temp = hexColor ^ r << 16;
    int g = temp >> 8;
    int b = temp ^ g << 8;
    return new float[]{r,g,b,255};
  }
  
  /** @invisible */
  public static final String upperCaseFirst(String value) {
		return Character.toString(value.charAt(0)).toUpperCase() + value.substring(1);
	}

  /**
   * Unpack a binary String into an int.
   * i.e. unbinary("00001000") would return 8.
   * @invisible 
   */
  static final public int unbinary(String what) {
    return Integer.parseInt(what, 2);
  }
  
  /**  @invisible */
  public static void die(PApplet papplet, String errStr)
  {
    if (papplet != null)
      try {
        papplet.die(errStr);
      } catch (Throwable e) {}
    else
      throw new RuntimeException(errStr);      
  }
  
  /** @invisible */
  public static byte[] loadBytes(PApplet p, InputStream is)
  {
    return PApplet.loadBytes(is);
  }
  
  /** @invisible */
  public static void pElapsed(long timestamp)
  {
    System.out.println(elapsedStr(timestamp));       
  }
  
  /**
   * Returns the passed in Object after dynamically casting
   * it to the specified interface. An example:
   * <pre>
      Object o = new Object() {
        public void run() {
          System.out.println("running...");
      }};
      Runnable r = (Runnable)RiTa.dynamicCast(o, Runnable.class);
      r.run();</pre>
   * Note: 'toCast' must be a an instance of a public class.
   */
  public static Object dynamicCast(Object toCast, Class iface) {
//System.out.println("RiTa.dynamicCast("+o+","+iface+")");
    RiDynamicObject rdo = new RiDynamicObject(toCast, iface);
    Object casted = rdo.getDelegate();
    return casted;
  }
  
  /**
   * Returns the passed in Object after dynamically casting
   * it to the specified interfaces.<br>
   * Note: 'toCast' must be a an instance of a public class.
   * @see #dynamicCast(Object, Class)
   */
  public static Object dynamicCast(Object toCast, Class[] ifaces) {
    RiDynamicObject rdo = new RiDynamicObject(toCast, ifaces);
    return rdo.getDelegate();
  }
  
  /** 
   * Returns time since <code>start</code> as string, e.g., '4.33s'
   *  @invisible 
   */
  public static String elapsedStr(long start) {
    if (DF == null) DF = new DecimalFormat("#.##");
    double time = (System.currentTimeMillis()-start)/1000d;
    return DF.format(time)+"s";
  }   private static DecimalFormat DF = null;
  /** 
   * Returns time since start of program as string, e.g., '4.33s'
   * @invisible   
   */
  public static String elapsedStr() {
    return elapsedStr(millisOffset);
  }
  
  /** 
   * Returns time since start of program as float (in seconds)
   *  @invisible   
   */
  public static float elapsed() {
    return elapsed(millisOffset);
  }
 /** Returns time since <code>start</code> as float (in seconds)    */
  public static float elapsed(long start) {
    return ((System.currentTimeMillis()-start)/1000f);
  } 
  
  /** 
   * Returns time since 'start' of program in ms
   * @invisible  
   */
  public static int millis() {
    return (int)(System.currentTimeMillis()-millisOffset);
  }  
  
  /** 
   * Returns time since <code>start</code> as an int (milliseconds)
   * @invisible  
   */
  public static int millis(long start) {
    return (int)(System.currentTimeMillis()-start);
  }  

  /** Delegates to the default sentencer parser to split <code>text</code> into sentences */
  public static String[] splitSentences(String text) {
    return RiSplitter.getInstance().splitSentences(text);
  }  
  
  /**
   * Uses the default WordTokenizer to split the line into words 
   * and places, then the results in <code>result</code>
   * @see RiTokenizer
   */
  public static void tokenize(String line, List result)
  {
    RiTokenizer.getInstance().tokenize(line, result);
  }
  /**
   * Uses the default WordTokenizer to split the line into words
   * @see RiTokenizer
   */
  public static String[] tokenize(String line) {
    return RiTokenizer.getInstance().tokenize(line);
  }
  
  /** 
   * Uses the default PosTagger to tag the input with the PENN tag set
   * @see RiPosTagger
   */
  public static String[] posTag(String[] tokens)
  {    
    return getTagger(null).tag(tokens);
  }  
  /**
   * Uses the default PosTagger to tag
   * (and add features for) each element of <code>words</code>.
   * @invisible 
   */
  public static String[] posTag(FeaturedIF[] tokens)
  {    
    return getTagger(null).tag(tokens);
  }  
  
  private static RiPosTagger getTagger(PApplet p) {
    return RiPosTagger.getInstance(p);
  }
  
  // ---------------------------------------------------------
    /**
   * Creates a File with <code>fname</code> and writes the String 'text' to it
   * @param fileName by which to create the File
   * @param text contents to be written to the file as a String
   */
  public static void writeFile(String fileName, String text)
  {
    BufferedWriter writer = null;
    try {
      writer = new BufferedWriter(new FileWriter(fileName));
      writer.write(text);
      writer.flush();
    } 
    catch (Exception e) {
      throw new RiTaException(e);
    }
    finally {
      try {
        if (writer != null) writer.close();
      }
      catch (IOException e) {}
      writer = null;
    }
  }
  
  /**
   * Creates a File with <code>fname</code> and writes the String[] 'lines' to it
   * @param fileName by which to create the File
   * @param lines contents to be written to the file
   */
  public static void writeFile(String fileName, String[] lines)
  {
    BufferedWriter writer = null;
    try {
      writer = new BufferedWriter(new FileWriter(fileName));
      for (int i = 0; i < lines.length; i++) {
        writer.write(lines[i]+LINE_BREAK);  
      }      
      writer.flush();
    } 
    catch (Exception e) {
      throw new RiTaException(e);
    }
    finally {
      try {
        if (writer != null) writer.close();
      }
      catch (IOException e) {}
      writer = null;
    }
  }
  
  /**
   * Creates a file with <code>fileName</code> and writes the contents 
   * of <code>persistent</code> to it in XML format
   * @invisible 
   */
  public static void persist(Xmlable persistent, String fileName)
  {
    writeFile(fileName, persistent.toXml());
  }
  
  /**
   * Creates and returns an Xmlable from the contents of String <code>xml</code>
   * after calling the no-argument constructor.  
   * @invisible 
   */
  public static Xmlable create(PApplet p, String xml)
  {
    Map props = RiTa.propertiesFromXml(xml);
//System.out.println("Props: "+props);
    String clz = (String) props.get("class"); 
    Xmlable xobj = null;
    try {
      Class c = Class.forName(clz);
      Constructor ctor = c.getConstructor(new Class[]{PApplet.class});
      xobj = (Xmlable)ctor.newInstance(new Object[]{p}); 
    }
    catch (Exception e) {
      throw new RiTaException(e);
    }
    xobj.initialize(p, props);
    return xobj;
  }
  
  /**
   * Creates and returns an Xmlable from the XML contents of <code>fileName</code>
   * after calling the no-argument constructor. 
   * @invisible  
   */
  public static Xmlable createFromFile(PApplet p, String fileName)
  {
    String xml = loadString(p, fileName);
    if (!xml.startsWith("<"+Xmlable.RITA_XML+" "))
      throw new RiTaException("Bad start xml: "+xml);
    return create(p, xml);
  }

  /** Converts the List to a String array */
  public static String[] strArr(List l)
  {
    if (l == null || l.size()==0) return new String[0];
    return (String[])l.toArray(new String[l.size()]);
  }
  
  /**
   * Removes white-space and line breaks from start and end of String
   * @param s String to be chomped
   * @return string without starting or ending white-space or line-breaks
   */
  public static String chomp(String s)
  {
    if (CHOMP == null) 
     CHOMP = Pattern.compile("\\s+$|^\\s+");
    Matcher m = CHOMP.matcher(s);
    return m.replaceAll("");
  } static Pattern CHOMP;
  
  /** @invisible */
  public static RiProbable select(Collection c, boolean probabalisticSelect) {
    return (RiProbable) (probabalisticSelect ? probabalisticSelect(c) : random(c));
  }
  
  /** @invisible */
  public static RiProbable probabalisticSelect(Collection c)
  {    
    //System.out.println("RiTa.probabalisticSelect("+c+", size="+c.size()+")");
    switch (c.size()) 
    {
      case  0:  return null;
      
      case  1:  return (RiProbable)c.iterator().next();
      
      default: // pick from multiple children
      {
        // select based on frequency
        double pTotal = 0, selector = Math.random();
        for (Iterator iter = c.iterator(); iter.hasNext();)
        {
          RiProbable pr = (RiProbable) iter.next();
          pTotal += pr.getProbability();
          if (selector < pTotal)           
            return pr;
        }
      }
      throw new RiTaException("Invalid State in RiTa.probabalisticSelect()");
    }   
  }

  /** @invisible */
  public static boolean lastCharIs(String string, char c)
  {
    return string.charAt(string.length()-1) == c;
  }

  /** @invisible */
  public static boolean lastCharMatches(String string, char[] chars)
  {   
    char c = string.charAt(string.length()-1);   
    for (int i = 0; i < chars.length; i++)
      if (c==chars[i])
        return true;
    return false;
  }
  
  /** Returns true if array contains the item, else false */
  public static boolean contains(Object[] array, Object item)
  {
    for (int i = 0; i < array.length; i++)
      if (array[i].equals(item))
        return true;
    return false;
  }

  /**
   * Set the directory from which statistical models should be loaded<p>
   * Note: expects an absolute path: e.g., '/Users/jill/models'
   * @invisible
   */
  public static void setModelDir(String modelDirectory) {
    RiObjectME.setModelDir(modelDirectory);
  }
  
  /** 
   * Returns the directory from which statistical models are loaded 
   * @invisible
   */
  public static String getModelDir() {
    return RiObjectME.getModelDir();
  }

  /** @invisible */ 
  public static boolean isAbsolutePath(String fileName) {
    return (fileName.startsWith("/") || 
     fileName.matches("^[A-Za-z]:")); // hmmmmm... 'driveA:\\'?
  }

  /**
   * Returns a decimal formatted to one place, (eg. #.#)
   * @invisible 
   */
  public static String decimalFormat(double d){
    if (DF1==null) DF1 = new DecimalFormat("#.#");
    return DF1.format(d);
  }private static DecimalFormat DF1;
  
  
  /**
   * Return true if 'search' is a substring of 'full' 
   */    
  public static boolean contains(CharSequence word, CharSequence search, boolean ignoreCase)
  {     
    if (word == null || search == null) return false;
    if (word instanceof RiText)
      word = ((RiText)word).getText();
    if (search instanceof RiText)
      search = ((RiText)search).getText();
    boolean result = word.toString().indexOf(search.toString()) > -1;
    if (!result && ignoreCase) {
      try {
        Matcher m = Pattern.compile(".*"+search+".*", Pattern.CASE_INSENSITIVE).matcher(word);
        result = m.matches();
      } catch (Exception e) {
        System.err.println("[WARN] RiTa.contains("+word+","+search+") threw Exception: "+e.getMessage()); 
      }
    }
    return result;
  }
  
  /**
   * Hides the cursor for the duration of the program (requires an application or signed applet).
   */
  public static void hideCursor(PApplet p)
  {
    try {
      p.setCursor(Toolkit.getDefaultToolkit().createCustomCursor
        (new ImageIcon("").getImage(), new Point(0, 0), ""));
    }
    catch (java.security.AccessControlException e) {
      System.err.println("[WARN] Unable to hide the cursor due to a security exception;"
      		+ " if you are using this method in an applet, you will need to sign it");
    }
    catch (Exception e2) {
      System.err.println("[WARN] Unable to hide the cursor!"+e2.getMessage());
    }
  }
  
  /**
   * Return true if any of the words in 'searches' are a substring of 'full' 
   */  
  public static boolean contains(CharSequence word, CharSequence[] searches, boolean ignoreCase) {
    if (word == null || searches == null || searches.length<1)
      return false;
    for (int i = 0; i < searches.length; i++) {
      if (contains(word, searches[i], ignoreCase))
        return true;
    }
    return false;
  }
  
  private static void containsTests() 
  {
    String test = "dogwalker";
    String[] cands = { "dog","walk", "dogwalker", "Dog", "DOG", "wAlk", "WALK", "DOGWALKER", "DOGWALKERS", "dogwalkers","dogwalks", "dogwallker", "", null};

    for (int i = 0; i < cands.length; i++) {
      System.out.println(test +" contains "+cands[i]+" (ic=f) -> "+contains(test, cands[i], false));
      System.out.println(test +" contains "+cands[i]+" (ic=t) -> "+contains(test, cands[i], true));
      System.out.println();
    }    
    
    test = null;
    System.out.println(test +" contains "+cands[0]+" (ic=f) -> "+contains(test, cands[0], false));
    System.out.println(test +" contains "+cands[0]+" (ic=t) -> "+contains(test, cands[0], true));
    System.out.println();
    
    test="dogwalker";
    System.out.println(test +" contains("+asList(cands)+") (ic=f) -> "+contains(test, cands, false));
    System.out.println(test +" contains("+asList(cands)+") (ic=t) -> "+contains(test, cands, true));
    System.out.println();
    
    String[] cands2 = {"DOGWALKER", "DOGWALKERS", "dogwalkers","dogwalks", "dogwallker", null};
    System.out.println(test +" contains("+asList(cands2)+") (ic=f) -> "+contains(test, cands2, false));
    System.out.println(test +" contains("+asList(cands2)+") (ic=t) -> "+contains(test, cands2, true));
    System.out.println();
      
    String[] cands3 = {"DOGWALKER", "DOGWALKERS", "dog" };
    System.out.println(test +" contains("+asList(cands3)+") (ic=f) -> "+contains(test, cands3, false));
    System.out.println(test +" contains("+asList(cands3)+") (ic=t) -> "+contains(test, cands3, true));
  }

  public static boolean is3D(PGraphics graphics) {
    return RiText.is3D(graphics);
  }

  ////////////////////////////////////////////////////////////////////
  
  /**
   * Utility method to sort a map by its values. 
   * @invisible
   */
  public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue( Map<K, V> map )
  {
      // from: http://stackoverflow.com/questions/109383/how-to-sort-a-mapkey-value-on-the-values-in-java
      List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>( map.entrySet() );
      
      Collections.sort( list, new Comparator<Map.Entry<K, V>>()
      {
          public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
          {
              return (o1.getValue()).compareTo( o2.getValue() );
          }
      } );

      Map<K, V> result = new LinkedHashMap<K, V>();
      for (Map.Entry<K, V> entry : list)
      {
          result.put( entry.getKey(), entry.getValue() );
      }
      return result;
  }

  /**
   * Removes the regex pattern from either side of the string.
   */
  public static String trimRegex(String input, String regex) 
  {
    String result = input;
    Pattern multiplierPattern = Pattern.compile(regex+"(.*)");
    Matcher m = multiplierPattern.matcher(result);
    if (m.matches() && m.groupCount()==1) {
      result = m.group(1);
      //System.out.println(m.groupCount()+" HIT1: '"+result+"'");
    }
    
    multiplierPattern = Pattern.compile("(.*?)"+regex);
    m = multiplierPattern.matcher(result);

    if (m.matches() && m.groupCount()==1) {
      result = m.group(1);
      //System.out.println(m.groupCount()+" HIT2: '"+result+"'");
    }
    return result;
  }
  
  public static void main(String[] args)
  {
    //RiTa.SILENT = true;
    System.out.println("'"+trimRegex(" \t he \t llo  \t ", "\\s+")+"'"); 
  }


  
}// end
