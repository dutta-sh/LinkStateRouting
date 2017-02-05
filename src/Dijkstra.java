import java.util.*;

class Dijkstra {
    private Set<Edge> edges = new HashSet<>();      		//all edges of the graph
    private Set<String> settled = new HashSet<>();  		//computed edges
    private Set<String> unSettled = new HashSet<>();		//pending edges
    private Map<String, String> prevMap = new HashMap<>();  //backtrack to source
    private Map<String, Integer> distMap = new HashMap<>(); //distance map

    //initialize source node and graph edges
    public Dijkstra(String source, Collection<Edge> edges) {
        distMap.put(source, 0);
        unSettled.add(source);
        this.edges.addAll(edges);
    }

    //compute shortest path until all nodes are settled
    public void compute() {
        while (unSettled.size() > 0) {
            String v = getMin(unSettled);
            settled.add(v);
            unSettled.remove(v);
            computeMinDist(v);
        }
    }

    private String getMin(Set<String> vertices) {
        String min = null;
        for (String v : vertices) {
            if (min == null)
                min = v;
            else if (getShortestDistance(min) > getShortestDistance(v))
                min = v;
        }
        return min;
    }

    //if no route exists, set to infinity
    private int getShortestDistance(String dest) {
        return distMap.get(dest) == null ? Main.INFINITY : distMap.get(dest);
    }

    //get minimum distance to destination
    private void computeMinDist(String node) {
        List<String> neighbors = new ArrayList<>();
        for (Edge edge : edges) {
            if (edge.source.equals(node) && !settled.contains(edge.dest)) {
                neighbors.add(edge.dest);
            }
            if (edge.dest.equals(node) && !settled.contains(edge.source)) {
                neighbors.add(edge.source);
            }
        }
        for (String v : neighbors) {
            if (getShortestDistance(v) > getShortestDistance(node) + getDistance(node, v)) {
                distMap.put(v, getShortestDistance(node) + getDistance(node, v));
                prevMap.put(v, node);
                unSettled.add(v);
            }
        }
    }

    //since BA and AB is same, compare either way
    private int getDistance(String node, String target) {
        for (Edge edge : edges) {
            if (edge.source.equals(node) && edge.dest.equals(target))
                return edge.cost;
            if (edge.source.equals(target) && edge.dest.equals(node))
                return edge.cost;
        }
        return Main.INFINITY;
    }

    //returns in format -> cost to destination;path[p1,p2,p3.....]
    public String getCostAndRoute(String dest) {
        if (prevMap.get(dest) == null) //check if a path exists
            return null;

        String step = dest;
        LinkedList<String> path = new LinkedList<>();
        path.add(step);
        Integer cost = distMap.get(step);    //cumulative cost to router, so take first one
        while (prevMap.get(step) != null) {
            step = prevMap.get(step);
            path.add(step);
        }
        Collections.reverse(path);          					// put in correct order
        StringBuilder strBld = new StringBuilder(cost + ";");   //add cost
        for(String v : path) {
            strBld.append(v).append(",");   					//add route
        }
        return strBld.toString(); 								//return cost and route
    }
}