import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ProduitApp extends Application {

    private String jwtToken;

    public ProduitApp(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Gestion des Produits");

        // Créer les boutons pour les actions CRUD
        Button createButton = new Button("Créer");
        Button readButton = new Button("Lire les Produits");
        Button updateButton = new Button("Mettre à jour");
        Button deleteButton = new Button("Supprimer");

        // Ajouter des actions pour les boutons
        createButton.setOnAction(e -> createEntity());
        readButton.setOnAction(e -> readEntities());
        updateButton.setOnAction(e -> updateEntity());
        deleteButton.setOnAction(e -> deleteEntity());

        // Disposer les boutons verticalement
        VBox crudBox = new VBox(10, createButton, readButton, updateButton, deleteButton);
        crudBox.setStyle("-fx-padding: 20px");

        // Créer la scène et l'ajouter à la fenêtre
        Scene scene = new Scene(crudBox, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void createEntity() {
        // Créer une fenêtre pour saisir les données du produit
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Créer un produit");

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);

        TextField libelleCourtField = new TextField();
        TextField libelleLongField = new TextField();
        TextField referenceFournisseurField = new TextField();
        TextField prixAchatField = new TextField();
        TextField prixVenteField = new TextField();
        TextField stockField = new TextField();
        CheckBox actifField = new CheckBox("Actif");
        TextField sousCategorieIdField = new TextField();
        TextField photoField = new TextField();
        TextField fournisseurIdField = new TextField();

        grid.add(new Label("Nom du produit :"), 0, 0);
        grid.add(libelleCourtField, 1, 0);
        grid.add(new Label("Description :"), 0, 1);
        grid.add(libelleLongField, 1, 1);
        grid.add(new Label("Référence fournisseur :"), 0, 2);
        grid.add(referenceFournisseurField, 1, 2);
        grid.add(new Label("Prix d'achat :"), 0, 3);
        grid.add(prixAchatField, 1, 3);
        grid.add(new Label("Prix de vente :"), 0, 4);
        grid.add(prixVenteField, 1, 4);
        grid.add(new Label("Stock :"), 0, 5);
        grid.add(stockField, 1, 5);
        grid.add(new Label("Sous-catégorie ID :"), 0, 6);
        grid.add(sousCategorieIdField, 1, 6);
        grid.add(new Label("Photo :"), 0, 7);
        grid.add(photoField, 1, 7);
        grid.add(new Label("Fournisseur ID :"), 0, 8);
        grid.add(fournisseurIdField, 1, 8);
        grid.add(actifField, 1, 9);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    JSONObject produitData = new JSONObject();
                    produitData.put("libelleCourt", libelleCourtField.getText());
                    produitData.put("libelleLong", libelleLongField.getText());
                    produitData.put("referenceFournisseur", referenceFournisseurField.getText());
                    produitData.put("prixAchat", Double.parseDouble(prixAchatField.getText()));
                    produitData.put("prixVente", Double.parseDouble(prixVenteField.getText()));
                    produitData.put("stock", Integer.parseInt(stockField.getText()));
                    produitData.put("actif", actifField.isSelected());
                    produitData.put("sousCategorie", "/api/categories/" + sousCategorieIdField.getText());
                    produitData.put("photo", photoField.getText());
                    produitData.put("idFournisseur", "/api/fournisseurs/" + fournisseurIdField.getText());

                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("https://127.0.0.1:8000/api/produits"))
                            .header("Content-Type", "application/json")
                            .header("Authorization", "Bearer " + jwtToken)
                            .POST(HttpRequest.BodyPublishers.ofString(produitData.toString()))
                            .build();

                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 201) {
                        showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit créé avec succès !");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la création du produit : " + response.statusCode());
                    }
                } catch (IOException | InterruptedException ex) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'envoi de la requête : " + ex.getMessage());
                }
            }
        });
    }

    private void readEntities() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://127.0.0.1:8000/api/produits"))
                .header("Authorization", "Bearer " + jwtToken)
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject jsonResponse = new JSONObject(response.body());
                JSONArray produitsArray = jsonResponse.getJSONArray("hydra:member");

                ListView<String> produitsList = new ListView<>();
                for (int i = 0; i < produitsArray.length(); i++) {
                    JSONObject produit = produitsArray.getJSONObject(i);
                    String id = produit.getString("@id").split("/")[3];  // Extraire l'ID de l'URI
                    String nom = produit.optString("libelleCourt", "N/A");
                    String description = produit.optString("libelleLong", "N/A");
                    double prix = produit.optDouble("prixVente", 0.0);

                    produitsList.getItems().add("ID: " + id + ", Nom: " + nom + ", Description: " + description + ", Prix: " + prix);
                }

                Stage resultStage = new Stage();
                resultStage.setTitle("Produits");
                Scene resultScene = new Scene(new VBox(produitsList), 400, 300);
                resultStage.setScene(resultScene);
                resultStage.show();

            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la récupération des produits : " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void updateEntity() {
        // Similaire à createEntity(), mais envoyer une requête PUT pour mettre à jour le produit
    }

    private void deleteEntity() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Supprimer un produit");
        dialog.setHeaderText("Entrez l'ID du produit à supprimer :");
        dialog.setContentText("ID :");

        dialog.showAndWait().ifPresent(id -> {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://127.0.0.1:8000/api/produits/" + id))
                    .header("Authorization", "Bearer " + jwtToken)
                    .DELETE()
                    .build();

            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 204) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit supprimé avec succès !");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression du produit : " + response.statusCode());
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

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



