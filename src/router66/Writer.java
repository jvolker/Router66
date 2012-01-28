package router66;

import java.io.PrintWriter;
import java.util.Vector;

import processing.core.PApplet;

public class Writer extends PApplet{
	private Vector<WriteMsg> writeMsgs = new Vector<WriteMsg>();
	private PrintWriter output;
	
	public void addMsg(WriteMsg msg){
		writeMsgs.add(msg);
		System.out.println("Writer: "+writeMsgs.lastElement().getMsg());
		writeFile(writeMsgs.lastElement().getMsg());
	}
	
	private void writeFile(String sentence) {
		//String[] list = split(sentence, ' ');

		output = createWriter("kindle/kindleScreen/helloClient.txt");

		//for (int x = 0; x < y; x++) {
		//	output.println(list[x]); // Write the coordinate to the file
		//}
		output.println(sentence);

		output.flush(); // Writes the remaining data to the file
		output.close(); // Finishes the file
	}
}
