/*
 * This file is part of the Origin-World game client.
 * Copyright (C) 2012 Arkadiy Fattakhov <ark@ark.su>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package a1;

import a1.utils.Utils;
import com.ericsson.otp.erlang.OtpErlangDecodeException;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpInputStream;
import com.ericsson.otp.erlang.OtpOutputStream;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPInputStream;

public class Packet {
    public int type;
    public int len = 0;
    public byte[] blob;
    int offset = 0;
    
	public Packet(int type, byte[] blob) {
		this.type = type;
		this.len = blob.length;
		this.blob = blob;
    }

    public Packet(int type, int len) {
		this.type = type;
		this.len = len;
		this.offset = 0;
		blob = new byte[len];
    }    
    
    public Packet(int type) {
		this.type = type;
		this.len = 0;
		blob = new byte[0];
    }      
    
    public byte[] add_data(byte[] buf, int offset, int len) {
    	if (len <= (this.len - this.offset)) {
    		System.arraycopy(buf, offset, this.blob, this.offset, len);
    		this.offset += len;
    		return null;
    	} else {
    		int ret_len = len - (this.len - this.offset);
    		System.arraycopy(buf, offset, this.blob, this.offset, this.len - this.offset);
    		byte[] ret_buf = new byte[ret_len];
    		System.arraycopy(buf, offset+(this.len - this.offset), ret_buf, 0, ret_len);
    		this.offset = this.len;
    		return ret_buf;
    	}
    }
    
    public boolean is_ready() {
    	return offset == len;
    }
    
    public boolean equals(Object pckt) {
    	if(!(pckt instanceof Packet))
    	    return(false);
    	Packet p2 = (Packet)pckt;
    	if(p2.blob.length != blob.length)
    	    return(false);
    	for(int i = 0; i < blob.length; i++) {
    	    if(p2.blob[i] != blob[i])
    		return(false);
    	}
    	return(true);
    }   
    
    public Packet clone() {
    	return(new Packet(type, blob));
    }

    public void Send(Connection conn) {
		if (conn != null) 
			conn.Send(this);
	}

    // WRITE METHODS -------------------------------------------------------
    
    public void write_bytes(byte[] src, int offset, int len) {
    	byte[] n = new byte[blob.length + len];
    	System.arraycopy(blob, 0, n, 0, blob.length);
    	System.arraycopy(src, offset, n, blob.length, len);
    	blob = n;
    }

    public void write_bytes(byte[] src) {
    	write_bytes(src, 0, src.length);
    }
    	
    public void write_byte(byte b) { // signed!
    	write_bytes(new byte[] {b});
    }

    public void write_int(int num) {
        byte[] buf = new byte[4];

        buf[0] = (byte) (num &0xff);
        buf[1] = (byte) (num >> 8 &0xff);
        buf[2] = (byte) (num >> 16 &0xff);
        buf[3] = (byte) (num >> 24 &0xff);
    	write_bytes(buf);
    }

    public void write_word(int num) {
        byte[] buf = new byte[2];

        buf[0] = (byte) (num &0xff);
        buf[1] = (byte) (num >> 8 &0xff);
        write_bytes(buf);
    }

    public void write_string_utf(String str) {
    	byte[] buf;
    	try {
    	    buf = str.getBytes("utf-8");
    	} catch(java.io.UnsupportedEncodingException e) {
    	    throw(new RuntimeException(e));
    	}
        write_word(buf.length);
    	write_bytes(buf);
    }

    public void write_string_ascii(String str) {
    	byte[] buf;
    	try {
    	    buf = str.getBytes("US-ASCII");
    	} catch(java.io.UnsupportedEncodingException e) {
    	    throw(new RuntimeException(e));
    	}
    	write_word(buf.length);
    	write_bytes(buf);
    }

    // READ FUNCTIONS ------------------------------------------------------------
    
    public boolean eof() {
    	return(offset >= blob.length);
    }
    
    public void read_skip(int val) {
        offset += val;
    }
    	
    public int read_signed_byte() {
    	return(blob[offset++]);
    }

    public int read_byte() {
    	return(Utils.unsigned_byte(blob[offset++]));
    }

    public int read_int() {
        offset +=4;
        return(  Utils.unsigned_byte(blob[offset - 4]) +
                (Utils.unsigned_byte(blob[offset - 3]) * 256) +
                (Utils.unsigned_byte(blob[offset - 2]) * 65536) +
                (Utils.unsigned_byte(blob[offset - 1]) * 16777216));
    }

    public int read_word() {
        offset +=2;
        return(  Utils.unsigned_byte(blob[offset - 2]) +
                (Utils.unsigned_byte(blob[offset - 1]) * 256));
    }

    public String read_string_ascii() {
    	int len = read_word();
    	offset += len;
    	String s = "";
    	if (len <= 0) return s;
    	
    	try {
    	    s = new String(blob, offset-len, len, "US-ASCII");
    	} catch(UnsupportedEncodingException e) {
    	    throw(new RuntimeException(e));
    	}    	   	
    	return s;
    }
    
    public String read_string_utf() {
    	int len = read_word();
    	offset += len;
    	String s = "";
    	if (len <= 0) return s;
    	
    	try {
    	    s = new String(blob, offset-len, len, "utf-8");
    	} catch(UnsupportedEncodingException e) {
    	    throw(new RuntimeException(e));
    	}    	   	
    	return s;
    }
    
    public OtpErlangObject read_erlang_term() {
    	int len = read_int();
    	byte [] data = read_bytes(len);
    	try {
			return OtpErlangObject.decode(new OtpInputStream(data));
		} catch (OtpErlangDecodeException e) {
			e.printStackTrace();
			return null;
		}
    }

    public void write_erlang_term(OtpErlangObject term) {
        OtpOutputStream outs = new OtpOutputStream(term);
        byte [] data = outs.toByteArray();
        write_int(data.length+1);
        // its a magic!
        write_byte((byte) (131 &0xff));
        write_bytes(data);
    }

    public byte[] read_bytes(int len) {
    	byte[] b = new byte[len];
    	offset += len;
    	System.arraycopy(blob, offset-len, b, 0, len);
    	return b;
    }
    
    public byte[] read_map_data() {
        int dl = read_int();
    	byte[] b = new byte[dl];
    	System.arraycopy(blob, offset, b, 0, dl);
    	try {
			GZIPInputStream gz = new GZIPInputStream(new ByteArrayInputStream(b));
			byte[] map = new byte[MapCache.GRID_SIZE_BYTES];
            
			int fl = 0;
            while (fl < MapCache.GRID_SIZE_BYTES) {
                fl += gz.read(map, fl, MapCache.GRID_SIZE_BYTES-fl);
            }
			return map;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
    }

    public Color read_color() {
    	return(new Color(read_byte(), read_byte(), read_byte(), read_byte()));
    }

    public String toString() {
        if (!Config.debug) return "";
        String ret;
        if (len < 100) {
            ret = " data: ";
            for (byte b : blob) {
                ret += String.format("%02x ", b);
            }
        } else
            ret = "";
        return("pkt (" + type + ") len=" + len + ret);
    }
}
