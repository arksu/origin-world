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

import a1.net.Packet;

public class Skill {
    public String name;
    public int level;
    public int bar;
    public int max_bar;
    public int req_exp;

    public Skill(Packet pkt) {
        name = pkt.read_string_ascii();
        level = pkt.read_int();
        bar = pkt.read_int();
        max_bar = pkt.read_int();
        req_exp = pkt.read_int();
    }
}
