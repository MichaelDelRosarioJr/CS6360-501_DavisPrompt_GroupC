package edu.utdallas.cs6360.davisbase.trees;

import edu.utdallas.cs6360.davisbase.utils.ByteHelpers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
	private TableLeafPage testPage;
	private TableConfig config;
	
	static DataType[] columnTypesText = {DataType.TINY_INT_TYPE_CODE,
			DataType.SHORT_TYPE_CODE, DataType.INT_TYPE_CODE, DataType.LONG_TYPE_CODE,
			DataType.FLOAT_TYPE_CODE, DataType.DOUBLE_TYPE_CODE, DataType.DATETIME_TYPE_CODE,
			DataType.DATE_TYPE_CODE, DataType.TEXT_TYPE_CODE};
	
	static DataType[] columnTypesNoText = {DataType.TINY_INT_TYPE_CODE,
			DataType.SHORT_TYPE_CODE, DataType.INT_TYPE_CODE, DataType.LONG_TYPE_CODE};
	
	
	static String[] columnData = {"1", "2", "4", "8"};
	
	static int treeOrder = 50;
	static int leafDegreeNoText = 18;
	static int dataRecordSizeNoText = 15;
	
	@BeforeEach
	void setUp() {
		config = new TableConfig(columnTypesNoText);
		testPage = new TableLeafPage(PageType.TABLE_LEAF_ROOT, ZERO, ZERO);
	}
	
	@Test
	void getDataCellAtOffsetInFile() {
	}
	
	@Test
	void writePage() {
	}
	
	@Test
	void getBytes() {
		TableLeafPage tableLeafPage = new
				TableLeafPage(ByteHelpers.byteArrayListToArray(testPage.getBytes()), ZERO);
		assertEquals(testPage, tableLeafPage);
	}
}