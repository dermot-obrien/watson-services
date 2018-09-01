package org.dobrien.watson.services.testing;

import static org.junit.Assert.*;

import org.junit.Test;

public class ScoringModelTest {
	
	@Test
	public void test() {
		ScoringModel scoringModel = new ScoringModel();
		int relevance = 10;
		int order = 10;
		float expectedScore = 5.5f;
		assertEquals(expectedScore,scoringModel.getScore(order, relevance),0);
		
		relevance = 10;
		order = 1;
		expectedScore = 10;
		assertEquals(expectedScore,scoringModel.getScore(order, relevance),0);

		relevance = 6;
		order = 6;
		expectedScore = 3.5f;
		assertEquals(expectedScore,scoringModel.getScore(order, relevance),0);

		relevance = 10;
		order = 11;
		expectedScore = 0;
		assertEquals(expectedScore,scoringModel.getScore(order, relevance),0);

		relevance = 6;
		order = 10;
		expectedScore = 1.5f;
		assertEquals(expectedScore,scoringModel.getScore(order, relevance),0);
	}

}
