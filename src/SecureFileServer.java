import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class SecureFileServer {

    private static final int PORT = 5000;
    private static final byte[] AES_KEY = "1234567890123456".getBytes();

    private static Map<String, String> users = new HashMap<>();
    static {
        users.put("yassine", "yassinepass");
        users.put("lahcen", "lahcenpass");
    }

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("SecureFileServer running on port " + PORT);

            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("Client connected: " + client.getInetAddress());
                new Thread(() -> handleClient(client)).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket socket) {
        try (
                DataInputStream in = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream())
        ) {
            // PHASE 1 : Authentification
            String login = in.readUTF();
            String password = in.readUTF();

            if (!users.containsKey(login) || !users.get(login).equals(password)) {
                out.writeUTF("AUTH_FAIL");
                socket.close();
                return;
            }
            out.writeUTF("AUTH_OK");
            System.out.println("User authenticated: " + login);

            // Boucle pour recevoir plusieurs fichiers
            while (true) {
                String command = in.readUTF();
                if (command.equalsIgnoreCase("QUIT")) {
                    System.out.println("Client disconnected: " + login);
                    break;
                } else if (command.equalsIgnoreCase("FILE")) {
                    // PHASE 2 : Metadata
                    String fileName = in.readUTF();
                    long fileSize = in.readLong();
                    String expectedHash = in.readUTF();
                    out.writeUTF("READY_FOR_TRANSFER");

                    // PHASE 3 : Transfert
                    byte[] encryptedData = new byte[(int) fileSize];
                    in.readFully(encryptedData);
                    byte[] decryptedData = decryptAES(encryptedData);

                    File dir = new File("received");
                    if (!dir.exists()) dir.mkdir();
                    FileOutputStream fos = new FileOutputStream(new File(dir, fileName));
                    fos.write(decryptedData);
                    fos.close();

                    String actualHash = sha256(decryptedData);
                    if (actualHash.equals(expectedHash)) {
                        out.writeUTF("TRANSFER_SUCCESS");
                        System.out.println("File " + fileName + " received successfully.");
                    } else {
                        out.writeUTF("TRANSFER_FAIL");
                        System.out.println("Integrity check failed for " + fileName);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] decryptAES(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(AES_KEY, "AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        return cipher.doFinal(data);
    }

    private static String sha256(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data);
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
