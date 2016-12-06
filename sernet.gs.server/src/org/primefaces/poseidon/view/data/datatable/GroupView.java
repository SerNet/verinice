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
package org.primefaces.poseidon.view.data.datatable;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import org.primefaces.poseidon.domain.Player;
import org.primefaces.poseidon.domain.Sale;

@ManagedBean(name="dtGroupView")
@ViewScoped
public class GroupView implements Serializable {

    private final static String[] manufacturers;
    private List<Sale> sales;

    private final static String[] playerNames;
    private List<Integer> years;
    private List<Player> players;

    static {
		manufacturers = new String[10];
		manufacturers[0] = "Apple";
		manufacturers[1] = "Samsung";
		manufacturers[2] = "Microsoft";
		manufacturers[3] = "Philips";
		manufacturers[4] = "Sony";
		manufacturers[5] = "LG";
		manufacturers[6] = "Sharp";
		manufacturers[7] = "Panasonic";
		manufacturers[8] = "HTC";
		manufacturers[9] = "Nokia";
	}

    static {
		playerNames = new String[10];
		playerNames[0] = "Lionel Messi";
		playerNames[1] = "Cristiano Ronaldo";
		playerNames[2] = "Arjen Robben";
		playerNames[3] = "Franck Ribery";
		playerNames[4] = "Ronaldinho";
		playerNames[5] = "Luis Suarez";
		playerNames[6] = "Sergio Aguero";
		playerNames[7] = "Zlatan Ibrahimovic";
		playerNames[8] = "Neymar Jr";
		playerNames[9] = "Andres Iniesta";
	}

    @PostConstruct
    public void init() {
        sales = new ArrayList<Sale>();
        for(int i = 0; i < 10; i++) {
            sales.add(new Sale(manufacturers[i], getRandomAmount(), getRandomAmount(), getRandomPercentage(), getRandomPercentage()));
        }

        years = new ArrayList<Integer>();
        years.add(2010);
        years.add(2011);
        years.add(2012);
        years.add(2013);
        years.add(2014);

        players = new ArrayList<Player>();
        for(int i = 0; i < 10; i++) {
            players.add(new Player(playerNames[i], generateRandomGoalStatsData()));
        }
    }

    public List<Sale> getSales() {
        return sales;
    }

    private int getRandomAmount() {
		return (int) (Math.random() * 100000);
	}

    private int getRandomPercentage() {
		return (int) (Math.random() * 100);
	}

    public String getLastYearTotal() {
        int total = 0;

        for(Sale sale : getSales()) {
            total += sale.getLastYearSale();
        }

        return new DecimalFormat("###,###.###").format(total);
    }

    public String getThisYearTotal() {
        int total = 0;

        for(Sale sale : getSales()) {
            total += sale.getThisYearSale();
        }

        return new DecimalFormat("###,###.###").format(total);
    }

    public List<Integer> getYears() {
        return years;
    }

    public int getYearCount() {
        return years.size();
    }

    public List<Player> getPlayers() {
        return players;
    }

    private Map<Integer,Integer> generateRandomGoalStatsData() {
        Map<Integer,Integer> stats = new LinkedHashMap<Integer, Integer>();
        for (int i = 0; i < 5; i++) {
            stats.put(years.get(i), getRandomGoals());
        }

        return stats;
    }

    private int getRandomGoals() {
        return (int) (Math.random() * 50);
    }
}
