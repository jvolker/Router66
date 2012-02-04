package router66;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;
import javax.swing.text.MutableAttributeSet;

import rita.RiGoogleSearch;
import rita.RiHtmlParser;
import rita.RiLexicon;

public class MsgWriter{
	private Writer writer;
	RiGoogleSearch gp = new RiGoogleSearch();
	RiLexicon lex = new RiLexicon();
	@SuppressWarnings("deprecation")
	RiHtmlParser rhp = new RiHtmlParser(null);
	
	public MsgWriter(Writer writer){
		this.writer = writer;
		//gp.setUserAgent("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/525.13 (KHTML, like Gecko) Chrome/0.A.B.C Safari/525.13");
		//System.out.println(gp.getUserAgent());
	}
	public void wWebDomain(SortMsg sMsg){
		String msg = sMsg.getClient()+" looks at "+sMsg.getServer();
		writeOut(msg, sMsg);
	}
	public void wSSLDomain(SortMsg sMsg){
		/*
		 * the randomizer picks a message
		 */
		int rMsg = (int)(Math.random()*3);
		String msg = null;
		switch(rMsg){
			case 0:
				msg = sMsg.getClient()+" secrets are at "+sMsg.getServer();
				break;
			case 1:
				msg = "At "+sMsg.getServer()+" "+sMsg.getClient()+" hides his secrets";
				break;
			case 2:
				// ### Trennt Nachrichten
				msg = sMsg.getServer()+" says Hello Client!###"+sMsg.getClient()+" says Hello Server";
				break;
			default:
				break;
		}
		writeOut(msg, sMsg);
	}
	public void wSearchGoogle(SortMsg sMsg){
		String searchString = sMsg.getAddArgs()[0];
		String[] rhyme = lex.similarBySound(sMsg.getAddArgs()[0]);
		//System.out.println(searchString);
		int k = gp.getCount("\""+sMsg.getAddArgs()[0]+"\"");
		System.out.println(gp.getBigram("god", "devil"));
		System.out.println("hits: "+k);
		String msg = sMsg.getClient()+" searched for È"+sMsg.getAddArgs()[0]+"Ç. Did he mean "+rhyme[0]+" or "+rhyme[1]+"?";
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
//		 final List<String> title = new ArrayList<String>();
//			URL url = null;
//			try {
//				url = new URL("http://"+sMsg.getAddArgs()[0]);
//			} catch (MalformedURLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			 System.out.println("URL: "+url);
//			 rhp.setUserAgent("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/525.13 (KHTML, like Gecko) Chrome/0.A.B.C Safari/525.13");
//			 rhp.customParse(url,
//				        new ParserCallback() // an inner class
//				        {
//				          boolean isTitle = false;
//				          public void handleStartTag(Tag t, MutableAttributeSet a, int pos) {
//				            if (t == Tag.TITLE) isTitle = true;
//				          }
//				          public void handleText(char[] data, int pos) {
//				        	  if (isTitle) title.add(new String(data));                
//				          }
//				          public void handleEndTag(Tag t, int pos) {
//				            if (t == Tag.TITLE) isTitle = false;
//				          }
//				        }
//				      );
//		System.out.println("Title: ");//+title.get(0));
		String msg = sMsg.getClient()+" is watching youtube.";
		writeOut(msg, sMsg);
	}
	public void wFacebook(SortMsg sMsg){
		int rMsg = (int)(Math.random()*2);
		String msg = null;
		switch(rMsg){
			case 0:
				msg = sMsg.getClient()+" procrastinates at facebook.";
				break;
			case 1:
				msg = sMsg.getClient()+" visits his friends at facebook.";
				break;
			default:
				break;
		}
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
	public void wEvernote(SortMsg sMsg){
		String msg = sMsg.getClient()+" is writing something down on Evernote";
		writeOut(msg, sMsg);
	}
	public void wAdvertising(SortMsg sMsg){
		String msg = sMsg.getClient()+" got some nice Ad-Banners";
		writeOut(msg, sMsg);
	}
	public void wWikipedia(SortMsg sMsg){
		String msg = sMsg.getClient()+" learns on wikipedia something about È"+sMsg.getAddArgs()[0]+"Ç";
		writeOut(msg, sMsg);
	}
	
	private void writeOut(String msg, SortMsg sMsg){
		writer.addMsg(new WriteMsg(msg,System.currentTimeMillis(),sMsg));
	}
}
