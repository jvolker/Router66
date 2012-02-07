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
		String msg = null;
		switch((int)(Math.random()*6)){
			case 0:
				msg = sMsg.getClient()+" looks at "+sMsg.getServer()+".";
				break;
			case 1:
				msg = sMsg.getClient()+" on "+sMsg.getServer()+".";
				break;
			case 2:
				msg = sMsg.getClient()+" surfs the web.";
				break;
			case 3:
				msg = sMsg.getClient()+" got <div class='spacer'> from "+sMsg.getServer()+".";
				break;
			case 4:
				msg = sMsg.getServer()+" sends a 200 OK. ### Today things just work fine.";
				break;
			case 5:
				msg = "Hey "+sMsg.getServer()+", "+sMsg.getClient()+" needs the last package again. ### It got lost somewhere.";
				break;
			default:
			break;
		}
		writeOut(msg, sMsg);
	}
	public void wSSLDomain(SortMsg sMsg){
		/*
		 * the randomizer picks a message
		 */
		int rMsg = (int)(Math.random()*7);
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
				msg = sMsg.getServer()+" says: “Hello Client!” ### "+sMsg.getClient()+" says: “Hello Server!”";
				break;
			case 3:
				msg = "Hello "+sMsg.getServer()+", "+sMsg.getClient()+" wants to high five.";
				break;
			case 4:
				msg = sMsg.getClient()+" handshake.";
				break;
			case 5:
				msg = sMsg.getServer()+" that's the key.";
				break;
			case 6:
				msg = "Only "+sMsg.getClient()+" and "+sMsg.getServer()+" know what's going on here.";
				break;
			default:
				break;
		}
		writeOut(msg, sMsg);
	}
	public void wSearchGoogle(SortMsg sMsg){
		int rMsg = (int)(Math.random()*6);
		String msg = null;
		switch(rMsg){
			case 0:
				msg = sMsg.getClient()+" got "+gp.getCount("\""+sMsg.getAddArgs()[0]+"\"")+" google hits for "+sMsg.getAddArgs()[0]+".";
				break;
			case 1:
				String[] rhyme = lex.similarBySound(sMsg.getAddArgs()[0]);
				msg = sMsg.getClient()+" searched for "+sMsg.getAddArgs()[0]+". Did he mean "+rhyme[0]+" or "+rhyme[1]+"?";
				break;
			case 2:
				String[] rhymes = lex.similarBySound(sMsg.getAddArgs()[0]);
				msg = sMsg.getAddArgs()[0]+", "+rhymes[0]+", "+rhymes[1]+" …";
				break;
			case 3:
				msg = sMsg.getClient()+" searched for "+sMsg.getAddArgs()[0]+".";
				break;
			case 4:
				msg = sMsg.getClient()+" is looking for "+sMsg.getAddArgs()[0]+".";
				break;
			case 5:
				msg = "Google got "+gp.getCount("\""+sMsg.getAddArgs()[0]+"\"")+"pages for you, "+sMsg.getClient()+".";
				break;
			default:
				break;
		}
		writeOut(msg, sMsg);
	}
	public void wDropboxLan(SortMsg sMsg){
		String msg = null;
		switch((int)(Math.random()*4)){
		case 0:
			msg = sMsg.getClient()+"'s Dropbox is looking for friends.";
			break;
		case 1:
			msg = sMsg.getClient()+" wants to put his files in your dropbox.";
			break;
		case 2:
			msg = sMsg.getClient()+" is dropping.";
			break;
		case 3:
			msg = "Files, files, files. from "+sMsg.getClient()+".";
			break;
		default:
			break;
		} 
		writeOut(msg, sMsg);
	}
	public void wDropboxWeb(SortMsg sMsg){
		String msg = null;
		
		switch((int)(Math.random()*6)){
		case 0:
			msg = sMsg.getClient()+"'s Dropbox checks for updates in the cloud.";
			break;
		case 1:
			msg = sMsg.getClient()+" says: “Dear Internet, my dropbox needs some updates.”";
			break;
		case 2:
			msg = "Dropbox.";
			break;
		case 3:
			msg = "again …";
		case 4:
			msg = sMsg.getAddArgs()[0].split(" ")[9]+" "+sMsg.getAddArgs()[0].split(" ")[10]+" "+sMsg.getAddArgs()[0].split(" ")[11]+" "+sMsg.getAddArgs()[0].split(" ")[12]+" "+sMsg.getAddArgs()[0].split(" ")[13];
			break;
		case 5:
			msg = sMsg.getAddArgs()[0].split(" ")[14]+" "+sMsg.getAddArgs()[0].split(" ")[15];
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
				switch((int)(Math.random()*3)){
				case 0:
					msg = sMsg.getClient()+" is watching "+title.replaceAll("^\\s+", "")+" on youtube.";
					break;
				case 1:
					msg = title.replaceAll("^\\s+", "")+" for "+sMsg.getClient();
					break;
				case 2:
					msg = "Loading "+title.replaceAll("^\\s+", "").substring(0, 5)+" ### "+sMsg.getClient()+" is watching "+title.replaceAll("^\\s+", "")+".";
					break;
				default:
					break;
				}
			}else{
				switch((int)(Math.random()*3)){
				case 0:
					msg = sMsg.getClient()+" is watching youtube.";
					break;
				case 1:
					msg = sMsg.getClient()+"'s starring @ the tube.";
					break;
				case 2:
					msg = sMsg.getClient()+" is wathching youtube. ### And youtube is watching "+sMsg.getClient()+".";
					break;
				default:
					break;
				}
				
			}
			writeOut(msg, sMsg);
	}
	public void wFacebook(SortMsg sMsg){
		int rMsg = (int)(Math.random()*9);
		String msg = null;
		switch(rMsg){
			case 0:
				msg = sMsg.getClient()+" procrastinates at facebook.";
				break;
			case 1:
				msg = sMsg.getClient()+" visits his friends at facebook.";
				break;
			case 2:
				msg = sMsg.getClient()+" is socializing.";
				break;
			case 3:
				msg = sMsg.getClient()+" is having a good time with friends.";
				break;
			case 4:
				msg = sMsg.getClient()+" can like this.";
				break;
			case 5:
				msg = sMsg.getClient()+" facebook wants you to like it! ### like it! like it! like it!";
				break;
			case 6:
				msg = "Hey "+sMsg.getClient()+" facebook again.";
				break;
			case 7:
				msg = "What does like mean? ### And what is love?";
				break;
			case 8:
				msg = "♥ ♥ ♥";
				break;
			default:
				break;
		}
		writeOut(msg, sMsg);
	}
	public void wIMAP(SortMsg sMsg){
		String theServer;
		if(sMsg.getServer().indexOf("1e100")!=-1){
			theServer="google Mail";
		}else{
			theServer=sMsg.getServer();
		}
		String msg = null;
		switch((int)(Math.random()*5)){
			case 0:
				msg = sMsg.getClient()+" checks mails at "+theServer+"."; 
				break;
			case 1:
				msg = sMsg.getClient()+" is looking at "+theServer+" if there is a love letter.";
				break;
			case 3:
				msg = "Any mails for "+sMsg.getClient()+"?";
				break;
			case 4:
				msg = sMsg.getClient()+" ### You've got mail.";
				break;
			default: 
				break;
		}
		writeOut(msg, sMsg);
	}
	public void wEvernote(SortMsg sMsg){
		String msg = null;
		switch((int)(Math.random()*4)){
		case 0:
			msg = sMsg.getClient()+" is writing something down on Evernote.";
			break;
		case 1:
			msg = sMsg.getClient()+" looks at the elephant.";
			break;
		case 2:
			msg = sMsg.getClient()+" remembers everything.";
			break;
		case 3:
			msg = sMsg.getClient()+" is idling.";
			break;
		default:
			break;
		}
		writeOut(msg, sMsg);
	}
	public void wAdvertising(SortMsg sMsg){
		String msg = null;
		switch((int)(Math.random()*4)){
		case 0:
			msg = sMsg.getClient()+" got some nice Ad-Banners";
			break;
		case 1:
			msg = "Hey "+sMsg.getClient()+" wanna buy this? or that?";
			break;
		case 2:
			msg = sMsg.getClient()+" sees shiny pictures of shiny products.";
			break;
		case 3:
			msg = sMsg.getClient()+", yeah! Pretty flash banners for you.";
			break;
		case 4:
			msg = "Puh, this banners are heavy stuff.";
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
					msg = sMsg.getAddArgs()[0]+"? ### "+definition;
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
			msg = sMsg.getClient()+" is looking on Amazon for "+sMsg.getAddArgs()[1];
		}else if(sMsg.getAddArgs()[0].equals("1")){
			msg = sMsg.getClient()+" is going to buy "+sMsg.getAddArgs()[1];
		}else if(sMsg.getAddArgs()[0].equals("2")){
			msg = sMsg.getClient()+" is window shopping on Amazon.";
		}
			writeOut(msg, sMsg);
	}
	
	private void writeOut(String msg, SortMsg sMsg){
		if(msg!=null){
			writer.addMsg(new WriteMsg(msg,System.currentTimeMillis(),sMsg));
		}
	}
}
