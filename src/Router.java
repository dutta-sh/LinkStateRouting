import java.util.*;

public class Router {
	private String id;
	private int lspSeq;
	private boolean isOn;
	private String networkName;
	private int costToNetwork;
	private int tick;

	private Map<String, Integer> processedLSPs = new LinkedHashMap<>();	//stores latest LSP from each origin
	private Collection<Edge> edges = new LinkedHashSet<>();				//create graph based on LSPs
	private Collection<Neighbor> neighbors = new ArrayList<>();			//neighbors from input file
	private Collection<RoutingTouple> routingTable;						//final routing table

	public Router(String id, String networkName, int costToNetwork) {
		this.id = id;
		this.networkName = networkName;
		this.costToNetwork = costToNetwork;
		initRoutingTable();
		startUp();
	}

	private void initRoutingTable() {	//only add own network to routing table to start with
		routingTable = new LinkedHashSet<>();
		RoutingTouple rt = new RoutingTouple();
		rt.network = networkName;
		rt.cost = costToNetwork;
		rt.link = id;
		routingTable.add(rt);
	}
	
	public void addNeighbor(String neighborId, int cost) {
		Neighbor n = new Neighbor();
		n.id = neighborId;
		n.cost = cost;
		neighbors.add(n);
		edges.add(new Edge(id, n.id, n.cost, Main.routers.get(n.id).networkName));
	}

	//create LSP from this router
	public void originatePacket() {
		if(!isOn)	//switched off
			return;

		tick++;
		lspSeq++;
		LSP lsp = new LSP(id, lspSeq);		//generate LSP
		for(Neighbor n : neighbors) {
			if(tick > n.tickForLSP + 2) {   //infinity for those who sent no LSP in 2 ticks
				n.cost = Main.INFINITY;
                edges.remove(new Edge(id, n.id, 0, ""));    //remove this edge from graph
			} else {                        //add to LSP this network details
				Edge l = new Edge(id, n.id, n.cost, Main.routers.get(n.id).networkName);
				lsp.addLink(l);
			}
		}
        computeShortestPaths();		//run djikstra algo to compute routing table
		sendPacket(lsp);			//send to all neighbors
	}

	//process received LSP
	public void receivePacket(LSP lsp) {
		if(!isOn)		//switched off
			return;

		for(Neighbor n : neighbors) {
			if(n.id.equals(lsp.sender))	//update for which tick LSP was received
				n.tickForLSP = tick;
		}
		lsp.ttl--;
		if(lsp.ttl <= 0 || lsp.originId.equals(id))	//LSP is dead or from this source in a loop
			return;

		Integer seq = processedLSPs.get(lsp.originId);
		if(seq != null && seq >= lsp.seq)	//already processed newer LSP for source
			return;

		processedLSPs.put(lsp.originId, lsp.seq);	//add/update to LSP list
		edges.addAll(lsp.links);		//update set of edges with latest info

		computeShortestPaths();			//run djikstra algo to compute routing table
		sendPacket(lsp);				//send to downstream neighbors
	}

	private void sendPacket(LSP lsp) {
		String sender = lsp.sender;
		lsp.sender = id;					//update sender ID to this router
		for(Neighbor n : neighbors) {
			if(n.cost != Main.INFINITY) {	//all connected routers for finite cost
				if(!n.id.equals(sender))	//except the sending one
					Main.routers.get(n.id).receivePacket(lsp);
			}
		}
	}

	//run dijkstra to populate routing table
	public void computeShortestPaths() {
		Set<String> destinations = new HashSet<>();
		for(Edge l : edges) {	                        //create edge from link info
			destinations.add(l.source);
			destinations.add(l.dest);
		}

		Dijkstra dijkstra = new Dijkstra(id, edges);	//pass start node and graph
		dijkstra.compute();								//run algo to compute shortest path
		initRoutingTable();								//reinitialize routing table
		for(String v : destinations) {
			String route = dijkstra.getCostAndRoute(v);	//in format: cost;p1,p2,p3....
			if(route != null) {
				String cost = route.split(";")[0];		//element before ;
				String[] path = (route.split(";")[1]).split(",");	//elements after ; delimited by ,
				Router dest = Main.routers.get(path[path.length - 1]);	//dest router is last on the array

				RoutingTouple rt = new RoutingTouple();
				rt.network = dest.networkName;
				rt.cost = Integer.parseInt(cost) + dest.costToNetwork;	//router to router + network cost
				rt.link = path[1];		//second hop on path is the outgoing link
				routingTable.add(rt);
			}
		}
	}

	public void startUp() {
	    if(isOn) {
            System.out.println("Router " + id + " is already ON");
        } else {
            isOn = true;
            System.out.println("Router " + id + " has been turned ON");
        }
	}

	public void shutDown() {
        if(!isOn) {
            System.out.println("Router " + id + " is already OFF");
        } else {
            isOn = false;
            System.out.println("Router " + id + " has been turned OFF");
        }
	}

	public String getRoutingTable() {
		StringBuilder strlbd = new StringBuilder("NetworkName, Cost, Link\r\n");
		for(RoutingTouple rt : routingTable) {
			strlbd.append(rt.network + ", " + rt.cost + ", " + rt.link + "\r\n");
		}
		return strlbd.toString();
	}
}