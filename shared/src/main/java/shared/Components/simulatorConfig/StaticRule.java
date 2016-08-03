/*
 * created 19.3.2012
 */

package shared.Components.simulatorConfig;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class StaticRule {
	private String in;
	private String out;

	public StaticRule() {
	}

	public StaticRule(String in, String out) {
		this.in = in;
		this.out = out;
	}

	public String getIn() {
		return in;
	}

	public void setIn(String in) {
		this.in = in;
	}

	public String getOut() {
		return out;
	}

	public void setOut(String out) {
		this.out = out;
	}
}
