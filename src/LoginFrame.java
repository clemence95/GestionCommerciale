import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

                // Vérification simple des identifiants
                if (authenticate(username, password)) {
                    JOptionPane.showMessageDialog(LoginFrame.this, "Connexion réussie !");
                    openMainWindow();
                } else {
                    JOptionPane.showMessageDialog(LoginFrame.this, "Nom d'utilisateur ou mot de passe incorrect.", "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    // Méthode pour authentifier l'utilisateur
    private boolean authenticate(String username, String password) {
        // Pour l'exemple, on accepte un seul utilisateur
        return "admin".equals(username) && "password".equals(password);
    }

    // Méthode pour ouvrir la fenêtre principale après connexion
    private void openMainWindow() {
        // Fermer la fenêtre de connexion
        dispose();

        // Ouvrir la fenêtre principale
        MainFrame mainFrame = new MainFrame();
        mainFrame.setVisible(true);
    }
}

