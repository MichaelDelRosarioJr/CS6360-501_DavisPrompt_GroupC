package edu.utdallas.cs6360.davisbase.trees;

import edu.utdallas.cs6360.davisbase.utils.ByteHelpers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
	
	private byte[] columnTypeBytes;
	private DataRecord testDataRecord = null;
	
	private static final int ROW_ID = 50;
	
	@BeforeEach
	void setUp() {
		
		columnTypeBytes = new byte[DataRecordTest.columnTypes.length];
		for(int i = 0; i < DataRecordTest.columnTypes.length; i++) {
			columnTypeBytes[i] = DataRecordTest.columnTypes[i].getTypeCode();
		}
		//columnTypeBytes[columnTypeBytes.length - 1] = (byte)(columnTypes[columnTypes.length - 1].getTypeCode() + 6);
		testDataRecord = new DataRecord(columnTypeBytes, DataRecordTest.columnData);
		
		testCell = new TableLeafCell(ROW_ID, testDataRecord);
		
	}
	
	@Test
	@DisplayName("Test case to recreate a TableLeafCell from bytes")
	void getBytes() {
		TableLeafCell tmpTestCell = new TableLeafCell(ByteHelpers.byteArrayListToArray(testCell.getBytes()));
		assertEquals(testCell, tmpTestCell);
	}
}