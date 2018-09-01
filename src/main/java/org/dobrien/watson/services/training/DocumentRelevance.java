package org.dobrien.watson.services.training;

public class DocumentRelevance {
	
	private int questionId;
	private int documentId;
	private int relevance;
	private String documentFilename;
	private String splitFilename;
	
	public DocumentRelevance() {
	}
	
	public int getQuestionId() {
		return questionId;
	}
	
	public void setQuestionId(int questionId) {
		this.questionId = questionId;
	}
	
	public int getDocumentId() {
		return documentId;
	}
	
	public void setDocumentId(int documentId) {
		this.documentId = documentId;
	}
	
	public int getRelevance() {
		return relevance;
	}
	
	public void setRelevance(int relevance) {
		this.relevance = relevance;
	}
	
	public String getDocumentFilename() {
		return documentFilename;
	}
	
	public void setDocumentFilename(String documentFilename) {
		this.documentFilename = documentFilename;
	}
	
	public String getSplitFilename() {
		return splitFilename;
	}
	
	public void setSplitFilename(String splitFilename) {
		this.splitFilename = splitFilename;
	}
}