package rita.support.behavior;

import rita.RiTa;
import rita.RiText;

public class TextMotion2D extends TextMotion 
{      
  public TextMotion2D(RiText rt, float[] targetXY, float duration) 
  {
    this(rt, targetXY, 0, duration);
  }
  
  public TextMotion2D(RiText rt, float newX, float newY, float duration) 
  {
    this(rt, new float[]{newX, newY}, 0, duration);
  }

  public TextMotion2D(RiText rt, float[] targetXY, float startTimeOffset, float duration) 
  {
    super(rt, startTimeOffset, duration); 
    this.interpolater = new RiInterpolater2D
      (rt.getPosition(), targetXY, toOffsetMs(startOffset), (int)(duration*1000));  
    setMotionType(rt.motionType); 
    setType(MOVE);
  }
      
  public void updateParentValues(RiText rt, float[] values) {
     rt.setPosition(values[0], values[1]);
  }

  public void getStartValueFromParent(RiText parent, Interpolater interpolater) {
    interpolater.setStart(parent.getPosition());
  }

  public static void main(String[] args) throws InterruptedException
  {
    long startTime = System.currentTimeMillis();
    RiText rt = new RiText(null, "hello", 500, 50);
    TextMotion2D e = new TextMotion2D(rt, new float[] { 0, 0 }, 2f);
    while (!e.isCompleted()) {    
      Thread.sleep(30);
      e.update();
      System.out.println(rt.getX()+","+rt.getY());
    }   
    System.out.println(rt.getX()+","+rt.getY()+" "+RiTa.millis(startTime));  
  }

}// end

