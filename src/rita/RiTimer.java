package rita;

import java.util.*;

import rita.support.RiConstants;
import rita.support.behavior.BehaviorListener;
import rita.support.dyn.RiDynamicObject;

/**
 * A basic timer implementation to which one can pass
 * a PApplet, a RiTaEventListener, or any other object
 * that implements the method: onRiTaEvent(RiTaEvent re) 
 * <p>
 * Note: uses dynamic casting via the RiDynamicType object.
 * <p>A typical use in Processing might be:<pre>
    void setup(RiTaEvent re)
    {
      new RiTimer(this, 1f);
      
        OR
        
      RiTimer.start(this, 1f);  
    }
    
    public void onRiTaEvent(RiTaEvent re)
    {
      // called every 1 second
    }
    </pre>
    or, if (outside of Processing) onRiTaEvent(re) was in another class (e.g., MyApplet):<pre>
    public class MyApplet extends Applet 
    {
      RiTimer timer;
      public void init()
      {
        timer = new RiTimer(this, 1f);
      }
      
      public void onRiTaEvent(RiTaEvent re)
      {
        // called every 1 second
      }
    } </pre>   
 * @invisible
 * @author dhowe
 */
public class RiTimer implements RiConstants, BehaviorListener
{
  static {
    RiTa.class.getName(); // make sure we're loaded
  }
  
  /** @invisible */
  public static final int TIMER_RESOLUTION = 20;
  
  protected RiTextBehavior rtb;
  protected Timer internalTimer;
  private List listeners;
  
  /**
   * A convenience method that creates, starts, & returns a repeating timer which calls 
   * the method 'parent.onRiTaEvent(RiTaEvent e)' every <code>duration</code> seconds.
   */
  public static RiTimer start(Object parent, float duration) {
    return new RiTimer(parent, duration, null, true);
  }
  
  // ------------------------- Constructors -----------------------------
  
  public RiTimer(Object pApplet, float duration) {
    this(pApplet, duration, null, true);
  }
    
  public RiTimer(Object pApplet, float duration, boolean repeating) {
    this(pApplet, duration, null, repeating);
  }
  
  public RiTimer(Object pApplet, float duration, String timerName) {
    this(pApplet, duration, timerName, true);
  }
  
  public RiTimer(Object pApplet, double duration) {
    this(pApplet, (float)duration, null, true);
  }
    
  public RiTimer(Object pApplet, double duration, boolean repeating) {
    this(pApplet, (float)duration, null, repeating);
  }
  
  public RiTimer(Object pApplet, double duration, String timerName) {
    this(pApplet, (float)duration, timerName, true);
  }
  
  public RiTimer(Object pApplet, float duration, String name, boolean repeating) 
  {
    this(pApplet, 0, duration, name, repeating);
  }
  
  public RiTimer(Object pApplet, float startTimeOffset, float duration, String name, boolean repeating) 
  {
    rtb = new RiTextBehavior(null, name, 0, duration);
    if (name == null) rtb.setName("timer#"+rtb.getId());
    rtb.type = RiConstants.TIMER;
    rtb.repeating = repeating;
    rtb.addListener(this);
    internalTimer = new Timer(true);
    internalTimer.schedule/*AtFixedRate*/(new TimerTask() {
        public void run() { rtb.update(); } 
      }, (long)(startTimeOffset*1000), TIMER_RESOLUTION);      
    if (pApplet != null) 
      addListener(pApplet);
  }
  
  public void delete() {
    internalTimer.cancel();
    listeners.clear();
    rtb.delete();
  }
    
  public void setPaused(boolean b) {
    rtb.setPaused(b);
  }
  
  public boolean isPaused() {
    return rtb.isPaused();
  }

  public void pause(float time) {
    rtb.pause(time);
  }
    
  public void removeListener(Object o) 
  {
    if (listeners == null)  return;
    listeners.remove(o);
  }
  
  public void removeListeners() 
  {
    if (listeners == null)  return;
    listeners.clear();
  }
  
  public void addListener(Object o) 
  {
    if (o == null)  return;
    
    // check for the interface
    if (o instanceof RiTaEventListener) {
      _addListener((RiTaEventListener)o);
    }
    // check for the method
    else if (RiDynamicObject.instanceOf(RiTaEventListener.class, o)) {
      _addListener((RiTaEventListener)RiTa.dynamicCast(o, RiTaEventListener.class));
    }
    else 
      throw new RiTaException("The listener object does not " +
      	"have the required method: public void onRiTaEvent(RiTaEvent re);");
  }
  
  public void behaviorCompleted(RiTextBehavior behavior) {
    notifyListeners();
  }
  
  /** @invisible */
  private void notifyListeners()
  {
    if (listeners != null) {
      for (Iterator i = listeners.iterator(); i.hasNext();) {
        RiTaEventListener rt = (RiTaEventListener) i.next();
        rt.onRiTaEvent(new RiTaEvent(this, TIMER_TICK, rtb));
      }
    }
  }
  
  /**
   * Adds a BehaviorListener for all events fired from this behavior
   */
  private void _addListener(RiTaEventListener rlt)
  {
    if (listeners == null) 
      this.listeners = new LinkedList();
    listeners.add(rlt);    
  }
  
  public RiTextBehavior getBehavior() {
    return rtb;
  }
  
  
}// end
