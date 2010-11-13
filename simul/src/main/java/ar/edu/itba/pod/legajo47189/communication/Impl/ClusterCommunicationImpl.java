package ar.edu.itba.pod.legajo47189.communication.Impl;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import ar.edu.itba.pod.legajo47189.architecture.Node;
import ar.edu.itba.pod.simul.communication.ClusterCommunication;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageListener;

public class ClusterCommunicationImpl implements ClusterCommunication {

    private final MessageListener messageListener;
    
    /**
     * @throws RemoteException
     */
     public ClusterCommunicationImpl() throws RemoteException {
         UnicastRemoteObject.exportObject(this, 0);
         messageListener = new MessageListenerImpl();
     }
        
    @Override
    public void broadcast(Message message) throws RemoteException {
        List<Node> nodes = NodeInitializer.getCluster().getGroup().getNodes();
        String me = NodeInitializer.getNodeId();
        for (Node node : nodes)
        {
            if (!node.getNodeId().equals(me) && !node.getNodeId().equals(message.getNodeId()) )
            {
            	System.out.println( (!node.getNodeId().equals(me) && !node.getNodeId().equals(message.getNodeId())) +  "Retransmito el mensaje " + message.getNodeId() + " al nodo " + node.getNodeId());
                send(message, node.getNodeId());
            }
        }
    }

    @Override
    public MessageListener getListener() throws RemoteException {
        return messageListener;
    }

    @Override
    public boolean send(Message message, String nodeId) throws RemoteException {
        boolean ret = false;
        try {
            ConnectionManager manager = 
                NodeInitializer.getConnection().getConnectionManager(nodeId);
        if (manager == null)
        {
            NodeInitializer.getCluster().getGroup().remove(nodeId);
            return false;
        }
            ret = manager.getGroupCommunication().getListener().onMessageArrive(message);
        } catch (RemoteException e) {
            NodeInitializer.getCluster().getGroup().remove(nodeId);
            throw e;
        }
        return ret;
    }
}
