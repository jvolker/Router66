package rita;

import rita.support.RiConstants;

/**
 * Simple class for event-based callbacks (generally dynamically
 * dispatched to PApplet subclasses). <p>A typical usage is to
 * switch on the type of a RiTaEvent within a callback:<pre>
    public void onRiTaEvent(RiTaEvent re)
    {
      switch (re.getType()) {
        case RiTa.TIMER_TICK:
           ...
        case RiTa.TEXT_ENTERED:
           ...
        case RiTa.SPEECH_COMPLETED:
           ...
        case RiTa.BEHAVIOR_COMPLETED:
           ...
      }
    }</pre>
 * @author dhowe
 */
public class RiTaEvent extends java.util.EventObject implements RiConstants
{ 
  protected String tag = "";
  protected Object data;
  protected int type;
  protected int id = -1;
  
  private static int ID_GEN;
  
  public RiTaEvent(Object source, int type) {
    this(source, type, null);
  }

  public RiTaEvent(Object source, int type, Object data) {
    super(source);    
    this.type = type;
    this.data = data;
    if (data != null) {
      this.tag = data.toString();
      if (data instanceof RiTextBehavior) {
        this.tag = ((RiTextBehavior)data).getName();
        this.id = ((RiTextBehavior)data).getId();
      }
    }
    if (id < 0) id = ++ID_GEN;
  }

  public String toString() {
    return "RiTaEvent[type="+type+", tag="+tag+" data="+getData()+", source="+source+"]";
  }
  
  /**
   * @deprecated
   * @see #getData() 
   * @invisible
   */
  public String getDescription() { return this.getData().toString(); }

  /**
   * Returns one of the event types specified in the RiConstants
   * interface, e.g., BEHAVIOR_COMPLETED, or SPEECH_COMPLETED.<p>
   * To test, use the following syntax:
   * <pre>
   *    if (re.getType() == RiTa.BEHAVIOR_COMPLETED)
   *      // do something
   * </pre>
   * @see RiTextBehavior
   * @see RiSpeech
   */
  public int getType()
  {
    return this.type;
  }
  
  /**
   * Returns auxillary data that varies based on the different event types.<br>
   * For example, <ul>
   * <li>if <code>type</code> == <code>SPEECH_COMPLETED</code>,
   * then <code>data</code> will contain a String with the last spoken text.
   *  
   * <li>if <code>type</code> == <code>TEXT_ENTERED</code>,
   * then <code>data</code> will contain a String with the entered text. 
   * 
   * <li>if <code>type</code> == <code>BEHAVIOR_COMPLETED</code>,
   * or <code>type</code> == <code>TIMER_COMPLETED</code>, then
   * <code>data</code> will contain the RiTextBehavior 
   * object that has just completed.
   * 
   * </ul>
   * 
   * @see RiTextBehavior
   * @see RiSpeech
   */
  public Object getData()
  {
    return this.data;
  }

  /**
   * 
   * Return the user-specified name for this event,
   * or for the associated TextBehavior. For example, if
   * a name has been assigned to a RiTa timer which generated 
   * this event, it will be accessible here.
   */
  public String getName() {
    return tag;
  }
  
  /**
   * @deprecated
   * @invisible
   * @see #getName
   */
   public String getTag() {
    return getName();
  }

  public int getId() {
    return id;
  }


}// end
