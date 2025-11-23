package org.tasks.web.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Data transfer object to send and receive parameters values and state of catalog item by HTTP.
 */
@Getter
@Setter
public class ItemDTO {

    /** Item unique identifier */
    private Integer id;
    /** Item name */
    private String name;
    /** Item category */
    private String category;
    /** Item brand */
    private String brand;
    /** Item price */
    private String price;
}
