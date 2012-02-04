package rita.support;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import processing.core.PApplet;
import rita.RiObject;
import rita.RiPosTagger;
import rita.RiTa;
import rita.RiTaException;

/**
 * A REST client for the Microsoft Web N-Gram Service.
 * <p> 
 * Arguments:
 * <p>
 * 'apiKey' - your Microsoft-supplied authorization key for the service
 * <p>
 * 
 * 'model' - the specific model you wish to use. If the argument is not provided
 * a default model is used (currently 'bing-body/jun09/3').
 * <p>
 * 
 * 'serviceURI' - can be left to the default unless the service location has changed.
 * <p>
 */
public class MsNGramClient extends RiObject
{
  private static final String ERROR = "ERROR";
  private static final String UTF_8 = "UTF-8";
  private static final String DEFAULT_USER_AGENT = "Mozilla/4.05 [en] (WinNT; I)";
  private static final int MAX_RESULTS = 30;
  private static Charset charset;

  protected String userAgent = DEFAULT_USER_AGENT;
  protected String serviceUri = "http://web-ngram.research.microsoft.com/rest/lookup.svc/";
  protected String model = "bing-body/jun09/3";
  protected String apiKey = "ce297f85-0c3d-4265-95bf-2ee02ef0eaca";
  protected String savedCookie  = "";
  protected HistoryQueue history = new HistoryQueue(6);
  protected float backoff = -1;
  private static final String[] NUMBERS = {
      "one","two","three","four","five","six","seven","eight","nine",
  };
  private static boolean WRITE_FILE_CACHE = false;
  
  FileWriter cacheWriter;
  // Constructors =============================================
  
  public MsNGramClient()
  {
    this(null);
  }
  
  public MsNGramClient(PApplet pApplet)
  {
    super(pApplet);
    if (WRITE_FILE_CACHE) {
      try
      {
        cacheWriter = new FileWriter(getClass().getName()+".cache");
      }
      catch (IOException e)
      {
        System.err.println("[WARN] Unable to create cacheWriter..."+e.getMessage()); 
      }
    }
  }

  // Interface =============================================
  private List<PWord> generatePWords(String phrase)
  {
    return this.generatePWords(phrase, MAX_RESULTS);
  }
  
  private List<PWord> generatePWords(String phrase, int maxResultSize)//, String cookie)
  {
    //System.out.println("generatePWords("+phrase+", "+maxResultSize+")");
    
    String url = getOpUrl("gen");
    url += "&p=" + quote(phrase);
    
    if (maxResultSize > -1)
      url += "&n=" + maxResultSize;
/*  if (cookie != null && cookie.length() > 0)
      url += "&cookie=" + cookie;*/

    String s = fetch(url);
    if (s == null || s.length() < 1)
    {
      error();
      return null;
    }

    String[] lines = s.split("\\r\\n");
    if (lines == null || lines.length <= 2)
    {
      error(lines+"\n"+RiTa.asList(lines));
      return null;
    }

 /*   for (int i = 0; i < lines.length; i++)
    {
      System.out.println(i + ") '" + lines[i] + "'");
    }*/

    //RiProbableImpl[] result = new RiProbableImpl[lines.length - 2];
    //float max = -Float.MAX_VALUE, min = Float.MAX_VALUE;
    float sum = 0;
    List<PWord> result = new ArrayList<PWord>();
    for (int i = 0; i < lines.length - 2; i++)
    {
      String[] parts = lines[i + 2].split(";");
      String word = parts[0];
      
      if (word.matches("[1-9]")) 
        word = NUMBERS[Integer.parseInt(word)];
      
      if (word.matches("<\\/s>")) 
        word = ".";
      
      if (!verifyWord(word)) continue;
      
      float p = Float.parseFloat(parts[1]); 
      p = (float)Math.pow(10, p);
      
      PWord pw = new PWord(word, p);
     
      result.add(pw);
      sum += p;
    } 
    
    if (result.size() < 1) {
      throw new RiTaException("No acceptable word found: "+RiTa.asList(lines));
    }
    
    for (Iterator it = result.iterator(); it.hasNext();)
    {
      PWord pword = (PWord) it.next();
      pword.normalize(sum);
    }

    // not working (in API) at moment
    // saveCookie(lines);

    try
    {
      backoff = Float.parseFloat(lines[1]);
    }
    catch (NumberFormatException e)
    {
      System.err.println("[WARN] Unable to set backoff value: " + e.getMessage());
    }
    
    return result;
  }

  private void saveCookie(String[] lines)
  {
    try
      {
        String c = RiTa.trimEnds(lines[0]);
        String ck = c.split(" \\s+")[0];
        this.savedCookie = ck;
      }
      catch (Exception e1)
      {
        System.err.println("[WARN] Unable to store cookie: " + e1.getMessage());
      }
  }
  
  private String verifyEndsOnNoun(String phrase)
  {
    boolean foundWord = false;
    Stack stack = new Stack();
    String[] words = phrase.split(" ");
    String[] tags = RiTa.posTag(words);
    for (int i = words.length-1; i >= 0; i--)
    {
      if (!foundWord && !RiPosTagger.isNoun(tags[i]))
        continue;
      foundWord = true;
      stack.push(words[i]);
    }
    String result = "";
    for (Iterator it = stack.iterator(); it.hasNext();)
    {
      result = it.next() + " "+ result;
    }
    if (result.length() == 0) {
      System.err.println("[WARN] "+getClass().getName()+".verifyEndsOnNoun() failed for: '"+phrase+"'");
      return phrase;
    }
    return result;
  }
  
  private String trimEndingStopWords(String phrase)
  {
    boolean foundWord = false;
    Stack stack = new Stack();
    String[] words = phrase.split(" ");
    for (int i = words.length-1; i >= 0; i--)
    {
      if (!foundWord && isStopWord(words[i]))
        continue;
      foundWord = true;
      stack.push(words[i]);
    }
    String result = "";
    for (Iterator it = stack.iterator(); it.hasNext();)
    {
      result = it.next() + " "+ result;
    }
    return result;
  }
  
  private boolean isStopWord(String string)
  {
    for (int i = 0; i < stopWords.length; i++)
    {
      if (string.equals(stopWords[i]))
        return true;
    }
    return false;
  }

  private boolean verifyWord(String word)
  {
    if (word.matches("[12][0-9]{3}")) // years are ok
        return true;
    
    if (!word.matches("(I)|([A-Za-z'-])+")) {
      System.out.println("Skipping (regex) '"+word+"' *************************************");
      return false;
    }
    else if (history.contains(word)) {
      System.out.println("Skipping (history) '"+word+"' *************************************");
      return false;
    }
    return true;
  }

  /**
   * Given a phrase (a sequence of space-separated words), find the words that
   * are the most likely to follow the phrase.
   * <p>
   * The returned String[] contains words in decreasing probability order.
   * 
   * <pre>
   *      Note: 
   *         For a model of order N, if phraseContext contains more than N-1 words, the 
   *         excess words at the beginning of phrase are ignored.
   * </pre>
   */
  public String[] generate(String phrase, int maxResultSize)//, String cookie)
  {
    //System.out.println("MsNGramClient.generate("+phrase+","+maxResultSize+")");
    String url = getOpUrl("gen");
    url += "&p=" + quote(phrase);
    if (maxResultSize > -1)
      url += "&n=" + maxResultSize;
/*    if (cookie != null && cookie.length() > 0)
      url += "&cookie=" + cookie;*/

    String s = fetch(url);
    if (s == null || s.length() < 1)
    {
      error();
      return null;
    }

    String[] lines = s.split("\\r\\n");
    if (lines == null || lines.length <= 2)
    {
      error(lines+"\n"+RiTa.asList(lines));
      return null;
    }

 /*   for (int i = 0; i < lines.length; i++)
    {
      System.out.println(i + ") '" + lines[i] + "'");
    }*/

    String[] result = new String[lines.length - 2];
    for (int i = 0; i < lines.length - 2; i++)
    {
      result[i] = lines[i + 2].split(";")[0];
    }

    // not working (in API) at moment
/*    try
    {
      String c = RiTa.trimEnds(lines[0]);
      String ck = c.split(" \\s+")[0];
      this.savedCookie = ck;
    }
    catch (Exception e1)
    {
      System.err.println("[WARN] Unable to store cookie: " + e1.getMessage());
    }
*/
    try
    {
      backoff = Float.parseFloat(lines[1]);
    }
    catch (NumberFormatException e)
    {
      System.err.println("[WARN] Unable to set backoff value: " + e.getMessage());
    }

    return result;
  }
  

  public String nextWord(String phrase)
  {
    List l = this.generatePWords(phrase);
    PWord pw = (PWord) RiTa.probabalisticSelect(l);
    //System.out.println("\nSelect: "+pw+" from: "+l);
    return pw.word;
  }

  public String[] generate(String phrase)
  {
    return this.generate(phrase, MAX_RESULTS);//, null);
  }

  public String[] generate(String phrase, String cookie)
  {
    return this.generate(phrase, MAX_RESULTS);//, cookie);
  }

  /**
   * Finds the joint probability of the words in a phrase (a sequence of
   * space-separated words), in the current model.
   * 
   * <P>
   * The base-10 log of the joint probability of the word sequence.
   * 
   * <pre>
   *     For instance, if you have the following word sequence:
   *       w1, w2, …, wn
   *       
   *     The return value is the log of the following:
   *         P(w1) P(w2|w1) … P(wn | wn-m+1…wn-1)
   *         
   *     Notes:
   *       The token <s> represents the beginning of a phrase.
   *       Punctuation is generally ignored.
   * </pre>
   */
  public float getProbability(String phrase)
  {
    return _getProbability(phrase, "jp");
  }

  /**
   * Finds the conditional probability of the words in a phrase (a sequence of
   * space-separated words) using the current model.
   * <P>
   * The base-10 log of the conditional probability of the last word in a
   * sequence, in a given context.
   * 
   * <pre>
   *  For instance, for the following word sequence (for an ngram-model of size m):
   *         w1, w2, …, wn
   *       
   *       The return value is the log of the following:
   *           P(wn | wn-m+1…wn-1)
   *           
   *       Notes:
   *         If n=0, the return value is Float.NaN.
   *         If n>m, the words at the beginning of phrase are ignored.
   * </pre>
   */
  public float getConditionalProbability(String phrase)
  {
    return _getProbability(phrase, "cp");
  }

  /**
   * Get a list of currently supported N-Gram models
   */
  public String[] getAvailableModels()
  {
    String result = fetch(serviceUri);
    if (result == null)
      throw new RiTaException("No response");
    return result.split("\\r\\n");
  }

  // Getters/Setters =================================================

  public String getApiKey()
  {
    return apiKey;
  }

  public void setApiKey(String apiKey)
  {
    this.apiKey = apiKey;
  }

  public String getUserAgent()
  {
    return userAgent;
  }

  public void setUserAgent(String userAgent)
  {
    this.userAgent = userAgent;
  }

  /**  // doesn't seem to work at moment
   * 
   * Returns the generated cookie (an opaque placeholder to facilitate
   * subsequent calls) from a the last call to generate(). Before the first call
   * to generate(), getGeneratedCookie() will return an empty string.

  public String getGeneratedCookie()
  {
    return savedCookie;
  }   */

  public String getServiceUri()
  {
    return serviceUri;
  }

  public void setServiceUri(String serviceUri)
  {
    this.serviceUri = serviceUri;
  }

  public String getModel()
  {
    return model;
  }

  /**
   * Should be of the form returned by #getModels()
   */
  public void setModel(String model)
  {
    this.model = model;
  }

  /**
   * The backoff value is made available in the event that you wish to call
   * generate() with a shortened phraseContext, so as to make the returned
   * values compare-able.
   * <p>
   * Before the first call to generate(), getBackoffValue() will return an empty
   * string.
   */
  public float getBackoffValue()
  {
    return backoff;
  }

  // Helpers =============================================

  private float _getProbability(String phrase, String op)
  {
    if (phrase == null || phrase.length() < 1)
      return Float.NaN;

    String url = getOpUrl(op);
    url += "&p=" + quote(phrase);
    String s = fetch(url);
    if (s == null)
      error();
    float f = -1;
    try
    {
      f = Float.parseFloat(s);
    }
    catch (NumberFormatException e)
    {
      error(e.getMessage());
    }
    return f;
  }

  private String quote(String phrase)
  {
    try
    {
      return URLEncoder.encode(phrase, UTF_8);
    }
    catch (UnsupportedEncodingException e)
    {
      throw new RiTaException(e);
    }
    //return "\"" + phrase.replaceAll("\\s+", "+") + "\"";
  }

  private String getOpUrl(String op)
  {
    return serviceUri + model + '/' + op + "?u=" + apiKey;
  }

  protected String fetch(String uri)
  {
    try
    {
      URL url = new URL(uri); 
      //System.out.println("MsNGramClient.fetch("+url+")");
      return fetch(url);
    }
    catch (Exception e)
    {
      String s =  ERROR + ":" + e.getMessage();
      throw new RiTaException(s);
    }
  }

  protected String fetch(URL url)
  {
    StringBuilder html = new StringBuilder(1024);
    try
    {
      HttpURLConnection conn = (HttpURLConnection) (url.openConnection());
      conn.setRequestProperty("User-Agent", userAgent);
      conn.setRequestProperty("Accept-Language", "en");
      conn.setRequestProperty("Accept-Charset", "iso-8859-1,*,utf-8");
      conn.connect();
      readStream((InputStream) conn.getContent(), html);
    }
    catch (IOException e)
    {
      throw new RiTaException(e);
    }
 
    return html.toString();
  }

  protected static void readStream(InputStream is, StringBuilder sb) throws IOException
  {
    if (charset == null)
      charset = Charset.forName(UTF_8);
    ByteBuffer buff = ByteBuffer.allocate(65536); // max socket-size
    ReadableByteChannel rbc = Channels.newChannel(is);
    buff.clear();
    while (rbc.read(buff) != -1)
    {
      buff.flip();
      sb.append(charset.decode(buff));
      buff.compact();
    }
  }

  private void error(String s)
  {
    throw new RiTaException("Unexpected error! " + s);
  }

  private void error()
  {
    error("");
  }
  
  public String generateWords(String startOfPhrase, int numWords)
  {
    int words = 0;
    StringBuilder sb = new StringBuilder();
    sb.append(startOfPhrase);
    while (++words < numWords) {
      String next = nextWord(sb.toString());
      sb.append(" "+next);
    }
    String s = sb.toString();
    s = s.replaceAll(" i ", " I ");
    s = s.replaceAll(" I m ", " I'm ");
    s = s.replaceAll(" s ", "'s ");
    s = s.replaceAll(" u s ", " U.S. ");

    if (WRITE_FILE_CACHE) {
      try
      {
        cacheWriter.append(s.trim()+"\n");
        cacheWriter.flush();
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }
    
    //System.out.println(s);
    return s;
  }
  
  Set context;
  private void loadContextFromFile(String fileName)
  {
    String data = RiTa.loadString(getPApplet(), fileName);
    String[] words = RiTa.tokenize(data);
    
    if (context == null)
      context = new HashSet();
    for (int i = 0; i < words.length; i++)
    {
      context.add(words[i]);
    }
    //System.out.println(context);
    
  }
  
  // ------------------------------------------------------------------
  static final String[] stopWords = {  "the", "and", "a", "of",
    "in", "i", "I", "you", "is", "to", "that", "it", "for", "on",
    "have", "with", "?", "this", "be", "...", "not", "are", "as", "was", "but", "or",
    "from", "my", "at", "if", "they", "your", "all", "he", "by", "one", "me", "what",
    "so", "can", "will", "do", "an", "about", "we", "just", "would", "there", "no",
    "like", "out", "his", "has", "up", "more", "who", "when", "don't", "some", "had",
    "them", "any", "their", "it's", "only", ";", "which", "i'm", "been", "other",
    "were", "how", "then", "now", "her", "than", "she", "well", "also", "us", "very",
    "because", "am", "here", "could", "even", "him", "into", "our", "much", "too",
    "did", "should", "over", "want", "these", "may", "where", "most", "many", "those",
    "does", "why", "please", "off", "going", "its", "i've", "down", "that's", "can't",
    "you're", "didn't", "another", "around", "must", "few", "doesn't", "every", "yes",
    "each", "maybe", "i'll", "away", "doing", "oh", "else", "isn't", "he's", "there's",
    "hi", "won't", "ok", "they're", "yeah", "mine", "we're", "what's", "shall",
    "she's", "hello", "okay", "here's", "less", "a", "about", "above", "across",
    "after", "afterwards", "again", "against", "all", "almost", "alone", "along",
    "already", "also", "although", "always", "am", "among", "amongst", "amoungst",
    "amount", "an", "and", "another", "any", "anyhow", "anyone", "anything", "anyway",
    "anywhere", "are", "around", "as", "at", "back", "be", "became", "because",
    "become", "becomes", "becoming", "been", "before", "beforehand", "behind", "being",
    "below", "beside", "besides", "between", "beyond", "bill", "both", "bottom", "but",
    "by", "call", "can", "cannot", "cant", "co", "computer", "con", "could", "couldnt",
    "cry", "de", "describe", "detail", "do", "done", "does", "down", "due", "during",
    "each", "eg", "eight", "either", "eleven", "else", "elsewhere", "empty", "enough",
    "etc", "even", "ever", "every", "everyone", "everything", "everywhere", "except",
    "few", "fifteen", "fify", "fill", "find", "fire", "first", "five", "for", "former",
    "formerly", "forty", "found", "four", "from", "front", "full", "further", "get",
    "give", "go", "had", "has", "hasnt", "have", "he", "hence", "her", "here",
    "hereafter", "hereby", "herein", "hereupon", "hers", "herself", "him", "himself",
    "his", "how", "however", "hundred", "i", "ie", "if", "in", "inc", "indeed",
    "interest", "into", "is", "it", "its", "itself", "keep", "last", "latter",
    "latterly", "least", "less", "ltd", "made", "many", "may", "me", "meanwhile",
    "might", "mill", "mine", "more", "moreover", "most", "mostly", "move", "much",
    "must", "my", "myself", "name", "namely", "neither", "never", "nevertheless",
    "next", "nine", "no", "nobody", "none", "noone", "nor", "not", "nothing", "now",
    "nowhere", "of", "off", "often", "on", "once", "one", "only", "onto", "or",
    "other", "others", "otherwise", "our", "ours", "ourselves", "out", "over", "own",
    "part", "per", "perhaps", "please", "put", "rather", "re", "same", "see", "seem",
    "seemed", "seeming", "seems", "serious", "several", "she", "should", "show",
    "side", "since", "sincere", "six", "sixty", "so", "some", "somehow", "someone",
    "something", "sometime", "sometimes", "somewhere", "still", "such", "system",
    "take", "ten", "than", "that", "the", "their", "them", "themselves", "then",
    "thence", "there", "thereafter", "thereby", "therefore", "therein", "thereupon",
    "these", "they", "thick", "thin", "third", "this", "those", "though", "three",
    "through", "throughout", "thru", "thus", "to", "together", "too", "top", "toward",
    "towards", "twelve", "twenty", "two", "un", "under", "until", "up", "upon", "us",
    "very", "via", "was", "we", "well", "were", "what", "whatever", "when", "whence",
    "whenever", "where", "whereafter", "whereas", "whereby", "wherein", "whereupon",
    "wherever", "whether", "which", "while", "whither", "who", "whoever", "whole",
    "whom", "whose", "why", "will", "with", "within", "without", "would", "yet", "you",
    "your", "yours", "yourself", "yourselves", };

  private static String[] TEST = {
      "The following terms and conditions nine", 
      "The internet the internet in addition", 
      "The right to have a good", 
      "The same amount of income and", 
      "The end of the U.S.", 
      "The following the success of last", 
      "The best online photo album free", 
      "The best of times for other", 
      "The first to suggest it would", 
      "The state or zip find a", 

  };
  public static void main(String[] args)
  {
    WRITE_FILE_CACHE = true;
    MsNGramClient mm = new MsNGramClient();
    //mm.loadContextFromFile("unabomber.txt");
    
    //if (1==1) return;

    List l = new ArrayList();
    if (true) {
    for (int i = 0; i < 10; i++) 
      l.add(mm.generateWords("The", 5));
    }
    else {
      for (int i = 0; i < TEST.length; i++)
      {
        l.add(TEST[i]);
      }
    }
    
    int k = 0;
    for (Iterator it = l.iterator(); it.hasNext(); k++) {
      String phrase = (String) it.next();
      System.out.println(k+") "+phrase);
      // maybe phrases should always end with a noun?
      System.out.println("   "+mm.trimEndingStopWords(phrase));
      System.out.println("   "+mm.verifyEndsOnNoun(phrase));
    }
  }


}// end

