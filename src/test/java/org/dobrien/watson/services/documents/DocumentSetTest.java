package org.dobrien.watson.services.documents;

import org.dobrien.watson.services.document.DocumentSet;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public class DocumentSetTest {

	private static String folder = "C:\\Users\\obriende\\OneDrive - Transpower\\Work\\05 Delivery\\Cognitive Search\\Document Sets\\Conductor sample test reports - for ML";

	@Test
	public void getPDFPaths() {
		DocumentSet documentsSet = new DocumentSet(folder);
		List<File> files = documentsSet.getPDFDocuments();
		for (File file : files) {
			System.out.println(file.getAbsolutePath());
		}
	}
}
