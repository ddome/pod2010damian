package ar.edu.itba.pod.legajo47189.payload.Impl;

import ar.edu.itba.pod.simul.communication.MarketData;
import ar.edu.itba.pod.simul.communication.payload.NodeMarketDataPayload;

public class NodeMarketDataPayloadImpl implements NodeMarketDataPayload {

    private MarketData marketData;
    
    @Override
    public MarketData getMarketData() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setMarketData(MarketData marketData) {
        this.marketData = marketData;
    }

}
