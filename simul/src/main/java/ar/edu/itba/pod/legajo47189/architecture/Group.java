package ar.edu.itba.pod.legajo47189.architecture;

import java.util.ArrayList;
import java.util.List;

public class Group {

    private List<Node> nodes;
    
    private String groupId; 
 
    public Group(String groupId)
    {
        nodes = new ArrayList<Node>();
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
    
    public void add(Node node)
    {
        this.nodes.add(node);
    }
    
    public void remove(Node node)
    {
        this.nodes.remove(node);
    }
    
    public List<Node> getNodes()
    {
        return nodes; 
    }

    public String getGroupId() {
        return groupId;
    }
}
