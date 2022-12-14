package fhtw.Model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Base64;

public class Client extends Thread{
    private Socket client;
    private String token;
    private String username;
    private PrintWriter output;
    private BufferedReader input;

    private final StringProperty message = new SimpleStringProperty();
    private final StringProperty chatroom = new SimpleStringProperty();

    /**
     * Initializes a Thread on the client to communicate with Socket from LoginController.
     * Shorthandles access to Write(PrintWriter) and Read(Buffered) Streams for easier use inside application
     * @param socket Socket
     * @param name Provided username, password is being handled in authenticate()
     * @throws IOException Socket streams can throw IOExceptions on interrupted connections
     */
    public Client(Socket socket, String name) throws IOException {
        this.output = new PrintWriter(socket.getOutputStream(), true);
        this.input = new BufferedReader(new InputStreamReader((socket.getInputStream())));
        this.username = name.replace(" ", "_");
        this.client = socket;
        this.start();
    }

    /**
     * Encodes password string and sends it together with class property username towards the server for verification.
     * Verification is handled in the appropriate Thread/Stream. First incoming message should always indicates userdata.
     * @param pw Provided password in clear text
     */
    public void authenticate(String pw) {
        pw = Base64.getEncoder().encodeToString(pw.getBytes());
        this.output.println(getUsername() + " " + pw);
    }

    /**
     * Thread; Handles direction of incoming buffered streams, appropriate to their first bit
     * <ul>
     *   <li> ~ signals flags passed directly from the verification procedure of the Server Thread </li>
     *   <li> # signals data messages e.g. Gamescores, Chatroom Size used for application specific data </li>
     *   <li> !,@ and everything else gets forwarded as raw message into Chatroom  </li>
     * </ul>
     */
    @Override
    public void run(){
        try{
            while (true){
                String response = input.readLine();

                if (response == null){
                    System.out.println("closing?");;
                    break;
                }
                if (response.charAt(0) == '^') {
                    this.output.println("^Pong");
                    //break;
                }else if (response.charAt(0) == '~') {
                    setToken(response.substring(1));
                    if (response.equals("~reject")){
                        input.close();
                        break;
                    }
                    //break;
                }else if(response.charAt(0) == '#'){
                    setChatroom(response.substring(1));
                }
                else{
                    //System.out.println("sending " + response);;
                    setMessage(response);
                }
            }
            client.close();
        }catch (IOException e){
            //System.out.println("Error occurred " + Arrays.toString(e.getStackTrace()));
        }finally {
            try{
                client.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public String getChatroom() {
        return chatroom.get();
    }

    public StringProperty chatroomProperty() {
        return chatroom;
    }

    public void setChatroom(String chatroom) {
        this.chatroom.set(chatroom);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message.get();
    }

    public StringProperty messageProperty() {
        return message;
    }

    public void setMessage(String message) {
        this.message.set(message);
    }

    public PrintWriter getOutput() {
        return output;
    }

    public void setOutput(PrintWriter output) {
        this.output = output;
    }

    public BufferedReader getInput() {
        return input;
    }

    public void setInput(BufferedReader input) {
        this.input = input;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Socket getClient() {
        return client;
    }

    public void setClient(Socket client) {
        this.client = client;
    }
}



