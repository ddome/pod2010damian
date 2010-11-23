package ar.edu.itba.pod.legajo47189.payload.Impl;

import ar.edu.itba.pod.simul.communication.payload.ResourceRequestPayload;
import ar.edu.itba.pod.simul.market.Resource;

public class ResourceTransferPayloadImpl implements ResourceRequestPayload {

    private int amount;
    private Resource resource;
    
    public ResourceTransferPayloadImpl(int amount, Resource resource)
    {
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
