package grammar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class GrammarAnalyzer_ref {
    public static void main(String[] args) {
        Test test = new Test();
        test.getNvNt();
        test.Init();
        test.createTable();
        test.analyzeLL();
        //test.analyzeSLR();
        test.ouput();
    }
}

class Test {
    //��������first��
    public HashMap<Character, HashSet<Character>> firstSet = new HashMap<>();
    //���Ŵ�first��
    public HashMap<String, HashSet<Character>> firstSetX = new HashMap<>();
    //��ʼ��
    public static char S = 'S';
    public HashMap<Character, HashSet<Character>> followSet = new HashMap<>();
    //���ս��
    public HashSet<Character> VnSet = new HashSet<>();
    //�ս��
    public HashSet<Character> VtSet = new HashSet<>();
    //���ս��-����ʽ����
    public HashMap<Character, ArrayList<String>> experssionSet = new HashMap<>();
    // E: TK | K: +TK $ | T : FM | M: *FM $|F :i (E)
    public String[][] table;
    public String[][] tableSLR = {
            {"", "i", "+", "*", "(", ")", "$", "E", "T", "F"},
            {"0", "s5", "", "", "s4", "", "", "1", "2", "3"},
            {"1", "", "s6", "", "", "", "acc", "", "", ""},
            {"2", "", "r2", "s7", "", "r2", "r2", "", "", ""},
            {"3", "", "r4", "r4", "", "r4", "r4", "", "", ""},
            {"4", "s5", "", "", "s4", "", "", "8", "2", "3"},
            {"5", "", "r6", "r6", "", "r6", "r6", "", "", ""},
            {"6", "s5", "", "", "s4", "", "", "", "9", "3"},
            {"7", "s5", "", "", "s4", "", "", "", "", "10"},
            {"8", "", "s6", "", "", "s11", "", "", "", ""},
            {"9", "", "r1", "s7", "", "r1", "r1", "", "", ""},
            {"10", "", "r3", "r3", "", "r3", "r3", "", "", ""},
            {"11", "", "r5", "r5", "", "r5", "r5", "", "", ""}};

    public String[] inputExperssion = { "S->I", "S->o", "I->i(E)SL", "L->eS", "L->~", "E->a", "E->b"};
    public Stack<Character> analyzeStatck = new Stack<>();
    public Stack<String> stackState = new Stack<>();
    public Stack<Character> stackSymbol = new Stack<>();
    public String strInput = "i(a)i(b)oeo$";
    public String action = "";
    public String[] LRGS = {"E->E+T", "E->T", "T->T*F", "T->F", "F->(E)", "F->i"};
    int index = 0;

    public void Init() {
        //��ȡ����ʽ
        for (String e : inputExperssion) {
            String[] str = e.split("->");
            char c = str[0].charAt(0);
            ArrayList<String> list = experssionSet.containsKey(c) ? experssionSet.get(c) : new ArrayList<>();
            list.add(str[1]);
            experssionSet.put(c, list);
        }
        //������ս����first��
        for (char c : VnSet)
            getFirst(c);
        //���쿪ʼ����follow��
        getFollow(S);
        //������ս����follow��
        for (char c : VnSet)
            getFollow(c);
    }

    /**
     * ������ս���������ս��
     */
    public void getNvNt() {
        for (String e : inputExperssion)
            VnSet.add(e.split("->")[0].charAt(0));
        for (String e : inputExperssion)
            for (char c : e.split("->")[1].toCharArray())
                if (!VnSet.contains(c))
                    VtSet.add(c);
    }

    public void getFirst(char c) {
        if (firstSet.containsKey(c))
            return;
        HashSet<Character> set = new HashSet<>();
        // ��cΪ�ս�� ֱ�����
        if (VtSet.contains(c)) {
            set.add(c);
            firstSet.put(c, set);
            return;
        }
        // cΪ���ս�� ������ÿ������ʽ
        for (String s : experssionSet.get(c)) {
            if ("~".equals(c)) {
                set.add('~');
            } else {
                for (char cur : s.toCharArray()) {
                    if (!firstSet.containsKey(cur))
                        getFirst(cur);
                    HashSet<Character> curFirst = firstSet.get(cur);
                    set.addAll(curFirst);
                    if (!curFirst.contains('~'))
                        break;
                }
            }
        }
        firstSet.put(c, set);
    }

    public void getFirst(String s) {
        if (firstSetX.containsKey(s))
            return;
        HashSet<Character> set = new HashSet<>();
        // ��������ɨ���ʽ
        int i = 0;
        while (i < s.length()) {
            char cur = s.charAt(i);
            if (!firstSet.containsKey(cur))
                getFirst(cur);
            HashSet<Character> rightSet = firstSet.get(cur);
            // ����ǿ� first��������
            set.addAll(rightSet);
            // �������մ� ������һ������
            if (rightSet.contains('~'))
                i++;
            else
                break;
            // ������β�� �����з��ŵ�first���������մ� �ѿմ�����fisrt��
            if (i == s.length()) {
                set.add('~');
            }
        }
        firstSetX.put(s, set);
    }


    public void getFollow(char c) {
        ArrayList<String> list = experssionSet.get(c);
        HashSet<Character> leftFollowSet = followSet.containsKey(c) ? followSet.get(c) : new HashSet<>();
        //����ǿ�ʼ�� ��� $
        if (c == S)
            leftFollowSet.add('$');
        //������������в���ʽ�����c�ĺ�� �ս��
        for (char ch : VnSet)
            for (String s : experssionSet.get(ch))
                for (int i = 0; i < s.length(); i++)
                    if (c == s.charAt(i) && i + 1 < s.length() && VtSet.contains(s.charAt(i + 1)))
                        leftFollowSet.add(s.charAt(i + 1));
        followSet.put(c, leftFollowSet);
        //����ɨ�账��c��ÿһ������ʽ
        for (String s : list) {
            int i = s.length() - 1;
            while (i >= 0) {
                char cur = s.charAt(i);
                //ֻ������ս��  I->i(E)SL
                if (VnSet.contains(cur)) {
                    // ���� A->��B��  ��ʽ����
                    //1.���²�����   followA ���� followB
                    //2.���´��ڣ��Ѧµķǿ�first��  ����followB
                    //3.���´���  ��first(��)�����մ�  followA ���� followB
                    String right = s.substring(i + 1);
                    HashSet<Character> rightFirstSet;
                    if(!followSet.containsKey(cur))
                        getFollow(cur);
                    HashSet<Character> curFollowSet = followSet.get(cur);
                    //���ҳ�first(��),���ǿյļ���followB
                    if (0 == right.length()) {
                        curFollowSet.addAll(leftFollowSet);
                    } else {
                        if (1 == right.length()) {
                            if (!firstSet.containsKey(right.charAt(0)))
                                getFirst(right.charAt(0));
                            rightFirstSet = firstSet.get(right.charAt(0));
                        } else {
                            if (!firstSetX.containsKey(right))
                                getFirst(right);
                            rightFirstSet = firstSetX.get(right);
                        }
                        for (char var : rightFirstSet)
                            if (var != '~')
                                curFollowSet.add(var);
                        // ��first(��)�����մ�,��followA����followB
                        if (rightFirstSet.contains('~'))
                            curFollowSet.addAll(leftFollowSet);
                    }
                    followSet.put(cur, curFollowSet);
                }
                i--;
            }
        }
    }


    public void createTable() {
        Object[] VtArray = VtSet.toArray();
        Object[] VnArray = VnSet.toArray();
        // Ԥ��������ʼ��
        table = new String[VnArray.length + 1][VtArray.length + 1];
        table[0][0] = "Vn/Vt";
        //��ʼ����������
        for (int i = 0; i < VtArray.length; i++)
            table[0][i + 1] = (VtArray[i].toString().charAt(0) == '~') ? "$" : VtArray[i].toString();
        for (int i = 0; i < VnArray.length; i++)
            table[i + 1][0] = VnArray[i] + "";
        //ȫ����error
        for (int i = 0; i < VnArray.length; i++)
            for (int j = 0; j < VtArray.length; j++)
                table[i + 1][j + 1] = "error";
        //��������ʽ
        for (char A : VnSet) {
            for (String s : experssionSet.get(A)) {
                if (!firstSetX.containsKey(s))
                    getFirst(s);
                HashSet<Character> set = firstSetX.get(s);
                for (char a : set)
                    insert(A, a, s);
                if (set.contains('~')) {
                    HashSet<Character> setFollow = followSet.get(A);
                    if (setFollow.contains('$'))
                        insert(A, '$', s);
                    for (char b : setFollow)
                        insert(A, b, s);
                }
            }
        }
    }

    public void analyzeLL() {
        System.out.println("****************LL��������**********");
        System.out.println("               Stack           Input     Action");
        analyzeStatck.push('$');
        analyzeStatck.push(S);
        displayLL();
        char X = analyzeStatck.peek();
        while (X != '$') {
            char a = strInput.charAt(index);
            if (X == a) {
                action = "match " + analyzeStatck.peek();
                analyzeStatck.pop();
                index++;
            } else if (VtSet.contains(X))
                return;
            else if (find(X, a).equals("error"))
                return;
            else if (find(X, a).equals("~")) {
                analyzeStatck.pop();
                action = X + "->~";
            } else {
                String str = find(X, a);
                if (str != "") {
                    action = X + "->" + str;
                    analyzeStatck.pop();
                    int len = str.length();
                    for (int i = len - 1; i >= 0; i--)
                        analyzeStatck.push(str.charAt(i));
                } else {
                    System.out.println("error at '" + strInput.charAt(index) + " in " + index);
                    return;
                }
            }
            X = analyzeStatck.peek();
            displayLL();
        }
        System.out.println("analyze LL1 successfully");
        System.out.println("****************LL��������**********");
    }

    public void analyzeSLR() {
        action = "";
        index = 0;
        stackState.push("0");
        char a = strInput.charAt(index);
        System.out.println("****************SLR��������**********");
        System.out.println("                    State         Symbol        Input         Action");
        this.displaySLR();
        while (true) {
            String s = stackState.peek();
            // ���Ϊ�ƽ�
            if (Action(s, a).charAt(0) == 's') {
                stackState.push(Action(s, a).substring(1));
                stackSymbol.push(a);
                a = strInput.charAt(++index);
                action = "shift ";
                displaySLR();
            }
            // ���Ϊ��Լ
            else if (Action(s, a).charAt(0) == 'r') {
                // ��ȡ�ķ���
                String str = LRGS[Integer.parseInt(Action(s, a).substring(1)) - 1];
                int len = str.substring(3).length();
                // �����Ҳ����ȵķ��ź�״̬
                for (int i = 0; i < len; i++) {
                    stackSymbol.pop();
                    stackState.pop();
                }
                // goto��ֵ��ջ
                String t = stackState.peek();
                stackState.push(Action(t, str.charAt(0)));
                stackSymbol.push(str.charAt(0));
                action = "reduce:" + str;
                displaySLR();
            } else if (Action(s, a) == "acc")
                break;
            else
                return;
        }
        System.out.println("analyze SLR successfully");
        System.out.println("****************SLR��������**********");
    }

    public String Action(String s, char a) {
        for (int i = 1; i < 13; i++)
            if (tableSLR[i][0].equals(s))
                for (int j = 1; j < 10; j++)
                    if (tableSLR[0][j].charAt(0) == a)
                        return tableSLR[i][j];
        return "";
    }

    public String find(char X, char a) {
        for (int i = 0; i < VnSet.size() + 1; i++) {
            if (table[i][0].charAt(0) == X)
                for (int j = 0; j < VtSet.size() + 1; j++) {
                    if (table[0][j].charAt(0) == a)
                        return table[i][j];
                }
        }
        return "";
    }

    public void insert(char X, char a, String s) {
        if (a == '~') a = '$';
        for (int i = 0; i < VnSet.size() + 1; i++) {
            if (table[i][0].charAt(0) == X)
                for (int j = 0; j < VtSet.size() + 1; j++) {
                    if (table[0][j].charAt(0) == a) {
                        table[i][j] = s;
                        return;
                    }
                }
        }
    }

    public void displayLL() {
        // ��� LL1
        Stack<Character> s = analyzeStatck;
        System.out.printf("%23s", s);
        System.out.printf("%13s", strInput.substring(index));
        System.out.printf("%10s", action);
        System.out.println();
    }

    public void displaySLR() {
        // ��� SLR
        System.out.printf("%25s", stackState);
        System.out.printf("%15s", stackSymbol);
        System.out.printf("%15s", strInput.substring(index));
        System.out.printf("%15s", action);
        System.out.println();
    }

    public void ouput() {
        System.out.println("*********first��********");
        for (Character c : VnSet) {
            HashSet<Character> set = firstSet.get(c);
            System.out.printf("%10s", c + "  ->   ");
            for (Character var : set)
                System.out.print(var);
            System.out.println();
        }
        System.out.println("**********first��**********");
        System.out.println("*********firstX��********");
        Set<String> setStr = firstSetX.keySet();
        for (String s : setStr) {
            HashSet<Character> set = firstSetX.get(s);
            System.out.printf("%10s", s + "  ->   ");
            for (Character var : set)
                System.out.print(var);
            System.out.println();
        }
        System.out.println("**********firstX��**********");
        System.out.println("**********follow��*********");

        for (Character c : VnSet) {
            HashSet<Character> set = followSet.get(c);
            System.out.print("Follow " + c + ":");
            for (Character var : set)
                System.out.print(var);
            System.out.println();
        }
        System.out.println("**********follow��**********");

        System.out.println("**********LL1Ԥ�������********");

        for (int i = 0; i < VnSet.size() + 1; i++) {
            for (int j = 0; j < VtSet.size() + 1; j++) {
                System.out.printf("%6s", table[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println("**********LL1Ԥ�������********");

        System.out.println("**********SLR�﷨������********");

        for (int i = 0; i < 12 + 1; i++) {
            for (int j = 0; j < 10; j++) {
                System.out.printf("%6s", tableSLR[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println("**********SLR�﷨������********");

    }

}