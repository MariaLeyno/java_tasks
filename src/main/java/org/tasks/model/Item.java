package org.tasks.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.util.UUID;

/**
 * Data object to store parameters values and state for catalog item.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"id", "active"})
public class Item implements DataObject, Comparable<Item> {

    /** Item unique identifier, randomly generated */
    @NonNull
    private UUID id;
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
    /** Item state */
    private boolean active = true;

    public Item(String name, String category, String brand, Double price) {
        this(UUID.randomUUID(), name, category, brand, price, true);
    }

    /**
     * Method returns item id as unique identifier for item.
     * @return id as item unique identifier
     */
    @Override
    @JsonIgnore
    public String getPrimary() {
        return id.toString();
    }

    /**
     * Method changes item state to inactive.
     */
    @Override
    public void setInactive() {
        active = false;
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
