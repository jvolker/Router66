package rita.support.remote;

import rita.support.ifs.RiParserIF;
import rita.support.me.RiObjectME;

public class RemoteParser extends RiClientStub implements RiParserIF
{
  public RemoteParser(Class delegatesTo) {
    super(delegatesTo);   
    initMap.put("modelDir", RiObjectME.getModelDir());
//System.out.println("RemoteParser.RemoteParser()");
    createRemote();
  }

  public String parse(String text)
  {
    return exec("parse", text, String.class);
  }

  /*public String parse(String text, boolean indent)
  {
    return exec("parse", new Object[] { text,indent }, 
      new Class[] { String.class, Boolean.TYPE});
  }*/

}// end
