package ar.edu.itba.pod.legajo47189.simul.Impl;

import ar.edu.itba.pod.legajo47189.communication.Impl.NodeInitializer;
import ar.edu.itba.pod.simul.ObjectFactory;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.market.MarketManager;
import ar.edu.itba.pod.simul.simulation.SimulationManager;
import ar.edu.itba.pod.simul.time.TimeMapper;

public class ObjectFactoryImpl implements ObjectFactory {

    private NodeInitializer node;

    @Override
    public ConnectionManager createConnectionManager(String localIp) {
        String host = localIp.split(":")[0];
        Integer port = Integer.parseInt(localIp.split(":")[1]);
        node = new NodeInitializer();
        return node.initialize(localIp, null, port);
    }
    
    @Override
    public ConnectionManager createConnectionManager(String localIp,
            String groupIp) {
        String host = localIp.split(":")[0];
        Integer port = Integer.parseInt(localIp.split(":")[1]);
        node = new NodeInitializer();
        return node.initialize(localIp, groupIp, port);
    }

    @Override
    public MarketManager getMarketManager(ConnectionManager mgr) {
        return node.getMarketImpl().getManager();
    }

    @Override
    public SimulationManager getSimulationManager(ConnectionManager mgr,
            TimeMapper timeMappers) {
        return node.getSimulationManager(timeMappers);
    }
}
