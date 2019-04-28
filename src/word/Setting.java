package word;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Setting extends JFrame {

  private JPanel contentPane;

  /**
   * Create the frame.
   */
  public Setting() {
    setVisible(true);
    setTitle("∆´∫√…Ë÷√");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setBounds(100, 100, 296, 247);
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(contentPane);
    contentPane.setLayout(null);
    
    JCheckBox checkBox = new JCheckBox("\u542F\u7528\u7C7B\u578B\u8F6C\u6362");
    checkBox.setBounds(72, 35, 133, 27);
    checkBox.setSelected(semantics.SemanticAnalyzer_Boolean.type_convert);
    contentPane.add(checkBox);
    
    JCheckBox checkBox_1 = new JCheckBox("\u9519\u8BEF\u540E\u9000\u51FA\u7A0B\u5E8F");
    checkBox_1.setBounds(72, 80, 133, 27);
    checkBox_1.setSelected(semantics.SemanticAnalyzer_Boolean.exit_after_error);
    contentPane.add(checkBox_1);
    
    JButton button = new JButton("\u786E\u5B9A");
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        semantics.SemanticAnalyzer_Boolean.exit_after_error=checkBox_1.isSelected();
        semantics.SemanticAnalyzer_Boolean.type_convert=checkBox.isSelected();
        setVisible(false);
      }
    });
    button.setBounds(32, 138, 94, 27);
    contentPane.add(button);
    
    JButton btnNewButton = new JButton("\u53D6\u6D88");
    btnNewButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        setVisible(false);
      }
    });
    btnNewButton.setBounds(151, 138, 94, 27);
    contentPane.add(btnNewButton);
  }
}
