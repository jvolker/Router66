package rita.support.dyn;

/**
 * Supports object that can respond to method
 * calls for methods they do not contain (a la Ruby's method-missing mechanism).
 */
public interface RiMessagable
{
  public Object methodMissing(String methodName, Object[] args);
}
