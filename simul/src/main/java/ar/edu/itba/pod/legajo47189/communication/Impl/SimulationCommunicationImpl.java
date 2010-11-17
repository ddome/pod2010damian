package ar.edu.itba.pod.legajo47189.communication.Impl;

import java.rmi.RemoteException;
import java.util.Collection;

import ar.edu.itba.pod.simul.communication.AgentDescriptor;
import ar.edu.itba.pod.simul.communication.NodeAgentLoad;
import ar.edu.itba.pod.simul.communication.SimulationCommunication;

public class SimulationCommunicationImpl implements SimulationCommunication {

    @Override
    public NodeAgentLoad getMinimumNodeKnownLoad() throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<AgentDescriptor> migrateAgents(int numberOfAgents)
            throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void nodeLoadModified(NodeAgentLoad newLoad) throws RemoteException {
        // TODO Auto-generated method stub

    }

    @Override
    public void startAgent(AgentDescriptor descriptor) throws RemoteException {
        // TODO Auto-generated method stub

    }

}
