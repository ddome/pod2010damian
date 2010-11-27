package ar.edu.itba.pod.legajo47189.payload.Impl;

import ar.edu.itba.pod.simul.communication.payload.ResourceTransferMessagePayload;
import ar.edu.itba.pod.simul.market.Resource;

public class ResourceTransferPayloadImpl implements ResourceTransferMessagePayload {

    /**
     * 
     */
    private static final long serialVersionUID = -3781033206856854862L;
    
    private int amount;
    private Resource resource;
    private String source;
    private String destination;
    
    public ResourceTransferPayloadImpl(int amount, Resource resource, String source, String destination)
    {
        this.amount = amount;
        this.resource = resource;
        this.source = source;
        this.destination = destination;
    }
    
    @Override
    public Resource getResource() {
        // TODO Auto-generated method stub
        return resource;
    }

    @Override
    public int getAmount() {
        // TODO Auto-generated method stub
        return amount;
    }

    @Override
    public String getDestination() {
        // TODO Auto-generated method stub
        return destination;
    }

    @Override
    public String getSource() {
        // TODO Auto-generated method stub
        return source;
    }

}
