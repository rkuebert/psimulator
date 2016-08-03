package psimulator.logicLayer.Simulator;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class ClientConnectionFailException extends Exception{
    
    private ConnectionFailtureReason connectionFailtureReason;

    public ClientConnectionFailException(ConnectionFailtureReason connectionFailtureReason) {
        this.connectionFailtureReason = connectionFailtureReason;
    }

    public ConnectionFailtureReason getConnectionFailtureReason() {
        return connectionFailtureReason;
    }

}

