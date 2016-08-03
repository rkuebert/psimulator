/*
 * Erstellt am 27.10.2011.
 */

package networkModule;

import utils.WorkerThread;

/**
 *
 * @author neiss
 */
public abstract class Layer {

    protected NetworkModule netMod;

    public Layer(NetworkModule netMod) {
        this.netMod = netMod;

    }

    public NetworkModule getNetMod() {
        return netMod;
    }
}
