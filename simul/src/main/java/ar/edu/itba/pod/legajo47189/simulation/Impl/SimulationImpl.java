package ar.edu.itba.pod.legajo47189.simulation.Impl;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.TimeUnit;

import ar.edu.itba.pod.legajo47189.communication.Impl.NodeInitializer;
import ar.edu.itba.pod.simul.simulation.Simulation;
import ar.edu.itba.pod.simul.simulation.SimulationEvent;
import ar.edu.itba.pod.simul.simulation.SimulationEventHandler;
import ar.edu.itba.pod.simul.simulation.SimulationManager;
import ar.edu.itba.pod.simul.time.TimeMapper;

public class SimulationImpl implements Simulation, Remote{

    private TimeMapper timeMapper;
    private SimulationManager simulationManager;
    
    public SimulationImpl() throws RemoteException
    {
        UnicastRemoteObject.exportObject(this, 0);
        simulationManager = NodeInitializer.getSimulationManager();
    }
    
    @Override
    public void add(SimulationEventHandler handler) {
        // TODO Auto-generated method stub
    }

    @Override
    public <T> T env(Class<T> param) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void raise(SimulationEvent event) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void remove(SimulationEventHandler handler) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void wait(int amount, TimeUnit unit) throws InterruptedException {
            Thread.sleep(amount);
    }
   
}
