package fr.liglab.adele.icasa.apps.demo.light.follow.me.context.controllers;

import java.util.Set;

public interface LightController {

    Set<String> getLights();

    boolean addLight(String id);

    void removeLight(String id);
}
