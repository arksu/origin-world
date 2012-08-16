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

import a1.DialogFactory;
import a1.Hotbar;
import a1.Input;
import a1.Lang;
import a1.gui.*;
import org.newdawn.slick.Color;

/**
 * диалог для настройки хотбаров
 */
public class dlg_Hotbars extends Dialog {
    public static dlg_Hotbars dlg = null;

    GUI_Window wnd, wnd_bind;
    GUI_StringList hlist;
    GUI_Panel options_panel, general_panel, position_panel, visibility_panel;
    GUI_Button btn_general, btn_position, btn_visibility, btn_bind_hotkeys;
    GUI_SpinEdit ed_buttons_count, ed_rows_count, ed_padding, ed_alpha, ed_alpha_bg;
    public GUI_SpinEdit ed_offset_x, ed_offset_y;
    GUI_Checkbox ch_enabled, ch_locked, ch_visible, ch_hide_hotkey, ch_click_through, ch_hide_empty_slots;
    GUI_Edit ed_name;
    // alpha!
    GUI_Scrollbar sb_alpha, sb_alpha_bg;
    GUI_Checkbox ch_move_locked;

    private static String SECTION = "hotbars";
    private static int LBL_Y_OFFSET = 20;
    private int mode = 0;
    public Hotbar current_hotbar = null;

    @Override
    public void DoShow() {
        dlg = this;
        wnd_bind = null;
        wnd = new GUI_Window(GUI.getInstance().normal) {
            protected void DoClose() {
                dlg_Hotbars.dlg.wnd = null;
                Dialog.Hide("dlg_hotbars");
            }
        };
        wnd.caption = Lang.getTranslate(SECTION, "caption");
        wnd.SetSize(500, 430);
        wnd.Center();
        wnd.resizeable = false;
        GUI_Label lbl;

        ch_move_locked = new GUI_Checkbox(wnd);
        ch_move_locked.caption = Lang.getTranslate(SECTION, "move_locked");
        ch_move_locked.SetPos(10, 38);
        ch_move_locked.SetSize(175, 21);
        ch_move_locked.checked = true;

        btn_bind_hotkeys = new GUI_Button(wnd) {
            public void DoClick() {
                EnterBindMode();
            }
        };
        btn_bind_hotkeys.caption = Lang.getTranslate(SECTION, "bind_hotkeys");
        btn_bind_hotkeys.SetSize(120, 25);
        btn_bind_hotkeys.SetPos(250, 35);

        hlist = new GUI_StringList(wnd) {
            public void DoClick() {
                SetHotbarOptions(Hotbar.hotbars.get(GetSelected()));
            }
        };
        hlist.SetPos(10, 65);
        hlist.SetSize(120, 345);

        options_panel = new GUI_Panel(wnd);
        options_panel.SetSize(350, 347);
        options_panel.SetPos(140, 65);
        options_panel.skin_element = "hint";
        options_panel.render_mode = GUI_Panel.RenderMode.rmSkin;

        general_panel = new GUI_Panel(options_panel);
        general_panel.SetSize(options_panel.size);
        position_panel = new GUI_Panel(options_panel);
        position_panel.SetSize(options_panel.size);
        visibility_panel = new GUI_Panel(options_panel);
        visibility_panel.SetSize(position_panel.size);

        //-------------------------------------------------
        btn_general = new GUI_Button(options_panel) {
            public void DoClick() {
                SetMode(0);
            }
            protected boolean getPressed() {
                return mode == 0;
            }
        };
        btn_position = new GUI_Button(options_panel) {
            public void DoClick() {
                SetMode(1);
            }
            protected boolean getPressed() {
                return mode == 1;
            }
        };
        btn_visibility = new GUI_Button(options_panel) {
            public void DoClick() {
                SetMode(2);
            }
            protected boolean getPressed() {
                return mode == 2;
            }
        };
        btn_general.caption = Lang.getTranslate(SECTION, "general");
        btn_position.caption = Lang.getTranslate(SECTION, "position");
        btn_visibility.caption = Lang.getTranslate(SECTION, "visibility");
        btn_general.SetSize(100, 25);
        btn_visibility.SetSize(btn_general.size);
        btn_position.SetSize(btn_general.size);


        int margin = 10;
        btn_general.SetPos(margin, margin);
        btn_position.SetPos(margin, margin);
        btn_position.CenterX();
        btn_visibility.SetPos(options_panel.Width()-btn_visibility.Width()-margin, margin);

        //-------------------------------------------------
        ed_name = new GUI_Edit(general_panel) {
            public void DoChanged() {
                current_hotbar.name = text;
                FillList();
                ApplyCurrentHotbar();
            }
        };
        ed_name.SetSize(120, 23);
        ed_name.SetPos(110, 45);
        lbl = new GUI_Label(general_panel);
        lbl.caption = Lang.getTranslate(SECTION, "name")+":";
        lbl.SetPos(10, ed_name.pos.y);


        ed_buttons_count = new GUI_SpinEdit(general_panel) {
            public void DoChanged() {
                current_hotbar.buttons_count = value;
                ApplyCurrentHotbar();
            }
        };
        ed_buttons_count.SetPos(125, 208);
        ed_buttons_count.SetSize(60, 25);
        ed_buttons_count.min = 1;
        ed_buttons_count.max = 40;
        //-------------------------------------------------
        ed_rows_count = new GUI_SpinEdit(general_panel) {
            public void DoChanged() {
                current_hotbar.rows_count = value;
                ApplyCurrentHotbar();
            }
        };
        ed_rows_count.SetPos(10, 208);
        ed_rows_count.SetSize(60, 25);
        ed_rows_count.min = 1;
        ed_rows_count.max = 40;

        ed_padding = new GUI_SpinEdit(general_panel) {
            public void DoChanged() {
                current_hotbar.padding = value;
                ApplyCurrentHotbar();
            }
        };
        ed_padding.SetPos(240, 208);
        ed_padding.SetSize(60, 25);
        ed_padding.min = 0;
        ed_padding.max = 40;

        lbl = new GUI_Label(general_panel);
        lbl.caption = Lang.getTranslate(SECTION, "rows_count");
        lbl.SetPos(ed_rows_count.pos.x, ed_rows_count.pos.y-LBL_Y_OFFSET);
        lbl = new GUI_Label(general_panel);
        lbl.caption = Lang.getTranslate(SECTION, "buttons_count");
        lbl.SetPos(ed_buttons_count.pos.x, ed_buttons_count.pos.y-LBL_Y_OFFSET);
        lbl = new GUI_Label(general_panel);
        lbl.caption = Lang.getTranslate(SECTION, "padding");
        lbl.SetPos(ed_padding.pos.x, ed_padding.pos.y-LBL_Y_OFFSET);

        //------------------------------------------------
        ch_enabled = new GUI_Checkbox(general_panel) {
            public void DoClick() {
                current_hotbar.enabled = checked;
                ApplyCurrentHotbar();
            }
        };
        ch_enabled.caption = Lang.getTranslate("generic", "enabled");
        ch_enabled.SetPos(10, 42+35);
        ch_enabled.SetSize(120, 21);

        ch_locked = new GUI_Checkbox(general_panel) {
            public void DoClick() {
                current_hotbar.locked = checked;
                ApplyCurrentHotbar();
            }
        };
        ch_locked.caption = Lang.getTranslate(SECTION, "locked");
        ch_locked.SetPos(125, 42+35);
        ch_locked.SetSize(120, 21);

        ch_visible = new GUI_Checkbox(general_panel) {
            public void DoClick() {
                current_hotbar.visible = checked;
                ApplyCurrentHotbar();
            }
        };
        ch_visible.caption = Lang.getTranslate(SECTION, "visible");
        ch_visible.SetPos(240, 42+35);
        ch_visible.SetSize(120, 21);


        //-----------------------------------------------
        sb_alpha = new GUI_Scrollbar(general_panel) {
            public void DoChange() {
                if (ed_alpha != null) {
                    ed_alpha.value = sb_alpha.getPos();
                    current_hotbar.alpha = sb_alpha.getPos();
                    ApplyCurrentHotbar();
                }
            }
        };
        sb_alpha.SetVertical(false);
        sb_alpha.SetMax(305);
        sb_alpha.SetMin(0);
        sb_alpha.SetPageSize(50);
        sb_alpha.SetWidth(120);
        sb_alpha.SetPos(10, 92+35);

        lbl = new GUI_Label(general_panel);
        lbl.caption = Lang.getTranslate(SECTION, "alpha");
        lbl.SetPos(sb_alpha.pos.x, sb_alpha.pos.y-LBL_Y_OFFSET);

        ed_alpha = new GUI_SpinEdit(general_panel) {
            public void DoChanged() {
                current_hotbar.alpha = value;
                sb_alpha.SetPos(value);
                ApplyCurrentHotbar();
            }
        };
        ed_alpha.SetSize(60, 25);
        ed_alpha.SetPos(sb_alpha.pos.x + (sb_alpha.Width()-ed_alpha.Width())/2, sb_alpha.pos.y+20);
        ed_alpha.min = 1;
        ed_alpha.max = 255;
        //----------------------------------------------
        sb_alpha_bg = new GUI_Scrollbar(general_panel) {
            public void DoChange() {
                if (ed_alpha_bg != null) {
                    ed_alpha_bg.value = sb_alpha_bg.getPos();
                    current_hotbar.alpha_bg = sb_alpha_bg.getPos();
                    ApplyCurrentHotbar();
                }
            }
        };
        sb_alpha_bg.SetVertical(false);
        sb_alpha_bg.SetMax(305);
        sb_alpha_bg.SetMin(0);
        sb_alpha_bg.SetPageSize(50);
        sb_alpha_bg.SetWidth(120);
        sb_alpha_bg.SetPos(155, sb_alpha.pos.y);

        lbl = new GUI_Label(general_panel);
        lbl.caption = Lang.getTranslate(SECTION, "alpha_bg");
        lbl.SetPos(sb_alpha_bg.pos.x, sb_alpha_bg.pos.y-LBL_Y_OFFSET);

        ed_alpha_bg = new GUI_SpinEdit(general_panel) {
            public void DoChanged() {
                current_hotbar.alpha = value;
                sb_alpha_bg.SetPos(value);
                ApplyCurrentHotbar();
            }
        };
        ed_alpha_bg.SetSize(60, 25);
        ed_alpha_bg.SetPos(sb_alpha_bg.pos.x + (sb_alpha_bg.Width()-ed_alpha_bg.Width())/2, sb_alpha_bg.pos.y+20);
        ed_alpha_bg.min = 1;
        ed_alpha_bg.max = 255;

        //----------------------------------------------
        ch_hide_hotkey = new GUI_Checkbox(general_panel) {
            public void DoClick() {
                current_hotbar.hide_hotkey = checked;
                ApplyCurrentHotbar();
            }
        };
        ch_hide_hotkey.caption = Lang.getTranslate(SECTION, "hide_hotkey");
        ch_hide_hotkey.SetPos(ed_rows_count.pos.x, ed_rows_count.pos.y+40);
        ch_hide_hotkey.SetSize(120, 21);

        ch_hide_empty_slots = new GUI_Checkbox(general_panel) {
            public void DoClick() {
                current_hotbar.hide_empty_slots = checked;
                ApplyCurrentHotbar();
            }
        };
        ch_hide_empty_slots.caption = Lang.getTranslate(SECTION, "hide_empty_slots");
        ch_hide_empty_slots.SetPos(ed_rows_count.pos.x, ch_hide_hotkey.pos.y+30);
        ch_hide_empty_slots.SetSize(120, 21);

        ch_click_through = new GUI_Checkbox(general_panel) {
            public void DoClick() {
                current_hotbar.click_through = checked;
                ApplyCurrentHotbar();
            }
        };
        ch_click_through.caption = Lang.getTranslate(SECTION, "click_through");
        ch_click_through.SetPos(ed_rows_count.pos.x, ch_hide_empty_slots.pos.y+30);
        ch_click_through.SetSize(120, 21);

        //-------------------------------------------------
        ed_offset_x = new GUI_SpinEdit(position_panel) {
            public void DoChanged() {
                current_hotbar.pos.x = value;
                ApplyCurrentHotbar();
            }
        };
        ed_offset_x.SetPos(10, 60);
        ed_offset_x.SetSize(60, 25);
        ed_offset_x.min = 0;
        ed_offset_x.max = 10000;

        ed_offset_y = new GUI_SpinEdit(position_panel) {
            public void DoChanged() {
                current_hotbar.pos.y = value;
                ApplyCurrentHotbar();
            }
        };
        ed_offset_y.SetPos(155, 60);
        ed_offset_y.SetSize(60, 25);
        ed_offset_y.min = 0;
        ed_offset_y.max = 10000;

        lbl = new GUI_Label(position_panel);
        lbl.caption = Lang.getTranslate(SECTION, "offset_x");
        lbl.SetPos(ed_offset_x.pos.x, ed_offset_x.pos.y-LBL_Y_OFFSET);
        lbl = new GUI_Label(position_panel);
        lbl.caption = Lang.getTranslate(SECTION, "offset_y");
        lbl.SetPos(ed_offset_y.pos.x, ed_offset_y.pos.y-LBL_Y_OFFSET);

        SetMode(0);

        FillList();
        hlist.SetSelected(0);
        SetHotbarOptions(Hotbar.hotbars.get(0));
    }

    public void SetHotbarOptions(Hotbar h) {
        current_hotbar = h;
        if (h == null) {
            options_panel.Hide();
        } else {
            options_panel.Show();

            ed_buttons_count.value = h.buttons_count;
            ed_rows_count.value = h.rows_count;
            ed_padding.value = h.padding;
            ch_enabled.checked = h.enabled;
            ch_locked.checked = h.locked;
            ch_visible.checked = h.visible;
            sb_alpha.SetPos(h.alpha);
            sb_alpha_bg.SetPos(h.alpha_bg);
            ed_name.SetText(h.name);
            ch_click_through.checked = h.click_through;
            ch_hide_empty_slots.checked = h.hide_empty_slots;
            ch_hide_hotkey.checked = h.hide_hotkey;
            ed_offset_x.value = h.pos.x;
            ed_offset_y.value = h.pos.y;
        }
    }

    public void ApplyCurrentHotbar() {
        current_hotbar.ApplySettings();
        current_hotbar.SaveSettings();
    }


    protected void SetMode(int m) {
        mode = m;
        HideAllCtrls();
        switch (mode) {
            case 0:
                general_panel.Show();
                break;
            case 1:
                position_panel.Show();
                break;
            case 2:
                visibility_panel.Show();
                break;
        }
    }

    protected void HideAllCtrls() {
        general_panel.Hide();
        position_panel.Hide();
        visibility_panel.Hide();
    }

    public void FillList() {
        int old_sel = hlist.GetSelected();
        hlist.Clear();
        for (int i = 0; i < Hotbar.hotbars.size(); i++) {
            hlist.Add(Hotbar.hotbars.get(i).GetName());
        }
        hlist.SetSelected(old_sel);
    }

    protected void EnterBindMode() {
        wnd.Hide();

        wnd_bind = new GUI_Window(GUI.getInstance().popup) {
            protected void DoClose() {
                LeaveBindMode();
            }
        };
        wnd_bind.caption = Lang.getTranslate(SECTION, "bind_hotkeys");
        wnd_bind.SetSize(470,140);
        wnd_bind.SetPos(10, 140);
        wnd_bind.CenterX();

        GUI_Label lbl = new GUI_Label(wnd_bind);
        lbl.caption = Lang.getTranslate(SECTION, "bind_msg");
        lbl.SetPos(15, 40);
        lbl.multi_row = true;
        lbl.SetSize(wnd_bind.Width() - 20, wnd_bind.Height() - 40);

        // MOD indicators
        lbl = new GUI_Label(wnd_bind) {
            public void DoUpdate() {
                color = Input.isShiftPressed() ? Color.green : Color.gray;
            }
        };
        lbl.caption = "SHIFT";
        lbl.SetPos(15, 95);
        lbl.SetSize(100, 20);

        lbl = new GUI_Label(wnd_bind) {
            public void DoUpdate() {
                color = Input.isCtrlPressed() ? Color.green : Color.gray;
            }
        };
        lbl.caption = "CTRL";
        lbl.SetPos(120, 95);
        lbl.SetSize(100, 20);

        lbl = new GUI_Label(wnd_bind) {
            public void DoUpdate() {
                color = Input.isAltPressed() ? Color.green : Color.gray;
            }
        };
        lbl.caption = "ALT";
        lbl.SetPos(230, 95);
        lbl.SetSize(100, 20);

        GUI_Button btn = new GUI_Button(wnd_bind) {
            public void DoClick() {
                wnd_bind.Unlink();
                LeaveBindMode();
            }
        };
        btn.caption = Lang.getTranslate("generic","ok");
        btn.SetSize(100, 25);
        btn.SetPos( wnd_bind.Width() - btn.Width() - 10, wnd_bind.Height() - btn.Height() - 10 );
    }

    protected void LeaveBindMode() {
        wnd_bind = null;
        wnd.Show();
    }

    public static boolean isBindMode() {
        return Exist() && dlg.wnd_bind != null;
    }

    public boolean isMovingMode() {
        return (!isBindMode() && wnd !=null && !ch_move_locked.checked);
    }

    @Override
    public void DoHide() {
        dlg = null;
        if (wnd != null)
            wnd.Unlink();
        if (wnd_bind != null)
            wnd_bind.Unlink();
        wnd = null;
        wnd_bind = null;
    }

    static {
        Dialog.AddType("dlg_hotbars", new DialogFactory() {
            public Dialog create() {
                return new dlg_Hotbars();
            }
        });
    }

    public static boolean Exist() {
        return dlg != null;
    }
}
