/*
 * Copyright 2009-2014 PrimeTek.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.primefaces.poseidon.view;

import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;

@ManagedBean
public class MarkersView implements Serializable {

    private MapModel simpleModel1;
    private MapModel simpleModel2;
    private MapModel simpleModel3;

    @PostConstruct
    public void init() {
        addMapModel1();
        addMapModel2();
        addMapModel3();
    }

    public void addMapModel1() {
        simpleModel1 = new DefaultMapModel();

        //Shared coordinates
        LatLng coord1 = new LatLng(40.71715, -74.0004547);
        LatLng coord2 = new LatLng(40.709896, -74.0102497);
        LatLng coord3 = new LatLng(40.714141, -74.0149807);
        LatLng coord4 = new LatLng(40.705073, -74.0128997);

        Marker marker1 = new Marker(coord1, "Canal Street", null, "resources/olympos-layout/images/marker-icon-red.png");
        Marker marker2 = new Marker(coord2, "Fulton Street", null, "resources/olympos-layout/images/marker-icon-blue.png");
        Marker marker3 = new Marker(coord3, "Barclay Street", null, "resources/olympos-layout/images/marker-icon-green.png");
        Marker marker4 = new Marker(coord4, "Beaver Street", null, "resources/olympos-layout/images/marker-icon-blue.png");

        //Basic marker
        simpleModel1.addOverlay(marker1);
        simpleModel1.addOverlay(marker2);
        simpleModel1.addOverlay(marker3);
        simpleModel1.addOverlay(marker4);
    }

    public void addMapModel2() {
        simpleModel2 = new DefaultMapModel();

        //Shared coordinates
        LatLng coord1 = new LatLng(40.71715, -74.0004547);
        LatLng coord2 = new LatLng(40.709896, -74.0102497);
        LatLng coord3 = new LatLng(40.714141, -74.0149807);
        LatLng coord4 = new LatLng(40.705073, -74.0128997);

        Marker marker1 = new Marker(coord1, "Canal Street", null, "resources/olympos-layout/images/marker-icon-red.png");
        Marker marker2 = new Marker(coord2, "Fulton Street", null, "resources/olympos-layout/images/marker-icon-blue.png");
        Marker marker3 = new Marker(coord3, "Barclay Street", null, "resources/olympos-layout/images/marker-icon-green.png");
        Marker marker4 = new Marker(coord4, "Beaver Street", null, "resources/olympos-layout/images/marker-icon-blue.png");

        //Basic marker
        simpleModel2.addOverlay(marker1);
        simpleModel2.addOverlay(marker2);
        simpleModel2.addOverlay(marker3);
        simpleModel2.addOverlay(marker4);
    }

    public void addMapModel3() {
        simpleModel3 = new DefaultMapModel();

        //Shared coordinates
        LatLng coord1 = new LatLng(40.71715, -74.0004547);

        Marker marker1 = new Marker(coord1, "Canal Street", null, "resources/olympos-layout/images/marker-icon-red.svg");

        //Basic marker
        simpleModel3.addOverlay(marker1);
    }

    public MapModel getSimpleModel1() {
        return simpleModel1;
    }

    public void setSimpleModel1(MapModel simpleModel1) {
        this.simpleModel1 = simpleModel1;
    }

    public MapModel getSimpleModel2() {
        return simpleModel2;
    }

    public void setSimpleModel2(MapModel simpleModel2) {
        this.simpleModel2 = simpleModel2;
    }

    public MapModel getSimpleModel3() {
        return simpleModel3;
    }

    public void setSimpleModel3(MapModel simpleModel3) {
        this.simpleModel3 = simpleModel3;
    }


}
