package ar.edu.itba.pod.legajo47189.communication.Impl;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import ar.edu.itba.pod.simul.communication.ClusterCommunication;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageListener;

public class ClusterCommunicationImpl implements ClusterCommunication {

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
        // TODO Auto-generated method stub

    }

    @Override
    public MessageListener getListener() throws RemoteException {
        return messageListener;
    }

    @Override
    public boolean send(Message message, String nodeId) throws RemoteException {
        return false;
    }
}
