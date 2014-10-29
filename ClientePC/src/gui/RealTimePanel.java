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
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Timer;

/**
 * Componente para ver los datos de triangulación en tiempo real.
 */
public class RealTimePanel extends javax.swing.JComponent {
    private final static boolean DEBUG = false;
    private final static int MeterPixelRate = 33;
    private final static int OffsetX = 22;
    private final static int OffsetY = 22;
    
    private final List<DatosCamara> camData;
    private String currCamId;
    private DatosNino childData;
    
    private boolean showCams;
    private boolean showChild;

    /**
     * Inicializa una nueva instancia a partir de la lista que contendrá
     * las cámaras.
     * 
     * @param camaras Cámaras.
     */
    public RealTimePanel(final List<DatosCamara> camaras) {
        initComponents();
        this.camData = camaras;
        
        // Timer de refresco
        Timer childTimer = new Timer(500, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showChild = !showChild;
                repaint();
            }
        });
        childTimer.start();
    }
    
    /**
     * Establece si se muestran o no las cámaras.
     * 
     * @param value Si se muestran o no las cámaras.
     */
    public void setShowCams(final boolean value) {
        this.showCams = value;
    }
    
    /**
     * Establece el nuevo valor de localización del niño.
     * 
     * @param childData Valor de localización del niño.
     */
    public void setChild(final DatosNino childData) {
        this.childData = childData;
    }
    
    /**
     * Establece el ID de la cámara que enfoca al niño.
     * 
     * @param currCamId ID de la cámara que enfoca al niño.
     */
    public void setCurrentCamaraId(final String currCamId) {
        this.currCamId = currCamId;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Si no hay datos de niño no se conoce ni las dimensiones de la sala.
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
        
        // Pinta las cámara
        if (this.showCams) {
            // Pinta el ángulo de la cámara
            for (DatosCamara cam : this.camData) {
                // Según si es la cámara actual o no se pinta más oscuro.
                if (currCamId != null && cam.getCamId().equals(currCamId))
                    g.setColor(new Color(255, 0, 0, 128));
                else
                    g.setColor(new Color(128, 128, 128, 128));

                // Pinta el ángulo de vision.
                drawVision(g, cam);
            
                // Pinta el punto de la cámara
                g.setColor(Color.blue);
                fillCircle(g, meter2Px(cam.getPosX()), meter2Px(cam.getPosY()));
            }
        }
        
        // Pinta el punto con el niño
        if (this.showChild) {
            g.setColor(Color.red);
            fillCircle(g, meter2Px(this.childData.getPosX()),
                    meter2Px(this.childData.getPosY()));
        }
    }
    
    /**
     * Convierte de metros a píxeles.
     * 
     * @param meters El valor en metros a convertir.
     * @return El equivalente en píxeles.
     */
    private int meter2Px(final double meters) {
        return (int)Math.round(meters * MeterPixelRate);
    }
    
    /**
     * Dibuja el ángulo de visión de una cámara.
     * 
     * @param g Objeto para pintar en el objeto.
     * @param cam Cámara a pintar.
     */
    private void drawVision(Graphics g, DatosCamara cam) {
        final double ANGULO    = Math.toRadians(65.0);  // Ángulo de visión

        // Ángulo de la cámara (en sentido contrario a las agujas del reloj).
        double camAngle = cam.getAngle();
        
        // Dimensiones de la sala
        double width    = this.childData.getSalaW();
        double length   = this.childData.getSalaL();
        
        // Posición de la cámara en píxeles
        int camXpx = this.meter2Px(cam.getPosX());
        int camYpx = this.meter2Px(cam.getPosY());
        
        /* 
         * Obtiene los ángulos entre la lína horizontal donde está la cámara
         * y la línea que une la cámara y cada esquina. De esta forma podemos
         * clasificar los límites del ángulo de visión en cuatro zonas. 
         */ 
        double[] refs = new double[4];
        refs[0] = calculateAngle(cam.getPosX(), cam.getPosY(), width, 0.0);
        refs[1] = calculateAngle(cam.getPosX(), cam.getPosY(), 0.0,   0.0);
        refs[2] = calculateAngle(cam.getPosX(), cam.getPosY(), 0.0,   length);
        refs[3] = calculateAngle(cam.getPosX(), cam.getPosY(), width, length);
        
        if (DEBUG) {
            System.out.printf("Ángulo centrado: %.4f\n", Math.toDegrees(camAngle));
            System.out.printf("Referencias: [%.4f, %.4f, %.4f, %.4f]\n", 
                    Math.toDegrees(refs[0]), Math.toDegrees(refs[1]),
                    Math.toDegrees(refs[2]), Math.toDegrees(refs[3]));
        }
        
        // Calcula el primer límite del ángulo de visión y lo normalizamos.
        double angle1 = camAngle + ANGULO / 2;
        if (angle1 > 2*Math.PI) // Al sumar puede que se pase de 360º
            angle1 -= 2*Math.PI;
        
        // Calculamos su zona y su punto final (aquel que toque con una pared).
        int zone1 = getAngleZone(angle1, refs);
        double[] endPoint1 = this.getEndPointLine(cam.getPosX(), cam.getPosY(),
                angle1, zone1);
        int[] endPoint1px = new int[] { meter2Px(endPoint1[0]), meter2Px(endPoint1[1]) };
        
        // Calcula el segundo límite del ángulo de visión y lo normalizamos.    
        double angle2 = camAngle - ANGULO / 2;
        if (angle2 < 0) // Al restar puede que se haga negativo
            angle2 += 2*Math.PI;
        
        // Calculamos su zona y punto final.
        int zone2 = getAngleZone(angle2, refs);
        double[] endPoint2 = this.getEndPointLine(cam.getPosX(), cam.getPosY(),
                angle2, zone2);
        int[] endPoint2px = new int[] { meter2Px(endPoint2[0]), meter2Px(endPoint2[1]) };
        
        if (DEBUG) {
            System.out.printf("Zonas: [%d, %d]\n", zone1, zone2);
            System.out.printf("Final 1: [%.2f, %.2f]\n", endPoint1[0], endPoint1[1]);
            System.out.printf("Final 2: [%.2f, %.2f]\n", endPoint2[0], endPoint2[1]);
        }

        // Dibujamos ambas líneas
        g.drawLine(camXpx, camYpx, endPoint1px[0], endPoint1px[1]);
        g.drawLine(camXpx, camYpx, endPoint2px[0], endPoint2px[1]);
        
        // Dibujamos el polígono
        if (zone1 == zone2) {
            g.fillPolygon(
                    new int[] { camXpx, endPoint1px[0], endPoint2px[0] }, 
                    new int[] { camYpx, endPoint1px[1], endPoint2px[1] },
                    3);
        } else {
            // En caso de no estar en la misma zona, calculamos la esquina que pasa.
            int esquinaX = (endPoint1[0] == width || endPoint1[0] == 0) ?
                    meter2Px(endPoint1[0]) : meter2Px(endPoint2[0]);
            int esquinaY = (endPoint1[1] == length || endPoint1[1] == 0) ?
                    meter2Px(endPoint1[1]) : meter2Px(endPoint2[1]);
            
            if (DEBUG)
                System.out.printf("Esquina: [%d, %d]\n", esquinaX, esquinaY);
            
            g.fillPolygon(
                    new int[] { camXpx, endPoint1px[0], esquinaX, endPoint2px[0] }, 
                    new int[] { camYpx, endPoint1px[1], esquinaY, endPoint2px[1] },
                    4);
        }
    }
    
    /**
     * Cálcula el ángulo entre la línea horizontal que pasa por P0 y la línea
     * que une P0 con P1
     * 
     * @param x0 Coordenada X de P0.
     * @param y0 Coordenada Y de P0.
     * @param x1 Coordenada X de P1.
     * @param y1 Coordenada Y de P1.
     * @return Ángulo.
     */
    private double calculateAngle(double x0, double y0, double x1, double y1) {
        // Aunque en el sistema de dibujado la Y crezca al descender
        // para estos cálculos lo tomamos como un sistema de referencia normal.
        y0 = -y0;
        y1 = -y1;
        
        // Punto de referencia (el que está en el borde de la sala en horizontal).
        double refX = this.childData.getSalaW();
        double refY = y0;
        
        // Vector 0, el que está en la línea horizontal.
        double vecX0 = refX - x0;
        double vecY0 = refY - y0;
        
        // Vector 1, el que va de un punto a otro
        double vecX1 = x1 - x0;
        double vecY1 = y1 - y0;
        
        // Producto escalar entre vectores.
        double num = vecX0 * vecX1 + vecY0 * vecY1;
        double dem = Math.sqrt((vecX0*vecX0+vecY0*vecY0) * (vecX1*vecX1+vecY1*vecY1));
        double angle = Math.acos(num / dem);
        
        // Como el producto escalar devuelve siempre el menor ángulo, en caso de
        // que uno de los vectores esté en el tercer o cuarto cuadrante, significa
        // que queremos el otro ángulo más grande.
        if (vecY1 < 0 || (vecY1 == 0 && vecX1 < 0))
            angle = 2 * Math.PI - angle;
        
        if (DEBUG) {
            System.out.printf("v0 = (%.4f, %.4f)\n", vecX0, vecY0);
            System.out.printf("v1 = (%.4f, %.4f)\n", vecX1, vecY1);
            System.out.printf("Ángulo: %.4f\n", Math.toDegrees(angle));
        }

        return angle;
    }
    
    /**
     * Calcula la zona en la que se situa el ángulo a partir de los ángulos
     * de referencia.
     * 
     * @param angle Ángulo a comprobar.
     * @param refs Ángulos de referencia.
     * @return Zona.
     */
    private int getAngleZone(double angle, double[] refs) {
        if ((angle >= 0 && angle <= refs[0]) || (angle >= refs[3] && angle <= 2*Math.PI))
            return 1;
        else if (angle <= refs[2] && angle >= refs[1])
            return 3;
        else if (angle >= refs[0] && angle <= refs[1])
            return 2;
        else
            return 4;
    }
    
    /**
     * Calcula el otro extremo del ángulo de visión a partir de la zona en la
     * que esté.
     * 
     * @param x0 Coordenada X de la posición de inicio.
     * @param y0 Coordenada Y de la posición de inicio.
     * @param angle Límite del ángulo de visión.
     * @param zone Zona en la que se encuentra.
     * @return Coordenadas del otro exremo.
     */
    private double[] getEndPointLine(double x0, double y0, double angle, int zone) {
        double x1, y1;

        switch (zone) {
            case 1:
                x1 = this.childData.getSalaW();
                y1 = y0 + (-Math.tan(angle)) * (x1 - x0);
                break;
                
            case 2:
                y1 = 0;
                x1 = x0 + (y1 - y0) / -Math.tan(angle);
                break;
                
            case 3:
                x1 = 0;
                y1 = y0 + (-Math.tan(angle)) * (x1 - x0);
                break;
                
            case 4:
            default:
                y1 = this.childData.getSalaL();
                x1 = x0 + (y1 - y0) / -Math.tan(angle);
                break;
        }

        return new double[] { x1, y1 };
    }
    
    /**
     * Pinta un círculo en una posición.
     * 
     * @param g Objeto para pintar.
     * @param posX Coordenada X de la posición del círculo.
     * @param posY Coordenada Y de la posición del círculo.
     */
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
