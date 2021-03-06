package edu.utdallas.cs6360.davisbase.trees;

import edu.utdallas.cs6360.davisbase.utils.ByteHelpers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit test class for DataRecord
 * @author Charles Krol
 * @author Matthew Villarreal
 * @author Michael Del Rosario
 * @author Mithil Vijay
 */
class DataRecordTest {
	private static final Logger LOGGER = Logger.getLogger(DataRecordTest.class.getName());
	static ArrayList<DataType> columnTypes = new ArrayList<>(Arrays.asList(DataType.NULL1_TYPE_CODE,
			DataType.NULL2_TYPE_CODE, DataType.NULL4_TYPE_CODE, DataType.NULL8_TYPE_CODE, DataType.TINY_INT_TYPE_CODE,
			DataType.SHORT_TYPE_CODE, DataType.INT_TYPE_CODE, DataType.LONG_TYPE_CODE,
			DataType.REAL_TYPE_CODE, DataType.DOUBLE_TYPE_CODE, DataType.DATETIME_TYPE_CODE,
			DataType.DATE_TYPE_CODE, DataType.TEXT_TYPE_CODE));
	
	static ArrayList<String> columnData = new ArrayList<>(Arrays.asList("", "", "", "", "1", "2", "4", "8", "3.14",
			"3.1256654353", "34564356734", "234783264782364","Hi bob"));
	
	
	private DataRecord dataRecord = null;
	
	@BeforeEach
	void setUp() {
		//columnTypeBytes[columnTypeBytes.length - 1] = (byte)(columnTypes[columnTypes.length - 1].getTypeCode() + 6);
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
		byte[] bytes = ByteHelpers.byteArrayListToArray(dataRecord.getBytes());
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