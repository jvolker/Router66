package rita.support;

/* HttpTimeoutFactory.java */
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 * Adapted from code by Niels Campbell
 * @invisible
 */
public class HttpTimeoutHandler extends sun.net.www.protocol.http.Handler
{
  private int iSoTimeout = 0;

  public HttpTimeoutHandler(int iSoTimeout)
  {
    // Divide the time out by two because two connection attempts are made in HttpClient.parseHTTP()

    if (iSoTimeout % 2 != 0)
    {
      iSoTimeout++;
    }
    this.iSoTimeout = (iSoTimeout / 1);
  }

  protected java.net.URLConnection openConnection(URL u) throws IOException
  {
    return new HttpTimeoutURLConnection(u, this, iSoTimeout);
  }

  protected String getProxy()
  {
    return proxy;
  }

  protected int getProxyPort()
  {
    return proxyPort;
  }

  public static void main(String[] args)
  {
    String sSoapUrl = "http://www.nanowrimo.org";//"http://192.168.0.223/mobaqSecurity/SslTunnelServlet";
    System.out.println("Connecting to [" + sSoapUrl + "]");

    URLConnection urlConnection = null;
    URL url = null;

    try
    {
      url = new URL((URL) null, sSoapUrl, new HttpTimeoutHandler(1000));
      urlConnection = url.openConnection();

      // Optional
      // url.setURLStreamHandlerFactory(new HttpTimeoutFactory(10000));

      System.out.println("Url class[" + urlConnection.getClass().getName() + "]");
    }
    catch (MalformedURLException mue)
    {
      System.out.println(">>MalformedURLException<<");
      mue.printStackTrace();
    }
    catch (IOException ioe)
    {
      System.out.println(">>IOException<<");
      ioe.printStackTrace();
    }

    HttpURLConnection httpConnection = (HttpURLConnection) urlConnection;
    System.out.println("Connected to [" + sSoapUrl + "]");

    byte[] messageBytes = new byte[10000];
    for (int i = 0; i < 10000; i++)
    {
      messageBytes[i] = 80;
    }

    try
    {
      httpConnection.setRequestProperty("Connection", "Close");
      httpConnection.setRequestProperty("Content-Length", String.valueOf(messageBytes.length));
      httpConnection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
      httpConnection.setRequestMethod("POST");
      httpConnection.setDoOutput(true);
      httpConnection.setDoInput(true);
    }
    catch (ProtocolException pe)
    {
      System.out.println(">>ProtocolException<<");
      pe.printStackTrace();
    }

    OutputStream outputStream = null;

    try
    {
      System.out.println("Getting output stream");
      outputStream = httpConnection.getOutputStream();
      System.out.println("Got output stream");

      outputStream.write(messageBytes);
    }
    catch (IOException ioe)
    {
      System.out.println(">>IOException<<");
      ioe.printStackTrace();
    }

    try
    {
      System.out.println("Getting input stream");
      InputStream is = httpConnection.getInputStream();
      System.out.println("Got input stream");

      byte[] buf = new byte[1000];
      int i;

      while ((i = is.read(buf)) > 0)
      {
        System.out.println("" + new String(buf));
      }
      is.close();
    }
    catch (Exception ie)
    {
      ie.printStackTrace();
    }

  }
}

// not used?
class HttpTimeoutFactory implements URLStreamHandlerFactory
{
  private int iSoTimeout = 0;

  public HttpTimeoutFactory(int iSoTimeout)
  {
    this.iSoTimeout = iSoTimeout;
  }

  public URLStreamHandler createURLStreamHandler(String str)
  {
    return new HttpTimeoutHandler(iSoTimeout);
  }
}
