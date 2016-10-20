/*
 * Copyright (C) 2015 xingziye
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.msu.cme.rdp.primerdesign.utils.comparator;

import edu.msu.cme.rdp.primerdesign.screenoligos.oligo.Oligo;
import java.util.Comparator;

/**
 *
 * @author xingziye
 */
public class OligoTmComparator implements Comparator<Oligo> {

    @Override
    public int compare(Oligo o1, Oligo o2) {
        Double temp1 = new Double(o1.getTm());
        Double temp2 = new Double(o2.getTm());
        return temp2.compareTo(temp1);
    }
}
