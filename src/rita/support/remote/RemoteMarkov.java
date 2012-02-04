package rita.support.remote;

import java.util.Map;

import rita.*;
import rita.support.MarkovModel;
import rita.support.ifs.RiMarkovIF;

public class RemoteMarkov extends RiClientStub implements RiMarkovIF
{
  public RemoteMarkov(int nFactor, boolean ignoreCase) {
    super(MarkovModel.class);
    initMap.put("nFactor", new Integer(nFactor));
    initMap.put("ignoreCase", new Boolean(ignoreCase));
    createRemote(/*initMap*/);    
  }

  public void disableSentenceProcessing()
  {
    exec("disableSentenceProcessing");
  }

  public String generate()
  {
    return generateSentence();
  }

  public String generateSentence()
  {
    return exec("generateSentence");
  }

  public String[] generateSentences(int numSentences)
  {
    return toStrArr(exec("generateSentences", numSentences));
  }

  public String generateTokens(int length)
  {
    return (exec("generateTokens", length));
  }

  public String[] getCompletions(String[] seed)
  {
    return toStrArr(exec("getCompletions", seed, String[].class));
  }

  public String[] getCompletions(String[] pre, String[] post)
  {
    return toStrArr(exec("getCompletions", new Object[] { 
     pre, post }, new Class[]{ String[].class, String[].class }));
  }
  
  public void setAllowDuplicates(boolean allow)
  {
    exec("setAllowDuplicates", allow, Boolean.TYPE);
  }

  public boolean isAllowingDuplicates()
  {
    return Boolean.parseBoolean(exec("isAllowingDuplicates"));  
  }
  public int getNFactor()
  {
    return Integer.parseInt(exec("getNFactor"));
  }

  public Map getProbabilities(String[] path)
  {
    return RiTa.stringToMap(exec("getProbabilities", path, String[].class));        
  }

  public float getProbability(String token)
  {
    return Float.parseFloat(exec("getProbability", token, String.class));
  }

  public float getProbability(String[] tokens)
  {
    return Float.parseFloat(exec("getProbability", tokens, String[].class));
  }

  public int getWordCount()
  {
    return Integer.parseInt(exec("getWordCount"));
  }

  public boolean isIgnoringCase()
  {
    return Boolean.parseBoolean(exec("isIgnoringCase"));
  }

  public boolean isRecognizingSentences()
  {
    return Boolean.parseBoolean(exec("isRecognizingSentences"));
  }

  public boolean isSmoothing()
  {
    return Boolean.parseBoolean(exec("isSmoothing"));
  }

  public void loadFile(String fileName, int multiplier)
  {
    exec("loadFile", new Object[]
      {fileName, multiplier}, new Class[] {String.class, Integer.TYPE});
  }

  public void loadFile(String fileName)
  {
    exec("loadFile", fileName, String.class);
  }

  public void loadSentences(String[] sentences)
  {
    exec("loadSentences", sentences, String[].class);
  }
  
  public void loadTokens(char[] tokens)
  {
    String[] s = new String[tokens.length];
    for (int i = 0; i < tokens.length; i++) 
      s[i] = Character.toString(tokens[i]);   
    this.loadTokens(s);
  }

  public void loadText(String rawText)
  {
    exec("loadText", rawText, String.class);
  }

  public void loadText(String rawText, int multiplier)
  {
    exec("loadFile", new Object[]
      {rawText, multiplier}, new Class[] {String.class, Integer.TYPE});
  }

  public void loadTokens(String[] tokens)
  {
    exec("loadTokens", tokens, String[].class);
  }

  public void setRecognizeSentences(boolean ignoreSentences)
  {
    exec("setRecognizeSentences", ignoreSentences, Boolean.TYPE);
  }

  public void setTokenizerRegex(String regex)
  {
    exec("setTokenizerRegex", regex, String.class);
  }

  public void setUseSmoothing(boolean useSmoothing)
  {
    exec("setUseSmoothing", useSmoothing, Boolean.TYPE);
  }

  public boolean isPrintingIgnoredText() {
    return Boolean.parseBoolean(exec("isPrintingIgnoredText"));
  }

  public void setPrintIgnoredText(boolean printIgnoredText) {
    exec("setPrintIgnoredText", printIgnoredText, Boolean.TYPE);
  }

  public void loadSentences(String[] sentences, int multiplier) {
    exec("loadSentences", new Object[]
      { sentences, multiplier }, new Class[] { String[].class, Integer.TYPE });
  }

  public void loadTokens(String[] tokens, int multiplier) {
    exec("loadTokens", new Object[]
      { tokens, multiplier }, new Class[] { String[].class, Integer.TYPE });
  }
    
  public int getMaxSentenceLength() {
    return Integer.parseInt(exec("getMaxSentenceLength"));
  }

  public int getMinSentenceLength() {
    return Integer.parseInt(exec("getMinSentenceLength"));
  }

  public boolean isRemovingQuotations() {
    return Boolean.parseBoolean(exec("isRemovingQuotations"));
  }

  public void setMaxSentenceLength(int maxSentenceLength) {
    exec("setMaxSentenceLength", maxSentenceLength);
  }

  public void setMinSentenceLength(int minSentenceLength) {
    exec("setMinSentenceLength", minSentenceLength);
  }

  public void setRemoveQuotations(boolean removeQuotations) {
    exec("setRemoveQuotations", removeQuotations, Boolean.TYPE);
  }
  
  public void setAddSpaces(boolean addSpacesBetweenTokens) {
    exec("setAddSpaces", addSpacesBetweenTokens, Boolean.TYPE);
  }
  
}// end
