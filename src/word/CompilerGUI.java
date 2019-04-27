package word;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import semantics.SemanticAnalyzer_Boolean;
import javax.swing.JTextArea;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.ImageIcon;

public class CompilerGUI extends JFrame {

  private JPanel contentPane;
  private final Action action = new SwingAction();
  private final Action action_1 = new SwingAction_1();

  private JTextArea textArea_word;
  private JTextArea textArea_text;
  private JTextArea textArea_grammar;
  private JTextArea textArea_semantic;
  private final Action action_2 = new SwingAction_2();
  private final Action action_3 = new SwingAction_3();
  private final Action action_4 = new SwingAction_4();

  /**
   * Launch the application.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          CompilerGUI frame = new CompilerGUI();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Create the frame.
   * 
   * @throws IOException
   */
  public CompilerGUI() throws IOException {
    setTitle("LR1分析器");
    setVisible(true);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setBounds(100, 100, 744, 514);

    JMenuBar menuBar = new JMenuBar();
    setJMenuBar(menuBar);

    JMenu menu = new JMenu("\u6587\u4EF6");
    menuBar.add(menu);

    JMenuItem menuItem = new JMenuItem("\u9009\u62E9\u5206\u6790\u6587\u4EF6");
    menuItem.setAction(action_1);
    menu.add(menuItem);

    JMenuItem menuItem_1 = new JMenuItem("\u6E05\u7A7A\u5F53\u524D\u5C4F\u5E55");
    menuItem_1.setAction(action_2);
    menu.add(menuItem_1);

    JMenuItem menuItem_2 = new JMenuItem("\u9000\u51FA\u7A0B\u5E8F");
    menuItem_2.setAction(action_3);
    menu.add(menuItem_2);
    
    JMenu menu_2 = new JMenu("\u67E5\u770B");
    menuBar.add(menu_2);
    
    JMenuItem menuItem_4 = new JMenuItem("\u67E5\u770B\u8BED\u6CD5\u5206\u6790\u8868");
    menuItem_4.setAction(action_4);
    menu_2.add(menuItem_4);

    JMenu menu_1 = new JMenu("\u5173\u4E8E");
    menuBar.add(menu_1);

    JMenuItem menuItem_3 = new JMenuItem("\u5173\u4E8E\u672C\u7A0B\u5E8F");
    menuItem_3.setAction(action);
    menu_1.add(menuItem_3);
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(contentPane);
    contentPane.setLayout(null);

    textArea_word = new JTextArea();
    JScrollPane js = new JScrollPane(textArea_word);
    js.setBounds(14, 224, 223, 172);
    textArea_word.setBounds(14, 224, 223, 172);
    textArea_word.setEditable(false);
    contentPane.add(js);

    textArea_text = new JTextArea();
    JScrollPane js2 = new JScrollPane(textArea_text);
    js2.setBounds(14, 13, 223, 172);
    textArea_text.setBounds(14, 13, 223, 172);
    textArea_text.setEditable(false);
    contentPane.add(js2);

    textArea_grammar = new JTextArea();
    JScrollPane js3 = new JScrollPane(textArea_grammar);
    js3.setBounds(251, 14, 223, 382);
    textArea_grammar.setBounds(251, 14, 223, 382);
    textArea_grammar.setEditable(false);
    contentPane.add(js3);

    textArea_semantic = new JTextArea();
    JScrollPane js4 = new JScrollPane(textArea_semantic);
    js4.setBounds(488, 13, 223, 382);
    textArea_semantic.setBounds(488, 13, 223, 382);
    textArea_semantic.setEditable(false);
    contentPane.add(js4);

    JLabel label = new JLabel("\u6587\u672C\u5185\u5BB9");
    label.setBounds(91, 198, 72, 18);
    contentPane.add(label);

    JLabel label_1 = new JLabel("\u8BCD\u6CD5\u5206\u6790\u540E");
    label_1.setBounds(81, 409, 82, 18);
    contentPane.add(label_1);

    JLabel label_2 = new JLabel("\u8BED\u6CD5\u5206\u6790\u7ED3\u679C");
    label_2.setBounds(315, 409, 101, 18);
    contentPane.add(label_2);

    JLabel label_3 = new JLabel("\u8BED\u4E49\u5206\u6790\u7ED3\u679C");
    label_3.setBounds(563, 408, 90, 18);
    contentPane.add(label_3);

  }

  private class SwingAction extends AbstractAction {
    public SwingAction() {
      putValue(NAME, "关于本程序");
      putValue(SHORT_DESCRIPTION, "关于本程序的信息");
    }

    public void actionPerformed(ActionEvent e) {
      JOptionPane.showMessageDialog(contentPane, "LR1分析器 by Dotman(R)2018");
    }
  }
  private class SwingAction_1 extends AbstractAction {
    public SwingAction_1() {
      putValue(NAME, "选择待分析文件");
      putValue(SHORT_DESCRIPTION, "选择需要LR分析的文本文件");
    }

    public void actionPerformed(ActionEvent e) {
      JFileChooser filechooser = new JFileChooser();
      filechooser.showDialog(new JLabel(), "选择");
      File file = filechooser.getSelectedFile();

      try {
        InputStream in = new FileInputStream(file);
        int flen = (int) file.length();
        byte[] strBuffer = new byte[flen];
        in.read(strBuffer, 0, flen);

        String tmp2 = new String(strBuffer);
        textArea_text.setText(tmp2);
        WordAnalyzer analyzer = new WordAnalyzer();
        analyzer.analyze(tmp2.toCharArray());
        String tmp = "";
        for (Token t : analyzer.tokens) {
          tmp += t.toString() + "\n";
        }
        textArea_word.setText(tmp);

        SemanticAnalyzer_Boolean semantic = new SemanticAnalyzer_Boolean(analyzer.tokens);
        textArea_grammar.setText(semantic.getGrammarAnalysisProc());
        textArea_semantic.setText(semantic.getSemanticAnalysisProc());

      } catch (FileNotFoundException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      } catch (IOException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }

    }
  }
  private class SwingAction_2 extends AbstractAction {
    public SwingAction_2() {
      putValue(NAME, "清空当前屏幕");
      putValue(SHORT_DESCRIPTION, "清空当前屏幕所有显示内容");
    }

    public void actionPerformed(ActionEvent e) {
      textArea_grammar.setText("");
      textArea_semantic.setText("");
      textArea_text.setText("");
      textArea_word.setText("");
    }
  }
  private class SwingAction_3 extends AbstractAction {
    public SwingAction_3() {
      putValue(NAME, "退出程序");
      putValue(SHORT_DESCRIPTION, "再见");
    }

    public void actionPerformed(ActionEvent e) {
      System.exit(0);
    }
  }
  private class SwingAction_4 extends AbstractAction {
    public SwingAction_4() {
      putValue(NAME, "查看语法分析表");
      putValue(SHORT_DESCRIPTION, "查看当前使用语法的语法分析表");
    }
    public void actionPerformed(ActionEvent e) {
      JFrame jmptable=new JFrame("语法分析表");
      jmptable.setSize(1053,1254);
      jmptable.setVisible(true);
      jmptable.add(new JLabel(){
        @Override
        public void paintComponent(Graphics g) {
          Image i=new ImageIcon("C:\\Users\\samma\\Desktop\\Compiling System\\1160801026_王弘毅_Lab3\\新语法分析表.JPG").getImage();
          g.drawImage(i, 0, 0, this);
        }
      });
    }
  }
}
