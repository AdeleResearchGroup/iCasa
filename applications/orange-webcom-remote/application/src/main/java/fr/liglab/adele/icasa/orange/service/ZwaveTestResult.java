package fr.liglab.adele.icasa.orange.service;

/**
 * Created by aygalinc on 22/08/16.
 */
public enum  ZwaveTestResult {

    NOTTESTED("notTested"),

    ABORTED("abort"),

    FAILED("failed"),

    SUCCESS("success"),

    RUNNING("running");

    public final String status;

    ZwaveTestResult(String testStatus) {
        status = testStatus;
    }

}
