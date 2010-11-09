package ar.edu.itba.pod.legajo47189;

import java.rmi.RemoteException;

import ar.edu.itba.pod.legajo47189.architecture.Cluster;
import ar.edu.itba.pod.legajo47189.architecture.Group;
import ar.edu.itba.pod.legajo47189.architecture.Node;
import ar.edu.itba.pod.simul.communication.ClusterAdministration;
import ar.edu.itba.pod.simul.communication.ConnectionManager;

public class ClusterAdministrationImpl implements ClusterAdministration {

    // Singleton
    private static ClusterAdministrationImpl current;
    static
    {
        current = new ClusterAdministrationImpl();
    }
    public static ClusterAdministrationImpl getCurrent()
    {
        return current;
    }
        
    @Override
    public Iterable<String> addNewNode(String newNode) throws RemoteException {
        
        // Chequeo si esta conectado a un grupo
        if (!isConnectedToGroup())
        {
            throw new RemoteException("El nodo no esta conectado a un grupo");
        } 
        // Connection Manager remoto
        ConnectionManager manager = 
           NodeInitializer.getConnection().getConnectionManager(newNode);
        // Cluster local
        Cluster cluster = NodeInitializer.getCluster();
        // Me fijo que los grupos coincidan
        if (!manager.getClusterAdmimnistration().getGroupId()
                .equals(cluster.getGroup().getGroupId()))
        {
            throw new RemoteException("Los grupos de los nodos no coinciden");
        }
        // Agrego el nodo al grupo local
        cluster.getGroup().add( new Node(newNode, manager));
        // Falta avisar a los otros guachines
        
        return Node.GetIdList(cluster.getGroup().getNodes());
    }

    @Override
    public void connectToGroup(String initialNode) throws RemoteException {
        
        Node node = NodeInitializer.getNode();
        ConnectionManager remoteConnection = 
            NodeInitializer.getConnection().getConnectionManager(initialNode);
        remoteConnection.getClusterAdmimnistration().addNewNode(node.getNodeId());
        //TODO: recibo los nodos y los guardo
    }

    @Override
    public void createGroup() throws RemoteException {
        Cluster cluster = NodeInitializer.getCluster();
        if (cluster.getGroup() != null)
        {
            throw new RemoteException("Ya existe un grupo");
        }
        else
        {
            Node node = NodeInitializer.getNode();
            cluster.setGroup(new Group(node.getNodeId()));
            cluster.getGroup().add(NodeInitializer.getNode());
        }
    }

    @Override
    public void disconnectFromGroup(String nodeId) throws RemoteException {
        Cluster cluster = NodeInitializer.getCluster();
        cluster.setGroup(null);
        //TODO: AVISAR AL grupo
    }

    @Override
    public String getGroupId() throws RemoteException {
        Cluster cluster = NodeInitializer.getCluster();
        if (!isConnectedToGroup())
        {
            throw new RemoteException("El nodo no esta conectado a un grupo");
        }
        return cluster.getGroup().getGroupId();
    }

    @Override
    public boolean isConnectedToGroup() throws RemoteException {
        Cluster cluster = NodeInitializer.getCluster();
        return cluster.getGroup() != null;
    }

}
