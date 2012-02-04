package rita.support.grammar;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class RuleList
{
  // name -> Map(rule, prob)
  
  private Map<String, Map<String, Float>> prules;

  public RuleList()
  {
    prules = new HashMap<String, Map<String, Float>>();
  }

  public Iterator<String> iterator()
  {
    return prules.keySet().iterator();
  }

  public Set<String> keySet()
  {
    return prules.keySet();
  }

/*  public void addRule(String pre, String rule) throws RuntimeException
  {
    addRule(pre, rule, 1.0f);
  }
*/
  public void addRule(String name, String rule, float weight) throws RuntimeException
  {
    Map<String, Float> temp;
    if (hasRule(name)) // we store multiple rules in existing map
    {
      temp = prules.get(name);
      temp.put(rule, weight);
    }
    else
    // we need a new rule/weight map
    {
      Map<String, Float> temp2 = new HashMap<String, Float>();
      temp2.put(rule, weight);
      prules.put(name, temp2);
    }
  }

  private String getStochasticRule(Map<String, Float> weightedRules)
  {
    String result = null;
    Map<String, Float> temp = weightedRules;
    Collection<Float> values = temp.values();
    Iterator<Float> it = values.iterator();
    float total = 0;
    double p = Math.random();
    while (it.hasNext())
    {
      total += it.next();
    }
    for (Iterator iterator = temp.entrySet().iterator(); iterator.hasNext();)
    {
      Map.Entry entry = (Map.Entry) iterator.next();
      if (p < (Float) entry.getValue() / total)
      {
        result = (String) entry.getKey();
        break;
      }
      else
      {
        p -= (Float) entry.getValue() / total;
      }
    }
    return result;
  }

  public String getRule(String pre)
  {
    Map<String, Float> temp = prules.get(pre);
    if (temp.size() == 1)
    {
      Object[] result = temp.keySet().toArray();
      return (String) result[0];
    }
    else
    {
      return getStochasticRule(temp);
    }
  }

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();//"Rules:\n");
    String ch = " ";
    for (Iterator it = prules.entrySet().iterator(); it.hasNext();)
    {
      Map.Entry entrySet = (Map.Entry) it.next();
      ch = (String) entrySet.getKey();
      sb.append(ch);
      sb.append(" =>\n");
      Map rules = (Map) entrySet.getValue();
      for (Iterator iterator = rules.entrySet().iterator(); iterator.hasNext();)
      {
        Map.Entry entry = (Map.Entry) iterator.next();
        String rule = (String) entry.getKey();
        sb.append("    ");
        sb.append("'"+rule+"'");
        Float weight = (Float) entry.getValue();
        sb.append(" [");
        sb.append(weight);
        sb.append(']');
        sb.append('\n');
      }
    }
    return sb.toString();
  }

  /**
   * 
   * Empty collections on dispose
   */
  public void clear()
  {
    // premises.clear();
    prules.clear();
  }

  public boolean hasRule(String pre)
  {
    return prules.keySet().contains(pre);
  }
}