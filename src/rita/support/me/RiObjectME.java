package rita.support.me;

import java.util.Map;

import processing.core.PApplet;
import rita.RiTa;
import rita.support.remote.RemoteConstants;
import rita.support.remote.RiRemotable;

public abstract class RiObjectME extends RiRemotable implements RemoteConstants
{      
  public static final boolean LOAD_FROM_MODEL_DIR = true; 
  
  public static final String ERROR_MSG =//"\n          from "+RiTa.cwd()+
    "\n  Have you downloaded the statistical models from the RiTa site?";
  
  protected static String modelDir = "models"+RiTa.OS_SLASH;

  public static String getModelDir() {
    return modelDir;       
  }

  /**
   * Set the directory from which statistical models should be loaded<p>
   * Note: expects an absolute path: e.g., '/Users/jill/models'
   */
  public static void setModelDir(String modelDirectory) {
    modelDir = modelDirectory;
    if (!(modelDir.endsWith("/") || modelDir.endsWith("\\")))
      modelDir += RiTa.OS_SLASH;
  }
  
  protected static void setModelDir(Map m) {
    if (m.containsKey("modelDir"))  
      setModelDir((String)m.get("modelDir"));
  }
  
  public RiObjectME(PApplet p) {
    super(p);
  }
  
}// end
