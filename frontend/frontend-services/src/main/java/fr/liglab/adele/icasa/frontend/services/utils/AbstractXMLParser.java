package fr.liglab.adele.icasa.frontend.services.utils;

import fr.liglab.adele.icasa.Constants;
import fr.liglab.adele.icasa.common.xml.utils.XMLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParserException;

import java.io.*;
import java.util.*;

/**
 *
 */
public abstract class AbstractXMLParser {

    private File storageFile;

    private long timestamp;

    private File backupFile;

    private Boolean disableAccessPolicy = false;

    private static Logger logger = LoggerFactory.getLogger(Constants.ICASA_LOG + ".frontend");

    public abstract String getLocation();

    public abstract String getFileName();

    public abstract List getInfo();

    /**
     * Load the iCasa XML file and build a List of Maps.
     *
     * @return
     */
    protected List<Map> loadFile() {

        // if access policy is disable, policy is not charge from storage
        if (disableAccessPolicy) {
            return null;
        }

        storageFile = new File(getLocation(), getFileName() + ".xml");
        backupFile = new File(getLocation(), getFileName() + ".bak");
        timestamp = storageFile.lastModified();
        //List mapsInFile = null;
        List returningList = null;
        if (storageFile.exists() && !storageFile.canRead()) {
            logger.error("Unable to read XML file: " + storageFile.getAbsolutePath());
            return null;
        }
        if (!(storageFile.exists() && storageFile.canRead())) {
            return null;
        }
        FileInputStream str = null;
        try {
            str = new FileInputStream(storageFile);
            returningList = XMLUtils.readListXml(str);
            str.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return returningList;
    }

    protected synchronized boolean writeFile() {

        // if access policy is disable, policy is not saved to storage
        if (disableAccessPolicy) {
            logger.debug("Persistence is not allowed when access right is disabled");
            return true;
        }

        // Rename the current file so it may be used as a backup during the next read
        if (storageFile.exists()) {
            if (!backupFile.exists()) {
                if (!storageFile.renameTo(backupFile)) {
                    logger.error("Couldn't rename file " + storageFile + " to backup file " + backupFile);
                    return false;
                }
            } else {
                storageFile.delete();
            }
        }

        // Attempt to write the file, delete the backup and return true as
        // atomically as
        // possible. If any exception occurs, delete the new file; next time we
        // will restore
        // from the backup.
        try {
            FileOutputStream str = createFileOutputStream(storageFile);
            if (str == null) {
                return false;
            }
            XMLUtils.writeListXml(getInfo(), str);
            str.close();
            timestamp = storageFile.lastModified();

            // Writing was successful, delete the backup file if there is one.
            backupFile.delete();
            logger.debug("Persistence is performed");
            return true;
        } catch (XmlPullParserException e) {
            logger.error("writeFileLocked: Got exception:");
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("writeFileLocked: Got exception:");
            e.printStackTrace();
        }
        // Clean up an unsuccessfully written file
        if (storageFile.exists()) {
            if (!storageFile.delete()) {
                logger.error("Couldn't clean up partially-written file " + storageFile);
            }
        }
        return false;
    }



    protected FileOutputStream createFileOutputStream(File file) {
        FileOutputStream str = null;
        try {
            str = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            File parent = file.getParentFile();
            if (!parent.mkdir()) {
                logger.error("Couldn't create directory for " + "Maps file " + file);
                return null;
            }

            try {
                str = new FileOutputStream(file);
            } catch (FileNotFoundException e2) {
                logger.error("Couldn't create Maps file " + file);
                e2.printStackTrace();
            }
        }
        return str;
    }

}
