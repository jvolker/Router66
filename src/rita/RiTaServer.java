package rita;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import rita.support.remote.RemoteConstants;
import rita.support.remote.RiRemotable;


/*
 * TODO:
 *   Need to finish run-server scripts...  
 */

/**
 * Another way of using the RiTa objects is in a server mode. In server mode, 
 * the RiTaServer keeps running indefinitely; it accepts commands via a socket, 
 * executes these  commands, and sends the results back through the socket. 
 * You can start the RiTaServer by double-clicking on the run-server script 
 * for your platform (see below) found within the standard RiTa distibution 
 * (or from the command line).
 * <p> 
 * One common use of the RiTaServer is for objects that may have 
 * expensive initialization routines (e.g. building a large n-gram model
 * from text files). Rather than incurring this loading cost each time  
 * the program is run (in development, for example), the RiTaServer enables 
 * the model to remain in memory while your program can be stopped and started
 * as often as you like.
 * <p> 
 * For example:
 * 
 *   <pre>   RiTa.useServer();   // add this one line
 *    
 *   RiMarkov rm = new RiMarkov(this, 3);
 *   rm.loadFile("war_peace.txt"); 
 *   String[] sents = rm.generateSentences(10);
 *   for (int i = 0; i < sents.length; i++) 
 *     System.out.println(sents[i]);</pre>
 *     
 *          
 * <br>Note: before enabling the server from your code (as above), make sure
 * to start the server using either run-server.sh (mac/linux) or run-server.bat (win), 
 * both of which can be found in the SKETCH_FOLDER/libraries/rita/ folder
 * (On the Mac, this is (most likely): ~/Documents/Processing/libraries/rita/)<p>
 * 
 * <br>Current objects supporting server-based operation include:<ul>
 * <li>RiMarkov
 * <li>RiChunker
 * <li>RiParser
 * <li>RiWordnet
 * <li>RiPosTagger</ul>
 * 
 * <br>
 * Note: although the default (and most common case) is to run
 * the server locally, it may also be run on a remote machine, 
 * and can even provide content for publicly accessible applets.
 * <!--The server can be run manually (outside of Processing) as follows:
 * <pre>          java rita.RiTaServer [port] (default=4444)</pre-->
 * <p>
 * <br>See related:<ul>
 * <li>RiTa.useServer();
 * <li>RiTa.useServer(boolean);
 * <li>RiTa.refreshServer(); &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;// to clear all server objects 
 * </ul>
 * @invisible
 */
public class RiTaServer extends Thread implements Runnable, RemoteConstants 
{
  /** @invisible  */
  public static int port = RiTa.DEFAULT_SERVER_PORT;
 
  static final Class[] CLASS_ARR_MAP = { Map.class }; 
   
  static Map remotables = new HashMap();
  static ServerSocket serverSocket;
    
  Socket clientSocket;      // need to go in client-obj?
  BufferedReader is;      // need to go in client-obj?
  BufferedWriter os;    // need to go in client-obj?
  boolean running = false; 
  boolean lazyLoading = true;

  /** @invisible */
  public RiTaServer(Socket cs) {
    clientSocket = cs; 
  } 

  private static boolean usageOnly(String args[]) {
    if (args.length > 1 || (args.length == 1 &&
      (args[0].equalsIgnoreCase("-usage") ||
      args[0].equalsIgnoreCase("-help") ||
      args[0].equalsIgnoreCase("-h")))) 
    {
      System.out.println("Usage:  java rita.RiTaServer [<port>]");
      System.out.println("  The default port is 4444.");
      return true;
    }
    else
      return false;
  }
  
  /** @invisible */
  public void run() {
    //System.out.println("client request...");
    try {
      os = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
      is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      running = true;
    }
    catch (IOException e) {
      printMsg("Cannot handle client connection: "+e.getMessage());
      cleanup();
      return;
    }
    
    String result = "[]";
    try {
      String input = is.readLine();
      
//System.out.println("read: "+input);
      
      String[] args = input.split(DELIM);
      if (args == null || args.length < 2 || args.length > 4)
        throw new RiTaException("Bad arg-string: "+input); 

/*for (int i = 0; i < args.length; i++)
  System.out.println(i+") ARGS: "+args[i]);
  */    
      String className  = args[0], methodName = args[1];
      String parameters = args.length>2 ? args[2] : null;
//System.out.println("PARAMS: "+parameters);      
      String argtypes   = args.length>3 ? args[3] : null;
      
//System.out.println("[RS] Call: "+className+", "+methodName+", args="+parameters+", types="+argtypes+")");      
//System.out.println("[INFO] Call: "+methodName+", checking for '"+className+"' in "+remotables);
      
      Object ro = remotables.get(className);
      if (methodName.equals("createRemote")) 
      {           
        if (ro == null) 
        {
//System.out.println("NEW CREATE: "+className+"====================================");          
          Map initMap = RiTa.stringToMap(parameters);
        
          // load the class
          Class c = null;
          try {
            c = Class.forName(className);
          } 
          catch (Throwable e) {
            String cp = "";  // error info
            try {
              cp += "\n        from: "+System.getProperty("user.dir")+"\n";                         
              cp += "        with classpath: "+System.getProperty("java.class.path")+"\n";
            } catch (Throwable ex) {
             cp += " [no further info]";
            }
            System.err.println("\n[ERROR] Unable to load class: "+className+cp);           
          }
          
          // construct the object
          if (c != null) 
            ro = RiTa.invoke(c, methodName, CLASS_ARR_MAP, new Object[] { initMap });          
          if (ro == null) 
            throw new RiTaException("Unable to create remote object: "+className);          
                      
          // cache object for later          
          if (!RiTa.SILENT)System.out.println("[INFO] RiTaServer created: "+className);
          remotables.put(className, ro);
        }      
      }
      else // a regular method call 
      {
//System.out.println("METHOD CALL ====================================");
        if (ro == null)  {
          if (!className.equals(getClass().getName()))                  
            throw new RiTaException("Attempt to access remote object("+className
            +"."+methodName+") before an instance exists: "+remotables);
          ro = this; // local method
        }
       
        Class[] types = parseTypes(argtypes);
        Object[] params = parseArgs(types, parameters);      
        Object returned = RiTa.invoke(ro, methodName, types, params);
        result = formatResult(returned);
//System.out.println("RESULT: "+returned+"\n         "+result);
      }
      os.write(result, 0, result.length());
      os.flush();      
    }
    catch (Throwable e) {
      printMsg("I/O error while processing client request: "+e.getMessage());
      e.printStackTrace();
      try {
        os.write(result);
      }
      catch (IOException e1) {}
    }
    finally {
      cleanup();
    }
  }

  private Object[] parseArgs(Class[] types, String args)
  {    
    if (args == null) return null;
    
    String[] arr = args.split(ARG_DELIM);   
    Object[] result = new Object[arr.length];
    for (int i = 0; i < arr.length; i++) 
    {   
      if (types[i].isArray())  
        result[i] =  strToArr(arr[i]);
      else if (types[i]==Integer.TYPE)
        result[i] = Integer.parseInt(arr[i]);
      else if (types[i]==Boolean.TYPE)
        result[i] = Boolean.parseBoolean(arr[i]);
      else if (types[i]==Float.TYPE)
        result[i] = Float.parseFloat(arr[i]);
      else if (types[i]==Character.TYPE)
        result[i] = arr[i].charAt(0);
      else
        result[i] = arr[i];
    }
    
/*System.out.println("RiTaServer.parseArgs()-----------");    
for (int i = 0; i < result.length; i++)
  System.out.println(i+") TYPE: "+result[i]);
System.out.println("-----------------------------------");*/

    return result;
  }
  
  private Class[] parseTypes(String argtypes)
  {
    if (argtypes == null) return null;
    
    String[] types = argtypes.split(TYPE_DELIM);  //cache?
    Class[] result = new Class[types.length];
    
    try {
      for (int i = 0; i < result.length; i++)
        if (types[i].equals("boolean"))
          result[i] = Boolean.TYPE;
        else if (types[i].equals("int"))
          result[i] = Integer.TYPE;
        else if (types[i].equals("float"))
          result[i] = Float.TYPE;
        else if (types[i].equals("char"))
          result[i] = Character.TYPE;      
        else
          result[i] = Class.forName(types[i]);
    }
    catch (ClassNotFoundException e) {
      throw new RiTaException(e);
    }
    
/*System.out.println("RiTaServer.parseTypes()------------");
for (int i = 0; i < result.length; i++)
  System.out.println(i+") TYPE: "+result[i]);
System.out.println("-----------------------------------");*/

    return result;    
  }
  

  protected Object[] strToArr(String str)
  {
    Object[] result = null;
    if (str != null) {
      if (str.indexOf(ARR_DELIM)>-1) 
        result = str.split(ARR_DELIM);
      else
        result = new String[] {str};
        //throw new RiTaException("RiRemotable.toStrArr("+str+")");
    } 
    return result;
  }
  
  /**
   * Clears all objects from the server so that new ones will
   * be created on subsequent calls.
   *  @invisible 
   */
  public void refresh()
  {
//System.out.println("RiTaServer.refresh() :: "+remotables);
    for (Iterator iter = remotables.values().iterator(); iter.hasNext();) {
      RiRemotable rr = (RiRemotable) iter.next();
      rr.destroy();
      rr = null;
    }
    remotables.clear();
    if (!RiTa.SILENT)System.out.println("[INFO] RitaServer refreshed...");
  }
  
  private String formatResult(Object o)
  {
    String result = ""; 
    if (o != null) {
//System.out.println("RiTaServer.formatResult("+o+") "+o.getClass()+" = '"+o.toString()+"'");
      if (o instanceof String[]) {
        String[] tmp = (String[]) o;
        for (int i = 0; i < tmp.length; i++)
        {
          result += tmp[i];
          if (i < tmp.length-1)
            result += RemoteConstants.ARR_DELIM;
        }
        //System.out.println("(RS) Returning String[]: "+result);
      }
      else if (o instanceof Map)
        result = RiTa.mapToString((Map)o);
      else
        result = o.toString();
    }
    
    return LB+result+RB;
  }
  

  private void cleanup() {
    try {
      if (is != null)
        is.close();
      if (os != null)
        os.close();
      if (clientSocket != null)
        clientSocket.close();
    }
    catch (Exception e) {
      //printMsg("[WARN] RiTaServer: I/O error while cleaning up: "+e.getMessage());      
    }
  }

  private static void processCommandLine(String args[]) {
    if (args.length != 1) return;
    try {
      port = Integer.parseInt(args[0]);
    }
    catch (NumberFormatException e) {
      System.out.println(" [WARN] Bad port argument: "+args[0]+ " ignored");
    }
    if (port < 1 || port > 65535) {
      port = RiTa.DEFAULT_SERVER_PORT;
      printMsg("[INFO] RiTaServer switching to port " + port + "...");
    }
  }

  private static void printMsg(String msg) {
    System.out.println("[INFO] RiTaServer:  " + msg);
  }

  /** @invisible */
  public static int getObjectCount() {      // testing only
    return remotables.size(); 
  }
  /** @invisible */
  public static String[] getObjectNames() { // testing only    
    int idx = 0;
    String[] ret = new String[remotables.size()];    
    for (Iterator i = remotables.keySet().iterator(); i.hasNext();)
      ret[idx++] = (String)remotables.get(i.next());
    return ret;
  }
  
  /** @invisible */
  public static void init(String[] args)
  {
    processCommandLine(args);
    try {
      remotables = new HashMap();
      serverSocket = new ServerSocket(port);
      if (!RiTa.SILENT)System.out.println("[INFO] RiTaServer running on port "+port); 
    }
    catch (IOException e) {
      printMsg("Cannot create server socket " +
        "on port:  " + port + ".  Exiting..."); 
      System.exit(0);
    }
    while (true)
    {
      try {
        Socket clientSocket = serverSocket.accept();
        RiTaServer es = new RiTaServer(clientSocket);
        es.start();
        Thread.sleep(20);
      }
      catch (Exception e) {
        printMsg("Cannot accept client connection.");
      }      
    }
  }

  public static void main(String args[]) {
    if (usageOnly(args)) 
      System.exit(0); 
    RiTaServer.init(args);   
  }
}
