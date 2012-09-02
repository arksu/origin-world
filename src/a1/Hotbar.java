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

import a1.gui.GUI;
import a1.gui.GUI_Hotbar;

import java.util.ArrayList;
import java.util.List;

/**
 * хранение настроек хотбара и операции с ним
 */
public class Hotbar {
    /**
     * максимально поддерживаемое количество хотбаров
     */
    public static int MAX_HOTBARS = 12;

    /**
     * индекс хотбара. число от 1 до MAX_HOTBARS (максимум хотбаров)
     */
    public int idx;

    /**
     * имя присвоенное юзером
     */
    public String name;

    /**
     * прозрачность (альфа с которой будут выводиться сами слоты)
     */
    public int alpha;

    /**
     * прозрачность фона (альфа с которой будет выводиться подложка, можно выставить вообще в 0)
     */
    public int alpha_bg;

    /**
     * включен - будет ли вообще отображаться на экране и ловить хоткеи
     */
    public boolean enabled;

    /**
     * заблокирован - ничего на него не перетащить и не утащить с него
     */
    public boolean locked;

    /**
     * видим ли хотбар. может быть невидимым и реагировать на хоткеи
     */
    public boolean visible;

    /**
     * количество кнопок в баре
     */
    public int buttons_count;

    /**
     * количество строк
     */
    public int rows_count;

    /**
     * размер отступа между слотами в пикселах
     */
    public int padding;

    /**
     * не показывать хоткей на который забинден слот
     */
    public boolean hide_hotkey;

    /**
     * прокликивать насквозь
     */
    public boolean click_through;

    /**
     * не отрисовывать подложку у пустых слотов
     */
    public boolean hide_empty_slots;

    /**
     * позиция
     */
    public Coord pos;

    /**
     * слоты
     */
    public List<HotbarSlot> slots = new ArrayList<HotbarSlot>();

    public GUI_Hotbar gui_hotbar = null;


    /**
     * создать хотбар
     * @param idx указываем его индекс
     */
    public Hotbar(int idx) {
        this.idx = idx;
        LoadSettings();
        ApplySettings();
    }


    public void SaveSettings() {
        String sec = GetSection();
        CharSettings.ini.putProperty(sec, "enabled", enabled);
        CharSettings.ini.putProperty(sec, "alpha", alpha);
        CharSettings.ini.putProperty(sec, "alpha_bg", alpha_bg);
        CharSettings.ini.putProperty(sec, "name", name);
        CharSettings.ini.putProperty(sec, "buttons_count", buttons_count);
        CharSettings.ini.putProperty(sec, "click_through", click_through);
        CharSettings.ini.putProperty(sec, "hide_empty_slots", hide_empty_slots);
        CharSettings.ini.putProperty(sec, "hide_hotkey", hide_hotkey);
        CharSettings.ini.putProperty(sec, "locked", locked);
        CharSettings.ini.putProperty(sec, "padding", padding);
        CharSettings.ini.putProperty(sec, "rows_count", rows_count);
        CharSettings.ini.putProperty(sec, "pos_x", pos.x);
        CharSettings.ini.putProperty(sec, "pos_y", pos.y);
        CharSettings.ini.putProperty(sec, "visible", visible);
        CharSettings.save();
    }

    public void LoadSettings() {
        String sec = GetSection();
        // первый хотбар по умолчанию включен
        if (idx == 1) {
            enabled = CharSettings.ini.getProperty(sec, "enabled", true);
        } else {
            enabled = CharSettings.ini.getProperty(sec, "enabled", false);
        }
        alpha = CharSettings.ini.getProperty(sec, "alpha", 255);
        alpha_bg = CharSettings.ini.getProperty(sec, "alpha_bg", 120);
        name = CharSettings.ini.getProperty(sec, "name", "");
        buttons_count = CharSettings.ini.getProperty(sec, "buttons_count", 12);
        rows_count = CharSettings.ini.getProperty(sec, "rows_count", 1);
        click_through = CharSettings.ini.getProperty(sec, "click_through", false);
        hide_empty_slots = CharSettings.ini.getProperty(sec, "hide_empty_slots", false);
        hide_hotkey = CharSettings.ini.getProperty(sec, "hide_hotkey", false);
        locked = CharSettings.ini.getProperty(sec, "locked", false);
        padding = CharSettings.ini.getProperty(sec,"padding", 3);
        visible = CharSettings.ini.getProperty(sec,"visible", true);
        pos = new Coord( CharSettings.ini.getProperty(sec, "pos_x", 200), CharSettings.ini.getProperty(sec, "pos_y", 200));

        slots.clear();
        for (int i=0; i<buttons_count; i++) {
            slots.add( new HotbarSlot(this, i+1) );
        }
    }

    /**
     * применить настройки к самому хотбару (гуи контрол)
     */
    public void ApplySettings() {
        if (enabled) {
            if (gui_hotbar == null) gui_hotbar = new GUI_Hotbar(GUI.getInstance().normal, this);
            if (slots.size() > buttons_count) {
                slots.subList(buttons_count, slots.size()).clear();
            }
            if (slots.size() < buttons_count) {
                int sz = slots.size();
                for (int i=sz; i<buttons_count; i++) {
                    slots.add( new HotbarSlot(this, i+1) );
                }
            }
            gui_hotbar.visible = visible;
            gui_hotbar.UpdateState();
        } else {
            if (gui_hotbar != null) {
                gui_hotbar.Unlink();
                gui_hotbar = null;
            }
        }
    }

    public void Update() {
        for (HotbarSlot s : slots)
            s.Update();
    }

    /**
     * получить секцию для сохранения хотбара в ини файле настроек
     * @return имя секции
     */
    protected String GetSection() {
        return "hotbar_"+idx;
    }

    /**
     * получить имя хотбара. нужно для отображения в списке
     * @return название
     */
    public String GetName() {
        return name.isEmpty() ? ("bar"+idx) : name;
    }


    public static List<Hotbar> hotbars = new ArrayList<Hotbar>();

    /**
     * загрузить все хотбары
     */
    public static void LoadAll() {
        hotbars.clear();

        for (int i=0; i<MAX_HOTBARS; i++) {
            hotbars.add(new Hotbar(i+1));
        }
    }

    public static void SaveAll() {
        // сохраняем настройки в ини
        for (Hotbar h : hotbars) {
            h.SaveSettings();
        }
        // пишем на диск
        CharSettings.save();
    }

    public static void ClearAll() {
        for (Hotbar h : hotbars) {
            if (h.gui_hotbar != null)
                h.gui_hotbar.Unlink();
        }
        hotbars.clear();
    }

    public static void UpdateAll() {
        for (Hotbar h : hotbars) {
            if (h.enabled)
                h.Update();
        }
    }
}
