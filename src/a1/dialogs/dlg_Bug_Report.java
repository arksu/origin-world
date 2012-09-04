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

package a1.dialogs;

import a1.Lang;
import a1.gui.*;
import a1.net.NetGame;

public class dlg_Bug_Report extends Dialog{
    public static dlg_Bug_Report dlg = null;
    public GUI_Window wnd;
    public GUI_Label lbl_subj, lbl_text;
    public GUI_Edit ed_subject, ed_text;
    public GUI_Button btn_ok, btn_cancel;

    static {
        Dialog.AddType("dlg_bug_report", new DialogFactory() {
            public Dialog create() {
                return new dlg_Bug_Report();
            }
        });
    }

    public void DoShow() {
        dlg = this;
        wnd = new GUI_Window(GUI.getInstance().normal) {
            protected void DoClose() {
                dlg_Bug_Report.dlg.wnd = null;
                Dialog.Hide("dlg_bug_report");
            }
        };
        wnd.caption = Lang.getTranslate("generic", "bug_report");

        lbl_subj = new GUI_Label(wnd);
        lbl_subj.caption = Lang.getTranslate("generic", "subject");
        lbl_text = new GUI_Label(wnd);
        lbl_text.caption = Lang.getTranslate("generic", "text");

        ed_subject = new GUI_Edit(wnd);
        ed_text = new GUI_Edit(wnd);

        btn_ok = new GUI_Button(wnd) {
            public void DoClick() {
                NetGame.SEND_bug_report( ed_subject.text, ed_text.text );
                Dialog.Hide("dlg_bug_report");
            }
        };
        btn_ok.caption = Lang.getTranslate("generic", "ok");

        btn_cancel = new GUI_Button(wnd) {
            public void DoClick() {
                Dialog.Hide("dlg_bug_report");
            }
        };
        btn_cancel.caption = Lang.getTranslate("generic", "cancel");


        UpdateControlsPos();
    }

    public void UpdateControlsPos() {
        wnd.SetSize(350, 200);
        wnd.Center();

        lbl_subj.SetPos(10, 40);
        ed_subject.SetPos(lbl_subj.pos.x + 100, lbl_subj.pos.y);
        ed_subject.SetSize(180, 25);

        lbl_text.SetPos(10, lbl_subj.pos.y + 50);
        ed_text.SetPos(lbl_text.pos.x + 100, lbl_text.pos.y);
        ed_text.SetSize(180, 25);

        btn_ok.SetSize(100, 24);
        btn_ok.SetPos(wnd.Width() - btn_ok.Width() - 15, wnd.Height() - btn_ok.Height() - 15);
        btn_cancel.SetSize(100, 24);
        btn_cancel.SetPos(15, wnd.Height() - btn_cancel.Height() - 15);
    }

    public void DoHide() {
        dlg = null;
        if (wnd != null)
            wnd.Unlink();
        wnd = null;
    }

    public static boolean Exist() {
        return dlg != null;
    }

}
