/*
 * created 2.5.2012
 */

package physicalModule;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents statistics on switchport.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class Stats {

	public AtomicInteger droppedPackets = new AtomicInteger(0);
	public AtomicInteger processedPackets = new AtomicInteger(0);
	public AtomicInteger processedBytes = new AtomicInteger(0);
}
