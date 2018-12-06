package edu.utdallas.cs6360.davisbase.trees;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static edu.utdallas.cs6360.davisbase.Config.*;
import static edu.utdallas.cs6360.davisbase.trees.TreeConstants.*;
import static edu.utdallas.cs6360.davisbase.utils.ByteHelpers.intToBytes;
import static edu.utdallas.cs6360.davisbase.utils.ByteHelpers.shortToBytes;

/**
 * @author Charles Krol
 * @author Matthew Villarreal
 * @author Michael Del Rosario
 * @author Mithil Vijay
 */
public class IndexInteriorPage extends Page {
	private static final Logger LOGGER = Logger.getLogger(IndexInteriorPage.class.getName());
	private boolean textColumns;
	private int recordSizeNoText;
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
	 * For IndexInteriorPages it points to the next right-child sub-tree of our page that can be either a TableLeafPage
	 * or another IndexInteriorPage
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
	public IndexInteriorPage() {
		super();
		this.nextPagePointer = 0;
	}
	
	/**
	 * Constructor that initializes an interior page without a right pointer
	 * @param pageType the type of page, root or regular
	 * @param pageNumber the page number as it appears in the file
	 */
	public IndexInteriorPage(PageType pageType, int pageNumber, TableConfig tableConfig) {
		super(pageType, pageNumber, tableConfig);
		this.nextPagePointer = -1;
	}
	
	/**
	 * Constructor that initializes an interior page with a right pointer
	 * @param pageType the type of page, root or regular
	 * @param pageNumber the page number as it appears in the file
	 * @param nextPagePointer a page number acting as a pointer to the right subtree in the file
	 */
	public IndexInteriorPage(PageType pageType, int pageNumber, int nextPagePointer, TableConfig tableConfig) {
		super(pageType, pageNumber, tableConfig);
		this.nextPagePointer = nextPagePointer;
	}
	
	
	/**
	 * A constructor to recreate a IndexInteriorPage object from it's byte representation stored in the file.
	 *
	 * @param pageHeader the page header in bytes on the disk
	 */
	public IndexInteriorPage(byte[] pageHeader, int pageNumber, TableConfig tableConfig) {
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
	 * @return an 8-byte page header formatted for a IndexInteriorPage
	 */
	@Override
	public List<Byte> getHeaderBytes() {
		ArrayList<Byte> indexInteriorPageHeader = new ArrayList<>();
		
		// Add header type code and number of data cells
		indexInteriorPageHeader.add(PageType.INDEX_INTERIOR_PAGE.getByteCode());
		indexInteriorPageHeader.add(getNumOfCells());
		
		// Convert `startOfCellPointers` to bytes and add to header
		for(Byte b : shortToBytes(getStartOfCellPointers())) {
			indexInteriorPageHeader.add(b);
		}
		
		// Convert `nextPagePointer` to bytes and add to header
		for(Byte b: intToBytes(this.nextPagePointer)) {
			indexInteriorPageHeader.add(b);
		}
		return indexInteriorPageHeader;
	}
	
	/**
	 * Abstract method that all subclasses should implement that returns the number of bytes taken up by the data cells.
	 * This value should already be calculated when preparing the page to be written to the file.<br>
	 *
	 * This is method is mainly used as a last resort if that mistakenly not saved, not calculated, or not set for some
	 * other reason.<br>
	 *
	 * @return the number of bytes taken up by the DataCell storage area
	 */
	short getSizeOfDataCells() {
		if(this.textColumns) {
			short num = ZERO;
			for (DataCell leafCell : getDataCells()) {
				num += leafCell.size();
			}
			return num;
		}
		return (short)(getNumOfCells() * this.recordSizeNoText);
	}
	
	int getNextPage(int rowId) {
		int nextPagePointer = getNextPageForRowId(rowId);
		return nextPagePointer != -ONE ? nextPagePointer : this.nextPagePointer;
	}
	
	/**
	 * Collects and logs information about this page to the console.
	 */
	@Override
	public void printPage() {
		String loggerString = toString() + LOGGER_PAGE_NEXT_POINTER + this.nextPagePointer + NEW_LINE +
				getDataCellStrings();
		LOGGER.log(Level.INFO, loggerString);
	}
	
	/**
	 * Determines if two IndexInteriorPages are equal by comparing their page numbers, pointers, and data cells
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
		
		// If object is not IndexInteriorPage return false
		if (!(o instanceof IndexInteriorPage)) {
			return false;
		}
		
		// Since not null and IndexInteriorPage
		// Cast to IndexInteriorPage
		IndexInteriorPage that = (IndexInteriorPage) o;
		
		return this.nextPagePointer == that.getNextPagePointer() && this.getPageNumber() == that.getPageNumber() &&
				this.getPageType() == that.getPageType() && this.getNumOfCells() == that.getNumOfCells() &&
				this.getDataCells().equals(that.getDataCells());
	}
	
	/**
	 * hashCode Method
	 * @return a hashCode representing a TableInteriorDataCell
	 */
	/*
	@Override
	public int hashCode() {
		DataCell[] array = new DataCell[getDataCells().size()];
		getDataCells().toArray(array);
		return Arrays.hashCode(array) + getNumOfCells() * getPageTypeCode() * getPageNumber();
	}*/
	@Override
	public int hashCode() { return Objects.hash(getDataCells(), getPageType(), getNumOfCells(), getPageNumber()); }
	
}