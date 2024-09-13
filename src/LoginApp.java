import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LoginApp extends Application {

    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private String jwtToken;
    private Stage loginStage;

    @Override
    public void start(Stage primaryStage) {
        this.loginStage = primaryStage;

        // Configurer la fenêtre de connexion
        primaryStage.setTitle("Connexion");

        // Créer les composants
        usernameField = new TextField();
        passwordField = new PasswordField();
        loginButton = new Button("Se connecter");

        // Disposition des composants dans une grille
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Nom d'utilisateur:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Mot de passe:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(loginButton, 1, 2);

        // Ajouter l'action du bouton de connexion
        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            // Authentifier l'utilisateur
            try {
                if (authenticate(username, password)) {
                    showCustomSuccessDialog();
                    openMainWindow();
                } else {
                    showErrorAlert("Nom d'utilisateur ou mot de passe incorrect.");
                }
            } catch (IOException | InterruptedException ex) {
                ex.printStackTrace();
                showErrorAlert("Erreur lors de la connexion à l'API.");
            }
        });

        // Créer la scène et l'ajouter à la fenêtre
        Scene scene = new Scene(grid, 300, 150);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Méthode pour authentifier l'utilisateur via l'API Symfony
    private boolean authenticate(String username, String password) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        String json = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", username, password);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://127.0.0.1:8000/api/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            jwtToken = parseTokenFromResponse(response.body());
            return true;
        } else {
            return false;
        }
    }

    // Méthode pour extraire le token JWT de la réponse JSON
    private String parseTokenFromResponse(String responseBody) {
        return responseBody.split(":")[1].replaceAll("[\"{}]", "").trim();
    }

    // Méthode pour afficher un message de succès personnalisé
    private void showCustomSuccessDialog() {
        // Création de la fenêtre modale
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.setTitle("Connexion Réussie");

        // Contenu de la fenêtre
        Label successLabel = new Label("Connexion réussie !");
        successLabel.setFont(new Font("Arial", 16));

        Button closeButton = new Button("OK");
        closeButton.setOnAction(e -> dialogStage.close());

        VBox vbox = new VBox(20, successLabel, closeButton);
        vbox.setAlignment(Pos.CENTER);
        vbox.setStyle("-fx-padding: 20px;");

        Scene dialogScene = new Scene(vbox, 250, 150);
        dialogStage.setScene(dialogScene);
        dialogStage.showAndWait(); // Attendre que l'utilisateur ferme la fenêtre
    }

    // Méthode pour afficher un message d'erreur
    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Méthode pour ouvrir la fenêtre principale après connexion
    private void openMainWindow() {
        loginStage.close(); // Fermer la fenêtre de connexion

        Stage mainStage = new Stage();
        MainApp mainApp = new MainApp(jwtToken); // Créer une instance de MainApp avec le token JWT
        try {
            mainApp.start(mainStage); // Ouvre la fenêtre principale
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args); // Lancer l'application JavaFX
    }

}
