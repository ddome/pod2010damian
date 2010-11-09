package ar.edu.itba.pod.legajo47189.architecture;

public class Cluster {

    private String clusterId;
    
    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getClusterId() {
        return clusterId;
    }
    
    public Cluster()
    {
        this.clusterId = "1";
    }

    private Group group;
    
    public void setGroup(Group group) {
        this.group = group;
    }

    public Group getGroup() {
        return group;
    }
}
