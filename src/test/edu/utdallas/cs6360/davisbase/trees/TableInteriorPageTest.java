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
	
	static DataType[] colTypes = {DataType.TINY_INT_TYPE_CODE,
			DataType.SHORT_TYPE_CODE, DataType.INT_TYPE_CODE, DataType.LONG_TYPE_CODE};
	
	TableConfig tableConfig;
	TableInteriorPage testPage;
	
	@BeforeEach
	void setUp() {
		this.tableConfig = new TableConfig(colTypes);
		this.testPage = new TableInteriorPage(PageType.TABLE_INTERIOR_ROOT, ZERO);
		
	}
	
	@Test
	@DisplayName("Test case for an empty TableInteriorPage")
	void emptyTest() {
		TableInteriorPage tableInteriorPage;
		try (RandomAccessFile file = new RandomAccessFile("table.tbl", "rw")) {
			LOGGER.log(Level.INFO, "Preparing to write first page");
			File tmp = new File("tableInteriorPageTest.tbl");
			LOGGER.log(Level.INFO, tmp.getAbsolutePath());
			file.setLength(PAGE_SIZE);
			file.seek(ZERO);
			LOGGER.log(Level.INFO, "About to write to file, *crossing fingers*");
			this.testPage.writePage(file);
			LOGGER.log(Level.INFO, this.testPage.getBytes().toString());
			LOGGER.log(Level.INFO, "Test page written successfully");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try (RandomAccessFile file = new RandomAccessFile("table.tbl", "rw")) {
			LOGGER.log(Level.INFO, "Attempting to recreate test page from file");
			byte[] fromFile = new byte[PAGE_SIZE];
			int pageStartAddr = this.testPage.getPageNumber() * PAGE_SIZE;
			LOGGER.log(Level.INFO, "Collecting " + PAGE_SIZE + " bytes from file at address " + pageStartAddr);
			file.seek(testPage.getPageNumber() * PAGE_SIZE);
			file.read(fromFile);
			LOGGER.log(Level.INFO, "Attemping to create new TableLeafPage from bytes collected in file");
			tableInteriorPage = new TableInteriorPage(fromFile, ZERO);
			assertEquals(this.testPage, tableInteriorPage);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	@Test
	@DisplayName("Test case for an non-empty TableInteriorPage")
	void testPageNonEmpty() {
		// Set up some data cells to text
		for (int i = ZERO; i < this.tableConfig.getTreeOrder(); i++) {
			TableInteriorCell cellTmp = new TableInteriorCell(i, Integer.MAX_VALUE);
			this.testPage.addDataCell(cellTmp);
		}
		
		TableInteriorPage tableInteriorPage;
		try (RandomAccessFile file = new RandomAccessFile("table.tbl", "rw")) {
			LOGGER.log(Level.INFO, "Preparing to write first page");
			File tmp = new File("tableInteriorPageTest.tbl");
			LOGGER.log(Level.INFO, tmp.getAbsolutePath());
			file.setLength(PAGE_SIZE);
			file.seek(ZERO);
			LOGGER.log(Level.INFO, "About to write to file, *crossing fingers*");
			this.testPage.writePage(file);
			LOGGER.log(Level.INFO, this.testPage.getBytes().toString());
			LOGGER.log(Level.INFO, "Test page written successfully");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try (RandomAccessFile file = new RandomAccessFile("table.tbl", "rw")) {
			LOGGER.log(Level.INFO, "Attempting to recreate test page from file");
			byte[] fromFile = new byte[PAGE_SIZE];
			int pageStartAddr = this.testPage.getPageNumber() * PAGE_SIZE;
			LOGGER.log(Level.INFO, "Collecting " + PAGE_SIZE + " bytes from file at address " + pageStartAddr);
			file.seek(testPage.getPageNumber() * PAGE_SIZE);
			file.read(fromFile);
			LOGGER.log(Level.INFO, "Attemping to create new TableLeafPage from bytes collected in file");
			tableInteriorPage = new TableInteriorPage(fromFile, ZERO);
			assertEquals(this.testPage, tableInteriorPage);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}