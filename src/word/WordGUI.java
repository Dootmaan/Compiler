package word;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextArea;

public class WordGUI extends JFrame {

  private JPanel contentPane;

  /**
   * Launch the application.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          WordGUI frame = new WordGUI();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Create the frame.
   * @throws IOException 
   */
  public WordGUI() throws IOException {
    setTitle("´Ê·¨·ÖÎö");
    setVisible(true);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setBounds(100, 100, 553, 478);
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(contentPane);
    contentPane.setLayout(null);
    
    JTextArea textArea = new JTextArea();
    JScrollPane js=new JScrollPane(textArea);
    textArea.setBounds(283, 13, 238, 371);
    js.setBounds(283, 13, 238, 371);
    contentPane.add(js);
        
    JTextArea textArea_1 = new JTextArea();
    JScrollPane js2=new JScrollPane(textArea_1);
    textArea_1.setBounds(14, 14, 255, 370);
    js2.setBounds(14, 14, 255, 370);
    contentPane.add(js2);
    
    JFileChooser filechooser=new JFileChooser();
    filechooser.showDialog(new JLabel(), "Ñ¡Ôñ");    
    File file=filechooser.getSelectedFile();
    
    InputStream in = new FileInputStream(file);
    int flen = (int)file.length();
    byte[] strBuffer = new byte[flen];
    in.read(strBuffer, 0, flen);
    
    String tmp2=new String(strBuffer);
    textArea_1.setText(tmp2);
    WordAnalyzer analyzer=new WordAnalyzer();
    analyzer.analyze(tmp2.toCharArray());
    String tmp="";
    for(Token t:analyzer.tokens) {
      tmp+=t.toString()+"\n";
    }
    textArea.setText(tmp);
    
    JLabel label = new JLabel("\u6587\u672C\u5185\u5BB9");
    label.setBounds(103, 397, 72, 18);
    contentPane.add(label);
    
    JLabel label_1 = new JLabel("\u8BCD\u6CD5\u5206\u6790\u540E");
    label_1.setBounds(366, 397, 82, 18);
    contentPane.add(label_1);

  }
}
