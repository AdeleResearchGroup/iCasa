/**
 * 
 */
package fr.liglab.adele.habits.monitoring.db.manager.fileinstall;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import fr.liglab.adele.habits.monitoring.autonomic.manager.listeners.DPInfos;
import fr.liglab.adele.habits.monitoring.db.manager.fileinstall.model.DPInfosHandler;
import org.xml.sax.SAXException;


/**
 * Parser for .db xml configuration files.
 *
 */
public class DBFileParser {

	
	public static Set<DPInfos> parse(File dbFile) throws SAXException, IOException, ParserConfigurationException {
		
		SAXParserFactory fabrique = SAXParserFactory.newInstance();
		SAXParser parseur = fabrique.newSAXParser();
		DPInfosHandler handler = new DPInfosHandler();
		parseur.parse(dbFile, handler);
		
		return handler.getListeInfos();
	}

	public static void unload(File dbFile) {
		
	}

}
