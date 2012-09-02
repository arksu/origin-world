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
package a1.utils;

import a1.Config;
import a1.Log;
import com.ericsson.otp.erlang.*;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Utils {
    public static byte signed_byte(int b) {
    	if(b > 127)
    	    return((byte)(-256 + b));
    	else
    	    return((byte)b);
    }
    
    // вернуть байт к целому
    public static int unsigned_byte(byte b) {
    	return b & 0xff;
    }    
    
    // unsigned int16 encode
    public static void uint16e(int num, byte[] buf, int off) {
    	buf[off] = signed_byte(num & 0xff);
    	buf[off + 1] = signed_byte((num & 0xff00) >> 8);
    }
    
    // unsigned int32 encode
    public static void uint32e(long num, byte[] buf, int off) {
    	buf[off + 3] = signed_byte((int)( num & 0xff));
    	buf[off + 2] = signed_byte((int)((num & 0xff00) >> 8));
    	buf[off + 1] = signed_byte((int)((num & 0xff0000) >> 16));
    	buf[off + 0] = signed_byte((int)((num & 0xff000000) >> 24));
    }
    
    // int32 decode
    public static int int32d(byte[] buf, int off) {
    	long u = uint32d(buf, off);
    	if(u > Integer.MAX_VALUE)
    	    return((int)((((long)Integer.MIN_VALUE) * 2) - u));
    	else
    	    return((int)u);
    }
    	
    // кодируем инт для передачи на сервер    
    public static void int32e(int num, byte[] buf, int off) {
    	if(num < 0)
    	    uint32e(0x100000000L + ((long)num), buf, off);
    	else
    	    uint32e(num, buf, off);
    }
        
    // unsigned int16 decode
    public static int uint16d(byte[] buf, int off) {
    	return(unsigned_byte(buf[off]) + (unsigned_byte(buf[off + 1]) * 256));
    }
    	
    public static int int16d(byte[] buf, int off) {
    	int u = uint16d(buf, off);
    	if(u > 32767)
    	    return(-65536 + u);
    	else
    	    return(u);
    }
    	
    // unsigned int32 decode
    public static long uint32d(byte[] buf, int off) {
        	return(  unsigned_byte(buf[off + 3]) + 
        			(unsigned_byte(buf[off + 2]) * 256) + 
        			(unsigned_byte(buf[off + 1]) * 65536) + 
        			(unsigned_byte(buf[off + 0]) * 16777216));
    } 
 
    
//    public static String strd(byte[] buf, int[] off) {
//    	int i;
//    	for(i = off[0]; buf[i] != 0; i++);
//    	String ret;
//    	try {
//    	    ret = new String(buf, off[0], i - off[0], "utf-8");
//    	} catch(UnsupportedEncodingException e) {
//    	    throw(new RuntimeException(e));
//    	}
//    	off[0] = i + 1;
//    	return(ret);
//    }
    public static int max(int a, int b) {
    	return a>b?a:b;
    }
    
    public static int min(int a, int b) {
    	return a<b?a:b;
    }
    
    // ******** Screenshot Section Begin ********//
	private static ByteBuffer allocBytes(int howmany) {
    	return ByteBuffer.allocateDirect(howmany * 1).order(ByteOrder.nativeOrder());
    }
	
	private static int[] flipPixels(int[] imgPixels, int imgw, int imgh)
    {
        int[] flippedPixels = null;
        if (imgPixels != null) {
            flippedPixels = new int[imgw * imgh];
            for (int y = 0; y < imgh; y++) {
                for (int x = 0; x < imgw; x++) {
                    flippedPixels[ ( (imgh - y - 1) * imgw) + x] = imgPixels[ (y * imgw) + x];
                }
            }
        }
        return flippedPixels;
    }
	
    public static void MakeScreenshot() {
    	// Set screen size
		int width = Config.getScreenWidth();
		int height = Config.getScreenHeight();
        // allocate space for RBG pixels
        ByteBuffer framebytes = allocBytes(width * height * 3);
        int[] pixels = new int[width * height];
        int bindex;
        // grab a copy of the current frame contents as RGB (has to be UNSIGNED_BYTE or colors come out too dark)
        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, framebytes);
        // copy RGB data from ByteBuffer to integer array
        for (int i = 0; i < pixels.length; i++) {
            bindex = i * 3;
            pixels[i] =
                0xFF000000                                          // A
                | ((framebytes.get(bindex) & 0x000000FF) << 16)     // R
                | ((framebytes.get(bindex+1) & 0x000000FF) << 8)    // G
                | ((framebytes.get(bindex+2) & 0x000000FF) << 0);   // B
        }
        // free up this memory
        framebytes = null;
        // flip the pixels vertically (opengl has 0,0 at lower left, java is upper left)
        pixels = flipPixels(pixels, width, height);
        try {
            // Create a BufferedImage with the RGB pixels then save as PNG
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            image.setRGB(0, 0, width, height, pixels, 0, width);
            
            // Generate filename
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String filename = sdf.format(cal.getTime());
            // Check directory
            File dir = new File("screenshots");
            if (!dir.exists()) dir.mkdir();
            
            javax.imageio.ImageIO.write(image, "png", new File("screenshots\\" + filename + ".png"));
        }
        catch (Exception e) {
            Log.info("GLApp.screenShot(): exception " + e);
        }
    }
	
	// ******** Screenshot Section End ********//
    
	public static String data2string(long data) {
		long bufd = data;
		StringBuffer buf = new StringBuffer();
//		if (bufd > 1024 * 1024 * 1024) {
//			buf.append(bufd / (1024 * 1024 * 1024));
//			buf.append("Gb ");
//			bufd /= 1024;
//		}
//		if (bufd > 1024 * 1024) {
//			buf.append(bufd / (1024 * 1024));
//			buf.append("M ");
//			bufd /= 1024;
//		}
		if (bufd > 1024) {
			buf.append(bufd / (1024));
			buf.append(" k ");
			bufd /= 1024;
		} else {
			buf.append(bufd);
			buf.append(" b");
		}
		return buf.toString();
	}
	
	public static int ErlangInt(OtpErlangObject e) {
		if (e instanceof OtpErlangLong) {
			try {
				return ((OtpErlangLong) e).intValue();
			} catch (OtpErlangRangeException e1) {
				e1.printStackTrace();
				return 0;
			}
		}
		
		return 0;
	}
	
	public static String ErlangAtom(OtpErlangObject e) {
		if (e instanceof OtpErlangAtom) {
			return ((OtpErlangAtom) e).atomValue();
		}
		return "";
	}

    public static String getHexString(byte[] b) {
        String result = "";
        for (int i=0; i < b.length; i++) {
            result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 ) + " ";
        }
        return result;
    }

    public static String getErlangString(OtpErlangObject term) {
        if (term instanceof OtpErlangString) {
            return ((OtpErlangString)term).stringValue();
        }
        if (term instanceof OtpErlangList) {
            OtpErlangList list = (OtpErlangList)term;
            try {
                return list.stringValue();
            } catch (OtpErlangException e) {
                e.printStackTrace();
            }
        }
        return "error";
    }
}
