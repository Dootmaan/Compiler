package grammar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import word.Token;

/**
 * ���Դ���մ�
 * 
 * @author samma
 *
 */
public class GrammarAnalyzer_�� {



  // Ҫ���ķ����ս��ֻ���ɴ�д��ĸ��ͷ�����������ÿո����
  // private String[] expression = {"S'->S", "S->B B", "B->float B", "B->int"};
  // private String[] expression = {"S'->S", "S->D ; A", "D->T L", "T->int", "L->id", "E->E + F",
  // "E->F", "F->id", "F->num", "A->L = E","A->��"};
  // private String[] expression = {"S'->S",
  // "S->do { D } while ( C )",
  // "C->id == num",
  // "D->A",
  // "A->id = num ;"};
//  private String[] expression =
//      {"S'->S", "S->S + T", "S->T", "T->T * F", "T->F", "F->( S )", "F->id"};
  private String[] expression;
  private DFAGraph dfa = new DFAGraph();
  private int state_number = 0;

  /*
   * String�Ƕ�Ӧ�����������ƣ�String[]�Ǹ������е���Ϣ���䳤��Ӧ��ȫ������һ�£��������õĺô��ǿ���ֱ��ͨ��������ΪString[]���±����ҵ���Ϣ��
   */
  private Map<String, String[]> jmptable = new HashMap<>();
  private Map<String, String[]> ACTION = new HashMap<>();
  private Map<String, String[]> GOTO = new HashMap<>();

  private Stack<Integer> state_stack = new Stack<>();
  private Stack<String> symbol_stack = new Stack<>();

  public GrammarAnalyzer_��(List<Token> tokens) {
    try {
      // ���ļ�
      FileInputStream is = new FileInputStream(new File("D:\\grammar_SLR.txt"));

      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      List<String> exps = new ArrayList<>();
      String str;
      while ((str = reader.readLine()) != null) {
        if (str.startsWith("//")) { // �������ע��
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
    // ���ļ����������
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
      String item = state.getItems().get(i);
      String[] tmp = item.split("\\.");
      if (tmp.length == 1) { // ˵����Լ����
        continue;
      }
      if (Character.isUpperCase(tmp[1].charAt(0))) { // ��Ҫ����հ�
        String start = tmp[1].split(" ")[0];
        for (String s : expression) {
          String[] tmp_s = s.split("->");
          if (tmp_s[0].equals(start)) {
            String newitem;
            if (tmp_s[1].equals("��")) {
              newitem = tmp_s[0] + "-> .";
            } else {
              newitem = tmp_s[0] + "->." + tmp_s[1];
            }
            if (!state.getItems().contains(newitem)) {
              state.addItem(newitem);
            }
          }
        }
      } else {
        continue; // ˵�����ս�������������ſ�ͷ�����Բ�����
      }
    }
  }

  private void initDFA() {
    // ��ʼ״̬
    State state0 = new State(state_number);
    state_number++;
    for (String s : expression) {
      if (s.split("->")[0].equals("S'")) {
        String[] tmp = s.split("->");
        state0.addItem(tmp[0] + "->." + tmp[1]);
        Closure(state0); // �հ�
        dfa.addState(state0);
      }
    }
  }

  /**
   * �ݹ齨��dfa�����뿪ʼ�ڵ㼴�ɡ�
   * 
   * @param start
   */
  private void buildDFA(State start) {
    List<String> items = start.getItems();
    for (String item : items) {
      String[] tmp = item.split("\\.");
      if (tmp.length == 1) { // ˵���Ѿ���Լ���
        continue;
      }
      String[] tmp_after = tmp[1].split(" ");
      String condition = tmp_after[0];
      String new_state_start_item;
      if (tmp_after.length > 1) {
        new_state_start_item =
            tmp[0] + tmp_after[0] + " ." + tmp[1].substring(condition.length() + 1);
      } else { // ˵��tmp_after��ʵ����û�пո���
        new_state_start_item = tmp[0] + tmp_after[0] + " .";
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
      } else { // ����ע�⣬�ⲿ��ע��Ҫ���ж��ܲ��ܽ�item�ϲ���״̬�С�����0״̬ͨ��E��������һ��������item��״̬�������жϽ������������ֿ�����״̬
        boolean canMerge = false;
        State tmp_state = null;
        for (Trans trans : dfa.getTrans()) {
          if (trans.getCondition().equals(condition) && trans.getSrc().equals(start)) {
            canMerge = true;
            trans.getDst().addItem(new_state_start_item);
            Closure(trans.getDst());
            tmp_state = trans.getDst();
            break;
          }
        }
        if (canMerge) {
          buildDFA(tmp_state); // ��ʱ����������µ�״̬�ڴ˽���buildDFA()�ݹ����
        }
        if (!canMerge) {
          State new_state = new State(state_number);
          state_number++;
          new_state.addItem(new_state_start_item);
          Closure(new_state);
          dfa.addState(new_state);
          dfa.addTrans(new Trans(start, new_state, condition));
          buildDFA(new_state);
        }
      }
    }
  }

  /**
   * ����First����
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
          while (calcFirst(right[i]).contains("��") && i < right.length) {
            result.addAll(calcFirst(right[i]));
            i++;
          }
          if (i == right.length && calcFirst(right[i - 1]).contains("��")) {
            result.add("��");
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
    return -1;// ���δ�ҵ�����-1
  }


  /**
   * ����Follow��
   */
  private Set<String> calcFollow(String str) {
    ///////
    // ��1��A��S����ʼ��)������#
    Set<String> result = new HashSet<>();
    if (str.equals("S'")) {
      result.add("#");
    }
    for (String s : expression) {
      String[] tmp = s.split("->");
      int index;
      String[] right = tmp[1].split(" ");
      if ((index = getIndex(right, str)) >= 0) {
        while ((index = getIndex(right, str)) >= 0) {
          if (index == right.length - 1) {
            if (!tmp[0].equals(str)) {
              result.addAll(calcFollow(tmp[0]));
            } else {
              return result; // �������ݹ�
            }
          } else {
            if (!Character.isUpperCase(right[index + 1].charAt(0))) {
              // if(!right[index+1].equals("��"))
              result.add(right[index + 1]);
            } else {
              result.addAll(calcFirst(right[index + 1]));
            }
          }
          right[index] = "null";
        }
      }
    }
    return result;
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
    int length = tokens.size();
    int i;
    for (i = 0; i < length; i++) {
      Token token = tokens.get(i);
      String action = ACTION(state_stack.peek(), token.getRealContent());
      if (action == null) {
        System.out.println("Error near :" + token.getRealContent());
        return;
      } else if (action.equals("acc")) {
        System.out.println("�﷨�����ɹ�������");
        return;
      } else {
        if (action.startsWith("s")) { // ����
          int state = Integer.parseInt(action.substring(1));
          state_stack.push(state);
          symbol_stack.push(token.getRealContent());
          // System.out.println("����"+token.getRealContent());
          // System.out.println("״̬ջ��Ϊ��"+state_stack.peek());
        }
        if (action.startsWith("r")) { // ��Լ
          int exp_num = Integer.parseInt(action.substring(1));
          String[] exps = expression[exp_num].split("->");
          System.out.println(expression[exp_num]);
          if (!exps[1].equals("��")) { // �ղ���ʽ����ջ
            for (String s : exps[1].split(" ")) {
              symbol_stack.pop();
              state_stack.pop();
            }
          }
          // System.out.println("��Լǰ״̬ջ��"+state_stack.peek());
          symbol_stack.push(exps[0]);
          state_stack.push(GOTO(state_stack.peek(), symbol_stack.peek()));
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

    for (State state : dfa.getStates()) { // ��Լ����Ϊ��Щ״̬���ܻ�û��Trans
      List<String> items = state.getItems();
      for (String item : items) {
        if (item.split("\\.").length == 1) { // ˵�����������Ҫ��Լ
          for (String s_col : calcFollow(item.split("->")[0])) {
            if (!item.split("->")[1].equals(" .")) {
              jmptable.get(s_col)[state.getNumber()] =
                  "r" + getIndex(expression, item.split(" \\.")[0]);
            } else { // �ղ���ʽ
              jmptable.get(s_col)[state.getNumber()] =
                  "r" + getIndex(expression, item.split("->")[0] + "->��");
            }
          }
        }
        if (item.split("->")[1].equals("S .")) {
          jmptable.get("#")[state.getNumber()] = "acc";
        }
      }
    }

    for (Trans trans : dfa.getTrans()) {
      if (Character.isUpperCase(trans.getCondition().charAt(0))) { // GOTO��
        jmptable.get(trans.getCondition())[trans.getSrc().getNumber()] =
            String.valueOf(trans.getDst().getNumber());
      } else { // ACTION��
        // List<String> items = trans.getDst().getItems();
        //
        // for (String str : items) {
        // if (str.split("\\.").length == 1) { // ˵�����Թ�Լ
        // // �����Ѿ���ɣ�Ӧ�ÿ��Բ���д��
        // }
        // if (str.split("\\.").length > 1) { // ˵����Ҫ����
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
      if (Character.isUpperCase(name.charAt(0))) { // ���ս������ת��
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

}
