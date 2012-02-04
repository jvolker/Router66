package rita.support;

import java.util.List;

import processing.core.PApplet;
import rita.RiObject;
import rita.support.ifs.RiTokenizerIF;

// BUGS/TODO:
// Doesn't handle decimal numbers correctly! 
//  eg "5 cookies divided by 2 is 2.5 cookies."
// All regexs shoul dbe compiled!
//  

/**
 * Simple word tokenizer that tokenizes according to the Penn Treebank conventions.
 */
public final class PennWordTokenizer extends RiObject implements RiTokenizerIF 
{
  private static final String SPC = " ";
  
  protected boolean splitContractions;
  
  public PennWordTokenizer() {
    this(null);    
  }
  
  public PennWordTokenizer(PApplet pApplet) {
    this(pApplet, true);    
  }
  
  
  public PennWordTokenizer(boolean splitContractions) {
    this(null, splitContractions);
  }
  
  public PennWordTokenizer(PApplet pApplet, boolean splitContractions) {
    super(pApplet);   
    this.splitContractions = splitContractions;
  }   
  
  // METHODS -------------------------------------------

  public String tokenizeInline(String words) {
     throw new RuntimeException("unimplemented!");
  }  
  
  /*private static String convertToken(String token)
  {
    if (token.equals("(")) return "-LRB-";
    else if (token.equals(")")) return "-RRB-";
    else if (token.equals("{")) return "-LCB-";
    else if (token.equals("}")) return "-RCB-";
    return token;
  }*/
  
  /**
   * Tokenizes the String according to the Penn Treebank conventions
   * and stores the result as a List in <code>result</code>
   */
  public void tokenize(String words, List result) 
  {
    String[] tokens = tokenize(words);
    for (int i = 0; i < tokens.length; i++)
      result.add(tokens[i]);
  }
  
	/**
	 * Tokenizes the String according to the Penn Treebank conventions.
	 */
	public String[] tokenize(String words) 
  {
	  
	  // these should all be compiled!!!!
		words = words.replaceAll("``", "`` ");
		words = words.replaceAll("''", "  ''");		
		
		words = words.replaceAll("([\\?!\"\\.,;:@#$%&])", " $1 ");
			
		//String last = words;
		//words = words.replaceAll("([0-9][0-9]*[\\.]{0,1}[0-9][0-9]*)", " $1 ");		
		//words = words.replaceAll("([\\.])", " $1 ");
		
		words = words.replaceAll("\\.\\.\\.", " ... ");
		words = words.replaceAll("\\s+", SPC);

		words = words.replaceAll(",([^0-9])", " , $1");

		words = words.replaceAll("([^.])([.])([\\])}>\"']*)\\s*$", "$1 $2$3 ");

		words = words.replaceAll("([\\[\\](){}<>])", " $1 ");
		words = words.replaceAll("--", " -- ");

		words = words.replaceAll("$", SPC);
		words = words.replaceAll("^", SPC);

		//str = str.replaceAll("\"", " '' ");
		words = words.replaceAll("([^'])' ", "$1 ' ");
    
    
		//words = words.replaceAll("'([sSmMdD]) ", " '$1 ");
    
    // DCH!! (changed from ^, only on a proper name(starting cap)?)
    words = words.replaceAll("'([SMD]) ", " '$1 "); 
            
    if (splitContractions) {
  		words = words.replaceAll("'ll ", " 'll ");
  		words = words.replaceAll("'re ", " 're ");
  		words = words.replaceAll("'ve ", " 've ");
  		words = words.replaceAll("n't ", " n't ");
  		words = words.replaceAll("'LL ", " 'LL ");
  		words = words.replaceAll("'RE ", " 'RE ");
  		words = words.replaceAll("'VE ", " 'VE ");
  		words = words.replaceAll("N'T ", " N'T ");
    }

		words = words.replaceAll(" ([Cc])annot ", " $1an not ");
		words = words.replaceAll(" ([Dd])'ye ", " $1' ye ");
		words = words.replaceAll(" ([Gg])imme ", " $1im me ");
		words = words.replaceAll(" ([Gg])onna ", " $1on na ");
		words = words.replaceAll(" ([Gg])otta ", " $1ot ta ");
		words = words.replaceAll(" ([Ll])emme ", " $1em me ");
		words = words.replaceAll(" ([Mm])ore'n ", " $1ore 'n ");
		words = words.replaceAll(" '([Tt])is ", " $1 is ");
		words = words.replaceAll(" '([Tt])was ", " $1 was ");
		words = words.replaceAll(" ([Ww])anna ", " $1an na ");

		// "Nicole I. Kidman" gets tokenized as "Nicole I . Kidman"
		words = words.replaceAll(" ([A-Z]) \\.", " $1. ");
		words = words.replaceAll("\\s+", SPC);
		words = words.replaceAll("^\\s+", "");           

		String[] result = words.split(SPC);
    /*for (int i = 0; i < result.length; i++)
      result[i] = convertToken(result[i]);*/
    return result;     // is this ^ necessary ?? 
	}
  
  public boolean isSplittingContractions()
  {
    return this.splitContractions;
  }

  public void setSplitContractions(boolean splitContractions)
  {
    this.splitContractions = splitContractions;
  }
  
  public static void main(String[] args)
  {
    String text= "2";//, that's why this is our place).";//"McDonald's - it cannot happen" , "McDonald's, it cannot happen"  };
    //for (int j = 0; j < 1; j++)
         
    //String text = texts[j];    
    PennWordTokenizer tk = new PennWordTokenizer();
    tk.splitContractions = false;
    String[] toks = tk.tokenize(text);
    System.out.print("[");
    for (int i = 0; i < toks.length; i++)
      System.out.print("'"+toks[i]+"' "); 
    System.out.println("]");
    
   /* tk.setSplitContractions(false);
    
    List l = new ArrayList();
    tk.tokenize(text, l);
    System.out.print("[");
    for (int i = 0; i < l.size(); i++)
      System.out.print("'"+l.get(i)+"' ");
    System.out.print("]");*/
  }
}