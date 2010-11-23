package ar.edu.itba.pod.legajo47189.simulation.Impl;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47189.communication.Impl.NodeInitializer;
import ar.edu.itba.pod.legajo47189.payload.Impl.NodeAgentLoadRequestPayloadImpl;
import ar.edu.itba.pod.legajo47189.tools.Helper;
import ar.edu.itba.pod.simul.communication.AgentDescriptor;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageType;
import ar.edu.itba.pod.simul.communication.NodeAgentLoad;
import ar.edu.itba.pod.simul.communication.payload.BalanceThread;
import ar.edu.itba.pod.simul.communication.payload.Payload;
import ar.edu.itba.pod.simul.simulation.Agent;
import ar.edu.itba.pod.simul.simulation.Simulation;
import ar.edu.itba.pod.simul.simulation.SimulationInspector;
import ar.edu.itba.pod.simul.simulation.SimulationManager;

import com.google.common.collect.Maps;

public class SimulationManagerImpl implements SimulationManager{

    private List<Agent> agents;
    private Map<String, NodeAgentLoad> agentLoads;
    private SimulationImpl simulation;
    private final Map<Class<?>, Object> env = Maps.newHashMap();
    
    private final static Logger LOGGER = Logger.getLogger(SimulationManagerImpl.class);
    private String nodeId;
    
    
    public SimulationManagerImpl(String nodeId) throws RemoteException
    {
        this.nodeId = nodeId;
        agents = Collections.synchronizedList(new ArrayList<Agent>());
        agentLoads = Collections.synchronizedMap(new HashMap<String, NodeAgentLoad>());
        // Guarda la informacion de la cantidad de agentes en los nodos
        String coordinator = nodeId;
        NodeInitializer.setCoordinator(coordinator);
        agentLoads.put(coordinator, new NodeAgentLoad(this.nodeId, 0));
        simulation = new SimulationImpl();
    }
    
    
    @Override
    public void addAgent(Agent agent) {

        NodeAgentLoad min = null;
        LOGGER.info(NodeInitializer.getCoordinator());
        
        if (NodeInitializer.getCoordinator().equals(this.nodeId))
        {
            LOGGER.info("El coordinador soy yo");
            try {
                min = NodeInitializer.getConnection().getSimulationCommunication()
                            .getMinimumNodeKnownLoad();
            } catch (RemoteException e) {
                LOGGER.error(e);
                return;
            }
        }
        else
        {
            LOGGER.info("El coordinador es remoto");
            try {
                min = NodeInitializer.getConnection()
                        .getConnectionManager(NodeInitializer.getCoordinator())
                            .getSimulationCommunication()
                                .getMinimumNodeKnownLoad();
            } catch (RemoteException e) {
                LOGGER.error(e);
            }
        }
        
        LOGGER.info("Starteo el agente en el nodo " + min.getNodeId());
        ConnectionManager connection = null;
        try {
            connection = NodeInitializer.getConnection().getConnectionManager(min.getNodeId());
        } catch (RemoteException e) {
            LOGGER.info("No se encontro la conexion para agregar agentes en el nodo " + min.getNodeId());
            try {
                NodeInitializer.getConnection().getClusterAdmimnistration().disconnectFromGroup(min.getNodeId());
            } catch (RemoteException e1) {
                LOGGER.error(e);
                return;
            }
        }
        try {
            connection.getSimulationCommunication().startAgent(agent.getAgentDescriptor());
        } catch (RemoteException e1) {
            LOGGER.error(e1);
            return;
        }
    }

    @Override
    public Collection<Agent> getAgents() {
        return agents;
    }

    @Override
    public SimulationInspector inspector() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> void register(Class<T> type, T instance) {
        env.put(type, instance);
    }

    @Override
    public void removeAgent(Agent agent) {

        if (agents.contains(agent))
        {   
            agent.finish();
            try {
                agent.join();
            } catch (InterruptedException e) {
                LOGGER.error(e);
            }
            agents.remove(agent);    
            NodeAgentLoad load  = agentLoads.get(this.nodeId);
            int number = load.getNumberOfAgents() - 1;
            load.setNumberOfAgents(number);
        }

    }

    @Override
    public void shutdown() {
        for (Agent agent : agents) {
            agent.finish();
        }
        for (Agent agent : agents) {
            try {
                agent.join();
            } catch (InterruptedException e) {
                LOGGER.error(e);
            }
        }
    }

    @Override
    public Simulation simulation() {
        return simulation;
    }

    @Override
    public void start() {
        LOGGER.info("Se inician los agentes");
        for (Agent agent : agents)
        {
            agent.start();
        }

    }
    
    public void addToMayAgents(Agent agent)
    {
        agents.add(agent);
    }
    
    public Map<String, NodeAgentLoad> getLoads()
    {
        return agentLoads;
    }

    public void setCoordinador()
    {
        String coordinator = this.nodeId;
        NodeInitializer.setCoordinator(coordinator);
        Payload payload = new NodeAgentLoadRequestPayloadImpl();
        try {
            NodeInitializer.getConnection().getGroupCommunication()
                .broadcast(new Message(coordinator, Helper.GetNow(), MessageType.NODE_AGENTS_LOAD_REQUEST, payload));
        } catch (RemoteException e) {
            LOGGER.error(e);
        }
        
        BalanceThread balanceThread = new BalanceThread(this);
        // Espero y balanceo
        balanceThread.start();
    }
    
    public void doBalance()
    {
        // Se supone que ya tengo los balances actualizados de los nodos del cluster
        // Si esto anda de una soy gardel
        LOGGER.info("Se comienza el balanceo de nodos");
        int agentsPerNode = getAgentsPerNode();
        Queue<AgentDescriptor> migrate = new ConcurrentLinkedQueue<AgentDescriptor>();
        
        LOGGER.info("Se balanceare a " + agentsPerNode + " por cada nodo en un total de " + agentLoads.size());
        
        List<AgentDescriptor> cut = new ArrayList<AgentDescriptor>();
        // Recorto la cantidad de agentes
        LOGGER.info("Comienzo de recorte de excesos de agentes en nodos");
        for (NodeAgentLoad load : agentLoads.values())
        {
            if (load.getNumberOfAgents() > agentsPerNode)
            {
                int deleteNumber = load.getNumberOfAgents() - agentsPerNode;
                LOGGER.info("Se recortara al nodo " + load.getNodeId() + " en " + deleteNumber);
                migrate.addAll(cutAgents(load, deleteNumber));
            }
        }
       
        LOGGER.info("Comienzo de relleno de agentes en nodos");
        // Relleno los que faltan
        int number = 0;
        AgentDescriptor current = null;
        ConnectionManager connection = null;
        for (NodeAgentLoad load : agentLoads.values())
        {
            if (load.getNumberOfAgents() < agentsPerNode)
            {
                number = agentsPerNode - load.getNumberOfAgents();
                LOGGER.info("Se rellenara al nodo " + load.getNodeId() + " en " + number);
                try {
                    connection = NodeInitializer.getConnection().getConnectionManager(load.getNodeId());
                } catch (RemoteException e) {
                    LOGGER.info("No se encontro la conexion para balancear agentes en el nodo " + load.getNodeId());
                    try {
                        NodeInitializer.getConnection().getClusterAdmimnistration().disconnectFromGroup(load.getNodeId());
                        break;
                    } catch (RemoteException e1) {
                        LOGGER.error(e1);
                        break;
                    }
                }
                
                while(number > 0)
                {
                     current = migrate.poll();
                     try {
                        connection.getSimulationCommunication().startAgent(current);
                    } catch (RemoteException e) {
                        LOGGER.error(e);
                        migrate.add(current);
                        break;
                    }
                    number --;
                }
            }
        }
       
        LOGGER.info("Fin del balanceo");
    }

    private int getTotalAgents()
    {
        //LINQ, TE EXTRA„O! MUERTE A JAVA
        Collection<NodeAgentLoad> loads = agentLoads.values();
        int total = 0;
        for (NodeAgentLoad load : loads)
        {
            total = total + load.getNumberOfAgents();
        }
        return total;
    }

    private int getAgentsPerNode()
    {
        double nodes = agentLoads.size();
        return (int)Math.floor((getTotalAgents()/nodes));
    }
    
    private Collection<AgentDescriptor> cutAgents(NodeAgentLoad load, int number)
    {
        Collection<AgentDescriptor> agents = new ArrayList<AgentDescriptor>();
        ConnectionManager connection = null;
        try {
            connection = NodeInitializer.getConnection().getConnectionManager(load.getNodeId());
        } catch (RemoteException e) {
            try {
                LOGGER.info("No se encontro la conexion para balancear agentes en el nodo " + load.getNodeId());
                NodeInitializer.getConnection().getClusterAdmimnistration().disconnectFromGroup(load.getNodeId());
                return agents;
            } catch (RemoteException e1) {
                LOGGER.error(e1);
                return agents;
            }
        }
        
        try {
            agents = connection.getSimulationCommunication().migrateAgents(number);
        } catch (RemoteException e) {
            LOGGER.error(e);
        }
        
        return agents;
         
    }
    
}
