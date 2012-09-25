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
				wnd.SetSize(300, 250);
				wnd.tag = CraftName;
				wnd.Center();
				
				// создаем кнопки крафта
				GUI_Button btn = new GUI_Button(wnd) {
					public void DoClick() {
						SendClick(this.tag, "one");
					}
				};
				btn.tag = CraftName;
				btn.SetPos(30,wnd.Height() - 35);
				btn.SetSize(100,20);
				btn.caption = Lang.getTranslate("generic", "craft");

				btn = new GUI_Button(wnd) {
					public void DoClick() {
						SendClick(this.tag, "all");
					}
				};
				btn.tag = CraftName;
				btn.SetPos(180,wnd.Height() - 35);
				btn.SetSize(100,20);
				btn.caption = Lang.getTranslate("generic", "craft_all");

                // иконки результата
                GUI_Label lbl = new GUI_Label(wnd);
                lbl.caption = Lang.getTranslate("generic", "result_items");
                lbl.SetPos(10, 40);

                GUI_Panel final_pnl = new GUI_Panel(wnd);
                final_pnl.render_mode = GUI_Panel.RenderMode.rmSkin;
                final_pnl.skin_element = "listbox";
                final_pnl.SetPos(10, 65);
				int final_count, req_count, count;
				String image_name;
				String hint;
				final_count = pkt.read_int();
                final_pnl.SetSize(final_count*40 + 10, 50);
                int ax, ay;
				ax = 10; ay = 10;
				while (final_count > 0) {
					final_count--;
					image_name = pkt.read_string_ascii();
					hint = pkt.read_string_ascii();
					count = pkt.read_int();
					
					GUI_Image img = new GUI_Image(final_pnl);
					img.skin_element = "icon_"+image_name;
                    if (!img.getSkin().hasElement(img.skin_element)) img.skin_element = "item_"+image_name;
					img.simple_hint = Lang.getTranslate("server", hint);
					img.SetSize(Main.skin.GetElementSize(img.skin_element));
					img.SetPos(ax,ay);
					ax += 40;
					
					lbl = new GUI_Label(img);
					lbl.caption = String.valueOf(count);
					lbl.SetPos(img.size.sub(10, 20));
				}

                // иконки требуемых материалов
                lbl = new GUI_Label(wnd);
                lbl.caption = Lang.getTranslate("generic", "required_items");
                lbl.SetPos(10, 120);
                GUI_Panel req_pnl = new GUI_Panel(wnd);
                req_pnl.render_mode = GUI_Panel.RenderMode.rmSkin;
                req_pnl.skin_element = "listbox";
                req_pnl.SetPos(10, 145);
                ax = 10; ay = 10;
				req_count = pkt.read_int();
                req_pnl.SetSize(req_count*40 + 10, 50);
				while (req_count > 0) {
					req_count--;
					image_name = pkt.read_string_ascii();
					hint = pkt.read_string_ascii();
					count = pkt.read_int();
					
					GUI_Image img = new GUI_Image(req_pnl);
					img.skin_element = "icon_"+image_name;
                    if (!img.getSkin().hasElement(img.skin_element)) img.skin_element = "item_"+image_name;
					img.simple_hint = Lang.getTranslate("server", hint);
					img.SetSize(Main.skin.GetElementSize(img.skin_element));
					img.SetPos(ax,ay);
					ax += 40;
					
					lbl = new GUI_Label(img);
					lbl.caption = String.valueOf(count);
                    lbl.SetPos(img.size.sub(10, 20));
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
