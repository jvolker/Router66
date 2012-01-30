package router66;

import java.io.PrintWriter;
import java.util.Vector;

import processing.core.PApplet;
import processing.net.Client;
import processing.net.Server;

public class Writer {
	private Vector<WriteMsg> writeMsgs = new Vector<WriteMsg>();
	private PrintWriter output;
	private Server s;
	private Client c;
	private String request;
	private PApplet p5;
	
	public Writer(PApplet p5){	
		setup();
		this.p5 = p5;
	}
	
	public void dispose(){
	}	
	public void setup(){
		s = new Server(p5, 12345); // Start a simple server on a port
		System.out.println("Socket Server Started");
	}
	
	public void addMsg(WriteMsg msg){
		writeMsgs.add(msg);
		System.out.println("Writer: "+writeMsgs.lastElement().getMsg());
		//writeFile(writeMsgs.lastElement().getMsg());
	}
	
	private void writeFile(String sentence) {
		//String[] list = split(sentence, ' ');

		output = p5.createWriter("kindle/kindleScreen/helloClient.txt");

		//for (int x = 0; x < y; x++) {
		//	output.println(list[x]); // Write the coordinate to the file
		//}
		output.println(sentence);

		output.flush(); // Writes the remaining data to the file
		output.close(); // Finishes the file
	}
	
	public void serverEvent(Server someServer, Client someClient) {
		p5.println("We have a new request by: " + someClient.ip());
		
		//answer text request
		request = someClient.readString();
		
	    p5.println(request);
	    someClient.write(writeMsgs.lastElement().getMsg()); // OUTPUT HERE
	    someServer.disconnect(c);

	}
}
