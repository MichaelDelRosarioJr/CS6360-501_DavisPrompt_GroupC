package edu.utdallas.cs6360.davisbase.trees;

import edu.utdallas.cs6360.davisbase.Config;

import java.nio.ByteBuffer;
import java.util.*;

import static edu.utdallas.cs6360.davisbase.utils.ByteHelpers.intToBytes;
import static edu.utdallas.cs6360.davisbase.utils.ByteHelpers.shortToBytes;

/**
 * Class to represent an interior page of a B+tree and it's cells
 * @author Charles Krol
 * @author Matthew Villarreal
 * @author Michael Del Rosario
 * @author Mithil Vijay
 */
public class TableInteriorPage extends Page{
	private int nextPagePointer;
	
	/**
	 * *****************************
	 * *****************************
	 * *****************************
	 *        Constructors
	 * *****************************
	 * *****************************
	 * *****************************
	 */
	
	/**
	 * Default constructor
	 */
	public TableInteriorPage() {
		super();
		this.nextPagePointer = 0;
	}
	
	/**
	 * Constructor that initializes an interior page without a right pointer
	 * @param pageType the type of page, root or regular
	 * @param pageNumber the page number as it appears in the file
	 */
	public TableInteriorPage(PageType pageType, int pageNumber) {
		super(pageType, pageNumber);
		this.nextPagePointer = -1;
	}
	
	/**
	 * Constructor that initializes an interior page with a right pointer
	 * @param pageType the type of page, root or regular
	 * @param pageNumber the page number as it appears in the file
	 * @param nextPagePointer a page number acting as a pointer to the right subtree in the file
	 */
	public TableInteriorPage(PageType pageType, int pageNumber, int nextPagePointer) {
		super(pageType, pageNumber);
		this.nextPagePointer = nextPagePointer;
	}
	
	
	/**
	 * A constructor to recreate a TableInteriorPage object from it's byte representation stored in the file.
	 *
	 * @param pageHeader the page header in bytes on the disk
	 */
	public TableInteriorPage(byte[] pageHeader, int pageNumber) {
		super(pageHeader, pageNumber);
		ByteBuffer headerBuffer = ByteBuffer.wrap(pageHeader);
		
		// Throw away page type, numOfCells, and startOfCellPointers since already assigned by call to super()
		headerBuffer.getInt();
		
		// Get next 4 bytes from ByteBuffer,
		this.nextPagePointer = headerBuffer.getInt();
	}
	
	/**
	 * *****************************
	 * *****************************
	 * *****************************
	 *       Overridden Methods
	 * *****************************
	 * *****************************
	 * *****************************
	 */
	/**
	 * A method to return the byte array containing
	 * DataRecord formatted into the specified format
	 * @return a byte array containing the correctly formatted data
	 */
	@Override
	public List<Byte> getBytes() {
		ArrayList<Byte> output = new ArrayList<>(Config.PAGE_SIZE);
		
		int i = 2;
		output.add(getPageType().getByteCode());
		output.add(getNumOfCells());
		
		for (byte b : shortToBytes(this.getStartOfCellPointers())) {
			output.add(b);
			i++;
		}
		
		for (byte b : intToBytes(this.nextPagePointer)) {
			output.add(b);
			i++;
		}
		output.addAll(i, getDataCellOffsetsBytes());
		
		i += 2*getNumOfCells();
		
		for(DataCell c: getDataCells()) {
			ArrayList<Byte> bytes = (ArrayList<Byte>)c.getBytes();
			for(int j = output.size() - 1; j > output.size() - 1 - Config.TABLE_INTERIOR_CELL_SIZE; j--) {
				output.add(j, bytes.get(j - output.size() + 1));
				if(j <= i) {
					throw new IllegalArgumentException("Error: Offset Array and Data Cells have met and page is full");
				}
			}
		}
		return output;
	}
	
	/**
	 * Method that creates a new interior cell when given a page of bytes and an offset to load from
	 * @param data a page of bytes
	 * @param offset the offset to load data from
	 * @return a TableInteriorCell containing the content from the file
	 */
	@Override
	public DataCell getDataCellAtOffsetInFile(byte[] data, short offset) {
		int beginningOfFirstCell = Config.PAGE_SIZE - 1 - offset;
		
		// Create array and grab data cell bytes
		byte[] payloadBytes = new byte[Config.TABLE_INTERIOR_CELL_SIZE];
		for(int i = beginningOfFirstCell; i > beginningOfFirstCell - Config.TABLE_INTERIOR_CELL_SIZE; i--) {
			payloadBytes[beginningOfFirstCell - i] = data[i];
		}
		
		// Return new data cell
		return new TableInteriorCell(payloadBytes, offset);
	}
	
	/**
	 * Gets the best location to insert a cell into a page depending using the size of an Interior cell<br>
	 *
	 * The DataCells are sorted by their location within the page, normally they are sorted by rowId<br>
	 *
	 * It looks for the first free space and inserts it, if there are none it adds it to the end of the page
	 *
	 * @param newEntrySumOfTextFields ignored for Interior node
	 * @param config a TableConfig class representing the configuration of the tree based on the table's
	 *                  columns
	 * @return the address of the best location for an insertion
	 */
	@Override
	public int getFreeCellLocation(int newEntrySumOfTextFields, TableConfig config) {
		if (getNumOfCells() == ZERO) {
			return ZERO;
		}
		
		// Sort array by their location within the page
		// normally they are sorted by rowId
		sortDataCellsByOffset();
		int lastCellPosition = 0;
		int currentCellPosition = 0;
		for(DataCell c : getDataCells()) {
			currentCellPosition = c.getPageOffset() / Config.TABLE_INTERIOR_CELL_SIZE;
			// If more than 1 position away from last cell then lastCellPosition+1 is free
			if (currentCellPosition - lastCellPosition > ONE) {
				// Fix the array order
				sort();
				return lastCellPosition + ONE;
			}
			lastCellPosition = currentCellPosition;
		}
		// Fix the array order
		sort();
		// Else all are contiguous and we fill in the next free slot
		return getNumOfCells();
	}
	
	/**
	 * TODO: printPage
	 */
	@Override
	public void printPage() {
	
	}
	
	/**
	 * Determines if two TableInteriorPages are equal by comparing their page numbers, pointers, and data cells
	 * @param o an object to compare
	 * @return true if they are the same data cell, false otherwise
	 */
	@Override
	public boolean equals(Object o) {
		if (o == this) { return true; }
		
		// If other object is null return false
		if (!Optional.ofNullable(o).isPresent()) {
			return false;
		}
		
		// If object is not TableInteriorPage return false
		if (!(o instanceof TableInteriorPage)) {
			return false;
		}
		
		// Since not null and TableInteriorPage
		// Cast to TableInteriorPage
		TableInteriorPage that = (TableInteriorPage) o;
		
		return nextPagePointer == that.nextPagePointer && this.getPageNumber() == that.getPageNumber() &&
				this.getPageType() == that.getPageType() && this.getNumOfCells() == that.getNumOfCells() &&
				this.getDataCells().equals(that.getDataCells());
	}
	
	/**
	 * hashCode Method
	 * @return a hashCode representing a TableInteriorDataCell
	 */
	@Override
	public int hashCode() {
		DataCell[] array = new DataCell[getDataCells().size()];
		getDataCells().toArray(array);
		return Arrays.hashCode(array) + getNumOfCells() * getPageTypeCode() * getPageNumber();
	}
}














