package edu.utdallas.cs6360.davisbase.trees;
import edu.utdallas.cs6360.davisbase.utils.FileHandler;

import static edu.utdallas.cs6360.davisbase.Config.*;
import static edu.utdallas.cs6360.davisbase.utils.FileHandler.*;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class TableTreeTest {
	final static int NUM_OF_CELLS = 5000;
	static TableTree tree;
	static TableConfig config;
	static ArrayList<DataType> testDataTypes = new ArrayList<>(Arrays.asList(DataType.LONG_TYPE_CODE,
			DataType.LONG_TYPE_CODE, DataType.LONG_TYPE_CODE, DataType.LONG_TYPE_CODE, DataType.LONG_TYPE_CODE,
			DataType.LONG_TYPE_CODE, DataType.LONG_TYPE_CODE, DataType.LONG_TYPE_CODE));
	static ArrayList<DataRecord> testData;
	
	@BeforeEach
	void setUp() {
		
		createDatabaseDirectory(DATA_DIRECTORY);
		createDatabaseDirectory(CATALOG_DIRECTORY);
		createDatabaseDirectory(USER_DATA_DIRECTORY);
		
		tree = new TableTree("TableTree-Test", testDataTypes);
		testData = new ArrayList<>();
		for(int i = ZERO; i < NUM_OF_CELLS; i++) {
			String col1 = Integer.toString(ZERO);
			String col2 = Integer.toString(ZERO);
			String col3 = Integer.toString(i);
			String col4 = Integer.toString(i);
			String col5 = Integer.toString(i);
			String col6 = Integer.toString(i);
			String col7 = Integer.toString(i);
			String col8 = Integer.toString(i);
			
			ArrayList<String> colValues = new ArrayList<>(Arrays.asList(col1, col2, col3, col4, col5, col6,
					col7, col8));
			testData.add(new DataRecord(testDataTypes, colValues));
		}
	}
	
	@AfterEach
	void tearDown() {
		deleteFile(tree.getFileName());
	}
	
	@Test
	void insert() {
		for(DataRecord record: testData) {
			tree.insert(record);
		}
		assertEquals(5000, tree.getRowIdCounter());
		System.out.println(tree.getCurrentHeight());
	}
}