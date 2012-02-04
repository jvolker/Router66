package rita;

import java.awt.Image;
import java.net.URL;
import java.util.Map;

import javax.swing.text.html.HTMLEditorKit;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * Not required in general usage...
 */
public interface HtmlParserIF
{

  public String getUserAgent();

  public void setUserAgent(String userAgent);

  /**
   * Fetches an Image from a string URL.
   */
  public Image fetchImage(String url);

  /**
   * Fetches an Image from a string URL and converts it to a processing.core.PImage
   * @author Megan Hugdahl
   */
  public PImage fetchPImage(PApplet pApplet, String url);

  /**
   * Fetches page contents from a string URL via a GET-type request
   * with a max-timeout of 'connectionTimeout' milliseconds
   */
  public String fetch(String url, int connectTimeout, int readTimeout);

  /**
   * Fetches page contents from the specified URL via a POST request using the
   * String values specified in 'keyValuePairs'.
   */
  public String post(String url, Map keyValuePairs);

  /**
   * Fetches page contents from the specified URL via a POST request using the
   * String values specified in 'keyValuePairs'.
   */
  public String post(URL url, Map keyValuePairs);

  /**
   * Returns the contents of the URL (generally a text/HTML page) after
   * executing the callbacks in the ParserCallback object.
   */
  public void customParse(String url, HTMLEditorKit.ParserCallback parserCallback);

  /**
   * Fetches the contents of the URL (generally a text/HTML page) with all HTML
   * tags removed as specified by the <code>stripTags</code> flag.
   */
  public String fetch(String url);

  /**
   * Fetches the contents of the URL (generally a text/HTML page) with all HTML
   * tags removed as specified by the <code>stripTags</code> flag.
   */
  public String fetch(String url, boolean stripTags);

  /**
   * Fetches the contents of the URL (generally a text/HTML page) with all HTML
   * tags removed as specified by the <code>stripTags</code> flag.
   */
  public String fetch(String url, boolean stripTags, int connectionTimeout);

  /**
   * Returns the text found within each link, e.g., of the format: <a
   * ...>text</a>, as a String array, one element per link on the page
   */
  public String[] fetchLinkText(String url);

  /**
   * Returns all tags of the format <a ...>...</a> as a String array, one
   * element per link on the page
   */
  public String[] fetchLinks(String url);

  /**
   * Fetches page contents from a string URL via a GET-type request.
   */
  public String fetch(URL url);

}