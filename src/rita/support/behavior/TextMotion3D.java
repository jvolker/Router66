package rita.support.behavior;

import rita.RiText;

public class TextMotion3D extends TextMotion
{
  public TextMotion3D(RiText rt, float x, float y, float z, float duration) {
    this(rt, new float[]{x,y,z}, 0, duration);
  }
  
  public TextMotion3D(RiText rt, float[] targetXYZ, float duration) 
  {
    this(rt, targetXYZ, 0, duration);
  }

  public TextMotion3D(RiText rt, float[] targetXYZ, float startTimeOffset, float duration) 
  {
    super(rt, startTimeOffset, duration); 
    this.interpolater = new RiInterpolater3D
      (rt.getPosition(), targetXYZ, toOffsetMs(startOffset), (int)(duration*1000));  
    setMotionType(rt.motionType); 
    setType(MOVE);
  }


  public void getStartValueFromParent(RiText parent, Interpolater interpolater) {
    interpolater.setStart(parent.getPosition());
  }  
  
  public void updateParentValues(RiText rt, float[] values) {
     rt.setPosition(values[0], values[1], values[2]);
  }


}// end

