# Secure File Transfer – Système de Transfert de Fichiers Sécurisé

Application **Client-Serveur** en Java permettant le transfert sécurisé de fichiers via TCP, avec **chiffrement AES** et **vérification d’intégrité SHA-256**.

---

## 📌 Équipe
- Lamnai Lahcen  
- Louanda Yassine  

Projet supervisé par : Professeur M. Ahmed Bentajer

---

## 🛠️ Fonctionnalités principales
- Authentification client via login/mot de passe  
- Négociation des métadonnées : nom, taille et hash SHA-256 du fichier  
- Transfert chiffré avec AES  
- Vérification d’intégrité des fichiers  
- Gestion multi-fichiers et déconnexion via la commande `QUIT`  
- Serveur multithread pour gérer plusieurs clients simultanément  

---

## 🔐 Sécurité et cryptographie
**Chiffrement AES**  
- Algorithme : AES/ECB/PKCS5Padding  
- Clé : 128 bits (partagée entre client et serveur)  
- API Java : `javax.crypto`  

**Hachage SHA-256**  
- Classe utilisée : `MessageDigest.getInstance("SHA-256")`  
- Permet de vérifier que le fichier reçu n’a subi aucune modification  

---

## 📂 Structure du projet
```
transfert_de_fichiers_securise/
├── src/
│   ├── SecureFileServer.java
│   └── SecureFileClient.java
├── .gitignore
└── README.md
```
- `src/` : contient le code source du serveur et du client  
- `.gitignore` : ignore les fichiers générés, dossiers IDE, et fichiers reçus (`received/`)  

---

## ⚡ Protocole de communication (3 phases)

### Phase 1 : Authentification
- Client → Serveur : login + password  
- Serveur → Client : `AUTH_OK` ou `AUTH_FAIL` (connexion fermée si échec)  

### Phase 2 : Négociation
- Client → Serveur : nom du fichier, taille du fichier chiffré, hash SHA-256  
- Serveur → Client : `READY_FOR_TRANSFER`  

### Phase 3 : Transfert et vérification
- Client → Serveur : fichier chiffré AES  
- Serveur : déchiffre, sauvegarde, calcule le hash SHA-256 local  
- Serveur → Client : `TRANSFER_SUCCESS` ou `TRANSFER_FAIL`  

---

## ⚙️ Exécution du projet

### Lancer le serveur :
```bash
javac src/SecureFileServer.java -d out/
java -cp out/ SecureFileServer
```

### Lancer le client :
```bash
javac src/SecureFileClient.java -d out/
java -cp out/ SecureFileClient
```

### Exemple d’utilisation

**Côté Serveur :**
```
SecureFileServer running on port 5000
Client connected: /127.0.0.1
User authenticated: lahcen
File AD-Pentest.pdf received successfully.
Client disconnected: lahcen
```

**Côté Client :**
```
Login: lahcen
Password: lahcenpass
Server IP: localhost
Authentication OK.
File path (ou QUIT pour terminer): C:\Users\LAHCEN\Documents\AD-Pentest.pdf
Server response: TRANSFER_SUCCESS
File path (ou QUIT pour terminer): QUIT
Session terminée.
```
---
