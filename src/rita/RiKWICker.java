package rita;

import java.util.*;
import java.util.regex.*;

import processing.core.PApplet;
import rita.support.Regex;

/**
 * An implementation of a simple KeyWord-In-Context (KWIC) model
 * for efficient indexing and lookup of words-in-phrases within 
 * a document.
 * 
<pre>
RiKWICker kwic = new RiKWICker(this); 
kwic.addLinesFromFile("one_sentence_per_line.txt");
String[] hits = kwic.lookup("cat");</pre>
 *
 * @author dhowe
 */
public class RiKWICker extends RiObject
{
  //TODO: implement this flag
  boolean ignoreStopWords = false;
  
  boolean lazyLoad = false;
  protected Map lookupMap;
  protected String[] lines;
  protected IndexedPair[] lookupArray;

  private boolean useMap = true;
  
  public RiKWICker(PApplet p) {
    super(p);  
  }
  
  /**
   * Assumes an input file with one sentence per line
   * and builds the model from it (removing any existing
   * data first).
   * @param fileName
   */
  public void setLinesFromFile(String fileName)
  {
    String[] sent = RiTa.loadStrings(_pApplet, fileName);
    this.setLines(sent);
  }
  
  /**
   * Assumes an input file with one sentence per line
   * and adds each to the current model.
   * @param fileName
   */
  public void addLinesFromFile(String fileName)
  {
    String[] sent = RiTa.loadStrings(_pApplet, fileName);
    this.addLines(sent);
  }
  
  /**
   * Adds lines to the model
   * @param lineArr
   */
  public void addLines(final String[] lineArr)
  {
    // need to rebuild these
    this.lookupArray = null; 
    this.lookupMap = null;
    
    if (lines != null) // add to existing lines
    {
      String[] tmp = new String[lines.length + lineArr.length];
      System.arraycopy(lines, 0, tmp, 0, lines.length);
      System.arraycopy(lineArr, 0, tmp, lines.length, lineArr.length);
      setLines(tmp);
    }
    else 
      this.setLines(lineArr);
  }

  /**
   * (Re)sets the array of lines in the model 
   * @param lineArr
   */
  public void setLines(final String[] lineArr)
  {
    this.lines = lineArr;
    this.lookupArray = null;
    this.lookupMap = null;
    if (!lazyLoad) {
      if (useMap)  
        this.lookupMap = buildLookupMap();
      else
        this.lookupArray = buildLookupArray();
    }
  }

  /**
   * Returns List of hits or null if none exist.
   */
  List _lookup(final String input)
  {   
    List hits = useMap ? mapLookup(input) : arrayLookup(input); 
    if (hits == null) 
      return new LinkedList();
    return hits;
  }
  
  /**
   * Does a lookup on the current model and returns an array of hits 
   * or null if none exist.
   */
  public String[] lookup(final String input)
  {
    List hits = this._lookup(input);
    return (String[])hits.toArray(new String[hits.size()]);
  }
  
  /**
   * Returns array of hits or null if none exist after first stemming
   * the word if <code>useStem</code> is true. 
   * @invisible
   */
  private String[] lookup(final String input, boolean useStem)
  {   
    throw new RuntimeException("unimplemented");
  }
  
  /**
   * Returns List of hits or null if none exist.
   * @invisible
   */
  public String[] lookupRegex(final String pattern)
  {   
    if (1==1) throw new RuntimeException("unimplemented");
    
    Pattern pat = Pattern.compile(pattern);
    List hits = null;//mapLookupRegex(pat); 
    if (hits == null || hits.size()<1)
      return new String[0];
    return (String[]) hits.toArray(new String[hits.size()]);
  }
  
  /**
   * Returns List of hits or null if none exist.
   */
  protected List mapLookup(String word)
  {
    if (lookupMap == null)
      this.lookupMap = buildLookupMap();
    
    //System.out.println("mapLookup("+word+")");
    
    List result = new LinkedList(); 
    List pairs = (List)lookupMap.get(word);
    if (pairs == null || pairs.size()<1) return null;
    for (Iterator iter = pairs.iterator(); iter.hasNext();)
    {
      IndexedPair pair = (IndexedPair) iter.next();
      result.add(pair.getOriginalLine());      
    }
    return result;
  }
  
  //linear scan, yuck (index by first letter?) 
  /**
   * Returns List of hits or null if none exist.
   */
  protected List arrayLookup(String word)
  {        
    boolean dbug = false;
    
    if (lookupArray == null)
      this.lookupArray = buildLookupArray();
    
    //System.out.println("arrayLookup("+word+")");
    List hits = new LinkedList();
    for (int i = 0; i < lookupArray.length; i++)
    {      
      String keyword = lookupArray[i].getKeyword();
      if (dbug) System.out.println("CHECKING: "+keyword+
        ": "+lookupArray[i].getShiftedline());
  
      if (keyword.equals(word))
        hits.add(lookupArray[i].getOriginalLine());
    }
    if (hits.size()<1) return null;
    
    return hits;
  }
  
  private Map buildLookupMap()
  {    
    //long start = System.currentTimeMillis();
    //System.out.print("[Info] building lookup map...");
    Map result = new HashMap();      
    IndexedPair[] shiftedPairs = circularShift();
    for(int i = shiftedPairs.length - 1 ; i >= 0; i--) 
      addToMap(result, shiftedPairs[i]);    
    //System.out.println(" done. "+ result.size()+" entries, time="+Util.elapsed(start));
    return result;
  }  
  
  private IndexedPair[] circularShift()
  {
    // Creates the array of indexes of circular shifts
    List circularShifts = new ArrayList();
    for (int i = 0; i < lines.length; i++)
    {
      StringTokenizer parser = new StringTokenizer(lines[i]);
      int lastPosition = 0;
     //System.out.println();
      while (parser.hasMoreTokens())
      {
        String token = parser.nextToken();
        lastPosition = lines[i].indexOf(token, lastPosition);
        circularShifts.add(new IndexedPair(lines, lastPosition, i));
        lastPosition = lastPosition + token.length();
      }
     // System.out.println();
    }
    return (IndexedPair[]) circularShifts.toArray(new IndexedPair[circularShifts.size()]);
  }
  
  private void addToMap(Map result, IndexedPair pair)
  {
    String keyword = pair.getKeyword();
    if (ignoreStopWords && RiTa.isStopWord(keyword))
      return;   // no insert
    Object value = result.get(keyword);
    List pairList = value==null ? new LinkedList() : (List)value;      
    pairList.add(pair);        
    result.put(keyword, pairList);    
  }
  
  private IndexedPair[] buildLookupArray() 
  {
    System.out.print("[Info] building lookup array...");
    
    long start = System.currentTimeMillis();
    
    IndexedPair[] shiftedPairs = circularShift();
  
    Arrays.sort(shiftedPairs);
    
    System.out.println(" done. "+ shiftedPairs.length+" entries, time="+RiTa.elapsed(start));
    
    // replace with Arrays.sort(comparable)...
    return shiftedPairs;// sorter.sortedLineIndexes(shiftedPairs, lines);
  }

  static String[] test = {
      "I don�t have the time to be organised.",
      "You can't make the time for me.",
      "She won't make dessert tonite.",
      "If everyone around you is so busy that they can't make time for you, don't let it ruffle your feathers...",
      "In case you don't have the time to read all of the above, I'll summarise: it's like Catholicism - it's bollocks....",
      "Or do they not have time to bask in the victory before pivoting to the Battle Royale that will be South Carolina?...",
      "Or do they not have time to bask in the victory before pivoting to the Battle Royale that will be South Carolina?...",
      "I can't find the time for you.",
      "I cannot find the time for you.",
      "I don't have time for you.",
      "I don't have the time for you.",
      "He don't have the time for me.",
      "He doesn't have the time for me.",
      "I just don't have the time, nor the inclination....",
      "I really need to vent and I just don't have the time to devote to therapy....",
      "For those of you that read my blog on a regular basis, you know that I do not blog everyday. I just don't have the time....",
      "I walk around all day with ideas for stories in my head that I don't have the time or concentration to write down....",
      ". . and I want to go on reading my book, but at school I can't find the time (and I read only at school,I prefer it). . . with friends is a strange situation....",
      ". . *groan*. The textbook is almost ready for reposting, (I really ought to take it offline while I don't have the time to correct student's homework)...I just don't have the time to write fanfiction anymore....",
      "Makes me wonder - Maroon 5: I still don't have the reason, And you don't have the time, And it really makes me wonder if I ever gave a fuck about you. No necesito agregar m�s....",
      " And many more. i don't have the time to update anymore, before i was updating every day....",
      "I don't know EVERYTHING and can't be EVERYWHERE so a lil help couldn't hurt....Sort of a one-stop shop for the \"minority\" of folks that like to keep abreast of the ever-changing world around them but just don't have the time....",
      "A simpler how-to, aimed at folks who don't have the time or inclination to spend months reading up on early music theory, would be useful....",
      ". . I am sorry to say I am still not making headers for $$$. {I just don't have the time!}...",
      "why you like to buy but can't find the time to see them. . . . . . OH. . . NO!!!!!!!!!...",
      "It today&rsquos society this is very important because people are always in such a rush, many people do not have the time to sit down and read a several page document t o find the information they need....",
      "Because I know that you probably don't have the time to to read through pages of Gold World, I've painted some very broad strokes with you today and left out a lot o f details....",
      "Not only for human=) These I feel like swimming. But don't have the time to do so. And I dont wanna swim alone. Addicted to swimming le lah....",
      " I really don't have the time right now....",
      "I just don't have the time to sit on a computer for eight hours straight pouring over fics....",
      " Just things that I would like to say but don't have the time or opportunity to....",
      "Many times its because I do not have the time to spend with it....",
      "And no, I don't have the time to post anything more substantive tonight....",
      "I could, but I really don't have the time for that this semester....",
      "And if you just don't see them cause your life is to busy and you can't find the time, then make the time....",
      "People have too many horses and don't want to feed them through the winter or don't have the time to spend with them so this year and probably this year only the p rices are unbelievable....",
      " I have to go to bed now and do not have the time to do the blog over from scratch....",
      "I need to do something to publicize this section - get it on search engines, et cetera, but I can't find the time to do the necessary work....",
      "I don't have the time for people and their stupid head games, their drama, and the stupid games that happen at work....",
      "18. Please do not tell us your personal 'stories' b/c we don't care and quite frankly. . . . we just don't have the time....",
      "Seriously though, it seems like more and more people I talk to say, oh I read every day but I don't have the time to comment or know what to say or or or....",
      "I don't have the time to keep up with the 'cliquey' who's inviting who so I'm going back to the basics and here's how it's gonna be....",
      "I don't have the time, nor the brain power to waste on these triffling services....",
      "I don't have the time, nor the brain power to waste on 24 hours of bullshit...",
      "If you don't have the time to teach them to fish, give them a fish to survive until someone comes along with the time to teach them....",
      "(Note these are just examples, I have not mentioned everyone that I love, but then again we don't have the time for everybody to be mentioned....",
      "I either don't have the time twrite stuff down like this or I don't fee like it. I guess I feel like it tonight....",
      "However, I find that I don't have the time for it as of yet....",
      "I just don't have the time to devote to phone conversations. No. . . it's not like I'm sooo dam busy. Usually I'm not....",
      "And you don't have the time...And you don't have the time...And you don't have the time...",
      "This is bad because: I don't have any money to be spending on fricking tabloids, I don't have the time to read it, it was on Britney Spears who I am sick." 
  };
  
  static Regex regex;
  class IndexedPair implements Comparable
  {
    private Pattern punctPattern;
    public int characterAddress;
    public int originalLineIndex;
    private String shiftedLine, keyword; // caches
    private String[] ilines;
    
    IndexedPair() {} // for testing
    
    public IndexedPair(String[] lines, int characterAddress, int originalLineIndex) {
      this.ilines = lines;
      this.characterAddress = characterAddress;
      this.originalLineIndex = originalLineIndex;
    }
    
    public String getOriginalLine()
    {
      return ilines[originalLineIndex];
    }
    
    public String getShiftedline()
    {
      if (shiftedLine == null) { 
        String originalLine = ilines[originalLineIndex];
        String circularShiftedline = originalLine.substring
          (characterAddress)+" "+originalLine.substring
          (0, characterAddress);
        StringTokenizer parser = new StringTokenizer(circularShiftedline);
        StringBuilder parserLine = new StringBuilder(circularShiftedline.length());
        while (parser.hasMoreTokens())
          parserLine.append(parser.nextToken() + " ");
        this.shiftedLine =  parserLine.toString();
      } 
      return shiftedLine;
    }

    // handle punctuation here?
    public String getKeyword()
    {        
      if (keyword == null) {
        String line = getShiftedline();
        int firstSpaceIndex = line.indexOf(' ');
        if (firstSpaceIndex < 0)  return line; // error?
        this.keyword = line.substring(0, firstSpaceIndex);
      }
      return trimPunctuation(keyword);
    }
    
    public int compareTo(Object o)
    {
      IndexedPair ip = (IndexedPair)o;
      String iShifted = ip.getShiftedline();
      return iShifted.compareTo(getShiftedline());
    }
    
    String trimPunctuation(String token) {
      if (punctPattern == null) 
        punctPattern = Pattern.compile(PATT, Pattern.CASE_INSENSITIVE);
      Matcher m = punctPattern.matcher(token);
      boolean match = m.find();
      if (false && match) {
        for (int i = 0; i <= m.groupCount(); i++)
          System.out.println(i+") "+m.group(i)); 
      }
      if (!match || m.groupCount() < 1)
        throw new RiTaException("Invalid regex state for token="+token+"");
      
      return m.group(1);
    }
  }// end
  
   
  final static String PATT = "^(?:[\\p{Punct}]*)((?:.)|(?:\\w.*?\\w))(?:[\\p{Punct}]*)$";
  
  static String testTrimPunctuation(String token) {    
    return new RiKWICker(null).new IndexedPair().trimPunctuation(token);
  }
  
  /**  @invisible */
  public static void mainc(String[] args)
  {
    if (1==2) { 
      System.out.println(testTrimPunctuation("'''can't'")); 
      return; 
    }
    
    String[] ret = null;
    RiKWICker kwic = new RiKWICker(null);
    kwic.useMap = true;
    for (int j = 0; j < 2; j++)
    {       
      System.out.println("\n-------------- useMap="+kwic.useMap+", no-stops="+kwic.ignoreStopWords+"--------------");
      kwic.setLines(test);
      ret = kwic.lookup("games");
      
      for (int i = 0; i < ret.length; i++)
        System.out.println(i + ") " + ret[i]);
      
      //kwic.ignoreStopWords = true;      
      //kwic.useMap = !kwic.useMap;
    }
    // dump keys
    int idx = 0;
    for (Iterator i = kwic.lookupMap.keySet().iterator(); i.hasNext();)
      System.out.println((idx++)+")"+i.next());
  }
  /**  @invisible */
  public static void main(String[] args)
  {
    long start = System.currentTimeMillis();
    RiKWICker kwic = new RiKWICker(null);
    kwic.addLinesFromFile("examples/wordnet.verb.phrases.txt");
    System.out.println("TIME1: "+(System.currentTimeMillis()-start)/1000f+"s");
    start = System.currentTimeMillis();
/*    List hits = kwic._lookup("cat");
    System.out.println("TIME2: "+(System.currentTimeMillis()-start)/1000f+"s");
    System.out.println("RESULTS: "+hits.size());
    for (int i = 0; i < hits.size(); i++)
    {
      System.out.println(hits.get(i));
    }*/
    String[] hits = kwic.lookup("cat");
    System.out.println("TIME2: "+(System.currentTimeMillis()-start)/1000f+"s");
    for (int i = 0; i < hits.length; i++)
    {
      System.out.println(hits[i]);
    }
    System.out.println("RESULTS: "+hits.length);
  }

}// end
