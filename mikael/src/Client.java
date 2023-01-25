import java.net.*;
import java.util.*;
import java.io.*;

public class Client{
//    public class Client extends Thread{

    private Socket socket;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    public int id;


    public Client(Socket socket, int id){
        this.socket = socket;
        this.id = id;
        try {
            OutputStream outputStream = socket.getOutputStream();
            this.objectOutputStream = new ObjectOutputStream(outputStream);
            InputStream inputStream = socket.getInputStream();
            this.objectInputStream = new ObjectInputStream(inputStream);
        } catch (IOException e){ }

    }

    public void sendMessage(Map<Integer, Double[]> lv_to_send){
        try {
            if(socket.isConnected() && objectOutputStream != null) {
                objectOutputStream.writeObject(lv_to_send);
                objectOutputStream.flush();
//                System.out.println("Client: " + id + " has sent: " + lv_to_send.keySet().toString());
//                System.out.println("client: "+ id+" sent message created by: "+ lv_to_send.getKey()+ " with values: " + Arrays.toString(lv_to_send.getValue()));
            }
        } catch (IOException e){
            closeEverything();
        }
    }

    public void closeEverything() {
        try {
            if (this.socket != null) {
                this.socket.close();
            }
            if (this.objectOutputStream != null) {
                this.objectOutputStream.close();
            }
        } catch (IOException e) {}
    }
}
