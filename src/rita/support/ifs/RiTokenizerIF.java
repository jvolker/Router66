package rita.support.ifs;

import java.util.List;

public interface RiTokenizerIF
{
  public String[] tokenize(String sentence);
  
  public void tokenize(String sentence, List result);
  
}
