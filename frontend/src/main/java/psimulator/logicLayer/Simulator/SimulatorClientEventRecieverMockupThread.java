package psimulator.logicLayer.Simulator;

import java.util.*;
import psimulator.dataLayer.DataLayerFacade;
import psimulator.dataLayer.Enums.ObserverUpdateEventType;
import psimulator.dataLayer.Simulator.ParseSimulatorEventException;
import psimulator.dataLayer.Simulator.SimulatorManagerInterface;
import psimulator.userInterface.UserInterfaceOuterFacade;
import shared.Components.CableModel;
import shared.Components.EthInterfaceModel;
import shared.Components.HwComponentModel;
import shared.SimulatorEvents.SerializedComponents.EventType;
import shared.SimulatorEvents.SerializedComponents.PacketType;
import shared.SimulatorEvents.SerializedComponents.SimulatorEvent;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class SimulatorClientEventRecieverMockupThread implements Runnable, Observer {

    private static boolean DEBUG = false;
    //
    private Thread thread;
    //
    private SimulatorManagerInterface simulatorManagerInterface;
    private Random tmpRandom = new Random();
    private DataLayerFacade dataLayer;
    //
    private long timeOfFirstEvent;
    //
    private volatile boolean isRecording;
    private volatile boolean doConnect;
    private volatile boolean doDisconnect;

    public SimulatorClientEventRecieverMockupThread(DataLayerFacade dataLayer, UserInterfaceOuterFacade userInterfaceOuterFacade) {
        this.dataLayer = dataLayer;
        this.simulatorManagerInterface = dataLayer.getSimulatorManager();
        this.isRecording = simulatorManagerInterface.isRecording();
    }
    
    public void startThread(Thread t) {
        this.thread = t;
        t.start();
    }
    
    @Override
    public void run() {
        int tmpCounter = 0;
        
        while(true){
            try{
                if(doConnect){
                    doConnect = false;
                    
                    if(DEBUG)System.out.println("Reciever do connect " + tmpCounter++);
                    
                    int i = tmpRandom.nextInt(10);
                    
                    if(i==3){
                        Thread.sleep(3000);
                        simulatorManagerInterface.connectingFailed();
                    }else{
                        Thread.sleep(2000);
                        simulatorManagerInterface.connected();
                    }
                }else if (doDisconnect){
                    doDisconnect = false;
                    //
                    if(DEBUG)System.out.println("Reciever do disconnect " + tmpCounter++);

                    simulatorManagerInterface.disconnected();
                }

                if(isRecording){
                    if(DEBUG)System.out.println("Reciever recording " + tmpCounter++);
                    
                    SimulatorEvent simulatorEvent = generateSimulatorEvent(true, 10000);
                    
                    if(!thread.isInterrupted() && isRecording == true && simulatorEvent != null){
                        try {
                            simulatorManagerInterface.addSimulatorEvent(simulatorEvent);
                        } catch (ParseSimulatorEventException ex) {
                            // inform simulator manager
                            simulatorManagerInterface.recievedWrongPacket();
                            
                            // set recording false immedeately
                            isRecording = false;
                        }
                    }
                    
                    //int time = 1;
                    //int time = tmpRandom.nextInt(10) + 1; // 1000 + 100
                    int time = tmpRandom.nextInt(100) + 100;

                    Thread.sleep(time);
                    //Thread.sleep(1);

                } else{
                    if(DEBUG)System.out.println("Reciever going to long sleep " + tmpCounter++);
                    // sleep for a long time
                    Thread.sleep(Long.MAX_VALUE);
                }
            } catch (InterruptedException ex) {
                if(DEBUG)System.out.println("Reciever interrupted");
            }
            
        }
        

    }

    @Override
    public void update(Observable o, Object o1) {
        switch ((ObserverUpdateEventType) o1) {
            case CONNECTION_DO_CONNECT:
                if(DEBUG)System.out.println("Event reciever: DO_CONNECT");
                this.doConnect = true;
                break;
            case CONNECTION_DO_DISCONNECT:
                if(DEBUG)System.out.println("Event reciever: DO_DISCONNECT");
                this.doDisconnect = true;
                break;
            case SIMULATOR_RECORDER_ON:
            case SIMULATOR_RECORDER_OFF:
                if(DEBUG)System.out.println("Event reciever: RECORDER");
                this.isRecording = simulatorManagerInterface.isRecording();
                break;
            default:
                return;
        }

        //System.out.println("Event reciever: recording=" + isRecording);
        
        thread.interrupt();
    }
    
    
    
    /**
     * Generates simulator event from real graph. It tries to create event with random 
     * components connected with cable.
     * @return 
     */
    private SimulatorEvent generateSimulatorEvent(boolean generateWrongOnes, int numberInProbability) {
        List<HwComponentModel> list = new ArrayList<>(dataLayer.getNetworkFacade().getHwComponents());
        int componentCount = dataLayer.getNetworkFacade().getHwComponentsCount();
        int cablesCount = dataLayer.getNetworkFacade().getCablesCount();
        
        // if no cable or less than two components, than no event can be generated
        if(componentCount <2 || cablesCount < 1){
            return null;
        }
        
        
        HwComponentModel c1 = null;
        HwComponentModel c2 = null;

        EthInterfaceModel eth1 = null;
        EthInterfaceModel eth2 = null;
        
        CableModel cable = null;

        int i1;
        int i2;

        // find connected components
        int counter = 0;
        while (counter < 100) {
            i1 = tmpRandom.nextInt(componentCount);
            i2 = tmpRandom.nextInt(componentCount);

            if (i1 == i2) {
                continue;
            }

            c1 = list.get(i1);
            c2 = list.get(i2);

            
            List<EthInterfaceModel> eth1list = new ArrayList<>(c1.getEthInterfaces());
            List<EthInterfaceModel> eth2list = new ArrayList<>(c2.getEthInterfaces());
            
            for (EthInterfaceModel tmp1 : eth1list) {
                if (!tmp1.hasCable()) {
                    continue;
                }
                for (EthInterfaceModel tmp2 : eth2list) {
                    if (!tmp2.hasCable()) {
                        continue;
                    }
                    if (tmp1.getCable().getId().intValue() == tmp2.getCable().getId().intValue()) {
                        cable = tmp1.getCable();
                        eth1 = tmp1;
                        eth2 = tmp2;
                        break;
                    }
                }
                if (cable != null) {
                    break;
                }
            }
            if (cable != null) {
                break;
            }
        }

        // generation was not succesfull
        if(cable == null || c1 == null || c2 == null || eth1 == null || eth2 == null){
            return null;
        }

        // if reset time
        if(simulatorManagerInterface.isTimeReset()){
            timeOfFirstEvent = System.currentTimeMillis();
        }
        
        // generate packet type
        int index = tmpRandom.nextInt(PacketType.values().length);
        PacketType packetType = PacketType.values()[index];

        // generate time
        int time = (int) (System.currentTimeMillis() - timeOfFirstEvent);

        String detailsText = "A very long text to be displayed in onle line or even in two lines. "
                + "This line should now skip to next line \n and this should be on the next line. "
                + "Now two empty lines follows. And this is the end of our text area.";

        int c1Id = c1.getId();
        int c2Id = c2.getId();
        int cableId = cable.getId().intValue();
        
        if(generateWrongOnes){
            int tmp = tmpRandom.nextInt(numberInProbability);
            
            if(tmp == 0){
                c1Id = tmpRandom.nextInt(10000);
                c2Id = tmpRandom.nextInt(10000);
                cableId = tmpRandom.nextInt(10000);
            }
        }
        
        EventType eventType;
        
        double tmp = Math.random();
        if (tmp < 0.5) {
            eventType = EventType.SUCCESSFULLY_TRANSMITTED;
        }else if (tmp < 0.9){
            eventType = EventType.LOST_IN_DEVICE;
            c2Id = -1;
            cableId = -1;
        }else{
            eventType = EventType.LOST_IN_CABLE;
        }
        
        SimulatorEvent simulatorEvent = new SimulatorEvent(time, c1Id, c2Id, 
                cableId, packetType, detailsText, eventType);
        
        return simulatorEvent;
    }
}
