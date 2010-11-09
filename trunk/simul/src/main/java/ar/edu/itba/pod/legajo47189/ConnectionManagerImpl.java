package ar.edu.itba.pod.legajo47189;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import ar.edu.itba.pod.legajo47189.architecture.Node;
import ar.edu.itba.pod.simul.communication.ClusterAdministration;
import ar.edu.itba.pod.simul.communication.ClusterCommunication;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.SimulationCommunication;
import ar.edu.itba.pod.simul.communication.ThreePhaseCommit;
import ar.edu.itba.pod.simul.communication.Transactionable;

public class ConnectionManagerImpl implements ConnectionManager {
    
    public static final int ConnectionPort = 1099;
    public static final String ServiceName = "ConnectionService";
    
    /**
     * @throws RemoteException
     */
     protected ConnectionManagerImpl() throws RemoteException {
         UnicastRemoteObject.exportObject(this, 0);
         bindService();
     }
     
     private void bindService() throws RemoteException
     {
         Registry registry = LocateRegistry.createRegistry(getClusterPort());
         registry.rebind(ServiceName,  this);
     }
    
    @Override
    public ClusterAdministration getClusterAdmimnistration()
            throws RemoteException {
        return ClusterAdministrationImpl.getCurrent();
    }

    @Override
    public int getClusterPort() throws RemoteException {
        return ConnectionPort;
    }

    @Override
    public ConnectionManager getConnectionManager(String nodeId)
            throws RemoteException {
        
        final Registry registry = LocateRegistry.getRegistry(nodeId);
        ConnectionManager stub = null;
        try {
            stub = (ConnectionManager) registry.lookup(ServiceName);
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
        return stub;
    }

    @Override
    public ClusterCommunication getGroupCommunication() throws RemoteException {
        return ClusterCommunicationImpl.getCurrent();
    }

    @Override
    public Transactionable getNodeCommunication() throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SimulationCommunication getSimulationCommunication()
            throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ThreePhaseCommit getThreePhaseCommit() throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    /* Private */
    
}
