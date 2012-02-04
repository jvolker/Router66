package rita.support;

import java.io.*;
import java.net.URL;
import java.util.*;

import processing.core.PApplet;
import rita.RiLexicon;
import rita.RiTaException;

public class RitaSpanishDictionary extends PApplet
{
  RiLexicon _lex;

  public void setup()
  {

    _lex = new RiLexicon(this);

    try
    {
      InputStream is = new BufferedInputStream(new FileInputStream("data/aspellutf8combined.txt"));
      _lex.setLexicalData(createLexicon(is, 700000));
    }
    catch (IOException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  public void draw()
  {
    PrintStream out;
    try
    {
      out = new PrintStream(System.out, true, "UTF-8");
      out.println(_lex.getRandomWord());
    }
    catch (UnsupportedEncodingException e1)
    {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
  }

  // The below methods were modified from RiCMULexicon.java
  // in the Rita source code

  private static final Map overrides = new HashMap();
  static
  {
    // overrides.put("offical", null);
    // overrides.put("blog","b l ao g\tnn vbg");
    // overrides.put("legible","l eh g ax b ax l\tjj");

  }

  private static InputStream getInputStream(URL url) throws IOException
  {
    if (url.getProtocol().equals("file"))
      return new FileInputStream(url.getFile());
    else
      return url.openStream();
  }

  public Map createLexicon(InputStream is, int estimatedSize) throws IOException
  {

    Map lexicon = new LinkedHashMap(estimatedSize * 4 / 3);
    addToMap(is, lexicon);
    return lexicon;
  }

  private int addToMap(InputStream is, Map lexicon) throws IOException
  {
    int num = 0;
    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
    String line = reader.readLine();
    while (line != null)
    {
      if (!line.startsWith("***"))
      {
        parseAndAdd(lexicon, line);
        num++;
      }
      line = reader.readLine();
    }
    reader.close();
    reader = null;
    for (Iterator iterator = overrides.keySet().iterator(); iterator.hasNext();)
    {
      String key = (String) iterator.next();
      String data = (String) overrides.get(key);
      if (lexicon.containsKey(key))
      {
        if (data == null)
        {
          if (RiLexicon.VERBOSE_WARNINGS)
            System.err.println("REMOVING: " + key);
          lexicon.remove(key);
        }
        else
          lexicon.put(key, data);
      }
    }
    // if (!overrides.containsKey(parts[0]))

    // lexicon.put(parts[0], parts[1].trim()+LEXICON_DELIM+parts[2].trim());
    // else
    // System.err.println("SKIPPING: "+parts[0]);
    // if (parts[0].equals("rpm"))
    // System.out.println(parts[0]+": '"+parts[1]+"' "+LEXICON_DELIM+" '"+parts[2]+"'");

    // String d ="";
    // lexicon.put(parts[0], overrides.get(parts[0]));
    // System.out.println("SKIPPING: "+parts[0]);
    // }
    // else

    return num;
  }

  private void parseAndAdd(Map lexicon, String line)
  {
    if (line == null || line.length() < 1)
      return;

    String[] parts = line.split("\t");
    if (parts == null || parts.length != 3)
      throw new RiTaException("Illegal entry: " + line);
    lexicon.put(parts[0].replace(":", ""), parts[1].trim() + "\t" + parts[2].trim());
  }
  
  public static void main(String[] args)
  {
    new RitaSpanishDictionary();
  }

}