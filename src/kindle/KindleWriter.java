package kindle;

import java.io.PrintWriter;

import processing.core.*;
import processing.net.*;

public class KindleWriter extends PApplet {
	Server s;
	Client c;
	
	String request;

	public KindleWriter() {
		s = new Server(this, 12345); // Start a simple server on a port
	}
	
	void serverEvent(Server someServer, Client someClient) {
		println("We have a new request by: " + someClient.ip());
		
		//answer text request
		request = someClient.readString();
		
	    println(request);
	    someClient.write("Output this!"+frameCount); // OUTPUT HERE
	    someServer.disconnect(c);

	}

}