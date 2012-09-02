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

import a1.dialogs.dlg_Game;
import a1.dialogs.dlg_Stat;
import a1.utils.Utils;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangTuple;

public class PlayerParam {
	public String type;
	public OtpErlangObject term;

	public PlayerParam(String type, OtpErlangObject term) {
		this.type = type;
		this.term = term;
        Log.debug("player param created type="+type+" data="+term.toString());
	}
	
	// обработать установку параметра объекту
	// old - старый параметр если уже был такого же типа
	public static void HandleSet(PlayerParam old, PlayerParam newp) {
		if (newp.type.equals("stamina")) {
			int val = Utils.ErlangInt(newp.term);
			if (dlg_Game.Exist()) {
				dlg_Game.dlg.SetStamina(val);
			}
		} else if (newp.type.equals("stat")) {
			if (newp.term instanceof OtpErlangTuple) {
				OtpErlangTuple exp = (OtpErlangTuple) ((OtpErlangTuple) newp.term).elementAt(0);
				OtpErlangTuple stats = (OtpErlangTuple) ((OtpErlangTuple) newp.term).elementAt(1);
				OtpErlangTuple max_fep = (OtpErlangTuple) ((OtpErlangTuple) newp.term).elementAt(2);
				
				Player.exp = new Player.Exp(exp);
				
				Player.stats.put("con",  new Player.Stat((OtpErlangTuple)stats.elementAt(1), Utils.ErlangInt(max_fep.elementAt(1))));
				Player.stats.put("str",  new Player.Stat((OtpErlangTuple)stats.elementAt(2), Utils.ErlangInt(max_fep.elementAt(2))));
				Player.stats.put("perc", new Player.Stat((OtpErlangTuple)stats.elementAt(3), Utils.ErlangInt(max_fep.elementAt(3))));
				Player.stats.put("agi",  new Player.Stat((OtpErlangTuple)stats.elementAt(4), Utils.ErlangInt(max_fep.elementAt(4))));
				Player.stats.put("dex",  new Player.Stat((OtpErlangTuple)stats.elementAt(5), Utils.ErlangInt(max_fep.elementAt(5))));
				Player.stats.put("int",  new Player.Stat((OtpErlangTuple)stats.elementAt(6), Utils.ErlangInt(max_fep.elementAt(6))));
				Player.stats.put("wis",  new Player.Stat((OtpErlangTuple)stats.elementAt(7), Utils.ErlangInt(max_fep.elementAt(7))));
				Player.stats.put("cha",  new Player.Stat((OtpErlangTuple)stats.elementAt(8), Utils.ErlangInt(max_fep.elementAt(8))));
				
				if (dlg_Stat.Exist()) {
					dlg_Stat.dlg.UpdateStats();
				}

                Log.debug("exp="+exp.toString()+" stats="+stats.toString());
			} else Main.GlobalError("error read stat param");
		}
	}
}
