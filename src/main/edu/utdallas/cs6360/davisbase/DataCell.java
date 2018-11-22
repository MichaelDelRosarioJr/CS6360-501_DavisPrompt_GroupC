package edu.utdallas.cs6360.davisbase;

import java.nio.ByteBuffer;

/**
 * An abstract class to represent Data Cells
 * @author Charles Krol
 */
public abstract class DataCell {
	private int rowId;
	
	/**
	 * Default constructor for completeness
	 */
	DataCell() {
		this.rowId = -1;
	}
	
	/**
	 * Constructor to create a new DataCell with the given rowId
	 * @param rowId the Id of the row to create a new cell for
	 */
	DataCell(int rowId) {
		this.rowId = rowId;
	}
	
	/**
	 * Constructor that initializes a DataCell when given a byte representation of
	 * its row ID
	 * @param data the byte representation of the DataCell's rowId
	 */
	DataCell(byte[] data) {
		this.rowId = ByteBuffer.wrap(data).getInt();
	}
	
	/**
	 * Getter for property 'rowId'.
	 *
	 * @return Value for property 'rowId'.
	 */
	int getRowId() {
		return rowId;
	}
}
