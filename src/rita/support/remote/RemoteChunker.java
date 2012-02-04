package rita.support.remote;

import java.util.List;

import rita.support.ifs.RiChunkerIF;
import rita.support.me.MaxEntChunker;
import rita.support.me.RiObjectME;

public class RemoteChunker extends RiClientStub implements RiChunkerIF
{
  public RemoteChunker(Class delegatesTo) {
    super(delegatesTo);   
    initMap.put("modelDir", RiObjectME.getModelDir());
    createRemote();
  }

  public String chunk(List tokens, List tags)
  {
    return chunk(listToStrArr(tokens), listToStrArr(tags));
  }

  public String chunk(String[] tokens, String[] tags)
  {
    return exec("chunk", new Object[]{tokens,tags}, new Class[]{ String[].class, String[].class});
  }

  public String tagAndChunk(String sentence)
  {
    return exec("tagAndChunk", sentence, String.class);
  }

  public String[] getChunkData()
  {
    return toStrArr(exec("getChunkData"));
  }
  
  private String[] getChunksByType(String type)
  {
    return toStrArr(exec("getChunksByType", type, String.class));
  }

  public String[] getNounPhrases() { return getChunksByType(MaxEntChunker.NOUN_PHRASE); }
  public String[] getVerbPhrases() { return getChunksByType(MaxEntChunker.VERB_PHRASE); }  
  public String[] getPrepPhrases() { return getChunksByType(MaxEntChunker.PREP_PHRASE); }
  public String[] getAdjPhrases()  { return getChunksByType(MaxEntChunker.ADJ_PHRASE);  }
  public String[] getAdvPhrases()  { return getChunksByType(MaxEntChunker.ADV_PHRASE);  }

}// end
