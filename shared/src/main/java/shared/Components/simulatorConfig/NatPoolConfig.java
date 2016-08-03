/*
 * created 19.3.2012
 */

package shared.Components.simulatorConfig;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class NatPoolConfig {

	private String name;
	private String start;
	private String end;
	private int prefix;

	public NatPoolConfig() {
	}

	public NatPoolConfig(String name, String start, String end, int prefix) {
		this.name = name;
		this.start = start;
		this.end = end;
		this.prefix = prefix;
	}

	public String getEnd() {
		return end;
	}

	public void setEnd(String end) {
		this.end = end;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPrefix() {
		return prefix;
	}

	public void setPrefix(int prefix) {
		this.prefix = prefix;
	}

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}
}
