package router66;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jpcap.PacketReceiver;
import jpcap.packet.IPPacket;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;
import jpcap.packet.UDPPacket;

public class Sorter{
	final static Pattern googleSearchPattern = Pattern.compile("\\&q\\=(.*?)\\s");
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

				//System.out.println(dst+" : "+((TCPPacket)packet).dst_port);
			switch (((TCPPacket)packet).src_port) {
			case 80:
			//	System.out.println(convertData(packet));
//				System.out.println(packet);
				break;
				default:
					break;
			}
			switch (((TCPPacket)packet).dst_port) {
				/**
				 * 	http package
				 */
				case 80:
					//if(!validateIPAddress(dst)){
					//System.out.println("web");
					//System.out.println(convertHeader(thePacket));
					/**
					 * Google Search
					 */
					String googleReturn=getGoogleSearchString(thePacket);
					if(googleReturn!=null){
						try {
							msgWriter.wSearchGoogle(new SortMsg(translateLocalHost(((TCPPacket) packet).src_ip.getHostAddress()), "", URLDecoder.decode(googleReturn.replace("+", " "),"UTF-8")));
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
					}else{
						String host = extractHost(thePacket);
						String client = HostDict.HOSTS.get(((TCPPacket) packet).src_ip.getHostAddress());
						/**
						 * dropbox Web
						 */
						if(host.indexOf("dropbox")!=-1){
							msgWriter.wDropboxWeb(new SortMsg(client, ""));
						}else if(host.indexOf("youtube")!=-1){
							convertData(thePacket);
						}else{
						/**
						 * Standard Website
						 */
						msgWriter.wWebDomain(new SortMsg(client, host));
						}
					//}
					}
					break;
				// SSL
				case 443:
					/**
					 * 	Encrypted Stuff
					 */
					String host = thePacket.dst_ip.getHostName();
					if(!validateIPAddress(host)){
						if(host.indexOf("1e100")!=-1){
							System.out.println("google ssl");
						}else if(host.indexOf("evernote")!=-1){
							System.out.println("evernote ssl");
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
				default:
					break;
				}
		}else if(packet instanceof UDPPacket){
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
		//String searchString = header.replaceAll(pattern, "$2");
		
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
		String hostname=null;
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
