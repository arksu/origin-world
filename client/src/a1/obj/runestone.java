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

import a1.Lang;
import a1.gui.GUI_Button;
import a1.gui.GUI_Edit;
import a1.gui.GUI_Label;
import a1.net.NetGame;
import a1.utils.Utils;
import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangString;
import com.ericsson.otp.erlang.OtpErlangTuple;

/**
 * runestone object
 */
public class runestone extends ObjectVisual{
    String text = "";
    GUI_Label lbl;
    GUI_Button btn_add, btn_send;
    GUI_Edit ed;

    @Override
    protected void Parse() {
        if ((term instanceof OtpErlangAtom) && (((OtpErlangAtom)term).atomValue().equals("none")))
            text = "";
        else text = Utils.getErlangString(term);
    }

    @Override
    protected void PlaceCtrls() {
        String[] list = text.split("\\r");

        int i = 0;
        for (String s : list) {
            lbl = new GUI_Label(wnd);
            lbl.caption = s;
            lbl.SetPos(10, 35+30*i);
            i++;
        }

        if (text.length() < 100) {
            btn_add = new GUI_Button(wnd) {
                @Override
                public void DoClick() {
                    AddTextEdit();
                }
            };
            btn_add.SetSize(120, 25);
            btn_add.SetPos(lbl.pos.add(0,30));
            btn_add.caption = Lang.getTranslate("generic","add");
        }

        wnd.SetSize(300,lbl.pos.y + 70);
    }

    private void AddTextEdit() {
        if (btn_add != null) {
            ed = new GUI_Edit(wnd);
            ed.SetPos(btn_add.pos);
            ed.SetSize(145,23);

            btn_send = new GUI_Button(wnd) {
                @Override
                public void DoClick() {
                    SendText(ed.text);
                }
            };
            btn_send.SetPos(ed.pos.add(170, 0));
            btn_send.SetSize(100, 25);
            btn_send.caption = Lang.getTranslate("generic","send");

            btn_add.Unlink();
            btn_add = null;
        }
    }

    private void SendText(String t) {
        OtpErlangObject[] arr = new OtpErlangObject[2];
        arr[0] = new OtpErlangAtom("add_text");
        arr[1] = new OtpErlangString(t);
        OtpErlangTuple ack = new OtpErlangTuple(arr);
        NetGame.SEND_object_visual_ack(id, ack);
    }


}
