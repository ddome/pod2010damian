package ar.edu.itba.pod.legajo47189.communication.Impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47189.architecture.Cluster;
import ar.edu.itba.pod.legajo47189.market.Impl.MarketImpl;
import ar.edu.itba.pod.legajo47189.simulation.Impl.SimulationManagerImpl;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.market.Market;
import ar.edu.itba.pod.simul.time.TimeMapper;

public class NodeInitializer {
    
    private final static Logger LOGGER = Logger.getLogger(NodeInitializer.class);
    
    private static MessageSync sync;
    
    private static String nodeId;
    public static String getNodeId()
    {
        return nodeId;
    }
    
    private static Cluster cluster;
    public static Cluster getCluster()
    {
        return cluster;
    }
    
    private static SimulationManagerImpl simulationManager;
    public static SimulationManagerImpl getSimulationManager()
    {
        return simulationManager;
    }
    public static SimulationManagerImpl getSimulationManager(TimeMapper timeMapper)
    {
        simulationManager.setTimeMapper(timeMapper);
        return simulationManager;
    }
    
    private static MarketImpl market;
    public static MarketImpl getMarketImpl()
    {
        return market;
    }
    
    private static String coordinator;
    public static String getCoordinator()
    {
        return coordinator;
    }
    public static void setCoordinator(String coordinator)
    {
        NodeInitializer.coordinator = coordinator;
    }
    
    private static List<Message> history;
    public static void addToHistory(Message message)
    {
        history.add(message);
    }
    public static List<Message> getHistory()
    {
        return history;
    }

    private static ConnectionManager connectionManager;
    public static ConnectionManager getConnection()
    {
        return connectionManager;
    }
       
    public ConnectionManager initialize(String initId, String groupId, int port)
    {
        String id = initId;
        setInitialProperties();
        history = Collections.synchronizedList(new ArrayList<Message>());
        cluster = new Cluster();
        
        try {
            //TODO: Sacar esto!!!!
            ConnectionManagerImpl.ConnectionPort = port;
            connectionManager = new ConnectionManagerImpl();
            NodeInitializer.nodeId = id; 
            simulationManager = new SimulationManagerImpl(id);
            if (market == null)
            {
                market = new MarketImpl();
            }
        } catch (RemoteException e) {
            LOGGER.info(e.getMessage());
        }
        if (groupId == null)
        {
            createGroup(id, port);
        }
        else
        {
            try {
                connectionManager.getClusterAdmimnistration().connectToGroup(groupId);
            } catch (RemoteException e) {
                LOGGER.error(e);
            }
        }
            
        beginSync();
        LOGGER.info("Iniciado el nodo " + id);
        return connectionManager;
    }
    
    private void createGroup(String id, int port)
    {
        try {
            connectionManager.getClusterAdmimnistration().createGroup();
            LOGGER.info("Grupo creado exitosamente, Vamo' lo pibe!");
        } catch (RemoteException e) {
            LOGGER.info(e.getMessage());
        }
    }
     
    public void beginSync()
    {
        sync = new MessageSync();
        sync.start();
    }
    
    private static void setInitialProperties()
    {
        BasicConfigurator.configure();
    }
    
    public static void disconnect() throws RemoteException
    {
       // Falta terminar procesos
        
        MessageListenerImpl listener = (MessageListenerImpl) connectionManager.getGroupCommunication().getListener();
        sync.finish();
        LOGGER.info("Finalizado el proceso de escucha de mensajes");
        listener.finish();
        LOGGER.info("Finalizado el proceso de sincronizacion de mensajes");
    }
    
}
