/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.socket;

/**
 *
 * @author well
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
public class tictactoe extends JFrame{
  public SocketClient client;
  public int port=13000;
  public String serverAddr="localhost",username,XorO;
  public Thread clientThread;
  JPanel panel,panel2,panel3,cards;
  JLabel name;
  JButton start;
  JTextField namefield;
  JTextArea log;
  CustomB buttons[]=new CustomB[9];
  public tictactoe(){
    initComponents();
        this.addWindowListener(new WindowListener() {

            @Override public void windowOpened(WindowEvent e) {}
            @Override public void windowClosing(WindowEvent e) { try{ client.send(new Message("message", username, ".bye", "SERVER")); clientThread.stop();  }catch(Exception ex){} }
            @Override public void windowClosed(WindowEvent e) {}
            @Override public void windowIconified(WindowEvent e) {}
            @Override public void windowDeiconified(WindowEvent e) {}
            @Override public void windowActivated(WindowEvent e) {}
            @Override public void windowDeactivated(WindowEvent e) {}
        });
//    model.addElement("All");
    this.setTitle("Tic Tac Toe Client");
    this.setSize(500,400);
    setResizable(true);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    cards=new JPanel(new CardLayout());
    cards.add(panel2);
    add(cards);
    //panel.setVisible(false);
    setVisible(true);
  }
  public void initComponents(){
      panel=new JPanel();
      panel2=new JPanel(new BorderLayout());
      panel3=new JPanel();
      name=new JLabel("Name");
      start=new JButton("Start");
      panel2.setPreferredSize(new Dimension(40,50));
      start.addActionListener((new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            startActionPerformed(evt);
            }
        }));
      log=new JTextArea();
      log.setText("LOGS\n");
      namefield= new JTextField(30);
      panel3.add(name);
      panel3.add(namefield);
      panel2.add(panel3,BorderLayout.NORTH);
      panel2.add(start,BorderLayout.CENTER);
      panel2.add(log,BorderLayout.SOUTH);
      log.setPreferredSize(new Dimension(200,150));
      panel.setLayout(new GridLayout(3,3));
  }
  public void startActionPerformed(java.awt.event.ActionEvent evt){
      System.out.println(namefield.getText());
       if(namefield.getText().equals("")){
           JOptionPane.showMessageDialog(this, "Please insert player's name!");
       }else{
           try{
                username=namefield.getText();
                client = new SocketClient(this) ;
                clientThread = new Thread(client);
                clientThread.start();
                client.send(new Message("test",namefield.getText(), "testContent", "SERVER"));
            }
            catch(Exception ex){
                log.append("[Application > Me] : Server not found\n");
            }
       }
  }
  public void declareButton(){
      for(int i=0;i<9;i++){
        buttons[i]=new CustomB(this,XorO,i);
        panel.add(buttons[i]);
      }
      cards.add(panel);
      add(cards);
  } 
   public void gamedraw(){
       JOptionPane.showMessageDialog(this,"Game Draw");
       for(int i=0;i<9;i++)
       panel.remove(buttons[i]);
   }
   public void gamewin(String XorO){
       JOptionPane.showMessageDialog(this,XorO+" Wins!");
       for(int i=0;i<9;i++)
       panel.remove(buttons[i]);
       panel2.setVisible(true);
       panel.setVisible(false);
       start.setEnabled(true);
   }
  public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } 
        catch(Exception ex){
            System.out.println("Look & Feel exception");
        }
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new tictactoe().setVisible(true);
            }
        });
    }
}
