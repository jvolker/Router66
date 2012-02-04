package rita.support.dyn;

import java.lang.reflect.*;

import rita.RiTaException;
import rita.support.remote.RiClientStub;
import rita.support.remote.RiWordnetSupport;

/**
 * A dynamic proxy invocation handler that enables dynamic typing for remote RiTa objects
 */
public class RiDynamicRemote implements InvocationHandler 
{
  protected RiClientStub stub;
  protected Object object;
  protected Class objectClass;
  
  protected RiDynamicRemote(Object object) {
    //super(object);
    this.object = object;
    this.objectClass = object.getClass();
    try {
      this.stub = new RiClientStub(Class.forName(RiWordnetSupport.RITA_WORDNET_CLASS));
      stub.createRemote(); 
    }
    catch (ClassNotFoundException e) {
      throw new RiTaException(e);
    }
  }
  
  /**
   * Causes object to implement the interface and returns an instance
   * of the object implementing interface even if
   * interface was not declared in object.getClass()'s implements
   * declaration.
   * 
   * This works as long as all methods declared in interface are
   * present in the object.
   * 
   * @param iface
   *          The Java class of the interface to implement
   * @param object
   *          The object to force to implement the interface
   *          
   * @return the object, but now implementing the interface
   */
  public static Object implement(Class iface, Object object) {
    return implement(new Class[] { iface }, new RiDynamicRemote(object));
  }
  
  /**
   * Causes object to implement the listed interfaces. Is succesful
   * is all methods declared in the interfaces are present in the object.
   * 
   * @param ifaces
   *          an array of Java classes representing the interfaces to implement
   * @param object
   *          The object to force to implement the interfaces
   *          
   * @return the object, but now implementing the interfaces
   */
  public static Object implement(Class[] ifaces, Object object) {
    //System.out.println("RiDynamicRemote1.implement() -> "+object);
    RiDynamicRemote rdr = new RiDynamicRemote(object);
    return Proxy.newProxyInstance(object.getClass().getClassLoader(), ifaces, rdr);
  }
  
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable 
  {
    //System.out.println("RiDynamicRemote.invoke("+method.getName()+"();)");
    String s = stub.exec(method.getName(), args, method.getParameterTypes());
    Class returns = method.getReturnType();
    Object result = s;
    if (returns.isArray())  
      result =  stub.toStrArr(s);
    else if (returns==Integer.TYPE)
      result = Integer.parseInt(s);
    else if (returns==Boolean.TYPE)
      result = Boolean.parseBoolean(s);
    else if (returns==Float.TYPE)
      result = Float.parseFloat(s);
    else if (returns==Character.TYPE)
      result = s.charAt(0);    
    return result;    
  }

  
}// end