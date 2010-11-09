package ar.edu.itba.pod.legajo47189;

import java.io.Serializable;
import java.rmi.RemoteException;

import ar.edu.itba.pod.simul.communication.ClusterCommunication;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageListener;

public class ClusterCommunicationImpl implements ClusterCommunication, Serializable {

    
    // Singleton
    private static ClusterCommunicationImpl current;
    static
    {
        current = new ClusterCommunicationImpl();
    }
    public static ClusterCommunicationImpl getCurrent()
    {
        return current;
    }
    
    private static final long serialVersionUID = 2584468691452576509L;    
    private static final MessageListener messageListener = 
        new MessageListenerImpl();
    
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
