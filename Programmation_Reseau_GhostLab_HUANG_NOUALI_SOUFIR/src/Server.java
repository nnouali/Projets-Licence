
import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

	public static LinkedList<Partie> parties = new LinkedList<>();
	public static LinkedList<Joueur> joueurs = new LinkedList<>();

	private ServerSocket serv_socket;
	private boolean shut = false;
	private final static String mdp = "angeange";

	public Server(int numPort) {
		try {
			this.serv_socket = new ServerSocket(numPort);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void runServ() {
		while (!shut) {
			try {
				System.out.println("Waiting for accept...");
				Socket socket = this.serv_socket.accept();// nombre de clients acceptés à la suite
				System.out.println("WELCOME TO GHOSTLAB !!");
				// A chaque nouveau client:
				Joueur j = new Joueur(socket);// correspond au service du cours
				Thread t = new Thread(j);
				t.start();
				Server.joueurs.add(j);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
	
	public static boolean tryShut(String pwd) {
		if(pwd.equals(mdp)) {
			for (Partie partie : Server.parties) {
	    		partie.setEnd(true);
	    		partie.isItTheEnd();		
			}
			for (Joueur joueur : Server.joueurs) {
				joueur.getCurrentThread().interrupt();
			}			
			System.exit(0);
		}
		return false;	
	}

	public LinkedList<Partie> getParties() {
		return parties;
	}

	public static void main(String[] args) {
		if(args.length!=0) {
			Server s = new Server(Integer.valueOf(args[0]));
			s.runServ();
		}else {
			System.out.println("Numero de port en argument");
			System.exit(0);
		}
	}

}
