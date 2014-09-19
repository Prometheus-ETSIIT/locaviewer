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

import java.awt.Color;
import java.awt.Toolkit;
import java.net.Socket;

/**
 * Ventana principal del programa.
 */
public class MainWindow extends javax.swing.JFrame {

    /**
     * Crea una nueva ventana sin funcionalidad.
     * Sólo para diseñador.
     */
    public MainWindow() {
        initComponents();
        
        this.setBackground(Color.white);
        this.getContentPane().setBackground(Color.white);
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(InicioSesion.class.getResource("icon.png")));
    }

    public MainWindow(final String[] topicos, final Socket serverUpdate) {
        this();
        
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        toolbar = new javax.swing.JToolBar();
        lblStatus = new javax.swing.JLabel();
        fillervideo = new javax.swing.Box.Filler(new java.awt.Dimension(640, 480), new java.awt.Dimension(640, 480), new java.awt.Dimension(32767, 32767));
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
        menubar = new javax.swing.JMenuBar();
        menuOpt = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("locaviewer Beta ~ by Prometheus");
        setMinimumSize(new java.awt.Dimension(800, 580));
        setPreferredSize(new java.awt.Dimension(800, 580));

        toolbar.setBackground(new java.awt.Color(176, 206, 230));
        toolbar.setFloatable(false);

        lblStatus.setFont(new java.awt.Font("Courier New", 0, 11)); // NOI18N
        lblStatus.setText("Transmitiendo vídeo. . .");
        toolbar.add(lblStatus);

        fillervideo.setBackground(new java.awt.Color(255, 255, 255));
        fillervideo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        panelCam1.setBackground(new java.awt.Color(255, 255, 255));
        panelCam1.setBorder(javax.swing.BorderFactory.createTitledBorder("Cámara 1"));

        lblCam1Child.setText("Niño:");

        comboCam1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        comboCam1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Benito Palacios Sánchez", "Pleonex Pleonizando" }));

        btnCam1.setText("Activar cámara");

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

        btnCam3.setText("Activar cámara");

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

        btnCam2.setText("Activar cámara");

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

        btnCam4.setText("Activar cámara");

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
                .addGap(0, 5, Short.MAX_VALUE))
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
                .addComponent(fillervideo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelCam4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelCam1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelCam3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelCam2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0))
            .addComponent(toolbar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fillervideo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(panelCam1, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(panelCam2, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(panelCam3, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(panelCam4, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addComponent(toolbar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        panelCam3.getAccessibleContext().setAccessibleName("Cámara 3");
        panelCam2.getAccessibleContext().setAccessibleName("Cámara 2");
        panelCam4.getAccessibleContext().setAccessibleName("Cámara 4");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton btnCam1;
    private javax.swing.JToggleButton btnCam2;
    private javax.swing.JToggleButton btnCam3;
    private javax.swing.JToggleButton btnCam4;
    private javax.swing.JComboBox comboCam1;
    private javax.swing.JComboBox comboCam2;
    private javax.swing.JComboBox comboCam3;
    private javax.swing.JComboBox comboCam4;
    private javax.swing.Box.Filler fillervideo;
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
    private javax.swing.JToolBar toolbar;
    // End of variables declaration//GEN-END:variables
}
