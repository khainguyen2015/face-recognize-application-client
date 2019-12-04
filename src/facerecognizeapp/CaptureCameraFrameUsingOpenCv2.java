/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package facerecognizeapp;

import java.awt.Image;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.videoio.VideoCapture;

/**
 *
 * @author Acer
 */
public class CaptureCameraFrameUsingOpenCv2 {
    
    private VideoCapture camera;
    
    
    public CaptureCameraFrameUsingOpenCv2() {
        System.loadLibrary("opencv_java411");
        camera = new VideoCapture();
    }
    
    public boolean openCamera(int deviceId) {
        return camera.open(deviceId);
    }
    
    public void releaseCamera() {
        if(!camera.isOpened()) {
            return;
        }
        if(camera != null) {
            camera.release();
        }
    }
    
    public Mat captureFrame () {
        Mat frame = new Mat();
//        for(int i = 0; i < 5; i++) {
//            camera.read(frame);
//        }
        camera.read(frame);
        return frame;
    }
    
    public Image captureImage() {
        Image image;
        image = HighGui.toBufferedImage(captureFrame());
        return image;
    }
    
    
    
}
