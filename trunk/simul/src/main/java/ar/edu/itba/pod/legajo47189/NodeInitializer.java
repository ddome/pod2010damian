package ar.edu.itba.pod.legajo47189;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import ar.edu.itba.pod.legajo47189.architecture.Cluster;
import ar.edu.itba.pod.legajo47189.architecture.Node;
import ar.edu.itba.pod.legajo47189.tools.Helper;
import ar.edu.itba.pod.simul.communication.ConnectionManager;

public class NodeInitializer {
    
    public static final String FilePolicyPath =
        "/Users/damian/Documents/workspace/RMI/file.policy";
    
    public static final String ClassesPath =
        "file:/Users/damian/Downloads/simul/target/classes/ar/edu/itba/pod/legajo47189/communication";

    private static Cluster cluster;
    public static Cluster getCluster()
    {
        return cluster;
    }
    
    private static Node me;
    public static Node getNode()
    {
        return me;
    }
    
    public static void main(String[] args) {
        Integer nodeId = null;
        if (args.length > 1)
        {
            nodeId = Integer.parseInt(args[0]);
        }
        initialize(nodeId);
    }
    
    private static ConnectionManager connectionManager;
    public static ConnectionManager getConnection()
    {
        return connectionManager;
    }
    
    private static void initialize(Integer initId)
    {
        String id = initId.toString();
        setInitialProperties();
        try {
            connectionManager = new ConnectionManagerImpl();
            cluster = new Cluster();
            me = new Node(id, connectionManager);
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
