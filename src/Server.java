import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * The Server class represents a simple server that can handle multiple client connections
 * and manage client messages and pseudonyms.
 */
public class Server {

    /**
     * A thread-safe list of active client Handlers.
     */
    private static List<Handler> clientList = new CopyOnWriteArrayList<>();

    /**
     * A list to store unique pseudonyms of connected clients.
     */
    private static List<String> pseudoList = new ArrayList<>();

    /**
     * The main method starts the server and accepts client connections.
     *
     * @param args Command line arguments.
     * @throws IOException if an I/O error occurs when waiting for a connection.
     */
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12001);
        System.out.println("Server activated....");

        while (true) {
            try {
                Socket socket = serverSocket.accept();
                Handler client = new Handler(socket);
                client.start();
            } catch (IOException e) {
                System.err.println("Error : " + e.getMessage());
            }
        }
    }

    /**
     * The Handler class handles individual client threads.
     */
    public static class Handler extends Thread {
        private Socket client;
        private String pseudonym;

        /**
         * Initializes a new client Handler.
         *
         * @param client The client socket.
         * @throws IOException if an I/O error occurs when creating the input and output streams.
         */
        public Handler(Socket client) throws IOException {
            this.client = client;
        }

        /**
         * Sends a message to all connected clients.
         *
         * @param msg The message to be sent.
         */
        private void sendMsg(String msg) {
            for (Handler cl : clientList) {
                try {
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(cl.client.getOutputStream()));
                    bw.write(msg);
                    bw.newLine();
                    bw.flush();
                } catch (IOException e) {
                    clientList.remove(cl);
                    pseudoList.remove(cl.pseudonym);
                }
            }
        }

        /**
         * The main entry point for the Handler thread.
         * Reads the client's pseudonym and manages incoming messages.
         */
        @Override
        public void run() {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

                do {
                    pseudonym = br.readLine();
                    String msg = pseudoList.contains(pseudonym) ? "Invalid" : "Valid";
                    bw.write(msg);
                    bw.newLine();
                    bw.flush();
                } while (pseudoList.contains(pseudonym));

                clientList.add(this);
                pseudoList.add(pseudonym);
                sendMsg(pseudonym + " connected.");
                System.out.println(pseudonym + " connected.");

                String msg;
                while ((msg = br.readLine()) != null && !msg.equals("exit")) {
                    System.out.println(pseudonym + " said : " + msg);
                    sendMsg(pseudonym + " said : " + msg);
                }
            } catch (IOException e) {
                // Log the exception
            } finally {
                // Clean up the client connection
                clientList.remove(this);
                pseudoList.remove(pseudonym);
                System.out.println(pseudonym + " disconnected.");
                sendMsg(pseudonym + " disconnected.");
                try {
                    client.close();
                } catch (IOException e) {
                    System.err.println("Error closing connection with " + pseudonym + ": " + e.getMessage());
                }
            }
        }
    }
}
