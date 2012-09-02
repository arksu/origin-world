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

import a1.*;
import a1.gui.GUI;
import a1.gui.GUI_Inventory;
import a1.gui.GUI_Window;
import a1.net.NetGame;
import a1.utils.InventoryClick;
import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangInt;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangTuple;

import java.util.HashMap;
import java.util.Map;

/**
 * объекты имеющие инвентарь: ящики, контейнеры и прочие
 */
public class inventory extends ObjectVisual {
    Inventory inv = null;

    @Override
    protected void Parse() {
        Map<Integer, GUI_Inventory> old = new HashMap<Integer, GUI_Inventory>();
        if (inv != null) inv.getCtrls(old);
        // читаем
        inv = new Inventory(data.read_int());
        inv.Read(data);

        // проходим по всем старым контролам
        for (Integer id : old.keySet()) {
            InvItem item = inv.getItem(id);

            // если не нашли такую вещь, и это не инвентарь игрока (т.к. его инвентарь есть всегда)
            if (item == null && id != this.objid) {
                old.get(id).parent.Unlink();
                continue;
            }

            // обновить инвентарь вещи, если такая вещь существует
            if ((item != null && item.isHaveInventory())) {
                // обновим состояние гуи объекта
                item.inv.ctrl = old.get(id);
                item.inv.ctrl.AssignInventory(item.inv);
                continue;
            }

            // обновить свой инвентарь если он открыт
            if (id == this.objid) {
                inv.ctrl = old.get(id);
                inv.ctrl.AssignInventory(inv);
            }
        }
    }

    @Override
    protected void Show() {
        if (inv.ctrl == null) {
            wnd = new GUI_Window(GUI.getInstance().normal) {
                @Override
                protected void DoClose() {
                    inv.ctrl = null;
                }

                @Override
                public void DoMoved() {
                    Inventory.UpdateInvPos(objid, pos);
                }
            };
            wnd.SetSize( inv.w * 33 + 20, inv.h * 33 + 20 + GUI_Window.CAPTION_HEIGHT );
            wnd.SetPos(Inventory.getInvPos(objid));
            wnd.caption = Lang.getTranslate("server", obj_type+"_inventory");
            wnd.resizeable = false;

            inv.ctrl = new GUI_Inventory(wnd) {
                @Override
                public void ItemClick(InventoryClick click) {
                    ItemClick2(click);
                }
            };
            inv.ctrl.AssignInventory(inv);
            inv.ctrl.SetPos(0, GUI_Window.CAPTION_HEIGHT+10);
            inv.ctrl.CenterX();
        }
    }

    public void ItemClick2(InventoryClick click) {
        // правый клик без модификаторов - открывает инвентарь
        if (click.btn == Input.MB_RIGHT && click.mod == 0) {
            InvItem item = inv.getItem(click.objid);
            if (item != null)
                if (item.isHaveInventory()) {
                    OpenInventory(item.inv, item);
                } else {
                    SendClick(click);
                }
        } else {
            SendClick(click);
        }
    }

    public void OpenInventory(Inventory invv, InvItem item) {
        if (invv.ctrl != null) {
            invv.ctrl.parent.Unlink();
            invv.ctrl = null;
        } else {
            GUI_Window wnd = new GUI_Window(GUI.getInstance().normal) {
                @Override
                protected void DoClose() {
                    // в инвентаре надо обнулить контрол
                    inv.CtrlClosed(tagi);
                }

                @Override
                public void DoMoved() {
                    Inventory.UpdateInvPos(tagi, pos);
                }
            };
            wnd.tagi = invv.objid;
            wnd.SetSize(invv.w * 33 + 20, invv.h * 33 + 20 + GUI_Window.CAPTION_HEIGHT);
            wnd.SetPos(Inventory.getInvPos(item.objid));
            wnd.caption = Lang.getTranslate("server", item.icon_name+"_inventory");
            wnd.resizeable = false;

            invv.ctrl = new GUI_Inventory(wnd) {
                @Override
                public void ItemClick(InventoryClick click) {
                    ItemClick2(click);
                }
            };
            invv.ctrl.AssignInventory(invv);
            invv.ctrl.SetPos(0, GUI_Window.CAPTION_HEIGHT+10);
            invv.ctrl.CenterX();
        }
    }

    @Override
    protected void Close() {
        if (inv != null) {
            Map<Integer, GUI_Inventory> old = new HashMap<Integer, GUI_Inventory>();
            inv.getCtrls(old);

            for (GUI_Inventory i : old.values()) {
                i.parent.Unlink();
            }
            inv = null;
        }
    }


    public void SendClick(InventoryClick click) {
        OtpErlangObject[] arr = new OtpErlangObject[2];
        arr[0] = new OtpErlangAtom("inv_click");

        // #inv_click
        OtpErlangObject[] click_arr = new OtpErlangObject[9];
        click_arr[0] = new OtpErlangAtom("inv_click");
        click_arr[1] = new OtpErlangInt(click.objid);
        click_arr[2] = new OtpErlangInt(click.inv_objid);
        click_arr[3] = new OtpErlangInt(click.btn);
        click_arr[4] = new OtpErlangInt(click.mod);
        click_arr[5] = new OtpErlangInt(click.x);
        click_arr[6] = new OtpErlangInt(click.y);
        click_arr[7] = new OtpErlangInt(click.offset_x);
        click_arr[8] = new OtpErlangInt(click.offset_y);

        arr[1] = new OtpErlangTuple(click_arr);

        OtpErlangTuple ack = new OtpErlangTuple(arr);
        Log.debug("inv click: " + ack.toString());
        NetGame.SEND_object_visual_ack(objid, ack);

    }
}
