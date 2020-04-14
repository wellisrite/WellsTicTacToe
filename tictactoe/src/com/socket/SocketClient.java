package com.socket;


import java.io.*;
import java.net.*;
import java.util.Date;
import javafx.geometry.HorizontalDirection;
import javafx.geometry.VerticalDirection;
import javax.swing.JDesktopPane;

public class SocketClient implements Runnable{
    
    public int port;
    public String serverAddr;
    public Socket socket;
    public tictactoe ui;
    public ObjectInputStream In;
    public ObjectOutputStream Out;
    public int clicked=0,diagonal1=0,diagonal2=0,horizontal1=0,horizontal2=0,horizontal3=0;
    
    public SocketClient(tictactoe frame) throws IOException{
        ui = frame; this.serverAddr = ui.serverAddr; this.port = ui.port;
        socket = new Socket(InetAddress.getByName(serverAddr), port);
            
        Out = new ObjectOutputStream(socket.getOutputStream());
        Out.flush();
        In = new ObjectInputStream(socket.getInputStream());
        
        
    }

    @Override
    public void run() {
        boolean keepRunning = true;
        while(keepRunning){
            try {
                Message msg = (Message) In.readObject();
                System.out.println("Incoming : "+msg.toString());
                if(msg.type.equals("test")){
                    ui.start.setEnabled(false);
                    ui.log.append("Searching for Opponent!");
                    send(new Message("search",ui.namefield.getText(),"searchContent","SERVER"));
                }
                else if(msg.type.equals("start")){
                    ui.panel2.setVisible(false);
                    ui.panel.setVisible(true);
                    ui.log.append("\nOpponent found!");
                    ui.setTitle("Tic tac toe Client:"+ui.username+" "+ui.XorO+" Side");
                }
                else if(msg.type.equals("getX")){
                    ui.XorO="X";
                    ui.declareButton();
                }
                else if(msg.type.equals("getO")){
                    ui.XorO="O";
                    ui.declareButton();
                }
                else if(msg.type.equals("setX")){
                    ui.buttons[Integer.parseInt(msg.content)].setIcon(ui.buttons[Integer.parseInt(msg.content)].X);
                    ui.buttons[Integer.parseInt(msg.content)].status="Xclicked";
                    checkCondition();
                }
                else if(msg.type.equals("setO")){
                    ui.buttons[Integer.parseInt(msg.content)].setIcon(ui.buttons[Integer.parseInt(msg.content)].O);
                    ui.buttons[Integer.parseInt(msg.content)].status="Oclicked";
                    checkCondition();
                }
                else if(msg.type.equals("gamedraw")){
                    ui.gamedraw();
                    ui.panel2.setVisible(true);
                    ui.panel.setVisible(false);
                    ui.start.setEnabled(true);
                }
                else if(msg.type.equals("dc")){
                    ui.gamewin(ui.XorO);
                }
                else{
                    ui.log.append("[SERVER > Me] : Unknown message type\n");
                }
            }
            catch(Exception ex) {
                keepRunning = false;
                ui.log.append("[Application > Me] : Connection Ended!\n");
                ui.clientThread.stop();
                
                System.out.println("Exception SocketClient run()");
                ex.printStackTrace();
            }
        }
    }
    public void checkCondition(){
            String horizontal1=horizontalCheck(0);
            String horizontal2=horizontalCheck(3);
            String horizontal3=horizontalCheck(7);
            String vertical1=verticalCheck(0);
            String vertical2=verticalCheck(1);
            String vertical3=verticalCheck(2);
            String diagonal1=diagonalCheck(0);
            String diagonal2=diagonalCheck(2);
            if(horizontal1=="Xwins"||horizontal2=="Xwins"||horizontal3=="Xwins"||vertical1=="Xwins"||vertical2=="Xwins"||vertical3=="Xwins"||diagonal1=="Xwins"||diagonal2=="Xwins"){
                ui.gamewin("X");
            }
            else if(horizontal1=="Owins"||horizontal2=="Owins"||horizontal3=="Owins"||vertical1=="Owins"||vertical2=="Owins"||vertical3=="Owins"||diagonal1=="Owins"||diagonal2=="Owins"){
                ui.gamewin("O");
            }
        clicked++;
        System.out.println(clicked);
        if(clicked==9){
            send(new Message("gamedraw", ui.username, "", "SERVER"));
        }
    }
    public String diagonalCheck(int o){
        if(o==0){
            if(ui.buttons[o].status=="Xclicked"&&ui.buttons[4].status=="Xclicked"&&ui.buttons[8].status=="Xclicked"){
                return "Xwins";
            }
            else if(ui.buttons[o].status=="Oclicked"&&ui.buttons[4].status=="Oclicked"&&ui.buttons[8].status=="Oclicked"){
                return "Owins";
            }
        }else{
            if(ui.buttons[o].status=="Xclicked"&&ui.buttons[4].status=="Xclicked"&&ui.buttons[6].status=="Xclicked"){
                return "Xwins";
            }
            else if(ui.buttons[o].status=="Oclicked"&&ui.buttons[4].status=="Oclicked"&&ui.buttons[6].status=="Oclicked"){
                return "Owins";
            }
        }
        return "nothing";
    }
    public String horizontalCheck(int o){
        if(ui.buttons[o].status=="Xclicked"&&ui.buttons[o+1].status=="Xclicked"&&ui.buttons[o+2].status=="Xclicked"){
            return "Xwins";
        }
        else if(ui.buttons[o].status=="Oclicked"&&ui.buttons[o+1].status=="Oclicked"&&ui.buttons[o+2].status=="Oclicked"){
            return "Owins";
        }
        System.out.println("horizontal check"+o+" "+ui.buttons[o].status);
        return "nothing";
    }
    public String verticalCheck(int o){
        if(ui.buttons[o].status=="Xclicked"&&ui.buttons[o+3].status=="Xclicked"&&ui.buttons[o+6].status=="Xclicked"){
            return "Xwins";
        }
        else if(ui.buttons[o].status=="Oclicked"&&ui.buttons[o+3].status=="Oclicked"&&ui.buttons[o+6].status=="Oclicked"){
            return "Owins";
        }
        return "nothing";
    }
    public void send(Message msg){
        try {
            Out.writeObject(msg);
            Out.flush();
            System.out.println("Outgoing : "+msg.toString());
            
            if(msg.type.equals("message") && !msg.content.equals(".bye")){
                String msgTime = (new Date()).toString();
                
            }
        } 
        catch (IOException ex) {
            System.out.println("Exception SocketClient send()");
        }
    }
    
    public void closeThread(Thread t){
        t = null;
    }
}
