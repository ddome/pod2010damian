package ar.edu.itba.pod.legajo47189.communication.Impl;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47189.architecture.Cluster;
import ar.edu.itba.pod.legajo47189.architecture.Group;
import ar.edu.itba.pod.legajo47189.architecture.Node;
import ar.edu.itba.pod.legajo47189.tools.Helper;
import ar.edu.itba.pod.simul.communication.ClusterAdministration;
import ar.edu.itba.pod.simul.communication.ConnectionManager;

public class ClusterAdministrationImpl implements ClusterAdministration {

    private final static Logger LOGGER = Logger.getLogger(ClusterAdministrationImpl.class);
    
    /**
     * @throws RemoteException
     */
     public ClusterAdministrationImpl() throws RemoteException {
         UnicastRemoteObject.exportObject(this, 0);
     }
    
    private Cluster cluster = NodeInitializer.getCluster();
    
    @Override
    public Iterable<String> addNewNode(String newNode) throws RemoteException {
        
        System.out.println("Un guachin me pidio conectarse");
        
        // Chequeo si esta conectado a un grupo
        if (!isConnectedToGroup())
        {
            throw new RemoteException("El nodo no esta conectado a un grupo");
        } 
        // Connection Manager remoto
        ConnectionManager manager = 
           NodeInitializer.getConnection().getConnectionManager(newNode);
        // Cluster local
        // Agrego el nodo al grupo local
        Iterable<String> ret = Node.GetIdList(cluster.getGroup().getNodes());
        cluster.getGroup().add( new Node(newNode, manager));
        return ret;
    }

    @Override
    public void connectToGroup(String initialNode) throws RemoteException {
        ConnectionManager remoteConnection = 
            NodeInitializer.getConnection().getConnectionManager(initialNode);
        Iterable<String> nodeIds = remoteConnection.getClusterAdmimnistration().addNewNode(NodeInitializer.getNodeId());
        String groupId = remoteConnection.getClusterAdmimnistration().getGroupId();
        cluster.setGroup(new Group(groupId));
        cluster.getGroup().add(new Node(NodeInitializer.getNodeId()));
        cluster.getGroup().add(randomSelection(nodeIds));
    }

    @Override
    public void createGroup() throws RemoteException {
        if (cluster.getGroup() != null)
        {
            throw new RemoteException("Ya existe un grupo");
        }
        else
        {
            cluster.setGroup(new Group(NodeInitializer.getNodeId()));
            cluster.getGroup().add(new Node(NodeInitializer.getNodeId(), 
                    NodeInitializer.getConnection()));
        }
    }

    @Override
    public void disconnectFromGroup(String nodeId) throws RemoteException {
        cluster.setGroup(null);
        //TODO: AVISAR AL grupo
    }

    @Override
    public String getGroupId() throws RemoteException {
        if (!isConnectedToGroup())
        {
            throw new RemoteException("El nodo no esta conectado a un grupo");
        }
        return cluster.getGroup().getGroupId();
    }

    @Override
    public boolean isConnectedToGroup() throws RemoteException {
        return cluster.getGroup() != null;
    }
    
    
    private List<Node> randomSelection(Iterable<String> nodeIds) {
        
        List<Node> randomNodes = new ArrayList<Node>();
        List<String> nodes = getNodeList(nodeIds);
//        List<Integer> sequence = 
  //          Helper.generateSequence(new Integer((int)(Math.floor(nodes.size()/2) + 1)), 
    //                new Integer(nodes.size()));
        //TODO: DEVOLVER UNA SUBLISTA AL AZAR
        for (String id : nodeIds)
        {
            LOGGER.debug("Agrego el nodo " + id + " a mi cluster");
            randomNodes.add(new Node(id));
        }
        return randomNodes;
    }
    
    private List<String> getNodeList(Iterable<String> nodeIds)
    {
        List<String> ret = new ArrayList<String>();
        for (String id : nodeIds)
        {
            ret.add(id);
        }
        return ret;
    }
}
