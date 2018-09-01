package org.dobrien.watson.services.testing;

import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.Node;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

import org.dobrien.watson.services.training.QuestionMapping;
import org.dobrien.watson.services.training.QuestionMappingReader;

public class ExternalSiteSearcher {
	
	private static final Logger logger = Logger.getLogger(ExternalSiteSearcher.class.getName());
	
	public class Result {
		public QuestionMapping query;
		public List<DocumentMetadata> documents;
	}

	public class DocumentMetadata {
		public String uri;
		public String type;
		public String title;
		public String fileTitle;
		public String name;
		
		public boolean isPDF() {
			return uri != null && uri.toLowerCase().endsWith(".pdf");
		}
	}

	private WebClient client;  
	private HtmlPage loginPage;
	private boolean pdfOnly = false;

	private HtmlPage login(String username,String password) {
		try {  
			client = new WebClient();  
			String urlText = "https://www.transpower.co.nz/";
			client.getOptions().setCssEnabled(false);  
			client.getOptions().setJavaScriptEnabled(false);  
			HtmlPage page = client.getPage(urlText);
			final HtmlForm loginForm = (HtmlForm)page.getElementById("user-login");
			final HtmlTextInput usernameField = loginForm.getInputByName("name");
			usernameField.setValueAttribute(username);
			final HtmlPasswordInput passwordField = loginForm.getInputByName("pass");
			passwordField.setValueAttribute(password);
			HtmlButton button = loginForm.getButtonByName("op");
	        page = button.click();
			return page;
		}
		catch(Exception e){
			logger.log(Level.SEVERE, "Unable to login.", e);
			return null;
		}
	}
	
	private DocumentMetadata getDocumentMetadata(HtmlPage page) {
		@SuppressWarnings("unchecked")
		List<HtmlElement> spans = (List<HtmlElement>) (List<?>)page.getByXPath("//span[@class='file']");
		if (spans == null || spans.isEmpty()) {
			return null;
		}
		Node node = spans.get(0).getFirstChild();
		if (node == null || node.getNextSibling() == null) {
			return null;
		}
		HtmlAnchor anchor = (HtmlAnchor)node.getNextSibling().getNextSibling();
		if (anchor == null || anchor.getFirstChild() == null) {
			return null;
		}
		
		List<HtmlElement> pageHeaders = (List<HtmlElement>) (List<?>)page.getByXPath("//h1[@class='page-header']");
		Node pageHeader  = pageHeaders.get(0).getFirstChild();
		
		DocumentMetadata documentMetadata = new DocumentMetadata();
		documentMetadata.uri = anchor.getHrefAttribute();
		documentMetadata.title = pageHeader.getNodeValue();
		documentMetadata.fileTitle = anchor.getAttribute("title");
		documentMetadata.type = anchor.getTypeAttribute();
		documentMetadata.name = anchor.getFirstChild().getNodeValue();
		return documentMetadata;
	}
	
	private List<DocumentMetadata> searchControlledDocuments(HtmlPage page,QuestionMapping questionMapping) {
		try {  
			// Now submit the form by clicking the button and get back the next page.
			String searchQuery = questionMapping.getNaturalLanguageQuestion();
			List<DocumentMetadata> documents = new ArrayList<DocumentMetadata>();
			String query = URLEncoder.encode(searchQuery, "UTF-8").replaceAll("\\+", "%20");
			String urlText = "https://www.transpower.co.nz/search/results/"+query+"?f%5B0%5D=bundle%3Acontrolled_document";
			long start = System.currentTimeMillis();
			page = client.getPage(urlText);
			long end = System.currentTimeMillis();
			long duration = end-start;
			logger.info("Search of "+questionMapping.getQuestionId()+". "+searchQuery+" took "+(duration / 1000)+" seconds");
			@SuppressWarnings("unchecked")
			List<HtmlElement> results = (List<HtmlElement>) (List<?>)page.getByXPath("//li[@class='search-result']");
			if (results !=  null && !results.isEmpty()) {
				int index = 1;
				for (HtmlElement result : results) {
					Node node = result.getFirstChild().getNextSibling();
					HtmlAnchor anchor = (HtmlAnchor ) node.getFirstChild().getNextSibling();
					HtmlPage documentPage = anchor.click();
					DocumentMetadata documentMetadata = getDocumentMetadata(documentPage);
					if (documentMetadata != null) {
						if (documentMetadata.isPDF() || !pdfOnly) {
							documents.add(documentMetadata);
						}
						else {
							logger.warning("Result "+index+" not a PDF ("+documentMetadata.type+") for query \""+query+"\"");
						}
					}
					else {
						logger.warning("Can't get document metadata from result "+index+" for query \""+query+"\"");
					}
					index++;
				}
			}

	        return documents;
		}
		catch(Exception e){
			logger.log(Level.SEVERE, "Unable to search.", e);
			return null;
		}
	}
	
	private void close() {
		client.close();
	}

	public Result test(HtmlPage loginPage,QuestionMapping query) {
		// Get filenames in order of search results for the query.
		Result result = new Result();
		result.query = query;
		result.documents = searchControlledDocuments(loginPage,query);
		return result;
	}

	public Result test(QuestionMapping query) {
		// Get filenames in order of search results for the query.
		HtmlPage loginPage = login();
		Result result = test(loginPage,query);
		return result;
	}
	
	private HtmlPage login() {
		try {  
			// Login.
			logger.info("Login");
			if (loginPage != null) return loginPage;
			String username = "dermot.obrien@transpower.co.nz";
			String password = "thi$.i$.my.pa$$w0rd";
			loginPage = login(username,password);
			return loginPage;
		}
		catch(Exception e){
			logger.log(Level.SEVERE, "Unable to login.", e);
			return null;
		}
	}

	public List<Result> test(List<QuestionMapping> queries) {
		return test(queries,Integer.MAX_VALUE);
	}

	public List<Result> test(List<QuestionMapping> queries,int limit) {
		try {  
			// Login.
			HtmlPage loginPage = login();

			// Do the queries.
			List<Result> results = new ArrayList<Result>();
			limit = Math.min(limit,queries == null ? 0 : queries.size());
			for (int i = 0; i < limit; i++) {
				// Get filenames in order of search results for the query.
				QuestionMapping query = queries.get(i);
				Result result = test(loginPage,query);
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
	
	public List<QuestionMapping> getQueries() {
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
			outCell.setCellValue("Question ID");
		    outCell = outRow.createCell(outColNo++);
			outCell.setCellValue("Question");
			outCell = outRow.createCell(outColNo++);
			outCell.setCellValue("Order");
			outCell = outRow.createCell(outColNo++);
			outCell.setCellValue("Document Name");
			outCell = outRow.createCell(outColNo++);
			outCell.setCellValue("Document Relevance");
			outCell = outRow.createCell(outColNo++);
			outCell.setCellValue("Score");
			outCell = outRow.createCell(outColNo++);
			outCell.setCellValue("Document Title");
			outCell = outRow.createCell(outColNo++);
			outCell.setCellValue("Filename");
			outCell = outRow.createCell(outColNo++);
			outCell.setCellValue("URL");
			
			ScoringModel scoringModel = new ScoringModel();
			
			for (Result result : results) {
				int index = 1;
				for (DocumentMetadata document : result.documents) {
					outRow = outSheet.createRow(outRowNo++);
					outColNo = 0;
					outCell = outRow.createCell(outColNo++);
					outCell.setCellValue(result.query.getQuestionId());
					outCell = outRow.createCell(outColNo++);
					outCell.setCellValue(result.query.getNaturalLanguageQuestion());
					outCell = outRow.createCell(outColNo++);
					outCell.setCellValue(index++);
					outCell = outRow.createCell(outColNo++);
					outCell.setCellValue(document.name);
					
					int relevance = 0;
					int order = index-1;
					float score = scoringModel.getScore(order, relevance);

					outCell = outRow.createCell(outColNo++);
					outCell.setCellValue(relevance);
					outCell = outRow.createCell(outColNo++);
					outCell.setCellValue(score);

					outCell = outRow.createCell(outColNo++);
					outCell.setCellValue(document.title);
					outCell = outRow.createCell(outColNo++);
					outCell.setCellValue(document.fileTitle);
					outCell = outRow.createCell(outColNo++);
					outCell.setCellValue(document.uri);
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
		// Initialise.
		String outputfolder = "data/training/";
		String outputfile = outputfolder+"QueryResults.xlsx";
		ExternalSiteSearcher externalConsultantsSiteTester = new ExternalSiteSearcher();
		
		// Get the queries.
		List<QuestionMapping> queries = externalConsultantsSiteTester.getQueries();
		
		// Search the queries and print the results.
		int limit = Integer.MAX_VALUE;
		long start = System.currentTimeMillis();
		List<Result> results = externalConsultantsSiteTester.test(queries,limit);
		long end = System.currentTimeMillis();
		long duration = end-start;
		logger.info("Search of "+Math.min(limit,queries.size())+" took "+(duration / 1000)+" seconds");
		logger.info("Saving results to "+outputfile);
		externalConsultantsSiteTester.save(outputfile,results);
	}
}
