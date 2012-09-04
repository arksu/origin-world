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

import java.util.ArrayList;
import java.util.List;

public class Knowledge {
    public List<Skill> skills = new ArrayList<Skill>();
    public String name;
    public String base;
    public int level;

    public Knowledge(Packet pkt) {
        name = pkt.read_string_ascii();
        base = pkt.read_string_ascii();
        level = pkt.read_int();
        int count = pkt.read_int();
        while (count > 0) {
            count--;
            skills.add(new Skill(pkt));
        }
    }
}
