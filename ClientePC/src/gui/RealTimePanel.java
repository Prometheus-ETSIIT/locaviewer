/*
 * Copyright (C) 2014 Prometheus
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package gui;

import control.DatosCamara;
import control.DatosNino;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Timer;

/**
 *
 */
public class RealTimePanel extends javax.swing.JComponent {
    private final static int MeterPixelRate = 22;
    private final static int OffsetX = 22;
    private final static int OffsetY = 22;
    
    private DatosCamara camData;
    private DatosNino childData;
    
    private boolean showCams;
    private boolean showChild;
    
    public RealTimePanel() {
        initComponents();
        Timer childTimer = new Timer(500, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showChild = !showChild;
                repaint();
            }
        });
        childTimer.start();
    }
    
    public void setShowCams(final boolean value) {
        this.showCams = value;
        this.repaint();
    }
    
    public void setChild(final DatosNino childData) {
        this.childData = childData;
        
        // Si el niño se mueve, puede que la cámara no tenga sentido.
        if (this.camData != null && 
                !this.childData.getCamId().equals(this.camData.getCamId()))
            this.camData = null;
    }
    
    public void setCamera(final DatosCamara camData) {
        this.camData = camData;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (this.childData == null)
            return;
        
        // Tamaño de la habitación en píxeles.
        int widPx = this.meter2Px(this.childData.getSalaW());
        int lenPx = this.meter2Px(this.childData.getSalaL());
        
        // Traslada para que se vea todo
        g.translate(OffsetX, OffsetY);
        
        // Pinta el fondo
        g.setColor(Color.white);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        
        // Pinta la habitación
        g.setColor(Color.blue);
        g.drawRect(0, 0, widPx, lenPx);
        
        // Pinta la cámara
        if (this.showCams && this.camData != null) {
            // Pinta el ángulo de la cámara
            g.setColor(new Color(255, 0, 0, 128));
            drawVision(g, meter2Px(this.camData.getPosX()),
                    lenPx - meter2Px(this.camData.getPosY()));
            
            // Pinta el punto de la cámara
            g.setColor(Color.blue);
            fillCircle(g, meter2Px(this.camData.getPosX()),
                    lenPx - meter2Px(this.camData.getPosY()));
        }
        
        // Pinta el punto con el niño
        if (this.showChild) {
            g.setColor(Color.red);
            fillCircle(g, meter2Px(this.childData.getPosX()),
                    lenPx - meter2Px(this.childData.getPosY()));
        }
    }
    
    private int meter2Px(final double meters) {
        return (int)Math.round(meters * MeterPixelRate);
    }
    
    private void drawVision(Graphics g, int camX, int camY) {
        final int ANGULO = 52;
        final double TAN_MEDIOS = 0.488;
                
        int roomWidth  = meter2Px(this.childData.getSalaW());
        int roomLength = meter2Px(this.childData.getSalaL());
        boolean vertical = (camX == 0 || camX == roomWidth);
        if (vertical) {
            int x1 = (camX == 0) ? roomWidth : 0;
            int y11 = (int)(camY + TAN_MEDIOS * (x1 - camX));
            g.drawLine(camX, camY, x1, y11);
            
            int y12 = (int)(camY - TAN_MEDIOS * (x1 - camX));
            g.drawLine(camX, camY, x1, y12);
            
            g.fillPolygon(new int[] { camX, x1, x1 }, new int[] { camY, y11, y12 }, 3);
        } else {
            int y1 = (camY == 0) ? roomLength : 0;
            int x11 = (int)(camX + TAN_MEDIOS * (y1 - camY));
            g.drawLine(camX, camY, x11, y1);
            
            int x12 = (int)(camX - TAN_MEDIOS * (y1 - camY));
            g.drawLine(camX, camY, x12, y1);
            
            g.fillPolygon(new int[] { camX, x11, x12 }, new int[] { camY, y1, y1 }, 3);    
        }
    }
    
    private void fillCircle(Graphics g, int posX, int posY) {
        final int SIZE  = 10;
        int radio = SIZE / 2;
        
        g.fillOval(posX - radio, posY - radio, SIZE, SIZE);        
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(null);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
