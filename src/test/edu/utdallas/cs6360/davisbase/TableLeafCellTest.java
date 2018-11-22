package edu.utdallas.cs6360.davisbase;

import edu.utdallas.cs6360.davisbase.utils.ByteHelpers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit test class for TableLeafCell
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
	void getBytes() {
		TableLeafCell tmpTestCell = new TableLeafCell(ByteHelpers.byteArrayListToArray(testCell.getBytes()));
		assertEquals(testCell, tmpTestCell);
	}
}