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

import control.LectorNino;
import es.prometheus.dds.TopicoControl;
import es.prometheus.dds.TopicoControlDinamico;
import es.prometheus.dds.TopicoControlFijo;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Toolkit;

/**
 * Ventana principal del programa.
 */
public class MainWindow extends javax.swing.JFrame {
    private boolean stop;
    
    //private String ninoKeys;
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
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(InicioSesion.class.getResource("icon.png")));
        this.panelVideo.setLayout(new GridLayout(1, 1));
        
        // Inicia GStreamer
        org.gstreamer.Gst.init();
    }

    /**
     * Crea una nueva ventana que participa en un dominio.
     * 
     * @param ninoKey Claves para discernir los datos en el tópico de los niños.
     */
    public MainWindow(final String ninoKey) {
        this();
        
        //this.ninoKeys    = ninoKey;
        this.controlNino = new TopicoControlDinamico("ParticipantesPC::ParticipanteVideo",
                "ChildDataTopic");
        this.controlCamaras = new TopicoControlFijo("ParticipantesPC::ParticipanteVideo",
                "SuscriptorVideo", null);
        
        this.susNino = new LectorNino(controlNino, ninoKey, controlCamaras);
        this.susNino.reanudar();
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        toolbar = new javax.swing.JToolBar();
        lblStatus = new javax.swing.JLabel();
        panelCam1 = new javax.swing.JPanel();
        lblCam1Child = new javax.swing.JLabel();
        comboCam1 = new javax.swing.JComboBox();
        btnCam1 = new javax.swing.JToggleButton();
        lblCam1Tag = new javax.swing.JLabel();
        lblCam1Place = new javax.swing.JLabel();
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

        panelCam1.setBackground(new java.awt.Color(255, 255, 255));
        panelCam1.setBorder(javax.swing.BorderFactory.createTitledBorder("Cámara 1"));

        lblCam1Child.setText("Niño:");

        comboCam1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        comboCam1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Benito Palacios Sánchez", "Pleonex Pleonizando" }));
        comboCam1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateCams(evt);
            }
        });

        btnCam1.setSelected(true);
        btnCam1.setText("Activar cámara");
        btnCam1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateCams(evt);
            }
        });

        lblCam1Tag.setText("Sala:");

        lblCam1Place.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        lblCam1Place.setText("Patio de recreo");

        javax.swing.GroupLayout panelCam1Layout = new javax.swing.GroupLayout(panelCam1);
        panelCam1.setLayout(panelCam1Layout);
        panelCam1Layout.setHorizontalGroup(
            panelCam1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(comboCam1, 0, 0, Short.MAX_VALUE)
            .addComponent(btnCam1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panelCam1Layout.createSequentialGroup()
                .addGroup(panelCam1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblCam1Child)
                    .addGroup(panelCam1Layout.createSequentialGroup()
                        .addComponent(lblCam1Tag)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblCam1Place)))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        panelCam1Layout.setVerticalGroup(
            panelCam1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCam1Layout.createSequentialGroup()
                .addComponent(lblCam1Child)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(comboCam1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCam1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCam1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCam1Tag)
                    .addComponent(lblCam1Place))
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
                .addComponent(panelCam1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(toolbar, javax.swing.GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelCam1, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelVideo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0)
                .addComponent(toolbar, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Actualiza el número de cámaras que se están viendo añadiendo o quitando.
     * 
     * @param evt Evento que lo dispara.
     */
    private void updateCams(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateCams
        // Si estamos modificando los datos de los controles (añdiendo elementos
        // a los combox) no queremos que este evento se dispare.
        if (this.stop)
            return;

        // Elimina la vista antigua
        this.panelVideo.removeAll();
        
        // Si el botón está desactivado, borrar la pantalla.
        if (!this.btnCam1.isSelected()) {
            this.susNino.parar();
            return;
        }
        
        // Cambia a la nueva
        this.susNino.reanudar();
        this.panelVideo.add(this.susNino.getSuscriptorCamara().getVideoComponent());
        
        // Actualiza
        this.panelVideo.validate();
    }//GEN-LAST:event_updateCams
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton btnCam1;
    private javax.swing.JComboBox comboCam1;
    private javax.swing.JLabel lblCam1Child;
    private javax.swing.JLabel lblCam1Place;
    private javax.swing.JLabel lblCam1Tag;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JMenu menuOpt;
    private javax.swing.JMenuBar menubar;
    private javax.swing.JPanel panelCam1;
    private javax.swing.JPanel panelVideo;
    private javax.swing.JToolBar toolbar;
    // End of variables declaration//GEN-END:variables
}
