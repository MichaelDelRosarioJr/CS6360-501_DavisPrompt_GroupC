package edu.utdallas.cs6360.davisbase.trees;

import edu.utdallas.cs6360.davisbase.utils.ByteHelpers;

import java.nio.ByteBuffer;
import java.util.*;

import static edu.utdallas.cs6360.davisbase.trees.TreeConstants.*;
import static edu.utdallas.cs6360.davisbase.trees.TreeConstants.NEW_LINE;

/**
 *
 *
 * @author Charles Krol
 * @author Matthew Villarreal
 * @author Michael Del Rosario
 * @author Mithil Vijay
 */
public class IndexInteriorCell extends IndexCell{
	
	private static final int START_OF_INTERIOR_INDEX_CELL_PAYLOAD = 6;
	
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
	IndexInteriorCell() {
		super();
		this.leftChildPointer = -1;
	}
	
	/**
	 * Constructor that creates a new IndexInteriorCell
	 * @param leftChildPointer the pageNumber acting as the key pointer into the left child of this cell
	 * @param payload the new index entry to store in this cell
	 */
	IndexInteriorCell(int leftChildPointer, DataRecord payload) {
		super(payload);
		this.leftChildPointer = leftChildPointer;
	}
	
	/**
	 * Constructor that initializes a IndexInteriorCell from it's byte representation in the file<br>
	 *
	 * The payload segment of the cell is loaded into the super constructor so it can use these to bytes to rebuild the
	 * Index record from the bytes then the leftChildPointer is stored
	 * @param data the byte representation of a TableInteriorCell stored in a file
	 */
	IndexInteriorCell(byte[] data) {
		super(Arrays.copyOfRange(data, START_OF_INTERIOR_INDEX_CELL_PAYLOAD, data.length));
		this.leftChildPointer = ByteBuffer.wrap(data).getInt();
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
	 * Used to get the byte representation of a IndexInteriorCell for storing later<br>
	 *
	 * Format:
	 *      4-byte left page pointer
	 *      2-byte signed int representing the number of bytes in the payload(size)
	 *      Payload
	 *
	 * @return an ArrayList containing the byte representation of a IndexInteriorCell
	 */
	public List<Byte> getBytes() {
		ArrayList<Byte> output = new ArrayList<>();
		
		for(Byte b: ByteHelpers.intToBytes(this.leftChildPointer)) {
			output.add(b);
		}
		
		ArrayList<Byte> indexRecord = getIndexPayload().getBytes();
		
		for(Byte b: ByteHelpers.shortToBytes((short)indexRecord.size())) {
			output.add(b);
		}
		
		output.addAll(indexRecord);
		return output;
	}
	
	/**
	 * Getter for the size of the interior cell by having calling the DataRecord's getBytes() method and using size() on
	 * the resulting ArrayList. 6 is added to the result as it is the header size.
	 * @return an integer representing the size of an interior cell
	 */
	public int size() {
		return getIndexPayload().size() + START_OF_INTERIOR_INDEX_CELL_PAYLOAD;
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
	 * TODO: Update this for Index.
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
		
		// If object is not IndexInterior return false
		if (!(o instanceof IndexInteriorCell)) { return false; }
		
		IndexInteriorCell that = (IndexInteriorCell) o;
		
		return Objects.equals(this.leftChildPointer, that.getLeftChildPointer()) &&
				Objects.equals(this.getIndexPayload(), that.getIndexPayload());
	}
	
	/**
	 * Overridden hashCode method
	 * @return the hashCode for the ohject
	 */
	@Override
	public int hashCode() {
		return Objects.hash(leftChildPointer, getIndexPayload());
	}
}
