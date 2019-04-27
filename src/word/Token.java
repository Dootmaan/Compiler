package word;

public class Token {
  private String real_word;      //��Ӧ���ı����ݣ����������������д���ű�
  private String real_content;   //�����﷨��д���õ�����
  private String name_code;      //����ʷ�������ʾ���������ֵ
  private String property;       //�ֱ�����������ֵ�����ڻ�ȡ������ֵ
  private Info info;
  
  public Token(String word,String real_content,String name_code,String property) {
    this.real_word=word;
    this.real_content=real_content;
    this.name_code=name_code;
    this.property=property;
  }
  
  public Token(String word,String real_content,String name,Info info) {
    this.real_word=word;
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
  
  /**
   * Method used in semantic analysis
   * @return
   */
  public String getRealWord() {
      return real_word;
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
