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

package a1;


import a1.gui.GUI;
import a1.utils.Resource;

import static a1.gui.GUI_Map.scale;

public class ObjLocal {
    public void prepare_draw(Coord oc) {

    }

    private void add_part(Resource.ResDraw dr, Coord c, int addz, boolean ignore_overlap) {
        if (dr == null) {
            return;
        }

        // real screen coords
        Coord cn = new Coord(c.x - dr.offx, c.y - dr.offy);


        // check visibility
        if (
                (cn.x + dr.width < 0) || (cn.y + dr.height < 0) ||
                        (cn.x > Config.getScreenWidth() * (1 / scale)) || (cn.y > Config.getScreenHeight() * (1 / scale))
                )
            return;

        // add part layers to render list
        for (Resource.ResLayer l : dr.layers) {
                GUI.map.render_parts.add(new RenderPart(c, cn, l, null, l.addz + addz, ignore_overlap));
        }
    }
}
