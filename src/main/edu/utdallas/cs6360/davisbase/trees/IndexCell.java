package edu.utdallas.cs6360.davisbase.trees;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static edu.utdallas.cs6360.davisbase.Config.*;

public abstract class IndexCell implements Comparable<IndexCell> {
	
	
	private DataRecord indexPayload;
		
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
	IndexCell() {
		this.indexPayload = null;
	}
	
	/**
	 * Constructor to create a new IndexCell with the given DataRecord
	 * @param dataRecord the data to store in this index cell
	 */
	IndexCell(DataRecord dataRecord) {
		this.indexPayload = dataRecord;
	}
	
	/**
	 * Constructor that initializes a DataCell when given a byte representation of
	 * its row ID
	 * @param data the byte representation of the DataCell's rowId
	 */
	IndexCell(byte[] data) {
		this.indexPayload = new DataRecord(data);
	}
	
	/**
	 * *****************************
	 * *****************************
	 * *****************************
	 *      Abstract Methods
	 * *****************************
	 * *****************************
	 * *****************************
	 */
	
	/**
	 * Abstract method to get the number of bytes of an instance to store on disk
	 * @return the number of bytes the cell will take up on disk
	 */
	public abstract int size();
	
	/**
	 * Abstract method to get the byte representation of a data cell to store on disk
	 * @return the bytes of the data cell to store on the disk
	 */
	public abstract List<Byte> getBytes();
	
	
	/**
	 * *****************************
	 * *****************************
	 * *****************************
	 *      Setters and Getters
	 * *****************************
	 * *****************************
	 * *****************************
	 */
	
	/**
	 * Get the value of the first column in this index entry as this value acts as a Primary Key into the index
	 * @return the value of the first column in the index
	 */
	String getFirstIndexColumnValue() {
		// TODO: set up sorting on secondary and third columns
		return this.indexPayload.getColumnData().get(ZERO);
	}
	
	/**
	 * Method to return the last column in the index entry which should be the rowID of this entry into the actual
	 * Table
	 * @return the rowId(Primary Key for entry in Table) for the selected index entry
	 */
	public String getRowId() {
		ArrayList<String> colData = this.indexPayload.getColumnData();
		return colData.get(colData.size() - ONE);
	}
	
	/**
	 * Getter for property 'indexPayload'.
	 *
	 * @return Value for property 'indexPayload'.
	 */
	public DataRecord getIndexPayload() {
		return indexPayload;
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
	 * At minimum returns the rowId, different depending on which subclass is implementing it
	 * @return a String representation of the DataCell
	 */
	public abstract String toString();
	
	
	/**
	 * A compareTo method which compares IndexCells by their first index column
	 * @param indexCell an IndexCell to compare to this one
	 * @return >0 if this > that, 0 if same, 0< if this < that
	 */
	public int compareTo(IndexCell indexCell) {
		return getFirstIndexColumnValue().compareTo(indexCell.getFirstIndexColumnValue());
	}
	
	/**
	 * Determines if two index cells are equal by comparing the payloads they wrap
	 * @param o an object to compare
	 * @return true if the equal, false otherwise
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!Optional.ofNullable(o).isPresent()) { return false; }
		
		if (!(o instanceof IndexCell)) {
			return false;
		}
		
		IndexCell indexCell = (IndexCell) o;
		
		return this.indexPayload.equals(indexCell.indexPayload) ;
	}
	
	/**
	 * Implemented for completness
	 * @return the hashcode of a IndexCell which is simply the hashCode() of the wrapped DataRecord
	 */
	@Override
	public int hashCode() {
		return this.indexPayload.hashCode();
	}
}
