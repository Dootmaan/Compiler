package word;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordAnalyzer {
  List<Token> tokens=new ArrayList<>();
  static Map<String, Info> symbol_chart = new HashMap<>(); // ���ű�ʹ��map��Ϊ�˼ӿ��ѯ�ٶȣ�ʵ���Ͽ������б�
  private Map<String,String> names=new HashMap<>();

  private String[] keywords =
      {"do", "while", "if", "else", "int", "float", "boolean", "struct", "break", "continue","call"};
  private char[] ops = {'+', '*', '/', '-', '!', '&', '^', '|', '=', '>', '<','~'};
  private String[] op2s = {"&&", "||", ">=", "<=", "==", "!=", "/*", "*/"};
  private char[] borders = {';', '(', '{', ')', '}', '[', ']', ',','#'};
  
  public WordAnalyzer() {  //����Ϊ����ʾ����������
    names.put("+", "PLUS");
    names.put("*", "MULT");
    names.put("/","DIV");
    names.put("-", "MINUS");
    names.put("!", "NOT");
    names.put("&", "BAND");
    names.put("^", "BXOR");
    names.put("|", "BOR");
    names.put("~","BNOT");
    names.put("=", "ASG");
    names.put(">", "GRT");
    names.put("<", "SML");
    names.put("&&", "AND");
    names.put("||", "OR");
    names.put(">=", "GRTE");
    names.put("<=", "SMLE");
    names.put("==", "EQL");
    names.put("!=", "NEQL");
    names.put("/*","NTES");
    names.put("*/", "NTEE");
    names.put(";", "SEMI");
    names.put("(", "SLP");
    names.put("{", "LP");
    names.put(")", "SRP");
    names.put("}", "RP");
    names.put("[", "MLP");
    names.put("]", "MRP");
    names.put(",", "COMMA");
  }

  boolean isKey(String str) {
    for (int i = 0; i < keywords.length; i++) {
      if (keywords[i].equals(str))
        return true;
    }
    return false;
  }

  boolean isLetter(char letter) {
    if ((letter >= 'a' && letter <= 'z') || (letter >= 'A' && letter <= 'Z') || letter == '_')
      return true;
    else
      return false;
  }

  boolean isDigit(char digit) {
    if (digit >= '0' && digit <= '9')
      return true;
    else
      return false;
  }

  boolean isBorder(char border) {
    for (char c : borders) {
      if (border == c) {
        return true;
      }
    }
    return false;
  }

  boolean isOp(char op) {
    for (char c : ops) {
      if (op == c) {
        return true;
      }
    }
    return false;
  }

  boolean isOp(String str) {
    for (String s : op2s) {
      if (str.equals(s)) {
        return true;
      }
    }
//    for (char c : ops) {
//      if (str.charAt(0) == c) {
//        return true;
//      }
//    }
    return false;
  }

  void analyze(char[] chars) {
    String arr = "";
    char ch;
    int i;
    int length = chars.length;
    for (i = 0; i < length; i++) {
      ch = chars[i];
      arr = "";
      if (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r') {
      } else if (isLetter(ch)) {
        while (isLetter(ch) || isDigit(ch)) {
          arr += ch;
          ch = chars[++i];
        }
        // ����һ���ַ�
        i--;
        if (isKey(arr)) {
          // �ؼ���
          System.out.println(arr + "\t<" +arr.toUpperCase()+ ",_>");
          tokens.add(new Token(arr,arr.toUpperCase(),"_"));
        } else {
          // ��ʶ��
          if(!symbol_chart.containsKey(arr)) {
            symbol_chart.put(arr, new Info(arr));
          }
          System.out.println(arr + "\t<IDN" + ","+symbol_chart.get(arr)+">");
          tokens.add(new Token("id","IDN",symbol_chart.get(arr)));
        }
      } else if (isDigit(ch) || (ch == '.')) {  //.34�ڱ�������Ҳ�Ϸ����ᱻʶ��Ϊ0.34

        int state=0;
        if(ch=='.') {
          arr="0";
        }
        while(isDigit(ch)) {
          arr+=ch;
          ch=chars[++i];
        }
        state=1;
        if(ch=='.') {
          arr+=ch;
          state=2;
          ++i;
          ch=chars[i];  //�ƶ���С�������һ���ַ�
          if(isDigit(ch)) {
            state=3;
          }
          while(isDigit(ch)) {
            arr+=ch;
            ch=chars[++i];
          }
          if(state==3 && ch=='e') {
            arr+=ch;
            state=4;
            ++i;
            ch=chars[i];
            if(ch=='+'||ch=='-'||isDigit(ch)) {
              if(isDigit(ch)) {
                while(isDigit(ch)) {
                  arr+=ch;
                  ch=chars[++i];
                }
                i--;
                state=7;
                System.out.println(arr+"\t<SCI,"+arr+">");
                tokens.add(new Token("num","SCI",arr));
              }else {
                arr+=ch;
                ++i;
                ch=chars[i];
                if(isDigit(ch)) {
                  while(isDigit(ch)) {
                    arr+=ch;
                    ch=chars[++i];
                  }
                  i--;
                  state=7;
                  System.out.println(arr+"\t<SCI,"+arr+">");
                  tokens.add(new Token("num","SCI",arr));
                }else {
                  System.out.println(arr+"����e���治��ֻ�з���");
                  i--;
                }
              }
            }else {
              System.out.println(arr+"����e����ʲô��û��");
              i--;
            }
          }else if(state==3){  //������
            state=7;
            i--;
            System.out.println(arr+"\t<FLOAT,"+arr+">");
            tokens.add(new Token("num","FLOAT",arr));
          }else {
            state=-1;
            i--;
            System.out.println(arr+"\t����С�������Ϊ��");
          }
        }else if(ch=='e') {
          arr+=ch;
          state=4;
          ++i;
          ch=chars[i];
          if(ch=='+'||ch=='-'||isDigit(ch)) {
            if(isDigit(ch)) {
              while(isDigit(ch)) {
                arr+=ch;
                ch=chars[++i];
              }
              i--;
              state=7;
              System.out.println(arr+"\t<SCI,"+arr+">");
              tokens.add(new Token("num","SCI",arr));
            } else {
                arr+=ch;
                ++i;
                ch=chars[i];
                if(isDigit(ch)) {
                  while(isDigit(ch)) {
                    arr+=ch;
                    ch=chars[++i];
                  }
                  i--;
                  state=7;
                  System.out.println(arr+"\t<SCI,"+arr+">");   //5e2
                  tokens.add(new Token("num","SCI",arr));
                }else {
                  System.out.println(arr+"����e���治��ֻ�з���");
                  i--;
                }
            }
          }
        }else {   //����
          state=7;
          System.out.println(arr+"\t<CONST,"+arr+">");
          tokens.add(new Token("num","CONST",arr));
          i--;
        }
      } else if (isBorder(ch)) {
        System.out.println(ch+"\t<"+names.get(String.valueOf(ch))+",_>");
        tokens.add(new Token(String.valueOf(ch),names.get(String.valueOf(ch)),"_"));
      } else if (isOp(ch)) {
        int count=0;
        while (isOp(ch)&&count<2) {    //Ŀǰ�����������Ϊ2
          count++;
          arr += ch;
          ch = chars[++i];
        }
        i--;
        if(isOp(arr)) {
          if(arr.equals("/*")) {
            while(true) {
              ch=chars[++i];
//              System.out.print(ch);     //ȥ����ע�������̨����ʾ�ı��е�ע������/*cc*/
              if(ch=='*') {
                if(chars[++i]=='/') {
//                  System.out.println(); //ȥ����ע�������̨����ʾ�ı��е�ע������/*cc*/
//                  System.out.println("*/"+"\t<"+"*/"+",_>");   //��������ʾע�͵Ŀ�ʼ��������ֻ��Ҫע��
                  break;
                }else {
                  i--;
                }
              }
            }
          }else {
            System.out.println(arr+"\t<"+names.get(arr)+",_>");
            tokens.add(new Token(arr,names.get(String.valueOf(arr)),"_"));
          }
        }else {   //˵����������ϲ��Ϻ����壬���Կ������Ƿֿ�ʶ��
          char[] tmp_chars=arr.toCharArray();
          for(char c:tmp_chars) {
            if(isOp(c)) {
              System.out.println(c+"\t<"+names.get(String.valueOf(c))+",_>");
              tokens.add(new Token(String.valueOf(c),names.get(String.valueOf(c)),"_"));
            }else {
              System.out.println(c+"\t<ERR,_>");   //ʵ���ϲ������ߵ������Ϊȷ������Op�Ż�ӵ�arr��
            }
          }
        }
      }else {
        System.out.println(ch+"\t<NULL,_>");
      }
    }
    System.out.println(symbol_chart);
  }
}
