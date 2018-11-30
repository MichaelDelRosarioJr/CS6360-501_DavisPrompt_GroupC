package edu.utdallas.cs6360.davisbase;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit Test class for Davisbase
 */
@RunWith (Parameterized.class)
class DavisBaseTest {
	
	@BeforeEach
	void setUp() {
	}
	
	@AfterEach
	void tearDown() {
	}
	
	@Test
	void createTableTest() {
		//assertTrue ("Create Table Test", DavisBase.checkCreateTable("CREATE TABLE table_name ( row_id INT PRIMARY KEY, column_name2 int NOT NULL, column_name3 text)"));
	}
	
	@Test
	void printCmd() {
	}
	
	@Test
	void printDef() {
	}
}