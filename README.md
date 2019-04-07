# CNN_CompterVision

## Introduction pour Checkout

### 1 - Récuperer la release 
### 2 - Lancer un pip install requirements.txt dans le folder CNN_serveur
### 3 - Ouvrir le projet Android avec Android Studio
### 4 - Lancer le serveur Avec Pycharm
### 5 - Le serveur necessite un repertoire d'entrainement -> se referer au fichier Conf.py
### 6 - L'application Android communiquera avec votre APi seulement si le fichier Conf.java est correctement renseigné.

## API request LIST

### GET img_searches/<pk> Pour recuperer le resultat d'une recherche
### GET img_searches/ Pour voir toutes les recherches
### POST img_searches/ Pour upload l'image a analyser par le serveur
### GET static/<str_name>.jpg pour aller cherche une ressource précise de votre dossier de d'apprentissage.
### POST index/<int> pour changer le nombre de résultats voulus par les clients.
  
  
 Liens utiles : 
 ## https://github.com/Brisseta/Back_endCV.git : repository du serveur (en cas de pb)
  ## https://github.com/ArthurHub/Android-Image-Cropper.git : mécanisme de rognage et modification d'image
