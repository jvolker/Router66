package rita.support;

public class RiTest 
{
  public RiTest(Object m, Object n, boolean c) {
    a = m;
    b = n;
    val = c;
  }

  Object a, b;
  boolean val;

  public String a() { return (String)a; }
  public String b() { return (String)b; }
  public boolean value() { return val; }
  
  public String toString() {
    return "RiTest("+a+ ","+b+") expects '" + val+"'";
  }
}