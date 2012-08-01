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
package a1.net;

import static a1.Main.*;

import a1.Log;

import a1.Config;
import a1.Connection;
import a1.Main;
import a1.Packet;
import a1.dialogs.Dialog;
import a1.dialogs.dlg_Chars;

public class NetLogin {
	public static final int LOGINSERVER_HELO = 1;
	public static final int LOGINSERVER_PROTO = 2;
	public static final int LOGINSERVER_PROTO_ACK = 3;
	public static final int LOGINSERVER_AUTH = 4;
	public static final int LOGINSERVER_AUTH_ACK = 5;
	public static final int LOGINSERVER_GETCOOKIE = 6;
	public static final int LOGINSERVER_GETCOOKIE_ACK = 7;
	public static final int LOGINSERVER_BYE = 8;
	public static final int LOGINSERVER_GETCHARS = 9;
	public static final int LOGINSERVER_GETCHARS_ACK = 10;
	public static final int LOGINSERVER_PING = 11;
	
	public static String login = "";
	public static String pwd = "";
	public static String error_text = "";
	public static byte[] cookie = null;
	public static String game_server = "";
    public static int    game_server_port = 778;
	public static String login_state = "";
	
	
	static final int LOGIN_PING_TIME = 15000;
	
	public static int CharID = 0;
	public static long LastPingTime = 0;
	public static long Ping = 0;
	
	static public void ProcessPackets() {
		if (LoginConnect != null) {
			while (true) {
				Packet pkt = null;
				if (LoginConnect != null)
					synchronized (LoginConnect) {
						if (!LoginConnect.packets.isEmpty())
							pkt = LoginConnect.packets.removeLast();
						else
							break;
					}
				else
					break;
				if (pkt != null)
					ParsePacket(pkt);
			}
		}
		
		if ((login_state.equals("auth_ok") || login_state.equals("chars")) && LoginConnect != null && LoginConnect.Alive()) {
			if (System.currentTimeMillis() > LastPingTime + LOGIN_PING_TIME) {
				LastPingTime = System.currentTimeMillis();
				Packet p = new Packet(LOGINSERVER_PING);
				LoginConnect.Send(p);
			}
		}
	}

	static public void Error(String msg) {
		error_text = msg;
		Config.quick_login_mode = false;
		Main.ReleaseAll();
	}
	
	static void ParsePacket(Packet pkt) {
		int val;
		Packet p;
		switch (pkt.type) {
		case LOGINSERVER_HELO :
			val = pkt.read_int();
			if (val != -7756) {
				Error("wrong_helo");
				return;
			}
			cookie = null;
			login_state = "";
			LastPingTime = 0;
			p = new Packet(LOGINSERVER_PROTO);
			p.write_int(Config.PROTO_VERSION);
			LoginConnect.Send(p);
			break;
		case LOGINSERVER_PROTO_ACK :
			val = pkt.read_int();
			if (val == 1) {
				p = new Packet(LOGINSERVER_AUTH);
				p.write_string_ascii(login);
				p.write_string_ascii(pwd);
				LoginConnect.Send(p);
				error_text = "auth";	
			} else {
				Error("proto_version");
			}
			break;
		case LOGINSERVER_AUTH_ACK :
			val = pkt.read_int();
			if (val == 1) {
				login_state = "auth_ok";
				p = new Packet(LOGINSERVER_GETCHARS);
				LoginConnect.Send(p);
				error_text = "get_chars";			
			} else if (val == 2) {
				Error("user_not_found");
			} else if (val == 3) {
				Error("already_logged");		
			} else {
				Error("wrong_password");
			}
			break;
		case LOGINSERVER_GETCHARS_ACK :
			val = pkt.read_int();
			if (val == 0) {
				Error("no_chars");			
			} else {
				login_state = "chars";
				error_text = "";
				Dialog.HideAll();
				Dialog.Show("dlg_chars");

                // last char id
				if (dlg_Chars.dlg != null)
					dlg_Chars.dlg.last_char_id = pkt.read_int();
                else
                    pkt.read_int();

                // id + name
				String name;
				int id;
				for (int i = 0; i < val; i++) {
					id = pkt.read_int();
					name = pkt.read_string_ascii();
					dlg_Chars.AddChar(id, name);
					Log.debug("id: " +id+ " char: "+name);
				}
				dlg_Chars.CharsRecv();
			}
			break;
		case LOGINSERVER_GETCOOKIE_ACK : 
			val = pkt.read_int();
			if (val == 1) {
				cookie = pkt.read_bytes(16);
				game_server = pkt.read_string_ascii();
                game_server_port = pkt.read_word();
				Log.info("Game server: "+game_server + " : "+game_server_port);
				error_text = "OK!";
				Dialog.HideAll();
				LoginConnect.Close();
				LoginConnect = null;
				
				GameConnect = new Connection(game_server, game_server_port);
				NetGame.Reset();
				Main.GameState = 1;
			} else {
				Error("wrong_cookie");
			}
			break;
		}
		
	}
}
