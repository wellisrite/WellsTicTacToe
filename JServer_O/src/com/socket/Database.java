package com.socket;

import java.io.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;

public class Database {
    
    
    public Database(String filePath){
        
    }
    
    public boolean userExists(String username){
        
        return false;
    }
    
    public boolean checkLogin(String username, String password){
        
        return true;
    }
    
    public void addUser(String username, String password){
        
        
	}
    
    public static String getTagValue(String sTag, Element eElement) {
	return "";
  }
}
