package shared.SimulatorEvents.SerializedComponents;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public enum PacketType {
    TCP,        // green
    UDP,        // blue
    ICMP,       // gray
    ARP,        // yellow
    GENERIC,    // pink
	ETHERNET,	// shouldn't happen, black
	IP;		    // shouldn't happen, black
}
