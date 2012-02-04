package rita;

import processing.core.PApplet;
import krister.Ess.*;
 
/**
 * An implementation of RiSample using the Ess audio library. 
 * @invisible 
 */
public class EssSamplePlayer extends RiSample
{
  AudioChannel delegate;  
  
  /**  @invisible   */
  public EssSamplePlayer(PApplet p) 
  {
    super(p);
    Ess.start(p);
    instances.add(this);
    //System.err.println("ESS.parent: "+Ess.parent.getClass());
  }

  public void load(String sample, float initialVol) 
  { 
    this.delegate = new AudioChannel(sample);
    if (delegate == null || delegate.samples==null)
      throw new RiTaException("[ERROR] "+getClass()+" unable to load sample: "+sample);
    this.delegate.volume(initialVol);
  }
  
  public void load(String sample) 
  { 
    this.load(sample, DEFAULT_VOLUME);
  }

  
  public void play()
  {    
    this.delegate.play();
  }

  public float getVolume()
  {
    return this.delegate.volume;
  }

  public void loop()
  {    
    this.delegate.play(Ess.FOREVER);    
  }

  public void setVolume(float v)
  {
    this.delegate.volume(v);
  }

  public void stop()
  {   
    if (delegate != null)
      this.delegate.stop();
  }
   
  public void dispose()
  {
    try
    {
      this.delete();
      Ess.stop();
    }
    catch (RuntimeException e) {}
  }
  
  public void delete()
  {
    if (delegate != null) {
      delegate.stopFade();
      delegate.stopPan();
      delegate.stop();
    }
  }

  public void mute(boolean bool)
  {
    if (delegate != null)
      delegate.mute(bool);
  }

  /**
   * pan value 1- to 1;
   */
  public void pan(float f)
  {
    delegate.pan(f);
  }
  
  public float getPan()
  {
    return delegate.pan;
  }

  public void pause()
  {
    if (delegate != null)
      delegate.pause();
  }

  public void loop(int i)
  {
    delegate.play(i);    
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
  
  public void fadeVolume(float v, float sec)
  {
    delegate.fadeTo(v, (int)sec*1000);
  }

  public void pan(float f, float sec)
  {
    delegate.panTo(f, (int)sec*1000);
  }

  public void stopFade()
  {
    if (delegate != null)
      delegate.stopFade();
  }

  public void stopPan()
  {
    if (delegate != null)
      delegate.stopPan();
  }

  public boolean isPlaying()
  {
    if (delegate == null) return false;
    return delegate.state == Ess.PLAYING;
  }

  public int getCurrentFrame()
  {
    return delegate.getCurrentPlayFrame();
  }

  public int getNumFrames()
  {   
    return delegate.size;
  }

  public void setCurrentFrame(int frame)
  {
    this.delegate.cue(frame);    
  }

  public boolean isLooping()
  {
    if (delegate == null) return false;
    return delegate.loop > 0;
  }
  
}// end
