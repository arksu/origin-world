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
import a1.gui.GUI;
import a1.gui.GUI_Inventory;
import a1.gui.GUI_Window;
import a1.net.NetGame;
import a1.utils.Hand;
import a1.utils.InventoryClick;
import a1.utils.Utils;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangTuple;

import java.util.*;


public class Player {
	public static int CharID = 0; // ид моего чара
	public static String CharName = ""; // имя моего чара
	public static int GlobalTime = 0; // глобальное время сервера от начала мира в секундах
	public static int TargetID = 0; // ид объекта взятого на цель
	public static List<Buff> buffs = new ArrayList<Buff>(); // бафы
	public static Exp exp = new Exp(0, 0, 0); // опыт
	public static Map<String, Stat> stats = new HashMap<String, Stat>(); // статы 
	public static int Speed = 0; // текущая скорость движения
    public static Map<String, Knowledge> knowledges = new HashMap<String, Knowledge>();
    public static Inventory inventory = null;
    public static Hand hand = new Hand();
    public static Equip equip = new Equip();
	
	private static List<PlayerParam> params = new ArrayList<PlayerParam>(); // параметры

    public static class Stat {
		public int level;
		public int fep;
		public int max_fep;
		
		public Stat (int level, int fep, int max_fep) {
			this.fep = fep;
			this.level = level;
			this.max_fep = max_fep;
		}
		
		public Stat(OtpErlangTuple term, int max_fep) {
			this.max_fep = max_fep;
			this.level = Utils.ErlangInt(term.elementAt(1));
			this.fep = Utils.ErlangInt(term.elementAt(2));
		}
	}
	
	public static class Exp {
		public int combat;
		public int industry;
		public int nature;
		
		public Exp(int combat, int industry, int nature) {
			this.combat = combat;
			this.industry = industry;
			this.nature = nature;
		}
		
		public Exp(OtpErlangTuple term) {
			this.nature = Utils.ErlangInt(term.elementAt(1));
			this.industry = Utils.ErlangInt(term.elementAt(2));
			this.combat = Utils.ErlangInt(term.elementAt(3));
		}
	}
	
	public static class Buff implements Comparable<Buff> {
		public String type;
		public int duration;
		public int time;
		
		public Buff(int time, int duration, String type) {
			this.duration = duration;
			this.time = time;
			this.type = type;
		}

		public int compareTo(Buff o) {
			return this.type.compareTo(o.type);
		}
	}
	
	public static void SetTarget(Packet pkt) {
		TargetID = pkt.read_int();
		Log.info("select target "+TargetID);
		if (dlg_Game.Exist()) {
			dlg_Game.dlg.MakeTarget(TargetID > 0);
		}
	}
	
	public static void BuffAdd(Packet pkt) {
		Log.info("buff add");
		int duration = pkt.read_int();
		int time = pkt.read_int();
		int target_id = pkt.read_int();
		String type = pkt.read_string_ascii();
		// state =  pkt.read_erlang_term();
		
		if (target_id == CharID) {
			for (Buff b : buffs) {
				if (b.type.equals(type)) {
					buffs.remove(b);
					break;
				}
			}
			buffs.add(new Player.Buff(time, duration, type));
			RebuildBuffs();
		} else Log.info("not my target objid!");
	}
	
	public static void BuffDelete(Packet pkt) {
		Log.info("buff delete");
		int target_id = pkt.read_int();
		String type = pkt.read_string_ascii();
		
		if (target_id == CharID) {
			for (Buff b : buffs) {
				if (b.type.equals(type)) {
					buffs.remove(b);
					RebuildBuffs();
					return;
				}
			}
		} else Log.info("not my target objid!");
	}
	
	public static void Clear() {
        CharID = 0;
		params.clear();
		buffs.clear();
		CharID = 0;
		GlobalTime = 0;
		TargetID = 0;
		Speed = 0;
		exp = new Exp(0, 0, 0);
		stats.clear();
        knowledges.clear();
        hand.Clear();
        equip.Clear();

        // уничтожим все окна с инвентарем
        if (inventory != null) {
            Map<Integer, GUI_Inventory> map = new HashMap<Integer, GUI_Inventory>();
            inventory.getCtrls(map);
            for (GUI_Inventory c : map.values()) {
                c.parent.Unlink();
            }
            inventory = null;
        }
	}
	
	// обновить отображение на экране
	private static void RebuildBuffs() {
		Collections.sort(buffs);
		// пересоздадим контролы для бафов
		if (dlg_Game.Exist()) {
			dlg_Game.dlg.buff_panel.Clear();
			for (Buff b : buffs) {
				dlg_Game.dlg.buff_panel.AddBuff(b);
			}
		}
	}
	
	public static int getHP() {
		Obj o = ObjCache.get(CharID);
		if (o != null) {
			ObjParam p =o.getparam("hp");
			if (p != null)
				return Utils.ErlangInt(p.term);
		}
		return 0;
	}
	
	public static void SetSpeed(Packet pkt) {
		Speed = pkt.read_int();
		
		// обновим отображение на экране
		if (dlg_Game.Exist()) {
			dlg_Game.dlg.runs_panel.HandleRunMode(Speed);
		}
	}
	
	public static void SetParam(Packet pkt) {
		String type = pkt.read_string_ascii(); 
		OtpErlangObject term = pkt.read_erlang_term();
		
		PlayerParam pp = new PlayerParam(type, term);
		
		PlayerParam old_param = null;
		// если уже есть параметр такого же типа - удалим его
		for (PlayerParam p : params) {
			if (p.type.equals(pp.type)) {
				old_param = p;
				params.remove(p);
				break;
			}
		}
		PlayerParam.HandleSet(old_param, pp);
		params.add(pp);
	}
	
	public static PlayerParam getparam(String type) {
		for (PlayerParam p : params) {
			if (p.type.equals(type))
				return p;
		}
		return null;
	}

    public static void KnowledgeAdd(Packet pkt) {
        Knowledge k = new Knowledge(pkt);
        knowledges.put(k.name, k);
        if (dlg_Stat.Exist())
            dlg_Stat.dlg.UpdateSkills();
    }

    /**
     * сервер прислал ивентарь.
     */
    public static void RecvInventory(Packet pkt) {
        Map<Integer, GUI_Inventory> old = new HashMap<Integer, GUI_Inventory>();
        if (inventory != null) inventory.getCtrls(old);
        // читаем
        inventory = new Inventory(pkt.read_int());
        inventory.Read(pkt);
        if (inventory.objid != CharID) Log.error("wrong inventory objid");

        // проходим по всем старым контролам
        for (Integer id : old.keySet()) {
            InvItem item = inventory.getItem(id);

            // если не нашли такую вещь, и это не инвентарь игрока (т.к. его инвентарь есть всегда)
            if (item == null && id != CharID) {
                old.get(id).parent.Unlink();
                continue;
            }

            // если такая вещь существует или это инвентарь игрока
            if ((item != null && item.isHaveInventory())) {
                // обновим состояние гуи объекта
                item.inv.ctrl = old.get(id);
                item.inv.ctrl.AssignInventory(item.inv);
                continue;
            }

            if (id == CharID) {
                inventory.ctrl = old.get(id);
                inventory.ctrl.AssignInventory(inventory);
            }
        }
    }

    /**
     * открыть инвентарь игрока
     */
    public static void OpenInventory() {
        if (inventory == null) return;

        if (inventory.ctrl != null) {
            inventory.ctrl.parent.Unlink();
            inventory.ctrl = null;
        } else {
            GUI_Window wnd = new GUI_Window(GUI.getInstance().normal) {
                @Override
                protected void DoClose() {
                    inventory.ctrl = null;
                }

                @Override
                public void DoMoved() {
                    Inventory.UpdateInvPos(Player.CharID, pos);
                }
            };
            wnd.SetSize( inventory.w * 33 + 20, inventory.h * 33 + 20 + GUI_Window.CAPTION_HEIGHT );
            wnd.SetPos(Inventory.getInvPos(CharID));
            wnd.caption = Lang.getTranslate("server", "player_inventory");
            wnd.resizeable = false;

            inventory.ctrl = new GUI_Inventory(wnd) {
                @Override
                public void ItemClick(InventoryClick click) {
                    Player.ItemClick(click);
                }
            };
            inventory.ctrl.AssignInventory(inventory);
            inventory.ctrl.SetPos(0, GUI_Window.CAPTION_HEIGHT+10);
            inventory.ctrl.CenterX();
        }
    }

    /**
     * обработать клик по вещи в инвентаре
     */
    public static void ItemClick(InventoryClick click) {
        // правый клик без модификаторов - открывает инвентарь
        if (click.btn == Input.MB_RIGHT && click.mod == 0) {
            InvItem item = inventory.getItem(click.objid);
            if (item != null)
                if (item.isHaveInventory()) {
                    OpenInventory(item.inv, item);
                } else {
                    NetGame.SEND_InventoryClick(click);
                }
        } else {
            NetGame.SEND_InventoryClick(click);
        }
    }

    /**
     * открыть инвентарь вещи
     * @param inv
     */
    public static void OpenInventory(Inventory inv, InvItem item) {
        if (inv.ctrl != null) {
            inv.ctrl.parent.Unlink();
            inv.ctrl = null;
        } else {
            GUI_Window wnd = new GUI_Window(GUI.getInstance().normal) {
                @Override
                protected void DoClose() {
                    // в инвентаре надо обнулить контрол
                    inventory.CtrlClosed(tagi);
                }

                @Override
                public void DoMoved() {
                    Inventory.UpdateInvPos(tagi, pos);
                }
            };
            wnd.tagi = inv.objid;
            wnd.SetSize(inv.w * 33 + 20, inv.h * 33 + 20 + GUI_Window.CAPTION_HEIGHT);
            wnd.SetPos(Inventory.getInvPos(item.objid));
            wnd.caption = Lang.getTranslate("server", item.icon_name+"_inventory");
            wnd.resizeable = false;

            inv.ctrl = new GUI_Inventory(wnd) {
                @Override
                public void ItemClick(InventoryClick click) {
                    Player.ItemClick(click);
                }
            };
            inv.ctrl.AssignInventory(inv);
            inv.ctrl.SetPos(0, GUI_Window.CAPTION_HEIGHT+10);
            inv.ctrl.CenterX();
        }
    }

    public static void RecvHand(Packet pkt) {
        hand.read(pkt);
    }

}
