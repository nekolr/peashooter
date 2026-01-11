package com.github.nekolr.peashooter.api.sonarr.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationField {

    private String name;
    private String label;
    private String helpText;
    private Object value;
    private String type;
    private Boolean advanced;
    private List<SelectOption> selectOptions;
    private String privacy;
    private Boolean isFloat;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SelectOption {
        private Integer value;
        private String name;
    }
}
