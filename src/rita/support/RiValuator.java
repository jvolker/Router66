package rita.support;

/**
 * Handles sorting of fixed-size Object array when insertion occirs.<P> 
 * The new object is inserted in the proper location, and the 
 * last object in the array, after the sort, is removed and returned.
 */
public interface RiValuator
{
  /**
   * Take a sorted Object[] and one new Object to  be inserted. <p> 
   * The new object is inserted in the proper location, and the 
   * last object in the array, after the sort, is removed and returned.
   */
  public Object filter(Object[] o, Object newObj);
}
