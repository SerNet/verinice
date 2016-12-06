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
package org.primefaces.poseidon.view.dnd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import org.primefaces.event.DragDropEvent;
import org.primefaces.poseidon.domain.Car;
import org.primefaces.poseidon.service.CarService;

@ManagedBean(name = "dndCarsView")
@ViewScoped
public class DNDCarsView implements Serializable {

    @ManagedProperty("#{carService}")
    private CarService service;

    private List<Car> cars;

    private List<Car> droppedCars;

    private Car selectedCar;

    @PostConstruct
    public void init() {
        cars = service.createCars(9);
        droppedCars = new ArrayList<Car>();
    }

    public void onCarDrop(DragDropEvent ddEvent) {
        Car car = ((Car) ddEvent.getData());

        droppedCars.add(car);
        cars.remove(car);
    }

    public void setService(CarService service) {
        this.service = service;
    }

    public List<Car> getCars() {
        return cars;
    }

    public List<Car> getDroppedCars() {
        return droppedCars;
    }

    public Car getSelectedCar() {
        return selectedCar;
    }

    public void setSelectedCar(Car selectedCar) {
        this.selectedCar = selectedCar;
    }
}
