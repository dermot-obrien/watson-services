package org.dobrien.watson.services.testing;

public class ScoringModel {
	
	private int maxRelevance = 10;
	private int minRelevance = 6;
	private int maxResults = 10;
	private float[][] scores;
	
	public ScoringModel() {
		create();
	}
	
	private void create() {
		scores = new float[maxRelevance-minRelevance+1][];
		for (int i = 0, relevance = maxRelevance; i < scores.length; i++, relevance--) {
			scores[i] = new float[maxResults];
			float score = relevance;
			for (int j = 0; j < maxResults; j++) {
				scores[i][j] = score;
				score -= 0.5;
			}
		}
	}

	public float getScore(int order, int relevance ) {
		if (relevance > maxRelevance || relevance < minRelevance) return 0;
		if (order > maxResults || order < 1) return 0;
		int i = 0+maxRelevance-relevance;
		return scores[i][order-1];
	}

	public static void main(String[] args) {
		ScoringModel scoringModel = new ScoringModel();
		for (int i = 0; i < scoringModel.scores.length; i++) {
			for (int j = 0; j < scoringModel.scores[i].length; j++) {
				if (j > 0) System.out.print(",");
				System.out.print(scoringModel.scores[i][j]);
			}
			System.out.println();
		}
	}
	
}
