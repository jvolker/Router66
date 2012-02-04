package rita.support.gui;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import processing.core.PApplet;
import processing.core.PFont;

import rita.RiObject;

public abstract class RiGUIWidget extends RiObject
{
  protected int _x;
  protected int _y;
  protected int wid;
  protected int hgt;  
  protected int index;
  protected boolean wasClicked;
  
  protected PFont font;
  protected String label;  
  protected Object listener;      
  protected RiGUILookAndFeel lookAndFeel;
  protected RiGUIController controller;
  protected float fontSize = RiGUIController.defaultFontSize;

  public RiGUIWidget(PApplet p) {
    super(p);
  }

  public void setIndex(int i)
  {
    index = i;
  }

  public int getIndex()
  {
    return index;
  }

  public void update(int argX, int argY) {
    /* empty */
  }

  public void draw() {
    /* empty */
  }

  public void setController(RiGUIController c)
  {
    controller = c;
  }

  public RiGUIController getController()
  {
    return controller;
  }

  public void initWithParent()
  {
    /* empty */
  }

  public void setLookAndFeel(RiGUILookAndFeel lf)
  {
    lookAndFeel = lf;
  }

  public String getLabel()
  {
    return label;
  }

  public void setLabel(String argLabel)
  {
    label = argLabel;
  }

  public boolean canReceiveFocus()
  {
    return true;
  }

  public int getWidth()
  {
    return wid;
  }

  public void setWidth(int newWidth)
  {
    if (newWidth > 0)
      wid = newWidth;
  }

  public int getHeight()
  {
    return hgt;
  }

  public void setHeight(int newHeight)
  {
    if (newHeight > 0)
      hgt = newHeight;
  }

/*  public void addActionListener(Object newListener)
  {
    listener = newListener;
  }*/

  
  public void setSize(int newWidth, int newHeight)
  {
    if (newHeight > 0 && newWidth > 0)
    {
      hgt = newHeight;
      wid = newWidth;
    }
  }

  public void setPosition(int newX, int newY)
  {
    if (newX > 0 && newY > 0)
    {
      _x = newX;
      _y = newY;
    }
  }

  public void setX(int newX)
  {
    if (newX > 0)
      _x = newX;
  }

  public int getX()
  {
    return _x;
  }

  public void setY(int newY)
  {
    if (newY > 0)
      _y = newY;
  }

  public int getY()
  {
    return _y;
  }

  public void mouseEvent(MouseEvent e)
  {
    if (e.getID() == 501)
    {
      if (isMouseOver(e.getX(), e.getY()))
        wasClicked = true;
    } else if (e.getID() == 502 && wasClicked
        && isMouseOver(e.getX(), e.getY()))
    {
      fireEventNotification(this, "Clicked");
      wasClicked = false;
    }
  }

  public void keyEvent(KeyEvent e)
  {
    /* empty */
  }

  public void actionPerformed(RiGUIEvent e)
  {
    /* empty */
  }

  public void fireEventNotification(RiGUIWidget argComponent, String argMessage)
  {
    if (listener != null)
    {
      do {
        try
        {
          RiGUIEvent e = new RiGUIEvent(argComponent, argMessage);
          Method m = (listener.getClass().getDeclaredMethod("actionPerformed",
              new Class[] { e.getClass() }));
          try
          {
            m.invoke(listener, new Object[] { e });
          }
          catch (InvocationTargetException ex)
          {
            System.out.println(ex.getCause().getMessage());
          }
          catch (IllegalAccessException illegalaccessexception)
          {
            break;
          }
          break;
        }
        catch (NoSuchMethodException ex)
        {
          System.out.println("NoSuchMethodException");
          break;
        }
      } while (false);
    }
  }

  public boolean isMouseOver(int mouseX, int mouseY)
  {
    if (mouseX >= _x && mouseY >= _y && mouseX <= _x + wid && mouseY <= _y + hgt)
      return true;
    return false;
  }
}
