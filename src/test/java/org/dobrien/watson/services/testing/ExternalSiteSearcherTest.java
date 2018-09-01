package org.dobrien.watson.services.testing;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import org.dobrien.watson.services.testing.ExternalSiteSearcher.Result;
import org.dobrien.watson.services.training.QuestionMapping;

public class ExternalSiteSearcherTest {

	private void test(ExternalSiteSearcher searcher,QuestionMapping query, List<String> expectedTop10) {
		Result result = searcher.test(query);
		assertEquals(expectedTop10.size(), result.documents.size());
		for (int i = 0; i < expectedTop10.size(); i++) {
			assertEquals(expectedTop10.get(i), result.documents.get(i).name);
		}
	}
	
	@Test
	@Ignore
	public void testGetQuestions() {
		ExternalSiteSearcher searcher = new ExternalSiteSearcher();
		List<QuestionMapping> queries = searcher.getQueries();
		assertEquals(64, queries.size());
	}

	@Test
	@Ignore
	public void testKnownPages() {
		ExternalSiteSearcher searcher = new ExternalSiteSearcher();
		QuestionMapping query = new QuestionMapping();
		query.setQuestionId(1);
		query.setQuestion("Standard fibre connector");
		query.setNaturalLanguageQuestion(query.getQuestion());
		test(searcher,query, Arrays.asList(
			"TP.SS 03.60",
			"TP.ML 16.34",
			"TP.MC 11.06",
			"TP.SS 03.57",
			"TP.SS 07.33",
			"TP.SS 03.70",
			"TP.DC 11.01",
			"TP.DP 03.01",
			"TP.SS 04.16"
		));
	}
}
