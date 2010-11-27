package ar.edu.itba.pod.legajo47189.communication.Impl;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47189.payload.Impl.ResourceTransferPayloadImpl;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.ThreePhaseCommit;
import ar.edu.itba.pod.simul.communication.Transactionable;
import ar.edu.itba.pod.simul.communication.payload.Payload;
import ar.edu.itba.pod.simul.market.Resource;

public class TransactionableImpl implements Transactionable {

    private final static Logger LOGGER = Logger.getLogger(TransactionableImpl.class);
    public static final int TIMEOUT = 10000;
    
    public enum TransactionableState {
        INITIAL,
        READY,
        WAITING,
        COMMITING,
        COMITTED
    }
   
    private TransactionThread timer;
    private TransactionableState current;
    private String remoteId;
    private long timeout;
    
    // EXCHANGED
    private AtomicInteger amount;
    private Resource resource;
    private AtomicBoolean exchanged;
    
    private String takeFrom;
    private String takeTo;
    
    public TransactionableImpl() throws RemoteException 
    {
        amount = new AtomicInteger();
        exchanged = new AtomicBoolean();
        resource = null;
        current = TransactionableState.INITIAL;
        UnicastRemoteObject.exportObject(this, 0); 
    }

    @Override
    public void beginTransaction(String remoteNodeId, long timeout)
            throws RemoteException {
        
        if (current != TransactionableState.INITIAL || timer != null && timer.isAlive())
        {
            throw new IllegalStateException("Ya hay una transaccion en progreso");
        }
        
        ConnectionManager connection = null;
        try
        {
            connection = NodeInitializer.getConnection().getConnectionManager(remoteNodeId);
        }
        catch(RemoteException e)
        {
            LOGGER.error("No pudo establecerse una conexion con el nodo " + remoteNodeId + ". Se desconecta el nodo de la cluster");
            NodeInitializer.getConnection().getClusterAdmimnistration().disconnectFromGroup(remoteNodeId);
            return;
        }
        
        connection.getNodeCommunication().acceptTransaction(NodeInitializer.getNodeId());
        
        changeState(TransactionableState.READY);
        
        remoteId = remoteNodeId;
        
        LOGGER.info("Comienzo de transaccion");
        timer = new TransactionThread(timeout, this);
        timer.start();
        
    }
    
    @Override
    public void acceptTransaction(String remoteNodeId) throws RemoteException {
                
        if (current != TransactionableState.INITIAL)
        {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                LOGGER.error(e);
            }
            if (current != TransactionableState.INITIAL)
            {   
                throw new IllegalStateException("Ya hay una transaccion en progreso");
            }
        }
        
        current = TransactionableState.READY;
        remoteId = remoteNodeId; 
        
        LOGGER.info("Comienzo de transaccion");
        timer = new TransactionThread(timeout, this);
        timer.start();
    }

    @Override
    public void endTransaction() throws RemoteException {

        if (current != TransactionableState.READY) {
            throw new IllegalStateException("No hay una transaccion inicializada");
        }
        changeState(TransactionableState.COMMITING);
        commit(remoteId);
        
    }

    @Override
    public void exchange(Resource resource, int amount, String sourceNode,
            String destinationNode) throws RemoteException {
 
        if (current != TransactionableState.READY) {
            rollback();
            throw new IllegalStateException("No se inicio una transaccion");
        }
        
        if (!remoteId.equals(destinationNode))
        {
            rollback();
            throw new IllegalStateException("No se inicio una transaccion con el nodo destino");
        }
        
        if (!NodeInitializer.getNodeId().equals(sourceNode))
        {
            rollback();
            throw new IllegalStateException("No hay una transiccion iniciada para el nodo source");
        }
        
        if (sourceNode.equals(destinationNode))
        {
            rollback();
            throw new IllegalStateException("Los nodos destino y fuente son iguales");
        }
        
        this.resource = resource;
        this.amount.set(amount);
        takeFrom = sourceNode;
        takeTo = destinationNode;
        exchanged.set(true);
    }

    @Override
    public Payload getPayload() throws RemoteException {
        
        if (current == TransactionableState.INITIAL)
        {
            throw new IllegalStateException("No hay una transaccion");
        }
        
        if (!exchanged.get())
        {
            throw new IllegalStateException("No hay recursos intercambiados");
        }
        return new ResourceTransferPayloadImpl(amount.get(), resource, takeFrom, takeTo);
    }

    @Override
    public void rollback() throws RemoteException {
        changeState(TransactionableState.INITIAL);
        exchanged.set(false);
        resource = null;
        amount.set(0);
        remoteId = null;
    }

    private void commit(String remoteId) throws RemoteException
    {
        LOGGER.debug(remoteId);
        ThreePhaseCommit from = null;
        try {
            from = NodeInitializer.getConnection().getThreePhaseCommit();
        } catch (RemoteException e) {
            LOGGER.error(e);
            rollback();
            return;
        }
        ThreePhaseCommit to = null;
        ConnectionManager remote = null;
        try {
            remote = NodeInitializer.getConnection().getConnectionManager(remoteId);
            to = remote.getThreePhaseCommit();
        } catch (RemoteException e) {
            rollback();
            LOGGER.error("No pudo establecerse una conexion con el nodo " + remoteId + ". Se desconecta el nodo de la cluster");
            try {
                NodeInitializer.getConnection().getClusterAdmimnistration().disconnectFromGroup(remoteId);
            } catch (RemoteException e1) {
                LOGGER.error(e1);
                rollback();
                return;
            }
            throw e;
        }
        
        LOGGER.debug(from);
        LOGGER.debug(to);
        
        // Can commit
        LOGGER.info("Se inicia el comiteo");
        if (from.canCommit(NodeInitializer.getNodeId(), TIMEOUT) 
                && to.canCommit(NodeInitializer.getNodeId(), TIMEOUT))
        {
            LOGGER.info("Can commit ok");
            // Pre commit
            try{
                to.preCommit(NodeInitializer.getNodeId());
                from.preCommit(NodeInitializer.getNodeId());
                changeState(TransactionableState.WAITING);
            }catch(Exception e)
            {
                rollback();
                LOGGER.info("Fallo la transaccion en el preCommit");
                from.abort();
                to.abort();
                throw new RemoteException(e.getMessage());
            }
            LOGGER.info("Pre commit ok");
            try{
                to.doCommit(NodeInitializer.getNodeId());
                from.doCommit(NodeInitializer.getNodeId());
                changeState(TransactionableState.COMMITING);
            }catch(Exception e)
            {
                rollback();
                LOGGER.info("Fallo la transaccion en el doCommit");
                from.abort();
                to.abort();
                throw new RemoteException(e.getMessage());
            }
            LOGGER.info("Termino el commit");
        }
        else
        {
            rollback();
            LOGGER.info("Los nodos no estan disponibles para comitear");
            throw new RemoteException("Los nodos no estan disponibles para comitear");
        }
    }
    
    public void changeState(TransactionableState state)
    {
        synchronized (current) {
            current = state;
        }
    }
    
    public void endTimer()
    {
        timer.setFinished(true);
    }
}
