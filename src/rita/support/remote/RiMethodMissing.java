package rita.support.remote;

import rita.RiTa;
import rita.RiTaException;

public class RiMethodMissing extends RiTaException
{
  protected String methodName;
  protected Class[] paramTypes;
  protected Object[] params;
  protected Object callee;
  
  public RiMethodMissing(Object callee, String methodName) {
    this(callee, methodName, (Object[])null, (Class[])null);
  }
  
  public RiMethodMissing(Object callee, String methodName, Object arg, Class type) {
    this(callee, methodName, new Object[]{arg}, new Class[] {type});
  }
  
  public RiMethodMissing(Object callee, String methodName, Object[] args, Class[] types) {
    super();   
    this.methodName = methodName;
    this.params = args;
    this.paramTypes = types;
    this.callee = callee;
  }
  
  public String toString()
  {
    return "MissingMethodException: for "+callee.getClass().getName()+"."+
      methodName+"("+RiTa.asList(params)+") : ["+RiTa.asList(paramTypes)+"]"; 
  }
   

  public RiMethodMissing(String message, Throwable cause) {
    super(message, cause);
    
  }

  public RiMethodMissing(String message) { 
    super(message);
    
  }

  public RiMethodMissing(Throwable cause) {
    super(cause);    
  }

  public String getMethodName()
  {
    return this.methodName;
  }

  public void setMethodName(String methodName)
  {
    this.methodName = methodName;
  }

  public Object[] getParams()
  {
    return this.params;
  }

  public void setParams(Object[] params)
  {
    this.params = params;
  }

  public Class[] getParamTypes()
  {
    return this.paramTypes;
  }

  public void setParamTypes(Class[] paramTypes)
  {
    this.paramTypes = paramTypes;
  }

  public Object getCallee()
  {
    return this.callee;
  }

  public void setCallee(Object callee)
  {
    this.callee = callee;
  }

}// end
