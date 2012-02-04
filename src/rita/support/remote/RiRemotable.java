package rita.support.remote;

import java.util.Map;

import processing.core.PApplet;
import rita.RiObject;
import rita.RiTaException;

public abstract class RiRemotable extends RiObject implements RemoteConstants
{      
 // public static boolean serverEnabled = false;
  
  public RiRemotable(PApplet p) {
    super(p);
  }
  
  // to be implemented in subclasses
  public abstract void destroy();
    
  
  public static RiRemotable createRemote(Map params)
  {
    throw new RiTaException("Static method createRemote(Map)"
      + " not implemented in class: "+params.get("class"));
  } 
  
}// end
