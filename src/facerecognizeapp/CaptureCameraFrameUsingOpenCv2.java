/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package facerecognizeapp;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

/**
 *
 * @author Acer
 */
public class CaptureCameraFrameUsingOpenCv2 {
    
    private final VideoCapture camera;
    
    
    public CaptureCameraFrameUsingOpenCv2(int deviceId) {
        System.loadLibrary("opencv_java411");
        camera = new VideoCapture(deviceId);
        if(!camera.isOpened()){
            System.out.println("Camera Error");
        }
        else{
            System.out.println("Camera OK?");
        }
        
    }
    
    public void releaseCamera() {
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
    
    
    
}
