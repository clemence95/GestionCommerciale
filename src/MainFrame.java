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
        String[] options = {"Catégorie Principale", "Sous-Catégorie"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Que souhaitez-vous créer ?",
                "Création de Catégorie",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) {
            createCategory(null);
        } else if (choice == 1) {
            String parentId = JOptionPane.showInputDialog(this, "Entrez l'ID de la catégorie parent :");
            if (parentId != null && !parentId.isEmpty()) {
                createCategory(parentId);
            } else {
                JOptionPane.showMessageDialog(this, "ID de catégorie parent invalide.");
            }
        }
    }

    private void createCategory(String parentId) {
        String nom = JOptionPane.showInputDialog(this, "Entrez le nom de la catégorie :");
        if (nom != null && !nom.isEmpty()) {
            String image = JOptionPane.showInputDialog(this, "Entrez l'URL de l'image de la catégorie :");
            if (image != null && !image.isEmpty()) {
                JSONObject categoryData = new JSONObject();
                categoryData.put("nom", nom);
                categoryData.put("image", image);
                if (parentId != null) {
                    categoryData.put("categorieParent", "/api/categories/" + parentId);
                }

                // Afficher le JSON envoyé pour débogage
                System.out.println("Envoi de la requête JSON : " + categoryData.toString());

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://127.0.0.1:8000/api/categories"))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + jwtToken)
                        .POST(HttpRequest.BodyPublishers.ofString(categoryData.toString()))
                        .build();

                try {
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    System.out.println("Code de statut HTTP : " + response.statusCode());
                    System.out.println("Réponse du serveur : " + response.body());

                    if (response.statusCode() == 201) {  // 201 = Created
                        JOptionPane.showMessageDialog(this, "Catégorie créée avec succès !");
                    } else {
                        JOptionPane.showMessageDialog(this, "Erreur lors de la création de la catégorie : " + response.statusCode() +
                                "\nRéponse: " + response.body());
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(this, "URL de l'image invalide.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Nom de catégorie invalide.");
        }
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
        String id = JOptionPane.showInputDialog(this, "Entrez l'ID de la catégorie à mettre à jour :");
        if (id != null && !id.isEmpty()) {
            String nom = JOptionPane.showInputDialog(this, "Entrez le nouveau nom de la catégorie :");
            if (nom != null && !nom.isEmpty()) {
                JSONObject categoryData = new JSONObject();
                categoryData.put("nom", nom);
                // Ajoute d'autres propriétés si nécessaire

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://127.0.0.1:8000/api/categories/" + id))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + jwtToken)
                        .PUT(HttpRequest.BodyPublishers.ofString(categoryData.toString()))
                        .build();

                try {
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() == 200) {  // 200 = OK
                        JOptionPane.showMessageDialog(this, "Catégorie mise à jour avec succès !");
                    } else {
                        JOptionPane.showMessageDialog(this, "Erreur lors de la mise à jour de la catégorie : " + response.statusCode());
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Nom de catégorie invalide.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "ID de catégorie invalide.");
        }
    }

    private void deleteEntity() {
        String id = JOptionPane.showInputDialog(this, "Entrez l'ID de la catégorie à supprimer :");
        if (id != null && !id.isEmpty()) {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://127.0.0.1:8000/api/categories/" + id))
                    .header("Authorization", "Bearer " + jwtToken)
                    .DELETE()
                    .build();

            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 204) {  // 204 = No Content (successful deletion)
                    JOptionPane.showMessageDialog(this, "Catégorie supprimée avec succès !");
                } else {
                    JOptionPane.showMessageDialog(this, "Erreur lors de la suppression de la catégorie : " + response.statusCode());
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "ID de catégorie invalide.");
        }
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







