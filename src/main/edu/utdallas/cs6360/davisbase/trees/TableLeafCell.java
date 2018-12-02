package edu.utdallas.cs6360.davisbase.trees;

import edu.utdallas.cs6360.davisbase.Config;

import java.nio.ByteBuffer;
import java.util.*;

import static edu.utdallas.cs6360.davisbase.utils.ByteHelpers.intToBytes;
import static edu.utdallas.cs6360.davisbase.utils.ByteHelpers.shortToBytes;

/**
 * Class to represent a TableLeafCell with a 6 byte(2 byte length, 4 byte rowId) and
 * a payload of either variable(if text columns) or constant length(no text columns)
 * @author Charles Krol
 * @author Matthew Villarreal
 * @author Michael Del Rosario
 * @author Mithil Vijay
 */
public class TableLeafCell extends DataCell {
	
	private static final int START_OF_LEAF_CELL_ROW_ID = 2;
	private static final int START_OF_LEAF_CELL_PAYLOAD = 6;
	private DataRecord payload;
	
	/**
	 * *****************************
	 * *****************************
	 * *****************************
	 *         Constructors
	 * *****************************
	 * *****************************
	 * *****************************
	 */
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
	 * Constructor to reinitialize an existing TableLeafCell from it's byte representation stored a cell of bytes
	 * @param data the byte representation of a TableLeafCell
	 * @param offset a short representing the location of the cell within the page
	 */
	TableLeafCell(byte[] data, short offset) {
		super(Arrays.copyOfRange(data, START_OF_LEAF_CELL_ROW_ID, START_OF_LEAF_CELL_ROW_ID + Integer.BYTES),
				offset);
		int payLoadSize = ByteBuffer.wrap(data).getShort(Config.ZERO);
		this.payload = new DataRecord(Arrays.copyOfRange(data, START_OF_LEAF_CELL_PAYLOAD, START_OF_LEAF_CELL_PAYLOAD
				+ payLoadSize));
	}
	
	/**
	 * Constructor to reinitialize an existing TableLeafCell from it's byte representation stored in the file
	 * @param data the byte representation of a TableLeafCell
	 */
	TableLeafCell(byte[] data) {
		super(Arrays.copyOfRange(data, START_OF_LEAF_CELL_ROW_ID, START_OF_LEAF_CELL_ROW_ID + Integer.BYTES));
		int payLoadSize = ByteBuffer.wrap(data).getShort(Config.ZERO);
		this.payload = new DataRecord(Arrays.copyOfRange(data, START_OF_LEAF_CELL_PAYLOAD, START_OF_LEAF_CELL_PAYLOAD
				+ payLoadSize));
	}
	
	/**
	 * *****************************
	 * *****************************
	 * *****************************
	 *           Getters
	 * *****************************
	 * *****************************
	 * *****************************
	 */
	/**
	 * Returns the byte representation of a TableLeafCell to write to the file
	 * @return an ArrayList containing the  byte representation of the TableLeafCell
	 */
	public List<Byte> getBytes() {
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
	 * Returns the side of the TableLeafCell<br>
	 *     2bytes + 4bytes + payloadSize
	 * @return the size in bytes the TableInteriorCell takes up in memory
	 */
	public int size() {
		return Config.TABLE_LEAF_CELL_HEADER_SIZE + this.payload.size();
	}
	
	/**
	 * *****************************
	 * *****************************
	 * *****************************
	 *      Overridden Methods
	 * *****************************
	 * *****************************
	 * *****************************
	 */
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