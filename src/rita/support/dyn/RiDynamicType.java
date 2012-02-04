package rita.support.dyn;

import java.lang.reflect.*;

import rita.RiTa;
import rita.RiTaException;

/**
 * A dynamic proxy invocation handler that enables dynamic typing for local RiTa objects
 */
public class RiDynamicType implements InvocationHandler 
{
  protected Object object;
  protected Class objectClass;
  
  protected RiDynamicType(Object object) {
    this.object = object;
    this.objectClass = object.getClass();
  }

  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable 
  {    
    Method realMethod = null;
    try {
      realMethod = objectClass.getMethod(method.getName(), method.getParameterTypes());
      return realMethod.invoke(object, args);
    } 
    catch (Exception e) {
      /*System.out.println("RiDynamicType.invoke() :: Unable to invoke declared method: "+method);
      System.out.println("FOUND: "+RiTa.asList(objectClass.getMethods()));
      try {
        realMethod = objectClass.getMethod(method.getName(), method.getParameterTypes());
        return realMethod.invoke(object, args);
      } catch (Exception e1) {
              System.out.println("RiDynamicType.invoke() :: Unable to invoke public method: "+method);
                   System.out.println("FOUND: "+RiTa.asList(objectClass.getMethods()));*/
      //}
      String msg = null;
      if (e instanceof java.lang.IllegalAccessException)
        msg = "[ERROR] Make sure the class you are trying to (dynamically) cast is publicly defined.\n          ----------------------------------------------------\n";
      throw new RiTaException(msg, e);        
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
    return implement(new Class[] { iface }, new RiDynamicType(object));
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
  static Object implement(Class[] ifaces, Object object) {
    Object o = Proxy.newProxyInstance
      (object.getClass().getClassLoader(), ifaces, new RiDynamicType(object));
    //System.out.println("RiDynamicObject.implement() -> "+o);
    return o;
  }
  
  public static void main(String[] args) {
    Object o = new Object() {
      public void run() {
        System.out.println("run() called");
      }
    };
    // Note: b/c 'o' is read as an inner class of RiDynamicType,  
    // o.run() cannot be executed (at least for now) elsewhere. 
    System.out.println(o);
    Runnable r = (Runnable)RiTa.dynamicCast(o, Runnable.class);
    r.run();    
  }
  
}// end