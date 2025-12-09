package org.tasks.web.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.tasks.errors.ItemException;

import org.tasks.service.stock.StockService;
import org.tasks.errors.stock.ItemsNotFoundException;
import org.tasks.starter.annotations.Auditable;
import org.tasks.starter.annotations.Loggable;
import org.tasks.starter.audit.EventType;
import org.tasks.web.dto.FilterDTO;
import org.tasks.web.dto.ItemDTO;
import org.tasks.web.dto.UpdateItemDTO;

import java.util.List;
import java.util.Set;

@RestController
@Loggable
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "catalog", description = "REST API to manage catalog items, an authentication token is required")
public class CatalogController {

    private final StockService stockService;

    @Autowired
    public CatalogController(StockService stockService) {
        this.stockService = stockService;
    }

    @Operation(summary = "Get catalog items, using filters by name, category, brand, price or without them", tags = "catalog")
    @GetMapping(value = "/catalog", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Auditable(eventType = EventType.GET_ITEMS)
    public List<ItemDTO> getCatalogItems(FilterDTO filters, HttpServletRequest request) {
        try {
            return stockService.findItems(filters);
        } catch (ItemsNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @Operation(summary = "Add a new catalog item", tags = "catalog")
    @PostMapping(value = "/catalog", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Auditable(eventType = EventType.CREATE_ITEMS)
    public void addNewCatalogItem(@RequestBody ItemDTO itemDTO, HttpServletRequest request) {
        try {
            stockService.addNewItem(itemDTO);
        } catch (ItemException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @Operation(summary = "Update catalog items, filtering them by their parameters and specifying new parameters values", tags = "catalog")
    @PutMapping(value = "/catalog", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Auditable(eventType = EventType.UPDATE_ITEMS)
    public void updateCatalogItems(FilterDTO filters, @RequestBody UpdateItemDTO updateParameters, HttpServletRequest request) {
        try {
            Set<String> itemIds = stockService.findItemIds(filters);
            if (!itemIds.isEmpty()) {
                stockService.updateItems(itemIds, updateParameters);
            }
        } catch (ItemException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @Operation(summary = "Delete catalog items, filtering them by their parameters", tags = "catalog")
    @DeleteMapping(value = "/catalog", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Auditable(eventType = EventType.DELETE_ITEMS)
    public void deleteCatalogItems(FilterDTO filters, HttpServletRequest request) {
        try {
            Set<String> itemIds = stockService.findItemIds(filters);
            if (!itemIds.isEmpty()) {
                stockService.deleteItems(itemIds);
            }
        } catch (ItemException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }
}
