package router66;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jpcap.PacketReceiver;
import jpcap.packet.IPPacket;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;
import jpcap.packet.UDPPacket;

public class Sorter{
	final static Pattern googleSearchPattern = Pattern.compile("\\&q\\=(.*?)\\s");
	
	static Vector<String> blackUrlList = new Vector<String>();
	static {
		blackUrlList.add("ytimg");
		blackUrlList.add("gstatic");
		blackUrlList.add("doubleclick");
	}
	
	private MsgWriter msgWriter;
	private PacketReceiverImpl pri;
	
	public Sorter(MsgWriter msgWriter){
		this.msgWriter = msgWriter;
	}
	public void sortPacket(Packet packet){
		String dst = null;
		String src = null;
		if(packet instanceof TCPPacket ){
			TCPPacket thePacket = ((TCPPacket)packet);
			dst = getHostName(thePacket.dst_ip);
			src = getHostName(thePacket.src_ip);
//			try {
//				InetAddress t = InetAddress.getAllByName("hanuman.local")[0];
//				System.out.println(InetAddress.getAllByName("hanuman.local")[0]);
//			} catch (UnknownHostException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			//System.out.println(new String(thePacket.toString()));

			
			String host = extractHost(thePacket);
			String client = HostDict.HOSTS.get(((TCPPacket) packet).src_ip.getHostAddress());			
			/**
			 *  Checks against the Blacklist and prevent the package from being inspected
			 */
//			for (int i = 0; i < blackUrlList.size(); i++) {
//				System.out.println(host.indexOf(blackUrlList.get(i)));
//				if(host.indexOf(blackUrlList.get(i))!=-1){
//					blackListed= true;
//				}
//			}
//			Iterator<String> itr = blackUrlList.iterator();
//				while(itr.hasNext()){
//					//System.out.println(itr.hasNext());
//					String c = itr.next();
//				 	if(host.indexOf(c)!=-1){
//				 		blackListed=true;
//				 	}
//			}
			//System.out.println("unfiltered: "+thePacket.dst_port);	
					
			/**
			 * Package isn't blacklisted, so let's look 
			 */
			switch (((TCPPacket)packet).dst_port) {
				/**
				 * 	http package
				 */
				case 80:
					/**
					 * Google Search
					 */
					 if(host.indexOf("google")!=-1){
							String googleReturn=getGoogleSearchString(thePacket);
							try {
								msgWriter.wSearchGoogle(new SortMsg(translateLocalHost(((TCPPacket) packet).src_ip.getHostAddress()), "", URLDecoder.decode(googleReturn.replace("+", " "),"UTF-8")));
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}
						}						
						/**
						 * dropbox Web
						 */
						else if(host.indexOf("dropbox")!=-1){
							msgWriter.wDropboxWeb(new SortMsg(client, ""));
						}
						/**
						 *  Youtube Web
						 */
						else if(host.indexOf("youtube")!=-1){
							msgWriter.wYoutubeWatch(new SortMsg(client, ""));
						}
						/**
						 * Standard Website
						 */
						else{
							Boolean blackListed = false;
							Iterator<String> itr = blackUrlList.iterator();
							while(itr.hasNext()){
							 	if(host.indexOf(itr.next())!=-1){
							 		blackListed = true;
							 	}
							}
							if(!blackListed){
								msgWriter.wWebDomain(new SortMsg(client, host));
							}
						}
				/**
				 * SSL Port
				 */
				case 443:
					/**
					 * 	Encrypted Stuff
					 */
					String sslHost = thePacket.dst_ip.getHostName();
					if(!validateIPAddress(sslHost)){
						if(sslHost.indexOf("1e100")!=-1){
							//System.out.println("google ssl");
						}else if(sslHost.indexOf("evernote")!=-1){
							//System.out.println("evernote ssl");
						}else{
							msgWriter.wSSLDomain(new SortMsg(HostDict.HOSTS.get(((TCPPacket) packet).src_ip.getHostAddress()),thePacket.dst_ip.getHostName()));
						}
					}
					//System.out.println(dst.indexOf("facebook"));
					break;
				case 1515:
					System.out.println("PORT 1515: "+thePacket.toString());
					break;
				// CUPS
				case 631:
					System.out.println("CUPS: "+packet.toString());
					break;
				// IMAP SSL:
				case 993:
					msgWriter.wIMAP(new SortMsg(client, thePacket.dst_ip.getCanonicalHostName()));
					break;
				default:
					break;
				}
				}
			if(packet instanceof UDPPacket){
			if(((UDPPacket)packet).protocol==17){
				//System.out.println("UPD: "+packet);
				switch (((UDPPacket)packet).dst_port) {
				// DROPBOX
				case 17500:
					msgWriter.wDropboxLan(new SortMsg(HostDict.HOSTS.get(((TCPPacket) packet).src_ip.getHostAddress()), ""));
					break;
				// BROWSER
				case 138:
					System.out.println(pri.translateNetbios(packet));
					//System.out.println("BROWSER: "+ ((UDPPacket)packet).);
					break;
				default:
					break;
				}
				// 
			}
		//	System.out.println(convertHeader(packet));
			//System.out.println(packet.);
		}else{
			if(packet.toString().substring(0, 11)=="ARP REQUEST"){
				System.out.println("ARP REQUEST: "+packet.toString());
			}else if(packet.toString().substring(0, 9)=="ARP REPLY"){
				System.out.println("ARP REPLY: "+packet.toString());
			}
		}
	
				  
	
	}
	
	
	public final static String getHostName(InetAddress ip){

	        // Get the host name
	        return ip.getHostName();
	        
		    // Get canonical host name
	   //     String canonicalhostname = ip.getCanonicalHostName();
	        
	    	
	}
	
	public final static String convertPacket(Packet p){
		byte[] bytes=new byte[p.header.length+p.data.length]; 
        System.arraycopy(p.header,0,bytes,0,p.header.length); 
        System.arraycopy(p.data,0,bytes,p.header.length,p.data.length); 
        StringBuffer buf=new StringBuffer(); 
        for(int i=0,j;i<bytes.length;){ 
                for(j=0;j<8 && i<bytes.length;j++,i++){ 
                        String d=Integer.toHexString((int)(bytes[i]&0xff)); 
                        buf.append((d.length()==1?"0"+d:d)+" "); 
                 if(bytes[i]<32 || bytes[i]>126) bytes[i]=46; //avoid the garbage characters 
                } 
                buf.append("["+new String(bytes,i-j,j)+"]\n");
        }
		return buf.toString();
	}
	public final static String convertData(Packet p){ 
        byte[] bytes=new byte[p.data.length]; 
        System.arraycopy(p.data,0,bytes,0,p.data.length); 
        String text = new String(bytes); 
        return text; 
	} 
	
	public final static String convertHeader(Packet p){ 
		byte[] bytes=new byte[p.header.length]; 
		System.arraycopy(p.header,0,bytes,0,p.header.length); 
		String text = new String(bytes); 
		return text; 
	} 
	
	public final static String getGoogleSearchString(Packet p){
		String header = convertData(p);
		Matcher m = googleSearchPattern.matcher(header);
		String searchString=null;
		while (m.find()) {
		     searchString = m.group(1);
		    // s now contains "BAR"
		}
		//String searchString = header.split("\n")[0];
		//String searchString = headergoogleSearchPattern.replaceAll(pattern, "$2");
		
			return searchString;
	}
	/**
	 * Checks if it's an IP Adress or a hostname
	 * @param ipAddress
	 * @return true if this is a real IP Adress
	 */
	public final static boolean validateIPAddress( String  ipAddress )
	{
	    String[] parts = ipAddress.split( "\\." );
	    if ( parts.length != 4 )
	    {
	        return false;
	    }

	    for ( String s : parts )
	    {
	        int i = Integer.parseInt( s );

	        if ( (i < 0) || (i > 255) )
	        {
	            return false;
	        }
	    }

	    return true;
	}
	
	public final static String extractHost(TCPPacket p){
		String hostname=p.dst_ip.getHostAddress();
		String[] lines = convertData(p).split("\n");
		if(lines[1].substring(0,4).equals("Host")){
			hostname = lines[1].substring(6,lines[1].length()-1);
		}
		return hostname;
	}
	
	public String translateLocalHost(String ip){
		String host = HostDict.HOSTS.get(ip);
		if(host==null){
			return ip;
		}else{
			return host;	
		}
	}
}
