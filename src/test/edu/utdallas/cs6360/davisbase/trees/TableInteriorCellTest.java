package edu.utdallas.cs6360.davisbase.trees;

import edu.utdallas.cs6360.davisbase.utils.ByteHelpers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the TableInteriorCellClass
 * @author Charles Krol
 * @author Matthew Villarreal
 * @author Michael Del Rosario
 * @author Mithil Vijay
 *
 */
class TableInteriorCellTest {
	private static final int ROW_ID = 50;
	private static final int LEFT_CHILD_POINTER = 34234;
	private TableInteriorCell tableInteriorCell;
	
	@BeforeEach
	void setUp() {
		tableInteriorCell = new TableInteriorCell(ROW_ID, LEFT_CHILD_POINTER);
	}
	
	@Test
	void getBytes() {
		byte[] bytes = ByteHelpers.byteArrayListToArray(tableInteriorCell.getBytes());
		TableInteriorCell testCell = new TableInteriorCell(bytes);
		assertEquals(tableInteriorCell, testCell);
	}
}