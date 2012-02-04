package rita;

import pitaru.sonia_v2_9.*;
import processing.core.PApplet;
 
// need to register the update(), no?
/**
 * An implementation of RiSample using the Sonias audio library. 
 * @invisible 
 */
public class SoniaSamplePlayer extends RiSample
{
  static final boolean DBUG = false;
  
  public Sample delegate;  
  public Lerp volumeFade, panFade;
  
  public float lastVolume=.5f;
  
  /**  @invisible   */
  public SoniaSamplePlayer(PApplet p) 
  {
    super(p);
    if (DBUG)System.err.println("SoniaSamplePlayer.init<>");
    Sonia.start(p); 
  }
  
  public void dispose()
  {    
    try
    {
      this.delete();
      Sonia.stop();
    }
    catch (RuntimeException e) {}
  }
  
  public void load(String sample) 
  { 
    this.load(sample, DEFAULT_VOLUME);
  }
  
  public void load(String sample, float initialVol) 
  { 
    if (DBUG)System.err.println("SoniaSamplePlayer.load("+sample+")");
    delegate = new Sample(sample); 
    if (delegate == null) 
      throw new RiTaException("Unable to load sample with Sonia: "+sample);
    delegate.setVolume(initialVol);
  }

  public void play()
  {    
    if (DBUG)System.err.println("SoniaSamplePlayer.play(vol="+getVolume()+")");
    this.delegate.play();
  }

  public float getVolume()
  {
    if (delegate == null) return 0;
    return this.delegate.getVolume();   
  }

  public void loop()
  {    
    this.delegate.repeat();
  }

  public void setVolume(float v)
  {
    //System.err.println("setV("+v+")");
    this.delegate.setVolume(v);
  }
  
  public void stop()
  {   
    if (DBUG)System.err.println("SoniaSamplePlayer.stop()");
    if (delegate != null) 
      this.delegate.stop(10);
  }
  
  public void delete()
  {
    if (DBUG) System.err.println("SoniaSamplePlayer.dispose");
    this.stop();
    if (delegate!=null)
      delegate.delete();
  }

  public void mute(boolean bool)
  {
    if (delegate == null) return; 
    if (bool) {
      this.lastVolume = getVolume();
      this.delegate.setVolume(0);
    }
    else {
      this.delegate.setVolume(lastVolume);
    }
  }

  /**
   * pan value 1- to 1;
   */
  public void pan(float f)
  {
    this.delegate.setPan(f);
  }
  
  public float getPan()
  {
    return this.delegate.getPan();
  }

  public void pause()
  {
    if (delegate != null) 
      this.delegate.stop();
  }

  public void loop(int i)
  {
    this.delegate.repeatNum(i);    
  }

  public void loop(String s)
  {
    load(s);
    loop();    
  }

  public void play(String s)
  {
    load(s);
    play(); 
  }

  public void update(/*int millis*/) { // need this? yes, for lerps?
    int millis = RiTa.millis();
    if (!delegate.isPlaying()) return;
    if (volumeFade != null)
      setVolume(volumeFade.update(millis)[0]);
    if (panFade != null)
      pan(panFade.update(millis)[0]);
  }
  
  public void fadeVolume(float targetVolume, float sec)
  {
    if (volumeFade == null) 
      volumeFade = new Lerp(_pApplet);
    volumeFade.setTarget(getVolume(), targetVolume, sec/*, PText.EVENT_VOLUME_FADE*/);  
  }

  public void pan(float targetPan, float sec)
  {
    //throw new RuntimeException("[ERROR] Method not implemented for SoniaSamplePlayer!");
    if (panFade == null) 
      panFade = new Lerp(_pApplet);
    panFade.setTarget(getPan(), targetPan, sec/*, PText.EVENT_VOLUME_FADE*/);  
  }

  public void stopFade()
  {
    //throw new RuntimeException("[ERROR] Method not implemented for SoniaSamplePlayer!");
    if (volumeFade != null)
      this.volumeFade.reset();
  }

  public void stopPan()
  {
    //throw new RuntimeException("[ERROR] Method not implemented for SoniaSamplePlayer!");
    if (panFade != null)
      this.panFade.reset();
  }
  
  public boolean isPlaying()
  {
    if (delegate == null) return false;
    return this.delegate.isPlaying();
  }

  public int getCurrentFrame()
  {   
    if (delegate == null) return 0;
    return this.delegate.getCurrentFrame();
  }

  public int getNumFrames()
  {
    if (delegate == null) return 0;
    return this.delegate.getNumFrames();
  }
  
  public void setCurrentFrame(int frame)
  {   
    if (1==1) throw new RuntimeException("implement me");
    this.delegate.play(frame, getNumFrames());    
  }
  
  public boolean isLooping()
  {       
    if (delegate == null) return false;
    return (delegate.state == 2);
  }

  
  
}// end

class Lerp
{
  public static boolean DBUG = false;

  float[] data, targetData, initialData;
  long lerpStart, lerpElapsed, lerpTotal;
  int startTimeStamp, type;
  boolean completed;
  public PApplet pApplet;
  
  public Lerp(PApplet p) {
    this.pApplet = p;
  }  

  /**
   * Sets new lerp target
   * @param _data
   * @param _target 
   * @param _startTimeOffset - time offset?? (in seconds) at which lerp should start
   * @param duration - time (in seconds) for lerp to occur 
   
   * @return true if this is a new target, else false.
   */
  public boolean setTarget
    (float[] _data, float[] _target, float _startTimeOffset, float duration)
  {  
    int startTimeTmp = (int)_startTimeOffset*1000;
    int durationTmp = (int)(duration*1000);   
    
/*    if (DBUG) System.err.println(pApplet.millis()+
        ": Lerp.setTarget(["+_data[0]+","+_target[0]+", "+startTimeTmp+", "+duration+")");
    */
    if (_data.length != _target.length) 
      throw new RuntimeException("Bad call to setTarget()"
        + " -> initialData.length != targetData.length");
        
    // initialize our data arrays
    if (data == null || data.length != _data.length) 
    {
      this.data = new float[_data.length]; 
      this.initialData = new float[_data.length];
      this.targetData = new float[_data.length];
    }
    
    System.arraycopy(_data, 0, data, 0, data.length);
    System.arraycopy(_data, 0, initialData, 0, data.length);
    System.arraycopy(_target, 0, targetData, 0, data.length);
            
    this.lerpStart = -1;
    this.lerpElapsed = 0; // compute time as offset
    this.startTimeStamp = pApplet.millis()+startTimeTmp;
    this.lerpTotal = durationTmp;  
    this.completed = false;
    
    return true; // we've set a new target
  } 
  
  public boolean setTarget(float x, float tX, float duration)
  {
    return this.setTarget(new float[]{x}, new float[] {tX}, duration);
  }
  
  /* (non-Javadoc)
   * @see rita.util.Interp#setTarget(float, float, float, float)
   */
  public boolean setTarget(float x, float tX, float startTimeOffset, float duration) 
  {
    return this.setTarget(new float[]{x}, new float[] {tX}, startTimeOffset, duration);
  }
  
  public boolean setTarget(float[] _data, float[] _target, float duration)
  {
    return this.setTarget(_data, _target, -1, duration);
  }
  
  /* (non-Javadoc)
   * @see rita.util.Interp#update(int)
   */
  public float[] update(int time) 
  {        
    // if we're not started and not in the future, start
    if (lerpStart < 0 && !inFuture(startTimeStamp))
      lerpStart = time;
    
    // if we're not started or don't have a target, ignore
    if (lerpStart < 0 || targetData == null) return null;
    
    // get the elapsed time for this lerp
    lerpElapsed = time - lerpStart;    
    
    float amt = Math.min(1, (lerpElapsed/(float)lerpTotal));
    
    if (amt == 1) { // we've run out of time
      //System.out.prfloatln("amt=1 start="+lerpStart+" elapsed="+lerpElapsed/1000f);
      for (int i = 0; i < data.length; i++)
        data[i] = targetData[i];
      
      if (!completed) {
        //System.out.println("Lerp.completed....");
        completed = true;            
      }
    } 
    else {
      for (int i = 0; i < data.length; i++)  
        this.data[i] = PApplet.lerp(initialData[i], targetData[i], amt);
      
      if (DBUG) System.err.println("lerped data[0] to"+data[0]);
    }         
    return data;
  }
  
  protected final float lerp(float start, float stop, float amt) {
    return start + (stop-start) * amt;
  }

  /** @return true if its in the future, else false */
  private boolean inFuture(int startTime)
  {
    return startTime > pApplet.millis();
  }

  public void reset()
  {
    targetData = null;
  }

  /* (non-Javadoc)
   * @see rita.util.Interp#isCompleted()
   */
  public boolean isCompleted()
  {
    return completed;
  }
  
}// end

