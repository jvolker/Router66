package rita.support.behavior;

import rita.RiText;
import rita.support.RiConstants;

public class ScaleBehavior extends InterpolatingBehavior
{
  public ScaleBehavior(RiText rt, float scaleXYZ, float duration) {
    this(rt, new float[]{scaleXYZ,scaleXYZ,scaleXYZ}, 0, duration);
  }
  
  public ScaleBehavior(RiText rt, float scaleXYZ, float startTimeOffset, float duration) {
    this(rt, new float[]{scaleXYZ, scaleXYZ, scaleXYZ}, startTimeOffset, duration);
  }
  
  public ScaleBehavior(RiText rt, float[] scaleXYZ, float duration) 
  {
    this(rt, scaleXYZ, 0, duration);
  }

  public ScaleBehavior(RiText rt, float[] scaleXYZ, float startTimeOffset, float duration) 
  {
    super(rt, startTimeOffset, duration); 
    this.setType(RiConstants.SCALE_TO);
    this.interpolater = new RiInterpolater3D
      (rt.getScale(), scaleXYZ, toOffsetMs(startOffset), (int)(duration*1000));  
  }
  
  public void getStartValueFromParent(RiText parent, Interpolater interpolater) {
    //System.out.println("ScaleBehavior.getStartValueFromParent("+RiTa.asList(parent.getScale())+") @ "+RiTa.millis());
    interpolater.setStart(parent.getScale());
  }  
  
  public void updateParentValues(RiText rt, float[] values) {
     //System.out.println(this+".updateParentValues("+RiTa.asList(values)+")");
     rt.scale(values);
  }

}// end

