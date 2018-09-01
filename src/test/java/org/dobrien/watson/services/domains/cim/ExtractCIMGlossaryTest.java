package org.dobrien.watson.services.domains.cim;

import org.junit.Test;

public class ExtractCIMGlossaryTest {
	
	@Test
	public void testExtract() {
		String filename = "src/test/resources/iec61968-2{ed2.0}en.pdf.html";
		new ExtractCIMGlossary().extractFrom(filename);
	}

}
