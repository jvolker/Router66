package rita.support.gui;

public interface TextField
{
  /**
   * @invisible
   */
  public abstract void draw();

  /** Sets the visibility for this object  */
  public abstract void setVisible(boolean visible);

  /**
   * Set the fg color for this object
   * @param r red component (0-255)
   * @param g green component (0-255)
   * @param b blue component (0-255)
   * @param alpha transparency (0-255)
   */
  public abstract void setFgColor(float r, float g, float b, float alpha);
  public abstract void setFgColor(float gray);
  public abstract void setFgColor(float gray, float alpha);
  public abstract void setFgColor(float r, float g, float b);

  //public abstract void setFgColor(float[] color);

  /**
   * Set the active color for this object
   * @param r red component (0-255)
   * @param g green component (0-255)
   * @param b blue component (0-255)
   * @param alpha transparency (0-255)
   */
  public abstract void setActiveColor(float r, float g, float b, float alpha);
  public abstract void setActiveColor(float gray);
  public abstract void setActiveColor(float gray, float alpha);
  public abstract void setActiveColor(float r, float g, float b);

  //public abstract void setActiveColor(float[] color);

  /**
   * Set the bg color for this object
   * @param r red component (0-255)
   * @param g green component (0-255)
   * @param b blue component (0-255)
   * @param alpha transparency (0-255)
   */
  public abstract void setBgColor(float r, float g, float b, float alpha);
  public abstract void setBgColor(float gray);
  public abstract void setBgColor(float gray, float alpha);
  public abstract void setBgColor(float r, float g, float b);

  //public abstract void setBgColor(float[] color);

}