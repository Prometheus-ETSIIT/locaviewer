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

import control.TopicoControl;
import control.SuscriptorCamara;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Toolkit;
import org.gstreamer.swing.VideoComponent;

/**
 * Ventana principal del programa.
 */
public class MainWindow extends javax.swing.JFrame {
    private boolean stop;
    private TopicoControl dominio;
    private final SuscriptorCamara[] suscriptores = new SuscriptorCamara[4];
    
    /**
     * Crea una nueva ventana sin funcionalidad.
     * Sólo para diseñador.
     */
    public MainWindow() {
        initComponents();
        
        this.setBackground(Color.white);
        this.getContentPane().setBackground(Color.white);
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(InicioSesion.class.getResource("icon.png")));
        
        // Inicia GStreamer
        org.gstreamer.Gst.init();
    }

    /**
     * Crea una nueva ventana que participa en un dominio.
     * 
     * @param dominio Dominio en el que participa.
     */
    public MainWindow(final TopicoControl dominio) {
        this();
        
        this.dominio = dominio;
        this.updateNumNinos();
    }
    
    /**
     * Actualiza los controles de cámaras según del número de niños a los que se
     * pueda visualizar.
     */
    private void updateNumNinos() {
        this.stop = true;
        
        // Pone los tópicos para elegir        
        this.comboCam1.removeAllItems();
        this.comboCam2.removeAllItems();
        this.comboCam3.removeAllItems();
        this.comboCam4.removeAllItems();
        
        for (String nombre : this.dominio.getKeys()) {
            this.comboCam1.addItem(nombre);
            this.comboCam2.addItem(nombre);
            this.comboCam3.addItem(nombre);
            this.comboCam4.addItem(nombre);
        }
        
        this.stop = false;
        
        switch (this.dominio.getNumKeys()) {
            case 0:
                this.setEnablePanel(panelCam1, false);
                this.setEnablePanel(panelCam2, false);
                this.setEnablePanel(panelCam3, false);
                this.setEnablePanel(panelCam4, false);
                break;
                
            case 1:
                this.setEnablePanel(panelCam1, true);
                this.setEnablePanel(panelCam2, false);
                this.setEnablePanel(panelCam3, false);
                this.setEnablePanel(panelCam4, false);
                break;
                
            case 2:
                this.setEnablePanel(panelCam1, true);
                this.setEnablePanel(panelCam2, true);
                this.setEnablePanel(panelCam3, false);
                this.setEnablePanel(panelCam4, false);
                break;
                
            case 3:
                this.setEnablePanel(panelCam1, true);
                this.setEnablePanel(panelCam2, true);
                this.setEnablePanel(panelCam3, true);
                this.setEnablePanel(panelCam4, false);
                break;
                
            default:
                this.setEnablePanel(panelCam1, true);
                this.setEnablePanel(panelCam2, true);
                this.setEnablePanel(panelCam3, true);
                this.setEnablePanel(panelCam4, true);
                break;
        }
    }
    
    /**
     * Habilita o deshabilita un JPanel y todos sus controles.
     * 
     * @param panel JPanel a modificar.
     * @param enable Si se habilita o deshabilita.
     */
    private void setEnablePanel(javax.swing.JPanel panel, boolean enable) {
        panel.setEnabled(enable);
        for (java.awt.Component comp : panel.getComponents())
            comp.setEnabled(enable);
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
        panelCam3 = new javax.swing.JPanel();
        lblCam3Child = new javax.swing.JLabel();
        comboCam3 = new javax.swing.JComboBox();
        btnCam3 = new javax.swing.JToggleButton();
        lblCam3Tag = new javax.swing.JLabel();
        lblCam3Place = new javax.swing.JLabel();
        panelCam2 = new javax.swing.JPanel();
        lblCam2Child = new javax.swing.JLabel();
        comboCam2 = new javax.swing.JComboBox();
        btnCam2 = new javax.swing.JToggleButton();
        lblCam2Tag = new javax.swing.JLabel();
        lblCam2Place = new javax.swing.JLabel();
        panelCam4 = new javax.swing.JPanel();
        lblCam4Child = new javax.swing.JLabel();
        comboCam4 = new javax.swing.JComboBox();
        btnCam4 = new javax.swing.JToggleButton();
        lblCam4Tag = new javax.swing.JLabel();
        lblCam4Place = new javax.swing.JLabel();
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

        panelCam3.setBackground(new java.awt.Color(255, 255, 255));
        panelCam3.setBorder(javax.swing.BorderFactory.createTitledBorder("Cámara 3"));

        lblCam3Child.setText("Niño:");

        comboCam3.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        comboCam3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Benito Palacios Sánchez", "Pleonex Pleonizando" }));
        comboCam3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateCams(evt);
            }
        });

        btnCam3.setText("Activar cámara");
        btnCam3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateCams(evt);
            }
        });

        lblCam3Tag.setText("Sala:");

        lblCam3Place.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        lblCam3Place.setText("Patio de recreo");

        javax.swing.GroupLayout panelCam3Layout = new javax.swing.GroupLayout(panelCam3);
        panelCam3.setLayout(panelCam3Layout);
        panelCam3Layout.setHorizontalGroup(
            panelCam3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(comboCam3, 0, 0, Short.MAX_VALUE)
            .addComponent(btnCam3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panelCam3Layout.createSequentialGroup()
                .addGroup(panelCam3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblCam3Child)
                    .addGroup(panelCam3Layout.createSequentialGroup()
                        .addComponent(lblCam3Tag)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblCam3Place)))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        panelCam3Layout.setVerticalGroup(
            panelCam3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCam3Layout.createSequentialGroup()
                .addComponent(lblCam3Child)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(comboCam3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCam3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCam3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCam3Tag)
                    .addComponent(lblCam3Place))
                .addContainerGap())
        );

        panelCam2.setBackground(new java.awt.Color(255, 255, 255));
        panelCam2.setBorder(javax.swing.BorderFactory.createTitledBorder("Cámara 2"));

        lblCam2Child.setText("Niño:");

        comboCam2.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        comboCam2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Benito Palacios Sánchez", "Pleonex Pleonizando" }));
        comboCam2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateCams(evt);
            }
        });

        btnCam2.setText("Activar cámara");
        btnCam2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateCams(evt);
            }
        });

        lblCam2Tag.setText("Sala:");

        lblCam2Place.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        lblCam2Place.setText("Patio de recreo");

        javax.swing.GroupLayout panelCam2Layout = new javax.swing.GroupLayout(panelCam2);
        panelCam2.setLayout(panelCam2Layout);
        panelCam2Layout.setHorizontalGroup(
            panelCam2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(comboCam2, 0, 0, Short.MAX_VALUE)
            .addComponent(btnCam2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panelCam2Layout.createSequentialGroup()
                .addGroup(panelCam2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblCam2Child)
                    .addGroup(panelCam2Layout.createSequentialGroup()
                        .addComponent(lblCam2Tag)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblCam2Place)))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        panelCam2Layout.setVerticalGroup(
            panelCam2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCam2Layout.createSequentialGroup()
                .addComponent(lblCam2Child)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(comboCam2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCam2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCam2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCam2Tag)
                    .addComponent(lblCam2Place))
                .addContainerGap())
        );

        panelCam4.setBackground(new java.awt.Color(255, 255, 255));
        panelCam4.setBorder(javax.swing.BorderFactory.createTitledBorder("Cámara 4"));

        lblCam4Child.setText("Niño:");

        comboCam4.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        comboCam4.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Benito Palacios Sánchez", "Pleonex Pleonizando" }));
        comboCam4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateCams(evt);
            }
        });

        btnCam4.setText("Activar cámara");
        btnCam4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateCams(evt);
            }
        });

        lblCam4Tag.setText("Sala:");

        lblCam4Place.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        lblCam4Place.setText("Patio de recreo");

        javax.swing.GroupLayout panelCam4Layout = new javax.swing.GroupLayout(panelCam4);
        panelCam4.setLayout(panelCam4Layout);
        panelCam4Layout.setHorizontalGroup(
            panelCam4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(comboCam4, 0, 0, Short.MAX_VALUE)
            .addComponent(btnCam4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panelCam4Layout.createSequentialGroup()
                .addGroup(panelCam4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblCam4Child)
                    .addGroup(panelCam4Layout.createSequentialGroup()
                        .addComponent(lblCam4Tag)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblCam4Place)))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        panelCam4Layout.setVerticalGroup(
            panelCam4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCam4Layout.createSequentialGroup()
                .addComponent(lblCam4Child)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(comboCam4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCam4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCam4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCam4Tag)
                    .addComponent(lblCam4Place))
                .addContainerGap())
        );

        panelVideo.setBackground(new java.awt.Color(255, 255, 255));
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelCam4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelCam1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelCam3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelCam2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addComponent(toolbar, javax.swing.GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(panelCam1, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(panelCam2, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(panelCam3, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(panelCam4, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(panelVideo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
        
        int numCams = 0;
        if (this.btnCam1.isSelected()) numCams++;
        if (this.btnCam2.isSelected()) numCams++;
        if (this.btnCam3.isSelected()) numCams++;
        if (this.btnCam4.isSelected()) numCams++;
        
        this.panelVideo.removeAll();
        
        if (numCams > 1)    // Multi View
            this.panelVideo.setLayout(new GridLayout(2, 2));
        else                // Single View
            this.panelVideo.setLayout(new GridLayout(1, 1));
        
        // Añade las vistas
        this.updateCam(this.btnCam1.isSelected(), this.comboCam1.getSelectedItem().toString(), 0);
        this.updateCam(this.btnCam2.isSelected(), this.comboCam2.getSelectedItem().toString(), 1);
        this.updateCam(this.btnCam3.isSelected(), this.comboCam3.getSelectedItem().toString(), 2);
        this.updateCam(this.btnCam4.isSelected(), this.comboCam4.getSelectedItem().toString(), 3);

        // Actualiza
        this.panelVideo.validate();
    }//GEN-LAST:event_updateCams
    
    /**
     * Elimina una vista.
     * 
     * @param i Índice de la vista a eliminar.
     */
    private void eliminaView(int i) {
        this.suscriptores[i].dispose();
        this.suscriptores[i] = null;
    }
    
    /**
     * Añade o elimina la iésima vista de cámara.
     * 
     * @param estado Si eliminar o crear.
     * @param nombre Parámetro key de la cámara.
     * @param i Elemento iésimo.
     */
    private void updateCam(boolean estado, String nombre, int i) {
        if (!estado && this.suscriptores[i] != null) {
            this.eliminaView(i);
        } else if (estado) {
            if (this.suscriptores[i] == null) {
                this.suscriptores[i] = new SuscriptorCamara(
                        this.dominio,
                        nombre,
                        new VideoComponent()
                );
            }

            VideoComponent comp = this.suscriptores[i].getVideoComponent();            
            this.panelVideo.add(comp);
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton btnCam1;
    private javax.swing.JToggleButton btnCam2;
    private javax.swing.JToggleButton btnCam3;
    private javax.swing.JToggleButton btnCam4;
    private javax.swing.JComboBox comboCam1;
    private javax.swing.JComboBox comboCam2;
    private javax.swing.JComboBox comboCam3;
    private javax.swing.JComboBox comboCam4;
    private javax.swing.JLabel lblCam1Child;
    private javax.swing.JLabel lblCam1Place;
    private javax.swing.JLabel lblCam1Tag;
    private javax.swing.JLabel lblCam2Child;
    private javax.swing.JLabel lblCam2Place;
    private javax.swing.JLabel lblCam2Tag;
    private javax.swing.JLabel lblCam3Child;
    private javax.swing.JLabel lblCam3Place;
    private javax.swing.JLabel lblCam3Tag;
    private javax.swing.JLabel lblCam4Child;
    private javax.swing.JLabel lblCam4Place;
    private javax.swing.JLabel lblCam4Tag;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JMenu menuOpt;
    private javax.swing.JMenuBar menubar;
    private javax.swing.JPanel panelCam1;
    private javax.swing.JPanel panelCam2;
    private javax.swing.JPanel panelCam3;
    private javax.swing.JPanel panelCam4;
    private javax.swing.JPanel panelVideo;
    private javax.swing.JToolBar toolbar;
    // End of variables declaration//GEN-END:variables
}
