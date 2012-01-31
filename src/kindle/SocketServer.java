package kindle;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.Vector;

import router66.WriteMsg;

/**
 * Title:        Sample Server
 * Description:  This utility will accept input from a socket, posting back to the socket before closing the link.
 * It is intended as a template for coders to base servers on. Please report bugs to brad at kieser.net
 * Copyright:    Copyright (c) 2002
 * Company:      Kieser.net
 * @author B. Kieser
 * @version 1.0
 */

public class SocketServer {
  private static int port=12345, maxConnections=0;
  // Listen for incoming connections and handle them
  //public static void main(String[] args) {
  public SocketServer(Vector<WriteMsg> _writeMsgs) {
	int i=0;

    try{
      ServerSocket listener = new ServerSocket(port);
      System.out.println("Socket server started.");
      Socket server;

      while((i++ < maxConnections) || (maxConnections == 0)){
        doComms connection;

        server = listener.accept();
        doComms conn_c= new doComms(server, _writeMsgs);
        Thread t = new Thread(conn_c);
        t.start();
      }
    } catch (IOException ioe) {
      System.out.println("IOException on socket listen: " + ioe);
      ioe.printStackTrace();
    }
  }

}

class doComms implements Runnable {
	private Vector<WriteMsg> writeMsgs;

    private Socket server;
    private String line,input;

    doComms(Socket server, Vector<WriteMsg> _writeMsgs) {
      writeMsgs = _writeMsgs;
      this.server=server;
    }

    public void run () {

      input="";

      try {
        // Get input from the client
    	BufferedReader in = new BufferedReader (new InputStreamReader(server.getInputStream()));
        PrintStream out = new PrintStream(server.getOutputStream());

        while((line = in.readLine()) != null && !line.equals(".")) {
          input=input + line;
          //out.println("I got:" + line);
          out.println(writeMsgs.lastElement().getMsg());
        }

        // Now write to the client

        System.out.println("Overall message is:" + input);
        out.println("Overall message is:" + input);

        server.close();
        
      } catch (IOException ioe) {
        System.out.println("IOException on socket listen: " + ioe);
        ioe.printStackTrace();
      }
    }
}