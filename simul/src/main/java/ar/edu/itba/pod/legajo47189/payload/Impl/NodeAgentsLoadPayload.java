package ar.edu.itba.pod.legajo47189.payload.Impl;

import ar.edu.itba.pod.simul.communication.payload.NodeAgentLoadPayload;

public class NodeAgentsLoadPayload implements NodeAgentLoadPayload {

    private int load;
    
    public NodeAgentsLoadPayload(int load)
    {
        this.load = load;
    }
    
    @Override
    public int getLoad() {
        // TODO Auto-generated method stub
        return load;
    }
    
}
