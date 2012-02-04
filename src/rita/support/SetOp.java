package rita.support;

import java.util.*;

// basics from javanlp
/**
 * Utility operations that can be performed on Sets.
 */
public final class SetOp 
{
  /** Converts a Set to a new String[]  */
	public static String[] toStringArray(Set set) {
    if (set == null || set.size()==0)
      return new String[0];
		int idx = 0;
		String[] s = new String[set.size()];
		for (Iterator i = set.iterator(); i.hasNext();) 
			s[idx++] = (String)i.next();	
		return s;
	}
	
	/**
   * returns the set cr oss product of s1 and s2, as <code>Pair</code>s
   */
	public static Set cross(Set s1, Set s2) {
		Set s = new HashSet();
		for (Iterator i = s1.iterator(); i.hasNext();) {
			Object o1 = i.next();
			for (Iterator j = s2.iterator(); j.hasNext();) {
				Object o2 = j.next();
				s.add(new ObjectPair(o1, o2));
			}
		}
		return s;
	}

	/** Set difference s1 - s2 */
	public static Set diff(Set s1, Set s2) {
		Set s = new HashSet();
		for (Iterator i = s1.iterator(); i.hasNext();) {
			Object o = i.next();
			if (!s2.contains(o))
				s.add(o);
		}
		return s;
	}

	/** Set union */
	public static Set union(Set s1, Set s2) {
		Set s = new HashSet();
		s.addAll(s1);
		s.addAll(s2);
		return s;
	}

	/** Set intersection */
	public static Set intersection(Set s1, Set s2) {
		Set s = new HashSet();
		s.addAll(s1);
		s.retainAll(s2);
		return s;
	}

	/** returns the powerset of a set */
	public static Set powerSet(Set s) 
	{
		if (s.isEmpty()) 
		{
			Set h = new HashSet();
			Set h0 = new HashSet(0);
			h.add(h0);
			return h;
		} 
		else {
			Iterator i = s.iterator();
			Object elt = i.next();
			s.remove(elt);
			Set pow = powerSet(s);
			Set pow1 = powerSet(s);
			for (Iterator j = pow1.iterator(); j.hasNext();) {
				Set t = new HashSet((Set) j.next());
				t.add(elt);
				pow.add(t);
			}
			s.add(elt);
			return pow;
		}
	}

	public static void main(String[] args) {
		Set h = new HashSet();
		h.add("a");
		h.add("b");
		h.add("c");
		Set pow = powerSet(h);
		System.out.println(pow);
	}

} // end

