import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainApp extends Application {

    private String jwtToken;

    // Constructeur avec token JWT
    public MainApp(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Menu Principal");

        // Créer des boutons pour les redirections vers différentes parties de l'application
        Button categoriesButton = new Button("Gestion des Catégories");
        categoriesButton.setOnAction(e -> openCategoriesWindow());

        Button produitsButton = new Button("Gestion des Produits");
        produitsButton.setOnAction(e -> openProduitsWindow());

        Button commandesButton = new Button("Gestion des Commandes");
        commandesButton.setOnAction(e -> openCommandesWindow());

        // Layout pour les boutons
        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(categoriesButton, produitsButton, commandesButton);
        vbox.setId("vbox");  // Appliquer un ID pour styliser via le CSS

        // Créer la scène
        Scene scene = new Scene(vbox, 300, 200);

        // Charger et appliquer le fichier CSS
        scene.getStylesheets().add(getClass().getResource("ressources/Style.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Méthodes pour ouvrir les autres fenêtres
    private void openCategoriesWindow() {
        Categorie2App categorieApp = new Categorie2App(jwtToken);
        Stage categorieStage = new Stage();
        try {
            categorieApp.start(categorieStage);  // Ouvre la fenêtre des catégories
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openProduitsWindow() {
        ProduitApp produitApp = new ProduitApp(jwtToken);
        Stage produitStage = new Stage();
        try {
            produitApp.start(produitStage);  // Ouvre la fenêtre des produits
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openCommandesWindow() {
        CommandeApp commandeApp = new CommandeApp(jwtToken);
        Stage commandeStage = new Stage();
        try {
            commandeApp.start(commandeStage);  // Ouvre la fenêtre des commandes
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);  // Lancer l'application
    }
}
