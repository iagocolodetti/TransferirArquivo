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

import java.awt.Component;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author iagocolodetti
 */
public class Util {
    
    public static void msgBoxErro(Component component, String mensagem) {
        JOptionPane.showMessageDialog(component, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
    }
    
    public static String calcularTamanho(String tipo, long tamanhoArquivo) {
        String tamanho = "";
        switch (tipo) {
            case "B":
                tamanho = String.valueOf(tamanhoArquivo);
                break;
            case "KB":
                tamanho = String.valueOf(tamanhoArquivo / 1024);
                break;
            case "MB":
                tamanho = String.valueOf(tamanhoArquivo / 1024 / 1024);
                break;
            case "GB":
                tamanho = String.format("%.2f", (double) tamanhoArquivo / 1024 / 1024 / 1024);
                break;
            case "TB":
                tamanho = String.format("%.2f", (double) tamanhoArquivo / 1024 / 1024 / 1024 / 1024);
                break;
        }
        return tamanho;
    }
    
    public static String selecionarDir(Component component) {
        String dir = "";
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Selecione o diretório");
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(component) == JFileChooser.OPEN_DIALOG) {
            dir = chooser.getSelectedFile().toString().endsWith("\\")
                    ? chooser.getSelectedFile().toString()
                    : chooser.getSelectedFile().toString() + "\\";

        }
        return dir;
    }
}
