package ar.edu.itba.pod.legajo47189.communication.Impl;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47189.architecture.Cluster;
import ar.edu.itba.pod.legajo47189.architecture.Group;
import ar.edu.itba.pod.legajo47189.architecture.Node;
import ar.edu.itba.pod.legajo47189.payload.Impl.DisconnectPayloadImpl;
import ar.edu.itba.pod.legajo47189.tools.Helper;
import ar.edu.itba.pod.simul.communication.ClusterAdministration;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageType;

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
        
        LOGGER.info("Se recibio un pedido de conexion del nodo " + newNode);
        
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
        cluster.getGroup().add(new Node(newNode, manager));
        
        return ret;
    }

    @Override
    public void connectToGroup(String initialNode) throws RemoteException {
        
        LOGGER.info("Me conecto al grupo a traves de " + initialNode);
        ConnectionManager remoteConnection = 
            NodeInitializer.getConnection().getConnectionManager(initialNode);
        Iterable<String> nodeIds = remoteConnection.getClusterAdmimnistration().addNewNode(NodeInitializer.getNodeId());
        String groupId = remoteConnection.getClusterAdmimnistration().getGroupId();
        cluster.setGroup(new Group(groupId));
        cluster.getGroup().add(new Node(NodeInitializer.getNodeId()));
        cluster.getGroup().add(getNodeList(nodeIds));
        
        // Me seteo como nodo coordinador
        NodeInitializer.getSimulationManager().setCoordinador(null);
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
        
        Message message = new Message(NodeInitializer.getNodeId(), 
                Helper.GetNow(), 
                MessageType.DISCONNECT, 
                new DisconnectPayloadImpl(nodeId));
        
        // Me seteo como nodo coordinador
        // Reorganizo todo sin el nodo a desconectar
        LOGGER.info("Reorganizo los agentes del nodo que se desconecto");
        NodeInitializer.getSimulationManager().setCoordinador(nodeId);
        
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e1) {
            LOGGER.error(e1);
        }
        
        if (!NodeInitializer.getNodeId().equals(nodeId))
        {
            try
            {
            NodeInitializer.getConnection().getGroupCommunication()
                .send(message, nodeId);
            }catch (RemoteException e) 
            {
                LOGGER.info("El nodo esta desconectado");
            }
        }
        
        NodeInitializer.getConnection().getGroupCommunication()
            .broadcast(message);
        
        // Me mando el mensaje a mi mismo
        NodeInitializer.getConnection()
            .getGroupCommunication()
            .getListener()
            .onMessageArrive(message);
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

    private List<Node> getNodeList(Iterable<String> nodeIds)
    {
        List<Node> ret = new ArrayList<Node>();
        for (String id : nodeIds)
        {
            ret.add(new Node(id));
        }
        return ret;
    }
}
