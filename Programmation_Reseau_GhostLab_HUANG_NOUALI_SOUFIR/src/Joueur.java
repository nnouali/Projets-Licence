
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Random;

public class Joueur implements Runnable { 

    private String id;
    private int posX;
    private int posY;
    private int score;
    private int port;
    private int nbBreak;
    private int help;
    private boolean start = false;
    private Partie partie;
    private Socket socket;
    private InputStream is;
    private OutputStream os;
    private String jip;
	private Thread currentThread;

    public Joueur(Socket s) {
        try {
            this.socket = s;
            this.is = socket.getInputStream();
            this.os = socket.getOutputStream();
            InetAddress addr = socket.getInetAddress();
            jip = addr.getHostAddress();
			currentThread = Thread.currentThread();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
   
    @Override
    public void run() {
        try {
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();
            String id;
            int port;
            Partie p;
            byte [] buffer = null;
            
            // Envoi du message du nombre de parties
            byte n = (byte)Server.parties.size();//nombre de parties sur 1 octet
    		buffer = new byte[10];
    		System.arraycopy("GAMES ".getBytes(), 0, buffer, 0, 6);//"GAMES "
        	buffer[6] = n;
        	System.arraycopy("***".getBytes(), 0, buffer, 6+1, 3);//"***"
        	os.write(buffer);
			os.flush();

            // Desription de chaque partie :
            for (Partie pa : Server.parties) {          
                byte m = (byte)pa.getIdPartie();;//identifiant de la partie
                byte s = (byte) pa.getJoueurs().size();
        		buffer = new byte[12];
        		System.arraycopy("OGAME ".getBytes(), 0, buffer, 0, 6);//"OGAME "
            	buffer[6] = m;
        		System.arraycopy(" ".getBytes(), 0, buffer, 6+1, 1);//" "
            	buffer[7] = s;//s
            	System.arraycopy("***".getBytes(), 0, buffer, 6+1+1+1, 3);		
            	os.write(buffer);
    			os.flush();
            }
            while (this.start == false) {
                byte[] buff_type = new byte[5];
                if (is.read(buff_type, 0, 5) < 5)continue;                
                String type = new String(buff_type);

                switch (type) {
                    case "NEWPL":
                        is.read();
                        id = parseId(is);// lit l'id
                        is.read();
                        port = parsePort(is);// lit l'ip
                        // en cas de nouvelle partie, on assigne le joueur à la partie qu'il vient de
                        // créer
                        this.id = id;
                        this.port=port;   
                        Partie nouvelle = new Partie(this, new Random().nextInt(3));
                        Server.parties.add(nouvelle);// new Partie à chaque fois => this.partie.run() si thread et dans ce
                        // cas, on peut directement depuis la partie attendre les gens
                        this.partie = nouvelle;
                        this.nbBreak = partie.nBreak;
                        Server.joueurs.add(this);
                        // reg YES : quand pourrait on avoir un reg no ?
                        byte m2 = (byte)this.partie.getIdPartie();//identifiant de la partie
                        buffer = new byte[10];
                		System.arraycopy("REGOK ".getBytes(), 0, buffer, 0, 6);//"OGAME "
                    	buffer[6] = m2;
                    	System.arraycopy("***".getBytes(), 0, buffer, 6+1, 3);		
                    	os.write(buffer);
            			os.flush();
            			System.out.println("Creation d'une nouvelle partie "+nouvelle.getIdPartie()+" par "+id+" utilisant son port "+port);
                        break;

                    case "REGIS":
                        is.read();
                        id = parseId(is);// lit l'id et le set
                        is.read();
                        port = parsePort(is);
                        p = parseNumPartie(is);
                        if (p == null) {
                            send(os, "REGNO***");
                        } else if(checkId(id,p) && checkIP(this.jip, port,p)){                            
                            this.id = id;
                            this.port = port;
                            this.partie = p;
                            this.nbBreak = this.partie.nBreak;
                            p.getJoueurs().add(this);
                            byte m = (byte)p.getIdPartie();//identifiant de la partie
                    		buffer = new byte[10];
                    		System.arraycopy("REGOK ".getBytes(), 0, buffer, 0, 6);//"OGAME "
                        	buffer[6] = m;
                        	System.arraycopy("***".getBytes(), 0, buffer, 6+1, 3);		
                        	os.write(buffer);
                			os.flush();
                			System.out.println("Inscription a la partie"+p.getIdPartie()+" par "+id+" utilisant son port "+port);
                        }else {
                            send(os, "REGNO***");
                        }
                        break;

                    case "START":
                        // Chercher le joueur avec cette adresse ip et mettre start a true
                        if (this.partie != null)this.start = true;
                        boolean all = true;
                        for (Joueur j : this.partie.getJoueurs()) {
                            if (j.getStart() == false) all = false;
                        }
                        if (all) {
                        	this.partie.setState(true);
                        	this.partie.welcome();
                        	synchronized (this.partie) {
                        	    partie.notifyAll();
                        	}
                             // Si tout le monde est pres on set l'état de la partie à start
                        }
                       break;

                    case "UNREG":
                        if (this.partie != null) {
                            System.out.println("Demande de désinscription à la partie " +partie.getIdPartie() +" !");
                            this.partie.getJoueurs().remove(this);                            
                            byte m = (byte)this.partie.getIdPartie();//identifiant de la partie
                    		buffer = new byte[10];
                    		System.arraycopy("UNROK ".getBytes(), 0, buffer, 0, 6);//"OGAME "
                        	buffer[6] = m;
                        	System.arraycopy("***".getBytes(), 0, buffer, 6+1, 3);		
                        	os.write(buffer);
                			os.flush();
                			Server.parties.remove(this.partie);
                            this.partie = null;
                        } else {
                            send(os, "DUNNO***");
                        }
                        break;

                    case "SIZE?":
                        p = parseNumPartie(is);
                        if (p != null) {
                            System.out.println("Demande de la taille du labyrinthe dans la partie " +p.getIdPartie() +"!");
                        	//Ecriture de h sur 2 bytes : 
                        	ByteBuffer b = ByteBuffer.allocate(2);
                        	b.putShort((short) this.partie.getLabyrinthe().getNbLine());
                        	byte[] result = b.array();
                        	
                        	//Ecriture de w sur 2 bytes : 
                        	ByteBuffer b2 = ByteBuffer.allocate(2);
                        	b2.putShort((short)this.partie.getLabyrinthe().getNbCol());
                        	byte[] result2 = b2.array();
                        	
                        	//Numero de la partie m en byte : 
                        	byte m = (byte)p.getIdPartie();
                        	
                        	//Buffer contentant l'ensemble de la reponse : 
                        	byte c[] = new byte [16];
                        	System.arraycopy("SIZE! ".getBytes(), 0, c, 0, 6);
                        	c[6]=m;
                        	System.arraycopy(" ".getBytes(), 0, c, 6+1, 1);
                        	System.arraycopy(result, 0, c, 8, result.length);
                        	System.arraycopy(" ".getBytes(), 0, c, 8+result2.length, 1);
                        	System.arraycopy(result2, 0, c, 8+result.length+1, result2.length);
                        	System.arraycopy("***".getBytes(), 0, c, 8+result.length+1+result2.length, 3);                        	
                        	os.write(c);
                        	os.flush();                        	
                        } else {
                            send(os, "DUNNO***");
                        }
                        break;

                    case "LIST?":
                        p = parseNumPartie(is);
                        if (p != null) {
                        	System.out.println("Demande de la liste des joueurs de la partie "+p.getIdPartie());
                        	byte m = (byte)p.getIdPartie();;//identifiant de la partie
                            byte s = (byte) p.getJoueurs().size();
                    		buffer = new byte[12];
                    		System.arraycopy("LIST! ".getBytes(), 0, buffer, 0, 6);//"LIST! "
                        	buffer[6] = m;
                    		System.arraycopy(" ".getBytes(), 0, buffer, 6+1, 1);//" "
                        	buffer[7] = s;//s
                        	System.arraycopy("***".getBytes(), 0, buffer, 6+1+1+1, 3);		
                        	os.write(buffer);
                			os.flush();
                            for (Joueur j : p.getJoueurs()) send(os, "PLAYR " + j.getIdString() + "***");
                        } else send(os, "DUNNO***");
                        break;

                    case "GAME?":
                    	System.out.println("Demande de la liste de toutes les parties");
                        LinkedList<Partie> disponibles = new LinkedList<>();
                        for (Partie partie : Server.parties) {
                            // partie avec des joueurs non commencée :
                            if (partie.getJoueurs().size() != 0 && !partie.getState())disponibles.add(partie);
                        }
                        byte nbdispo = (byte)disponibles.size();//nombre de parties sur 1 octet
                        buffer = new byte[10];
                		System.arraycopy("GAMES ".getBytes(), 0, buffer, 0, 6);//"GAMES "
                    	buffer[6] = nbdispo;
                    	System.arraycopy("***".getBytes(), 0, buffer, 6+1, 3);//"***"
                    	os.write(buffer);
            			os.flush();

                     // Desription de chaque partie :
                        for (Partie pa : disponibles) {                                            
                            byte m = (byte)pa.getIdPartie();;//identifiant de la partie
                            byte s = (byte) pa.getJoueurs().size();
                    		buffer = new byte[12];
                    		System.arraycopy("OGAME ".getBytes(), 0, buffer, 0, 6);//"OGAME "
                        	buffer[6] = m;
                    		System.arraycopy(" ".getBytes(), 0, buffer, 6+1, 1);//" "
                        	buffer[7] = s;//s
                        	System.arraycopy("***".getBytes(), 0, buffer, 6+1+1+1, 3);		
                        	os.write(buffer);
                			os.flush();
                        }
                        break;
                    case "SHUTD":
                    	System.out.println("SHUT DOWN du serveur");
                        is.read();
                    	String pwd = parseId(is);
                    	boolean b = Server.tryShut(pwd);
                    	if(b==false) {
                    		os.write("DUNNO***".getBytes());
                			os.flush();
                    	}       	
                    break;
                }
            }

            synchronized (this.partie) {
                while (this.partie.getState() == false) {
                    try { 
                    	System.out.println("En attente des autres joueurs pour la partie "+partie.getIdPartie()+"...");
                    	this.partie.wait(); }
                    catch (InterruptedException e) {
                        break;
                    }
                }
                System.out.println("Attente terminee pour la partie "+partie.getIdPartie()+"!");
            }
            
            help = this.partie.getNbHelp();
            
            while (!this.partie.getEnd()) {
                // Pour parser les 5 premiers caractères
                byte[] buff_type = new byte[5];
                if (is.read(buff_type, 0, 5) < 5)continue;
               
                String r = "";
                String type = new String(buff_type);
                int dist = 0;
                boolean croise;
                boolean canMove;
                switch (type) {
                    case "UPMOV":
                        dist = parseDist(is);
                        croise = partie.upMove(this, dist, false);
                        if (croise)r = "MOVEF " + adjustPosition(this.posX) + " " + adjustPosition(this.posY) + " " + adjustScore(this.score) + "***";
                        else r = "MOVE! " + adjustPosition(this.posX) + " " + adjustPosition(this.posY) + "***";
                        send(os, r);                   
                        this.partie.isItTheEnd();
                        break;
                        
                    case "DOMOV":
                        dist = parseDist(is);
                        croise = partie.downMove(this, dist, false);
                        if (croise)r = "MOVEF " + adjustPosition(this.posX) + " " + adjustPosition(this.posY) + " " + adjustScore(this.score) + "***";
                        else r = "MOVE! " + adjustPosition(this.posX) + " " + adjustPosition(this.posY) + "***";
                        send(os, r);   
                        this.partie.isItTheEnd();
                        break;
                        
                    case "LEMOV":
                        dist = parseDist(is);
                        croise = partie.leftMove(this, dist, false);
                        if (croise)r = "MOVEF " + adjustPosition(this.posX) + " " + adjustPosition(this.posY) + " " + adjustScore(this.score) + "***";
                        else r = "MOVE! " + adjustPosition(this.posX) + " " + adjustPosition(this.posY) + "***";
                        send(os, r);
                        this.partie.isItTheEnd();
                        break;
                        
                    case "RIMOV":
                        dist = parseDist(is);
                        croise = partie.rightMove(this, dist, false);
                        if (croise)r = "MOVEF " + adjustPosition(this.posX) + " " + adjustPosition(this.posY) + " " + adjustScore(this.score) + "***";
                        else r = "MOVE! " + adjustPosition(this.posX) + " " + adjustPosition(this.posY) + "***";
                        send(os, r);
                        this.partie.isItTheEnd();
                        break;

                    case "UPBRE":
                        canMove = partie.upMove(this, 1, this.getNbBreak() > 0);
                        if (canMove)r = "OKBRE " + adjustPosition(this.posX) + " " + adjustPosition(this.posY) + " " + (this.nbBreak) + "***";
                        else r = "NOBRE " + adjustPosition(this.posX) + " "+ adjustPosition(this.posY)+"***";
                        send(os, r);
                        this.partie.getLabyrinthe().printLabyrinth();
                        this.partie.isItTheEnd();
                        break;

                    case "DOBRE":
                        canMove = partie.downMove(this, 1, this.getNbBreak() > 0);
                        if (canMove)r = "OKBRE " + adjustPosition(this.posX) + " " + adjustPosition(this.posY) + " " + (this.nbBreak) + "***";
                        else r = "NOBRE " + adjustPosition(this.posX) + " "+ adjustPosition(this.posY)+"***";
                        send(os, r);                
                        this.partie.getLabyrinthe().printLabyrinth();
                        this.partie.isItTheEnd();
                        break;

                    case "LEBRE":
                        canMove = partie.leftMove(this, 1, this.getNbBreak() > 0);
                        if (canMove)r = "OKBRE " + adjustPosition(this.posX) + " " + adjustPosition(this.posY) + " " + (this.nbBreak) + "***";
                        else r = "NOBRE " + adjustPosition(this.posX) + " "+ adjustPosition(this.posY)+"***";
                        send(os, r);
                        this.partie.getLabyrinthe().printLabyrinth();
                        this.partie.isItTheEnd();
                        break;
                    case "RIBRE":
                        canMove = partie.rightMove(this, 1, this.getNbBreak() > 0);
                        if (canMove)r = "OKBRE " + adjustPosition(this.posX) + " " + adjustPosition(this.posY) + " " + (this.nbBreak) + "***";
                        else r = "NOBRE " + adjustPosition(this.posX) + " "+ adjustPosition(this.posY)+"***";
                        send(os, r);
                        this.partie.getLabyrinthe().printLabyrinth();
                        this.partie.isItTheEnd();
                        break;

                    case "GLIS?": 
                        byte s = (byte)partie.getJoueurs().size();
                        System.out.println("Demande de la liste des joueurs et de leurs points dans la partie " +partie.getIdPartie());
                        byte [] buf = new byte[10];
                        System.arraycopy("GLIS! ".getBytes(), 0, buf, 0, 6);//"GLIS! "
                    	buf[6] = s;
                    	System.arraycopy("***".getBytes(), 0, buf, 6+1, 3);//"***"
                    	os.write(buf);
            			os.flush();
                        for (Joueur j : partie.getJoueurs()) {
                            String des_play = "GPLYR " + new String(j.id) + " " + adjustPosition(j.posX) + " " + adjustPosition(j.posY) + " " + adjustScore(j.score) + "***";
                            send(os, des_play);
                        }
                        break;
                    case "MALL?":
                        System.out.println("Envoi de messages à tous sur la partie " +partie.getIdPartie());
                        String message = parseMessage(is);
                        // envoi multiple :
                        String mess = "MESSA " + this.id + " " + message + "+++";
                        boolean sendAllSuccess = this.partie.multicast(mess.getBytes());
                        // reponse au client :
                        if (sendAllSuccess)send(os, "MALL!***");
                        else send(os, "NSEND***");
                        break;
                    case "SEND?":
                    	byte[] id_player = new byte[8];
                        is.read();
                        is.read(id_player, 0, 8);
                        String mp = "MESSP "+new String(this.id)+" "+parseMessage(is);
                        // envoi à l'autre joueur :
                        String rep_mp;
                        System.out.println("Envoi de message privé de "+ this.id+" à " + new String(id_player));
                        if (sendMP(id_player, mp))rep_mp = "SEND!***";
                        else rep_mp = "NSEND***";
                        send(os, rep_mp);
                        break;
                    case "IQUIT":
                    	System.out.println("Demande de quitter la partie");
                        String goodbye = "GOBYE***";
                        send(os, goodbye);
                        partie.getJoueurs().remove(this);
                        if(partie.getJoueurs().isEmpty()) {
                        	partie.setEnd(true);
                        	partie.isItTheEnd();
                        }
                        Server.joueurs.remove(this);
                        is.close();
                        os.close();
                        socket.close();
                        return;
                    case "HELP?":
                    	System.out.println("Le joueur a demandé de l'aide.");
                        buf= new byte[8];
                        if (help <= 0){ //[NHELP***]
                            System.arraycopy("NHELP***".getBytes(), 0, buf, 0, 8);
                            os.write(buf);
                            os.flush();
                        }else{ // [HELP! N R***](5+4+3 = 12) + N*[OHELP T X Y***](5+2+4+4+3 = 18)
                            help -= 1;
                            LinkedList<String> carte = this.partie.getLabyrinthe().printHelpLabyrinth(posX,posY);
                            byte N = (byte)carte.size();
                            byte R = (byte)help;
                            buf = new byte[12];
                            System.arraycopy("HELP! ".getBytes(), 0, buf, 0, 6);//"HELP! "
                            buf[6] = N;//N
                            System.arraycopy(" ".getBytes(), 0, buf, 6+1, 1);//" "
                            buf[8] = R;//R
                            System.arraycopy("***".getBytes(), 0, buf, 9, 3);//"***"
                            os.write(buf);
                            os.flush();
                            for (String str : carte)send(os, str);
                        }
                        break;
                    default:
                        String idk = "DUNNO***";
                        send(os, idk);
                        break;
                }
            }
        } catch (Exception e) {
            if(partie!=null)partie.getJoueurs().remove(this);
				try {
					os.close();
					is.close();
					socket.close();
				} catch (IOException e1) {e1.printStackTrace();}
            return;
        }
    }
    //Parse l'id du joueur
    private String parseId(InputStream is) throws IOException {
        byte[] buffId = parse(is, 8);
        String tmp = new String(buffId);
        return tmp;
    }
    //Parse le port du joueur
    private int parsePort(InputStream is) throws IOException {
        byte[] buffPort = parse(is, 4);
        String tmp = new String(buffPort);
        return Integer.parseInt(tmp);
    }

    //Parse le numero de partie et renvoie l'objet Partie p correspondant
    private Partie parseNumPartie(InputStream is) throws IOException {
        is.read();
        byte[] buff_num = parse(is, 1);
        String tmp = new String(buff_num);
        is.read();
        int m = Integer.parseInt(tmp);
        for (Partie p : Server.parties) {
            if (p.getIdPartie() == m) {
                // la partie existe :
                if (!p.getState()) return p;
            }
        }
        return null;
    }
    //Parse la distance du deplacement
    public int parseDist(InputStream is) throws IOException {
        is.read();
        byte[] buff_dist = parse(is, 3);
        String dist_str = new String(buff_dist);
        return Integer.parseInt(dist_str);
    }
    //Parse un message
    public String parseMessage(InputStream is) throws IOException {
        String message = "";
        is.read();
        char c;
        while ((c = (char) is.read()) != '*') {
            message+=(c);
        }
        return message+ "+++";
    }
    public int parseDist(InputStreamReader is) {
        char[] buff_dist = new char[4];
        int lu;
        int dist = 0;
        try {
            lu = is.read(buff_dist, 0, 4);
            if (lu == -1) System.exit(1);
            String dist_str = new String(buff_dist);
            dist_str = dist_str.replaceAll(" ", "");
            dist = Integer.parseInt(dist_str);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dist;
    }
    private byte[] parse(InputStream is, int length) throws IOException {
        byte[] buffer = new byte[length];
        int lu = is.read(buffer, 0, length);
        assert lu != -1;
        return buffer;
    }

    //Envoi d'un message privé
    public boolean sendMP(byte[] id_player, String mp) {
        String id_player_s = new String(id_player);
        Joueur j = null;
        // Trouver le joueur dans la partie
        for (Joueur i : this.partie.getJoueurs()) {
            if (id_player_s.equals(new String(i.id))) {
                j = i;
                break;
            }
        }
        if (j == null)return false;
        else {
        	byte[] m = mp.getBytes();
            try (DatagramSocket test = new DatagramSocket()) {
                System.out.println("J'envoie sur le port numero " + j.port + " jip :" + j.jip+ " le message " + mp);
                DatagramPacket paquet = new DatagramPacket(m, m.length, InetAddress.getByName(j.jip), j.port);
                test.send(paquet);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    //envois simples :
    public void send(OutputStream os, String response) throws IOException {
        if(!this.socket.isClosed()) {
        	os.write(response.getBytes());
            os.flush();
        }
    	
    }
    
    //pour ajuster la position et renvoyer la bonne string de taille 3 formatée
    public String adjustPosition(int x) {
    	String s = "";
    	if(x < 10) s= "00"+String.valueOf(x);
    	else if (x<100) s= "0"+String.valueOf(x);
    	else s=String.valueOf(x);
    	return s;
    }
    //traiter les scores trop grands
    public String adjustScore(int p) {
    	String s = "";
    	if(p < 10) s= "000"+String.valueOf(p);
    	else if (p<100) s= "00"+String.valueOf(p);
    	else if(p<1000) s="0"+String.valueOf(p);
    	else s=String.valueOf(p);
    	return s;
    }
    
    public boolean checkId(String id, Partie p) {
    	if(p!=null) {
    		for (Joueur j : p.getJoueurs()) {
        		if(j.id.equals(id))return false;
       		}
    	}
   		return true;	
    }
    public boolean checkIP(String ip, int port, Partie p) {
    	if(p!=null) {
    		for (Joueur j : p.getJoueurs()) {
        		if(j.jip.equals(ip) && port==j.port)return false;
       		}
    	}
   		return true;
    }
    public void setPartieAndJoueur(Partie p) {
        this.partie = p;
        p.getJoueurs().add(this);// faire des vérifi pour que pas de doublons
    }

    public void setPartie(Partie p) {
        this.partie = p;
    }

    public int getScore() {
        return this.score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getX() {
        return this.posX;
    }

    public int getY() {
        return this.posY;
    }

    public int getPortInt() {
        return (this.port);
    }

    public void setX(int x) {
        this.posX = x;
    }

    public void setY(int y) {
        this.posY = y;
    }

    public void setStart(boolean s) {
        this.start = s;
    }

    public boolean getStart() {
        return this.start;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSocket(Socket s) {
        this.socket = s;
    }

    public String getIdString() {
        return new String(this.id);
    }

    public OutputStream getOw() {
        return this.os;
    }
    
    public Socket getSocket() {
    	return this.socket;
    }

	public InputStream getIs() {
		return this.is;
	}

    public int getNbBreak() {
        return nbBreak;
    }
    public Thread getCurrentThread() {
		return currentThread;
	}

    public void setNbBreak(int nbBreak) {
        this.nbBreak = nbBreak;
    }
}
