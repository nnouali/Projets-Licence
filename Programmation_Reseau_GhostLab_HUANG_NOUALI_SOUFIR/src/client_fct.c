#include <sys/socket.h>
#include <stdlib.h>
#include <arpa/inet.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <time.h>
#include <netdb.h>
#include <pthread.h>



#define ID_SIZE 8
#define BUF_SIZE 1024


void print_options(int inscrit){ 
    // inscrit indique si le joueur est deja dans une partie ou pas
    // 0 : pas encore
    // 1 : oui
    printf("-------------------------------\n");
    if (inscrit == 0){
        printf("Creer une nouvelle partie : [NEWPL]\nRejoindre la partie m : [REGIS]\n");
    }else if(inscrit == 1){
        printf("Si vous etez pret, veuillez entrer : [START]\n");
    }else{
        perror("Error in the usage of fonction <print_option>, please contact admin XD.");
        exit(-1);
    }
    printf("Ou les autres options :\n");
    printf("Deinscrire : [UNREG]\n");
    printf("Demander la taille du labyrinthe de la partie <m> :[SIZE]\n");
    printf("Demander la liste des joueurs dans la partie <m> : [LIST]\n");
    printf("Demander la liste des parties pour lesquelles des joueurs se sont inscrits et qui ne sont pas encore commencees : [GAME]\n");
    printf("-------------------------------\n");
    return;
}

void receive_mess(int taille_mess, int sock){
    char mess[taille_mess+1];
        int rcv_n = recv(sock,mess, taille_mess,0);
        if(rcv_n <= 0){
            perror("mess recu probleme la:");
            exit(-1);
        }
        mess[rcv_n] = '\0';
    return;
}

char *tirer_mess(char * buf, int plus_ou_etoile){ // pour un seul message (supprimer ***)
    //plus_ou_etoile == 0 : +
    // plus_ou_etoile == 1: *
    char *mess_break = malloc(BUF_SIZE);
    int cmp = 0;
    for (int i = 0, j = 0, len = 0; i < strlen(buf); i++){
        if (plus_ou_etoile == 0){
            if (buf[i] == '+'){
                cmp++;
            }else{
                len++;
            }
        }else{
            if (buf[i] == '*'){
            cmp++;
            }else{
                len++;
            }
        }
        if (cmp == 3){
            memcpy(mess_break, buf+j, len);
            j += len;
            len = 0;
        }
    }
    return mess_break;
}
void print_mess(uint8_t n, int sock, int size){
    for (int k = 0; k < n; k++){
        char mess_break[size+1];

        int rcv_n = recv(sock,mess_break, size * sizeof(char),0);

        if(rcv_n <= 0){
            perror("mess recu probleme ici:");
            exit(1);
        }

        mess_break[rcv_n] = '\0';
        uint8_t m;
        uint8_t s;

        memcpy(&m, mess_break+6, sizeof(uint8_t));
        memcpy(&s, mess_break+5+sizeof(uint8_t)+1, sizeof(uint8_t));
        printf("Partie %d : %d joueurs inscrits.\n", m, s);
    }
    return;
}

int id_is_valide(char *id){
    if(strcmp(id,"")==0){
        return -1;
    }else if (strlen(id)!=8 ){
        printf("Longeur d'id est incorrect.\n");
        return -1;
    }
    for (int i = 0; i < 8; i++){
        if (id[i]<48||(id[i]>57&&id[i]<65)||(id[i]>90&&id[i]<97)||id[i]>122){
            printf("Id est formée de caracteres alpha-numeriques.\n");
            return -1;
        }
    }
    return 1;
}

int port_is_valide(char *port){
    if(strcmp(port,"")==0){
        return -1;
    }else if (strlen(port)!=4){
        printf("Longeur de port est incorrect.\n");
        return -1;
    }  
    for (int i = 0; i < 4; i++){
        if (port[i]<48||port[i]>57){
            printf("Port est formée de caracteres numeriques.\n");
            return -1;
        }
    }
    if(port[0]=='1'){
        return -1;
    }
    return 1;
}

char * switch_options_env(char *head, int inscrit){ //traiter les options et les envoyer au serveur
    char *buf_env = malloc(BUF_SIZE);
    int m;
    if (strcmp(head,"UNREG")==0){ //Deinscrire
        //[UNREG***]
        printf("======ACTION======\nVous voulez deinscrire a cette partie\n==================\n");
        sprintf(buf_env,"UNREG***");
    }else if(strcmp(head,"SIZE")==0){//taille du labyrinthe
        //[SIZE? m***]
        printf("======ACTION======\nEntrez le numero de partie <m> vous voulez demander sous forme [m].\n==================\n");
        scanf("%d", &m);
        sprintf(buf_env,"%s? %d***",head,m);
    }else if(strcmp(head,"LIST")==0){//liste de joueurs
        //[LIST? m***]
        printf("======ACTION======\nEntrez le numero de partie <m> vous voulez demander sous forme [m].\n==================\n");
        scanf("%d", &m);
        sprintf(buf_env,"%s? %d***",head,m);
    }else if(strcmp(head,"GAME")==0){//liste de partie
        //[GAME?***]
        printf("======ACTION======\nDemander la liste des parties pour lesquelles des joueurs se sont inscrits et qui ne sont pas encore commencees.\n==================\n");
        sprintf(buf_env,"%s?***",head);
    }else if(strcmp(head,"SHUTD")==0){
        char id[8];
        printf("Mot de passe : \n");
        scanf("%s", id);
        sprintf(buf_env,"%s %s***",head,id);
    }else{
        printf("==================\nMerci d'envoyer les requetes sous une bonne forme.\n==================\n");
        return "";
    }
    return buf_env;
}

int switch_options_rcv(char *head, int sock){
    printf("\n==========REPONSE========\n");
    char buf[BUF_SIZE];
    if (strcmp(head,"DUNNO")==0){ 
        //[DUNNO***]
        int rcv_n = recv(sock,buf, 3 * sizeof(char),0);
        buf[rcv_n] = '\0';
        printf("Echec.\n");
    }else if(strcmp(head,"UNROK")==0){
        //[UNROK m***]
        int rcv_n = recv(sock,buf, 5 * sizeof(char),0);
        buf[rcv_n] = '\0';
        uint8_t m;
        memcpy(&m, buf+1, sizeof(uint8_t));
        printf("Deinscription reussi de la partie %hhd.\n",m);
        return -1;
    }else if(strcmp(head,"SIZE!")==0){
        //[SIZE! m h w***]
        int rcv_n = recv(sock,buf, BUF_SIZE,0);
        buf[rcv_n] = '\0';
        
        uint8_t m;
        memcpy(&m, buf+1, sizeof(uint8_t));

        uint16_t h;
        uint16_t w;
        memcpy(&h, buf+3, sizeof(uint16_t));
        memcpy(&w, buf+sizeof(uint16_t)+1+3, sizeof(uint16_t));       
        printf("La taille du labyrinthe de la partie %hhd :\n",m);
        printf("Hauteur : %d ; Largeur : %d\n",htons(h),ntohs(w));

    }else if(strcmp(head,"LIST!")==0){
        //[LIST! m s***]
        //[PLAYR id***] * s
        int rcv_n = recv(sock,buf, 7,0);
        buf[rcv_n] = '\0';
        uint8_t s;
        uint8_t m;
        memcpy(&m, buf+1, sizeof(uint8_t));
        memcpy(&s, buf+sizeof(uint8_t)+1, sizeof(uint8_t));
        printf("La liste des %hhd joueurs de la partie %hhd :\n    PLAYER\tID\n",s,m);
        if (s > 0){
            for(int i = 0; i < s; i++){
                char playr[18];
                int rcv_n = recv(sock, playr, 17,0);
                playr[rcv_n]='\0';
                char id[9];
                memcpy(id, playr+6, 8);
                id[8] = '\0';
                printf("\t%d \t %s\n",i+1, id);
            }
        }

    }else if(strcmp(head,"GAMES")==0){
        // [GAMES n***]
        // [OGAME m s***] * n
        int rcv_n = recv(sock,buf, 5,0);
        buf[rcv_n] = '\0';
        uint8_t n;
        memcpy(&n, buf+1,sizeof(uint8_t));
        printf("La liste des %d parties pour lesquelles des joueurs se sont inscrits et qui ne sont pas encore commencees:\n",n);  
        if (n > 0){
            print_mess(n,sock,12);
        } 
    }else if (strcmp(head,"REGOK")==0){
        int rcv_n = recv(sock,buf,BUF_SIZE,0);
        buf[rcv_n] = '\0';
        uint8_t m;
        memcpy(&m,buf+1, sizeof(uint8_t));
        printf("Inscription reussie a la partie %d.\n",m);
        return 0;
    }else if (strcmp(head,"REGNO")==0){
        int rcv_n = recv(sock,buf,3,0);
        buf[rcv_n] = '\0';
        printf("L'inscription a échoué\n");
        return 0;
    }else if (strcmp(head,"GOBYE")==0){//Pour shutdown
        //[GOBYE***]
        int rcv_n = recv(sock, buf, 3 * sizeof(char), 0);
        buf[rcv_n] = '\0';
        printf("Bye~ \n");
        exit(0);
    }
    else{
        int rcv_n = recv(sock,buf, BUF_SIZE,0);
        buf[rcv_n] = '\0';
        printf("Mess rcv :%s\n",buf);
        printf("HEAD : %s\n", head);
        perror("Error in the usage of fonction <switch_option>, please contact admin XD.");
        exit(1);
    }
    printf("\n=========================\n");
    return 0;
}

