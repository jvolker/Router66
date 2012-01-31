package router66;

public class MsgWriter {
	private Writer writer;
	public MsgWriter(Writer writer){
		this.writer = writer;
	}
	public void wWebDomain(SortMsg sMsg){
		String msg = sMsg.getClient()+" looks at "+sMsg.getServer();
		writeOut(msg, sMsg);
	}
	public void wSSLDomain(SortMsg sMsg){
		String msg = sMsg.getClient()+" secrets are at "+sMsg.getServer();
		writeOut(msg, sMsg);
	}
	public void wSearchGoogle(SortMsg sMsg){
		String msg = sMsg.getClient()+" searched for È"+sMsg.getAddArgs()[0]+"Ç";
		writeOut(msg, sMsg);
	}
	public void wDropboxLan(SortMsg sMsg){
		String msg = sMsg.getClient()+"'s Dropbox is looking for friends.";
		writeOut(msg, sMsg);
	}
	public void wDropboxWeb(SortMsg sMsg){
		String msg = sMsg.getClient()+"'s Dropbox checks for updates on the interwebz.";
		writeOut(msg, sMsg);
	}
	
	private void writeOut(String msg, SortMsg sMsg){
		writer.addMsg(new WriteMsg(msg,System.currentTimeMillis(),sMsg));
	}
}
