package org.dobrien.watson.services.testing;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.dobrien.watson.services.Context;
import org.dobrien.watson.services.document.DocumentService;
import org.dobrien.watson.services.model.Collection;
import org.dobrien.watson.services.training.QuestionMapping;
import org.dobrien.watson.services.training.QuestionMappingReader;

public class GetWatsonResultsForQuestions {
	
	private static final Logger logger = Logger.getLogger(GetWatsonResultsForQuestions.class.getName());
	
	private static final List<String> returnFields = Arrays.asList("extracted_metadata","metadata","result_metadata","enriched_text","title");
	
	public class Result {
		public String query;
		public List<DocumentMetadata> documents;
	}

	public class DocumentMetadata {
		public String uri;
		public String type;
		public String title;
		public String filename;
		
		public boolean isPDF() {
			return uri != null && uri.toLowerCase().endsWith(".pdf");
		}
	}
	
	private String getFilter(QuestionMapping question) {
		// (enriched_text.entities.type::"Topic",enriched_text.entities.text::"current transformer")|(enriched_text.entities.type::"Topic",enriched_text.entities.text::"metering equipment")
		String[] topics = question.getTopics();
		if (topics == null || topics.length == 0) return null;
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < topics.length; i++) {
			if (i > 0) buffer.append("|");
			buffer.append("(enriched_text.entities.type::\"Topic\",enriched_text.entities.text::\""+topics[i]+"\")");
		}
		return buffer.toString();
	}
	
	public List<DocumentMetadata> getMatchingDocuments(DocumentService documentsService,Collection collection,QuestionMapping question,int resultsLimit,boolean useTopics) {
		try {
			// Initialise.
			List<DocumentMetadata> documents = new ArrayList<DocumentMetadata>();
			
			// Query.
			String naturalLanguageQuery = question.getNaturalLanguageQuestion();
			String filter = useTopics ? getFilter(question) : null;
			int offset = 0;
			int count = resultsLimit;
			long start = System.currentTimeMillis();
			JsonObject resultObject = documentsService.naturalLanguageQuery(collection,naturalLanguageQuery,filter,returnFields,offset,count);
			long end = System.currentTimeMillis();
			long duration = end-start;
			logger.info("Search of "+question.getQuestion()+". "+question.getNaturalLanguageQuestion()+" took "+(duration / 1000)+" seconds");
			
			// Query might have failed.
			if (resultObject == null || resultObject.get("matching_results") == null) {
				logger.warning("No results were returned for \""+question+"\".");
				return documents;
			}
			
			// Process results.
			int resultCount = resultObject.get("matching_results").getAsInt();
			if (resultCount == 0) {
				logger.warning("No results for \""+naturalLanguageQuery+"\"");
			}
			else {
				JsonArray results = resultObject.get("results").getAsJsonArray();
				for (int i = 0; i < results.size(); i++) {
					JsonObject result = results.get(i).getAsJsonObject();
					String title = null;
					String filename = null;
					JsonObject extractedMetadata = result.get("extracted_metadata").getAsJsonObject();
					if (extractedMetadata != null) {
						JsonElement titleElement = extractedMetadata.get("title");
						if (titleElement != null) title = titleElement.getAsString();
						JsonElement filenameElement = extractedMetadata.get("filename");
						if (filenameElement != null) filename = filenameElement.getAsString();
						if (filename != null && filename.equalsIgnoreCase("filename")) {
							filename = null;
						}
					}
					JsonObject metadata = result.get("metadata").getAsJsonObject();
					if (metadata != null) {
						JsonElement filenameElement = metadata.get("filename");
						if (filenameElement != null && filename == null) {
							filename = filenameElement.getAsString();
						}
						
					}
					DocumentMetadata documentMetadata = new DocumentMetadata();
					documentMetadata.filename = filename;
					documentMetadata.title = title;
					documents.add(documentMetadata);
				}
			}
			return documents;
		}
		catch(Exception e) {
			logger.log(Level.SEVERE,"Unable to retrieve.",e);
			return null;
		}
	}	

	private List<DocumentMetadata> getMatchingDocuments(QuestionMapping question,Collection collection,int numberOfRequiredResults,boolean useTopics) {
		try {  
			// Set up.
			Configurations context = Configurations.get();
			DocumentService documentsService = new DocumentService(context);
			
			// Retrieve.
			List<DocumentMetadata> documents = getMatchingDocuments(documentsService,collection, question,numberOfRequiredResults,useTopics);

	        // Return result.
			return documents;
		}
		catch(Exception e){
			logger.log(Level.SEVERE, "Unable to search.", e);
			return null;
		}
	}
	
	private void close() {
	}

	public Result askQuestion(QuestionMapping question, Collection collection, int numberOfRequiredResults,boolean useTopics) {
		// Get filenames in order of search results for the query.
		Result result = new Result();
		result.query = question.getQuestion();
		result.documents = getMatchingDocuments(question,collection,numberOfRequiredResults,useTopics);
		return result;
	}

	public List<Result> askQuestions(List<QuestionMapping> questions, Collection collection,int numberOfQuestionsToAsk, int numberOfRequiredResults,boolean useTopics) {
		try {  
			// Do the queries.
			List<Result> results = new ArrayList<Result>();
			for (int i = 0; i < numberOfQuestionsToAsk && i < questions.size(); i++) {
				// Get filenames in order of search results for the query.
				QuestionMapping question = questions.get(i);
				Result result = askQuestion(question,collection,numberOfRequiredResults,useTopics);
				results.add(result);
			}
			
			// Done.
			close();
			return results;
		}
		catch(Exception e){
			logger.log(Level.SEVERE, "Unable to test.", e);
			return null;
		}
	}	
	
	private List<String> getT2Queries() {
		String folder = "data/training/";
		String file = folder+"Training Data.xlsx";
		String worksheet = "T2 Standards Mapping";
		List<QuestionMapping> queryMappings = new QuestionMappingReader(file,worksheet).getQueryMappings();
		List<String> queries = new ArrayList<String>();
		for (QuestionMapping queryMapping : queryMappings) {
			queries.add(queryMapping.getQuestion());
		}
		return queries;
	}
	
	public List<QuestionMapping> getQuestionMappings() {
		String folder = "data/training/";
		String file = folder+"Training Data.xlsx";
		String worksheet = "Questions";
		List<QuestionMapping> queryMappings = new QuestionMappingReader(file,worksheet).getQueryMappings();
		return queryMappings;
	}
	
	private void save(String outputFilename,List<Result> results) {
		try {
			// Create workbook.
			XSSFWorkbook outWorkbook = new XSSFWorkbook();
			XSSFSheet outSheet = outWorkbook.createSheet("Results");
			int outRowNo = 0;
			XSSFRow outRow = outSheet.createRow(outRowNo++);
			int outColNo = 0;
			
			// Output header.
			Cell outCell = outRow.createCell(outColNo++);
			outCell.setCellValue("Question");
			outCell = outRow.createCell(outColNo++);
			outCell.setCellValue("Order");
			outCell = outRow.createCell(outColNo++);
			outCell.setCellValue("Filename");
			outCell = outRow.createCell(outColNo++);
			outCell.setCellValue("Title");
			outCell = outRow.createCell(outColNo++);
			outCell.setCellValue("Link");
			
			for (Result result : results) {
				int index = 1;
				if (result.documents != null) {
					for (DocumentMetadata document : result.documents) {
						outRow = outSheet.createRow(outRowNo++);
						outColNo = 0;
						outCell = outRow.createCell(outColNo++);
						outCell.setCellValue(result.query);
						outCell = outRow.createCell(outColNo++);
						outCell.setCellValue(index++);
						outCell = outRow.createCell(outColNo++);
						outCell.setCellValue(document.filename);
						outCell = outRow.createCell(outColNo++);
						outCell.setCellValue(document.title == null ? "" : document.title);
						outCell = outRow.createCell(outColNo++);
						outCell.setCellValue(document.uri);
					}
				}
			}
			
			// Close.
			FileOutputStream outStream = new FileOutputStream(outputFilename);
			outWorkbook.write(outStream);
			outWorkbook.close();
			outStream.close();
		}
		catch(Exception e) {
			logger.log(Level.SEVERE,"Unable to output results. ", e);
		}
	}
	
	/**
	 * Fire a single query at the external site and get the results.
	 * 
	 * @param args Not used.
	 */
	public static void main(String[] args) {
		// Parameters
		int numberOfQuestionsToAsk = Integer.MAX_VALUE;
		int numberOfRequiredResults = 10;
		boolean useTopics = false;
		String collectionName = "Snapshot_003";

		// Initialise.
		String outputfolder = "data/training/";
		String outputfile = outputfolder+collectionName+"QueryResults"+(useTopics ? "WithTopics" : "")+".xlsx";
		GetWatsonResultsForQuestions getWatsonResultsForQuestions = new GetWatsonResultsForQuestions();
		
		// Get the queries.
		List<QuestionMapping> queries = getWatsonResultsForQuestions.getQuestionMappings();
		Collection collection = Configurations.get().getCollection(collectionName);		
		
		// Search the queries and print the results.
		List<Result> results = getWatsonResultsForQuestions.askQuestions(queries,collection,numberOfQuestionsToAsk,numberOfRequiredResults,useTopics);
		logger.info("Saving results to "+new File(outputfile).getAbsolutePath());
		getWatsonResultsForQuestions.save(outputfile,results);
	}
}
