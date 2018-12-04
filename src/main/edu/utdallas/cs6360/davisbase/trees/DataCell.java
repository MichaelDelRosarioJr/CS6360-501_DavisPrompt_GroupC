package edu.utdallas.cs6360.davisbase.trees;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;

/**
 * An abstract class to represent Data Cells<br>
 *     TableInteriorCell and TableLeafCell
 * @author Charles Krol
 * @author Matthew Villarreal
 * @author Michael Del Rosario
 * @author Mithil Vijay
 */
public abstract class DataCell implements Comparable<DataCell> {
	private int rowId;
	
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
	DataCell() {
		this.rowId = -1;
	}
	
	/**
	 * Constructor to create a new DataCell with the given rowId and assumed pageOffset of 0<br>
	 *     i.e. it is the first cell in the file
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
	 * Getter for property 'rowId'.
	 *
	 * @return Value for property 'rowId'.
	 */
	int getRowId() {
		return rowId;
	}
	void setRowId(int id) {
		this.rowId = id;
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
	 * A compareTo method which compares DataCells by rowIds
	 * @param dataCell a DataCell to compare to this one
	 * @return >0 if this > that, 0 if same, 0< if this < that
	 */
	public int compareTo(DataCell dataCell) {
		return this.rowId - dataCell.getRowId();
	}
	
	/**
	 * Determines if two data cells are equal by comparing their page offsets and rowIds
	 * @param o an object to compare
	 * @return true if the equal, false otherwise
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!Optional.ofNullable(o).isPresent()) { return false; }
		
		if (!(o instanceof DataCell)) {
			return false;
		}
		
		DataCell dataCell = (DataCell) o;
		
		return rowId == dataCell.rowId ;
	}
	
	@Override
	public int hashCode() {
		return rowId;
	}
}