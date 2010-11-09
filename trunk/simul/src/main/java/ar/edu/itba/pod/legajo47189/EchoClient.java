package ar.edu.itba.pod.legajo47189;

import java.rmi.registry.LocateRegistry; 
import java.rmi.registry.Registry;

import ar.edu.itba.pod.legajo47189.communication.Impl.PayloadImpl;
import ar.edu.itba.pod.simul.communication.Message;

import ar.edu.itba.pod.simul.communication.ConnectionManager;
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
		try{
		        System.setProperty("java.security.policy", "/Users/damian/Documents/workspace/RMI/file.policy");
		        //System.setProperty("java.rmi.server.codebase", "/Users/damian/Documents/workspace/RMI/bin/Ejemplo");
		        
		        System.setSecurityManager(new java.rmi.RMISecurityManager());
		    
			final Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1099);
			ConnectionManager stub = (ConnectionManager) registry.lookup("ConnectionService");
			int response = stub.getClusterPort();
			
			MessageListener listener = stub.getGroupCommunication().getListener();
			listener.onMessageArrive(new Message("pepito", 1, MessageType.DISCONNECT, new PayloadImpl()));
			System.out.println("response: " + response);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}