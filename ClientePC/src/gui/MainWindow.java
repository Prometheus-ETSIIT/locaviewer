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
import es.prometheus.dds.DiscoveryChange;
import es.prometheus.dds.DiscoveryChangeStatus;
import es.prometheus.dds.DiscoveryData;
import es.prometheus.dds.DiscoveryListener;
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
    private static final String CHILD_TOPIC_NAME = "ChildDataTopic";
    private static final String VIDEO_TOPIC_NAME = "VideoDataTopic";
    private static final String PARTICIPANT_NAME = "MisParticipantes::ParticipantePC";
    
    private boolean stop;
    
    private LectorNino lectorNino;
    private TopicoControl controlNino;
    private TopicoControl controlCamaras;
    private final List<DatosCamara> camData = new ArrayList<>();
    private final List<DatosNino> childData = new ArrayList<>();

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
        
        // Creamos una lista que iremos completando poco a poco.
        for (String id : childrenId) {
            DatosNino d = new DatosNino();
            d.setId(id);
            this.childData.add(d);
        }
        
        // Crea los dos controles de tópicos (niños y vídeo).
        this.controlNino = TopicoControlFactoria.crearControlDinamico(
                PARTICIPANT_NAME,
                CHILD_TOPIC_NAME);
        this.controlCamaras = TopicoControlFactoria.crearControlDinamico(
                PARTICIPANT_NAME,
                VIDEO_TOPIC_NAME);
        
        // Crea el lector de vídeo.
        this.lectorNino = new LectorNino(controlNino, childrenId[0], controlCamaras);
        this.panelVideo.add(this.lectorNino.getSuscriptorCamara().getVideoComponent());
        
        // Actualizamos las listas por cada publicador ya existente
        for (DiscoveryData d : this.controlNino.getParticipanteControl().getDiscoveryWriterData())
            onWriterDiscovered(d, DiscoveryChangeStatus.ANADIDO);
        
        // Listener para cuando se descubra un publicador nuevo.
        this.controlNino.getParticipanteControl().addDiscoveryWriterListener(new DiscoveryListener() {
            @Override
            public void onChange(DiscoveryChange[] changes) {
                for (DiscoveryChange ch : changes)
                    onWriterDiscovered(ch.getData(), ch.getStatus());
            }
        });
        
        // Listener para cuando se reciba un dato nuevo del niño.
        this.lectorNino.setExtraListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                onNinoDataReceived(lectorNino.getUltimoDato());
            }
        });
        
        // Le decimos que no procese las muestras y lo iniciamos.
        this.lectorNino.suspender();
        this.lectorNino.iniciar();
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
        lblPlace.setText(" ");

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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
            .addGap(0, 150, Short.MAX_VALUE)
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
                .addGap(10, 10, 10)
                .addComponent(panelLoc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(toolbar, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Actualiza las listas de cámaras y niños a partir de los publicadores
     * descubiertos.
     * 
     * @param data Datos del publicador descubierto.
     * @param status Estado del publicador descubierto.
     */
    private void onWriterDiscovered(DiscoveryData data, DiscoveryChangeStatus status) {
        String userData = new String(data.getUserData().toArrayByte(null));
        
        if (data.getTopicName().equals(VIDEO_TOPIC_NAME)) {
            // Busca si ya está en la lista
            DatosCamara info = DatosCamara.FromStringSummary(userData);
            int idx = -1;
            for (int i = 0; i < this.camData.size() && idx == -1; i++)
                if (this.camData.get(i).getCamId().equals(info.getCamId()))
                    idx = i;
            
            // Actualiza la lista
            if (idx != -1 && status == DiscoveryChangeStatus.ELIMINADO) {
                // TODO: Enviar notificación
                this.comboIdCam.removeItemAt(idx);
                this.camData.remove(idx);
            } else if (idx == -1 && status == DiscoveryChangeStatus.ANADIDO) {
                this.comboIdCam.addItem(info.getSala() + " | " + info.getCamId());
                this.camData.add(info);
            }
        } else if (data.getTopicName().equals(CHILD_TOPIC_NAME)) {
            // Busca si está en la lista
            DatosNino info = DatosNino.FromSummary(userData);
            int idx = -1;
            boolean filled = false;
            for (int i = 0; i < this.childData.size() && idx == -1; i++) {
                if (this.childData.get(i).getId().equals(info.getId())) {
                    idx = i;
                    filled = this.childData.get(i).getNombre() != null;
                }
            }

            // ¡No es nuestro niño!
            if (idx == -1)
                return;
            
            // Actualiza la lista
            if (status == DiscoveryChangeStatus.ELIMINADO) {
                if (filled) {
                    // TODO: Enviar notificación
                    if (this.comboNino.getSelectedIndex() == idx)
                        this.btnCam.setSelected(false);
                    this.comboNino.removeItemAt(idx);
                }
                
                // Lo establecemos a vacío para que la próxima vez
                // filled esté a false.
                this.childData.get(idx).setNombre(null);
            } else if (status == DiscoveryChangeStatus.ANADIDO) {
                if (!filled) {
                    // TODO: Enviar notificación
                    this.childData.set(idx, info);
                    this.comboNino.addItem(info.getApodo());
                }
            }
            
            this.btnCam.setEnabled(this.comboNino.getItemCount() > 0);
        }
    }
    
    private void onNinoDataReceived(DatosNino data) {
        this.lblPlace.setText(data.getSala());
    }
    
    private void btnCamClick(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCamClick
        // Si el botón está desactivado, borrar la pantalla.
        if (!this.btnCam.isSelected()) {
            this.lectorNino.suspender();
            this.lectorNino.getSuscriptorCamara().getVideoComponent().setVisible(false);
            return;
        }
        
        // Cambia a la nueva
        this.lectorNino.reanudar();
        this.lectorNino.getSuscriptorCamara().getVideoComponent().setVisible(true);
        
        // Actualiza
        this.panelVideo.revalidate();
    }//GEN-LAST:event_btnCamClick

    private void comboNinoSelected(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboNinoSelected
        if (this.stop)
            return;

        if (!this.btnCam.isSelected())
            return;
            
        // Cambia la clave en el lector de niños
        int idx = this.comboNino.getSelectedIndex();
        this.lectorNino.cambiarNinoId(this.childData.get(idx).getId());
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
        
        if (this.camData.size() > 0)
            this.comboIdCam.setSelectedIndex(0);
    }//GEN-LAST:event_checkManualActionPerformed

    private void updateManualView(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateManualView
        if (!this.checkManual.isSelected())
            return;
        
        // Cambia a la nueva
        int idx = this.comboIdCam.getSelectedIndex();
        String newKey = "'" + this.camData.get(idx).getCamId() + "'";
        this.lectorNino.getSuscriptorCamara().cambioParametros(new String[] { newKey });
        this.lectorNino.getSuscriptorCamara().getVideoComponent().setVisible(true);
        
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
