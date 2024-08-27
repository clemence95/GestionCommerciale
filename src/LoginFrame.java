import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;

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

                // Utiliser la méthode authentifier pour vérifier les identifiants
                if (authenticate(username, password)) {
                    JOptionPane.showMessageDialog(LoginFrame.this, "Connexion réussie !");
                    openMainWindow();
                } else {
                    JOptionPane.showMessageDialog(LoginFrame.this, "Nom d'utilisateur ou mot de passe incorrect.", "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    // Méthode pour authentifier l'utilisateur en vérifiant dans la base de données
    private boolean authenticate(String username, String password) {
        boolean isAuthenticated = false;

        String sql = "SELECT password FROM users WHERE username = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            // Paramétrer la requête SQL
            statement.setString(1, username);

            // Exécuter la requête
            ResultSet resultSet = statement.executeQuery();

            // Vérifier le mot de passe
            if (resultSet.next()) {
                String storedPassword = resultSet.getString("password");

                // Comparer le mot de passe fourni avec le mot de passe stocké
                isAuthenticated = storedPassword.equals(password);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return isAuthenticated;
    }

    // Méthode pour ouvrir la fenêtre principale après connexion
    private void openMainWindow() {
        // Fermer la fenêtre de connexion
        dispose();

        // Ouvrir la fenêtre principale
        MainFrame mainFrame = new MainFrame();
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


