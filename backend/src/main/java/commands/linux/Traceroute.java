/*
 * Erstellt am 16.3.2012.
 */

package commands.linux;

import applications.LinuxTracerouteApplication;
import commands.AbstractCommandParser;
import commands.ApplicationNotifiable;
import commands.LongTermCommand;
import dataStructures.ipAddresses.BadIpException;
import dataStructures.ipAddresses.IpAddress;

/**
 * TODO dodelat traceroute
 * @author Tomas Pitrinec
 */
public class Traceroute extends LinuxCommand implements LongTermCommand, ApplicationNotifiable{

	protected IpAddress adr;
    protected int navrKod = 0;
    protected int maxTtl = 30; //to je to, co se vypisuje jako hops max
    protected double interval = 0.1; //interval mezi odesilanim v sekudach
    /**
     * Stav vykonavani prikazu:
     * 0 - ceka se na pakety<br />
     * 1 - vratil se paket od cilovyho pocitace - skoncit<br />
     * 2 - byl timeout - vypisovat hvezdicky a skoncit<br />
     * 3 - vratilo se host unreachable nebo net unreachable<br />
     */
    protected int stavKonani = 0;

	private LinuxTracerouteApplication app;

	public Traceroute(AbstractCommandParser parser) {
		super(parser);
	}

	@Override
	public void run() {
		parsujPrikaz();
		if(navrKod ==0){ // jen kdyz je parsovani v poradku
			parser.setRunningCommand(this, false);

			vykonejPrikaz();
		}
	}

	@Override
	public void catchSignal(Signal signal) {
		if(signal==Signal.CTRL_C){
			app.exit();
		}
	}

	@Override
	public void catchUserInput(String line) {
		// nic nedela
	}

	@Override
	public void applicationFinished() {
		parser.deleteRunningCommand();
	}



	private void vykonejPrikaz() {
		app = new LinuxTracerouteApplication(getDevice(), this);
		app.setTarget(adr);
		app.setQueriesPerTTL(3);
		app.setMaxTTL(30);
		app.start();
	}

// privatni metody - parsovani:

    protected void parsujPrikaz(){
        try{
            adr=new IpAddress(dalsiSlovo());
        }catch(BadIpException ex){
            navrKod=1;
            parser.printService(": traceroute: Error in command syntax." +
                    " Supported syntax is: \"traceroute <IP address>\"");

        }
    }


// privatni metody - vykonavani

//    protected void vykonejPrikaz() {
//
//        /*
//         * Neodesilani zkusebniho paketu s icmp_seq -1. Je jen zkusebni, metoda odesliNovejPaket(...)
//         * ho zablokuje a nikam se NEPOSILA ani nepocita.
//         */
//        if (pc.posliIcmpRequest(adr, -1, maxTtl, this)) {
//            //paket pujde poslat - vypsani prvniho radku:
//            printLine("traceroute to "+adr.vypisAdresu()+" ("+adr.vypisAdresu()+
//                    "), "+maxTtl+" hops max, 40 byte packets");
//        } else {
//            //paket nepujde poslat, vypise se hlaseni a program se ukonci
//            //ve skutecnosti se ale neukoncuje
//            printLine("traceroute: Warning: findsaddr: netlink error: Network is unreachable");
//            return; //dalsi pakety se neposilaj, prvni radek se nevypisuje
//        }
//
//        /*
//         * posilani dalsich pingu:
//         */
//        for(int i=0;i<maxTtl;i++) {
//           //nejdriv se kontrolujeminule odeslanej paket:
//            if (stavKonani == 1) { //uz dorazil paket z cile
//                break;
//            }
//            if (prijate < odeslane) { //posledni paket nedorazil
//                stavKonani=2;
////                printLine("paket timeoutoval");
//                dopisZbylyHvezdicky(i-1);
//                break;
//            }
//            if (stavKonani == 3) { //vratilo se host nebo net unreachable
//                break;
//            }
//           //pak se posila novej paket
//            int icmp_seq = (i + 1) % 65536; //zacina to od jednicky a po 65535 se jede znova od nuly
//            int ttl = i + 1; //ttl se od jednicky postupne zvysuje
//            if (pc.posliIcmpRequest(adr, icmp_seq, ttl, this)) {
//                //paket se odeslal
//            } else {
//                //proste to necham timeoutovat
//            }
//            odeslane++;
//            if (i != maxTtl - 1) //cekani po zadany interval - naposled se neceka
//            {
//                AbstraktniPrikaz.cekej((int) (interval * 1000));
//            }
//
//        }
//    }
//
//
//    @Override
//    public void zpracujPaket(Paket p) {
//        double k1 = (Math.random()/5)+0.9; //vraci cisla mezi 0.9 a 1.1
//        double k2 = (Math.random()/5)+0.0; //vraci cisla mezi 0.9 a 1.1
//        prijate++;
//
//        if (p.typ == 0) { //icmp reply - jsem v cili
//            stavKonani=1;
//            printLine(zarovnejZLeva(prijate+"", 2)+"  "+p.zdroj.vypisAdresu()+" ("+p.zdroj.vypisAdresu()
//                    +")  "+zaokrouhli(p.cas)+" ms  "+zaokrouhli(p.cas*k1)+" ms  "+zaokrouhli(p.cas*k2)+" ms ");
//        } else if (p.typ == 3) {
//            stavKonani=3;
//            if (p.kod == 0) {
//                printLine(zarovnejZLeva(prijate+"", 2)+"  "+p.zdroj.vypisAdresu()+" ("+p.zdroj.vypisAdresu()
//                    +")  "+zaokrouhli(p.cas)+" ms !N  "+zaokrouhli(p.cas*k1)+" ms !N  "
//                    +zaokrouhli(p.cas*k2)+" ms !N");
//            } else if (p.kod == 1) {
//                printLine(zarovnejZLeva(prijate+"", 2)+"  "+p.zdroj.vypisAdresu()+" ("+p.zdroj.vypisAdresu()
//                    +")  "+zaokrouhli(p.cas)+" ms !H  "+zaokrouhli(p.cas*k1)+" ms !H  "
//                    +zaokrouhli(p.cas*k2)+" ms !H");
//            }
//        } else if (p.typ == 11) { //timeout - musim pokracovat
//            printLine(zarovnejZLeva(prijate+"", 2)+"  "+p.zdroj.vypisAdresu()+" ("+p.zdroj.vypisAdresu()
//                    +")  "+zaokrouhli(p.cas)+" ms  "+zaokrouhli(p.cas*k1)+" ms  "+zaokrouhli(p.cas*k2)+" ms ");
//        }
//    }
//
//    protected void dopisZbylyHvezdicky(int a) {
//        for (int i = a; i < maxTtl; i++) {
//            printLine(zarovnejZLeva((i + 1) + "", 2) + "  * * *");
//        }
//    }







}
