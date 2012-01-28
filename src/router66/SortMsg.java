package router66;

public class SortMsg {
	private String client;
	private String server;
	private String[] addArgs;
	
	public SortMsg(String client, String server, String ... addArgs){
		this.client = client;
		this.server = server;
		this.addArgs = addArgs;
	}

	public String getClient() {
		return client;
	}

	public String getServer() {
		return server;
	}
	
	public String[] getAddArgs(){
		return addArgs;
	}
}
