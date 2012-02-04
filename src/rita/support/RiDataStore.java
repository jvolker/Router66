package rita.support;

import processing.core.PApplet;
import rita.RiHtmlParser;
import rita.RiTa;
import rita.RiTaException;

// TODO: change to use post (and include passwd?)

/**
 * 
 * Simple key-value based datastore with 3 functions (get, set, & find).<p>
 * An example:<pre>
     RiDataStore rds = new RiDataStore("my.name.space");
     println( rds.set("mykey1", "fox1") ); 
     println( rds.set("mykey2", "fox12") );
     println( rds.get("mykey2") );
     println( rds.find("mykey") );   returns a String[]
 * </pre>
 * @invisible
 */
public class RiDataStore extends RiHtmlParser
{
  String login, password, namespace, host;
  static final String DELIM = "\\|DELIM\\|";
  
  public RiDataStore(String namespace) {
    this(null, namespace);
  }
  
  public RiDataStore(PApplet p, String namespace) {
    super(p);
    this.namespace = namespace;
  }
  
  // --------------------------- methods --------------------------- 
 /**
   * Fetches page contents from a string representing a URL 
   */
  private String dbFetch(String url)
  {
    if (host == null)
      throw new RiTaException("You must specify a valid db host!");
    return fetch(url);
  }
  
  /**
   * This method will inserts the value for the given key in
   * the specified namespace, updating it if it already exists.
   */
  public boolean set(String key, String value)
  {
    String sql = "?cmd=set&key="+getPrefixedKey(key)+"&value="+value;
    String res = dbFetch(host+sql+dbInfo());
    if (res.equals("ok")) return true;
    throw new RiTaException("Database Error: '"+res+"'");
  }

  /**
   * Returns the single value for the specified key, 
   * or null if the key is not found.
   */
  public String get(String key)
  {
    String sql = "?cmd=get&key="+getPrefixedKey(key);
    String res = dbFetch(host+sql+dbInfo());
    return (res == null || res.length()<1) ? null : res;
  }
  
  /**
   * Returns a String[] (containing one or more values)
   * where the specified input ('toMatch') is contained
   * in any part of the key, using the specified namespace,
   * or null if none exist. 
   */
  public String[] find(String toMatch)
  {
    String sql = "?cmd=find&query="+getPrefixedKey(toMatch);
    String res = dbFetch(host+sql+dbInfo());
    if (res == null || res.length()<1) return null;
    return res.split(DELIM);
  }  

  private String dbInfo()
  {
    String db = "";
    if (login != null) db += "&login="+login;
    if (password != null) db += "&pass="+password;
    return db;
  }
  
  // --------------------------- get/sets --------------------------- 
  
  private String getPrefixedKey(String key)
  {
    if (namespace == null || namespace.length()<1)
      throw new RiTaException("A namespace must be supplied!");
    return namespace+"."+key;
  }

  public String getNamespace()
  {
    return namespace;
  }

  public void setNamespace(String namespace)
  {
    this.namespace = namespace;
  }

  public String getHost()
  {
    return host;
  }

  public void setHost(String host)
  {
    this.host = host;
  }

  public String getLogin()
  {
    return login;
  }

  public void setLogin(String login)
  {
    this.login = login;
  }

  
  public String getPassword()
  {
    return password;
  }

  public void setPassword(String password)
  {
    this.password = password;
  }
  
  /**
   * @invisible
   */
  public static void main(String[] args)
  {
    RiDataStore rds = new RiDataStore("my.test.namespace");
    rds.setHost("http://localhost:8888/db/");
    rds.setLogin("root");
    rds.setPassword("dch");
    
    System.out.println(rds.set("mykey11","fox1")); 
    System.out.println(rds.set("mykey26","DELETE2"));
    System.out.println(rds.get("mykey21"));
    
    System.out.println(RiTa.asList(rds.find("mykey")));
    
  }
  
}
