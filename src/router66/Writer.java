package router66;

import java.io.PrintWriter;
import java.util.Vector;

import kindle.SocketServer;

import processing.core.PApplet;
import processing.net.Client;
import processing.net.Server;

public class Writer {
	private Vector<WriteMsg> writeMsgs = new Vector<WriteMsg>();
	private PrintWriter output;
	private Client c;
	private String request;
	private PApplet p5;
	private SocketServer server;
	
	public Writer(){
		server = new SocketServer(writeMsgs);
	}
	
	public void addMsg(WriteMsg msg){
		writeMsgs.add(msg);
		System.out.println("Writer: "+writeMsgs.lastElement().getMsg());
		//writeFile(writeMsgs.lastElement().getMsg());
	}
}
