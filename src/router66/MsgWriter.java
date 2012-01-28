package router66;

public class MsgWriter {
	private Writer writer;
	public MsgWriter(Writer writer){
		this.writer = writer;
	}
	public void wWebDomain(SortMsg sMsg){
		String msg = sMsg.getClient()+" looks at "+sMsg.getServer();
		writeOut(msg);
	}
	public void wSSLDomain(SortMsg sMsg){
		String msg = sMsg.getClient()+" secrets are at "+sMsg.getServer();
		writeOut(msg);
	}
	public void wSearchGoogle(SortMsg sMsg){
		String msg = sMsg.getClient()+" searched for È"+sMsg.getAddArgs()[0]+"Ç";
		writeOut(msg);
	}
	public void wDropboxLan(SortMsg sMsg){
		String msg = sMsg.getClient()+"'s Dropbox is looking for friends.";
		writeOut(msg);
	}
	
	private void writeOut(String msg){
		writer.addMsg(new WriteMsg(msg,System.currentTimeMillis()));
	}
}
