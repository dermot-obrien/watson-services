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

import org.dobrien.watson.services.domains.cim.CIMEntities;
import org.dobrien.watson.services.domains.cim.CIMEntities.Entity;
import org.dobrien.watson.services.testing.GetWatsonResultsForQuestions.DocumentMetadata;
import org.dobrien.watson.services.testing.GetWatsonResultsForQuestions.Result;

public class ExtractCIMGroupsFromEnrichments {

	private static Logger logger = Logger.getLogger(ExtractCIMGroupsFromEnrichments.class.getName());
	
	public void extract(String enrichmentsFilename, String outputFolder) {
		try {
			// Retrieve.
			JsonParser parser = new com.google.gson.JsonParser();
            JsonElement jsonElement = parser.parse(new FileReader(enrichmentsFilename));
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonArray results = jsonObject.get("results").getAsJsonArray();
            Map<String,Map<String,Integer>> typeMap = new HashMap<String,Map<String,Integer>>();
            Map<String,Entity> termToEntityMap = CIMEntities.termToEntityMap();
            Map<String,Map<String,Integer>> filenameToGroupMap = new HashMap<String,Map<String,Integer>>();
            
            List<CIMEntities.Entity> entities = CIMEntities.entities();
            for (JsonElement element : results) {
            	if (element.getAsJsonObject().get("enriched_text") == null) continue; 
               	JsonObject enrichedText = element.getAsJsonObject().get("enriched_text").getAsJsonObject();
                String filename = element.getAsJsonObject().get("extracted_metadata").getAsJsonObject().get("filename").getAsString();
            	JsonElement entitiesObject = enrichedText.get("entities");
            	if (entitiesObject == null) continue;
                JsonArray entityArray = entitiesObject.getAsJsonArray();
                for (JsonElement entity : entityArray) {
                	String type = entity.getAsJsonObject().get("type").getAsString();
                	if (!type.equals("CIM_Term")) continue; 

                    Map<String,Integer> groupMap = new HashMap<String,Integer>();
                    filenameToGroupMap.put(filename, groupMap);

                    String text = entity.getAsJsonObject().get("text").getAsString();
                	int count = entity.getAsJsonObject().get("count").getAsInt();
                	
                	String term = text.toLowerCase();
                	CIMEntities.Entity cimEntity = termToEntityMap.get(term);
                	String group;
                	if (cimEntity == null || cimEntity.group == null) {
                		group = "Unassigned";
                	}
                	else {
                		group = cimEntity.group;
                	}; 

                	Integer total = groupMap.get(group);
                	if (total == null) {
                		groupMap.put(group,count);
                	}
                	else {
                		groupMap.put(group,total+count);
                	}
                }
            }
                
			// Write.
	        save(outputFolder,filenameToGroupMap);

		}
		catch(Exception e) {
			logger.log(Level.SEVERE,"Unable to retrieve.",e);
		}
	}	
	
	private void save(String outputFolder,Map<String,Map<String,Integer>> filenameToGroupMap) {
		try {
			// Create workbook.
			XSSFWorkbook outWorkbook = new XSSFWorkbook();
			XSSFSheet outSheet = outWorkbook.createSheet("Documents");
			int outRowNo = 0;
			XSSFRow outRow = outSheet.createRow(outRowNo++);
			int outColNo = 0;
			
			// Output header.
			Cell outCell = outRow.createCell(outColNo++);
			outCell.setCellValue("Filename");
			outCell = outRow.createCell(outColNo++);
			outCell.setCellValue("CIM Group");
			outCell = outRow.createCell(outColNo++);
			outCell.setCellValue("Count");
			
        	for (String filename : filenameToGroupMap.keySet()) {
        		Map<String,Integer> groupMap = filenameToGroupMap.get(filename);
        		
                // Get the highest hit group.
                int maxCount = 0;
                String assignedGroup = null;
                for (String group : groupMap.keySet()) {
                	Integer total = groupMap.get(group);
					if (total > maxCount) {
						assignedGroup = group;
					}
				}

                for (String group : groupMap.keySet()) {
	        		outRow = outSheet.createRow(outRowNo++);
					outColNo = 0;
					outCell = outRow.createCell(outColNo++);
					outCell.setCellValue(filename);
					outCell = outRow.createCell(outColNo++);
					outCell.setCellValue(group);
					outCell = outRow.createCell(outColNo++);
					outCell.setCellValue(groupMap.get(group));
                }
			}
			
			// Close.
			FileOutputStream outStream = new FileOutputStream(outputFolder+"FilenameToCIMGroup.xlsx");
			outWorkbook.write(outStream);
			outWorkbook.close();
			outStream.close();
		}
		catch(Exception e) {
			logger.log(Level.SEVERE,"Unable to output results. ", e);
		}
	}	
	
	public static void main(String[] args) {
		String inputFilename = RetrieveEnrichments.EnrichmentsFilename;
		ExtractCIMGroupsFromEnrichments extractor = new ExtractCIMGroupsFromEnrichments();
		String outputFolder = "data/reports/";
		extractor.extract(inputFilename,outputFolder);
	}	
}
