#include "start_fct.c"

#define EQUALS(str1, str2) (strcmp(str1, str2) == 0)

int main(int argc, char *argv[]){
  // TCP pour serveur
  struct addrinfo *first_info;
  struct addrinfo hints;
  memset(&hints, 0, sizeof(hints));
  hints.ai_family = AF_INET;
  hints.ai_socktype = SOCK_STREAM;
  int getinfo = getaddrinfo("lulu.informatique.univ-paris-diderot.fr",argv[1],&hints,&first_info);
  // int getinfo = getaddrinfo("localhost",argv[1],&hints,&first_info);
  //int getinfo = getaddrinfo("localhost", "4646", &hints, &first_info);
  if (getinfo == 0){
    struct addrinfo *info = first_info; // head
    int found = 0;
    struct sockaddr *saddr;
    struct sockaddr_in *adr_sock;
    if (info != NULL){
      saddr = info->ai_addr;
      adr_sock = (struct sockaddr_in *)saddr;
      found = 1;
    }
    if (found == 1){
      int sock_main = socket(PF_INET, SOCK_STREAM, 0);
      if (sock_main < 0){
        perror("Connect socket error");
        exit(-1);
      }
      int r = connect(sock_main, (struct sockaddr *)adr_sock, sizeof(struct sockaddr_in));
      if (r < 0){
        perror("Connect problem");
        exit(-1);
      }else{
        ////////////////// AVANT COMMENCER ////////////////////////
        // mess recu
        char buf_rec[BUF_SIZE];
        int rcv_n = recv(sock_main, buf_rec, 10 * sizeof(char), 0);
        if (rcv_n <= 0){
          perror("mess recu probleme  :");
          exit(-1);
        }
        buf_rec[rcv_n] = '\0';
        //[GAMES n***] au joueur se connect
        // n : le nb de parties pas commence
        char tete[8];
        memcpy(tete, buf_rec, 7);
        uint8_t nb;
        memcpy(&nb, buf_rec+6,sizeof(uint8_t));
        printf("Nombre de parties disponibles : %hhd\n", nb);

        //[OGAME m s***] * n // n mess
        // m : le numero de la partie
        // s : le nb de joueur inscrits
        if (nb != 0){
          printf("===Liste des parties :===\n");
          print_mess(nb, sock_main, 12);
        }

        // mess envoyer
        //[NEWPL id port***] : creer une nouvelle partie

        //[REGIS id port m***] :rejoindre la partie m
        // port : port UDP de joueur
        int inscription_partie_reussi = -1;
        char buf_env[BUF_SIZE];
        char head[10];
        char id[9];
        char port[5];
        uint8_t m;
        do{
          int id_v = -1;
          int port_v = -1;
          do{
            print_options(0);
            scanf("%s", head);
            if (strcmp(head, "NEWPL") == 0){
              printf("Entrez votre <id> et votre <port> sous forme [id port].\nAttention : id est formée d'exactement 8 caracteres alpha-numeriques\n");
              scanf("%s %s", id, port);
              printf("======ACTION======\nCreer une nouvelle partie\nid : %s\nport : %s\n==================\n", id, port);
              sprintf(buf_env, "%s %s %s***", head, id, port);
              id_v = id_is_valide(id);
              port_v = port_is_valide(port);

              if (id_v == -1 || port_v == -1){  
                continue;
              }else{
                PORT = atoi(port);
                printf("NOTRE PORT : %d\n", PORT);  
              }             
            }else if (strcmp(head, "REGIS") == 0){
              printf("Entrez votre <id>, votre <port> et le numero de partie <m> vous voulez rejoindre sous forme [id port m].\n");
              scanf("%s %s %hhd", id, port, &m);
              printf("======ACTION======\nRejoindre une partie\nid : %s\nport : %s\npartie_id : %hhd\n==================\n", id, port, m);
              sprintf(buf_env, "%s %s %s %hhd***", head, id, port, m);
              id_v = id_is_valide(id);
              port_v = port_is_valide(port);

              if (id_v == -1 || port_v == -1){
                continue;
              }else{
                PORT = atoi(port);
                printf("NOTRE PORT : %d\n", PORT);  
              } 
            }else{
              char * env = switch_options_env(head, 0);
              if(strcmp(env,"")== 0){continue;}
              strcpy(buf_env, env);
            }
            // send to serveur
            int sd_n = send(sock_main, buf_env, strlen(buf_env), 0);
            if (sd_n <= 0){
              perror("mess envoi probleme :");
              exit(-1);
            }
            // mess repondre recu
            rcv_n = recv(sock_main, buf_rec, 5 * sizeof(char), 0);
            if (rcv_n <= 0){
              exit(0);
            }
            buf_rec[rcv_n] = '\0';
            switch_options_rcv(buf_rec, sock_main);
          } while (id_v == -1 || port_v == -1);
          
          if (strcmp(buf_rec, "REGOK") == 0){
            //[REGOK m***]
            printf("REGOK nous donne : %s\n", buf_rec);
            inscription_partie_reussi = 1;
            
            // mess envoyer :
            // [START***]
            int start = -1;
            do{
              print_options(1);
              scanf("%s", head);
              if (strcmp(head, "START") == 0){
                start = 1;
                printf("Vous etez pret.\n");
                sprintf(buf_env, "%s***", head);
                send(sock_main, buf_env, strlen(buf_env), 0);

                start_main(sock_main);
              }else{
                char * env = switch_options_env(head, 1);
                if(strcmp(env,"")== 0){continue;}
                strcpy(buf_env, env);
              }
              int sd_n = send(sock_main, buf_env, strlen(buf_env), 0);
              if (sd_n <= 0){
                exit(0);
              }

              // reply
              rcv_n = recv(sock_main, buf_rec, 5 * sizeof(char), 0);
              if (rcv_n <= 0){
                break;
              }
              buf_rec[rcv_n] = '\0';

              if(switch_options_rcv(buf_rec, sock_main)  < 0){
                  start = -2;
                  inscription_partie_reussi = -1;
              }
            } while (start == -1);
          }else if (strstr(buf_rec, "REGNO")){
            //[REGNO m***]
            continue;
          }
        } while (inscription_partie_reussi < 0);
        printf("Fini\n");
      }
    }
  }
}
