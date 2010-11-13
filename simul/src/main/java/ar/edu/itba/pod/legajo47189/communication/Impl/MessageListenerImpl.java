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

import ar.edu.itba.pod.legajo47189.tools.Helper;
import ar.edu.itba.pod.simul.communication.ClusterCommunication;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageListener;

public class MessageListenerImpl extends Thread implements MessageListener {
    
    private BlockingQueue<Message> messagesQueue;
    private List<Message> history;
    private Map<String, Long> requests;
    
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
        Message message;
        while(true)
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
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public Iterable<Message> getNewMessages(String remoteNodeId)
            throws RemoteException {
        
        Long lastSync = requests.get(remoteNodeId);
        requests.put(remoteNodeId, Helper.GetNow());
        return history;
    }
    
    private Iterable<Message> getMessagesSince(Long lastSync)
    {
        List<Message> messages = new ArrayList<Message>();
        
        int index = history.size() - 1;
        Message current = null;
        while(current.getTimeStamp() > lastSync)
        { 
          messages.add(current); 
          index--;
          current = history.get(index);
        } 
        return messages;
    }
    

    @Override
    public boolean onMessageArrive(Message message) throws RemoteException {
        boolean messageExists = history.contains(message);
        if (!messageExists)
        {
            history.add(message);
            try {
                messagesQueue.put(message);
            } catch (InterruptedException e) {
                throw new RemoteException(e.getMessage());
            }
            NodeInitializer.getConnection().getGroupCommunication().broadcast(message);
        }
        return messageExists;
    }
    
    private void messageProcess(Message message) throws InterruptedException
    {
        System.out.println(message.toString());
        history.add(message);
    }
}
