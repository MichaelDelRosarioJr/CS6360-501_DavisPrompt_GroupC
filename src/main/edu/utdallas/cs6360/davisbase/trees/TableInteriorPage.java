package edu.utdallas.cs6360.davisbase.trees;

import java.nio.ByteBuffer;
import java.util.*;

import static edu.utdallas.cs6360.davisbase.Config.*;
import static edu.utdallas.cs6360.davisbase.utils.ByteHelpers.*;

/**
 * Class to represent an interior page of a B+tree and it's cells
 * @author Charles Krol
 * @author Matthew Villarreal
 * @author Michael Del Rosario
 * @author Mithil Vijay
 */
public class TableInteriorPage extends Page{
	/**
	 * Setter for property 'nextPagePointer'.
	 *
	 * @param nextPagePointer Value to set for property 'nextPagePointer'.
	 */
	public void setNextPagePointer(int nextPagePointer) {
		this.nextPagePointer = nextPagePointer;
	}
	
	/**
	 * 4 byte page number that is the page number of the right child of the Page.<br>
	 *
	 * For TableInteriorPages it points to the next right-child sub-tree of our page that can be either a TableLeafPage
	 * or another TableInteriorPage
	 * @see TableLeafPage
	 */
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
	public TableInteriorPage(PageType pageType, int pageNumber, TableConfig tableConfig) {
		super(pageType, pageNumber, tableConfig);
		this.nextPagePointer = -1;
	}
	
	/**
	 * Constructor that initializes an interior page with a right pointer
	 * @param pageType the type of page, root or regular
	 * @param pageNumber the page number as it appears in the file
	 * @param nextPagePointer a page number acting as a pointer to the right subtree in the file
	 */
	public TableInteriorPage(PageType pageType, int pageNumber, int nextPagePointer, TableConfig tableConfig) {
		super(pageType, pageNumber, tableConfig);
		this.nextPagePointer = nextPagePointer;
	}
	
	
	/**
	 * A constructor to recreate a TableInteriorPage object from it's byte representation stored in the file.
	 *
	 * @param pageHeader the page header in bytes on the disk
	 */
	public TableInteriorPage(byte[] pageHeader, int pageNumber, TableConfig tableConfig) {
		super(pageHeader, pageNumber, tableConfig);
		ByteBuffer headerBuffer = ByteBuffer.wrap(pageHeader);
		
		// Throw away page type, numOfCells, and startOfCellPointers since already assigned by call to super()
		headerBuffer.getInt();
		
		// Get next 4 bytes from ByteBuffer,
		this.nextPagePointer = headerBuffer.getInt();
	}
	
	/**
	 * Getter for property 'nextPagePointer'.
	 *
	 * @return Value for property 'nextPagePointer'.
	 */
	int getNextPagePointer() {
		return nextPagePointer;
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
	 * Method returns  8-byte array to be stored at the beginning of each page and acts as a header containing only the
	 * most basic information associated with reconstructing it from raw bytes.
	 * @return an 8-byte page header formatted for a TableInteriorPage
	 */
	@Override
	public List<Byte> getHeaderBytes() {
		ArrayList<Byte> tableInteriorPageHeader = new ArrayList<>();
		
		// Add header type code and number of data cells
		tableInteriorPageHeader.add(PageType.TABLE_INTERIOR_PAGE.getByteCode());
		tableInteriorPageHeader.add(getNumOfCells());
		
		// Convert `startOfCellPointers` to bytes and add to header
		for(byte b : shortToBytes(getStartOfCellPointers())) {
			tableInteriorPageHeader.add(b);
		}
		
		// Convert `nextPagePointer` to bytes and add to header
		for(byte b: intToBytes(this.nextPagePointer)) {
			tableInteriorPageHeader.add(b);
		}
		return tableInteriorPageHeader;
	}
	
	
	/**
	 * This method calculates and returns the number bytes taken up by the DataCell storage area within the page.<br>
	 *
	 * This is method is mainly used as a last resort if that mistakenly not saved, not calculated, or not set for some
	 * other reason.<br>
	 *
	 * For TableInteriorPages since all cells are 8 bytes we simply return TABLE_INTERIOR_CELL_SIZE * numOfCells
	 * @return the number of bytes taken up by the DataCell storage area
	 */
	short getSizeOfDataCells() {
		return (short)(getNumOfCells() * TABLE_INTERIOR_CELL_SIZE);
		
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
		
		return this.nextPagePointer == that.getNextPagePointer() && this.getPageNumber() == that.getPageNumber() &&
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