/*
 * created 19.3.2012
 */

package shared.Components.simulatorConfig;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class NatPoolAccessConfig {
	private int number;
	private String poolName;
	private boolean overload;

	public NatPoolAccessConfig() {
	}

	public NatPoolAccessConfig(int number, String poolName, boolean overload) {
		this.number = number;
		this.poolName = poolName;
		this.overload = overload;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public boolean isOverload() {
		return overload;
	}

	public void setOverload(boolean overload) {
		this.overload = overload;
	}

	public String getPoolName() {
		return poolName;
	}

	public void setPoolName(String poolName) {
		this.poolName = poolName;
	}
}
