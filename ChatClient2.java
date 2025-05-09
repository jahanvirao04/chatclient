import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ChatClient {
    private JFrame frame = new JFrame("Java Chat");
    private JTextArea messageArea = new JTextArea(20, 50);
    private JTextField inputField = new JTextField(40);
    private BufferedReader in;
    private PrintWriter out;
    private String username;

    public ChatClient() {
        // GUI
        messageArea.setEditable(false);
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
        JPanel panel = new JPanel();
        panel.add(inputField);
        frame.getContentPane().add(panel, BorderLayout.SOUTH);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        inputField.addActionListener(e -> {
            String message = inputField.getText();
            if (!message.trim().isEmpty()) {
                out.println(message);
                inputField.setText("");
            }
        });
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket("localhost", 12345);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            while (true) {
                String line = in.readLine();
                if (line.startsWith("SUBMIT_USERNAME")) {
                    username = JOptionPane.showInputDialog(frame, "Enter your username:");
                    out.println(username);
                } else if (line.startsWith("USERNAME_TAKEN")) {
                    JOptionPane.showMessageDialog(frame, "Username already taken. Try another.");
                } else if (line.startsWith("USERNAME_ACCEPTED")) {
                    inputField.setEditable(true);
                    break;
                }
            }

            // Incoming messages
            new Thread(() -> {
                try {
                    String msg;
                    while ((msg = in.readLine()) != null) {
                        messageArea.append(msg + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ChatClient client = new ChatClient();
        client.connectToServer();
    }
}
