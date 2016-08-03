/*
 * created 5.3.2012
 */
package shared.Components.simulatorConfig;

/**
 * Ukladaci struktura pro jeden zaznam.
 * Nemuze bejt ve trite RoutingTableConfig, proto6e to marshaller neumi.
 */
public class Record {

	private String destination;
	private String interfaceName;
	private String gateway;

	public Record(String destination, String interfaceName, String gateway) {
		this.destination = destination;
		this.interfaceName = interfaceName;
		this.gateway = gateway;
	}

	/**
	 * Prazdnej konstruktor pro marshaller.
	 */
	public Record() {
	}



	public String getDestination() {
		return destination;
	}

	public String getGateway() {
		return gateway;
	}

	public String getInterfaceName() {
		return interfaceName;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public void setGateway(String gateway) {
		this.gateway = gateway;
	}

	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}
}