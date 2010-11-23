package ar.edu.itba.pod.legajo47189.communication.Impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47189.architecture.Group;
import ar.edu.itba.pod.legajo47189.architecture.Node;
import ar.edu.itba.pod.legajo47189.tools.Helper;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.Message;

public class MessageSync extends Thread{

    private final static Logger LOGGER = Logger.getLogger(MessageSync.class);
    
    private boolean stopFlag = false;
    
    public void run()
    {        
        String me = NodeInitializer.getNodeId();
        ConnectionManager connection = null;
        Iterable<Message> messages = null;
        List<Node> nodes = null;
        
        while(!stopFlag)
        {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            
            nodes = waitForGroup();
           
            // Agrego uno mas ya que el nodo actual se remueve
            nodes = RandomSelection(nodes);
            nodes.remove(new Node(me));
            
            for (Node node : nodes)
            {
                try {
                    connection = NodeInitializer.getConnection().getConnectionManager(node.getNodeId());
                    messages = connection.getGroupCommunication().getListener().getNewMessages(me);       
                    
                    for (Message message : messages)
                    {
                        NodeInitializer.getConnection().getGroupCommunication().getListener().onMessageArrive(message);
                    }
                } catch (RemoteException e) {
                    NodeInitializer.getCluster().getGroup().remove(node.getNodeId());
                    //TODO: Avisar a la cluster
                    LOGGER.error(e.getMessage());
                }
            }
        }
    }
    
    public List<Node> waitForGroup()
    {
        Group group;
        do
        {
            group = NodeInitializer.getCluster().getGroup();
            try {
            	if (group == null) Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (group == null);
        return group.getNodes();
    }
    
    public void endThread()
    {
        stopFlag = true;
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
}
