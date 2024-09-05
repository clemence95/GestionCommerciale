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

public class CategorieFrame extends JFrame {

    private String jwtToken;

    public CategorieFrame(String jwtToken) {
        this.jwtToken = jwtToken;

        setTitle("Gestion des Catégories");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

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

        setLayout(new BorderLayout());
        add(crudPanel, BorderLayout.CENTER);
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

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://127.0.0.1:8000/api/categories"))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + jwtToken)
                        .POST(HttpRequest.BodyPublishers.ofString(categoryData.toString()))
                        .build();

                try {
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
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
    
                JSONObject jsonResponse = new JSONObject(responseBody);
                JSONArray categoriesArray = jsonResponse.getJSONArray("hydra:member");
    
                // Modèles pour les catégories parents et sous-catégories
                DefaultListModel<String> parentModel = new DefaultListModel<>();
                DefaultListModel<String> subcategoryModel = new DefaultListModel<>();
    
                for (int i = 0; i < categoriesArray.length(); i++) {
                    JSONObject category = categoriesArray.getJSONObject(i);
    
                    String idUri = category.getString("@id");
                    String[] parts = idUri.split("/");
                    String id = parts[parts.length - 1];
    
                    String nom = category.optString("nom", "N/A");
                    String image = category.optString("image", "N/A");
                    String categorieParent = category.optString("categorieParent", null);  // Champ contenant la catégorie parente
    
                    // Séparer les catégories parents et sous-catégories
                    if (categorieParent == null || categorieParent.isEmpty()) {
                        // C'est une catégorie parent
                        parentModel.addElement("ID: " + id + ", Nom: " + nom + ", Image: " + image);
                    } else {
                        // C'est une sous-catégorie
                        subcategoryModel.addElement("ID: " + id + ", Nom: " + nom + ", Image: " + image);
                    }
                }
    
                // Créer les listes pour afficher les catégories parents et sous-catégories
                JList<String> parentList = new JList<>(parentModel);
                JList<String> subcategoryList = new JList<>(subcategoryModel);
    
                // Ajouter les listes dans des panneaux de défilement
                JScrollPane parentScrollPane = new JScrollPane(parentList);
                JScrollPane subcategoryScrollPane = new JScrollPane(subcategoryList);
    
                // Créer la fenêtre avec deux sections (catégories parents et sous-catégories)
                JFrame resultFrame = new JFrame("Catégories");
                resultFrame.setLayout(new GridLayout(2, 1));  // 2 lignes, 1 colonne
                resultFrame.setSize(400, 600);
                resultFrame.add(new JLabel("Catégories Parents"));
                resultFrame.add(parentScrollPane);
                resultFrame.add(new JLabel("Sous-Catégories"));
                resultFrame.add(subcategoryScrollPane);
                resultFrame.setLocationRelativeTo(null);
                resultFrame.setVisible(true);
    
            } else {
                JOptionPane.showMessageDialog(null, "Erreur lors de la récupération des catégories : " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    private void updateEntity() {
        // Étape 1 : Récupérer et afficher les catégories
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://127.0.0.1:8000/api/categories"))
                .header("Authorization", "Bearer " + jwtToken)
                .build();
    
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String responseBody = response.body();
    
                JSONObject jsonResponse = new JSONObject(responseBody);
                JSONArray categoriesArray = jsonResponse.getJSONArray("hydra:member");
    
                // Modèles pour les catégories parents et sous-catégories
                DefaultListModel<String> parentModel = new DefaultListModel<>();
                DefaultListModel<String> subcategoryModel = new DefaultListModel<>();
    
                for (int i = 0; i < categoriesArray.length(); i++) {
                    JSONObject category = categoriesArray.getJSONObject(i);
    
                    String idUri = category.getString("@id");
                    String[] parts = idUri.split("/");
                    String id = parts[parts.length - 1];
    
                    String nom = category.optString("nom", "N/A");
                    String image = category.optString("image", "N/A");
                    String categorieParent = category.optString("categorieParent", null);  // Champ contenant la catégorie parente
    
                    // Séparer les catégories parents et sous-catégories
                    if (categorieParent == null || categorieParent.isEmpty()) {
                        parentModel.addElement("ID: " + id + ", Nom: " + nom + ", Image: " + image);
                    } else {
                        subcategoryModel.addElement("ID: " + id + ", Nom: " + nom + ", Image: " + image + ", Parent ID: " + categorieParent);
                    }
                }
    
                // Afficher les catégories parents et sous-catégories dans des panneaux de défilement
                JList<String> parentList = new JList<>(parentModel);
                JList<String> subcategoryList = new JList<>(subcategoryModel);
    
                JScrollPane parentScrollPane = new JScrollPane(parentList);
                JScrollPane subcategoryScrollPane = new JScrollPane(subcategoryList);
    
                // Créer une fenêtre temporaire pour afficher les catégories
                JFrame categoryFrame = new JFrame("Sélectionner une Catégorie");
                categoryFrame.setLayout(new GridLayout(2, 1));  // 2 lignes, 1 colonne
                categoryFrame.setSize(400, 600);
                categoryFrame.add(new JLabel("Catégories Parents"));
                categoryFrame.add(parentScrollPane);
                categoryFrame.add(new JLabel("Sous-Catégories"));
                categoryFrame.add(subcategoryScrollPane);
                categoryFrame.setLocationRelativeTo(null);
                categoryFrame.setVisible(true);
    
                // Étape 2 : Sélectionner une catégorie à mettre à jour
                String id = JOptionPane.showInputDialog(categoryFrame, "Entrez l'ID de la catégorie à mettre à jour :");
                if (id != null && !id.isEmpty()) {
                    String nom = JOptionPane.showInputDialog(categoryFrame, "Entrez le nouveau nom de la catégorie :");
                    String imagePath = JOptionPane.showInputDialog(categoryFrame, "Entrez le chemin d'accès de la nouvelle image de la catégorie :");
    
                    if (nom != null && !nom.isEmpty() && imagePath != null && !imagePath.isEmpty()) {
                        // Création de l'objet JSON avec le nom et le chemin de l'image
                        JSONObject categoryData = new JSONObject();
                        categoryData.put("nom", nom);
                        categoryData.put("image", imagePath);  // Ajout du chemin de l'image dans les données à envoyer
    
                        HttpRequest updateRequest = HttpRequest.newBuilder()
                                .uri(URI.create("https://127.0.0.1:8000/api/categories/" + id))
                                .header("Content-Type", "application/json")
                                .header("Authorization", "Bearer " + jwtToken)
                                .PUT(HttpRequest.BodyPublishers.ofString(categoryData.toString()))
                                .build();
    
                        try {
                            HttpResponse<String> updateResponse = client.send(updateRequest, HttpResponse.BodyHandlers.ofString());
                            if (updateResponse.statusCode() == 200) {  // 200 = OK
                                JOptionPane.showMessageDialog(categoryFrame, "Catégorie mise à jour avec succès !");
                            } else {
                                JOptionPane.showMessageDialog(categoryFrame, "Erreur lors de la mise à jour de la catégorie : " + updateResponse.statusCode());
                            }
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        JOptionPane.showMessageDialog(categoryFrame, "Nom de catégorie ou chemin d'image invalide.");
                    }
                } else {
                    JOptionPane.showMessageDialog(categoryFrame, "ID de catégorie invalide.");
                }
    
            } else {
                JOptionPane.showMessageDialog(null, "Erreur lors de la récupération des catégories : " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
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
}
