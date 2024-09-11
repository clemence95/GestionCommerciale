import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CommandeApp extends Application {

    private String jwtToken;

    public CommandeApp(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Gestion des Commandes");

        // Créer la zone de texte pour afficher les commandes
        TextArea commandesArea = new TextArea();
        commandesArea.setEditable(false);

        // Créer une ScrollPane pour permettre le défilement du texte
        ScrollPane scrollPane = new ScrollPane(commandesArea);
        scrollPane.setFitToWidth(true);

        // Créer la scène et l'ajouter à la fenêtre
        Scene scene = new Scene(scrollPane, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Récupérer les commandes depuis l'API
        getCommandesFromAPI(commandesArea);
    }

    // Méthode pour récupérer les commandes depuis l'API
    private void getCommandesFromAPI(TextArea commandesArea) {
        try {
            // Créer le client HTTP
            HttpClient httpClient = HttpClient.newHttpClient();

            // Créer la requête HTTP
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://127.0.0.1:8000/api/commandes"))
                    .header("Authorization", "Bearer " + jwtToken)
                    .GET()
                    .build();

            // Envoyer la requête et obtenir la réponse
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

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
    private void parseCommandes(String jsonResponse, TextArea commandesArea) {
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

                // Requête supplémentaire pour obtenir les détails de la commande
                String detailsCommande = getCommandeDetails(commandeUri);
                commandesText.append(detailsCommande).append("\n");
                commandesText.append("----------------------------\n");
            }

            // Afficher les commandes dans la zone de texte
            commandesArea.setText(commandesText.toString());
        } catch (Exception e) {
            e.printStackTrace();
            commandesArea.setText("Erreur lors du parsing des commandes.");
        }
    }

    // Méthode pour obtenir les détails d'une commande en faisant une requête supplémentaire
    private String getCommandeDetails(String commandeUri) {
        try {
            // Créer le client HTTP
            HttpClient httpClient = HttpClient.newHttpClient();

            // Créer la requête HTTP pour l'URI spécifique de la commande
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://127.0.0.1:8000" + commandeUri))  // Complète l'URI avec l'URL de base
                    .header("Authorization", "Bearer " + jwtToken)
                    .GET()
                    .build();

            // Envoyer la requête et obtenir la réponse
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject commandeDetails = new JSONObject(response.body());

                // Extraire les détails de la commande (ajoute ici tous les champs que tu veux)
                String commandeId = commandeDetails.getString("@id").split("/")[3];
                String clientNom = commandeDetails.optString("client", "Inconnu");
                double total = commandeDetails.optDouble("total", 0.0);
                String statut = commandeDetails.optString("statut", "Inconnu");

                return "Commande ID: " + commandeId + "\nClient: " + clientNom + "\nTotal: " + total + " €\nStatut: " + statut;
            } else {
                return "Erreur lors de la récupération des détails de la commande (Code " + response.statusCode() + ")";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur lors de la récupération des détails de la commande.";
        }
    }

    // Méthode principale pour tester l'interface (avec un exemple de JWT)
    public static void main(String[] args) {
        launch(args);  // Lancer l'application JavaFX
    }
}






