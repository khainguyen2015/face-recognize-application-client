/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package facerecognizeapp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.*;
import java.util.Properties;


/**
 *
 * @author macbook
 */
public class ConnectSQL {
    private String url;
    private String username;
    private String password;
    
    
    public ConnectSQL() {
        
    }

    public ConnectSQL(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }
    
    

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public Connection getConnection() throws SQLException{
        Connection conn = null;
        Properties prop = new Properties();
        prop.setProperty("user", username);
        prop.setProperty("password", password);
        conn = DriverManager.getConnection(url, prop);
        return conn;
    } 
    
}

 
