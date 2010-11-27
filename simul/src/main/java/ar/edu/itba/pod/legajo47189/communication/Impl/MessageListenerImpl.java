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

import ar.edu.itba.pod.legajo47189.communication.Impl.ThreePhaseCommitImpl.ThreePhaseCommitState;
import ar.edu.itba.pod.legajo47189.communication.Impl.TransactionableImpl.TransactionableState;
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
import ar.edu.itba.pod.simul.market.ResourceStock;

import com.google.common.collect.Multiset;

public class MessageListenerImpl extends Thread implements MessageListener {
    
    private final static Logger LOGGER = Logger.getLogger(MessageListenerImpl.class);
    
    private BlockingQueue<Message> messagesQueue;
    private List<Message> messages;
    private Map<String, Long> requests;
    private boolean stopFlag = false;
    
    public MessageListenerImpl() throws RemoteException
    {
        UnicastRemoteObject.exportObject(this, 0);
        messagesQueue = new LinkedBlockingQueue<Message>();
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
                        NodeInitializer.getHistory().clear();
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
        List<Message> ret = getMessagesSince(lastSync, remoteNodeId);
        return ret;
    }
    
    private List<Message> getMessagesSince(Long lastSync, String toNodeId)
    {
        List<Message> messages = new ArrayList<Message>();
        List<Message> except = except(NodeInitializer.getHistory(), toNodeId);
        
        if(lastSync == null)
        {
            return except;
        }
        
        int index = except.size() - 1;
        if (index < 0)
        	return except;
        
        Message current = except.get(index);
        
        if(current==null)
        {
            return except;
        }
        
        Long timestamp = current.getTimeStamp();
        if (timestamp > lastSync)
        {
                messages.add(current);
        }
        index--;
        while(index >= 0)
        {
        	current = except.get(index);
        	//if (current.getTimeStamp() < lastSync)
        	//{
        	//    break;
        	//}
        	messages.add(current);
        	index--;
        } 
        return messages;
    }
    
    public List<Message> except(List<Message> messages, String nodeId)
    {
        List<Message> ret = new ArrayList<Message>();
        for (Message message : messages)
        {
            if (!message.getNodeId().equals(nodeId))
            {
                ret.add(message);
            }
        }
        return ret;
    }
    

    @Override
    public boolean onMessageArrive(Message message) throws RemoteException {
        boolean messageExists = messages.contains(message);
        if (!messageExists)
        {
            messages.add(message);
            // Es un broadcast que inicie yo
            if (message.getNodeId().equals(NodeInitializer.getNodeId()) && isBroadcast(message))
            {
                return messageExists;
            }
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
        if (type == MessageType.RESOURCE_TRANSFER)
        {
            onRequestTranfer(message);
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
        TransactionableImpl transaction = null;
        ThreePhaseCommitImpl commit = null;
        Multiset<Resource> colaExterna = null;
        Multiset<ResourceStock> selling = null;
        
        try {
            ConnectionManager connection = NodeInitializer.getConnection();
            transaction = (TransactionableImpl)connection.getNodeCommunication();
            commit = (ThreePhaseCommitImpl)connection.getThreePhaseCommit();
            selling =  NodeInitializer.getMarketImpl().getSelling();
            colaExterna = NodeInitializer.getMarketImpl().getColaExterna();
        } catch (RemoteException e) {
            LOGGER.error(e);
            return;
        }
        
        int amount = ((ResourceTransferMessagePayload)message.getPayload()).getAmount();
        Resource resource = ((ResourceTransferMessagePayload)message.getPayload()).getResource();
        String source = ((ResourceTransferMessagePayload)message.getPayload()).getSource();
        String destination = ((ResourceTransferMessagePayload)message.getPayload()).getDestination();
        
        if (source.equals(NodeInitializer.getNodeId()))
        {
            // Entrego
            LOGGER.info("Transfiero " + amount + " de " + resource.name());
            synchronized (colaExterna) {
                if (colaExterna.count(resource) != 0) {
                    colaExterna.add(resource, amount);
                } else {
                    colaExterna.setCount(resource, amount);
            }
        }
        }
        else
        {
            // Me entregan
            LOGGER.info("Me transfieren " + amount + " de " + resource.name());
            for (ResourceStock venta : selling)
            {
                if(venta.resource().equals(resource))
                {
                    int cantidad = selling.count(venta);
                    LOGGER.info(venta.name() + " vendio " + amount);
                    if (cantidad - amount > 0)
                    {
                        // ME SOBRA PAPA!
                        selling.setCount(venta, cantidad, cantidad - amount);
                        break;
                    }
                    else
                    {
                        selling.setCount(venta, cantidad, 0);
                        amount = amount - cantidad;
                        if (amount <= 0)
                        {
                            break;
                        }
                        
                    }
                }
            }
        }
        LOGGER.info("Terminada la transferencia de recursos");
        commit.changeState(ThreePhaseCommitState.INITIAL);
        transaction.changeState(TransactionableState.INITIAL);
        transaction.endTimer();
    }
}
