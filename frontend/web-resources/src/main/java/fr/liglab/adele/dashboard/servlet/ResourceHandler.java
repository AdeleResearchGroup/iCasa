package fr.liglab.adele.dashboard.servlet;

import org.osgi.framework.BundleContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 *
 */
public class ResourceHandler {
    protected static  StringBuilder getTemplate(BundleContext context, String resourcePath) throws IOException {
        //get template.
        URL f= context.getBundle().getResource(resourcePath);//"www/map.html"

        byte[] buf = new byte[8192];

        InputStream is = f.openStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        String line;
        StringBuilder result = new StringBuilder();
        while( ( line = reader.readLine() ) != null)
        {
            result.append(line).append("\n");
        }
        reader.close();
        return result;

    }
}
