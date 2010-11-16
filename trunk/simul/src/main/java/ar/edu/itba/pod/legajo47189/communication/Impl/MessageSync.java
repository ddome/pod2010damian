package ar.edu.itba.pod.legajo47189.communication.Impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47189.architecture.Node;
import ar.edu.itba.pod.legajo47189.tools.Helper;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.Message;

public class MessageSync extends Thread{

    private final static Logger LOGGER = Logger.getLogger(MessageSync.class);
    
    private boolean stopFlag = false;
    
    public void run()
    {
        List<Node> nodes = NodeInitializer.getCluster().getGroup().getNodes();
        String me = NodeInitializer.getNodeId();
        ConnectionManager connection = null;
        Iterable<Message> messages = null;
        
        while(!stopFlag)
        {
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            LOGGER.info("Pidiendo mensajes nuevos a nodos conocidos");
            // Agrego uno mas ya que el nodo actual se remueve
            nodes = RandomSelection(nodes);
            nodes.remove(new Node(me));
            
            if (nodes.size() == 0)
            {
                LOGGER.info("No hay nodos registrados para pedir mensajes nuevos");
            }
            
            for (Node node : nodes)
            {
                LOGGER.info("Pido mensajes al nodo " + node.getNodeId());
                try {
                    connection = NodeInitializer.getConnection().getConnectionManager(node.getNodeId());
                    messages = connection.getGroupCommunication().getListener().getNewMessages(me);       
                    
                    if (!messages.iterator().hasNext())
                    {
                        LOGGER.info("No hay mensajes nuevos del nodo " + node.getNodeId());
                    }
                    
                    for (Message message : messages)
                    {
                        NodeInitializer.getConnection().getGroupCommunication().getListener().onMessageArrive(message);
                    }
                    LOGGER.info("Se terminaron de procesar los mensajes nuevos del nodo " + node.getNodeId());
                } catch (RemoteException e) {
                    NodeInitializer.getCluster().getGroup().remove(node.getNodeId());
                    //TODO: Avisar a la cluster
                    LOGGER.error(e.getMessage());
                }
            }
        }
    }
    
    public void endThread()
    {
        stopFlag = true;
    }
    
    private List<Node> RandomSelection(List<Node> nodes) {
        List<Node> randomNodes = new ArrayList<Node>();
        for (Node node : nodes)
        {
            if (Helper.flipCoin(0.7))
            {
                randomNodes.add(node);
            }
        }
        return randomNodes;
    }
}
