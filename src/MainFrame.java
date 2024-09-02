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
        buttonPanel.setLayout(new GridLayout(1, 2, 10, 10));

        JButton categorieButton = new JButton("Catégories");
        JButton produitButton = new JButton("Produits");

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

        buttonPanel.add(categorieButton);
        buttonPanel.add(produitButton);

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









