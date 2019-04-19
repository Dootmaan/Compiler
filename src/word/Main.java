package word;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import grammar.GrammarAnalyzer;

public class Main {

  public static void main(String[] args) {
    // TODO Auto-generated method stub
    
    try {
      File file = new File("D:\\test_compile.txt");//����һ��file����������ʼ��FileReader
      FileReader reader;
      reader = new FileReader(file);
      int length = (int) file.length();
      char buf[] = new char[length+1];
      reader.read(buf);
      reader.close();
      WordAnalyzer word=new WordAnalyzer();
      word.analyze(buf);
      new GrammarAnalyzer(word.tokens);
      
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    

  }

}
