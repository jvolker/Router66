package rita.support;

/**
 * A utility class for holding a pair of objects. 
 */
public class ObjectPair implements Comparable
{
	private Object first, second;

	public ObjectPair() {}

	public ObjectPair(Object first, Object second) {
		this.first = first;
		this.second = second;
	}

	public Object first() {
		return first;
	}

	public Object second() {
		return second;
	}

	public void setFirst(Object o) {
		first = o;
	}

	public void setSecond(Object o) {
		second = o;
	}

	public String toString() {
		return "(" + first + "," + second + ")";
	}

	public boolean equals(Object o) {
		if (o instanceof ObjectPair) {
			ObjectPair p = (ObjectPair) o;
			return (first == null ? p.first == null : first.equals(p.first))
					&& (second == null ? p.second == null : second.equals(p.second));
		} else {
			return false;
		}
	}

	public int hashCode() {
		return (((first == null) ? 0 : first.hashCode()) << 16)
				^ ((second == null) ? 0 : second.hashCode());
	}

	/**
   * Compares this <code>ObjectPair</code> to another object. If the object is
   * a <code>ObjectPair</code>, this function will work providing the
   * elements of the <code>ObjectPair</code> are themselves comparable. It
   * will then return a value based on the pair of objects, where
   * <code>p &gt; q iff p.first() &gt; q.first() || 
	 *      (p.first().equals(q.first()) && p.second() &gt; q.second())</code>.
   * If the other object is not a <code>ObjectPair</code>, it throws a
   * <code>ClassCastException</code>.
   * 
   * @param o
   *          the <code>Object</code> to be compared.
   * @return the value <code>0</code> if the argument is a
   *         <code>ObjectPair</code> equal to this <code>ObjectPair</code>;
   *         a value less than <code>0</code> if the argument is a
   *         <code>ObjectPair</code> greater than this <code>ObjectPair</code>;
   *         and a value greater than <code>0</code> if the argument is a
   *         <code>ObjectPair</code> less than this <code>ObjectPair</code>.
   * @exception ClassCastException
   *              if the argument is not a <code>ObjectPair</code>.
   * @see java.lang.Comparable
   */
	public int compareTo(Object o) {
		ObjectPair another = (ObjectPair) o;
		int comp = ((Comparable) first()).compareTo(another.first());
		if (comp != 0) {
			return comp;
		} else {
			return ((Comparable) second()).compareTo(another.second());
		}
	}

}// end

