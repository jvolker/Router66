package router66;

public class WriteMsg {
	private long time;
	private String msg;
	
	public WriteMsg(String msg, long time){
		this.msg = msg;
		this.time = time;
	}

	public long getTime() {
		return time;
	}

	public String getMsg() {
		return msg;
	}
}
