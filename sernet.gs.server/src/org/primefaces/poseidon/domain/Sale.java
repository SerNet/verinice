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
package org.primefaces.poseidon.domain;

import java.io.Serializable;

public class Sale implements Serializable {

    private String manufacturer;

    private int lastYearSale;

    private int thisYearSale;

    private int lastYearProfit;

    private int thisYearProfit;

    public Sale() {}

    public Sale(String manufacturer, int lastYearSale, int thisYearSale, int lastYearProfit, int thisYearProfit) {
        this.manufacturer = manufacturer;
        this.lastYearSale = lastYearSale;
        this.thisYearSale = thisYearSale;
        this.lastYearProfit = lastYearProfit;
        this.thisYearProfit = thisYearProfit;
    }

    public int getLastYearProfit() {
        return lastYearProfit;
    }

    public void setLastYearProfit(int lastYearProfit) {
        this.lastYearProfit = lastYearProfit;
    }

    public int getLastYearSale() {
        return lastYearSale;
    }

    public void setLastYearSale(int lastYearSale) {
        this.lastYearSale = lastYearSale;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public int getThisYearProfit() {
        return thisYearProfit;
    }

    public void setThisYearProfit(int thisYearProfit) {
        this.thisYearProfit = thisYearProfit;
    }

    public int getThisYearSale() {
        return thisYearSale;
    }

    public void setThisYearSale(int thisYearSale) {
        this.thisYearSale = thisYearSale;
    }
}
