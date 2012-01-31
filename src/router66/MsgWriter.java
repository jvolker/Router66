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
		/**
		 * the randomizer picks a message
		 */
		int rMsg = (int)(Math.random()*1);
		String msg = null;
		switch(rMsg){
			case 0:
				msg = sMsg.getClient()+" secrets are at "+sMsg.getServer();
				break;
			case 1:
				msg = "At "+sMsg.getServer()+" "+sMsg.getClient()+" hides his secrets";
				break;
		}
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
	public void wYoutubeWatch(SortMsg sMsg){
		String msg = sMsg.getClient()+" is watching youtube.";
		writeOut(msg, sMsg);
	}
	public void wIMAP(SortMsg sMsg){
		String theServer;
		if(sMsg.getServer().indexOf("1e100")!=-1){
			theServer="googlemail";
		}else{
			theServer=sMsg.getServer();
		}
		String msg = sMsg.getClient()+" checks mails at "+theServer;
		writeOut(msg, sMsg);
	}
	
	private void writeOut(String msg, SortMsg sMsg){
		writer.addMsg(new WriteMsg(msg,System.currentTimeMillis(),sMsg));
	}
}
