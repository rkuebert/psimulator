/*
 * Vytvoreno 1.2.2012
 */
package networkModule.L2;

/**
 * Represents settings of switchport in Network module in ethernet layer.
 *
 * @author neiss
 */
public class SwitchportSettings {

	protected final int switchportNumber;
	protected boolean isUp;
	protected EthernetInterface assignedInterface;

	public SwitchportSettings(int switchportNumber) {
		this.switchportNumber = switchportNumber;
		isUp=true;
	}

	@Override
	public String toString() {
		return "SwitchportSettings: iface="+assignedInterface.name+", isUp="+isUp+", nr="+switchportNumber;
	}
}
