import java.io.*;
import java.net.*;
import java.util.*;


public class ExManager {
    private String path;
    private int num_of_nodes;
    ArrayList<Node> nodes = new ArrayList<>();

    // your code here

    public ExManager(String path) throws FileNotFoundException {
        this.path = path;
        // your code here
    }

    public Node get_node(int id) {
        // your code here
        return this.nodes.get(id - 1); // check if this is the right index
    }

    public int getNum_of_nodes() {
        return this.num_of_nodes;
    }

    public void update_edge(int id1, int id2, double weight) {
        //your code here
        Node node1 = get_node(id1);
        Node node2 = get_node(id2);
        node1.adj_matrix[id1 - 1][id2 - 1] = weight;
        node2.adj_matrix[id2 - 1][id1 - 1] = weight;
    }

    public void read_txt() throws IOException {
        Scanner scanner = new Scanner(new File(this.path));
        this.num_of_nodes = Integer.parseInt(scanner.nextLine());

        // create ArrayList of all graph nodes
//        Node[] nodes = new Node[this.num_of_nodes]; // need to be property of ExManager!?
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.contains("stop")) break;
//            System.out.println(line);
            Node node = createNodeFromLine(line);
            this.nodes.add(node);
        }
        for (Node node : this.nodes) {
            node.createNeighboursList(this.nodes);
        }
    }

    public void start() {
        // your code here
        boolean[] is_finish = new boolean[num_of_nodes];
        boolean all_nodes_finished = false;
        for (int i = 0; i < num_of_nodes; i++) {
            this.nodes.get(i).finished = false;
            this.nodes.get(i).serversClean();
        }

        for (int i = 0; i < num_of_nodes; i++) {
            Thread thread = new Thread(this.nodes.get(i));
            thread.start();
            is_finish[i] = false;
        }

        while(!all_nodes_finished) {
            boolean check = true;
            for (int i = 0; i < num_of_nodes; i++) {
                is_finish[i] = this.nodes.get(i).finished;
            }
            for (int i = 0; i < num_of_nodes; i++) {
                if (!is_finish[i])
                    check = false;
            }
            all_nodes_finished = check;
        }
        for (int i = 0; i < this.num_of_nodes; i++) {
            this.nodes.get(i).terminateClients();
        }
//
//
//        for (Node node : this.nodes) {
//            if (!node.isStarted()) {
//                node.start();
//            }
//        }
    }

    public void terminate() {
        for (int i = 0; i < this.num_of_nodes; i++) {
            this.nodes.get(i).terminateServers();
            this.nodes.get(i).terminateClients();
        }
        for (int i = 0; i < this.num_of_nodes; i++) {
            int n = this.nodes.get(i).serversThreads.size();
            for (int j = 0; j < n; j++) {
                this.nodes.get(i).serversThreads.get(j).interrupt();
            }
        }
    }

    public Node createNodeFromLine(String line) throws IOException {
        String[] strArray = line.split(" ");
        int id = Integer.parseInt(strArray[0]);
        return new Node(id, getNum_of_nodes(), strArray);
    }

}

class Node extends Thread {
    public int id;
    boolean started = false;
    public int number_graph_nodes;
    int number_of_neighbours;
    int [] neighbours_ids;
    int [] adj_ports_in_matrix;
    int [] adj_ports_out_matrix;
    Double [][] adj_matrix;
    List<Socket> sockets;
    List<Pair> serverNodes;
    List<Thread> serversThreads;
    List<Node> neighbours_list;
    ArrayList<Pair<Integer, ClientNode>> clients;
    public boolean finished;


    public Node(int id, int number_graph_nodes, String[] strArray) throws IOException {
        this.id = id;
        this.number_graph_nodes = number_graph_nodes;
        this.number_of_neighbours = (strArray.length - 1) / 4;
        this.neighbours_ids = getNeighboursIds(strArray);
        this.adj_matrix = new Double[number_graph_nodes][number_graph_nodes];
        this.adj_matrix = updateAdjMatrix(id, strArray);
        this.adj_ports_in_matrix = updatePortsIn(id, strArray);
        this.adj_ports_out_matrix = updatePortsOut(id, strArray);
        this.neighbours_list = new ArrayList<>();
        this.sockets = new ArrayList<>();
        this.serverNodes = new ArrayList<>();
        this.serversThreads = new ArrayList<>();

        // servers init
        try {
            for (int i = 0; i < this.number_of_neighbours; i++) {
                int n_id = this.neighbours_ids[i];
                int port_in = this.adj_ports_in_matrix[n_id-1];
                ServerSocket s = new ServerSocket(port_in);
                ServerNode serverNode = new ServerNode(s, this.id);
                Thread t = new Thread(serverNode);
                t.start();
                this.serversThreads.add(t);
                this.serverNodes.add(new Pair(n_id, serverNode));
        }

        } catch (BindException e) {
//            boolean closed = false;
//            while (!closed) {
//                try {
//                    for (int i = 0; i < this.number_of_neighbours; i++) {
//                        int n_id = this.neighbours_ids[i];
//                        int port_in = this.adj_ports_in_matrix[n_id-1];
//                        ServerSocket s = new ServerSocket(port_in);
//                        s.setReuseAddress(true);
//                        ServerNode serverNode = new ServerNode(s, this.id);
//                        Thread t = new Thread(serverNode);
//                        t.start();
//                        this.serversThreads.add(t);
//                        this.serverNodes.add(new Pair(n_id, serverNode));
//                        closed = true;
//                    }
//                } catch (BindException e1) {
//                    System.out.println("fail");
//                }
//            }
        }

    }

    public Double[][] updateAdjMatrix(int id, String[] strArray) {
        int N = this.number_graph_nodes;
        Double [][] adj_matrix = new Double[N][N];
        int[] neighbours_ids = getNeighboursIds(strArray);
        double[] neighbours_weights = getNeighboursWeights(strArray);

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                adj_matrix[i][j] = -1.0;
            }
        }

        int length = neighbours_ids.length;
        for (int i = 0; i < length; i++) {
            int neighbour_id = neighbours_ids[i];
            double neighbour_weight = neighbours_weights[i];
            adj_matrix[id-1][neighbour_id-1] = neighbour_weight;
        }
        return adj_matrix;
    }

    public int[] updatePortsIn(int id, String[] strArray) {
        int N = this.number_graph_nodes;
        int [] adj_ports_in_matrix = new int[N];
        int[] neighbours_ids = getNeighboursIds(strArray);
        int[] neighbours_ports_in = getNeighboursPortsIn(strArray);
        int length = neighbours_ids.length;
        for (int i = 0; i < length; i++) {
            int neighbour_id = neighbours_ids[i];
            int neighbour_port_in = neighbours_ports_in[i];
            adj_ports_in_matrix[neighbour_id-1] = neighbour_port_in;
        }
        return adj_ports_in_matrix;
    }

    public int[] updatePortsOut(int id, String[] strArray) {
        int N = this.number_graph_nodes;
        int [] adj_ports_out_matrix = new int[N];
        int[] neighbours_ids = getNeighboursIds(strArray);
        int[] neighbours_ports_out = getNeighboursPortsOut(strArray);
        int length = neighbours_ids.length;
        for (int i = 0; i < length; i++) {
            int neighbour_id = neighbours_ids[i];
            int neighbour_port_out = neighbours_ports_out[i];
            adj_ports_out_matrix[neighbour_id-1] = neighbour_port_out;
        }
        return adj_ports_out_matrix;
    }

    public int[] getNeighboursIds(String[] strArray) {
        int[] neighbours_ids = new int[this.number_of_neighbours];
        int n = strArray.length;
        int j = 0;
        for (int i = 1; i < n; i+=4) {
            neighbours_ids[j] = Integer.parseInt(strArray[i]);
            j++;
        }
        return neighbours_ids;
    }

    public double[] getNeighboursWeights(String[] strArray) {
        double[] neighbours_weights = new double[this.number_of_neighbours];
        int n = strArray.length;
        int j = 0;
        for (int i = 2; i < n; i += 4) {

            neighbours_weights[j] = Double.parseDouble(strArray[i]);
            j++;
        }
        return neighbours_weights;
    }

    public int[] getNeighboursPortsIn(String[] strArray) {
        int[] neighbours_ports_in = new int[this.number_of_neighbours];
        int n = strArray.length;
        int j = 0;
        for (int i = 4; i < n; i += 4) {
            neighbours_ports_in[j] = Integer.parseInt(strArray[i]);
            j++;
        }
        return neighbours_ports_in;
    }

    public int[] getNeighboursPortsOut(String[] strArray) {
        int[] neighbours_ports_out = new int[this.number_of_neighbours];
        int n = strArray.length;
        int j = 0;
        for (int i = 3; i < n; i += 4) {
            neighbours_ports_out[j] = Integer.parseInt(strArray[i]);
            j++;
        }
        return neighbours_ports_out;
    }

    public void createNeighboursList(List<Node> nodes) {
        int[] neighbours_ids = this.neighbours_ids;
        for (int n_id: neighbours_ids) {
            for (Node node: nodes) {
                if (n_id == node.id) {
                    this.neighbours_list.add(node);
                }
            }
        }
    }

    public void print_graph() {
        for (int i = 0; i < this.number_graph_nodes; i++) {
            for (int j = 0; j < this.number_graph_nodes; j++) {
                System.out.print(this.adj_matrix[i][j]);
                if (j < this.number_graph_nodes -1)
                    System.out.print(", ");
            }
            System.out.println();
        }
    }


    @Override
    public void run() {
        // listen on every port in adj_ports_in_matrix
        // need to listen in same time --> each listening happens on
        // a separate thread

        // initialize all clients sockets for sending to neighbors
        clientsInit();

        // for checking if all nodes finish
        Map<Integer, Boolean> finished_map = new HashMap<>();
        for (int i = 0; i < this.number_graph_nodes+1; i++)
            finished_map.put(i, false);
        finished_map.put(this.id, true);
        finished_map.put(0, true); // dont care about id = 0

        // create the message to send
        Map<Integer, Double[]> message = new HashMap<>();
        message.put(this.id, this.adj_matrix[this.id-1]);

        // send message to neighbors. (id, adj_matrix)
        sendMessageToClients(message);

        while (finished_map.containsValue(false)) {
            Map<Integer, Double[]> messagesFromNeighbors = getMessagesFromNeighbors();
            Map<Integer, Double[]> messageToSend = new HashMap<>();
            for (Integer message_origin_id: messagesFromNeighbors.keySet()) {
                if (!finished_map.get(message_origin_id)) {
                    messageToSend.put(message_origin_id, messagesFromNeighbors.get(message_origin_id));
                    Double[] neighbor_adj_mat_row = messagesFromNeighbors.get(message_origin_id);
                    for (int i = 0; i < this.number_graph_nodes; i++)
                        updateMatrix(message_origin_id, i + 1, neighbor_adj_mat_row[i]);
                    finished_map.put(message_origin_id, true);
                }
            }
            if(messageToSend.size() > 0)
                sendMessageToClients(messageToSend);
        }
        this.finished = true;

    }


    public void clientsInit() {
        // init all clients need to send them
        clients = new ArrayList<>();
        for(Node neighbor : this.neighbours_list){
            try {
                int port_out = this.adj_ports_out_matrix[neighbor.id-1];
                Socket socket = new Socket("localhost", port_out);
                ClientNode c = new ClientNode(socket, id);
                clients.add(new Pair(neighbor.id, c));
            } catch (IOException ignored){
            }
        }
    }

    public void sendMessageToClients(Map<Integer, Double[]> message) {
        // message is adj matrix row
        for (int i = 0; i < this.clients.size(); i++) {
            clients.get(i).getValue().sendMessage(message);
        }
    }

    public Map<Integer, Double[]> getMessagesFromNeighbors(){
        Map<Integer, Double[]> m = new HashMap<>();
        for(int i = 0; i < this.serverNodes.size(); i ++){
            if(!((ServerNode) this.serverNodes.get(i).getValue()).messagesQueue.isEmpty())
                m.putAll(((ServerNode) this.serverNodes.get(i).getValue()).messagesQueue.remove());
        }
        return m;
    }

//    public boolean isStarted() {
//        return started;
//    }

    /*public void start() {
        this.started = true;
        super.start();
    }*/

    public void updateMatrix(int i, int j, Double weight) {

        this.adj_matrix[i - 1][j - 1] = weight;

    }

    public void terminateClients() {
        for (Pair p : this.clients) {
            ((ClientNode) p.getValue()).closeConnections();
        }
    }
    public void terminateServers(){
        for(Pair p: this.serverNodes) {
            ((ServerNode)p.getValue()).closeConnections();
        }
    }

    public void serversClean(){
        for(int i = 0; i < this.serverNodes.size(); i ++){
            ((ServerNode)this.serverNodes.get(i).getValue()).messagesQueue = new ArrayDeque<>();
        }
    }

}



class ServerNode extends Thread {
    private ServerSocket serverSocket;
    private ObjectInputStream objectInputStream;
    public int id;
    public Queue<Map<Integer, Double[]>> messagesQueue;


    public ServerNode(ServerSocket serverSocket, int id){
        this.serverSocket = serverSocket;
        this.id = id;
        this.messagesQueue = new ArrayDeque<>();
    }

    @Override
    public void run() {
        while (!serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept();
                this.objectInputStream = new ObjectInputStream(socket.getInputStream());
                while(socket.isConnected()){
                    Object o = objectInputStream.readObject();
                    Map<Integer, Double[]> temp = (Map<Integer, Double[]>) o;
                    if (temp != null) {
                        messagesQueue.add(temp);
                    }
                }
            } catch(IOException | ClassNotFoundException ignored) { }

        }
    }

    public void closeConnections() {
        try{
            this.objectInputStream.close();
            this.serverSocket.close();
        } catch (IOException ignored){}
    }
}

class ClientNode {
    private Socket socket;
    private ObjectOutputStream objectOutputStream;
    public int id;


    public ClientNode(Socket socket, int id){
        this.socket = socket;
        this.id = id;
        try {
            OutputStream outputStream = socket.getOutputStream();
            this.objectOutputStream = new ObjectOutputStream(outputStream);
        } catch (IOException ignored){ }

    }

    public void sendMessage(Map<Integer, Double[]> message_to_send){
        try {
            if(socket.isConnected() && objectOutputStream != null) {
                objectOutputStream.writeObject(message_to_send);
                objectOutputStream.flush();
        }
        } catch (IOException e){
            closeConnections();
        }
    }

    public void closeConnections() {
        try {
            if (this.socket != null) {
                this.socket.close();
            }
            if (this.objectOutputStream != null) {
                this.objectOutputStream.close();
            }
        } catch (IOException ignored) {}
    }
}
















