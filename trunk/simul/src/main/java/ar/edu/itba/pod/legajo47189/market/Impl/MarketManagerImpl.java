package ar.edu.itba.pod.legajo47189.market.Impl;

import ar.edu.itba.pod.simul.market.Market;
import ar.edu.itba.pod.simul.market.MarketInspector;
import ar.edu.itba.pod.simul.market.MarketManager;

public class MarketManagerImpl implements MarketManager {

    private Market market;
    
    public MarketManagerImpl()
    {
        market = new MarketImpl();
    }
    
    @Override
    public MarketInspector inspector() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Market market() {
        return market;
    }

    @Override
    public void shutdown() {
        // TODO Auto-generated method stub

    }

    @Override
    public void start() {
        // TODO Auto-generated method stub

    }

}
