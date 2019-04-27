package semantics;

/*
 * 四元式中间代码
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
   * 用于System.out.println()
   */
  public String toString() {
    return "("+op+","+arg1+","+arg2+","+result+")";
  }
  
  /**
   * 和toString()方法实际上是一样的，只是为了方便中间代码生成部分的理解
   * @return
   */
  public String getCode() {
    return "("+op+","+arg1+","+arg2+","+result+")";
  }
}
