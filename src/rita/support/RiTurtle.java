package rita.support;

import java.awt.geom.Point2D;

import processing.core.PApplet;

public class RiTurtle
{
  PApplet pApplet;
  String toDraw;    
  float length, theta, radius;
  float strokeWeight, color[];
  
  public RiTurtle(PApplet p, float l, float t, float sw) {
    this(p, null, l, t, sw);
  }
  
  public RiTurtle(PApplet p, String s, float len, float thta, float sw) {
    pApplet = p;
    pApplet.smooth();
    toDraw = s;
    length = len;
    theta = thta;
    strokeWeight = sw;
  }
    
  public void draw(String s) 
  {
    if (s != null) this.setToDraw(s);       
    
    if (toDraw == null) return;
    
    if (color == null) setRandomColor();       
    
    PApplet p = pApplet; 
    for (int i = 0; i < toDraw.length(); i++)
    {
      char c = toDraw.charAt(i);
      
      if (c == 'F' || c == 'G' || c=='f' || c=='g') 
        drawLine(p);
 
      else if (c == '+')
        p.rotate(theta);
 
      else if (c == '-')
        p.rotate(-theta);
 
      else if (c == '[')
        p.pushMatrix();
 
      else if (c == ']')
        p.popMatrix(); 
    }
    
  }

  private void setRandomColor() {
    color = new float[] { pApplet.random(100,150), 
        pApplet.random(100,200), 255, pApplet.random(32,64) };
  }

  private Point2D drawLine(PApplet p)
  {
    float screenX = p.g.screenX(0, 0);    
    float screenY = p.g.screenY(0, 0);
    Point2D pt = new Point2D.Float(screenX, screenY);
    p.strokeCap(0);
    if (screenY < p.height+length) {   
      p.strokeWeight(strokeWeight);      
      p.stroke(color[0],color[1],color[2], color[3]);     
      p.line(0, 0, length, 0);
    }
    p.translate(length, 0);
    return pt;
  }
    
  public void setLength(float l) {
    length = l;
  }
  
  public void strokeWeight(float l) {
    strokeWeight = l;
  }

  public void changeLength(float percent) {
    length *= percent;
  }
  
  public void changeStrokeWeight(float percent) {
    strokeWeight *= percent;
  }

  public void setToDraw(String s) {
    toDraw = s;
  }
  
  public void setTheta(float angleInRadians) {
    theta = angleInRadians;
  }
  
  public void changeTheta(float percent) {
    theta = (theta * percent);
  }

  public float[] getColors() {
    return this.color;
  }

  public void setColors(float[] hsba) {
    this.color = hsba;
  }
  
}// end