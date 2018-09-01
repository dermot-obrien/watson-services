package org.dobrien.watson.services.document;

import org.dobrien.watson.services.Context;
import org.dobrien.watson.services.cropping.PDFSplitter;
import org.dobrien.watson.services.training.UploadAllQueriesAction;
import org.dobrien.watson.services.training.UploadAllSamplesAction;

import java.io.IOException;

public class AddDocumentCollection {

	public static void showInfo () {
		System.out.println("Please enter a command line argument to specify which program to run");
		System.out.println("Use -c to crop files, -d for document service upload, -q for training query upload and -s for uploading samples");
		System.out.println("Thank you");
	}

	//ALT+SHIFT+F10

	// TODO use strategy design pattern to abstract the behavior performed
	// TODO the dynamic type of the object would determine which action is performed
	public static void main(String[] args) {

		if (args.length == 0) {
			showInfo();
			return;
		}

		String flag = args[0];

		if (flag.equals("-d")) {
			String folderPath = Configurations.get().getDocumentCollectionPath();
			DocumentService documentsService = new DocumentService();
			documentsService.addPDFDocumentsFromFolder(Configurations.getMainCollection(), folderPath,10000);
		}

		else if (flag.equals("-q")) {
			UploadAllQueriesAction uploadAllQueriesAction = new UploadAllQueriesAction();
			uploadAllQueriesAction.readAndUploadData();
		}
		else if (flag.equals("-s")) {
			UploadAllSamplesAction uploadAllSamplesAction = new UploadAllSamplesAction();
			uploadAllSamplesAction.readAndUploadData();
		}

		else if (flag.equals("-c")) {
			PDFSplitter splitter = new PDFSplitter();
			try {
				splitter.readAndSplit();
			} catch (IOException ex) {ex.printStackTrace();}
		}

		else {
			showInfo();
		}

	}

}
