package rita.support;

import rita.RiTaException;

class PWord implements RiProbable
{
  public String word;
  float rawP, sum = -1;

  public PWord(String word, float rawP)
  {
    this.word = word;
    this.rawP = rawP;
  }

  public void normalize(float sum)
  {
    this.sum = sum;
  }

  public float getProbability()
  {
    if (sum < 0)
      throw new RiTaException("You must call normalize(sum) before getProbability();");
    return rawP / sum;
  }

  public String toString()
  {
    float prob = sum >= 0 ? getProbability() : rawP;
    return word + "(" + prob + ")";
  }

  public float getRawValue()
  {
    return rawP;
  }
}
