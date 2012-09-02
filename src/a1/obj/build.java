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
import a1.Packet;
import a1.gui.GUI_BuildSlot;
import a1.gui.GUI_Button;
import a1.net.NetGame;
import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangInt;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangTuple;

import java.util.ArrayList;
import java.util.List;

public class build extends ObjectVisual {
    public List<BuildSlot> slots = new ArrayList<BuildSlot>();

    @Override
    protected void Parse() {
        slots.clear();
        int len = data.read_byte();
        while (len > 0) {
            len--;
            slots.add(new BuildSlot(data));
        }
    }

    @Override
    protected void PlaceCtrls() {
        int ay = 50;
        int n = 0;
        for (BuildSlot s : slots) {
            n++;
            GUI_BuildSlot gs = new GUI_BuildSlot(wnd) {
                @Override
                public void DoClick() {
                    SendSlotClick(order);
                }
            };
            gs.order = n;
            gs.Assign(s);
            gs.SetPos(20, ay);
            ay += gs.Height() + 15;
        }

        GUI_Button btn = new GUI_Button(wnd) {
            @Override
            public void DoClick() {
                SendBuild();
            }
        };
        btn.caption = Lang.getTranslate("server", "build");
        btn.SetPos(20, ay);
        btn.SetSize(140, 25);
        ay += 30;

        wnd.SetSize(180, ay + 20);
    }

    protected void SendBuild() {
        NetGame.SEND_object_visual_ack(objid, new OtpErlangAtom("build"));
    }

    protected void SendSlotClick(int n) {
        OtpErlangObject[] arr = new OtpErlangObject[2];
        arr[0] = new OtpErlangAtom("slot_click");
        arr[1] = new OtpErlangInt(n);

        NetGame.SEND_object_visual_ack(objid, new OtpErlangTuple(arr));

    }

    public class BuildSlot {
        public String icon;
        public int n1;
        public int n2;
        public int n3;

        public BuildSlot(Packet pkt) {
            icon = pkt.read_string_ascii();
            n1 = pkt.read_word();
            n2 = pkt.read_word();
            n3 = pkt.read_word();
        }
    }
}
