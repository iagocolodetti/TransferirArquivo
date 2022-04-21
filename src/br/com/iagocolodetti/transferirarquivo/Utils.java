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
public class Utils {
    
    public static void msgBoxError(Component component, String message) {
        JOptionPane.showMessageDialog(component, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }
    
    public static String bytesTo(long bytes, String bytesTo) {
        String size = "";
        switch (bytesTo) {
            case "B":
                size = String.valueOf(bytes);
                break;
            case "KB":
                size = String.valueOf(bytes / 1024);
                break;
            case "MB":
                size = String.valueOf(bytes / 1024 / 1024);
                break;
            case "GB":
                size = String.format("%.2f", (double) bytes / 1024 / 1024 / 1024);
                break;
            case "TB":
                size = String.format("%.2f", (double) bytes / 1024 / 1024 / 1024 / 1024);
                break;
        }
        return size;
    }
    
    public static String selectDir(Component component) {
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
