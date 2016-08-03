package ostatni;

/*
 * Erstellt am 5.3.2012.
 */

import device.Device;
import networkModule.NetworkModule;
import networkModule.SwitchNetworkModule;
import networkModule.IpNetworkModule;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author neiss
 */
public class JenPokus {

	public JenPokus() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testIsAssignableFrom(){
		SwitchNetworkModule ss = new SwitchNetworkModule(new Device(1, "name1", Device.DeviceType.cisco_router));
		IpNetworkModule ip = new IpNetworkModule(new Device(1, "name2", Device.DeviceType.cisco_router));

//		System.out.println(ss.getClass().isAssignableFrom(IpNetworkModule.class));	//true
//		System.out.println(ip.getClass().isAssignableFrom(ss.getClass()));	// false
//		System.out.println(ss.getClass().isAssignableFrom(NetworkModule.class));	// false
//		System.out.println(NetworkModule.class.isAssignableFrom(ip.getClass()));	// true
//		System.out.println(NetworkModule.class.isAssignableFrom(ss.getClass()));	// true

		System.out.println("Tedka skutecny pouziti:");
		assertFalse(ss.isStandardTcpIpNetMod());
		assertTrue(ip.isStandardTcpIpNetMod());


	}
}
