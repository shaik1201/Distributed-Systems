import java.net.*;
import java.io.*;
import java.util.*;

public class Server  extends Thread{
    private ServerSocket serverSocket;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    public int id;
    public Queue<Map<Integer, Double[]>> queue;


    public Server(ServerSocket serverSocket, int id){
        this.serverSocket = serverSocket;
        this.id = id;
        this.queue = new ArrayDeque<>();
    }

    @Override
    public void run() {
        while (!serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept();
                this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                this.objectInputStream = new ObjectInputStream(socket.getInputStream());
                while(socket.isConnected()){
                    Object o = objectInputStream.readObject();
                    Map<Integer, Double[]> temp = (Map<Integer, Double[]>) o;
                    if (temp != null) {
                        queue.add(temp);
//                        System.out.println("Server: " + id + " has got: " + temp.keySet().toString());
                    }
                }
            } catch(IOException | ClassNotFoundException e) { }
        }
    }

    public void closeEverything() {
        try{
            this.serverSocket.close();
        } catch (IOException e){}
    }

}