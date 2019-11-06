/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package facerecognizeapp;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Base64;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JTextField;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;

/**
 *
 * @author Acer
 */
public class ServerResponseHandler implements Runnable{
    static byte[] inputData;
    static Socket socket;
    static PrintWriter printWriter;
    static BufferedReader reader;
    static BufferedWriter writer;
    static final String DEFAULT_IMAGE_PATH = "C:\\Users\\Acer\\Desktop\\images_for_test\\test\\test.jpg";
    private List<Mat> frameList;
    private JTextField textField;
    
    
    public JTextField getTextField() {
        return textField;
    }

    public void setTextField(JTextField textField) {
        this.textField = textField;
    }

    public void setFrameList(List<Mat> frameList) {
        this.frameList = frameList;
    }
    
        
    public ServerResponseHandler(Socket socket) {
        try {
            this.socket = socket;
            printWriter = new PrintWriter(socket.getOutputStream());
            InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
            reader = new BufferedReader(streamReader);
            writer = new BufferedWriter(printWriter);
        }catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Can't connect to sever");
            return;
        }
        inputData = new byte[1024*1024];
    }

    public boolean sentFrameToServer(Mat frame) throws IOException {
        BufferedImage bufferedImage;
        String encodedfile;
        bufferedImage = (BufferedImage)HighGui.toBufferedImage(frame);
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpg", b);
        inputData = b.toByteArray();
        System.out.println(inputData.length);
        encodedfile = new String(Base64.getEncoder().encode(inputData), "UTF-8");
        writer.write(encodedfile);
        writer.flush();
        socket.getOutputStream().write(new byte[]{-1});
        return true;
    }

    public boolean sentListOfFrame(List<Mat> frameList) throws IOException {
        for(Mat frame : frameList) {
            if(!sentFrameToServer(frame)) {
                return false;
            }
        }
        socket.getOutputStream().write(new byte[]{-2});
        return true;
    }

    public boolean saveImageToFile(byte[] imageData, String imagePath) throws IOException {
        File f;
        BufferedOutputStream bos;
        f = new File(imagePath);
        if(!f.exists()) {
            f.createNewFile();
        }
        bos = new BufferedOutputStream(new FileOutputStream(f));    
        bos.write(imageData);
        bos.flush();
        bos.close();
        return true;
    }

    public boolean saveImageToFile(byte[] imageData) throws IOException {
        return saveImageToFile(imageData, DEFAULT_IMAGE_PATH);
    }

    public void run() {
        BufferedImage bufferedImage;
        String encodedFile;
        String signal;
        byte[] imageData;
        try {
            while((signal = reader.readLine()) != null) {
                System.out.println("Receive signal: " + signal);
                if(signal.equalsIgnoreCase("success")) {
                    System.out.println("test");
                    while((signal = reader.readLine()) != null) {
                        if(signal.equalsIgnoreCase("image")) {
                            String temp;
                            encodedFile = "";
                            while((temp = reader.readLine()) != null) {
                                if(temp.equalsIgnoreCase("OK")) {
                                    break;
                                }
                                if(temp.length() > 0) {
                                    encodedFile += temp;
                                }
                            }
                        imageData = Base64.getDecoder().decode(encodedFile);
                        saveImageToFile(imageData);
                        } else if(signal.equalsIgnoreCase("info")) {
                            String temp;
                            while((temp = reader.readLine()) != null) {
                                if(temp.equalsIgnoreCase("OK")) {
                                    break;
                                }
                                System.out.println(temp);
                                textField.setText(temp);
                            }
                            if(temp.equalsIgnoreCase("OK")) {
                                Thread.sleep(2000);
                                break;
                            }
                        } 
                    }
                }
//                if(signal.equalsIgnoreCase("image")) {
//                    String temp;
//                    encodedFile = "";
//                    while((temp = reader.readLine()) != null) {
//                        if(temp.equalsIgnoreCase("OK")) {
//                            break;
//                        }
//                        if(temp.length() > 0) {
//                            encodedFile += temp;
//                        }
//                    }
//                    imageData = Base64.getDecoder().decode(encodedFile);
//                    saveImageToFile(imageData);
//                } else if(signal.equalsIgnoreCase("info")) {
//                    String temp;
//                    while((temp = reader.readLine()) != null) {
//                        if(temp.equalsIgnoreCase("OK")) {
//                            break;
//                        }
//                        textField.setText(temp);
//                    }
//                    Thread.sleep(10000);
//                } 

                while(frameList.size() < 10) {
                    Thread.sleep(1000);
                }
                System.out.println("abcd");
                if(frameList.size() == 10) {
                    if(sentListOfFrame(frameList)) {
                        frameList.clear();
                    }else {
                        return;
                    }
                }
                System.out.println("Send Frame");
                System.out.println("End");
                Thread.sleep(50); 
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        } finally {
            printWriter.close();
        }
   }
       
}
