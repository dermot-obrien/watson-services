package org.dobrien.watson.services.preprocessing;

import org.junit.Ignore;
import org.junit.Test;

public class CreateJSONFromPDFTest {
	
	@Test
	@Ignore
	public void test() {
		String pdfFilename = "src/test/resources/AG 10.01.pdf";
		String jsonFilename = "data/output/AG 10.01.json";
		new CreateJSONFromPDF().create(pdfFilename, jsonFilename);
	}

	@Test
	@Ignore
	public void convertFolder() {
		String pdfFolder = "C:\\Development\\Transpower\\Documents";
		String jsonFolder = "C:\\Development\\Transpower\\Documents\\json";
		new CreateJSONFromPDF().convertDocumentsinFolder(pdfFolder, jsonFolder);
	}

}
