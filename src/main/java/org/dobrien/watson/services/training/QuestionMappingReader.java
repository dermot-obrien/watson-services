package org.dobrien.watson.services.training;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class QuestionMappingReader {

	private static Logger logger = Logger.getLogger(QuestionMappingReader.class.getName());

	private File file;
	private String worksheet;
	private List<QuestionMapping> queryMappings;
	private Map<Integer,QuestionMapping> queryMappingsMap;
	private List<DocumentRelevance> documentRelevances;
	private Map<Integer,List<DocumentRelevance>> documentRelevanceMap;
	private List<CollectionDocument> documents;
	private Map<Integer,CollectionDocument> documentMap;

	@SuppressWarnings("unused")
	private FormulaEvaluator evaluator = null;
	
	public QuestionMappingReader(String filepath,String worksheet) {
		this.file = new File(filepath);
		this.worksheet = worksheet;
	}	
	
	public File getFile() {
		return file;
	}

	public List<QuestionMapping> getQueryMappings() {
		if (queryMappings == null) {
			queryMappings = new ArrayList<QuestionMapping>();
			extractQueryMappings();
		}
		return queryMappings;
	}
	
	private void extractQueryMappings() {
		XSSFWorkbook workbook = null;
		try {
			// Open.
			String excelFilename = getFile().getAbsolutePath();
			InputStream inputStream = new FileInputStream(excelFilename);
			workbook = new XSSFWorkbook(inputStream); 
			evaluator = workbook.getCreationHelper().createFormulaEvaluator();	
			
			// Get questions.
			XSSFSheet sheet = workbook.getSheet(worksheet);
			if (sheet == null) {
				logger.warning("No sheet named \""+worksheet+"\" in "+getFile().getAbsolutePath());
				return;
			}
			extractFromWorkheet(sheet);

			// Get rankings.
			String rankingsSheetName = "Examples";
			sheet = workbook.getSheet(rankingsSheetName);
			if (sheet == null) {
				logger.warning("No sheet named \""+rankingsSheetName+"\" in "+getFile().getAbsolutePath());
				return;
			}
			extractFromRankings(sheet);
			
			String collectionSheetName = "Collection";
			sheet = workbook.getSheet(collectionSheetName);
			if (sheet == null) {
				logger.warning("No sheet named \""+collectionSheetName+"\" in "+getFile().getAbsolutePath());
				return;
			}
			extractDocumentCollection(sheet);

			// Link.
			for (QuestionMapping queryMapping : queryMappings) {
				int id = queryMapping.getQuestionId();
				List<DocumentRelevance> documents = documentRelevanceMap.get(id);
				queryMapping.setDocumentRelevances(documents);
			}
			
			// Done.
			workbook.close();
			inputStream.close();
		}
		catch(Exception e) {
			logger.log(Level.SEVERE,"Unable to extract.", e);
		}
		finally {
			if (workbook != null) {
				try { 
					workbook.close(); 
				} 
				catch(Exception e) {
					logger.warning("Unable to close workbook. "+e.getMessage());
				}
			}
		}
	}

	private void extractFromWorkheet(XSSFSheet sheet) {
		int rowNo = 0;
		try {
			// Get column map.
			int lastRowNum = sheet.getLastRowNum();
			Map<String,Integer> nameToColumn = new HashMap<String,Integer>();
			Row header = sheet.getRow(rowNo);
			for (int colNo = 0; colNo <= header.getLastCellNum(); colNo++) {
				Cell cell = header.getCell(colNo);
				if (cell == null) continue;
				String name = cell.getStringCellValue();
				if (name == null || name.trim().length() == 0) continue;
				nameToColumn.put(name.trim(), colNo);
			}

			// Get question column no.
			Integer questionCol = nameToColumn.get("Question");
			if (questionCol == null) {
				questionCol = nameToColumn.get("Original Question");
			}
			if (questionCol == null) {
				throw new RuntimeException("Unable to find Question column in sheet "+sheet.getSheetName());
			}
			
			Integer questionIdCol = nameToColumn.get("Question ID");
			if (questionIdCol == null) {
				throw new RuntimeException("Unable to find Question ID column in sheet "+sheet.getSheetName());
			}
			
			// Get topics column no.
			Integer topicsCol = nameToColumn.get("Topic(s)");
			if (topicsCol == null) {
				throw new RuntimeException("Unable to find topics column in sheet "+sheet.getSheetName());
			}

			// Get topics column no.
			Integer nluCol = nameToColumn.get("Expanded natural language question");
			if (nluCol == null) {
				throw new RuntimeException("Unable to find \"Expanded natural language question\" column in sheet "+sheet.getSheetName());
			}
			
			// Initialise.
			queryMappingsMap = new HashMap<Integer,QuestionMapping>();

			// Get questions.
			for (rowNo = 1; rowNo <= lastRowNum; rowNo++) {
				Row row = sheet.getRow(rowNo);
				if (row == null) continue;
				
				Cell cell = row.getCell(questionCol);
				if (cell == null) continue;
				if (cell.getStringCellValue() == null) continue;
				if (cell.getStringCellValue().trim().length() == 0) continue;
				String question = cell.getStringCellValue().trim();

				cell = row.getCell(topicsCol);
				String topics = cell.getStringCellValue().trim();

				cell = row.getCell(nluCol);
				String nlQuestion = cell.getStringCellValue().trim();
				
				cell = row.getCell(questionIdCol);
				int questionId = (int)cell.getNumericCellValue();
				
				QuestionMapping queryMapping = new QuestionMapping();
				queryMappingsMap.put(questionId, queryMapping);
				queryMappings.add(queryMapping);
				queryMapping.setQuestion(question);
				queryMapping.setTopics(topics);
				queryMapping.setNaturalLanguageQuestion(nlQuestion);
				queryMapping.setQuestionId(questionId);
			}
		}
		catch(Exception e) {
			logger.log(Level.SEVERE,"Unable to extract from sheet \""+sheet.getSheetName() + "\" row "+rowNo, e);
		}			
	}	
	
	private void extractFromRankings(XSSFSheet sheet) {
		int rowNo = 0;
		try {
			// Get column map.
			int lastRowNum = sheet.getLastRowNum();
			Map<String,Integer> nameToColumn = new HashMap<String,Integer>();
			Row header = sheet.getRow(rowNo);
			for (int colNo = 0; colNo <= header.getLastCellNum(); colNo++) {
				Cell cell = header.getCell(colNo);
				if (cell == null) continue;
				String name = cell.getStringCellValue();
				if (name == null || name.trim().length() == 0) continue;
				nameToColumn.put(name.trim(), colNo);
			}

			Integer questionIdCol = nameToColumn.get("Question ID");
			if (questionIdCol == null) {
				throw new RuntimeException("Unable to find Question ID column in sheet "+sheet.getSheetName());
			}
			
			Integer documentIdCol = nameToColumn.get("Document ID");
			if (documentIdCol == null) {
				throw new RuntimeException("Unable to find Document ID column in sheet "+sheet.getSheetName());
			}
			
			Integer relevanceCol = nameToColumn.get("Relevance (0-10)");
			if (relevanceCol == null) {
				throw new RuntimeException("Unable to find \"Relevance (0-10)\" column in sheet "+sheet.getSheetName());
			}

			Integer documentFilenameCol = nameToColumn.get("Document Filename");
			if (documentFilenameCol == null) {
				throw new RuntimeException("Unable to find \"Document Filename\" column in sheet "+sheet.getSheetName());
			}

			Integer splitFileCol = nameToColumn.get("Split File");
			if (splitFileCol == null) {
				throw new RuntimeException("Unable to find \"Split File\" column in sheet "+sheet.getSheetName());
			}
			
			// Get questions.
			documentRelevances = new ArrayList<DocumentRelevance>();
			documentRelevanceMap = new HashMap<Integer,List<DocumentRelevance>>() ;
			
			for (rowNo = 1; rowNo <= lastRowNum; rowNo++) {
				Row row = sheet.getRow(rowNo);
				if (row == null) continue;
				
				Cell cell = row.getCell(questionIdCol);
				if (cell == null) continue;
				
				DocumentRelevance documentRelevance = new DocumentRelevance();
				documentRelevance.setQuestionId((int)cell.getNumericCellValue());

				cell = row.getCell(documentIdCol);
				documentRelevance.setDocumentId((int)cell.getNumericCellValue());

				cell = row.getCell(relevanceCol);
				documentRelevance.setRelevance((int)cell.getNumericCellValue());

				cell = row.getCell(documentFilenameCol);
				documentRelevance.setDocumentFilename(cell.getStringCellValue());

				cell = row.getCell(splitFileCol);
				documentRelevance.setSplitFilename(cell.getStringCellValue());
				
				List<DocumentRelevance> questionDocumentRelevances = documentRelevanceMap.get(documentRelevance.getQuestionId());
				if (questionDocumentRelevances == null) {
					questionDocumentRelevances = new ArrayList<DocumentRelevance>();
					documentRelevanceMap.put(documentRelevance.getQuestionId(), questionDocumentRelevances);
				}
				questionDocumentRelevances.add(documentRelevance);
				documentRelevances.add(documentRelevance);
			}
		}
		catch(Exception e) {
			logger.log(Level.SEVERE,"Unable to extract from sheet \""+sheet.getSheetName() + "\" row "+rowNo, e);
		}			
	}	
	
	private void extractDocumentCollection(XSSFSheet sheet) {
		int rowNo = 0;
		try {
			// Get column map.
			int lastRowNum = sheet.getLastRowNum();
			Map<String,Integer> nameToColumn = new HashMap<String,Integer>();
			Row header = sheet.getRow(rowNo);
			for (int colNo = 0; colNo <= header.getLastCellNum(); colNo++) {
				Cell cell = header.getCell(colNo);
				if (cell == null) continue;
				String name = cell.getStringCellValue();
				if (name == null || name.trim().length() == 0) continue;
				nameToColumn.put(name.trim(), colNo);
			}

			Integer documentIdCol = nameToColumn.get("Document ID");
			if (documentIdCol == null) {
				throw new RuntimeException("Unable to find \"Document ID\" column in sheet "+sheet.getSheetName());
			}
			
			Integer documentFilenameCol = nameToColumn.get("Document Filename");
			if (documentFilenameCol == null) {
				throw new RuntimeException("Unable to find \"Document Filename\" column in sheet "+sheet.getSheetName());
			}
			
			Integer watsonDocumentIDCol = nameToColumn.get("Watson Document ID");
			if (watsonDocumentIDCol == null) {
				throw new RuntimeException("Unable to find \"Watson Document ID\" column in sheet "+sheet.getSheetName());
			}

			Integer titleCol = nameToColumn.get("Title");
			if (titleCol == null) {
				throw new RuntimeException("Unable to find \"Title\" column in sheet "+sheet.getSheetName());
			}

			Integer documentNumberCol = nameToColumn.get("Document Number");
			if (documentNumberCol == null) {
				throw new RuntimeException("Unable to find \"Document Number\" column in sheet "+sheet.getSheetName());
			}

//			SharePoint Link		Type	Standard name		Activity	Size	No. of Pages	No. of Characters											
			
			// Get questions.
			documents = new ArrayList<CollectionDocument>();
			documentMap = new HashMap<Integer,CollectionDocument>() ;
			
			for (rowNo = 1; rowNo <= lastRowNum; rowNo++) {
				Row row = sheet.getRow(rowNo);
				if (row == null) continue;
				
				Cell cell = row.getCell(documentIdCol);
				if (cell == null) continue;
				
				CollectionDocument document = new CollectionDocument();
				document.setDocumentID((int)cell.getNumericCellValue());

				cell = row.getCell(documentFilenameCol);
				document.setDocumentFilename(cell.getStringCellValue());

				cell = row.getCell(watsonDocumentIDCol);
				document.setWatsonDocumentID(cell.getStringCellValue());
				
				cell = row.getCell(titleCol);
				document.setTitle(cell.getStringCellValue());
				
				cell = row.getCell(documentNumberCol);
				document.setDocumentNumber(cell.getStringCellValue());
				
				documentMap.put(document.getDocumentID(), document);
				documents.add(document);
			}
		}
		catch(Exception e) {
			logger.log(Level.SEVERE,"Unable to extract from sheet \""+sheet.getSheetName() + "\" row "+rowNo, e);
		}			
	}	
	
	public static void main(String[] args) {
		String folder = "data/training/";
		String file = folder+"Training Data.xlsx";
		String worksheet = "Questions";
		List<QuestionMapping> queryMappings = new QuestionMappingReader(file,worksheet).getQueryMappings();
		for (QuestionMapping queryMapping : queryMappings) {
			System.out.println(queryMapping.getQuestionId()+". "+queryMapping.getQuestion());
			List<DocumentRelevance> documents = queryMapping.getDocumentRelevances();
			if (documents == null) {
				logger.warning("No document rankings for "+queryMapping.getQuestionId()+". "+queryMapping.getQuestion());
			}
			else {
				for (DocumentRelevance document : documents) {
					System.out.println("   "+document.getDocumentFilename()+": "+document.getRelevance());
				}
			}
		}
	}	
}
