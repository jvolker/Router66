package rita;

import javax.sound.sampled.FloatControl;
import javax.sound.sampled.FloatControl.Type;

import processing.core.PApplet;
import ddf.minim.*;

/**
 * An implementation of RiSample using the Minim audio library. 
 * @invisible 
 */
public class MinimSamplePlayer extends RiSample
{
  private static final boolean DBUG = false;
  private static boolean numFramesWarning;
  static boolean disabledGainWarning;
  
  public AudioSnippet delegate;    

  private static Minim minim;

  /**  @invisible   */
  public MinimSamplePlayer(PApplet p) {
    super(p);
    if (DBUG) System.err.println("MinimSamplePlayer.init<>");
    if (minim == null)
    	minim = new Minim(p);
  }
  
  public static void disableGainWarnings() {
    disabledGainWarning = true;
  }
  
  public void dispose()
  {
    this.delete();
  }
  
  public void load(String sample) 
  { 
    this.load(sample, DEFAULT_VOLUME);
  }
  
  /** Takes String name or Url of wav, aiff, or mp3 file */ 
  public void load(String sample, float initialVol) 
  { 
    if (DBUG)System.err.println("MinimSamplePlayer.load("+sample+")");

    delegate = minim.loadSnippet(sample); 
    
    if (delegate == null)
      throw new RiTaException(this,"Unable to load file: "+sample);
    
    if (delegate.hasControl(Type.VOLUME))
      delegate.setVolume(initialVol);
  }

  public boolean isPlaying()
  {
    if (delegate == null) return false;
    return delegate.isPlaying();
  }
  
  public void play()
  {    
    if (DBUG)System.err.println("MinimSamplePlayer.play(vol="+getVolume()+")");
    
    if (delegate == null) return;
    
    delegate.rewind(); // will this cause problems?
    
    delegate.play();
  }

  public float getVolume()
  {
    if (delegate == null) return 0;
    if (this.delegate.hasControl(Controller.VOLUME)) {
      return this.delegate.getVolume();  
    }
    else if (this.delegate.hasControl(Controller.GAIN)) {
      if (!disabledGainWarning)
        System.err.println("[WARN] No volume control, using gain("+gainRange()+")!" +
        		"\n        Call RiSample.disableGainWarnings() to silence this message.");
      return this.delegate.getGain();  
    }    
    if (!disabledGainWarning)
      System.err.println("[WARN] No volume or gain control!");    
    return 0;
  }

  public void loop()
  {       
    this.delegate.loop();
  }

  private String gainRange() {    
    FloatControl gain = this.delegate.gain();    
    float min = gain.getMinimum();
    float max = gain.getMaximum();
    return "min="+min+" "+"max="+max;    
  }
  
  public void setVolume(float v)
  {
    if (DBUG) System.err.println("setVolume("+v+")");
    if (this.delegate.hasControl(Controller.VOLUME)) {
      this.delegate.setVolume(v);
    }
    else if (this.delegate.hasControl(Controller.GAIN)) {
      this.delegate.setGain(v);
      if (!disabledGainWarning)
        System.err.println("[WARN] No volume control, using gain("+gainRange()+")!");
    }
    else { 
      if (!disabledGainWarning)
        System.err.println("[WARN] No volume or gain control!");
    }
  }
  
  public void stop()
  {   
    if (DBUG)System.err.println("MinimSamplePlayer.stop()");
    if (this.delegate != null)
      this.delegate.pause();
    //this.audioPlayer.rewind(); // ?
  }
  
  public void delete()
  {
    if (DBUG) System.err.println("MinimSamplePlayer.dispose");    
    this.stop();
    if (delegate != null)
      delegate.close();
    if (minim != null)
      minim.stop();
  }

  public void mute(boolean bool)
  {
    if (DBUG) System.err.println("MinimSamplePlayer.mute("+bool+")");    
    if (bool) {   
      this.delegate.mute();
    }
    else {
      this.delegate.unmute();
    }
  }

  /**
   * pan value 1- to 1;
   */
  public void pan(float f)
  {
    if (DBUG) System.err.println("MinimSamplePlayer.pan("+f+")");    
    if (this.delegate.hasControl(Controller.PAN)) 
      this.delegate.setPan(f);     
    else 
      System.err.println("[WARN] No pan control!");
  }
  
  public float getPan()
  {
    if (delegate == null) return 0;
    if (this.delegate.hasControl(Controller.PAN)) 
      return this.delegate.getPan();   
    System.err.println("[WARN] No pan control!");
    return 0;

  }

  public void pause()
  {
    if (delegate == null) return;
    this.delegate.pause();
  }

  public void loop(int i)
  {
    this.delegate.loop(i);    
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

  public void update() { /* no Lerp needed here */ }
  
  public void fadeVolume(float targetVolume, float sec)
  {
    if (delegate == null) return;
    this.delegate.shiftVolume(getVolume(), targetVolume, (int)(sec*1000));
  }
  
  public void stopFade()
  {
    if (delegate == null) return;
    this.delegate.shiftVolume(getVolume(), getVolume(), 0); 
  }

  public void pan(float f, float sec)
  {
    if (delegate == null) return;
    if (this.delegate.hasControl(Controller.PAN))
       this.delegate.shiftVolume(getPan(), f, (int)(sec*1000));
    else
      System.err.println("[WARN] No pan control!");
  }

  public void stopPan()
  {
    if (delegate == null) return;
    if (this.delegate.hasControl(Controller.PAN))
      this.delegate.shiftPan(getPan(), getPan(), 0);
    else
      System.err.println("[WARN] No pan control!");
  }

  public int getCurrentFrame()
  {
    return this.delegate.position();
  }

  public int getNumFrames()
  {
    checkFrameWarning();
    return this.delegate.length();
  }

  private void checkFrameWarning()
  {
    if (!numFramesWarning) {
      System.err.println("[WARN] Minim only returns the length of a sample in milliseconds!");
      numFramesWarning = true;
    }
  }

  /**
   * NOTE: can cause an error before the loop has played through once 
   */
  public void setCurrentFrame(int frame)
  {
    checkFrameWarning();
    this.delegate.cue(frame);
  }

  public boolean isLooping()
  {   
    if (delegate == null) return false;
    return delegate.isLooping();
  }

}// end
