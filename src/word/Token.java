package word;

public class Token {
  private String real_content;
  private String name_code;
  private String property;
  private Info info;
  
  public Token(String real_content,String name_code,String property) {
    this.real_content=real_content;
    this.name_code=name_code;
    this.property=property;
  }
  
  public Token(String real_content,String name,Info info) {
    this.real_content=real_content;
    this.name_code=name;
    this.info=info;
  }
  
  /**
   * ���ظ�token��Ӧ���ֱ���
   * @return
   */
  public String getName() {
    return name_code;
  }
  
  public String getRealContent() {
    return real_content;
  }
  
  /**
   * ���ظ�token��Ӧ������ֵ���������һ����ʶ����ô�᷵��null
   * @return
   */
  public String getProperty() {
      return property;
  }
  
  /**
   * ���ظ�token��Ӧ�ķ��ű���ڣ��������һ��������ô�᷵��null
   * @return
   */
  public Info getInfo() {
    return info;
  }
  
  public String toString() {
    if(property!=null) {
      return "<"+name_code+","+property+">";
    }
    if(info!=null) {
      return "<"+name_code+","+info+">";
    }
    return "<"+name_code+", ?>";
    
  }
}
