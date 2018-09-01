package org.dobrien.watson.services.domains.cim;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class CIMEntities {
	
	private static Logger logger = Logger.getLogger(CIMEntities.class.getName());
	
	public static class Entity {
	
		public String name;
		public String term;
		public String type;
		public String connectorType;
		public String supplierName;
		public String path;
		public String group;
		
		public String key;
		public Entity supplier;
		public boolean duplicate = false;
		public List<Entity> children = new ArrayList<Entity>();
		public boolean hidden;
		
		public String getKey() {
//			if (key == null) key = path+"/"+name;
			if (key == null) key = name;
			return key;
		}
		
		int childCount() {
			if (children == null || children.size() == 0) return 0;
			int count = 0;
			for (Entity child : children) {
				count += child.childCount() + 1;
			}
			return count;
		}
	}
	
	private static String filename = "C:\\Users\\dermot\\Dropbox\\Work\\Transpower\\Cognitive Search\\CIM\\CIMModel.xlsx";

	private static Map<String,Entity> termToEntityMap;
	
	public static List<Entity> entities() {
		return extractEntities(filename);
	}

	public static Map<String,Entity> termToEntityMap() {
		if (termToEntityMap != null) return termToEntityMap;
		termToEntityMap = new HashMap<String,Entity>();
		List<Entity> entities = entities();
		for (Entity entity : entities) {
			termToEntityMap.put(entity.term, entity);
		}
		return termToEntityMap;
	}

	public static List<Entity> extractEntities(String filename) {
		XSSFWorkbook workbook = null;
		try {
			// Open.
			String excelFilename = filename;
			InputStream inputStream = new FileInputStream(excelFilename);
			workbook = new XSSFWorkbook(inputStream); 
			
			// Get questions.
			XSSFSheet sheet = workbook.getSheetAt(0);
			if (sheet == null) {
				logger.warning("No sheet in "+excelFilename);
				return null;
			}
			List<Entity> entities = extractFromWorkheet(sheet);

			// Done.
			workbook.close();
			inputStream.close();
			return entities;
		}
		catch(Exception e) {
			logger.log(Level.SEVERE,"Unable to extract.", e);
			return null;
		}
		finally {
			if (workbook != null) {
				try { 
					workbook.close(); 
				} 
				catch(Exception e) {
					logger.warning("Unable to close workbook. "+e.getMessage());
				}
			}
		}
	}

	private static List<Entity> extractFromWorkheet(XSSFSheet sheet) {
		int rowNo = 0;
		try {
			// Get column map.
			int lastRowNum = sheet.getLastRowNum();
			Map<String,Integer> nameToColumn = new HashMap<String,Integer>();
			Row header = sheet.getRow(rowNo);
			for (int colNo = 0; colNo <= header.getLastCellNum(); colNo++) {
				Cell cell = header.getCell(colNo);
				if (cell == null) continue;
				String name = cell.getStringCellValue();
				if (name == null || name.trim().length() == 0) continue;
				nameToColumn.put(name.trim(), colNo);
			}
			
			// Get question column no.
			Integer nameCol = nameToColumn.get("Name");
			if (nameCol == null) {
				throw new RuntimeException("Unable to find Name column in sheet "+sheet.getSheetName());
			}
			
			Integer typeCol = nameToColumn.get("Type");
			if (typeCol == null) {
				throw new RuntimeException("Unable to find Type column in sheet "+sheet.getSheetName());
			}
			
			// Get topics column no.
			Integer connectorTypeCol = nameToColumn.get("Connector Type");
			if (connectorTypeCol == null) {
				throw new RuntimeException("Unable to find Connector Type column in sheet "+sheet.getSheetName());
			}

			// Get topics column no.
			Integer supplierCol = nameToColumn.get("Supplier");
			if (supplierCol == null) {
				throw new RuntimeException("Unable to find \"Supplier\" column in sheet "+sheet.getSheetName());
			}
			
			// Get topics column no.
			Integer pathCol = nameToColumn.get("Path");
			if (pathCol == null) {
				throw new RuntimeException("Unable to find \"Path\" column in sheet "+sheet.getSheetName());
			}
			
			// Get group column no.
			Integer groupCol = nameToColumn.get("Group");
			if (groupCol == null) {
				throw new RuntimeException("Unable to find \"Group\" column in sheet "+sheet.getSheetName());
			}

			Integer termCol = nameToColumn.get("Term");
			if (termCol == null) {
				throw new RuntimeException("Unable to find \"Term\" column in sheet "+sheet.getSheetName());
			}

			// Initialise.
			List<Entity> entities = new ArrayList<Entity>();

			// Get questions.
			for (rowNo = 1; rowNo <= lastRowNum; rowNo++) {
				Row row = sheet.getRow(rowNo);
				if (row == null) continue;
				
				Cell cell = row.getCell(nameCol);
				if (cell == null) continue;
				if (cell.getStringCellValue() == null) continue;
				if (cell.getStringCellValue().trim().length() == 0) continue;
				Entity entity = new Entity();
				entities.add(entity);
				entity.name = cell.getStringCellValue().trim();

				cell = row.getCell(typeCol);
				entity.type = cell.getStringCellValue().trim();

				cell = row.getCell(connectorTypeCol);
				entity.connectorType = cell.getStringCellValue().trim();
				
				cell = row.getCell(supplierCol);
				entity.supplierName = cell.getStringCellValue().trim();
				
				cell = row.getCell(pathCol);
				entity.path = cell.getStringCellValue().trim();

				cell = row.getCell(groupCol);
				entity.group = cell.getStringCellValue().trim();

				cell = row.getCell(termCol);
				entity.term = cell.getStringCellValue().trim();
			}
			return entities;
		}
		catch(Exception e) {
			logger.log(Level.SEVERE,"Unable to extract from sheet \""+sheet.getSheetName() + "\" row "+rowNo, e);
			return null;
		}			
	}		
	
	public static void main(String[] args) {
		new CIMEntities().preprocess(filename);
	}

	String[] hidden  = { 
			"IdentifiedObject", 
	};
	
	private void preprocess(String filename) {
		List<Entity> entities = extractEntities(filename);
		Map<String,Entity> nameToEntityMap = new HashMap<String,Entity>();
		
		String[] missing  = { 
			"IdentifiedObject", 
			"TapChangerTablePoint",
			"Quality61850",
			"StateVariable",
			"EndDeviceAction",
			"UserAttribute",
			"AuxiliaryObject",
			"CurveData"
		};

		for (int i = 0; i < missing.length; i++) {
			Entity entity = new Entity();
			entity.name = missing[i];
			nameToEntityMap.put(entity.name, entity);
			for (int j = 0; j < hidden.length; j++) {
				if (entity.name.equals(hidden[j])) {
					entity.hidden = true;
				}
			}
		}

		for (Entity entity : entities) {
			Entity other = nameToEntityMap.get(entity.name);
			if (other != null) {
				logger.warning("Duplicate for "+entity.name); 
				other.duplicate = true;
				entity.duplicate = true;
			}
			nameToEntityMap.put(entity.name,entity);
		}

		for (Entity entity : entities) {
			entity.supplier = nameToEntityMap.get(entity.supplierName);
			if (entity.supplier == null) {
				logger.warning("No supplier entity named "+entity.supplierName); 
			}
			else {
				entity.supplier.children.add(entity);
			}
		}
		
		for (Entity entity : entities) {
			List<Entity> ancestors = new ArrayList<Entity>();
			getAncestry(ancestors, entity);
//			System.out.print(entity.name+"["+getTerm(entity)+"]");
//			for (Entity ancestor : ancestors) {
//				System.out.print(" -> ");
//				System.out.print(getTerm(ancestor));
//				System.out.print("("+ancestor.childCount()+")");
//			}
//			System.out.println();
			System.out.println(entity.name+","+getTerm(entity));
		}
	}
	
	private String convert(String word) {
		if (word.toLowerCase().equals("mkt")) return "Market";
		return word;
	}
	
	private String getTerm(Entity entity) {
		int start = 0;
		StringBuffer term = new StringBuffer();
		for (int i = 1; i < entity.name.length(); i++) {
			if (Character.isUpperCase(entity.name.charAt(i)) && i < entity.name.length()-1 && Character.isLowerCase(entity.name.charAt(i+1))) {
				String word = entity.name.substring(start,i);
				if (start > 0) term.append(" ");
				term.append(convert(word));
				start = i;
			}
		}
		term.append(" ");
		String word = entity.name.substring(start,entity.name.length());
		term.append(word);
		return term.toString().trim().toLowerCase();
	}
	
	private void getAncestry(List<Entity> ancestors,Entity entity) {
		if (entity == null || entity.supplier == null) return;
		if (entity.supplier.hidden) return;
		ancestors.add(entity.supplier);
		getAncestry(ancestors,entity.supplier);
	}

}
