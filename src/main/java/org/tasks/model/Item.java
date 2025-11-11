package org.tasks.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"id", "active"})
public class Item implements DataObject, Comparable<Item> {
    @NonNull
    private UUID id;
    @NonNull
    @Setter
    private String name;
    @NonNull
    @Setter
    private String category;
    @NonNull
    @Setter
    private String brand;
    @Setter
    private Double price;
    private boolean active = true;

    public Item(String name, String category, String brand, Double price) {
        this(UUID.randomUUID(), name, category, brand, price, true);
    }

    @Override
    @JsonIgnore
    public String getPrimary() {
        return id.toString();
    }

    @Override
    public void setUnactive() {
        active = false;
    }

    @Override
    public int compareTo(Item o) {
        return id.compareTo(o.id);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Item otherItme) {
            return id.equals(otherItme.id);
        }
        return false;
    }
}
