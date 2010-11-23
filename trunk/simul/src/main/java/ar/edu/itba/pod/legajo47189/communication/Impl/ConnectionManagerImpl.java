package ar.edu.itba.pod.legajo47189.communication.Impl;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import ar.edu.itba.pod.simul.communication.ClusterAdministration;
import ar.edu.itba.pod.simul.communication.ClusterCommunication;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.SimulationCommunication;
import ar.edu.itba.pod.simul.communication.ThreePhaseCommit;
import ar.edu.itba.pod.simul.communication.Transactionable;

public class ConnectionManagerImpl implements ConnectionManager {
    
    public static int ConnectionPort = 1099;
    public static final String ServiceName = "ConnectionService";
    private ClusterAdministration clusterAdministration = 
        new ClusterAdministrationImpl();
    private ClusterCommunication clusterCommunication = 
        new ClusterCommunicationImpl();
    private TransactionableImpl transaction = 
        new TransactionableImpl();
    private SimulationCommunicationImpl simulation = 
        new SimulationCommunicationImpl();
    
    /**
     * @throws RemoteException
     */
     public ConnectionManagerImpl() throws RemoteException {
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
        return clusterAdministration;
    }

    @Override
    public int getClusterPort() throws RemoteException {
        return ConnectionPort;
    }

    @Override
    public ConnectionManager getConnectionManager(String nodeId)
            throws RemoteException {
        
    	if (nodeId.equals(NodeInitializer.getNodeId()))
    	{
    		return this;
    	}
    	
        //TODO: CAMBIAR ESTA VILLEREADA
        String host = nodeId.split(":")[0];
        Integer port = Integer.parseInt(nodeId.split(":")[1]);
        
        final Registry registry = LocateRegistry.getRegistry(host, port);
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
        return clusterCommunication;
    }

    @Override
    public Transactionable getNodeCommunication() throws RemoteException {
        return transaction;
    }

    @Override
    public SimulationCommunication getSimulationCommunication()
            throws RemoteException {
        return simulation;
    }

    @Override
    public ThreePhaseCommit getThreePhaseCommit() throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }    
}
