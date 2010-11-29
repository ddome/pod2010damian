package ar.edu.itba.pod.legajo47189.architecture;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Group implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 8895406076850572303L;

    private List<Node> nodes;
    
    private String groupId; 
 
    public Group(String groupId)
    {
        nodes = (List<Node>) Collections.synchronizedList(new ArrayList<Node>());
        this.groupId = groupId;
    }
    
    public Group(String groupId, Iterable<Node> nodes)
    {
        this.groupId = groupId;
        this.nodes = new ArrayList<Node>();
        for (Node node : nodes)
        {
            add(node);
        }
    }
    
    public int size()
    {
        return nodes.size();
    }
    
    public void add(Iterable<Node> nodes)
    {
        for (Node node : nodes)
        {
            this.nodes.add(node);
        }
    }
    
    public void add(Node node)
    {
        this.nodes.add(node);
    }
    
    public void remove(Node node)
    {
        this.nodes.remove(node);
    }
    
    public void remove(String nodeId)
    {
        for (Node node : nodes)
        {
            if (node.getNodeId().equals(nodeId))
            {
                nodes.remove(node);
                break;
            }
        }
    }
    
    public List<Node> getNodes()
    {
        return nodes; 
    }

    public String getGroupId() {
        return groupId;
    }
}
