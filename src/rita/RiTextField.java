package rita;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import processing.core.PApplet;
import rita.support.gui.*;

/*
 * NOTE: This is a placeholder class, borrowed from MyGUI
 * (I think?) until the regular RiTextField can be properly 
 * debugged.
 */

/**
 * A simple text widget to handle user keyboard input<p>
 * To capture the input from a RiTextField, you need to
 * add the <br>method 'void onTextInput(String input)' to your 
 * sketch as follows:
 *   <pre>
     // called when 'enter' is typed in the text-field
     void onRiTaEvent(RiTaEvent re) 
     {
       String input = (String)re.getData();
       
       // do something with the text that was entered
       System.out.println("Text entered: "+input);
       ...
     }</pre>
 *  <p>
 *  Note: if you wish to change the key that triggers when text is captured,
 *  you can call textField.resetEnterCode(int codeForKeyToUseAsEnter);
 */
public class RiTextField extends RiGUIWidget implements TextField
{
  private static final String DEFAULT_FONT_NAME = "Arial";
  protected static final int DEFAULT_HEIGHT = 19;
  protected static int DEFAULT_FONT_SIZE = 12;
  
  protected int currentColor;
  protected String contents = "";
  protected int cursorPos = 0;
  protected int visiblePortionStart = 0;
  protected int visiblePortionEnd = 0;
  protected int startSelect = -1;
  protected int endSelect = -1;
  protected float contentWidth = 0.0F;
  protected float visiblePortionWidth = 0.0F;
  protected float cursorXPos = 0.0F;
  protected float startSelectXPos = 0.0F;
  protected float endSelectXPos = 0.0F;
  protected boolean visible = true;
  protected float textOffsetY = 15; // hack to let user set adjustment
  protected boolean callbackDisabled;
  protected boolean oldCallbackDisabled;
  protected int enterKeyCode = 10; // default=enter-key

  public RiTextField(PApplet pApplet) {
    this(pApplet, (int)(pApplet.width/3f), (int)(pApplet.height/2f), 100, DEFAULT_HEIGHT, DEFAULT_FONT_SIZE, "");    
  }

  public RiTextField(PApplet pApplet, int x, int y) {
    this(pApplet, x, y, 100, DEFAULT_HEIGHT, DEFAULT_FONT_SIZE, "");
  }
  
  public RiTextField(PApplet pApplet, int x, int y, int width) 
  {
    this(pApplet,  x, y, width, DEFAULT_HEIGHT, DEFAULT_FONT_SIZE, "");
  }
  
  public RiTextField(PApplet pApplet, int x, int y, int width, int height) 
  {
    this(pApplet,  x, y, width, height, DEFAULT_FONT_SIZE, "");
  }
  
  public RiTextField(PApplet pApplet, int x, int y, int width,  int height, float fontSize) {
    this(pApplet, x, y, width, height, fontSize, "");
  }

  public RiTextField(PApplet pApplet, int x, int y, int width, int height, float fontSize, String initialText) {
    super(pApplet);
    setPosition(x, y);
    setValue(initialText);
    setSize(width, height);  
    //yTextOffset = getHeight();
    if (font == null) 
      font = pApplet.createFont(DEFAULT_FONT_NAME, fontSize);
    this.fontSize = fontSize;
    pApplet.textFont(font, fontSize);
    // System.out.println("RiGUILookAndFeel.font="+font);
    controller = new RiGUIController(pApplet, fontSize);
    controller.add(this);    
  }
  
  /**
   * Sets the text showing in the text field
   */
  public void setText(String text) {
    setValue(text);
  }

  /**
   * Clears any text in the text field
   */
  public void clear() {
    setValue("");
  }
  
  private synchronized void notifyListener(String s) 
  {
    //System.out.println("RiTextField.onTextInput("+s+")");
    if (!oldCallbackDisabled) {          
      // try the old style
      try {

        RiTa.invoke(_pApplet, "onTextInput", new Class[]{String.class}, new Object[]{s});                                     //     ??????????
      }
      catch (RiTaException e) {
        oldCallbackDisabled = true;
      }
      catch (Throwable e) {
        throwError(e);
      }
    }
    
    if (!callbackDisabled) { 
      // try the new style
      try {        
        RiTa.invoke(_pApplet, "onRiTaEvent", new Class[]{RiTaEvent.class}, new Object[]
           { new RiTaEvent(this, TEXT_ENTERED, s) });        
      }
      catch (RiTaException e) {      
        callbackDisabled = true;
      }
      catch (Throwable e) {
        throwError(e);
      }
    }
    
    if (callbackDisabled && oldCallbackDisabled) {
      System.err.println("\n[WARN] To capture the input from a RiTextField, you"
        +" need to\n       implement the method: 'void onRiTaEvent(RiTaEvent rt)'");
    }
  }

  private void throwError(Throwable e) {
    Throwable cause = e.getCause();
    while (cause != null) {
       e = cause;
       cause = e.getCause();
    }
    System.err.println("[ERROR] RiTextField.notifyListener: '"+e.getMessage()+"'");
    throw new RiTaException(e);
  } 

  protected static boolean validUnicode(char b)
  {
    int c = b;
    if ((c < 32 || c > 126) && (c < 161 || c > 383) && c != 399 && c != 402
        && (c < 416 || c > 417) && (c < 431 || c > 432) && (c < 464 || c > 476)
        && (c < 506 || c > 511) && (c < 536 || c > 539) && (c < 592 || c > 680)
        && (c < 688 || c > 745) && (c < 768 || c > 837) && (c < 884 || c > 885)
        && c != 890 && c != 894 && (c < 900 || c > 906) && (c < 910 || c > 929)
        && (c < 931 || c > 974) && (c < 976 || c > 982) && c < 986 && c < 988
        && c < 990 && c < 992 && (c < 994 || c > 1011)
        && (c < 1025 || c > 1103) && (c < 1105 || c > 1116)
        && (c < 1118 || c > 1158) && (c < 1168 || c > 1220)
        && (c < 1223 || c > 1225) && (c < 1227 || c > 1228)
        && (c < 1232 || c > 1259) && (c < 1262 || c > 1269)
        && (c < 1272 || c > 1273) && (c < 1425 || c > 1441)
        && (c < 1443 || c > 1476) && (c < 1488 || c > 1514)
        && (c < 1520 || c > 1524) && c < 1548 && c < 1563 && c < 1567
        && (c < 1569 || c > 1594) && (c < 1600 || c > 1621)
        && (c < 1632 || c > 1774) && (c < 1776 || c > 1790)
        && (c < 2305 || c > 2361) && (c < 2364 || c > 2381)
        && (c < 2384 || c > 2388) && (c < 2392 || c > 2416)
        && (c < 3585 || c > 3642) && (c < 7808 || c > 7813)
        && (c < 7840 || c > 7929) && (c < 8192 || c > 8238)
        && (c < 8240 || c > 8262) && c != 8304 && (c < 8308 || c > 8334)
        && c != 8337 && (c < 8352 || c > 8364) && (c < 8448 || c > 8504)
        && (c < 8531 || c > 8578) && (c < 8592 || c > 8682)
        && (c < 8592 || c > 8682) && (c < 8192 || c > 8945) && c != 8962
        && (c < 8992 || c > 8993) && (c < 9312 || c > 9321) && c != 9472
        && c != 9474 && c != 9484 && c != 9488 && c != 9492 && c != 9496
        && c != 9500 && c != 9508 && c != 9516 && c != 9524 && c != 9532
        && (c < 9552 || c > 9580) && c != 9600 && c != 9604 && c != 9608
        && c != 9612 && (c < 9616 || c > 9619) && c != 9632
        && (c < 9642 || c > 9644) && c != 9650 && c != 9658 && c != 9660
        && c != 9668 && c != 9670 && (c < 9674 || c > 9676) && c != 9679
        && (c < 9687 || c > 9689) && c != 9702 && c != 9733 && c != 9742
        && c != 9755 && c != 9758 && (c < 9786 || c > 9788) && c != 9792
        && c != 9794 && c != 9824 && c != 9827 && c != 9829 && c != 9830
        && c != 9834 && c != 9835 && (c < 9985 || c > 9993)
        && (c < 9996 || c > 10023) && (c < 10025 || c > 10059) && c != 10061
        && (c < 10063 || c > 10066) && c != 10070 && (c < 10072 || c > 10078)
        && (c < 10081 || c > 10087) && (c < 10102 || c > 10132)
        && (c < 10136 || c > 10174) && (c < 61441 || c > 61442)
        && (c < 61473 || c > 61695) && (c < 62977 || c > 62981)
        && (c < 62992 || c > 62998) && (c < 63488 || c > 63495)
        && (c < 63498 || c > 63499) && (c < 63502 || c > 63505)
        && (c < 63508 || c > 63509) && (c < 63519 || c > 63520)
        && (c < 63519 || c > 63520) && c != 63539)
      return false;
    return true;
  }

  /**
   * @invisible
   */
  public void initWithParent()
  {
    controller.parent.registerMouseEvent(this);
  }

  private void addChar(char c)
  {
    String t1;
    String t2;
    if (startSelect != -1 && endSelect != -1)
    {
      if (startSelect > endSelect)
      {
        int temp = startSelect;
        startSelect = endSelect;
        endSelect = temp;
      }
      if (endSelect > contents.length())
        endSelect = contents.length();
      t1 = contents.substring(0, startSelect);
      t2 = contents.substring(endSelect);
      cursorPos = startSelect;
      startSelect = endSelect = -1;
    } 
    else
    {
      t1 = contents.substring(0, cursorPos);
      t2 = contents.substring(cursorPos);
    }
    contents = t1 + c + t2;
    cursorPos++;
    if (controller.parent.textWidth(contents) < (float) (getWidth() - 12))
    {
      visiblePortionStart = 0;
      visiblePortionEnd = contents.length();
    } 
    else if (cursorPos == contents.length())
    {
      visiblePortionEnd = cursorPos;
      shrinkLeft();
    } else if (cursorPos >= visiblePortionEnd)
      centerCursor();
    else
    {
      visiblePortionEnd = visiblePortionStart;
      growRight();
    }
    fireEventNotification(this, "Modified");
  }

  private void backspaceChar()
  {
    String t1 = "";
    String t2 = "";
    if (startSelect != -1 && endSelect != -1)
    {
      if (startSelect > endSelect)
      {
        int temp = startSelect;
        startSelect = endSelect;
        endSelect = temp;
      }
      if (endSelect > contents.length())
        endSelect = contents.length();
      t1 = contents.substring(0, startSelect);
      t2 = contents.substring(endSelect);
      cursorPos = startSelect;
      startSelect = endSelect = -1;
      contents = t1 + t2;
    } 
    else if (cursorPos > 0)
    {
      if (cursorPos > contents.length())
        cursorPos = contents.length();
      t1 = contents.substring(0, cursorPos - 1);
      t2 = contents.substring(cursorPos);
      cursorPos--;
      contents = t1 + t2;
    }
    if (controller.parent.textWidth(contents) < (float) (getWidth() - 12))
    {
      visiblePortionStart = 0;
      visiblePortionEnd = contents.length();
    } 
    else if (cursorPos == contents.length())
    {
      visiblePortionEnd = cursorPos;
      growLeft();
    } 
    else if (cursorPos <= visiblePortionStart)
      centerCursor();
    else
    {
      visiblePortionEnd = visiblePortionStart;
      growRight();
    }
    fireEventNotification(this, "Modified");
  }

  private void deleteChar()
  {
    if (cursorPos < contents.length())
    {
      cursorPos++;
      backspaceChar();
    }
  }

  private void updateXPos()
  {
    cursorXPos = (controller.parent.textWidth
      (contents.substring(visiblePortionStart, cursorPos)));
    if (startSelect != -1 && endSelect != -1)
    {
      int tempStart;
      int tempEnd;
      if (endSelect < startSelect)
      {
        tempStart = endSelect;
        tempEnd = startSelect;
      } 
      else
      {
        tempStart = startSelect;
        tempEnd = endSelect;
      }
      if (tempStart < visiblePortionStart)
        startSelectXPos = 0.0F;
      else
        startSelectXPos = (controller.parent.textWidth(contents.substring(
            visiblePortionStart, tempStart)));
      if (tempEnd > visiblePortionEnd)
        endSelectXPos = (float) (getWidth() - 4);
      else
        endSelectXPos = (controller.parent.textWidth(contents.substring(
            visiblePortionStart, tempEnd)));
    }
  }

  private void growRight()
  {
    for (/**/; ((controller.parent.textWidth(contents.substring(
        visiblePortionStart, visiblePortionEnd))) < (float) (getWidth() - 12)); visiblePortionEnd++)
    {
      if (visiblePortionEnd == contents.length())
      {
        if (visiblePortionStart == 0)
          break;
        visiblePortionStart--;
      }
    }
  }

  private void growLeft()
  {
    for (/**/; ((controller.parent.textWidth(contents.substring(
        visiblePortionStart, visiblePortionEnd))) < (float) (getWidth() - 12)); visiblePortionStart--)
    {
      if (visiblePortionStart == 0)
      {
        if (visiblePortionEnd == contents.length())
          break;
        visiblePortionEnd++;
      }
    }
  }

  private void shrinkRight()
  {
    for (/**/; ((controller.parent.textWidth(contents.substring(
        visiblePortionStart, visiblePortionEnd))) > (float) (getWidth() - 12)); visiblePortionEnd--)
    {
      /* empty */
    }
  }

  private void shrinkLeft()
  {
    for (/**/; ((controller.parent.textWidth(contents.substring(
        visiblePortionStart, visiblePortionEnd))) > (float) (getWidth() - 12)); visiblePortionStart++)
    {
      /* empty */
    }
  }

  private void centerCursor()
  {
    visiblePortionStart = visiblePortionEnd = cursorPos;
    while ((controller.parent.textWidth(contents.substring(visiblePortionStart,
        visiblePortionEnd))) < (float) (getWidth() - 12))
    {
      if (visiblePortionStart != 0)
        visiblePortionStart--;
      if (visiblePortionEnd != contents.length())
        visiblePortionEnd++;
      if (visiblePortionEnd == contents.length() && visiblePortionStart == 0)
        break;
    }
  }

  private int findClosestGap(int x)
  {
    float prev = 0.0F;
    if (x < 0)
      return visiblePortionStart;
    if (x > getWidth())
      return visiblePortionEnd;
    for (int i = visiblePortionStart; i < visiblePortionEnd; i++)
    {
      float cur = controller.parent.textWidth(contents.substring(
          visiblePortionStart, i));
      if (cur > (float) x)
      {
        if (cur - (float) x < (float) x - prev)
          return i;
        return i - 1;
      }
      prev = cur;
    }
    return contents.length();
  }

  /**
   * @invisible
   */
  public void setValue(String newValue)
  {
    contents = newValue;
    cursorPos = contents.length();
    startSelect = endSelect = -1;
    visiblePortionStart = 0;
    visiblePortionEnd = contents.length();
    if (controller != null && controller.parent.textWidth
      (contents) > (float) (getWidth() - 12)) shrinkRight();
    fireEventNotification(this, "Set");
  }

  /**
   * @invisible
   */
  public String getValue()
  {
    return contents;
  }


  /**
   *  @invisible
   */
  public void mouseEvent(MouseEvent e)
  {
    controller.userState.saveSettingsForApplet(controller.parent);
    lookAndFeel.defaultGraphicsState.restoreSettingsToApplet(controller.parent);
    if (e.getID() == 501)
    {
      if (isMouseOver(e.getX(), e.getY()))
      {
        controller.requestFocus(this);
        wasClicked = true;
        endSelect = -1;
        startSelect = cursorPos = findClosestGap(e.getX() - getX());
      } 
      else if (controller.getFocusStatusForComponent(this))
      {
        wasClicked = false;
        controller.yieldFocus(this);
        startSelect = endSelect = -1;
      }
    } 
    else if (e.getID() == 506)
      endSelect = cursorPos = findClosestGap(e.getX() - getX());
    else if (e.getID() == 502 && endSelect == startSelect)
    {
      startSelect = -1;
      endSelect = -1;
    }
    updateXPos();
    controller.userState.restoreSettingsToApplet(controller.parent);
  }
  
  /**
   * Resets the key that triggers 'enter' in the text field; the default value
   * is 10, for the enter key on most keyboards. A value of -1 will disable this trigger (so that
   * the entered text is never processed).
   */
  public void resetEnterCode(int keyCodeForEnter) {
    this.enterKeyCode = keyCodeForEnter;
  }
  
  /**
   * Returns the key that currently triggers 'enter' in the text field; the default value
   * is 10, for the enter key on most keyboards.
   */
  public int getEnterCode() {
    return this.enterKeyCode;
  }

  /**
   *  @invisible
   */
  public void keyEvent(KeyEvent e)
  {
    controller.userState.saveSettingsForApplet(controller.parent);
    lookAndFeel.defaultGraphicsState.restoreSettingsToApplet(controller.parent);
    int shortcutMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    boolean shiftDown = (e.getModifiersEx() & 0x40) == 64;
    if (e.getID() == 401)
    {
      if (e.getKeyCode() == 40)
      {
        if (shiftDown)
        {
          if (startSelect == -1)
            startSelect = cursorPos;
          endSelect = cursorPos = visiblePortionEnd = contents.length();
        } 
        else
        {
          startSelect = endSelect = -1;
          cursorPos = visiblePortionEnd = contents.length();
        }
        visiblePortionStart = visiblePortionEnd;
        growLeft();
      } 
      else if (e.getKeyCode() == 38)
      {
        if (shiftDown)
        {
          if (endSelect == -1)
            endSelect = cursorPos;
          startSelect = cursorPos = visiblePortionStart = 0;
        } else
        {
          startSelect = endSelect = -1;
          cursorPos = visiblePortionStart = 0;
        }
        visiblePortionEnd = visiblePortionStart;
        growRight();
      } 
      else if (e.getKeyCode() == 37)
      {
        if (shiftDown)
        {
          if (cursorPos > 0)
          {
            if (startSelect != -1 && endSelect != -1)
            {
              startSelect--;
              cursorPos--;
            } else
            {
              endSelect = cursorPos;
              cursorPos--;
              startSelect = cursorPos;
            }
          }
        }
        else if (startSelect != -1 && endSelect != -1)
        {
          cursorPos = Math.min(startSelect, endSelect);
          startSelect = endSelect = -1;
        } 
        else if (cursorPos > 0)
          cursorPos--;
        centerCursor();
      } 
      else if (e.getKeyCode() == 39)
      {
        if (shiftDown)
        {
          if (cursorPos < contents.length())
          {
            if (startSelect != -1 && endSelect != -1)
            {
              endSelect++;
              cursorPos++;
            } else
            {
              startSelect = cursorPos;
              cursorPos++;
              endSelect = cursorPos;
            }
          }
        } 
        else if (startSelect != -1 && endSelect != -1)
        {
          cursorPos = Math.max(startSelect, endSelect);
          startSelect = endSelect = -1;
        } else if (cursorPos < contents.length())
          cursorPos++;
        centerCursor();
      }
      else if (e.getKeyCode() == 127)
        deleteChar();
      else if (e.getKeyCode() == enterKeyCode) {
        notifyListener(contents);
        clear();
      }      
      else if ((e.getModifiers() & shortcutMask) == shortcutMask)
      {
        switch (e.getKeyCode())
        {
          case 67:
            System.out.println("Copy");
            break;
          case 86:
            System.out.println("Paste");
            break;
          case 88:
            System.out.println("Cut");
            break;
          case 65:
            startSelect = 0;
            endSelect = contents.length();
        }
      }
    } 
    else if (e.getID() == 400 && (e.getModifiers() & shortcutMask) != shortcutMask)
    {
      if (e.getKeyChar() == '\010')
        backspaceChar();
      else if (e.getKeyChar() != '\uffff' && validUnicode(e.getKeyChar()))
        addChar(e.getKeyChar());
    }
    updateXPos();
    controller.userState.restoreSettingsToApplet(controller.parent);
  }

  /**
   * @invisible
   */
  public void draw()
  {
    if (!visible) return;
    
    boolean hasFocus = controller.getFocusStatusForComponent(this);
    
    if (wasClicked)
      currentColor = lookAndFeel.activeColor;    
    else if (isMouseOver(controller.parent.mouseX, controller.parent.mouseY) || hasFocus)
      currentColor = lookAndFeel.highlightColor;
    else
      currentColor = lookAndFeel.baseColor;
    
    // draw the rectangle for the textfield   
    controller.parent.stroke(lookAndFeel.highlightColor);
    controller.parent.fill(lookAndFeel.borderColor);    
    controller.parent.rect((float) getX(), (float) getY(), (float) getWidth(), (float) getHeight());
    controller.parent.noStroke();
    
    float offset = 8.0F; // get the offset
    if (cursorPos == contents.length() 
      && controller.parent.textWidth(contents) > (float) (getWidth() - 8))
    {
      offset = ((float) (getWidth() - 4) - (controller.parent.textWidth
        (contents.substring(visiblePortionStart, visiblePortionEnd))));
    }
    
    // draw the selected rectangle if there is one
    if (hasFocus && startSelect != -1 && endSelect != -1)
    {
      controller.parent.fill(lookAndFeel.selectionColor);
      controller.parent.rect((float) getX() + startSelectXPos + offset,
          (float) (getY() + 3), endSelectXPos - startSelectXPos + 1.0F, getHeight()-4);
    }
        
    // draw the text if there is any
    String txt  = contents.substring(visiblePortionStart, visiblePortionEnd);
    if (txt.length() > 0) 
    {                
      controller.parent.textFont(font, fontSize);
      controller.parent.fill(lookAndFeel.textColor);      
      controller.parent.text(txt, (float) getX() + offset, (float) (getY() + textOffsetY));
    }      
    
    // draw the cursor if we have focus
    if (hasFocus && (startSelect == -1 || endSelect == -1)
        && controller.parent.millis() % 1000 > 500)
    {
      controller.parent.stroke(lookAndFeel.darkGrayColor);
      controller.parent.line(((float) (getX() + (int) cursorXPos) + offset),
          (float) (getY()+2), ((float) (getX() + (int) cursorXPos) + offset),
          (float) (getY() +getHeight()-2));
    }
  }

  /**
   * @invisible
   */
  public void actionPerformed(RiGUIEvent e)
  {
    super.actionPerformed(e);
    if (e.getSource() == this)
    {
      if (e.getMessage().equals("Received Focus"))
      {
        if (contents != "")
        {
          startSelect = 0;
          endSelect = contents.length();
        }
      } else if (e.getMessage().equals("Lost Focus") && contents != "")
        startSelect = endSelect = -1;
    }
  }

  public void setActiveColor(float r, float g, float b, float alpha){
    lookAndFeel.activeColor = _pApplet.color(r,g,b,alpha);
  }
  public void setActiveColor(float gray){
    setActiveColor(gray,gray,gray,255);
  }
  public void setActiveColor(float gray, float alpha){
    setActiveColor(gray,gray,gray,alpha);
  }
  public void setActiveColor(float r, float g, float b){
    setActiveColor(r,g,b,255);
  }

  public void setBgColor(float r, float g, float b, float alpha){
    lookAndFeel.borderColor = _pApplet.color(r,g,b,alpha);
  }
  public void setBgColor(float gray, float alpha) {
    setBgColor(gray,gray,gray,alpha);
  }
  public void setBgColor(float r, float g, float b) {
    setBgColor(r,g,b, 255);
  }
  public void setBgColor(float gray){
    setBgColor(gray, gray, gray, 255);
  }   
  
  public void setColor(float r, float g, float b, float a){
    lookAndFeel.baseColor = _pApplet.color(r,g,b,a);
  }
    public void setColor(float gray){ 
    setColor(gray,gray,gray,255);
  }
  public void setColor(float gray, float alpha){
    setColor(gray,gray,gray,alpha);
  }
  public void setColor(float r, float g, float b){
    setColor(r,g,b,255);
  }  
  
  public void setFgColor(float r, float g, float b, float alpha) {
    lookAndFeel.textColor = _pApplet.color(r,g,b,alpha);    
  }
  public void setFgColor(float gray) {
    setFgColor(gray,gray,gray,255);
  }
  public void setFgColor(float gray, float alpha) {
    setFgColor(gray,gray,gray,alpha);
  }
  public void setFgColor(float r, float g, float b) {
    setFgColor(r,g,b,255);
  }

  /** Sets whether the text-field should be drawn (default=true)  */
  public void setVisible(boolean visible) {
    this.visible  = visible;
  }
  
  /** Returns true if the object is visible  */
  public boolean isVisible() { return visible;  }
  
  /** Returns the current Y-offset for typed text  */
  public float getTextOffsetY() {
    return textOffsetY;
  }

  /** Sets the Y-offset for typed text  */
  public void setTextOffsetY(float textOffset) {
    textOffsetY = textOffset;
  }
  
}// end
