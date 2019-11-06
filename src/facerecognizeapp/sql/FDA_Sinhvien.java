/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package facerecognizeapp.sql;

import java.util.Date;

/**
 *
 * @author macbook
 */
public class FDA_Sinhvien {
    private String mssv;
    private String tensv;
    private Date ngaysinhsv;
    private String gioitinh;
    private String nganh;
    private String khoas;
    private String khoa;
    
    public FDA_Sinhvien() {

    }

    public FDA_Sinhvien(String mssv) {
        this.mssv = mssv;
    }
    
    public FDA_Sinhvien(String mssv, String tensv, Date ngaysinhsv, String gioitinh, String nganh, String khoas, String khoa) {
        this.mssv = mssv;
        this.tensv = tensv;
        this.ngaysinhsv = ngaysinhsv;
        this.gioitinh = gioitinh;
        this.nganh = nganh;
        this.khoas = khoas;
        this.khoa = khoa;
    }
    

    public String getMssv() {
        return mssv;
    }

    public void setMssv(String mssv) {
        this.mssv = mssv;
    }

    public String getTensv() {
        return tensv;
    }

    public void setTensv(String tensv) {
        this.tensv = tensv;
    }

    public Date getNgaysinhsv() {
        return ngaysinhsv;
    }

    public void setNgaysinhsv(Date ngaysinhsv) {
        this.ngaysinhsv = ngaysinhsv;
    }

    public String getNganh() {
        return nganh;
    }

    public void setNganh(String nganh) {
        this.nganh = nganh;
    }

    public String getKhoas() {
        return khoas;
    }

    public void setKhoas(String khoas) {
        this.khoas = khoas;
    }

    public String getKhoa() {
        return khoa;
    }

    public void setKhoa(String khoa) {
        this.khoa = khoa;
    }

    public String getGioitinh() {
        return gioitinh;
    }

    public void setGioitinh(String gioitinh) {
        this.gioitinh = gioitinh;
    }
    
    
    
}
