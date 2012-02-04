package rita;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.*;

import processing.core.*;

/**
 * Note: still under-development, use w' care...
 * 
 * @invisible
 */
public class RiPageLayout extends RiObject
{
  public static final String NON_BREAKING_SPACE = "<sp>";

  public static final String LINE_BREAK = "<br>";

  public static final String PARAGRAPH = "<p>";
  

  /**  amount of lead to use between lines (default is twice the font-height)  */
  public static float DEFAULT_LEADING = -1;
  
  /** # of (space-width) characters to use as a paragraph indent (default=0) */
  public static int DEFAULT_PARAGRAPH_INDENT = 4;
  
  /** amount of additional lead to add between paragraphs (default is 0)  */
  public static float DEFAULT_PARAGRAPH_SPACING = 0;
  
  /** whether to add an indent to first paragraph for a page layout (default true)  */
  public static boolean DEFAULT_INDENT_FIRST_PARAGRAPH = true;
  
  private static final int PAGE_NO_OFFSET = 35;

  private static final String SPC = " ";

  protected int indents;
  protected PFont font;
  protected float leading, paragraphLeading;
  protected boolean showPageNumbers, indentFirstParagraph;
  protected int pageWidth, pageHeight, pageNo = 1;
  protected float[] textColor;
  
  protected RiText lines[], header, footer;
  protected Rectangle textRectangle;
  protected Stack words;

  public RiPageLayout(PApplet pApplet, int leftMargin, int topMargin, int rightMargin, int bottomMargin)
  {
    this(pApplet, leftMargin, topMargin, rightMargin, bottomMargin, pApplet.width, pApplet.height);
  }

  public RiPageLayout(PApplet pApplet, int leftMargin, int topMargin, int rightMargin, int bottomMargin, int pageWidth, int pageHeight)
  {
    this(pApplet, new Rectangle(leftMargin, topMargin, pageWidth - (leftMargin + rightMargin), pageHeight - (topMargin + bottomMargin)), pageWidth, pageHeight);
  }

  public RiPageLayout(PApplet pApplet, Rectangle rect, int pageWidth, int pageHeight) {
    super(pApplet);
    leading = DEFAULT_LEADING;
    indents = DEFAULT_PARAGRAPH_INDENT;
    paragraphLeading = DEFAULT_PARAGRAPH_SPACING;
    indentFirstParagraph = DEFAULT_INDENT_FIRST_PARAGRAPH;
    // System.out.println("\nleading="+leading+"\nindents="+indents+"\nparagraphLeading="+paragraphLeading);
    //System.out.println("indentFirstParagraph="+indentFirstParagraph+"\n-------------------------------");
    this.textRectangle = rect;
    this.pageWidth = pageWidth;
    this.pageHeight = pageHeight;
  }

  /**
   * Creates an array of RiText, one per line from the text loaded from the
   * specified 'fileName', and lays it out on the page according to the specified
   * font.
   */
  public void layoutFromFile(PFont pf, String fileName)
  {
    String txt = RiTa.loadString(_pApplet, fileName);
    layout(pf, txt.replaceAll("[\\r\\n]", " "));
  }

    /**
   * Creates an array of RiText, one per line from the input text
   * and lays it out on the page.
   * @return number of lines on the page
   */
  public int layout(String text)
  {
    //System.out.println("RiPageLayout.layout("+text.length()+")");
    //if (text.length()<10) System.out.println("->'"+text+"'\n");
    return layout(null, text);
  }
  
  /**
   * Creates an array of RiText, one per line from the input text
   * and lays it out on the page.
   * @return number of lines on the page
   */
  public int layout(PFont pf, String text)
  {
    // System.out.println("RiPageLayout.layout("+text.length()+")");

    if (showPageNumbers)
      setFooter(Integer.toString(pageNo));

    if (text == null || text.length() == 0)
      return 0;

    // remove any line breaks from the original
    text = text.replaceAll("\n", SPC);

    // adds spaces around html tokens
    text = text.replaceAll(" ?(<[^>]+>) ?", " $1 ");

    this.words = new Stack();

    pushLine(text.split(SPC));

    this.lines = renderPage(pf);
    
    return lines.length;
  }

  // add to word stack in reverse order
  private void pushLine(String[] tmp)
  {
    for (int i = tmp.length - 1; i >= 0; i--)
      words.push(tmp[i]);
  }

  RiText[] renderPage(PFont pf)
  {
    if (words.isEmpty()) return new RiText[0];
    
    // use for line/bb-height metrics
    RiText tmp = new RiText(_pApplet, SPC);
    if (pf != null) tmp.textFont(pf);
    Rectangle2D bb = tmp.getBoundingBox();
    float lineHeight = (float) bb.getHeight();
    float textH = tmp.textHeight();
    RiText.delete(tmp);

    float currentH = 0, currentW = 0;
    float maxW = (float) textRectangle.getWidth();
    float maxH = (float) textRectangle.getHeight();
    boolean newParagraph = false; 
    boolean forceBreak = false;
    
    List strLines = new ArrayList();
    StringBuilder sb = new StringBuilder();
    
    while (!words.isEmpty())
    {
      String next = (String) words.pop();
      if (next.length() == 0)
        continue;

      if (next.startsWith("<") && next.endsWith(">"))
      {
        if (next.equals(NON_BREAKING_SPACE) || next.equals("</sp>"))
        {
          sb.append(SPC);
        }
        else if (next.endsWith(PARAGRAPH) || next.equals("</p>"))
        {
          if (sb.length() > 0)      // case: paragraph break
            newParagraph = true;
          else if (indentFirstParagraph)
            addSpaces(sb, indents); // case: first paragraph
        }
        else if (next.endsWith(LINE_BREAK) || next.equals("</br>")) {
          forceBreak = true;
        }
        continue;
      }

      if (pf != null) _pApplet.textFont(pf);
      currentW = _pApplet.textWidth(sb.toString() + next);

      // check line-length & add a word
      if (!newParagraph && !forceBreak && currentW < maxW)
      {
        addWord(sb, next);
      }
      else // new paragraph or line-break
      {
        // check vertical space, add line & next word
        if (checkLineHeight(currentH, textH, maxH))
        {
          addLine(strLines, sb);
          
          if (newParagraph)  { // do indent
            addSpaces(sb, indents);
            if (paragraphLeading>0)
              sb.append('|');
          }
          newParagraph = false;
          forceBreak = false;
          addWord(sb, next);
          currentH += lineHeight;
        }
        else {
          if (next != null)
            words.push(next);
          break;
        }
      }
    }
    // check if leftover words can make a new line
    if (checkLineHeight(currentH, textH, maxH))
      addLine(strLines, sb);
    else
      pushLine(sb.toString().split(SPC));

    RiText[] rts = RiText.createLines
      (_pApplet, pf, (String[]) strLines.toArray(new String[strLines.size()]), 
       textRectangle.x + 1, textRectangle.y + textH - 2, -1, leading);
    
    // set the paragraph spacing
    if (paragraphLeading > 0)  {
      float lead = 0;
      for (int i = 0; i < rts.length; i++)
      {
        int idx = rts[i].indexOf('|');
        if (idx > -1) {
          lead += paragraphLeading;
          rts[i].removeCharAt(idx);
        }
        rts[i].y += lead;
      }
    }

    // double-check that all the lines are in the rect (yuk!)
    RiText check = rts[rts.length - 1];
    while (check.y > textRectangle.y + textRectangle.height)
    {
      String[] chkArr = check.getText().split(SPC);
      rts = RiText.popArray(rts);
      pushLine(chkArr); // re-add words to stack
      check = rts[rts.length - 1];
    }
    
    // and set the color if we have one
    if (textColor != null) {
      for (int i = 0; i < rts.length; i++)
        rts[i].fill(textColor);
    }

    return rts;
  }

  private void addWord(StringBuilder sb, String next)
  {
    sb.append(next + SPC);
  }

  private void addLine(List l, StringBuilder sb)
  {
    String s = sb.toString();
    if (s != null)
    { 
      // strip trailing spaces
      while (s.length() > 0 && s.endsWith(" "))
        s = s.substring(0, s.length() - 1);
    }
    l.add(s); // add && clear the builder

    sb.delete(0, sb.length());
  }

  public RiPageLayout copy()
  {
    if (_pApplet == null)
      throw new RuntimeException("Null pApplet!");
    RiPageLayout rpl = new RiPageLayout(_pApplet, textRectangle, pageWidth, pageHeight);
    rpl.pageNo = pageNo;
    rpl.indents = indents;
    rpl.paragraphLeading = paragraphLeading;
    rpl.pageHeight = pageHeight;
    rpl.leading = leading;
    rpl.font = font;
    rpl.indentFirstParagraph = indentFirstParagraph;
    rpl.pageNo =  pageNo;
    rpl.textColor = textColor;
    rpl.textRectangle = textRectangle;
    rpl.showPageNumbers = showPageNumbers;
    rpl.header = header != null ? header.copy() : null;
    rpl.footer = footer != null ? footer.copy() : null;
    if (lines != null)
    {
      rpl.lines = new RiText[lines.length];
      for (int i = 0; i < lines.length; i++)
        rpl.lines[i] = lines[i].copy();
    }
    if (words != null)
    {
      for (Iterator it = this.words.iterator(); it.hasNext();)
        rpl.words.add(it.next());
    }
    return rpl;
  }

  public void delete()
  {
    RiText.delete(lines);
    RiText.delete(header);
    RiText.delete(footer);
  }
  
  private void addSpaces(StringBuilder sb, int num)
  {
    for (int i = 0; i < num; i++)
      sb.append(SPC+SPC); // ?huh?
  }

  private static boolean checkLineHeight(float currentH, float lineH, float maxH)
  {
    return currentH + lineH <= maxH;
  }

  public void drawGuides()
  {
    this.drawGuides(getPApplet().g);
  }

  public void drawGuides(PGraphics p)
  {
    p.line(textRectangle.x, 0, textRectangle.x, textRectangle.y + pageHeight);
    p.line(textRectangle.x + textRectangle.width, 0, textRectangle.x + textRectangle.width, textRectangle.y + pageHeight);
    p.line(0, textRectangle.y, textRectangle.x + pageWidth, textRectangle.y);
    p.line(0, textRectangle.y + textRectangle.height, textRectangle.x + pageWidth, textRectangle.y + textRectangle.height);
  }

  public int getTopMargin()
  {
    return (int) textRectangle.y;
  }

  public int getLeftMargin()
  {
    return (int) textRectangle.x;
  }

  public int getRightMargin()
  {
    return pageWidth - (textRectangle.x + textRectangle.width);
  }

  public int getBottomMargin()
  {
    return pageHeight - (textRectangle.y + textRectangle.height);
  }

  public Rectangle getTextRectangle()
  {
    return textRectangle;
  }

  public float getTextX()
  {
    return (float) textRectangle.x;
  }

  public float getTextY()
  {
    return (float) textRectangle.y;
  }

  public int getPageWidth()
  {
    return pageWidth;
  }

  public void setPageWidth(int pageWidth)
  {
    this.pageWidth = pageWidth;
  }

  public int getPageHeight()
  {
    return pageHeight;
  }

  public void setPageHeight(int pageHeight)
  {
    this.pageHeight = pageHeight;
  }

  public RiText getHeader()
  {
    return header;
  }

  public void setHeader(RiText header)
  {
    this.header = header;
  }

  public void setHeader(String headerText)
  {
    this.header = new RiText(_pApplet, headerText, textRectangle.x + textRectangle.width / 2f, textRectangle.y - 25);
    header.textAlign(CENTER);
  }

  public RiText getFooter()
  {
    return footer;
  }

  public void setFooter(RiText footer)
  {
    this.footer = footer;
  }

  public void showPageNumbers(boolean showPageNumbers)
  {
    this.showPageNumbers = showPageNumbers;
  }

  public void setFooter(String footerText)
  {
    this.footer = new RiText(_pApplet, footerText, 
        textRectangle.x + textRectangle.width / 2f, textRectangle.y + textRectangle.height + PAGE_NO_OFFSET);
    footer.textAlign(CENTER);
  }

  public void setPageNo(int pageNumber)
  {
    this.pageNo = pageNumber;
  }

  public int getPageNo()
  {
    return pageNo;
  }

  public void setLines(RiText lines[])
  {
    this.lines = lines;
  }

  public RiText[] getLines()
  {
    if (lines == null)
      throw new RiTaException("No text has been assigned to this layout(" + hashCode() + "), make sure to call render() or setLines() first!");
    return lines;
  }

  public RiText[] getWords()
  {
    List l = new ArrayList();
    RiText[] rts = getLines();
    for (int i = 0; i < rts.length; i++)
    {
      RiText[] words = RiText.createWords(_pApplet, rts[i].getText(), rts[i].x, rts[i].y);
      for (int j = 0; j < words.length; j++)
        l.add(words[j]);
    }
    return (RiText[]) l.toArray(new RiText[l.size()]);
  }

  public void setIndentSize(int indentSize)
  {
    this.indents = indentSize;
  }

  public int getIndentSize()
  {
    return indents;
  }

  public String getRemainingText()
  {
    return stackToString(words);
  }

  private String stackToString(Stack words)
  {
    if (words == null) return "";
    StringBuilder sb = new StringBuilder();
    while (!words.isEmpty())
    {
      if (sb.length() > 0)
        sb.append(SPC);
      sb.append(words.pop());
    }
    return sb.toString();
  }

  /** Returns the current leading, or -1 if using the defaults */
  public float getLeading()
  {
    return leading;
  }

  /** Sets the leading.  Use -1 to reset to default leading */
  public void setLeading(float leading)
  {
    this.leading = leading;
  }

  public void setIndents(int indents)
  {
    this.indents = indents;
  }
  
  /**
   * Sets the amount of additional lead to add between paragraphs
   */
  public void setParagraphSpacing(float lead)
  {
    this.paragraphLeading = lead;
  }

    /**
   * Whether to add an indent before the first paragraph
   */
  public void setIndentFirstParagraph(boolean indentFirstParagraph)
  {
    this.indentFirstParagraph = indentFirstParagraph;
  }

  public String toString()
  {
    if (lines == null || lines.length<1)
      return "EMPTY!";
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < lines.length; i++)
      sb.append(lines[i].getText()+" ");
    return sb.toString().trim();
  }

  public void setTextColor(float[] color)
  {
    if (color != null) this.textColor = color;
  }
  
  public static void main(String[] args)
  {
    String text = "Hello. My name is Kevin. ";

    System.out.println(RiTa.replaceEntities(text));
    text = text.replaceAll(" ?(<[^>]+>) ?", " $1 ");
    text = text.replaceAll("(&[^;]+;)", " $1 ");
    System.out.println(text);
  }

}// end
