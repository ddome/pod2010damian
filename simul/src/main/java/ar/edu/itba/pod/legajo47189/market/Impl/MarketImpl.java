package ar.edu.itba.pod.legajo47189.market.Impl;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47189.communication.Impl.NodeInitializer;
import ar.edu.itba.pod.legajo47189.payload.Impl.NodeMarketDataRequestPayloadImpl;
import ar.edu.itba.pod.legajo47189.payload.Impl.ResourceRequestPayloadImpl;
import ar.edu.itba.pod.legajo47189.tools.Helper;
import ar.edu.itba.pod.simul.communication.MarketData;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageType;
import ar.edu.itba.pod.simul.communication.TransferHistory;
import ar.edu.itba.pod.simul.communication.payload.ResourceRequestPayload;
import ar.edu.itba.pod.simul.local.LocalMarket;
import ar.edu.itba.pod.simul.market.MarketManager;
import ar.edu.itba.pod.simul.market.Resource;
import ar.edu.itba.pod.simul.market.ResourceStock;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;

public class MarketImpl extends LocalMarket {

    private final static Logger LOGGER = Logger.getLogger(MarketImpl.class);
    
    private Multiset<Resource> colaExterna;
    private MarketManager manager;
    private double total;
    
    public MarketImpl() {
        super();
        colaExterna = ConcurrentHashMultiset.create();
        manager = new MarketManagerImpl(this);
    }

    public MarketData basicMarketData()
    {
        return super.marketData();
    }
    
    @Override
    public MarketData marketData() {

            MarketData myInfo = super.marketData();
            
            total = 0;
            
            Message message = new Message(NodeInitializer.getNodeId(),
                    Helper.GetNow(), MessageType.NODE_MARKET_DATA_REQUEST,
                    new NodeMarketDataRequestPayloadImpl());
            
            try {
                NodeInitializer.getConnection().getGroupCommunication().broadcast(message);
            } catch (RemoteException e) {
                LOGGER.error(e);
            }
            
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                LOGGER.error(e);
            }
            
            return new MarketData(
                    myInfo.getBuying(), 
                    myInfo.getSelling(), 
                    new TransferHistory(myInfo.getHistory().getHistoryItems(),
                            myInfo.getHistory().getTransactionsPerSecond() + total));
    }

    @Override
     public void matchBothEnds() {
             
        for (ResourceStock buyer : buying) {
            for (ResourceStock seller : selling) {
                    if (buyer.resource().equals(seller.resource())) {
                            transfer(buyer, seller);
                    }
            }
            
            // Busco sobras de una transferencia anterior en caso de no alcanzar
            if (buying.count(buyer) != 0) {
                
                for (Resource externo : colaExterna) {
                        if (buyer.resource().equals(externo)) {
                                externalTransfer(buyer, externo);
                        }
                }
                // Si falta, le pregunto a la cluster (mando broadcast)
                if (buying.count(buyer) != 0) {
                        pedirRecurso(buyer.resource(), buying.count(buyer));
                }
            }
        }
    } 
    
    private void pedirRecurso(Resource resource, int amount)
    {
        ResourceRequestPayload payload = new ResourceRequestPayloadImpl(amount, resource);
        Message message = new Message(NodeInitializer.getNodeId(), Helper.GetNow(), MessageType.RESOURCE_REQUEST, payload);
        try {
            NodeInitializer.getConnection().getGroupCommunication().broadcast(message);
        } catch (RemoteException e) {
            LOGGER.error(e);
        }
    }
    
    // Copio el transfer pero para la cola externa
    private Integer externalTransfer(ResourceStock buyer, Resource externo) {
        while (true) {
                int wanted = buying.count(buyer);
                int available = colaExterna.count(externo);
                int transfer = Math.min(available, wanted);

                if (transfer == 0) {
                        return 0;
                }

                boolean procured = colaExterna.setCount(externo, available, available - transfer);
                if (procured) {
                        boolean sent = buying.setCount(buyer, wanted, wanted - transfer);
                        if (sent) {
                                try {
                                        buyer.add(transfer);
                                } catch (RuntimeException e) {
                                        buying.remove(buyer, transfer);
                                        continue;
                                }
                                myLogTransfer(externo, buyer, transfer);
                                return transfer;
                        } else {
                                // Compensation. restore what we took from the order!
                            colaExterna.add(externo, transfer);
                        }
                }
                // Reaching here mean we hit a race condition. Try again.
        }
    }
    
    public void myLogTransfer(Resource from, ResourceStock to, int amount) {
        transactionCount++;
        System.out.printf("SELL: from remote agent to %s --> %d of %s\n", to.name(), amount, from);
    }
    
    public void addTransaction()
    {
       super.transactionCount++;
    }
    
    public void run()
    {
        super.run();
    }
     
    public void addTotal(double total)
    {
        this.total += total;
    }

    public Multiset<Resource> getColaExterna() {
        return colaExterna;
    }
    
    public Multiset<ResourceStock> getSelling() {
        return super.selling;
    }

    public MarketManager getManager() {
        return manager;
    }    
}
