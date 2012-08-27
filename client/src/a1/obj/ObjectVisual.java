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
import a1.Log;
import a1.Packet;
import a1.gui.GUI;
import a1.gui.GUI_Window;
import com.ericsson.otp.erlang.OtpErlangObject;

import java.util.HashMap;
import java.util.Map;

/**
 * визуальное отображение состояния объекта
 */
public class ObjectVisual {
    protected int id;
    protected String obj_type;
    protected OtpErlangObject term;
    protected GUI_Window wnd;

    private static Map<Integer, ObjectVisual> obj_list = new HashMap<Integer, ObjectVisual>();

    public static void RecvVisualState(Packet pkt) {
        int id = pkt.read_int();
        String obj_type = pkt.read_string_ascii();
        OtpErlangObject term =  pkt.read_erlang_term();
        Log.debug("visual obj : " + id + " type: " + obj_type + " state: " + term.toString());

        ObjectVisual ov = obj_list.get(id);
        if (ov != null) {
            ov.Init(id, obj_type, term);
        } else {
            try {
                //-------------------------
                if ("runestone".equals(obj_type)) ov = new runestone();
                if ("claim".equals(obj_type)) ov = new claim();
                //-------------------------

                if (ov != null) {
                    obj_list.put(id, ov);
                    ov.Init(id, obj_type, term);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void CloseObj(Packet pkt) {
        int id = pkt.read_int();
        Log.debug("close obj : " + id);
        ObjectVisual ov = obj_list.get(id);
        if (ov != null) ov.Close();
        obj_list.remove(id);
    }

    public static void CloseAll() {
        for (ObjectVisual ov : obj_list.values()) {
            ov.Close();
        }
    }

    public void Init(int id, String obj_type, OtpErlangObject term) {
        this.id = id;
        this.obj_type = obj_type;
        this.term = term;
        Parse();
        Show();
    }

    protected void Parse() {}

    protected void OnClose() {}

    protected void Show() {
        if (wnd == null) {
            wnd = new GUI_Window(GUI.getInstance().normal) {
                protected void DoClose() {
                    OnClose();
                    wnd = null;
                }
            };
            wnd.set_close_button(false);
            wnd.SetSize(300,150);
            wnd.SetPos(250, 200);
            wnd.caption = Lang.getTranslate("server", obj_type);

            PlaceCtrls();
        } else {
            wnd.BringToFront();
            RefreshCtrls();
        }
    }

    private void RefreshCtrls() {
        wnd.UnlinkChilds();
        PlaceCtrls();
    }

    protected void PlaceCtrls() {}

    protected void Close() {
        if (wnd != null) {
            OnClose();
            wnd.Unlink();
            wnd = null;
        }
    }
    public boolean Exist() {
        return wnd != null;
    }
}
