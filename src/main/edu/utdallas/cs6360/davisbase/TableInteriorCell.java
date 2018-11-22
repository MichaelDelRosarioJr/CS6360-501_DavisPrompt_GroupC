package edu.utdallas.cs6360.davisbase;

import edu.utdallas.cs6360.davisbase.utils.ByteHelpers;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * Class to represent a Table Interior Cell
 * @author Charles Krol
 * @see DataCell
 */
public class TableInteriorCell extends DataCell {
	public static final int START_OF_TABLE_INTERIOR_ROWID = 4;
	// Page Number
	private int leftChildPointer;
	
	/**
	 * Default constructor for completeness
	 */
	TableInteriorCell() {
		super();
		this.leftChildPointer = -1;
	}
	
	/**
	 * Constructor that creates a new TableInteriorCell
	 * @param rowId the row ID for the item that is the keyDelimiter
	 * @param leftChildPointer the pointer into the left sub tree
	 */
	TableInteriorCell(int rowId, int leftChildPointer) {
		super(rowId);
		this.leftChildPointer = leftChildPointer;
	}
	
	/**
	 * Constructor that initializes a TableInteriorCell from it's byte
	 * representation in the file<br>
	 *
	 * First the rowId is grabbed and sent to the super constrcutor<br>
	 * Then the leftChildPointer is stored
	 * @param data the byte representation of a TableInteriorCell stored in a file
	 */
	TableInteriorCell(byte[] data) {
		super(Arrays.copyOfRange(data, START_OF_TABLE_INTERIOR_ROWID, data.length));
		ByteBuffer headerBuffer = ByteBuffer.wrap(data);
		this.leftChildPointer = headerBuffer.getInt();
	}
	
	/**
	 * Used to get the byte representation of a TableInteriorCell for storing later<br>
	 *
	 * @return an ArrayList containing the byte representation of a TableInteriorCell
	 */
	List<Byte> getBytes() {
		ArrayList<Byte> output = new ArrayList<>();
		
		for(byte b: ByteHelpers.intToBytes(this.leftChildPointer)) {
			output.add(b);
		}
		
		for(byte b: ByteHelpers.intToBytes(this.getRowId())) {
			output.add(b);
		}
		
		return output;
	}
	
	/**
	 * Overridden toEquals class to help with unit tests
	 * @param o hopefully a TableInteriorCell object to compare
	 * @return true if they are the same, false otherwise
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!Optional.ofNullable(o).isPresent()) { return false; }
		
		// If object is not DataRecord return false
		if (!(o instanceof TableInteriorCell)) { return false; }
		
		TableInteriorCell that = (TableInteriorCell) o;
		
		return Objects.equals(this.leftChildPointer, that.leftChildPointer) &&
				Objects.equals(this.getRowId(), that.getRowId());
	}
	
	/**
	 * Overridden hashCode method
	 * @return the hashCode for the ohject
	 */
	@Override
	public int hashCode() {
		return Objects.hash(leftChildPointer, this.getRowId());
	}
}
