package ar.edu.itba.pod.legajo47189;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.TimeUnit;

import ar.edu.itba.pod.legajo47189.communication.Impl.NodeInitializer;
import ar.edu.itba.pod.legajo47189.market.Impl.MarketImpl;
import ar.edu.itba.pod.legajo47189.market.Impl.MarketManagerImpl;
import ar.edu.itba.pod.legajo47189.simul.Impl.ObjectFactoryImpl;
import ar.edu.itba.pod.legajo47189.tools.Helper;
import ar.edu.itba.pod.simul.ObjectFactory;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.market.Market;
import ar.edu.itba.pod.simul.market.MarketManager;
import ar.edu.itba.pod.simul.market.Resource;
import ar.edu.itba.pod.simul.simulation.Agent;
import ar.edu.itba.pod.simul.simulation.SimulationManager;
import ar.edu.itba.pod.simul.time.TimeMapper;
import ar.edu.itba.pod.simul.time.TimeMappers;
import ar.edu.itba.pod.simul.ui.ConsoleFeedbackCallback;
import ar.edu.itba.pod.simul.ui.FeedbackCallback;
import ar.edu.itba.pod.simul.ui.FeedbackMarketManager;
import ar.edu.itba.pod.simul.ui.FeedbackSimulationManager;
import ar.edu.itba.pod.simul.units.Factory;
import ar.edu.itba.pod.simul.units.SimpleConsumer;
import ar.edu.itba.pod.simul.units.SimpleProducer;

/** 
 * Client for Echo server 
 * @author POD 
 * since May 16, 2010 
 */ 

public class Simulation1 { 
        private static final String HOST = "localhost"; 

        private static ObjectFactory fac;
        static Resource pigIron = new Resource("Mineral", "Pig Iron");
        static Resource copper = new Resource("Mineral", "Copper");
        static Resource steel = new Resource("Alloy", "Steel");
        static Resource gold = new Resource("Alloy", "Gold");
        static Resource silver = new Resource("Alloy", "Silver");
        public static  SimulationManager simu;
        public static TimeMapper timeMapper = TimeMappers.oneSecondEach(6, TimeUnit.HOURS);
        
        /** 
         * @param args 
         */
        public static void main(final String[] args) {
                        System.setProperty("java.security.policy", "/Users/damian/Documents/workspace/RMI/file.policy");
                        System.setSecurityManager(new java.rmi.RMISecurityManager());
                        fac = new ObjectFactoryImpl();

                        
                            connect();
                            try {
								Thread.sleep(5000);
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
                   
                        
                        //    addProducer(pigIron, "p1");
                          //  addProducer(copper, "p2");
                            //addFactory(pigIron, copper, steel, "f1");
                            //addFactory(steel, copper, silver, "f2");
                            //addConsumer(silver, "c1");
     

                        Thread addagents = new Thread(){ 
	                        	public void run()
	                        	{
	                        		int turno = 0;
	                                int number = 1;
	                        		while(true)
	                        		{
	                        			
										if (turno == 0)
										{
											addProducer(pigIron, "p" + number);
											turno++;
										}
										else if (turno == 1)
										{
											addProducer(copper,"p" +  number);
											turno++;
										}
										else if (turno == 2)
										{
											addFactory(pigIron, copper, steel, "f" + number);
											turno ++;
										}
										else if (turno == 3)
										{
											addFactory(steel, copper, silver, "f" + number);
											turno ++;
										}
										else if (turno == 4)
										{
											addConsumer(silver, "c" + number);
											turno = 0;
										}
										number++;
										try {
											Thread.sleep(10000);
										} catch (InterruptedException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
	                        			
	                        		}
	                        	} 
                        	};
                        	addagents.start();
                        
                        FileOutputStream fout = null;
                        try {
                            fout = new FileOutputStream ("/Users/damian/Downloads/output2.txt");
                        } catch (FileNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        double trans = 0;
                        do
                        {
                             trans = getData();
                             System.out.println("Transacciones " + trans);
                            new PrintStream(fout).println (trans);
                            try {
								fout.flush();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

                        }while(true);
        }
        
    private static void create() throws IOException, NotBoundException 
    {
        String command2 = "127.0.0.1:1099";
        fac.createConnectionManager(command2);
        simu = fac.getSimulationManager(null, timeMapper);
        
        MarketImpl market = NodeInitializer.getMarketImpl();
        FeedbackCallback callback = new ConsoleFeedbackCallback();
        MarketManager marketManager = market.getManager();
        marketManager = new FeedbackMarketManager(callback, marketManager);
        marketManager.start();
        simu.register(Market.class, market);
    }
    
    private static void connect()
    {
        FeedbackCallback callback = new ConsoleFeedbackCallback();
        String command = "127.0.0.1:1099";
        String command2 = "127.0.0.1:1098";
        fac.createConnectionManager(command2, command);
        simu = fac.getSimulationManager(null, timeMapper);
        simu =   new FeedbackSimulationManager(callback, simu);
        
        MarketImpl market = NodeInitializer.getMarketImpl();
        MarketManager marketManager = market.getManager();
        marketManager = new FeedbackMarketManager(callback, marketManager);
        marketManager.start();
        simu.register(Market.class, market);
    }
    
    private static void addProducer(Resource resource, String name)
    {
        Agent mine1 = SimpleProducer.named(name)
        .producing(2).of(resource)
            .every(1, TimeUnit.HOURS)
                .build();
        simu.addAgent(mine1);
    } 
    
    private static void addConsumer(Resource resource, String name)
    {
        Agent factory = SimpleConsumer.named(name)
        .consuming(10).of(resource)
            .every(1, TimeUnit.DAYS)
                .build();
        simu.addAgent(factory);
    } 
    
    private static void addFactory(Resource resource1, Resource resource2, Resource resource3, String name)
    {
        Agent refinery = Factory.named(name)
        .using(5, resource1).and(5, resource2)
            .producing(10, resource3)
                .every(1, TimeUnit.DAYS)
                    .build();
        simu.addAgent(refinery);
    } 

    private static double getData() 
    {
        MarketManagerImpl manager = (MarketManagerImpl)fac.getMarketManager(null);
        Market market = manager.market();
        double transactions = market.marketData().getHistory().getTransactionsPerSecond();
        return transactions;
    }
    
    private static ConnectionManager getConnection(String host, int port) throws RemoteException, NotBoundException
    {
        Registry registry = LocateRegistry.getRegistry(host, port);
        ConnectionManager stub = (ConnectionManager) registry.lookup("ConnectionService");
        return stub;
    }
        
}