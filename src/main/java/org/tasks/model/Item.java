package org.tasks.model;

import lombok.*;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"id"})
public class Item implements DataObject, Comparable<Item> {
    @NonNull
    private UUID id;
    @NonNull
    private String name;
    @NonNull
    private String category;
    @NonNull
    private String brand;
    private double price;

    @Override
    public String getPrimary() {
        return id.toString();
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
