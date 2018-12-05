package edu.utdallas.cs6360.davisbase.trees;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static edu.utdallas.cs6360.davisbase.Config.*;
import static edu.utdallas.cs6360.davisbase.trees.TreeConstants.*;
import static edu.utdallas.cs6360.davisbase.utils.ByteHelpers.intToBytes;
import static edu.utdallas.cs6360.davisbase.utils.ByteHelpers.shortToBytes;

/**
 * Class to represent a leaf page in a file and it's cells
 * @author Charles Krol
 * @author Matthew Villarreal
 * @author Michael Del Rosario
 * @author Mithil Vijay
 */
public class TableLeafPage extends Page{
	/**
	 * A logger that logs things for logging purposes
	 */
	private static final Logger LOGGER = Logger.getLogger(TableLeafPage.class.getName());
	private boolean textColumns;
	private int recordSizeNoText;
	
	/**
	 * 4 byte page number that is the page number of the right child of the Page.<br>
	 *
	 * For TableLeafPages it points to the next TableLeafPage in the LinkedList of TableLeafPages at the last level
	 * in a Table Tree(B+Tree)
	 * @see TableLeafPage
	 */
	private int nextPagePointer;
	
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
	 * Default constructor
	 */
	public TableLeafPage() {
		super();
	}
	
	/**
	 * Constructor that creates a new TableLeafPage from a pageType, pageNumber, and right subtree pointer
	 * @param pageType the type of page, root or normal
	 * @param pageNumber the page number as it will appear in the file
	 * @param nextPagePointer the page number acting as a pointer into the right subtree
	 */
	TableLeafPage(PageType pageType, int pageNumber, int nextPagePointer, TableConfig tableConfig) {
		super(pageType, pageNumber, tableConfig);
		this.textColumns = tableConfig.hasTextColumns();
		this.recordSizeNoText = tableConfig.getDataRecordSizeNoText();
		this.nextPagePointer = nextPagePointer;
		if(pageType == PageType.TABLE_LEAF_ROOT && nextPagePointer > ZERO) { throw new
				IllegalArgumentException("Table root leaf page with non-null next page pointer"); }
	}
	
	/**
	 * A constructor to recreate a TableLeafPage object from it's byte representation stored in the file.
	 *
	 * @param data an array of bytes representing an entire page from a file
	 * @param pageNumber the pageNumber as it appears in the file
	 */
	TableLeafPage(byte[] data, int pageNumber, TableConfig tableConfig) {
		super(data, pageNumber, tableConfig);
		this.textColumns = tableConfig.hasTextColumns();
		this.recordSizeNoText = tableConfig.getDataRecordSizeNoText();
	}
	
	/**
	 * *****************************
	 * *****************************
	 * *****************************
	 *           Methods
	 * *****************************
	 * *****************************
	 * *****************************
	 */
	
	/**
	 * Returns a TableLeafCell when given a rowId
	 * @param rowId the key of the cell to retrieve
	 * @return the requested TableLeafCell
	 */
	TableLeafCell getDataCell(int rowId) {
		for(DataCell c : getDataCells()) {
			if (c.getRowId() == rowId) {
				return (TableLeafCell)c;
			}
		}
		throw new IllegalArgumentException("DataCell not found");
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
	 * Setter for property 'nextPagePointer'.
	 *
	 * @param nextPagePointer Value to set for property 'nextPagePointer'.
	 */
	public void setNextPagePointer(int nextPagePointer) {
		this.nextPagePointer = nextPagePointer;
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
	 * @return an 8-byte page header formatted for a TableCellPage
	 */
	@Override
	public List<Byte> getHeaderBytes() {
		ArrayList<Byte> tableLeafCellHeader = new ArrayList<>();
		
		// Add header type code and number of data cells
		tableLeafCellHeader.add(PageType.TABLE_LEAF_PAGE.getByteCode());
		tableLeafCellHeader.add(getNumOfCells());
		
		// Convert `startOfCellPointers` to bytes and add to header
		for(Byte b : shortToBytes(getStartOfCellPointers())) {
			tableLeafCellHeader.add(b);
		}
		
		// Convert `nextPagePointer` to bytes and add to header
		for(Byte b: intToBytes(this.nextPagePointer)) {
			tableLeafCellHeader.add(b);
		}
		return tableLeafCellHeader;
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
	
	/**
	 * Determines if two TableLeafPage objects are equivalent
	 * @param o an object to compare
	 * @return true if equivalent, false otherwise
	 */
	@Override
	public boolean equals(Object o) {
		if (o == this) { return true; }
		
		// If other object is null return false
		if (!Optional.ofNullable(o).isPresent()) {
			return false;
		}
		
		// If object is not TableInteriorPage return false
		if (!(o instanceof TableLeafPage)) {
			return false;
		}
		
		// Since not null and TableInteriorPage
		// Cast to TableInteriorPage
		TableLeafPage that = (TableLeafPage) o;
		
		// Sort the ArrayLists to ensure they are in the same order based on row ID
		Collections.sort(this.getDataCells());
		Collections.sort(that.getDataCells());
		
		return this.nextPagePointer == that.getNextPagePointer() && this.getPageNumber() == that.getPageNumber() &&
				this.getPageType() == that.getPageType() && this.getNumOfCells() == that.getNumOfCells() &&
				this.getDataCells().equals(that.getDataCells());
	}
	
	/**
	 * hashCode
	 * @return a hashCode for a TableLeafPage object
	 */
	@Override
	public int hashCode() {
		DataCell[] array = new DataCell[getDataCells().size()];
		getDataCells().toArray(array);
		return Arrays.hashCode(array) + getNumOfCells() * getPageTypeCode() * getPageNumber();
	}
}