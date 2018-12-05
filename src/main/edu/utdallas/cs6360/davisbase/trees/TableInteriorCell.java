package edu.utdallas.cs6360.davisbase.trees;

import edu.utdallas.cs6360.davisbase.utils.ByteHelpers;
import java.nio.ByteBuffer;
import java.util.*;

import static edu.utdallas.cs6360.davisbase.Config.*;
import static edu.utdallas.cs6360.davisbase.trees.TreeConstants.*;

/**
 * Class to represent a Table Interior Cell of 8 bytes in size
 * (4 byte left-subtree pointer and a 4 byte rowId)
 *
 * @author Charles Krol
 * @author Matthew Villarreal
 * @author Michael Del Rosario
 * @author Mithil Vijay
 * @see DataCell
 */
public class TableInteriorCell extends DataCell implements Comparable<DataCell> {
	
	/**
	 * The page number of the next tree page to the left of this cell's
	 */
	private int leftChildPointer;
	
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
		super(Arrays.copyOfRange(data, START_OF_TABLE_INTERIOR_ROWID, START_OF_TABLE_INTERIOR_ROWID + Integer.BYTES));
		ByteBuffer headerBuffer = ByteBuffer.wrap(data);
		this.leftChildPointer = headerBuffer.getInt();
	}
	
	/**
	 * *****************************
	 * *****************************
	 * *****************************
	 *        Getter Methods
	 * *****************************
	 * *****************************
	 * *****************************
	 */
	/**
	 * Used to get the byte representation of a TableInteriorCell for storing later<br>
	 *
	 * @return an ArrayList containing the byte representation of a TableInteriorCell
	 */
	public List<Byte> getBytes() {
		ArrayList<Byte> output = new ArrayList<>();
		
		for(Byte b: ByteHelpers.intToBytes(this.leftChildPointer)) {
			output.add(b);
		}
		
		for(Byte b: ByteHelpers.intToBytes(this.getRowId())) {
			output.add(b);
		}
		return output;
	}
	
	/**
	 * Getter for the size of the interior cell
	 * @return an integer representing the size of an interior cell
	 */
	public int size() {
		return TABLE_INTERIOR_CELL_SIZE;
	}
	
	/**
	 * Getter for property 'leftChildPointer'.
	 *
	 * @return Value for property 'leftChildPointer'.
	 */
	int getLeftChildPointer() {
		return leftChildPointer;
	}
	
	/**
	 * Setter for property 'leftChildPointer'.
	 *
	 * @param leftChildPointer Value to set for property 'leftChildPointer'.
	 */
	void setLeftChildPointer(int leftChildPointer) {
		this.leftChildPointer = leftChildPointer;
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
	 * Method to return the rowId and the left child pointer of the TableInteriorCell for
	 * logging purposes
	 * @return String representation of a TableInteriorCell
	 */
	@Override
	public String toString() {
		return LOGGER_DATACELL_ROWID + this.getRowId() + NEW_LINE + LEFTCHILD_POINTER +
				this.leftChildPointer + NEW_LINE;
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