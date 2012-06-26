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
		keepWritelistSmall();
	//	if(!checkRecentMsgs(msg)){
			//writeMsgs.add(msg);
			System.out.println("Writer: "+writeMsgs.lastElement().getMsg());
		//}
	}
	
	public String getMsg(){
		return writeMsgs.lastElement().getMsg();
		//return "Pssst. Christoph is doing his presentation. No surfing now.";
	}
	
	private void keepWritelistSmall(){
		if(writeMsgs.size()>50){
			writeMsgs.subList(20, writeMsgs.size()).clear();
		}
	}
	private Boolean checkRecentMsgs(WriteMsg msg){
		Boolean isKnown = false;
		if(writeMsgs.size()>3){
			for(int i=1; i<3; i++){
				if(msg.getSMsg().getServer().equals(writeMsgs.get(writeMsgs.size()-i).getSMsg().getServer())){
					isKnown=true;
					break;
				}
			}
		}
		return isKnown;
	}
}
