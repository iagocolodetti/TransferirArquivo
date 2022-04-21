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

import br.com.iagocolodetti.transferirarquivo.exception.ClientConnectException;
import br.com.iagocolodetti.transferirarquivo.exception.SendFileException;
import br.com.iagocolodetti.transferirarquivo.exception.ServerStartException;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.DefaultCaret;

/**
 *
 * @author iagocolodetti
 */
public class MainGUI extends javax.swing.JFrame {

    private final String FILE_PROPERTIES = "TransferirArquivo.properties";

    private Server server = null;
    private List<File> srvFiles = null;
    private long srvTotalSize = 0;
    private String srvLastDir = "";

    private Client client = null;
    private List<File> cltFiles = null;
    private long cltTotalSize = 0;
    private String cltLastDir = "";

    private void resetProperties() {
        try {
            Properties p = new Properties();
            p.setProperty("srvPort", "");
            p.setProperty("srvDir", "");
            p.setProperty("srvLastDir", "");
            p.setProperty("srvCmbBatch", "");
            p.setProperty("cltIP", "");
            p.setProperty("cltPort", "");
            p.setProperty("cltDir", "");
            p.setProperty("cltLastDir", "");
            p.setProperty("cltCmbBatch", "");
            p.store(new FileWriter(FILE_PROPERTIES), "Properties File");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void loadProperties() {
        try {
            if (new File(FILE_PROPERTIES).exists()) {
                Properties p = new Properties();
                p.load(new FileReader(FILE_PROPERTIES));
                srvTxtPort.setText(p.getProperty("srvPort"));
                srvTxtDir.setText(p.getProperty("srvDir"));
                srvLastDir = p.getProperty("srvLastDir");
                srvChkBatch.setSelected(Boolean.valueOf(p.getProperty("srvCmbBatch")));
                cltTxtIP.setText(p.getProperty("cltIP"));
                cltTxtPort.setText(p.getProperty("cltPort"));
                cltTxtDir.setText(p.getProperty("cltDir"));
                cltLastDir = p.getProperty("cltLastDir");
                cltChkBatch.setSelected(Boolean.valueOf(p.getProperty("cltCmbBatch")));
            } else {
                resetProperties();
            }
        } catch (IOException ex) {
            resetProperties();
            ex.printStackTrace();
        }
    }

    private void saveProperties() {
        try {
            Properties p = new Properties();
            p.setProperty("srvPort", srvTxtPort.getText());
            p.setProperty("srvDir", srvTxtDir.getText());
            p.setProperty("srvLastDir", srvLastDir);
            p.setProperty("srvCmbBatch", Boolean.toString(srvChkBatch.isSelected()));
            p.setProperty("cltIP", cltTxtIP.getText());
            p.setProperty("cltPort", cltTxtPort.getText());
            p.setProperty("cltDir", cltTxtDir.getText());
            p.setProperty("cltLastDir", cltLastDir);
            p.setProperty("cltCmbBatch", Boolean.toString(cltChkBatch.isSelected()));
            p.store(new FileWriter(FILE_PROPERTIES), "Properties File");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Métodos para o servidor">
    public void srvAddFiles(List<File> files, long totalSize) {
        if (files != null && !files.isEmpty()) {
            this.srvFiles = files;
            srvTxtFile.setText("Vários");
            srvTotalSize = totalSize;
            srvTxtSize.setText(Utils.bytesTo(srvTotalSize, srvCmbBytes.getSelectedItem().toString()));
        }
    }

    public void srvSetLastDir(String lastDir) {
        this.srvLastDir = lastDir;
    }

    public void srvAddTextAreaLog(String message) {
        if (!srvTxaLog.getText().isEmpty()) {
            srvTxaLog.append("\n");
        }
        srvTxaLog.append(String.format("[%s] %s", new SimpleDateFormat("HH:mm:ss").format(new Date()), message));
    }

    public void srvSetClient(String ip) {
        srvTxtClient.setText(ip);
    }

    public void srvSetStatus(String status) {
        srvTxtStatus.setText(status);
    }

    public void srvSetProgressPainted(boolean painted) {
        srvPrg.setStringPainted(painted);
    }

    public void srvSetProgress(int value) {
        srvPrg.setValue(value);
    }

    private void srvStart() {
        if (!srvTxtPort.getText().isEmpty()) {
            int port;
            try {
                port = Integer.parseInt(srvTxtPort.getText());
                server.start(port, srvTxtDir.getText());
            } catch (NumberFormatException ex) {
                Utils.msgBoxError(rootPane, "A porta deve ser definida com número inteiro.");
            } catch (ServerStartException ex) {
                Utils.msgBoxError(rootPane, ex.getMessage());
                if (ex.getErrorCode() == 1) {
                    srvTxtDir.setText(Utils.selectDir(this));
                }
            }
        } else {
            Utils.msgBoxError(rootPane, "Defina o número da porta.");
        }
    }

    public void srvStarting() {
        srvBtnStart.setText("LIGANDO");
        srvTxtPort.setEditable(false);
    }

    public void srvStarted(String ip) {
        srvTxtIP.setText(ip);
        srvBtnStart.setText("DESLIGAR");
    }

    private void srvShutdown() {
        server.shutdown();
    }

    public void srvShutdowned() {
        srvTxtIP.setText("");
        srvTxtPort.setEditable(true);
        srvTxtClient.setText("");
        srvTxtStatus.setText("");
        srvPrg.setValue(0);
        srvBtnStart.setText("LIGAR");
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Métodos para o cliente">
    public void cltAddFiles(List<File> files, long totalSize) {
        if (files != null && !files.isEmpty()) {
            this.cltFiles = files;
            cltTxtFile.setText("Vários");
            cltTotalSize = totalSize;
            cltTxtSize.setText(Utils.bytesTo(cltTotalSize, cltCmbBytes.getSelectedItem().toString()));
        }
    }

    public void cltSetLastDir(String lastDir) {
        this.cltLastDir = lastDir;
    }

    public void cltAddTextAreaLog(String message) {
        if (!cltTxaLog.getText().isEmpty()) {
            cltTxaLog.append("\n");
        }
        cltTxaLog.append(String.format("[%s] %s", new SimpleDateFormat("HH:mm:ss").format(new Date()), message));
    }

    public void cltSetStatus(String status) {
        cltTxtStatus.setText(status);
    }

    public void cltSetProgressPainted(boolean painted) {
        cltPrg.setStringPainted(painted);
    }

    public void cltSetProgress(int value) {
        cltPrg.setValue(value);
    }

    private void cltConnect() {
        if (!cltTxtPort.getText().isEmpty()) {
            int port;
            try {
                port = Integer.parseInt(cltTxtPort.getText());
                client.connect(cltTxtIP.getText(), port, cltTxtDir.getText());
            } catch (NumberFormatException ex) {
                Utils.msgBoxError(rootPane, "A porta deve ser definida com número inteiro.");
            } catch (ClientConnectException ex) {
                Utils.msgBoxError(rootPane, ex.getMessage());
                if (ex.getErrorCode() == 1) {
                    cltTxtDir.setText(Utils.selectDir(this));
                }
            }
        } else {
            Utils.msgBoxError(rootPane, "Defina o número da porta.");
        }
    }

    public void cltConnecting() {
        cltTxtIP.setEditable(false);
        cltTxtPort.setEditable(false);
        cltBtnConnect.setText("CONECTANDO");
    }

    public void cltConnected() {
        cltBtnConnect.setText("DESCONECTAR");
    }

    private void cltDisconnect() {
        client.disconnect();
    }

    public void cltDisconnected() {
        cltTxtIP.setEditable(true);
        cltTxtPort.setEditable(true);
        cltBtnConnect.setText("CONECTAR");
    }
    // </editor-fold>

    /**
     * Creates new form MainGUI
     */
    public MainGUI() {
        initComponents();
        server = new Server(this);
        srvFiles = new ArrayList<>();
        client = new Client(this);
        cltFiles = new ArrayList<>();
        DefaultCaret srvCaret = (DefaultCaret) srvTxaLog.getCaret();
        srvCaret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        DefaultCaret cltCaret = (DefaultCaret) cltTxaLog.getCaret();
        cltCaret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        loadProperties();
        srvTxtFile.setDropTarget(new DropTarget() {
            @Override
            public synchronized void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    List droppedFiles = (List) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    srvFiles.clear();
                    if (droppedFiles.size() == 1) {
                        Object object = droppedFiles.get(0);
                        if (object instanceof File) {
                            File file = (File) object;
                            srvFiles.add(file);
                            srvLastDir = file.getParent();
                            srvTxtFile.setText(srvFiles.get(0).getName());
                            srvTxtSize.setText(Utils.bytesTo(srvFiles.get(0).length(), srvCmbBytes.getSelectedItem().toString()));
                        }
                    } else if (droppedFiles.size() > 1) {
                        for (Object object : droppedFiles) {
                            if (object instanceof File) {
                                File file = (File) object;
                                srvFiles.add(file);
                                srvTotalSize += file.length();
                                srvTxtFile.setText("Vários");
                                srvTxtSize.setText(Utils.bytesTo(srvTotalSize, srvCmbBytes.getSelectedItem().toString()));
                            }
                        }
                    }
                } catch (UnsupportedFlavorException | IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        cltTxtFile.setDropTarget(new DropTarget() {
            @Override
            public synchronized void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    List droppedFiles = (List) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    cltFiles.clear();
                    if (droppedFiles.size() == 1) {
                        Object object = droppedFiles.get(0);
                        if (object instanceof File) {
                            File file = (File) object;
                            cltFiles.add(file);
                            cltLastDir = file.getParent();
                            cltTxtFile.setText(cltFiles.get(0).getName());
                            cltTxtSize.setText(Utils.bytesTo(cltFiles.get(0).length(), cltCmbBytes.getSelectedItem().toString()));
                        }
                    } else if (droppedFiles.size() > 1) {
                        for (Object object : droppedFiles) {
                            if (object instanceof File) {
                                File file = (File) object;
                                cltFiles.add(file);
                                cltTotalSize += file.length();
                                cltTxtFile.setText("Vários");
                                cltTxtSize.setText(Utils.bytesTo(cltTotalSize, cltCmbBytes.getSelectedItem().toString()));
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

        jTabbedPane1 = new javax.swing.JTabbedPane();
        srvPnl = new javax.swing.JPanel();
        srvLblIP = new javax.swing.JLabel();
        srvLblPort = new javax.swing.JLabel();
        srvTxtIP = new javax.swing.JTextField();
        srvTxtPort = new javax.swing.JTextField();
        srvTxtDir = new javax.swing.JTextField();
        srvLblDir = new javax.swing.JLabel();
        srvBtnStart = new javax.swing.JButton();
        srvLblFile = new javax.swing.JLabel();
        srvTxtFile = new javax.swing.JTextField();
        srvLblSize = new javax.swing.JLabel();
        srvTxtSize = new javax.swing.JTextField();
        srvCmbBytes = new javax.swing.JComboBox<>();
        srvBtnSelect = new javax.swing.JButton();
        srvBtnSend = new javax.swing.JButton();
        srvLblClient = new javax.swing.JLabel();
        srvTxtClient = new javax.swing.JTextField();
        srvLblStatus = new javax.swing.JLabel();
        srvTxtStatus = new javax.swing.JTextField();
        srvPrg = new javax.swing.JProgressBar();
        jScrollPane1 = new javax.swing.JScrollPane();
        srvTxaLog = new javax.swing.JTextArea();
        srvChkBatch = new javax.swing.JCheckBox();
        cltPnl = new javax.swing.JPanel();
        cltLblIP = new javax.swing.JLabel();
        cltTxtIP = new javax.swing.JTextField();
        cltLblPort = new javax.swing.JLabel();
        cltTxtPort = new javax.swing.JTextField();
        cltTxtDir = new javax.swing.JTextField();
        cltLblDir = new javax.swing.JLabel();
        cltBtnConnect = new javax.swing.JButton();
        cltLblFile = new javax.swing.JLabel();
        cltTxtFile = new javax.swing.JTextField();
        cltLblSize = new javax.swing.JLabel();
        cltTxtSize = new javax.swing.JTextField();
        cltCmbBytes = new javax.swing.JComboBox<>();
        cltBtnSelect = new javax.swing.JButton();
        cltBtnSend = new javax.swing.JButton();
        cltTxtStatus = new javax.swing.JTextField();
        cltLblStatus = new javax.swing.JLabel();
        cltPrg = new javax.swing.JProgressBar();
        jScrollPane2 = new javax.swing.JScrollPane();
        cltTxaLog = new javax.swing.JTextArea();
        cltChkBatch = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("TransferirArquivo");
        setIconImage(new ImageIcon(getClass().getResource("/br/com/iagocolodetti/transferirarquivo/resource/icon.png")).getImage());
        setMaximumSize(new java.awt.Dimension(600, 767));
        setMinimumSize(new java.awt.Dimension(600, 767));
        setName("FramePrincipal"); // NOI18N
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jTabbedPane1.setBackground(new java.awt.Color(230, 230, 230));
        jTabbedPane1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jTabbedPane1.setMaximumSize(new java.awt.Dimension(1920, 1080));
        jTabbedPane1.setMinimumSize(new java.awt.Dimension(1, 1));
        jTabbedPane1.setPreferredSize(new java.awt.Dimension(470, 800));

        srvPnl.setBackground(new java.awt.Color(235, 235, 235));

        srvLblIP.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        srvLblIP.setText("IP:");

        srvLblPort.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        srvLblPort.setText("Porta:");

        srvTxtIP.setEditable(false);
        srvTxtIP.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        srvTxtIP.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        srvTxtIP.setFocusable(false);

        srvTxtPort.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        srvTxtPort.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        srvTxtDir.setEditable(false);
        srvTxtDir.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        srvTxtDir.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        srvTxtDir.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                srvTxtDirMouseClicked(evt);
            }
        });
        srvTxtDir.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                srvTxtDirKeyPressed(evt);
            }
        });

        srvLblDir.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        srvLblDir.setText("Diretório:");

        srvBtnStart.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        srvBtnStart.setText("LIGAR");
        srvBtnStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                srvBtnStartActionPerformed(evt);
            }
        });

        srvLblFile.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        srvLblFile.setText("Arquivo:");

        srvTxtFile.setEditable(false);
        srvTxtFile.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        srvTxtFile.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        srvTxtFile.setFocusable(false);

        srvLblSize.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        srvLblSize.setText("Tamanho:");

        srvTxtSize.setEditable(false);
        srvTxtSize.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        srvTxtSize.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        srvTxtSize.setFocusable(false);

        srvCmbBytes.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        srvCmbBytes.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "B", "KB", "MB", "GB", "TB" }));
        srvCmbBytes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                srvCmbBytesActionPerformed(evt);
            }
        });

        srvBtnSelect.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        srvBtnSelect.setText("Selecionar Arquivo");
        srvBtnSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                srvBtnSelectActionPerformed(evt);
            }
        });

        srvBtnSend.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        srvBtnSend.setText("Enviar");
        srvBtnSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                srvBtnSendActionPerformed(evt);
            }
        });

        srvLblClient.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        srvLblClient.setText("Cliente Conectado:");

        srvTxtClient.setEditable(false);
        srvTxtClient.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        srvTxtClient.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        srvTxtClient.setFocusable(false);

        srvLblStatus.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        srvLblStatus.setText("Status:");

        srvTxtStatus.setEditable(false);
        srvTxtStatus.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        srvTxtStatus.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        srvTxtStatus.setFocusable(false);

        srvPrg.setForeground(new java.awt.Color(0, 204, 51));
        srvPrg.setFocusable(false);

        srvTxaLog.setEditable(false);
        srvTxaLog.setBackground(new java.awt.Color(240, 240, 240));
        srvTxaLog.setColumns(20);
        srvTxaLog.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        srvTxaLog.setLineWrap(true);
        srvTxaLog.setRows(5);
        srvTxaLog.setWrapStyleWord(true);
        srvTxaLog.setFocusable(false);
        jScrollPane1.setViewportView(srvTxaLog);

        srvChkBatch.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        srvChkBatch.setText("Lote");

        javax.swing.GroupLayout srvPnlLayout = new javax.swing.GroupLayout(srvPnl);
        srvPnl.setLayout(srvPnlLayout);
        srvPnlLayout.setHorizontalGroup(
            srvPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(srvPnlLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(srvPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(srvPnlLayout.createSequentialGroup()
                        .addComponent(srvLblIP)
                        .addGap(50, 50, 50)
                        .addComponent(srvTxtIP)
                        .addGap(18, 18, 18)
                        .addComponent(srvLblPort)
                        .addGap(18, 18, 18)
                        .addComponent(srvTxtPort, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(srvPrg, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(srvPnlLayout.createSequentialGroup()
                        .addComponent(srvLblDir)
                        .addGap(10, 10, 10)
                        .addComponent(srvTxtDir))
                    .addGroup(srvPnlLayout.createSequentialGroup()
                        .addComponent(srvLblSize)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(srvPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(srvTxtFile, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, srvPnlLayout.createSequentialGroup()
                                .addComponent(srvTxtSize, javax.swing.GroupLayout.PREFERRED_SIZE, 414, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(srvCmbBytes, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(srvPnlLayout.createSequentialGroup()
                        .addComponent(srvBtnSelect, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(srvChkBatch)
                        .addGap(50, 50, 50)
                        .addComponent(srvBtnSend, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, srvPnlLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(srvBtnStart, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(166, 166, 166))
                    .addGroup(srvPnlLayout.createSequentialGroup()
                        .addComponent(srvLblStatus)
                        .addGap(18, 18, 18)
                        .addComponent(srvTxtStatus))
                    .addGroup(srvPnlLayout.createSequentialGroup()
                        .addGroup(srvPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(srvLblFile)
                            .addGroup(srvPnlLayout.createSequentialGroup()
                                .addComponent(srvLblClient)
                                .addGap(18, 18, 18)
                                .addComponent(srvTxtClient, javax.swing.GroupLayout.PREFERRED_SIZE, 441, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        srvPnlLayout.setVerticalGroup(
            srvPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(srvPnlLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(srvPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(srvLblIP)
                    .addComponent(srvTxtIP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(srvLblPort)
                    .addComponent(srvTxtPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(srvPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(srvLblDir)
                    .addComponent(srvTxtDir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(srvBtnStart)
                .addGap(18, 18, 18)
                .addGroup(srvPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(srvLblClient)
                    .addComponent(srvTxtClient, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(srvPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(srvLblFile)
                    .addComponent(srvTxtFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(srvPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(srvLblSize)
                    .addComponent(srvTxtSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(srvCmbBytes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(srvPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(srvBtnSelect)
                    .addComponent(srvBtnSend)
                    .addComponent(srvChkBatch))
                .addGap(18, 18, 18)
                .addGroup(srvPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(srvLblStatus)
                    .addComponent(srvTxtStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(13, 13, 13)
                .addComponent(srvPrg, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE)
                .addContainerGap())
        );

        srvTxtPort.getAccessibleContext().setAccessibleName("");

        jTabbedPane1.addTab("      Servidor      ", srvPnl);

        cltPnl.setBackground(new java.awt.Color(235, 235, 235));

        cltLblIP.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        cltLblIP.setText("IP:");

        cltTxtIP.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        cltTxtIP.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        cltLblPort.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        cltLblPort.setText("Porta:");

        cltTxtPort.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        cltTxtPort.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        cltTxtDir.setEditable(false);
        cltTxtDir.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        cltTxtDir.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        cltTxtDir.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cltTxtDirMouseClicked(evt);
            }
        });
        cltTxtDir.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                cltTxtDirKeyPressed(evt);
            }
        });

        cltLblDir.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        cltLblDir.setText("Diretório:");

        cltBtnConnect.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        cltBtnConnect.setText("CONECTAR");
        cltBtnConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cltBtnConnectActionPerformed(evt);
            }
        });

        cltLblFile.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        cltLblFile.setText("Arquivo:");

        cltTxtFile.setEditable(false);
        cltTxtFile.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        cltTxtFile.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        cltTxtFile.setFocusable(false);

        cltLblSize.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        cltLblSize.setText("Tamanho:");

        cltTxtSize.setEditable(false);
        cltTxtSize.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        cltTxtSize.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        cltTxtSize.setFocusable(false);

        cltCmbBytes.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        cltCmbBytes.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "B", "KB", "MB", "GB", "TB" }));
        cltCmbBytes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cltCmbBytesActionPerformed(evt);
            }
        });

        cltBtnSelect.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        cltBtnSelect.setText("Selecionar Arquivo");
        cltBtnSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cltBtnSelectActionPerformed(evt);
            }
        });

        cltBtnSend.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        cltBtnSend.setText("Enviar");
        cltBtnSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cltBtnSendActionPerformed(evt);
            }
        });

        cltTxtStatus.setEditable(false);
        cltTxtStatus.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        cltTxtStatus.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        cltTxtStatus.setFocusable(false);

        cltLblStatus.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        cltLblStatus.setText("Status:");

        cltPrg.setForeground(new java.awt.Color(0, 204, 51));
        cltPrg.setFocusable(false);

        cltTxaLog.setEditable(false);
        cltTxaLog.setBackground(new java.awt.Color(240, 240, 240));
        cltTxaLog.setColumns(20);
        cltTxaLog.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        cltTxaLog.setLineWrap(true);
        cltTxaLog.setRows(5);
        cltTxaLog.setWrapStyleWord(true);
        cltTxaLog.setFocusable(false);
        jScrollPane2.setViewportView(cltTxaLog);

        cltChkBatch.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        cltChkBatch.setText("Lote");

        javax.swing.GroupLayout cltPnlLayout = new javax.swing.GroupLayout(cltPnl);
        cltPnl.setLayout(cltPnlLayout);
        cltPnlLayout.setHorizontalGroup(
            cltPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cltPnlLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(cltPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2)
                    .addGroup(cltPnlLayout.createSequentialGroup()
                        .addComponent(cltLblIP)
                        .addGap(50, 50, 50)
                        .addComponent(cltTxtIP)
                        .addGap(18, 18, 18)
                        .addComponent(cltLblPort)
                        .addGap(18, 18, 18)
                        .addComponent(cltTxtPort, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, cltPnlLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(cltBtnConnect, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(166, 166, 166))
                    .addGroup(cltPnlLayout.createSequentialGroup()
                        .addComponent(cltLblDir)
                        .addGap(10, 10, 10)
                        .addComponent(cltTxtDir))
                    .addComponent(cltPrg, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(cltPnlLayout.createSequentialGroup()
                        .addComponent(cltLblSize)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(cltPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cltTxtFile, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, cltPnlLayout.createSequentialGroup()
                                .addComponent(cltTxtSize, javax.swing.GroupLayout.PREFERRED_SIZE, 414, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(cltCmbBytes, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(cltPnlLayout.createSequentialGroup()
                        .addComponent(cltBtnSelect, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(51, 51, 51)
                        .addComponent(cltChkBatch)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cltBtnSend, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(cltPnlLayout.createSequentialGroup()
                        .addGroup(cltPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cltLblFile)
                            .addGroup(cltPnlLayout.createSequentialGroup()
                                .addComponent(cltLblStatus)
                                .addGap(18, 18, 18)
                                .addComponent(cltTxtStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 513, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        cltPnlLayout.setVerticalGroup(
            cltPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cltPnlLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(cltPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cltLblIP)
                    .addComponent(cltTxtIP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cltLblPort)
                    .addComponent(cltTxtPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(cltPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cltLblDir)
                    .addComponent(cltTxtDir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(cltBtnConnect)
                .addGap(18, 18, 18)
                .addGroup(cltPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cltLblFile)
                    .addComponent(cltTxtFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(cltPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cltLblSize)
                    .addComponent(cltTxtSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cltCmbBytes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(cltPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cltBtnSelect)
                    .addComponent(cltBtnSend)
                    .addComponent(cltChkBatch))
                .addGap(18, 18, 18)
                .addGroup(cltPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cltLblStatus)
                    .addComponent(cltTxtStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(13, 13, 13)
                .addComponent(cltPrg, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("      Cliente      ", cltPnl);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 767, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    // <editor-fold defaultstate="collapsed" desc="Ações para o servidor">
    private void srvBtnStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_srvBtnStartActionPerformed
        if (srvBtnStart.getText().equals("LIGAR")) {
            srvStart();
        } else if (srvBtnStart.getText().equals("DESLIGAR")) {
            srvShutdown();
        }
    }//GEN-LAST:event_srvBtnStartActionPerformed

    private void srvCmbBytesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_srvCmbBytesActionPerformed
        if (!srvFiles.isEmpty()) {
            if (srvFiles.size() == 1) {
                srvTxtSize.setText(Utils.bytesTo(srvFiles.get(0).length(), srvCmbBytes.getSelectedItem().toString()));
            } else {
                srvTxtSize.setText(Utils.bytesTo(srvTotalSize, srvCmbBytes.getSelectedItem().toString()));
            }
        }
    }//GEN-LAST:event_srvCmbBytesActionPerformed

    private void srvBtnSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_srvBtnSelectActionPerformed
        if (!server.sendingFiles()) {
            if (!srvChkBatch.isSelected()) {
                try {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    if (!srvLastDir.isEmpty()) {
                        chooser.setCurrentDirectory(new File(srvLastDir));
                    }
                    chooser.setDialogTitle("Selecione o arquivo");

                    if (chooser.showOpenDialog(this) == JFileChooser.OPEN_DIALOG) {
                        srvFiles.clear();
                        srvFiles.add(chooser.getSelectedFile());
                        srvLastDir = chooser.getSelectedFile().getParent();
                        srvTxtFile.setText(srvFiles.get(0).getName());
                        srvTxtSize.setText(Utils.bytesTo(srvFiles.get(0).length(), srvCmbBytes.getSelectedItem().toString()));
                    }
                } catch (IndexOutOfBoundsException ex) {
                    ex.printStackTrace();
                }
            } else {
                this.setVisible(false);
                new AddFiles(this, "srv", srvFiles, srvLastDir).setVisible(true);
            }
        } else {
            Utils.msgBoxError(rootPane, "O servidor ainda está enviando arquivos, aguarde o termino da operação atual.");
        }
    }//GEN-LAST:event_srvBtnSelectActionPerformed

    private void srvBtnSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_srvBtnSendActionPerformed
        if (srvBtnStart.getText().equals("DESLIGAR") && !srvTxtClient.getText().isEmpty()) {
            try {
                server.sendFiles(srvFiles);
            } catch (SendFileException ex) {
                Utils.msgBoxError(rootPane, ex.getMessage());
            }
        } else {
            Utils.msgBoxError(rootPane, "O servidor deve estar ligado e um cliente conectado.");
        }
    }//GEN-LAST:event_srvBtnSendActionPerformed

    private void srvTxtDirMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_srvTxtDirMouseClicked
        if (srvBtnStart.getText().equals("LIGAR")) {
            srvTxtDir.setText(Utils.selectDir(this));
        }
    }//GEN-LAST:event_srvTxtDirMouseClicked

    private void srvTxtDirKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_srvTxtDirKeyPressed
        if (srvBtnStart.getText().equals("LIGAR") && evt.getKeyCode() == KeyEvent.VK_ENTER) {
            srvTxtDir.setText(Utils.selectDir(this));
        }
    }//GEN-LAST:event_srvTxtDirKeyPressed
    // </editor-fold>

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        saveProperties();
    }//GEN-LAST:event_formWindowClosing

    // <editor-fold defaultstate="collapsed" desc="Ações para o cliente">
    private void cltTxtDirMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cltTxtDirMouseClicked
        if (cltBtnConnect.getText().equals("CONECTAR")) {
            cltTxtDir.setText(Utils.selectDir(this));
        }
    }//GEN-LAST:event_cltTxtDirMouseClicked

    private void cltTxtDirKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cltTxtDirKeyPressed
        if (cltBtnConnect.getText().equals("CONECTAR") && evt.getKeyCode() == KeyEvent.VK_ENTER) {
            cltTxtDir.setText(Utils.selectDir(this));
        }
    }//GEN-LAST:event_cltTxtDirKeyPressed

    private void cltBtnConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cltBtnConnectActionPerformed
        if (cltBtnConnect.getText().equals("CONECTAR")) {
            cltConnect();
        } else if (cltBtnConnect.getText().equals("DESCONECTAR") || cltBtnConnect.getText().equals("CONECTANDO")) {
            cltDisconnect();
        }
    }//GEN-LAST:event_cltBtnConnectActionPerformed

    private void cltCmbBytesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cltCmbBytesActionPerformed
        if (!cltFiles.isEmpty()) {
            if (cltFiles.size() == 1) {
                cltTxtSize.setText(Utils.bytesTo(cltFiles.get(0).length(), cltCmbBytes.getSelectedItem().toString()));
            } else {
                cltTxtSize.setText(Utils.bytesTo(cltTotalSize, cltCmbBytes.getSelectedItem().toString()));
            }
        }
    }//GEN-LAST:event_cltCmbBytesActionPerformed

    private void cltBtnSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cltBtnSelectActionPerformed
        if (!client.sendingFiles()) {
            if (!cltChkBatch.isSelected()) {
                try {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    if (!cltLastDir.isEmpty()) {
                        chooser.setCurrentDirectory(new File(cltLastDir));
                    }
                    chooser.setDialogTitle("Selecione o arquivo");

                    if (chooser.showOpenDialog(this) == JFileChooser.OPEN_DIALOG) {
                        cltFiles.clear();
                        cltFiles.add(chooser.getSelectedFile());
                        cltLastDir = chooser.getSelectedFile().getParent();
                        cltTxtFile.setText(cltFiles.get(0).getName());
                        cltTxtSize.setText(Utils.bytesTo(cltFiles.get(0).length(), cltCmbBytes.getSelectedItem().toString()));
                    }
                } catch (IndexOutOfBoundsException ex) {
                    ex.printStackTrace();
                }
            } else {
                this.setVisible(false);
                new AddFiles(this, "clt", cltFiles, cltLastDir).setVisible(true);
            }
        } else {
            Utils.msgBoxError(rootPane, "O cliente ainda está enviando arquivos, aguarde o termino da operação atual.");
        }
    }//GEN-LAST:event_cltBtnSelectActionPerformed

    private void cltBtnSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cltBtnSendActionPerformed
        if (cltBtnConnect.getText().equals("DESCONECTAR")) {
            try {
                client.sendFiles(cltFiles);
            } catch (SendFileException e) {
                Utils.msgBoxError(rootPane, e.getMessage());
            }
        } else {
            Utils.msgBoxError(rootPane, "Conecte-se a um servidor.");
        }
    }//GEN-LAST:event_cltBtnSendActionPerformed
    // </editor-fold>

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
            java.util.logging.Logger.getLogger(MainGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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
                new MainGUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cltBtnConnect;
    private javax.swing.JButton cltBtnSelect;
    private javax.swing.JButton cltBtnSend;
    private javax.swing.JCheckBox cltChkBatch;
    private javax.swing.JComboBox<String> cltCmbBytes;
    private javax.swing.JLabel cltLblDir;
    private javax.swing.JLabel cltLblFile;
    private javax.swing.JLabel cltLblIP;
    private javax.swing.JLabel cltLblPort;
    private javax.swing.JLabel cltLblSize;
    private javax.swing.JLabel cltLblStatus;
    private javax.swing.JPanel cltPnl;
    private javax.swing.JProgressBar cltPrg;
    private javax.swing.JTextArea cltTxaLog;
    private javax.swing.JTextField cltTxtDir;
    private javax.swing.JTextField cltTxtFile;
    private javax.swing.JTextField cltTxtIP;
    private javax.swing.JTextField cltTxtPort;
    private javax.swing.JTextField cltTxtSize;
    private javax.swing.JTextField cltTxtStatus;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JButton srvBtnSelect;
    private javax.swing.JButton srvBtnSend;
    private javax.swing.JButton srvBtnStart;
    private javax.swing.JCheckBox srvChkBatch;
    private javax.swing.JComboBox<String> srvCmbBytes;
    private javax.swing.JLabel srvLblClient;
    private javax.swing.JLabel srvLblDir;
    private javax.swing.JLabel srvLblFile;
    private javax.swing.JLabel srvLblIP;
    private javax.swing.JLabel srvLblPort;
    private javax.swing.JLabel srvLblSize;
    private javax.swing.JLabel srvLblStatus;
    private javax.swing.JPanel srvPnl;
    private javax.swing.JProgressBar srvPrg;
    private javax.swing.JTextArea srvTxaLog;
    private javax.swing.JTextField srvTxtClient;
    private javax.swing.JTextField srvTxtDir;
    private javax.swing.JTextField srvTxtFile;
    private javax.swing.JTextField srvTxtIP;
    private javax.swing.JTextField srvTxtPort;
    private javax.swing.JTextField srvTxtSize;
    private javax.swing.JTextField srvTxtStatus;
    // End of variables declaration//GEN-END:variables
}
