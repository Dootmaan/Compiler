package word;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import grammar.GrammarAnalyzer;
import grammar.GrammarAnalyzer_LR1;
import grammar.GrammarAnalyzer_��;
import semantics.SemanticAnalyzer_Boolean;
import semantics.SemanticAnalyzer_LR1;

public class Main {

  public static void main(String[] args) {
    // TODO Auto-generated method stub
    
    try {
      JFileChooser filechooser=new JFileChooser();
      filechooser.showDialog(new JLabel(), "ѡ��");    
      File file = filechooser.getSelectedFile();//����һ��file����������ʼ��FileReader
      FileReader reader;
      reader = new FileReader(file);
      int length = (int) file.length();
      char buf[] = new char[length+1];
      reader.read(buf);
      reader.close();
      WordAnalyzer word=new WordAnalyzer();
      word.analyze(buf);
      new SemanticAnalyzer_Boolean(word.tokens);
      
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    

  }

}
