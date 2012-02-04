package rita.support.gui;

public class RiGUIEvent
{
  private RiGUIWidget source;
  private String message;

  public RiGUIEvent(RiGUIWidget argSource, String argMessage) {
    source = argSource;
    message = argMessage;
  }

  public RiGUIWidget getSource()
  {
    return source;
  }

  public String getMessage()
  {
    return message;
  }
}
