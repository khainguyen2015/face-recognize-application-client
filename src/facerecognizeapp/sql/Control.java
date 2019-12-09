/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package facerecognizeapp.sql;

import facerecognizeapp.ConnectSQL;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author macbook
 */
public class Control {
    
    private static final String URL = "jdbc:mysql://localhost:3306/facedetectapplication";
    private static final String USERNAME = "khai";
    private static final String PASSWORD = "123456";
    

    private final Connection conn;
    public Control() throws SQLException  {   
        Properties prop = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("resources/database.properties")) {
            prop.load(input); 
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        ConnectSQL connectSQL = new ConnectSQL(prop.getProperty("jdbcUrl")
                                               ,prop.getProperty("username")
                                               ,prop.getProperty("password"));
        conn = connectSQL.getConnection();
        System.out.println("Ket noi CSDL thanh cong !!!");
    }
    
    
    public FDA_Account getAccount(String username, String password) throws SQLException {
        if(username == null || password == null) {
            return null;
        }
        FDA_Account account = null;
        String sql;
        sql = "SELECT * FROM ACCOUNT_CB WHERE USER_NAME = ? AND PASS_WORD = ?";
        PreparedStatement ps= conn.prepareStatement(sql);
        ps.setString(1, username);
        ps.setString(2, password);
        ResultSet rs=ps.executeQuery();
        if (rs.next()) {
            account = new FDA_Account();
            account.setId(rs.getString("MSCB"));
            account.setPrivilege(rs.getString("Privilege"));
        }
        return account;
    }
    
    public void closeConnection() throws SQLException {
        conn.close();
    }
    
    
    public FDA_Canbo getCanBo(String j) {
        FDA_Canbo cb = new FDA_Canbo();
        String sql;
        sql = "SELECT * FROM CANBO WHERE MSCB = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1,j);
            ResultSet rs=ps.executeQuery();
                while(rs.next()) {
                    cb.setMscb(rs.getString("mscb"));
                    cb.setTencb(rs.getString("tencb"));
                    cb.setNgaysinh(rs.getDate("ngaysinhcb"));
                    cb.setGioitinh(rs.getString("gioitinh"));
                    cb.setBomon(rs.getString("bomon"));
                    cb.setKhoa(rs.getString("khoa"));
                    cb.setAvatarPath(rs.getString("avatar_path"));
                }
        } catch (SQLException ex) {
            Logger.getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);
            //System.out.println("Khong co ma nhan vien nay !!!");
        }
       return cb; 
    }
    
    public boolean updateCanBo(FDA_Canbo canbo) {
        String sql;
        sql = "CALL UPDATECANBO(?, ?, ?, ?, ?, ?, ?)";
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        java.sql.Date sqlDate = java.sql.Date.valueOf(df.format(canbo.getNgaysinh()));
        try {
            PreparedStatement ps;
            ps = conn.prepareCall(sql);
            ps.setString(1, canbo.getMscb());
            ps.setString(2, canbo.getTencb());
            ps.setDate(3, sqlDate);
            ps.setString(4, canbo.getGioitinh());
            ps.setString(5, canbo.getBomon());
            ps.setString(6, canbo.getKhoa());
            ps.setString(7, canbo.getAvatarPath());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            Logger.getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);
            //System.out.println("Khong co ma nhan vien nay !!!");
        }
        return false;
    }
    
    
    
    public boolean themSinhvien(FDA_Sinhvien s){
        String sql;
        sql = "CALL THEM_SINHVIEN(?,?,?,?,?,?,?)";
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
            PreparedStatement ps;
            ps = conn.prepareCall(sql);
            ps.setString(1, s.getMssv());
            ps.setString(2, s.getTensv());
            ps.setString(3, df.format(s.getNgaysinhsv()));
            ps.setString(4, s.getGioitinh());
            ps.setString(5, s.getNganh());
            ps.setString(6, s.getKhoas());
            ps.setString(7, s.getKhoa());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            Logger.getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public FDA_Sinhvien getSinhVien(String i) {
        FDA_Sinhvien s = null;
        String sql;
        sql = "SELECT * FROM SINHVIEN WHERE MSSV = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1,i);
            ResultSet rs=ps.executeQuery();
                while(rs.next()) {
                    s = new FDA_Sinhvien();
                    s.setMssv(rs.getString("mssv"));
                    s.setTensv(rs.getString("tensv"));
                    s.setNgaysinhsv(rs.getDate("ngaysinhsv"));
                    s.setGioitinh(rs.getString("gioitinh"));
                    s.setNganh(rs.getString("nganh"));
                    s.setKhoas(rs.getString("khoas"));
                    s.setKhoa(rs.getString("khoa"));  
                }
        } catch (SQLException ex) {
            Logger.getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);
            //System.out.println("Khong co ma nhan vien nay !!!");
        }
       return s; 
    }
    
    public ArrayList<FDA_Lichsudiemdanh> doclichsu(Date date) {
        String sql;
        ArrayList<FDA_Lichsudiemdanh> list = new ArrayList<>();
        sql = "CALL DOCLICHSU(?)";
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        java.sql.Date sqlDate = java.sql.Date.valueOf(df.format(date));
        System.out.println(sqlDate);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDate(1, sqlDate);
            ResultSet rs = ps.executeQuery();
                while(rs.next()) {
                    FDA_Lichsudiemdanh l = new FDA_Lichsudiemdanh();
                    l.setMssv(rs.getString("mssv"));
                    l.setTensv(rs.getString("tensv"));
                    l.setGioitinh(rs.getString("gioitinh"));
                    l.setNgaydiemdanh(rs.getDate("ngaydiemdanh"));
                    l.setThoiGianDiemDanh(rs.getTime("thoigiandiemdanh").toString());
                    list.add(l);
                }
        } catch (SQLException ex) {
            Logger.getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
//        for (int i = 0; i < list.size(); i++) {
//            System.out.print(list.get(i).getMssv());
//            System.out.print(list.get(i).getTensv());
//            System.out.print(list.get(i).getNgaysinhsv());
//            System.out.print(list.get(i).getGioitinh());
//            System.out.print(list.get(i).getNgaydiemdanh());
//        }
//        return null;
    }
    
    public ArrayList<FDA_Lichsudiemdanh> doclichsutheomssv(String ms, Date date) {
        String sql;
        ArrayList<FDA_Lichsudiemdanh> list1 = new ArrayList<>();
        sql = "CALL DOCLICHSUTHEOMSSV(?, ?)";
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        java.sql.Date sqlDate = java.sql.Date.valueOf(df.format(date));
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, ms);
            ps.setDate(2, sqlDate);
            ResultSet rs = ps.executeQuery();
                while(rs.next()) {
                    FDA_Lichsudiemdanh l = new FDA_Lichsudiemdanh();
                    l.setMssv(rs.getString("mssv"));
                    l.setTensv(rs.getString("tensv"));
                    l.setGioitinh(rs.getString("gioitinh"));
                    l.setNgaydiemdanh(rs.getDate("ngaydiemdanh"));
                    l.setThoiGianDiemDanh(rs.getTime("thoigiandiemdanh").toString());
                    list1.add(l);
                }
        } catch (SQLException ex) {
            Logger.getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list1;
    }
    
    public boolean ghilichsu(FDA_Lichsudiemdanh lichsudiemdanh) {      
        String sql;
        sql = "CALL GHILICHSU(?, ?, ?)";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String[] dateAndTime = sdf.format(lichsudiemdanh.getNgaydiemdanh()).split(" ");
        java.sql.Date sqlDate = java.sql.Date.valueOf(dateAndTime[0]);
        java.sql.Time sqlTime = java.sql.Time.valueOf(dateAndTime[1]);
        try {
            PreparedStatement ps;
            ps = conn.prepareCall(sql);
            ps.setString(1, lichsudiemdanh.getMssv());
            ps.setDate(2, sqlDate);
            ps.setTime(3, sqlTime);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            Logger.getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }
    
    
}
