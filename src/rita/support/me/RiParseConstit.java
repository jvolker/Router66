package rita.support.me;

import java.util.List;

import opennlp.tools.parser.Parse;
import opennlp.tools.util.Span;


// NOT USESD FOR NOW
/** Data structure for holding parser constitents. */
public class RiParseConstit extends Parse 
{
  private static final boolean DBUG = false;

  public RiParseConstit(String text, Span span, String type, double p, RiParseConstit h) {
    super(text, span, type, p, h);
  }
  
  public RiParseConstit(String text, Span span, String type, double p, int i) {
    super(text, span, type, p, i);
  }

  /**
   * Inserts the specified constituent into this parse based on its text span.  This
   * method assumes that the specified constituent can be inserted into this parse.
   * @param constituent The constituent to be inserted.
   */
  public void insert(final Parse constituent) {
    Span span = getSpan();
    List parts = getParts();
    Span ic = constituent.getSpan();
    if (span.contains(ic)) {
      //double oprob=c.prob;
      int pi=0;
      int pn = parts.size();
      for (; pi < pn; pi++) {
        Parse subPart = (RiParseConstit) parts.get(pi);
if (DBUG)System.err.println("Parse.insert:con="+constituent+"  sp["+pi+"] "+subPart+" "+subPart.getType());
        Span sp = subPart.getSpan();
        if (sp.getStart() >= ic.getEnd()) {
          break;
        }
        // constituent contains subPart
        else if (ic.contains(sp)) {
          //System.err.println("Parse.insert:con contains subPart");
          parts.remove(pi);
          pi--;
          constituent.getParts().add(subPart);
          subPart.setParent(constituent);
if (DBUG)System.err.println("Parse.insert: "+subPart.hashCode()+" -> "+subPart.getParent().hashCode());
          pn = parts.size();
        }
        else if (sp.contains(ic)) {
          //System.err.println("Parse.insert:subPart contains con");
          subPart.insert(constituent);
          return;
        }
      }
if (DBUG)System.err.println("Parse.insert:adding con="+constituent+" to: "+this);
      parts.add(pi, constituent);
      constituent.setParent(this);
if (DBUG)System.err.println("Parse.insert: "+constituent.hashCode()+" -> "+constituent.getParent().hashCode());
    }
    else {
      throw (new InternalError("RiParseConstit: Inserting constituent not contained in the sentence: "+ic));
    }
  }
  
}