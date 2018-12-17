package sernet.verinice.service.test;
/*******************************************************************************
 * Copyright (c) 2018 Jochen Kemnade.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************************/

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import sernet.gs.model.Gefaehrdung;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUtil;

public class GefaehrdungsUtilTest extends ContextConfiguration {

    @Test
    public void testRemoveGefaehrdungsUmsetzungBySameIdWithMatch() {
        GefaehrdungsUmsetzung gefaehrdungsUmsetzung = new GefaehrdungsUmsetzung(null);
        gefaehrdungsUmsetzung.setId("1");
        List<GefaehrdungsUmsetzung> list = new ArrayList<>();
        list.add(gefaehrdungsUmsetzung);

        List<GefaehrdungsUmsetzung> result = GefaehrdungsUtil.removeBySameId(list,
                gefaehrdungsUmsetzung);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(gefaehrdungsUmsetzung, result.get(0));
        Assert.assertTrue(list.isEmpty());
    }

    @Test
    public void testRemoveGefaehrdungsUmsetzungBySameIdWithoutMatch() {
        GefaehrdungsUmsetzung gefaehrdungsUmsetzung = new GefaehrdungsUmsetzung(null);
        gefaehrdungsUmsetzung.setId("1");
        GefaehrdungsUmsetzung gefaehrdungsUmsetzung2 = new GefaehrdungsUmsetzung(null);
        gefaehrdungsUmsetzung2.setId("2");

        List<GefaehrdungsUmsetzung> list = new ArrayList<>();
        list.add(gefaehrdungsUmsetzung);

        List<GefaehrdungsUmsetzung> result = GefaehrdungsUtil.removeBySameId(list,
                gefaehrdungsUmsetzung2);
        Assert.assertEquals(0, result.size());
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(gefaehrdungsUmsetzung, list.get(0));

    }

    @Test
    public void testRemoveGefaehrdungBySameIdWithMatch() {
        Gefaehrdung gefaehrdung = new Gefaehrdung();
        gefaehrdung.setId("123");
        GefaehrdungsUmsetzung gefaehrdungsUmsetzung = new GefaehrdungsUmsetzung(null);
        gefaehrdungsUmsetzung.setId("123");
        List<GefaehrdungsUmsetzung> list = new ArrayList<>();
        list.add(gefaehrdungsUmsetzung);

        List<GefaehrdungsUmsetzung> result = GefaehrdungsUtil.removeBySameId(list, gefaehrdung);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(gefaehrdungsUmsetzung, result.get(0));
        Assert.assertTrue(list.isEmpty());

    }

    @Test
    public void testRemoveGefaehrdungBySameIdWithoutMatch() {
        Gefaehrdung gefaehrdung = new Gefaehrdung();
        gefaehrdung.setId("123");
        GefaehrdungsUmsetzung gefaehrdungsUmsetzung = new GefaehrdungsUmsetzung(null);
        gefaehrdungsUmsetzung.setId("456");
        List<GefaehrdungsUmsetzung> list = new ArrayList<>();
        list.add(gefaehrdungsUmsetzung);

        List<GefaehrdungsUmsetzung> result = GefaehrdungsUtil.removeBySameId(list, gefaehrdung);
        Assert.assertTrue(result.isEmpty());
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(gefaehrdungsUmsetzung, list.get(0));
    }

    @Test
    public void testRemoveGefaehrdungBySameIdMultipleItemsWithMatch() {
        Gefaehrdung gefaehrdung = new Gefaehrdung();
        gefaehrdung.setId("123");
        GefaehrdungsUmsetzung gefaehrdungsUmsetzung1 = new GefaehrdungsUmsetzung(null);
        gefaehrdungsUmsetzung1.setId("123");
        GefaehrdungsUmsetzung gefaehrdungsUmsetzung2 = new GefaehrdungsUmsetzung(null);
        gefaehrdungsUmsetzung2.setId("456");
        List<GefaehrdungsUmsetzung> list = new ArrayList<>();
        list.add(gefaehrdungsUmsetzung1);
        list.add(gefaehrdungsUmsetzung2);

        List<GefaehrdungsUmsetzung> result = GefaehrdungsUtil.removeBySameId(list, gefaehrdung);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(gefaehrdungsUmsetzung1, result.get(0));
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(gefaehrdungsUmsetzung2, list.get(0));

    }

    @Test
    public void testRemoveGefaehrdungWithNullId() {
        Gefaehrdung gefaehrdung = new Gefaehrdung();
        GefaehrdungsUmsetzung gefaehrdungsUmsetzung = new GefaehrdungsUmsetzung(null);
        gefaehrdungsUmsetzung.setId("123");
        List<GefaehrdungsUmsetzung> list = new ArrayList<>();
        list.add(gefaehrdungsUmsetzung);

        List<GefaehrdungsUmsetzung> result = GefaehrdungsUtil.removeBySameId(list, gefaehrdung);
        Assert.assertTrue(result.isEmpty());
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(gefaehrdungsUmsetzung, list.get(0));

    }

    @Test
    public void testRemoveGefaehrdungWithNullIdInList() {
        Gefaehrdung gefaehrdung = new Gefaehrdung();
        gefaehrdung.setId("123");

        GefaehrdungsUmsetzung gefaehrdungsUmsetzung = new GefaehrdungsUmsetzung(null);
        List<GefaehrdungsUmsetzung> list = new ArrayList<>();
        list.add(gefaehrdungsUmsetzung);

        List<GefaehrdungsUmsetzung> result = GefaehrdungsUtil.removeBySameId(list, gefaehrdung);
        Assert.assertTrue(result.isEmpty());
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(gefaehrdungsUmsetzung, list.get(0));

    }

    @Test
    public void testRemoveGefaehrdungWithNullIdInListAndGefaehrdung() {
        Gefaehrdung gefaehrdung = new Gefaehrdung();

        GefaehrdungsUmsetzung gefaehrdungsUmsetzung = new GefaehrdungsUmsetzung(null);
        List<GefaehrdungsUmsetzung> list = new ArrayList<>();
        list.add(gefaehrdungsUmsetzung);

        List<GefaehrdungsUmsetzung> result = GefaehrdungsUtil.removeBySameId(list, gefaehrdung);
        Assert.assertTrue(result.isEmpty());
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(gefaehrdungsUmsetzung, list.get(0));

    }

}
