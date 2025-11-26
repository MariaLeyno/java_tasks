package org.tasks.web.servlet;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.tasks.ItemException;
import org.tasks.service.stock.StockService;
import org.tasks.service.stock.errors.ItemsNotFoundException;
import org.tasks.service.stock.errors.StockServiceIsNotInstantiatedException;
import org.tasks.web.annotations.Loggable;
import org.tasks.web.dto.ItemDTO;
import org.tasks.web.dto.MessageDTO;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@WebServlet(value = "/catalog")
@Loggable
public class CatalogServlet extends AbstractHttpServlet {

    private StockService stockService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        try {
            this.stockService = new StockService();
        } catch (StockServiceIsNotInstantiatedException ex) {
            throw new ServletException(ex);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Multimap<String, String> filters = getFilters(req.getParameterMap());
        int responseCode = HttpServletResponse.SC_OK;

        Object result;
        try {
            result = stockService.findItems(filters);
        } catch (ItemsNotFoundException ex) {
            responseCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            result = new MessageDTO(ex.getMessage());
        }

        resp.setStatus(responseCode);
        resp.setContentType(APPLICATION_JSON);
        resp.getOutputStream().write(objectMapper.writeValueAsBytes(result));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        MessageDTO message = null;
        try {
            ItemDTO itemDTO = objectMapper.readValue(req.getReader(), ItemDTO.class);
            stockService.addNewItem(itemDTO);
            resp.setStatus(HttpServletResponse.SC_CREATED);
        } catch (IOException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            message = new MessageDTO(ex.getMessage());
        } catch (ItemException ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            message = new MessageDTO(ex.getMessage());
        }

        if (message != null) {
            resp.setContentType(APPLICATION_JSON);
            resp.getOutputStream().write(objectMapper.writeValueAsBytes(message));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Multimap<String, String> filters = getFilters(req.getParameterMap());
        MessageDTO message = null;
        try {
            Set<String> itemIds = stockService.findItemIds(filters);
            if (itemIds.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                ItemDTO itemDTO = objectMapper.readValue(req.getReader(), ItemDTO.class);
                stockService.updateItems(itemIds, itemDTO);
                resp.setStatus(HttpServletResponse.SC_ACCEPTED);
            }
        } catch (IOException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            message = new MessageDTO(ex.getMessage());
        } catch (ItemException ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            message = new MessageDTO(ex.getMessage());
        }

        if (message != null) {
            resp.setContentType(APPLICATION_JSON);
            resp.getOutputStream().write(objectMapper.writeValueAsBytes(message));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Multimap<String, String> filters = getFilters(req.getParameterMap());
        try {
            Set<String> itemIds = stockService.findItemIds(filters);
            if (itemIds.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                stockService.deleteItems(itemIds);
                resp.setStatus(HttpServletResponse.SC_ACCEPTED);
            }
        } catch (ItemException ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType(APPLICATION_JSON);
            resp.getOutputStream().write(objectMapper.writeValueAsBytes(new MessageDTO(ex.getMessage())));
        }
    }

    private Multimap<String, String> getFilters(Map<String, String[]> httpParameters) {
        Multimap<String, String> data = TreeMultimap.create();
        for (Map.Entry<String, String[]> entry : httpParameters.entrySet()) {
            String paramName = entry.getKey().toUpperCase();
            for (String value : entry.getValue()) {
                data.put(paramName, value);
            }
        }
        return data;
    }
}
