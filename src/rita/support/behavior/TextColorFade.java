package rita.support.behavior;

import rita.RiText;

public class TextColorFade extends InterpolatingBehavior
{
  public TextColorFade(RiText rt, float[] colors, float duration) {
    this(rt, colors, 0, duration);
  }
  
  public TextColorFade(RiText rt, float[] colors, float startTime, float duration) {
    super(rt, startTime, duration);
    
    this.interpolater = new RiInterpolater4D
      (rt.getColor(), colors, toOffsetMs(startTime), toMs(duration));
  }

  public void getStartValueFromParent(RiText parent, Interpolater interpolater) {
    interpolater.setStart(parent.getColor());
  }  
  
  public void updateParentValues(RiText rt, float[] values) {
     rt.fill(values);
  }

}// end

