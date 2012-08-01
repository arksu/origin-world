/*
 *  This file is part of the Origin-World game client.
 *  Copyright (C) 2012 Arkadiy Fattakhov <ark@ark.su>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, version 3 of the License.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package a1;

import java.io.DataInputStream;
import java.io.IOException;

import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.LinkedList;

import a1.utils.MyThread;
import a1.utils.Utils;

public class Connection {
    enum ConnectionState {
        CONNECTING,
        CONNECTED,
        CLOSED,
        DEAD
    }

    public static final int ERROR_CONNECT = 1;
    public static final int ERROR_READ = 2;
    public static final int ERROR_INTERNAL = 3;
    public static final int ERROR_TIMEOUT = 4;

    public static final int HEADER_SIZE = 3;

    private int server_port;
    Socket socket = null;
    String server_addr;
    public ConnectionState state = ConnectionState.CONNECTING;
    public int network_error = 0;
    public LinkedList<Packet> packets;

    RecvWorker recv_worker;


    public Connection(String host, int port) {
        server_port = port;
        server_addr = host;

        packets = new LinkedList<Packet>();

        recv_worker = new RecvWorker();
        recv_worker.start();
    }

    private class RecvWorker extends MyThread {
        boolean alive;
        byte[] read_buffer = new byte[65535];

        boolean is_header = true;
        Packet cur_packet = null;
        byte [] left_buf = null;

        public RecvWorker() {
            super("RECV thread");
            setDaemon(true);
        }

        private void ProcessData(int readed) {
            byte[] buf;
            if (left_buf != null) {
                buf = new byte[left_buf.length + readed];
                System.arraycopy(left_buf, 0, buf, 0, left_buf.length);
                System.arraycopy(read_buffer, 0, buf, left_buf.length, readed);
                left_buf = null;
            } else {
                buf = new byte[readed];
                System.arraycopy(read_buffer, 0, buf, 0, readed);
            }
            //Log.info("process data readed="+readed);
            while (true) {

                //if (buf != null)
                //Log.info("buf len="+buf.length);
                //else
                //Log.info("buf null");


                if (is_header) {
                    //Log.info("is header");

                    if ((buf != null && buf.length < HEADER_SIZE) || buf == null) {
                        left_buf = buf;
                        break;
                    }
                    int data_len = Utils.uint16d(buf, 0)-HEADER_SIZE;
                    int type = Utils.unsigned_byte(buf[2]);
                    if (Config.debug_packets)
                        Log.debug("new pkt: ["+type+"] len="+data_len);
                    cur_packet = new Packet(type, data_len);
                    buf = cur_packet.add_data(buf, HEADER_SIZE, buf.length- HEADER_SIZE);
                    if (cur_packet.is_ready()) {
                        synchronized(Connection.this) {
                            if (Config.debug_packets) Log.info("recv packet "+cur_packet.toString());
                            cur_packet.offset = 0;
                            synchronized (packets) {
                                packets.addFirst(cur_packet);
                            }
                        }
                        cur_packet = null;
                    } else {
                        is_header = false;
                    }
                } else {
                    //Log.info("not is header");
                    if (buf == null) {
                        left_buf = buf;
                        break;
                    }
                    buf = cur_packet.add_data(buf, 0, buf.length);
                    if (cur_packet.is_ready()) {
                        synchronized(Connection.this) {
                            //Log.info("recv packet "+cur_packet.toString());
                            cur_packet.offset = 0;
                            packets.addFirst(cur_packet);
                        }
                        cur_packet = null;
                        is_header = true;
                    }
                }
            }
        }

        public void run() {
            try {
                alive = true;
                try {
                    socket = new Socket(server_addr, server_port);
                } catch(IOException e) {
                    synchronized(Connection.this) { network_error = ERROR_CONNECT;}
                    throw(new RuntimeException("error create socket"));
                }
                try { socket.setSoTimeout(1000); }
                catch(SocketException e) {
                    synchronized(Connection.this) { network_error = ERROR_INTERNAL; }
                    throw(new RuntimeException("error socket.setSoTimeout"));
                }
                DataInputStream input;
                try { input = new DataInputStream(socket.getInputStream()); }
                catch (IOException e) {
                    synchronized(Connection.this) { network_error = ERROR_INTERNAL; }
                    throw(new RuntimeException("error socket.getInputStream"));
                }
                synchronized(Connection.this) {
                    state = ConnectionState.CONNECTED;
                }
                int readed;

                while(alive) {
                    try {
                        readed = input.read(read_buffer);
                        Main.DataRecieved += readed;
                    } catch (SocketTimeoutException e) {
                        continue;
                    } catch (IOException e) {
                        synchronized(Connection.this) { network_error = ERROR_READ; }
                        throw(new RuntimeException("error input.read(buffer)"));
                    }
                    if (readed < 0) {
                        synchronized(Connection.this) { network_error = ERROR_READ; }
                        throw(new RuntimeException("readed -1 count from socket"));
                    }
                    ProcessData(readed);

                }
            } finally {
                synchronized(Connection.this) {
                    state = ConnectionState.DEAD;
                    try {
                        if (socket != null)
                            socket.close();
                    } catch (IOException e) {
                    }
                    Connection.this.notifyAll();
                }
            }
        }

        public void interrupt() {
            alive = false;
            super.interrupt();
        }
    }

    static public void PrintData(byte[] data, int len) {
        Log.info("readed count: "+Integer.toString(len));
//		String s = "";
//    	for(int i = 0; i < len; i++) {
//    	    s += String.format("%02x ", data[i]);
//    	}
//    	Log.info("data: "+s);
    }

    public boolean Alive() {
        return (state != ConnectionState.DEAD);
    }

    static public String GetErrorReason(int reason) {
        switch (reason) {
            case ERROR_CONNECT:		return "cant_connect";
            case ERROR_INTERNAL:	return "internal_error";
            case ERROR_READ:		return "network_read_error";
            case ERROR_TIMEOUT:		return "timeout_connection";
            default : 				return "unknown";
        }
    }

    public String GetStateDesc() {
        switch (state) {
            case CONNECTING : return "connecting";
            case CLOSED : return "closed";
            case CONNECTED : return "connected";
            case DEAD : return "dead";
            default : return "unknown";
        }
    }

    public void Close() {
        recv_worker.interrupt();
    }

    public void Send(Packet pkt) {
        // old code
//    	byte[] buf = new byte[pkt.blob.length + 4];
//    	Utils.uint32e(pkt.type, buf, 0);
//    	System.arraycopy(pkt.blob, 0, buf, 4, pkt.blob.length);

        byte[] buf = new byte[pkt.blob.length + 3];
        buf[0] = (byte) ((pkt.blob.length + 2) &0xff);
        buf[1] = (byte) ((pkt.blob.length + 2) >> 8 &0xff);
        buf[2] = (byte) pkt.type;
        System.arraycopy(pkt.blob, 0, buf, 3, pkt.blob.length);
        if (Config.debug_packets)
            Log.debug("send pkt: "+Utils.getHexString(buf));
        Send(buf);
    }

    public void Send(byte[] data) {
        try {
            Main.DataSended += data.length;
            socket.getOutputStream().write(data);
            socket.getOutputStream().flush();
        } catch(IOException e) { }
    }
}
