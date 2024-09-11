import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class MainApp extends Application {

    private String jwtToken;
    private ListView<String> parentCategoriesListView;
    private ListView<String> subCategoriesListView;

    public MainApp() {
        // Constructeur par défaut
    }

    public MainApp(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    @Override
    public void start(Stage primaryStage) {
        // Configurer la fenêtre principale
        primaryStage.setTitle("Village Green");

        // Label de bienvenue
        Label welcomeLabel = new Label("Bienvenue dans l'application !");
        welcomeLabel.setStyle("-fx-font-size: 16px; -fx-alignment: center;");

        // Créer les ListViews pour les catégories parents et sous-catégories
        parentCategoriesListView = new ListView<>();
        subCategoriesListView = new ListView<>();

        // Créer les boutons
        Button loadCategoriesButton = new Button("Charger Catégories et Produits");
        loadCategoriesButton.setOnAction(e -> loadCategoriesAndProducts());

        // Disposition des composants
        VBox categoriesBox = new VBox(10, new Label("Catégories Parents"), parentCategoriesListView, new Label("Sous-Catégories"), subCategoriesListView);
        categoriesBox.setStyle("-fx-padding: 10px");

        HBox buttonPanel = new HBox(10);
        buttonPanel.getChildren().add(loadCategoriesButton);
        buttonPanel.setStyle("-fx-alignment: center;");

        // Utiliser un BorderPane pour disposer les composants
        BorderPane root = new BorderPane();
        root.setTop(welcomeLabel);
        root.setCenter(categoriesBox);
        root.setBottom(buttonPanel);

        // Créer la scène et l'ajouter à la fenêtre
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Méthode pour charger les catégories et leurs produits associés
    private void loadCategoriesAndProducts() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://127.0.0.1:8000/api/categories"))
                .header("Authorization", "Bearer " + jwtToken)
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject jsonResponse = new JSONObject(response.body());
                JSONArray categoriesArray = jsonResponse.getJSONArray("hydra:member");

                // Vider les listes actuelles
                parentCategoriesListView.getItems().clear();
                subCategoriesListView.getItems().clear();

                // Parcourir les catégories et afficher toutes les informations
                for (int i = 0; i < categoriesArray.length(); i++) {
                    JSONObject category = categoriesArray.getJSONObject(i);

                    // Récupérer toutes les informations pertinentes
                    String id = category.getString("@id").split("/")[3];
                    String nom = category.optString("nom", "N/A");
                    String image = category.optString("image", "Pas d'image");
                    String categorieParent = category.optString("categorieParent", "Pas de parent");
                    String description = category.optString("description", "Pas de description");

                    // Charger les produits pour cette catégorie
                    StringBuilder produitsInfo = new StringBuilder();
                    loadProductsForCategory(id, produitsInfo);

                    // Créer un texte pour afficher toutes les informations de catégorie et produits
                    String categoryInfo = String.format("ID: %s\nNom: %s\nImage: %s\nDescription: %s\nProduits:\n%s",
                            id, nom, image, description, produitsInfo.toString());

                    if (categorieParent.equals("Pas de parent")) {
                        // Ajouter à la liste des catégories parents
                        parentCategoriesListView.getItems().add(categoryInfo);
                    } else {
                        // Ajouter à la liste des sous-catégories
                        subCategoriesListView.getItems().add(categoryInfo);
                    }
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la récupération des catégories : " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la récupération des catégories.");
        }
    }

    // Méthode pour récupérer les produits d'une catégorie
    private void loadProductsForCategory(String categoryId, StringBuilder produitsInfo) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://127.0.0.1:8000/api/produits?categorie=" + categoryId))  // Assume que l'API supporte la filtration par catégorie
                .header("Authorization", "Bearer " + jwtToken)
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject jsonResponse = new JSONObject(response.body());
                JSONArray produitsArray = jsonResponse.getJSONArray("hydra:member");

                for (int i = 0; i < produitsArray.length(); i++) {
                    JSONObject produit = produitsArray.getJSONObject(i);
                    String produitNom = produit.optString("nom", "N/A");
                    double prixVente = produit.optDouble("prixVente", 0.0);
                    produitsInfo.append("Produit: ").append(produitNom).append(", Prix: ").append(prixVente).append(" €\n");
                }
            } else {
                produitsInfo.append("Erreur lors de la récupération des produits (Code: ").append(response.statusCode()).append(")\n");
            }
        } catch (IOException | InterruptedException e) {
            produitsInfo.append("Erreur lors de la récupération des produits.\n");
        }
    }

    // Méthode pour afficher une alerte
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);  // Lancer l'application JavaFX
    }
}















