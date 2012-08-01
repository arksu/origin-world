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
import a1.Log;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.DataFlavor;
import java.awt.Toolkit;

import a1.Coord;
import a1.Input;
import a1.IntCoord;
import a1.Render2D;

public class GUI_Edit extends GUI_Control implements ClipboardOwner {
	public String text = "";
	public Color bg_color = Color.white;
	public Color text_color = Color.white;
	public boolean is_pass = false;
	public boolean pressed = false;
	public String font_name = "default";
	public boolean allow_copy = true;

	public static final int OFFSET = 3;
	
	int pos1, pos2;
	boolean marking;
	int scroll = 0;
	
	public GUI_Edit(GUI_Control parent) {
		super(parent);
		focusable = true;
		skin_element = "edit";
	}
	
	public void DoClick() {}
	
	protected void UpdateCursor() {		
		if (!marking) return;
		pos2 = getIndex(gui.mouse_pos.x - abs_pos.x);
	}
	
	protected int getIndex(int x) {
		int r = getVisualText().length();
		for (int i = 1; i <= r; i++) {
			if (x + scroll < Render2D.GetTextWidth(font_name, getVisualText().substring(0, i))) {
				r = i-1;
				break;
			}
		}
		return r;
	}
	
	protected String getVisualText() {
		if (is_pass) {
			String s = "";
			for (int i = 0; i < text.length(); i++) {
				s += "*";
			}
			return s;
		} else return text;
	}
	
	protected void StartMark() {
		pos1 = getIndex(gui.mouse_pos.x - abs_pos.x);
		pos2 = pos1;
		marking = true;
	}
	
	protected void SetCursor(int p) {
		pos1 = Math.min(getVisualText().length(), Math.max(0, p));
		pos2 = pos1;
		marking = false;
	}
	
	protected int getSelectionStart() {
		return Math.min(pos1, pos2);
	}
	
	protected int getSelectionFinish() {
		return Math.max(pos1, pos2);
	}
	
	protected int getSelectionLength() {
		return getSelectionFinish() - getSelectionStart();
	}
	
	protected void pressDel() {
		if (pos1 != pos2) {
			DeleteSelection();
		} else if (pos1 < text.length()) {
			text = text.substring(0, pos1) + text.substring(pos1+1);
		}
	}
	
	protected void pressBackspace() {
		if (pos1 != pos2) {
			DeleteSelection();
		} else if (pos1 > 0) {
			text = text.substring(0, pos1 - 1) + text.substring(pos1);
			MoveCursor(0, false);
		}
	}
	
	protected void DoCopy() {
		Toolkit tk = Toolkit.getDefaultToolkit();
		Clipboard cp = tk.getSystemClipboard();
		StringSelection st = 
	           new StringSelection(getSelection());
		cp.setContents(st, this);
	}
	
	protected void DoCut() {
		Toolkit tk = Toolkit.getDefaultToolkit();
		Clipboard cp = tk.getSystemClipboard();
		StringSelection st = 
	           new StringSelection(getSelection());
		cp.setContents(st, this);
		DeleteSelection();
	}
	
	protected void DoPaste() {
		Toolkit tk = Toolkit.getDefaultToolkit();
		Clipboard cp = tk.getSystemClipboard();
		Transferable clipboardContent = cp.getContents (this);
		if ((clipboardContent != null) && 
				(clipboardContent.isDataFlavorSupported (DataFlavor.stringFlavor))) {
			try {
				String t = (String) clipboardContent.getTransferData(DataFlavor.stringFlavor);
				InsertText(t);
			} catch (Exception e) {
				Log.info("fail paste clipboard!");
			}
		}
	}

	protected void MoveCursor(int d, boolean isSelect) {
		int dest;
		switch (d) {
		// left
		case 0: dest = pos2 - 1; break;
		// right
		case 1: dest 	= pos2 + 1; break;
		// home
		case 2: dest = 0; break;
		// end
		case 3: dest = text.length(); break;
		default: dest = 0; break;
		}
		dest = Math.max(0, Math.min(dest, text.length()));
		if (isSelect) {
			SetMarker(dest);
		} else {
			SetCursor(dest);
		}
	}
	
	protected int CutPos(int i) {
		return Math.max(0, Math.min(text.length(), i));
	}
	
	protected void DeleteSelection() {
		if (getSelectionLength() == 0) return;
		text = text.substring(0, getSelectionStart()) + 
			text.substring(getSelectionStart() + getSelectionLength());
		pos1 = getSelectionStart();
		pos2 = pos1;
	}
	
	protected String getSelection() {
		return text.substring(getSelectionStart(), getSelectionFinish());
	}
	
	protected void SetMarker(int p) {
		pos2 = Math.max(0, Math.min(text.length(), p));
	}
	
	protected void InsertText(String t) {
		DeleteSelection();
		text = text.substring(0,pos1) + t + 
			text.substring(pos1);
		pos1 = CutPos(pos1 + t.length());
		pos2 = pos1;
		marking = false;
	}
	
	protected void SelectAll() {
		pos1 = 0;
		pos2 = text.length();
		marking = false;
	}
	
	protected void InsertText(char t) {
		DeleteSelection();
		text = text.substring(0,pos1) + t + 
			text.substring(pos1);
		pos1 = CutPos(pos1 + 1);
		pos2 = pos1;
		marking = false;
	}
	
	public boolean DoMouseBtn(int btn, boolean down) {
		if (!enabled) return false;
		
		if (btn == Input.MB_LEFT && (!down)) {
			marking = false;
			
		}
		
		if (btn == Input.MB_DOUBLE)
			SelectAll();
		
		if (btn == Input.MB_LEFT)
			if (down) {
				if (MouseInMe()){
					if (isFocused()) {
						StartMark();
					} else {
						gui.SetFocus(this);
						//SetCursor(text.length());
						StartMark();
					}
					pressed = true;
					return true;
				}
			} else {
				if (pressed && MouseInMe()) {
					DoClick();
					return true;
				}
				pressed = false;
			}
		return false;
	}
	
	public boolean DoKey(char c, int key, boolean down) {
		if (isFocused()) {
			if (down) {
				int mod = Input.GetKeyState();
				if (key == Keyboard.KEY_LEFT) 
					MoveCursor(0, mod == 2);
				else if (key == Keyboard.KEY_RIGHT) 
					MoveCursor(1, mod == 2);
				else if (key == Keyboard.KEY_HOME) 
					MoveCursor(2, mod == 2);
				else if (key == Keyboard.KEY_END) 
					MoveCursor(3, mod == 2);
				else if (c == 8) 
					pressBackspace();
				else if (key == Keyboard.KEY_DELETE)
					pressDel();
				else if (key == Keyboard.KEY_RETURN)
					DoEnter();
				else if (key == Keyboard.KEY_A && mod == 1)
					SelectAll();
				else if (key == Keyboard.KEY_C && mod == 1)
					DoCopy();
				else if (key == Keyboard.KEY_X && mod == 1)
					DoCut();
				else if (key == Keyboard.KEY_V && mod == 1)
					DoPaste();
				else if ((c >= 32) && (mod == 0 || mod == 2)) {
					InsertText(c);
				}
			}
			return true;
		} else
			return false;
	}

	public void DoUpdate() {
		UpdateCursor();
		int cursorx = Render2D.GetTextWidth(
				font_name, getVisualText().substring(0, pos2));
		if ((cursorx - scroll > size.x) || (cursorx - scroll < 0))
			scroll = cursorx - (size.x / 2);
		scroll = Math.max(0, scroll);
	}
	
	public void DoRender() {
		int state;
		if (!enabled)
			state = StateDisable;
		else {
			if (isFocused())
				state = StateNormal_Checked;
			else {
				if (MouseInMe()) {
					if (pressed) 
						state = StatePressed;
					else
						state = StateHighlight;
				}
				else
					state = StateNormal;
			}
		}
		getSkin().Draw(skin_element, abs_pos.x, abs_pos.y, size.x, size.y, state);
		
		Render2D.PushScissor(new IntCoord(abs_pos.x + OFFSET, abs_pos.y+OFFSET, size.x - OFFSET, size.y-OFFSET));
		int left = Render2D.GetTextWidth(font_name, getVisualText().substring(0, getSelectionStart()));
		int SelectionWidth = Render2D.GetTextWidth(font_name, getVisualText().substring(
				getSelectionStart(), getSelectionFinish()));
		int CursorShift = Render2D.GetTextWidth(font_name, getVisualText().substring(0, pos2));
		
		if (isFocused()) {
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			if (pos1 != pos2) {
				Render2D.FillRect(new Coord(abs_pos.x + left + OFFSET - scroll, abs_pos.y),
						new Coord(SelectionWidth, size.y),
						new Color(0.5f, 0.5f, 0.8f, 0.9f));
			}
			if ((System.currentTimeMillis() % 1000) > 500) {
				Render2D.ChangeColor(Color.green);
				Render2D.Rectangle(new Coord(abs_pos.x + CursorShift + OFFSET - scroll+1, abs_pos.y),
						new Coord(1, size.y));
			}
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}
		

		if (getVisualText().length() > 0)
			Render2D.Text(font_name, 
					abs_pos.x+OFFSET-scroll,	abs_pos.y+OFFSET,
					size.x - OFFSET, size.y - OFFSET,
					Render2D.Align_Left + Render2D.Align_VStretch,
					getVisualText(), 
					text_color);

		Render2D.PopScissor();
	}
	
	public void DoGetFocus() {
		SelectAll();
	}
	
	public void SetText(String text) {
		this.text = text;
		pos1 = pos2 = 0;
	}
	
	public void DoEnter() {}

	public void lostOwnership(Clipboard arg0, Transferable arg1) {}
}
