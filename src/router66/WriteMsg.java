package router66;


public class WriteMsg {
	private long time;
	private String msg;
	private SortMsg sMsg;
	
	public WriteMsg(String msg, long time, SortMsg sMsg){
		this.msg = msg;
		this.time = time;
		this.sMsg = sMsg;
	}

	public long getTime() {
		return time;
	}

	public String getMsg() {
		return msg;
	}
	
	public SortMsg getSMsg(){
		return sMsg;
	}
}
