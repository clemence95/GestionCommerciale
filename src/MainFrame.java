import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("Fenêtre Principale");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Créer un label de bienvenue
        JLabel welcomeLabel = new JLabel("Bienvenue dans l'application !");
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Créer un bouton pour quitter l'application
        JButton quitButton = new JButton("Quitter l'application");

        // Ajouter une action au bouton pour fermer l'application
        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Fermer l'application
                System.exit(0);
            }
        });

        // Ajouter les composants à la fenêtre
        setLayout(new BorderLayout());
        add(welcomeLabel, BorderLayout.CENTER);
        add(quitButton, BorderLayout.SOUTH); // Le bouton est placé en bas de la fenêtre
    }

    public static void main(String[] args) {
        // Créer et afficher la fenêtre
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);
            }
        });
    }
}

