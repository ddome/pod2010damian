package ar.edu.itba.pod.legajo47189.market.Impl;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.legajo47189.communication.Impl.NodeInitializer;
import ar.edu.itba.pod.legajo47189.payload.Impl.ResourceRequestPayloadImpl;
import ar.edu.itba.pod.legajo47189.tools.Helper;
import ar.edu.itba.pod.simul.communication.MarketData;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageType;
import ar.edu.itba.pod.simul.communication.TransferHistory;
import ar.edu.itba.pod.simul.communication.payload.ResourceRequestPayload;
import ar.edu.itba.pod.simul.local.LocalMarket;
import ar.edu.itba.pod.simul.market.Resource;
import ar.edu.itba.pod.simul.market.ResourceStock;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;

public class MarketImpl extends LocalMarket {

    private final static Logger LOGGER = Logger.getLogger(MarketImpl.class);
    
    private Multiset<Resource> colaExterna;
    
    public MarketImpl() {
        super();
        colaExterna = ConcurrentHashMultiset.create();
    }

    @Override
    public MarketData marketData() {

            double total = 0;
            MarketData myInfo = super.marketData();

            //MANDO BROADCAST
            
            return new MarketData(
                    myInfo.getBuying(), 
                    myInfo.getSelling(), 
                    new TransferHistory(myInfo.getHistory().getHistoryItems(),
                            myInfo.getHistory().getTransactionsPerSecond( ) + total));
    }

    @Override
    protected void matchBothEnds() {
     
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
                                LOGGER.info("ENCONTRADO EN LA COLA EXTERNA.");
                                Integer resp = externalTransfer(buyer, externo);
                                if (resp != 0) {
                                        LOGGER.info("SE TRANSFIRIERON:" + buyer.name()
                                                        + "<====" + resp + " de "
                                                        + buyer.resource());
                                }
                        }
                }

                // Si falta, le pregunto a la cluster (mando broadcast)
                
                if (buying.count(buyer) != 0) {
                        LOGGER.info("Pido recursos a la cluster");
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
                                return transfer;
                        } else {
                                // Compensation. restore what we took from the order!
                            colaExterna.add(externo, transfer);
                        }
                }
                // Reaching here mean we hit a race condition. Try again.
        }
    }

    public Multiset<Resource> getColaExterna() {
        return colaExterna;
    }
    
    public Multiset<ResourceStock> getSelling() {
        return super.selling;
    }    
}
