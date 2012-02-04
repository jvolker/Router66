package rita;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

import processing.core.*;
import rita.support.Regex;
import rita.support.RiCharSequence;
import rita.support.behavior.*;

/*
 * TODO:
 *   Remove selected, draggable, etc.
 *   should-return RiTextBehavior's instead of Ids
 */

/**
 * RiTa's text display object. Wraps an instance of RiString to provide utility
 * methods for typography, display, animation, text-to-speech, and audio
 * playback.
 * <p>
 * 
 * Note: by default, RiText objects, once created, will draw themselves to the
 * screen at each frame until they are either a) deleted (via
 * RiText.delete(myRiText);) or b) hidden (via myRiText.setVisible(false);).
 * 
 * To disable this behavior (and draw the objects manually), one can call
 * disableAutoDraw().
 * <p>
 * An example usage:
 * 
 * <pre>
 *     import rita.*;
 *     
 *     void setup() {
 *       // start in the upper left corner
 *       RiText rt = new RiText(this, "hello", 0, 10);
 *       
 *       // measure the width of the text
 *       float tw = rt.textWidth();
 * 
 *       // move to the lower right over 2 sec
 *       rt.moveTo(width-tw, height, 2);    
 *     }
 *     
 *     void draw() {
 *       // draw the bg every frame
 *       background(255);
 *     }
 * </pre>
 */
public class RiText extends RiObject implements RiCharSequence
{
  protected static final String ANY_CHAR_REGEX = ".*?";
  // Consts ============================================================

  protected static final String QQ = "", SPC = " ";// PARAGRAPH="<p>";
  protected static final float DEFAULT_BB_PADDING = 1;
  protected static int DEFAULT_LINE_BREAK_WIDTH = 70, DEFAULT_ALIGN = LEFT;
  protected static float DEFAULT_RFILL = 0, DEFAULT_GFILL = 0, DEFAULT_BFILL = 0;
  protected static float DEFAULT_ALPHA = 255, DEFAULT_BB_STROKE_WEIGHT = 1;
  protected static float[] DEFAULT_BB_STROKE = { 0, 0, 0, 255 };
  protected static float[] DEFAULT_BB_FILL = { 0, 0, 0, 0 };
  protected static boolean DEFAULT_MOUSE_DRAGGABLE, AUTODRAW = true;
  protected static boolean DEFAULT_SHOW_BOUNDING_BOXES;
  protected static float DEFAULT_FONT_SIZE_CONST = 14;
  protected static final String DEFAULT_FONT_CONST = "arial";

  // flag for default font creation: load(false) or create(true)
  //protected static boolean CREATE_DEFAULT_FONT = true;

  // Statics ============================================================

  /** @invisible */
  public static String DEFAULT_FONT;
  
  /** @invisible */
  protected static float DEFAULT_FONT_SIZE;

  /** @invisible */
  public static int DEFAULT_MOTION_TYPE;

  /** @invisible */
  public static boolean DBUG_INFO = false;

  protected static List instances = new ArrayList();
  protected static boolean behaviorWarningsDisabled, callbacksDisabled;
  protected static Boolean rendering3D;
  protected static Map fontCache;

  static
  {
    initDefaults();
  }

  // Members ============================================================

  /**
   * Current Sample object for this text
   */
  public RiSample sample;

  /** @invisible */
  public int motionType;

  /* Current Feature String for this text */
  protected RiString text;

  // scale/rotate variables

  /** @invisible */
  public float scaleX = 1, scaleY = 1, scaleZ = 1;

  /** @invisible */
  public float rotateX, rotateY, rotateZ;

  /**
   * Current x-position of this text
   */
  public float x;

  /**
   * Current y-position of this text
   */
  public float y;

  /**
   * Current z-position of this text
   * 
   * @invisible
   */
  public float z;

  /** Font for this RiText */
  protected PFont font;

  protected List behaviors;
  protected boolean hidden;

  /** color variables */
  protected float fillR = DEFAULT_RFILL, fillG = DEFAULT_GFILL;
  protected float fillB = DEFAULT_BFILL, fillA = DEFAULT_ALPHA;
  protected float bbFillR = 0, bbFillG = 0, bbFillB = 0, bbFillA = 0;
  protected float bbStrokeR = 0, bbStrokeG = 0, bbStrokeB = 0, bbStrokeA = 255;
  protected float bbsStrokeR = 0, bbsStrokeG = 0, bbsStrokeB = 0, bbsStrokeA = 255;
  protected float fontSize, bbStrokeWeight, imageWidth, imageHeight, bbPadding;
  protected boolean boundingBoxVisible, mouseDraggable; // behavior?
  protected Rectangle2D boundingBox, screenBoundingBox, imageRect;
  protected float mouseXOff, mouseYOff, imageXOff, imageYOff;
  protected RiText fadeToTextCopy;
  protected int textAlignment;

  // specific to this instance
  protected boolean autodraw = false;

  // Constructors ============================================================

  public RiText(PApplet pApplet)
  {
    this(pApplet, QQ);
  }

  public RiText(PApplet pApplet, String text)
  {
    this(pApplet, text, Float.MIN_VALUE, Float.MIN_VALUE);
  }

  public RiText(PApplet pApplet, char character)
  {
    this(pApplet, Character.toString(character), Float.MIN_VALUE, Float.MIN_VALUE, DEFAULT_ALIGN);
  }

  public RiText(PApplet pApplet, float startXPos, float startYPos)
  {
    this(pApplet, QQ, startXPos, startYPos, DEFAULT_ALIGN);
  }

  public RiText(PApplet pApplet, char character, float startXPos, float startYPos)
  {
    this(pApplet, Character.toString(character), startXPos, startYPos, DEFAULT_ALIGN);
  }

  public RiText(PApplet pApplet, String text, float startXPos, float startYPos)
  {
    this(pApplet, text, startXPos, startYPos, DEFAULT_ALIGN);
  }

  public RiText(PApplet pApplet, String text, float startXPos, float startYPos, PFont font)
  {
    this(pApplet, text, startXPos, startYPos, DEFAULT_ALIGN, font);
  }

  public RiText(PApplet pApplet, String text, float xPos, float yPos, int alignment)
  {
    this(pApplet, text, xPos, yPos, alignment, null);
  }

  /**
   * Creates a new RiText object base-aligned at x='xPos', y='yPos', with
   * 'alignment' from one of (LEFT, CENTER, RIGHT), using font specified by
   * 'theFont'.
   */
  public RiText(PApplet pApplet, String text, float xPos, float yPos, int alignment, PFont theFont)
  {
    super(pApplet);
    if (theFont != null)
      this.font = theFont;
    this.setDefaults();
    this.setText(text);
    this.registerInstance(pApplet);
    this.textMode(alignment);
    this.verifyFont();
    this.x = xPos == Float.MIN_VALUE ? getCenterX() : xPos;
    this.y = yPos == Float.MIN_VALUE ? getCenterY() : yPos;
  }

  private void setDefaults()
  {
    this.boundingBoxVisible = DEFAULT_SHOW_BOUNDING_BOXES;
    this.bbStrokeWeight = DEFAULT_BB_STROKE_WEIGHT;
    this.bbPadding = DEFAULT_BB_PADDING;
    this.mouseDraggable = DEFAULT_MOUSE_DRAGGABLE;
    this.motionType = DEFAULT_MOTION_TYPE;
    this.boundingBoxFill(DEFAULT_BB_FILL);
    this.boundingBoxStroke(DEFAULT_BB_STROKE);
    this.fontSize = DEFAULT_FONT_SIZE;
  }

  /**
   * Creates and returns a new behavior that interpolates between 2 values,
   * 'start' and 'target', and generates a callback to
   * pApplet.onRiTaEvent(RiTaEvent) when finished.
   * 
   * @param pApplet
   *          The parent applet to call back to
   * @param start
   *          the start value
   * @param target
   *          the target value
   * @param startOffsetSec
   *          Time before the timer starts (in seconds)
   * @param durationSec
   *          The timer's period (in seconds)
   * 
   * @see RiLerpBehavior#getValue()
   * 
   * @invisible
   */
  public RiLerpBehavior createLerp(PApplet pApplet, float start, float target, float startOffsetSec, float durationSec)
  {
    // System.out.println("RiText.createLerp("+start+","+target+","+startOffsetSec+","+durationSec+")");
    return (RiLerpBehavior) addBehavior(new RiLerpBehavior(this, start, target, startOffsetSec, durationSec));
  }

  /**
   * @invisible
   */
  public RiLerpBehavior createLerp(PApplet pApplet, float start, float target, float durationSec)
  {
    return createLerp(pApplet, start, target, 0, durationSec);
  }

  /**
   * @invisible
   */
  public RiLerpBehavior createLerp(PApplet pApplet)
  {
    return createLerp(pApplet, 0, 0, 0, 0);
  }

  static boolean msgNullPAppletRegisterInstance;

  private void registerInstance(PApplet pApplet)
  {
    instances.add(this);
    if (pApplet == null)
    {
      if (!msgNullPAppletRegisterInstance)
      {
        System.err.println("[WARN] Null PApplet passed to RiText.registerInstance(PApplet)");
        msgNullPAppletRegisterInstance = true;
      }
      return;
    }
    if (AUTODRAW)
      pApplet.registerDraw(this);
    this.registerDispose();
    pApplet.registerMouseEvent(this);
    pApplet.smooth(); // for clean fonts
  }

  static boolean msgNullPAppletVerifyFont;

  private void verifyFont()
  {
    if (_pApplet == null)
    {
      if (!msgNullPAppletVerifyFont)
      {
        System.err.println("[WARN] Null PApplet passed to RiText.verifyFont(PApplet)."
            + "\nMake sure you pass a valid (non-null) instance of PApplet to RiTa");
        msgNullPAppletVerifyFont = true;
      }
      return;
    }

    if (this.font == null) {
      this.font = _defaultFont(_pApplet);
    }

    _pApplet.textFont(font);
    if (this.fontSize > 0)
      _pApplet.textSize(fontSize);
  }

  /**
   * Returns the point representing the center of the RiText
   * 
   * @invisible
   */
  public Point2D getCenter()
  {
    return new Point2D.Float(x + textWidth() / 2f, y - textHeight() / 2f);
  }

  /**
   * Returns the current default font.
   * <p>
   * Note: This method may create one or more objects so should be used
   * sparingly.
   * 
   * @invisible
   */
  public static PFont getDefaultFont(PApplet pApplet)
  {
    RiText tmp = new RiText(pApplet);
    PFont pf = tmp.getFont();
    delete(tmp);
    return pf;
  }

  private static PFont _defaultFont(PApplet p)
  {
    //System.out.println("_defaultFont("+CREATE_DEFAULT_FONT+")");
    
    PFont pf = checkFontCache(DEFAULT_FONT, DEFAULT_FONT_SIZE);
    
    if (pf == null)
    {
      if (DEFAULT_FONT == null)
        pf = _createFont(p, DEFAULT_FONT_CONST, DEFAULT_FONT_SIZE_CONST);
      else if (DEFAULT_FONT_SIZE > 0)
        pf = _createFont(p, DEFAULT_FONT, DEFAULT_FONT_SIZE);
      else 
        pf = _loadFont(p, DEFAULT_FONT, -1);
      
      if (pf == null)
        
      {
        String msg = "Unable to load/create font " + "with name='" + DEFAULT_FONT + "'";
        if (DEFAULT_FONT_SIZE > -1)
          msg += " and size=" + DEFAULT_FONT_SIZE;
        throw new RiTaException(msg);
      }  
    }
    return pf;
  }

  private float getCenterX()
  {
    return (_pApplet != null) ? getCenterX(_pApplet.g) : -1;
  }

  private float getCenterX(PGraphics p)
  {
    if (p == null)
      return 0;
    float cx = p.width / 2;
    if (textAlignment == LEFT)
      cx -= (textWidth() / 2f);
    else if (textAlignment == RIGHT)
      cx += (textWidth() / 2);
    return cx;
  }

  private float getCenterY()
  {
    return (_pApplet != null) ? _pApplet.height / 2 : -1;
  }

  /**
   * Returns the bounding box stroke weight
   * 
   * @invisible
   */
  public float boundingBoxStrokeWeight()
  {
    return bbStrokeWeight;
  }

  /**
   * Returns the bounding box padding
   * 
   * @invisible
   */
  public float boundingBoxPadding()
  {
    return bbPadding;
  }

  // ------------------------- Colors -----------------------------
  /**
   * Set the text fill for this object (same as setColor())
   * 
   * @param r
   *          red component (0-255)
   * @param g
   *          green component (0-255)
   * @param b
   *          blue component (0-255)
   * @param alpha
   *          transparency (0-255)
   */
  public void fill(float r, float g, float b, float alpha)
  {
    this.setColor(r, g, b, alpha);
  }

  public void fill(float gray)
  {
    this.setColor(gray, gray, gray, 255);
  }

  public void fill(float gray, float alpha)
  {
    this.setColor(gray, gray, gray, alpha);
  }

  public void fill(float r, float g, float b)
  {
    this.setColor(r, g, b, 255);
  }

  public void fill(float[] color)
  {
    this.setColor(color);
  }

  /**
   * Sets the text fill color according to a single hex number.
   * 
   * @invisible
   */
  public void fillHex(int hexColor)
  {
    this.setColor(RiTa.unhex(hexColor));
  }

  /**
   * Set the text color for this object
   * 
   * @param r
   *          red component (0-255)
   * @param g
   *          green component (0-255)
   * @param b
   *          blue component (0-255)
   * @param alpha
   *          transparency (0-255)
   * @invisible
   */
  private void setColor(float r, float g, float b, float alpha)
  {
    this.fillR = r;
    this.fillG = g;
    this.fillB = b;
    this.fillA = alpha;
  }

  /**
   * Set the text color for this object
   * 
   * @param r
   *          red component (0-255)
   * @param g
   *          green component (0-255)
   * @param b
   *          blue component (0-255)
   * @param alpha
   *          transparency (0-255)
   * @invisible
   */
  public void setColor(float[] color)
  {
    float r = color[0], g = 0, b = 0, a = fillA;
    switch (color.length)
    {
      case 4:
        g = color[1];
        b = color[2];
        a = color[3];
        break;
      case 3:
        g = color[1];
        b = color[2];
        break;
      case 2:
        g = color[0];
        b = color[0];
        a = color[1];
        break;
    }
    this.setColor(r, g, b, a);
  }

  /**
   * Set the bounding-box (or background) color for this object
   * 
   * @param r
   *          red component (0-255)
   * @param g
   *          green component (0-255)
   * @param b
   *          blue component (0-255)
   * @param alpha
   *          transparency (0-255)
   * @invisible
   */
  public void boundingBoxFill(float r, float g, float b, float alpha)
  {
    this.bbFillR = r;
    this.bbFillG = g;
    this.bbFillB = b;
    this.bbFillA = alpha;
  }

  /**
   * Set the current boundingBoxFill color for this object, applicable only when
   * <code>showBoundingBox(true)</code> has been called.
   * 
   * @invisible
   */
  public void boundingBoxFill(float[] color)
  {
    bbFillR = color[0];
    bbFillG = color[1];
    bbFillB = color[2];
    bbFillA = 255;
    if (color.length > 3)
      this.bbFillA = color[3];
  }

  public void boundingBoxFill(float gray)
  {
    this.boundingBoxFill(gray, gray, gray, 255);
  }

  public void boundingBoxFill(float gray, float alpha)
  {
    this.boundingBoxFill(gray, gray, gray, alpha);
  }

  public void boundingBoxFill(float r, float g, float b)
  {
    this.boundingBoxFill(r, g, b, 255);
  }

  /**
   * Set the stroke color for the bounding-box of this object, assuming it has
   * been set to visible.
   * 
   * @param r
   *          red component (0-255)
   * @param g
   *          green component (0-255)
   * @param b
   *          blue component (0-255)
   * @param alpha
   *          transparency (0-255)
   * @see #showBoundingBox(boolean)
   * @see #showBoundingBoxes(boolean)
   * @invisible
   */
  public void boundingBoxStroke(float r, float g, float b, float alpha)
  {
    this.bbStrokeR = r;
    this.bbStrokeG = g;
    this.bbStrokeB = b;
    this.bbStrokeA = alpha;
  }

  public void boundingBoxStroke(float gray)
  {
    this.boundingBoxStroke(gray, gray, gray, 255);
  }

  public void boundingBoxStroke(float gray, float alpha)
  {
    this.boundingBoxStroke(gray, gray, gray, alpha);
  }

  public void boundingBoxStroke(float r, float g, float b)
  {
    this.boundingBoxStroke(r, g, b, 255);
  }

  /**
   * Returns the current text color for this object
   * 
   * @invisible
   */
  public float[] getColor()
  { // yuck
    return new float[] { fillR, fillG, fillB, fillA };
  }

  /**
   * Returns the current bounding box fill color for this object
   * 
   * @invisible
   */
  public float[] getBoundingBoxFill()
  { // yuck
    return new float[] { bbFillR, bbFillG, bbFillB, bbFillA };
  }

  /**
   * Returns the current bounding box stroke color for this object
   * 
   * @invisible
   */
  public float[] getBoundingBoxStroke()
  { // yuck
    return new float[] { bbStrokeR, bbStrokeG, bbStrokeB, bbStrokeA };
  }

  /**
   * Set the current boundingBoxStroke color for this object, applicable only
   * when <code>showBoundingBox(true)</code> has been called.
   * 
   * @invisible
   */
  public void boundingBoxStroke(float[] color)
  {
    bbStrokeR = color[0];
    bbStrokeG = color[1];
    bbStrokeB = color[2];
    bbStrokeA = 255;
    if (color.length == 4)
      this.bbStrokeA = color[3];
  }

  /**
   * Set the current alpha trasnparency for this object (0-255))
   * 
   * @param alpha
   */
  public void setAlpha(float alpha)
  {
    this.fillA = alpha;
  }

  /**
   * Returns the fill alpha value (transparency)
   * 
   * @invisible
   */
  public float getAlpha()
  {
    return fillA;
  }

  // -------------------- end colors ----------------------

  /**
   * Checks if the input point is inside the bounding box
   */
  public boolean contains(float mx, float my)
  {
    // System.out.println("Testing: ("+mx+","+my+") vs ("+x1+","+y1+")");
    this.updateBoundingBox(_pApplet.g);
    return (boundingBox.contains(mx - x, my - y));
  }

  /**
   * Checks if the input point is inside the bounding box public boolean
   * imageContains(float mx, float my) { if (this.image == null) return false;
   * 
   * if (imageRect == null) imageRect = new
   * Rectangle2D.Float((int)(x+imageXOff), (int)(y+imageYOff), (int)imageWidth,
   * (int)imageHeight); else imageRect.setRect(x+imageXOff, y+imageYOff,
   * imageWidth, imageHeight);
   * 
   * return imageRect.contains(x, y); }
   */

  /**
   * Checks if the input point is inside the object's bounding box after
   * converting its x,y,z coordinates to screen x & y.
   * 
   * @see PApplet#screenX(float, float, float)
   * @see PApplet#screenY(float, float, float)
   * @invisible
   */
  public boolean contains3D(float mx, float my)
  {
    this.updateBoundingBox(_pApplet.g);
    float x3d = _pApplet.screenX(x, y, z);
    float y3d = _pApplet.screenY(x, y, z);
    return (boundingBox.contains(x3d - x, y3d - y));
  }

  /**
   * Draw the RiText object at current x,y,color,font,alignment, etc.
   * <p>
   * NOTE: called automatically unless disabled
   * 
   * @see RiText#disableAutoDraw()
   * @invisible
   */
  public void draw()
  {
    this.update();
    this.render();
  }

  /**
   * Draw the RiText object at current x,y,color,font,alignment, etc. on the
   * specified PGraphics object
   * <p>
   * 
   * @see #draw()
   * @invisible
   */
  public void draw(PGraphics p)
  {
    if (p == null) {
      draw();
    }
    else {
      this.update(p);
      this.render(p);
    }
  }

  /**
   * Override in subclasses to do custom rendering
   * <p>
   * Note: It is preferable to override this method rather than the draw()
   * method in subclasses to ensure proper maintenance of contained objects.
   * 
   * @invisible
   */
  public void render()
  {
    render(_pApplet.g);
  }

  /**
   * Override in subclasses to do custom rendering
   * <p>
   * Note: It is preferable to override this method rather than the draw()
   * method in subclasses to ensure proper maintenance of contained objects.
   * 
   * @invisible
   */
  public void render(PGraphics p)
  {
    if (this.hidden)
      return;

    if (text == null || text.length() == 0)
      return;

    // translate & draw at 0,0
    p.pushMatrix(); // --------------

    doAffineTransforms(p);

    if (boundingBoxVisible)
      this.drawBoundingBox(p);

    p.fill(fillR, fillG, fillB, fillA);

    if (font != null)
      p.textFont(font);

    p.textAlign(textAlignment);

    if (this.fontSize > 0)
      p.textSize(fontSize);

    if (text != null)
    {
      if (is3D(p))
        p.text(text.toString(), 0, 0, 0);
      else
        p.text(text.toString(), 0, 0);
    }

    p.popMatrix(); // --------------
  }

  private void doAffineTransforms(PGraphics p)
  {
    if (is3D(p))
    {
      // p.translate((int) x, (int) y, (int) z); 
      p.scale(scaleX, scaleY, scaleZ);
      p.rotateX(rotateX);
      p.rotateY(rotateY);
      p.rotateZ(rotateZ);
      p.translate((int) x, (int) y, (int) z); // why is this here??
    }
    else
    {
      p.translate((int) x, (int) y);
      p.scale(scaleX, scaleY);
      p.rotate(rotateZ);
    }
  }

  /**
   * Returns true if we are rendering in 3D, else false
   * 
   * @invisible
   */
  static boolean is3D(PGraphics p)
  {
    if (p instanceof PGraphicsJava2D) // for PDF renderer
      return false;
    
    if (rendering3D == null)
    {
      if (p == null)
        rendering3D = Boolean.FALSE;
      else
        rendering3D = new Boolean(!(p instanceof PGraphics2D || p instanceof PGraphicsJava2D));
    }
    return rendering3D.booleanValue();
  }

  protected void drawBoundingBox(PGraphics p)
  {
    if (bbFillA <= 0) // ?
      p.noFill();
    else
      p.fill(bbFillR, bbFillG, bbFillB, bbFillA);
    p.stroke(bbStrokeR, bbStrokeG, bbStrokeB, bbStrokeA);
    if (bbStrokeWeight > 0)
      p.strokeWeight(bbStrokeWeight);
    else
      p.noStroke();
    p.rectMode(CORNER);
    p.rect((float) boundingBox.getX(), (float) boundingBox.getY(), (float) boundingBox.getWidth(), (float) boundingBox.getHeight());
  }

  /**
   * Disposes of any resources associated with this RiText and removes it from
   * the draw() queue.
   */
  protected void _dispose()
  {
    setVisible(false);

    if (text != null)
      RiString.delete(text);

    if (sample != null)
    {
      sample.delete();
    }

    if (behaviors != null)
    {
      for (int i = 0; i < behaviors.size(); i++)
      {
        RiTextBehavior rtb = (RiTextBehavior) behaviors.get(i);
        rtb.delete();
      }
      behaviors.clear();
      behaviors = null;
    }
    imageRect = null;
    boundingBox = null;

    instances.remove(this);
  }

  /**
   * To be called only by AppletContainer on destroy, not by users!
   * 
   * @invisible
   */
  public void dispose()
  {
    delete(this);
  }

  protected static void initDefaults()
  {
    //DEFAULT_FONT = DEFAULT_FONT_CONST;
    DEFAULT_SHOW_BOUNDING_BOXES = false;
    DEFAULT_BB_STROKE = new float[] { 0, 0, 0, 255 };
    DEFAULT_BB_FILL = new float[] { 0, 0, 0, 0 };
    DEFAULT_BB_STROKE_WEIGHT = 1;
    DEFAULT_MOUSE_DRAGGABLE = false;
    DEFAULT_LINE_BREAK_WIDTH = 70;
    DEFAULT_MOTION_TYPE = LINEAR;
    DEFAULT_FONT_SIZE = -1;
    DEFAULT_ALIGN = LEFT;
    DEFAULT_ALPHA = 255;
    DEFAULT_RFILL = 0;
    DEFAULT_GFILL = 0;
    DEFAULT_BFILL = 0;
    AUTODRAW = true;
    fontCache = new HashMap();
  }

  /**
   * Returns a field for field copy of this object
   */
  public RiText copy()
  {
    return copy(this);
  }

  /**
   * Call to remove a RiText from the current sketch (and from existence),
   * cleaning up whatever resources it may have held
   */
  public static synchronized void delete(RiText rt)
  {
    if (rt != null)
    {
      PApplet p = rt.getPApplet();
      if (p != null)
      {
        p.unregisterDraw(rt);
        p.unregisterDispose(rt);
        p.unregisterMouseEvent(rt);
      }
      rt._dispose();
    }
  }

  /**
   * @invisible
   */
  public void mouseEvent(MouseEvent e)
  {
    float mx = e.getX();
    float my = e.getY();

    switch (e.getID())
    {
      case MouseEvent.MOUSE_PRESSED:
        if (mouseDraggable && !hidden)
        {
          this.mouseXOff = mx - x;
          this.mouseYOff = my - y;
        }
        break;
      case MouseEvent.MOUSE_RELEASED:
        if (mouseDraggable)
        {
          pauseBehaviors(false);
        }
        break;
      case MouseEvent.MOUSE_CLICKED:
        break;
      case MouseEvent.MOUSE_DRAGGED:
        if (mouseDraggable)
        {
          x = mx - mouseXOff;
          y = my - mouseYOff;
        }
        break;
      case MouseEvent.MOUSE_MOVED:
        break;
    }
  }

  /**
   * Returns the current text width in pixels
   */
  public float textWidth()
  {
    String txt = text.toString();
    if (txt == null)
    { // this happens occasionally -- need to dbug further
      // if (false) throw new
      // RuntimeException("[WARN] textWidth() called for null text!");
      if (!printedTextWidthWarning)
      {
        System.err.println("[WARN] textWidth() called for null text!");
        printedTextWidthWarning = true;
      }
      return 0; // hmm?
    }
    this.verifyFont();
    return _pApplet.textWidth(txt) * scaleX;
  }

  static boolean printedTextWidthWarning;

  /**
   * Returns the height for the current font in pixels (including ascendors and
   * descendors)
   */
  public float textHeight()
  {
    return (_pApplet.textAscent() + _pApplet.textDescent()) * scaleY;
  }

  protected void update()
  {
    update(_pApplet.g);
  }

  protected void update(PGraphics p)
  {

    if (x == Float.MIN_VALUE && text.toString() != null)
      x = getCenterX();

    this.updateBehaviors();

    if (boundingBoxVisible && text.toString() != null)
      this.updateBoundingBox(p);

    if (sample != null)
      sample.update();
  }

  /**
   * Sets the animation <code>motionType</code> for this for moveTo() or
   * moveBy() methods on this object, set via one of the following constants: <br>
   * <ul>
   * <li>RiText.LINEAR
   * <li>RiText.EASE_IN
   * <li>RiText.EASE_OUT
   * <li>RiText.EASE_IN_OUT
   * <li>RiText.EASE_IN_OUT_CUBIC
   * <li>RiText.EASE_IN_CUBIC
   * <li>RiText.EASE_OUT_CUBIC;
   * <li>RiText.EASE_IN_OUT_QUARTIC
   * <li>RiText.EASE_IN_QUARTIC
   * <li>RiText.EASE_OUT_QUARTIC;
   * <li>RiText.EASE_IN_OUT_SINE
   * <li>RiText.EASE_IN_SINE
   * <li>RiText.EASE_OUT_SINE
   * </ul>
   * 
   * @param motionType
   */
  public void setMotionType(int motionType)
  {
    this.motionType = motionType;
  }

  /**
   * Returns the <code>motionType</code> for this object,
   */
  public int getMotionType()
  {
    return this.motionType;
  }

  /**
   * See RiTa#setCallbackTimer(PApplet, float):
   * <p>
   * Generates a single callback to onRiTaEvent() after the amount of time
   * specifed in <code>seconds</code>.
   * 
   * @deprecated
   * @invisible
   */
  public RiTextBehavior setCallbackTimer(float seconds)
  {
    return RiTa.setCallbackTimer(_pApplet, seconds, false);
  }

  /**
   * See RiTa#setCallbackTimer(PApplet, float, boolean):
   * <p>
   * Generates a callback to onRiTaEvent() after the amount of time specifed in
   * <code>seconds</code>. Will repeat indefinitely if <code>repeat</code> is
   * set to true (default=true).
   * 
   * @deprecated
   * @invisible
   */
  public RiTextBehavior setCallbackTimer(float seconds, boolean repeat)
  {
    return RiTa.setCallbackTimer(_pApplet, seconds, repeat);
  }

  /**
   * Move to new absolute x,y (or x,y,z) position over 'time' seconds
   * <p>
   * Note: uses the current <code>motionType</code> for this object.
   * 
   * @return the unique id for this behavior
   */
  public int moveTo(float newX, float newY, float seconds)
  {
    return this.moveTo(new float[] { newX, newY }, 0, seconds);
  }

  /**
   * Move to new absolute x,y (or x,y,z) position over 'time' seconds
   * <p>
   * Note: uses the current <code>motionType</code> for this object, starting at
   * 'startTime' seconds in the future
   * 
   * @return the unique id for this behavior
   */
  public int moveTo(float newX, float newY, float startTime, float seconds)
  {
    return this.moveTo(new float[] { newX, newY }, startTime, seconds);
  }

  /**
   * Move to new absolute x,y (or x,y,z) position over 'time' seconds
   * <p>
   * Note: uses the current <code>motionType</code> for this object, starting at
   * 'startTime' seconds in the future
   * 
   * @see #setMotionType(int)
   * @return the unique id for this behavior
   */
  public int moveTo(final float[] newPosition, final float startTime, final float seconds)
  {
    String err3d = "Invalid newPosition.length for moveTo(),"
        + " expected 2 (or 3 in 3d mode), but found: " + newPosition.length;

    TextMotion moveTo = null;
    if (!is3D(_pApplet.g) || newPosition.length == 2) // 2d
    {
      if (newPosition.length != 2)
        throw new RiTaException(err3d);
      moveTo = new TextMotion2D(this, newPosition, startTime, seconds);
    }
    else
    // 3d
    {
      if (newPosition.length != 3)
        throw new RiTaException(err3d + "\nPerhaps you wanted moveTo3D()?");
      moveTo = new TextMotion3D(this, newPosition, startTime, seconds);
      // moveTo.resetTarget(new float[] {x,y,z}, newPosition, startTime,
      // seconds);
    }
    moveTo.setMotionType(motionType);

    addBehavior(moveTo);

    return moveTo.getId();
  }

  /**
   * Move to new position by x,y,z over the duration specified by 'seconds'.
   * <p>
   * Note: uses the current <code>motionType</code> for this object.
   * 
   * @see #setMotionType(int)
   * @return the unique id for this behavior
   * @invisible
   */
  public int moveTo3D(float newX, float newY, float newZ, float startTime, float seconds)
  {
    return this.moveTo(new float[] { newX, newY, newZ }, startTime, seconds);
  }

  /**
   * Move to new position by x,y,z over the duration specified by 'seconds'.
   * <p>
   * Note: uses the current <code>motionType</code> for this object.
   * 
   * @see #setMotionType(int)
   * @invisible
   * @return the unique id for this behavior
   */
  public int moveTo3D(float newX, float newY, float newZ, float seconds)
  {
    return this.moveTo(new float[] { newX, newY, newZ }, 0, seconds);
  }

  /**
   * Move to new position by x,y offset over the duration specified by
   * 'seconds', starting at 'startTime' seconds in the future
   * <p>
   * 
   * @return the unique id for this behavior
   */
  public int moveBy(float xOffset, float yOffset, float startTime, float seconds)
  {
    
    return (is3D(_pApplet.g)) ?
      this.moveBy(new float[] { xOffset, yOffset, 0 }, startTime, seconds) :
      this.moveBy(new float[] { xOffset, yOffset }, startTime, seconds);
  }

  /**
   * Move to new position by x,y offset over the duration specified by
   * 'seconds'.
   * <p>
   * 
   * @return the unique id for this behavior
   */
  public int moveBy(float xOffset, float yOffset, float seconds)
  {
    return this.moveBy(xOffset, yOffset, 0, seconds);
  }

  /**
   * Move to new position by x,y offset over the duration specified by
   * 'seconds'.
   * <p>
   * Note: uses the current <code>motionType</code> for this object.
   * 
   * @see #setMotionType(int)
   * @return the unique id for this behavior
   */
  public int moveBy(float[] posOffset, float startTime, float seconds)
  {

    boolean is3d = is3D(_pApplet.g);
    float[] newPos =  is3d ? new float[3] : new float[2];
    
    if (posOffset.length != newPos.length) {
 /*     if (is3d && posOffset.length == 2) {
        posOffset = new float[] {posOffset[0],posOffset[1],0};
      }*/
      throw new RiTaException("Expecting a 2d array(or 3 in 3d) "
          + "for the 1st argument, but found: " + RiTa.asList(posOffset));
    }
    newPos[0] = x + posOffset[0];
    newPos[1] = y + posOffset[1];
    if (newPos.length > 2)
      newPos[2] = posOffset.length > 2 ? z += posOffset[2] : z;
    return this.moveTo(newPos, startTime, seconds);
  }

  /**
   * Move to new position by x,y,z offset over the duration specified by
   * 'seconds'.
   * <p>
   * Note: uses the current <code>motionType</code> for this object.
   * 
   * @see #setMotionType(int)
   * @invisible
   */
  public int moveBy3D(float xOffset, float yOffset, float zOffset, float seconds)
  {
    return this.moveBy(new float[] { xOffset, yOffset, zOffset }, 0, seconds);
  }

  /**
   * Move to new position by x,y,z offset over the duration specified by
   * 'seconds'.
   * <p>
   * Note: uses the current <code>motionType</code> for this object.
   * 
   * @see #setMotionType(int)
   * @invisible
   */
  public int moveBy3D(float xOffset, float yOffset, float zOffset, float startTime, float seconds)
  {
    return this.moveBy(new float[] { xOffset, yOffset, zOffset }, startTime, seconds);
  }

  /**
   * Returns true if the object is offscreen
   */
  public boolean isOffscreen()
  {
    return isOffscreen(_pApplet.g);
  }

  /**
   * Returns true if the object is offscreen
   * 
   * @invisible
   */
  public boolean isOffscreen(PGraphics p)
  {
    // System.err.println(text+" - offscreen? ("+x+","+y+")");
    return (x < 0 || x >= p.width) || (y < 0 || y >= p.height);
  }

  // Scale methods ----------------------------------------

  /**
   * Scales object to 'newScale' over 'time' seconds, starting at 'startTime'
   * seconds in the future
   * <p>
   * Note: uses linear interpolation unless otherwise specified. Returns the Id
   * of the RiTextBehavior object used for the scale.
   */
  public int scaleTo(float newScale, float startTime, float seconds)
  {
    return scaleTo(newScale, newScale, newScale, startTime, seconds);
  }

  /**
   * Scales object to 'newScale' over 'time' seconds, starting immediately.
   * <p>
   * Note: uses linear interpolation unless otherwise specified. Returns the Id
   * of the RiTextBehavior object used for the scale.
   */
  public int scaleTo(float newScale, float seconds)
  {
    return scaleTo(newScale, newScale, newScale, 0, seconds);
  }

  /**
   * Scales object to {scaleX, scaleY, scaleZ} over 'time' seconds. Note: uses
   * linear interpolation unless otherwise specified. Returns the Id of the
   * RiTextBehavior object used for the scale.
   */
  public int scaleTo(float scaleX, float scaleY, float scaleZ, float seconds)
  {
    return scaleTo(scaleX, scaleY, scaleZ, 0, seconds);
  }

  /**
   * Scales object to {scaleX, scaleY, scaleZ} over 'time' seconds, starting at
   * 'startTime' seconds in the future.
   * <p>
   * Returns the Id of the RiTextBehavior object used for the scale. Note: uses
   * linear interpolation unless otherwise specified.
   */
  public int scaleTo(final float newScaleX, final float newScaleY, final float newScaleZ, final float startTime, final float seconds)
  {
    ScaleBehavior scaleTo = new ScaleBehavior(this, new float[] { newScaleX, newScaleY,
        newScaleZ }, startTime, seconds);
    scaleTo.setMotionType(LINEAR);
    addBehavior(scaleTo);
    return scaleTo.getId();
  }

  // Fade methods -----------------------------------------

  /**
   * Fades in current text over <code>seconds</code> starting at
   * <code>startTime</code>. Interpolates from the current color {r,g,b,a} to
   * {r,g,b,255}.
   * 
   * @param startTime
   *          time in future to start
   * @param seconds
   *          time for fade
   * @return a unique id for this behavior
   */
  public int fadeIn(float startTime, float seconds)
  {
    float[] col = { fillR, fillG, fillB, 255 };
    return _fadeColor(col, startTime, seconds, FADE_IN, false);
  }

  public int fadeIn(float seconds)
  {
    return this.fadeIn(0, seconds);
  }

  /**
   * Fades out current text over <code>seconds</code> starting at
   * <code>startTime</code>. Interpolates from the current color {r,g,b,a} to
   * {r,g,b,0}.
   * 
   * @param startTime
   *          time in future to start
   * @param seconds
   *          time for fade
   * @param removeOnComplete
   *          destroys the object when the behavior completes
   * @return the unique id for this behavior
   */
  public int fadeOut(float startTime, float seconds, boolean removeOnComplete)
  {
    float[] col = { fillR, fillG, fillB, 0 };
    // if (isBoundingBoxVisible()) // fade bounding box too
    // addBehavior(new BoundingBoxAlphaFade(this, 0, startTime, seconds));
    return _fadeColor(col, startTime, seconds, FADE_OUT, removeOnComplete);
  }

  public int fadeOut(float startTime, float seconds)
  {
    return this.fadeOut(startTime, seconds, false);
  }

  public int fadeOut(float seconds, boolean removeOnComplete)
  {
    return this.fadeOut(0, seconds, removeOnComplete);
  }

  public int fadeOut(float seconds)
  {
    return this.fadeOut(0, seconds, false);
  }

  protected synchronized int _fadeColor(final float[] color, final float startTime, final float seconds, final int type, final boolean deleteWhenFinished)
  {
    // System.out.println(this+"._fadeColor("+RiTa.asList(color)+")");

    if (boundingBoxVisible && (type == FADE_IN || type == FADE_OUT))
    {
      if (color[3] >= 255 || color[3] < 1) // hack to fade bounding box too
        addBehavior(new BoundingBoxAlphaFade(this, color[3], startTime, seconds));
    }

    TextColorFade colorFade = new TextColorFade(this, color, startTime, seconds);
    colorFade.setType(type);

    if (deleteWhenFinished)
      addDeleteListener(colorFade); // deletes the RiText after fadeOut (retest
                                    // this)

    addBehavior(colorFade);

    return colorFade.getId();
  }

  /**
   * Transitions to 'color' (rgba) over 'seconds' starting at 'startTime'
   * seconds in the future
   * 
   * @param seconds
   *          time for fade
   * @return a unique id for this behavior
   */
  public int fadeColor(float r, float g, float b, float a, float seconds)
  {
    return this.fadeColor(new float[] { r, g, b, a }, 0f, seconds);
  }

  public int fadeColor(float[] color, float seconds)
  {
    return this.fadeColor(color, 0, seconds);
  }

  public int fadeColor(float gray, float seconds)
  {
    return this.fadeColor(new float[] { gray, gray, gray, this.fillA }, 0, seconds);
  }

  /**
   * Transitions to 'color' (rgba) over 'seconds' starting at 'startTime'
   * seconds in the future
   * 
   * @param startTime
   *          time in future to start
   * @param seconds
   *          time for fade
   * @return a unique id for this behavior
   */
  public int fadeColor(float[] color, float startTime, float seconds)
  {
    return this._fadeColor(color, startTime, seconds, FADE_COLOR, false);
  }

  /**
   * Fades out the current text and fades in the <code>newText</code> over
   * <code>seconds</code> starting immediately
   * 
   * @return the unique id for this behavior
   */
  public int fadeToText(final String newText, final float seconds)
  {
    return fadeToText(newText, 0, seconds);
  }

  /**
   * Fades out the current text and fades in the <code>newText</code> over
   * <code>seconds</code> starting at 'startTime' seconds in the future
   * 
   * @param newText
   *          to be faded in
   * @param startTime
   *          # of seconds in the future that the fade will start
   * @param seconds
   *          time for fade
   * @return the unique id for this behavior
   */
  public int fadeToText(final String newText, final float startTime, final float seconds)
  {
    // grab the alpha if needed
    float startAlpha = 0;
    if (fadeToTextCopy != null)
    {
      startAlpha = fadeToTextCopy.getAlpha();
      delete(fadeToTextCopy); // stop any currents
    }

    // use the copy to fade out
    fadeToTextCopy = RiText.copy(this);
    fadeToTextCopy.setAutoDraw(true);
    fadeToTextCopy.fadeOut(seconds);

    // and use 'this' to fade in
    this.setText(newText);
    this.setAlpha(startAlpha);
    float[] col = { fillR, fillG, fillB, 255 }; // fadeIn
    return _fadeColor(col, startTime, seconds * .95f, FADE_TO_TEXT, false);
  }

  // deletes the RiText on completion (useful, for example, in fadeOut())
  protected void addDeleteListener(RiTextBehavior rtb)
  {
    if (rtb == null)
      return;
    rtb.addListener(new BehaviorListener()
    {
      public void behaviorCompleted(RiTextBehavior behavior)
      {
        delete(behavior.getParent());
      }
    });
  }

  // static xAll methods --------------------------------------------

  /**
   * Set the bounding box strokeweight for all existing RiTexts; set to 0 if you
   * wish to assign a background color with no stroke.
   * 
   * @deprecated see {@link #setDefaultBBoxStrokeWeight(float)}
   * @invisible
   */
  public static void setBoundingBoxStrokeWeights(float strokeWeight)
  {
    for (Iterator iter = instances.iterator(); iter.hasNext();)
    {
      RiText rt = (RiText) iter.next();
      rt.bbStrokeWeight = strokeWeight;
    }
  }

  /**
   * Set the default bounding box strokeweight for all RiTexts to be created.
   * Set to 0 if you wish to assign a background color with no stroke.
   */
  public static void setDefaultBBoxStrokeWeight(float strokeWeight)
  {
    DEFAULT_BB_STROKE_WEIGHT = strokeWeight;
  }

  /**
   * Set the bounding box stroke color for all RiTexts to be created;
   */
  public static void setDefaultBBoxStroke(float[] strokeColor)
  {
    DEFAULT_BB_STROKE = strokeColor;
  }

  /**
   * Set the bounding box fill color for all RiTexts to be created;
   */
  public static void setDefaultBBoxStroke(float r, float g, float b, float a)
  {
    DEFAULT_BB_STROKE = new float[] { r, g, b, a };
  }

  /**
   * Set the bounding box fill color for all RiTexts to be created;
   */
  public static void setDefaultBBoxFill(float[] fillColor)
  {
    DEFAULT_BB_FILL = fillColor;
  }

  /**
   * Set the bounding box fill color for all RiTexts to be created;
   */
  public static void setDefaultBBoxFill(float r, float g, float b, float a)
  {
    DEFAULT_BB_FILL = new float[] { r, g, b, a };
  }

  /**
   * Deletes all current instances.
   */
  public static synchronized void deleteAll()
  {
    delete(instances);
  }

  /**
   * Deletes all objects in the array (and the array itself)
   * 
   * @deprecated
   * @invisible
   * @see #delete(RiText[])
   */
  public static synchronized void removeAll(RiText[] c)
  {
    delete(c);
  }

  /**
   * Deletes all objects in the array (and the array itself)
   * 
   * @deprecated
   * @invisible
   * @see #delete(List)
   */
  public static synchronized void removeAll(List c)
  {
    delete(c);
  }

  /**
   * Deletes all current instances.
   * 
   * @deprecated
   * @invisible
   * @see #deleteAll()
   */
  public static synchronized void removeAll()
  {
    deleteAll();
  }

  /**
   * Deletes all objects in the array (and the array itself)
   */
  public static synchronized void delete(RiText[] c)
  {
    if (c == null)
      return;
    for (int i = 0; i < c.length; i++)
    {
      if (c[i] != null)
      {
        delete(c[i]);
        c[i] = null;
      }
    }
    c = null;
  }

  /**
   * Deletes all objects in the List.
   */
  public static synchronized void delete(List l)
  {
    if (l == null)
      return;
    while (l.size() > 0)
    {
      RiText p = (RiText) l.remove(0);
      delete(p);
    }
  }

  /**
   * Sets font for all existing objects.
   * 
   * @param pfont
   * @deprecated
   * @invisible
   * @see #textFont(PFont)
   * @see #setDefaultFont(String)
   */
  public static void setFont(PFont pfont)
  {
    for (int i = 0; i < instances.size(); i++)
    {
      RiText p = (RiText) instances.get(i);
      p.textFont(pfont);
    }
  }

  /**
   * Sets default text size for all existing objects.
   * 
   * @param fontSize
   * @deprecated
   * @invisible
   * @see #loadFont(String, float)
   */
  public static void setFontSize(float fontSize)
  {
    for (int i = 0; i < instances.size(); i++)
    {
      RiText rt = (RiText) instances.get(i);
      rt.textSize(fontSize);
    }
  }

  /**
   * Sets boundingBoxes (in)visible for all existing objects
   * 
   * @invisible
   * @deprecated
   */
  public static void setBoundingBoxesVisible(boolean visible)
  {
    showBoundingBoxes(visible);
  }

  /**
   * Sets visibility of boundingBoxes for all existing objects
   * 
   * @deprecated
   * @invisible
   * @see #setDefaultBBoxVisibility(boolean)
   */
  public static void showBoundingBoxes(boolean visible)
  {
    for (int i = 0; i < instances.size(); i++)
    {
      RiText p = (RiText) instances.get(i);
      p.showBoundingBox(visible);
    }
  }

  /**
   * Sets alignment for all existing objects
   * 
   * @param alignment
   * @deprecated
   * @invisible
   * @see #textAlign(int)
   */
  public static void setTextAlignment(int alignment)
  {
    for (int i = 0; i < instances.size(); i++)
    {
      RiText p = (RiText) instances.get(i);
      p.textMode(alignment);
    }
  }

  /**
   * Sets font for all existing RiText objects.
   * 
   * @deprecated
   * @invisible
   * @see #setDefaultFont(String)
   */
  public static void setFont(String fontName)
  {
    RiText.setDefaultFont(fontName);
  }

  /**
   * Returns all existing instances of RiText objects in an array
   */
  public static RiText[] getInstances()
  {
    return (RiText[]) instances.toArray(new RiText[instances.size()]);
  }
  
  /**
   * Returns all RiTexts that contain the point x,y or null if none do.
   * <p>
   * Note: this will return an array even if only one item is picked, therefore,
   * you should generally use it as follows:
   * 
   * <pre>
   *   RiText picked = null;
   *   RiText[] rts = RiText.getPicked(mx, my);
   *   if (rts != null)
   * picked = rts[0];
   * 
   * <pre>
   * @return RiText[] 1 or more RiTexts containing
   * the point, or null if none do.
   */
  public static RiText[] getPicked(float x, float y)
  {
    List pts = null;
    for (int i = 0; i < instances.size(); i++)
    {
      RiText rt = (RiText) instances.get(i);
      if (rt.contains(x, y))
      {
        if (pts == null)
          pts = new ArrayList();
        pts.add(rt);
      }
    }
    if (pts == null || pts.size() == 0)
      return null;

    return (RiText[]) pts.toArray(new RiText[pts.size()]);
  }

  // end statics ----------------------------------------------

  /**
   * Fades all visible RiText objects.
   */
  public static void fadeAllOut(float seconds)
  {
    for (Iterator i = instances.iterator(); i.hasNext();)
    {
      RiText p = (RiText) i.next();
      p.fadeOut(seconds);
    }
  }

  /**
   * Fades in all RiText objects over the specified duration
   */
  public static void fadeAllIn(float seconds)
  {
    for (Iterator i = instances.iterator(); i.hasNext();)
    {
      RiText p = (RiText) i.next();
      p.fadeIn(seconds);
    }
  }

  /**
   * Loads a sample file and returns the appropriate type of RiSample object
   * according to the library specified in <code>playerType</code> (defaults to
   * Minim). Starts the sample looping if <code>setLooping</code> is true
   * 
   * @see RiSample#MINIM
   * @see RiSample#ESS
   * @see RiSample#SONIA
   */
  public RiSample loadSample(String sampleFileName, int playerType, boolean setLooping)
  {
    if (this.sample != null)
      this.sample.stop();
    this.sample = RiTa.loadSample(_pApplet, sampleFileName, playerType, setLooping);
    return this.sample;
  }

  public RiSample loadSample(String sampleFileName, boolean setLooping)
  {
    return loadSample(sampleFileName, RiSample.MINIM, setLooping);
  }

  public RiSample loadSample(String sampleFileName, int playerType)
  {
    return loadSample(sampleFileName, playerType, false);
  }

  public RiSample loadSample(String sampleFileName)
  {
    return loadSample(sampleFileName, RiSample.MINIM, false);
  }

  // getters / setters ----------------------------------------------

  /**
   * Return the current font for this object
   */
  public PFont getFont()
  {
    return font;
  }

  /**
   * Sets the font and size for this object's text
   */
  public void textFont(PFont pf, float size)
  {
    this.font = pf;
    this.fontSize = size; // even -1 ? yes...
  }

  public void textFont(PFont pf)
  {
    this.textFont(pf, -1);
  }

  private static PFont checkFontCache(String fontFileName, float sz)
  {
    PFont pf = (PFont) fontCache.get(fontFileName + sz);
    // System.out.println("CacheCheck: "+fontFileName+sz+" -> "+(pf!=null));
    return pf;
  }

  /**
   * Returns the font specified after loading it and setting it as the the
   * current font.
   */
  public PFont loadFont(String fontFileName)
  {
    return loadFont(fontFileName, -1);
  }

  private static PFont fontFromStream(InputStream is, String name)
  {
    // System.out.println("fontFromStream("+name+")");
    try
    {
      return new PFont(is);
    }
    catch (IOException e)
    {
      throw new RiTaException("creating font from stream: " + is + " with name=" + name);
    }
  }

  /**
   * Returns the font specified after loading it and setting the current font
   * size.
   */
  public PFont loadFont(String fontFileName, float size)
  {
    PFont pf = _loadFont(getPApplet(), fontFileName, size);
    this.textFont(pf, size);
    return pf;
  }

  protected static PFont _loadFont(PApplet pApplet, String fontFileName, float size)
  {
    PFont pf = checkFontCache(fontFileName, size);
    if (pf == null)
    {
      // System.out.println("LOADING: "+fontFileName);
      //if (fontFileName.equals(DEFAULT_FONT_CONST))
        //pf = fontFromZip(fontFileName);

      // try the filesystem...
      if (pf == null)
      {
        try
        {
          // System.out.println("looking for font: "+fontFileName);
          InputStream is = RiTa.openStream(pApplet, fontFileName);
          pf = fontFromStream(is, fontFileName);
        }
        catch (Throwable e)
        {
          String errStr = "Could not load font '"+ fontFileName + "'. Make "
              + "sure that the font\nhas been copied to the data folder of your sketch\nError="
              + e.getMessage();
          RiTa.die(pApplet, errStr);
        }
      }
      cacheFont(fontFileName, size, pf); // add to cache
    }
    return pf;
  }

  private static PFont fontFromZip(String fontFileName)
  {
    PFont pf = null;
    try
    {
      URL url = RiText.class.getResource(fontFileName);
      if (url != null)
      {
        pf = fontFromStream(url.openStream(), fontFileName);
      }
    }
    // should never happen
    catch (Throwable e)
    {
    }
    return pf;
  }

  private static void cacheFont(String fontFileName, float fontSz, PFont pf)
  {
    // System.out.println("caching: "+fontFileName+fontSz+"->"+pf);
    fontCache.put(fontFileName + fontSz, pf);
  }

  /**
   * Creates (and caches) the font for this object from a System font (via
   * PApplet.createFont()). Note: this is not a good idea for web-applets as the
   * user's machine must have the specified font.
   * 
   * @see PApplet#createFont(String, float)
   */
  public void createFont(String fontName, float sz)
  {
    // System.out.println("RiText.createFont("+fontName+","+sz+")");
    this.font = _createFont(_pApplet, fontName, sz);
    textFont(font, sz);
    // System.out.println("RiText.createFont() returning "+font);
  }

  protected static PFont _createFont(PApplet p, String fontName, float sz)
  {
    PFont pf = checkFontCache(fontName, sz);
    //System.out.println("Checking cache: "+fontName+"-"+sz);
    if (pf == null)
    {
      //System.out.println("Creating font: "+fontName+"-"+sz);
      pf = p.createFont(fontName, sz); 
      cacheFont(fontName, sz, pf);
    }
    return pf;
  }

  /**
   * Set the current bgstroke weight for this object
   */
  public void boundingBoxStrokeWeight(float r)
  {
    this.bbStrokeWeight = r;
  }

  /**
   * Sets min padding (in pixels) around all sides of text in bounding box
   * (default=1)
   * 
   * @invisible
   */
  public void boundingBoxPadding(float padding)
  {
    this.bbPadding = padding;
  }

  /**
   * Gets the current text size
   * 
   * @invisible
   */
  public float textSize()
  {
    return fontSize;
  }

  /**
   * Sets the object to this size
   */
  public void textSize(float textSize)
  {
    this.fontSize = textSize;
  }

  /**
   * Returns the current text
   */
  public String getText()
  {
    return text.getText();
  }

  /**
   * Sets the current text to this String
   */
  public void setText(String _text)
  {
    if (this.text == null)
      this.text = new RiString(_text);
    else
      this.text.setString(_text);
  }

  /**
   * Sets boolean flag to show or hide the object
   */
  public void setVisible(boolean visible)
  {
    this.hidden = !visible;
  }

  /**
   * Sets the current text to the character
   * 
   * @invisible
   */
  public void setText(char ch)
  {
    this.setText(Character.toString(ch));
  }

  /** @invisible */
  public String toString()
  {
    return text.toString();
    // return "RiText#"+getId()+"['" + text + "']";
  }

  /**
   * Gets the current x-position of the RiText
   */
  public float getX()
  {
    return x;
  }

  /**
   * Sets the x-position of the current RiText
   */
  public void setX(float x)
  {
    this.x = x;
  }

  /**
   * Gets the current y-position of the RiText
   */
  public float getY()
  {
    return y;
  }

  /**
   * Sets the y-position of the current RiText
   */
  public void setY(float y)
  {
    this.y = y;
  }

  /**
   * Returns true if the objects is not hidden
   */
  public boolean isVisible()
  {
    return !this.hidden;
  }

  /**
   * Returns the RiSample object associated with this RiText
   */
  public RiSample getSample()
  {
    return sample;
  }

  /**
   * Returns the height of the image associated with this RiText
   */
  public float getImageHeight()
  {
    return this.imageHeight;
  }

  /**
   * Returns the width of the image associated with this RiText
   */
  public float getImageWidth()
  {
    return this.imageWidth;
  }

  /** @invisible */
  public synchronized void updateBehaviors()
  {
    for (int i = 0; behaviors != null && i < behaviors.size(); i++)
    {
      RiTextBehavior rtb = (RiTextBehavior) behaviors.get(i);
      // System.out.println("RiText.updateBehaviors("+rtb+")");
      if (rtb == null)
      {
        behaviors.remove(rtb);
        continue;
      }
      rtb.update();
    }
  }

  /**
   * Add a new behavior to this RiText's run queue
   */
  public synchronized RiTextBehavior addBehavior(RiTextBehavior behavior)
  {
    if (behaviors == null)
      this.behaviors = new ArrayList();
    if (!behaviors.contains(behavior))
      this.behaviors.add(behavior);
    return behavior;
  }

  /**
   * Remove a Behavior from the RiText's run queue
   * @invisible
   */
  public void removeBehavior(RiTextBehavior behavior)
  {
    // System.out.println("REMOVED behavior: " + behavior);
    if (behaviors == null)
      return;
    behaviors.remove(behavior);
    behavior.delete();
  }

  /**
   * Immediately marks all Behaviors in the RiText's run queue as
   * complete and causes them to fire their<code>behaviorCompleted()</code> methods.
   * @invisible 
   */
  public void completeBehaviors()
  {
    if (behaviors == null)
      return;
    for (int i = 0; i < behaviors.size(); i++)
      ((RiTextBehavior) behaviors.get(i)).finish();
  }// NEEDs MORE TESTING!

  /**
   * Pauses (or unpauses) all Behaviors in the RiText's run queue
   * @invisible
   */
  public synchronized void pauseBehaviors(boolean paused)
  {
    if (behaviors == null)
      return;
    for (int i = 0; i < behaviors.size(); i++)
    {
      RiTextBehavior tb = (RiTextBehavior) behaviors.get(i);
      tb.setPaused(paused);
    }
  }

  /**
   * Remove all Behaviors from the RiText's run queue
   * 
   * @invisible
   */
  public synchronized void removeBehaviors()
  {
    if (behaviors == null)
      return;
    for (int i = 0; i < behaviors.size(); i++)
    {
      RiTextBehavior tb = (RiTextBehavior) behaviors.get(i);
      this.removeBehavior(tb);
    }
  }

  /**
   * Sets the position for the current RiText
   */
  public void setPosition(float x, float y)
  {
    this.x = x;
    this.y = y;
  }

  /**
   * Sets the 3d position for the current RiText
   * @invisible 
   */
  public void setPosition(float x, float y, float z)
  {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  /**
   * Returns the visibility of the objects bounding box (default=false)
   * 
   * @invisible
   */
  public boolean isBoundingBoxVisible()
  {
    return this.boundingBoxVisible;
  }

  /**
   * Sets the visibility of the objects bounding box (default=false)
   */
  public void showBoundingBox(boolean showBoundingBox)
  {

    this.boundingBoxVisible = showBoundingBox;
  }

  /**
   * Returns a list of behaviors for the object.
   * 
   * @invisible
   */
  public List getBehaviors()
  {
    return this.behaviors;
  }

  /**
   * Returns a list of behaviors of the specified type for this object, where
   * type is generally one of (MOVE, FADE_IN, FADE_OUT, FADE_TO_TEXT, SCALE_TO,
   * etc.)
   * 
   * @invisible
   */
  public RiTextBehavior[] getBehaviorsByType(int type)
  {
    List l = RiTextBehavior.selectByType(behaviors, type);
    return (RiTextBehavior[]) l.toArray(new RiTextBehavior[l.size()]);
  }

  /**
   * Returns the behavior corresponding to the specified 'id'.
   * 
   * @invisible
   */
  public static RiTextBehavior getBehaviorById(int id)
  {
    return RiTextBehavior.getBehaviorById(id);
  }

  /**
   * Returns true if the object has been set to be draggable by the mouse
   * 
   * @invisible
   */
  public boolean isMouseDraggable()
  {
    return this.mouseDraggable;
  }

  /**
   * Sets the object's draggable state (default=false)
   */
  public void setMouseDraggable(boolean mouseDraggable)
  {
    this.mouseDraggable = mouseDraggable;
  }

  /**
   * Returns the current alignment (default=LEFT)
   * 
   * @invisible
   */
  public int getTextAlignment()
  {
    return this.textAlignment;
  }

  /**
   * Sets text mode for RiText object.
   * 
   * @param alignment
   *          (CENTER, RIGHT, LEFT[default])
   * @see #textAlign(int)
   */
  public void textAlign(int alignment)
  {
    this.textMode(alignment);
  }

  /**
   * Sets text mode for RiText object.
   * 
   * @param alignment
   *          (CENTER, RIGHT, LEFT[default])
   * @see #textAlign(int)
   */
  public void textMode(int alignment)
  {
    switch (alignment)
    {
      case LEFT:
        break;
      case CENTER:
        break;
      case RIGHT:
        break;
      default:
        throw new RiTaException("Illegal alignment value: use LEFT, CENTER, or RIGHT");
    }
    this.textAlignment = alignment;
  }

  /**
   * Returns a rectangle representing the current screen position of the
   * bounding box
   */
  public Rectangle2D getBoundingBox()
  {
    updateBoundingBox(_pApplet.g);
    if (screenBoundingBox == null)
      screenBoundingBox = new Rectangle2D.Float();

    screenBoundingBox.setRect((float) (x + boundingBox.getX()), (float) (y + boundingBox.getY()), (float) (boundingBox.getWidth()), (float) (boundingBox.getHeight()));

    return screenBoundingBox;
  }

  /**
   * Converts and returns a list of RiTexts as a RiText[]
   * @invisible 
   */
  public static RiText[] toArray(List result)
  {
    return (RiText[]) result.toArray(new RiText[result.size()]);
  }

  /**
   * Splits this object into an array of RiTexts, one per word, with correct
   * x-positions, based on the <code>pFont</code> font metrics.
   * <p>
   * Note: uses String.split(String) to split on the regex.
   * 
   * @see String#split(String)
   * @deprecated
   * @invisible
   * @see #splitWords()
   * @see #splitLetters()
   */
  private RiText[] split(PFont pfont, String regex, float yPos)
  {
    if (regex.equals(ANY_CHAR_REGEX) || regex.equals(".")) // hmm :[
      return splitLetters(pfont);

    String[] txts = getText().split(regex);

    List result = new ArrayList();
    for (int i = 0; i < txts.length; i++)
    {
      if (txts[i] != null && txts[i].length() > 0)
      {
        result.add(new RiText(_pApplet, txts[i], getWordOffsetWith(pfont, i, regex), yPos));
      }
    }
    return toArray(result);
  }

  /**
   * Splits this object into an array of RiTexts, one per word, with correct x
   * and y-positions.
   * <p>
   * To split into words, according to the default tokenizer, use
   * RiTa.tokenize(String).
   * <p>
   * Note: If desired, the original RiText should be deleted explicitly (via
   * RiText.delete(originalRiText)).
   * 
   * @invisible
   */
  public RiText[] splitWords()
  {
    return createWords(getPApplet(), getText(), x, y);
  }

  /**
   * Returns number of characters in the contained String
   */
  public int length()
  {
    return text.length();
  }

  /* Returns the pixel x-offset for the given word index using the given font<p> */
  protected float getWordOffsetWith(PFont pfont, int wordIdx, String delim)
  {
    String[] words = text.toString().split(delim);
    return this.getWordOffset(pfont, words, wordIdx);
  }

  protected float getWordOffset(PFont pfont, String[] words, int wordIdx)
  {
    if (wordIdx >= words.length)
      throw new IllegalArgumentException("\nBad wordIdx=" + wordIdx + " for "
          + RiTa.asList(words));

    if (pfont == null)
      verifyFont();
    else
      _pApplet.textFont(pfont);

    float xPos = this.x;
    if (wordIdx > 0)
    {
      String[] pre = new String[wordIdx];
      System.arraycopy(words, 0, pre, 0, pre.length);
      String preStr = RiTa.join(pre, SPC) + SPC;
      float tw = _pApplet.textWidth(preStr);
      switch (textAlignment)
      {
        case LEFT:
          xPos = this.x + tw;
          break;
        case RIGHT:
          xPos = this.x - tw;
          break;
        default:
          throw new RiTaException("getWordOffset() only supported for LEFT & RIGHT alignments");
      }
    }
    return xPos;
  }

  
  /**
   * Returns the x-position (in pixels) for the character at 'charIdx'.
   * @param pf 
   * 
   * @invisible
   */
  public float positionForChar(int charIdx)
  {
    return positionForChar(getDefaultFont(_pApplet), charIdx);
  }
  
  /**
   * Returns the x-position (in pixels) for the character at 'charIdx'.
   * @param pf 
   * 
   * @invisible
   */
  public float positionForChar(PFont pf, int charIdx)
  {
    if (charIdx <= 0) return x;
    if (charIdx > length()) // -1?
      charIdx = length();
    String sub = getText().substring(0, charIdx);
    _pApplet.textFont(pf);
    return x + _pApplet.textWidth(sub);
  }

  /** @invisible */
  public PApplet getPApplet()
  {
    if (_pApplet == null && !msgNullRootApplet)
    {
      System.err.println("[WARN] getPApplet() returned null");
      msgNullRootApplet = true;
    }
    return _pApplet;
  }

  static boolean msgNullRootApplet;

  // ========================= STATICS =============================
  /**
   * Immediately pauses all Behaviors in the RiText's run queue
   * 
   * @invisible
   */
  public static synchronized void pauseAllBehaviors(boolean paused)
  {
    RiText[] cts = RiText.getInstances();
    for (int i = 0; i < cts.length; i++)
      cts[i].pauseBehaviors(paused);
  }

  /**
   * Creates an array of RiText from a file, by delegating to
   * PApplet.loadStrings(), then creating one array element per line (including
   * blanks).
   * 
   * @param fileName
   *          located in data directory
   */
  public static RiText[] loadStrings(PApplet p, String fileName)
  {
    String[] lines = RiTa.loadStrings(p, fileName);
    RiText[] rts = new RiText[lines.length];
    for (int i = 0; i < lines.length; i++)
      rts[i] = new RiText(p, lines[i]);
    return rts;
  }

  /**
   * Creates an array of RiText from the file specified by 'fileName', one per
   * line of input text, constrained to the 'maxChars' specified, starting with
   * upper left corner at 'startX' and 'startY'.
   */
  public static RiText[] createLinesFromFile(PApplet pApplet, String fileName, float startX, float startY, int maxCharsPerLine)
  {
    return createLinesFromFile(pApplet, fileName, startX, startY, maxCharsPerLine, -1);
  }

  /**
   * Creates an array of RiText from the file specified by 'fileName', one per
   * line of input text, starting with upper left corner at 'startX' and
   * 'startY'.
   */
  public static RiText[] createLinesFromFile(PApplet pApplet, String fileName, int x, int y)
  {
    return createLinesFromFile(pApplet, fileName, x, y, -1, -1);
  }

  /**
   * Creates an array of RiText from the file specified by 'fileName', one per
   * line of input text, constrained to the 'maxChars' specified, starting with
   * upper left corner at 'startX' and 'startY'.
   * 
   * @param leading
   *          pixels to add between lines (defaults to 1/2 the font-height)
   * @return RiText[] or null for empty input
   */
  public static RiText[] createLinesFromFile(PApplet pApplet, PFont font, String fileName, float startX, float startY, int maxCharsPerLine, float leading)
  {
    String tmp = RiTa.loadString(pApplet, fileName);
    tmp = tmp.replaceAll("[\\r\\n]", " ");
    return createLines(pApplet, font, tmp, startX, startY, maxCharsPerLine, leading);
  }

  /**
   * Creates an array of RiText from the file specified by 'fileName', one per
   * line of input text, constrained to the 'maxChars' specified, starting with
   * upper left corner at 'startX' and 'startY', using the 'leading' specified.
   */
  public static RiText[] createLinesFromFile(PApplet pApplet, String fileName, float startX, float startY, int maxCharsPerLine, float leading)
  {
    return createLinesFromFile(pApplet, _defaultFont(pApplet), fileName, startX, startY, maxCharsPerLine, leading);
  }

  /**
   * Creates an array of RiText, one per line of input 'text', constrained to
   * the 'maxChars' specified, starting with upper left corner at 'startX' and
   * 'startY'.
   */
  public static RiText[] createLines(PApplet pApplet, String text, float startX, float startY, int maxCharsPerLine, float leading)
  {
    return createLines(pApplet, _defaultFont(pApplet), text, startX, startY, maxCharsPerLine, leading);
  }

  public static RiText[] createLines(PApplet p, String text, float startX, float startY)
  {
    return createLines(p, text, startX, startY, DEFAULT_LINE_BREAK_WIDTH, RiPageLayout.DEFAULT_LEADING);
  }

  public static RiText[] createLines(PApplet p, String text, float startX, float startY, int maxCharsPerLine)
  {
    return createLines(p, text, startX, startY, maxCharsPerLine, RiPageLayout.DEFAULT_LEADING);
  }

  /**
   * Creates an array of RiText, one per line of input 'text', constrained to
   * the 'maxChars' specified, starting with upper left corner at 'startX' and
   * 'startY'.
   * 
   * @param startX
   *          x-position for 1st line of text
   * @param startY
   *          x-position for 1st line of text
   * @param leading
   *          # of pixels to add between lines in addition to the textAscent
   *          (defaults to ~2x the textAscent of the current font)
   * @return RiText[] or null on empty input
   */
  public static RiText[] createLines(PApplet pApplet, PFont font, String text, float startX, float startY, int maxCharsPerLine, float leading)
  {
    //System.out.println("RiText.createLines("+text+", "+startX+", "+startY+", "+maxCharsPerLine+", "+leading+")");
    
    if (maxCharsPerLine < 1)
      maxCharsPerLine = Integer.MAX_VALUE;

    if (text == null || text.length() == 0)
      return null;

    if (text.length() < maxCharsPerLine)
      return new RiText[] { new RiText(pApplet, text, startX, startY) };

    // remove any line breaks from the original
    text = text.replaceAll("\n", SPC);

    List texts = new LinkedList();
    while (text.length() > maxCharsPerLine)
    {
      String toAdd = text.substring(0, maxCharsPerLine);
      text = text.substring(maxCharsPerLine, text.length());

      int idx = toAdd.lastIndexOf(RiTa.SPC);
      String end = QQ;
      if (idx >= 0)
      {
        end = toAdd.substring(idx, toAdd.length());
        if (maxCharsPerLine < Integer.MAX_VALUE)
          end = end.trim();
        toAdd = toAdd.substring(0, idx);
      }
      //if (text.length() > 0) {
      texts.add(new RiText(pApplet, toAdd.trim(), startX, startY));
      text = end + text;
      //}
    }

    if (text.length() > 0)
    {
      // System.out.println("Adding2: "+text);
      if (maxCharsPerLine < Integer.MAX_VALUE)
        text = text.trim();
      //if (text.length() > 0)
      texts.add(new RiText(pApplet, text, startX, startY));
    }

    for (Iterator iterator = texts.iterator(); iterator.hasNext();)
    {
      RiText rt = (RiText) iterator.next();
      if (rt.length() < 1)
        iterator.remove();
    }
    
    handleLeading(pApplet, font, texts, startY, leading);

    return (RiText[]) texts.toArray(new RiText[texts.size()]); 
  }

  /**
   * Creates a RiText[] from the <code>lines</code>, re-formatting lines
   * according to <code>maxCharsPerLine</code> (using the specified 'font').
   */
  public static RiText[] createLines(PApplet pApplet, String[] lines, float startX, float startY, int maxCharsPerLine, float leading)
  {
    return createLines(pApplet, _defaultFont(pApplet), lines, startX, startY, maxCharsPerLine, leading);
  }

  public static RiText[] createLines(PApplet pApplet, String[] text, float startX, float startY)
  {
    return createLines(pApplet, text, startX, startY, DEFAULT_LINE_BREAK_WIDTH, RiPageLayout.DEFAULT_LEADING);
  }

  public static RiText[] createLines(PApplet pApplet, String[] text, float startX, float startY, int maxCharsPerLine)
  {
    return createLines(pApplet, text, startX, startY, maxCharsPerLine, RiPageLayout.DEFAULT_LEADING);
  }

  /**
   * Creates a RiText[] from the <code>lines</code>, re-formatting lines
   * according to <code>maxCharsPerLine</code> (using the specified 'font') -- to
   * maintain the current line breaks, set <code>maxCharsPerLine</code> to -1.
   * 
   * @param startX
   *          start x-position of each lines
   * @param startY
   *          start y-position for the 1st line
   * @param maxCharsPerLine
   *          set to -1 to use line breaks specified in <code>lines</code>array
   * @param leading
   *          # of pixels to add between lines in addition to the textAscent
   *          (defaults to ~2x the textAscent of the current font)
   */
  static RiText[] createLines(PApplet pApplet, PFont font, String[] lines, float startX, float startY, int maxCharsPerLine, float leading)
  {
    // System.out.println("RiText.createLines("+RiTa.asList(lines)+", "+startX+", "+startY+", "+maxCharsPerLine+", "+
    // leading+")");

    if (maxCharsPerLine == -1)
    {
      List ritexts = new LinkedList();
      for (int i = 0; i < lines.length; i++)
      {
        RiText rr = new RiText(pApplet, lines[i], startX, startY);
        if (font != null)
        {
          rr.textFont(font);
        }
        ritexts.add(rr);
      }

      if (ritexts.size() < 1)
        return new RiText[0];

      handleLeading(pApplet, font, ritexts, startY, leading);

      return (RiText[]) ritexts.toArray(new RiText[ritexts.size()]);
    }
    else
      return createLines(pApplet, font, RiTa.join(lines), startX, startY, maxCharsPerLine, leading);
  }

  /**
   * Creates an array of RiTexts, one per letter, with appropriate x and y
   * positions for each (using the default 'font'), starting at 'xStart' and
   * 'yStart'.
   */
  public static RiText[] createLetters(PApplet pApplet, String theText, int startX, int startY)
  {
    return createLetters(pApplet, null, theText, startX, startY);
  }

  /**
   * Creates an array of RiTexts, one per letter, with appropriate x and y
   * positions for each (using the 'font' specified), starting at 'xStart' and
   * 'yStart'.
   */
  public static RiText[] createLetters(PApplet pApplet, PFont theFont, String theText, int startX, int startY)
  {
    if (theFont == null) theFont = getDefaultFont(pApplet);
    RiText tmp = new RiText(pApplet, theText, startX, startY, theFont);
    RiText[] result = tmp.splitLetters(theFont);
    delete(tmp);
    return result;
  }

  /**
   * Creates an array of RiText, one per line from the input String, and lays it
   * out according to the rectangle specified.
   * <p>
   */
  public static RiText[] createLines(PApplet pApplet, String text, Rectangle rectangle)
  {
    return createLines(pApplet, null, text, rectangle);
  }

  /**
   * Creates an array of RiText, one per line from the input String, and lays it
   * out according to the rectangle (and font) specified.
   * <p>
   */
  public static RiText[] createLines(PApplet pApplet, PFont pf, String text, Rectangle rectangle)
  {
    return createLines(pApplet, pf, text, rectangle, RiPageLayout.DEFAULT_LEADING);
  }

  /**
   * Creates an array of RiText, one per line from the input String, and lays it
   * out according to the rectangle (and font/leading) specified.
   * <p>
   */
  public static RiText[] createLines(PApplet pApplet, PFont pf, String text, Rectangle rectangle, float leading)
  {
    RiPageLayout rp = createLayout(pApplet, rectangle, leading);
    rp.layout(pf, text);
    return rp.getLines();
  }

  /**
   * Creates an array of RiTexts, one per line from the input file, and lays it
   * out according to the rectangle specified.
   * <p>
   */
  public static RiText[] createLinesFromFile(PApplet pApplet, String fileName, Rectangle rectangle)
  {
    return createLinesFromFile(pApplet, null, fileName, rectangle);
  }

  /**
   * Creates an array of RiTexts, one per line from the input file, and lays it
   * out according to the rectangle specified.
   * <p>
   */
  public static RiText[] createLinesFromFile(PApplet pApplet, PFont pf, String fileName, Rectangle rectangle)
  {
    return createLinesFromFile(pApplet, pf, fileName, rectangle, RiPageLayout.DEFAULT_LEADING);
  }

  /**
   * Creates an array of RiTexts, one per line from the input file, and lays it
   * out according to the rectangle specified.
   * <p>
   */
  public static RiText[] createLinesFromFile(PApplet pApplet, PFont pf, String fileName, Rectangle rectangle, float leading)
  {
    RiPageLayout rp = createLayout(pApplet, rectangle, leading);
    rp.layoutFromFile(pf, fileName);
    return rp.getLines();
  }

  private static RiPageLayout createLayout(PApplet pApplet, Rectangle rectangle, float leading)
  {
    return createLayout(pApplet, rectangle, leading, RiPageLayout.DEFAULT_PARAGRAPH_INDENT);
  }

  private static RiPageLayout createLayout(PApplet pApplet, Rectangle rectangle, float leading, int indentSize)
  {
    RiPageLayout rp = new RiPageLayout(pApplet, rectangle, pApplet.width, pApplet.height);
    rp.setIndentSize(indentSize);
    rp.setLeading(leading);
    return rp;
  }

  /**
   * Pops the last value off the array, deletes it, and returns the new array
   * (shortened by one element).
   * <p>
   * If there are no elements in the array, the original array is returned
   * unchanged.
   * 
   * @invisible
   */
  public static RiText[] popArray(RiText[] rts)
  {
    if (rts == null || rts.length < 1)
      return rts;
    RiText[] tmp = new RiText[rts.length - 1];
    System.arraycopy(rts, 0, tmp, 0, tmp.length);
    RiText.delete(rts[rts.length - 1]);
    return tmp;
  }

  /**
   * Shifts the first value off the array, deletes it, and returns the new array
   * (shortened by one element).
   * <p>
   * If there are no elements in the array, the original array is returned
   * unchanged.
   * 
   * @invisible
   */
  public static RiText[] shiftArray(RiText[] rts)
  {
    if (rts == null || rts.length < 1)
      return rts;
    RiText[] tmp = new RiText[rts.length - 1];
    System.arraycopy(rts, 1, tmp, 0, tmp.length);
    return tmp;
  }

  private static void handleLeading(PApplet p, PFont font, List ritexts, float startY, float leading)
  {
    if (p == null || ritexts.size() < 1)
      return;

    // set the font
    if (font != null)
      p.textFont(font);

    // calculate the leading
    float yOff = leading >= 0 ? p.textAscent() + leading
        : ((RiText) ritexts.get(0)).textHeight() * 1.4f;

    // handle the y-spacing
    float nextHeight = startY;
    for (Iterator iter = ritexts.iterator(); iter.hasNext();)
    {
      ((RiText) iter.next()).y = nextHeight;
      nextHeight += yOff;
    }
  }
  
  /**
   * Splits the current object into an array of RiTexts, one per letter,
   * maintaining the x and y position of each. Note: In most cases the original
   * RiText should be deleted manually to avoid a doubling effect (via
   * RiText.delete(originalRiText)).
   * 
   * @invisible
   */
  public RiText[] splitLetters()
  {
    return splitLetters(font);  
  }
  
  /**
   * Splits the current object into an array of RiTexts, one per letter,
   * maintaining the x and y position of each. Note: In most cases the original
   * RiText should be deleted manually to avoid a doubling effect (via
   * RiText.delete(originalRiText)).
   * 
   * @invisible
   */
  public RiText[] splitLetters(PFont pf)
  {
    
    List l = new ArrayList();
    String[] chars = splitChars();
    for (int i = 0; i < chars.length; i++)
    {
      float mx = positionForChar(pf, i);
      l.add(new RiText(_pApplet, chars[i], mx, y, pf));
    }
    return (RiText[]) l.toArray(new RiText[l.size()]);
  }

  protected String[] splitChars()
  {
    String[] chars = getText().split(ANY_CHAR_REGEX);
    while (chars[0].length() < 1)
    {
      String[] tmp = new String[chars.length - 1];
      System.arraycopy(chars, 1, tmp, 0, tmp.length);
      chars = tmp;
    }
    return chars;
  }

  /**
   * Creates an array of RiTexts laid out in lines, with one RiText for each
   * word of input, with x-spacing determined by the specified 'font'.
   */
  public static RiText[] createWords(PApplet pApplet, PFont pfont, String lines, float startX, float startY, int maxCharsPerLine, float leading)
  {
    return createWords(pApplet, pfont, new String[] { lines }, startX, startY, maxCharsPerLine, leading);
  }

  /**
   * Creates an array of RiTexts laid out in lines, with one RiText for each
   * word of input, with x-spacing determined by the default 'font'.
   */
  public static RiText[] createWords(PApplet pApplet, String lines, float startX, float startY, int maxCharsPerLine)
  {
    return createWords(pApplet, new String[] { lines }, startX, startY, maxCharsPerLine, RiPageLayout.DEFAULT_LEADING);
  }

  /**
   * Creates an array of RiTexts laid out in lines, with one RiText for each
   * word of input, with x-spacing determined by the default 'font'.
   */
  public static RiText[] createWords(PApplet pApplet, String lines, float startX, float startY, int maxCharsPerLine, float leading)
  {
    return createWords(pApplet, new String[] { lines }, startX, startY, maxCharsPerLine, leading);
  }

  /**
   * Creates an array of RiTexts laid out in lines, with one RiText for each
   * word of input, with x-spacing determined by the default 'font'.
   */
  public static RiText[] createWords(PApplet pApplet, String[] lines, float startX, float startY, int maxCharsPerLine, float leading)
  {
    return createWords(pApplet, null, lines, startX, startY, maxCharsPerLine, leading);
  }

  /**
   * Creates an array of RiTexts laid out in lines, with one RiText for each
   * word of input, with x-spacing determined by the default 'font'.
   */
  public static RiText[] createWords(PApplet pApplet, String[] lines, float startX, float startY, int maxCharsPerLine)
  {
    return createWords(pApplet, lines, startX, startY, maxCharsPerLine, RiPageLayout.DEFAULT_LEADING);
  }

  /**
   * Creates an array of RiTexts laid out in lines, with one RiText for each
   * word of input, with x-spacing determined by the specified 'font'.
   */
  public static RiText[] createWords(PApplet pApplet, PFont pfont, String[] lines, float startX, float startY)
  {
    return createWords(pApplet, pfont, lines, startX, startY, Integer.MAX_VALUE, RiPageLayout.DEFAULT_LEADING);
  }

  /**
   * Creates an array of RiTexts laid out in lines, with one RiText for each
   * word of input, with x-spacing determined by the specified 'font'.
   */
  public static RiText[] createWords(PApplet pApplet, PFont pfont, String[] lines, float startX, float startY, int maxCharsPerLine)
  {
    return createWords(pApplet, pfont, lines, startX, startY, maxCharsPerLine, RiPageLayout.DEFAULT_LEADING);
  }

  /**
   * Creates an array of RiTexts laid out in lines, with one RiText for each
   * word of input, with x-spacing determined by the default 'font'.
   */
  public static RiText[] createWords(PApplet pApplet, String[] lines, float startX, float startY)
  {
    return createWords(pApplet, lines, startX, startY, DEFAULT_LINE_BREAK_WIDTH, RiPageLayout.DEFAULT_LEADING);
  }

  /**
   * Creates an array of RiTexts laid out in lines, with one RiText for each
   * word of input, with x-spacing determined by the default 'font'.
   */
  public static RiText[] createWords(PApplet pApplet, String input, float startX, float startY)
  {
    return createWords(pApplet, new String[] { input }, startX, startY, Integer.MAX_VALUE, RiPageLayout.DEFAULT_LEADING);
  }

  public static RiText[] createWords(PApplet pApplet, String input, Rectangle rectangle)
  {
    return createWords(pApplet, null, input, rectangle);
  }

  public static RiText[] createWords(PApplet pApplet, String input, Rectangle rectangle, float leading)
  {
    return createWords(pApplet, null, input, rectangle, leading);
  }

  public static RiText[] createWords(PApplet pApplet, PFont font, String input, Rectangle rectangle)
  {
    return createWords(pApplet, font, input, rectangle, RiPageLayout.DEFAULT_LEADING);
  }

  public static RiText[] createWords(PApplet pApplet, PFont font, String input, Rectangle rectangle, float leading)
  {
    RiText[] lines = createLines(pApplet, font, input, rectangle, leading);
    RiText[] words = linesToWords(pApplet, font, lines);
    delete(lines);
    return words;
  }

  private static RiText[] linesToWords(PApplet pApplet, PFont font, RiText[] lines)
  {
    List l = new ArrayList();
    for (int i = 0; i < lines.length; i++)
    {
      RiText[] words = RiText.createWords(pApplet, font, lines[i].getText(), lines[i].x, lines[i].y);
      for (int j = 0; j < words.length; j++)
      {
        if (words[j] != null)
          l.add(words[j]);
      }
    }
    return (RiText[]) l.toArray(new RiText[l.size()]);
  }

  /**
   * Creates an array of RiTexts laid out in lines, with one RiText for each
   * word of input, with x-spacing determined by the specified 'font'.
   */
  public static RiText[] createWords(PApplet pApplet, PFont pfont, String input, float startX, float startY)
  {
    return createWords(pApplet, pfont, new String[] { input }, startX, startY, Integer.MAX_VALUE, RiPageLayout.DEFAULT_LEADING);
  }

  /**
   * Creates an array of RiTexts laid out in lines, one per array element, with
   * one RiText for each of 'words', with x-spacing determined by the specified
   * 'font'.
   * 
   * @param maxCharsPerLine
   *          set to -1 to use line breaks specified in <code>lines</code>array
   * @param leading
   *          # of pixels to add between lines in addition to the textAscent
   *          (defaults to ~2x the textAscent of the current font)
   * @return RiText[] or null if null or empty input
   */
  public static RiText[] createWords(PApplet pApplet, PFont font, String[] words, float startX, float startY, int maxCharsPerLine, float leading)
  {
    // System.out.println("RiText.createWords("+RiTa.asList(words)+", "+startX+", "+startY+", "+maxCharsPerLine+", "+
    // leading+")");
    if (words == null || words.length == 0)
      return null;

    List result = new LinkedList();
    RiText[] rlines = createLines(pApplet, font, words, startX, startY, maxCharsPerLine, leading);

    for (int i = 0; rlines != null && i < rlines.length; i++)
    {
      RiText[] rts = rlines[i].split(font, SPC, rlines[i].y);
      for (int j = 0; j < rts.length; j++)
      {
        result.add(rts[j]); // add the words
        if (font != null)
        {
          rts[j].textFont(font);
        }
      }
      delete(rlines[i]); // remove the line
    }

    return (RiText[]) result.toArray(new RiText[result.size()]);
  }

  /**
   * Enables/disables auto-drawing for this object<br>
   * 
   * @see RiText#disableAutoDraw()
   * @invisible
   */
  public void setAutoDraw(boolean b)
  {
    this.autodraw = b;
    if (autodraw)
      _pApplet.registerDraw(this);
    else
      _pApplet.unregisterDraw(this);
  }

  // -----------------------------------------------------------------------

  /**
   * Disables auto-drawing of subsequently created RiTexts.<br>
   * Note: <code>draw()</code> must be explicitly called on these objects.
   */
  public static void disableAutoDraw()
  {
    AUTODRAW = false;
  }

  /**
   * Utility method to do regex replacement on a String
   * 
   * @param patternStr
   *          regex
   * @param fullStr
   *          String to check
   * @param replaceStr
   *          String to insert
   * @see Pattern
   * @invisible
   */
  public static String regexReplace(String patternStr, String fullStr, String replaceStr)
  {
    return Regex.getInstance().replace(patternStr, fullStr, replaceStr);
  }

  /**
   * Utility method to test whether a String partially matches a regex pattern.
   * 
   * @param patternStr
   *          regex String
   * @param fullStr
   *          String to check
   * @see Pattern
   * @invisible
   */
  public static boolean regexMatch(String patternStr, String fullStr)
  {
    return Regex.getInstance().test(patternStr, fullStr);
  }

  /**
   * Sets the default font for all RiTexts to be created (via
   * PApplet.loadFont(String)).
   * <p>
   * <p>
   * Returns the PFont in case it is needed.
   * 
   * @see PApplet#loadFont(String)
   */
  public static void setDefaultFont(String defaultFontName)
  {
    DEFAULT_FONT = defaultFontName;
    //CREATE_DEFAULT_FONT = false;
  }

  /**
   * Sets the default font for all RiTexts to be created (via
   * PApplet.createFont(String)).
   * <p>
   * Note: this is generally a bad idea for web applets as the user's machine
   * must have the specified font for the sketch to function.
   * 
   * @deprecated
   * @invisible
   * @see #createDefaultFont(PApplet, String, float)
   */
  public static void createDefaultFont(String fontName, float fontSize)
  {
    DEFAULT_FONT = fontName;
    DEFAULT_FONT_SIZE = fontSize;
    //CREATE_DEFAULT_FONT = true;
  }

  /**
   * Sets the default font for all RiTexts to be created (via
   * PApplet.createFont(String)).
   * <p>
   * Note: this is not a good idea for web-applets as the user's machine must
   * have the specified font.
   * <p>
   * Note: same PApplet#createDefaultFont(String, float) but also returns the
   * created font.
   * 
   * @see PApplet#createFont(String, float)
   */
  public static PFont createDefaultFont(PApplet p, String fontName, float fontSize)
  {
    createDefaultFont(fontName, fontSize);
    return getDefaultFont(p);
  }

  /**
   * Sets the default behavior regarding whether newly created RiTexts are to
   * display their bounding boxes<br>
   * Note: this does not affect already created RiText objects
   * 
   * @invisible
   */
  public static void setDefaultBBoxVisibility(boolean show)
  {
    DEFAULT_SHOW_BOUNDING_BOXES = show;
  }

  /**
   * Sets the default behavior regarding whether newly created RiTexts are to be
   * mouse-draggable<br>
   * Note: this does not affect already created RiText objects
   * 
   * @param draggable
   * @invisible
   */
  public static void setDefaultMouseDraggable(boolean draggable)
  {
    DEFAULT_MOUSE_DRAGGABLE = draggable;
  }

  /**
   * Sets the default color for all RiTexts to be created. This does not affect
   * already created RiText objects.
   */
  public static void setDefaultColor(float r, float g, float b, float alpha)
  {
    DEFAULT_RFILL = r;
    DEFAULT_GFILL = g;
    DEFAULT_BFILL = b;
    DEFAULT_ALPHA = alpha;
  }

  public static void setDefaultColor(float gray)
  {
    setDefaultColor(gray, gray, gray, 255);
  }

  public static void setDefaultColor(float gray, float alpha)
  {
    setDefaultColor(gray, gray, gray, alpha);
  }

  public static void setDefaultColor(float r, float g, float b)
  {
    setDefaultColor(r, g, b, 255);
  }

  /**
   * Sets the default alignment for all RiTexts to be created. This does not
   * affect already created RiText objects - for this, use textMode().
   * 
   * @param defaultAlignment
   * @see #textMode(int) *
   */
  public static void setDefaultAlignment(int defaultAlignment)
  {
    DEFAULT_ALIGN = defaultAlignment;
  }

  /**
   * Sets the default motion type for all (subsequently) created RiTexts. This
   * does not affect already created RiText objects - for this, use
   * setMotionType().
   * 
   * @see #setMotionType(int)
   * @invisible
   */
  public static void setDefaultMotionType(int defaultMotionType)
  {
    DEFAULT_MOTION_TYPE = defaultMotionType;
  }

  /**
   * Returns a 2 or 3-dimensional array with the objects x,y, or x,y,z position
   * (depending on the renderer)
   * 
   * @invisible
   */
  public float[] getPosition()
  {
    if (is3D(_pApplet.g))
      return new float[] { x, y, z };
    else
      return new float[] { x, y, };
  }

  /**
   * Returns a 3-dimensional array with the objects x,y,z scale (1=100% or
   * unscaled)
   * 
   * @invisible
   */
  public float[] getScale()
  {
    return new float[] { scaleX, scaleY, scaleZ };
  }

  /**
   * Draws all (visible) RiText objects
   * @invisible
   */
  public static void drawAll(PGraphics p)
  {
    for (int i = 0; i < instances.size(); i++)
    {
      RiText rt = (RiText) instances.get(i);
      rt.draw(p);
    }
  }
  
  /**
   * Draws all (visible) RiText objects
   * @invisible
   */
  public static void drawAll() { drawAll(null); }

  // ==================== Featured delegates ============================

  /**
   * Adds a feature (a key-value pair) to the RiText.
   * 
   * @invisible
   */
  public void addFeature(CharSequence name, CharSequence value)
  {
    text.addFeature(name, value);
  }

  /**
   * Replaces all the features (key-value pairs) for the RiText with those in
   * <code>features</code>
   * 
   * @invisible
   */
  public void setFeatures(Map features)
  {
    this.text.setFeatures(features);
  }

  /**
   * Checks whether the named feature (key-value pair) exists for this RiText
   * 
   * @invisible
   */
  public boolean hasFeature(CharSequence name)
  {
    return text.hasFeature(name);
  }

  /**
   * Clears all the features (key-value pairs) for the RiText
   * 
   * @invisible
   */
  public void clearFeatures()
  {
    text.clearFeatures();
  }

  /**
   * Returns a Set of all the available feature keys for the RiText
   * 
   * @invisible
   */
  public Set getAvailableFeatures()
  {
    return text.getAvailableFeatures();
  }

  /**
   * Returns the String value for the given feature
   * 
   * @invisible
   */
  public String getFeature(CharSequence name)
  {
    return text.getFeature(name);
  }

  /**
   * Returns a Map of all the features (key-value pairs) exists for this RiText
   * 
   * @invisible
   */
  public Map getFeatures()
  {
    return text.getFeatures();
  }

  /**
   * Clears the entry for the named feature (a key-value pair)
   * 
   * @invisible
   */
  public void removeFeature(CharSequence name)
  {
    text.removeFeature(name);
  }

  /**
   * Returns the character at the specified index
   * 
   * @invisible
   */
  public char charAt(int index)
  {
    return text.charAt(index);
  }

  /**
   * Returns a new character sequence that is a subsequence of this sequence.
   * 
   * <p>
   * An invocation of this method of the form
   * 
   * <blockquote>
   * 
   * <pre>
   * str.subSequence(begin, end)
   * </pre>
   * 
   * </blockquote>
   * 
   * behaves in exactly the same way as the invocation
   * 
   * <blockquote>
   * 
   * <pre>
   * str.substring(begin, end)
   * </pre>
   * 
   * </blockquote>
   * 
   * @invisible
   */
  public CharSequence subSequence(int start, int end)
  {
    return text.subSequence(start, end);
  }

  /**
   * Sets the default color for all (subsequently) created RiTexts.
   */
  public static void setDefaultColor(float[] color)
  {
    float r = color[0], g = 0, b = 0, a = 255;
    switch (color.length)
    {
      case 4:
        g = color[1];
        b = color[2];
        a = color[3];
        break;
      case 3:
        g = color[1];
        b = color[2];
        break;
      case 2:
        g = color[0];
        b = color[0];
        a = color[1];
        break;
    }
    setDefaultColor(r, g, b, a);
  }

  /**
   * Returns the number of existing RiTexts
   * 
   * @invisible
   */
  public static int getNumInstances()
  {
    return instances.size();
  }

  /**
   * Returns the z position for this object
   * 
   * @invisible
   */
  public float getZ()
  {
    return this.z;
  }

  /**
   * Sets the z position for this object
   * 
   * @invisible
   */
  public void setZ(float z)
  {
    this.z = z;
  }

  // RiString delegates =================================

  /**
   * See rita.support.feature.Featured#appendFeature(java.lang.String,
   * java.lang.String)
   * 
   * @invisible
   */
  public void appendFeature(String name, String value)
  {
    text.appendFeature(name, value);
  }

  /**
   * @invisible See rita.RiString#compareTo(java.lang.Object)
   */
  public int compareTo(Object arg0)
  {
    return text.compareTo(arg0);
  }

  /**
   * @invisible See rita.RiString#compareTo(java.lang.String)
   */
  public int compareTo(String anotherString)
  {
    return text.compareTo(anotherString);
  }

  /**
   * @invisible See rita.RiString#compareToIgnoreCase(java.lang.String)
   */
  public int compareToIgnoreCase(String str)
  {
    return text.compareToIgnoreCase(str);
  }

  /**
   * @invisible See rita.RiString#concat(java.lang.String)
   */
  public String concat(String str)
  {
    return text.concat(str);
  }

  /**
   * @invisible See rita.RiString#contains(java.lang.CharSequence)
   */
  public boolean contains(CharSequence s)
  {
    return text.contains(s);
  }

  /**
   * @invisible See rita.RiString#endsWith(java.lang.String)
   */
  public boolean endsWith(String suffix)
  {
    return text.endsWith(suffix);
  }

  /**
   * @invisible See rita.RiString#equals(java.lang.Object) public boolean
   *            equals(Object anObject) { return text.equals(anObject); }
   */

  /**
   * @invisible See rita.RiString#equalsIgnoreCase(java.lang.String)
   */
  public boolean equalsIgnoreCase(String anotherString)
  {
    return text.equalsIgnoreCase(anotherString);
  }

  /**
   * @invisible See rita.RiString#firstIndexOf(java.lang.String)
   */
  public int firstIndexOf(String word)
  {
    return text.firstIndexOf(word);
  }

  // should these check/add the pos feature???

  /**
   * See rita.RiString#getPos()
   * 
   * @invisible
   */
  public String[] getPosArr(boolean useWordNetTags)
  {
    return text.getPosArr(useWordNetTags);
  }

  /**
   * See rita.RiString#getPos()
   * 
   * @invisible
   */
  public String[] getPosArr()
  {
    return getPosArr(false);
  }

  /**
   * Returns the part-of-speech tags, one per word, separated by WORD_BOUNDARY,
   * using the default Tokenizer & Poart-of-speech tagger. See
   * rita.RiString#getPos(java.lang.String)
   */
  public String getPos()
  {
    return getPos(false);
  }

  /**
   * See rita.RiString#getPos(java.lang.String)
   * 
   * @invisible
   */
  public String getPos(boolean useWordNetTags)
  {
    return text.getPos(useWordNetTags);
  }

  /**
   * See rita.RiString#getPosAt(int)
   * 
   * @invisible
   */
  public String getPosAt(int wordIdx, boolean useWordNetTags)
  {
    return text.getPosAt(wordIdx, useWordNetTags);
  }

  /**
   * See rita.RiString#getPosAt(int)
   * 
   * @invisible
   */
  public String getPosAt(int wordIdx)
  {
    return getPosAt(wordIdx, false);
  }

  /**
   * See rita.RiString#getWordCount()
   * 
   * @invisible
   */
  public int getWordCount()
  {
    return text.getWordCount();
  }

  /**
   * See rita.RiString#getWords()
   * 
   * @invisible
   */
  public String[] getWords()
  {
    return text.getWords();
  }

  /**
   * @invisible See rita.RiString#indexOf(int, int)
   */
  public int indexOf(int ch, int fromIndex)
  {
    return text.indexOf(ch, fromIndex);
  }

  /**
   * @invisible See rita.RiString#indexOf(int)
   */
  public int indexOf(int ch)
  {
    return text.indexOf(ch);
  }

  /**
   * @invisible See rita.RiString#indexOf(java.lang.String, int)
   */
  public int indexOf(String str, int fromIndex)
  {
    return text.indexOf(str, fromIndex);
  }

  /**
   * See rita.RiString#indexOf(java.lang.String)
   * 
   * @invisible
   */
  public int indexOf(String str)
  {
    return text.indexOf(str);
  }

  /**
   * @invisible See rita.RiString#insertWordAt(java.lang.String, int)
   */
  public boolean insertWordAt(String newWord, int wordIdx)
  {
    return text.insertWordAt(newWord, wordIdx);
  }

  /**
   * @invisible See rita.RiString#lastIndexOf(int, int)
   */
  public int lastIndexOf(int ch, int fromIndex)
  {
    return text.lastIndexOf(ch, fromIndex);
  }

  /**
   * @invisible See rita.RiString#lastIndexOf(int)
   */
  public int lastIndexOf(int ch)
  {
    return text.lastIndexOf(ch);
  }

  /**
   * @invisible See rita.RiString#lastIndexOf(java.lang.String, int)
   */
  public int lastIndexOf(String str, int fromIndex)
  {
    return text.lastIndexOf(str, fromIndex);
  }

  /**
   * @invisible See rita.RiString#lastIndexOf(java.lang.String)
   */
  public int lastIndexOf(String str)
  {
    return text.lastIndexOf(str);
  }

  /**
   * See rita.RiString#matches(java.lang.String)
   */
  public boolean matches(String regex)
  {
    return text.matches(regex);
  }

  /**
   * @invisible See rita.RiString#regionMatches(boolean, int, java.lang.String,
   *            int, int)
   */
  public boolean regionMatches(boolean ignoreCase, int toffset, String other, int ooffset, int len)
  {
    return text.regionMatches(ignoreCase, toffset, other, ooffset, len);
  }

  /**
   * @invisible See rita.RiString#replace(char, char)
   */
  public String replace(char oldChar, char newChar)
  {
    return text.replace(oldChar, newChar);
  }

  /**
   * See rita.RiString#replace(java.lang.CharSequence, java.lang.CharSequence)
   */
  public String replace(CharSequence target, CharSequence replacement)
  {
    return text.replace(target, replacement);
  }

  /**
   * See rita.RiString#replace(java.lang.String, java.lang.String)
   */
  public void replace(String oldText, String newText)
  {
    text.replace(oldText, newText);
  }

  /**
   * See rita.RiString#replaceAll(java.lang.String, java.lang.String)
   * 
   * @invisible
   */
  public String replaceAll(String regex, String replacement)
  {
    return text.replaceAll(regex, replacement);
  }

  // -----

  /**
   * @invisible Randomly chooses from all words matching <code>pos</code> and
   *            replaces a random one with <code>newWord</code>. Returns the
   *            replaced word, or null if if no substitution could be made.
   */
  public String replaceByPos(String newWord, String pos)
  {
    return text.replaceByPos(newWord, pos);
  }

  /**
   * @invisible See rita.RiString#replaceFirst(java.lang.String,
   *            java.lang.String)
   */
  public String replaceFirst(String regex, String replacement)
  {
    return text.replaceFirst(regex, replacement);
  }

  /**
   * See rita.RiString#replaceWordAt(java.lang.String, int)
   * 
   * @invisible
   */
  public boolean replaceWordAt(String newWord, int wordIdx)
  {
    return text.replaceWordAt(newWord, wordIdx);
  }

  /**
   * @invisible See rita.RiString#setString(java.lang.CharSequence[])
   */
  public void setString(CharSequence[] words)
  {
    text.setString(words);
  }

  /**
   * @invisible See rita.RiString#setString(java.lang.String)
   */
  public void setString(String newWord)
  {
    text.setString(newWord);
  }

  /**
   * @invisible See rita.RiString#startsWith(java.lang.String, int)
   */
  public boolean startsWith(String prefix, int toffset)
  {
    return text.startsWith(prefix, toffset);
  }

  /**
   * @invisible See rita.RiString#startsWith(java.lang.String)
   */
  public boolean startsWith(String prefix)
  {
    return text.startsWith(prefix);
  }

  /**
   * @invisible See rita.RiString#substring(int, int)
   */
  public String substring(int beginIndex, int endIndex)
  {
    return text.substring(beginIndex, endIndex);
  }

  /**
   * @invisible See rita.RiString#substring(int)
   */
  public String substring(int beginIndex)
  {
    return text.substring(beginIndex);
  }

  /**
   * @invisible See rita.RiString#toCharArray()
   */
  public char[] toCharArray()
  {
    return text.toCharArray();
  }

  /**
   * @invisible See rita.RiString#toLowerCase()
   */
  public String toLowerCase()
  {
    return text.toLowerCase();
  }

  /**
   * 
   * @invisible See rita.RiString#toLowerCase(java.util.Locale)
   */
  public String toLowerCase(Locale locale)
  {
    return text.toLowerCase(locale);
  }

  /**
   * @invisible See rita.RiString#toUpperCase()
   */
  public String toUpperCase()
  {
    return text.toUpperCase();
  }

  /**
   * 
   * @invisible See rita.RiString#toUpperCase(java.util.Locale)
   */
  public String toUpperCase(Locale locale)
  {
    return text.toUpperCase(locale);
  }

  /**
   * @invisible See rita.RiString#trim()
   */
  public String trim()
  {
    setText(text.trim());
    return getText();
  }

  // rotate and scale methods =======================

  /**
   * Sets the z-rotation for the object
   * 
   * @invisible
   */
  public void rotateZ(float rotate)
  {
    this.rotateZ = rotate;
  }

  /** 
   * Rotate the object via affine transform. This is same as rotateZ, but for 2D
   *  
   */
  public void rotate(float rotate)
  {
    this.rotateZ = rotate;
  }

  /**
   * Sets the x-rotation for the object
   * 
   * @invisible
   */
  public void rotateX(float rotate)
  {
    this.rotateX = rotate;
  }

  /** 
   * Sets the y-rotation for the object 
   * @invisible 
   */
  public void rotateY(float rotate)
  {
    this.rotateY = rotate;
  }

  /** 
   * Sets the x-scale for the object
   *  @invisible 
   */
  public void scaleX(float scale)
  {
    this.scaleX = scale;
  }

  /** 
   * Sets the y-scale for the object 
   * @invisible 
   */
  public void scaleY(float scale)
  {
    this.scaleY = scale;
  }

  /** 
   * Sets the z-scale for the object 
   * @invisible 
   */
  public void scaleZ(float scale)
  {
    this.scaleZ = scale;
  }

  /** 
   * Uniformly scales the object on all dimensions (x,y,z) 
   */
  public void scale(float scale)
  {
    scaleX = scaleY = scaleZ = scale;
  }

  /**
   *  Scales the object on all dimensions (x,y,z)
   *  @invisible
   */
  public void scale(float sX, float sY, float sZ)
  {
    scale(new float[] { sX, sY, sZ });
  }

  /** 
   * Scales the object on either 2 or 3 dimensions (x,y,[z])
   * @invisible 
   */
  public void scale(float[] scales)
  {
    if (scales.length < 2)
      throw new RiTaException("scale(float[]) requires at least 2 values!");

    if (scales.length > 1)
    {
      scaleX = scales[0];
      scaleY = scales[1];
    }

    if (scales.length > 2)
      scaleZ = scales[2];
  }

  // ========================================================

  /**
   * Returns the distance between the center points of the two RiTexts.
   */
  public float distanceTo(RiText riText)
  {
    Point2D p1 = getCenter();
    Point2D p2 = riText.getCenter();
    return PApplet.dist((float) p1.getX(), (float) p1.getY(), (float) p2.getX(), (float) p2.getY());
  }

  /**
   * Deletes the character at at the specified character index ('idx'). If the
   * specified 'idx' is less than xero, or beyond the length of the current
   * text, there will be no effect. Returns true if the deletion was successful,
   * else false
   * @invisible
   */
  public boolean removeCharAt(int idx)
  {
    return replaceCharAt(idx, null);
  }

  /**
   * Replaces the character at the specified character index ('idx') with the
   * 'replaceWith' character.
   * <p>
   * If the specified 'idx' is less than zero, or beyond the length of the
   * current text, there will be no effect. Returns true if the replacement was
   * made
   * @invisible
   */
  public boolean replaceCharAt(int idx, char replaceWith)
  {
    return replaceCharAt(idx, Character.toString(replaceWith));
  }

  /**
   * Inserts the character at the specified character index ('idx'). If the
   * specified 'idx' is less than zero, or beyond the length of the current
   * text, there will be no effect. Returns true if the insertion was made.
   * @invisible
   */
  public boolean insertCharAt(int idx, char toInsert)
  {
    return insertAt(idx, Character.toString(toInsert));
  }

  /**
   * Inserts the 'toInsert' String at the desired character index ('idx'). If
   * the specified 'idx' is less than zero, or beyond the length of the current
   * text, there will be no effect. Returns true if the insertion was made.
   * @invisible
   */
  public boolean insertAt(int idx, String toInsert)
  {
    if (idx < 0 || idx > length())
      return false;

    String s = getText();
    String beg = s.substring(0, idx);
    String end = s.substring(idx);
    String s2 = null;
    if (toInsert != null)
      s2 = beg + toInsert + end;
    else
      s2 = beg + end;

    if (s2.equals(s))
      return false; // no change

    setText(s2);

    return true;
  }

  /*
   * TODO: add lazy(er) updates
   */
  protected void updateBoundingBox(PGraphics p)
  {
    verifyFont(); // need this here (really!)

    if (boundingBox == null)
      boundingBox = new Rectangle2D.Float();

    float bbw = textWidth() + (2 * bbPadding) + (1.8f * (bbStrokeWeight - 1) + 2);
    float bbh = _pApplet.textAscent() + _pApplet.textDescent() + (2 * bbPadding)
        + (1.8f * (bbStrokeWeight - 1) + 2);

    // offsets from RiText.x/y
    float bbx = -bbw / 2f;
    float bby = (-(bbStrokeWeight - 1) + -_pApplet.textAscent() - bbPadding) - 2;

    switch (textAlignment)
    {
      case LEFT:
        bbx += textWidth() / 2f + bbPadding / 2f;
        break;
      case CENTER: // ok as is
        break;
      case RIGHT:
        bbx -= textWidth() / 2f + bbPadding / 2f;
        break;
    }

    if (boundingBox != null)
      boundingBox.setRect(bbx, bby, bbw, bbh);
  }

  /**
   * Replaces the character at 'idx' with 'replaceWith'. If the specified 'idx'
   * is less than xero, or beyond the length of the current text, there will be
   * no effect. Returns true if the replacement was made.
   * @invisible
   */
  public boolean replaceCharAt(int idx, String replaceWith)
  {
    return text.replaceCharAt(idx, replaceWith);
  }

  /**
   * Turns off warnings about conflicting or non-terminating behaviors.
   * 
   * @invisible
   */
  public static void disableBehaviorWarnings()
  {
    behaviorWarningsDisabled = false;
  }

 /**
  * @invisible
  * Returns whether the bounding box is currently showing
  */
  public boolean isShowingBoundingBox()
  {
    return boundingBoxVisible;
  }

  // ughh, need to rethink, maybe all features?
  /**
   * Returns a field for field copy of <code>toCopy</code>
   * 
   * @invisible
   */
  public static RiText copy(RiText toCopy)
  {

    RiText rt = new RiText(toCopy.getPApplet());

    rt.sample = toCopy.sample;
    rt.font = toCopy.font;

    rt.behaviors = toCopy.behaviors; // deep or shallow?

    rt.text = new RiString(toCopy.text);
    rt.x = toCopy.x;
    rt.y = toCopy.y;
    rt.z = toCopy.z;
    rt.autodraw = toCopy.autodraw;
    rt.fillR = toCopy.fillR;
    rt.fillG = toCopy.fillG;
    rt.fillB = toCopy.fillB;
    rt.fillA = toCopy.fillA;
    rt.bbFillR = toCopy.bbFillR;
    rt.bbFillG = toCopy.bbFillG;
    rt.bbFillB = toCopy.bbFillB;
    rt.bbFillA = toCopy.bbFillA;
    rt.bbStrokeR = toCopy.bbStrokeR;
    rt.bbStrokeG = toCopy.bbStrokeG;
    rt.bbStrokeB = toCopy.bbStrokeB;
    rt.bbStrokeA = toCopy.bbStrokeA;
    rt.bbsStrokeR = toCopy.bbsStrokeR;
    rt.bbsStrokeG = toCopy.bbsStrokeG;
    rt.bbsStrokeB = toCopy.bbsStrokeB;
    rt.bbsStrokeA = toCopy.bbsStrokeA;
    rt.bbStrokeWeight = toCopy.bbStrokeWeight;
    rt.textAlignment = toCopy.textAlignment;
    rt.fontSize = toCopy.fontSize;
    rt.imageXOff = toCopy.imageXOff;
    rt.imageYOff = toCopy.imageYOff;
    rt.imageWidth = toCopy.imageWidth;
    rt.imageHeight = toCopy.imageHeight;
    rt.mouseDraggable = toCopy.mouseDraggable;
    rt.boundingBoxVisible = toCopy.boundingBoxVisible;
    rt.bbPadding = toCopy.bbPadding;
    rt.motionType = toCopy.motionType;
    rt.mouseXOff = toCopy.mouseXOff;
    rt.mouseYOff = toCopy.mouseYOff;
    rt.hidden = toCopy.hidden;

    rt.scaleX = toCopy.scaleX;
    rt.scaleY = toCopy.scaleY;
    rt.scaleZ = toCopy.scaleZ;

    rt.rotateX = toCopy.rotateX;
    rt.rotateY = toCopy.rotateY;
    rt.rotateZ = toCopy.rotateZ;

    // add the features
    Map m = toCopy.getFeatures();
    for (Iterator it = m.keySet().iterator(); it.hasNext();)
    {
      CharSequence key = (CharSequence) it.next();
      CharSequence val = (CharSequence) m.get(key);
      rt.addFeature(key, val);
    }
    return rt;
  }

  public static void main(String[] args)
  {
    String s = "Bubble Gum";
    RiText ri = new RiText(null, s, 10, 10);
    RiAnalyzer ra = new RiAnalyzer(null);
    ra.analyze(ri);
    System.out.println(ri.getFeatures());
    /*
     * for (int i = 0; i <= s.length(); i++) { ri.insertCharAt(i,'a');
     * System.out.println("'"+ri.getText()+"'"); ri.setText(s); }
     */
  }

}// end
