package router66;

import java.util.Vector;

public class Writer {
	private Vector<WriteMsg> writeMsgs = new Vector<WriteMsg>();
	
	public void addMsg(WriteMsg msg){
		writeMsgs.add(msg);
		System.out.println("Writer: "+writeMsgs.lastElement().getMsg());
	}
}
