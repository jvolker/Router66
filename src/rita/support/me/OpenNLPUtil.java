package rita.support.me;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

import opennlp.maxent.io.SuffixSensitiveGISModelReader;

import processing.core.PApplet;

import rita.RiTa;
import rita.RiTaException;

public abstract class OpenNLPUtil
{
  private static final boolean DBUG_LOADS = false;
  
  public static opennlp.maxent.MaxentModel getResourceAsModel
    (Class resourceDir, String fileName) 
  {
    //System.err.println("OpenNLPUtil.getModelFromJar("+fileName+")");
    try 
    {
      return new opennlp.maxent.io.SuffixSensitiveGISModelReader
        (resourceDir, fileName).getModel();
    }
    catch (Throwable e) {
      throw new RiTaException
        ("Unable to load model(class="+resourceDir+"): "+fileName,e);
    }    
  }
  
  public static opennlp.maxent.MaxentModel getFileAsModel(PApplet p, String fileName) {
    return getFileAsModel(p, fileName, true);
  }
  
  public static opennlp.maxent.MaxentModel getFileAsModel(PApplet p, String fileName, boolean makeGuesses) {
    try
    {
      return new SuffixSensitiveGISModelReader(getFile(p, fileName, makeGuesses)).getModel();
    }
    catch (IOException e)
    {
      if (DBUG_LOADS) System.err.println("[WARN] Unable to load model: "+fileName);
      return null;
    }
  }
  
  public static File getFile(PApplet p, String fileName, boolean makeGuesses) 
  {
    if (!makeGuesses) return new File(fileName);
    
    File f = null;
    String[] guesses = RiTa.getDataDirectoryGuesses();
    FOR: for (int j = 0; j < guesses.length; j++) {
      String fName = fileName;      
      if (guesses[j].length() > 0) {
        if (RiTa.isAbsolutePath(fileName))
          continue FOR;
        fName = guesses[j] + RiTa.OS_SLASH + fName;
      }
    
      if (DBUG_LOADS)System.out.print("[INFO] Trying "+fName+"...");
      
      f = new File(fName); 
      if (f != null && f.exists()) {             
        if (DBUG_LOADS)System.out.println("OK");
        break;
      }      
      if (DBUG_LOADS)System.out.println(" failed");      
    }
    return f;
  }
  
  public static BufferedReader getResourceAsBufferedReader(Class resourceDir, String fileName)
  {
    //System.err.println("OpenNLPUtil.getBufferedReaderFromJar("+resourceDir+","+fileName+")");
    InputStream is = resourceDir.getResourceAsStream(fileName);
    if (is == null) {
      if (DBUG_LOADS)System.out.println("[WARN] Unable to load file: "+fileName);
      return null;
    }
    return getInputStreamAsBufferedReader(is, fileName);
  }
    
  public static BufferedReader getFileAsBufferedReader(PApplet p, String fileName)
  {
    return getFileAsBufferedReader(p, fileName, true);
  }
  
  public static BufferedReader getFileAsBufferedReader(PApplet p, String fileName, boolean makeGuesses)
  {
    File f = getFile(p, fileName, makeGuesses);
    try {
      return getInputStreamAsBufferedReader(new FileInputStream(f), fileName);
    } 
    catch (FileNotFoundException e) {
      if (DBUG_LOADS)System.out.println("[WARN] Unable to load file: "+fileName);
      return null;
    }
  }
  
/*  public static BufferedReader getFileAsBufferedReader(PApplet p, String fileName)
  {
//System.out.println("OpenNLPUtil.getFileAsBufferedReader("+fname+") from "+RiTa.cwd());
    InputStream is;
    try {
      is = RiTa.openStream(p, fileName);
    } catch (RiTaException e) {  
      System.out.println("[WARN] Unable to load file: "+fileName);
      return null;  
    }
    return getInputStreamAsBufferedReader(is, fileName);
  }*/
  
  private static BufferedReader getInputStreamAsBufferedReader(InputStream is, String fname)
  {
    //if (DBUG_LOADS) System.out.println("OpenNLPUtil.getInputStreamAsBufferedReader("+fname+")");        
    InputStreamReader isr = new InputStreamReader(is);
    return new BufferedReader(isr);
    
  }
/*  
  static class RiTaGISModelReader extends opennlp.maxent.io.SuffixSensitiveGISModelReader
  {
    public RiTaGISModelReader(PApplet p, String fileName) throws IOException {
      super(openStreamLocal(fileName), fileName.indexOf(".bin")>0);
    }  
  }*/
/*  private static InputStream openStreamLocal(String streamName)
  {
    String[] guesses = { "src"+RiTa.OS_SLASH+"data", "data", "" };
    
    InputStream is = null;
    try // check for url first  (from PApplet)
    {
      URL url = new URL(streamName);
      is = url.openStream();
      System.out.println("Returning URL stream: "+is);
      return is;
    } catch (MalformedURLException mfue) {
      // not a url, that's fine
    } catch (FileNotFoundException fnfe) {
      // Java 1.5 likes to throw this when URL not available. (fix for 0119)
      // http://dev.processing.org/bugs/show_bug.cgi?id=403
    } catch (Throwable e) {
      throw new RiTaException(e);
    }

    for (int i = 0; i < guesses.length; i++) {
      String guess = streamName;
      if (guesses[i].length() > 0) {
        if (RiTa.isAbsolutePath(guess))
          continue;
        guess = guesses[i] +  RiTa.OS_SLASH + guess;
      }
      if (!guess.endsWith("addenda.txt"))
        System.out.print("[INFO] Trying "+guess);
      try {
        is = new FileInputStream(guess);
        System.out.println("Returning File stream: "+is);
        if (!guess.endsWith("addenda.txt")) 
          System.out.println("... OK");        
      } 
      catch (FileNotFoundException e) {
        if (!guess.endsWith("addenda.txt"))
          System.out.println("... failed");
      }
      if (is != null) break;
    }
    if (is == null)
      throw new RiTaException("Unable to create stream for: "+streamName);
    
    return is;
  } */
  
}// end


