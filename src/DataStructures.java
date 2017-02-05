import java.util.ArrayList;
import java.util.Collection;


//used as links between two routers and also on LSP
class Edge {
    String source;
    String dest;
    int cost;
    String network;

    public Edge(String source, String dest, int cost, String network) {
        this.source = source;
        this.dest = dest;
        this.cost = cost;
        this.network = network;
    }

    @Override
    public int hashCode() {
        return source.hashCode() + dest.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;

        //since undirected, so AB and BA is the same edge
        Edge other = (Edge) obj;
        if(source.equals(other.source) && dest.equals(other.dest))    return true;
        if(source.equals(other.dest) && dest.equals(other.source))    return true;
        return false;
    }
}

class LSP {
    String originId;
    int seq;
    int ttl;
    String sender; //so that router knows who sent it, and doesn't forward to that

    Collection<Edge> links = new ArrayList<>(); //store networks reachable by this LSP's source

    public LSP(String originId, int seq) {
        this.originId = originId;
        this.sender = originId;
        this.seq = seq;
        ttl = 10;
    }

    public void addLink(Edge l) {
        links.add(l);
    }
}

class Neighbor {	//hold neighboring router info
    String id;
    int cost;
    int tickForLSP;	//tick when last LSP was received from this guy
}

class RoutingTouple {//each row of routing table
    String network;
    int cost;
    String link;

    //network name is Primary Key in routing table, hence use only this for hashcode and equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoutingTouple that = (RoutingTouple) o;
        return network.equals(that.network);
    }

    @Override
    public int hashCode() {
        return network.hashCode();
    }
}