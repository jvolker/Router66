package router66;

import jpcap.PacketReceiver;
import jpcap.packet.Packet;
import jpcap.packet.UDPPacket;

import java.io.UnsupportedEncodingException;

public class PacketReceiverImpl implements PacketReceiver {

//    public PacketReceiverImpl(PopupReceiverIF receiver) {
//        popupParser = new PopupParser(receiver);
//    }

    public void receivePacket(Packet p) {
        UDPPacket packet = (UDPPacket) p;

        String src_ip = packet.src_ip.getHostAddress();
        String dst_ip = packet.dst_ip.getHostAddress();
        byte[] sm = new byte[6];
        System.arraycopy(p.header, 6, sm, 0, 6);
        String src_mac = byteToHexString(sm).toUpperCase();
        byte[] dm = new byte[6];
        System.arraycopy(p.header, 0, dm, 0, 6);
        String dst_mac = byteToHexString(dm).toUpperCase();

        //todo: ignore by mac\ip

        try {
            String s = new String(packet.data, "cp866");

            if (s.toLowerCase().indexOf("\\mailslot\\messngr") != -1) {
                int data_offset = 66 + 21 + Integer.parseInt(String.valueOf(packet.data[s.indexOf("SMB")]));
                s = s.substring(data_offset);
                String[] parts = s.split("\0");
                if (parts.length < 3) return;

                byte[] netbiosBytes = new byte[34];
                System.arraycopy(packet.data, 14, netbiosBytes, 0, netbiosBytes.length);

                String netbiosString = byteArrayToString(netbiosBytes);

                String mailslot = parts[0];
                String receiver = parts[1];
                String text = parts[2];
                //popupParser.add(mailslot, src_ip, src_mac.toUpperCase(), netbiosString, receiver, dst_ip, dst_mac.toUpperCase(), text);
            }

        } catch (UnsupportedEncodingException e) {
            System.out.println("no cp866 encoding installed, exiting...");
            System.exit(1);
        }

    }
    
    public String translateNetbios(Packet p){
    	String netbiosString = null;
    	 UDPPacket packet = (UDPPacket) p;

         String src_ip = packet.src_ip.getHostAddress();
         String dst_ip = packet.dst_ip.getHostAddress();
         byte[] sm = new byte[6];
         System.arraycopy(p.header, 6, sm, 0, 6);
         String src_mac = byteToHexString(sm).toUpperCase();
         byte[] dm = new byte[6];
         System.arraycopy(p.header, 0, dm, 0, 6);
         String dst_mac = byteToHexString(dm).toUpperCase();

         //todo: ignore by mac\ip
    	try {
            String s = new String(packet.data, "cp866");

            if (s.toLowerCase().indexOf("\\mailslot\\messngr") != -1) {
                int data_offset = 66 + 21 + Integer.parseInt(String.valueOf(packet.data[s.indexOf("SMB")]));
                s = s.substring(data_offset);
                String[] parts = s.split("\0");
                //if (parts.length < 3) return;

                byte[] netbiosBytes = new byte[34];
                System.arraycopy(packet.data, 14, netbiosBytes, 0, netbiosBytes.length);

                netbiosString = byteArrayToString(netbiosBytes);

                String mailslot = parts[0];
                String receiver = parts[1];
                String text = parts[2];
                //popupParser.add(mailslot, src_ip, src_mac.toUpperCase(), netbiosString, receiver, dst_ip, dst_mac.toUpperCase(), text);
            }

        } catch (UnsupportedEncodingException e) {
            System.out.println("no cp866 encoding installed, exiting...");
            System.exit(1);
        }
    	return netbiosString;
    }

    private String byteToHexString(byte[] data) {
        StringBuffer sb = new StringBuffer(12);
        for (byte b : data) {
            String s = Integer.toHexString((b + 256) % 256);
            if (s.length() == 1) s = "0" + s;
            sb.append(s);
        }
        return sb.toString();
    }

    private static String byteArrayToString(byte[] b) throws UnsupportedEncodingException {
        StringBuffer sb = new StringBuffer(15);
        byte[] bb = new byte[b.length / 2];
        for (int i = 1; i < bb.length; i++) {
            bb[i] = (byte) (((b[i * 2 - 1] - 'A') << 4) + (b[i * 2] - 'A'));
        }
        sb.append(new String(bb, "cp866"));
        return sb.toString().trim();
    }
}