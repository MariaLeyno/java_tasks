package org.tasks.web.controllers;

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
import org.tasks.web.annotations.Loggable;
import org.tasks.web.dto.FilterDTO;
import org.tasks.web.dto.ItemDTO;
import org.tasks.web.dto.UpdateItemDTO;

import java.util.List;
import java.util.Set;

@RestController
@Loggable
public class CatalogController {

    private final StockService stockService;

    @Autowired
    public CatalogController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping(value = "/catalog", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<ItemDTO> getCatalogItems(FilterDTO filters) {
        try {
            return stockService.findItems(filters);
        } catch (ItemsNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @PostMapping(value = "/catalog", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void addNewCatalogItem(@RequestBody ItemDTO itemDTO) {
        try {
            stockService.addNewItem(itemDTO);
        } catch (ItemException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @PutMapping(value = "/catalog", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void updateCatalogItems(FilterDTO filters, @RequestBody UpdateItemDTO updateParameters) {
        try {
            Set<String> itemIds = stockService.findItemIds(filters);
            if (!itemIds.isEmpty()) {
                stockService.updateItems(itemIds, updateParameters);
            }
        } catch (ItemException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @DeleteMapping(value = "/catalog", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void deleteCatalogItems(FilterDTO filters) {
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
