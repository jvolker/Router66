package rita;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;
import java.util.List;

import processing.core.PApplet;
import rita.support.RiConstants;

/** 
 * Superclass for all RiTa objects
 * @invisible
 * @author dhowe
 */
public abstract class RiObject implements RiConstants
{  
  private static int ID_GEN = 0;
  
  static {
    if (!RiTa.SILENT)System.out.println("[INFO] RiTa.version ["+RiTa.VERSION+"] "+RiTa.elapsed());    
  }
  
	protected PApplet _pApplet;
  private int id;
	
  public PApplet getPApplet() { return _pApplet; }
  
	public RiObject(PApplet pApplet) {    
    this._pApplet = pApplet;	 
    this.id = ++ID_GEN;    
	}

	/** 
	 * Empty method to be optionally implemented by subclasses
	 * needing to clean-up resources on program end.<br>
	 * Not to be called in user-code.
	 */
	public void dispose() {}
	
	 /* Adds dispose() call to applications as well as applets */
  protected void registerDispose() {
    registerDispose(getPApplet());
  }
    
	/* Adds dispose() call to applications as well as applets */
  protected void registerDispose(PApplet p) {
    if (p != null) {
      p.registerDispose(this);
      if (p.frame != null) {
        p.frame.addWindowListener(new WindowAdapter(){
          public void windowClosing(WindowEvent e) {
            dispose();
        }});
      }
    }
  }
  
  /*
   * Current platform in use, one of the
   * PConstants WINDOWS, MACOSX, MACOS9, LINUX or OTHER.
   */
  protected static int getOS() {
    return PApplet.platform;
  }

  protected static List asList(char[] arr) { return RiTa.asList(arr); }  
  protected static List asList(Object[] arr) { return RiTa.asList(arr); }
  protected String[] strArr(List l) { return RiTa.strArr(l); }  
  protected static void cwd() { cwd(System.out); }  
  protected static void cwd(PrintStream ps) {
    ps.println(System.getProperty("user.dir"));
  }

  public int getId() {
    return id;
  }
  
  /**
   * Not for general use...
   * @invisible
   */
  protected void setId(int id) {
    this.id = id;
  }

  public static synchronized int nextId()
  {
    return ++ID_GEN;
  }

/*  public static void main(String[] args)
  {
    RiTa.SILENT = true;
    //System.out.println("'"+trimRegex(" \t he \t llo  \t ", "\\s+")+"'"); 
  }
*/
}// end
