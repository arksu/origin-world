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
import a1.IntCoord;

public class GUI_ScrollPage extends GUI_Control {
	boolean HaveVerticalScrollbar;
	boolean HaveHorizontalScrollbar;
	int FullHeight;
	int FullWidth;
	public int MouseWheelStep;
	public boolean AutoScrollV;
	public boolean AutoScrollH;
	boolean VScrollBarIsRight;
	boolean HScrollBarIsBottom;
	boolean ScrollbarsVisible;
	GUI_Scrollbar FVerticalScrollbar;
	GUI_Scrollbar FHorizontalScrollbar;

	public GUI_ScrollPage(GUI_Control parent) {
		super(parent);
		VScrollBarIsRight = true;
		HScrollBarIsBottom = true;
		ScrollbarsVisible = true;
		HaveVerticalScrollbar = false;
		HaveHorizontalScrollbar = false;
		FullWidth = min_size.x;
		FullHeight = min_size.y;
		MouseWheelStep = 20;
		AutoScrollV = true;
		AutoScrollH = true;
	}

	public void DoSetSize() {
		super.DoSetSize();
		UpdateScrollBars();
	}

	public boolean DoMouseWheel(boolean isUp, int len) {
		boolean result = false;
		if (!MouseInMe())
			return result;

		if (HaveVerticalScrollbar) {
			FVerticalScrollbar.Step = MouseWheelStep;
			if (!isUp)
				FVerticalScrollbar.DoInc();
			else
				FVerticalScrollbar.DoDec();
			result = true;
		} else if (HaveHorizontalScrollbar) {
			FHorizontalScrollbar.Step = MouseWheelStep;
			if (!isUp)
				FHorizontalScrollbar.DoInc();
			else
				FHorizontalScrollbar.DoDec();
			result = true;
		}
		return result;
	}

	protected IntCoord WorkRect() { // HaveVerticalScrollbar
		int x, y, w, h;
		if (HaveVerticalScrollbar) {
			y = FVerticalScrollbar.Pos;
			if (HaveHorizontalScrollbar)
				h = ClientRect.h - FHorizontalScrollbar.Height();
			else
				h = ClientRect.h;
		} else {
			y = 0;
			if (HaveHorizontalScrollbar)
				h = ClientRect.h - FHorizontalScrollbar.Height();
			else
				h = ClientRect.h;
		}

		if (HaveHorizontalScrollbar) {
			x = FHorizontalScrollbar.Pos;
			if (HaveVerticalScrollbar)
				w = ClientRect.w - FVerticalScrollbar.Width();
			else
				w = ClientRect.w;
		} else {
			x = 0;
			if (HaveVerticalScrollbar)
				w = ClientRect.w - FVerticalScrollbar.Width();
			else
				w = ClientRect.w;
		}

		return new IntCoord(x, y, w, h);
	}

	protected Coord AbsWorkCoord() {
		Coord Result = new Coord(abs_pos.x + ClientRect.x - WorkRect().x,
				abs_pos.y + ClientRect.y - WorkRect().y);

		if (HaveHorizontalScrollbar)
			if (!HScrollBarIsBottom)
				Result.y = Result.y + FHorizontalScrollbar.size.y;

		if (HaveVerticalScrollbar)
			if (!VScrollBarIsRight)
				Result.x = Result.x + FVerticalScrollbar.Width();

		return Result;
	}

	protected void UpdateScrollBars() {
		int old_max, old_page_size;
		if (HaveVerticalScrollbar) {
			if (ScrollbarsVisible)
				FVerticalScrollbar.Show();
			else
				FVerticalScrollbar.Hide();

			if (VScrollBarIsRight)
				FVerticalScrollbar.SetX(ClientRect.x + ClientRect.w
						- FVerticalScrollbar.Width());
			else
				FVerticalScrollbar.SetX(ClientRect.x);

			FVerticalScrollbar.SetY(ClientRect.y);
			old_max = FVerticalScrollbar.Max;
			old_page_size = FVerticalScrollbar.PageSize;
			FVerticalScrollbar.Min = 0;
			FVerticalScrollbar.Max = FullHeight;

			if (HaveHorizontalScrollbar) {
				FVerticalScrollbar.SetHeight(ClientRect.h
						- FHorizontalScrollbar.Height());
			} else {
				FVerticalScrollbar.SetHeight(ClientRect.h);
			}
			FVerticalScrollbar.PageSize = FVerticalScrollbar.Height();

			if (AutoScrollV)
				if (FVerticalScrollbar.Pos + old_page_size >= old_max)
					FVerticalScrollbar.SetPos(FVerticalScrollbar.Max
							- FVerticalScrollbar.PageSize);
		}

		if (HaveHorizontalScrollbar) {
			if (ScrollbarsVisible)
				FHorizontalScrollbar.Show();
			else
				FHorizontalScrollbar.Hide();

			FHorizontalScrollbar.SetX(ClientRect.x);
			if (HScrollBarIsBottom)
				FHorizontalScrollbar.SetY(ClientRect.y + ClientRect.h
						- FHorizontalScrollbar.Height());
			else
				FHorizontalScrollbar.SetY(ClientRect.y);

			old_max = FHorizontalScrollbar.Max;
			old_page_size = FHorizontalScrollbar.PageSize;
			FHorizontalScrollbar.Min = 0;
			FHorizontalScrollbar.Max = FullWidth;
			if (HaveVerticalScrollbar) {
				FHorizontalScrollbar.SetWidth(ClientRect.w
						- FVerticalScrollbar.Width());
			} else {
				FHorizontalScrollbar.SetWidth(ClientRect.w);
			}
			FHorizontalScrollbar.PageSize = FHorizontalScrollbar.Width();

			if (AutoScrollH)
				if (FHorizontalScrollbar.Pos + old_page_size >= old_max)
					FHorizontalScrollbar.SetPos(FHorizontalScrollbar.Max
							- FHorizontalScrollbar.PageSize);
		}
	}

	public void DoScrollChange() {

	}

	protected GUI_Scrollbar CreateScrollbar(boolean is_vertical) {
		return new GUI_Scrollbar(this) {
			public void DoChange() {
				DoScrollChange();
			}
		};
	}

	protected void UpdateFullSize() {

	}

	public int GetVertScrollWidth() {
		if (HaveVerticalScrollbar)
			return FVerticalScrollbar.Width();
		else
			return 0;
	}

	public int GetHorizScrollHeight() {
		if (HaveHorizontalScrollbar)
			return FHorizontalScrollbar.Height();
		else
			return 0;
	}

	public void ResetScrollPos(boolean vertical) {
		if (vertical) {
			if (FVerticalScrollbar != null)
				FVerticalScrollbar.SetPos(FVerticalScrollbar.Min);
		} else if (FHorizontalScrollbar != null)
			FHorizontalScrollbar.SetPos(FHorizontalScrollbar.Min);
	}

	public void SetStyle(boolean v, boolean h) {
		boolean changed = false;
		// { +VERT}
		if (v && !HaveVerticalScrollbar) {
			if (FVerticalScrollbar != null)
				FVerticalScrollbar.Unlink();
			FVerticalScrollbar = CreateScrollbar(true);
			FVerticalScrollbar.SetVertical(true);
			FVerticalScrollbar.Min = 1;
			FVerticalScrollbar.Pos = 1;
			changed = true;
		}
		// { -VERT}
		if (!v && HaveVerticalScrollbar) {
			if (FVerticalScrollbar != null) {
				FVerticalScrollbar.Unlink();
				FVerticalScrollbar = null;
			}
			changed = true;
		}
		// { +HORIZ}
		if (h && !HaveHorizontalScrollbar) {
			if (FHorizontalScrollbar != null)
				FHorizontalScrollbar.Unlink();
			FHorizontalScrollbar = CreateScrollbar(false);
			FHorizontalScrollbar.SetVertical(true);
			FHorizontalScrollbar.Min = 1;
			FHorizontalScrollbar.Pos = 1;
			changed = true;
		}
		// { -HORIZ}
		if (!h && HaveHorizontalScrollbar) {
			if (FHorizontalScrollbar != null) {
				FHorizontalScrollbar.Unlink();
				FHorizontalScrollbar = null;
			}
			changed = true;
		}

		if (changed) {
			HaveHorizontalScrollbar = h;
			HaveVerticalScrollbar = v;
			UpdateScrollBars();
		}

	}

	public void SetFullHeight(int val) {
		if (FullHeight == val)
			return;
		FullHeight = val;
		UpdateScrollBars();
	}

	public void SetFullWidth(int val) {
		if (FullWidth == val)
			return;
		FullWidth = val;
		UpdateScrollBars();
	}

	public void SetHScrollBarIsBottom(boolean val) {
		HScrollBarIsBottom = val;
		UpdateScrollBars();
	}

	public void SetVScrollBarIsRight(boolean val) {
		VScrollBarIsRight = val;
		UpdateScrollBars();
	}

	public void SetScrollbarsVisible(boolean val) {
		ScrollbarsVisible = val;
		UpdateScrollBars();
	}

}
