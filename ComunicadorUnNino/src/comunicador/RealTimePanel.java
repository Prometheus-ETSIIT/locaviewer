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

package comunicador;

import comunicador.CamaraPos;
import comunicador.Dato;
import java.awt.Color;
import java.awt.Graphics;
import java.util.List;

/**
 *
 */
public class RealTimePanel extends javax.swing.JPanel {

    private final static String ScriptPath = "../../Localizacion/detectarcamara.m";
    private final static String FuncName   = "detectarcamara";
    private final static int MeterPixelRate = 50;
    private final static int OffsetX = 10;
    private final static int OffsetY = 10;
    
    private TriangulacionOctave octave;
    private int width;
    private int length;
    private List<Dato> lastSensors;
    
    private boolean showCams;
    private boolean showSensors;
    private boolean showBestCam;
    
    public RealTimePanel() {
        initComponents();
    }
    
    public void setShowCams(final boolean value) {
        this.showCams = value;
        this.repaint();
    }
    
    public void setShowSensors(final boolean value) {
        this.showSensors = value;
        this.repaint();
    }
    
    public void setShowBestCam(final boolean value) {
        this.showBestCam = value;
        this.repaint();
    }
    
    public void initialize(final TriangulacionOctave triangulacion) {
        this.width  = triangulacion.getWidth();
        this.length = triangulacion.getLength();
        this.octave = triangulacion;
    }

    public void close() {
        if (this.octave.isAlive())
            this.octave.close();
    }
    
    public void setNewSensors(final List<Dato> sensors) {
        this.lastSensors = sensors;
        this.repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (this.octave == null)
            return;
        
        // Pinta el fondo
        g.setColor(Color.white);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        
        // Pinta la habitación
        g.setColor(Color.red);
        g.drawRect(0, 0, meter2Px(this.width), meter2Px(this.length));
        
        // Pinta los ángulos de las cámaras
        if (this.showCams) {
            g.setColor(new Color(128, 128, 128, 128));
            for (CamaraPos cam : this.octave.getCamaras())
                drawVision(g, meter2Px(cam.getPosX()), meter2Px(cam.getPosY()));
        }
        
        // Pinta los puntos de las cámaras
        g.setColor(Color.blue);
        for (CamaraPos cam : this.octave.getCamaras())
            g.fillOval(meter2Px(cam.getPosX()), meter2Px(cam.getPosY()), 10, 10);
        
        if (this.showSensors && this.lastSensors != null) {
            g.setColor(Color.green);
            for (Dato sensor : this.lastSensors) {
                int sensorX = meter2Px(sensor.getPosicionSensor().getPrimero());
                int sensorY = meter2Px(sensor.getPosicionSensor().getSegundo());
                g.fillOval(sensorX, sensorY, 10, 10);
            }
        }
    }
    
    private int meter2Px(final int meters) {
        return meters * MeterPixelRate;
    }
    
    private void drawVision(Graphics g, int camX, int camY) {
        final int ANGULO = 52;
        final double TAN_MEDIOS = 0.488;
                
        int roomWidth  = meter2Px(this.width);
        int roomLength = meter2Px(this.length);
        boolean vertical = (camX == 0 || camX == roomWidth);
        if (vertical) {
            camY += 5;
            int x1 = (camX == 0) ? roomWidth : 0;
            int y11 = (int)(camY + TAN_MEDIOS * (x1 - camX));
            g.drawLine(camX, camY, x1, y11);
            
            int y12 = (int)(camY - TAN_MEDIOS * (x1 - camX));
            g.drawLine(camX, camY, x1, y12);
            
            g.fillPolygon(new int[] { camX, x1, x1 }, new int[] { camY, y11, y12 }, 3);
        } else {
            camX += 5;
            int y1 = (camY == 0) ? roomLength : 0;
            int x11 = (int)(camX + TAN_MEDIOS * (y1 - camY));
            g.drawLine(camX, camY, x11, y1);
            
            int x12 = (int)(camX - TAN_MEDIOS * (y1 - camY));
            g.drawLine(camX, camY, x12, y1);
            
            g.fillPolygon(new int[] { camX, x11, x12 }, new int[] { camY, y1, y1 }, 3);    
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
