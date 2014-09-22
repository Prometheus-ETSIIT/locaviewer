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

import control.DatosNino;
import control.LectorNino;
import es.prometheus.dds.TopicoControl;
import es.prometheus.dds.TopicoControlDinamico;
import es.prometheus.dds.TopicoControlFijo;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Ventana principal del programa.
 */
public class MainWindow extends javax.swing.JFrame {
    private boolean stop;
    private String[] ninoKeys;
    private LectorNino susNino;
    private TopicoControl controlNino;
    private TopicoControl controlCamaras;

    /**
     * Crea una nueva ventana sin funcionalidad.
     * Sólo para diseñador.
     */
    public MainWindow() {
        initComponents();
        
        this.setBackground(Color.white);
        this.getContentPane().setBackground(Color.white);
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(MainWindow.class.getResource("icon.png")));
        this.panelVideo.setLayout(new GridLayout(1, 1));
        
        // Inicia GStreamer
        org.gstreamer.Gst.init();
    }

    /**
     * Crea una nueva ventana que participa en un dominio.
     * 
     * @param ninoKeys Claves para discernir los datos en el tópico de los niños.
     */
    public MainWindow(final String[] ninoKeys) {
        this();
        
        this.controlNino = new TopicoControlDinamico("ParticipantesPC::ParticipanteVideo",
                "ChildDataTopic");
        this.controlCamaras = new TopicoControlFijo("ParticipantesPC::ParticipanteVideo",
                "SuscriptorVideo", null);
        
        this.susNino = new LectorNino(controlNino, ninoKeys[0], controlCamaras);
        this.susNino.setExtraListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                onNinoDataReceived(susNino.getUltimoDato());
            }
        });
        
        // Establece las posibles claves
        // TODO: Que aperazca el apodo en lugar del ID
        // Por ejemplo, no activando filtros momentáneamente y poniendo el listener extra
        this.ninoKeys = ninoKeys;
        this.stop = true;
        this.comboNino.removeAllItems();
        for (String k : ninoKeys)
            this.comboNino.addItem(k);
        this.stop = false;
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        toolbar = new javax.swing.JToolBar();
        lblStatus = new javax.swing.JLabel();
        panelCam = new javax.swing.JPanel();
        lblChild = new javax.swing.JLabel();
        comboNino = new javax.swing.JComboBox();
        btnCam = new javax.swing.JToggleButton();
        lblTag = new javax.swing.JLabel();
        lblPlace = new javax.swing.JLabel();
        panelVideo = new javax.swing.JPanel();
        menubar = new javax.swing.JMenuBar();
        menuOpt = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("locaviewer Beta ~ by Prometheus");
        setMinimumSize(new java.awt.Dimension(800, 583));
        setPreferredSize(new java.awt.Dimension(800, 583));

        toolbar.setBackground(new java.awt.Color(176, 206, 230));
        toolbar.setFloatable(false);

        lblStatus.setFont(new java.awt.Font("Courier New", 0, 11)); // NOI18N
        lblStatus.setText("Transmitiendo vídeo. . .");
        toolbar.add(lblStatus);

        panelCam.setBackground(new java.awt.Color(255, 255, 255));
        panelCam.setBorder(javax.swing.BorderFactory.createTitledBorder("Cámara Info"));

        lblChild.setText("Niño:");

        comboNino.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        comboNino.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Benito Palacios Sánchez", "Pleonex Pleonizando" }));
        comboNino.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboNinoSelected(evt);
            }
        });

        btnCam.setText("Activar cámara");
        btnCam.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCamClick(evt);
            }
        });

        lblTag.setText("Sala:");

        lblPlace.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        lblPlace.setText("Patio de recreo");

        javax.swing.GroupLayout panelCamLayout = new javax.swing.GroupLayout(panelCam);
        panelCam.setLayout(panelCamLayout);
        panelCamLayout.setHorizontalGroup(
            panelCamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(comboNino, 0, 0, Short.MAX_VALUE)
            .addComponent(btnCam, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panelCamLayout.createSequentialGroup()
                .addGroup(panelCamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblChild)
                    .addGroup(panelCamLayout.createSequentialGroup()
                        .addComponent(lblTag)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblPlace)))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        panelCamLayout.setVerticalGroup(
            panelCamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCamLayout.createSequentialGroup()
                .addComponent(lblChild)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(comboNino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCam)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTag)
                    .addComponent(lblPlace))
                .addContainerGap())
        );

        panelVideo.setBackground(new java.awt.Color(0, 0, 0));
        panelVideo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        panelVideo.setMinimumSize(new java.awt.Dimension(640, 480));
        panelVideo.setPreferredSize(new java.awt.Dimension(640, 480));

        javax.swing.GroupLayout panelVideoLayout = new javax.swing.GroupLayout(panelVideo);
        panelVideo.setLayout(panelVideoLayout);
        panelVideoLayout.setHorizontalGroup(
            panelVideoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        panelVideoLayout.setVerticalGroup(
            panelVideoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 478, Short.MAX_VALUE)
        );

        menubar.setBackground(new java.awt.Color(176, 206, 230));
        menubar.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 51, 255)));
        menubar.setBorderPainted(false);
        menubar.setPreferredSize(new java.awt.Dimension(78, 50));

        menuOpt.setBorder(null);
        menuOpt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/Settings.png"))); // NOI18N
        menuOpt.setText("Opciones");
        menuOpt.setToolTipText("Abre las opciones.");
        menuOpt.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        menubar.add(menuOpt);

        setJMenuBar(menubar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panelVideo, javax.swing.GroupLayout.DEFAULT_SIZE, 644, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelCam, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(toolbar, javax.swing.GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelCam, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelVideo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0)
                .addComponent(toolbar, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void onNinoDataReceived(DatosNino data) {
        this.lblPlace.setText(data.getSala());
    }
    
    private void btnCamClick(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCamClick
        // Elimina la vista antigua
        this.panelVideo.removeAll();
        
        // Si el botón está desactivado, borrar la pantalla.
        if (!this.btnCam.isSelected()) {
            this.susNino.parar();
            this.panelVideo.revalidate();
            this.panelVideo.repaint();
            return;
        }
        
        // Cambia a la nueva
        this.susNino.reanudar();
        this.panelVideo.add(this.susNino.getSuscriptorCamara().getVideoComponent());
        
        // Actualiza
        this.panelVideo.revalidate();
    }//GEN-LAST:event_btnCamClick

    private void comboNinoSelected(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboNinoSelected
        if (this.stop)
            return;

        // Cambia la clave en el lector de niños
        this.susNino.cambiarNinoId(this.ninoKeys[this.comboNino.getSelectedIndex()]);
    }//GEN-LAST:event_comboNinoSelected
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton btnCam;
    private javax.swing.JComboBox comboNino;
    private javax.swing.JLabel lblChild;
    private javax.swing.JLabel lblPlace;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblTag;
    private javax.swing.JMenu menuOpt;
    private javax.swing.JMenuBar menubar;
    private javax.swing.JPanel panelCam;
    private javax.swing.JPanel panelVideo;
    private javax.swing.JToolBar toolbar;
    // End of variables declaration//GEN-END:variables
}
