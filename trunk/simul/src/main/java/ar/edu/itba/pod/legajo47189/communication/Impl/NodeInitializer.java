package ar.edu.itba.pod.legajo47189.communication.Impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47189.architecture.Cluster;
import ar.edu.itba.pod.legajo47189.payload.Impl.DisconnectPayloadImpl;
import ar.edu.itba.pod.legajo47189.simulation.Impl.SimulationManagerImpl;
import ar.edu.itba.pod.legajo47189.tools.Helper;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageType;
import ar.edu.itba.pod.simul.simulation.SimulationManager;

public class NodeInitializer {
    
    private final static Logger LOGGER = Logger.getLogger(NodeInitializer.class);
	
    public static final String FilePolicyPath =
        "/Users/damian/Documents/workspace/RMI/file.policy";
    
    public static final String ClassesPath =
        "file:/Users/damian/Downloads/simul/target/classes/ar/edu/itba/pod/legajo47189/communication/Impl";
    
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
    
    private static String coordinator;
    public static String getCoordinator()
    {
        return coordinator;
    }
    public static void setCoordinator(String coordinator)
    {
        NodeInitializer.coordinator = coordinator;
    }
    
    public static List<Message> history;

    private static ConnectionManager connectionManager;
    public static ConnectionManager getConnection()
    {
        return connectionManager;
    }
    
    public static void main(String[] args) {
        String nodeId = null;
        String initialNode = null;
        Integer port = 1099;
        if (args.length >= 1)
        {
            nodeId = args[0];
        }
        if (args.length >= 3)
        {
            initialNode = args[2];
        }
        if (args.length >= 2)
        {
            port = Integer.parseInt(args[1]);
        }
        initialize(nodeId, initialNode, port);   
    }
    
    private static void initialize(String initId, String initialNode, int port)
    {
        String id = initId;
        setInitialProperties();
        history = Collections.synchronizedList(new ArrayList<Message>());
        cluster = new Cluster();
        try {
            simulationManager = new SimulationManagerImpl(initId);
        } catch (RemoteException e1) {
            LOGGER.info(e1);
        }
        try {
            //TODO: Sacar esto!!!!
            ConnectionManagerImpl.ConnectionPort = port;
            connectionManager = new ConnectionManagerImpl();
            NodeInitializer.nodeId = id; 
            if (initialNode == null)
            {
                connectionManager.getClusterAdmimnistration().createGroup();
                LOGGER.info("Grupo creado exitosamente, Vamo' lo pibe!");
            }
            sync = new MessageSync();
            sync.start();
            
        } catch (RemoteException e) {
            LOGGER.info(e.getMessage());
        }
        
        LOGGER.info("Iniciado el nodo " + id);
    }
    
    private static void setInitialProperties()
    {
        BasicConfigurator.configure();
        System.setProperty("java.security.policy", FilePolicyPath);
        System.setProperty("java.rmi.server.codebase", ClassesPath);
        System.setSecurityManager(new java.rmi.RMISecurityManager());
    }

    private static void sendRandomMessages(boolean inicial)
    {
    	LOGGER.debug("Inicio mensajes de prueba");
        int time = 0;
        while(true)
        {
            try {
                Thread.currentThread().sleep(20000);
                time += 20000;
            } catch (InterruptedException e) {
                LOGGER.debug(e.getMessage());
            }
            try {
            	LOGGER.debug("Inicio un broadcast de prueba a los " + time / 1000 + " segundos");
                connectionManager.getGroupCommunication()
                    .broadcast(new Message(getNodeId(), Helper.GetNow(), MessageType.NEW_MESSAGE_REQUEST, new PayloadImpl()) );
                if (time >= 50000 && !inicial)
                {
                    LOGGER.debug("Mando broadcast para desconectarme del grupo");
                    connectionManager.getGroupCommunication()
                        .broadcast(new Message(getNodeId(), Helper.GetNow(), MessageType.DISCONNECT, new DisconnectPayloadImpl(getNodeId())) );
                    end();
                    LOGGER.debug("Desconectado exitosamente del grupo");
                    return;
                }
            } catch (RemoteException e) {

            	LOGGER.debug(e.getMessage());
            }
        }
    }
    
    private static void end() throws RemoteException
    {
        MessageListenerImpl listener = (MessageListenerImpl) connectionManager.getGroupCommunication().getListener();
        sync.endThread();
        LOGGER.debug("Finalizado el proceso de escucha de mensajes");
        listener.endThread();
        LOGGER.debug("Finalizado el proceso de sincronizacion de mensajes");
    }
    
}
