package org.dobrien.watson.services.analysis;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.dobrien.watson.services.testing.GetWatsonResultsForQuestions.DocumentMetadata;
import org.dobrien.watson.services.testing.GetWatsonResultsForQuestions.Result;

public class ExtractEntityCountsFromEnrichments {

	private static Logger logger = Logger.getLogger(ExtractEntityCountsFromEnrichments.class.getName());
	
	public void extract(String enrichmentsFilename, String outputFolder) {
		try {
			// Retrieve.
			JsonParser parser = new com.google.gson.JsonParser();
            JsonElement jsonElement = parser.parse(new FileReader(enrichmentsFilename));
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonArray results = jsonObject.get("results").getAsJsonArray();
            Map<String,Map<String,Integer>> typeMap = new HashMap<String,Map<String,Integer>>();
            Map<String,Integer> subjectMap = new HashMap<String,Integer>();
            Map<String,Integer> conceptMap = new HashMap<String,Integer>();
            Map<String,Integer> keywordsMap = new HashMap<String,Integer>();
            for (JsonElement element : results) {
            	if (element.getAsJsonObject().get("enriched_text") == null) continue; 
            	JsonObject enrichedText = element.getAsJsonObject().get("enriched_text").getAsJsonObject();
            	JsonElement entitiesObject = enrichedText.get("entities");
            	if (entitiesObject == null) continue;
                JsonArray entities = entitiesObject.getAsJsonArray();
                for (JsonElement entity : entities) {
                	String type = entity.getAsJsonObject().get("type").getAsString();
                	String text = entity.getAsJsonObject().get("text").getAsString();
                	int count = entity.getAsJsonObject().get("count").getAsInt();
                	Map<String,Integer> map = typeMap.get(type);
                	if (map == null) {
                		map = new HashMap<String,Integer>();
                		typeMap.put(type, map);
                	}
                	Integer total = map.get(text);
                	if (total == null) {
                		map.put(text,count);
                	}
                	else {
                		map.put(text,total+count);
                	}
                }
                
                JsonArray semanticRoles = enrichedText.get("semantic_roles").getAsJsonArray();
                for (JsonElement semanticRole : semanticRoles) {
                	JsonElement subject = semanticRole.getAsJsonObject().get("subject");
                	String text = subject.getAsJsonObject().get("text").getAsString();
                	text = text.toLowerCase();
                	Integer total = subjectMap.get(text);
                	if (total == null) {
                		subjectMap.put(text,1);
                	}
                	else {
                		subjectMap.put(text,total+1);
                	}
                }
                
                JsonArray concepts = enrichedText.get("concepts").getAsJsonArray();
                for (JsonElement concept : concepts) {
                	String text = concept.getAsJsonObject().get("text").getAsString();
                	text = text.toLowerCase();
                	Integer total = conceptMap.get(text);
                	if (total == null) {
                		conceptMap.put(text,1);
                	}
                	else {
                		conceptMap.put(text,total+1);
                	}
                }
                
                JsonArray keywords = enrichedText.get("keywords").getAsJsonArray();
                for (JsonElement keyword : keywords) {
                	String text = keyword.getAsJsonObject().get("text").getAsString();
                	text = text.toLowerCase();
                	Integer total = keywordsMap.get(text);
                	if (total == null) {
                		keywordsMap.put(text,1);
                	}
                	else {
                		keywordsMap.put(text,total+1);
                	}
                }

            }
                
			// Write.
	        save(outputFolder,typeMap);

			// Write for word cloud.
	        for (String type : typeMap.keySet()) {
            	Map<String,Integer> map = typeMap.get(type);
    	        PrintWriter writer = new PrintWriter(new FileWriter(outputFolder+"EntityCount_"+type+".txt"));
            	for (String text : map.keySet()) {
            		writer.println(map.get(text)+" "+text.toLowerCase());
				}
    			writer.close();
			}

			// Write.
	        PrintWriter writer = new PrintWriter(new FileWriter(outputFolder+"Subjects.csv"));
    		writer.println("Subject,Count");
	        for (String subject : subjectMap.keySet()) {
            	Integer count = subjectMap.get(subject);
        		writer.println("\""+subject+"\","+count);
			}
			writer.close();

			// Write for word cloud.
	        writer = new PrintWriter(new FileWriter(outputFolder+"Subjects.txt"));
	        for (String subject : subjectMap.keySet()) {
            	Integer count = subjectMap.get(subject);
        		writer.println(count+" "+subject);
			}
			writer.close();
		
			// Write for word cloud.
	        writer = new PrintWriter(new FileWriter(outputFolder+"Concepts.txt"));
	        for (String concept : conceptMap.keySet()) {
            	Integer count = conceptMap.get(concept);
        		writer.println(count+" "+concept);
			}
			writer.close();

			// Write for word cloud.
	        writer = new PrintWriter(new FileWriter(outputFolder+"Keywords.txt"));
	        for (String concept : conceptMap.keySet()) {
            	Integer count = conceptMap.get(concept);
        		writer.println(count+" "+concept);
			}
			writer.close();
		}
		catch(Exception e) {
			logger.log(Level.SEVERE,"Unable to retrieve.",e);
		}
	}	
	
	private void save(String outputFolder,Map<String,Map<String,Integer>> typeMap) {
		try {
	        for (String type : typeMap.keySet()) {
				// Create workbook.
				XSSFWorkbook outWorkbook = new XSSFWorkbook();
				XSSFSheet outSheet = outWorkbook.createSheet("Subjects");
				int outRowNo = 0;
				XSSFRow outRow = outSheet.createRow(outRowNo++);
				int outColNo = 0;
				
				// Output header.
				Cell outCell = outRow.createCell(outColNo++);
				outCell.setCellValue("Type");
				outCell = outRow.createCell(outColNo++);
				outCell.setCellValue("Subject");
				outCell = outRow.createCell(outColNo++);
				outCell.setCellValue("Count");
				
            	Map<String,Integer> map = typeMap.get(type);
            	for (String text : map.keySet()) {
					outRow = outSheet.createRow(outRowNo++);
					outColNo = 0;
					outCell = outRow.createCell(outColNo++);
					outCell.setCellValue(type);
					outCell = outRow.createCell(outColNo++);
					outCell.setCellValue(text.toLowerCase());
					outCell = outRow.createCell(outColNo++);
					outCell.setCellValue(map.get(text));
				}
				
				// Close.
				FileOutputStream outStream = new FileOutputStream(outputFolder+"EntityCount_"+type+".xlsx");
				outWorkbook.write(outStream);
				outWorkbook.close();
				outStream.close();
	        }
		}
		catch(Exception e) {
			logger.log(Level.SEVERE,"Unable to output results. ", e);
		}
	}	
	
	public static void main(String[] args) {
		String inputFilename = RetrieveEnrichments.EnrichmentsFilename;
		ExtractEntityCountsFromEnrichments extractor = new ExtractEntityCountsFromEnrichments();
		String outputFolder = "data/reports/";
		extractor.extract(inputFilename,outputFolder);
	}	
}
