package ar.edu.itba.pod.legajo47189.communication.Impl;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47189.communication.Impl.TransactionableImpl.TransactionableState;
import ar.edu.itba.pod.legajo47189.tools.Helper;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageType;
import ar.edu.itba.pod.simul.communication.ThreePhaseCommit;
import ar.edu.itba.pod.simul.communication.payload.Payload;

public class ThreePhaseCommitImpl implements ThreePhaseCommit {
    
    
    private final static Logger LOGGER = Logger.getLogger(ThreePhaseCommitImpl.class);
    
    private TransactionableImpl transaction;
    private ThreePhaseThread timer;
    
    public enum ThreePhaseCommitState {
        INITIAL,
        CAN_COMMIT,
        PRE_COMMIT,
        COMMITED
    }
    
    public ThreePhaseCommitImpl(TransactionableImpl transaction) throws RemoteException
    {
        this.transaction = transaction;
        current = ThreePhaseCommitState.INITIAL;
        changeState(ThreePhaseCommitState.INITIAL);
        UnicastRemoteObject.exportObject(this, 0); 
    }
    
    private ThreePhaseCommitState current;
    private String coordinator;
    
    @Override
    public void abort() throws RemoteException {
        switch (current) {
        case INITIAL:
        case CAN_COMMIT:
        case PRE_COMMIT:
                changeState(ThreePhaseCommitState.INITIAL);
                break;
        case COMMITED:
            // TODO: Tirar todo para atras
            changeState(ThreePhaseCommitState.INITIAL);
            break;
        }
    }

    @Override
    public boolean canCommit(String coordinatorId, long timeout)
            throws RemoteException {

        if (current == ThreePhaseCommitState.INITIAL)
        {
            changeState(ThreePhaseCommitState.CAN_COMMIT);
            coordinator = coordinatorId;
            timer = new ThreePhaseThread(timeout, this);
            timer.start();
            return true;
        }
        return false;
    }

    @Override
    public void preCommit(String coordinatorId) throws RemoteException {
        changeState(ThreePhaseCommitState.PRE_COMMIT);
    }
    
    @Override
    public void doCommit(String coordinatorId) throws RemoteException {
        
        ConnectionManager coordinator = 
                NodeInitializer.getConnection().getConnectionManager(coordinatorId);
        
        // Comiteo los cambios
        Payload payload = coordinator.getNodeCommunication().getPayload();
        NodeInitializer.getConnection().getGroupCommunication()
            .send(new Message(NodeInitializer.getNodeId(), Helper.GetNow(), MessageType.RESOURCE_TRANSFER, payload), 
                    NodeInitializer.getNodeId());

        changeState(ThreePhaseCommitState.COMMITED);
        transaction.changeState(TransactionableState.COMITTED);
        timer.setFinished(true);
    }

    @Override
    public void onTimeout() throws RemoteException {
        switch (current) {
        case CAN_COMMIT:
                abort();
                break;
        case PRE_COMMIT:
                // Se hace el commit igualmente
                doCommit(coordinator);
                break;
        case COMMITED:
            abort();
            break;
        default:
            break;
        }
    }
    
    public void changeState(ThreePhaseCommitState state)
    {
        synchronized (current) {
            current = state;
        }
    }

}
