package ar.edu.itba.pod.legajo47189.payload.Impl;

import ar.edu.itba.pod.simul.communication.payload.ResourceRequestPayload;
import ar.edu.itba.pod.simul.market.Resource;

public class ResourceRequestPayloadImpl implements
        ResourceRequestPayload {

    /**
     * 
     */
    private static final long serialVersionUID = 3287154437209702808L;
    
    private int amount;
    private Resource resource;
    
    public ResourceRequestPayloadImpl(int amount, Resource resource) {
        super();
        this.amount = amount;
        this.resource = resource;
    }
    
    @Override
    public int getAmountRequested() {
        // TODO Auto-generated method stub
        return amount;
    }
    @Override
    public Resource getResource() {
        // TODO Auto-generated method stub
        return resource;
    }
    


}
