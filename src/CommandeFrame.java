import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;

public class CommandeFrame extends JFrame {

    private String jwtToken;

    public CommandeFrame(String jwtToken) {
        this.jwtToken = jwtToken;

        setTitle("Gestion des Commandes");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JTextArea commandesArea = new JTextArea();
        commandesArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(commandesArea);
        add(scrollPane, BorderLayout.CENTER);

        // Récupérer les commandes depuis l'API
        getCommandesFromAPI(commandesArea);
    }

    // Méthode pour récupérer les commandes depuis l'API
    private void getCommandesFromAPI(JTextArea commandesArea) {
        try {
            // Créer le client HTTP
            HttpClient client = HttpClient.newHttpClient();

            // Créer la requête HTTP
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://127.0.0.1:8000/api/commandes"))
                    .header("Authorization", "Bearer " + jwtToken)
                    .GET()  // Spécifie que c'est une requête GET
                    .build();

            // Envoyer la requête et obtenir la réponse
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Vérifier si la requête a réussi (code 200)
            if (response.statusCode() == 200) {
                // Affiche la réponse brute dans la console pour déboguer
                System.out.println("Réponse brute de l'API : " + response.body());

                // Parser et afficher les commandes dans la zone de texte
                parseCommandes(response.body(), commandesArea);
            } else {
                commandesArea.setText("Erreur : Impossible de récupérer les commandes (Code " + response.statusCode() + ")");
            }
        } catch (Exception e) {
            e.printStackTrace();
            commandesArea.setText("Erreur lors de la récupération des commandes.");
        }
    }

    // Méthode pour parser et afficher les commandes
    private void parseCommandes(String jsonResponse, JTextArea commandesArea) {
        try {
            // Imprimer le JSON brut pour voir à quoi il ressemble
            System.out.println("JSON brut reçu : " + jsonResponse);

            // Si la réponse est un tableau JSON direct
            if (jsonResponse.startsWith("[")) {
                JSONArray commandesArray = new JSONArray(jsonResponse);
                StringBuilder commandesText = new StringBuilder();

                for (int i = 0; i < commandesArray.length(); i++) {
                    JSONObject commande = commandesArray.getJSONObject(i);
                    commandesText.append("Commande ID: ").append(commande.getInt("id")).append("\n");
                    commandesText.append("Client: ").append(commande.getString("client")).append("\n");
                    commandesText.append("Total: ").append(commande.getDouble("total")).append(" €\n");
                    commandesText.append("Statut: ").append(commande.getString("statut")).append("\n");
                    commandesText.append("----------------------------\n");
                }

                commandesArea.setText(commandesText.toString());
            } 
            // Si la réponse est un objet JSON qui contient un tableau
            else {
                JSONObject jsonObject = new JSONObject(jsonResponse);
                JSONArray commandesArray = jsonObject.getJSONArray("data"); // Adapte le nom de la clé si nécessaire
                StringBuilder commandesText = new StringBuilder();

                for (int i = 0; i < commandesArray.length(); i++) {
                    JSONObject commande = commandesArray.getJSONObject(i);
                    commandesText.append("Commande ID: ").append(commande.getInt("id")).append("\n");
                    commandesText.append("Client: ").append(commande.getString("client")).append("\n");
                    commandesText.append("Total: ").append(commande.getDouble("total")).append(" €\n");
                    commandesText.append("Statut: ").append(commande.getString("statut")).append("\n");
                    commandesText.append("----------------------------\n");
                }

                commandesArea.setText(commandesText.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            commandesArea.setText("Erreur lors du parsing des commandes.");
        }
    }

    // Méthode principale pour tester l'interface (avec un exemple de JWT)
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CommandeFrame frame = new CommandeFrame("exemple-de-token-jwt");
            frame.setVisible(true);
        });
    }
}



