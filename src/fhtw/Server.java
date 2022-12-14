package fhtw;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

import fhtw.Model.Highscore;

public class Server {
    int port;

    public static void main(String[] args) {
        new Server(inputPort()).run();
    }

    public Server(int port) {
        this.port = port;
    }

    /**
     * Main Server Thread. Passive open socket stays to allow TCP connections.
     * First incoming message stream from the client is used for authentication in the "database".
     * (De-Encoding not necessary because values are already stored encoded, Encoded strings only have to match~~)
     * <code>Pass ? Username & Password | Reject ? Username & !Password | Pass ? ? & ?</code>
     * Opens a separate Thread to handle Client communication
     */
    public void run() {
        ArrayList<ServerThread> threadList = new ArrayList<>();
        String database = "src\\fhtw\\Resources\\database.txt";
        File f = new File(database);
        if(!f.exists()){
            try {
                boolean err = f.createNewFile();
            } catch (IOException e) {
                System.err.println("Could not create "+database+" file.");
            }
        }
        boolean exists, reject;
        try (ServerSocket serversocket = new ServerSocket(port)) {
            System.out.println("Server running on port " + port);
            while (true) {

                exists = false;
                reject = false;
                Socket socket = serversocket.accept();
                String params = (new Scanner(socket.getInputStream())).nextLine();

                String username = params.substring(0, params.indexOf(" "));
                String password = params.substring(params.indexOf(" ") + 1, params.length());
                //password = Base64.getEncoder().encodeToString(password.getBytes());
                //if (f.isFile() && f.canRead()) {
                    Scanner fileScan = null;
                    try {
                        fileScan = new Scanner(f);
                        PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

                        while (fileScan.hasNextLine()) {
                            String fcontents = fileScan.nextLine();
                            String fUsername = fcontents.substring(0, fcontents.indexOf(' '));
                            String fPassword = fcontents.substring(fcontents.indexOf(' ') + 1, fcontents.length());

                            //byte[] decodedBytes = Base64.getDecoder().decode(fPassword);
                            //fPassword = new String(decodedBytes);

                            if (fUsername.equals(username)) {
                                if ((fPassword.equals(password))) {
                                    for (ServerThread client : threadList) {
                                        if (client.getUsername().equals(username)){
                                            output.println("~reject");
                                            reject = true;
                                            socket.shutdownOutput();
                                            socket.close();
                                        }
                                    }
                                    if (!reject){
                                        output.println("~pass");
                                        exists = true;
                                    }
                                } else {
                                    output.println("~reject");
                                    System.out.println("Rejected");
                                    reject = true;
                                    socket.shutdownOutput();
                                    socket.close();
                                }
                            }
                        }

                        if (!reject) {
                            if (!exists) {
                                output.println("~pass");
                                FileWriter fileWriter = new FileWriter(database, true);
                                fileWriter.write(username + " " + password + "\n");
                                fileWriter.close();
                            }

                            System.out.println("Client: " + username + " connected.");
                            ServerThread serverThread = new ServerThread(socket, threadList, username);
                            threadList.add(serverThread);
                            output.println("#" + threadList.size());
                            // run listener thread for each user
                            new Thread(serverThread).start();
                        }
                    }catch (FileNotFoundException e) {
                        e.printStackTrace();
                        try {
                            boolean err = f.createNewFile();
                        } catch (IOException err) {
                            System.err.println("Could not create "+database+" file.");
                        }
                    } finally {
                        if (fileScan != null) {
                            fileScan.close();
                        }
                    }
                //}else{
                //    //what should happen here ...!?
                //    //output.println("~tryagainlater:)");
                //}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads an Integer to use as Port (Loops)
     * @return Port
     */
    public static int inputPort() {
        int p;
        java.util.Scanner scan = new java.util.Scanner(System.in);
        System.out.print("Enter a port: ");
        String parseport = scan.next();
        try {
            p = Integer.parseInt(parseport);
        }catch (NumberFormatException e){
            System.out.println("Error during input!");
            return inputPort();
        }
        return p;
    }
}


class ServerThread implements Runnable {

    private Socket socket;
    private ArrayList<ServerThread> threadList;
    private PrintWriter output;
    private String username;

    /**
     * Starts Thread to communicate with client. No Shorthandles to keep it Thread-Safe ..
     * @param socket Socket
     * @param threads List of all Threads and their ^Sockets
     * @param username Username associated with the Thread
     */
    public ServerThread(Socket socket, ArrayList<ServerThread> threads, String username) {
        this.socket = socket;
        this.threadList = threads;
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Takes care of sending messages to everyone in the ArrayList ThreadList housing User streams. Sends out # data message for Chatroom size.
     * @param message Received message
     */
    private void multiMessages(String message, String user) {
        for (ServerThread client : threadList) {
            if (!client.username.equals(this.username)){
                client.output.println(user + ": " + message);
            }
            client.output.println("#" + threadList.size());
            //System.out.println("broadcasting " + message + " to " + client.username);
        }
    }

    /**
     * Takes care of sending messages to a specific user. Target and Username in ArrayList ThreadList have to match. Source gets notified.
     * Incoming package @UsernameTarget Message
     * @param message Message to pass through
     * @param target String username of Target
     */
    private void directMessages(String message, String target) {
        boolean sent = false;
        for (ServerThread client : threadList) {
            if (client.username.equals(target) && !client.username.equals(this.username)) {
                client.output.println("Incoming message from " + this.username + " : " + message);
                sent = true;
            }
        }
        if (sent) {
            for (ServerThread client : threadList) {
                if (client.username.equals(this.username)) {
                    client.output.println(this.username + " -> " + target + ": " + message);
                }
            }
        }
    }

    /**
     * Thread; Handles direction of incoming buffered streams, appropriate to their first bit
     * <ul>
     *   <li> @ Signals messages directed to a specific Client / User </li>
     *   <li> # Not really used, can be used for storing Highscores Server-side, Communication with UI Thread has to be Setup </li>
     *   <li> ! Signals a Multicast to every Client inside the Threadlist  </li>
     * </ul>
     */
    @Override
    public void run() {
        try {
            //reading input //Threadsafe ?
            BufferedReader input = new BufferedReader(new InputStreamReader((this.socket.getInputStream())));

            //return output to the client
            output = new PrintWriter(this.socket.getOutputStream(), true);
            //multiMessages("Logged in.", this.getUsername());


            while (true) {
                String outputString = input.readLine();
                if (outputString == null){
                    break;
                }
                //System.out.println("Server received " + outputString);

                String usrnameasc = outputString.substring(1, outputString.indexOf(" "));
                String usrmessage = outputString.substring(outputString.indexOf(" ") + 1);

                if (outputString.charAt(0) == '!') {
                    multiMessages(usrmessage, usrnameasc);
                }
                //if (outputString.charAt(0) == '#') {
                //    System.out.println("Updating GameScores");
                //    Highscore.getInstance().addHighscore(this.username, 100);
                //}
                if (outputString.charAt(0) == '@') {
                    directMessages(usrmessage, usrnameasc);
                }
            }
            System.out.println("Bye bye " + this.username);
            this.threadList.remove(this);
            this.socket.close();
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            //EOF
            System.out.println("Last call for " + this.username);
            long startTime = System.currentTimeMillis(); //fetch starting time
            output.println("^Ping");
            boolean isalive = false;
            try {
                BufferedReader input = new BufferedReader(new InputStreamReader((this.socket.getInputStream())));

                while((System.currentTimeMillis() - startTime) < 60000)
                {
                    String outputString = input.readLine();
                    if (outputString == null){
                        isalive = false;
                        break;
                    }
                    if (outputString.equals("^Pong")) {
                        isalive = true;
                        break;
                    }
                }
                if (!isalive){
                    try{
                        System.out.println("Omae wa mou shindeiru..");
                        this.threadList.remove(this);
                        this.socket.close();
                        Thread.currentThread().interrupt();
                        return;
                    }catch (Exception err){
                        e.printStackTrace();
                    }
                }
            } catch (IOException ioException) {
                System.out.println("Closing connection for " + this.username);
                this.threadList.remove(this);
                try {
                    this.socket.close();
                } catch (IOException exception) {
                    //Exceptioinception
                    exception.printStackTrace();
                }
                Thread.currentThread().interrupt();
                return;
            }

            System.out.println("Error occurred " + Arrays.toString(e.getStackTrace()));

        }finally {
            try{
                this.threadList.remove(this);
                this.socket.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }
}

