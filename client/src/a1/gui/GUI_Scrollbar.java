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

import a1.Coord;
import a1.Input;

import static a1.gui.Skin.StateHighlight;
import static a1.gui.Skin.StatePressed;

public class GUI_Scrollbar extends GUI_Control {
	public int Step = 10;
	int MinPageSize;
	boolean PagePressed;
	boolean Vertical;
	Coord PagePressedMousePos;
	int PagePressedPos;
	int PagePressedPos_pix;
	int Max, Min, Pos, PageSize;
	GUI_Button BtnInc, BtnDec;

	public GUI_Scrollbar(GUI_Control parent) {
		super(parent);

		Min = 0;
		Max = 100;
		Pos = 0;
		PageSize = 50;
		Vertical = false;
		BtnDec = null;
		BtnInc = null;
		Step = 10;
		SetVertical(true, true);

		Coord s = getSkin().GetElementSize(skin_element + "_track");
		if (Vertical) {
			MinPageSize = s.y;
			s = getSkin().GetElementSize(skin_element);
			SetWidth(s.x);
		} else {
			MinPageSize = s.x;
			s = getSkin().GetElementSize(skin_element);
			SetHeight(s.y);
		}
		UpdateButtonsPos();
	}

	public void DoDec() {
		if (!HaveTrack())
			SetPos(Min);
		else
			SetPos(Pos - Step);
	}

	public void DoInc() {
		if (!HaveTrack())
			SetPos(Min);
		else
			SetPos(Pos + Step);
	}

	public boolean HaveTrack() {
		return PageSize < (Max - Min);
	}

	public void SetMin(int val) {
		if (val == Min)
			return;

		if (val >= Max) {
			Min = Max;
		} else {
			if (Pos < val) {
				Pos = val;
				PagePressed = false;
			}
			Min = val;
		}
		UpdateButtonsPos();
		DoChanged();

	}

	public void SetMax(int val) {
		if (val == Max)
			return;

		if (val <= Min) {
			Max = Min;
		} else {
			if (Pos > val - PageSize) {
				Pos = val - PageSize;
				if (Pos < Min)
					Pos = Min;

				PagePressed = false;
			}
			Max = val;
		}
		UpdateButtonsPos();
		DoChanged();
	}

	public void SetPos(int val) {
		if (Pos == val)
			return;
		int OldVal = Pos;

		if (val < Min)
			Pos = Min;
		else if (val > Max - PageSize)
			Pos = Max - PageSize;
		else
			Pos = val;

		if (Pos != OldVal)
			DoChanged();
	}
	
	public int getPos() {
		return Pos;
	}

	public void SetPageSize(int val) {
		if (PageSize == val)
			return;

		if (val > (Max - Min)) {
			PagePressed = false;
			Pos = Min;
		}
		PageSize = val;
		DoChanged();
	}
	
	public boolean getVertical() {
		return Vertical;
	}

	public void SetVertical(boolean val) {
		SetVertical(val, false);
	}

	public void SetVertical(boolean val, boolean immediate) {
		if ((PagePressed) && (!immediate))
			return;

		if ((Vertical != val) || immediate) {
			Vertical = val;
			// вертикально
			if (val) {

				if (BtnDec != null)
					BtnDec.Unlink();
				BtnDec = null;
				if (BtnInc != null)
					BtnInc.Unlink();
				BtnInc = null;

				BtnDec = new GUI_Button(this) {
					public void DoClick() {
						DoDec();
					}
				};
				BtnDec.skin_element = "button_up";
				BtnDec.SetSize(getSkin().GetElementSize("button_up"));

				BtnInc = new GUI_Button(this) {
					public void DoClick() {
						DoInc();
					}
				};
				BtnInc.skin_element = "button_down";
				BtnInc.SetSize(getSkin().GetElementSize("button_down"));
				skin_element = "vscroll";
			} else {
				// горизонтально
				if (BtnDec != null)
					BtnDec.Unlink();
				BtnDec = null;
				if (BtnInc != null)
					BtnInc.Unlink();
				BtnInc = null;

				BtnDec = new GUI_Button(this) {
					public void DoClick() {
						DoDec();
					}
				};
				BtnDec.skin_element = "button_left";
				BtnDec.SetSize(getSkin().GetElementSize("button_left"));

				BtnInc = new GUI_Button(this) {
					public void DoClick() {
						DoInc();
					}
				};
				BtnInc.skin_element = "button_right";
				skin_element = "hscroll";
				BtnInc.SetSize(getSkin().GetElementSize("button_right"));
			}
			SetSize(getSkin().GetElementSize(skin_element));
			UpdateButtonsPos();
		}
	}

	public void DoChange() {

	}

	protected void UpdateButtonsPos() {
		if (BtnDec == null || BtnInc == null)
			return;

		BtnDec.SetPos(0, 0);

		if (Vertical)
			BtnInc.SetPos(0, size.y - BtnInc.size.y);
		else
			BtnInc.SetPos(size.x - BtnInc.size.x, 0);

	}

	protected boolean MouseInPage() {
		boolean res = false;
		if (!MouseInMe())
			return res;

		if (!HaveTrack())
			return res;

		int len_pix = GetLenPixels();
		int page_size_pix = Math.round((float)len_pix / (Max - Min) * PageSize);
		if (page_size_pix < MinPageSize)
			page_size_pix = MinPageSize;

		int pos_pix = Math.round(((float)len_pix - page_size_pix) * (Pos - Min)
				/ (Max - Min - PageSize));

		if (Vertical)
			res = gui.MouseInRect(new Coord(abs_pos.x, abs_pos.y
					+ BtnDec.size.y + pos_pix),
					new Coord(size.x, page_size_pix));
		else
			res = gui.MouseInRect(new Coord(
					abs_pos.x + BtnDec.size.x + pos_pix, abs_pos.y), new Coord(
					page_size_pix, size.y));
		return res;
	}

	protected void DoChanged() {
		DoChange();
	}

	protected int GetLenPixels() {
		if (Vertical)
			return size.y - BtnDec.size.y - BtnInc.size.y;
		else
			return size.x - BtnDec.size.x - BtnInc.size.x;
	}

	public void DoSetSize() {
		UpdateButtonsPos();
	}

	public void DoRender() {
		if (BtnDec == null)
			return;
		getSkin().Draw(skin_element, abs_pos.x, abs_pos.y, size.x, size.y);

		if (HaveTrack()) {
			int len_pix = GetLenPixels();
			int page_size_pix = Math.round(((float)len_pix / (Max - Min)) * PageSize);
			if (page_size_pix < MinPageSize)
				page_size_pix = MinPageSize;

			int pos_pix;
			if (!PagePressed) {
				// если обычное состояние
				pos_pix = Math.round(((float)len_pix - page_size_pix) * (Pos - Min)
						/ (Max - Min - PageSize));
			} else {
				// если нажат слайдер
				if (Vertical)
					pos_pix = PagePressedPos_pix
							+ (gui.mouse_pos.y - PagePressedMousePos.y);
				else
					pos_pix = PagePressedPos_pix
							+ (gui.mouse_pos.x - PagePressedMousePos.x);
				if (pos_pix < 0)
					pos_pix = 0;
				if (pos_pix > len_pix - page_size_pix)
					pos_pix = len_pix - page_size_pix;
			}
			// рисуем слайдер
			if (Vertical)
				if (PagePressed)
					getSkin().Draw(skin_element + "_track", abs_pos.x,
							abs_pos.y + BtnDec.size.y + pos_pix, size.x,
							page_size_pix, StatePressed);
				else if (MouseInPage())
					getSkin().Draw(skin_element + "_track", abs_pos.x,
							abs_pos.y + BtnDec.size.y + pos_pix, size.x,
							page_size_pix, StateHighlight);
				else
					getSkin().Draw(skin_element + "_track", abs_pos.x,
							abs_pos.y + BtnDec.size.y + pos_pix, size.x,
							page_size_pix);
			else if (PagePressed)
				getSkin().Draw(skin_element + "_track",
						abs_pos.x + BtnDec.size.x + pos_pix, abs_pos.y,
						page_size_pix, size.y, StatePressed);
			else if (MouseInPage())
				getSkin().Draw(skin_element + "_track",
						abs_pos.x + BtnDec.size.x + pos_pix, abs_pos.y,
						page_size_pix, size.y, StateHighlight);
			else
				getSkin().Draw(skin_element + "_track",
						abs_pos.x + BtnDec.size.x + pos_pix, abs_pos.y,
						page_size_pix, size.y);
		}
	}

	public void DoUpdate() {
		super.DoUpdate();

		if (PagePressed && HaveTrack()) {
			int len_pix = GetLenPixels();

			int page_size_pix = Math.round(((float)len_pix / (Max - Min)) * PageSize);
			if (page_size_pix < MinPageSize)
				page_size_pix = MinPageSize;

			int new_pos_pix;
			if (Vertical)
				new_pos_pix = PagePressedPos_pix
						+ (gui.mouse_pos.y - PagePressedMousePos.y);
			else
				new_pos_pix = PagePressedPos_pix
						+ (gui.mouse_pos.x - PagePressedMousePos.x);
			if (new_pos_pix < 0)
				new_pos_pix = 0;
			if (new_pos_pix > len_pix - page_size_pix)
				new_pos_pix = len_pix - page_size_pix;

			SetPos(Math.round(((float)new_pos_pix) * (Max - Min - PageSize)
					/ (len_pix - page_size_pix) + Min));
		}

	}

	public boolean DoMouseBtn(int btn, boolean down) {
		boolean res = false;
		if (btn != Input.MB_LEFT)
			return res;

		if (down && HaveTrack()) {
			if (MouseInMe() && MouseInPage()) {
				PagePressed = true;
				PagePressedMousePos = new Coord(gui.mouse_pos);
				PagePressedPos = Pos;
				int len_pix = GetLenPixels();
				int page_size_pix = Math
						.round((float)len_pix / (Max - Min) * PageSize);
				if (page_size_pix < MinPageSize)
					page_size_pix = MinPageSize;
				PagePressedPos_pix = Math.round(((float)len_pix - page_size_pix)
						* (Pos - Min) / (Max - Min - PageSize));
				res = true;
			}
		} else
			PagePressed = false;
		return res;
	}

}
