package semantics;

public class Trans {
  private State src;
  private State dst;
  private String condition;
  
  public Trans(State src,State dst,String condition) {
    this.src=src;
    this.dst=dst;
    this.condition=condition;
  }
  
  public State getSrc() {
    return src;
  }
  
  public State getDst() {
    return dst;
  }
  
  public String getCondition() {
    return condition;
  }
}
