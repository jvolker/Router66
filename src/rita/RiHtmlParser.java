package rita;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.*;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;
import javax.swing.text.html.parser.ParserDelegator;

import processing.core.*;
import rita.support.HttpTimeoutHandler;

/**
 * Provides various utility functions for fetching and parsing text data from
 * web pages using either the Document-Object-Model (DOM) or regular
 * expressions.
 * <p>
 * 
 * Parses an HTML document and returns the html text, with or without the HTML
 * tags stripped. Can also be used for custom parsing, as in the fetchLinks()
 * and fetchLinkText() methods (see example below.)
 * <p>
 * Simple Examples:
 * 
 * <pre>
 * RiHtmlParser rhp = new RiHtmlParser();
 * System.out.println(rhp.fetch(&quot;http://www.google.com&quot;)); // simple fetch
 * // -------------------------------------------------------------------
 * System.out.println(rhp.fetch(&quot;http://www.google.com&quot;, true)); // fetch &amp; strip
 * // -------------------------------------------------------------------
 * String[] links = rhp.fetchLinks(&quot;http://www.google.com&quot;); // get links
 * for (int i = 0; i &lt; links.length; i++)
 *   // &amp; print 'em
 *   System.out.println(i + &quot;) &quot; + links[i]); // one by one
 * // -------------------------------------------------------------------
 * System.out.println(rhp.parse(&quot;http://www.google.com&quot;)); // an empty parse
 * </pre>
 * <p>
 * 
 * Also provides a base implementation so that subclasses can override the
 * handleText(), handleSimpleTag(), handleStartTag(), and handleEndTag(),
 * methods to define custom behavior (as below and in RiGoogleParser).
 * <p>
 * An example of a custom parse to retrieve all linked text:
 * 
 * <pre>
 *      final List links = new ArrayList();
 *      rhp.customParse(new URL("http://www.google.com"), 
 *        new HTMLEditorKit.ParserCallback() // an inner class
 *        {
 *          boolean isLink = false;
 *          public void handleStartTag(Tag t, MutableAttributeSet a, int pos) {
 *            if (t == Tag.A) isLink = true;
 *          }
 *          public void handleText(char[] data, int pos) {
 *            if (isLink) links.add(new String(data));        
 *          }
 *          public void handleEndTag(Tag t, int pos) {
 *            if (t == Tag.A) isLink = false;
 *          }
 *        }
 *      ));
 *      
 *      // print out the link texts that we found 
 *      for (int i = 0; i < links.size(); i++) {
 *        System.out.println(i+") "+links.get(i));
 *      }
 * </pre>
 */
public class RiHtmlParser 
{
  private static final String UTF_8 = "UTF-8";
  public static final String DEFAULT_USER_AGENT = "Mozilla/4.05 [en] (WinNT; I)";
  
  public static String DEFAULT_CHARSET = UTF_8;
  public static int DEFAULT_CONNECT_TIMEOUT= 30000;
  public static int DEFAULT_READ_TIMEOUT = 30000;
  
  protected String userAgent = DEFAULT_USER_AGENT;
  protected int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
  protected int readTimeout = DEFAULT_READ_TIMEOUT;
  protected PApplet _pApplet;

  /**
   * @invisible
   * @deprecated
   */
  public RiHtmlParser() { 
    this(null); 
  }
  
  public RiHtmlParser(PApplet p) {
    this._pApplet = p;
  } // For consistency with RiObjects
  
  public String fetch(URL url)
  {
    UnicodeHttpFetch file = null;
    try
    {
      file = new UnicodeHttpFetch(url, connectTimeout, readTimeout);
    }
    catch (Exception e)
    {
      throw new RiTaException(e);
    }
    
    String mimeType = file.getMIMEType();
    Object content = file.getContent();
    
    if (mimeType.equals("text/html") && content instanceof String )
    {
      return ((String)content).trim();
    }
    throw new RiTaException("Unexpected mime-type: " +
      mimeType+" object is class: "+content.getClass().getName());
  }

  public Image fetchImage(String url)
  {
    try
    {
      return ImageIO.read(new URL(url));
    }
    catch (Exception e)
    {
      throw new RiTaException(e);
    }
  }

  //public PImage fetchPImage(String url)
  public PImage fetchPImage(PApplet pApplet, String url)
  {
    BufferedImage i = null;
    try
    {
      i = ImageIO.read(new URL(url));
    }
    catch (Exception e)
    {
      throw new RiTaException(e);
    }
    
    if (i == null) return null;
    
    int imgW = i.getWidth(pApplet);
    int imgH = i.getHeight(pApplet);
    PImage pImage = pApplet.createImage(imgW, imgH, PConstants.RGB); 
    pImage.loadPixels();

    for (int row = 0; row < imgH; row++) {
      for (int col = 0; col < imgW; col++) {
        int idx = row * imgW + col; 
        pImage.pixels[idx] = i.getRGB(col, row); 
      }
    }
    pImage.updatePixels();  
    
    return pImage; 
  }
/*
  public String fetch(String url, int connectTimeout, int readTimeout)
  {
    try
    {
      UnicodeHttpFetch file = new UnicodeHttpFetch(url, connectTimeout, readTimeout);
      String mimeType = file.getMIMEType();
      Object content = file.getContent();
      if ( mimeType.equals( "text/html" ) && content instanceof String )
      {
          return (String)content;
      }
      throw new RiTaException("Unexpected mimetype="+mimeType+" and/or objectType="+content.getClass());
    }
    catch (Exception e)
    {
      throw new RiTaException(e);
    }
  }*/

  public String post(String url, Map keyValuePairs)
  {
    try
    {
      return this.post(new URL(url), keyValuePairs);
    }
    catch (MalformedURLException e)
    {
      throw new RiTaException(e);
    }
  }

  public String post(URL url, Map keyValuePairs)
  {
    StringBuilder sb = new StringBuilder();
    try
    {
      Iterator it = keyValuePairs.keySet().iterator();
      while (it.hasNext())
      {
        String key = (String) it.next();
        String val = (String) keyValuePairs.get(key);
        sb.append(URLEncoder.encode(key, UTF_8) + "=" + URLEncoder.encode(val, UTF_8));
        if (it.hasNext())
          sb.append("&");

      }
      URLConnection conn = url.openConnection();
      conn.setDoOutput(true);
      OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
      wr.write(sb.toString());
      wr.flush();

      // Get the response
      sb.delete(0, sb.length());
      BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      String line;
      while ((line = rd.readLine()) != null)
      {
        sb.append(line);
        sb.append("\n");
      }
      wr.close();
      rd.close();
    }
    catch (Exception e)
    {
      throw new RiTaException(e);
    }

    return sb.toString();
  }

  public void customParse(String url, ParserCallback parserCallback)
  {
    try
    {
      customParse(new URL(url), parserCallback);
    }
    catch (MalformedURLException e)
    {
      e.printStackTrace();
    }
  }
  
  protected static void customParse(Reader r, HTMLEditorKit.ParserCallback pcb) throws IOException
  {
    new ParserDelegator().parse(r, pcb, true);
  }
  
  public void customParse(URL url, HTMLEditorKit.ParserCallback parserCallback)
  {
    try
    {
      StringReader str = new StringReader(fetch(url));
      customParse(str, parserCallback);
    }
    catch (Exception e)
    {
      throw new RiTaException(e);
    }
  }
  
  public String fetch(String url)
  {
    return fetch(url, false);
  }

  public String fetch(String url, boolean stripTags)
  {
    try
    {
      return this.fetch(new URL(url), stripTags);
    }
    catch (MalformedURLException e)
    {
      throw new RiTaException(e);
    }
  }
  protected String fetch(URL url, boolean stripTags)
  {
    if (!stripTags) return fetch(url);

    final StringBuilder parsed = new StringBuilder(1024);
    customParse(url, new HTMLEditorKit.ParserCallback()
    {
      boolean ignoreText = false;

      public void handleStartTag(Tag t, MutableAttributeSet a, int pos)
      {
        if (t == Tag.STYLE)
          ignoreText = true;
        super.handleStartTag(t, a, pos);
      }

      public void handleText(char[] data, int pos)
      {
        // System.out.println("handleText("+new String(data)+")");
        if (!ignoreText)
          parsed.append(data).append(" ");
      }

      public void handleEndTag(Tag t, int pos)
      {
        ignoreText = false;
        super.handleEndTag(t, pos);
      }
    });
    return parsed.toString();
  }
  
  public String fetch(String url, boolean stripTags, int connectionTimeout)
  {
    try
    {
      URL u = new URL( (URL) null, url, new HttpTimeoutHandler(connectionTimeout));
      return this.fetch(u, stripTags);
    }
    catch (MalformedURLException e)
    {
      throw new RiTaException(e);
    }
  }

  public String[] fetchLinkText(String url)
  {
    final List links = new ArrayList();
    try
    {
      customParse(url, new HTMLEditorKit.ParserCallback()
      {
        String link = null;

        public void handleStartTag(Tag t, MutableAttributeSet a, int pos)
        {
          if (t == Tag.A && link == null)
            link = "";
          super.handleStartTag(t, a, pos);
        }

        public void handleText(char[] data, int pos)
        {
          if (link != null && data.length > 0)
            link += new String(data);
          super.handleText(data, pos);
        }

        public void handleEndTag(Tag t, int pos)
        {
          if (t == Tag.A && link.length() > 0)
          {
            links.add(link.toString());
            link = null;
          }
          super.handleEndTag(t, pos);
        }
      });
    }
    catch (Exception e)
    {
      throw new RiTaException(e);
    }

    return (String[]) links.toArray(new String[links.size()]);
  }

  public String[] fetchLinks(String url)
  {
    final List links = new ArrayList();
    try
    {
      customParse(url, new HTMLEditorKit.ParserCallback()
      {
        String link = null;

        public void handleStartTag(Tag t, MutableAttributeSet a, int pos)
        {
          if (t == Tag.A && link == null)
            link = "<a " + a.toString().trim() + ">";
          super.handleStartTag(t, a, pos);
        }

        public void handleText(char[] data, int pos)
        {
          if (link != null && data.length > 0)
            link += (new String(data));
          super.handleText(data, pos);
        }

        public void handleEndTag(Tag t, int pos)
        {
          if (t == Tag.A && link.length() > 0)
          {
            links.add(link + "</a>");
            link = null;
          }
          super.handleEndTag(t, pos);
        }
      });
    }
    catch (Exception e)
    {
      throw new RiTaException(e);
    }
    return (String[]) links.toArray(new String[links.size()]);
  }
  
  public String getUserAgent()
  {
    return userAgent;
  }

  public void setUserAgent(String userAgent)
  {
    setUserAgent(userAgent);
  }
  
  public int getReadTimeout()
  {
    return readTimeout;
  }

  public void setReadTimeout(int readTimeout)
  {
    this.readTimeout = readTimeout;
  }

  public int getConnectTimeout()
  {
    return connectTimeout;
  }

  public void setConnectTimeout(int connectTimeout)
  {
    this.connectTimeout = connectTimeout;
  }
  
  /* Adapted from http://nadeausoftware.com/node/73#Code */
  class UnicodeHttpFetch
  {
    private Map<String, java.util.List<String>> responseHeader = null;
    private String MIMEtype = null, charset = null;
    private Object content = null;
    private URL responseURL = null;
    private int responseCode = -1;
    
    public UnicodeHttpFetch(String url) throws MalformedURLException, java.io.IOException
    {
      this(url, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }
    
    public UnicodeHttpFetch(URL url) throws MalformedURLException, java.io.IOException
    {
      this(url, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }
    
    public UnicodeHttpFetch(String url, int connectTimeout, int readTimeout) throws MalformedURLException, java.io.IOException
    {
      this(new URL(url), connectTimeout, readTimeout);
    }
    
    public UnicodeHttpFetch(URL url, int connectTimeout, int readTimeout) throws MalformedURLException, java.io.IOException
    {
      //System.out.println("UnicodeHttpFetch.UnicodeHttpFetch() "+new Date());
      
      // Open a URL connection.
      final URLConnection uconn = url.openConnection();
      if (!(uconn instanceof HttpURLConnection))
        throw new java.lang.IllegalArgumentException("URL protocol must be HTTP.");
      final HttpURLConnection conn = (HttpURLConnection) uconn;
  
      // Set up a request.
      conn.setRequestProperty("User-Agent", userAgent );
      conn.setRequestProperty("Accept-Charset", "iso-8859-1,*,utf-8");
      conn.setConnectTimeout(connectTimeout); 
      conn.setReadTimeout(readTimeout); 
      conn.setInstanceFollowRedirects(true);
      
      // conn.setRequestProperty("Host", "www.nyu.edu");
      // conn.setRequestProperty("Accept-Language", "en");
      // conn.setRequestProperty("User-agent", "spider");
      
      // Send the request.
      conn.connect();
  
      // Get the response.
      responseHeader = conn.getHeaderFields();
      responseCode = conn.getResponseCode();
      responseURL = conn.getURL();
      
      final int length = conn.getContentLength();
      final String type = conn.getContentType();
      if (type != null)
      {
        final String[] parts = type.split(";");
        MIMEtype = parts[0].trim();
        for (int i = 1; i < parts.length && charset == null; i++)
        {
          final String t = parts[i].trim();
          final int index = t.toLowerCase().indexOf("charset=");
          if (index != -1)
            charset = t.substring(index + 8);
        }
      }
      if (charset == null)  charset = "UTF-8"; // default
  
      // Get the content.
      final java.io.InputStream stream = conn.getErrorStream();
      if (stream != null) {
        content = readStream(length, stream);
      }
      else if ((content = conn.getContent()) != null
          && content instanceof java.io.InputStream) {
        content = readStream(length, (java.io.InputStream) content);
      }
      conn.disconnect();
    }
  
    /* Read stream bytes */
    private Object readStream(int length, java.io.InputStream stream) throws java.io.IOException
    {
      final int buflen = Math.max(1024, Math.max(length, stream.available()));
      byte[] buf = new byte[buflen];
      byte[] bytes = null;
  
      for (int nRead = stream.read(buf); nRead != -1; nRead = stream.read(buf))
      {
        if (bytes == null)
        {
          bytes = buf;
          buf = new byte[buflen];
          continue;
        }
        final byte[] newBytes = new byte[bytes.length + nRead];
        System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
        System.arraycopy(buf, 0, newBytes, bytes.length, nRead);
        bytes = newBytes;
      }
      
      return new String(bytes, charset);
    }
  
    /** Get the content. */
    public Object getContent()
    {
      return content;
    }
  
    /** Get the response code. */
    public int getResponseCode()
    {
      return responseCode;
    }
  
    /** Get the response header. */
    public java.util.Map<String, java.util.List<String>> getHeaderFields()
    {
      return responseHeader;
    }
  
    /** Get the URL of the received page. */
    public URL getURL()
    {
      return responseURL;
    }
  
    /** Get the MIME type. */
    public String getMIMEType()
    {
      return MIMEtype;
    }

  } // end UnicodeHttpFetch
 
  public static void main(String[] args)
  {
    String test = "http://asianfanatics.net/forum/topic/697454-cantonese-slang/";
    test = "http://rednoise.org/cookie-check.php";
    // TEST 1
    RiHtmlParser rh = new RiHtmlParser();
    String res = rh.fetch(test);
    System.out.println(res);
    
  }
  
}// end
