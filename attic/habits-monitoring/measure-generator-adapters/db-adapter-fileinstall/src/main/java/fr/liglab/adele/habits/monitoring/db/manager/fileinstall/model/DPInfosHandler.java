package fr.liglab.adele.habits.monitoring.db.manager.fileinstall.model;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import fr.liglab.adele.habits.monitoring.autonomic.manager.listeners.DPInfos;

/**
 * XML handler for db config files.
 *
 * 
 */
public class DPInfosHandler extends DefaultHandler {

	private static final Logger logger = LoggerFactory
			.getLogger(DPInfosHandler.class);
	private Set<DPInfos> listeInfos;
	private DPInfos dpInfos;
	private boolean inDb, inId, inInterface;
	private StringBuffer buffer;

	public DPInfosHandler() {
		super();
	}

	public Set<DPInfos> getListeInfos() {
		return listeInfos;
	}

	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {

		if (qName.equals("db")) {
			listeInfos = new HashSet<DPInfos>();
			inDb = true;
		} else if (qName.equals("id")) {
			dpInfos = new DPInfos();
			try {
				dpInfos.setName(attributes.getValue("name"));
				dpInfos.setUrl(attributes.getValue("url"));
			} catch (Exception e) {
				// erreur, le contenu de id n'est pas un entier
				throw new SAXException(e);
			}
			inId = true;
		} else {
			buffer = new StringBuffer();
			if (qName.equals("interface")) {
				inInterface = true;
			} else {
				// erreur, on peut lever une exception
				throw new SAXException("Balise " + qName + " inconnue.");
			}
		}

	}

	public void endElement(String uri, String localName, String qName)
			throws SAXException {

		if (qName.equals("db")) {
			inDb = false;
		} else if (qName.equals("id")) {
			listeInfos.add(dpInfos);
			dpInfos = null;
			inId = false;
		} else if (qName.equals("interface")) {
			dpInfos.getInterfaces().add(buffer.toString());
			buffer = null;
			inInterface = false;
		} else {
			throw new SAXException("Balise " + qName + " inconnue.");
		}
	}

	public void characters(char[] ch, int start, int length)
			throws SAXException {
		String lecture = new String(ch, start, length);
		if (buffer != null)
			buffer.append(lecture);
	}

	public void startDocument() throws SAXException {
		logger.info("Debut du parsing");
	}

	public void endDocument() throws SAXException {
		logger.info("Fin du parsing");
		logger.info("Resultats du parsing");
		for (DPInfos p : listeInfos) {
			logger.info(p.toString());
		}
	}
}
