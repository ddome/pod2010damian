package ar.edu.itba.pod.legajo47189;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream.GetField;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import ar.edu.itba.pod.legajo47189.communication.Impl.PayloadImpl;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageListener;
import ar.edu.itba.pod.simul.communication.MessageType;

/** 
 * Client for Echo server 
 * @author POD 
 * since May 16, 2010 
 */ 

public class EchoClient { 
	private static final String HOST = "localhost"; 
	
	/** 
	 * @param args 
	 */
	public static void main(final String[] args) {
		        System.setProperty("java.security.policy", "/Users/damian/Documents/workspace/RMI/file.policy");
		        System.setSecurityManager(new java.rmi.RMISecurityManager());
		    
			String command = "";
			while(true)
			{
			        try{
			        	System.out.println("1-Agregar nodo");
			    	    System.out.println("2-Remover nodo");
			    	    System.out.println("3-Agregar agente");
			    	    System.out.println("4-Remover agente");
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
	            addNode();
	            break;
	        case 2:
	            removeNode();
	            break;
	        case 3:
	        case 4:
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

    private static void addNode() throws IOException, NotBoundException 
    {
    	System.out.println("1-Agregar nodo con id (host:port)");
        String command = command();
        String host = command.split(":")[0];
        Integer port = Integer.parseInt(command.split(":")[1]);
        ConnectionManager conn = getConnection(host, port);
        conn.getClusterAdmimnistration().connectToGroup("127.0.0.1:1099");
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