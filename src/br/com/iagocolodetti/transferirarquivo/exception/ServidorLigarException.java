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
package br.com.iagocolodetti.transferirarquivo.exception;

/**
 *
 * @author iagocolodetti
 */
public class ServidorLigarException extends Exception {

    private int code;
    
    public ServidorLigarException() {
        super("Não foi possível ligar o servidor.");
    }

    public ServidorLigarException(String msg) {
        super(msg);
    }
    
    public ServidorLigarException(String msg, int code) {
        super(msg);
        this.code = code;
    }
    
    public int getErrorCode() {
        return code;
    }
}
