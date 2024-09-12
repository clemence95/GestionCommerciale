import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
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

    public MainApp(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    @Override
    public void start(Stage primaryStage) {
        setupPrimaryStage(primaryStage);
    }

    private void setupPrimaryStage(Stage primaryStage) {
        primaryStage.setTitle("Village Green");

        Label welcomeLabel = new Label("Bienvenue dans l'application !");
        welcomeLabel.setStyle("-fx-font-size: 16px; -fx-alignment: center;");

        parentCategoriesListView = new ListView<>();
        subCategoriesListView = new ListView<>();

        setupListViewEvents();

        // Bouton pour charger les catégories et sous-catégories
        Button loadCategoriesButton = new Button("Charger Catégories et Sous-Catégories");
        loadCategoriesButton.setOnAction(e -> loadCategoriesAndSubCategories());

        // Ajout d'un bouton "Créer" pour ajouter une nouvelle catégorie
        Button createCategoryButton = new Button("Créer Catégorie/Sous-Catégorie");
        createCategoryButton.setOnAction(e -> openCategoryEditor(null, true, true)); // Ouvre l'éditeur pour créer une nouvelle catégorie parent

        VBox categoriesBox = new VBox(10, new Label("Catégories Parents"), parentCategoriesListView, new Label("Sous-Catégories"), subCategoriesListView);
        categoriesBox.setStyle("-fx-padding: 10px");

        HBox buttonPanel = new HBox(10);
        buttonPanel.getChildren().addAll(loadCategoriesButton, createCategoryButton); // Ajouter le bouton Créer ici
        buttonPanel.setStyle("-fx-alignment: center;");

        BorderPane root = new BorderPane();
        root.setTop(welcomeLabel);
        root.setCenter(categoriesBox);
        root.setBottom(buttonPanel);

        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Méthode pour gérer les événements sur les ListViews
    private void setupListViewEvents() {
        parentCategoriesListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedCategory = parentCategoriesListView.getSelectionModel().getSelectedItem();
                if (selectedCategory != null) {
                    openCategoryEditor(selectedCategory, true, false); // Modifier une catégorie parent
                }
            }
        });

        subCategoriesListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedCategory = subCategoriesListView.getSelectionModel().getSelectedItem();
                if (selectedCategory != null) {
                    openCategoryEditor(selectedCategory, false, false); // Modifier une sous-catégorie
                }
            }
        });
    }

    // Méthode pour charger les catégories et sous-catégories
    private void loadCategoriesAndSubCategories() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://127.0.0.1:8000/api/categories"))
                .header("Authorization", "Bearer " + jwtToken)
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                processCategoriesResponse(response.body());
            } else {
                handleError(response.statusCode(), "Erreur lors de la récupération des catégories.");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la récupération des catégories.");
        }
    }

    // Méthode pour traiter la réponse des catégories et inclure l'image
    private void processCategoriesResponse(String responseBody) {
        JSONObject jsonResponse = new JSONObject(responseBody);
        JSONArray categoriesArray = jsonResponse.getJSONArray("hydra:member");

        parentCategoriesListView.getItems().clear();
        subCategoriesListView.getItems().clear();

        for (int i = 0; i < categoriesArray.length(); i++) {
            JSONObject category = categoriesArray.getJSONObject(i);
            String id = category.getString("@id").split("/")[3]; // Récupère l'ID de la catégorie
            String nom = category.optString("nom", "N/A");
            String image = category.optString("image", "default.jpg"); // Récupère l'image ou utilise une valeur par défaut

            // Vérifie si la catégorie a un parent
            String categorieParent = category.isNull("categorieParent") ? "Pas de parent" : category.getJSONObject("categorieParent").getString("@id");

            // Inclure l'image dans les informations affichées
            String categoryInfo = String.format("ID: %s\nNom: %s\nImage: %s", id, nom, image);

            // Si la catégorie n'a pas de parent, on l'ajoute à parentCategoriesListView
            if (categorieParent.equals("Pas de parent")) {
                parentCategoriesListView.getItems().add(categoryInfo);
            } else {
                subCategoriesListView.getItems().add(categoryInfo + "\nCatégorie Parent: " + categorieParent);
            }
        }
    }

    // Méthode pour ouvrir l'éditeur de catégorie ou sous-catégorie (création ou modification)
    private void openCategoryEditor(String selectedCategory, boolean isParentCategory, boolean isCreateMode) {
        Stage editorStage = new Stage();
        editorStage.setTitle(isCreateMode ? "Créer une Catégorie" : "Modifier la Catégorie");
        editorStage.initModality(Modality.APPLICATION_MODAL);

        // Initialiser les champs
        TextField nameField = new TextField();
        TextField imageField = new TextField("default.jpg"); // Champ pour entrer/modifier l'image
        TextField categorieParentField = null;

        // Déclaration de l'ID en dehors de la classe interne
        final String[] id = {null};

        // Si on est en mode modification, on extrait les informations de la catégorie
        if (!isCreateMode && selectedCategory != null) {
            String[] categoryDetails = selectedCategory.split("\n");
            id[0] = categoryDetails[0].split(":")[1].trim(); // L'ID est stocké dans un tableau final pour être accessible dans les classes internes
            String nom = categoryDetails[1].split(":")[1].trim();
            String image = categoryDetails[2].split(":")[1].trim();
            nameField.setText(nom);
            imageField.setText(image);

            if (!isParentCategory) {
                String categorieParent = categoryDetails[3].split(":")[1].trim();
                categorieParentField = new TextField(categorieParent); // Champ pour le parent
            }
        }

        // Si c'est une sous-catégorie, on affiche également le champ pour le parent
        if (!isParentCategory) {
            if (categorieParentField == null) {
                categorieParentField = new TextField(); // Champ vide si on est en mode création
            }
        }

        Button saveButton = new Button(isCreateMode ? "Créer" : "Enregistrer");
        Button deleteButton = null;
        if (!isCreateMode) {
            deleteButton = new Button("Supprimer"); // Ajout du bouton Supprimer pour le mode modification
        }

        TextField finalCategorieParentField = categorieParentField;
        saveButton.setOnAction(e -> {
            String updatedName = nameField.getText();
            String updatedImage = imageField.getText(); // Récupérer l'image mise à jour
            String updatedParent = (finalCategorieParentField != null) ? finalCategorieParentField.getText() : null;

            if (isCreateMode) {
                createCategoryOnServer(updatedName, updatedImage, updatedParent); // Créer une nouvelle catégorie ou sous-catégorie
            } else {
                updateCategoryOnServer(id[0], updatedName, updatedImage, updatedParent); // Mettre à jour la catégorie ou sous-catégorie
            }
            editorStage.close();
        });

        // Action du bouton Supprimer (uniquement en mode modification)
        if (deleteButton != null) {
            deleteButton.setOnAction(e -> {
                deleteCategoryFromServer(id[0]); // Supprime la catégorie
                editorStage.close();
            });
        }

        VBox editorLayout = new VBox(10, new Label("Nom de la catégorie"), nameField, new Label("Image de la catégorie"), imageField);
        if (!isParentCategory) {
            editorLayout.getChildren().addAll(new Label("Catégorie Parent"), categorieParentField);
        }
        if (deleteButton != null) {
            editorLayout.getChildren().addAll(saveButton, deleteButton); // Ajouter le bouton Supprimer uniquement si on est en mode modification
        } else {
            editorLayout.getChildren().add(saveButton); // Ajouter uniquement le bouton Enregistrer pour le mode création
        }
        editorLayout.setStyle("-fx-padding: 10px");

        Scene editorScene = new Scene(editorLayout, 300, 200);
        editorStage.setScene(editorScene);
        editorStage.show();
    }

    // Méthode pour créer une nouvelle catégorie ou sous-catégorie sur le serveur
    private void createCategoryOnServer(String name, String image, String parentCategoryId) {
        HttpClient client = HttpClient.newHttpClient();

        // Création de l'objet JSON pour la nouvelle catégorie
        JSONObject newCategoryData = new JSONObject();
        newCategoryData.put("nom", name);
        newCategoryData.put("image", image);

        // Si c'est une sous-catégorie, on précise le parent
        if (parentCategoryId != null) {
            newCategoryData.put("categorieParent", parentCategoryId);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://127.0.0.1:8000/api/categories"))
                .header("Authorization", "Bearer " + jwtToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(newCategoryData.toString()))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201) {  // 201 = Created
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Catégorie créée avec succès.");
                loadCategoriesAndSubCategories();  // Recharger les catégories après création
            } else {
                handleError(response.statusCode(), "Erreur lors de la création de la catégorie.");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la création de la catégorie.");
        }
    }

    // Méthode pour mettre à jour une catégorie ou sous-catégorie
    private void updateCategoryOnServer(String categoryId, String updatedName, String updatedImage, String updatedParent) {
        HttpClient client = HttpClient.newHttpClient();

        // Création de l'objet JSON pour la mise à jour de la catégorie
        JSONObject updatedCategoryData = new JSONObject();
        updatedCategoryData.put("nom", updatedName);
        updatedCategoryData.put("image", updatedImage); // Inclusion de l'image mise à jour

        // Si une sous-catégorie, on met à jour le parent
        if (updatedParent != null) {
            updatedCategoryData.put("categorieParent", updatedParent);
        } else {
            updatedCategoryData.put("categorieParent", JSONObject.NULL);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://127.0.0.1:8000/api/categories/" + categoryId))
                .header("Authorization", "Bearer " + jwtToken) // Assurez-vous que jwtToken est valide
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(updatedCategoryData.toString()))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Catégorie mise à jour avec succès.");
            } else {
                System.out.println("Erreur HTTP " + response.statusCode());
                System.out.println("Corps de la réponse : " + response.body());
                handleError(response.statusCode(), "Échec de la mise à jour de la catégorie.");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la mise à jour de la catégorie.");
        }
    }

    // Méthode pour supprimer une catégorie
    private void deleteCategoryFromServer(String categoryId) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://127.0.0.1:8000/api/categories/" + categoryId))
                .header("Authorization", "Bearer " + jwtToken)
                .DELETE()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 204) { // 204 = No Content, succès de la suppression
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Catégorie supprimée avec succès.");
                loadCategoriesAndSubCategories(); // Recharger les catégories après suppression
            } else {
                handleError(response.statusCode(), "Erreur lors de la suppression de la catégorie.");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression de la catégorie.");
        }
    }

    // Méthode pour gérer les erreurs
    private void handleError(int statusCode, String message) {
        System.out.println("Erreur : " + statusCode);
        showAlert(Alert.AlertType.ERROR, "Erreur", message + " Code : " + statusCode);
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





















