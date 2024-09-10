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
        String libelleCourt = JOptionPane.showInputDialog(this, "Entrez le nom du produit :");
        String libelleLong = JOptionPane.showInputDialog(this, "Entrez la description du produit :");
        String referenceFournisseur = JOptionPane.showInputDialog(this, "Entrez la référence fournisseur :");
        String prixAchatStr = JOptionPane.showInputDialog(this, "Entrez le prix d'achat du produit :");
        String prixVenteStr = JOptionPane.showInputDialog(this, "Entrez le prix de vente du produit :");
        String stockStr = JOptionPane.showInputDialog(this, "Entrez la quantité en stock :");
        String actifStr = JOptionPane.showInputDialog(this, "Le produit est-il actif ? (true/false)");
        String sousCategorieId = JOptionPane.showInputDialog(this, "Entrez l'ID de la sous-catégorie :");
        String photo = JOptionPane.showInputDialog(this, "Entrez l'URL de la photo du produit :");
        String fournisseurId = JOptionPane.showInputDialog(this, "Entrez l'ID du fournisseur :");
    
        if (libelleCourt != null && !libelleCourt.isEmpty() && 
            libelleLong != null && !libelleLong.isEmpty() &&
            referenceFournisseur != null && !referenceFournisseur.isEmpty() &&
            prixAchatStr != null && !prixAchatStr.isEmpty() &&
            prixVenteStr != null && !prixVenteStr.isEmpty() &&
            stockStr != null && !stockStr.isEmpty() &&
            actifStr != null && !actifStr.isEmpty() &&
            sousCategorieId != null && !sousCategorieId.isEmpty() &&
            photo != null && !photo.isEmpty() &&
            fournisseurId != null && !fournisseurId.isEmpty()) {
    
            JSONObject produitData = new JSONObject();
            produitData.put("libelleCourt", libelleCourt);
            produitData.put("libelleLong", libelleLong);
            produitData.put("referenceFournisseur", referenceFournisseur);
            produitData.put("prixAchat", Double.parseDouble(prixAchatStr));
            produitData.put("prixVente", Double.parseDouble(prixVenteStr));
            produitData.put("stock", Integer.parseInt(stockStr));
            produitData.put("actif", Boolean.parseBoolean(actifStr));
            produitData.put("sousCategorie", "/api/categories/" + sousCategorieId);
            produitData.put("photo", photo);
            produitData.put("idFournisseur", "/api/fournisseurs/" + fournisseurId);
    
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
            JOptionPane.showMessageDialog(this, "Certaines informations sont manquantes ou invalides.");
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
            System.out.println("Code de statut HTTP : " + response.statusCode());
            if (response.statusCode() == 200) {
                String responseBody = response.body();
                System.out.println("Réponse de l'API : " + responseBody);
    
                JSONObject jsonResponse = new JSONObject(responseBody);
                JSONArray produitsArray = jsonResponse.getJSONArray("hydra:member");
    
                DefaultListModel<String> listModel = new DefaultListModel<>();
                for (int i = 0; i < produitsArray.length(); i++) {
                    JSONObject produit = produitsArray.getJSONObject(i);
    
                    String idUri = produit.getString("@id");
                    String[] parts = idUri.split("/");
                    String id = parts[parts.length - 1];
    
                    String nom = produit.optString("libelleCourt", "N/A");
                    String description = produit.optString("libelleLong", "N/A");
                    double prix = produit.optDouble("prixVente", 0.0);
    
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
            String referenceFournisseur = JOptionPane.showInputDialog(this, "Entrez la référence fournisseur :");
            String prixAchat = JOptionPane.showInputDialog(this, "Entrez le prix d'achat :");
            String prixVente = JOptionPane.showInputDialog(this, "Entrez le prix de vente :");
            String stock = JOptionPane.showInputDialog(this, "Entrez la quantité en stock :");
            String actif = JOptionPane.showInputDialog(this, "Le produit est-il actif ? (true/false) :");
            String photo = JOptionPane.showInputDialog(this, "Entrez le chemin ou l'URL de la photo du produit :");
            String sousCategorie = JOptionPane.showInputDialog(this, "Entrez l'ID de la sous-catégorie :");
            String idFournisseur = JOptionPane.showInputDialog(this, "Entrez l'ID du fournisseur :");
    
            if (nom != null && !nom.isEmpty() && description != null && !description.isEmpty() 
                    && referenceFournisseur != null && !referenceFournisseur.isEmpty()
                    && prixAchat != null && !prixAchat.isEmpty() && prixVente != null && !prixVente.isEmpty()
                    && stock != null && !stock.isEmpty() && actif != null && !actif.isEmpty()
                    && photo != null && !photo.isEmpty() && sousCategorie != null && !sousCategorie.isEmpty()
                    && idFournisseur != null && !idFournisseur.isEmpty()) {
    
                try {
                    // Création de l'objet JSON avec tous les champs
                    JSONObject produitData = new JSONObject();
                    produitData.put("libelleCourt", nom);
                    produitData.put("libelleLong", description);
                    produitData.put("referenceFournisseur", referenceFournisseur);
                    produitData.put("prixAchat", Double.parseDouble(prixAchat));
                    produitData.put("prixVente", Double.parseDouble(prixVente));
                    produitData.put("stock", Integer.parseInt(stock));
                    produitData.put("actif", Boolean.parseBoolean(actif));
                    produitData.put("photo", photo);
                    produitData.put("sousCategorie", "/api/categories/" + sousCategorie);
                    produitData.put("idFournisseur", "/api/fournisseurs/" + idFournisseur);
    
                    // Afficher le JSON pour vérification
                    System.out.println("Données envoyées : " + produitData.toString());
    
                    // Construction de la requête HTTP PUT
                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("https://127.0.0.1:8000/api/produits/" + id))
                            .header("Content-Type", "application/json")
                            .header("Authorization", "Bearer " + jwtToken)
                            .PUT(HttpRequest.BodyPublishers.ofString(produitData.toString()))
                            .build();
    
                    // Envoi de la requête et gestion de la réponse
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    
                    System.out.println("Réponse reçue : " + response.statusCode() + " " + response.body());
    
                    if (response.statusCode() == 200) {
                        JOptionPane.showMessageDialog(this, "Produit mis à jour avec succès !");
                    } else {
                        JOptionPane.showMessageDialog(this, "Erreur lors de la mise à jour du produit : " + response.statusCode() + "\n" + response.body());
                    }
                } catch (IOException | InterruptedException | NumberFormatException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Erreur lors de l'envoi de la requête : " + e.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(this, "Certains champs sont manquants ou invalides.");
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


