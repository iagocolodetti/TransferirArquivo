/*
 * Copyright (C) 2019 Iago Colodetti
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
package br.com.iagocolodetti.transferirarquivo;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author iagocolodetti
 */
public class AddFiles extends javax.swing.JFrame {

    private MainGUI mainGUI = null;
    private String side = null;
    private List<File> selectedFiles = null;
    private List<File> _files = null;
    private long totalSize = 0;
    private String lastDir = "";

    private final DefaultTableModel model;

    private void addFiles() {
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            for (File file : selectedFiles) {
                if (!_files.contains(file)) {
                    _files.add(file);
                    String bytesTo = cmbTotalBytes.getSelectedItem().toString();
                    model.addRow(new Object[]{file.getName(), Utils.bytesTo(file.length(), bytesTo) + " " + bytesTo});
                    tblFiles.changeSelection(tblFiles.getRowCount() - 1, 0, false, false);
                    totalSize += file.length();
                    txtTotalSize.setText(Utils.bytesTo(totalSize, bytesTo));
                } else {
                    Utils.msgBoxError(rootPane, "O arquivo \"" + file.getName() + "\" já foi adicionado à lista.");
                }
            }
            txtFile.setText("");
            txtSize.setText("");
            selectedFiles.clear();
        }
    }

    private void removeFiles() {
        int[] rows = tblFiles.getSelectedRows();
        for (int i = 0; i < rows.length; i++) {
            int index = rows[i] - i;
            if (index > -1) {
                totalSize -= _files.get(index).length();
                _files.remove(index);
                model.removeRow(index);
                if (index == 0 && tblFiles.getRowCount() > 0) {
                    tblFiles.changeSelection(index, 0, false, false);
                } else if (index > 0) {
                    tblFiles.changeSelection(index - 1, 0, false, false);
                }
                txtTotalSize.setText(Utils.bytesTo(totalSize, cmbTotalBytes.getSelectedItem().toString()));
            }
        }
    }

    private void removeAllFiles() {
        _files.clear();
        model.setRowCount(0);
        totalSize = 0;
        txtTotalSize.setText("");
    }

    /**
     * Creates new form AddFiles
     */
    private AddFiles() {
        initComponents();
        model = (DefaultTableModel) tblFiles.getModel();
    }

    public AddFiles(MainGUI mainGUI, String side, List<File> files, String lastDir) {
        initComponents();
        model = (DefaultTableModel) tblFiles.getModel();
        this.mainGUI = mainGUI;
        this.side = side;
        _files = new ArrayList<>();
        selectedFiles = new ArrayList<>();
        this.lastDir = lastDir;
        if (files != null && !files.isEmpty()) {
            selectedFiles = files;
            addFiles();
        }
        tblFiles.setDropTarget(new DropTarget() {
            @Override
            public synchronized void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    List droppedFiles = (List) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (droppedFiles.size() > 0) {
                        for (Object object : droppedFiles) {
                            if (object instanceof File) {
                                File file = (File) object;
                                if (!_files.contains(file)) {
                                    _files.add(file);
                                    String bytesTo = cmbTotalBytes.getSelectedItem().toString();
                                    model.addRow(new Object[]{file.getName(), Utils.bytesTo(file.length(), bytesTo) + " " + bytesTo});
                                    tblFiles.changeSelection(tblFiles.getRowCount() - 1, 0, false, false);
                                    totalSize += file.length();
                                    txtTotalSize.setText(Utils.bytesTo(totalSize, bytesTo));
                                } else {
                                    Utils.msgBoxError(rootPane, "O arquivo \"" + file.getName() + "\" já foi adicionado à lista.");
                                }
                            }
                        }
                    }
                } catch (UnsupportedFlavorException | IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblFile = new javax.swing.JLabel();
        txtFile = new javax.swing.JTextField();
        lblSize = new javax.swing.JLabel();
        txtSize = new javax.swing.JTextField();
        cmbBytes = new javax.swing.JComboBox<>();
        btnSelect = new javax.swing.JButton();
        btnAdd = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblFiles = new javax.swing.JTable();
        btnRemove = new javax.swing.JButton();
        lblTotalSize = new javax.swing.JLabel();
        txtTotalSize = new javax.swing.JTextField();
        cmbTotalBytes = new javax.swing.JComboBox<>();
        btnConfirm = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        btnRemoveAll = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("TransferirArquivo - Adicionar Arquivos");
        setFocusable(false);
        setIconImage(new ImageIcon(getClass().getResource("/br/com/iagocolodetti/transferirarquivo/resource/icon.png")).getImage());
        setMinimumSize(new java.awt.Dimension(600, 767));
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        lblFile.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        lblFile.setText("Arquivo:");

        txtFile.setEditable(false);
        txtFile.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        txtFile.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtFile.setFocusable(false);

        lblSize.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        lblSize.setText("Tamanho:");

        txtSize.setEditable(false);
        txtSize.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        txtSize.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtSize.setFocusable(false);

        cmbBytes.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        cmbBytes.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "B", "KB", "MB", "GB", "TB" }));
        cmbBytes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbBytesActionPerformed(evt);
            }
        });

        btnSelect.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        btnSelect.setText("Selecionar Arquivo(s)");
        btnSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectActionPerformed(evt);
            }
        });

        btnAdd.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        btnAdd.setText("Adicionar Arquivo(s)");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        tblFiles.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        tblFiles.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        tblFiles.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Arquivo", "Tamanho"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblFiles.setFillsViewportHeight(true);
        tblFiles.setRowHeight(26);
        tblFiles.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tblFiles.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(tblFiles);
        if (tblFiles.getColumnModel().getColumnCount() > 0) {
            tblFiles.getColumnModel().getColumn(0).setMinWidth(100);
            tblFiles.getColumnModel().getColumn(0).setPreferredWidth(420);
            tblFiles.getColumnModel().getColumn(1).setMinWidth(100);
            tblFiles.getColumnModel().getColumn(1).setPreferredWidth(160);
        }
        tblFiles.getTableHeader().setFont(new java.awt.Font("Tahoma", 0, 14));
        tblFiles.setShowGrid(true);
        jScrollPane1.getViewport().setBackground(java.awt.Color.WHITE);

        btnRemove.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        btnRemove.setText("Remover Arquivo(s) Selecionado(s)");
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveActionPerformed(evt);
            }
        });

        lblTotalSize.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        lblTotalSize.setText("Tamanho Total:");

        txtTotalSize.setEditable(false);
        txtTotalSize.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        txtTotalSize.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtTotalSize.setFocusable(false);

        cmbTotalBytes.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        cmbTotalBytes.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "B", "KB", "MB", "GB", "TB" }));
        cmbTotalBytes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbTotalBytesActionPerformed(evt);
            }
        });

        btnConfirm.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        btnConfirm.setText("Confirmar");
        btnConfirm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConfirmActionPerformed(evt);
            }
        });

        btnCancel.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        btnCancel.setText("Cancelar");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        btnRemoveAll.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        btnRemoveAll.setText("Remover Todos Arquivos");
        btnRemoveAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveAllActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 580, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblSize)
                            .addComponent(lblFile))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtSize)
                                .addGap(18, 18, 18)
                                .addComponent(cmbBytes, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(txtFile)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnSelect, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblTotalSize)
                        .addGap(18, 18, 18)
                        .addComponent(txtTotalSize)
                        .addGap(18, 18, 18)
                        .addComponent(cmbTotalBytes, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnConfirm, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnRemoveAll, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnRemove, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblFile)
                    .addComponent(txtFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSize)
                    .addComponent(txtSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbBytes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSelect)
                    .addComponent(btnAdd))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 493, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRemoveAll)
                    .addComponent(btnRemove))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTotalSize)
                    .addComponent(txtTotalSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbTotalBytes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnConfirm)
                    .addComponent(btnCancel))
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void cmbBytesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbBytesActionPerformed
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            long tamanho = 0;
            for (File arquivo : selectedFiles) {
                tamanho += arquivo.length();
            }
            txtSize.setText(Utils.bytesTo(tamanho, cmbBytes.getSelectedItem().toString()));
        }
    }//GEN-LAST:event_cmbBytesActionPerformed

    private void btnSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectActionPerformed
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setMultiSelectionEnabled(true);
            if (lastDir != null && !lastDir.isEmpty()) {
                chooser.setCurrentDirectory(new File(lastDir));
            }
            chooser.setDialogTitle("Selecione o(s) arquivo(s)");

            if (chooser.showOpenDialog(this) == JFileChooser.OPEN_DIALOG) {
                selectedFiles.clear();
                long size = 0;
                for (File file : chooser.getSelectedFiles()) {
                    selectedFiles.add(file);
                    size += file.length();
                }
                lastDir = chooser.getSelectedFile().getParent();
                txtFile.setText((selectedFiles.size() == 1 ? selectedFiles.get(0).getName() : "Vários"));
                txtSize.setText(Utils.bytesTo(size, cmbBytes.getSelectedItem().toString()));
            }
        } catch (IndexOutOfBoundsException ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_btnSelectActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        addFiles();
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed
        removeFiles();
    }//GEN-LAST:event_btnRemoveActionPerformed

    private void cmbTotalBytesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbTotalBytesActionPerformed
        if (!_files.isEmpty()) {
            String bytesTo = cmbTotalBytes.getSelectedItem().toString();
            for (int i = 0; i < model.getRowCount(); i++) {
                model.setValueAt(Utils.bytesTo(_files.get(i).length(), bytesTo) + " " + bytesTo, i, 1);
            }
            txtTotalSize.setText(Utils.bytesTo(totalSize, bytesTo));
        }
    }//GEN-LAST:event_cmbTotalBytesActionPerformed

    private void btnConfirmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConfirmActionPerformed
        if (_files.size() > 1) {
            if (side.equals("srv")) {
                mainGUI.srvAddFiles(_files, totalSize);
                mainGUI.srvSetLastDir(lastDir);
            } else if (side.equals("clt")) {
                mainGUI.cltAddFiles(_files, totalSize);
                mainGUI.cltSetLastDir(lastDir);
            }
            this.dispose();
            mainGUI.setVisible(true);
        } else {
            Utils.msgBoxError(rootPane, "Adicione pelo menos 2 (dois) arquivos.");
        }
    }//GEN-LAST:event_btnConfirmActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        if (side.equals("srv")) {
            mainGUI.srvSetLastDir(lastDir);
        } else if (side.equals("clt")) {
            mainGUI.cltSetLastDir(lastDir);
        }
        this.dispose();
        mainGUI.setVisible(true);
    }//GEN-LAST:event_btnCancelActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        mainGUI.setVisible(true);
    }//GEN-LAST:event_formWindowClosing

    private void btnRemoveAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveAllActionPerformed
        if (JOptionPane.showConfirmDialog(rootPane, "Deseja realmente remover todos os arquivos adicionados?", "Remover Todos Arquivos", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
            removeAllFiles();
        }
    }//GEN-LAST:event_btnRemoveAllActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AddFiles.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AddFiles.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AddFiles.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AddFiles.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }
                new AddFiles().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnConfirm;
    private javax.swing.JButton btnRemove;
    private javax.swing.JButton btnRemoveAll;
    private javax.swing.JButton btnSelect;
    private javax.swing.JComboBox<String> cmbBytes;
    private javax.swing.JComboBox<String> cmbTotalBytes;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblFile;
    private javax.swing.JLabel lblSize;
    private javax.swing.JLabel lblTotalSize;
    private javax.swing.JTable tblFiles;
    private javax.swing.JTextField txtFile;
    private javax.swing.JTextField txtSize;
    private javax.swing.JTextField txtTotalSize;
    // End of variables declaration//GEN-END:variables
}
