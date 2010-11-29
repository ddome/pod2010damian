package ar.edu.itba.pod.legajo47189.communication.Impl;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47189.simulation.Impl.SimulationManagerImpl;
import ar.edu.itba.pod.simul.communication.AgentDescriptor;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.NodeAgentLoad;
import ar.edu.itba.pod.simul.communication.SimulationCommunication;
import ar.edu.itba.pod.simul.simulation.Agent;
import ar.edu.itba.pod.simul.simulation.Simulation;

public class SimulationCommunicationImpl implements SimulationCommunication {

    private final static Logger LOGGER = Logger.getLogger(SimulationCommunicationImpl.class);
    
    private AtomicBoolean started = new AtomicBoolean();
    public void start()
    {
        started.set(true);
    }
    
    public SimulationCommunicationImpl() throws RemoteException
    {
        UnicastRemoteObject.exportObject(this, 0);
        started.set(true);
    }
    
    
    @Override
    public NodeAgentLoad getMinimumNodeKnownLoad() throws RemoteException {
        NodeAgentLoad node = null;
        SimulationManagerImpl manager = (SimulationManagerImpl)NodeInitializer.getSimulationManager();
        if (NodeInitializer.getCoordinator().equals(NodeInitializer.getNodeId()))
        {
            node = getMinLoad(manager.getLoads());
        }
        return node;
    }

    @Override
    public Collection<AgentDescriptor> migrateAgents(int numberOfAgents)
            throws RemoteException {
        SimulationManagerImpl manager = (SimulationManagerImpl)NodeInitializer.getSimulationManager();
        List<AgentDescriptor> ret = new ArrayList<AgentDescriptor>();
        List<Agent> toRemove = new ArrayList<Agent>();
       
        if (numberOfAgents > manager.getAgents().size())
        {
            LOGGER.error("La cantidad de agentes a remover es mas grande que la cantidad de agentes corriendo");
            throw new RemoteException("La cantidad de agentes a remover es mas grande que la cantidad de agentes corriendo");
        }
        
        Collection<Agent> actuales = manager.getAgents();
        for (Agent agent : actuales)
        {
            if (numberOfAgents == 0)
                break;
            toRemove.add(agent);
            ret.add(agent.getAgentDescriptor());
            numberOfAgents--;
        }
        
        for (Agent agent : toRemove)
        {
            manager.removeAgent(agent);
        }        
        LOGGER.info("Se removieron " + toRemove.size() + " agentes");
        NodeAgentLoad myLoad = manager.getLoads()
            .put(NodeInitializer.getNodeId(), 
                    new NodeAgentLoad(NodeInitializer.getNodeId(), actuales.size()));
        // Aviso sobre mi nueva carga
        avisarNuevaCarga(myLoad);
        return ret;
    }

    @Override
    public void nodeLoadModified(NodeAgentLoad newLoad) throws RemoteException {
        SimulationManagerImpl manager = (SimulationManagerImpl)NodeInitializer.getSimulationManager();
        if (NodeInitializer.getCoordinator().equals(NodeInitializer.getNodeId()))
        {
            LOGGER.info("El nodo " + newLoad.getNodeId() + " me informo de una nueva carga " + newLoad.getNumberOfAgents() );
            manager.getLoads().put(newLoad.getNodeId(), newLoad);
        }
    }

    @Override
    public void startAgent(AgentDescriptor descriptor) throws RemoteException {
        SimulationManagerImpl manager = (SimulationManagerImpl)NodeInitializer.getSimulationManager();
        LOGGER.info("Comienzo un agente en este nodo");
        Agent agent = descriptor.build();
        Simulation simulation = NodeInitializer.getSimulationManager().getSimulation();
        agent.onBind(simulation);
        manager.addToMayAgents(agent);
        
        int number = manager.getLoads().get(NodeInitializer.getNodeId()).getNumberOfAgents() + 1;
        NodeAgentLoad newLoad = new NodeAgentLoad(NodeInitializer.getNodeId(), number);
        avisarNuevaCarga(newLoad);
       // agent.start();
    }
    
    private void avisarNuevaCarga(NodeAgentLoad load) 
    {
        SimulationManagerImpl manager = (SimulationManagerImpl)NodeInitializer.getSimulationManager();
        LOGGER.info("Informo al coordinador que mi nueva carga es " + load.getNumberOfAgents());
        // Si no soy el coordinador, tengo que avisar al coordinador que cambie mi carga
        if (!NodeInitializer.getCoordinator().equals(NodeInitializer.getNodeId()))
        {
            ConnectionManager connection = null;
            try
            {
                connection = NodeInitializer.getConnection().getConnectionManager(NodeInitializer.getCoordinator());
                connection.getSimulationCommunication().nodeLoadModified(load);
            } catch(Exception e)
            {
                try {
                    NodeInitializer.getConnection().getClusterAdmimnistration().disconnectFromGroup(NodeInitializer.getCoordinator());
                } catch (RemoteException e1) {
                    LOGGER.error(e1);
                }
                LOGGER.error("No se encontro la conexion al nodo coordinador");
                manager.setCoordinador(null);
                LOGGER.info("Se establece al nodo actual como coordinador");
                manager.getLoads().put(NodeInitializer.getNodeId(), load);
            }
        }
        manager.getLoads().put(NodeInitializer.getNodeId(), load);
    }
    
    private NodeAgentLoad getMinLoad(Map<String, NodeAgentLoad> nodeLoads)
    {
        Collection<NodeAgentLoad> loads = nodeLoads.values();
        int min = Integer.MAX_VALUE;
        NodeAgentLoad nodeLoad = null;
        
        LOGGER.debug("Voy a procesar " + nodeLoads.size());
        for (NodeAgentLoad load : loads)
        {
            if (load.getNumberOfAgents() < min)
            {
                min = load.getNumberOfAgents();
                nodeLoad = load;
            }
        }
        return nodeLoad;
    }
    

}
