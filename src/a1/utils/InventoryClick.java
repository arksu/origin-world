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

package a1.utils;

import a1.net.Packet;
import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangInt;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangTuple;

public class InventoryClick {
    /**
     * ид вещи по которой кликнули. 0 если в пустое место
     */
    public int objid = 0;
    /**
     * ид вещи в инвентаре которой произошел клик
     */
    public int inv_objid = 0;
    /**
     * координаты слота
     */
    public int x = 0;
    public int y = 0;
    /**
     * отступ мыши внутри слота куда кликнули
     */
    public int offset_x = 0;
    public int offset_y = 0;
    /**
     * кнопка
     */
    public int btn = 0;
    /**
     * модификаторы клавиатуры
     */
    public int mod = 0;

    public OtpErlangObject MakeTerm() {
        OtpErlangObject[] click_arr = new OtpErlangObject[9];
        click_arr[0] = new OtpErlangAtom("inv_click");
        click_arr[1] = new OtpErlangInt(objid);
        click_arr[2] = new OtpErlangInt(inv_objid);
        click_arr[3] = new OtpErlangInt(btn);
        click_arr[4] = new OtpErlangInt(mod);
        click_arr[5] = new OtpErlangInt(x);
        click_arr[6] = new OtpErlangInt(y);
        click_arr[7] = new OtpErlangInt(offset_x);
        click_arr[8] = new OtpErlangInt(offset_y);
        return new OtpErlangTuple(click_arr);
    }

    public void MakePkt(Packet p) {
        p.write_int(objid);
        p.write_int(inv_objid);
        p.write_byte((byte)btn);
        p.write_byte((byte)mod);
        p.write_byte((byte)x);
        p.write_byte((byte)y);
        p.write_word(offset_x);
        p.write_word(offset_y);
    }
}
