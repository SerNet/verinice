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
package org.primefaces.poseidon.view.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.primefaces.poseidon.domain.Car;

@ManagedBean
public class RingView implements Serializable {

    private List<Car> cars;
    private Car selectedCar;

    @PostConstruct
    public void init() {
        cars = new ArrayList<Car>();

        cars.add(new Car("1", "Ford", 2000, "Black"));
        cars.add(new Car("2", "Audi", 2003, "Orange"));
        cars.add(new Car("4", "BMW", 2012, "Red"));
        cars.add(new Car("5", "Fiat", 2001, "Yellow"));
        cars.add(new Car("6", "Mercedes", 2014, "Blue"));
    }

    public List<Car> getCars() {
        return cars;
    }

    public void setCars(List<Car> cars) {
        this.cars = cars;
    }

    public Car getSelectedCar() {
        return selectedCar;
    }

    public void setSelectedCar(Car selectedCar) {
        this.selectedCar = selectedCar;
    }
}
