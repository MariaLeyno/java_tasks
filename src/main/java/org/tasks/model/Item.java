package org.tasks.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

/**
 * Data object to store parameters values and state for catalog item in database.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Item implements DataObject, Comparable<Item> {

    /** Item unique identifier */
    private Integer id;
    /** Item name */
    @NonNull
    @Setter
    private String name;
    /** Item category */
    @NonNull
    @Setter
    private String category;
    /** Item brand */
    @NonNull
    @Setter
    private String brand;
    /** Item price */
    @Setter
    private Double price;

    public Item(String name, String category, String brand, Double price) {
        this(null, name, category, brand, price);
    }

    /**
     * Method compares item data object with another one by their identifiers.
     * @param o another item data object
     * @return ordering flag
     */
    @Override
    public int compareTo(Item o) {
        return id.compareTo(o.id);
    }

    /**
     * Method checks if item data object equals to another one by comparing their identifiers.
     * @param other another item data object
     * @return are they equal or not
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof Item otherItme) {
            return id.equals(otherItme.id);
        }
        return false;
    }
}