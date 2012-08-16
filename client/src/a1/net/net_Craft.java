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
package a1.net;

import a1.Lang;
import a1.Main;
import a1.Packet;
import a1.dialogs.dlg_Game;
import a1.gui.*;

import static a1.Main.GameConnect;
import static a1.net.NetPacketsID.GAMESERVER_CRAFT_CLICK;
import static a1.net.NetPacketsID.GAMESERVER_CRAFT_LIST;

public class net_Craft extends NetHandler {
	
	public static boolean PacketHandler(Packet pkt) {
		switch (pkt.type) {
			case GAMESERVER_CRAFT_LIST:
				String CraftName = pkt.read_string_ascii();
				// ищем такое окошко в списке. если нашли - выходим.
				for (GUI_Window w : dlg_Game.dlg.craft_wnds) {
					if (w.tag.equals(CraftName)) {
						w.BringToFront();
						return true;
					}
				}
				
				GUI_Window wnd = new GUI_Window(GUI.getInstance().normal) {
					protected void DoClose() {
						dlg_Game.dlg.craft_wnds.remove(this);
					}
				};
				dlg_Game.dlg.craft_wnds.add(wnd);
				
				wnd.caption = Lang.getTranslate("generic", "craft") + " " + Lang.getTranslate("server", CraftName);
				wnd.SetSize(300, 200);
				wnd.tag = CraftName;
				wnd.Center();
				
				// создаем кнопки крафта
				GUI_Button btn = new GUI_Button(wnd) {
					public void DoClick() {
						SendClick(this.tag, "one");
					}
				};
				btn.tag = CraftName;
				btn.SetPos(30,150);
				btn.SetSize(100,20);
				btn.caption = Lang.getTranslate("generic", "craft");

				btn = new GUI_Button(wnd) {
					public void DoClick() {
						SendClick(this.tag, "all");
					}
				};
				btn.tag = CraftName;
				btn.SetPos(180,150);
				btn.SetSize(100,20);
				btn.caption = Lang.getTranslate("generic", "craft_all");
				
				int final_count, req_count, count;
				String image_name;
				String hint;
				final_count = pkt.read_int();
				int ax, ay;
				ax = 30; ay = 30;
				while (final_count > 0) {
					final_count--;
					image_name = pkt.read_string_ascii();
					hint = pkt.read_string_ascii();
					count = pkt.read_int();
					
					GUI_Image img = new GUI_Image(wnd);
					img.skin_element = "icon_"+image_name;
					img.simple_hint = Lang.getTranslate("server", hint);
					img.SetSize(Main.skin.GetElementSize(img.skin_element));
					img.SetPos(ax,ay);
					ax += 40;
					
					GUI_Label lbl = new GUI_Label(img);
					lbl.caption = String.valueOf(count);
					lbl.SetPos(10,10);
				}
				
				ax = 30; ay = 80;
				req_count = pkt.read_int();
				while (req_count > 0) {
					req_count--;
					image_name = pkt.read_string_ascii();
					hint = pkt.read_string_ascii();
					count = pkt.read_int();
					
					GUI_Image img = new GUI_Image(wnd);
					img.skin_element = "icon_"+image_name;
					img.simple_hint = Lang.getTranslate("server", hint);
					img.SetSize(Main.skin.GetElementSize(img.skin_element));
					img.SetPos(ax,ay);
					ax += 40;
					
					GUI_Label lbl = new GUI_Label(img);
					lbl.caption = String.valueOf(count);
					lbl.SetPos(10,10);					
				}
				return true;
		}
		
		return false;
	}
	
	public static void SendClick(String CraftName, String Tag) {
		Packet p = new Packet(GAMESERVER_CRAFT_CLICK);
		p.write_string_ascii(CraftName);
		p.write_string_ascii(Tag);
		p.Send(GameConnect);
	}
}
