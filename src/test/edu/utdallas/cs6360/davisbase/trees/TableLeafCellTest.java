package edu.utdallas.cs6360.davisbase.trees;

import edu.utdallas.cs6360.davisbase.utils.ByteHelpers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit test class for TableLeafCell
 * @author Charles Krol
 * @author Matthew Villarreal
 * @author Michael Del Rosario
 * @author Mithil Vijay
 */
class TableLeafCellTest {
	private TableLeafCell testCell;
	
	private ArrayList<DataType> columnTypes = DataRecordTest.columnTypes;
	private DataRecord testDataRecord = null;
	
	private static final int ROW_ID = 50;
	
	@BeforeEach
	void setUp() {
		//columnTypes[columnTypes.length - 1] = (byte)(columnTypes[columnTypes.length - 1].getTypeCode() + 6);
		testDataRecord = new DataRecord(DataRecordTest.columnTypes, DataRecordTest.columnData);
		
		testCell = new TableLeafCell(ROW_ID, testDataRecord);
		
	}
	
	@Test
	@DisplayName("Test case to recreate a TableLeafCell from bytes")
	void getBytes() {
		TableLeafCell tmpTestCell = new TableLeafCell(ByteHelpers.byteArrayListToArray(testCell.getBytes()));
		assertEquals(testCell, tmpTestCell);
	}
}