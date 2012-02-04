package rita.support;

import java.util.Map;

import processing.core.PApplet;

/**
 * An interface for RiTa objects that can be serialized as xml
 */
public interface Xmlable
{
  public static final String RITA_XML = "rxml";
  
  public String toXml();  

  public void initialize(PApplet p, Map properties);
  
}// end
