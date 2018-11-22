package edu.utdallas.cs6360.davisbase;

import java.util.*;

import static edu.utdallas.cs6360.davisbase.utils.ByteHelpers.intToBytes;
import static edu.utdallas.cs6360.davisbase.utils.ByteHelpers.shortToBytes;

/**
 * Class to represent a TableLeafCell
 * @author Charles Krol
 */
public class TableLeafCell extends DataCell {
	private static final int START_OF_LEAF_CELL_ROW_ID = 2;
	private static final int START_OF_LEAF_CELL_PAYLOAD = 6;
	private DataRecord payload;
	
	/**
	 * Default constructor for completeness
	 */
	TableLeafCell() {
		super();
		this.payload = null;
	}
	
	/**
	 * Constructor to create a new TableLeafCell with a given row ID but no payload
	 * @param rowId the rowId to assign to the new TableLeafCell
	 */
	TableLeafCell(int rowId) {
		super(rowId);
		this.payload = null;
	}
	
	/**
	 * Constructor to create a new TableLeafCell with a given rowID and DataRecord payload
	 * @param rowId the rowId to assign to the new TableLeafCell
	 * @param payload the payload to store
	 */
	TableLeafCell(int rowId, DataRecord payload) {
		super(rowId);
		this.payload = payload;
	}
	
	/**
	 * Constructor to reinitialize an existing TableLeafCell from it's byte representation stored in the file
	 * @param data the byte representation of a TableLeafCell
	 */
	TableLeafCell(byte[] data) {
		super(Arrays.copyOfRange(data, START_OF_LEAF_CELL_ROW_ID, START_OF_LEAF_CELL_ROW_ID + Integer.BYTES));
		this.payload = new DataRecord(Arrays.copyOfRange(data, START_OF_LEAF_CELL_PAYLOAD, data.length));
	}
	
	/**
	 * Returns the byte representation of a TableLeafCell to write to the file
	 * @return an ArrayList containing the  byte representation of the TableLeafCell
	 */
	List<Byte> getBytes() {
		ArrayList<Byte> output = new ArrayList<>();
		
		// Get payload byte representation
		ArrayList<Byte> payloadBytes= this.payload.getBytes();
		
		// Get payload size for cells 0-1 at the beginning of the header
		for(byte b : shortToBytes((short)payloadBytes.size())) {
			output.add(b);
		}
		
		// It rowId for bytes 2-5 of the header
		for(byte b : intToBytes(this.getRowId())) {
			output.add(b);
		}
		
		// Add payload to ArrayList
		output.addAll(payloadBytes);
		
		return output;
	}
	
	/**
	 * Used mainly for JUnit test classes
	 * @param o an object to compare
	 * @return true of the objects are equal, false otherwise
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!Optional.ofNullable(o).isPresent()) { return false; }
		
		// If object is not DataRecord return false
		if (!(o instanceof TableLeafCell)) { return false; }
		
		TableLeafCell that = (TableLeafCell) o;
		
		return Objects.equals(this.payload, that.payload) &&
				Objects.equals(this.getRowId(), that.getRowId());
	}
	
	/**
	 * hashCode for completeness
	 * @return the TableLeafCell hashCode
	 */
	@Override
	public int hashCode() {
		return Objects.hash(payload, getRowId());
	}
}
