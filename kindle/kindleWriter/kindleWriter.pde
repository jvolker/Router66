PrintWriter output;

int c =0;
String lines[];

void setup() {
  // Create a new file in the sketch directory
  lines = loadStrings("input.txt");
}

void draw() {
  
  int i = int(random(lines.length));
  
  String[] list = split(lines[i],' ');

  for (int y=0; y < list.length && y < 16; y++) {
            
    
            output = createWriter("/Users/js/data/Studium/08 Is this thing on?/#04/kindleScreen/helloClient.txt"); 
            for (int x=0; x < y; x++) {
              output.println(list[x]); // Write the coordinate to the file
              println(list[x]);    
            }
            printFile();
            delay(500);
  }

}

void printFile() {
  output.flush(); // Writes the remaining data to the file
  output.close(); // Finishes the file
}
