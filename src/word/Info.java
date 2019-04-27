package word;

/**
 * Info类，用于符号表的构建
 * @author samma
 *
 */
public class Info {
  private String name;
  private int length;
  private String type;
  private String value;
  
  public Info(String name) {
    this.name=name;
  }
  
  public Info(String name,int length,String type,String value) {
    this.setName(name);
    this.setLength(length);
    this.setType(type);
    this.setValue(value);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
  
  public String toString() {
    return name+"\t|"+type+"\t|"+length+"\t|"+value;
  }

  public int getLength() {
    return length;
  }

  public void setLength(int length) {
    this.length = length;
  }

}
