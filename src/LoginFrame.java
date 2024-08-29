import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private String jwtToken;

    public LoginFrame() {
        // Configurer la fenêtre de connexion
        setTitle("Connexion");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Créer les composants
        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);
        loginButton = new JButton("Se connecter");

        // Ajouter les composants à la fenêtre
        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("Nom d'utilisateur:"));
        panel.add(usernameField);
        panel.add(new JLabel("Mot de passe:"));
        panel.add(passwordField);
        panel.add(new JLabel(""));  // Espace pour aligner le bouton
        panel.add(loginButton);
        add(panel);

        // Ajouter l'action au bouton de connexion
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                // Utiliser la méthode authentifier pour vérifier les identifiants via l'API
                try {
                    if (authenticate(username, password)) {
                        JOptionPane.showMessageDialog(LoginFrame.this, "Connexion réussie !");
                        openMainWindow();
                    } else {
                        JOptionPane.showMessageDialog(LoginFrame.this, "Nom d'utilisateur ou mot de passe incorrect.", "Erreur", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (IOException | InterruptedException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(LoginFrame.this, "Erreur lors de la connexion à l'API.", "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    // Méthode pour authentifier l'utilisateur via l'API Symfony
    private boolean authenticate(String username, String password) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        String json = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", username, password);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://127.0.0.1:8000/api/login"))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            // Extraire le token JWT de la réponse
            jwtToken = parseTokenFromResponse(response.body());
            return true;
        } else {
            return false;
        }
    }

    // Méthode pour extraire le token JWT de la réponse JSON
    private String parseTokenFromResponse(String responseBody) {
        // Simplifié : en supposant que le token est dans le format {"token":"eyJhbGc..."}
        return responseBody.split(":")[1].replaceAll("[\"{}]", "").trim();
    }

    // Méthode pour ouvrir la fenêtre principale après connexion
    private void openMainWindow() {
        // Fermer la fenêtre de connexion
        dispose();

        // Ouvrir la fenêtre principale avec le token JWT disponible
        MainFrame mainFrame = new MainFrame(jwtToken);
        mainFrame.setVisible(true);
    }

    public static void main(String[] args) {
        // Lancer la fenêtre de connexion
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginFrame().setVisible(true);
            }
        });
    }
}



