package router66;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;


import org.apache.commons.lang3.StringEscapeUtils;

import rita.RiGoogleSearch;
import rita.RiHtmlParser;
import rita.RiLexicon;

public class MsgWriter{
	private Writer writer;
	RiGoogleSearch gp = new RiGoogleSearch();
	RiLexicon lex = new RiLexicon();
	RiHtmlParser rhp = new RiHtmlParser(null);
	
	public MsgWriter(Writer writer){
		this.writer = writer;
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
//		String num = null;
//		BufferedReader reader;
//		try {
//			Boolean tFound = false;
//			URL cgiURL = new URL("http://www.google.de/search?q="+sMsg.getAddArgs()[0]); 
//			 HttpURLConnection connection = (HttpURLConnection) cgiURL.openConnection();
//			 //URLConnection connection = cgiURL.openConnection(); 
//			//connection.addRequestProperty( "User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.77 Safari/535.7" );
//			connection.setDoOutput(true);
//			connection.setDoInput(true);
//			connection.setRequestProperty(
//                "User-Agent",
//                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; .NET CLR 1.1.4322)");
//			connection.setRequestProperty("Referer", "http://www.google.de/");
//			connection.connect();
//			reader = new BufferedReader( new InputStreamReader(connection.getInputStream()));
//			reader = WebsiteReader.read("http://www.google.de/search?q="+sMsg.getAddArgs()[0]);		// website wird gelesen
//			String line = reader.readLine();
//			while (line != null) {					// zeile für zeile wird durchgegangen
//				if(line.indexOf("resultStats")!=-1){
//					System.out.println(line);
//				}
//			reader.close();
//				if(tFound){							// letzte zeile war title
//					num=StringEscapeUtils.unescapeHtml4(line);
//					tFound = false;
//				}
//				if(line.indexOf("")!=-1){	// wenn die zeile der title ist
//					tFound=true;					// muss die nächste zeile der tatsächliche titel sein (youtube spezifisch)
//				}
//				line = reader.readLine(); 
//			}
//		} catch (Exception e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		
//		String searchString = sMsg.getAddArgs()[0];
		String[] rhyme = lex.similarBySound(sMsg.getAddArgs()[0]);
		//System.out.println(searchString);
//		int k = gp.getCount("\""+sMsg.getAddArgs()[0]+"\"");
//		System.out.println(gp.getBigram("god", "devil"));
//		System.out.println("hits: "+k);
		String msg = sMsg.getClient()+" searched for »"+sMsg.getAddArgs()[0]+"«. Did he mean "+rhyme[0]+" or "+rhyme[1]+"?";
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
			String title = null;
			BufferedReader reader;
			try {
				Boolean tFound = false;
				reader = WebsiteReader.read("http://"+sMsg.getAddArgs()[0]);		// website wird gelesen
				String line = reader.readLine();
				while (line != null) {					// zeile für zeile wird durchgegangen
					if(tFound){							// letzte zeile war title
						title=StringEscapeUtils.unescapeHtml4(line);
						tFound = false;
					}
					if(line.indexOf("<title>")!=-1){	// wenn die zeile der title ist
						tFound=true;					// muss die nächste zeile der tatsächliche titel sein (youtube spezifisch)
					}
					line = reader.readLine(); 
				}
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			String msg;
			if(title!=null){
				msg = sMsg.getClient()+" is watching "+title.replaceAll("^\\s+", "")+" on youtube.";		
			}else{
				msg = sMsg.getClient()+" is watching youtube.";
			}
		
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
		String msg = sMsg.getClient()+" learns on wikipedia something about »"+sMsg.getAddArgs()[0]+"«";
		writeOut(msg, sMsg);
	}
	
	private void writeOut(String msg, SortMsg sMsg){
		writer.addMsg(new WriteMsg(msg,System.currentTimeMillis(),sMsg));
	}
}
