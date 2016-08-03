/*
 * created 19.3.2012
 */
package networkModule.L3.nat;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class PoolAccess implements Comparable<PoolAccess> {

	/**
	 * Cislo 1-2699.
	 */
	public final int access;
	/**
	 * Unikatni name poolu.
	 */
	public final String poolName;
	public final boolean overload;

	public PoolAccess(int access, String pool, boolean overload) {
		this.access = access;
		this.poolName = pool;
		this.overload = overload;
	}

	@Override
	public int compareTo(PoolAccess o) {
		if (access < o.access) {
			return -1;
		}
		if (access > o.access) {
			return 1;
		}
		return 0;
	}
}
