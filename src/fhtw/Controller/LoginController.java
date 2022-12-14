package fhtw.Controller;

import fhtw.Model.Client;
import fhtw.View.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

public class LoginController {
    @FXML
    private TextField txt_server;

    @FXML
    private TextField txt_user;

    @FXML
    private TextField txt_port;

    @FXML
    private TextField txt_password;

    @FXML
    private Text txt_errormsg;

    @FXML
    private Button btn_login;

    /**
     * Login Button Action Handler
     * @param scene Scene object reference
     */
    public void initLogin(ViewManager scene) {
        btn_login.setOnAction(event -> {
            Client session = null;
            try {
                session = authorize();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (session != null) {
                scene.authenticated(session);
            }
        });
    }

    /**
     * Authorization procedures. Creates a Socket and sends credentials through the stream to try authenticate on the server.
     * @return Client object with Username, Read/Write Stream etc.
     * @throws IOException on Socket
     */
    private Client authorize() throws IOException {
        txt_errormsg.setText("");
        String token;
        int port;

        try {
            port = Integer.parseInt(txt_port.getText());
        }catch (NumberFormatException e){
            txt_errormsg.setText("Error occurred. Please check port.");
            return null;
        }
        Socket socket = null;
        try {
            socket = new Socket(txt_server.getText(), port);
        }catch (ConnectException e) {
            txt_errormsg.setText("Please start the server :)");
            return null;
        }
        Client client = new Client(socket, txt_user.getText());
        client.authenticate(txt_password.getText());

        while (true){
            token = client.getToken();
            if (token != null){
                break;
            }
        }
        if (token.equals("reject")){
            txt_errormsg.setText("Error occurred.");
            client = null;
            socket.close();
        }
        return client;
    }
}