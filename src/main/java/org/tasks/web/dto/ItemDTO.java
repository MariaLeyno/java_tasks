package org.tasks.web.dto;

import java.util.Objects;

/**
 * Data transfer object to send and receive by REST parameters values and state of catalog item.
 */
public record ItemDTO (Integer id, String name, String category, String brand, String price) {
    public ItemDTO {
        Objects.requireNonNull(name);
        Objects.requireNonNull(category);
        Objects.requireNonNull(brand);
    }
}
