package rita.support;

import processing.core.PApplet;
import processing.core.PFont;

public class PAppletState
{
    public boolean tint, fill, stroke, smooth;
    public PFont textFont;
    
    public int rectMode;
    public int ellipseMode;    
    public int textAlign;    
    public int textMode;    
    public int tintColor;
    public int fillColor;
    public int strokeColor;    
    public int cMode;
    
    public float textSize;
    public float strokeWeight;
    public float cModeX;
    public float cModeY;
    public float cModeZ;
    public float cModeA;
    
    public PAppletState() {
	/* empty */
    }
    
    public PAppletState(PApplet applet) {
	saveSettingsForApplet(applet);
    }
    
    public void saveSettingsForApplet(PApplet applet) {
	smooth = applet.g.smooth;
	rectMode = applet.g.rectMode;
	ellipseMode = applet.g.ellipseMode;
	textFont = applet.g.textFont;
	textAlign = applet.g.textAlign;
	textSize = applet.g.textSize;
	textMode = applet.g.textMode;
	tint = applet.g.tint;
	fill = applet.g.fill;
	stroke = applet.g.stroke;
	tintColor = applet.g.tintColor;
	fillColor = applet.g.fillColor;
	strokeColor = applet.g.strokeColor;
	strokeWeight = applet.g.strokeWeight;
	cMode = applet.g.colorMode;
	cModeX = applet.g.colorModeX;
	cModeY = applet.g.colorModeY;
	cModeZ = applet.g.colorModeZ;
	cModeA = applet.g.colorModeA;
    }
    
    public void restoreSettingsToApplet(PApplet applet) {
	if (smooth)
	    applet.smooth();
	else
	    applet.noSmooth();
	applet.rectMode(rectMode);
	applet.ellipseMode(ellipseMode);
	if (textFont != null) {
	    applet.textFont(textFont);
	    applet.textSize(textSize);
	}
	applet.textAlign(textAlign);
	applet.textMode(textMode);
	if (tint)
	    applet.tint(tintColor);
	else
	    applet.noTint();
	if (fill)
	    applet.fill(fillColor);
	else
	    applet.noFill();
	if (stroke)
	    applet.stroke(strokeColor);
	else
	    applet.noStroke();
	applet.strokeWeight(strokeWeight);
	applet.colorMode(cMode, cModeX, cModeY, cModeZ, cModeA);
    }
}
