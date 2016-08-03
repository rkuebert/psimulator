/*
 * created 19.3.2012
 */

package shared.Components.simulatorConfig;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class NatAccessListConfig {

	private int number;
	private String address;
	private String wildcard;

	public NatAccessListConfig() {
	}

	public NatAccessListConfig(int number, String address, String wildcard) {
		this.number = number;
		this.address = address;
		this.wildcard = wildcard;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getWildcard() {
		return wildcard;
	}

	public void setWildcard(String wildcard) {
		this.wildcard = wildcard;
	}
}
