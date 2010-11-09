package ar.edu.itba.pod.legajo47189.communication.Impl;

import java.rmi.RemoteException;

import ar.edu.itba.pod.legajo47189.architecture.Cluster;
import ar.edu.itba.pod.legajo47189.architecture.Node;
import ar.edu.itba.pod.simul.communication.ConnectionManager;

public class NodeInitializer {
    
    public static final String FilePolicyPath =
        "/Users/damian/Documents/workspace/RMI/file.policy";
    
    public static final String ClassesPath =
        "file:/Users/damian/Downloads/simul/target/classes/ar/edu/itba/pod/legajo47189/communication/Impl";
    
    private static Node me;
    public static Node getNode()
    {
        return me;
    }
    
    public static void main(String[] args) {
        String nodeId = null;
        String initialNode = null;
        Integer port = 1099;
        if (args.length >= 1)
        {
            nodeId = args[0];
        }
        if (args.length >= 3)
        {
            initialNode = args[2];
        }
        if (args.length >= 2)
        {
            port = Integer.parseInt(args[1]);
        }
        initialize(nodeId, initialNode, port);   
    }
    
    private static ConnectionManager connectionManager;
    public static ConnectionManager getConnection()
    {
        return connectionManager;
    }
    
    private static void initialize(String initId, String initialNode, int port)
    {
        String id = initId;
        setInitialProperties();
        try {
            //TODO: Sacar esto!!!!
            ConnectionManagerImpl.ConnectionPort = port;
            connectionManager = new ConnectionManagerImpl();
            me = new Node(id, connectionManager);
            if (initialNode == null)
            {
                connectionManager.getClusterAdmimnistration().createGroup();
            }
            else
            {
                connectionManager.getClusterAdmimnistration().connectToGroup(initialNode);
            }
                
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    
    private static void setInitialProperties()
    {
        System.setProperty("java.security.policy", FilePolicyPath);
        System.setProperty("java.rmi.server.codebase", ClassesPath);
        System.setSecurityManager(new java.rmi.RMISecurityManager());
    }

}
