package rita.support;

import java.io.*;
import java.security.AccessControlException;
import java.util.*;

import javax.sound.sampled.*;

import processing.core.PApplet;
import rita.*;

import com.sun.speech.freetts.*;
import com.sun.speech.freetts.audio.*;
import com.sun.speech.freetts.cart.CART;
import com.sun.speech.freetts.cart.CARTImpl;
import com.sun.speech.freetts.en.us.*;
import com.sun.speech.freetts.lexicon.Lexicon;
import com.sun.speech.freetts.util.Utilities;

/*
 *  TODO:
 *    Make a singleton with each new voice running -- see VThread below!
 */
/**
 * Proxy object for the FreeTTS implementation of JSAPI
 */
public class RiFreeTTSEngine implements RiSpeechEngine
{
  private static final int MAX_TOKENS_PER_UTT = 500;

  public static final boolean USE_TTS = true;

  protected static final String DEFAULT_VOICE = "kevin16";

  protected static final String FREE_TTS_VOICES = "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory";

  protected static final String MBROLA_VOICES = "de.dfki.lt.freetts.en.us.MbrolaVoiceDirectory";

  public static final String MBROLA_BASE = "C:\\Documents and Settings\\dhowe\\My Documents\\freetts-1.2.1\\mbrola\\";

  // private static RiFreeTTSEngine instance;

  protected List speakThreads; // ?
  protected Voice voice; // REMOVE THIS FOR V_THREADS

  protected boolean killed;
  // protected PApplet pApplet;
  protected Lexicon lexicon;

  protected RiSpeech parent; // is this the problem??

  protected static RiTokenToWords tokenProcessor; // or these?
  protected static boolean ttsEnabled = USE_TTS;

  protected static String mbrolaBase;
  protected Segmenter segmenter;

  // default voice parameters
  protected static float defaultVoiceDurationStretch = Float.MIN_VALUE;
  protected static float defaultVoicePitchInHertz = Float.MIN_VALUE;
  protected static float defaultVoicePitchRange = Float.MIN_VALUE;
  protected static float defaultVoicePitchShift = Float.MIN_VALUE;
  protected static float defaultVoiceVolume = Float.MIN_VALUE;
  protected static float defaultVoiceWPM = Float.MIN_VALUE;

  /*
   * public static RiFreeTTSEngine getInstance(PApplet p) { return
   * getInstance(p, DEFAULT_VOICE, null); }
   * 
   * public static RiFreeTTSEngine getInstance(PApplet p, String voiceName) {
   * return getInstance(p, voiceName, null); }
   * 
   * public static RiFreeTTSEngine getInstance(PApplet p, String voiceName,
   * String mbrolaBase) { if (instance == null) instance = new
   * RiFreeTTSEngine(p, null, voiceName, mbrolaBase); return instance; }
   */

  public RiFreeTTSEngine(RiSpeech rispeech, String voiceName, String mbrolaBase)
  {
    this(rispeech.getPApplet(), rispeech, voiceName, mbrolaBase);
  }

  public RiFreeTTSEngine(PApplet pApplet, String voiceName, String mbrolaBase)
  {
    this(pApplet, null, voiceName, mbrolaBase);
  }

  private RiFreeTTSEngine(PApplet pApplet, RiSpeech rispeech, String voiceName, String mbrolaBase)
  {
    // System.out.println("RiFreeTTSEngine.RiFreeTTSEngine()");
    PApplet p = pApplet;
    this.parent = rispeech;
    if (mbrolaBase != null)
      setMbrolaBase(mbrolaBase);
    this.segmenter = new Segmenter();
    if (tokenProcessor == null)
      tokenProcessor = initTokenProcessor();
    if (p == null && parent != null)
      p = parent.getPApplet();
    this.lexicon = RiLexiconImpl.getInstance(p);
    if (voiceName == null)
      voiceName = DEFAULT_VOICE;
    this.voice = loadVoice(voiceName, lexicon); // move to vthread    
    allocateVoice();
  }

  public void dispose()
  {
    // System.out.println("RiFreeTTSEngine.dispose()");
    this.killed = true;
    if (this.voice != null)
    {
      try
      {
        stopSpeakThreads();
        try
        {
          this.voice.deallocate();
        }
        catch (Throwable e)
        {
          System.out.println("[WARN] Error disposing of RiFreeTTSEngine");
        }
        this.voice = null;
        this.segmenter = null;
        this.lexicon = null;
      }
      catch (RuntimeException e)
      {
        System.out.println("[WARN] RiFreeTTSEngine->dispose: " + e);
      }
    }
  }

  private void stopSpeakThreads()
  {
    this.stop();
    try
    {
      if (speakThreads != null)
      {
        for (int i = 0; i < speakThreads.size(); i++)
          killThread((Thread) speakThreads.get(i));
        speakThreads.clear();
      }
    }
    catch (Exception e)
    {
      throw new RiTaException(e);
    }
    finally
    {
      speakThreads = null;
    }
  }

  private Voice loadVoice(String voiceName, Lexicon lex)
  {
    Voice vc = null;
    if (ttsEnabled)
    {
      setVoiceDirectories();
      // System.out.println("Info: finding "+voiceDesc+"\n"+vm.getVoiceInfo());
      if (voiceName.equals("kevin"))
        voiceName = "kevin16"; // for backwards-compatibility
      vc = RiVoiceManager.getInstance().getVoice(voiceName);
    }
    else
      // dummy voice
      vc = new NullVoice();

    if (vc == null)
      throw new RiTaException("Unable to load voice: '" + voiceName
          + "' from available voices:\n" + RiTa.asList(getVoiceDescriptions()));

    vc.setLexicon(lex);
    
    return vc;
  }

  private void setVoiceDirectories()
  {
    try
    {
      if (mbrolaBase == null) // check the system prop
        mbrolaBase = Utilities.getProperty("mbrola.base", null);
    }
    catch (AccessControlException e1)
    {
      /* ignore */
    }

    if (mbrolaBase != null)
    {
      setMbrolaBase(mbrolaBase);
    }
    else
    {
      try
      {
        System.setProperty("freetts.voices", FREE_TTS_VOICES);
      }
      catch (AccessControlException e)
      {
        /* ignore */
      }
    }
  }

  public List textToUtterance(CharSequence text)
  {
    // should this be the default tokenizer instead??
    Tokenizer tokenizer = getTokenizer();
    tokenizer.setInputText(text.toString());

    List utts = new ArrayList();

    Token savedToken = null;
    boolean first = true;
    while (tokenizer.hasMoreTokens())
    {
      // Fill a new Utterance:
      List tokenList = new ArrayList();
      Utterance utterance = null;
      if (savedToken != null)
      {
        tokenList.add(savedToken);
        savedToken = null;
      }

      while (tokenizer.hasMoreTokens())
      {
        Token token = tokenizer.getNextToken();
        // if ((token.getWord().length() == 0)
        if ((token.toString().length() == 0) || (tokenList.size() > MAX_TOKENS_PER_UTT) // ?
            || tokenizer.isBreak())
        {
          savedToken = token;
          break;
        }
        tokenList.add(token);
      }

      utterance = new Utterance(voice, tokenList);
      utterance.setFirst(first);
      first = false;
      utterance.setLast(!tokenizer.hasMoreTokens());

      // Process tokens here...
      try
      {
        if (utterance == null)
        {
          System.out.println("[ERROR] Null Utterance!");
          System.exit(1);
        }
        tokenProcessor.processUtterance(utterance);
        segmenter.processUtterance(utterance);
      }
      catch (Throwable e)
      {
        System.err.println("\n[ERROR] TokenProcessor/Segmenter ----------------");
        e.printStackTrace();
        System.err.println("-------------------------------------------------");
      }
      utts.add(utterance);
    }

    return utts;
  }

  private Tokenizer getTokenizer()
  {
    if (tokenizerInst == null)
    {
      tokenizerInst = new RiUtteranceTokenizer();
      tokenizerInst.setWhitespaceSymbols(USEnglish.WHITESPACE_SYMBOLS);
      tokenizerInst.setSingleCharSymbols(USEnglish.SINGLE_CHAR_SYMBOLS);
      tokenizerInst.setPrepunctuationSymbols(USEnglish.PREPUNCTUATION_SYMBOLS);
      tokenizerInst.setPostpunctuationSymbols(USEnglish.PUNCTUATION_SYMBOLS);
    }
    return tokenizerInst;
  }

  private static com.sun.speech.freetts.Tokenizer tokenizerInst;

  private RiTokenToWords initTokenProcessor()
  {

    CART numbersCart = null;
    PronounceableFSM prefixFSM = null;
    PronounceableFSM suffixFSM = null;
    try
    {
      numbersCart = new CARTImpl(CMUVoice.class.getResource("nums_cart.txt"));
      prefixFSM = new PrefixFSM(CMUVoice.class.getResource("prefix_fsm.txt"));
      suffixFSM = new SuffixFSM(CMUVoice.class.getResource("suffix_fsm.txt"));
    }
    catch (Exception e)
    {
      String err = "Unable to create TokenProcessor: " + e.getMessage();
      System.err.println(err);
      throw new RiTaException(err, e);
    }
    return new RiTokenToWords(numbersCart, prefixFSM, suffixFSM);
  }

  public Voice getVoice()
  {
    if (ttsEnabled && !voice.isLoaded())
      allocateVoice();
    return this.voice;
  }

  /* s */
  /*
   * // should this return the same voice everytime?
   * 
   * 
   * public String getVoiceDescription() { return voiceToString(getVoice()); }
   */

  public String[] getVoiceDescriptions()
  {
    List vc = new LinkedList();
    Voice[] voices = RiVoiceManager.getInstance().getVoices();
    if (voices != null)
    {
      for (int i = 0; i < voices.length; i++)
      {
        if (!voices[i].getName().equals("kevin16"))
          vc.add(voiceToString(voices[i]));
      }
    }
    return (String[]) vc.toArray(new String[vc.size()]);
  }

  public static String voiceToString(Voice v)
  {
    String br = System.getProperty("line.separator");
    String info = br + "Name: " + v.getName() + br + "\tDescription: "
        + v.getDescription() + br + "\tOrganization: " + v.getOrganization() + br
        + "\tDomain: " + v.getDomain() + br + "\tLocale: " + v.getLocale().toString()
        + br + "\tStyle: " + v.getStyle() + br + "\tGender: " + v.getGender().toString()
        + br + "\tAge: " + v.getAge().toString() + br + "\tPitch: " + v.getPitch() + br
        + "\tPitch Range: " + v.getPitchRange() + br + "\tPitch Shift: "
        + v.getPitchShift() + br + "\tRate: " + v.getRate() + br + "\tVolume: "
        + v.getVolume() + br;
    return info;
  }

  public void setVoice(String voiceDesc)
  {
    Voice vc = loadVoice(voiceDesc, this.lexicon);
    if (vc != null)
      setVoice(vc);
  }

  public void setVoice(Voice vc)
  {
    if (this.voice.isLoaded())
      this.voice.deallocate();
    this.voice = vc;
  }

  class VThread extends Thread
  {
    Voice v;
    String text;
    boolean speaking;

    public VThread(Voice v)
    {
      this(v, null);
    }

    public VThread(Voice v, String text)
    {
      this.v = v;
      this.text = text;
    }

    public boolean isSpeaking()
    {
      return speaking;
    }

    public void speak(String text)
    {
      this.text = text;
      this.start();
    }

    public void run()
    {
      synchronized (this)
      {
        if (isSpeaking())
        {
          System.err.println("[WARN] Voice(id=" + v.hashCode() + ") is in use!"
              + " Ignoring new utterance: '" + text + "'\n       Perhaps you want to "
              + "create multiple RiSpeech objects (for concurrent speech)?");
          return;
        }
        if (text == null)
          return;
        speaking = true;
        try
        {
          final String toSpeak = text;
          v.createOutputThread();
          v.speak(toSpeak);
          onComplete(this, toSpeak);
        }
        catch (Throwable e)
        {
          throw new RiTaException("VThread", e);
        }
        speaking = false;
      }
    }

    public void delete()
    {
      try
      {
        v.deallocate();
        v = null;
        text = null;
      }
      catch (Exception e)
      {
        System.err.println("[WARN] Exception while deleting voice: " + this);
      }
    }
  }

  private static final boolean USE_VTHREADS = false;
  private boolean speaking;
  private String audioFileName;

  public void speak(final String text)
  {
    if (!ttsEnabled)
      throw new RiTaException("TTS capabilities not enabled!"
          + " Make sure you've downloaded the RiTa+TTS zip...");

    if (killed)
      return;

    if (!voice.isLoaded())
      allocateVoice();

    final Voice v = this.voice;

    Thread t = null;
    if (USE_VTHREADS)
    {
      t = new VThread(v, text);
    }
    else
    {
      try
      {
        final RiFreeTTSEngine parent = this;
        t = new Thread()
        {

          public void run()
          {
            try
            {
              if (speaking)
              {
                System.err.println("[WARN] Voice(id=" + v.hashCode() + ") is in use!"
                    + " Ignoring new utterance: '"+text+"'\n       Perhaps you want to "
                    + "create multiple RiSpeech objects (for concurrent speech)?");
                return;
              }
              
              speaking = true;
              
              if (audioFileName != null)
              {
                AudioPlayer audioPlayer = new SingleFileAudioPlayer(audioFileName, AudioFileFormat.Type.WAVE);
                v.setAudioPlayer(audioPlayer);
              }
              // System.out.println("AudioFormat: "+v.getAudioPlayer().getAudioFormat());
              v.speak(text);

              if (audioFileName != null)
                parent.stop();
              onComplete(this, text);
              speaking = false;
            }
            catch (Throwable e1)
            {
              throw new RiTaException(e1);
            }
          }
        };
      }
      catch (Throwable e)
      {
        throw new RiTaException(e);
      }
    }
    getSpeakThreads().add(t);
    t.start();
  }

  /**
   * Stops the voice immediately if speaking.
   */
  public void stop()
  {
    if (voice != null)
    {
      AudioPlayer ap = voice.getAudioPlayer();
      if (ap != null)
      {
        ap.close(); // hack: recreate the audio player
        if (!(ap instanceof JavaStreamingAudioPlayer || ap instanceof SingleFileAudioPlayer))
          System.err.println("[WARN] Unexpected AudioPlayer type in RiSpeech.stop(): expected"
              + " JavaStreamingAudioPlayer or SingleFileAudioPlayer, but found "
              + ap.getClass());
        voice.setAudioPlayer(new JavaStreamingAudioPlayer());
      }
    }
  }

  public boolean isSpeaking()
  {
    return speaking;
  }

  protected void killThread(VThread speakThread)
  {
    // killed = true;
    try
    {
      speakThread.delete();
      speakThread = null;
    }
    catch (Throwable e)
    {
      throw new RiTaException(e);
    }
  }

  protected void killThread(Thread speakThread)
  {
    try
    {
      speakThread = null;
    }
    catch (Throwable e)
    {
      throw new RiTaException(e);
    }
  }

  protected void onComplete(Thread t, String text)
  {
    if (speakThreads != null)
      speakThreads.remove(t);
    if (parent != null)
      parent.fireSpeechCompletedEvent(text);
  }

  /*
   * //System.out.println("RiFreeTTSEngine.onComplete("+this+")"); if (pApplet
   * != null) { final RiTextEvent rte = new RiTextEvent(this,
   * RiTextEvent.SPEECH_COMPLETED, text); try { speakThreads.remove(t); // reuse
   * these? rita.RiTa.invoke(pApplet, "onSpeechCompleted", new Class[] {
   * RiTextEvent.class}, new Object[]{ rte }); } catch (RiTaException e) { //
   * ignore if the method doesnt exist... } }
   */

  synchronized List getSpeakThreads()
  {
    if (speakThreads == null)
      speakThreads = new ArrayList();
    return speakThreads;
  }

  // make this happen on init?
  private void allocateVoice()
  {
    try
    {
      this.voice.allocate();
    }
    catch (OutOfMemoryError m)
    {
      throw new RiTaException(m);
    }
    catch (Throwable e)
    {
      /* e.printStackTrace(); */
      String err = "Unable to allocate requested voice: " + voice.getName();
      err += ".\n          Are you using a version of RiTa with TTS-support?\n";
      if (voice.getName().indexOf("mbrola") > -1)
        err += " If you're using an mbrola voice, make sure you've "
            + "set\n'mbrolaBase' to the directory containing the mbrola binary";
      throw new RiTaException(err, e);
    }
    /*
     * setVoicePitch(defaultVoicePitchInHertz);
     * setVoicePitchRange(defaultVoicePitchRange);
     * setVoicePitchShift(defaultVoicePitchShift);
     * setVoiceVolume(defaultVoiceVolume); setVoiceRate(defaultVoiceWPM);
     */
  }

  public static boolean isTTSEnabled()
  {
    return ttsEnabled;
  }

  public static void setTTSEnabled(boolean enableTts)
  {
    ttsEnabled = enableTts;
  }

  // Inner classes ---------------------------------------------

  class NullVoice extends Voice
  {
    protected UtteranceProcessor getAudioOutput() throws IOException
    {
      return null;
    }

    public com.sun.speech.freetts.Tokenizer getTokenizer()
    {
      return null;
    }

    protected void loader() throws IOException
    {
    }
  }

  public float getVoicePitch()
  {
    return this.voice.isLoaded() ? this.voice.getPitch() : defaultVoicePitchInHertz;
  }

  public float getVoicePitchRange()
  {
    return this.voice.isLoaded() ? this.voice.getPitchRange() : defaultVoicePitchRange;
  }

  public float getVoicePitchShift()
  {
    return this.voice.isLoaded() ? this.voice.getPitchShift() : defaultVoicePitchShift;
  }

  /**
   * @return rate in words per min (WPM)
   */
  public float getVoiceRate()
  {
    return this.voice.isLoaded() ? this.voice.getRate() : defaultVoiceWPM;
  }

  public float getVoiceVolume()
  {
    return this.voice.isLoaded() ? this.voice.getVolume() : defaultVoiceVolume;
  }

  public void setVoicePitch(float hertz)
  {
    if (this.voice.isLoaded())
      this.voice.setPitch(hertz);
    else
      defaultVoicePitchInHertz = hertz;
  }

  public void setVoicePitchRange(float range)
  {
    if (this.voice.isLoaded())
      this.voice.setPitchRange(range);
    else
      defaultVoicePitchRange = range;
  }

  public void setVoicePitchShift(float shift)
  {
    if (this.voice.isLoaded())
      this.voice.setPitchShift(shift);
    else
      defaultVoicePitchShift = shift;
  }

  public static void setMbrolaBase(String mbrolaBase)
  {
    RiFreeTTSEngine.mbrolaBase = mbrolaBase;
    if (mbrolaBase != null)
    {
      try
      {
        System.setProperty("mbrola.base", mbrolaBase);
        // make sure we check the mbrola voice directory too
        System.setProperty("freetts.voices", FREE_TTS_VOICES + "," + MBROLA_VOICES);
      }
      catch (AccessControlException e)
      {
        /* ignore */
      }
    }
  }

  public void setVoiceRate(float wpm)
  {
    if (this.voice.isLoaded())
      this.voice.setRate(wpm);
    else
      defaultVoiceWPM = wpm;
  }

  public void setVoiceVolume(float vol)
  {
    if (this.voice.isLoaded())
      this.voice.setVolume(vol);
    else
      defaultVoiceVolume = vol;
  }

  public String getVoiceName()
  {
    return getVoice().getName();
  }

  public String getVoiceDescription()
  {
    return voiceToString(getVoice());
  }

  private String getVoiceName(String desc)
  {
    String nameTag = "Name: ";
    int idx = desc.indexOf(nameTag);
    if (idx < 0)
      return "unknown";
    desc = desc.substring(idx + nameTag.length());
    idx = desc.indexOf("\n");
    if (idx < 0)
      return "unknown";
    return desc.substring(0, idx);
  }

  public String[] getVoiceNames()
  {
    String[] descs = getVoiceDescriptions();
    String[] names = new String[descs.length];
    for (int i = 0; i < names.length; i++)
      names[i] = getVoiceName(descs[i]);
    return names;
  }

  public String getAudioFileName()
  {
    return audioFileName;
  }

  public void setAudioFileName(String audioFileName)
  {
    this.audioFileName = audioFileName;
  }

  public static void main(String[] args) throws IOException, InterruptedException
  {
    ttsEnabled = true;

    RiFreeTTSEngine ftl = new RiFreeTTSEngine(null, null, null, null);
    //Voice v = ftl.getVoice();
    ftl.setVoicePitchShift(5);
    ftl.speak("hello daniel");
    //if (1==1) reutr
    //v.setPitchShift(5);
    //v.speak("hello daniel");
    new Thread()
    {
      public void run()
      {
        try
        {
          Thread.sleep(2000);
        }
        catch (InterruptedException e)
        {
        }
        System.exit(1);
      }
    }.start();
    /*
     * System.out.println(ftl.getVoiceNames()[0]); Thread.sleep(1000);
     * System.exit(1);
     */
    // ftl.say("The cat jumped over the dog","Vicki");
    // RiFreeTTSEngine ftl = RiFreeTTSEngine.getInstance(null);//.getLexicon();
    // ftl.setMbrolaBase(MBROLA_BASE);
    // System.out.println(Util.asList(ftl.getVoiceDescriptions()));
    // ftl.setVoice("mbrola_us1");
    // ftl.speak("cat");
    // ftl.speak("cater");
    // ftl.speak("hello");
    // Thread.sleep(500);
    // ftl.delete();
    // List l = ftl.textToUtterance("hello daniel");
    // RiSpeech.setTTSEnabled(true);
    // System.setProperty("mbrola.base", MBROLA_BASE);
    // MbrolaVoiceDirectory.main(new String[]{"mbrola_us1"});
    /*
     * ftl.setVoiceRate(100); System.out.println(ftl.getVoicePitch());
     * System.out.println(ftl.getVoicePitchRange());
     * System.out.println(ftl.getVoicePitchShift());
     * System.out.println(ftl.getVoiceRate());
     * System.out.println(ftl.getVoiceVolume()); ftl.speak("hello daniel");
     */
  }

}// end
