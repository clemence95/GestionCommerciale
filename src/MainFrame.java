import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainFrame extends JFrame {

    private String jwtToken;

    public MainFrame(String jwtToken) {
        this.jwtToken = jwtToken;

        setTitle("Village Green");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel welcomeLabel = new JLabel("Bienvenue dans l'application !");
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel crudPanel = new JPanel(new GridLayout(2, 2, 10, 10));

        JButton createButton = new JButton("Créer");
        JButton readButton = new JButton("Lire les Catégories");
        JButton updateButton = new JButton("Mettre à jour");
        JButton deleteButton = new JButton("Supprimer");

        crudPanel.add(createButton);
        crudPanel.add(readButton);
        crudPanel.add(updateButton);
        crudPanel.add(deleteButton);

        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createEntity();
            }
        });

        readButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                readEntities();
            }
        });

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateEntity();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteEntity();
            }
        });

        JButton quitButton = new JButton("Quitter l'application");
        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        setLayout(new BorderLayout());
        add(welcomeLabel, BorderLayout.NORTH);
        add(crudPanel, BorderLayout.CENTER);
        add(quitButton, BorderLayout.SOUTH);
    }

    private void createEntity() {
        System.out.println("Créer une nouvelle entité");
    }

    private void readEntities() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://127.0.0.1:8000/api/categories"))
                .header("Authorization", "Bearer " + jwtToken)
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String responseBody = response.body();
                System.out.println("Réponse JSON complète reçue de l'API : " + responseBody);

                JSONObject jsonResponse = new JSONObject(responseBody);
                JSONArray categoriesArray = jsonResponse.getJSONArray("hydra:member");

                DefaultListModel<String> listModel = new DefaultListModel<>();
                for (int i = 0; i < categoriesArray.length(); i++) {
                    JSONObject category = categoriesArray.getJSONObject(i);

                    // Afficher tout l'objet JSON pour vérifier sa structure
                    System.out.println("Objet JSON de la catégorie : " + category.toString());

                    // Extraction des informations
                    String idUri = category.getString("@id");
                    String[] parts = idUri.split("/");
                    String id = parts[parts.length - 1];

                    String nom = category.optString("nom", "N/A");
                    String image = category.optString("image", "N/A");

                    listModel.addElement("ID: " + id + ", Nom: " + nom + ", Image: " + image);
                }

                JList<String> categoriesList = new JList<>(listModel);
                JScrollPane scrollPane = new JScrollPane(categoriesList);

                JFrame resultFrame = new JFrame("Catégories");
                resultFrame.setSize(400, 300);
                resultFrame.add(scrollPane);
                resultFrame.setLocationRelativeTo(null);
                resultFrame.setVisible(true);

            } else {
                System.out.println("Erreur lors de la récupération des catégories : " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void updateEntity() {
        System.out.println("Mettre à jour une entité");
    }

    private void deleteEntity() {
        System.out.println("Supprimer une entité");
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




