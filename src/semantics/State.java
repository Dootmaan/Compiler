package semantics;

import java.util.ArrayList;
import java.util.List;

/**
 * DFA��һ��״̬��Ҳ��һ���հ�
 * @author samma
 *
 */
public class State {
  private int number;
  private List<String> items =new ArrayList<>();
  
  public State(int number) {
    this.number=number;
  }
  
  public int getNumber() {
    return number;
  }
  
  public void addItem(String item) {
    if(!items.contains(item))
      items.add(item);
  }
  
  public void removeItem(String item) {
    if(items.contains(item))
      items.remove(item);
  }
  
  public List<String> getItems() {
    return items;
  }
}
