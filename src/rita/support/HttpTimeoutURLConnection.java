package rita.support;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;

import sun.net.www.http.HttpClient;

/**
 * Adapted from code by Niels Campbell
 * @invisible
 */
public class HttpTimeoutURLConnection extends sun.net.www.protocol.http.HttpURLConnection
{
  public HttpTimeoutURLConnection(URL u, HttpTimeoutHandler handler, int iSoTimeout) throws IOException
  {
    super(u, handler);
    HttpTimeoutClient.setSoTimeout(iSoTimeout);
  }

  public void connect() throws IOException
  {
    if (connected)
    {
      return;
    }

    try
    {
      if ("http".equals(url.getProtocol())) // && !failedOnce <-PRIVATE
      {
        // for safety's sake, as reported by KLGroup
        synchronized (url)
        {
          http = HttpTimeoutClient.getNew(url);
        }
      }
      else
      {
        if (handler instanceof HttpTimeoutHandler)
        {
          http = new HttpTimeoutClient(super.url, ((HttpTimeoutHandler) handler).getProxy(), ((HttpTimeoutHandler) handler).getProxyPort());
        }
        else
        {
          throw new IOException("HttpTimeoutHandler expected");
        }
      }

      ps = (PrintStream) http.getOutputStream();
    }
    catch (IOException e)
    {
      throw e;
    }

    connected = true;
  }

  protected HttpClient getNewClient(URL url) throws IOException
  {
    HttpTimeoutClient httpTimeoutClient = new HttpTimeoutClient(url, (String) null, -1);
    return httpTimeoutClient;
  }

  protected HttpClient getProxiedClient(URL url, String s, int i) throws IOException
  {
    HttpTimeoutClient httpTimeoutClient = new HttpTimeoutClient(url, s, i);
    return httpTimeoutClient;
  }

}

class  HttpTimeoutClient extends HttpClient{
private static int iSoTimeout = 0;

public HttpTimeoutClient(URL url, String proxy, int proxyPort) throws IOException
{
  super(url, proxy, proxyPort);
}

public HttpTimeoutClient(URL url) throws IOException
{
  super(url, (String) null, -1);
}

public static HttpTimeoutClient getNew(URL url) throws IOException
{
  HttpTimeoutClient httpTimeoutClient = (HttpTimeoutClient) kac.get(url);

  if (httpTimeoutClient == null)
  {
    httpTimeoutClient = new HttpTimeoutClient(url); 
  }
  else
  {
    httpTimeoutClient.url = url;
  }

  return httpTimeoutClient;
}

public static void setSoTimeout(int iNewSoTimeout)
{
  iSoTimeout = iNewSoTimeout;
}

public static int getSoTimeout()
{
  return iSoTimeout;
}

// Override doConnect in NetworkClient

protected Socket doConnect(String s, int i) throws IOException, UnknownHostException, SocketException
{
  Socket socket = super.doConnect(s, i);

  // This is the important bit
  socket.setSoTimeout(iSoTimeout);
  return socket;
}
}