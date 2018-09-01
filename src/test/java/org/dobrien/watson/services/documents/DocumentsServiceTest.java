package org.dobrien.watson.services.documents;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import org.dobrien.watson.services.Context;
import org.dobrien.watson.services.document.DocumentService;
import org.dobrien.watson.services.model.Document;

public class DocumentsServiceTest {
	
	public void add(String path) {
		Configurations context = Configurations.get("Test");
		DocumentService documentsService = new DocumentService(context);
		String documentId = documentsService.add(context.getTestCollection(), path, "application/pdf");
		assertNotNull(documentId);
	}

	public void addJSONDocument(String documentPath) {
		Configurations context = Configurations.get("Test");
		DocumentService documentsService = new DocumentService(context);
		String documentId = documentsService.addGenericJSONDocument(Configurations.getTestCollection(),documentPath);
		assertNotNull(documentId);
	}

	@Test
	@Ignore
	public void addViaJSON() {
		add("src/test/resources/AG 10.01.pdf");
		add("src/test/resources/AG 10.05.pdf");
	}
	

	@Test
	@Ignore
	public void add() {
		add("src/test/resources/AG 10.01.pdf");
		add("src/test/resources/AG 10.05.pdf");
	}
	
	@Test
	@Ignore
	public void getDocumentsMain() {
		Configurations context = Configurations.get("Default");
		DocumentService documentsService = new DocumentService(context);
		List<Document> documents = documentsService.getDocumentDetails(context.getMainCollection());
		assertNotNull(documents);
		for (Document document : documents) {
			System.out.println(document.getFilename());
		}
		System.out.println(documents.size()+" documents");
	}	
	
	@Test
	@Ignore
	public void addPDFDocumentsFromFolder() {
		Configurations context = Configurations.get("Test");
		String folderPath = "src/test/resources";
		new File("data/reports").mkdirs();
		DocumentService documentsService = new DocumentService(context);
		documentsService.addPDFDocumentsFromFolder(context.getTestCollection(), folderPath);
	}	

	@Test
	@Ignore
	public void addJSONDocumentsFromFolderToAnalysisControlledDocuments() {
		Configurations context = Configurations.get("Controlled Documents Analysis");
		String jsonFolder = "C:\\Development\\Transpower\\Documents\\json";
		DocumentService documentsService = new DocumentService(context);
		documentsService.addGenericJSONDocumentsFromFolder(context.getCollection(), jsonFolder, 1);
//		documentsService.addGenericJSONDocumentsFromFolder(context.getCollection(), jsonFolder);
	}	

	@Test
	@Ignore
	public void addJSONDocumentsFromFolderToTest() {
		Configurations context = Configurations.get("Test");
		String jsonFolder = "C:\\Development\\Transpower\\Documents\\json";
		DocumentService documentsService = new DocumentService(context);
		documentsService.addGenericJSONDocumentsFromFolder(Configurations.getTestCollection(), jsonFolder, 10);
	}	

	@Test
	@Ignore
	public void addJSONDocumentsFromFolder() {
		Configurations context = Configurations.get("Controlled Documents");
		String jsonFolder = "C:\\Development\\Transpower\\Documents\\json";
		DocumentService documentsService = new DocumentService(context);
		documentsService.addGenericJSONDocumentsFromFolder(context.getCollection(), jsonFolder);
	}	

	@Test
	@Ignore
	public void getDocumentsTest() {
		Configurations context = Configurations.get("Test");
		DocumentService documentsService = new DocumentService(context);
		List<Document> documents = documentsService.getDocumentDetails(context.getTestCollection());
		assertNotNull(documents);
		for (Document document : documents) {
			System.out.println(document.getFilename());
		}
		System.out.println(documents.size()+" documents");
	}	
}