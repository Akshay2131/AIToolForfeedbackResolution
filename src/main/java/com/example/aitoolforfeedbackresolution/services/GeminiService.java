package com.example.aitoolforfeedbackresolution.services;

import com.example.aitoolforfeedbackresolution.model.LOG_ERROR;
import com.example.aitoolforfeedbackresolution.repositories.LogErrorRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService{

    @Autowired
    LogErrorRepository logErrorRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getData(String queryToRun){
        if (queryToRun == null || queryToRun.trim().isEmpty()) {
            return errorJson("Empty query provided");
        }

        String normalized = queryToRun.trim().toLowerCase();
        if (!normalized.startsWith("select")) {
            return errorJson("Only SELECT queries are allowed");
        }
        if (!normalized.contains(" from ")) {
            return errorJson("Malformed SELECT query: missing FROM clause");
        }
        if (!normalized.contains("log_error")) {
            return errorJson("Query must target LOG_ERROR table");
        }

        try {
            // Try mapping to entity first for full-entity selects
            List<?> results;
            try {
                Query entityQuery = entityManager.createNativeQuery(queryToRun, LOG_ERROR.class);
                results = entityQuery.getResultList();
                return toJson(results);
            } catch (Exception mappingFailure) {
                // Fallback to untyped native query (likely scalar or partial column selection)
                Query nativeQuery = entityManager.createNativeQuery(queryToRun);
                List<?> raw = nativeQuery.getResultList();
                return toJsonUntyped(raw);
            }
        } catch (Exception e) {
            return errorJson("Failed to execute query: " + e.getMessage());
        }
    }

    private String toJson(List<?> results) throws JsonProcessingException {
        if (results == null || results.isEmpty()) {
            return successJson(List.of());
        }
        return successJson(results);
    }

    private String toJsonUntyped(List<?> raw) throws JsonProcessingException {
        if (raw == null || raw.isEmpty()) {
            return successJson(List.of());
        }
        // Normalize rows: if element is Object[], convert to List<Object>; else pass through
        List<Object> normalizedRows = new ArrayList<>();
        for (Object row : raw) {
            if (row instanceof Object[]) {
                Object[] arr = (Object[]) row;
                List<Object> list = new ArrayList<>(arr.length);
                for (Object v : arr) list.add(v);
                normalizedRows.add(list);
            } else {
                normalizedRows.add(row);
            }
        }
        return successJson(normalizedRows);
    }

    private String successJson(Object payload) throws JsonProcessingException {
        Map<String, Object> wrapper = new HashMap<>();
        wrapper.put("success", true);
        wrapper.put("data", payload);
        return objectMapper.writeValueAsString(wrapper);
    }

    private String errorJson(String message) {
        try {
            Map<String, Object> wrapper = new HashMap<>();
            wrapper.put("success", false);
            wrapper.put("error", message);
            return objectMapper.writeValueAsString(wrapper);
        } catch (JsonProcessingException e) {
            // Last-resort fallback
            return "{\"success\":false,\"error\":\"" + message.replace("\"", "'") + "\"}";
        }
    }
}
