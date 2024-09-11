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

public class CategorieApp extends Application {

    private String jwtToken;

    public CategorieApp(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Gestion des Catégories");

        // Créer les boutons pour les actions CRUD
        Button createButton = new Button("Créer");
        Button readButton = new Button("Lire les Catégories");
        Button updateButton = new Button("Mettre à jour");
        Button deleteButton = new Button("Supprimer");

        // Ajouter des actions aux boutons
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
        String[] options = {"Catégorie Principale", "Sous-Catégorie"};
        ChoiceDialog<String> dialog = new ChoiceDialog<>(options[0], options);
        dialog.setTitle("Création de Catégorie");
        dialog.setHeaderText("Que souhaitez-vous créer ?");
        dialog.setContentText("Choix :");

        dialog.showAndWait().ifPresent(choice -> {
            if (choice.equals("Catégorie Principale")) {
                createCategory(null);
            } else if (choice.equals("Sous-Catégorie")) {
                TextInputDialog parentDialog = new TextInputDialog();
                parentDialog.setTitle("Sous-Catégorie");
                parentDialog.setHeaderText("Entrez l'ID de la catégorie parent");
                parentDialog.setContentText("ID de la catégorie parent:");

                parentDialog.showAndWait().ifPresent(parentId -> {
                    if (parentId != null && !parentId.isEmpty()) {
                        createCategory(parentId);
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Erreur", "ID de catégorie parent invalide.");
                    }
                });
            }
        });
    }

    private void createCategory(String parentId) {
        TextInputDialog nameDialog = new TextInputDialog();
        nameDialog.setTitle("Nom de la Catégorie");
        nameDialog.setHeaderText("Entrez le nom de la catégorie");
        nameDialog.setContentText("Nom :");

        nameDialog.showAndWait().ifPresent(nom -> {
            if (nom != null && !nom.isEmpty()) {
                TextInputDialog imageDialog = new TextInputDialog();
                imageDialog.setTitle("Image de la Catégorie");
                imageDialog.setHeaderText("Entrez l'URL de l'image de la catégorie");
                imageDialog.setContentText("URL :");

                imageDialog.showAndWait().ifPresent(image -> {
                    if (image != null && !image.isEmpty()) {
                        JSONObject categoryData = new JSONObject();
                        categoryData.put("nom", nom);
                        categoryData.put("image", image);
                        if (parentId != null) {
                            categoryData.put("categorieParent", "/api/categories/" + parentId);
                        }

                        HttpClient client = HttpClient.newHttpClient();
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create("https://127.0.0.1:8000/api/categories"))
                                .header("Content-Type", "application/json")
                                .header("Authorization", "Bearer " + jwtToken)
                                .POST(HttpRequest.BodyPublishers.ofString(categoryData.toString()))
                                .build();

                        try {
                            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                            if (response.statusCode() == 201) {
                                showAlert(Alert.AlertType.INFORMATION, "Succès", "Catégorie créée avec succès !");
                            } else {
                                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la création de la catégorie : " + response.statusCode());
                            }
                        } catch (IOException | InterruptedException ex) {
                            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue : " + ex.getMessage());
                        }
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Erreur", "URL de l'image invalide.");
                    }
                });
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Nom de catégorie invalide.");
            }
        });
    }

    private void readEntities() {
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

                ListView<String> parentList = new ListView<>();
                ListView<String> subcategoryList = new ListView<>();

                for (int i = 0; i < categoriesArray.length(); i++) {
                    JSONObject category = categoriesArray.getJSONObject(i);

                    String id = category.getString("@id").split("/")[3];
                    String nom = category.optString("nom", "N/A");
                    String image = category.optString("image", "N/A");
                    String categorieParent = category.optString("categorieParent", null);

                    if (categorieParent == null || categorieParent.isEmpty()) {
                        parentList.getItems().add("ID: " + id + ", Nom: " + nom + ", Image: " + image);
                    } else {
                        subcategoryList.getItems().add("ID: " + id + ", Nom: " + nom + ", Image: " + image);
                    }
                }

                Stage resultStage = new Stage();
                resultStage.setTitle("Catégories");

                GridPane grid = new GridPane();
                grid.setVgap(10);
                grid.setHgap(10);

                grid.add(new Label("Catégories Parents"), 0, 0);
                grid.add(parentList, 0, 1);
                grid.add(new Label("Sous-Catégories"), 1, 0);
                grid.add(subcategoryList, 1, 1);

                Scene resultScene = new Scene(grid, 600, 400);
                resultStage.setScene(resultScene);
                resultStage.show();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la récupération des catégories : " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la récupération des catégories.");
        }
    }

    private void updateEntity() {
        TextInputDialog idDialog = new TextInputDialog();
        idDialog.setTitle("Mise à jour d'une Catégorie");
        idDialog.setHeaderText("Entrez l'ID de la catégorie à mettre à jour");
        idDialog.setContentText("ID :");

        idDialog.showAndWait().ifPresent(id -> {
            if (id != null && !id.isEmpty()) {
                TextInputDialog nomDialog = new TextInputDialog();
                nomDialog.setTitle("Nom de la Catégorie");
                nomDialog.setHeaderText("Entrez le nouveau nom de la catégorie");
                nomDialog.setContentText("Nom :");

                nomDialog.showAndWait().ifPresent(nom -> {
                    if (nom != null && !nom.isEmpty()) {
                        TextInputDialog imageDialog = new TextInputDialog();
                        imageDialog.setTitle("Image de la Catégorie");
                        imageDialog.setHeaderText("Entrez l'URL de la nouvelle image de la catégorie");
                        imageDialog.setContentText("URL :");

                        imageDialog.showAndWait().ifPresent(imagePath -> {
                            if (imagePath != null && !imagePath.isEmpty()) {
                                JSONObject categoryData = new JSONObject();
                                categoryData.put("nom", nom);
                                categoryData.put("image", imagePath);

                                HttpClient client = HttpClient.newHttpClient();
                                HttpRequest request = HttpRequest.newBuilder()
                                        .uri(URI.create("https://127.0.0.1:8000/api/categories/" + id))
                                        .header("Content-Type", "application/json")
                                        .header("Authorization", "Bearer " + jwtToken)
                                        .PUT(HttpRequest.BodyPublishers.ofString(categoryData.toString()))
                                        .build();

                                try {
                                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                                    if (response.statusCode() == 200) {
                                        showAlert(Alert.AlertType.INFORMATION, "Succès", "Catégorie mise à jour avec succès !");
                                    } else {
                                        showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la mise à jour de la catégorie : " + response.statusCode());
                                    }
                                } catch (IOException | InterruptedException e) {
                                    showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue : " + e.getMessage());
                                }
                            } else {
                                showAlert(Alert.AlertType.ERROR, "Erreur", "URL de l'image invalide.");
                            }
                        });
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Nom de catégorie invalide.");
                    }
                });
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "ID de catégorie invalide.");
            }
        });
    }

    private void deleteEntity() {
        TextInputDialog idDialog = new TextInputDialog();
        idDialog.setTitle("Suppression d'une Catégorie");
        idDialog.setHeaderText("Entrez l'ID de la catégorie à supprimer");
        idDialog.setContentText("ID :");

        idDialog.showAndWait().ifPresent(id -> {
            if (id != null && !id.isEmpty()) {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://127.0.0.1:8000/api/categories/" + id))
                        .header("Authorization", "Bearer " + jwtToken)
                        .DELETE()
                        .build();

                try {
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() == 204) {
                        showAlert(Alert.AlertType.INFORMATION, "Succès", "Catégorie supprimée avec succès !");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression de la catégorie : " + response.statusCode());
                    }
                } catch (IOException | InterruptedException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue : " + e.getMessage());
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "ID de catégorie invalide.");
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
