package rita;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import processing.core.PApplet;
import rita.support.ifs.RiSearcherIF;

// THIS SHOULD BE REMOTABLE (for caching)!

// NEED TO IGNORE GOOGLE STOP-WORDS ??

// IMPLEMENT SERIALIZED CACHING

/*
FACES: Simply add &imgtype=face to the URL, and enter. Your new face search results will be displayed.
http://images.google.com/images?q=paris&imgtype=face

NEWS:  If you append &imgtype=news to the URL, the new results features images related to the news event.
http://images.google.com/images?q=paris&imgtype=news
*/

/* GOOGLE Stop-Words?
 
    I
    a
    about
    an
    are
    as
    at
    be
    by
    com
    de
    en
    for
    from
    how
    in
    is
    it
    la
    of
    on
    or
    that
    the
    this
    to
    was
    what
    when
    where
    who
    will
    with
    und
    the
    www

 */
 /* Note: Too many queries in a short time may likely get you program blocked
 * by the Google server. In this case,  */

/**  
 * A utility object for obtaining unigram, bigram, and weighted-bigram counts
 * for words and phrases via the Google search engine. 
 <pre>
      RiGoogleSearch gp = new RiGoogleSearch(this);
      float f = gp.getBigram("canid", "ski'd");</pre>
 */
public class RiGoogleSearch extends RiObject implements RiSearcherIF
{	
  /** @invisible */
  public static boolean DBUG = false;
  
  /** @invisible */
  public static boolean DBUG_FETCH = false;
  
  private static final String FORBIDDEN_503 = "Server returned HTTP response code: 503";
  private static final String QQ = "";
  private static final String END_STRING = "</?string>";
  private static final String END_KEY = "</?key>";
  private static final String END_DICT = "</dict>";
  private static final String VALUE = "Value";
  private static final String NAME = "Name";
  private static final String KEY = "<key>";
  private static final String DICT = "<dict>";
  private static final String STRING = "<string>";
  private static final String DOMAIN = "Domain";

  private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.0.1) Gecko/2008070208";
  
  static final String RESULTS_PAT_STR = " <b>1</b> - <b>(?:[0-9]|10)</b> of (?:about )?<b>([0-9,]+)</b>";
  static final String RESULTS_PAT_STR2 = "<div id=resultStats>About ([0-9,]+) results<nobr>";
  static final String RESULTS_PAT_STR3 = "<div id=resultStats>([0-9,]+) results?<nobr>";
  static final String NO_RESULTS_PAT1 = "&nbsp;No results found for";
  static final String NO_RESULTS_PAT2 = "</b> - did not match any documents.";
  static final String FORBIDDEN_403_PAT = "<title>403 Forbidden</title>";
  
  protected static Pattern patResult, patResult2, patResult3, patNoResult;
  protected static boolean cacheEnabled = true;
  protected static Map cache; // static cache    
  protected static int numCalls;
  
  protected String googleCookie, userAgent, cookiePath;
  protected boolean searchBooks;

  /** @invisible */
  public RiGoogleSearch() {
    super(null);
  } 

  public RiGoogleSearch(PApplet pApplet) {
    super(pApplet);
  } 
  
  /**
   *  Allows for a custom cookie String, generally of the format:
   *    "PREF=ID=ee8b4e3d4e15d9f5:TM=1219349742:LM=1219349742:S=MGXvStJPax5onGxv;"...
   *  to prevent some 403 errors for non-cookie carrying requests.

  public RiGoogleSearch(String googleCookie) {   
    super(null);
    this.googleCookie = googleCookie;
    this.userAgent = DEFAULT_USER_AGENT;
  }    
   */
  
  /**
   * Allows the google cookie to be automatically loaded from the file system.
   * As an example, the path for Safari is generally:
   *    '/Users/$userName/Library/Cookies/Cookies.plist'
   */
  public void setLocalCookiePath(String path) {
    //System.out.println("RiGoogleSearch.setLocalCookiePath("+path+")");
    try {
      if (!(new File(path).exists())) 
        throw new Exception();
    }
    catch (Exception e) {
      System.out.println("[WARN] Unable to find cookie file at: "
        +path+"\n"+(e==null?QQ:e.getMessage()));
      return;
    }
    this.cookiePath = path;
    PApplet p = getPApplet();
    String contents = RiTa.loadString(p, path);
//System.out.println("cookies: "+contents); 
    String[] cookies = contents.split(END_DICT);
    List cmaps = createCookieMaps(cookies, true);
    String cStr = QQ;
    for (Iterator iterator = cmaps.iterator(); iterator.hasNext();)
    {
      Map cookie = (Map) iterator.next();
      String name = (String)cookie.get(NAME);
      cStr += name+"="+cookie.get(VALUE)+"; ";
    }
    if (!RiTa.SILENT)System.out.println("[INFO] Using Cookie: "+cStr);
    googleCookie = cStr;
  }

  private List createCookieMaps(String[] cookies, boolean googleOnly)
  {
    List l = new ArrayList();
    for (int i = 0; i < cookies.length; i++)
    {
      String key = null;
      boolean gotDict = false;
      Map m = new HashMap();
      String[] lines = cookies[i].split("\n");
      for (int j = 0; j < lines.length; j++)
      {
        String line = RiTa.trimEnds(lines[j]);
        if (line.equals(DICT)) {
          gotDict = true;
          continue;
        }
        if (gotDict) {
          if (line.startsWith(KEY))
            key = line.replaceAll(END_KEY, QQ);
          else if (line.startsWith(STRING) && key != null) {
            m.put(key, line.replaceAll(END_STRING, QQ));
            key = null;
          }
        }
      }
      if (m.size() > 0) { 
        String domain = (String) m.get(DOMAIN);
        if (domain != null) {
          if (!googleOnly || domain.endsWith("google.com"))
            l.add(m);
        }
      }
    }
    return l;
  }

  protected void setRequestHeaders(HttpURLConnection conn) {
    conn.setRequestProperty("User-Agent", DEFAULT_USER_AGENT);
    conn.setRequestProperty("Accept-Language", "en-us,en;q=0.5");
    conn.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
    conn.setRequestProperty("Keep-Alive", "300");
    conn.setRequestProperty("Connection", "keep-alive");
    conn.setRequestProperty("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
    conn.setRequestProperty("Referer", "http://www.google.com/");
    if (googleCookie != null)
      conn.setRequestProperty("Cookie", googleCookie); 
  } 
  
 
  /**
   * Returns the number of live URL connections made by this object so far
   * @see #setCacheEnabled(boolean)
   */
  public int getCallCount()
  {
    return numCalls;
  }

  /** 
   * Returns the trigram coherence for the word pair where trigram-coherence
   * (w1, w2, w3) = count(w1 + w2 + w3) / (getBigram(w1,w2) + getBigram(w2,w3)))
   * @invisible
   */
  public float getTrigram(String word1, String word2, String word3)
  { 
    // perhaps this should be:
    //   tcount = count(1,2,3) / (count(1,2) + count(2,3) ); 
    //   return tcount / (bigram(1,2) + bigram(2,3));
    if (1==1) throw new RuntimeException("Not yet implemented...");
    long trigram = getCount("\""+word1+" "+word2+" "+word3+"\"");
    float firstPair = getBigram(word1, word2);
    float lastPair  = getBigram(word2, word3);
    return trigram / (firstPair + lastPair);
  }
  
  /** 
   * Returns the bigram coherence for the word pair where
   * coherence(w1, w2) = count(w1 + w2)/(count(w1) + count(w2))
   * [from Gervas]
   */
  public float getBigram(String word1, String word2)
  {        
//System.out.println("RiGoogler.getBigram("+"\""+word1+" "+word2+"\""+")");
    long pair = getCount("\""+word1+" "+word2+"\"");
    if (!DBUG && pair==0) return 0;
    long first = getCount(word1);
    long last = getCount(word2);    

    if (DBUG) {
      System.out.println("getCount1("+word1+") = "+first);
      System.out.println("getCount2("+word2+") = "+last);
      System.out.println("getPair(\""+word1+" "+word2+"\") = "+pair);
      
      if (pair==0) {
        System.out.println("getBigram("+word1+","+word2+") = 0");
        return 0;
      }
    }
        
    float val = pair / (float)(first+last);
    if (DBUG)System.out.println("getBigram("+word1+","+word2+") = "+val);
    return val;
  }
  
  /**
   * Returns the product of the count of the query and the # of words. 
   */
  public float getWeightedUnigram(String query)
  {
    long count = getCount("\""+query+"\"");
    String[] words = query.split(" ");
    if (words == null || words.length<2)
      throw new RiTaException("Invalid input: expect >1 word but got: "+query);
    return count * words.length;    
  }
  
  /**
   * Returns the product of the count of the query and the # of words. 
   */
  public float getWeightedUnigram(String[] words)
  {
    long count = getCount("\""+RiTa.join(words," ")+"\"");
    if (words == null || words.length<2)
      throw new RiTaException("Invalid input: expect >1 word but got: "+RiTa.asList(words));
    return count * words.length;    
  }
  
  /**
   * Returns the product of the avg value of all bigram pairs 
   * and the min bigram value in the sentence. Equivalent to (
   * but more efficient than): getBigramAvg(s) * getBigramMin(s)
   */
  public float getWeightedBigram(String[] sentence)
  {    
    float sum = 0;
    float minVal = Float.MAX_VALUE;
    for (int i = 1; i < sentence.length; i++)  {
      float bg = getBigram(sentence[i-1], sentence[i]);
      if (bg == 0) return 0; 
      if (bg < minVal) minVal = bg;
      sum += bg;
    }
    float avg = sum/(float)(sentence.length-1);
    if (DBUG)System.out.println("avg="+avg+" / min="+minVal);
    return avg * minVal;
  }
  
  /**
   * Returns the avg value of all bigram pairs in
   * the sentence. 
   */
  public float getBigramAvg(String[] sentence)
  {
    float sum = 0;
    for (int i = 1; i < sentence.length; i++)  {      
      sum += getBigram(sentence[i-1], sentence[i]);
//System.out.println("getBigramAvg() = "+sum+"/"+(sentence.length-1));
    }
    return sum/(float)(sentence.length-1);
  }
  
  /**
   * Returns the min value of all bigram pairs in
   * the sentence. 
   */
  public float getBigramMin(String[] sentence)
  {
    float min = Float.MAX_VALUE;
    for (int i = 1; i < sentence.length; i++) { 
      float test = getBigram(sentence[i-1], sentence[i]);
      if (test == 0) return 0;
      if (test < min) min=test;
    }
    return min;
  }
  
  /**
   * Returns the weighted value of all bigram pairs in
   * the sentence. 
   */
  public float getWeightedBigram(List sentence)
  {
    return getWeightedBigram((String[]) sentence.toArray(new String[sentence.size()]));
  }
  
  /**
   * Returns the avg value of all bigram pairs in
   * the sentence. 
   */
  public float getBigramAvg(List sentence)
  {
    return getBigramAvg((String[]) sentence.toArray(new String[sentence.size()]));
  }
  
  /**
   * Returns the min value of all bigram pairs in
   * the sentence. 
   */
  public float getBigramMin(List sentence)
  {
    return getBigramMin((String[]) sentence.toArray(new String[sentence.size()]));
  }
  
  /**
   * Returns the number of hits via Google for the search query. To obtain an
   * exact match, place your query in quotes, e.g. <pre>
   *   int k = gp.getCount("\"attained their momentum\"");
   * </pre>
   * @param query The string to be searched for.
   * @return The number of hits Google returned for the search query.
   */
  public int getCount(String query)
  {
    if (cacheEnabled) {
      if (cache == null) cache = new HashMap();
      Integer tmp = (Integer)cache.get(query);      
      if (tmp != null) return tmp.intValue();
    }
    String line = QQ;
    StringBuilder html = null;
    BufferedReader in = null;
    try
    {      
      String type = searchBooks ? "books" : "search";
      query = query.replaceAll("\"", "%22");
      query = query.replaceAll(" ", "+");
      String queryURL = "http://www.google.com/"+type+"?hl=en&safe=off&q="
        + query
        //+URLEncoder.encode(query, "UTF-8")
        +"&btnG=Google+Search";
      
      queryURL = queryURL.replaceAll("%2B", "+");      

      URL url = new URL(queryURL);
      if (DBUG_FETCH) System.out.println("Url: "+queryURL);
      HttpURLConnection conn = (HttpURLConnection) (url.openConnection()); 
      setRequestHeaders(conn);
       
      numCalls++;
      try {
        in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      }
      catch (IOException e) {
        if (e.getMessage().startsWith(FORBIDDEN_503))
          throw new RiTaException("Google request rejected(503): "+queryURL);
      }
 
      if (patResult == null) { 
        patResult  = Pattern.compile(RESULTS_PAT_STR);
        patResult2 = Pattern.compile(RESULTS_PAT_STR2);
        patResult3 = Pattern.compile(RESULTS_PAT_STR3);
      }

      int result = -1;

      int k = 0;
      if (DBUG_FETCH) html = new StringBuilder();
      WHILE: while ((line = in.readLine()) != null)
      {       
        k++;
        if (line.indexOf(FORBIDDEN_403_PAT)>-1)
          throw new RiTaException("Google request rejected(403): "+line);
        
        if (line.indexOf("<p>... but your computer or network may be sending automated queries.")>-1)
          throw new RiTaException("Google request rejected(Sorry): "+line);

        if (DBUG_FETCH) html.append(line);  

        //System.out.println(k+") "+line);

        if (line.indexOf(NO_RESULTS_PAT1)>-1 || line.indexOf(NO_RESULTS_PAT2)>-1) {
//System.out.println("NO RESULTS FOUND ***********************");     
          result = 0;
          break WHILE;
        }
                
        Matcher m = patResult.matcher(line);
        if (m.find()) {
          String countOld = m.group(1);
          StringBuilder countNew = new StringBuilder();
          for (int i = 0; i < countOld.length(); i++) {
            char c = countOld.charAt(i);
            if (c >= '0' && c <= '9')
              countNew.append(c);
          }
          long l = Long.parseLong(countNew.toString());
          result = (l > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int)l;
          if (!searchBooks) break WHILE;
        }
        
        Matcher m2 = patResult2.matcher(line);
        if (m2.find()) {
          String countOld = m2.group(1);
          StringBuilder countNew = new StringBuilder();
          for (int i = 0; i < countOld.length(); i++) {
            char c = countOld.charAt(i);
            if (c >= '0' && c <= '9')
              countNew.append(c);
          }
          long l = Long.parseLong(countNew.toString());
          result = (l > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int)l;
          if (!searchBooks) break WHILE;
        }
        
        Matcher m3 = patResult3.matcher(line);
        if (m3.find()) {
          String countOld = m3.group(1);
          StringBuilder countNew = new StringBuilder();
          for (int i = 0; i < countOld.length(); i++) {
            char c = countOld.charAt(i);
            if (c >= '0' && c <= '9')
              countNew.append(c);
          }
          long l = Long.parseLong(countNew.toString());
          result = (l > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int)l;
          if (!searchBooks) break WHILE;
        }

        //System.out.println((++idx)+") "+line);
      }
      if (result >= 0) {
        if (cacheEnabled) 
          cache.put(query, new Integer(result));
        return result; 
      }      
      else if (DBUG_FETCH) {
        String htmlStr = html==null ? "null" : html.toString(); 
        new FileWriter("google-out.html").write(htmlStr.toString());        
        throw new RiTaException
          ("No value found for query: '"+query+"' writing .html file to google-out.html");
      }
    }
    catch (IOException e)
    {
      System.err.println("EXCEPTION: "+e.getClass()+" query="+query);
      e.printStackTrace();
      throw new RiTaException("query="+query,e);
    }
    finally {
      try {
        if (in != null) in.close();
      } 
      catch (IOException e) {}
      in = null;
    }
    return 0; // should never happen
  }

  /**
   * Returns whether the cache is enabled
   */
  public static boolean isCacheEnabled()
  {
    return cacheEnabled;
  }

  /**
   * Sets whether the cache is enabled and duplicate
   * requests are returned immediately rather than
   * re-contacting google (default=true).
   */
  public static void setCacheEnabled(boolean enableCache)
  {
    cacheEnabled = enableCache;
  }


  /**
   * Returns the current user-agent
   */
  public String getUserAgent()
  {
    return this.userAgent;
  }
  
  /**
   * Sets the user-agent for subsequent requests
   */
  public void setUserAgent(String userAgent)
  {
    this.userAgent = userAgent;
  }

  /**
   * Returns the cookie string used in the last sent query  
   */
  public String getCookie() {
    return this.googleCookie;
  }

  /**
   * Sets the cookie string for subsequent requests 
   */
  public void setCookie(String googleCookie)
  {
    this.googleCookie = googleCookie;
  }  
  
  /**
   * if set to true, searches are restricted to google books
   */
  public void useGoogleBooks(boolean b)
  {
    this.searchBooks = b;
  }

  private static String toToken(String word)
  {
    String trim = word;
    trim = trim.replaceAll("\"", "\b");
    trim = trim.replaceAll("\'", "\b");
    if (trim.charAt(0) == '-' || trim.charAt(trim.length() - 1) == '-')
      trim = trim.replaceAll("-", "\b");
    trim = trim.trim();
    // System.out.println("trimmed: " + trim);
    return trim;
  }

  public static void main(String[] args)
  {
/*      RiGoogleSearch.DBUG = true;
      RiGoogleSearch.DBUG_FETCH = true;
      //String sentence = "The white house has 10000 yards.";
      // http://books.google.com/books?q=%22queen+michael+vanity%22&btnG=Search+Books
      String search = "\"queen+michael+vanity\"";
      String[] sentence = {"The", "white", "house", "has","15200000000", "yards"};
      RiGoogleSearch gp = new RiGoogleSearch();
      //gp.useGoogleBooks(true);
      long f = gp.getCount("-smith -nations \"﻿an, inquiry\"");
      System.out.println(f);
      //gp.useGoogleBooks(true);
      //int k = gp.getCount(search);
      //System.out.println(k);
      //gp.useGoogleBooks(true);
      //gp.setLocalCookiePath("/Users/dhowe/Library/Cookies/Cookies.plist");

      //float f = gp.getBigram("love", "you");
      //float f  = gp.getBigramAvg(sentence);
      
      
      //System.out.println(gp.getBigram("canid", "ski'd"));
*/  }
 
}// end


