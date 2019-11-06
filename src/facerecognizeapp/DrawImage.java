/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package facerecognizeapp;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 *
 * @author macbook
 */
public class DrawImage extends JPanel{
    private Image image;
    
    @Override
    public void paintComponent(Graphics g){
        Dimension d = getSize();
        g.setColor(Color.GRAY);
        g.drawRect(0, 0, this.getWidth(), this.getHeight());
        if(image != null) {
            Graphics2D g2d = (Graphics2D) g;
            g.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), this);
        }
    }

    public void setImage(Image image) {
        this.image = image;
    }
    
    public void drawImage(Image image) {
        setImage(image);
        this.repaint();
    }
    
    public void clear() {
        image = null;
        this.repaint();
    }
}
