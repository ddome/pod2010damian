package ar.edu.itba.pod.legajo47189.communication.Impl;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47189.tools.Helper;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageListener;
import ar.edu.itba.pod.simul.communication.MessageType;
import ar.edu.itba.pod.simul.communication.payload.DisconnectPayload;
import ar.edu.itba.pod.simul.communication.payload.Payload;

public class MessageListenerImpl extends Thread implements MessageListener {
    
    private final static Logger LOGGER = Logger.getLogger(MessageListenerImpl.class);
    
    private BlockingQueue<Message> messagesQueue;
    private List<Message> history;
    private Map<String, Long> requests;
    private boolean stopFlag = false;
    
    public MessageListenerImpl() throws RemoteException
    {
        UnicastRemoteObject.exportObject(this, 0);
        messagesQueue = new LinkedBlockingQueue<Message>();
        history = Collections.synchronizedList(new ArrayList<Message>());
        requests = new ConcurrentHashMap<String, Long>();
        new Thread(this).start();
    }
    
    @Override
    public void run()
    {
        LOGGER.info("Iniciando el proceso de escucha de mensajes");
        Message message;
        while(!stopFlag)
        {
            try {
                message = messagesQueue.poll();
                if (message == null)
                {
                    this.currentThread().sleep(100);
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
        LOGGER.info("Recibido un pedido de nuevos mensajes para el nodo " + remoteNodeId 
                + ", la ultima sincronizaci—n fue " + lastSync);
        requests.put(remoteNodeId, Helper.GetNow());
        List<Message> ret = getMessagesSince(lastSync);
        LOGGER.info("Hay " + ret.size() + " mensajes nuevos para el nodo " + remoteNodeId);
        return ret;
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
        boolean messageExists = history.contains(message);
        if (!messageExists)
        {
            try {
                messagesQueue.put(message);
            } catch (InterruptedException e) {
                throw new RemoteException(e.getMessage());
            }
            if (isBroadcast(message))
            {
                history.add(message);
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
        if (type == MessageType.DISCONNECT)
        {
            return true;
        }
        else if (type == MessageType.NEW_MESSAGE_REQUEST)
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
        
        if (isBroadcast(message))
        {
            history.add(message);
        }
        
        Payload payload = message.getPayload();
        MessageType type = message.getType();
        if (type == MessageType.DISCONNECT)
        {
            String disconnectedNode = ((DisconnectPayload)payload).getDisconnectedNodeId();
            LOGGER.info("Se desconectar‡ el nodo " + disconnectedNode + " de mi cluster");
            NodeInitializer.getCluster().getGroup().remove(disconnectedNode);
            LOGGER.info("Se desconecto el nodo " + disconnectedNode + " de mi cluster");
        } 
    }
}
