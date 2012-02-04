package rita.support;

/**
 * Simple interface for probabilistic objects.
 */
public interface RiProbable
{
  /**
   * Returns a probability value between 0 - 1
   */
  public float getProbability();
  
  /**
   * Returns the raw value from which probability will be calculated
   */
  public float getRawValue();
}
