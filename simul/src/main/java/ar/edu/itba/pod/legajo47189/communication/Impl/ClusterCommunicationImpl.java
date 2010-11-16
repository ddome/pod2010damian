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
        // Agrego dos mas, ya que el que origino el mensaje y el nodo actual
        // no se incluyen
        //Integer number = new Integer((int)(Math.floor(nodes.size()/2) + 1) + 2);
        //nodes = RandomSelection(nodes, number);
        //TODO: SELECCIONAR AL AZAR
        nodes.remove(new Node(me));
        nodes.remove(new Node(message.getNodeId()));
        
        if (nodes.size() == 0)
        {
            LOGGER.info("No hay nodos para transmitir broadcast");
        }
        else
        {
            LOGGER.info("Inicio un broadcast a " + nodes.size() + " nodos");
        }
        
        for (Node node : nodes)
        {
            if (!node.getNodeId().equals(message.getNodeId()) )
            {
                send(message, node.getNodeId());
            }
            LOGGER.info("Broadcast finalizado exitosamente");
        }
    }

    private List<Node> RandomSelection(List<Node> nodes, Integer number) {
        
        List<Node> randomNodes = new ArrayList<Node>();
        Integer max =  new Integer(nodes.size());
        
        if (number <= 0)
            return nodes;
        
        int pos = 0;
        while (number > 0 && pos < max)
        {
            randomNodes.add(nodes.get(pos));
            pos++;
            number--;
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
        LOGGER.info("Mando el mensaje " + message.getNodeId() + " al nodo " + nodeId);
        try {
            ConnectionManager manager = 
                NodeInitializer.getConnection().getConnectionManager(nodeId);
            ret = manager.getGroupCommunication().getListener().onMessageArrive(message);
        } catch (RemoteException e) {
            LOGGER.error("El mensaje " + message.getNodeId() + " al nodo " + nodeId + "no pudo ser transmitido. Se borra el nodo de la cluster");
            NodeInitializer.getCluster().getGroup().remove(nodeId);
            //TODO: Avisar a la cluster
            throw e;
        }
        LOGGER.info("Mensaje " + message.getNodeId() + " al nodo " + nodeId + " enviado correctamente");
        return ret;
    }
}
