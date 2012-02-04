package rita.support;

import com.sun.speech.freetts.Voice;

public interface RiSpeechEngine {

  public abstract void dispose();

  public abstract Voice getVoice();

  public abstract void stop();
  
  public abstract String[] getVoiceDescriptions();

  public abstract void setVoice(String voiceDesc);

  public abstract void setVoice(Voice vc);

  public abstract void speak(final String text);

  public abstract boolean isSpeaking();

  public abstract float getVoicePitch();

  public abstract float getVoicePitchRange();

  public abstract float getVoicePitchShift();

  public abstract float getVoiceRate();

  public abstract float getVoiceVolume();

  public abstract void setVoicePitch(float hertz);

  public abstract void setVoicePitchRange(float range);

  public abstract void setVoicePitchShift(float shift);

  public abstract void setVoiceRate(float wpm);

  public abstract void setVoiceVolume(float vol);

  public abstract String getVoiceName();

  public abstract String[] getVoiceNames();

  public abstract String getVoiceDescription();
  
  public abstract String getAudioFileName();
  
  public abstract void setAudioFileName(String audioFileName);

}