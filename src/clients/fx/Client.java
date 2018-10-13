package clients.fx;

import javafx.application.Application;
import javafx.application.Platform;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


import java.io.*;
import java.net.Socket;
import java.util.Iterator;
import java.util.Scanner;

public class Client extends Application {
    private Stage win;
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 3443;
    private Socket clientSocket;
    private Scanner inMessage;
    private PrintWriter outMessage;
    private String clientName = "";
    public String getClientName() {
        return this.clientName;
    }

    @FXML private Label jlNumberOfClients;
    @FXML private TextField jtfMessage;
    @FXML private TextField jtfName;
    @FXML private ScrollPane scroll;
    private TextArea jtaTextAreaMessage = new TextArea();


    @FXML private void initialize(){

            try {
                clientSocket = new Socket(SERVER_HOST, SERVER_PORT);

                inMessage = new Scanner(clientSocket.getInputStream());
                outMessage = new PrintWriter(clientSocket.getOutputStream());
            } catch (IOException e) {
                new Alert(Alert.AlertType.ERROR, "Error connection to server. Retry connect").showAndWait();

            }

       // jtfMessage.focusedProperty().addListener((observable, oldValue, newValue) -> jtfMessage.setText(""));

        //jtfName.focusedProperty().addListener((observable, oldValue, newValue) -> jtfName.setText(""));

        scroll.contentProperty().set(jtaTextAreaMessage);
        jtaTextAreaMessage.prefWidthProperty().bind(scroll.widthProperty());
        jtaTextAreaMessage.prefHeightProperty().bind(scroll.heightProperty());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        if (inMessage.hasNext()) {
                            String inMes = inMessage.nextLine();
                            String clientsInChat = "Клиентов в чате = ";
                            if (inMes.indexOf(clientsInChat) == 0) {
                                jlNumberOfClients.setText(inMes);
                            } else {
                                jtaTextAreaMessage.appendText(inMes);
                                jtaTextAreaMessage.appendText("\n");
                            }
                        }
                    }
                } catch (Exception e) {
                }
            }
        }).start();
    }

    @FXML private void send(){
        if (!jtfMessage.getText().trim().isEmpty() && !jtfName.getText().trim().isEmpty()) {
            clientName = jtfName.getText();
            sendMsg();
            // фокус на текстовое поле с сообщением
            jtfMessage.requestFocus();
        }
    }

    @FXML private void load(){
        FileChooser choose = new FileChooser();
        choose.setTitle("Open File");
        File file = choose.showOpenDialog(this.win);
        if(file!=null){
         //   actionOnFile(file);
        }

    }

    private void actionOnFile(File file){
        try(BufferedReader reader =new BufferedReader(new FileReader(file.getAbsoluteFile()));){
            String line;

            while((line=reader.readLine())!=null){
                try{
                    int i= Integer.parseInt(line);
                    double res=Math.sqrt(i)*Math.PI;
                    System.out.println(res);
                } catch (Exception e){}

                System.out.println(line);

                String[] words = line.split("_");
                line.replaceAll("_", "[ ]");
                for (String word:words){
                    System.out.println(word);
                }
                System.out.println(line);
            }

        }catch (IOException e){
            System.out.println(e.getMessage());
            return;
        }
    }


    public void sendMsg() {
        String messageStr = jtfName.getText() + ": " + jtfMessage.getText();
        outMessage.println(messageStr);
        outMessage.flush();
        jtfMessage.setText("");
    }



    @Override
    public void start(Stage primaryStage) throws Exception {
        win=primaryStage;
        Parent parent = FXMLLoader.load(getClass().getResource("/clients/fx/client.fxml"));
        win.setTitle("Chat");
        win.setScene(new Scene(parent));
        win.setResizable(false);
        win.setOnCloseRequest(event -> {
            event.consume();
            closeApp();
        });
        win.show();
    }

    private void closeApp() {
        try {
            if (!clientName.isEmpty() && clientName != "Введите ваше имя: ") {
                outMessage.println(clientName + " вышел из чата!");
            } else {
                outMessage.println("Участник вышел из чата, так и не представившись!");
            }
            outMessage.println("##session##end##");
            outMessage.flush();
            outMessage.close();
            inMessage.close();
            clientSocket.close();

        } catch (IOException exc) {

        }
        finally {
            Platform.exit();
            System.exit(0);
        }
    }
}
