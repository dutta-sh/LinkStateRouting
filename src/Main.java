import java.io.File;
import java.util.*;

public class Main {

	public static Integer INFINITY = Integer.MAX_VALUE;
	public static Map<String, Router> routers = new LinkedHashMap<>();	//all routers in the graph
	
	public static void main(String[] args) {
        List<String> lines = new ArrayList<>();
		try {
            Scanner sc = new Scanner(new File("infile.dat"));		//read input file
            while(sc.hasNext()) {
                String l = sc.nextLine();
                lines.add(l);
            }
            sc.close();
		} catch (Exception e) {
			System.out.println("File Not Found");
			return;
		}

		//first initialize only routers
		for(String l : lines) {
            String[] line = l.split("[\\s]+");    //ignore all trailing and in-between space and tabs
            if (!line[0].equals("")) {            //[router-id] [network-name] [network-cost]
                String id = line[0];
                String networkName = line[1];
                int costToNetwork = line.length < 3 ? 1 : Integer.parseInt(line[2]);    //default cost = 1
                Router r = new Router(id, networkName, costToNetwork);
                routers.put(id, r);
            }
        }

        //now initialize neighborhoods for each
        String id = null;
        for(String l : lines) {
            String[] line = l.split("[\\s]+");    //ignore all trailing and in-between space and tabs
            if (!line[0].equals("")) {            //[router-id] [network-name] [network-cost]
                id = line[0];
            } else {                              //[] [directly-linked-router-id] [link-cost]
                String neighborId = line[1];
                int costToNeighbor = line.length < 3 ? 1 : Integer.parseInt(line[2]);    //default cost = 1
                routers.get(id).addNeighbor(neighborId, costToNeighbor);
            }
        }

		//user instructions
		System.out.println("To continue, enter C");
		System.out.println("To quit, enter Q");
		System.out.println("To print the routing table of a router, enter P followed by router id");
		System.out.println("To shut down a router, enter S followed by router id");
		System.out.println("To start up a router, enter T followed by router id");
		System.out.print("Enter choice: ");

		Scanner sc = new Scanner(System.in);
		String ip;
		while(!(ip = sc.nextLine().trim()).equals("Q")) {
			if(ip.equals("C")) {								//release packets to neighbors
//				for(Integer i = 6; i >=0; i--)
//					routers.get(i.toString()).originatePacket();
				for(Router t : routers.values())
					t.originatePacket();
			} else if(ip.startsWith("P") || ip.startsWith("S") || ip.startsWith("T")) {
				Router rt = routers.get(ip.substring(1));
				if(rt == null) {
					System.out.println("Invalid router id !!");
				} else if(ip.startsWith("P")) {					//print routing table
					System.out.println(rt.getRoutingTable());
				} else if(ip.startsWith("S")) {					//stop a router
					rt.shutDown();
				} else if(ip.startsWith("T")) {					//start a router
					rt.startUp();
				}
			} else {
				System.out.println("Invalid input !!");
			}
			System.out.print("Enter choice: ");
		}
		sc.close();
		System.out.println("Bye !!");
	}
}