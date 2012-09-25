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

package a1.obj;

import a1.Inventory;
import a1.Lang;
import a1.Log;
import a1.gui.GUI_Button;
import a1.gui.GUI_Inventory;
import a1.net.NetGame;
import a1.utils.InventoryClick;
import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangTuple;

public class bonfire extends ObjectVisual {
    boolean is_fire;
    Inventory inv, fuel;
    GUI_Inventory inv_ctrl, fuel_ctrl;
    GUI_Button fire_btn;

    @Override
    protected void Parse() {
        is_fire = data.read_byte() == 1;
        inv = new Inventory(objid);
        inv.Read(data);

        fuel = new Inventory(objid);
        fuel.Read(data);
    }

    @Override
    protected void PlaceCtrls() {
        inv_ctrl = new GUI_Inventory(wnd) {
            @Override
            public void ItemClick(InventoryClick click) {
                InvClick(click);
            }
        };
        inv_ctrl.SetPos(10, 50);
        inv_ctrl.AssignInventory(inv);

        fuel_ctrl = new GUI_Inventory(wnd) {
            @Override
            public void ItemClick(InventoryClick click) {
                FuelClick(click);
            }
        };
        fuel_ctrl.SetPos(10, 150);
        fuel_ctrl.AssignInventory(fuel);

        fire_btn = new GUI_Button(wnd) {
            @Override
            public void DoClick() {
                FireClick();
            }
        };
        fire_btn.caption = Lang.getTranslate("server", "fire");
        fire_btn.SetPos(fuel_ctrl.pos.add(0, 40));
        fire_btn.SetSize(120, 25);

        wnd.SetSize(155,225);
        fire_btn.CenterX();
    }

    public void InvClick(InventoryClick click) {
        OtpErlangObject[] arr = new OtpErlangObject[2];
        arr[0] = new OtpErlangAtom("inv_click");
        arr[1] = click.MakeTerm();

        OtpErlangTuple ack = new OtpErlangTuple(arr);
        Log.debug("inv click: " + ack.toString());
        NetGame.SEND_object_visual_ack(objid, ack);
    }

    public void FuelClick(InventoryClick click) {
        OtpErlangObject[] arr = new OtpErlangObject[2];
        arr[0] = new OtpErlangAtom("fuel_click");
        arr[1] = click.MakeTerm();

        OtpErlangTuple ack = new OtpErlangTuple(arr);
        Log.debug("fuel click: " + ack.toString());
        NetGame.SEND_object_visual_ack(objid, ack);
    }

    public void FireClick() {
        OtpErlangAtom ack = new OtpErlangAtom("fire");
        NetGame.SEND_object_visual_ack(objid, ack);

    }
}
