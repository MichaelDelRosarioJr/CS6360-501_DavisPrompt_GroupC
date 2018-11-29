package edu.utdallas.cs6360.davisbase.trees;

import edu.utdallas.cs6360.davisbase.utils.ByteHelpers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sun.tools.jconsole.Tab;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
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
	private TableLeafPage testPageEmpty;
	private TableLeafPage nonEmptyTestPage;
	private TableConfig config;
	
	static DataType[] columnTypesText = {DataType.TINY_INT_TYPE_CODE,
			DataType.SHORT_TYPE_CODE, DataType.INT_TYPE_CODE, DataType.LONG_TYPE_CODE,
			DataType.FLOAT_TYPE_CODE, DataType.DOUBLE_TYPE_CODE, DataType.DATETIME_TYPE_CODE,
			DataType.DATE_TYPE_CODE, DataType.TEXT_TYPE_CODE};
	
	static DataType[] columnTypesNoText = {DataType.TINY_INT_TYPE_CODE,
			DataType.SHORT_TYPE_CODE, DataType.INT_TYPE_CODE, DataType.LONG_TYPE_CODE};
	
	static String[] columnValuesNoText = {"1", "16", "1000", "1000000000"};
	
	static int treeOrder = 50;
	static int leafDegreeNoText = 18;
	static int dataRecordSizeNoText = 15;
	
	@BeforeEach
	void setUp() {
		config = new TableConfig(columnTypesNoText);
		testPageEmpty = new TableLeafPage(PageType.TABLE_LEAF_ROOT, ZERO, ZERO);
		
		nonEmptyTestPage = new TableLeafPage(PageType.TABLE_LEAF_ROOT, ZERO, ZERO);
		
		for (int i = ZERO; i < ONE; i++) {
			DataRecord tmp = new DataRecord(columnTypesNoText, columnValuesNoText);
			TableLeafCell cellTmp = new TableLeafCell(i, tmp);
			nonEmptyTestPage.addDataCell(cellTmp);
		}
	}
	
	@Test
	void getDataCellAtOffsetInFile() {
	}
	
	@Test
	void writePage() {
		TableLeafPage tableLeafPage;
		try (RandomAccessFile file = new RandomAccessFile("table.tbl", "rw")) {
			LOGGER.log(Level.INFO, "Preparing to write first page");
			File tmp = new File("table.tbl");
			LOGGER.log(Level.INFO, tmp.getAbsolutePath());
			file.setLength(PAGE_SIZE);
			file.seek(ZERO);
			LOGGER.log(Level.INFO, "About to write to file, *crossing fingers*");
			nonEmptyTestPage.writePage(file);
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
			tableLeafPage = new TableLeafPage(fromFile, ZERO);
			assertEquals(nonEmptyTestPage, tableLeafPage);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	void getBytes() {
		TableLeafPage tableLeafPage = new
				TableLeafPage(ByteHelpers.byteArrayListToArray(testPageEmpty.getBytes()), ZERO);
		assertEquals(testPageEmpty, tableLeafPage);
	}
}