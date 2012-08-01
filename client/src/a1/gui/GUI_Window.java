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

import static a1.gui.Skin.*;

import org.newdawn.slick.Color;

import a1.Coord;
import a1.Input;
import a1.IntCoord;
import a1.Lang;
import a1.Packet;
import a1.Render2D;

public class GUI_Window extends GUI_Control {
	public boolean moveable = true;
	public boolean resizeable = true;
	public String caption = "";
	public Color caption_color = Color.white;
	public int caption_align = Render2D.Align_Stretch;
	public String font = "default";
	
	private boolean have_close_button = true;
	private GUI_Button close_btn = null;
	private Coord resize_pos_begin;
	private Coord resize_size;
	private int resizex;
	private boolean left_resize = false;
	private boolean right_resize = false;
	
	protected static final int CAPTION_HEIGHT = 28;
	private static final int CLOSE_BTN_W = 24;
	private static final int CLOSE_BTN_H = 22;
	
	static {
		GUI.AddType("gui_window", new ControlFactory() {		
			public GUI_Control create(GUI_Control parent) {
				return new GUI_Window(parent);
			}
		});
	}
	
	public GUI_Window(GUI_Control parent) {
		super(parent);
		if (have_close_button) {
			close_btn = spawn_close_btn();
			update_close_btn();
		}
		min_size = new Coord(70, 35);
	}
	
	protected GUI_Button spawn_close_btn() {
		return new GUI_Button(this) {
			public void DoClick() {
				((GUI_Window)parent).close();
			}
		};
	}
	
	protected void close() {
		DoClose();
		this.Unlink();
	}
	
	protected void DoClose() {
	}
	
	public boolean DoMouseBtn(int btn, boolean down) {
		if (!enabled) return false;
		
		if (down && MouseInMe())
			BringToFront();
		
		if (btn == Input.MB_LEFT && resizeable) {
			if (down) {
				if (mouse_in_left_resize()) {
					left_resize = true;
					resize_pos_begin = gui.mouse_pos;
					resize_size = size;
					resizex = pos.x;
					return true;
				}
				if (mouse_in_right_resize()) {
					right_resize = true;
					resize_pos_begin = gui.mouse_pos;
					resize_size = size;
					resizex = pos.x;
					return true;
				}
			} else {
				left_resize = false;
				right_resize = false;
			}
		}
		
		if (btn == Input.MB_LEFT && moveable) {
			if (down) {
				if (MouseInMe() && mouse_in_caption()) {
					BeginDragMove();
					return true;
				}				
			} else
				EndDragMove();
		}
	
		return false;
	}
	
	public void DoUpdate() {
//		if (have_close_button && (close_btn == null || close_btn.terminated) ) {
//			close_btn = spawn_close_btn();
//		}
		int dx, dy;
		if (left_resize) {
			dx = gui.mouse_pos.x - resize_pos_begin.x;
			dy = gui.mouse_pos.y - resize_pos_begin.y;
			int w = resize_size.x - dx;
			SetX(resizex + resize_size.x - w);
			SetSize(w, resize_size.y+dy);
			update_close_btn();
		}
		if (right_resize) {
			dx = gui.mouse_pos.x - resize_pos_begin.x;
			dy = gui.mouse_pos.y - resize_pos_begin.y;
			SetSize(resize_size.x+dx, resize_size.y+dy);
			update_close_btn();
		}
	}
	
	public void DoRender() {
		String e_name;
		int state;
		if (have_close_button) {
			e_name = "window_caption_close"; 
			if (mouse_in_caption()) state = StateHighlight; else state = StateNormal;
		}
		else {
			e_name = "window_caption"; 
			if (mouse_in_caption()) state = StateHighlight; else state = StateNormal;
		}
		getSkin().Draw(e_name, abs_pos.x, abs_pos.y, size.x, size.y, state);
		
		getSkin().Draw("window", abs_pos.x, abs_pos.y+CAPTION_HEIGHT, size.x, size.y-CAPTION_HEIGHT, StateNormal);
		
		if (resizeable) {
			Coord sz = getSkin().GetElementSize("window_resize_right");
			getSkin().Draw("window_resize_right", abs_pos.x+size.x-sz.x, abs_pos.y+size.y-sz.y, sz.x, sz.y, 
					((mouse_in_right_resize()||right_resize)?StateHighlight:StateNormal));
			getSkin().Draw("window_resize_left", abs_pos.x, abs_pos.y+size.y-sz.y, sz.x, sz.y, 
					((mouse_in_left_resize()||left_resize)?StateHighlight:StateNormal));
			}
		
		if (have_close_button)
			Render2D.Text(font, abs_pos.x, abs_pos.y-2, size.x-CLOSE_BTN_W, CAPTION_HEIGHT, caption_align, caption, caption_color);
		else
			Render2D.Text(font, abs_pos.x, abs_pos.y-2, size.x, CAPTION_HEIGHT, caption_align, caption, caption_color);
	}
	
	public void DoSetSize() {
		update_close_btn();
	}
	
	public void set_close_button(boolean close) {
		if (have_close_button != close) {
			if (have_close_button && close_btn != null) {
				close_btn.Unlink();
				close_btn = null;
			}
			if (!have_close_button) {
				close_btn = spawn_close_btn();
			}
			have_close_button = close;
		}
		update_close_btn();
	}
	
	protected boolean mouse_in_caption() {
		IntCoord rect = new IntCoord(abs_pos, new Coord(size.x,CAPTION_HEIGHT));
		return (rect.PointInRect(gui.mouse_pos) && MouseInMe());
	}
	
	protected boolean mouse_in_right_resize() {
		Coord sz = getSkin().GetElementSize("window_resize_right");
		IntCoord rect = new IntCoord(abs_pos.x+size.x-sz.x, abs_pos.y+size.y-sz.y, sz);
		return (rect.PointInRect(gui.mouse_pos) && MouseInMe());
	}
	
	protected boolean mouse_in_left_resize() {
		Coord sz = getSkin().GetElementSize("window_resize_left");
		IntCoord rect = new IntCoord(abs_pos.x, abs_pos.y+size.y-sz.y, sz);
		return (rect.PointInRect(gui.mouse_pos) && MouseInMe());
	}
	
	protected void update_close_btn() {
		if (close_btn == null) return;
		close_btn.SetPos(size.x-CLOSE_BTN_W-2, 3);
		close_btn.SetSize(CLOSE_BTN_W, CLOSE_BTN_H);
		close_btn.icon_name = "button_close";
		close_btn.render_bg = false;
	}
	
	// прочитать параметры контрола из сети
	public void DoNetRead(Packet pkt) {
		caption = Lang.getTranslate("server", pkt.read_string_ascii());
		int w = pkt.read_int();
		int h = pkt.read_int();
		SetSize(w, h);
		resizeable = false;
	}
}
