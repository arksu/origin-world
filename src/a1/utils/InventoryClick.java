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
}
