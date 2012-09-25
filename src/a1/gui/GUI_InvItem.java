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

package a1.gui;

import a1.*;
import a1.net.Packet;
import a1.utils.InventoryClick;
import a1.utils.Rect;
import org.newdawn.slick.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        objid = 0;
		is_local = true;
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
            for (Param p : params) {
                if (p.name.equals(param)) {
                    m.appendReplacement(sb, p.str);
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

    public void Assign(InvItem i) {
        objid = i.objid;
        if (objid==0) {
            x = 0;
            y = 0;
            inited = false;
            return;
        }
        inited = true;
        x = i.x;
        y = i.y;
        w = i.w;
        h = i.h;
        quality = i.quality;
        icon_name = i.icon_name;
        hint = i.hint;
        simple_hint = MakeHint();
        progress = i.progress;
        max = i.max;
//        int params_count = pkt.read_int();
//        while (params_count > 0) {
//            params_count--;
//            Param p = new Param(pkt);
//            params.add(p);
//        }

        SetPos(x*33, y*33);
        SetSize(w*33,h*33);

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

        if (parent != null && (parent instanceof GUI_Inventory)) {
            InventoryClick click = new InventoryClick();
            click.objid = objid;
            click.btn = btn;
            click.inv_objid = ((GUI_Inventory) parent).objid;
            click.mod = Input.GetKeyState();
            click.offset_x = mx;
            click.offset_y = my;
            int xx = (x * 33 + mx - (Player.hand.isExist() ? Player.hand.offset_x : 0) + 16) / 33;
            int yy = (y * 33 + my - (Player.hand.isExist() ? Player.hand.offset_y : 0) + 16) / 33;
            click.x = xx;
            click.y = yy;

            ((GUI_Inventory) parent).ItemClick(click);
        }

        if (parent != null && parent instanceof GUI_Equip) {
            InventoryClick click = new InventoryClick();
            click.objid = objid;
            click.btn = btn;
            click.mod = Input.GetKeyState();
            click.offset_x = mx;
            click.offset_y = my;
            click.inv_objid = getEquipID(); // тут вышлем ид слота куда кликнули
            ((GUI_Equip) parent).ItemClick(click);
        }
	}

    protected int getEquipID() {
        return  0;
    }

	// обработчик нажатия кнопок мыши
	public boolean DoMouseBtn(int btn, boolean down) {
		if (MouseInMe() && down && parent != null) {
			OnClicked(btn);
			return true;
		}
		return false;
	}
	
	public void DoRender() {
		if (inited) {
            // картинка
			getSkin().Draw("item_" + icon_name, abs_pos.x+1, abs_pos.y+1);

            // прогресс
            if (max > 0)
                DrawProgress( (float)progress / (float)max );

            // качество в уголке
			Coord sz = getSkin().GetElementSize("item_" + icon_name);
			Render2D.Text("smallfont", 
					abs_pos.x+1, abs_pos.y+1, 
					sz.x-2, sz.y-1, 
					Render2D.Align_Right + Render2D.Align_Bottom, 
					Integer.toString(quality), 
					Color.white);

            if (Config.debug && max != 0)
            Render2D.Text("smallfont",
                    abs_pos.x+1, abs_pos.y+1,
                    sz.x-2, sz.y-1,
                    Render2D.Align_Left + Render2D.Align_Top,
                    Integer.toString(progress) + "/" + max,
                    Color.white);

            // подсветка наведения "руки"
            if (Player.hand.isExist()) {
                int mx = gui.mouse_pos.x - abs_pos.x;
                int my = gui.mouse_pos.y - abs_pos.y;
                int xx = (x * 33 + mx - Player.hand.offset_x + 16) / 33;
                int yy = (y * 33 + my - Player.hand.offset_y + 16) / 33;

                Rect myc = new Rect(x, y, w-1, h-1);
                Rect hc = new Rect(xx,yy,Player.hand.w-1, Player.hand.h-1);

                if (hc.is_inside(myc)) {
                    Render2D.ChangeColor(new Color(120, 0, 0, 100));
                    Render2D.Disable2D();
                    Render2D.FillRect(abs_pos, size);
                    Render2D.Enable2D();
                }
            }
		}
	}

    private void DrawProgress(float p) {
        if (p == 0) return;
        int fp = 90 + (int)(p * 360);
        Render2D.Disable2D();
        Render2D.ChangeColor(new Color(0, 200, 0, 100));
        Render2D.PushScissor(new Rect(abs_pos.add(1), new Coord(32, 32)));
        Render2D.FillEllipse(abs_pos.add(16), new Coord(30, 30), 90, fp);
        Render2D.PopScissor();
        Render2D.Enable2D();
    }

    public boolean inRect(Rect r) {
        boolean bx = false;
        boolean by = false;

       return false;
    }
	
	public String toString() {
		return getClass().getName() + " pos="+pos.toString() + " size="+size.toString() + " c="+x+","+y+" sz="+w+","+h;
		
	}
}
