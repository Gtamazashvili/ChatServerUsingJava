import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class ChatServer {
    public static Map <String,Socket>nameAndSocket=new HashMap<>();
    public static final int MAX = 50;
    public static final int port = 3000;
    public static List<Socket> clientSockets = new ArrayList<>();
    public static List<String> clientNames = new ArrayList<>();
    public static Map<String,LocalTime>usernamesAndTimes=new HashMap<>();
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(port)){
            System.out.println("Server is waiting on port " + serverSocket.getLocalPort());
            while (true) {
                Socket clientSocket = serverSocket.accept();
                if (clientSockets.size() < MAX) {
                    clientSockets.add(clientSocket);
                    new Thread(new ClientHandler(clientSocket)).start();
                } else {
                    clientSocket.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }public static void printInServerLogIn(String user){
        System.out.println("* "+ user +" joined the chat");
        System.out.println(" * Server is waiting on port " + port);
    }public static void printInServerLogOut(String user){
        System.out.println("* "+ user +" logged out");
        System.out.println(" * Server is waiting on port " + port);
    }
    public static void broadcastMessage(String message) {
        for (int i = 0; i < clientSockets.size(); i++) {
            try {
                PrintWriter out = new PrintWriter(clientSockets.get(i).getOutputStream(), true);
                out.println(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}class ClientHandler implements Runnable {
    private Socket clientSocket;
    private String clientName;
    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            out.println("Enter your username");
            clientName = in.readLine();
            while (ChatServer.clientNames.contains(clientName)){
                out.println("Error : this server already has a client with that name ");
                clientName=in.readLine();
            }
            ChatServer.nameAndSocket.put(clientName,clientSocket);
            ChatServer.usernamesAndTimes.put(clientName,LocalTime.now());
            ChatServer.clientNames.add(clientName);
            ChatServer.printInServerLogIn(clientName);
            ChatServer.broadcastMessage(" * "+clientName+" joined the chat");
            out.println("Connected to chat server  welcome to PinguChat");
            out.println("1.Simply type message to send broadcast to all active clients and the time they have joined");
            out.println("2 Type '@username<space> (Your message) ' without quotes to send message to a desired client ");
            out.println("3 Type 'WHOIS' without quotes to see list of active clients ");
            out.println("4 Type 'LOGOUT' without quotes to logoff from a sever");
            out.println("5 Type 'PENGU' without quotes without quotes to request a random fact");
            while (true) {
                String message = in.readLine();
                if (message.equals("PENGU")) {
                    ChatServer.broadcastMessage("Penguins hug each other to get warm");
                } else if (message.equals("WHOIS")) {
                    ChatServer.usernamesAndTimes.entrySet().forEach(n->{
                        out.println("User "+n.getKey()+" joined on  " + n.getValue());
                    });
                } else if (message.equals("LOGOUT")){
                        out.println("Goodbye ");
                        ChatServer.printInServerLogOut(clientName);
                        ChatServer.usernamesAndTimes.remove(clientName);
                        ChatServer.broadcastMessage("* "+clientName+" has left the chat");
                        ChatServer.clientNames.remove(clientName);
                        ChatServer.clientSockets.remove(clientSocket);
                 }else if(message.charAt(0)=='@'){
                    String x[]=message.split(" ");
                    String name=x[0].substring(1);
                    if(ChatServer.clientNames.contains(name)&&!name.equals(clientName)){
                        PrintWriter msg=new PrintWriter(ChatServer.nameAndSocket.get(name).getOutputStream(),true);
                        message=message.replaceFirst(x[0],"");
                        msg.println(LocalTime.now()+" "+clientName+" is writing you a private message :"+message);
                    }else if(name.equals(clientName)){
                        out.println("You cant write to your self :(");
                    }else{
                        out.println("This client does not exist");
                    }
                 }else{
                    ChatServer.broadcastMessage(LocalTime.now().toString() + " " + clientName + ": " + message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


