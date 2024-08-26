import javax.swing.*;

public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("Fenêtre Principale");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Ajoutez ici les composants que vous souhaitez pour la fenêtre principale
        JLabel welcomeLabel = new JLabel("Bienvenue dans l'application !");
        add(welcomeLabel);
    }
}
