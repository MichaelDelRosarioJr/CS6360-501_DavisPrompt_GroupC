package edu.utdallas.cs6360.davisbase.trees;

import edu.utdallas.cs6360.davisbase.utils.ByteHelpers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static edu.utdallas.cs6360.davisbase.Config.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit test class for TableInteriorPage
 * @author Charles Krol
 * @author Matthew Villarreal
 * @author Michael Del Rosario
 * @author Mithil Vijay
 */
class TableInteriorPageTest {
	private TableConfig config;
	private TableInteriorPage testPage;
	
	static DataType[] colTypesText = {DataType.TINY_INT_TYPE_CODE,
			DataType.SHORT_TYPE_CODE, DataType.INT_TYPE_CODE, DataType.LONG_TYPE_CODE,
			DataType.FLOAT_TYPE_CODE, DataType.DOUBLE_TYPE_CODE, DataType.DATETIME_TYPE_CODE,
			DataType.DATE_TYPE_CODE, DataType.TEXT_TYPE_CODE};
	
	static int treeOrder = 50;
	static int leafDegreeText = 2;
	static int dataRecordSizeText = 170;
	
	
	static DataType[] colTypesNoText = {DataType.TINY_INT_TYPE_CODE,
			DataType.SHORT_TYPE_CODE, DataType.INT_TYPE_CODE, DataType.LONG_TYPE_CODE};
	
	static int leafDegreeNoText = 18;
	static int dataRecordSizeNoText = 15;
	
	
	
	@BeforeEach
	void setUp() {
		this.config = new TableConfig(colTypesNoText);
		this.testPage = new TableInteriorPage(PageType.TABLE_INTERIOR_ROOT, ZERO);
	}
	
	@Test
	void getBytes() {
		testPage.getBytes();
		TableInteriorPage page = new TableInteriorPage(ByteHelpers.byteArrayListToArray(testPage.getBytes()),
				ZERO);
		
		assertEquals(testPage, page);
	}
	
	@Test
	void getDataCellAtOffsetInFile() {
	}
	
	@Test
	void writePage() {
	}
}