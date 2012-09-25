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
import a1.gui.GUI_Button;
import a1.gui.GUI_Label;
import a1.gui.GUI_SpinEdit;
import a1.net.NetGame;
import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangInt;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangTuple;

public class claim extends ObjectVisual {
    boolean is_error = false;
    boolean inited = false;
    public int claim_owner = 0;
    ClaimPersonal claim = null;

    GUI_SpinEdit ed_combat, ed_industry, ed_nature;
    GUI_Label lbl_required, lbl_total;
    GUI_Button btn_expand;
    int required_exp = 0;
    int total_exp = 0;

    @Override
    protected void Parse() {
        claim_owner = data.read_int();
        if (claim_owner == 0) {
            is_error = true;
            return;
        }

        Calc();
    }

    protected void Calc() {
        if (claim_owner == Player.CharID) {
            required_exp = 0;
            total_exp = 0;

            // ищем клайм с таким овнером
            if (Claims.claims.containsKey(claim_owner) && Claims.claims.get(claim_owner).object_id == objid) {
                // клайм есть. отобразим его состояние
                inited = true;
                claim = Claims.claims.get(claim_owner);
                Claims.expand_claim = new ClaimPersonal(claim);
                Claims.Refresh();
            } else {
                // если клайма в клиенте нет. значит клайм брошеный
                inited = false;
            }
        } else {
            is_error = true;
        }

    }

    public void RefreshClaims() {
        Calc();
        RefreshCtrls();
    }

    @Override
    protected void PlaceCtrls() {
        if (is_error) {
            GUI_Label l = new GUI_Label(wnd);
            l.caption = Lang.getTranslate("generic", "stranger_claim");
            l.SetPos(10, 50);
        } else
        if (!inited) {
            GUI_Button init_btn = new GUI_Button(wnd) {
                @Override
                public void DoClick() {
                    SendInit();
                }
            };
            wnd.SetSize(300, 150);
            init_btn.SetSize(200, 25);
            init_btn.caption = Lang.getTranslate("generic", "claim_territory");
            init_btn.Center();
        } else {
            GUI_Label lbl_expand = new GUI_Label(wnd);
            lbl_expand.caption = Lang.getTranslate("generic", "expand");
            lbl_expand.SetPos(15, 40);

            Coord btn_size = new Coord(25,25);
            GUI_Button btn_left = new GUI_Button(wnd) {
                @Override
                public void DoClick() {
                    ExpandLeft();
                }
            };
            btn_left.icon_name = "button_left";
            btn_left.SetSize(btn_size);

            GUI_Button btn_right = new GUI_Button(wnd) {
                @Override
                public void DoClick() {
                    ExpandRight();
                }
            };
            btn_right.icon_name = "button_right";
            btn_right.SetSize(btn_size);

            GUI_Button btn_up = new GUI_Button(wnd) {
                @Override
                public void DoClick() {
                    ExpandUp();
                }
            };
            btn_up.icon_name = "button_up";
            btn_up.SetSize(btn_size);

            GUI_Button btn_down = new GUI_Button(wnd) {
                @Override
                public void DoClick() {
                    ExpandDown();
                }
            };
            btn_down.icon_name = "button_down";
            btn_down.SetSize(btn_size);

            Coord bc = new Coord(130, 80);
            int btn_margin = 30;
            btn_left.SetPos(bc.sub(btn_margin,0));
            btn_right.SetPos(bc.add(btn_margin,0));
            btn_up.SetPos(bc.sub(0,btn_margin));
            btn_down.SetPos(bc.add(0,btn_margin));

            //----------------------------------------------------------------
            int ed_margin = 30;
            Coord spin_edit_sz = new Coord(100, 23);

            lbl_required = new GUI_Label(wnd);
            lbl_required.caption = make_req_caption();
            lbl_required.SetPos(15, btn_down.pos.add(0,15).add(btn_down.size).y);

            lbl_total = new GUI_Label(wnd);
            lbl_total.caption = make_total_caption();
            lbl_total.SetPos(lbl_required.pos.add(0,ed_margin));

            //----------------------------------------------------------------
            GUI_Label lbl = new GUI_Label(wnd);
            lbl.caption = Lang.getTranslate("generic","exp_combat");
            lbl.SetPos(lbl_total.pos.add(0,ed_margin));

            ed_combat = new GUI_SpinEdit(wnd) {
                @Override
                public void DoChanged() {
                    total_exp = ed_combat.value + ed_industry.value + ed_nature.value;
                    lbl_total.caption = make_total_caption();
                    refresh_spin_max();
                }
            };
            ed_combat.SetPos(lbl.pos.add(100, 0));
            ed_combat.SetSize(spin_edit_sz);
            ed_combat.step = 10;
            ed_combat.max = 0;

            lbl = new GUI_Label(wnd);
            lbl.caption = Lang.getTranslate("generic","exp_industry");
            lbl.SetPos(15, ed_combat.pos.add(0,ed_margin).y);

            ed_industry = new GUI_SpinEdit(wnd) {
                @Override
                public void DoChanged() {
                    total_exp = ed_combat.value + ed_industry.value + ed_nature.value;
                    lbl_total.caption = make_total_caption();
                    refresh_spin_max();
                }
            };
            ed_industry.SetPos(lbl.pos.add(100, 0));
            ed_industry.SetSize(spin_edit_sz);
            ed_industry.step = 10;
            ed_industry.max = 0;

            lbl = new GUI_Label(wnd);
            lbl.caption = Lang.getTranslate("generic","exp_nature");
            lbl.SetPos(15, ed_industry.pos.add(0,ed_margin).y);

            ed_nature = new GUI_SpinEdit(wnd) {
                @Override
                public void DoChanged() {
                    total_exp = ed_combat.value + ed_industry.value + ed_nature.value;
                    lbl_total.caption = make_total_caption();
                    refresh_spin_max();
                }
            };
            ed_nature.SetPos(lbl.pos.add(100, 0));
            ed_nature.SetSize(spin_edit_sz);
            ed_nature.step = 10;
            ed_nature.max = 0;

            //-----------------------------------
            btn_expand = new GUI_Button(wnd) {
                @Override
                public void DoClick() {
                    SendExpand();
                }
            };
            btn_expand.caption = Lang.getTranslate("generic", "expand");
            btn_expand.SetSize(120, 25);
            btn_expand.SetPos(lbl.pos.add(0, ed_margin));

            wnd.SetSize(300,400);
        }
    }

    private void refresh_spin_max() {
        ed_combat.max = Math.min( Player.exp.combat, required_exp - (total_exp-ed_combat.value)  );
        ed_industry.max = Math.min( Player.exp.industry, required_exp - (total_exp-ed_industry.value)  );
        ed_nature.max = Math.min( Player.exp.nature, required_exp - (total_exp-ed_nature.value)  );
    }

    private void ExpandLeft() {
		Claims.expand_claim.ExpandLeft();
        Claims.Refresh();
        RecalcRequiredExp();
    }
    private void ExpandRight() {
        Claims.expand_claim.ExpandRight();
        Claims.Refresh();
        RecalcRequiredExp();
    }
    private void ExpandUp() {
        Claims.expand_claim.ExpandUp();
        Claims.Refresh();
        RecalcRequiredExp();
    }
    private void ExpandDown() {
        Claims.expand_claim.ExpandDown();
        Claims.Refresh();
        RecalcRequiredExp();
    }

    private void RecalcRequiredExp() {
        int old = claim.rb.sub(claim.lt).add(1).area();
        int n = Claims.expand_claim.rb.sub(Claims.expand_claim.lt).add(1).area();
        required_exp = (n - old) * 100;
        lbl_required.caption = make_req_caption();
        refresh_spin_max();
    }

    @Override
    protected void OnClose() {
        Claims.expand_claim = null;
		Claims.Refresh();
    }

    private String make_req_caption() {
        return Lang.getTranslate("generic", "required") + " : " + required_exp;
    }
    private String make_total_caption() {
        return Lang.getTranslate("generic", "total") + " : " + total_exp;
    }

    private void SendInit() {
        OtpErlangObject[] arr = new OtpErlangObject[1];
        arr[0] = new OtpErlangAtom("claim_init");
        OtpErlangTuple ack = new OtpErlangTuple(arr);
        NetGame.SEND_object_visual_ack(objid, ack);
    }

    private void SendExpand() {
        OtpErlangObject[] new_rect = new OtpErlangObject[4];
        new_rect[0] = new OtpErlangInt(Claims.expand_claim.lt.x);
        new_rect[1] = new OtpErlangInt(Claims.expand_claim.lt.y);
        new_rect[2] = new OtpErlangInt(Claims.expand_claim.rb.x);
        new_rect[3] = new OtpErlangInt(Claims.expand_claim.rb.y);

        OtpErlangObject[] new_exp = new OtpErlangObject[3];
        new_exp[0] = new OtpErlangInt(ed_combat.value);
        new_exp[1] = new OtpErlangInt(ed_industry.value);
        new_exp[2] = new OtpErlangInt(ed_nature.value);



        OtpErlangObject[] arr = new OtpErlangObject[3];
        arr[0] = new OtpErlangAtom("claim_expand");
        arr[1] = new OtpErlangTuple(new_rect);
        arr[2] = new OtpErlangTuple(new_exp);
        OtpErlangTuple ack = new OtpErlangTuple(arr);
        Log.debug("claim expand: "+ack.toString());
        NetGame.SEND_object_visual_ack(objid, ack);
    }

}
