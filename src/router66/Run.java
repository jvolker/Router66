package router66;

import java.net.InetAddress;
import java.net.UnknownHostException;

import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.PacketReceiver;
import jpcap.packet.*;

public class Run implements PacketReceiver {

	public void receivePacket(Packet packet) {
		if(packet instanceof TCPPacket)
				   //(((TCPPacket)packet).src_port==80 || ((TCPPacket)packet).dst_port==80))
		System.out.println(packet+" : "+((TCPPacket)packet).src_port+" : ");
		System.out.println(getHostName(((TCPPacket)packet).src_ip));
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		NetworkInterface[] devices = JpcapCaptor.getDeviceList();
		JpcapCaptor jpcap = JpcapCaptor.openDevice(devices[2], 2000, false, 20);
		
		jpcap.loopPacket(-1, new Run());
	}
	
	private String getHostName(InetAddress ip){

	    try {

	        // Get hostname by textual representation of IP address
	        InetAddress addr = InetAddress.getByName("127.0.0.1");
	    
	        // Get hostname by a byte array containing the IP address
	        byte[] ipAddr = new byte[]{127, 0, 0, 1};
	        addr = InetAddress.getByAddress(ipAddr);
	    
	        // Get the host name
	        String hostname = ip.getHostName();
	    
		        // Get canonical host name
	        String canonicalhostname = addr.getCanonicalHostName();
	        return hostname+" - "+canonicalhostname;
	    } catch (UnknownHostException e) {
	    	return "";
	    }
	    	
	}
}
