package semantics;

import java.util.ArrayList;
import java.util.List;

public class DFAGraph {
  private List<State> states=new ArrayList<>();
  private List<Trans> trans=new ArrayList<>();
  
  public boolean addState(State s) {
    if(!states.contains(s)) {
      states.add(s);
      return true;
    }
    return false;
  }
  
  public boolean addTrans(Trans s) {
    if(!trans.contains(s)) {
      trans.add(s);
      return true;
    }
    return false;
  }
  
  /**
   * 返回一个状态下特定输入对应的下一个状态，如无法跳转返回null
   * @param nowState
   * @param condition
   * @return
   */
  public State getNextState(State nowState,String condition) {
    for(Trans t:trans) {
      if(t.getSrc().equals(nowState)&&t.getCondition().equals(condition)) {
        return t.getDst();
      }
    }
    return null;
  }
  
  public List<Trans> getTrans(){
    return trans;
  }
  
  public List<State> getStates(){
    return states;
  }
}
