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
import control.LectorNino;
import es.prometheus.dds.TopicoControl;
import es.prometheus.dds.TopicoControlFactoria;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.List;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * Ventana principal del programa.
 */
public class MainWindow extends javax.swing.JFrame {
    private boolean stop;
    
    private String[] ninoId;
    private LectorNino lectorNino;
    private TopicoControl controlNino;
    private TopicoControl controlCamaras;
    private List<String> camIds;

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
     * @param childrenId ID de los niños a seguir.
     */
    public MainWindow(final String[] childrenId) {
        this();
        this.ninoId = childrenId;
        
        this.controlNino = TopicoControlFactoria.crearControlDinamico(
                "MisParticipantes::ParticipantePC",
                "ChildDataTopic");
        this.controlCamaras = TopicoControlFactoria.crearControlDinamico(
                "MisParticipantes::ParticipantePC",
                "VideoDataTopic");
        
        this.lectorNino = new LectorNino(controlNino, childrenId[0], controlCamaras);
        this.lectorNino.setExtraListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                onNinoDataReceived(lectorNino.getUltimoDato());
            }
        });
        this.lectorNino.suspender();
        this.lectorNino.iniciar();
        
        this.camIds = new ArrayList<>();
        this.comboIdCam.removeAllItems();
        
        // Establece las posibles claves
        // TODO: Que aperazca el apodo en lugar del ID
        this.stop = true;
        this.comboNino.removeAllItems();
        for (String k : childrenId)
            this.comboNino.addItem(k);
        this.stop = false;
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        toolbar = new javax.swing.JToolBar();
        lblStatus = new javax.swing.JLabel();
        panelAuto = new javax.swing.JPanel();
        lblChild = new javax.swing.JLabel();
        comboNino = new javax.swing.JComboBox();
        btnCam = new javax.swing.JToggleButton();
        lblViewing = new javax.swing.JLabel();
        lblPlace = new javax.swing.JLabel();
        panelVideo = new javax.swing.JPanel();
        panelManual = new javax.swing.JPanel();
        checkManual = new javax.swing.JCheckBox();
        lblIdCam = new javax.swing.JLabel();
        comboIdCam = new javax.swing.JComboBox();
        panelLoc = new javax.swing.JPanel();
        menubar = new javax.swing.JMenuBar();
        menuOpt = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("locaviewer Beta ~ by Prometheus");
        setMinimumSize(new java.awt.Dimension(540, 500));
        setPreferredSize(new java.awt.Dimension(540, 500));
        setResizable(false);

        toolbar.setBackground(new java.awt.Color(176, 206, 230));
        toolbar.setFloatable(false);

        lblStatus.setFont(new java.awt.Font("Courier New", 0, 11)); // NOI18N
        lblStatus.setText("Iniciando...");
        toolbar.add(lblStatus);

        panelAuto.setBackground(new java.awt.Color(255, 255, 255));
        panelAuto.setBorder(javax.swing.BorderFactory.createTitledBorder("Modo automático"));

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

        lblViewing.setText("Viendo:");

        lblPlace.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        lblPlace.setText("Patio de recreo");

        javax.swing.GroupLayout panelAutoLayout = new javax.swing.GroupLayout(panelAuto);
        panelAuto.setLayout(panelAutoLayout);
        panelAutoLayout.setHorizontalGroup(
            panelAutoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnCam, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblChild)
            .addComponent(lblViewing)
            .addComponent(lblPlace)
            .addComponent(comboNino, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelAutoLayout.setVerticalGroup(
            panelAutoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelAutoLayout.createSequentialGroup()
                .addComponent(lblChild)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(comboNino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCam)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblViewing)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblPlace))
        );

        panelVideo.setBackground(new java.awt.Color(0, 0, 0));
        panelVideo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        panelVideo.setMinimumSize(new java.awt.Dimension(320, 240));
        panelVideo.setPreferredSize(new java.awt.Dimension(320, 240));

        javax.swing.GroupLayout panelVideoLayout = new javax.swing.GroupLayout(panelVideo);
        panelVideo.setLayout(panelVideoLayout);
        panelVideoLayout.setHorizontalGroup(
            panelVideoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 318, Short.MAX_VALUE)
        );
        panelVideoLayout.setVerticalGroup(
            panelVideoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 238, Short.MAX_VALUE)
        );

        panelManual.setBackground(new java.awt.Color(255, 255, 255));
        panelManual.setBorder(javax.swing.BorderFactory.createTitledBorder("Modo manual"));

        checkManual.setBackground(new java.awt.Color(255, 255, 255));
        checkManual.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        checkManual.setText("Activar");
        checkManual.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkManualActionPerformed(evt);
            }
        });

        lblIdCam.setText("ID Cámara:");

        comboIdCam.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        comboIdCam.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "C1A0", "C1A1" }));
        comboIdCam.setEnabled(false);
        comboIdCam.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateManualView(evt);
            }
        });

        javax.swing.GroupLayout panelManualLayout = new javax.swing.GroupLayout(panelManual);
        panelManual.setLayout(panelManualLayout);
        panelManualLayout.setHorizontalGroup(
            panelManualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelManualLayout.createSequentialGroup()
                .addGroup(panelManualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(checkManual)
                    .addComponent(lblIdCam))
                .addGap(0, 112, Short.MAX_VALUE))
            .addComponent(comboIdCam, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelManualLayout.setVerticalGroup(
            panelManualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelManualLayout.createSequentialGroup()
                .addComponent(checkManual)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblIdCam)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(comboIdCam, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        panelLoc.setBackground(new java.awt.Color(255, 255, 255));
        panelLoc.setBorder(javax.swing.BorderFactory.createTitledBorder("Localización"));

        javax.swing.GroupLayout panelLocLayout = new javax.swing.GroupLayout(panelLoc);
        panelLoc.setLayout(panelLocLayout);
        panelLocLayout.setHorizontalGroup(
            panelLocLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        panelLocLayout.setVerticalGroup(
            panelLocLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 149, Short.MAX_VALUE)
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
            .addComponent(toolbar, javax.swing.GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panelVideo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelLoc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panelManual, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelAuto, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(panelAuto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelManual, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .addComponent(panelVideo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelLoc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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
            this.lectorNino.suspender();
            this.panelVideo.revalidate();
            this.panelVideo.repaint();
            return;
        }
        
        // Cambia a la nueva
        this.lectorNino.reanudar();
        this.panelVideo.add(this.lectorNino.getSuscriptorCamara().getVideoComponent());
        
        // Actualiza
        this.panelVideo.revalidate();
    }//GEN-LAST:event_btnCamClick

    private void comboNinoSelected(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboNinoSelected
        if (this.stop)
            return;

        // Cambia la clave en el lector de niños
        this.lectorNino.cambiarNinoId(this.ninoId[this.comboNino.getSelectedIndex()]);
    }//GEN-LAST:event_comboNinoSelected

    private void checkManualActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkManualActionPerformed
        this.comboIdCam.setEnabled(this.checkManual.isSelected());
        
        this.comboNino.setEnabled(!this.checkManual.isSelected());
        this.btnCam.setEnabled(!this.checkManual.isSelected());
        
        if (this.checkManual.isSelected()) {
            this.lectorNino.suspender();
            this.lectorNino.getSuscriptorCamara().reanudar();
        } else {
            this.btnCamClick(evt);
        }
        
        if (this.camIds.size() > 0) {
            this.comboIdCam.setSelectedIndex(0);
            this.updateManualView(evt);
        }
    }//GEN-LAST:event_checkManualActionPerformed

    private void updateManualView(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateManualView
        // Elimina la vista antigua
        this.panelVideo.removeAll();
        
        // Cambia a la nueva
        String newKey = "'" + this.comboIdCam.getSelectedItem() + "'";
        this.lectorNino.getSuscriptorCamara().cambioParametros(new String[] { newKey });
        this.panelVideo.add(this.lectorNino.getSuscriptorCamara().getVideoComponent());
        
        // Actualiza
        this.panelVideo.revalidate();
    }//GEN-LAST:event_updateManualView
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton btnCam;
    private javax.swing.JCheckBox checkManual;
    private javax.swing.JComboBox comboIdCam;
    private javax.swing.JComboBox comboNino;
    private javax.swing.JLabel lblChild;
    private javax.swing.JLabel lblIdCam;
    private javax.swing.JLabel lblPlace;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblViewing;
    private javax.swing.JMenu menuOpt;
    private javax.swing.JMenuBar menubar;
    private javax.swing.JPanel panelAuto;
    private javax.swing.JPanel panelLoc;
    private javax.swing.JPanel panelManual;
    private javax.swing.JPanel panelVideo;
    private javax.swing.JToolBar toolbar;
    // End of variables declaration//GEN-END:variables
}
