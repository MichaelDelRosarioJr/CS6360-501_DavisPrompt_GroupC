package edu.utdallas.cs6360.davisbase.trees;

import java.util.*;

import static edu.utdallas.cs6360.davisbase.trees.TreeConstants.*;
import static edu.utdallas.cs6360.davisbase.utils.ByteHelpers.shortToBytes;

/**
 * Index B-Tree Leaf Cell (header 0x0a):
 * 	2-byte SMALLINT which is the total number of bytes of key payload
 * -Payload: The initial portion of the payload that does not spill to overflow pages.
 *
 * @author Charles Krol
 * @author Matthew Villarreal
 * @author Michael Del Rosario
 * @author Mithil Vijay
 */
public class IndexLeafCell extends IndexCell {
	private static final int START_OF_INDEX_LEAF_CELL_PAYLOAD = 2;
	
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
	IndexLeafCell() {
		super();
	}
	
	/**
	 * Constructor to create a new IndexLeafCell with a given rowID and DataRecord payload
	 * @param payload the payload to store
	 */
	IndexLeafCell(DataRecord payload) {
		super(payload);
	}
	
	/**
	 * Constructor to reinitialize an existing IndexLeafCell from it's byte representation stored in the file
	 * @param data the byte representation of a IndexLeafCell
	 */
	IndexLeafCell(byte[] data) {
		super(Arrays.copyOfRange(data, START_OF_INDEX_LEAF_CELL_PAYLOAD, data.length));
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
	 * Returns the byte representation of a IndexLeafCell to write to the file
	 * @return an ArrayList containing the  byte representation of the IndexLeafCell
	 */
	public List<Byte> getBytes() {
		ArrayList<Byte> output = new ArrayList<>();
		
		// Get payload byte representation
		ArrayList<Byte> payloadBytes= getIndexPayload().getBytes();
		
		// Get payload size for cells 0-1 at the beginning of the header
		for(Byte b : shortToBytes((short)payloadBytes.size())) {
			output.add(b);
		}
		
		// Add payload to ArrayList
		output.addAll(payloadBytes);
		
		return output;
	}
	
	/**
	 * Returns the size of the IndexLeafCell<br>
	 *     2bytes(payload size) + payloadSize
	 * @return the size in bytes the IndexLeafCell takes up in memory
	 */
	public int size() {
		return getIndexPayload().size() + START_OF_INDEX_LEAF_CELL_PAYLOAD;
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
	 * Method to output basic data about a IndexLeafCell and it's payload
	 * @return String representation of the IndexLeafCell
	 * // TODO UPDATE for Index
	 */
	@Override
	public String toString() {
		return LOGGER_DATACELL_ROWID + getRowId() + NEW_LINE +
				LOGGER_PAYLOAD + getIndexPayload().toString();
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
		if (!(o instanceof IndexLeafCell)) { return false; }
		
		IndexLeafCell that = (IndexLeafCell) o;
		
		return Objects.equals(getIndexPayload(), that.getIndexPayload());
	}
	
	/**
	 * hashCode for completeness
	 * @return the IndexLeafCell hashCode
	 */
	@Override
	public int hashCode() {
		return Objects.hash(getIndexPayload());
	}
}
