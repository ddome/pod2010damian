package ar.edu.itba.pod.legajo47189.communication.Impl;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47189.architecture.Node;
import ar.edu.itba.pod.legajo47189.tools.Helper;
import ar.edu.itba.pod.simul.communication.ClusterCommunication;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageListener;

public class ClusterCommunicationImpl extends Thread implements ClusterCommunication {

    private final static Logger LOGGER = Logger.getLogger(ClusterCommunicationImpl.class);
    
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
        boolean flag = false;
        boolean ret;
        NodeInitializer.addToHistory(message);
        while(!flag)
        {
            nodes = RandomSelection(nodes);
            // No me lo mando a mi mismo
            nodes.remove(new Node(me));
            // No se lo mando al nodo que inicio el mensaje 
            nodes.remove(new Node(message.getNodeId()));
            if (nodes.size() == 0)
            {
                flag = true;
            }
            else
            {
               // LOGGER.info("Inicio un broadcast a " + nodes.size() + " nodos");
            }
            
            for (Node node : nodes)
            {
                if (!node.getNodeId().equals(message.getNodeId()) 
                        && !message.getNodeId().equals(node.getNodeId()))
                {
                    ret = send(message, node.getNodeId());
                    if (ret) flag = true;
                }
            }
        }
    }

    private List<Node> RandomSelection(List<Node> nodes) {
        
        List<Node> randomNodes = new ArrayList<Node>();
        for (Node node : nodes)
        {
            if (Helper.flipCoin(nodes.size()))
            {
              randomNodes.add(node);
            }
        }
        return randomNodes;
    }

    @Override
    public MessageListener getListener() throws RemoteException {
        return messageListener;
    }

    @Override
    public boolean send(Message message, String nodeId) throws RemoteException {
        boolean ret = false;
       // LOGGER.info("Mando el mensaje " + message.getNodeId() + " al nodo " + nodeId);
        try {
            ConnectionManager manager = 
                NodeInitializer.getConnection().getConnectionManager(nodeId);
            ret = manager.getGroupCommunication().getListener().onMessageArrive(message);
        } catch (RemoteException e) {
            LOGGER.error("El mensaje " + message.getNodeId() + " al nodo " + nodeId + "no pudo ser transmitido.");      
            throw e;
        }
        //LOGGER.info("Mensaje " + message.getNodeId() + " al nodo " + nodeId + " enviado correctamente");
        return ret;
    }
}
