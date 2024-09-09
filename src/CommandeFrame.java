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
            // Parser la réponse JSON
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray commandesArray = jsonObject.getJSONArray("hydra:member");  // Récupère le tableau de commandes

            // Vérifier si le tableau est vide
            if (commandesArray.length() == 0) {
                commandesArea.setText("Aucune commande trouvée.");
                return;
            }

            // Construire le texte des commandes
            StringBuilder commandesText = new StringBuilder();
            for (int i = 0; i < commandesArray.length(); i++) {
                JSONObject commande = commandesArray.getJSONObject(i);

                // Affiche la commande brute dans la console pour déboguer
                System.out.println("Commande brute: " + commande.toString());

                // Extraire l'ID de l'URI dans "@id"
                String commandeUri = commande.getString("@id");
                String[] uriParts = commandeUri.split("/");  // Découpe l'URI pour obtenir l'ID
                String commandeId = uriParts[uriParts.length - 1];  // L'ID est la dernière partie de l'URI

                // Extraire d'autres détails de la commande si disponibles
                String type = commande.optString("@type", "N/A");
                // Tu peux ajouter d'autres champs ici si disponibles dans la réponse, par exemple :
                // String client = commande.optString("client", "Inconnu");
                // double total = commande.optDouble("total", 0);

                // Ajoute les informations de la commande
                commandesText.append("Commande ID: ").append(commandeId).append("\n");
                commandesText.append("Type: ").append(type).append("\n");

                // Si tu as des champs supplémentaires à afficher, ajoute-les ici :
                // commandesText.append("Client: ").append(client).append("\n");
                // commandesText.append("Total: ").append(total).append(" €\n");

                commandesText.append("----------------------------\n");
            }

            // Afficher les commandes dans la zone de texte
            commandesArea.setText(commandesText.toString());
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




