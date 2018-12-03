package edu.utdallas.cs6360.davisbase.trees;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit test class for TableConfig
 * @author Charles Krol
 * @author Matthew Villarreal
 * @author Michael Del Rosario
 * @author Mithil Vijay
 */
class TableConfigTest {
	private TableConfig testObject = null;
	
	static DataType[] colTypes = {DataType.TINY_INT_TYPE_CODE,
			DataType.SHORT_TYPE_CODE, DataType.INT_TYPE_CODE, DataType.LONG_TYPE_CODE,
			DataType.REAL_TYPE_CODE, DataType.DOUBLE_TYPE_CODE, DataType.DATETIME_TYPE_CODE,
			DataType.DATE_TYPE_CODE, DataType.TEXT_TYPE_CODE};
	
	static int treeOrder = 50;
	static int leafDegree = 2;
	static int dataRecordSize = 170;
	
	static byte[] col = {(byte)0x05,(byte)0x05, (byte)0x05};
	
	@BeforeEach
	void setUp() {
		this.testObject = new TableConfig(colTypes);
	}
	
	@Test
	void getDataRecordSize() {
		assertEquals(testObject.getDataMaxRecordSize(), dataRecordSize);
	}
	
	
	@Test
	void getTreeOrder() {
		assertEquals(testObject.getTreeOrder(), treeOrder);
	}
	
	@Test
	void getLeafPageDegree() {
		assertEquals(testObject.getLeafPageDegree(), leafDegree);
	}
}