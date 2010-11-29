package ar.edu.itba.pod.legajo47189.simulation.Impl;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import ar.edu.itba.pod.legajo47189.communication.Impl.NodeInitializer;
import ar.edu.itba.pod.simul.simulation.Simulation;
import ar.edu.itba.pod.simul.simulation.SimulationEvent;
import ar.edu.itba.pod.simul.simulation.SimulationEventHandler;
import ar.edu.itba.pod.simul.simulation.SimulationInspector;
import ar.edu.itba.pod.simul.time.TimeMapper;
import ar.edu.itba.pod.thread.doc.ThreadSafe;

import com.google.common.base.Preconditions;

/**
 * Local implementation of a simulation.
 * This implementation uses a different threrad for each agent
 */
@ThreadSafe
class SimulationImpl implements Simulation, SimulationInspector {
        private final List<SimulationEventHandler> handlers =  new CopyOnWriteArrayList<SimulationEventHandler>();
        private  TimeMapper timeMapper;

        public SimulationImpl(TimeMapper timeMapper) {
                super();
                this.timeMapper = timeMapper;
        }
        
        @Override
        public void add(SimulationEventHandler handler) {
                Preconditions.checkArgument(!handlers.contains(handler), "Can't add a handler twice!");
                handlers.add(handler);
        }
        
        @Override
        public void remove(SimulationEventHandler handler) {
                Preconditions.checkArgument(handlers.contains(handler), "Handler not registered!");
                handlers.remove(handler);
        }
        
        @Override
        public void raise(SimulationEvent event) {
                for(SimulationEventHandler handler : handlers) {
                        handler.onEvent(event);
                }
        }
        
        @Override
        public void wait(int amount, TimeUnit unit) throws InterruptedException {
                long millis = timeMapper.toMillis(amount, unit);
                Thread.sleep(millis);
        }

        
        @SuppressWarnings("unchecked")
        @Override
        public <T> T env(Class<T> param) {
                return NodeInitializer.getSimulationManager().getEnv(param);
        }
        
        public void setTimeMapper(TimeMapper timer)
        {
            this.timeMapper = timer;
        }

        @Override
        public int runningAgents() {
            return NodeInitializer.getSimulationManager().getAgents().size();
        }
}
