import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainFrame extends JFrame {

    private String jwtToken;

    public MainFrame(String jwtToken) {
        this.jwtToken = jwtToken;

        setTitle("Village Green");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel welcomeLabel = new JLabel("Bienvenue dans l'application !");
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 3, 10, 10)); // Ajout d'une colonne pour le 3ème bouton

        JButton categorieButton = new JButton("Catégories");
        JButton produitButton = new JButton("Produits");
        JButton commandeButton = new JButton("Commandes");  // Nouveau bouton

        categorieButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openCategorieFrame();
            }
        });

        produitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openProduitFrame();
            }
        });

        commandeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openCommandeFrame();  // Ajout d'une action pour ouvrir la fenêtre Commande
            }
        });

        buttonPanel.add(categorieButton);
        buttonPanel.add(produitButton);
        buttonPanel.add(commandeButton);  // Ajout du bouton dans le panel

        setLayout(new BorderLayout());
        add(welcomeLabel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
    }

    private void openCategorieFrame() {
        CategorieFrame categorieFrame = new CategorieFrame(jwtToken);
        categorieFrame.setVisible(true);
    }

    private void openProduitFrame() {
        ProduitFrame produitFrame = new ProduitFrame(jwtToken);
        produitFrame.setVisible(true);
    }

    // Nouvelle méthode pour ouvrir la fenêtre de Commandes
    private void openCommandeFrame() {
        CommandeFrame commandeFrame = new CommandeFrame(jwtToken); // Par exemple, une fenêtre Commande
        commandeFrame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainFrame mainFrame = new MainFrame("exemple-de-token-jwt");
                mainFrame.setVisible(true);
            }
        });
    }
}










