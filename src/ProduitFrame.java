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

public class ProduitFrame extends JFrame {

    private String jwtToken;

    public ProduitFrame(String jwtToken) {
        this.jwtToken = jwtToken;

        setTitle("Gestion des Produits");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel crudPanel = new JPanel(new GridLayout(2, 2, 10, 10));

        JButton createButton = new JButton("Créer");
        JButton readButton = new JButton("Lire les Produits");
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
        String nom = JOptionPane.showInputDialog(this, "Entrez le nom du produit :");
        if (nom != null && !nom.isEmpty()) {
            String description = JOptionPane.showInputDialog(this, "Entrez la description du produit :");
            String prix = JOptionPane.showInputDialog(this, "Entrez le prix du produit :");

            if (description != null && !description.isEmpty() && prix != null && !prix.isEmpty()) {
                JSONObject produitData = new JSONObject();
                produitData.put("nom", nom);
                produitData.put("description", description);
                produitData.put("prix", Double.parseDouble(prix));

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://127.0.0.1:8000/api/produits"))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + jwtToken)
                        .POST(HttpRequest.BodyPublishers.ofString(produitData.toString()))
                        .build();

                try {
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() == 201) {  // 201 = Created
                        JOptionPane.showMessageDialog(this, "Produit créé avec succès !");
                    } else {
                        JOptionPane.showMessageDialog(this, "Erreur lors de la création du produit : " + response.statusCode() +
                                "\nRéponse: " + response.body());
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Description ou prix invalide.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Nom de produit invalide.");
        }
    }

    private void readEntities() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://127.0.0.1:8000/api/produits"))
                .header("Authorization", "Bearer " + jwtToken)
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String responseBody = response.body();

                JSONObject jsonResponse = new JSONObject(responseBody);
                JSONArray produitsArray = jsonResponse.getJSONArray("hydra:member");

                DefaultListModel<String> listModel = new DefaultListModel<>();
                for (int i = 0; i < produitsArray.length(); i++) {
                    JSONObject produit = produitsArray.getJSONObject(i);

                    String idUri = produit.getString("@id");
                    String[] parts = idUri.split("/");
                    String id = parts[parts.length - 1];

                    String nom = produit.optString("nom", "N/A");
                    String description = produit.optString("description", "N/A");
                    double prix = produit.optDouble("prix", 0.0);

                    listModel.addElement("ID: " + id + ", Nom: " + nom + ", Description: " + description + ", Prix: " + prix);
                }

                JList<String> produitsList = new JList<>(listModel);
                JScrollPane scrollPane = new JScrollPane(produitsList);

                JFrame resultFrame = new JFrame("Produits");
                resultFrame.setSize(400, 300);
                resultFrame.add(scrollPane);
                resultFrame.setLocationRelativeTo(null);
                resultFrame.setVisible(true);

            } else {
                JOptionPane.showMessageDialog(this, "Erreur lors de la récupération des produits : " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void updateEntity() {
        String id = JOptionPane.showInputDialog(this, "Entrez l'ID du produit à mettre à jour :");
        if (id != null && !id.isEmpty()) {
            String nom = JOptionPane.showInputDialog(this, "Entrez le nouveau nom du produit :");
            String description = JOptionPane.showInputDialog(this, "Entrez la nouvelle description du produit :");
            String prix = JOptionPane.showInputDialog(this, "Entrez le nouveau prix du produit :");

            if (nom != null && !nom.isEmpty() && description != null && !description.isEmpty() && prix != null && !prix.isEmpty()) {
                JSONObject produitData = new JSONObject();
                produitData.put("nom", nom);
                produitData.put("description", description);
                produitData.put("prix", Double.parseDouble(prix));

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://127.0.0.1:8000/api/produits/" + id))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + jwtToken)
                        .PUT(HttpRequest.BodyPublishers.ofString(produitData.toString()))
                        .build();

                try {
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() == 200) {  // 200 = OK
                        JOptionPane.showMessageDialog(this, "Produit mis à jour avec succès !");
                    } else {
                        JOptionPane.showMessageDialog(this, "Erreur lors de la mise à jour du produit : " + response.statusCode());
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Nom, description ou prix invalide.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "ID de produit invalide.");
        }
    }

    private void deleteEntity() {
        String id = JOptionPane.showInputDialog(this, "Entrez l'ID du produit à supprimer :");
        if (id != null && !id.isEmpty()) {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://127.0.0.1:8000/api/produits/" + id))
                    .header("Authorization", "Bearer " + jwtToken)
                    .DELETE()
                    .build();

            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 204) {  // 204 = No Content (successful deletion)
                    JOptionPane.showMessageDialog(this, "Produit supprimé avec succès !");
                } else {
                    JOptionPane.showMessageDialog(this, "Erreur lors de la suppression du produit : " + response.statusCode());
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "ID de produit invalide.");
        }
    }
}

