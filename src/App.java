import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        // Logique pour démarrer l'application (ex: fenêtre de connexion)
        primaryStage.setTitle("Login");
        // Configurez la fenêtre ici...
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
