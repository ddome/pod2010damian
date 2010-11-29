package ar.edu.itba.pod.legajo47189.communication.Impl;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47189.simulation.Impl.SimulationManagerImpl;

public class BalanceThread extends Thread {

    SimulationManagerImpl manager;
    private final static Logger LOGGER = Logger.getLogger(BalanceThread.class);
    private String except;
    
    public BalanceThread(SimulationManagerImpl manager, String except)
    {
        this.manager = manager;
        this.except = except;
    }
    
    @Override
    public void run()
    {
        LOGGER.info("Comienzo de espera de sincronizacion de coordinador");
        try {
            //TODO: definir el tiempo necesario en algun lado
            Thread.sleep(2000);
        } catch (InterruptedException e) {
                LOGGER.error(e);
        }
        
        if (!NodeInitializer.getCoordinator().equals(NodeInitializer.getNodeId()))
        {
            LOGGER.info("Se designo a otro nodo como coordinador");
        }
        else
        {
            manager.doBalance(except);
        }
    }

}
