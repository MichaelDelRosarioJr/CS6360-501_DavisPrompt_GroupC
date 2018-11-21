package edu.utdallas.cs6360.davisbase;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit test class for DataRecord
 * @author Charles Krol
 */
class DataRecordTest {
	private static byte[] columnTypes = {DataRecord.NULL1_TYPE_CODE, DataRecord.NULL2_TYPE_CODE,
		DataRecord.NULL4_TYPE_CODE, DataRecord.NULL8_TYPE_CODE, DataRecord.TINY_INT_TYPE_CODE,
		DataRecord.SHORT_TYPE_CODE, DataRecord.INT_TYPE_CODE, DataRecord.LONG_TYPE_CODE,
		DataRecord.FLOAT_TYPE_CODE, DataRecord.DOUBLE_TYPE_CODE, DataRecord.DATETIME_TYPE_CODE,
		DataRecord.DATE_TYPE_CODE, DataRecord.TEXT_TYPE_CODE};
	
	private static String[] columnData = {"", "", "", "", "1", "2", "4", "8", "3.14", "3.1256654353",
			"34564356734", "234783264782364","Hi bob"};
	
	
	private DataRecord dataRecord = null;
	
	@BeforeEach
	void setUp() {
		dataRecord = new DataRecord(columnTypes, columnData);
	}
	
	@AfterEach
	void tearDown() {
	}
	
	@Test
	void getColumnDataType() {
	}
	
	@Test
	void setColumnData() {
	}
	
	@Test
	void setColumnNull() {
	}
	
	@Test
	void getBytes() {
		byte[] bytes = dataRecord.getBytes();
		DataRecord dataRecord2 = new DataRecord(bytes);
		assertEquals(dataRecord, dataRecord2);
	}
	
	@Test
	void getDateTimeTypeString() {
	}
	
	@Test
	void getDateTypeString() {
	}
	
	@Test
	void equals() {
	}
}