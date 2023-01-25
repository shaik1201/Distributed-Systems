import java.io.*;
import java.util.*;

public class ExManager {
    private String path;
    private int num_of_nodes;
    private Scanner initial_data;
    private Node[] network;
    private Server[] servers;

    public ExManager(String path) throws FileNotFoundException {
        this.path = path;
        initial_data = new Scanner(new File(path));
    }

    public Node get_node(int i) {
        return network[i - 1];
    }

    public int getNum_of_nodes() {
        return this.num_of_nodes;
    }

    public void update_edge(int id1, int id2, double weight) {
        network[id1 - 1].edge_update(id2, weight);
        network[id2 - 1].edge_update(id1, weight);
    }

    public void read_txt() {
        num_of_nodes = Integer.parseInt(initial_data.nextLine());
        this.network = new Node[num_of_nodes];
        this.servers = new Server[2*num_of_nodes];
        String line;
        while (initial_data.hasNextLine()) {
            line = initial_data.nextLine();
            if (line.equals("stop"))
                break;
            Node n = new Node(num_of_nodes, line);
            network[n.get_id() - 1] = n;
        }
    }
    public void terminate(){
        for (int i = 0; i < network.length; i++) {
            network[i].killClients();
        }
        for (int i = 0; i < network.length; i++) {
            network[i].killServers();
        }
    }

//  link state routing
    public void start() {
        boolean all_nodes_finished = false;
        ArrayList<Thread> threads = new ArrayList<>(num_of_nodes);
        boolean[] status = new boolean[num_of_nodes];

        for (int i = 0; i < num_of_nodes; i++) {
            network[i].finished = false;
            network[i].servers_clean();
        }

        for (int i = 0; i < num_of_nodes; i++) {
            Thread thread = new Thread(network[i]);
            thread.start();
            threads.add(thread);
            status[i] = false;
        }

        while(!all_nodes_finished) {
            boolean temp = true;
            for (int i = 0; i < num_of_nodes; i++) {
                status[i] = network[i].finished;
            }
            for (int i = 0; i < num_of_nodes; i++) {
                if (!status[i])
                    temp = false;
            }
            all_nodes_finished = temp;
        }
        for (int i = 0; i < network.length; i++) {
            network[i].killClients();
        }
    }
}
