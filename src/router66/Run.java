/**
 * If it doesn't run try: Terminal -> sudo chmod 777 /dev/bpf*
 * 
 * 
 */


package router66;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import processing.core.PApplet;

import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.NetworkInterfaceAddress;
import jpcap.PacketReceiver;
import jpcap.packet.*;

public class Run extends PApplet implements PacketReceiver {
	private Writer writer;
	private MsgWriter msgWriter = new MsgWriter(writer);
	private Sorter sorter = new Sorter(msgWriter);
	
	public void setup(){
		writer = new Writer(this);
	}
	public void receivePacket(Packet packet) {
		String dst = null;
		String src = null;
		
		//if(packet instanceof TCPPacket ){ 
			sorter.sortPacket(packet);
		//}
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		NetworkInterface[] devices = JpcapCaptor.getDeviceList();
		JpcapCaptor jpcap = null;
		try {
			jpcap = JpcapCaptor.openDevice(devices[2], 2000, false, 20);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/**
		 * List Network Interfaces
		 */
//		for (int i = 0; i < devices.length; i++) {
//			System.out.println(i+" :"+devices[i].name + "(" + devices[i].description+")");
//			System.out.println("    data link:"+devices[i].datalink_name + "("
//					+ devices[i].datalink_description+")");
//			System.out.print("    MAC address:");
//			for (byte b : devices[i].mac_address)
//				System.out.print(Integer.toHexString(b&0xff) + ":");
//			System.out.println();
//			for (NetworkInterfaceAddress a : devices[i].addresses)
//				System.out.println("    address:"+a.address + " " + a.subnet + " "
//						+ a.broadcast);
//		}
			jpcap.loopPacket(-1, new Run());
	}
	
	
	
}
