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
import a1.gui.GUI_Equip;
import a1.gui.GUI_Window;
import a1.net.NetGame;
import a1.utils.InventoryClick;

public class Equip {
    public InvItem head, body, lhand, rhand, legs, foots;

    GUI_Window wnd;
    GUI_Equip equip_ctrl;

    public void Open() {
        if (head == null) return;

        if (wnd == null) {
            wnd = new GUI_Window(GUI.getInstance().normal) {
                @Override
                protected void DoClose() {
                    wnd = null;
                    equip_ctrl = null;
                }
            };
            wnd.caption = Lang.getTranslate("server", "player_equip");
            wnd.SetSize(330, 365);
            wnd.Center();

            equip_ctrl = new GUI_Equip(wnd) {
                @Override
                public void ItemClick(InventoryClick click) {
                    ItemClick2(click);
                }
            };
            equip_ctrl.SetPos(10, 40);
            equip_ctrl.SetSize(wnd.size.sub(20, 30));
            equip_ctrl.Init(this);
        } else {
            wnd.Unlink();
            wnd = null;
            equip_ctrl = null;
        }
    }

    public void ItemClick2(InventoryClick click) {
        NetGame.SEND_EquipClick(click);
    }


    public void Clear() {
        head = null;
        body = null;
        lhand = null;
        rhand = null;
        legs = null;
        foots = null;

        if (wnd != null) {
            wnd.Unlink();
            wnd = null;
        }
    }

    public void Read(Packet pkt) {
        head = new InvItem(pkt);
        body = new InvItem(pkt);
        lhand = new InvItem(pkt);
        rhand = new InvItem(pkt);
        legs = new InvItem(pkt);
        foots = new InvItem(pkt);

        if (wnd != null) {
            equip_ctrl.Init(this);
        }
    }
}
