package ar.edu.itba.pod.legajo47189.market.Impl;

import ar.edu.itba.pod.simul.market.Market;
import ar.edu.itba.pod.simul.market.MarketInspector;
import ar.edu.itba.pod.simul.market.MarketManager;

import com.google.common.base.Preconditions;

public class MarketManagerImpl implements MarketManager {

    private MarketImpl market;

    public MarketManagerImpl(MarketImpl market)
    {
        this.market = market;
    }
    
    @Override
    public MarketInspector inspector() {
        return market;
    }

    @Override
    public Market market() {
        Preconditions.checkState(market != null,"There is no active market to be retrieved");
        return market;
    }

    @Override
    public void shutdown() {
        market.finish();
    }

    @Override
    public void start() {
        market.start();
    }

}
