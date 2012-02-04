package rita.support.dyn;

import java.lang.reflect.Method;

import rita.RiTa;
import rita.RiTaException;

/**
 * Implements dynamic typing (eg 'duck-typing') for RiTa objects -- allowing programs to treat
 * objects from separate hierarchies and packages as the same, assuming they
 * implement the specified interfaces (which are checked only at runtime).
 */
public class RiDynamicObject
{  
  protected Object delegate;

  public RiDynamicObject(Class dynamicProxyClass, Class iface) {
    this((Object)dynamicProxyClass, iface);
  }
  
  public RiDynamicObject(Class dynamicProxyClass, Class[] ifaces) {
    this((Object)dynamicProxyClass, ifaces);
  }
  
  public RiDynamicObject(Object dynamicProxy, Class iface) {
    this(dynamicProxy, new Class[]{ iface });
  }
  
  public RiDynamicObject(Object dynamicProxy, Class[] ifaces) {
    //System.out.println("RiDynamicObject.RiDynamicObject1("+dynamicProxy+","+RiTa.asList(ifaces)+")");
    dynamicProxy = multiGetClass(dynamicProxy);
    //System.out.println("RiDynamicObject.RiDynamicObject2("+dynamicProxy+","+RiTa.asList(ifaces)+")");
    for (int i = 0; i < ifaces.length; i++) {
      if (!instanceOf(ifaces[i], dynamicProxy)) 
        throw new RiTaException(dynamicProxy.getClass()+" cannot dynamically implement "+ifaces[i].getCanonicalName());
    }    
    delegate = RiDynamicType.implement(ifaces, dynamicProxy);  
  } 
  
  /**
   * Indicates if an object is a Dynamic (DuckTyped) instance of an interface. 
   * 
   * @param iface The interface to implement
   * @param object The object to test
   * @return true if every method in the interface is present in the object, 
   *         otherwise false
   */
  public static boolean instanceOf(Class iface, Object object) {
    //System.out.println("RiDynamicType.instanceOf("+object+")"); 
    final Method[] methods = iface.getMethods();
    Class candClass = object.getClass();
    for (int i = 0; i < methods.length; i++) {
      Method method = methods[i];
      //System.out.println(i+") "+methods[i]);
      try {
        candClass.getMethod(method.getName(), method.getParameterTypes());        
      } catch (NoSuchMethodException e) {
        //System.err.println("[ERROR] no match for method: "+method.getName()+"("+RiTa.asList(method.getParameterTypes())+");");
        return false;     
      } catch (Throwable e) {
        throw new RiTaException("Unexpected Exception! no match for method: "+method.getName()+"("+RiTa.asList(method.getParameterTypes())+");");
      }        
    }
    return true;
  }
  
  private static Object multiGetClass(Object o) {
    try {
      if (o instanceof Class) 
        return ((Class)o).newInstance();     
      return o;      
    } catch (Exception e) {
      throw new RiTaException(e);
    }   
  } 

  public static void main(String[] args) {
    Object o = new Object() {
      public void run() {
        System.out.println("run() called");
      }
    };
/*    Test rdo = new Test(o);
    o.run();*/
    
    RiDynamicObject rdo = new RiDynamicObject(o, Runnable.class);
    Runnable r = (Runnable)rdo.delegate;
    r.run();
  }

  public Object getDelegate() {
    return delegate;
  }
  
 /* class Test extends RiDynamicObject implements Runnable
  {
    public Test(Object dynamicProxy) {
      super(dynamicProxy, Runnable.class);
    }
  }*/

}// end
