/*
 * Erstellt am 2.3.2012.
 */

package shared.Components.simulatorConfig;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;

/**
 * Ukladaci struktura pro routovaci tabulku.
 * @author Tomas Pitrinec
 */
public class RoutingTableConfig {

	private List<Record> records = new ArrayList<>();	// podle Martina L. tohle marshaller umoznuje

	@XmlElement(name = "routingTableItem")
	public List<Record> getRecords() {
		return records;
	}

	public void setRecords(List<Record> records) {
		this.records = records;
	}

	public void addRecord(String destination,String interfaceName, String gateway){
		records.add(new Record(destination, interfaceName, gateway));
	}
}
