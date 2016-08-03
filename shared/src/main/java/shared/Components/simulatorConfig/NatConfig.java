/*
 * created 3.3.2012
 */

package shared.Components.simulatorConfig;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class NatConfig {

	private List<String> inside = new ArrayList<>();
	private String outside;
	private List<NatPoolConfig> pools = new ArrayList<>();
	private List<NatPoolAccessConfig> poolAccesses = new ArrayList<>();
	private List<NatAccessListConfig> accessLists = new ArrayList<>();
	private List<StaticRule> rules = new ArrayList<>();

	public NatConfig() {
	}

	public List<NatAccessListConfig> getAccessLists() {
		return accessLists;
	}

	public void setAccessLists(List<NatAccessListConfig> accessLists) {
		this.accessLists = accessLists;
	}

	public List<String> getInside() {
		return inside;
	}

	public void setInside(List<String> inside) {
		this.inside = inside;
	}

	public String getOutside() {
		return outside;
	}

	public void setOutside(String outside) {
		this.outside = outside;
	}

	public List<NatPoolAccessConfig> getPoolAccesses() {
		return poolAccesses;
	}

	public void setPoolAccesses(List<NatPoolAccessConfig> poolAccesses) {
		this.poolAccesses = poolAccesses;
	}

	public List<NatPoolConfig> getPools() {
		return pools;
	}

	public void setPools(List<NatPoolConfig> pools) {
		this.pools = pools;
	}

	public List<StaticRule> getRules() {
		return rules;
	}

	public void setRules(List<StaticRule> rules) {
		this.rules = rules;
	}
}
