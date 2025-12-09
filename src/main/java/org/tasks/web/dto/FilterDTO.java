package org.tasks.web.dto;

import java.util.List;

public record FilterDTO(List<String> name, List<String> category, List<String> brand, List<String> price) {
}
