import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class MainFrame extends JFrame {

    private String jwtToken;

    // Constructeur avec Token JWT
    public MainFrame(String jwtToken) {
        this.jwtToken = jwtToken;

        setTitle("Village Green");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Créer un label de bienvenue
        JLabel welcomeLabel = new JLabel("Bienvenue dans l'application !");
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Créer un bouton pour quitter l'application
        JButton quitButton = new JButton("Quitter l'application");

        // Ajouter une action au bouton pour fermer l'application
        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0); // Fermer l'application
            }
        });

        // Ajouter les composants à la fenêtre
        setLayout(new BorderLayout());
        add(welcomeLabel, BorderLayout.NORTH); // Label de bienvenue en haut
        add(quitButton, BorderLayout.SOUTH);  // Le bouton est placé en bas de la fenêtre

        // Utiliser le token JWT pour récupérer des données depuis l'API
        fetchDataFromApi();
    }

    // Méthode pour utiliser le token JWT pour faire une requête API
    private void fetchDataFromApi() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://127.0.0.1:8000/api/protected-endpoint")) // Remplacez par votre endpoint protégé
                .header("Authorization", "Bearer " + jwtToken)
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Réponse de l'API: " + response.body());
            // Vous pouvez également afficher les données dans l'interface utilisateur
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Pour tester, vous pouvez passer un token fictif
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainFrame mainFrame = new MainFrame("exemple-de-token-jwt");
                mainFrame.setVisible(true);
            }
        });
    }
}

