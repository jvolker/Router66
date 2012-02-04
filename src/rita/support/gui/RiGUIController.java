package rita.support.gui;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;

import processing.core.PApplet;
import rita.support.PAppletState;

public class RiGUIController implements ClipboardOwner
{
  private static final String RITA_GUI_CLIPBOARD = "RiTaClipboard";

  public static float defaultFontSize = 12;
  
  private RiGUIWidget[] contents;
  public boolean showBounds,visible;
  private int numItems, focusIndex = -1;  
  private RiGUILookAndFeel lookAndFeel;
  public PAppletState userState;
  private Clipboard clipboard;
  public PApplet parent;
  
  public RiGUIController(PApplet p) {
    this(p, defaultFontSize , true);
  }

  public RiGUIController(PApplet p, float fontSize) {
    this(p, fontSize, true);
  }

  public RiGUIController(PApplet p, float fontSize, boolean newVisible) {    
    setParent(p);
    setVisible(newVisible);
    contents = new RiGUIWidget[5];
    //RiGUILookAndFeel.fontSize = fontSize;
    lookAndFeel = new RiGUILookAndFeel(parent, '\001');
    userState = new PAppletState();
    SecurityManager security = System.getSecurityManager();
    if (security != null)
    {
      try
      {
        security.checkSystemClipboardAccess();
        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      }
      catch (SecurityException e) {
        clipboard = new Clipboard(RITA_GUI_CLIPBOARD);
      }
    } 
    else {
      try
      {
        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      }
      catch (Exception exception){
        /* empty */
      }
    }
    parent.registerKeyEvent(this);
    parent.registerDraw(this);
  }

  public void setLookAndFeel(RiGUILookAndFeel lf)
  {
    lookAndFeel = lf;
  }

  public RiGUILookAndFeel getLookAndFeel()
  {
    return lookAndFeel;
  }

  public void add(RiGUIWidget component)
  {
    if (numItems == contents.length)
    {
      RiGUIWidget[] temp = contents;
      contents = new RiGUIWidget[contents.length * 2];
      System.arraycopy(temp, 0, contents, 0, numItems);
    }
    component.setController(this);
    component.setLookAndFeel(lookAndFeel);
    contents[numItems++] = component;
    component.initWithParent();
  }

  public void remove(RiGUIWidget component)
  {
    int componentIndex = -1;
    for (int i = 0; i < numItems; i++)
    {
      if (component == contents[i])
      {
        componentIndex = i;
        break;
      }
    }
    if (componentIndex != -1)
    {
      contents[componentIndex] = null;
      if (componentIndex < numItems - 1)
        System.arraycopy(contents, componentIndex + 1, contents,
            componentIndex, numItems);
      numItems--;
    }
  }

  public void setParent(PApplet argParent)
  {
    parent = argParent;
  }

  public PApplet getParent()
  {
    return parent;
  }

  public void setVisible(boolean newVisible)
  {
    visible = newVisible;
  }

  public boolean getVisible()
  {
    return visible;
  }

  public void requestFocus(RiGUIWidget c)
  {
    for (int i = 0; i < numItems; i++)
    {
      if (c == contents[i])
        focusIndex = i;
    }
  }

  public void yieldFocus(RiGUIWidget c)
  {
    if (focusIndex > -1 && focusIndex < numItems && contents[focusIndex] == c)
      focusIndex = -1;
  }

  public RiGUIWidget getComponentWithFocus()
  {
    return contents[focusIndex];
  }

  public boolean getFocusStatusForComponent(RiGUIWidget c)
  {
    if (focusIndex >= 0 && focusIndex < numItems)
    {
      if (c == contents[focusIndex])
        return true;
      return false;
    }
    return false;
  }

  public void lostOwnership(Clipboard parClipboard, Transferable parTransferable)
  {
    System.out.println("Lost ownership");
  }

  public void copy(String v)
  {
    StringSelection fieldContent = new StringSelection(v);
    clipboard.setContents(fieldContent, this);
  }

  public String paste()
  {
    Transferable clipboardContent = clipboard.getContents(this);
    do
    {
      if (clipboardContent != null
          && clipboardContent.isDataFlavorSupported(DataFlavor.stringFlavor))
      {
        String string;
        try
        {
          String tempString = ((String) clipboardContent
              .getTransferData(DataFlavor.stringFlavor));
          string = tempString;
        }
        catch (Exception e)
        {
          e.printStackTrace();
          break;
        }
        return string;
      }
    } while (false);
    return "";
  }

  public void keyEvent(KeyEvent e)
  {
    if (visible)
    {
      if (e.getID() == 401 && e.getKeyCode() == 9)
      {
        if (focusIndex != -1 && contents[focusIndex] != null)
          contents[focusIndex].actionPerformed(new RiGUIEvent(
              contents[focusIndex], "Lost Focus"));
        if ((e.getModifiersEx() & 0x40) == 64)
          giveFocusToPreviousComponent();
        else
          giveFocusToNextComponent();
        if (focusIndex != -1 && contents[focusIndex] != null)
          contents[focusIndex].actionPerformed(new RiGUIEvent(
              contents[focusIndex], "Received Focus"));
      } else if (e.getKeyCode() != 9 && focusIndex >= 0
          && focusIndex < contents.length)
        contents[focusIndex].keyEvent(e);
    }
  }

  private void giveFocusToPreviousComponent()
  {
    int oldFocus = focusIndex;
    for (focusIndex = (focusIndex - 1) % numItems; !contents[focusIndex]
        .canReceiveFocus()
        && focusIndex != oldFocus; focusIndex = (focusIndex - 1) % numItems)
    {
      /* empty */
    }
  }

  private void giveFocusToNextComponent()
  {
    int oldFocus = focusIndex;
    for (focusIndex = (focusIndex + 1) % numItems; !contents[focusIndex]
        .canReceiveFocus()
        && focusIndex != oldFocus; focusIndex = (focusIndex + 1) % numItems)
    {
      /* empty */
    }
  }

  public void draw()
  {
    if (visible)
    {
      userState.saveSettingsForApplet(parent);
      lookAndFeel.defaultGraphicsState.restoreSettingsToApplet(parent);
      for (int i = 0; i < contents.length; i++)
      {
        if (contents[i] != null)
          contents[i].draw();
      }
      userState.restoreSettingsToApplet(parent);
    }
  }
}
