package rita.support.ifs;

public interface RiGrammarIF
{
  String expand();
  
  String expandFrom(String s);
  
  String expandWith(String literalString, String productionName);
  
  void setGrammarFromString(String s);
  
  String getGrammarFileName();
}
