package ar.edu.itba.pod.legajo47189.communication.Impl;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47189.payload.Impl.NodeAgentsLoadPayload;
import ar.edu.itba.pod.legajo47189.simulation.Impl.SimulationManagerImpl;
import ar.edu.itba.pod.legajo47189.tools.Helper;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageListener;
import ar.edu.itba.pod.simul.communication.MessageType;
import ar.edu.itba.pod.simul.communication.NodeAgentLoad;
import ar.edu.itba.pod.simul.communication.payload.DisconnectPayload;
import ar.edu.itba.pod.simul.communication.payload.Payload;
import ar.edu.itba.pod.simul.communication.payload.ResourceTransferMessagePayload;
import ar.edu.itba.pod.simul.market.Resource;

public class MessageListenerImpl extends Thread implements MessageListener {
    
    private final static Logger LOGGER = Logger.getLogger(MessageListenerImpl.class);
    
    private BlockingQueue<Message> messagesQueue;
    private List<Message> history;
    private List<Message> messages;
    private Map<String, Long> requests;
    private boolean stopFlag = false;
    
    public MessageListenerImpl() throws RemoteException
    {
        UnicastRemoteObject.exportObject(this, 0);
        messagesQueue = new LinkedBlockingQueue<Message>();
        history = NodeInitializer.history;
        messages = new ArrayList<Message>();
        requests = new ConcurrentHashMap<String, Long>();
        new Thread(this).start();
    }
    
    @Override
    public void run()
    {
        LOGGER.info("Iniciando el proceso de escucha de mensajes");
        Message message;
        int milis = 0;
        while(!stopFlag)
        {
            try {
                message = messagesQueue.poll();
                if (message == null)
                {
                    this.currentThread().sleep(100);
                    milis += 100;
                    if (milis == 10000)
                    {
                        history.clear();
                        messages.clear();
                        requests.clear();
                        milis = 0;
                    }
                }
                else
                {
                    messageProcess(message);
                }                   
            } catch (InterruptedException e) {
                LOGGER.info(e.getMessage());
            }
        }
        LOGGER.info("Finalizado el proceso de escucha de mensajes");
    }
    
    @Override
    public Iterable<Message> getNewMessages(String remoteNodeId)
            throws RemoteException {
        
        Long lastSync = requests.get(remoteNodeId);
        requests.put(remoteNodeId, Helper.GetNow());
        //List<Message> ret = getMessagesSince(lastSync);
        //return ret;
        return history;
    }
    
    private List<Message> getMessagesSince(Long lastSync)
    {
        List<Message> messages = new ArrayList<Message>();
        
        int index = history.size() - 1;
        if (index < 0)
        	return messages;
        
        Message current = history.get(index);
        if (current.getTimeStamp() > lastSync)
        {
        	history.add(current);
        }
        index--;
        
        while(current.getTimeStamp() > lastSync && index >= 0)
        {
        	current = history.get(index);
        	messages.add(current); 
        	index--;
        } 
        return messages;
    }
    

    @Override
    public boolean onMessageArrive(Message message) throws RemoteException {
        boolean messageExists = messages.contains(message);
        if (!messageExists && !message.getNodeId().equals(NodeInitializer.getNodeId()))
        {
            messages.add(message);
            try {
                messagesQueue.put(message);
            } catch (InterruptedException e) {
                throw new RemoteException(e.getMessage());
            }
            if (isBroadcast(message))
            {
                LOGGER.info("Se retransmitir‡ el mensaje " + message.getNodeId());
                NodeInitializer.getConnection().getGroupCommunication().broadcast(message);
                LOGGER.info("Se retransmiti— exitosamente el mensaje " + message.getNodeId());
            }
            
        }
        return messageExists;
    }
    
    private boolean isBroadcast(Message message)
    {
        MessageType type = message.getType();
        
        if (message.getNodeId().equals(NodeInitializer.getNodeId()))
        {
            return false;
        }
        else if (type == MessageType.DISCONNECT)
        {
            return true;
        }
        else if (type == MessageType.NEW_MESSAGE_REQUEST)
        {
            return true;
        }
        else if (type == MessageType.NODE_AGENTS_LOAD_REQUEST)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public void endThread()
    {
        stopFlag = true;
    }
    
    private void messageProcess(Message message) throws InterruptedException
    {
        LOGGER.info("Procesando el mensaje " + message.getNodeId() + " de tipo " + message.getType().toString());
        MessageType type = message.getType();
        if (type == MessageType.DISCONNECT)
        {
            onDisconnect(message);
        }
        if (type == MessageType.NODE_AGENTS_LOAD_REQUEST)
        {
            onNodeAgentsLoadRequest(message);
        }
        if (type == MessageType.NODE_AGENTS_LOAD)
        {
            onNodeAgentsLoad(message);
        }
    }
    
    private void onDisconnect(Message message)
    {
        Payload payload = message.getPayload();
        String disconnectedNode = ((DisconnectPayload)payload).getDisconnectedNodeId();
        if (disconnectedNode.equals(NodeInitializer.getNodeId()))
        {
            LOGGER.info("Se desconectara el nodo actual del grupo");
            NodeInitializer.getCluster().setGroup(null);
        }
        else
        {
            LOGGER.info("Se desconectar‡ el nodo " + disconnectedNode + " de mi cluster");
            NodeInitializer.getCluster().getGroup().remove(disconnectedNode);
            LOGGER.info("Se desconecto el nodo " + disconnectedNode + " de mi cluster");
        }
    }
    
    private void onNodeAgentsLoadRequest(Message message)
    {
        if (!message.getNodeId().equals(NodeInitializer.getNodeId()))
        {
            NodeInitializer.setCoordinator(message.getNodeId());
            int agents = NodeInitializer.getSimulationManager().getAgents().size();
            Payload payload = new NodeAgentsLoadPayload(agents);
            Message newMessage = new Message(NodeInitializer.getNodeId(), Helper.GetNow(), MessageType.NODE_AGENTS_LOAD, payload);
            try {
                NodeInitializer.getConnection().getGroupCommunication().send(newMessage, NodeInitializer.getCoordinator());
            } catch (RemoteException e) {
                LOGGER.error(e);
            }
        }
    }
    
    private void onNodeAgentsLoad(Message message)
    {
        SimulationManagerImpl manager = 
            (SimulationManagerImpl)NodeInitializer.getSimulationManager();
        int size = ((NodeAgentsLoadPayload)message.getPayload()).getLoad();
        NodeAgentLoad load = new NodeAgentLoad(message.getNodeId(), size);
        
        // Si no soy el coordinador, tengo que avisar al coordinador que cambie la carga
        if (!NodeInitializer.getCoordinator().equals(NodeInitializer.getNodeId()))
        {
            ConnectionManager connection = null;
            try
            {
                connection = NodeInitializer.getConnection().getConnectionManager(NodeInitializer.getCoordinator());
                connection.getSimulationCommunication().nodeLoadModified(load);
            } catch(Exception e)
            {
                LOGGER.error("No se encontro la conexion al nodo coordinador");
                manager.setCoordinador();
                LOGGER.info("Se establece al nodo actual como coordinador");
                manager.getLoads().put(NodeInitializer.getNodeId(), load);
            }
        }
        // Si soy el coordinador, actualizo la carga en mis cargas
        else
        {
            manager.getLoads().put(message.getNodeId(), load);
        }
    }
    
    private void onRequestTranfer(Message message)
    {
        int amount = ((ResourceTransferMessagePayload)message.getPayload()).getAmount();
        Resource resource = ((ResourceTransferMessagePayload)message.getPayload()).getResource();
        LOGGER.info("Transfiero " + amount + " de " + resource.name());
    }
}
