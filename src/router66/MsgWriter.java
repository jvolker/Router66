package router66;

import java.io.BufferedReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;

import rita.RiGoogleSearch;
import rita.RiHtmlParser;
import rita.RiLexicon;

public class MsgWriter{
	final static Pattern defineExtract = Pattern.compile("<div class\\=\"dndata\">(.*?)</div>");
	
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
				msg = sMsg.getClient()+"'s secrets are at "+sMsg.getServer();
				break;
			case 1:
				msg = "At "+sMsg.getServer()+" "+sMsg.getClient()+" hides his secrets";
				break;
			case 2:
				// ### Trennt Nachrichten
				msg = sMsg.getServer()+" says “Hello Client!” ### "+sMsg.getClient()+" says “Hello Server!”";
				break;
			default:
				break;
		}
		writeOut(msg, sMsg);
	}
	public void wSearchGoogle(SortMsg sMsg){
		
		int rMsg = (int)(Math.random()*2);
		String msg = null;
		switch(rMsg){
			case 0:
				msg = sMsg.getClient()+" got "+gp.getCount("\""+sMsg.getAddArgs()[0]+"\"")+" google hits for "+sMsg.getAddArgs()[0];
				break;
			case 1:
				String[] rhyme = lex.similarBySound(sMsg.getAddArgs()[0]);
				msg = sMsg.getClient()+" searched for "+sMsg.getAddArgs()[0]+". Did he mean "+rhyme[0]+" or "+rhyme[1]+"?";
				break;
			default:
				break;
		}
		writeOut(msg, sMsg);
	}
	public void wDropboxLan(SortMsg sMsg){
		String msg = null;
		switch((int)(Math.random()*2)){
		case 0:
			msg = sMsg.getClient()+"'s Dropbox is looking for friends.";
			break;
		case 1:
			msg = sMsg.getClient()+" wants to put his files in your dropbox.";
			break;
		default:
			break;
		} 
		writeOut(msg, sMsg);
	}
	public void wDropboxWeb(SortMsg sMsg){
		String msg = null;
		switch((int)(Math.random()*2)){
		case 0:
			msg = sMsg.getClient()+"'s Dropbox checks for updates in the cloud.";
			break;
		case 1:
			msg = sMsg.getClient()+"says “Dear Internet, my dropbox need some updates.”";
			break;
		default:
				break;
		}
		writeOut(msg, sMsg);
	}
	public void wYoutubeWatch(SortMsg sMsg){
			String title = null;
			BufferedReader reader;
			try {
				Boolean tFound = false;
				reader = WebsiteReader.read("http://"+sMsg.getAddArgs()[0]);		// website wird gelesen
				String line = reader.readLine();
				while (line != null) {					// zeile f�r zeile wird durchgegangen
					if(tFound){							// letzte zeile war title
						title=StringEscapeUtils.unescapeHtml4(line);
						tFound = false;
					}
					if(line.indexOf("<title>")!=-1){	// wenn die zeile der title ist
						tFound=true;					// muss die n�chste zeile der tats�chliche titel sein (youtube spezifisch)
					}
					line = reader.readLine(); 
				}
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			String msg = null;
			if(title!=null){
				msg = sMsg.getClient()+" is watching "+title.replaceAll("^\\s+", "")+" on youtube.";		
			}else{
				switch((int)(Math.random()*2)){
				case 1:
					msg = sMsg.getClient()+" is watching youtube.";
					break;
				case 2:
					msg = sMsg.getClient()+"'s starring @ the tube.";
					break;
				default:
					break;
				}
				
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
		String msg = null;
		switch((int)(Math.random()*2)){
			case 1:
				msg = sMsg.getClient()+" checks mails at "+theServer; 
				break;
			case 2:
				msg = sMsg.getClient()+" is looking at "+theServer+" if there is a love letter.";
				break;
			default: 
				break;
		}
		writeOut(msg, sMsg);
	}
	public void wEvernote(SortMsg sMsg){
		String msg = null;
		switch((int)(Math.random()*2)){
		case 1:
			msg = sMsg.getClient()+" is writing something down on Evernote";
			break;
		case 2:
			msg = sMsg.getClient()+" looks at the elephant in the cloud";
			break;
		default:
			break;
		}
		writeOut(msg, sMsg);
	}
	public void wAdvertising(SortMsg sMsg){
		String msg = null;
		switch((int)(Math.random()*3)){
		case 1:
			msg = sMsg.getClient()+" got some nice Ad-Banners";
			break;
		case 2:
			msg = "Hey "+sMsg.getClient()+" wanna buy this? or that?";
			break;
		case 3:
			msg = sMsg.getClient()+" sees shiny pictures of shiny products.";
			break;
		
		default:
			break;
		}
		writeOut(msg, sMsg);
	}
	public void wWikipedia(SortMsg sMsg){
		
		String msg = null;
		int rMsg = (int)(Math.random()*2);
		switch(rMsg){
			case 0:
				String definitionReturn = rhp.fetch("http://dictionary.reference.com/browse/"+sMsg.getAddArgs()[0]);
				Matcher m = defineExtract.matcher(definitionReturn);
				String definition= null;
				m.find();
				definition = (((m.group().replaceAll("\\<.*?>","")).replaceAll("[ \t]+$", "")));
				if(definition!=null){
					msg = "On wikipedia "+sMsg.getClient()+" learned: "+definition+" about "+sMsg.getAddArgs()[0];
				}
				break;
			case 1:
				msg = sMsg.getClient()+" learns on wikipedia something about "+sMsg.getAddArgs()[0]+".";		
				break;
			default:
				break;
		}
			writeOut(msg, sMsg);
	}
	public void wAmazon(SortMsg sMsg){
		String msg = null;
		if(sMsg.getAddArgs()[0].equals("0")){
			msg = sMsg.getClient()+" is look on amazon for "+sMsg.getAddArgs()[1];
		}else if(sMsg.getAddArgs()[0].equals("1")){
			msg = sMsg.getClient()+" is going to buy "+sMsg.getAddArgs()[1];
		}else if(sMsg.getAddArgs()[0].equals("1")){
			msg = sMsg.getClient()+" is window shopping on amazon ";
		}
			writeOut(msg, sMsg);
	}
	
	private void writeOut(String msg, SortMsg sMsg){
		if(msg!=null){
			writer.addMsg(new WriteMsg(msg,System.currentTimeMillis(),sMsg));
		}
	}
}
