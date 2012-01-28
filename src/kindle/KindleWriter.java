package kindle;

import java.io.PrintWriter;

import processing.core.*;

public class KindleWriter extends PApplet {
	PrintWriter output;

	public KindleWriter() {
		//writeFile("Hello Client!");
	}

	public void writeFile(String sentence) {
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
