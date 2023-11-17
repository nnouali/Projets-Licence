#include "client_fct.c"
#include <string.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <sys/types.h>
#include <signal.h>
#include <unistd.h>
int PORT = 0;
int FIN = 1;

void print_start_option()
{
  printf("--------------------\nDéplacements : \n\tVers haut : [UPMOV]\n\tVers bas : [DOMOV]\n\tVers gauche : [LEMOV]\n\tVers Droite : [RIMOV]\n");
  printf("\nEnvoi de messages :\n\tVers tout le monde : [MALL]\n\tVers une personne : [SEND]\n");
  printf("\nPour demander la liste des joueurs presents dans cette partie : [GLIS]\n");
  printf("\nAides :\n\tUtiliser une aide pour voir les cases autour de vous : [HELP]\n");
  printf("\tCasser un mur : \n\t\tVers haut : [UPBRE]\n\t\tVers bas : [DOBRE]\n\t\tVers gauche : [LEBRE]\n\t\tVers Droite : [RIBRE]\n");

  printf("\nPour quitter le jeu : [IQUIT]\n--------------------\n");
}

/*
    -1 : Echec, le client doit refaire cette action
    1  : Reussi
    0  : Quitter le jeu
*/

typedef struct _multiInfos
{
  char ip[16];
  char port[5];
} multiInfos;


void *multi_diffusion(void *data)
{
  multiInfos *infos = (multiInfos *)data;

  int sock = socket(PF_INET, SOCK_DGRAM, 0);
  int ok = 1;
  if(setsockopt(sock, SOL_SOCKET, SO_REUSEPORT, &ok, sizeof(ok))==-1){
    perror("setsockopt:");
    exit(1);
  }
  struct sockaddr_in address_sock;
  address_sock.sin_family = AF_INET;
  address_sock.sin_port = htons(atoi(infos->port));
  address_sock.sin_addr.s_addr = htonl(INADDR_ANY);

  if(bind(sock, (struct sockaddr *)&address_sock, sizeof(struct sockaddr_in)) !=0){
    perror("bind:");
    exit(1);
  }
  struct ip_mreq mreq;
  mreq.imr_multiaddr.s_addr = inet_addr(infos->ip);
  mreq.imr_interface.s_addr = htonl(INADDR_ANY);

  if(setsockopt(sock, IPPROTO_IP, IP_ADD_MEMBERSHIP, &mreq, sizeof(mreq))==-1){
    perror("setsockopt:");
    exit(1);
  }
  char tampon[219];

  char head[6];

// Pour recuperer des messages multidiffusion. Imprimer des messages dans ce fichier.
  int fd = open("multi.txt", O_CREAT|O_RDWR|O_TRUNC,S_IRWXU);
  int pid=fork();
    if(pid == 0){
      char *argv = malloc(50);
      sprintf(argv, "xterm -e \"tail -f --pid %d multi.txt\"", getppid());
      system(argv);
    }else{
      while (1){

        int rec = recv(sock, tampon, 218, 0);
        tampon[rec] = '\0';

        // Récupérer le head
        strncpy(head, tampon, 5);
        head[5] = '\0';

        if (strcmp(head, "GHOST") == 0){ 
          // GHOST x y+++ --> 5B_3B_3B+++
          // GHOST 001 004+++
          char tmp[10];

          // Extraire le x
          strncpy(tmp, tampon + 6, 3);
          tmp[3] = '\0';
          int x = atoi(tmp);

          // Extraire le y
          strncpy(tmp, tampon + 10, 3);
          tmp[3] = '\0';
          int y = atoi(tmp);

          char *buffer = malloc(512);
          sprintf(buffer,"Le fantome vient de se deplacer sur la case (%d,%d)\n", x, y);
          write(fd, buffer, strlen(buffer));
          free(buffer);
        }else if (strcmp(head, "SCORE") == 0){ // SCORE id p x y+++ -> 5B_8B_4B_3B_3B+++
          char tmp[30];

          // Extraire l'id
          strncpy(tmp, tampon + 6, 8);
          char id[9];
          memcpy(id, tampon + 6, 8);
          id[8] = '\0';

          // Extraire le p
          strncpy(tmp, tampon + 15, 4);
          int p = atoi(tmp);

          // Extraire le x
          char tmp_x[4];
          memcpy(tmp_x, tampon + 20, 3);
          tmp_x[3] = '\0';

          // Extraire le y
          char tmp_y[4];
          memcpy(tmp_y, tampon + 24, 3);
          tmp_y[3] = '\0';

          char *buffer = malloc(512);
          sprintf(buffer,"Le joueur %s (%s,%s) passe à %d points !\n",id, tmp_x,tmp_y,p);
          write(fd, buffer, strlen(buffer));
          free(buffer); 
        }else if (strcmp(head, "MESSA") == 0){ // MESSA id message+++ --> 5B_8D_JUSQUAU*** ou +++
          char tmp[219];
          memcpy(tmp, tampon, 218);
          tmp[218] = '\0';

          char id[9];
          memcpy(id, tampon + 6, 8);
          char mess_rcv[204];
          memcpy(mess_rcv, tampon + 15, 203);
          char *mess = tirer_mess(mess_rcv, 0);
          char *buffer = malloc(512);
          sprintf(buffer,"%s:%s\n", id, mess);
          write(fd, buffer, strlen(buffer));
          free(buffer);
        }else if (strcmp(head, "ENDGA") == 0){ // ENDGA id p+++ 5B_8B_4B
          char tmp[23];

          // Extraire l'id
          memcpy(tmp, tampon, 22);
          tmp[22] = '\0';

          char gagnant[9];
          char points[5];
          memcpy(gagnant, tmp + 6, 8);
          memcpy(points, tmp + 6 + 8 + 1, 4);
          gagnant[8] = '\0';
          points[4] = '\0';
          FIN = 0;
          printf("Le gagnant de la partie est **%s** avec un total de %s points ! Bravo à toi !\n", gagnant, points);
          printf("👻👻👻 Merci d'avoir joué ! A bientôt chez GhostLab ! 👻👻👻\n");
          exit(0);
        }
      }
    }
  return NULL;
}

void print_glis_mess(uint8_t n, int sock, int size){
  // [GPLYR id x y p***] 6+8+4+4+5+3 = 30
  for (int k = 0; k < n; k++){
    char mess_break[size + 1];
    int rcv_n = recv(sock, mess_break, size * sizeof(char), 0);
    if (rcv_n <= 0){
      perror("mess recu probleme ici:");
      exit(1);
    }
    mess_break[rcv_n] = '\0';
    char id[9];
    char pos_x[4];
    char pos_y[4];
    char score[5];

    int position = 6;
    memcpy(&id, mess_break + position, 8 * sizeof(char));
    id[8]='\0';
    position += 9;
    memcpy(pos_x, mess_break + position, 3);
    pos_x[3] = '\0';
    int p_x = atoi(pos_x);
    position += 4;

    memcpy(pos_y, mess_break + position, 3);
    pos_y[3] = '\0';
    int p_y = atoi(pos_y);
    position += 4;

    memcpy(score, mess_break + position, 4);
    score[4] = '\0';
    printf("%d\t%s\t(%d,%d)\t\t%s\n", k, id, p_x, p_y, score);
  }
  return;
}

void print_help_mess(uint8_t n, int sock, int size){
  // [OHELP T X Y***](5+2+4+4+3 = 18)
  int line = -1;
  for (int k = 0; k < n; k++){
    char mess_break[size + 1];
    int rcv_n = recv(sock, mess_break, size * sizeof(char), 0);
    if (rcv_n <= 0){
      perror("mess recu probleme ici:");
      exit(1);
    }

    mess_break[rcv_n] = '\0';
    char type[2];
    char pos_x[4];

    int position = 6;
    memcpy(&type, mess_break + position, 1 * sizeof(char));
    type[1] = '\0';
    position += 2;
    memcpy(pos_x, mess_break + position, 3);
    pos_x[3] = '\0';
    int p_x = atoi(pos_x);
    position += 4;

    if (strcmp(type, "l") == 0){
      strcpy(type, " ");
    }

    if (p_x != line){
      printf("\n|-----|\n|");
      line = p_x;
    }
    printf("%s|", type);
  }
  printf("\n|-----|\n\n");
  return;
}

int switch_start_option_rcv(char *head, int sock){
  char buf[BUF_SIZE];
  if (strcmp(head, "MOVE!") == 0){ 
    //[MOVE! x y***] 4+4+3 = 11
    int rcv_n = recv(sock, buf, 11 * sizeof(char), 0);
    buf[rcv_n] = '\0';
    char pos_x[4];
    char pos_y[4];
    memcpy(pos_x, buf + 1, 3 * sizeof(char));
    memcpy(pos_y, buf + 5, 3 * sizeof(char));
    pos_x[3] = '\0';
    pos_y[3] = '\0';
    int p_x = atoi(pos_x);
    int p_y = atoi(pos_y);
    printf("==============\nVotre nouvelle position : (%d,%d)\n==============\n", p_x, p_y);
  }else if (strcmp(head, "MOVEF") == 0){ 
    // Si rencontre fantome : [MOVEF x y p***] 4+4+4+3=15
    int rcv_n = recv(sock, buf, 16 * sizeof(char), 0);
    buf[rcv_n] = '\0';
    char pos_x[4];
    char pos_y[4];
    char points[5];
    memcpy(pos_x, buf + 1, 3 * sizeof(char));
    memcpy(pos_y, buf + 5, 3 * sizeof(char));
    memcpy(points, buf + 9, 4 * sizeof(char));
    pos_x[3] = '\0';
    pos_y[3] = '\0';
    points[4]='\0';
    int p_x = atoi(pos_x);
    int p_y = atoi(pos_y);
    int point = atoi(points);
    printf("==============\nVous avez capte un fantome !\nVotre nouvelle position : (%d,%d)\nVos points : %d\n==============\n", p_x, p_y, point);
  }else if (strcmp(head, "GLIS!") == 0){ 
    // [GLIS! s***] + [GPLYR id x y p***] 6+8+4+4+5+3 = 30
    int rcv_n = recv(sock, buf, 5 * sizeof(char), 0);
    buf[rcv_n] = '\0';
    uint8_t s;
    memcpy(&s, buf + 1, sizeof(uint8_t));
    printf("La liste des %hhd joueurs :\n\tPLAYER\tID\tPOSITION(x,y)\tPOINTS\n", s);
    if (s != 0){
      print_glis_mess(s, sock, 30);
    }
  }else if (strcmp(head, "GOBYE") == 0){ 
    //[GOBYE***]
    int rcv_n = recv(sock, buf, 3 * sizeof(char), 0);
    buf[rcv_n] = '\0';
    printf("Bye~ \n");
    return 1;
  }else if (strcmp(head, "MALL!") == 0){ // [MALL!***]
    int rcv_n = recv(sock, buf, 3 * sizeof(char), 0);
    buf[rcv_n] = '\0';
  }else if (strcmp(head, "SEND!") == 0){ // [SEND!***]
    int rcv_n = recv(sock, buf, 3 * sizeof(char), 0);
    buf[rcv_n] = '\0';
    printf("Message a envoye.\n");
  }else if (strcmp(head, "NSEND") == 0){ // [NSEND***]
    int rcv_n = recv(sock, buf, 3 * sizeof(char), 0);
    buf[rcv_n] = '\0';
    printf("=====================\nMessage privé non envoyé. Il se peut que le destinataire n'exxiste pas !\n=====================\n");
  }else if (strcmp(head,"DUNNO")==0){
    int rcv_n = recv(sock, buf, 3, 0);
    buf[rcv_n]='\0';
    printf("Commande inconnue.\n");
  }
  /////////////////////////EXTENTION///////////////////////
  //////////////////Casser une case de mur/////////////////
  else if (strcmp(head, "OKBRE") == 0){
    int rcv_n = recv(sock, buf, 13 * sizeof(char), 0);
    buf[rcv_n] = '\0';
    
    char pos_x[4];
    char pos_y[4];
    char nb_restants[2];
    memcpy(pos_x, buf + 1, 3 * sizeof(char));
    memcpy(pos_y, buf + 5, 3 * sizeof(char));
    memcpy(nb_restants, buf + 9, 1 * sizeof(char));
    pos_x[3] = '\0';
    pos_y[3] = '\0';
    nb_restants[1] = '\0';
    int p_x = atoi(pos_x);
    int p_y = atoi(pos_y);
    int nb = atoi(nb_restants);
    printf("Vous venez de casser un mur. Votre nouvelle position (%d,%d). Il vous reste %d breaks possibles.\n ", p_x, p_y, nb);
  }else if (strcmp(head, "NOBRE") == 0){
    int rcv_n = recv(sock, buf, 11 * sizeof(char), 0);
    buf[rcv_n] = '\0';

    char pos_x[4];
    char pos_y[4];
    memcpy(pos_x, buf + 1, 3 * sizeof(char));
    memcpy(pos_y, buf + 5, 3 * sizeof(char));
    pos_x[3] = '\0';
    pos_y[3] = '\0';
    int p_x = atoi(pos_x);
    int p_y = atoi(pos_y);
    printf("Vous n'avez pas casser de mur. Votre nouvelle position (%d,%d).\n", p_x, p_y);
  }
  //////////////Voir des cases autour de joueur////////////
  else if (strcmp(head, "NHELP") == 0){ 
    // [NHELP***]
    int rcv_n = recv(sock, buf, 3 * sizeof(char), 0);
    buf[rcv_n] = '\0';
    printf("Echec.\n");
  }else if (strcmp(head, "HELP!") == 0){
    // [HELP! N R***](4+3 = 7) + N*[OHELP T X Y***](5+2+4+4+3 = 18)
    // N : nombre de case pouvant etre vu, 1 octect max = 9 min = 4
    // R : nombre de fois d'aide qui reste, 1 octect
    // T : type de case 1 octect de char, m = mur ; c = chemin ; j = joueur
    // X et Y : position de chaque case, 3 octect sous forme de %03d
    int rcv_n = recv(sock, buf, 7 * sizeof(char), 0);
    buf[rcv_n] = '\0';
    uint8_t N, R;
    memcpy(&N, buf + 1, sizeof(uint8_t));
    memcpy(&R, buf + 3, sizeof(uint8_t));
    printf("Il vous reste encore %hhd fois d'aide (HELP).\n", R);
    print_help_mess(N, sock, 18);
  }else if (strcmp(head,"DUNNO")==0){
    int rcv_n = recv(sock, buf, 3, 0);
    buf[rcv_n]='\0';
    printf("Ce que vous demandez ne semble pas exister... Relisez votre demande.\n");
  }
  return 0;
}

int switch_start_option_env(char *head, int sock){
  int dist;
  char buf_env[BUF_SIZE];
  // Mess envoi Deplacement
  //[UPMOV d***]
  //[DOMOV d***]
  //[LEMOV d***]
  //[RIMOV d***]

  if (strcmp(head, "UPMOV") == 0){
    printf("La distance d'action UPMOV (<999)?\n");
    scanf("%d", &dist);
    if (dist > 999){
      printf("La distance doit etre < 999.\n");
      return -2;
    }
    sprintf(buf_env, "UPMOV %03d***", dist);
  }else if (strcmp(head, "DOMOV") == 0){
    printf("La distance d'action DOMOV (<999)?\n");
    scanf("%d", &dist);
    if (dist > 999){
      printf("La distance doit etre < 999.\n");
      return -2;
    }
    sprintf(buf_env, "DOMOV %03d***", dist);
  }else if (strcmp(head, "RIMOV") == 0){
    printf("La distance d'action RIMOV (<999)?\n");
    scanf("%d", &dist);
    if (dist > 999){
      printf("La distance doit etre < 999.\n");
      return -2;
    }
    sprintf(buf_env, "RIMOV %03d***", dist);
  }else if (strcmp(head, "LEMOV") == 0){
    printf("La distance d'action LEMOV (<999)?\n");
    scanf("%d", &dist);
    if (dist > 999){
      printf("La distance doit etre < 999.\n");
      return -2;
    }
    sprintf(buf_env, "LEMOV %03d***", dist);
  }else if (strcmp(head, "MALL") == 0){ 
    //[MALL? mess***]
    char *message;
    size_t len = 0;
    ssize_t l;
    getc(stdin);
    do{
      printf("Entrez votre message : ");
      l = getline(&message, &len, stdin);
    }while (l <= 1);
    sprintf(buf_env, "MALL? %s***", message);
  }else if (strcmp(head, "SEND") == 0){ 
    //[SEND? id mess***]
    char *message = NULL;
    size_t len = 0;
    char joueur[9];
    printf("Entrez l'identifiant du destinataire : ");
    scanf("%s", joueur);
    getc(stdin); // Escape "\n"
    ssize_t l;
    do{
      printf("Entrez votre message : ");
      l = getline(&message, &len, stdin);
    }while (l <= 1);
    sprintf(buf_env, "SEND? %s %s***", joueur, message);
  }else if (strcmp(head, "IQUIT") == 0){ //[IQUIT***]
    printf("Vous voulez quitter le jeu.\n");
    strcpy(buf_env, "IQUIT***");
    send(sock, buf_env, strlen(buf_env), 0);
    return -1;
  }else if (strcmp(head, "GLIS") == 0){
    strcpy(buf_env, "GLIS?***");
  }
  //////////////////////EXTENSIONS/////////////////////////
  //////////////////Casser une case de mur////////////////
  else if (strcmp(head, "UPBRE") == 0){
    sprintf(buf_env, "UPBRE***");
  }else if (strcmp(head, "DOBRE") == 0){
    sprintf(buf_env, "DOBRE***");
  }else if (strcmp(head, "RIBRE") == 0){
    sprintf(buf_env, "RIBRE***");
  }else if (strcmp(head, "LEBRE") == 0){
    sprintf(buf_env, "LEBRE***");
  }
  //////////////Voir des cases autour de joueur////////////
  else if (strcmp(head, "HELP") == 0){
    printf("Vous voulez utiliser une fois d'aide.\n");
    strcpy(buf_env, "HELP?***");
  }else{
    char tmp[6];
    memcpy(tmp, head, 5);
    tmp[5]='\0';
    printf("La forme de message incorrecte.\n");
    return -2;
  }
  if ((send(sock, buf_env, strlen(buf_env), 0)) <= 0){
    printf("Action Echec.");
  }
  return 1;
}

char *ip_nettoyer(char *ip){
  char *mess_break = malloc(15);
  int len = 0;
  for (int i = 0; i < strlen(ip); i++){
    if (ip[i] != '#'){
      len++;
    }else{ break;}
  }
  memcpy(mess_break, ip, len);
  mess_break[len] = '\0';
  return mess_break;
}

void *message_prive(void *data){
  char *id = (char *)data;
  int sock = socket(PF_INET, SOCK_DGRAM, 0);
  struct sockaddr_in address_sock;
  address_sock.sin_family = AF_INET;
  address_sock.sin_port = htons(PORT);
  address_sock.sin_addr.s_addr = htonl(INADDR_ANY);
  int r = bind(sock, (struct sockaddr *)&address_sock, sizeof(struct sockaddr_in));
  char *path = malloc(30);
  memcpy(path, "mp",2);
  memcpy(path+2,id,8);
  memcpy(path+10, ".txt", 4);
  path[14]='\0';
  int fd = open(path, O_CREAT|O_RDWR|O_TRUNC,S_IRWXU);

  // Imprimer des message UDP dans un fichier 
  char *argv = malloc(50);
  sprintf(argv, "xterm -e \"tail -f --pid %d %s\"", getppid(), path);
  if(fork()==0){
    system(argv);
  }else{
    if (r == 0){
      char tampon[219]; //[MESSP id2 mess+++] 5+1+8+1+200+3 =218
      while (FIN){
        int rec = recv(sock, tampon, 218, 0);
        tampon[rec] = '\0';
        char id2[9];
        memcpy(id2, tampon + 6, 8);
        char mess_rcv[204];
        memcpy(mess_rcv, tampon + 15, 203);
        char *mess = tirer_mess(mess_rcv, 0);
        char *buffer = malloc(512);
        sprintf(buffer,"%s:%s\n", id2, mess);
        write(fd, buffer, strlen(buffer));
        free(buffer);
      }
      close(fd);
      exit(0);
    }
  }
  return NULL;
}

void start_main(int sock){
  char buf_rcv[BUF_SIZE];
  // Mess recu Welco : [WELCO m h w f ip port***] 6+2+3+3+2+16+4+3 = 39
  int n_rcv = recv(sock, buf_rcv, 39, 0);
  if (n_rcv <= 0){
    perror("WELCO MESS ERROR");
  }

  uint8_t m, f;
  uint16_t h, w;
  char port[5];
  char ip_brut[16];
  int position = 6;

  memcpy(&m, buf_rcv + position, sizeof(uint8_t));
  position += 2;
  memcpy(&h, buf_rcv + position, sizeof(uint16_t));
  position += 3;
  memcpy(&w, buf_rcv + position, sizeof(uint16_t));
  position += 3;
  memcpy(&f, buf_rcv + position, sizeof(uint8_t));

  position += 2;
  memcpy(ip_brut, buf_rcv + position, 15);
  position += 16;
  ip_brut[15] = '\0';

  memcpy(port, buf_rcv + position, 4);
  port[4] = '\0';

  char * ip_propre = ip_nettoyer(ip_brut);

  printf("*******************\n*      Bienvenue a ghostlab la partie %d!\t\t*\n*      La taille du labyrinthe : %d x %d\t\t\t*\n", m, htons(h), htons(w));
  printf("*      Nombre de fantomes: %d\t\t\t\t*\n*      Votre adresse ip : %s\t\t\t*\n*      Votre port : %s\t\t\t\t*\n*********************\n", f, ip_propre, port);
  // Mess recu posi : [POSIT id x y***]6+9+4+3+3=25
  n_rcv = recv(sock, buf_rcv, 25, 0);
  buf_rcv[n_rcv] = '\0';
  if (n_rcv <= 0){
    perror("POSIT MESS ERROR");
  }
  char id[9];
  char pos_x[4];
  char pos_y[4];

  position = 6;
  memcpy(id, buf_rcv + position, 8);
  position += 9;
  id[9] = '\0';

  memcpy(pos_x, buf_rcv + position, 3);
  pos_x[3] = '\0';
  position += 4;
  int p_x = atoi(pos_x);

  memcpy(pos_y, buf_rcv + position, 3);
  pos_y[3] = '\0';
  int p_y = atoi(pos_y);
  printf("%s, votre position : (%d , %d)\n", id, p_x, p_y);

  multiInfos infos;
  strcpy(infos.ip,  ip_propre);
  strcpy(infos.port, port);

  pthread_t th1;
  pthread_create(&th1, NULL, multi_diffusion, (void *)&infos);

  // Creer un fil pour les messages privés
  pthread_t th2;
  pthread_create(&th2, NULL, message_prive, id);

  int conti = 1;
  int fin = 0;
  do{
    print_start_option();
    char head[6];
    scanf("%s", head);
    conti = switch_start_option_env(head, sock);
    if (conti == 0){
      printf("Le jeu est termine.");
      pthread_join(th1, NULL);
      pthread_join(th2, NULL);
      exit(0);
    }else if (conti == -2){ continue; }
    
    if ((recv(sock, head, 5, 0)) <= 0){
      perror("OPTION RECEIVE ERROR");
    }
    fin = switch_start_option_rcv(head, sock);
  } while ((conti == 1 || conti == -2) && fin == 0);
  return;
}