package semantics;

/*
 * ��Ԫʽ�м����
 */
public class SemanticCode {
  private String op;
  private String arg1;
  private String arg2;
  private String result;
  
  public SemanticCode(String op,String arg1,String arg2,String result) {
    this.op=op;
    this.arg1=arg1;
    this.arg2=arg2;
    this.result=result;
  }
  
  /**
   * ����System.out.println()
   */
  public String toString() {
    return "("+op+","+arg1+","+arg2+","+result+")";
  }
  
  /**
   * ��toString()����ʵ������һ���ģ�ֻ��Ϊ�˷����м�������ɲ��ֵ����
   * @return
   */
  public String getCode() {
    return "("+op+","+arg1+","+arg2+","+result+")";
  }
}
