package org.dobrien.watson.services.document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class GetFileMetadata {
	
	private static Logger logger = Logger.getLogger(GetFileMetadata.class.getName());
	
	public static void main(String[] args) {
		try {
			String folderName = "C:\\Development\\Transpower\\Documents\\";
			String reportFolderName = "data\\reports\\";
			File folder = new File(folderName);
			File[] pdfFiles = folder.listFiles(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".pdf");
				}
			});
			
			// Create workbook.
			new File(reportFolderName).mkdirs();
			String outputFilename = reportFolderName+"FileMetadata.xlsx";
			XSSFWorkbook outWorkbook = new XSSFWorkbook();
			
			// Create worksheet.
			XSSFSheet outSheet = outWorkbook.createSheet("Documents");
			int outRowNo = 0;
			XSSFRow outRow = outSheet.createRow(outRowNo++);
			int outColNo = 0;
			String[] header = { "Filename", "Title", "Size", "No. of Pages", "No. of Characters", "Parts" };
			for (int i = 0; i < header.length; i++) {
				Cell outCell = outRow.createCell(outColNo++);
				outCell.setCellValue(header[i]);
			}
			
			final int MaxLength = 50000;
			for (int i = 0; i < pdfFiles.length; i++) {
				try {
					PDDocument document = PDDocument.load(pdfFiles[i]);	
					PDDocumentInformation documentInfo = document.getDocumentInformation();
					PDFTextStripper pdfStripper = new PDFTextStripper();
					PDPageTree pages = document.getPages();
					String text = pdfStripper.getText(document);
					outRow = outSheet.createRow(outRowNo++);
					outColNo = 0;
					Cell outCell = outRow.createCell(outColNo++);
					outCell.setCellValue(pdfFiles[i].getName());
					
					outCell = outRow.createCell(outColNo++);
					outCell.setCellValue(documentInfo.getTitle());

					outCell = outRow.createCell(outColNo++);
					outCell.setCellValue(Files.size(pdfFiles[i].toPath()));

					outCell = outRow.createCell(outColNo++);
					outCell.setCellValue(document.getNumberOfPages());

					outCell = outRow.createCell(outColNo++);
					outCell.setCellValue(text.length());

					outCell = outRow.createCell(outColNo++);
					outCell.setCellValue(Math.max(Math.ceil(text.length() / MaxLength),1));

					document.close();
				}
				catch(Exception e) {
					logger.warning("Can't process "+pdfFiles[i].getName()+". "+e.getMessage());
				}
			}
			
			// Close.
			FileOutputStream outStream = new FileOutputStream(outputFilename);
			outWorkbook.write(outStream);
			outWorkbook.close();
			outStream.close();
		}
		catch(Exception e) {
			logger.log(Level.SEVERE, "Failed to extract metadata.", e);
		}
	}

}
