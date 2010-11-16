package ar.edu.itba.pod.legajo47189.payload.Impl;

import ar.edu.itba.pod.simul.communication.payload.DisconnectPayload;

public class DisconnectPayloadImpl implements DisconnectPayload {

    /**
     * 
     */
    private static final long serialVersionUID = -1781504728941806977L;
    
    private String nodeId;
    
    public DisconnectPayloadImpl(String nodId)
    {
        setDisconnectedNodeId(nodId);
    }
    
    @Override
    public String getDisconnectedNodeId() {
        return nodeId;
    }
    
    public void setDisconnectedNodeId(String nodeId)
    {
        this.nodeId = nodeId;
    }
}
