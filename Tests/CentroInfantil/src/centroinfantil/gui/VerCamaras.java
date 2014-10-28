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

package centroinfantil.gui;

import centroinfantil.DatosCamara;
import centroinfantil.LectorCamara;
import es.prometheus.dds.DiscoveryChange;
import es.prometheus.dds.DiscoveryChangeStatus;
import es.prometheus.dds.DiscoveryData;
import es.prometheus.dds.DiscoveryListener;
import es.prometheus.dds.TopicoControl;
import es.prometheus.dds.TopicoControlFactoria;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gstreamer.swing.VideoComponent;

/**
 * Diálogo para ver todas las cámaras del sistema clasificadas por habitación.
 */
public class VerCamaras extends javax.swing.JFrame {
    private static final String VIDEO_TOPIC_NAME = "VideoDataTopic";
    private static final String PARTICIPANT_NAME = "MisParticipantes::ParticipanteCI";
    
    private final Map<String, List<String>> cams = new HashMap<>();
    private final List<TabComponents> tabComp = new ArrayList<>();
    
    private final List<LectorCamara[]> lectores = new ArrayList<>();
    private final TopicoControl controlCamaras;
    
    /**
     * Crea una nueva instancia.
     */
    public VerCamaras() {
        initComponents();
        
        this.setTitle("Videovigilancia de Centro Infantil");
        // TODO: Poner icono
        this.setBackground(Color.white);
        this.getContentPane().setBackground(Color.white);
        
        // Inicia GStreamer
        if (!org.gstreamer.Gst.isInitialized())
            org.gstreamer.Gst.init();
        
                // Creo el control del tópico de las cámaras
        this.controlCamaras = TopicoControlFactoria.crearControlDinamico(
                PARTICIPANT_NAME,
                VIDEO_TOPIC_NAME);
                        
        // Actualizamos las listas por cada publicador ya existente
        for (DiscoveryData d : this.controlCamaras.getParticipanteControl().getDiscoveryWriterData())
            onWriterDiscovered(d, DiscoveryChangeStatus.ANADIDO);
        
        // Listener para cuando se descubra un publicador nuevo.
        this.controlCamaras.getParticipanteControl().addDiscoveryWriterListener(new DiscoveryListener() {
            @Override
            public void onChange(DiscoveryChange[] changes) {
                for (DiscoveryChange ch : changes)
                    onWriterDiscovered(ch.getData(), ch.getStatus());
            }
        });
        
        this.addTab();
    }
    
    /**
     * Actualiza las listas de cámaras a partir de los publicadores
     * descubiertos.
     * 
     * @param data Datos del publicador descubierto.
     * @param status Estado del publicador descubierto.
     */
    private void onWriterDiscovered(DiscoveryData data, DiscoveryChangeStatus status) {
        String userData = new String(data.getUserData().toArrayByte(null));
        
        // Solo nos centramos en las cámaras, los datos de los niños
        // nos lo da el servidor
        if (!data.getTopicName().equals(VIDEO_TOPIC_NAME))
            return;
            
        // Busca si ya está en la lista
        DatosCamara info = DatosCamara.FromStringSummary(userData);
        int idx = -1;
        if (this.cams.containsKey(info.getSala())) {
            List<String> camIds = this.cams.get(info.getSala());
            for (int i = 0; i < camIds.size() && idx == -1; i++)
                if (camIds.get(i).equals(info.getCamId()))
                    idx = i;
        }

        // Actualiza la lista
        if (idx != -1 && status == DiscoveryChangeStatus.ELIMINADO) {
            // TODO: Enviar notificación
            System.out.println("Fuera cámara: " + info.getCamId());
            this.cams.get(info.getSala()).remove(idx);
            
            boolean removeRoom = this.cams.get(info.getSala()).isEmpty();
            if (removeRoom)
                this.cams.remove(info.getSala());
            
            // Actualiza los combobox de todas las pestañas
            for (TabComponents c : this.tabComp) {
                boolean thisRoom = c.getComboRoom().getSelectedItem() == info.getSala();
                if (removeRoom)
                    c.getComboRoom().removeItem(info.getSala());
                
                // Actualiza los controles de cámaras
                for (int i = 0; i < c.getControlsNum() && thisRoom; i++) {
                    if (c.getComboControl(i).getSelectedItem().equals(info.getCamId()))
                        c.getCheckControl(i).setSelected(false);
                    
                    c.getComboControl(i).removeItem(info.getCamId());
                }
            }
        } else if (idx == -1 && status == DiscoveryChangeStatus.ANADIDO) {
            // TODO: Enviar notificación
            System.out.println("Dentro cámara: " + info.getCamId());
            if (!this.cams.containsKey(info.getSala())) {
                this.cams.put(info.getSala(), new ArrayList<String>());
                for (TabComponents c : this.tabComp)
                    c.getComboRoom().addItem(info.getSala());
            }
            
            this.cams.get(info.getSala()).add(info.getCamId());
            for (TabComponents c : this.tabComp) {
                boolean thisRoom = c.getComboRoom().getSelectedItem() == info.getSala();
                for (int i = 0; i < c.getControlsNum() && thisRoom; i++) {
                    c.getComboControl(i).addItem(info.getCamId());
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        roomTabs = new javax.swing.JTabbedPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(900, 620));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(roomTabs, javax.swing.GroupLayout.DEFAULT_SIZE, 570, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(roomTabs, javax.swing.GroupLayout.DEFAULT_SIZE, 439, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        for (LectorCamara[] lectoresTab : this.lectores)
            for (LectorCamara lector : lectoresTab)
                lector.dispose();
        
        this.controlCamaras.dispose();
    }//GEN-LAST:event_formWindowClosing

    private void addTab() {
        final int tabIdx = this.roomTabs.getTabCount();
        
        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        layout.columnWidths = new int[] { 170, 170, 170, 170, 210 };
        layout.rowHeights   = new int[] {  30, 130, 130, 130, 130 };
        panel.setLayout(layout); 
        GridBagConstraints con = new GridBagConstraints();
        
        // Componentes de selección de habitación.
        JLabel label = new JLabel("Seleccione habitación:");
        con.gridx = 0;
        con.gridy = 0;
        con.gridwidth  = 1;
        con.gridheight = 1;
        con.fill   = GridBagConstraints.NONE;
        con.ipadx  = 0;
        con.ipady  = 0;
        con.insets = new Insets(0, 5, 0, 0);
        con.anchor = GridBagConstraints.LINE_START;
        con.weightx = 0;
        con.weighty = 0;
        panel.add(label, con);
        
        final JComboBox combo = new JComboBox(new String[] { "Desactivar" });
        for (String room : this.cams.keySet())
            combo.addItem(room);
        combo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                onComboRoomChanged(combo, tabIdx);
            }
        });
        con.gridx = 1;
        con.gridy = 0;
        con.gridwidth  = 1;
        con.gridheight = 1;
        con.fill   = GridBagConstraints.HORIZONTAL;
        con.ipadx  = 0;
        con.ipady  = 0;
        con.insets = new Insets(0, 0, 0, 5);
        con.anchor = GridBagConstraints.LINE_START;
        con.weightx = 1;
        con.weighty = 0;
        panel.add(combo, con);
        
        // Botones para añadir o quitar pestañas
        JButton btnAnadir = new JButton("Añadir habitación");
        btnAnadir.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                addTab();
            }
        });
        con.gridx = 2;
        con.gridy = 0;
        con.gridwidth  = 1;
        con.gridheight = 1;
        con.fill   = GridBagConstraints.NONE;
        con.ipadx  = 0;
        con.ipady  = 0;
        con.insets = new Insets(0, 5, 0, 5);
        con.anchor = GridBagConstraints.LINE_END;
        con.weightx = 0;
        con.weighty = 0;
        panel.add(btnAnadir, con);
        
        JButton btnEliminar = new JButton("Eliminar habitación");
        btnEliminar.setEnabled(roomTabs.getTabCount() >= 1);
        btnEliminar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                removeTab();
            }
        });
        con.gridx = 3;
        con.gridy = 0;
        con.gridwidth  = 1;
        con.gridheight = 1;
        con.fill   = GridBagConstraints.NONE;
        con.ipadx  = 0;
        con.ipady  = 0;
        con.insets = new Insets(0, 5, 0, 5);
        con.anchor = GridBagConstraints.LINE_START;
        con.weightx = 0;
        con.weighty = 0;
        panel.add(btnEliminar, con);
        
        // Cámaras
        TabComponents comps = new TabComponents(combo);
        LectorCamara[] lectoresCam = new LectorCamara[4];
        this.lectores.add(lectoresCam);
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 2; x++) {
                final int idCam = x + y * 2;
                
                // Crea el panel de vídeo
                JPanel panelVideo = new JPanel();
                panelVideo.setBackground(Color.black);
                panelVideo.setSize(320, 240);
                panelVideo.setPreferredSize(new Dimension(320, 240));
                con.gridx = x * 2;
                con.gridy = (y * 2) + 1;
                con.gridwidth  = 2;
                con.gridheight = 2;
                con.fill   = GridBagConstraints.BOTH;
                con.ipadx  = 0;
                con.ipady  = 0;
                con.insets = new Insets(5, 5, 5, 5);
                con.anchor = GridBagConstraints.CENTER;
                con.weightx = 0.5;
                con.weighty = 0.5;
                panel.add(panelVideo, con);
                
                // Crea el lector
                VideoComponent videoComp = new VideoComponent();
                videoComp.setVisible(false);
                panelVideo.add(videoComp);
                lectoresCam[idCam] = new LectorCamara(controlCamaras, "'-1'", videoComp);
                lectoresCam[idCam].suspender();
                lectoresCam[idCam].iniciar();
                
                // Crea el controlador de la cámara
                JPanel panelControl = new JPanel();
                panelControl.setBorder(new TitledBorder("Cámara " + (idCam + 1)));
                panelControl.setSize(200, 120);
                panelControl.setPreferredSize(new Dimension(200, 120));
                con.gridx = 4;
                con.gridy = (y * 2) + x + 1;
                con.gridwidth  = 1;
                con.gridheight = 1;
                con.fill   = GridBagConstraints.HORIZONTAL;
                con.ipadx  = 0;
                con.ipady  = 0;
                con.insets = new Insets(5, 5, 5, 5);
                con.anchor = GridBagConstraints.CENTER;
                con.weightx = 0;
                con.weighty = 0.5;
                
                // Añade los controles
                panelControl.setLayout(new BoxLayout(panelControl, BoxLayout.Y_AXIS));
                
                final JCheckBox checkControl = new JCheckBox("Activar", false);
                checkControl.setName(String.valueOf(idCam));
                checkControl.setAlignmentX(Component.LEFT_ALIGNMENT);
                checkControl.setAlignmentY(Component.TOP_ALIGNMENT);
                checkControl.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent ce) {
                        onCheckControlChanged(checkControl, tabIdx, idCam);
                    }
                });
                panelControl.add(checkControl);
                
                Dimension minFill = new Dimension(0, 20);
                Dimension maxFill = new Dimension(0, 60);
                panelControl.add(new Box.Filler(minFill, minFill, maxFill));
                
                JLabel lblControl = new JLabel("Cámara ID:");
                lblControl.setAlignmentX(Component.LEFT_ALIGNMENT);
                lblControl.setAlignmentY(Component.BOTTOM_ALIGNMENT);
                panelControl.add(lblControl);
                
                final JComboBox comboControl = new JComboBox();
                comboControl.setName(String.valueOf(idCam));
                comboControl.setAlignmentX(Component.LEFT_ALIGNMENT);
                comboControl.setAlignmentY(Component.BOTTOM_ALIGNMENT);
                comboControl.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        onComboControlChanged(comboControl, tabIdx, idCam);
                    }
                });
                panelControl.add(comboControl);
                
                PanelEnabled(panelControl, false);
                panel.add(panelControl, con);
                comps.addControl(panelVideo, panelControl, checkControl, comboControl);
            }
        }
        
        roomTabs.addTab("Desactivar", panel);
        this.tabComp.add(comps);
    }
    
    private static void PanelEnabled(JPanel panel, boolean enabled) {
        panel.setEnabled(enabled);
        for (Component c : panel.getComponents())
            c.setEnabled(enabled);
    }
    
    private void removeTab() {
        int currTab = roomTabs.getSelectedIndex();
        roomTabs.removeTabAt(currTab);
        this.tabComp.remove(currTab);
        
        for (LectorCamara lector : this.lectores.get(currTab))
            lector.dispose();
        this.lectores.remove(currTab);
    }
    
    private void onComboRoomChanged(JComboBox combo, int tabIdx) {
        boolean activate = combo.getSelectedIndex() > 0;
        this.roomTabs.setTitleAt(tabIdx, (String)combo.getSelectedItem());
        
        // Busca los componentes correspondientes a esa pestaña
        TabComponents currTab = this.tabComp.get(tabIdx);
        
        // Activa o deshabilita los paneles
        for (int i = 0; i < currTab.getControlsNum(); i++)
            PanelEnabled(currTab.getPanelControl(i), activate);
                
        // Actualiza los combobox
        for (int i = 0; i < currTab.getControlsNum(); i++) {
            // Primero deshabilito todo
            currTab.getComboControl(i).removeAllItems();
            currTab.getCheckControl(i).setSelected(false);
            
            // Añado los nuevos elementos si no es "Deshabilitar"
            if (activate) {
                for (String camId : this.cams.get((String)combo.getSelectedItem()))
                    currTab.getComboControl(i).addItem(camId);
            }
        }
    }
    
    private void onCheckControlChanged(JCheckBox check, int tabIdx, int ctlIdx) {
        LectorCamara lector = this.lectores.get(tabIdx)[ctlIdx];
        if (tabIdx >= this.tabComp.size())
            return;
        
        if (check.isSelected()) {
            // Comienza vídeo
            JComboBox combo = this.tabComp.get(tabIdx).getComboControl(ctlIdx);
            if (combo.getSelectedIndex() == -1)
                return;
            
            lector.getVideoComponent().setVisible(true);
            lector.cambioParametros(new String[] { "'" + combo.getSelectedItem() + "'" });
            lector.reanudar();
        } else {
            lector.getVideoComponent().setVisible(false);
            lector.suspender();
        }
        
        this.tabComp.get(tabIdx).getVideoPanel(ctlIdx).revalidate();
    }
    
    private void onComboControlChanged(JComboBox combo, int tabIdx, int ctlIdx) {
        System.out.println(tabIdx + "|" + ctlIdx);
        if (!this.tabComp.get(tabIdx).getCheckControl(ctlIdx).isSelected())
            return;
        System.out.println(tabIdx + "||" + ctlIdx);
        
        LectorCamara lector = this.lectores.get(tabIdx)[ctlIdx];
        lector.cambioParametros(new String[] { "'" + combo.getSelectedItem() + "'" });
        lector.getVideoComponent().setVisible(true);
        lector.reanudar();
        this.tabComp.get(tabIdx).getVideoPanel(ctlIdx).revalidate();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane roomTabs;
    // End of variables declaration//GEN-END:variables

    private static class TabComponents {
        private final JComboBox comboRoom;
        private final List<JPanel> panelsVideo = new ArrayList<>();
        private final List<JPanel> panelsControl = new ArrayList<>();
        private final List<JCheckBox> checksControl = new ArrayList<>();
        private final List<JComboBox> combosControl = new ArrayList<>();
        
        public TabComponents(JComboBox comboRoom) {
            this.comboRoom = comboRoom;
        }
        
        public void addControl(JPanel videoPanel, JPanel controlPanel, JCheckBox check,
                JComboBox combo) {
            this.panelsVideo.add(videoPanel);
            this.panelsControl.add(controlPanel);
            this.checksControl.add(check);
            this.combosControl.add(combo);
        }
        
        public JComboBox getComboRoom() {
            return this.comboRoom;
        }
        
        public int getControlsNum() {
            return this.panelsControl.size();
        }
        
        public JPanel getVideoPanel(int i) {
            return this.panelsVideo.get(i);
        }
        
        public JPanel getPanelControl(int i) {
            return this.panelsControl.get(i);
        }
        
        public JCheckBox getCheckControl(int i) {
            return this.checksControl.get(i);
        }
        
        public JComboBox getComboControl(int i) {
            return this.combosControl.get(i);
        }
    }
}
