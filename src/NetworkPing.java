import java.io.IOException;
import java.net.InetAddress;
 
public class NetworkPing {
 
	/**
	 * JavaProgrammingForums.com
	 */
	public static void main(String[] args) throws IOException {
 
		InetAddress localhost = InetAddress.getLocalHost();
		// this code assumes IPv4 is used
		byte[] ip = localhost.getAddress();
 
		for (int i = 1; i <= 200; i++)
		{
			ip[3] = (byte)i;
			InetAddress address = InetAddress.getByAddress(ip);
		if (address.isReachable(30))
		{
			System.out.println(address.getHostName() + " machine is turned on and can be pinged");
		}
		else if (!address.getHostAddress().equals(address.getHostName()))
		{
			System.out.println(address + " machine is known in a DNS lookup");
		}
		else
		{
			System.out.println(address + " the host address and host name are equal, meaning the host name could not be resolved");
		}
		}
 
	}
}