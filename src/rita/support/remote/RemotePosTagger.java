package rita.support.remote;

import java.util.List;

import rita.support.ifs.RiTaggerIF;
import rita.support.me.RiObjectME;

public class RemotePosTagger extends RiClientStub implements RiTaggerIF
{
  public RemotePosTagger(Class delegatesTo) {
    super(delegatesTo);
    initMap.put("modelDir", RiObjectME.getModelDir());
    createRemote();
  }

  public boolean isAdjective(String word)
  {
    return Boolean.parseBoolean(exec("isAdjective", word, String.class));
  }

  public boolean isAdverb(String word)
  {
    return Boolean.parseBoolean(exec("isAdjective", word, String.class));
  }

  public boolean isNoun(String word)
  {
    return Boolean.parseBoolean(exec("isNoun", word, String.class));
  }

  public boolean isVerb(String word)
  {
    return Boolean.parseBoolean(exec("isVerb", word, String.class));
  }

// dont we handle Lists now?????
  public List tag(List tokens)
  {
    throw new RiMethodMissing(this, "tag", tokens, List.class);
  }

  public String[] tag(String[] tokens)
  {
    return toStrArr(exec("tag", tokens, String[].class))  ;
  }

  public String tagInline(String[] tokens)
  {
    return exec("tagInline", tokens, String[].class);
  }

  public String tagInline(String sentence)
  {
    return exec("tagInline", sentence, String.class);
  }

  public void destroy()
  {
    throw new RuntimeException("should never be called!");
  }

  public String[] tagFile(String fileName) 
  {
    return toStrArr(exec("tagFile", fileName, String.class));
  }
  
  public static void main(String[] args) {

  }

  
}// end
