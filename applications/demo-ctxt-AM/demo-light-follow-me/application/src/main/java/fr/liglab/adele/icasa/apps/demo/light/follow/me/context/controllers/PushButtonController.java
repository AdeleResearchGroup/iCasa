package fr.liglab.adele.icasa.apps.demo.light.follow.me.context.controllers;

import java.util.Set;

public interface PushButtonController {

    Set<String> getButtons();

    boolean addButton(String id);

    void removeButton(String id);

    void pushAllButtons();

    void pushButton(String id);

    void pushButtonInZone(String zoneId);
}
