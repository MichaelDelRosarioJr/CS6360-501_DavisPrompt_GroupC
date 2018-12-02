package edu.utdallas.cs6360.davisbase.trees;

import edu.utdallas.cs6360.davisbase.utils.ByteHelpers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import static edu.utdallas.cs6360.davisbase.Config.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit test class for TableInteriorPage
 * TODO: Test pages with variable length test data
 *
 *
 * @author Charles Krol
 * @author Matthew Villarreal
 * @author Michael Del Rosario
 * @author Mithil Vijay
 */
class TableInteriorPageTest {
	private static final Logger LOGGER = Logger.getLogger(TableInteriorPageTest.class.getName());
	
	private TableConfig config;
	private TableInteriorPage testPageEmpty;
	private TableInteriorPage nonEmptyTestPage;
	
	static DataType[] colTypesText = {DataType.TINY_INT_TYPE_CODE,
			DataType.SHORT_TYPE_CODE, DataType.INT_TYPE_CODE, DataType.LONG_TYPE_CODE,
			DataType.FLOAT_TYPE_CODE, DataType.DOUBLE_TYPE_CODE, DataType.DATETIME_TYPE_CODE,
			DataType.DATE_TYPE_CODE, DataType.TEXT_TYPE_CODE};
	
	public static final int FIFTY = 50;
	static int treeOrder = FIFTY;
	static int leafDegreeText = 2;
	static int dataRecordSizeText = 170;
	
	
	static DataType[] colTypesNoText = {DataType.TINY_INT_TYPE_CODE,
			DataType.SHORT_TYPE_CODE, DataType.INT_TYPE_CODE, DataType.LONG_TYPE_CODE};
	
	static int leafDegreeNoText = 18;
	static int dataRecordSizeNoText = 15;
	
	
	@BeforeEach
	void setUp() {
		this.config = new TableConfig(colTypesNoText);
		this.testPageEmpty = new TableInteriorPage(PageType.TABLE_INTERIOR_ROOT, ZERO);
		this.nonEmptyTestPage = new TableInteriorPage(PageType.TABLE_INTERIOR_ROOT, ZERO);
		
		// Set up some data cells to text
		for(int i = ZERO ; i < FIFTY; i++) {
			TableInteriorCell cellTmp = new TableInteriorCell(i , Integer.MAX_VALUE);
			nonEmptyTestPage.addDataCell(cellTmp);
		}
	}
	
	@Test
	@DisplayName("Test case to recreate a TableInteriorCell from bytes")
	void getBytes() {
		TableInteriorPage page = new TableInteriorPage(ByteHelpers.byteArrayListToArray(testPageEmpty.getBytes()),
				ZERO);
		
		assertEquals(testPageEmpty, page);
	}
	
	@Test
	@DisplayName("Test case for an empty TableInteriorPage")
	void writePage() {
		TableInteriorPage tableInteriorPage;
		try (RandomAccessFile file = new RandomAccessFile("table.tbl", "rw")) {
			LOGGER.log(Level.INFO, "Preparing to write first page");
			File tmp = new File("tableInteriorPageTest.tbl");
			LOGGER.log(Level.INFO, tmp.getAbsolutePath());
			file.setLength(PAGE_SIZE);
			file.seek(ZERO);
			LOGGER.log(Level.INFO, "About to write to file, *crossing fingers*");
			nonEmptyTestPage.writePage(file);
			LOGGER.log(Level.INFO, nonEmptyTestPage.getBytes().toString());
			LOGGER.log(Level.INFO, "Test page written successfully");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try (RandomAccessFile file = new RandomAccessFile("table.tbl", "rw")) {
			LOGGER.log(Level.INFO, "Attempting to recreate test page from file");
			byte[] fromFile = new byte[PAGE_SIZE];
			int pageStartAddr = nonEmptyTestPage.getPageNumber() * PAGE_SIZE;
			LOGGER.log(Level.INFO, "Collecting " + PAGE_SIZE + " bytes from file at address " + pageStartAddr);
			file.seek(nonEmptyTestPage.getPageNumber() * PAGE_SIZE);
			file.read(fromFile);
			LOGGER.log(Level.INFO, "Attemping to create new TableLeafPage from bytes collected in file");
			tableInteriorPage = new TableInteriorPage(fromFile, ZERO);
			assertEquals(nonEmptyTestPage, tableInteriorPage);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	@Test
	@DisplayName("Test case for an non-empty TableInteriorPage")
	void testPageNonEmpty() {
		TableInteriorPage tableInteriorPage;
		try (RandomAccessFile file = new RandomAccessFile("tableInteriorPageTest.tbl", "rw")) {
			LOGGER.log(Level.INFO, "Preparing to write first page");
			File tmp = new File("tableInteriorPageTest.tbl");
			LOGGER.log(Level.INFO, tmp.getAbsolutePath());
			file.setLength(PAGE_SIZE);
			file.seek(ZERO);
			LOGGER.log(Level.INFO, "About to write to file, *crossing fingers*");
			nonEmptyTestPage.writePage(file);
			LOGGER.log(Level.INFO, nonEmptyTestPage.getBytes().toString());
			LOGGER.log(Level.INFO, "Test page written successfully");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try (RandomAccessFile file = new RandomAccessFile("tableInteriorPageTest.tbl", "rw")) {
			LOGGER.log(Level.INFO, "Attempting to recreate test page from file");
			byte[] fromFile = new byte[PAGE_SIZE];
			int pageStartAddr = nonEmptyTestPage.getPageNumber() * PAGE_SIZE;
			LOGGER.log(Level.INFO, "Collecting " + PAGE_SIZE + " bytes from file at address " + pageStartAddr);
			file.seek(nonEmptyTestPage.getPageNumber() * PAGE_SIZE);
			file.read(fromFile);
			LOGGER.log(Level.INFO, "Attemping to create new TableLeafPage from bytes collected in file");
			tableInteriorPage = new TableInteriorPage(fromFile, ZERO);
			assertEquals(nonEmptyTestPage, tableInteriorPage);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}