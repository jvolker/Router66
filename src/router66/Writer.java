package router66;

import java.util.Vector;

import kindle.SocketServer;

public class Writer {
	private Vector<WriteMsg> writeMsgs = new Vector<WriteMsg>();
	private SocketServer server;
	
	public Writer(){
        server = new SocketServer(this);
		Thread s = new Thread(server);
	    s.start();
	}
	
	public void addMsg(WriteMsg msg){
		writeMsgs.add(msg);
		System.out.println("Writer: "+writeMsgs.lastElement().getMsg());
		//writeFile(writeMsgs.lastElement().getMsg());
	}
	
	public String getMsg(){
		return writeMsgs.lastElement().getMsg();
	}
}
