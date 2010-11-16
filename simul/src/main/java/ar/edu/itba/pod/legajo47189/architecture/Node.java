package ar.edu.itba.pod.legajo47189.architecture;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ar.edu.itba.pod.simul.communication.ConnectionManager;

public class Node implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = -5324801407930003196L;

    public static List<String> GetIdList(List<Node> nodes)
    {
        List<String> ret = new ArrayList<String>();
        for (Node node : nodes)
        {
            ret.add(node.nodeId);
        }
        return ret;
    }
    
    private String nodeId;
    
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeId() {
        return nodeId;
    }
    
    private ConnectionManager connectionManager;
    
    public void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }
    
    public Node(String nodeId, ConnectionManager connectionManager)
    {
        setNodeId(nodeId);
        setConnectionManager(connectionManager);
    }
    
    public Node(String nodeId)
    {
        setNodeId(nodeId);
        setConnectionManager(connectionManager);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((nodeId == null) ? 0 : nodeId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Node other = (Node) obj;
        if (nodeId == null) {
            if (other.nodeId != null)
                return false;
        } else if (!nodeId.equals(other.nodeId))
            return false;
        return true;
    }
}
