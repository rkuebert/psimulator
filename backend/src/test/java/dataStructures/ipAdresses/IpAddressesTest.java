/*
 * Erstellt am 29.11.2011.
 */
package dataStructures.ipAdresses;

import commands.linux.Ip;
import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.ipAddresses.IpNetmask;
import dataStructures.ipAddresses.BadNetmaskException;
import dataStructures.ipAddresses.BadIpException;
import dataStructures.ipAddresses.IpAddress;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Tomas Pitrinec
 */
public class IpAddressesTest {

    public IpAddressesTest() {
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
    public void testIP() {

        IpAddress ip;
        String adr;

        adr = "147.32.125.138";
        ip = new IpAddress(adr);
        assertEquals(ip.toString(), adr);

        adr = "0.0.0.0";
        ip = new IpAddress(adr);
        assertEquals(ip.toString(), adr);

        adr = "1.1.1.1";
        ip = new IpAddress(adr);
        assertEquals(ip.toString(), adr);

        adr = "192.168.1.0";
        ip = new IpAddress(adr);
        assertEquals(ip.toString(), adr);
    }

    @Test
    public void testNetmask() {
        IpNetmask maska;

        maska = new IpNetmask(24);
        System.out.println(maska.toString());
        assertEquals(maska.toString(), "255.255.255.0");

        maska = new IpNetmask(25);
        System.out.println(maska.toString());
        assertEquals(maska.toString(), "255.255.255.128");

        maska = new IpNetmask(23);
        System.out.println(maska.toString());
        assertEquals(maska.toString(), "255.255.254.0");

        maska = new IpNetmask(0);
        System.out.println(maska.toString());
        assertEquals(maska.toString(), "0.0.0.0");

        maska = new IpNetmask(32);
        System.out.println(maska.toString());
        assertEquals(maska.toString(), "255.255.255.255");

        maska = new IpNetmask(7);
        System.out.println(maska.toString());
        assertEquals(maska.toString(), "254.0.0.0");

        System.out.println("------------------------------------------");

        maska = new IpNetmask("255.255.255.0");
        System.out.println(maska.toString());
        assertEquals(maska.toString(), "255.255.255.0");

        maska = new IpNetmask("255.255.255.128");
        System.out.println(maska.toString());
        assertEquals(maska.toString(), "255.255.255.128");

        try {
            maska = new IpNetmask("43.23.234.43");
            fail();
        } catch (BadNetmaskException ex) {
        }

        try {
            maska = new IpNetmask(33);
            fail();
        } catch (BadNetmaskException ex) {
        }
    }

    @Test
    public void testBroadcast() {
        System.out.println("------------------------------------------");
        IPwithNetmask adr = new IPwithNetmask("192.168.1.0", 24);
        assertEquals(adr.getBroadcast().toString(), "192.168.1.255");

        adr = new IPwithNetmask("192.168.1.0", 0); //vsechno je cislo pocitace -> cislo site je 0.0.0.0/32
        assertEquals(adr.getBroadcast().toString(), "255.255.255.255");

        adr = new IPwithNetmask("192.168.1.0", 32);//vsechno je cislo site -> cislo site je 192.168.1.0/32
        assertEquals(adr.getBroadcast().toString(), "192.168.1.0");

        adr = new IPwithNetmask("192.168.1.0", 30); //  cislo site je 192.168.1.0/30
        assertEquals(adr.getBroadcast().toString(), "192.168.1.3");
    }

    @Test
    public void testCisloSite() {
        IpAddress ip = new IpAddress("147.32.125.234");

        IPwithNetmask ipnm = new IPwithNetmask(ip, 24);
        assertEquals(ipnm.getNetworkNumber().toString(), "147.32.125.0/24");

        ipnm = new IPwithNetmask(ip, 23);
        assertEquals(ipnm.getNetworkNumber().toString(), "147.32.124.0/23");

        ipnm = new IPwithNetmask(ip, 25);
        assertEquals(ipnm.getNetworkNumber().toString(), "147.32.125.128/25");
    }

//    @Test
//    public void testCisloPocitaceVSiti(){
//        // Tahle metoda byla ve starym simulatoru, ted je zrusena, kdyztak ji zkopirovat.
//    }
    @Test
    public void testAdresaSMaskou() {
        IpAddress ip = new IpAddress("147.32.125.138");
        IPwithNetmask ipnm;

        ipnm = new IPwithNetmask(ip, 24);
        assertEquals(ipnm.toString(), "147.32.125.138/24");

        ipnm = new IPwithNetmask(ip, 23);
        assertEquals(ipnm.toString(), "147.32.125.138/23");

        ipnm = new IPwithNetmask(ip, 25);
        assertEquals(ipnm.toString(), "147.32.125.138/25");

        ipnm = new IPwithNetmask(ip, 0);
        assertEquals(ipnm.toString(), "147.32.125.138/0");

        ipnm = new IPwithNetmask(ip, 32);
        assertEquals(ipnm.toString(), "147.32.125.138/32");
    }

    /**
     * Ve starym simulatoru se to jmenovalo testIsInMyNetwork
     */
    @Test
    public void testIsInMyNetwork() {
        IPwithNetmask adr = new IPwithNetmask("0.0.0.0", 0);
        assertTrue(adr.isInMyNetwork(new IpAddress("1.1.1.1")));
        assertTrue(adr.isInMyNetwork(new IpAddress("0.0.0.0")));

        adr = new IPwithNetmask("89.190.94.1", 24);
        assertTrue(adr.isInMyNetwork(new IpAddress("89.190.94.128")));
        assertTrue(adr.isInMyNetwork(new IpAddress("89.190.94.0")));
        assertFalse(adr.isInMyNetwork(new IpAddress("89.190.93.0")));
        assertTrue(adr.isInMyNetwork(new IpAddress("89.190.94.1")));
    }

//    @Test
//    public void testJeVRozsahu(){
//        //tohle bylo taky v novym simulatoru zruseno
//    }
    @Test //jen takovej neuplnej test
    public void vytvareniIp() {
        IpAddress ip;
        ip = new IpAddress("1.1.1.1");
        ip = new IpAddress("19.255.255.0");
        ip = new IpAddress("10.0.0.0");
        ip = new IpAddress("192.168.1.1");
        ip = new IpAddress("255.0.0.0"); // i tohle musi projit (napriklad v LinuxRoute)
    }

    @Test
    public void testSpravnaAdresaNebMaska() {
        //testovani masky:
        assertNotNull(IpNetmask.correctNetmask("255.255.255.0"));
        assertNotNull(IpNetmask.correctNetmask("0.0.0.0"));
        assertNotNull(IpNetmask.correctNetmask("255.255.255.255"));
        assertNotNull(IpNetmask.correctNetmask("255.255.255.128"));
        assertNotNull(IpNetmask.correctNetmask("192.0.0.0"));

        assertNull(IpNetmask.correctNetmask("255.255.255.3"));
        assertNull(IpNetmask.correctNetmask("255.255.255.32"));
        assertNull(IpNetmask.correctNetmask("255.255.255.129"));
        assertNull(IpNetmask.correctNetmask("255.0.155.0"));

        //testovani adresy:
        assertNotNull(IpAddress.correctAddress("192.168.1.1"));
        assertNotNull(IpAddress.correctAddress("1.1.1.1"));
        assertNotNull(IpAddress.correctAddress("10.10.10.10"));

        assertNull(IpAddress.correctAddress("0.0.256.0"));
        assertNull(IpAddress.correctAddress("0.0.254.0.9"));
        assertNull(IpAddress.correctAddress("1,1,1,1"));
    }

    @Test
    public void testVratOJednickuVetsi() {
        IpAddress ip = new IpAddress("1.1.1.1");
        assertEquals("1.1.1.2", IpAddress.nextAddress(ip).toString());
        ip = new IpAddress("240.0.0.0");
        assertEquals("240.0.0.1", IpAddress.nextAddress(ip).toString());
        ip = new IpAddress("240.0.0.255");
        assertEquals("240.0.1.0", IpAddress.nextAddress(ip).toString());
    }

	@Test
	public void testVratMaskuZWildCard() {
		IpAddress ip = new IpAddress("0.0.0.31");
		assertEquals("255.255.255.224", IpAddress.negateAddress(ip).toString());
		ip = new IpAddress("0.0.0.3");
		assertEquals("255.255.255.252", IpAddress.negateAddress(ip).toString());

		IpNetmask mask;
		mask = IpNetmask.maskFromWildcard("0.0.0.255");
		assertEquals("255.255.255.0", mask.toString());
		mask = IpNetmask.maskFromWildcard("0.0.0.31");
		assertEquals("255.255.255.224", mask.toString());
		mask = IpNetmask.maskFromWildcard("0.0.0.3");
		assertEquals("255.255.255.252", mask.toString());

		IpNetmask wildcard = IpNetmask.maskFromWildcard("0.0.0.3");
		mask = new IpNetmask("255.255.255.252");
		assertEquals(wildcard, mask);

		assertEquals("0.0.0.3", mask.getWildcardRepresentation());
		assertEquals("0.0.0.31", IpNetmask.maskFromWildcard("0.0.0.31").getWildcardRepresentation());



	}


    @Test
    public void testVytvorAdresu(){
        assertEquals("1.1.1.1/32", new IPwithNetmask("1.1.1.1",32,false).toString());
        assertEquals("0.0.0.0/0", new IPwithNetmask("0.0.0.0/0",32,false).toString());
        assertEquals("255.255.255.255/1", new IPwithNetmask("255.255.255.255/1",32,false).toString());
        assertEquals("1.2.3.4/32", new IPwithNetmask("1.2.3.4",32,false).toString());
		assertEquals("1.2.3.4/24", new IPwithNetmask("1.2.3.4/24", 24, true).toString());

        try{
            new IPwithNetmask("",32,false);
            fail();
        }catch(BadIpException e){}
        try{
            new IPwithNetmask("/",32,false);
            fail();
        }catch(BadIpException e){}
        try{
            new IPwithNetmask("23.23.23.263/",32,false);
            fail();
        }catch(BadIpException e){}

        try{
            new IPwithNetmask("23.23.23.23/",32,false);
            fail();
        }catch(BadNetmaskException e){}
        try{
            new IPwithNetmask("23.23.23.23/33",32,false);
            fail();
        }catch(BadNetmaskException e){}
        try{
            new IPwithNetmask("23.23.23.23/23d",32,false);
            fail();
        }catch(BadNetmaskException e){}

        assertEquals("1.1.1.1/0", new IPwithNetmask("1.1.1.1/32",32,true).toString());
        assertEquals("1.1.1.1/8", new IPwithNetmask("1.1.1.1",-1,false).toString());
        assertEquals("1.1.1.1/0", new IPwithNetmask("1.1.1.1/64",32,true).toString());
        assertEquals("1.1.1.1/2", new IPwithNetmask("1.1.1.1/34",-1,true).toString());
    }

    @Test
    public void dopocitaniMasky(){

        assertEquals("1.1.1.1/8", new IPwithNetmask("1.1.1.1").toString());
        assertEquals("100.1.2.3/8", new IPwithNetmask("100.1.2.3").toString());
        assertEquals("172.16.1.1/16", new IPwithNetmask("172.16.1.1").toString());
        assertEquals("192.168.1.1/24", new IPwithNetmask("192.168.1.1").toString());
    }

	@Test
	public void testByteArrayKonstruktor(){

		IpAddress adr;

		byte[] array = new byte[]{1,2,3,4};
		adr = new IpAddress(array);
		assertEquals("1.2.3.4", adr.toString());

		array = new byte[]{(byte)255,(byte)255,(byte)255,(byte)255};
		adr = new IpAddress(array);
		assertEquals("255.255.255.255", adr.toString());

		array = new byte[]{(byte)128,(byte)128,(byte)128,(byte)128};
		adr = new IpAddress(array);
		assertEquals("128.128.128.128", adr.toString());
	}
}