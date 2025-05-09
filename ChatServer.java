import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static Map<String, PrintWriter> userMap = new HashMap<>();

    public static void main(String[] args) {
        System.out.println("Chat server started...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Ask for a unique username
                while (true) {
                    out.println("SUBMIT_USERNAME");
                    username = in.readLine();
                    if (username == null) return;

                    synchronized (userMap) {
                        if (!userMap.containsKey(username)) {
                            userMap.put(username, out);
                            break;
                        } else {
                            out.println("USERNAME_TAKEN");
                        }
                    }
                }

                out.println("USERNAME_ACCEPTED");
                broadcast("Server: " + username + " has joined the chat");

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("@")) {
                        int spaceIdx = message.indexOf(' ');
                        if (spaceIdx != -1) {
                            String targetUser = message.substring(1, spaceIdx);
                            String privateMsg = message.substring(spaceIdx + 1);
                            sendPrivateMessage(username, targetUser, privateMsg);
                        }
                    } else {
                        broadcast(username + ": " + message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (username != null) {
                        userMap.remove(username);
                        broadcast("Server: " + username + " has left the chat");
                    }
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void broadcast(String message) {
            for (PrintWriter writer : userMap.values()) {
                writer.println(message);
            }
        }

        private void sendPrivateMessage(String sender, String recipient, String message) {
            PrintWriter recipientOut = userMap.get(recipient);
            if (recipientOut != null) {
                recipientOut.println("[Private] " + sender + ": " + message);
                out.println("[Private to " + recipient + "] " + sender + ": " + message);
            } else {
                out.println("User '" + recipient + "' not found.");
            }
        }
    }
}
