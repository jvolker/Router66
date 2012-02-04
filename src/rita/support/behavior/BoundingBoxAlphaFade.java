package rita.support.behavior;

import rita.RiTa;
import rita.RiText;

// fades fill and stroke of bounding box
public class BoundingBoxAlphaFade extends InterpolatingBehavior
{
  public BoundingBoxAlphaFade(RiText rt, float newAlpha, float duration) {
    this(rt, newAlpha, 0, duration);
  }
  
  public BoundingBoxAlphaFade(RiText rt, float newAlpha, float startTime, float duration) {
    super(rt, startTime, duration);    
    float[] fill = rt.getBoundingBoxFill();
    float[] stroke = rt.getBoundingBoxStroke();
    this.interpolater = new RiInterpolater2D(new float[] { fill[3], stroke[3] }, 
      new float[] { newAlpha, newAlpha }, toOffsetMs(startTime), toMs(duration));
    //System.out.println("BoundingBoxAlphaFade("+rt+", ["+fill[3]+","+stroke[3] +"], ["+newAlpha+","+newAlpha+"], "+startTime+", "+duration+")");
    this.type = BOUNDING_BOX_ALPHA;
  }
  
  public void getStartValueFromParent(RiText parent, Interpolater interpolater) {
    float[] bbf = rt.getBoundingBoxFill();
    float[] bbs = rt.getBoundingBoxStroke();
    interpolater.setStart(new float[]{ bbf[3], bbs[3] });
  }  
  
  public void updateParentValues(RiText rt, float[] values) 
  {
    //fill
    float[] bbf = rt.getBoundingBoxFill();
    bbf[3] = values[0];
    rt.boundingBoxFill(bbf);
    
    //stroke
    float[] bbs = rt.getBoundingBoxStroke();
    bbs[3] = values[0];
    rt.boundingBoxStroke(bbs);
  }

}// end

