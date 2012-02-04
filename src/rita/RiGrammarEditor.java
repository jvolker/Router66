package rita;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JToolBar;

import processing.core.PApplet;
import rita.support.RiEditorWindow;
import rita.support.ifs.RiGrammarIF;

/**
 * Provides a live, editable view of a RiGrammar text file
 * that can be dynamically loaded into a sketch without
 * stopping and restarting it. Only one additional line
 * is needed:<pre> 
    RiGrammar rg = new RiGrammar(this, "mygrammar.g");
    rg.openGrammarEditor();  // add this line
    System.out.println(rg.expand());</pre>
 *   
 * 
 * @invisible
 */
public class RiGrammarEditor extends RiEditorWindow {
 
  /** @invisible */
  public RiGrammarIF rg;

  
  /** @invisible */
  public PApplet p;
  

  public RiGrammarEditor(PApplet p, final RiGrammarIF grammar) {
    this(p, grammar, 600, 600);
  }
  
  public RiGrammarEditor(PApplet p, final RiGrammarIF grammar, int width, int height)
  {
    this(p, grammar, positionX(p), positionY(p), width, height);
  }

  private RiGrammarEditor(PApplet p, final RiGrammarIF grammar, int x, int y, int width, int height)
  {
    super("RiGrammarEditor", p, x, y, width, height);
    this.p = p;
    this.rg = grammar;       
    String fileName = grammar.getGrammarFileName();
    if (fileName != null) {
      String contents = loadFileByName(p, fileName);
      rg.setGrammarFromString(contents);
    }
  }

  private static int positionX(PApplet p)
  {
    
    return p == null ? 100 : p.width + 1;
  }
  
  private static int positionY(PApplet p)
  {
    
    return p == null ? 100 : p.getY();
  }
  
  public void addButtons(JToolBar jtbToolBar) {
    super.addButtons(jtbToolBar);
    
    // add refresh button
    JButton jbnToolbarButtons = new JButton("refresh");
    jbnToolbarButtons.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        displayInTextArea("refresh");
        rg.setGrammarFromString(textArea.getText());
      }
    });
    jtbToolBar.add(jbnToolbarButtons);

    jtbToolBar.addSeparator();
  }


}// end