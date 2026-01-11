package com.github.nekolr.peashooter.api.sonarr.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddWebhookNotification {

    private String name;
    private List<NotificationField> fields;
    private Boolean onGrab;
    private String implementationName;
    private String implementation;
    private String configContract;
}
