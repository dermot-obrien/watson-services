package org.dobrien.watson.services.domains.cim;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;

public class ExtractCIMGlossary {
	
	private static Logger logger = Logger.getLogger(ExtractCIMGlossary.class.getName());
	
	private String text(Node node) {
		StringBuffer buffer = new StringBuffer();
		for (Node child : node.childNodes()) {
			if (child.childNodeSize() == 0) {
				String text = child.toString().trim();
				/* if (!text.startsWith("2.")) */ buffer.append(child.toString());
			}
		}
		return buffer.toString();
	}
	
	private void process(List<Node> nodes) {
		for (Node node : nodes) {
			if (node.toString().trim().startsWith("2.")) {
				System.out.println(text(node.parentNode()));
			}
			else {
				process(node.childNodes());
			}
		}
	}
	
	public void extractFrom(String filename) {
		try {
			File input = new File(filename);	
			Document document = Jsoup.parse(input, "UTF-8", "http://example.com/");
			process(document.childNodes());
		}
		catch(Exception e) {
			logger.log(Level.SEVERE, "Unable to extract from "+filename, e);
		}
	}
	
	public static void main(String[] args) {
		// String filename = "src/test/resources/iec61970-2{ed1.0}en.html";
		String filename = "src/test/resources/iec61968-2{ed2.0}en.pdf.html";
		new ExtractCIMGlossary().extractFrom(filename);
	}
	
}
