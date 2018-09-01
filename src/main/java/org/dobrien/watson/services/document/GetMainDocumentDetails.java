package org.dobrien.watson.services.document;

import org.dobrien.watson.services.Context;

public class GetMainDocumentDetails {
	
	public static void main(String[] args) {
		String reportPath = "data\\reports\\DocumentDetails.xlsx";
		DocumentService documentsService = new DocumentService();
		documentsService.getDocumentDetails(Configurations.getMainCollection(), reportPath);
	}

}
