package edu.utdallas.cs6360.davisbase.trees;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import static edu.utdallas.cs6360.davisbase.Config.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit class for TableLeafPage
 * @author Charles Krol
 * @author Matthew Villarreal
 * @author Michael Del Rosario
 * @author Mithil Vijay
 */
class TableLeafPageTest {
	private static final Logger LOGGER = Logger.getLogger(TableLeafPageTest.class.getName());
	public static final String NULL_VALUE = "\0";
	
	// Arrays of column type codes and values for different test configurations
	static ArrayList<DataType> columnTypesText = new ArrayList<>(Arrays.asList(DataType.TEXT_TYPE_CODE,
			DataType.TEXT_TYPE_CODE));
	static ArrayList<String> columnValuesText = new ArrayList<>(Arrays.asList("Aa", "Ba"));
	
	static ArrayList<DataType> columnTypesText2 = new ArrayList<>(Arrays.asList(DataType.TINY_INT_TYPE_CODE,
			DataType.SHORT_TYPE_CODE, DataType.INT_TYPE_CODE, DataType.LONG_TYPE_CODE, DataType.TEXT_TYPE_CODE));
	static ArrayList<String> columnValuesText2 = new ArrayList<>(Arrays.asList("1", "16", "1000", "1000000000", "Aa"));
	
	static ArrayList<DataType> columnTypesNoText = new ArrayList<>(Arrays.asList(DataType.TINY_INT_TYPE_CODE,
			DataType.SHORT_TYPE_CODE, DataType.INT_TYPE_CODE, DataType.LONG_TYPE_CODE));
	static ArrayList<String> columnValuesNoText = new ArrayList<>(Arrays.asList("1", "16", "1000", "1000000000"));
	
	static ArrayList<DataType> columnTypesNoText2 = new ArrayList<>(Arrays.asList(DataType.REAL_TYPE_CODE,
			DataType.DOUBLE_TYPE_CODE, DataType.DATETIME_TYPE_CODE, DataType.DATE_TYPE_CODE));
	static ArrayList<String> columnValuesNoText2 = new ArrayList<>(Arrays.asList("4.14", "4.1566666", "1112124354",
			"1112124354"));
	
	@BeforeEach
	void setUp() {
	
	}
	
	@Test
	@DisplayName("Test case for an empty leaf page")
	void emptyLeafPage() {
		TableConfig configNoText = new TableConfig(columnTypesNoText);
		TableLeafPage testPageEmpty = new TableLeafPage(PageType.TABLE_LEAF_ROOT, ZERO, ZERO, configNoText);
		TableLeafPage tableLeafPage;
		
		try (RandomAccessFile file = new RandomAccessFile("table.tbl", "rw")) {
			LOGGER.log(Level.INFO, "Preparing to write first page");
			File tmp = new File("table.tbl");
			LOGGER.log(Level.INFO, tmp.getAbsolutePath());
			file.setLength(PAGE_SIZE);
			file.seek(ZERO);
			LOGGER.log(Level.INFO, "About to write to file, *crossing fingers*");
			testPageEmpty.writePage(file);
			LOGGER.log(Level.INFO, testPageEmpty.getBytes().toString());
			LOGGER.log(Level.INFO, "Test page written successfully");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try (RandomAccessFile file = new RandomAccessFile("table.tbl", "rw")) {
			LOGGER.log(Level.INFO, "Attempting to recreate test page from file");
			byte[] fromFile = new byte[PAGE_SIZE];
			int pageStartAddr = testPageEmpty.getPageNumber() * PAGE_SIZE;
			LOGGER.log(Level.INFO, "Collecting " + PAGE_SIZE + " bytes from file at address " + pageStartAddr);
			file.seek(testPageEmpty.getPageNumber() * PAGE_SIZE);
			file.read(fromFile);
			LOGGER.log(Level.INFO, "Attemping to create new TableLeafPage from bytes collected in file");
			tableLeafPage = new TableLeafPage(fromFile, ZERO, configNoText);
			assertEquals(testPageEmpty, tableLeafPage);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	@DisplayName("Test case for 2 text columns")
	void testTextPage1() {
		TableConfig configText = new TableConfig(columnTypesText);
		TableLeafPage textTestPage = new TableLeafPage(PageType.TABLE_LEAF_ROOT, ZERO, ZERO, configText);
		for (int i = ZERO; i < configText.getLeafPageDegree(); i++) {
			int times = 5;
			
			ArrayList<String> tmpCols = columnValuesText;
			tmpCols.set(0, new String(new char[times]).replace(NULL_VALUE, columnValuesText.get(0)));
			tmpCols.set(1, new String(new char[times]).replace(NULL_VALUE, columnValuesText.get(1)));
			
			DataRecord tmp = new DataRecord(columnTypesText, tmpCols);
			TableLeafCell cellTmp = new TableLeafCell(i, tmp);
			textTestPage.addDataCell(cellTmp);
		}
		
		TableLeafPage tableLeafPage;
		try (RandomAccessFile file = new RandomAccessFile("table.tbl", "rw")) {
			LOGGER.log(Level.INFO, "Preparing to write first page");
			File tmp = new File("table.tbl");
			LOGGER.log(Level.INFO, tmp.getAbsolutePath());
			file.setLength(PAGE_SIZE);
			file.seek(ZERO);
			LOGGER.log(Level.INFO, "About to write to file, *crossing fingers*");
			textTestPage.writePage(file);
			LOGGER.log(Level.INFO, textTestPage.getBytes().toString());
			LOGGER.log(Level.INFO, "Test page written successfully");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try (RandomAccessFile file = new RandomAccessFile("table.tbl", "rw")) {
			LOGGER.log(Level.INFO, "Attempting to recreate test page from file");
			byte[] fromFile = new byte[PAGE_SIZE];
			int pageStartAddr = textTestPage.getPageNumber() * PAGE_SIZE;
			LOGGER.log(Level.INFO, "Collecting " + PAGE_SIZE + " bytes from file at address " + pageStartAddr);
			file.seek(textTestPage.getPageNumber() * PAGE_SIZE);
			file.read(fromFile);
			LOGGER.log(Level.INFO, "Attemping to create new TableLeafPage from bytes collected in file");
			tableLeafPage = new TableLeafPage(fromFile, ZERO, configText);
			assertEquals(textTestPage, tableLeafPage);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	@DisplayName("Test case for 1 text column and all other columns")
	void testTextPage2() {
		TableConfig configText = new TableConfig(columnTypesText2);
		TableLeafPage textTestPage= new TableLeafPage(PageType.TABLE_LEAF_ROOT, ZERO, ZERO, configText);
		
		for (int i = ZERO; i < configText.getLeafPageDegree(); i++) {
			int times = 3;
			ArrayList<String> tmpCols = columnValuesText2;
			tmpCols.set(4, new String(new char[times]).replace(NULL_VALUE, columnValuesText2.get(4)));
			
			DataRecord tmp = new DataRecord(columnTypesText2, tmpCols);
			TableLeafCell cellTmp = new TableLeafCell(i, tmp);
			textTestPage.addDataCell(cellTmp);
		}
		
		TableLeafPage tableLeafPage;
		try (RandomAccessFile file = new RandomAccessFile("table.tbl", "rw")) {
			LOGGER.log(Level.INFO, "Preparing to write first page");
			File tmp = new File("table.tbl");
			LOGGER.log(Level.INFO, tmp.getAbsolutePath());
			file.setLength(PAGE_SIZE);
			file.seek(ZERO);
			LOGGER.log(Level.INFO, "About to write to file, *crossing fingers*");
			textTestPage.writePage(file);
			LOGGER.log(Level.INFO, textTestPage.getBytes().toString());
			LOGGER.log(Level.INFO, "Test page written successfully");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try (RandomAccessFile file = new RandomAccessFile("table.tbl", "rw")) {
			LOGGER.log(Level.INFO, "Attempting to recreate test page from file");
			byte[] fromFile = new byte[PAGE_SIZE];
			int pageStartAddr = textTestPage.getPageNumber() * PAGE_SIZE;
			LOGGER.log(Level.INFO, "Collecting " + PAGE_SIZE + " bytes from file at address " + pageStartAddr);
			file.seek(textTestPage.getPageNumber() * PAGE_SIZE);
			file.read(fromFile);
			LOGGER.log(Level.INFO, "Attemping to create new TableLeafPage from bytes collected in file");
			tableLeafPage = new TableLeafPage(fromFile, ZERO, configText);
			assertEquals(textTestPage, tableLeafPage);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	@DisplayName("Test case for table with no text columns")
	void testNoTextPage() {
		TableConfig configNoText = new TableConfig(columnTypesNoText);
		TableLeafPage nonTextTestPage = new TableLeafPage(PageType.TABLE_LEAF_ROOT, ZERO, ZERO, configNoText);
		TableLeafPage tableLeafPage;
		
		// Initialize data cells
		for (int i = ZERO; i < configNoText.getLeafPageDegree(); i++) {
			DataRecord tmp = new DataRecord(columnTypesNoText, columnValuesNoText);
			TableLeafCell cellTmp = new TableLeafCell(i, tmp);
			nonTextTestPage.addDataCell(cellTmp);
		}
		
		try (RandomAccessFile file = new RandomAccessFile("table.tbl", "rw")) {
			LOGGER.log(Level.INFO, "Preparing to write first page");
			File tmp = new File("table.tbl");
			LOGGER.log(Level.INFO, tmp.getAbsolutePath());
			file.setLength(PAGE_SIZE);
			file.seek(ZERO);
			LOGGER.log(Level.INFO, "About to write to file, *crossing fingers*");
			nonTextTestPage.writePage(file);
			LOGGER.log(Level.INFO, nonTextTestPage.getBytes().toString());
			LOGGER.log(Level.INFO, "Test page written successfully");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try (RandomAccessFile file = new RandomAccessFile("table.tbl", "rw")) {
			LOGGER.log(Level.INFO, "Attempting to recreate test page from file");
			byte[] fromFile = new byte[PAGE_SIZE];
			int pageStartAddr = nonTextTestPage.getPageNumber() * PAGE_SIZE;
			LOGGER.log(Level.INFO, "Collecting " + PAGE_SIZE + " bytes from file at address " + pageStartAddr);
			file.seek(nonTextTestPage.getPageNumber() * PAGE_SIZE);
			file.read(fromFile);
			LOGGER.log(Level.INFO, "Attemping to create new TableLeafPage from bytes collected in file");
			tableLeafPage = new TableLeafPage(fromFile, ZERO, configNoText);
			assertEquals(nonTextTestPage, tableLeafPage);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	@DisplayName("2nd test case for table with no text columns")
	void testNoTextPage2() {
		TableConfig configNoText = new TableConfig(columnTypesNoText2);
		TableLeafPage testPageNoTest = new TableLeafPage(PageType.TABLE_LEAF_ROOT, ZERO, ZERO, configNoText);
		for (int i = ZERO; i < configNoText.getLeafPageDegree(); i++) {
			DataRecord tmp = new DataRecord(columnTypesNoText2, columnValuesNoText2);
			TableLeafCell cellTmp = new TableLeafCell(i, tmp);
			testPageNoTest.addDataCell(cellTmp);
		}
		
		TableLeafPage tableLeafPage;
		try (RandomAccessFile file = new RandomAccessFile("table.tbl", "rw")) {
			LOGGER.log(Level.INFO, "Preparing to write first page");
			File tmp = new File("table.tbl");
			LOGGER.log(Level.INFO, tmp.getAbsolutePath());
			file.setLength(PAGE_SIZE);
			file.seek(ZERO);
			LOGGER.log(Level.INFO, "About to write to file, *crossing fingers*");
			testPageNoTest.writePage(file);
			LOGGER.log(Level.INFO, testPageNoTest.getBytes().toString());
			LOGGER.log(Level.INFO, "Test page written successfully");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try (RandomAccessFile file = new RandomAccessFile("table.tbl", "rw")) {
			LOGGER.log(Level.INFO, "Attempting to recreate test page from file");
			byte[] fromFile = new byte[PAGE_SIZE];
			int pageStartAddr = testPageNoTest.getPageNumber() * PAGE_SIZE;
			LOGGER.log(Level.INFO, "Collecting " + PAGE_SIZE + " bytes from file at address " + pageStartAddr);
			file.seek(testPageNoTest.getPageNumber() * PAGE_SIZE);
			file.read(fromFile);
			LOGGER.log(Level.INFO, "Attemping to create new TableLeafPage from bytes collected in file");
			tableLeafPage = new TableLeafPage(fromFile, ZERO, configNoText);
			assertEquals(testPageNoTest, tableLeafPage);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}