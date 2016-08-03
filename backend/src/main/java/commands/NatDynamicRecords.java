/*
 * created 20.3.2012
 */

package commands;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class NatDynamicRecords extends AbstractCommand {

	public NatDynamicRecords(AbstractCommandParser parser) {
		super(parser);
	}

	@Override
	public void run() {
		printService("Dynamic NAT rules in use");
		printWithDelay(getNetMod().ipLayer.getNatTable().getDynamicRulesInUse(), 30);
	}
}
