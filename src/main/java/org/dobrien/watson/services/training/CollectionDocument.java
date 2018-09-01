package org.dobrien.watson.services.training;

public class CollectionDocument {

	private int documentID;
	private String documentFilename;
	private String watsonDocumentID;
	private String title;
	private String sharePointLink;
	private String documentNumber;
	private String type;
	private String standardName;
	private String activity;
	private int size;
	private int noOfPages;
	private int noOfCharacters;

	public int getDocumentID() {
		return documentID;
	}

	public void setDocumentID(int documentID) {
		this.documentID = documentID;
	}

	public String getDocumentFilename() {
		return documentFilename;
	}

	public void setDocumentFilename(String documentFilename) {
		this.documentFilename = documentFilename;
	}

	public String getWatsonDocumentID() {
		return watsonDocumentID;
	}

	public void setWatsonDocumentID(String watsonDocumentID) {
		this.watsonDocumentID = watsonDocumentID;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSharePointLink() {
		return sharePointLink;
	}

	public void setSharePointLink(String sharePointLink) {
		this.sharePointLink = sharePointLink;
	}

	public String getDocumentNumber() {
		return documentNumber;
	}

	public void setDocumentNumber(String documentNumber) {
		this.documentNumber = documentNumber;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStandardName() {
		return standardName;
	}

	public void setStandardName(String standardName) {
		this.standardName = standardName;
	}

	public String getActivity() {
		return activity;
	}

	public void setActivity(String activity) {
		this.activity = activity;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getNoOfPages() {
		return noOfPages;
	}

	public void setNoOfPages(int noOfPages) {
		this.noOfPages = noOfPages;
	}

	public int getNoOfCharacters() {
		return noOfCharacters;
	}

	public void setNoOfCharacters(int noOfCharacters) {
		this.noOfCharacters = noOfCharacters;
	}

}
