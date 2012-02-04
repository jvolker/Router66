package rita.support.gui;

import processing.core.*;
import rita.support.PAppletState;

public class RiGUILookAndFeel
{
  public int baseColor;
  public int borderColor;
  public int highlightColor;
  public int selectionColor;
  public int activeColor;
  public int textColor;
  public int lightGrayColor;
  public int darkGrayColor;
  public PAppletState defaultGraphicsState;
  public static final char DEFAULT = '\001';

  public RiGUILookAndFeel(PApplet parent, char type) {
    defaultGraphicsState = new PAppletState();
    if (type == '\001')
    {
      PAppletState temp = new PAppletState(parent);
      parent.colorMode(1, 255.0F);
      baseColor = parent.color(153, 153, 153);
      highlightColor = parent.color(102, 102, 102);
      activeColor = parent.color(255, 153, 51);
      selectionColor = parent.color(230);
      borderColor = parent.color(255);
      textColor = parent.color(0);
      lightGrayColor = parent.color(100);
      darkGrayColor = parent.color(50);
      parent.rectMode(0);
      parent.textAlign(PConstants.LEFT);
      parent.ellipseMode(0);
      parent.strokeWeight(1.0F);
      parent.colorMode(1, 255.0F);
      parent.smooth();
      defaultGraphicsState.saveSettingsForApplet(parent);
      temp.restoreSettingsToApplet(parent);
    }
  }
}
