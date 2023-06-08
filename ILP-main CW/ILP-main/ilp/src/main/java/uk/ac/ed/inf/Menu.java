package uk.ac.ed.inf;

import java.util.List;

/**
 * Shops class representing a shop with its information
 */

public class Menu {
    public String name;
    public String location;
    public List<Item> menu;

    public static class Item {
        String item;
        Integer pence;

        @Override
        public String toString() {
            return "Item{" +
                    "item='" + item + '\'' +
                    ", pence=" + pence +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "Menu{" +
                "name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", menu=" + menu +
                '}';
    }
}
