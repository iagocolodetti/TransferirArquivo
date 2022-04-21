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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * @author iagocolodetti
 */
public class Client {

    private final int MIN_PORT = 0;
    private final int MAX_PORT = 65535;
    
    private MainGUI mainGUI = null;

    private Socket socket = null;

    private volatile boolean connected = false;

    private boolean receivingFile = false;
    private Thread sendingFile = null;

    private final int CONNECT_TIMEOUT = 5000;
    private final int MAX_RECONNECT_TRIES = 5;
    private final int WAIT_TO_RECONNECT = 500;
    private final int WAIT_AFTER_SEND = 2000;
    private final int WAIT_AFTER_SEND_LOOP = 10;

    public Client(MainGUI mainGUI) {
        this.mainGUI = mainGUI;
    }

    public void connect(String ip, int port, String dir) throws ClientConnectException {
        if (ip.isEmpty()) {
            throw new ClientConnectException("Entre com o IP do servidor.");
        }
        if (!Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$").matcher(ip).matches()) {
            throw new ClientConnectException("Digite um IP válido.");
        }
        if (port < MIN_PORT || port > MAX_PORT) {
            throw new ClientConnectException("A porta deve ser no mínimo " + MIN_PORT + " e no máximo " + MAX_PORT + ".");
        }
        if (dir.isEmpty()) {
            throw new ClientConnectException("Selecione um diretório para recebimento de arquivos.", 1);
        } else {
            try {
                new File(dir).mkdirs();
            } catch (SecurityException ex) {
                throw new ClientConnectException("O diretório selecionado não existe e não foi possível criá-lo.\nSelecione outro diretório ou tente iniciar a aplicação como administrador.");
            }
        }
        mainGUI.cltConnecting();
        Thread t = new Thread(new ClientConnect(ip, port, dir));
        t.start();
    }

    public void disconnect() {
        try {
            connected = false;
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            socket = null;
            mainGUI.cltDisconnected();
        }
    }

    public void sendFiles(List<File> files) throws SendFileException {
        if (!isSocketConnected()) {
            throw new SendFileException("É necessário estar conectado a um servidor para enviar arquivos.");
        }
        if (files == null || files.isEmpty()) {
            throw new SendFileException("Selecione pelo menos um arquivo para enviar.");
        }
        if (receivingFile || sendingFiles()) {
            throw new SendFileException("Uma transferência já está em andamento.");
        }
        sendingFile = new Thread(new SendFiles(files));
        sendingFile.start();
    }

    public boolean sendingFiles() {
        return (sendingFile != null && sendingFile.isAlive());
    }
    
    private boolean isSocketConnected() {
        return (socket != null && !socket.isClosed() && socket.isConnected());
    }
    
    private void threadSleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    private class ClientConnect implements Runnable {

        private final String ip;
        private final int port;
        private final String dir;

        public ClientConnect(String ip, int port, String dir) {
            connected = true;
            this.ip = ip;
            this.port = port;
            this.dir = dir;
        }

        @Override
        public void run() {
            while (connected) {
                receivingFile = false;
                if (!isSocketConnected()) {
                    threadSleep(socket == null ? 0 : WAIT_TO_RECONNECT);
                    int tries = 0;
                    while (connected && tries < MAX_RECONNECT_TRIES) {
                        try {
                            socket = null;
                            socket = new Socket(ip, port);
                            mainGUI.cltConnected();
                            break;
                        } catch (IllegalArgumentException ex) {
                            Utils.msgBoxError(mainGUI, "IP e/ou porta incorreto(s).");
                            disconnect();
                            ex.printStackTrace();
                            break;
                        } catch (IOException ex) {
                            tries++;
                            ex.printStackTrace();
                            threadSleep(CONNECT_TIMEOUT);
                        }
                    }
                    if (tries == MAX_RECONNECT_TRIES) {
                        Utils.msgBoxError(mainGUI, "Não foi possível conectar-se a esse servidor.");
                        disconnect();
                    }
                }
                if (connected) {
                    InputStream is = null;
                    DataInputStream dis = null;
                    OutputStream out = null;
                    try {
                        is = socket.getInputStream();
                        dis = new DataInputStream(is);
                        String[] data = dis.readUTF().split(Pattern.quote("|"));
                        receivingFile = true;
                        threadSleep(1000);
                        String newFileName = data[0];
                        for (int i = 1; new File(dir + newFileName).exists(); i++) {
                            newFileName = data[0].substring(0, data[0].lastIndexOf(".")) + "(" + i + ")" + data[0].substring(data[0].lastIndexOf("."));
                        }
                        long size = Long.parseLong(data[1]);
                        long sizekb = (data[1].equals("0") ? 1 : (size / 1024));
                        sizekb = (sizekb == 0 ? 1 : sizekb);
                        is = socket.getInputStream();
                        out = new FileOutputStream(dir + newFileName);
                        mainGUI.cltSetStatus("Recebendo Arquivo");
                        mainGUI.cltAddTextAreaLog("Recebendo Arquivo: \"" + newFileName + "\".");
                        mainGUI.cltSetProgressPainted(true);
                        byte[] buffer = new byte[1024];
                        long receivedBytes = 0;
                        int count;
                        long kb = 0;
                        int progress;
                        while ((count = is.read(buffer)) > 0) {
                            out.write(buffer, 0, count);
                            receivedBytes += count;
                            progress = (int)(++kb * 100 / sizekb);
                            mainGUI.cltSetProgress(progress < 100 ? progress : 100);
                        }
                        if (receivedBytes == size) {
                            mainGUI.cltAddTextAreaLog("Arquivo \"" + newFileName + "\" recebido com sucesso.");
                        } else {
                            mainGUI.cltAddTextAreaLog("Arquivo \"" + newFileName + "\" corrompido durante o recebimento.");
                            disconnect();
                            Utils.msgBoxError(mainGUI, "Conexão com o servidor perdida.");
                        }
                    } catch (FileNotFoundException ex) {
                        if (connected && !sendingFiles()) {
                            disconnect();
                            Utils.msgBoxError(mainGUI, "O diretório selecionado não foi encontrado.");
                            ex.printStackTrace();
                        }
                    } catch (IOException ex) {
                        if (connected && !sendingFiles()) {
                            disconnect();
                            Utils.msgBoxError(mainGUI, "Conexão com o servidor perdida.");
                            ex.printStackTrace();
                        }
                    } finally {
                        try {
                            if (out != null) {
                                out.close();
                            }
                            if (is != null) {
                                is.close();
                            }
                            if (dis != null) {
                                dis.close();
                            }
                            if (socket != null) {
                                socket.close();
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        } finally {
                            mainGUI.cltSetStatus("");
                            mainGUI.cltSetProgress(0);
                            mainGUI.cltSetProgressPainted(false);
                        }
                    }
                }
            }
        }
    }

    private class SendFiles implements Runnable {

        private final List<File> files;

        public SendFiles(List<File> files) {
            this.files = files;
        }

        @Override
        public void run() {
            for (File file : files) {
                int loop = 0;
                while (!isSocketConnected() && loop < WAIT_AFTER_SEND_LOOP) {
                    loop++;
                    threadSleep(WAIT_AFTER_SEND);
                }
                if (loop == WAIT_AFTER_SEND_LOOP) {
                    Utils.msgBoxError(mainGUI, "Não foi possível enviar os arquivos restantes.");
                    break;
                }
                if (file.exists()) {
                    DataOutputStream dos = null;
                    FileInputStream fis = null;
                    OutputStream out = null;
                    try {
                        dos = new DataOutputStream(socket.getOutputStream());
                        dos.writeUTF(file.getName() + "|" + file.length());
                        threadSleep(1000);
                        fis = new FileInputStream(file);
                        out = socket.getOutputStream();
                        mainGUI.cltSetStatus("Enviando Arquivo");
                        mainGUI.cltAddTextAreaLog("Enviando Arquivo: \"" + file.getName() + "\".");
                        mainGUI.cltSetProgressPainted(true);
                        byte[] buffer = new byte[1024];
                        long sendedBytes = 0;
                        int count;
                        long sizekb = (file.length() / 1024);
                        sizekb = (sizekb == 0 ? 1 : sizekb);
                        long kb = 0;
                        int progress;
                        while ((count = fis.read(buffer)) > 0) {
                            out.write(buffer, 0, count);
                            sendedBytes += count;
                            progress = (int)(++kb * 100 / sizekb);
                            mainGUI.cltSetProgress(progress < 100 ? progress : 100);
                        }
                        if (sendedBytes == file.length()) {
                            mainGUI.cltAddTextAreaLog("Arquivo \"" + file.getName() + "\" enviado com sucesso.");
                        } else {
                            mainGUI.cltAddTextAreaLog("Arquivo \"" + file.getName() + "\" corrompido durante o envio.");
                        }
                    } catch (IOException ex) {
                        if (connected) {
                            disconnect();
                            Utils.msgBoxError(mainGUI, "Conexão com o servidor perdida.");
                            ex.printStackTrace();
                        }
                    } finally {
                        try {
                            if (out != null) {
                                out.close();
                            }
                            if (fis != null) {
                                fis.close();
                            }
                            if (dos != null) {
                                dos.close();
                            }
                            if (socket != null) {
                                socket.close();
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        } finally {
                            mainGUI.cltSetStatus("");
                            mainGUI.cltSetProgress(0);
                            mainGUI.cltSetProgressPainted(false);
                        }
                    }
                } else {
                    mainGUI.cltAddTextAreaLog("Arquivo: \"" + file.getName() + "\" não foi encontrado.");
                }
                threadSleep(WAIT_AFTER_SEND);
            }
        }
    }
}
