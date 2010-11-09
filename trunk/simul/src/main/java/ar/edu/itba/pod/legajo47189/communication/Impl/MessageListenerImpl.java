package ar.edu.itba.pod.legajo47189.communication.Impl;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageListener;

public class MessageListenerImpl extends Thread implements MessageListener {
    
    private BlockingQueue<Message> messagesQueue =
        new LinkedBlockingQueue<Message>();
    
    public MessageListenerImpl() throws RemoteException
    {
        UnicastRemoteObject.exportObject(this, 0);
        new Thread(this).start();
    }
    
    @Override
    public void run()
    {
        Message message;
        while(true)
        {
            message = messagesQueue.poll();
            if (message == null)
            {
                try {
                    this.currentThread().sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                messageProcess(message);
            }   
        }
    }
    
    
    @Override
    public Iterable<Message> getNewMessages(String remoteNodeId)
            throws RemoteException {
        return messagesQueue;
    }

    @Override
    public boolean onMessageArrive(Message message) throws RemoteException {
        boolean messageExists = messagesQueue.contains(message);
        
        if (!messageExists)
        {
            messagesQueue.add(message);
        }
        return messageExists;
    }
    
    private void messageProcess(Message message)
    {
        System.out.println(message.toString());
    }
    
}