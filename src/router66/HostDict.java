package router66;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HostDict {
	private static final Map<String, String> HOSTS = createMap();

    private static Map<String, String> createMap() {
        Map<String, String> result = new HashMap<String, String>();
        result.put("192.168.123.105", "Christoph's MacBook");
        result.put("192.168.123.109", "js");
        result.put("192.168.123.106", "Jerry's MacBook Pro");
        //result.put("192.168.1.50", "lakshmi");
        return Collections.unmodifiableMap(result);
    }
    public static String getHost(String ip){
    	String host = HOSTS.get(ip);
    	if(host==null){
    		host = ip;
    	}
    	return host;
    }
}
