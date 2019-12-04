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
public class FDA_Lichsudiemdanh {
    private String mssv;
    private String tensv;
    private String gioitinh;
    private Date ngaydiemdanh;
    private String thoiGianDiemDanh;

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

    public String getGioitinh() {
        return gioitinh;
    }

    public void setGioitinh(String gioitinh) {
        this.gioitinh = gioitinh;
    }

    public String getThoiGianDiemDanh() {
        return thoiGianDiemDanh;
    }

    public void setThoiGianDiemDanh(String thoiGianDiemDanh) {
        this.thoiGianDiemDanh = thoiGianDiemDanh;
    }

    

    public Date getNgaydiemdanh() {
        return ngaydiemdanh;
    }

    public void setNgaydiemdanh(Date ngaydiemdanh) {
        this.ngaydiemdanh = ngaydiemdanh;
    }
    
    
}
