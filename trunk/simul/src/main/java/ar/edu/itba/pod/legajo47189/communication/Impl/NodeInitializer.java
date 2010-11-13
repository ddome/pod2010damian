package ar.edu.itba.pod.legajo47189.communication.Impl;

import java.rmi.RemoteException;

import ar.edu.itba.pod.legajo47189.architecture.Cluster;
import ar.edu.itba.pod.legajo47189.tools.Helper;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageType;

public class NodeInitializer {
    
    public static final String FilePolicyPath =
        "/Users/damian/Documents/workspace/RMI/file.policy";
    
    public static final String ClassesPath =
        "file:/Users/damian/Downloads/simul/target/classes/ar/edu/itba/pod/legajo47189/communication/Impl";
    
    private static String nodeId;
    public static String getNodeId()
    {
        return nodeId;
    }
    
    private static Cluster cluster;
    public static Cluster getCluster()
    {
        return cluster;
    }

    private static ConnectionManager connectionManager;
    public static ConnectionManager getConnection()
    {
        return connectionManager;
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
    
    private static void initialize(String initId, String initialNode, int port)
    {
        String id = initId;
        setInitialProperties();
        cluster = new Cluster();
        try {
            //TODO: Sacar esto!!!!
            ConnectionManagerImpl.ConnectionPort = port;
            
            connectionManager = new ConnectionManagerImpl();
            NodeInitializer.nodeId = id; 
            if (initialNode == null)
            {
                connectionManager.getClusterAdmimnistration().createGroup();
            }
            else
            {
                connectionManager.getClusterAdmimnistration().connectToGroup(initialNode);
            }
            
            sendRandomMessages();
                
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

    private static void sendRandomMessages()
    {
        while(true)
        {
            try {
                Thread.currentThread().sleep(5000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                System.out.println("Mando!");
                connectionManager.getGroupCommunication().broadcast(new Message(getNodeId(), Helper.GetNow(), MessageType.DISCONNECT, new PayloadImpl()) );
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
}
