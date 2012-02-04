package rita.support.ifs;

public interface RiParserIF
{
  /**
   * Returns the String of the most probable parse 
   * using the Penn Treebank's inline format. 
   */
  public String parse(String text);

}// end
