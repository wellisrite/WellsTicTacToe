package com.socket;

import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

class ServerThread extends Thread { 
	
    public SocketServer server = null;
    public Socket socket = null;
    public int ID = -1,OID,turn;
    public String username = "",status="idle";
    public ObjectInputStream streamIn  =  null;
    public ObjectOutputStream streamOut = null;
    public ServerFrame ui;

    public ServerThread(SocketServer _server, Socket _socket){  
    	super();
        server = _server;
        socket = _socket;
        ID     = socket.getPort();
        ui = _server.ui;
    }
    
    public void send(Message msg){
        try {
            streamOut.writeObject(msg);
            streamOut.flush();
        } 
        catch (IOException ex) {
            System.out.println("Exception [SocketClient : send(...)]");
        }
    }
    
    public int getID(){  
	    return ID;
    }
   
    @SuppressWarnings("deprecation")
	public void run(){  
    	ui.jTextArea1.append("\nServer Thread " + ID + " running.");
        while (true){  
    	    try{  
                Message msg = (Message) streamIn.readObject();
    	    	server.handle(ID, msg);
                    
            }
            catch(Exception ioe){  
            	System.out.println(ID + " ERROR reading: " + ioe.getMessage());
                server.remove(ID);
                stop();
            }
        }
    }
    
    public void open() throws IOException {  
        streamOut = new ObjectOutputStream(socket.getOutputStream());
        streamOut.flush();
        streamIn = new ObjectInputStream(socket.getInputStream());
    }
    
    public void close() throws IOException {  
    	if (socket != null)    socket.close();
        if (streamIn != null)  streamIn.close();
        if (streamOut != null) streamOut.close();
    }
}





public class SocketServer implements Runnable {
    
    public ServerThread clients[];
    public ServerSocket server = null;
    public Thread       thread = null;
    public int clientCount = 0, port = 13000;
    public ServerFrame ui;
    public Database db;
    public ResultSet rs ;

    public SocketServer(ServerFrame frame){
       
        clients = new ServerThread[50];
        ui = frame;
        db = new Database(ui.filePath);
        
	try{  
	    server = new ServerSocket(port);
            port = server.getLocalPort();
	    ui.jTextArea1.append("Server startet. IP : " + InetAddress.getLocalHost() + ", Port : " + server.getLocalPort());
	    start(); 
        }
	catch(IOException ioe){  
            ui.jTextArea1.append("Can not bind to port : " + port + "\nRetrying"); 
            ui.RetryStart(0);
	}
    }
    
    public SocketServer(ServerFrame frame, int Port){
       
        clients = new ServerThread[50];
        ui = frame;
        port = Port;
        db = new Database(ui.filePath);
	try{  
	    server = new ServerSocket(port);
            port = server.getLocalPort();
	    ui.jTextArea1.append("Server startet. IP : " + InetAddress.getLocalHost() + ", Port : " + server.getLocalPort());
	    start(); 
        }
	catch(IOException ioe){  
            ui.jTextArea1.append("\nCan not bind to port " + port + ": " + ioe.getMessage()); 
	}
    }
	
    public void run(){  
	while (thread != null){  
            try{  
		ui.jTextArea1.append("\nWaiting for a client ..."); 
	        addThread(server.accept()); 
	    }
	    catch(Exception ioe){ 
                ui.jTextArea1.append("\nServer accept error: \n");
                ui.RetryStart(0);
	    }
        }
    }
	
    public void start(){  
    	if (thread == null){  
            thread = new Thread(this); 
	    thread.start();
	}
    }
    
    @SuppressWarnings("deprecation")
    public void stop(){  
        if (thread != null){  
            thread.stop(); 
	    thread = null;
	}
    }
    
    private int findClient(int ID){  
    	for (int i = 0; i < clientCount; i++){
        	if (clients[i].getID() == ID){
                    return i;
                }
	}
	return -1;
    }
    public void searchOpponent(int ID){
            System.out.println(ID+" "+clients[findClient(ID)].status);
            for(int i=0;i< clientCount;i++){
                if(clients[i].status.equals("searching")&&clients[i].ID!=ID){
                    clients[i].status="playing";
                    clients[i].OID=ID;
                    clients[findClient(ID)].status="playing";
                    clients[findClient(ID)].OID=clients[i].ID;
                    clients[findClient(ID)].send(new Message("getO","SERVER"," ",clients[findClient(ID)].username));
                    clients[i].send(new Message("start", "SERVER", "Start", clients[i].username));
                    clients[findClient(ID)].send(new Message("start", "SERVER", "Start",clients[findClient(ID)].username));
                    return;
                }
                System.out.println("client"+clients[i].ID+"status "+clients[i].status+" ID searcher"+ID);
        }
        System.out.println(ID+" "+clients[findClient(ID)].status);
        clients[findClient(ID)].send(new Message("getX", "SERVER", " ", clients[findClient(ID)].username));
        clients[findClient(ID)].turn=1;
    }
	
    public synchronized void handle(int ID, Message msg){  
	if (msg.content.equals(".bye")){
            System.out.println(ID+" "+clients[findClient(ID)].OID);
            clients[findClient(clients[findClient(ID)].OID)].send(new Message("dc","SERVER","","opponent"));
            Announce("signout", "SERVER", msg.sender);
            
            remove(clients[findClient(ID)].OID);
            remove(ID); 
	}
	else{
            if(msg.type.equals("test")){
                clients[findClient(ID)].send(new Message("test", "SERVER", "OK", msg.sender));
            }
            else if(msg.type.equals("search")){
                clients[findClient(ID)].status="searching";
                ui.jTextArea1.append("Searching opponent for "+msg.sender);
                searchOpponent(ID);
            }
            else if(msg.type.equals("setX")){
                if(clients[findClient(ID)].turn==1){
                    clients[findClient(ID)].send(new Message("setX", "SERVER", ""+msg.content, clients[findClient(ID)].username));
                    clients[findClient(clients[findClient(ID)].OID)].send(new Message("setX", "SERVER", ""+msg.content, "opponent"));
                    clients[findClient(ID)].turn=0;
                    clients[findClient(clients[findClient(ID)].OID)].turn=1;
                }
            }
            else if(msg.type.equals("setO")){
                if(clients[findClient(ID)].turn==1){
                    clients[findClient(ID)].send(new Message("setO", "SERVER", ""+msg.content, clients[findClient(ID)].username));
                    clients[findClient(clients[findClient(ID)].OID)].send(new Message("setO", "SERVER", ""+msg.content, "opponent"));
                    clients[findClient(ID)].turn=0;
                    clients[findClient(clients[findClient(ID)].OID)].turn=1;
                }
            }
            else if(msg.type.equals("gamedraw")){
                clients[findClient(ID)].send(new Message("gamedraw", "SERVER", "", clients[findClient(ID)].username));
                    clients[findClient(clients[findClient(ID)].OID)].send(new Message("gamedraw", "SERVER", "", "opponent"));
                    remove(clients[findClient(ID)].OID);
                    remove(ID);
            }
            else if(msg.type.equals("api")){
                final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
            final String DB_URL = "jdbc:mysql://localhost/javamysql";
            
   //  Database credentials
   final String USER = "root";
   final String PASS = "";
   
    
   Connection conn = null;
   Statement stmt = null;
   try{
      //STEP 2: Register JDBC driver
      Class.forName("com.mysql.jdbc.Driver");

      //STEP 3: Open a connection
      System.out.println("Connecting to database...");
      conn = DriverManager.getConnection(DB_URL,USER,PASS);

      //STEP 4: Execute a query
      System.out.println("Creating statement...");
      stmt = conn.createStatement   ();
      String sql;
      sql = "SELECT NIM, NAMA, ALAMAT, UMUR FROM mhs where NIM = \""+clients[findClient(ID)].username+"\"";
       System.out.println(sql);
      rs = stmt.executeQuery(sql);
          int id,age,arr=0;
        String first=null,last=null,content = null;
      //STEP 5: Extract data from result set
      while(rs.next()){
          System.out.println(rs.getRow());
          arr++;
      }
      rs.beforeFirst();
       System.out.println(rs.getRow());
      if(arr!=0){
      while(rs.next()){
         //Retrieve by column name
         id  = rs.getInt("NIM");
         age = rs.getInt("UMUR");
         first = rs.getString("NAMA");
         last = rs.getString("ALAMAT");

         //Display values
         System.out.print("NIM: " + id);
         System.out.print(", Umur: " + age);
         System.out.print(", Nama: " + first);
         System.out.println(", Alamat: " + last);
         content=("Nim: "+id+" Age: "+age+" First: "+first+" Last: "+last);
      }
       clients[findClient(ID)].username=first;
       clients[findClient(ID)].send(new Message("api_chg","SERVER",clients[findClient(ID)].username,msg.sender));
       clients[findClient(ID)].send(new Message("api_res", "SERVER", content, msg.sender));
      }
      else{
          content="Nim not found loging in as guest!";
          clients[findClient(ID)].send(new Message("api_res", "SERVER", content, msg.sender));
      }
      //STEP 6: Clean-up environment
      rs.close();
      stmt.close();
      conn.close();
   }catch(SQLException se){
      //Handle errors for JDBC
      se.printStackTrace();
   }catch(Exception e){
      //Handle errors for Class.forName
      e.printStackTrace();
   }finally{
      //finally block used to close resources
      try{
         if(stmt!=null)
            stmt.close();
      }catch(SQLException se2){
      }// nothing we can do
      try{
         if(conn!=null)
            conn.close();
      }catch(SQLException se){
         se.printStackTrace();
      }//end finally try
   }//end try
   System.out.println("Goodbye!");

	}
    }
    }
    public void Announce(String type, String sender, String content){
        Message msg = new Message(type, sender, content, "All");
        for(int i = 0; i < clientCount; i++){
            clients[i].send(msg);
        }
    }
    
    public void SendUserList(String toWhom){
        for(int i = 0; i < clientCount; i++){
            findUserThread(toWhom).send(new Message("newuser", "SERVER", clients[i].username, toWhom));
        }
    }
    
    public ServerThread findUserThread(String usr){
        for(int i = 0; i < clientCount; i++){
            if(clients[i].username.equals(usr)){
                return clients[i];
            }
        }
        return null;
    }
	
    @SuppressWarnings("deprecation")
    public synchronized void remove(int ID){  
    int pos = findClient(ID);
        if (pos >= 0){  
            ServerThread toTerminate = clients[pos];
            ui.jTextArea1.append("\nRemoving client thread " + ID + " at " + pos);
	    if (pos < clientCount-1){
                for (int i = pos+1; i < clientCount; i++){
                    clients[i-1] = clients[i];
	        }
	    }
	    clientCount--;
	    try{  
	      	toTerminate.close(); 
	    }
	    catch(IOException ioe){  
	      	ui.jTextArea1.append("\nError closing thread: " + ioe); 
	    }
	    toTerminate.stop(); 
	}
    }
    
    private void addThread(Socket socket){  
	if (clientCount < clients.length){  
            ui.jTextArea1.append("\nClient accepted: " + socket);
	    clients[clientCount] = new ServerThread(this, socket);
	    try{  
	      	clients[clientCount].open(); 
	        clients[clientCount].start();  
	        clientCount++; 
	    }
	    catch(IOException ioe){  
	      	ui.jTextArea1.append("\nError opening thread: " + ioe); 
	    } 
	}
	else{
            ui.jTextArea1.append("\nClient refused: maximum " + clients.length + " reached.");
	}
    }
}
