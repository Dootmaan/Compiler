package semantics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import word.Token;

public class SemanticAnalyzer_Boolean {

  // 要求文法非终结符只能由大写字母开头，各个符号用空格隔开
  // private String[] expression = {"S'->S", "S->B B", "B->float B", "B->int"};
  // private String[] expression = {"S'->S", "S->L = R", "S->R", "L->* R", "L->id", "R->L"};
  // private String[] expression;
  // private String[] expression = {"S'->S",
  // "S->do { D } while ( C )",
  // "C->id == num",
  // "D->A",
  // "A->id = num ;"};
  private String[] expression =
      {"S'->S", "S->S + T", "S->T", "T->T * F", "T->F", "F->( S )", "F->id"};
  private DFAGraph dfa = new DFAGraph();
  private int state_number = 0;
  private StringBuilder grammar_text = new StringBuilder();
  private StringBuilder semantic_text = new StringBuilder();

  //检测到语义错误后是否退出
  private boolean exit_after_error = false;   
  
  //是否启用类型转换
  private boolean type_convert = true;

  // 可视化
  public String getGrammarAnalysisProc() {
    return grammar_text.toString();
  }

  public String getSemanticAnalysisProc() {
    return semantic_text.toString();
  }
  //

  /*
   * String是对应的列属性名称，String[]是该列所有的信息，其长度应该全部保持一致，这样设置的好处是可以直接通过行数作为String[]的下标来找到信息。
   */
  private Map<String, String[]> jmptable = new HashMap<>();
  private Map<String, String[]> ACTION = new HashMap<>();
  private Map<String, String[]> GOTO = new HashMap<>();
  // private List<SemanticCode> codes = new ArrayList<>();

  private Stack<Integer> state_stack = new Stack<>();
  private Stack<String> symbol_stack = new Stack<>();

  private Stack<Map<String, String>> semantic_stack = new Stack<>();

  public SemanticAnalyzer_Boolean(List<Token> tokens) {
    try {
      // 读文件
      FileInputStream is = new FileInputStream(new File("D:\\grammar_LR1_boolean.txt"));

      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      List<String> exps = new ArrayList<>();
      String str;
      while ((str = reader.readLine()) != null) {
        if (str.startsWith("//")) { // 允许添加注释
          continue;
        }
        exps.add(str);
      }
      this.expression = new String[exps.size()];
      int i;
      for (i = 0; i < exps.size(); i++) {
        expression[i] = exps.get(i);
      }
      reader.close();
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    // 读文件到这里结束

    initDFA();
    State start = dfa.getStates().get(0);
    buildDFA(start);
    System.out.println("done");

    buildTable();
    analyze(tokens);

  }

  private void Closure(State state) {
    int i;
    for (i = 0; i < state.getItems().size(); i++) {
      // String itemmm=state.getItems().get(i);
      String item = state.getItems().get(i).split(",")[0]; // 先去掉搜索符
      String search = state.getItems().get(i).split(",")[1]; // 先提取搜索符，方便后面使用
      String[] tmp = item.split("\\.");
      if (tmp.length == 1) { // 说明规约完了
        continue;
      }
      if (Character.isUpperCase(tmp[1].charAt(0))) { // 需要计算闭包
        String start = tmp[1].split(" ")[0];
        for (String s : expression) {
          String[] tmp_s = s.split("->");
          if (tmp_s[0].equals(start)) {
            String newitem;
            if (tmp_s[1].equals("ε")) {
              newitem = tmp_s[0] + "-> .," + search;
              if (tmp[1].split(" ").length > 1) {
                if (Character.isUpperCase(tmp[1].split(" ")[1].charAt(0))) { // 说明需要计算first集
                  Set<String> first = calcFirst(tmp[1].split(" ")[1]);
                  if (first.contains("ε")) {
                    int p = 1;
                    Set<String> extra_search = calcFirst(tmp[1].split(" ")[p]);
                    do {
                      first.addAll(extra_search);
                      p++;
                    } while (p < tmp[1].split(" ").length
                        && (extra_search = calcFirst(tmp[1].split(" ")[p])).contains("ε"));
                  }
                  newitem += " ";
                  for (String s2 : first) {
                    if (-1 == getIndex(search.split(" "), s2) && !s2.equals("ε"))
                      newitem += s2 + " ";
                  }
                  newitem = newitem.substring(0, newitem.length() - 1);
                }
              }
            } else {
              newitem = tmp_s[0] + "->." + tmp_s[1];

              if (tmp[1].split(" ").length > 1) {
                if (Character.isUpperCase(tmp[1].split(" ")[1].charAt(0))) { // 说明需要计算first集
                  Set<String> first = calcFirst(tmp[1].split(" ")[1]);
                  if (first.contains("ε")) {
                    int p = 1;
                    Set<String> extra_search = calcFirst(tmp[1].split(" ")[p]);
                    do {
                      first.addAll(extra_search);
                      p++;
                    } while (p < tmp[1].split(" ").length
                        && (extra_search = calcFirst(tmp[1].split(" ")[p])).contains("ε"));
                  }
                  if (first.isEmpty() || (first.size() == 1 && first.contains("ε"))) {
                    newitem += "," + search;
                  } else {
                    newitem += ",";
                    for (String s2 : first) {
                      if (-1 == getIndex(search.split(" "), s2) && !s2.equals("ε"))
                        newitem += s2 + " ";
                    }
                    // newitem=newitem.substring(0,newitem.length()-1); //去掉多余空格
                    newitem += search;
                  }
                } else {
                  newitem += "," + tmp[1].split(" ")[1] + " " + search;
                }
              } else {
                newitem += "," + search;
              }

            }

            boolean needAdd = true;
            Iterator<String> iter = state.getItems().iterator();
            while (iter.hasNext()) {

              String it = iter.next();
              if ((it.split(",")[0]).equals(newitem.split(",")[0])) {
                if (it.length() < newitem.length()) { // 说明newitem中有新搜索符
                  iter.remove();
                } else {
                  needAdd = false;
                }
              }
            }
            if (needAdd) {
              String search_after = newitem.split(",")[1];
              Set<String> tmp_set = new HashSet<>();
              for (String ss : search_after.split(" ")) { // 去重复
                tmp_set.add(ss);
              }
              String real_search = "";
              for (String ss : tmp_set) {
                real_search += ss + " ";
              }
              real_search = real_search.substring(0, real_search.length() - 1);
              newitem = newitem.split(",")[0] + "," + real_search;
              state.addItem(newitem);
            }
          }
        }
      } else {
        continue; // 说明以终结符或者其他符号开头，可以不管了
      }
    }
  }

  private void initDFA() {
    // 初始状态
    State state0 = new State(state_number);
    state_number++;
    for (String s : expression) {
      if (s.split("->")[0].equals("S'")) {
        String[] tmp = s.split("->");
        state0.addItem(tmp[0] + "->." + tmp[1] + ",#");
        Closure(state0); // 闭包
        dfa.addState(state0);
      }
    }
  }

  /**
   * 递归建立dfa，输入开始节点即可。
   * 
   * @param start
   */
  private void buildDFA(State start) {
    List<String> ignore = new ArrayList<>();
    List<String> items = start.getItems();
    for (String item : items) {
      if (ignore.contains(item)) {
        continue;
      }
      String search = item.split(",")[1];
      item = item.split(",")[0];
      String[] tmp = item.split("\\.");
      if (tmp.length == 1) { // 说明已经规约完毕
        continue;
      }
      String[] tmp_after = tmp[1].split(" ");
      String condition = tmp_after[0];
      String new_state_start_item;
      if (tmp_after.length > 1) {
        new_state_start_item =
            tmp[0] + tmp_after[0] + " ." + tmp[1].substring(condition.length() + 1);
        new_state_start_item += "," + search;
      } else { // 说明tmp_after中实际上没有空格了
        new_state_start_item = tmp[0] + tmp_after[0] + " .," + search;
      }
      State exist = null;
      for (State s : dfa.getStates()) {
        if (s.getItems().get(0).equals(new_state_start_item)) {

          exist = s;
          break;
        }
      }
      if (exist != null) {
        Trans trans = new Trans(start, exist, condition);
        dfa.addTrans(trans);
      } else { // 尤其注意，这部分注意要先判断能不能将item合并到状态中。例如0状态通过E可能跳到一个有两个item的状态，而不判断将会生成两个分开的新状态
        boolean canMerge = false;
        if (!canMerge) {
          State new_state = new State(state_number);
          state_number++;
          new_state.addItem(new_state_start_item);
          for (String ss2 : items) {
            if (!ss2.split(",")[0].equals(item)) {
              String search2 = ss2.split(",")[1];
              String s2 = ss2.split(",")[0];
              String[] tmp2 = s2.split("\\.");
              if (tmp2.length > 1) {
                String[] tmp2_after = tmp2[1].split(" ");
                if (tmp2_after[0].equals(condition)) {
                  ignore.add(ss2); // 已经处理过了，以后碰到就不处理了
                  String new_state_start_item2;
                  if (tmp2_after.length > 1) {
                    new_state_start_item2 =
                        tmp2[0] + tmp2_after[0] + " ." + tmp2[1].substring(condition.length() + 1);
                    new_state_start_item2 += "," + search2;
                  } else { // 说明tmp_after中实际上没有空格了
                    new_state_start_item2 = tmp2[0] + tmp2_after[0] + " .," + search2;
                  }
                  new_state.addItem(new_state_start_item2);
                }
              }
            }
          }
          Closure(new_state);
          dfa.addState(new_state);
          dfa.addTrans(new Trans(start, new_state, condition));
          buildDFA(new_state);
        }
      }
    }
  }

  /**
   * 计算First函数，参数只能是非终结符
   */
  private Set<String> calcFirst(String str) {
    Set<String> result = new HashSet<>();
    for (String s : expression) {
      String[] tmp = s.split("->");
      if (tmp[0].equals(str)) {
        String[] right = tmp[1].split(" ");
        if (!Character.isUpperCase(right[0].charAt(0))) {
          result.add(right[0]);
        } else {
          int i = 0;
          for (String s2 : expression) {
            String tmpp = s2.split("->")[1].split(" ")[0];
            if (s2.split("->")[0].equals(str) && !tmpp.equals(str)
                && Character.isUpperCase(tmpp.charAt(0))) {
              result.addAll(calcFirst(tmpp));
            }
          }
          while (i < right.length && !(right[i].equals(str)) && calcFirst(right[i]).contains("ε")) {
            result.addAll(calcFirst(right[i]));
            i++;
          }
          if (i == right.length && calcFirst(right[i - 1]).contains("ε")) {
            result.add("ε");
          }
        }
      }
    }
    return result;
  }

  private int getIndex(String[] arr, String value) {
    for (int i = 0; i < arr.length; i++) {
      if (arr[i].equals(value)) {
        return i;
      }
    }
    return -1;// 如果未找到返回-1
  }

  public void analyze(List<Token> tokens) {
    System.out.println(
        "=========================ACTION======================================================GOTO=========================");
    int j;
    System.out.print("\t");
    for (String s : ACTION.keySet()) {
      System.out.print(s + "\t");
    }
    System.out.print(" ");
    for (String s : GOTO.keySet()) {
      System.out.print(s + "\t");
    }
    System.out.println();
    for (j = 0; j < jmptable.get("#").length; j++) {
      System.out.print(j + "\t");
      for (String s : ACTION.keySet()) {
        if (ACTION.get(s)[j] != null) {
          System.out.print(ACTION.get(s)[j] + "\t");
        } else {
          System.out.print("\t");
        }
      }
      System.out.print("|");
      for (String s : GOTO.keySet()) {
        if (GOTO.get(s)[j] != null) {
          System.out.print(GOTO.get(s)[j] + "\t");
        } else {
          System.out.print("\t");
        }
      }
      System.out.println();
    }
    System.out.println(
        "=========================================================Finish======================================================");

    state_stack.push(0);
    symbol_stack.push("#");
    System.out.println("开始语法分析");
    System.out.println("状态栈：" + state_stack);
    System.out.println("符号栈：" + symbol_stack);
    System.out.println("===============");

    // 可视化
    grammar_text.append("开始语法分析\n");
    grammar_text.append("状态栈：" + state_stack + "\n");
    grammar_text.append("符号栈：" + symbol_stack + "\n");
    grammar_text.append("===============\n");

    //
    int length = tokens.size();
    int i;
    for (i = 0; i < length; i++) {
      Token token = tokens.get(i);
      String action = ACTION(state_stack.peek(), token.getRealContent());
      System.out.println(action);
      if (action == null) {
        System.out.println("Error near :" + token.getRealContent());
        return;
      } else if (action.equals("acc")) {
        System.out.println("语法分析成功，结束");
        System.out.println("===============\n语义分析结果：");
        System.out.println(semantic_stack.peek().get("code"));
        System.out.println("===============\n符号表：");
        System.out.println("Name\t|Type\t|Length\t|Value");

        // 可视化
        grammar_text.append("语法分析成功，结束\n");
        semantic_text.append("===============\n语义分析结果：\n");
        semantic_text.append(semantic_stack.peek().get("code"));
        semantic_text.append("\n===============\n符号表：");
        semantic_text.append("Name\t|Type\t|Length\t|Value\n");
        //

        for (String s : word.WordAnalyzer.symbol_chart.keySet()) {
          System.out.println(word.WordAnalyzer.symbol_chart.get(s));

          // 可视化
          semantic_text.append(word.WordAnalyzer.symbol_chart.get(s) + "\n");
          //
        }
        return;
      } else {
        if (action.startsWith("s")) { // 移入

          int state = Integer.parseInt(action.substring(1));
          state_stack.push(state);
          symbol_stack.push(token.getRealContent());

          // 语义部分的操作
          Map<String, String> tmp = new HashMap<String, String>();
          if (token.getRealContent().equals("num")) {
            tmp.put("type", token.getRealWord());
            tmp.put("value", token.getProperty());
          }
          if (token.getRealContent().equals("id")) {
            tmp.put("name", token.getRealWord());
          }

          semantic_stack.push(tmp);
          // 语义部分结束

          System.out.println("状态栈：" + state_stack);
          System.out.println("符号栈：" + symbol_stack);
          System.out.println("===============");
          // System.out.println("移入"+token.getRealContent());
          // System.out.println("状态栈顶为："+state_stack.peek());
        }
        if (action.startsWith("r")) { // 归约
          int exp_num = Integer.parseInt(action.substring(1));
          SemanticAction(exp_num);
          String[] exps = expression[exp_num].split("->");
          System.out.println("使用下式规约");
          System.out.println(expression[exp_num]);
          System.out.println("弹栈");

          //
          grammar_text.append("使用下式规约\n");
          grammar_text.append(expression[exp_num] + "\n");
          grammar_text.append("弹栈\n");
          //

          if (!exps[1].equals("ε")) { // 空产生式不弹栈
            for (String s : exps[1].split(" ")) {
              symbol_stack.pop();
              state_stack.pop();
            }
          }
          System.out.println("弹栈后状态栈：" + state_stack);
          System.out.println("弹栈后符号栈：" + symbol_stack);

          //
          grammar_text.append("弹栈后状态栈：" + state_stack + "\n");
          grammar_text.append("弹栈后符号栈：" + symbol_stack + "\n");
          //
          // System.out.println("规约前状态栈："+state_stack.peek());
          symbol_stack.push(exps[0]);
          state_stack.push(GOTO(state_stack.peek(), symbol_stack.peek()));
          System.out.println("规约后状态栈：" + state_stack);
          System.out.println("规约后符号栈：" + symbol_stack);
          System.out.println("===============");

          //
          grammar_text.append("规约后状态栈：" + state_stack + "\n");
          grammar_text.append("规约后符号栈：" + symbol_stack + "\n");
          grammar_text.append("===============\n");
          //
          i--;
        }
      }
    }
  }

  private void buildTable() {
    int length = dfa.getStates().size();
    String[] col = new String[length];
    jmptable.put("#", col);

    for (Trans trans : dfa.getTrans()) {
      String[] col2 = new String[length];
      jmptable.put(trans.getCondition(), col2);
    }

    for (State state : dfa.getStates()) { // 规约，因为这些状态可能会没有Trans
      List<String> items = state.getItems();
      for (String item : items) {
        String search = item.split(",")[1];
        item = item.split(",")[0];
        if (item.split("\\.").length == 1) { // 说明点在最后，需要规约
          for (String s_col : search.split(" ")) {
            if (!item.split("->")[1].equals(" .")) {
              jmptable.get(s_col)[state.getNumber()] =
                  "r" + getIndex(expression, item.split(" \\.")[0]);
            } else { // 空产生式
              jmptable.get(s_col)[state.getNumber()] =
                  "r" + getIndex(expression, item.split("->")[0] + "->ε");
            }
          }
        }
        if (item.split("->")[1].equals("S .")) {
          jmptable.get("#")[state.getNumber()] = "acc";
        }
      }
    }

    for (Trans trans : dfa.getTrans()) {
      if (Character.isUpperCase(trans.getCondition().charAt(0))) { // GOTO表
        jmptable.get(trans.getCondition())[trans.getSrc().getNumber()] =
            String.valueOf(trans.getDst().getNumber());
      } else {
        jmptable.get(trans.getCondition())[trans.getSrc().getNumber()] =
            "s" + trans.getDst().getNumber();
        // }
      }

      // }
    }

    buildACTION_GOTO();
  }

  private void buildACTION_GOTO() {
    for (String name : jmptable.keySet()) {
      if (Character.isUpperCase(name.charAt(0))) { // 非终结符，跳转表
        GOTO.put(name, jmptable.get(name));
      } else {
        ACTION.put(name, jmptable.get(name));
      }
    }
  }

  private int GOTO(int state, String input) {
    return Integer.parseInt(GOTO.get(input)[state]);
  }

  private String ACTION(int state, String input) {
    return ACTION.get(input)[state];
  }

  private void SemanticAction(int exp_num) {
    Map<String, String> newtmp = new HashMap<>();
    switch (exp_num) {
      case 0:
        Map<String, String> tmpS = semantic_stack.pop();
        newtmp.put("code", tmpS.get("code"));
        semantic_stack.push(newtmp);
        // semaproc.get("S'").put("code", S_code);
        break;
      case 1:
        Map<String, String> tmpS_2 = semantic_stack.pop();
        Map<String, String> tmpS_1 = semantic_stack.pop();
        newtmp.put("code", tmpS_1.get("code") + "\n" + tmpS_2.get("code"));
        semantic_stack.push(newtmp);
        break;
      case 2:
        Map<String, String> tmpW = semantic_stack.pop();
        newtmp.put("code", tmpW.get("code"));
        semantic_stack.push(newtmp);
        // semaproc.get("S").put("code", W_code);
        break;
      case 3:
        Map<String, String> tmpI = semantic_stack.pop();
        newtmp.put("code", tmpI.get("code"));
        semantic_stack.push(newtmp);
        // semaproc.get("S").put("code", I_code);
        break;
      case 4:
        Map<String, String> tmpD = semantic_stack.pop();
        newtmp.put("code", tmpD.get("code"));
        semantic_stack.push(newtmp);
        // semaproc.get("S").put("code", D_code);
        break;
      case 5:
        Map<String, String> tmpF = semantic_stack.pop();
        newtmp.put("code", tmpF.get("code"));
        semantic_stack.push(newtmp);
        // semaproc.get("S").put("code", F_code);
        break;
      case 6: // 声明语句
        semantic_stack.pop(); // 弹掉分号
        Map<String, String> tmp6_L = semantic_stack.pop();
        Map<String, String> tmp6_T = semantic_stack.pop();
        newtmp.put("code", "(" + tmp6_T.get("type") + "," + tmp6_L.get("name") + ",-,-)");
        // 填写符号表
        word.WordAnalyzer.symbol_chart.get(tmp6_L.get("name"))
            .setLength(Integer.parseInt(tmp6_T.get("width")));
        word.WordAnalyzer.symbol_chart.get(tmp6_L.get("name")).setType(tmp6_T.get("type"));
        // newtmp.put("type", tmp7_T.get("type"));
        semantic_stack.push(newtmp);
        // semaproc.get("S").put("code", I_code);
        break;
      case 7:
        semantic_stack.pop(); // 弹出来int
        newtmp.put("type", "int");
        newtmp.put("width", "4");
        semantic_stack.push(newtmp);
        break;
      case 8:
        Map<String, String> id_tmp = semantic_stack.pop(); // 弹出id
        newtmp.put("name", id_tmp.get("name"));
        // newtmp.put("name", tokens.get(token_num-1).getRealWord()); 参数精简后失效，使用上面的语句代替
        semantic_stack.push(newtmp);
        break;
      case 9:
        semantic_stack.pop(); // 弹出来float
        newtmp.put("type", "float");
        newtmp.put("width", "4");
        semantic_stack.push(newtmp);
        break;
      case 10: // 循环语句的翻译
        for (int i = 0; i < 2; i++) {
          semantic_stack.pop();
        }
        Map<String, String> tmpC_10 = semantic_stack.pop();
        for (int i = 0; i < 3; i++) {
          semantic_stack.pop();
        }
        Map<String, String> tmpS_10 = semantic_stack.pop();
        for (int i = 0; i < 2; i++) {
          semantic_stack.pop();
        }
        String while_code = "";
        String while_endcode = "";

        String wC_code = tmpC_10.get("code");
        String while_S = tmpS_10.get("code");
        int lengthS_while = while_S.split("\n").length;

        int while_C_length = wC_code.split("\n").length;
        String real_endcode = "";

        while_endcode +=
            wC_code.replaceAll("\\?C\\.true\\?", "-" + (lengthS_while + while_C_length - 2));
        while_endcode = while_endcode.replaceAll("\\?C\\.false\\?", "+1");

        int j;
        for (j = 0; j < while_C_length; j++) {
          String tmp = while_endcode.split("\n")[j];
          if (tmp.contains("?C.ANDfalse?")) { // 终止循环
            int jmp_length = while_C_length - j;
            real_endcode += tmp.replaceAll("\\?C\\.ANDfalse\\?", "+" + jmp_length) + "\n";
          } else if (tmp.contains("?C.ORtrue?")) {
            int jmp_length = lengthS_while + j; // 往回跳，跳到循环体开头，所以下面是负号
            real_endcode += tmp.replaceAll("\\?C\\.ORtrue\\?", "-" + jmp_length) + "\n";
          } else {
            real_endcode += tmp + "\n";
          }
        }

        while_code += while_S;
        while_code += "\n" + real_endcode;

        newtmp.put("code", while_code);
        semantic_stack.push(newtmp);
        break;
      case 11:
        for (int i = 0; i < 2; i++) {
          semantic_stack.pop();
        }
        Map<String, String> tmpS_11_2 = semantic_stack.pop();
        for (int i = 0; i < 3; i++) {
          semantic_stack.pop();
        }
        Map<String, String> tmpS_11_1 = semantic_stack.pop();
        for (int i = 0; i < 2; i++) {
          semantic_stack.pop();
        }
        Map<String, String> tmpC_11 = semantic_stack.pop();
        for (int i = 0; i < 2; i++) {
          semantic_stack.pop();
        }
        String if_code = "";
        String C_code = tmpC_11.get("code");
        if_code += C_code.replaceAll("\\?C\\.true\\?", "+2"); // 跳过下面一个无条件跳转，?C.true?是布尔表达式末尾未填写的空位

        String then_code = tmpS_11_1.get("code");
        String else_code = tmpS_11_2.get("code");
        int lengthS1 = then_code.split("\n").length; // 多少行
        int lengthS2 = else_code.split("\n").length;

        if_code = if_code.replaceAll("\\?C\\.false\\?", "+" + (lengthS1 + 2)); // +2是因为要跳过一个then{}执行完后跳转到最后的语句

        int if_C_length = C_code.split("\n").length;
        String real_if_code = "";
        int i;
        for (i = 0; i < if_C_length; i++) {
          String tmp = if_code.split("\n")[i];
          if (tmp.contains("?C.ANDfalse?")) {
            int jmp_length = lengthS1 + 1 + (if_C_length - i);
            real_if_code += tmp.replaceAll("\\?C\\.ANDfalse\\?", "+" + jmp_length) + "\n";
          } else if (tmp.contains("?C.ORtrue?")) {
            int jmp_length = if_C_length - i;
            real_if_code += tmp.replaceAll("\\?C\\.ORtrue\\?", "+" + jmp_length) + "\n";
          } else {
            real_if_code += tmp + "\n";
          }
        }

        real_if_code += then_code;
        real_if_code += "\n" + "(j,-,-,+" + (lengthS2 + 1) + ")";
        real_if_code += "\n" + else_code;

        newtmp.put("code", real_if_code);
        semantic_stack.push(newtmp);
        break;
      case 12:
        Map<String, String> tmpNum12 = semantic_stack.pop();
        semantic_stack.pop();
        Map<String, String> tmpId12 = semantic_stack.pop();
        float id_val12 = 0, num_val12 = 0;
        if (word.WordAnalyzer.symbol_chart.get(tmpId12.get("name")).getType().equals("int")) {
          id_val12 =
              Integer.parseInt(word.WordAnalyzer.symbol_chart.get(tmpId12.get("name")).getValue());
        } else if (word.WordAnalyzer.symbol_chart.get(tmpId12.get("name")).getType()
            .equals("float")) {
          id_val12 =
              Float.parseFloat(word.WordAnalyzer.symbol_chart.get(tmpId12.get("name")).getValue());
        } else {
          System.out.println("id数值翻译错误");
          return;
        }

        if (tmpNum12.get("type").equals("int")) {
          num_val12 = Integer.parseInt(tmpNum12.get("value"));
        } else if (tmpNum12.get("type").equals("float")) {
          num_val12 = Float.parseFloat(tmpNum12.get("value"));
        } else {
          System.out.println("num数值翻译错误");
          return;
        }

        if (id_val12 == num_val12) {
          newtmp.put("boolean", "true");
        } else {
          newtmp.put("boolean", "false");
        }
        newtmp.put("true", "?");
        newtmp.put("false", "?");
        newtmp.put("code",
            "(j==," + tmpId12.get("name") + "," + tmpNum12.get("value") + "," + "?C.true?)\n" // 真出口是个问号
                + "(j,-,-,?C.false?)");
        semantic_stack.push(newtmp);
        break;
      case 13:
        Map<String, String> tmpNum13 = semantic_stack.pop();
        semantic_stack.pop();
        Map<String, String> tmpId13 = semantic_stack.pop();
        float id_val13 = 0, num_val13 = 0;
        if (word.WordAnalyzer.symbol_chart.get(tmpId13.get("name")).getType().equals("int")) {
          id_val13 =
              Integer.parseInt(word.WordAnalyzer.symbol_chart.get(tmpId13.get("name")).getValue());
        } else if (word.WordAnalyzer.symbol_chart.get(tmpId13.get("name")).getType()
            .equals("float")) {
          id_val13 =
              Float.parseFloat(word.WordAnalyzer.symbol_chart.get(tmpId13.get("name")).getValue());
        } else {
          System.out.println("id数值翻译错误");
          return;
        }

        if (tmpNum13.get("type").equals("int")) {
          num_val13 = Integer.parseInt(tmpNum13.get("value"));
        } else if (tmpNum13.get("type").equals("float")) {
          num_val13 = Float.parseFloat(tmpNum13.get("value"));
        } else {
          System.out.println("num数值翻译错误");
          return;
        }

        if (id_val13 >= num_val13) {
          newtmp.put("boolean", "true");
        } else {
          newtmp.put("boolean", "false");
        }
        newtmp.put("true", "?");
        newtmp.put("false", "?");
        newtmp.put("code",
            "(j>=," + tmpId13.get("name") + "," + tmpNum13.get("value") + "," + "?C.true?)\n" // 真出口是个问号
                + "(j,-,-,?C.false?)");
        semantic_stack.push(newtmp);
        break;
      case 14:
        Map<String, String> tmpNum14 = semantic_stack.pop();
        semantic_stack.pop();
        Map<String, String> tmpId14 = semantic_stack.pop();
        float id_val14 = 0, num_val14 = 0;
        if (word.WordAnalyzer.symbol_chart.get(tmpId14.get("name")).getType().equals("int")) {
          id_val14 =
              Integer.parseInt(word.WordAnalyzer.symbol_chart.get(tmpId14.get("name")).getValue());
        } else if (word.WordAnalyzer.symbol_chart.get(tmpId14.get("name")).getType()
            .equals("float")) {
          id_val14 =
              Float.parseFloat(word.WordAnalyzer.symbol_chart.get(tmpId14.get("name")).getValue());
        } else {
          System.out.println("id数值翻译错误");
          return;
        }

        if (tmpNum14.get("type").equals("int")) {
          num_val14 = Integer.parseInt(tmpNum14.get("value"));
        } else if (tmpNum14.get("type").equals("float")) {
          num_val14 = Float.parseFloat(tmpNum14.get("value"));
        } else {
          System.out.println("num数值翻译错误");
          return;
        }

        if (id_val14 <= num_val14) {
          newtmp.put("boolean", "true");
        } else {
          newtmp.put("boolean", "false");
        }
        newtmp.put("true", "?");
        newtmp.put("false", "?");
        newtmp.put("code",
            "(j<=," + tmpId14.get("name") + "," + tmpNum14.get("value") + "," + "?C.true?)\n" // 真出口是个问号
                + "(j,-,-,?C.false?)");
        semantic_stack.push(newtmp);
        break;

      case 15:
        Map<String, String> tmpNum15 = semantic_stack.pop();
        semantic_stack.pop();
        Map<String, String> tmpId15 = semantic_stack.pop();
        float id_val15 = 0, num_val15 = 0;
        if (word.WordAnalyzer.symbol_chart.get(tmpId15.get("name")).getType().equals("int")) {
          id_val15 =
              Integer.parseInt(word.WordAnalyzer.symbol_chart.get(tmpId15.get("name")).getValue());
        } else if (word.WordAnalyzer.symbol_chart.get(tmpId15.get("name")).getType()
            .equals("float")) {
          id_val15 =
              Float.parseFloat(word.WordAnalyzer.symbol_chart.get(tmpId15.get("name")).getValue());
        } else {
          System.out.println("id数值翻译错误");
          return;
        }

        if (tmpNum15.get("type").equals("int")) {
          num_val15 = Integer.parseInt(tmpNum15.get("value"));
        } else if (tmpNum15.get("type").equals("float")) {
          num_val15 = Float.parseFloat(tmpNum15.get("value"));
        } else {
          System.out.println("num数值翻译错误");
          return;
        }

        if (id_val15 < num_val15) {
          newtmp.put("boolean", "true");
        } else {
          newtmp.put("boolean", "false");
        }
        newtmp.put("true", "?");
        newtmp.put("false", "?");
        newtmp.put("code",
            "(j<," + tmpId15.get("name") + "," + tmpNum15.get("value") + "," + "?C.true?)\n" // 真出口是个问号
                + "(j,-,-,?C.false?)");
        semantic_stack.push(newtmp);
        break;
      case 16:
        Map<String, String> tmpNum16 = semantic_stack.pop();
        semantic_stack.pop();
        Map<String, String> tmpId16 = semantic_stack.pop();
        float id_val16 = 0, num_val16 = 0;
        if (word.WordAnalyzer.symbol_chart.get(tmpId16.get("name")).getType().equals("int")) {
          id_val16 =
              Integer.parseInt(word.WordAnalyzer.symbol_chart.get(tmpId16.get("name")).getValue());
        } else if (word.WordAnalyzer.symbol_chart.get(tmpId16.get("name")).getType()
            .equals("float")) {
          id_val16 =
              Float.parseFloat(word.WordAnalyzer.symbol_chart.get(tmpId16.get("name")).getValue());
        } else {
          System.out.println("id数值翻译错误");
          return;
        }

        if (tmpNum16.get("type").equals("int")) {
          num_val16 = Integer.parseInt(tmpNum16.get("value"));
        } else if (tmpNum16.get("type").equals("float")) {
          num_val16 = Float.parseFloat(tmpNum16.get("value"));
        } else {
          System.out.println("num数值翻译错误");
          return;
        }

        if (id_val16 > num_val16) {
          newtmp.put("boolean", "true");
        } else {
          newtmp.put("boolean", "false");
        }
        newtmp.put("true", "?");
        newtmp.put("false", "?");
        newtmp.put("code",
            "(j>," + tmpId16.get("name") + "," + tmpNum16.get("value") + "," + "?C.true?)\n" // 真出口是个问号
                + "(j,-,-,?C.false?)");
        semantic_stack.push(newtmp);
        break;
      case 17: // 赋值，给符号表中符号赋值
        semantic_stack.pop(); // 分号
        Map<String, String> tmpNum17 = semantic_stack.pop();
        semantic_stack.pop();
        Map<String, String> tmpId17 = semantic_stack.pop();

        if (word.WordAnalyzer.symbol_chart.get(tmpId17.get("name")).getType() != null) {
          if (word.WordAnalyzer.symbol_chart.get(tmpId17.get("name")).getType()
              .equals(tmpNum17.get("type"))) {
            word.WordAnalyzer.symbol_chart.get(tmpId17.get("name")).setValue(tmpNum17.get("value"));
          } else {
            System.out.println("类型错误，无法赋值");
            if (type_convert) {  //类型转换
              if (word.WordAnalyzer.symbol_chart.get(tmpId17.get("name")).getType().equals("int")) {
                // 数字只可能是float
                word.WordAnalyzer.symbol_chart.get(tmpId17.get("name"))
                    .setValue(String.valueOf((int) Float.parseFloat(tmpNum17.get("value"))));
              } else if (word.WordAnalyzer.symbol_chart.get(tmpId17.get("name")).getType()
                  .equals("float")) {
                word.WordAnalyzer.symbol_chart.get(tmpId17.get("name"))
                    .setValue(String.valueOf((float) Integer.parseInt(tmpNum17.get("value"))));
              } else {
                // 暂未完成，因为目前只定义了float和int
              }
            } else {
              if (exit_after_error) {
                System.out.println("根据设置，程序退出");
                System.exit(1);
              }
            }
            // return;
          }
        } else {
          System.out.println("未定义的变量");
          if (exit_after_error) {
            System.out.println("根据设置，程序退出");
            System.exit(1);
          }
          // return;
        }
        // word.WordAnalyzer.symbol_chart.get(tmpId17.get("name")).setType(tmpNum17.get("type"));
        newtmp.put("code", "(=," + tmpId17.get("name") + "," + tmpNum17.get("value") + ",-)");
        semantic_stack.push(newtmp);
        break;
      case 18:
        semantic_stack.pop(); // 分号
        Map<String, String> tmpId18_3 = semantic_stack.pop();
        semantic_stack.pop(); // 加号
        Map<String, String> tmpId18_2 = semantic_stack.pop();
        semantic_stack.pop(); // 等号
        Map<String, String> tmpId18_1 = semantic_stack.pop();

        // if(word.WordAnalyzer.symbol_chart.get(tokens.get(token_num-4).getRealWord()).getType().equals(tokens.get(token_num-2).getRealWord()))
        // {
        // word.WordAnalyzer.symbol_chart.get(tokens.get(token_num-4).getRealWord()).setValue(tokens.get(token_num-2).getProperty());
        // }else {
        // System.out.println("类型错误，无法赋值");
        // return;
        // }
        if (word.WordAnalyzer.symbol_chart.get(tmpId18_1.get("name")).getType() != null) {
          if (word.WordAnalyzer.symbol_chart.get(tmpId18_1.get("name")).getType()
              .equals(word.WordAnalyzer.symbol_chart.get(tmpId18_3.get("name")).getType())
              && word.WordAnalyzer.symbol_chart.get(tmpId18_1.get("name")).getType()
                  .equals(word.WordAnalyzer.symbol_chart.get(tmpId18_2.get("name")).getType())) {
            if (word.WordAnalyzer.symbol_chart.get(tmpId18_1.get("name")).getType().equals("int")) {
              int result = Integer
                  .parseInt(word.WordAnalyzer.symbol_chart.get(tmpId18_2.get("name")).getValue())
                  + Integer.parseInt(
                      word.WordAnalyzer.symbol_chart.get(tmpId18_3.get("name")).getValue());
              word.WordAnalyzer.symbol_chart.get(tmpId18_1.get("name"))
                  .setValue(String.valueOf(result));
            } else if (word.WordAnalyzer.symbol_chart.get(tmpId18_1.get("name")).getType()
                .equals("float")) {
              float result = Float
                  .parseFloat(word.WordAnalyzer.symbol_chart.get(tmpId18_2.get("name")).getValue())
                  + Float.parseFloat(
                      word.WordAnalyzer.symbol_chart.get(tmpId18_3.get("name")).getValue());
              word.WordAnalyzer.symbol_chart.get(tmpId18_1.get("name"))
                  .setValue(String.valueOf(result));
            } else {
              System.out.println("符号表中出现未定义的类型");
              if (exit_after_error) {
                System.out.println("根据设置，程序退出");
                System.exit(1);
              }
              // return;
            }
          } else {
            System.out.println("类型错误，无法赋值");
            if (exit_after_error) {
              System.out.println("根据设置，程序退出");
              System.exit(1);
            }
            // return;
          }
        } else {
          System.out.println("未定义的变量");
          if (exit_after_error) {
            System.out.println("根据设置，程序退出");
            System.exit(1);
          }
          // return;
        }
        // word.WordAnalyzer.symbol_chart.get(tmpId17.get("name")).setType(tmpNum17.get("type"));
        newtmp.put("code", "(+," + tmpId18_2.get("name") + "," + tmpId18_3.get("name") + ",t1)\n"
            + "(=," + tmpId18_1.get("name") + "," + "t1,-)");
        semantic_stack.push(newtmp);
        break;
      case 19:
        semantic_stack.pop(); // 分号
        Map<String, String> tmpId19_3 = semantic_stack.pop();
        semantic_stack.pop(); // 加号
        Map<String, String> tmpId19_2 = semantic_stack.pop();
        semantic_stack.pop(); // 等号
        Map<String, String> tmpId19_1 = semantic_stack.pop();

        // if(word.WordAnalyzer.symbol_chart.get(tokens.get(token_num-4).getRealWord()).getType().equals(tokens.get(token_num-2).getRealWord()))
        // {
        // word.WordAnalyzer.symbol_chart.get(tokens.get(token_num-4).getRealWord()).setValue(tokens.get(token_num-2).getProperty());
        // }else {
        // System.out.println("类型错误，无法赋值");
        // return;
        // }
        if (word.WordAnalyzer.symbol_chart.get(tmpId19_1.get("name")).getType() != null) {
          if (word.WordAnalyzer.symbol_chart.get(tmpId19_1.get("name")).getType()
              .equals(word.WordAnalyzer.symbol_chart.get(tmpId19_3.get("name")).getType())
              && word.WordAnalyzer.symbol_chart.get(tmpId19_1.get("name")).getType()
                  .equals(word.WordAnalyzer.symbol_chart.get(tmpId19_2.get("name")).getType())) {
            if (word.WordAnalyzer.symbol_chart.get(tmpId19_1.get("name")).getType().equals("int")) {
              int result = Integer
                  .parseInt(word.WordAnalyzer.symbol_chart.get(tmpId19_2.get("name")).getValue())
                  * Integer.parseInt(
                      word.WordAnalyzer.symbol_chart.get(tmpId19_3.get("name")).getValue());
              word.WordAnalyzer.symbol_chart.get(tmpId19_1.get("name"))
                  .setValue(String.valueOf(result));
            } else if (word.WordAnalyzer.symbol_chart.get(tmpId19_1.get("name")).getType()
                .equals("float")) {
              float result = Float
                  .parseFloat(word.WordAnalyzer.symbol_chart.get(tmpId19_2.get("name")).getValue())
                  * Float.parseFloat(
                      word.WordAnalyzer.symbol_chart.get(tmpId19_3.get("name")).getValue());
              word.WordAnalyzer.symbol_chart.get(tmpId19_1.get("name"))
                  .setValue(String.valueOf(result));
            } else {
              System.out.println("符号表中出现未定义的类型");
              if (exit_after_error) {
                System.out.println("根据设置，程序退出");
                System.exit(1);
              }
              // return;
            }
          } else {
            System.out.println("类型错误，无法赋值");
            if (exit_after_error) {
              System.out.println("根据设置，程序退出");
              System.exit(1);
            }
            // return;
          }
        } else {
          System.out.println("未定义的变量");
          if (exit_after_error) {
            System.out.println("根据设置，程序退出");
            System.exit(1);
          }
          // return;
        }
        // word.WordAnalyzer.symbol_chart.get(tmpId17.get("name")).setType(tmpNum17.get("type"));
        newtmp.put("code", "(*," + tmpId19_2.get("name") + "," + tmpId19_3.get("name") + ",t1)\n"
            + "(=," + tmpId19_1.get("name") + "," + "t1,-)");
        semantic_stack.push(newtmp);
        break;
      case 20:
        Map<String, String> tmpC_20_2 = semantic_stack.pop();
        semantic_stack.pop(); // 弹出&&
        Map<String, String> tmpC_20_1 = semantic_stack.pop();

        String AND_code = "";
        String AND_C1 = tmpC_20_1.get("code");
        String AND_C2 = tmpC_20_2.get("code");

        AND_code += AND_C1.replaceAll("\\?C\\.true\\?", "+2");
        AND_code = AND_code.replaceAll("\\?C\\.false\\?", "?C.ANDfalse?"); // 待处理
        AND_code += "\n" + AND_C2;

        newtmp.put("code", AND_code);
        semantic_stack.push(newtmp);
        break;
      case 21:
        Map<String, String> tmpC_21_2 = semantic_stack.pop();
        semantic_stack.pop(); // 弹出||
        Map<String, String> tmpC_21_1 = semantic_stack.pop();

        String OR_C1 = tmpC_21_1.get("code");
        String OR_C2 = tmpC_21_2.get("code");

        String OR_code = "";
        OR_code += OR_C1.replaceAll("\\?C\\.false\\?", "+1");
        OR_code = OR_code.replaceAll("\\?C\\.true\\?", "?C.ORtrue?"); // 待处理
        OR_code += "\n" + OR_C2;

        newtmp.put("code", OR_code);
        semantic_stack.push(newtmp);
        break;
      default:
        System.out.println("错误的语义分析处理");
    }
  }
}
