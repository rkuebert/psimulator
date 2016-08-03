package psimulator.logicLayer.Simulator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import psimulator.dataLayer.DataLayerFacade;
import psimulator.dataLayer.Enums.ObserverUpdateEventType;
import psimulator.dataLayer.Simulator.ParseSimulatorEventException;
import psimulator.dataLayer.Simulator.SimulatorManagerInterface;
import psimulator.userInterface.UserInterfaceOuterFacade;
import shared.NetworkObject;
import shared.SimulatorEvents.SerializedComponents.SimulatorEvent;
import shared.telnetConfig.TelnetConfig;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class SimulatorClientEventRecieverThread implements Runnable, Observer {

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
    //
    private Socket clientSocket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    public SimulatorClientEventRecieverThread(DataLayerFacade dataLayer, UserInterfaceOuterFacade userInterfaceOuterFacade) {
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

        while (true) {
            try {
                // if connect
                if (doConnect) {
                    doConnect();
                    // if disconnect
                } else if (doDisconnect) {
                    doDisconnect();
                }

                SimulatorEvent simulatorEvent = null;

                // if connected, recieve packet
                if (simulatorManagerInterface.isConnectedToServer()) {
                    // recieve event
                    try {
                        simulatorEvent = getEventFromServer();
                    } catch (ClientConnectionFailException ex) {
                        // connection broke down
                        simulatorManagerInterface.connectionFailed(ex.getConnectionFailtureReason());

                        // set recording false immedeately
                        isRecording = false;
                    }

                    // event wasnt recieved, go next round
                    if (simulatorEvent == null) {
                        if (DEBUG) System.out.println("Nepodarilo se nacist eventu");
                        continue;
                    }else{
                        if (DEBUG) System.out.println("Nactena eventa" + tmpCounter++);
                    }

                    // if in recording mode, try to add packet to event table
                    if (isRecording == true) {
                        if (DEBUG) {
                            System.out.println("Adding packet " + tmpCounter++);
                        }
                        try {
                            simulatorManagerInterface.addSimulatorEvent(simulatorEvent);
                            if (DEBUG) {
                                System.out.println("Packet added " + tmpCounter++);
                            }
                        } catch (ParseSimulatorEventException ex) {
                            // inform simulator manager
                            simulatorManagerInterface.recievedWrongPacket();

                            // set recording false immedeately
                            isRecording = false;
                        }
                    }
                } else {
                    if (DEBUG) {
                        System.out.println("Reciever going to long sleep " + tmpCounter++);
                    }
                    // sleep for a long time
                    Thread.sleep(Long.MAX_VALUE);
                }
            } catch (InterruptedException ex) {
                if (DEBUG) {
                    System.out.println("Reciever interrupted");
                }
            }
        }
    }

    @Override
    public void update(Observable o, Object o1) {
        switch ((ObserverUpdateEventType) o1) {
            case CONNECTION_DO_CONNECT:
                if (DEBUG) {
                    System.out.println("Event reciever: DO_CONNECT");
                }
                this.doConnect = true;
                break;
            case CONNECTION_DO_DISCONNECT:
                if (DEBUG) {
                    System.out.println("Event reciever: DO_DISCONNECT");
                }
                this.doDisconnect = true;
                break;
            case SIMULATOR_RECORDER_ON:
            case SIMULATOR_RECORDER_OFF:
                if (DEBUG) {
                    System.out.println("Event reciever: RECORDER");
                }
                this.isRecording = simulatorManagerInterface.isRecording();
                break;
            default:
                return;
        }
        thread.interrupt();
    }
    
    
    
    private void connectToServer() throws UnknownHostException, IOException, NumberFormatException {
        String ipAddress = dataLayer.getConnectionIpAddress();
        String port = dataLayer.getConnectionPort();

        int portNumber = Integer.parseInt(port); // catch a NumberFormatException

        if (DEBUG) {
            System.out.println("Creating socket ");
        }

        // create new socket
        clientSocket = new Socket(ipAddress, portNumber);
        // set timeout for creating streams
        clientSocket.setSoTimeout(5000);

        if (DEBUG) {
            System.out.println("Creating output stream ");
        }

        // have to create output stream first, if output not created, than input cant be created
        outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        // have to flush
        outputStream.flush();

        if (DEBUG) {
            System.out.println("Creating input stream ");
        }

        // create input stream
        inputStream = new ObjectInputStream(clientSocket.getInputStream());

        if (DEBUG) {
            System.out.println("Setting timeout ");
        }

        // set timeout for normal communication
        clientSocket.setSoTimeout(1000);
    }

    private void disconnectFromServer() {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException ex) {
            // nothing to do
        }

        try {
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (IOException ex) {
            // nothing to do
        }

        try {
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException ex) {
            // nothing to do
        }

        inputStream = null;
        outputStream = null;
        clientSocket = null;
    }

    private boolean recieveTableWithTelnetPorts() {
        int repeatReadTimes = 3;  // aka 5 secons i no object readed
        int times = 0;

        try {
            while (true) {
                TelnetConfig telnetConfig = (TelnetConfig) inputStream.readObject();

                // if read timeouted
                if (telnetConfig == null) {
                    // if final timeout
                    if (times > repeatReadTimes) {
                        return false;
                    } else {        // repeat read
                        times++;
                        continue; // continue while
                    }

                } else { // process object
                    simulatorManagerInterface.setTelnetConfig(telnetConfig);
                    // exit while loop
                    return true;
                }
            }
        } catch (SocketTimeoutException timeout) {
            // its ok.. no need to be handled, just check if object is not null
        } catch (IOException ex) {
            //Logger.getLogger(SimulatorClientEventRecieverThread.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (ClassNotFoundException ex) {
            //Logger.getLogger(SimulatorClientEventRecieverThread.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return false;
    }

    private SimulatorEvent getEventFromServer() throws ClientConnectionFailException {

        // if reset time
        if (simulatorManagerInterface.isTimeReset()) {
            timeOfFirstEvent = System.currentTimeMillis();
        }

        try {
            NetworkObject networkObject = (NetworkObject) inputStream.readObject();

            SimulatorEvent simulatorEvent = (SimulatorEvent) networkObject;

            // generate time
            int time = (int) (System.currentTimeMillis() - timeOfFirstEvent);
            simulatorEvent.setTimeStamp(time);

            return simulatorEvent;
        } catch (SocketTimeoutException timeout) {
            // its ok.. no need to be handled, just check if object is not null
            if (DEBUG) System.out.println("Socket timeout exception during get simulator event");
            return null;
        } catch (IOException ex) {
            if (DEBUG)System.out.println("IOException during get simulator event");
            //Logger.getLogger(SimulatorClientEventRecieverThread.class.getName()).log(Level.SEVERE, null, ex);
            throw new ClientConnectionFailException(ConnectionFailtureReason.SERVER_DISCONNECTED);
        } catch (ClassNotFoundException ex) {
            if (DEBUG)System.out.println("Class cast exception during get simulator event");
            throw new ClientConnectionFailException(ConnectionFailtureReason.SERVER_SENT_WRONG_OBJECT);
        }
    }

    private void doConnect() {
        doConnect = false;

        if (DEBUG) {
            System.out.println("Reciever do connect ");
        }

        try {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                // nothing to do
            }

            // connect to server
            connectToServer();
            if (DEBUG) {
                System.out.println("Connected");
            }

            // if connecting succesfull
            simulatorManagerInterface.connected();

            // recieve table with telnet ports
            boolean successs = recieveTableWithTelnetPorts();

            // if table not recieved
            if (!successs) {
                // disconnect
                disconnectFromServer();
                // inform about connection fail
                simulatorManagerInterface.connectionFailed(ConnectionFailtureReason.TABLE_WITH_TELNET_NOT_RECIEVED);
            }
        } catch (UnknownHostException | NumberFormatException e) {
            if (DEBUG)System.out.println("Unknown host or number format ex");
            disconnectFromServer();
            simulatorManagerInterface.connectingFailed();
        } catch (IOException ex) {
            if (DEBUG)System.out.println("IOEx");
            disconnectFromServer();
            simulatorManagerInterface.connectingFailed();
        }
    }

    private void doDisconnect() {
        doDisconnect = false;
        //
        if (DEBUG) {
            System.out.println("Reciever do disconnect ");
        }

        disconnectFromServer();
        simulatorManagerInterface.disconnected();
    }
}
