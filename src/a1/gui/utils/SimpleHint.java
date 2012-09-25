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

package a1.gui.utils;


import a1.Coord;
import a1.Render2D;
import org.newdawn.slick.Color;

public class SimpleHint {
    static String hint_font = "default";

    static public void Render(int x, int y, int w, int h, String s) {
        int cy = 0;
        String[] sl = s.split("%n");
        for (String ss : sl) {
            Render2D.Text(hint_font, x+4, y+cy, w, h, Render2D.Align_Left + Render2D.Align_Top, ss, Color.white);
            cy += Render2D.GetTextHeight(hint_font, ss)+5;
        }
    }

    static public Coord getSize(String s) {
        String[] sl = s.split("%n");
        int w = 0, h = 0, cw;
        for (String ss : sl) {
            cw = Render2D.GetTextWidth(hint_font, ss)+10;
            h += Render2D.GetTextHeight(hint_font, ss)+5;
            if (cw > w) w = cw;
        }

        return new Coord(w,h);
    }
}
