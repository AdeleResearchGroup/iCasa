package fr.liglab.adele.icasa.apps.demo.global;

public interface DemoApp {

    String getAppName();

    boolean getRegistrationState();

    boolean toggleRegistration();

    String getState();
}
