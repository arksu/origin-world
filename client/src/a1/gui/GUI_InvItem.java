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
package a1.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.newdawn.slick.Color;

import a1.Coord;
import a1.Lang;
import a1.Packet;
import a1.Render2D;
import a1.net.NetGame;

// слот инвентаря. отдельная вещь
public class GUI_InvItem extends GUI_Control {
	private boolean inited = false;
	public int objid;
	public int quality;
	// положение вещи
	public int x; 
	public int y;
	// размеры вещи
	public int w;
	public int h;
	public String icon_name;
	public String hint;
	// для отображения прогресса
	public int progress, max;
	// настраиваемые параметры
	public List<Param> params = new ArrayList<Param>();
	public boolean is_local = false;
	
	public GUI_InvItem(GUI_Control parent) {
		super(parent);
	}
	
	public boolean Contains(int ax, int ay) {
		return (ax >= x && ay >= y && ax < x+w && ay < y+h);
	}
	
	public void SetLocal(int x, int y) {
		this.x = x;
		this.y = y;
		w = 1;
		h = 1;
		SetPos(x*33, y*33);
		SetSize(w*33,h*33);
		is_local = true;
	}

	public void NetRead(Packet pkt) {
		inited = true;
		objid = pkt.read_int();
        if (objid==0) {
            x = 0;
            y = 0;
            return;
        }
		x = pkt.read_int();
		y = pkt.read_int();
		w = pkt.read_int();
		h = pkt.read_int();
		quality = pkt.read_int();
		icon_name = pkt.read_string_ascii();
		hint = pkt.read_string_ascii();
		simple_hint = MakeHint();
		progress = pkt.read_int();
		max = pkt.read_int();
		int params_count = pkt.read_int();
		while (params_count > 0) {
			params_count--;
			Param p = new Param(pkt);
			params.add(p);
		}
		
		SetPos(x*33, y*33);
		SetSize(w*33,h*33);
	}
	
	// получить хинт на основе маски и с учетом языка
	public String MakeHint() {
		if (!inited) return "";
		// Маска из перевода
		String hint_mask = Lang.getTranslate("hint", hint);
		
		// Замена подстановок
		Pattern patt = Pattern.compile("%param_([a-z]*)");
		Matcher m = patt.matcher(hint_mask);
		StringBuffer sb = new StringBuffer(hint_mask.length());
		while (m.find()) {
			String param = m.group(1);
			for (int i = 0; i < params.size(); i++) {
				if (params.get(i).name.equals(param)) {
					m.appendReplacement(sb, params.get(i).str);
					break;
				}
			}
		}
		m.appendTail(sb);
		hint_mask = sb.toString();
		
		// Замена дефолтных параметов
		hint_mask = hint_mask.replaceAll("%q", Integer.toString(quality));
		hint_mask = hint_mask.replaceAll("%p", Integer.toString(progress) + "/" + Integer.toString(max));
		//hint = hint_mask;
		
		// %q = quality
		// %param_name = взять параметр с именем name и подставить значение
		// %p = progress/max, например progress = 15 max = 100, нужно вывести 15/100
		
		// bucket_silk=Ведро шелка (%param_amount литров), качество %q, стадия %param_stage
		return hint_mask;
	}
	
	// доп класс для реализации любых параметров
	public class Param {
		public String name;
		public String str = "";
		
		public Param(Packet pkt) {
			name = pkt.read_string_ascii();
			boolean is_numeric = pkt.read_byte() == 1;
			if (is_numeric)
				str = Integer.toString(pkt.read_int());
			else
				str = pkt.read_string_ascii();
		}
		
		public Param(String n, String st) {
			name = n; str = st;
		}
	}
	
	public void OnClicked(int btn) {
		int mx = gui.mouse_pos.x - abs_pos.x;
		int my = gui.mouse_pos.y - abs_pos.y;
		NetGame.SEND_gui_click(x*33+mx, y*33+my, mx, my, parent.id, btn);
	}

	// обработчик нажатия кнопок мыши
	public boolean DoMouseBtn(int btn, boolean down) {
		if (MouseInMe() && down && parent != null) {
			// если это серверный контрол
			if (parent.id > 0) {
				OnClicked(btn);
			}
			return true;
		}
		return false;
	}
	
	public void DoRender() {
		if (inited) {
			getSkin().Draw("item_" + icon_name, abs_pos.x+1, abs_pos.y+1);
			Coord sz = getSkin().GetElementSize("item_" + icon_name);
			Render2D.Text("smallfont", 
					abs_pos.x+1, abs_pos.y+1, 
					sz.x-2, sz.y-1, 
					Render2D.Align_Right + Render2D.Align_Bottom, 
					Integer.toString(quality), 
					Color.white);
		}
	}
	
	public String toString() {
		return getClass().getName() + " pos="+pos.toString() + " size="+size.toString() + " c="+x+","+y+" sz="+w+","+h;
		
	}
}
