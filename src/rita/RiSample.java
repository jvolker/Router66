package rita;

import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;

import processing.core.PApplet;

/*
 * TODO: need to register for dispose() method?
 */

/**
 * Simple library-agnostic audio support for RiTa that handles playback 
 * of .wav and .aiff samples and server-based streaming of .mp3s. The following
 * example create a sample object attached to a RiText object.
 *<pre> 
    RiText rt = new RiText(this);
    RiSample sample = rt.loadSample("sampleName.aiff");</pre>      
 * @author dhowe
 */
// should we register update & dispose methods here??? (yes)
public abstract class RiSample extends RiObject
{ 
  /** @invisible */
  public static List instances = null;
  
  /** @invisible */
  public static float DEFAULT_VOLUME = 1;
    
  private static final String BASE_PKG = "rita";
  
  /** @invisible */
  public static final String MINIM_SAMPLE_PLAYER =  BASE_PKG+".MinimSamplePlayer";;
  
  /** @invisible */
  public static final String SONIA_SAMPLE_PLAYER = BASE_PKG+".SoniaSamplePlayer";
  
  /** @invisible */
  public static final String ESS_SAMPLE_PLAYER = BASE_PKG+".EssSamplePlayer";
  
  /** @invisible */
  public static final String DEFAULT_SAMPLE_PLAYER = MINIM_SAMPLE_PLAYER;
  
  /**
   * Creates a new RiSample object of correct type
   * based upon available libraries.
   * @invisible
   */ 
  public static RiSample create(PApplet p) 
  {    
    return create(p, DEFAULT_SAMPLE_PLAYER);
  }
  // TODO: try each type here..
  
  /**
   * Creates a new RiSample object using the specified library.
   * @invisible
   */
  public static RiSample create(PApplet p, String classType)
  {
    try {
      Class playerClass = Class.forName(classType);
      Constructor ctor = playerClass.getConstructor(new Class[]{PApplet.class});
      RiSample sp = (RiSample)ctor.newInstance(new Object[]{p});
      if (!printedAudioPlayer) {
        if (!RiTa.SILENT)System.out.println("[INFO] RiTa.audioPlayer="+RiTa.shortName(sp));
        printedAudioPlayer = true;
      }
      sp.registerDispose();
      return sp;
    }
    catch (Exception e) {
      throw new RiTaException("Unable to load audio library: "+classType+
        "\nMake sure you've properly installed an audio library (eg minim)", e);
    }
  }  static boolean printedAudioPlayer;  
  
/*  *//**
   *  @invisible 
   *  for applet-shutdown 
   *//* 
  public abstract void dispose();*/
  
  /**
   * @invisible
   */
  public static void deleteAll()
  {
    if (instances == null) return;
    for (int i = 0; i < instances.size(); i++) {
      RiSample ris = ((RiSample) instances.get(i));
      if (ris != null) ris.delete();
    }
    instances.clear();
  }
  
  public static void disableGainWarnings() {
    MinimSamplePlayer.disabledGainWarning = true;
  }
  
  /**
   * Sets the default volume, e.g., the starting volume, for the sample
   * @param vol
   */
  public static void setDefaultVolume(float vol)
  {
    DEFAULT_VOLUME = vol;
  }  
  
  protected RiSample(PApplet p) {
     super(p);
    if (instances == null)      
      instances = new LinkedList();
    //System.out.println("RiSample.RiSample()");
    instances.add(this);
  }

  /**
   * Loads the sample specified by <code>sampleFileName</code>
   * and sets the initial volume to <code>initialVolume</code>
   */  
  public abstract void load(String sampleFileName, float initialVolume);
  public abstract void load(String sampleFileName);
  

  /**
   * Starts the current sample which will play once
   */
  public abstract void play();
   
  /**
   * Loads and starts the sample specified by 'sampleFileName'
   * @param sampleFileName  file to load and start
   */
  public abstract void play(String sampleFileName);
  
  /**
   * Starts the current sample which will loop indefinately
   */
  public abstract void loop();
  
  /**
   * Starts the sample which will loop for 'numberOfTimes' 
   * @param numberOfTimes  # of times to loop
   */
  public abstract void loop(int numberOfTimes);
  
  /**
   * Loads and loops indefinately the sample specified by 'sampleFileName'
   * @param sampleFileName - file to load and start
   */
  public abstract void loop(String sampleFileName);
  
  /**
   * Stops the currently playing sample
   */
  public abstract void stop();
  
  /**
   * Set volume between 0 and 1 for the current sample
   * @param newVol float  btwn 0 and 1
   */
  public abstract void setVolume(float newVol);
  
  /** 
   * Returns current volume for the sample
   */
  public abstract float getVolume();
  
  /** 
   * Returns true if the sample is playing
   */
  public abstract boolean isPlaying();
  
  /** 
   * Returns true if the sample is looping 
   */
  public abstract boolean isLooping();  
  
  /**
   * Fades volume to new value between 0 and 1
   * @param newVol  float btwn 0 and 1
   * @param sec  time in which to do the volume fade
   */
  public abstract void fadeVolume(float newVol, float sec);
  
  /**
   * Mutes or unmutes the current sample 
   * @param muted  true to mute, false to unmute
   */
  public abstract void mute(boolean muted);
    
  /**
   * @invisible
   */
  public abstract void delete();
   
  /**
   * Pauses the currently playing sample
   */
  public abstract void pause();

  /**
   * Pans sound between left(-1) and right (1)
   * @param panValue 
   * @param sec time in which to do the pan
   */
  public abstract void pan(float panValue, float sec);
  
  /**
   * Pans sound between left(-1) and right (1)
   * @param panValue 
   */
  public abstract void pan(float panValue);
  
  /** 
   * Returns current panValue
   */
  public abstract float getPan();
  
  /** 
   * Returns current frame in sample
   */
  public abstract int getCurrentFrame();
  
  /** 
   * Returns total # of frames in sample
   */
  public abstract int getNumFrames();
  
  /** 
   * Moves the playhead to the specified frame
   */
  public abstract void setCurrentFrame(int frame);

  /**
   * Stops any in-progress 'fades' on the current sample
   */
  public abstract void stopFade();

  /**
   * Stops any in-progress 'pans' on the current sample
   */
  public abstract void stopPan();
  
  /**
   * Used to update a samplePlayer at the frameRate  
   * @invisible
   */
  public void update(/*int millis*/) {}
  
}// end
