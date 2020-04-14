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
import java.awt.event.*;
import java.awt.*;
public class CustomB extends JButton implements ActionListener{
  ImageIcon O,X;
  byte value=0;
  String XorO,status="";
  int number;
  tictactoe ui;
  public CustomB(tictactoe frame,String a,int b){
    setBackground(Color.black);
    ui=frame;
    number=b;
    X=new ImageIcon(this.getClass().getResource("../../img/1.png"));
    O=new ImageIcon(this.getClass().getResource("../../img/2.png"));
    this.addActionListener(this);
    XorO=a;
  }
  public void actionPerformed(ActionEvent e){
      if(status!="Xclicked"&&status!="Oclicked"){
      if(XorO=="X"){
       //value=2;
          System.out.println("status"+status);
        ui.client.send(new Message("setX",ui.username,""+number,"SERVER"));
        status="X";
      }
      else if(XorO=="O"){
         // value=3;
             System.out.println("status"+status);
          ui.client.send(new Message("setO",ui.username,""+number,"SERVER"));
          status="O";
        }
     }
//    switch(value){
//      case 0:
//        setIcon(null);
//        break;
//      case 1:
//        setIcon(X);
//        break;
//      case 2:
//        setIcon(O);
//        break;
//    }
  }
}

