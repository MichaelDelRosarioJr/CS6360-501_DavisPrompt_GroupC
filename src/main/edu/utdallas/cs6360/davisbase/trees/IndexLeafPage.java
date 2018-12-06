package edu.utdallas.cs6360.davisbase.trees;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static edu.utdallas.cs6360.davisbase.Config.*;
import static edu.utdallas.cs6360.davisbase.trees.TreeConstants.*;
import static edu.utdallas.cs6360.davisbase.utils.ByteHelpers.intToBytes;
import static edu.utdallas.cs6360.davisbase.utils.ByteHelpers.shortToBytes;

/**
 * @author Charles Krol
 * @author Matthew Villarreal
 * @author Michael Del Rosari
 * @author Mithil Vijay
 */
public class IndexLeafPage extends Page{
	/**
	 * A logger that logs things for logging purposes
	 */
	private static final Logger LOGGER = Logger.getLogger(IndexLeafPage.class.getName());
	private boolean textColumns;
	private int recordSizeNoText;
	
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
	public IndexLeafPage() {
		super();
	}
	
	/**
	 * Constructor that creates a new IndexLeafPage from a pageType, pageNumber, and right subtree pointer
	 * @param pageType the type of page, root or normal
	 * @param pageNumber the page number as it will appear in the file
	 * @param nextPagePointer the page number acting as a pointer into the right subtree
	 */
	IndexLeafPage(PageType pageType, int pageNumber, int nextPagePointer, TableConfig tableConfig) {
		super(pageType, pageNumber, tableConfig);
		this.textColumns = tableConfig.hasTextColumns();
		this.recordSizeNoText = tableConfig.getDataRecordSizeNoText();
		if(pageType == PageType.TABLE_LEAF_ROOT && nextPagePointer > ZERO) { throw new
				IllegalArgumentException("Table root leaf page with non-null next page pointer"); }
	}
	
	/**
	 * A constructor to recreate a IndexLeafPage object from it's byte representation stored in the file.
	 *
	 * @param data an array of bytes representing an entire page from a file
	 * @param pageNumber the pageNumber as it appears in the file
	 */
	IndexLeafPage(byte[] data, int pageNumber, TableConfig tableConfig) {
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
		ArrayList<Byte> indexLeafCellHeader = new ArrayList<>();
		
		// Add header type code and number of data cells
		indexLeafCellHeader.add(PageType.INDEX_LEAF_PAGE.getByteCode());
		indexLeafCellHeader.add(getNumOfCells());
		
		// Convert `startOfCellPointers` to bytes and add to header
		for(Byte b : shortToBytes(getStartOfCellPointers())) {
			indexLeafCellHeader.add(b);
		}
		
		// Set next 4-bytes to 0 as the nextPagePointer is not used by the IndexLeafPages to bytes and add to header
		for(Byte b: intToBytes(ZERO)) {
			indexLeafCellHeader.add(b);
		}
		return indexLeafCellHeader;
	}
	
	/**
	 * Collects and logs information about this page to the console.
	 */
	@Override
	public void printPage() {
		String loggerString = toString() + NEW_LINE + getDataCellStrings();
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
	 * Determines if two IndexLeafPage objects are equivalent
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
		if (!(o instanceof IndexLeafPage)) {
			return false;
		}
		
		// Since not null and TableInteriorPage
		// Cast to TableInteriorPage
		IndexLeafPage that = (IndexLeafPage) o;
		
		// Sort the ArrayLists to ensure they are in the same order based on row ID
		Collections.sort(this.getDataCells());
		Collections.sort(that.getDataCells());
		
		return  this.getPageNumber() == that.getPageNumber() &&
				this.getPageType() == that.getPageType() && this.getNumOfCells() == that.getNumOfCells() &&
				this.getDataCells().equals(that.getDataCells());
	}
	
	/**
	 * hashCode
	 * @return a hashCode for a IndexLeafPage object
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
