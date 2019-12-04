/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package facerecognizeapp;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Acer
 */
public class Utilities {
    
    private static final String STUDENT_CODE_REGEX = "B1[0-9]{6}";
    
    
    public static boolean isStudentCodeValid(String studentCode) {
        return studentCode.matches(STUDENT_CODE_REGEX);
    }
    
    public static void main(String[] args){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(sdf.format(new Date()));
    }
    
}
