package org.dobrien.watson.services.training;

import java.util.List;

public class QuestionMapping {
	
	private int questionId;
	private String question;
	private String naturalLanguageQuestion;
	private List<String> documentNames;
	private String[] topics;
	private List<DocumentRelevance> documentRelevances;
	
	public QuestionMapping() {
		this(null,null);
	}

	public QuestionMapping(String question) {
		this(question,null);
	}

	public QuestionMapping(String question,String topics) {
		this.question = question;
		setTopics(topics);
	}
	
	public void setTopics(String topics) {
		if (topics != null) {
			this.topics = topics.split(",");
			for (int i = 0; i < this.topics.length; i++) {
				this.topics[i] = this.topics[i].trim().toLowerCase();
			}
		}
	}	
	
	public String[] getTopics() {
		return topics;
	}

	public void setTopics(String[] topics) {
		this.topics = topics;
	}

	public List<DocumentRelevance> getDocumentRelevances() {
		return documentRelevances;
	}

	public void setDocumentRelevances(List<DocumentRelevance> documentRelevances) {
		this.documentRelevances = documentRelevances;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String query) {
		this.question = query;
	}

	public int getQuestionId() {
		return questionId;
	}

	public void setQuestionId(int questionId) {
		this.questionId = questionId;
	}

	public List<String> getDocumentNames() {
		return documentNames;
	}

	public void setDocumentNames(List<String> documentNames) {
		this.documentNames = documentNames;
	}

	public void setNaturalLanguageQuestion(String nlQuestion) {
		this.naturalLanguageQuestion = nlQuestion;
	}

	public String getNaturalLanguageQuestion() {
		return naturalLanguageQuestion;
	}
}
