package ar.edu.itba.pod.legajo47189;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.TimeUnit;

import ar.edu.itba.pod.legajo47189.communication.Impl.NodeInitializer;
import ar.edu.itba.pod.legajo47189.market.Impl.MarketImpl;
import ar.edu.itba.pod.legajo47189.market.Impl.MarketManagerImpl;
import ar.edu.itba.pod.legajo47189.simul.Impl.ObjectFactoryImpl;
import ar.edu.itba.pod.simul.ObjectFactory;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.local.LocalMarketManager;
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

public class EchoClient { 
	private static final String HOST = "localhost"; 

	private static ObjectFactory fac;
	static Resource pigIron = new Resource("Mineral", "Pig Iron");
	static Resource copper = new Resource("Mineral", "Copper");
	static Resource steel = new Resource("Alloy", "Steel");
	public static  SimulationManager simu;
	public static TimeMapper timeMapper = TimeMappers.oneSecondEach(6, TimeUnit.HOURS);
	
	/** 
	 * @param args 
	 */
	public static void main(final String[] args) {
		        System.setProperty("java.security.policy", "/Users/damian/Documents/workspace/RMI/file.policy");
		        System.setSecurityManager(new java.rmi.RMISecurityManager());
		        fac = new ObjectFactoryImpl();

			String command = "";
			while(true)
			{
			        try{
			            System.out.println("1-Conectarme a un nodo");
			            System.out.println("2-Crear grupo");
			    	    System.out.println("3-Agregar agente consumidor");
			    	    System.out.println("4-Agregar agente productor");
			    	    System.out.println("5-Agregar agente fabrica");
			    	    System.out.println("6-Desconectarme");
			    	    System.out.println("7-Buscar data");
			             command = command();
			             process(command);
			         }catch (Exception e) {
		                        e.printStackTrace();
		                }
			}		
	}
		
	private static void process(String command) throws IOException, NotBoundException 
	{
	    Integer commandId = Integer.parseInt(command);
	    switch(commandId)
	    {
	        case 1:
	            connnect();
	            break;
	        case 2:
	            create();
	            break;
	        case 3:
	            addConsumer();
	            break;
	        case 4:
	            addProducer();
	            break;
	        case 5:
                    addFactory();
                    break;
	        case 6:
	            removeNode();
                    break;
	        case 7:
                    getData();
                    break;
	        default:
	                System.out.println("Comando invalido");
	    }
        }
	
    private static void removeNode() throws IOException, NotBoundException 
    {
    	System.out.println("2-Remover nodo con id (host:port)");
        String command = command();
        String host = command.split(":")[0];
        Integer port = Integer.parseInt(command.split(":")[1]);
        ConnectionManager conn = getConnection("127.0.0.1", 1099);
        conn.getClusterAdmimnistration().disconnectFromGroup(command);
    }

    private static void connnect() throws IOException, NotBoundException 
    {
        FeedbackCallback callback = new ConsoleFeedbackCallback();
        System.out.println("1-Conectarse al grupo con id (host:port)");
        String command = command();
        System.out.println("1-Mi id es (host:port)");
        String command2 = command();
        fac.createConnectionManager(command2, command);
        simu = fac.getSimulationManager(null, timeMapper);
        simu =   new FeedbackSimulationManager(callback, simu);
        
        MarketImpl market = NodeInitializer.getMarketImpl();
        MarketManager marketManager = market.getManager();
        marketManager = new FeedbackMarketManager(callback, marketManager);
        marketManager.start();
        simu.register(Market.class, market);
    }
    
    private static void create() throws IOException, NotBoundException 
    {
        System.out.println("2-Mi id es (host:port)");
        String command2 = command();
        fac.createConnectionManager(command2);
        simu = fac.getSimulationManager(null, timeMapper);
        
        MarketImpl market = NodeInitializer.getMarketImpl();
        FeedbackCallback callback = new ConsoleFeedbackCallback();
        MarketManager marketManager = market.getManager();
        marketManager = new FeedbackMarketManager(callback, marketManager);
        marketManager.start();
        simu.register(Market.class, market);
    }
    
    private static void addProducer() throws IOException, NotBoundException 
    {
        Agent mine1 = SimpleProducer.named("pig iron mine")
        .producing(2).of(pigIron)
            .every(2, TimeUnit.HOURS)
                .build();
        simu.addAgent(mine1);
    } 
    
    private static void addConsumer() throws IOException, NotBoundException 
    {
        Agent factory = SimpleConsumer.named("factory")
        .consuming(10).of(pigIron)
            .every(2, TimeUnit.DAYS)
                .build();
        simu.addAgent(factory);
    } 
    
    private static void addFactory() throws IOException, NotBoundException 
    {
        Agent refinery = Factory.named("steel refinery")
        .using(5, copper)
            .producing(6, pigIron)
                .every(2, TimeUnit.DAYS)
                    .build();
        simu.addAgent(refinery);
    } 

    private static void getData() throws IOException, NotBoundException 
    {
        MarketManagerImpl manager = (MarketManagerImpl)fac.getMarketManager(null);
        Market market = manager.market();
        double transactions = market.marketData().getHistory().getTransactionsPerSecond();
        System.out.println("Hay un total de " + transactions + " transactions por segundo");
    }
    
    private static String command() throws IOException
	{
	    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));	    
	    return br.readLine().trim();
	}
    
    private static ConnectionManager getConnection(String host, int port) throws RemoteException, NotBoundException
    {
        Registry registry = LocateRegistry.getRegistry(host, port);
        ConnectionManager stub = (ConnectionManager) registry.lookup("ConnectionService");
        return stub;
    }
	
}