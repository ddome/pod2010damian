package ar.edu.itba.pod.simul.communication.payload;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47189.communication.Impl.ClusterCommunicationImpl;
import ar.edu.itba.pod.legajo47189.communication.Impl.NodeInitializer;
import ar.edu.itba.pod.legajo47189.simulation.Impl.SimulationManagerImpl;

public class BalanceThread extends Thread {

    SimulationManagerImpl manager;
    private final static Logger LOGGER = Logger.getLogger(BalanceThread.class);
    
    public BalanceThread(SimulationManagerImpl manager)
    {
        this.manager = manager;
    }
    
    @Override
    public void run()
    {
        LOGGER.info("Comienzo de espera de sincronizacion de coordinador");
        try {
            //TODO: definir el tiempo necesario en algun lado
            Thread.sleep(10000);
        } catch (InterruptedException e) {
                LOGGER.error(e);
        }
        
        if (!NodeInitializer.getCoordinator().equals(NodeInitializer.getNodeId()))
        {
            LOGGER.info("Se designo a otro nodo como coordinador");
        }
        else
        {
            manager.doBalance();
        }
    }

}
