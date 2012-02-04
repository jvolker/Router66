package rita;

import processing.core.PApplet;
import rita.support.*;

/*
 * 
 * TODO:
 * 
    DOES NOT CLOSE AFTER speak() call -- THIS IS A BUG!!!
         
 *  Need to add numbers 1-10 to lexicon/addenda w' correct pronounciation
 *  
    Make sure we can do these tests!!! 
        a) 1 voice, 
        b) 1 voice, 2 utts in parallel
        c) 1 voice, 2 utts sequentially (w' and w 'out callback?)
        d) 2 voice, 2 utts in parallel
       
 * BUG: (see below) click immediately on launch
 *   
    RiSpeech rs;

    public void setup() {
      size(200,200);
      RiText rt = new RiText(this, "Hello from RiTa");
      rs = new RiSpeech(this);  
      rs.speak(rt);
    }
  
    public void draw() {
      background(255);
    }
  
    public void mouseReleased() {
      println("mouse");
      rs.speak("u clicked me!");  
    }
 
 * TODO:
 *   can we make the params (pitch, rate, etc) real-time? probably not :(   
 */

/** 
 * Provides basic cross-platform text-to-speech facilities with control over 
 * a range of parameters including voice-selection, pitch, speed, rate, etc.
 * When needed, multiple RiSpeech objects can be created, each with their own
 * parameters, for concurrent speech. <p>
 * 
 * Note: requires the installation of one or more FreeTTS compatible voices 
 * (see the RiTa+TTS install which includes a default 16-bit voice)...  
 * <p>
 * To receive callbacks when a RiSpeech object has completed
 * speaking a chunk of text, implement the following method in your applet:<pre>
 *   void onRiTaEvent(RiTaEvent re) {
 *     RiSpeech rs = (RiSpeech)re.getSource();
 *     String lastSpokenText = re.getData();
 *     ...
 *   }</pre> 
 * <p>
 * Also compatible with mbrola voices (http://tcts.fpms.ac.be/synthesis/mbrola.html),
 * but requires a local installation of the mbrola binary
 * (which must be named 'mbrola'), as well as a call to: 
 * <pre>   RiSpeech.setMbrolaBase(String pathToMbrola);</pre>
 * 
 * Specific instructions for installing and testing mbrola with RiTa can be 
 * found <a href="http://www.rednoise.org/pdal/index.php?n=Main.Mbrola" target="_new">here</a>.<p>
 * 
 * Note: there is currently an incompatability w' FreeTTS and Mbrola on Mac OSX, but you can use the
 * native MacTTS voices with code as follows:
 * <pre>
 *    RiSpeech rs = new RiSpeech(this);
 *    rs.useMacTTS();
 *    rs.setVoice("Bruce");
 *    rs.speak("Hello");</pre>
 *    
 * The default voices included on the Mac are: <pre>
    "Victoria", "Agnes", "Kathy", "Princess", "Vicki", 
    "Bruce", "Fred", "Junior", "Ralph", "Albert",
    "Bad News", "Bahh", "Bells", "Boing", "Bubbles", 
    "Cellos", "Deranged", "Good News", "Hysterical", 
    "Pipe Organ", "Trinoids", "Whisper", "Zarvox"
    </pre>
 *
 * @author dhowe 
*/
public class RiSpeech extends RiObject
{
  private static final String WAV = ".wav";
  private String mbrolaBase, voiceName;
  private RiSpeechEngine delegate;
  private boolean callbacksDisabled;
  private boolean useMacTTS;  
  
  /**
   * Creates a new object for Text-To-Speech  
   * and enables TTS for the system (if possible)
   * @invisible 
   */
  public RiSpeech() {
    this(null);    
  }
  
  /**
   * Creates a new object for Text-To-Speech  
   * and enables TTS for the system (if possible) 
   */
  public RiSpeech(PApplet pApplet) 
  {
    this(pApplet, null);
  }
  
  /**
   * Creates a new object for Text-To-Speech  
   * and enables TTS for the system (if possible),
   * using the specified voice name. 
   */
  public RiSpeech(PApplet pApplet, String voiceName) 
  {
    this(pApplet, voiceName, null);   
  }
  
 /**
   * Creates a new object for Text-To-Speech and enables TTS for the system<br> using the 
   * specified voice name, and setting the optional property 'mbrola.base' to
   * <code>mbrolaBase</code>.
   */
  public RiSpeech(PApplet pApplet, String voiceName, String mbrolaBase) 
  {
    super(pApplet);    
    this.voiceName = voiceName;
    this.mbrolaBase = mbrolaBase;
    this.registerDispose();    
  }
  
  /** 
   * Cleans-up resources on program end - not to be called in user-code.
   */
  public void dispose() {
    getDelegate().dispose();
  }

  /** Returns the current output filename of null if none is specified. */
  public String getOutputFile() {
    return getDelegate().getAudioFileName();
  }

  /**
   * Will direct output to a file instead of to the system's audio output. 
   * To re-enable audio output (the default), pass null to this method.  
   */
  public void setOutputFile(String wavFileName) {
    if (wavFileName.endsWith(WAV))
      wavFileName = wavFileName.substring(0, wavFileName.length()-WAV.length());
    this.getDelegate().setAudioFileName(wavFileName);
  }
  
  /** 
   * Loads all speech resources immediately (on initialization)
   * to avoid any delays on the first utterance -- only necessary if
   * there is a noticeable delay in program execution when speech begins.
   */
  public void preload() {
    getDelegate();
  }
  
  private RiSpeechEngine getDelegate() {
    if (delegate == null) {
      if (!useMacTTS) {           
        RiFreeTTSEngine.setTTSEnabled(true);
        this.delegate =  new RiFreeTTSEngine(this, voiceName, mbrolaBase);
      }
      else { 
        this.delegate = new RiMacSpeechEngine(this, voiceName);
      }
    }
    return delegate;
  }
  
  //TODO: add listener list for non PApplet callbacks...
  /**
   * Creates dynamic callback to the parent PApplet implementing:<pre>
   *     void onRiTaEvent(RiTaEvent re) {...}</pre>
   * @invisible
   */
  public void fireSpeechCompletedEvent(String text)
  {
    if (!callbacksDisabled) {
      boolean ok = RiTa.fireEvent(getPApplet(),
        new RiTaEvent(this, RiTaEvent.SPEECH_COMPLETED, text));
      if (!ok) callbacksDisabled = true;
    }      
  }

  /**
   * Returns whether Text-To-Speech is currently enabled
   */
  public static boolean isTTSEnabled()
  {
    return RiFreeTTSEngine.isTTSEnabled();
  }

  /**
   * Set whether Text-To-Speech is currently enabled
   * @param enableTts
   */
  public static void setTTSEnabled(boolean enableTts)
  {
    RiFreeTTSEngine.setTTSEnabled(enableTts);
  }

  /**
   * Returns the names and descriptions for each available voice. 
   */
  public String[] getVoiceDescriptions()
  {
    if (this.getDelegate() == null)
      throw new RiTaException("Unable to load voices: " +
        "are you sure you downloaded the tts-enabled version of RiTa?");
    return this.getDelegate().getVoiceDescriptions();
  }
  
  /**
   * Returns the names for all currently available voices. 
   */
  public String[] getAvailableVoiceNames()
  {
    if (this.getDelegate() == null)
      throw new RiTaException("Unable to load voices: " +
        "are you sure you downloaded the tts-enabled version of RiTa?");
    return this.getDelegate().getVoiceNames();
  }

  /**
   * Speaks the text string contained in a RiText object
   * using the current voice.
   */
  public void speak(RiText riText)
  {
    this.speak(riText.getText());
  }
  
  /**
   * Speaks the text string contained in a RiString object
   * using the current voice.
   */
  public void speak(RiString riString)
  {
    this.speak(riString.getText());
  }
  

  /**
   * Speaks the text string using the current voice.
   */
  public void speak(String text)
  {
    this.getDelegate().speak(text);    
  }  
  
  /**
   * Cleans-up resources associated with the text-to-speech engine.
   * Automatically called on 'shutdown' when using Processing.
   */
  public void delete() 
  {
    RiSpeechEngine rse = getDelegate();
    rse.dispose();
    rse = null;
  }

  /** 
   * Returns a descriptive name for the current voice
   */
  public String getVoiceName()
  {
    return getDelegate().getVoiceName();
  }
  
  /** 
   * Returns descriptive info on the current voice
   */
  public String getVoiceDescription()
  {
    return getDelegate().getVoiceDescription();
  }
  
  /**
   * @deprecated
   * @invisible
   * @see #getVoiceDescriptions()
   */
  public String[] getVoiceInfo()
  {
    return this.getDelegate().getVoiceDescriptions();
  }

  /** 
   * Returns the pitch of the current voice
   */
  public float getVoicePitch()
  {
    return this.getDelegate().getVoicePitch();
  }

  /** 
   * Returns the pitch range of the current voice
   */
  public float getVoicePitchRange()
  {
    return this.getDelegate().getVoicePitchRange();
  }
  
  /** 
   * Returns the pitch shift of the current voice
   */
  public float getVoicePitchShift()
  {
    return this.getDelegate().getVoicePitchShift();
  }

  /**
   * Stops the voice immediately if speaking.
   */ 
  public void stop() {
    if (delegate == null) 
      return; // no need to stop
    getDelegate().stop();
  } 
  
  /** 
   * Returns the rate of the current voice
   */  
  public float getVoiceRate()
  {
    return this.getDelegate().getVoiceRate();
  }

  /** 
   * Returns the volume of the current voice
   */
  public float getVoiceVolume()
  {
    return this.getDelegate().getVoiceVolume();
  }

  /** 
   * Sets the current voice by name (default voice='kevin') <p>Additional 'mbrola' voices 
   * can also be enabled if the mbrola binary is installed and
   * the 'mbrola.base' system property has been set by calling:
   * <pre>    RiSpeech.setMbrolaBase("c:\\path\\to\\mbrola\\");</pre>
   * 
   */
  public void setVoice(String voiceDesc)
  {
    this.getDelegate().setVoice(voiceDesc);
  }
  
  /** 
   * Enables support for mbrola voices by specifying 
   * the path to the installed mbrola binary. <p>
   * Note: this method must be called before setting
   * the current voice to an mbrola voice.
   */
  public static void setMbrolaBase(String mbrolaBase)
  { 
    RiFreeTTSEngine.setMbrolaBase(mbrolaBase);
  }

  /** 
   * Sets the pitch of the current voice
   */
  public void setVoicePitch(float hertz)
  {
    this.getDelegate().setVoicePitch(hertz);
  }
  
  /** 
   * Sets the pitch range of the current voice
   */  
  public void setVoicePitchRange(float range)
  {
    this.getDelegate().setVoicePitchRange(range);
  }
  
  /** 
   * Sets the pitch shift of the current voice
   */  
  public void setVoicePitchShift(float shift)
  {
    this.getDelegate().setVoicePitchShift(shift);
  }
  
  /** 
   * Sets the rate of the current voice
   */  
  public void setVoiceRate(float wpm)
  {
    this.getDelegate().setVoiceRate(wpm);
  }

  /** 
   * Sets the volume of the current voice
   */  
  public void setVoiceVolume(float vol)
  {
    this.getDelegate().setVoiceVolume(vol);
  }    
  
  /**
   * Tells the object to use the built-in Mac Speech API for all subsequent utterances
   */
  public void useMacTTS() {
    useMacTTS(null);    
  }
  
  /**
   * Tells the object to use the built-in Mac Speech API for all subsequent utterances,
   * then sets the current voice to <code>voice</code>
   */
  public void useMacTTS(String voice) {
    this.useMacTTS = true;
    this.voiceName = voice;
  }

  /**
   * Returns true if the speech engine is using the built-in Mac Speech API
   */
  public boolean isUsingMacTTS() {
    return useMacTTS;
  }
    
  
  public static void main(String[] args)
  {
    String text = "The boy jumped over the dog"; 
        text = "abalone";
    
    //RiSpeech.useMacTTS();
    RiSpeech pa = new RiSpeech(null);
    pa.setVoiceRate(5);
    //pa.useMacTTS();
    //pa.setVoice("Bruce");
    //System.out.println(pa.getVoiceVolume());
    pa.speak("hello");
    
    //pa.useMacTTS("Vicki");
    //pa.setVoice("kevin");
    //
   // pa.setMbrolaBase("C:\\Documents and Settings\\dhowe\\My Documents\\freetts-1.2.1\\mbrola\\");                  
   // pa.setVoice("mbrola_us1");
    //System.out.println(pa.getVoiceDescriptions());    
        //pa.speak(text);
  /*  pa.setOutputFile("/Users/dhowe/Desktop/night.wav");
    pa.speak("night");
    pa.setOutputFile("/Users/dhowe/Desktop/day.wav");
    pa.speak("day");*/
    /*
    RiSpeech pa2 = new RiSpeech(null);
    pa2.setVoice("kevin");
    pa.speak(text);
    pa2.speak(text2);  */  
  }
  
}// end
