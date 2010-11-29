package ar.edu.itba.pod.legajo47189.communication.Impl;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47189.communication.Impl.ThreePhaseCommitImpl.ThreePhaseCommitState;
import ar.edu.itba.pod.legajo47189.communication.Impl.TransactionableImpl.TransactionableState;
import ar.edu.itba.pod.legajo47189.market.Impl.MarketImpl;
import ar.edu.itba.pod.legajo47189.payload.Impl.NodeAgentsLoadPayload;
import ar.edu.itba.pod.legajo47189.simulation.Impl.SimulationManagerImpl;
import ar.edu.itba.pod.legajo47189.tools.Helper;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.MarketData;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageListener;
import ar.edu.itba.pod.simul.communication.MessageType;
import ar.edu.itba.pod.simul.communication.NodeAgentLoad;
import ar.edu.itba.pod.simul.communication.Transactionable;
import ar.edu.itba.pod.simul.communication.payload.DisconnectPayload;
import ar.edu.itba.pod.simul.communication.payload.NodeMarketDataPayload;
import ar.edu.itba.pod.simul.communication.payload.Payload;
import ar.edu.itba.pod.simul.communication.payload.ResourceRequestPayload;
import ar.edu.itba.pod.simul.communication.payload.ResourceTransferMessagePayload;
import ar.edu.itba.pod.simul.market.NodeMarketDataPayloadImpl;
import ar.edu.itba.pod.simul.market.Resource;
import ar.edu.itba.pod.simul.market.ResourceStock;
import ar.edu.itba.pod.thread.CleanableThread;

import com.google.common.collect.Multiset;

public class MessageListenerImpl extends CleanableThread implements MessageListener {
    
    private final static Logger LOGGER = Logger.getLogger(MessageListenerImpl.class);
    
    private BlockingQueue<Message> messagesQueue;
    private List<Message> messages;
    private Map<String, Long> requests;
    
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
        while(!shouldFinish())
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
        
        try
        {
        for (Message message : messages)
        {
            if (!message.getNodeId().equals(nodeId))
            {
                ret.add(message);
            }
        }
        } catch(ConcurrentModificationException e)
        {
        	
        }
        return ret;
    }
    
    private void urgent(Message message)
    {
        if (message.getType() == MessageType.NODE_AGENTS_LOAD_REQUEST)
        {
            NodeInitializer.setCoordinator( message.getNodeId() );
        }
    }
    

    @Override
    public boolean onMessageArrive(Message message) throws RemoteException {
        boolean messageExists = messages.contains(message);
        if (!messageExists)
        {
            urgent(message);
            messages.add(message);
            //// Es un broadcast que inicie yo
            //if (message.getNodeId().equals(NodeInitializer.getNodeId()) && isBroadcast(message))
            //{
            //    return messageExists;
            //}
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
        else if (type == MessageType.NODE_MARKET_DATA_REQUEST)
        {
            return true;
        }
        else if (type == MessageType.NODE_MARKET_DATA)
        {
            return false;
        }
        else
        {
            return false;
        }
    }
    
    private void messageProcess(Message message) throws InterruptedException
    {
        //LOGGER.info("Procesando el mensaje " + message.getNodeId() + " de tipo " + message.getType().toString());
        MessageType type = message.getType();
        if (type == MessageType.DISCONNECT)
        {
            onDisconnect(message);
        }
        else if (type == MessageType.NODE_AGENTS_LOAD_REQUEST)
        {
            onNodeAgentsLoadRequest(message);
        }
        else if (type == MessageType.NODE_AGENTS_LOAD)
        {
            onNodeAgentsLoad(message);
        }
        else if (type == MessageType.RESOURCE_TRANSFER)
        {
            onRequestTranfer(message);
        }
        else if (type == MessageType.RESOURCE_REQUEST)
        {
            onResourceRequest(message);
        }
        else if (type == MessageType.NODE_MARKET_DATA_REQUEST)
        {
            onNodeMarketDataRequest(message);
        }
        else if (type == MessageType.NODE_MARKET_DATA)
        {
            onNodeMarketData(message);   
        }
    }
    
    private void onNodeMarketDataRequest(Message message)
    {        
        if (!message.getNodeId().equals(NodeInitializer.getNodeId()))
        {
            MarketData data = NodeInitializer.getMarketImpl().basicMarketData();
            NodeInitializer.setCoordinator(message.getNodeId());
            Payload payload = new NodeMarketDataPayloadImpl(data);
            Message newMessage = new Message(NodeInitializer.getNodeId(), Helper.GetNow(), MessageType.NODE_MARKET_DATA, payload);
            try {
                NodeInitializer.getConnection().getGroupCommunication().send(newMessage, message.getNodeId());
            } catch (RemoteException e) {
                LOGGER.error(e);
            }
        }
    }
    
    private void onNodeMarketData(Message message)
    {
        if (!message.getNodeId().equals(NodeInitializer.getNodeId()))
        {
            MarketData data = ((NodeMarketDataPayload)message.getPayload()).getMarketData();
            LOGGER.info("Agrego transacciones " + data.getHistory().getTransactionsPerSecond());
            NodeInitializer.getMarketImpl().addTotal(data.getHistory().getTransactionsPerSecond());
        }
    }
    
    private void onDisconnect(Message message)
    {
        Payload payload = message.getPayload();
        String disconnectedNode = ((DisconnectPayload)payload).getDisconnectedNodeId();
        if (disconnectedNode.equals(NodeInitializer.getNodeId()))
        {
            LOGGER.info("Se desconectara el nodo actual del grupo");
            // le doy tiempo a mi cluster para que reorganice mis agentes
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                LOGGER.error(e);
            }
            NodeInitializer.getCluster().setGroup(null);
            try {
                NodeInitializer.disconnect();
            } catch (RemoteException e) {
                LOGGER.error(e);
            }
        }
        else
        {
            LOGGER.info("Se desconectar‡ el nodo " + disconnectedNode + " de mi cluster");
            NodeInitializer.getCluster().getGroup().remove(disconnectedNode);
            LOGGER.info("Se desconecto el nodo " + disconnectedNode + " de mi cluster");
            
            // Si el nodo que se desconecto era el coordinador, tengo que organizar todo 
            // devuelta para que los demas nodos tengan un coordinador valido
            //if (NodeInitializer.getCoordinator().equals(disconnectedNode) 
              //      || NodeInitializer.getCoordinator().equals(NodeInitializer.getNodeId()))
            //{
             //   LOGGER.info("ENTREee");
             //   NodeInitializer.getSimulationManager().setCoordinador(null);
            //}
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
                try {
                    NodeInitializer.getConnection().getClusterAdmimnistration().disconnectFromGroup(NodeInitializer.getCoordinator());
                } catch (RemoteException e1) {
                    LOGGER.error(e1);
                }
                LOGGER.error("No se encontro la conexion al nodo coordinador");
                manager.setCoordinador(null);
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
        
        if (!source.equals(NodeInitializer.getNodeId()))
        {
            // Me entregan
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
            // Entrego
            for (ResourceStock venta : selling)
            {
                if(venta.resource().equals(resource))
                {
                    int cantidad = selling.count(venta);
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
        transaction.changeState(TransactionableState.INITIAL);
        transaction.endTimer();
        commit.changeState(ThreePhaseCommitState.INITIAL);
    }
    
    public void onResourceRequest(Message message)
    {
        ResourceRequestPayload payload = (ResourceRequestPayload)message.getPayload();
        int cantidad = payload.getAmountRequested();
        Resource recurso = payload.getResource();
        String requester = message.getNodeId();
        int transferencia = 0;
        
        if (requester == null)
        {
            LOGGER.error("El nodo solicitante es invalido");
            return;
        }
        
        MarketImpl market = NodeInitializer.getMarketImpl();
        Multiset<ResourceStock> selling = market.getSelling();

        synchronized (selling) {
            for (ResourceStock sell : selling)
            {
                if (sell.resource().equals(recurso))
                {
                    if (sell.current() >= cantidad)
                    {
                        transferencia = cantidad;
                    }
                    else
                    {
                        transferencia = sell.current();
                    }
                }
            }
        }
        
        if (transferencia != 0)
        {
            ConnectionManager connection = NodeInitializer.getConnection();
            Transactionable transaction;
            try {
                transaction = connection.getNodeCommunication();
            } catch (RemoteException e) {
                LOGGER.error(e);
                return;
            }
            try {
                transaction.beginTransaction(requester, 10000);
            } catch (RemoteException e) {
                try {
                    transaction.rollback();
                    return;
                } catch (RemoteException e1) {
                    LOGGER.error(e1);
                    return;
                }
            }
            catch (IllegalStateException e2) {
                try {
                    transaction.rollback();
                    return;
                } catch (RemoteException e1) {
                    LOGGER.error(e1);
                    return;
                }
            }
            try {
                transaction.exchange(recurso, transferencia, NodeInitializer.getNodeId(), requester);
            } catch (RemoteException e) {
                LOGGER.error(e);
                try {
                    transaction.rollback();
                    LOGGER.error(e);
                    return;
                } catch (RemoteException e1) {
                    LOGGER.error(e1);
                    return;
                }
            }
            catch (IllegalStateException e2) {
                try {
                    LOGGER.error(e2);
                    transaction.rollback();
                    return;
                } catch (RemoteException e1) {
                    LOGGER.error(e1);
                    return;
                }
            }
            try {
                transaction.endTransaction();
            } catch (RemoteException e) {
                LOGGER.error(e);
                try {
                    transaction.rollback();
                    return;
                } catch (RemoteException e1) {
                    LOGGER.error(e1);
                    return;
                }
            }
            catch (IllegalStateException e2) {
                try {
                    LOGGER.error(e2);
                    transaction.rollback();
                    return;
                } catch (RemoteException e1) {
                    LOGGER.error(e1);
                    return;
                }
            }
            
        }        
    }
}
