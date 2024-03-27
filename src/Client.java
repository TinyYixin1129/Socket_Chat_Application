import java.io.*;
import java.net.Socket;
import java.util.Scanner;


/**
 * The Client class represents a client in a client-server network model.
 * It connects to the server, handles incoming and outgoing messages.
 */
public class Client {
    /**
     * The main method starts the client and initiates communication with the server.
     *
     * @param args The command-line arguments (not used).
     */
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 12001);
            ClientHandler clientHandler = new ClientHandler(socket);
            clientHandler.start();
        } catch (IOException e) {
            System.err.println("Error : Server closed. " + e.getMessage());
        }
    }

    /**
     * The ClientHandler class is responsible for setting up the communication
     * channels and starting the sender and receiver threads.
     */
    public static class ClientHandler {
        private Socket socket;
        private boolean flag = true;

        /**
         * Constructs a ClientHandler object for managing the socket connection to the server.
         * It also handles the initial pseudonym exchange process.
         *
         * @param socket The socket through which the client communicates with the server.
         * @throws IOException If an I/O error occurs when creating the input and output streams.
         */
        public ClientHandler(Socket socket) throws IOException {
            this.socket = socket;

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("Please enter your pseudonym:");
            Scanner sc = new Scanner(System.in);
            bw.write(sc.next());
            bw.newLine();
            bw.flush();

            String msgPseudo;
            do {
                msgPseudo = br.readLine();
                if ("Invalid".equals(msgPseudo)) {
                    System.out.println("Pseudonym existed, please enter a new one:");
                    bw.write(sc.next());
                    bw.newLine();
                    bw.flush();
                }
            } while ("Invalid".equals(msgPseudo));
            System.out.println("You are connected");
            System.out.println("---------------------------------------");
        }

        /**
         * The MsgSender class is responsible for reading user input and sending it to the server.
         */
        public class MsgSender extends Thread {
            /**
             * Reads user input and sends it to the server until the 'exit' command is entered.
             */
            @Override
            public void run() {
                try (Scanner sc = new Scanner(System.in);
                     BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
                    while (flag) {
                        String msg = sc.nextLine();
                        if (msg.isEmpty()) continue;
                        if ("exit".equals(msg)) {
                            flag = false;
                        }
                        bw.write(msg);
                        bw.newLine();
                        bw.flush();
                    }
                } catch (IOException e) {
                    System.out.println("Error : Server closed. " + e.getMessage());
                }
            }
        }

        /**
         * The MsgReceiver class is responsible for reading messages from the server and displaying them to the user.
         */
        public class MsgReceiver extends Thread {
            /**
             * Listens for messages from the server and prints them until the socket is closed.
             */
            @Override
            public void run() {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    String msg;
                    while (flag && (msg = br.readLine()) != null) {
                        System.out.println(msg);
                    }
                } catch (IOException e) {
                    if (!socket.isClosed()) {
                        System.out.println("Exception in MsgReceiver: " + e.getMessage());
                    }
                }
            }
        }

        /**
         * Starts the sender and receiver threads that handle communication with the server.
         */
        public void start() {
            MsgReceiver msgReceiver = new MsgReceiver();
            MsgSender msgSender = new MsgSender();
            msgReceiver.start();
            msgSender.start();
        }
    }
}
