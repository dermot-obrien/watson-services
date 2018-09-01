package org.dobrien.watson.services.document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
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

import com.ibm.watson.developer_cloud.discovery.v1.Discovery;
import com.ibm.watson.developer_cloud.service.exception.TooManyRequestsException;
import com.ibm.watson.developer_cloud.discovery.v1.model.AddDocumentOptions;
import com.ibm.watson.developer_cloud.discovery.v1.model.DocumentAccepted;
import com.ibm.watson.developer_cloud.discovery.v1.model.GetCollectionOptions;
// v3.5.0
//import com.ibm.watson.developer_cloud.discovery.v1.model.collection.GetCollectionRequest;
//import com.ibm.watson.developer_cloud.discovery.v1.model.collection.GetCollectionResponse;
//import com.ibm.watson.developer_cloud.discovery.v1.model.document.CreateDocumentRequest;
//import com.ibm.watson.developer_cloud.discovery.v1.model.document.CreateDocumentResponse;
//import com.ibm.watson.developer_cloud.discovery.v1.model.query.QueryRequest;
//import com.ibm.watson.developer_cloud.discovery.v1.model.query.QueryResponse;
//v3.5.1-SNAPSHOT
import com.ibm.watson.developer_cloud.discovery.v1.model.QueryOptions;
import com.ibm.watson.developer_cloud.discovery.v1.model.QueryResponse;

import org.dobrien.watson.services.Context;
import org.dobrien.watson.services.collection.CollectionService;
import org.dobrien.watson.services.model.Collection;
import org.dobrien.watson.services.model.Document;
import org.dobrien.watson.services.util.ExponentialBackOff;
import org.dobrien.watson.services.util.ExponentialBackOffFunction;

public class DocumentService {
	
	private static final Logger logger = Logger.getLogger(DocumentService.class.getName());
	
	private Configurations context;
	private CollectionService collectionService;

	public DocumentService() {
		this(Configurations.get());
		}
	public DocumentService(Configurations context) {
		this.context = context;
		collectionService = new CollectionService(context);
		}

	/**
	 * Add a document to the collection. Discovery won't accept documents if 20 or more documents are being processed.
	 * 
	 * @param path The file path.
	 * @param mediaType The HTTP media type (e.g. "application/pdf").
	 * @return The unique identifier of the ingested document.
	 */
	public String add(Collection collection,String path, String mediaType) {
		File filePath = new File(path);
		JsonObject metadata = new JsonObject();
		metadata.addProperty("filename", filePath.getName());
		return add(collection,path, mediaType, metadata);
	}

	/**
	 * Add a document to the collection. Discovery won't accept documents if 20 or more documents are being processed.
	 *
	 * @param path The file path.
	 * @param mediaType The HTTP media type (e.g. "application/pdf").
	 * @return The unique identifier of the ingested document.
	 */
	public String add(Collection collection,String path, String mediaType, JsonObject metadata) {
		try {
			Discovery discovery = context.getDiscovery();
			String waitMessage = "Too Many Requests.";
			List<Class<? extends Exception>> expectedErrors = Arrays.asList(TooManyRequestsException.class);
			DocumentAccepted createResponse = ExponentialBackOff.execute(new ExponentialBackOffFunction<DocumentAccepted>() {
				public DocumentAccepted execute() {
					try {
						File filePath = new File(path);
						if (metadata.get("filename") == null) {
							metadata.addProperty("filename", filePath.getName());
						}

						AddDocumentOptions.Builder builder = new AddDocumentOptions.Builder(collection.getEnvironmentId(), collection.getCollectionId());
					    InputStream documentStream = new FileInputStream(filePath);

						builder.filename(filePath.getName());
					    builder.file(documentStream);
					    builder.metadata(metadata.toString());
					    DocumentAccepted response = discovery.addDocument(builder.build()).execute();
					    return response;
					}
					catch(Exception e) {
						throw new RuntimeException(e);
					}
				}
				
			}, expectedErrors,waitMessage,Integer.MAX_VALUE);
			return createResponse.getDocumentId();
		}
		catch(Exception e) {
			logger.log(Level.SEVERE, "Unable to add document.", e);
			return null;
		}

	}
	
	/**
	 * Add all PDF documents in a specified folder to the collection. 
	 * 
	 * @param folderPath The folder path.
	 */
	public void addPDFDocumentsFromFolder(Collection collection,String folderPath) {
		addPDFDocumentsFromFolder(collection,folderPath,Integer.MAX_VALUE);
	}

	/**
	 * Add all PDF documents in a specified folder to the collection. 
	 * 
	 * @param folderPath The folder path.
	 */
	public void addPDFDocumentsFromFolder(Collection collection,String folderPath,int maxCount) {
		try {
			File folder = new File(folderPath);
			File[] pdfFiles = folder.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".pdf");
				}
			});

			// Get details of existing collection.

			// write the name of the file to the csv file:
			PrintWriter pw;
			try {
				String pathToCSVFile = Configurations.get().getPathToCSVFile();
				pw = new PrintWriter( new FileWriter(pathToCSVFile,true));
			} catch (FileNotFoundException e) {
				logger.log(Level.SEVERE, "Unable to get .csv file path", e);
				return;
			}

			Map<String,Document> filenameToDocumentMap = getFilenameToDocumentMap(collection);

			int addedCount = 0;
			for (int i = 0; i < pdfFiles.length && addedCount < maxCount; i++) {
				if (filenameToDocumentMap.get(pdfFiles[i].getName()) != null) {
					logger.info("Ignoring "+pdfFiles[i].getName()+" (already in collection)");
					continue;
				}
				String documentId = add(collection,pdfFiles[i].getAbsolutePath(), "application/pdf");
				logger.info("Added "+documentId+" ("+(++addedCount)+" of "+Math.min(pdfFiles.length,maxCount)+")");

				// now write the response to the csv file:
				String documentName = pdfFiles[i].getName();
				pw.println(documentId + "," +  documentName);
				logger.log(Level.INFO,"wrote to csv file: " + documentName);
				//
			}

			pw.close();
		}
		catch(Exception e) {
			logger.log(Level.SEVERE, "Unable to add document set.", e);
		}
	}

	public void addGenericJSONDocumentsFromFolder(Collection collection,String folderPath) {
		addGenericJSONDocumentsFromFolder(collection,folderPath,Integer.MAX_VALUE);
	}

	/**
	 * Add all generic (not mapped to a specific collection) JSON documents in a
	 * specified folder to the collection.
	 *
	 * @param folderPath The folder path.
	 */
	public void addGenericJSONDocumentsFromFolder(Collection collection,String folderPath,int maxCount) {
		try {
			File folder = new File(folderPath);
			File[] jsonFiles = folder.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".json");
				}
			});

			int addedCount = 0;
			for (int i = 0; i < jsonFiles.length && addedCount < maxCount; i++) {
				JsonObject metadata = new JsonObject();
				String documentId = add(collection,jsonFiles[i].getAbsolutePath(), "application/json", metadata);
				logger.info("Added "+documentId+" ("+(++addedCount)+" of "+Math.min(jsonFiles.length,maxCount)+")");
			}
		}
		catch(Exception e) {
			logger.log(Level.SEVERE, "Unable to add document set.", e);
		}
	}
	
	/**
	 * Add all generic (not mapped to a specific collection) JSON documents in a
	 * specified folder to the collection.
	 *
	 * @param folderPath The folder path.
	 */
	public String addGenericJSONDocument(Collection collection,String path) {
		try {
			String documentId = add(collection,new File(path).getAbsolutePath(), "application/json");
			return documentId;
		}
		catch(Exception e) {
			logger.log(Level.SEVERE, "Unable to add document set.", e);
			return null;
		}
	}
	
	/**
	 * Get details for all documents.
	 *
	 */
	public void getDocumentDetails(Collection collection,String reportPath) {
		try {
			// Get.
			List<Document> documents = getDocumentDetails(collection);
			
			// Open workbook.
			XSSFWorkbook outWorkbook = new XSSFWorkbook();
			XSSFSheet outSheet = outWorkbook.createSheet("Documents");
			int outRowNo = 0;
			XSSFRow outRow = outSheet.createRow(outRowNo++);
			int outColNo = 0;
			String[] header = { "Filename", "Document ID", "Title" };
			for (int i = 0; i < header.length; i++) {
				Cell outCell = outRow.createCell(outColNo++);
				outCell.setCellValue(header[i]);
			}

			// Append to workbook.
			for (Document document : documents) {
				outRow = outSheet.createRow(outRowNo++);
				outColNo = 0;
				
				Cell outCell = outRow.createCell(outColNo++);
				outCell.setCellValue(document.getFilename());

				outCell = outRow.createCell(outColNo++);
				outCell.setCellValue(document.getDocumentId());

				outCell = outRow.createCell(outColNo++);
				outCell.setCellValue(document.getTitle());
			}
			
			// Close.
			FileOutputStream outStream = new FileOutputStream(reportPath);
			outWorkbook.write(outStream);
			outWorkbook.close();
			outStream.close();
		}
		catch(Exception e) {
			logger.log(Level.SEVERE, "Unable to add document set.", e);
		}
	}	
	
	public Map<String,Document> getFilenameToDocumentMap(Collection collection) {
		try {
			Map<String,Document> map = new HashMap<String,Document>();

			List<Document> documents = getDocumentDetails(collection);
			for (Document document : documents) {
				if (document.getFilename() != null) map.put(document.getFilename(), document);
				else {
					logger.warning("Filename unexpectedly null for documetn with id "+document.getDocumentId());
				}
			}
			return map;
		}
		catch(Exception e) {
			logger.log(Level.SEVERE, "Unable to get document map.", e);
			return null;
		}
		
	}
	
	/**
	 * Get metadata for all documents in the document.
	 * 
	 * @param path The file path.
	 * @param mediaType The HTTP media type (e.g. "application/pdf").
	 * @return The unique identifier of the ingested document.
	 */
	public List<Document> getDocumentDetails(Collection collection) {
		return getDocumentDetails(collection,Arrays.asList( "extracted_metadata","metadata"));
	}

	/**
	 * Get metadata for all documents in the document.
	 *
	 * @param path The file path.
	 * @param mediaType The HTTP media type (e.g. "application/pdf").
	 * @return The unique identifier of the ingested document.
	 */
	public List<Document> getDocumentDetails(Collection collection,List<String> returnFields) {
		try {
			// Context.
			Discovery discovery = context.getDiscovery();
			
			// Get the number of documents for paging.
			collection = collectionService.getIfRequired(collection);
			@SuppressWarnings("unused")
			int available = collection.getDocumentCounts().getAvailable().intValue();
			int count = 5000;
			// Query.
			JsonObject result = getDocumentDetailsJSON(collection,returnFields,0, count);
			
			// Process results.
			JsonArray documents = result.getAsJsonArray("results");
			List<Document> list = new ArrayList<Document>();
			for (JsonElement element : documents) {
				JsonObject data = element.getAsJsonObject();
				JsonElement extractedMetadataElement = data.get("extracted_metadata");
				
				// Fields.
				JsonObject extractedMetadataObject = extractedMetadataElement == null ? null : extractedMetadataElement.getAsJsonObject();
				String title = extractedMetadataObject == null ? null : extractedMetadataObject.getAsJsonObject().get("title").getAsString();
				String documentId = data.get("id").getAsString();
				
				// Create document.
				Document document = new Document();
				document.setFilename(getFilename(data));
				document.setTitle(title);
				document.setDocumentId(documentId);
				list.add(document);
			}
			
			return list;
		}
		catch(Exception e) {
			logger.log(Level.SEVERE, "Unable to get documents.", e);
			return null;
		}
	}

	public JsonObject getDocumentDetailsJSON(Collection collection,List<String> returnFields,int offset, int count) {
		return query(collection,null,null,returnFields,offset, count);
	}

	public JsonObject query(Collection collection,String query,String filter,List<String> returnFields,int offset, int count) {
		try {
			// Do the query.
			Discovery discovery = context.getDiscovery();
			QueryOptions.Builder queryBuilder = new QueryOptions.Builder(collection.getEnvironmentId(), collection.getCollectionId());
			if (query != null) queryBuilder.query(query);
			queryBuilder.returnFields(returnFields);
			queryBuilder.offset(offset);
			queryBuilder.count(count);
			QueryResponse queryResponse = discovery.query(queryBuilder.build()).execute();
			JsonParser jsonParser = new JsonParser();
			JsonObject result = (JsonObject)jsonParser.parse(queryResponse.toString());
			return result;
		}
		catch(Exception e) {
			logger.log(Level.SEVERE, "Unable to get documents.", e);
			return null;
		}
	}	
	
	public JsonObject naturalLanguageQuery(Collection collection,String naturalLanguageQuery,String filter,List<String> returnFields,int offset, int count) {
		try {
			// Do the query.
			Discovery discovery = context.getDiscovery();
			
			// Build the query.
			QueryOptions.Builder queryBuilder = new QueryOptions.Builder(collection.getEnvironmentId(), collection.getCollectionId());
			if (naturalLanguageQuery != null) queryBuilder.naturalLanguageQuery(naturalLanguageQuery);
			queryBuilder.returnFields(returnFields);
			queryBuilder.offset(offset);
			queryBuilder.count(count);
			QueryResponse queryResponse = discovery.query(queryBuilder.build()).execute();
			JsonParser jsonParser = new JsonParser();
			JsonObject result = (JsonObject)jsonParser.parse(queryResponse.toString());
			return result;
		}
		catch(Exception e) {
			logger.log(Level.SEVERE, "Unable to get documents.", e);
			return null;
		}
	}		
	
	private String getFilename(JsonObject data) {
		JsonElement extractedMetadataElement = data.get("extracted_metadata");
		JsonObject extractedMetadataObject = extractedMetadataElement == null ? null : extractedMetadataElement.getAsJsonObject();
		JsonElement metadataElement = data.get("metadata");
		JsonObject metadataObject = metadataElement == null ? null : metadataElement.getAsJsonObject();
		
		// Filename will have been added as metadata if added by this service.
		String filename = metadataObject == null ? null : metadataObject.getAsJsonObject().get("filename").getAsString();
		
		// Otherwise filename will be added to the extracted_metadata element if added via Discovery tooling.
		if (filename == null) filename = extractedMetadataObject == null ? null : extractedMetadataObject.getAsJsonObject().get("filename").getAsString();
		
		// A value of "filename" means extracted metadata does not have a filename.
		if (filename != null && filename.equalsIgnoreCase("filename")) filename = null;
		return filename;
	}
	
	/**
	 * Pull the HTML for documents for the collection.
	 * @param collection The collection to retrieve.
	 */
	public void saveContent(Collection collection) {
		// Collection details.
		GetCollectionOptions getRequest = new GetCollectionOptions.Builder(collection.getEnvironmentId(), collection.getCollectionId()).build();
		com.ibm.watson.developer_cloud.discovery.v1.model.Collection response = context.getDiscovery().getCollection(getRequest).execute();
		System.out.print(response);
		
		// Metadata.
		QueryOptions.Builder queryBuilder = new QueryOptions.Builder(collection.getEnvironmentId(), collection.getCollectionId());
		queryBuilder.returnFields(Arrays.asList( "extracted_metadata","html"));
		QueryResponse queryResponse = context.getDiscovery().query(queryBuilder.build()).execute();
		JsonParser jsonParser = new JsonParser();
		JsonObject result = (JsonObject)jsonParser.parse(queryResponse.toString());
		JsonArray documents = result.getAsJsonArray("results");
		File folder = new File("data");
		folder.mkdirs();
		int maxCharacters = 39999;
		for (JsonElement element : documents) {
			try {
				JsonObject document = element.getAsJsonObject();
				String filename = document.get("extracted_metadata").getAsJsonObject().get("filename").getAsString();
				if (document.get("text") != null) {
					String text = document.get("text").getAsString();
					if (text.length() > maxCharacters) text = text.substring(0, maxCharacters);
					FileOutputStream fileStream = new FileOutputStream(new File(folder.getAbsolutePath()+"/"+filename+".txt"));
					OutputStreamWriter writer = new OutputStreamWriter(fileStream, StandardCharsets.UTF_8);				
					writer.write(text);
				    writer.close();
				}
				if (document.get("html") != null) {
					String text = document.get("html").getAsString();
					if (text.length() > maxCharacters) text = text.substring(0, maxCharacters);
					FileOutputStream fileStream = new FileOutputStream(new File(folder.getAbsolutePath()+"/"+filename+".html"));
					OutputStreamWriter writer = new OutputStreamWriter(fileStream, StandardCharsets.UTF_8);				
					writer.write(text);
				    writer.close();
				}
				System.out.println(filename);
			}
			catch(Exception e) {
				e.printStackTrace(System.out);
			}
		}
	}
}
