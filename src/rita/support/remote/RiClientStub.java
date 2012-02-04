package rita.support.remote;

import rita.*;
import java.io.*;
import java.net.Socket;
import java.util.*;

import rita.support.*;
import rita.support.me.*;

/*
 * TODO: handle different hosts and ports
 */
public class RiClientStub implements RemoteConstants
{
  public static int serverPort = RiTa.DEFAULT_SERVER_PORT;
  public static String serverHost = "127.0.0.1";
  
  protected static RiClientStub instance;
  
  protected HashMap initMap;
  protected String calleeName;  

  public static RiClientStub getProxy() {
    if (instance == null)
      instance = new RiClientStub(null);
    return instance;
  }
  
  public RiClientStub(Class delegatesTo) {
    this(delegatesTo, serverHost, serverPort);
  }
  
  public RiClientStub(int port) {
    this(null, serverHost, port);
  }
  
  public RiClientStub(String host, int port) {
    this(null, host, port);
  }
  
  public RiClientStub(Class delegatesTo, String host, int port) {
//System.out.println("RiClientStub.RiClientStub("+delegatesTo+")");
    this.port = port;
    serverHost = host;
    serverPort = port;
    if (delegatesTo != null) {
      calleeName = delegatesTo.getName();
      initMap = new HashMap();      
      initMap.put("class", calleeName);
    }
    instance = this;
  }
  
  public String[] listToStrArr(List l)
  {
    return (String[])l.toArray(new String[l.size()]);
  }
  
  
  public String[] toStrArr(String str)
  {
    String[] result = null;
    if (str != null) {
      if (str.indexOf(ARR_DELIM)>-1) 
        result = str.split(ARR_DELIM);
      else
        result = new String[] {str};
        //throw new RiTaException("RiRemotable.toStrArr("+str+")");
    } 
    return result;
  }
/*  
  protected void checkServerState(Object o, String methodDesc)
  {
    if (RiTa.isServerEnabled()) throw new RiTaException("Invalid call to "
      + o.getClass().getName()+"."+methodDesc+" with serverEnabled=true" + 
      "\n         Have you started the RiTaServer in a command shell?");
  } */
  
  PrintWriter out;
  BufferedReader in;
  Socket kkSocket;
  boolean connected;
  int port;  
  
  public void createRemote() 
  {
    this.createRemote(initMap);
  }
  
  public void createRemote(Map m)  // remove??
  {
    this.exec("createRemote", m, Map.class);
  }  

  public void connect()
  {
    try
    {
      kkSocket = new Socket("127.0.0.1", port);
      out = new PrintWriter(kkSocket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()));
      connected = true;
    }
    catch (java.net.ConnectException ce) {
      throw new RiTaException("Unable to connect to the RiTaServer on port "+
        port+"...\n          Have you started the RiTaServer in a command shell?");
    }
    catch (Exception e)
    {
      throw new RiTaException("Error: "+e.getMessage(),e);      
    }  
  }

  protected String serializeCall
    (String callerName, String method, Object[] args, Class[] types) 
  {
//System.out.println("RiClientStub.serializeCall("+callerName+"."+method+"()"+")");    
    String argStr = "";
    if (args != null) {
      
      for (int i = 0; i < args.length; i++)
      {
        if (args[i] == null) throw new RiTaException
          ("Null arg at idx="+i+" arr="+RiTa.asList(args));
        
        if (args[i] instanceof Map) {
          argStr += RiTa.mapToString((Map)args[i]);            
        } 
        else if (args[i] instanceof Object[]) {
          argStr +=  RiTa.join((Object[])args[i], ARR_DELIM);
        }
        else {
          argStr += args[i].toString();
        }
        if (i < args.length - 1)
          argStr += ARG_DELIM;
      }
    }
    String typeStr = "";
    if (types != null)
    {
      if (types.length != args.length)
        throw new RiTaException("Arg/Type mismatch: args=" 
          + RiTa.asList(args)+" types="+ RiTa.asList(types));
      
      for (int i = 0; i < types.length; i++)
      {
        if (types[i] == null) throw new RiTaException
          ("Null type at idx=" + i + " arr="+ RiTa.asList(args));

        typeStr += types[i].getName();       
        if (i < types.length - 1)
          typeStr += TYPE_DELIM;
      }
    }
      
    String call = callerName+DELIM+method+DELIM+argStr+DELIM+typeStr;
    
//System.out.println("CALL: "+call);
    
    return call;
  }
  
  protected String serializeCall
    (String callerName, String method, Object arg, Class type) 
  {    
    return callerName + DELIM + method + DELIM 
      + arg.toString() + DELIM + type.getName();
  }  
   
  public String exec(String method){
    return exec(method, (Object[])null, (Class[])null);
  }
    
  public String exec(String method, Object arg) {   
    return exec(method, arg, arg.getClass());
  }
 
  public String exec(String method, Object arg, Class type) {   
    return exec(method, new Object[]{arg}, new Class[]{type});
  }
  
  public String exec(String method, int arg) {
    return exec(method, new Object[]{arg}, new Class[]{Integer.TYPE});
  }
  
  public String exec(String method, boolean arg) {
    return exec(method, new Boolean(arg), Boolean.TYPE);
  }
  
  public String exec(String method, float arg){
    return exec(method, new Float(arg), Float.TYPE);
  }
  
  public String exec(String method, char arg){
    return exec(method, new Character(arg), Character.TYPE);
  }  
  
  public String exec(String method, Object[] args, Class[] types)  
  {
//System.out.println("RiClientStub.exec("+method+", "+args+", "+types+")");
    if (!connected) connect();
           
    try
    {
      String msg = serializeCall(calleeName, method, args, types);
//System.out.println("CALL: "+msg);      
      return sendCallMsg(msg, out, in);
    }
    catch (IOException e)
    {     
      throw new RiTaException(e);
    }
    finally {
      connected = false;
      disconnect(); 
    }
  }

  private static String sendCallMsg(String msg, PrintWriter out, BufferedReader in) throws IOException
  {
    String result = null;

    out.println(msg);
//System.out.println("SENT: '"+msg+"'");
    String fromServer = null;
    while ((fromServer = in.readLine()) != null) {       
//System.out.println("read: "+fromServer);
      if (fromServer.endsWith("]")) break;
    }
    if (fromServer == null) {
      System.out.println("[WARN] Unexpected reply from server:  "+
       fromServer+"\n       check the server console for an error message");
    }
    else if (fromServer != null && fromServer.startsWith("[") && fromServer.length()>2) {      
      result = fromServer.substring(1, fromServer.length()-1);
    }  
    else {
      if (!fromServer.equals("[]"))
        System.out.println("UNEXPECTED RESPONSE: '"+fromServer+"'");
    }
    return result;
  }

  protected void disconnect()
  {
    try {
      if (out != null) out.close();
      if (in != null) in.close();
      if (kkSocket != null) kkSocket.close();
    }
    catch (IOException e) {}
  }  

  public void setPort(int port)
  {
    this.port = port;
  }
    
  public void refreshServer() {
    invokeOnServer("refresh");
  }
  
  public String invokeOnServer(String methodName) {
    try
    {
      if (!connected) connect();
      String msg = RiTaServer.class.getName()+DELIM+methodName;
      return sendCallMsg(msg, out, in);
    }
    catch (IOException e) {
      throw new RiTaException(e);
    }
    finally {
      connected = false;
      disconnect(); 
    }
  }
  
  public static void setServerHost(String host) {
    serverHost = host;
  }
  
  public static String getServerHost() {
    return serverHost;
  }
  
  public static void setServerPort(int port) {
    serverPort = port;
  }
  
  public static int getServerPort() {
    return serverPort;
  }
  
  public int getRemoteObjectCount() {    
    return Integer.parseInt(invokeOnServer("getObjectCount"));
  }

}// end

