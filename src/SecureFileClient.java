import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.security.MessageDigest;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class SecureFileClient {

    private static final int PORT = 5000;
    private static final byte[] AES_KEY = "1234567890123456".getBytes();

    public static void main(String[] args) {
        try {
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

            // Login / Password
            System.out.print("Login: ");
            String login = console.readLine();

            System.out.print("Password: ");
            String password = console.readLine();

            // Adresse IP du serveur
            System.out.print("Server IP: ");
            String serverIP = console.readLine();

            // Connexion au serveur
            Socket socket = new Socket(serverIP, PORT);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            // PHASE 1 : Authentification
            out.writeUTF(login);
            out.writeUTF(password);
            if (!in.readUTF().equals("AUTH_OK")) {
                System.out.println("Authentication failed!");
                socket.close();
                return;
            }
            System.out.println("Authentication OK.");

            // Boucle pour envoyer plusieurs fichiers
            while (true) {
                System.out.print("File path (ou QUIT pour terminer): ");
                String filePath = console.readLine();
                if (filePath.equalsIgnoreCase("QUIT")) {
                    out.writeUTF("QUIT");
                    break;
                }

                File file = new File(filePath);
                if (!file.exists()) {
                    System.out.println("Fichier introuvable.");
                    continue;
                }

                byte[] fileBytes = Files.readAllBytes(file.toPath());
                byte[] encryptedData = encryptAES(fileBytes);
                String hash = sha256(fileBytes);

                // Indiquer qu'on envoie un fichier
                out.writeUTF("FILE");

                // PHASE 2 : Metadata
                out.writeUTF(file.getName());
                out.writeLong(encryptedData.length);
                out.writeUTF(hash);

                if (!in.readUTF().equals("READY_FOR_TRANSFER")) {
                    System.out.println("Server not ready.");
                    continue;
                }

                // PHASE 3 : Transfert
                out.write(encryptedData);
                String result = in.readUTF();
                System.out.println("Server response: " + result);
            }

            socket.close();
            System.out.println("Session terminée.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] encryptAES(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec key = new SecretKeySpec(AES_KEY, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
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
