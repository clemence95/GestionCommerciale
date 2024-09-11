import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
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

    @Override
    public void start(Stage primaryStage) {
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
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Connexion réussie !");
                    alert.showAndWait();
                    openMainWindow();  // Ouvrir la fenêtre principale après connexion
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Nom d'utilisateur ou mot de passe incorrect.");
                    alert.showAndWait();
                }
            } catch (IOException | InterruptedException ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de la connexion à l'API.");
                alert.showAndWait();
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
            // Extraire le token JWT de la réponse
            jwtToken = parseTokenFromResponse(response.body());
            return true;
        } else {
            return false;
        }
    }

    // Méthode pour extraire le token JWT de la réponse JSON
    private String parseTokenFromResponse(String responseBody) {
        // Simplifié : en supposant que le token est dans le format {"token":"eyJhbGc..."}
        return responseBody.split(":")[1].replaceAll("[\"{}]", "").trim();
    }

    // Méthode pour ouvrir la fenêtre principale après connexion
    private void openMainWindow() {
        // Fermer la fenêtre de connexion
        Stage mainStage = new Stage();
        MainApp mainApp = new MainApp(jwtToken);  // Passer le jwtToken à la fenêtre principale
        try {
            mainApp.start(mainStage);  // Ouvre la fenêtre principale
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);  // Lancer l'application JavaFX
    }
}





