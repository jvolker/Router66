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
		if(!checkRecentMsgs()){
			writeMsgs.add(msg);
			System.out.println("Writer: "+writeMsgs.lastElement().getMsg());
		}
	}
	
	public String getMsg(){
		return writeMsgs.lastElement().getMsg();
	}

	private Boolean checkRecentMsgs(){
		Boolean isKnown = false;
		if(writeMsgs.size()>2){
			for(int i=1; i<2; i++){
				System.out.println("crm: "+writeMsgs.get(writeMsgs.size()-1).getSMsg().getServer().toString());
			}	
		}
		
		return isKnown;
	}
}
