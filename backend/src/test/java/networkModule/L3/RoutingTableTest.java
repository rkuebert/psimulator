/*
 * Erstellt am 23.2.2012.
 */
package networkModule.L3;

import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.ipAddresses.IpAddress;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author neiss
 */
public class RoutingTableTest {

	RoutingTable rt;
    NetworkInterface eth0;
    NetworkInterface wlan0;

	public RoutingTableTest() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
    public void setUp() {
        rt = new RoutingTable();
        eth0=new NetworkInterface(1, "eth0",null);
        wlan0=new NetworkInterface(2, "wlan0", null);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void prvniTest(){
        System.out.println("Prvni test ------------------------------------------------------------------------");
		assertTrue(new IPwithNetmask("1.1.1.0",24).isInMyNetwork(new IpAddress("1.1.1.1")));

        assertEquals( 2 , rt.addRecord(new IPwithNetmask("0.0.0.0",0),new IpAddress("1.1.1.1"), null));
        assertEquals( 0 , rt.addRecord(new IPwithNetmask("1.1.1.0",24), eth0));
        assertEquals( 0 , rt.addRecord(new IPwithNetmask("0.0.0.0",0),new IpAddress("1.1.1.1"), null));
        assertEquals( 1 , rt.addRecord(new IPwithNetmask("0.0.0.0",0),new IpAddress("1.1.1.1"), null));
        assertEquals( 0 , rt.addRecord(new IPwithNetmask("1.1.2.128",25), wlan0) );
        assertEquals( 1 , rt.addRecord(new IPwithNetmask("1.1.2.128",25), wlan0) );
        assertEquals( 0 , rt.addRecord(new IPwithNetmask("1.1.2.128",25),eth0) );
        assertEquals( 0 , rt.addRecord(new IPwithNetmask("2.0.0.0",1),wlan0) );

        System.out.println(rt.vypisSeLinuxove());

        assertTrue(rt.deleteRecord((new IPwithNetmask("1.1.1.0",24)), null, null));
        assertTrue(rt.deleteRecord((new IPwithNetmask("1.1.2.128",25)), null, wlan0));
        assertFalse(rt.deleteRecord((new IPwithNetmask("1.1.2.128",25)), null, wlan0));
        assertTrue(rt.deleteRecord((new IPwithNetmask("1.1.2.128",25)), null, eth0));
        assertFalse(rt.deleteRecord((new IPwithNetmask("0.0.0.0",0)), null, wlan0));
        assertTrue(rt.deleteRecord((new IPwithNetmask("0.0.0.0",0)), new IpAddress("1.1.1.1"),null));
        assertFalse(rt.deleteRecord((new IPwithNetmask("2.0.0.0",1)), new IpAddress("1.1.1.1"), wlan0));
        assertTrue(rt.deleteRecord((new IPwithNetmask("2.0.0.0",1)), null, wlan0));

        System.out.println(rt.vypisSeLinuxove());
    }

    @Test
    public void druhyTest(){
        System.out.println("Druhy test ------------------------------------------------------------------------");

        assertEquals( 0 , rt.addRecord(new IPwithNetmask("1.1.2.128",25), wlan0) );
        assertEquals( 0 , rt.addRecord(new IPwithNetmask("1.1.2.128",25), eth0) );
        assertEquals(0, rt.addRecord(new IPwithNetmask("1.1.2.128",25), new IpAddress("1.1.2.129"), wlan0));
        assertEquals(0, rt.addRecord(new IPwithNetmask("1.1.2.128",25), new IpAddress("1.1.2.130"), eth0));

        System.out.println(rt.vypisSeLinuxove());

    }
}
