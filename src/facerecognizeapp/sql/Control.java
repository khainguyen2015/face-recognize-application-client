/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package facerecognizeapp.sql;

import facerecognizeapp.ConnectSQL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author macbook
 */
public class Control {

    private final Connection conn;
    public Control() throws SQLException  {
        ConnectSQL connectSQL = new ConnectSQL("jdbc:mysql://localhost:3306/facedetectapplication"
                                               ,"khai"
                                               ,"123456");
        conn = connectSQL.getConnection();
        System.out.println("Ket noi CSDL thanh cong !!!");
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
                    l.setNgaysinhsv(rs.getDate("ngaysinhsv"));
                    l.setGioitinh(rs.getString("gioitinh"));
                    l.setNgaydiemdanh(rs.getDate("ngaydiemdanh"));
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
    
    public ArrayList<FDA_Lichsudiemdanh> doclichsutheomssv(String ms) {
        String sql;
        ArrayList<FDA_Lichsudiemdanh> list1 = new ArrayList<>();
        sql = "CALL DOCLICHSUTHEOMSSV(?)";
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, ms);
            ResultSet rs = ps.executeQuery();
                while(rs.next()) {
                    FDA_Lichsudiemdanh l = new FDA_Lichsudiemdanh();
                    l.setMssv(rs.getString("mssv"));
                    l.setTensv(rs.getString("tensv"));
                    l.setNgaysinhsv(rs.getDate("ngaysinhsv"));
                    l.setGioitinh(rs.getString("gioitinh"));
                    l.setNgaydiemdanh(rs.getDate("ngaydiemdanh"));
                    list1.add(l);
                }
        } catch (SQLException ex) {
            Logger.getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list1;
    }
    
    public boolean ghilichsu(FDA_Lichsudiemdanh lichsudiemdanh) {      
        String sql;
        sql = "CALL GHILICHSU(?,?)";
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        java.sql.Date sqlDate = java.sql.Date.valueOf(df.format(lichsudiemdanh.getNgaydiemdanh()));
        try {
            PreparedStatement ps;
            ps = conn.prepareCall(sql);
            ps.setString(1, lichsudiemdanh.getMssv());
            ps.setDate(2, sqlDate);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            Logger.getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }
}
