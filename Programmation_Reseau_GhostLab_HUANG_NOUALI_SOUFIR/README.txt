Groupe 38
HUANG Yingqi 22010841
NOUALI Noura 71806787
SOUFIR Emma 21956518


*************************************************
INSTRUCTIONS DE COMPILATION

Se placer dans le répertoire src/ et faire la commande make.
Le Makefile compile les codes sources en C et en Java.
Pour supprimer les exécutables, les ".class" et les fichiers auxiliaires, faire la commande make clean. 

*************************************************
CONNEXION ET EXECUTION

Le projet fonctionne sur le réseau de l'UFR.
Pour se connecter aux machines de l'UFR, faire :
ssh -XJ login@lucy.informatique.univ-paris-diderot.fr login@machine

Le serveur Server doit tourner sur lulu. On lance son exécution de la manière suivante : 
java Server [port]

Le client peut être exécuté sur n'importe quelle machine du réseau de l'UFR. On l'exécute de la manière suivante : ./client [port]



*************************************************
IMPLEMENTATION

Le projet a été réalisé en Java et en C. Le client a été implémenté en C et le Seveur en Java. 
L'intégralité du protocole demandé a été implémenté. 


*************************************************
EXTENSIONS

- XTERM : Les messages privés et multidiffusés ont été redirigés vers des fichiers afin de faciliter la compréhension et la saisie des requêtes. Ces messages sont affichés dans des mini terminaux xterm à part.  


- AIDES AUX JOUEURS : Deux requêtes d'aide différentes aux joueurs ont été ajoutée. 
La première est HELP. Quand le client envoie [HELP?***], le serveur répond [HELP! N R***] où N est le nombre de cases adjacentes au joueur sur 1 octet et R le nombre d'aides HELP restant sur un octet. Suite à ce message, le serveur envoie N messages de la forme [OHELP T X Y***] où T est le type de case (mur (M), chemin (l), fantome (f) ou joueur (j)) sur un caractère, X et Y les coordonnées de la case d'interêt en chaine de caractères sur 3 octets. Cette aide permet au joueur de connaitre ce qui se trouve sur les cases adjacentes à lui. Le nombre d'aides de type HELP est limité au cours d'une partie et dépend de sa difficulté. Si le joueur a épuisé son nombre d'aides HELP, le serveur répond [NHELP***]. 

Le deuxième type d'aide aux joueurs est le BREAK. Les différents types de messages sont [UPBRE***], [DOBRE***], [LEBRE***] et [RIBRE***].  Le nombre d'aides de type BREAK est limité au cours d'une partie et dépend de sa difficulté.
Quand le joueur envoie un message de type BREAK, il casse le mur sur la case adjacente dans la direction souhaitée et renvoie [OKBRE x y n***] où x et y sont les nouvelles coordonnées du joueur respectivement sur 3 octets et n le nombre de breaks restants sur 1 octet. S'il n'y a pas de mur dans cette direction, la requete à l'action d'un MOVE et le serveur répond [NOBRE x y***] où x et y sont les nouvelles coordonnées du joueur respectivement sur 3 octets. Il en va de même si le joueur a épuisé son nombre d'aides BREAK.

- SERIALIZATION : Trois labyrinthes ont été réalisés en dur et sont serializés au moment de leur première création. Les parties suivantes utilisant ces labyrinthes désérializent ces objets. 

- TERMINAISON DU SERVEUR : L'envoi de la requête SHUTD suivie d'un mot de passe ("angeange") permet de terminer complètement le serveur. Le serveur va alors répondre des [GOBYE***] à chacun des joueurs connectés et fermer leurs connexions, fermer proprement les parties en cours et terminer. 

*************************************************
POTENTIELS BUGS RENCONTRES

La fenètre xterm qui gère l'affichage des messages privés doit être fermée à la main. 

Si on execute deux clients sur la même machine avec des numéros de ports différents et des id différents provevants d'ordinateurs différents, il semble qu'un des clients ne puisse pas toujours rejoindre l'adresse de multi-diffusion.

