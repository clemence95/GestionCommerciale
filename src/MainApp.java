import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class MainApp extends Application {

    private String jwtToken;

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

        // Créer les boutons
        Button categorieButton = new Button("Catégories");
        Button produitButton = new Button("Produits");
        Button commandeButton = new Button("Commandes");

        // Ajouter des actions pour les boutons
        categorieButton.setOnAction(e -> openCategorieWindow());
        produitButton.setOnAction(e -> openProduitWindow());
        commandeButton.setOnAction(e -> openCommandeWindow());

        // Disposition des boutons
        HBox buttonPanel = new HBox(10);
        buttonPanel.getChildren().addAll(categorieButton, produitButton, commandeButton);
        buttonPanel.setStyle("-fx-alignment: center;");

        // Label pour afficher le token
        Label tokenLabel = new Label("Token JWT : " + jwtToken);
        tokenLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: grey;");

        // Utiliser un BorderPane pour disposer les composants
        BorderPane root = new BorderPane();
        root.setTop(welcomeLabel);
        root.setCenter(buttonPanel);
        root.setBottom(tokenLabel);  // Afficher le token en bas

        // Créer la scène et l'ajouter à la fenêtre
        Scene scene = new Scene(root, 400, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Méthodes pour ouvrir les différentes fenêtres
    private void openCategorieWindow() {
        System.out.println("Ouverture de la fenêtre des catégories avec le token : " + jwtToken);
        // Logique pour ouvrir la fenêtre des catégories et utiliser le jwtToken
    }

    private void openProduitWindow() {
        System.out.println("Ouverture de la fenêtre des produits avec le token : " + jwtToken);
        // Logique pour ouvrir la fenêtre des produits et utiliser le jwtToken
    }

    private void openCommandeWindow() {
        System.out.println("Ouverture de la fenêtre des commandes avec le token : " + jwtToken);
        // Logique pour ouvrir la fenêtre des commandes et utiliser le jwtToken
    }

    public static void main(String[] args) {
        launch(args);  // Lancer l'application JavaFX
    }
}












