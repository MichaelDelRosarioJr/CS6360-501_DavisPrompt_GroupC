package edu.utdallas.cs6360.davisbase.trees;

import edu.utdallas.cs6360.davisbase.utils.ByteHelpers;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static edu.utdallas.cs6360.davisbase.Config.*;
import static edu.utdallas.cs6360.davisbase.utils.ByteHelpers.*;

/**
 * Abstract class containing common tasks associated with managing pages and their data cells
 *
 * TODO: If time go and change all the INTs that will never be larger than bytes/shorts to them respectively
 * @author Charles Krol
 * @author Matthew Villarreal
 * @author Michael Del Rosario
 * @author Mithil Vijay
 */
public abstract class Page {
	/**
	 * A logger that logs things for logging purposes
	 */
	private static final Logger LOGGER = Logger.getLogger(Page.class.getName());
	
	/**
	 * A class that holds metadata relating to the logical database this file backs and methods to calculate various
	 * information about the underlying tree structure such as the maximum number of DataCells a LeafPage can hold when
	 * accounting for the number of space taken up by each row in the file.
	 */
	private TableConfig tableConfig;
	
	/**
	 * 4-byte signed integer page number representing the page's physical position in the file not the logical ordering.
	 */
	private int pageNumber;
	
	/**
	 * An enumerator that can take on the values of: <br>
	 *  <i>INDEX_LEAF_ROOT 0x00</i>,<br>
	 * 	<i>INDEX_INTERIOR_ROOT 0x01</i>,<br>
	 * 	INDEX_INTERIOR_PAGE 0x02,<br>
	 * 	INDEX_LEAF_PAGE 0x0A,<br><br>
	 *
	 * 	<i>TABLE_LEAF_ROOT 0x03</i>,<br>
	 * 	<i>TABLE_INTERIOR_ROOT 0x04</i>,<br>
	 * 	TABLE_INTERIOR_PAGE 0x05,<br>
	 * 	TABLE_LEAF_PAGE 0x0D;<br>
	 *
	 * 	The types in <i>italics</i> represent page types not specified in the requirements, but added to simplify things
	 * 	<b>These new type codes for the various *_ROOT page types are NOT written to file. They are replaced with the
	 * 	correct code at write time by when the getBytes() function calls the getByteCode() method of PageType</b>
	 */
	private PageType pageType;
	
	/**
	 * A 1-byte signed integer representing the number of bytes contained on this page.
	 * Theoretically the maximum number of cells that can fit on a page is the largest value a 1-byte signed integer
	 * can take on. Maximum possible number of records on a page is 127, the actual is much less as they would all have
	 * to be 1 column entries of byte values based on the default PAGE_SIZE = 512.
	 */
	private byte numOfCells;
	
	/**
	 * The start of the data cell pointers from the end of the page
	 * 2^16 - valueFromDisk = startOfCellPointers
	 * Assuming we cannot have a negative value then<br>
	 * Range: [0, 2^15-1],
	 * Range of Possible starting places for data cells:
	 *        [65536, 32769]
	 */
	private short startOfCellPointers;
	
	/**
	 * The ArrayList containing the data cells stored at the end of the [age this page
	 */
	private ArrayList<DataCell> dataCells;
	
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
	Page() {
		this.pageType = null;
		this.pageNumber = -ONE;
		this.dataCells = new ArrayList<>();
		this.tableConfig = new TableConfig();
	}
	
	/**
	 * Constructor that accepts the page type and the number of the file within the page
	 * @param pageType the page type
	 * @param pageNumber the number of the page within the file
	 */
	Page(PageType pageType, int pageNumber, TableConfig tableConfig) {
		this.pageType = pageType;
		this.pageNumber = pageNumber;
		this.dataCells = new ArrayList<>();
		this.tableConfig = tableConfig;
	}
	
	/**
	 * Constructor that accepts a pageType, pageNumber, and List of dataCells<br>
	 * @param pageType the type of page
	 * @param pageNumber the page number and pointer location of the page in the file
	 * @param dataCells the data cells to store in the page
	 */
	Page(PageType pageType, int pageNumber, ArrayList<DataCell> dataCells, TableConfig tableConfig) {
		this.pageType = pageType;
		this.pageNumber = pageNumber;
		this.dataCells = new ArrayList<>(dataCells);
		this.numOfCells = (byte)this.dataCells.size();
		this.tableConfig = tableConfig;
	}
	
	/**
	 * Constructor recreates a page from the file using it's byte representation and page number
	 * @param data the raw bytes of the page in the file
	 * @param pageNumber the ordering of the page within the file
	 */
	Page(byte[] data, int pageNumber, TableConfig tableConfig) {
		// TODO Check page size here
		if(data.length != PAGE_SIZE) {
			LOGGER.log(Level.SEVERE, "Pages must be exactly: {0}", PAGE_SIZE);
			throw new IllegalStateException("Size of array does not match the PAGE_SIZE value");
		}
		
		// Save table config for later use
		this.tableConfig = tableConfig;
		
		// Wrap the byte array in a byteBuffer to simplify out lives
		ByteBuffer byteBuffer = ByteBuffer.wrap(data);
		
		// Get the page type(first byte in the page)
		byte tmpPageType = byteBuffer.get();
		
		// Create new array list to hold the data cells, get the number of cells in the page and where they start within
		// the page
		this.dataCells = new ArrayList<>();
		int numCells = byteBuffer.get();
		short startOfDataCellPointers = byteBuffer.getShort();
		
		// Skip the next 4-bytes which are associated with the rightPagePointers some types of pages use
		// If they need if they can grab it
		byteBuffer.getInt();
		
		// If pageNumber = 0 then it is a root page
		if(pageNumber != ZERO) {
			this.pageType = PageType.getEnum(tmpPageType);
		} else {
			PageType tmpType = PageType.getEnum(tmpPageType);
			if (tmpType == PageType.TABLE_LEAF_PAGE) { this.pageType = PageType.TABLE_LEAF_ROOT; }
			if (tmpType == PageType.TABLE_INTERIOR_PAGE) { this.pageType = PageType.TABLE_INTERIOR_ROOT; }
		}
		
		// Initialize the data cells from the page
		initDataCellsFromBytes(Arrays.copyOfRange(data, data.length - startOfDataCellPointers, data.length),
				numCells, startOfDataCellPointers);
	}
	
	/**
	 * A helper method that strips out the data cells from the end of the page file and reverses the bytes
	 * before returning
	 * @param data anarray of bytes representing the data cell area within the page
	 */
	
	private void initDataCellsFromBytes(byte[] data, int numOfCells, int startOfCellPointers) {
		if(startOfCellPointers != ZERO) {
			// Grab only the DataCell bytes and reverse them
			byte[] inOrderBytes = ByteHelpers.reverseByteArray(data);
			
			// Keep track of our position within the array of bytes
			int byteArrayPointer = ZERO;
			if(isLeaf()) {
				for(int i = 0; i < numOfCells; i++) {
					int dataCellSize = ByteBuffer.wrap(inOrderBytes).getShort(byteArrayPointer) + TABLE_LEAF_CELL_HEADER_SIZE;
					
					addDataCell(Arrays.copyOfRange(inOrderBytes, byteArrayPointer, byteArrayPointer + dataCellSize));
					byteArrayPointer += dataCellSize;
				}
			} else {
				for(int i = 0; i < numOfCells; i++) {
					addDataCell(Arrays.copyOfRange(inOrderBytes, byteArrayPointer, TABLE_INTERIOR_CELL_SIZE +
							byteArrayPointer));
					byteArrayPointer += TABLE_INTERIOR_CELL_SIZE;
				}
			}
		}
	}
	
	/**
	 * Sorts the DataCells based on their location within the Page file
	 */
	void sortDataCellsByOffset() {
		this.dataCells.sort((o1, o2) -> o1.getPageOffset() - o2.getPageOffset());
	}
	
	/**
	 * *****************************
	 * *****************************
	 * *****************************
	 *      Getters and Setters
	 * *****************************
	 * *****************************
	 * *****************************
	 */
	
	/**
	 * Adds a new cell to the data store array
	 * @param data an array of bytes representing a data cell
	 */
	private void addDataCell(byte[] data) {
		if(isLeaf()) {
			this.dataCells.add(new TableLeafCell(data));
			incrementNumOfCells();
		} else {
			incrementNumOfCells();
			this.dataCells.add(new TableInteriorCell(data));
		}
	}
	
	void addDataCell(DataCell data) {
		if(isLeaf()) {
			if (data instanceof TableLeafCell) {
				this.dataCells.add(data);
				incrementNumOfCells();
				this.startOfCellPointers += data.size();
			} else {
				throw new IllegalArgumentException("Error: Cannot add non-Leaf data cell to a Leaf page");
			}
		} else {
			if (data instanceof  TableInteriorCell) {
				this.dataCells.add(data);
				incrementNumOfCells();
				this.startOfCellPointers += TABLE_INTERIOR_CELL_SIZE;
			} else {
				throw new IllegalArgumentException("Error: Cannot add an Interior data cell to a Leaf page");
			}
		}
	}
	
	/**
	 * Getter for property 'dataCells'.
	 *
	 * @return Value for property 'dataCells'.
	 */
	List<DataCell> getDataCells() {
		return dataCells;
	}
	
	/**
	 * Return the page type
	 * @return the type of the page
	 */
	PageType getPageType() { return this.pageType; }
	
	/**
	 * Setter for property 'startOfCellPointers'.
	 * TODO: Remove probably
	 * @param startOfCellPointers Value to set for property 'startOfCellPointers'.
	 */
	public void setStartOfCellPointers(short startOfCellPointers) {
		this.startOfCellPointers = startOfCellPointers;
	}
	
	/**
	 * Getter for property 'startOfCellPointers'.
	 *
	 * @return Value for property 'startOfCellPointers'.
	 */
	short getStartOfCellPointers() {
		return startOfCellPointers;
	}
	
	/**
	 * Getter for property 'numOfCells'.
	 *
	 * @return Value for property 'numOfCells'.
	 */
	byte getNumOfCells() {
		return numOfCells;
	}
	
	/**
	 * Getter for property 'dataCells'.
	 *
	 * @return Value for property 'dataCells'.
	 */
	public List<Byte> getDataCellOffsetsBytes() {
		ArrayList<Byte> output = new ArrayList<>();
		for (DataCell offset : this.dataCells) {
			for (byte b : shortToBytes(offset.getPageOffset())) {
				output.add(b);
			}
		}
		return output;
	}
	
	/**
	 * Return the byte code of the page type to store in the page header on the file
	 * @return the byte codes specified in the DavisBase requirements
	 */
	byte getPageTypeCode() { return this.pageType.getByteCode(); }
	
	/**
	 * Change the type of the current page<br>
	 *
	 * If a table page then it can only switch between it's standard version and root status<br>
	 *     i.e. TABLE_LEAF_ROOT can only become TABLE_LEAF_PAGE and
	 *          TABLE_INTERIOR_ROOT can only become TABLE_INTERIOR_PAGE
	 * @param pageType the new type for this page
	 */
	void setPageType(PageType pageType) {
		// if table page check if this type change is allowed
		if (isTablePage()) {
			if (isLeaf()) {
				this.pageType = pageType;
				if (isInterior()) {
					throw new IllegalArgumentException("Cannot convert Table Leaf Page to Table Interior Page");
				}
			} else {
				this.pageType = pageType;
				if (isLeaf()) {
					throw new IllegalArgumentException("Cannot convert Table Leaf Page to Table Interior Page");
				}
			}
		} else {
			// Else Index page and we don't care about them
			this.pageType = pageType;
		}
	}
	
	/**
	 * Return the page number within the file
	 * @return the page number
	 */
	int getPageNumber() { return this.pageNumber; }
	
	/**
	 * Update the page number
	 * @param pageNumber new number of the page in the file
	 */
	void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }
	
	/**
	 * Returns the rowId of the (nth - 1)/2 item of the DataCells when in sorted order. The Median rowID within a Page
	 * is the rowId we split the page on such that <br>
	 *
	 *              mRowId_______others_____
	 *             /      \                 |
	 *           /         \                 \
	 * New page /       mRowId(smallest       \
	 * smaller rowIds   in this new page)      other pages....
	 * New page
	 *
	 * @return the median rowId within the page that the page can be split on
	 */
	int getMediaRowId() {
		Collections.sort(this.dataCells);
		return this.dataCells.get((this.numOfCells + ONE) / TWO).getRowId();
	}
	/**
	 * Check if the page is full and needs splitting<br>
	 * 
	 * A Page is considered full when the header and cell areas grow
	 * and meet each other
	 * @return true if the page is full, false it it is not
	 */
	boolean isFull(TableConfig config) {
		if (isLeaf()) {
			return config.getMaxLeafPageRecords() == this.numOfCells;
		} else {
			return config.getMaxInteriorPageCells() == this.numOfCells;
		}
	}
	
	boolean needMerge(TableConfig config) {
		if(isRoot()) { return this.numOfCells <= ONE; }
		else if (isLeaf()) {
			return config.getMinLeafPageRecords() >= this.numOfCells;
		} else {
			return config.getMinInteriorPageCell() >= this.numOfCells;
		}
	}
	
	/**
	 * Increases the number of cells on the page
	 */
	void incrementNumOfCells() {
		numOfCells++;
	}
	
	/**
	 * Decreases the number of cells on the page
	 */
	void decrementNumOfCells() {
		numOfCells--;
	}
	
	/**
	 * Sorts the DataCell ArrayList based on rowId
	 */
	void sort() {
		Collections.sort(this.dataCells);
	}
	
	/**
	 * Check if the page is empty and needs merging
	 *
	 * @return true if the page is empty, false otherwise
	 */
	boolean isEmpty() { return this.numOfCells == ZERO; }
	
	/**
	 * Check if the page is a leaf page
	 * @return true if a leaf page, false otherwise
	 */
	boolean isLeaf() { return this.pageType == PageType.TABLE_LEAF_ROOT || this.pageType == PageType.TABLE_LEAF_PAGE ||
			this.pageType == PageType.INDEX_LEAF_PAGE || this.pageType == PageType.INDEX_LEAF_ROOT; }
	
	/**
	 * Check if the page is an interior page
	 * @return true if an interior page, false otherwise
	 */
	boolean isInterior() { return this.pageType == PageType.TABLE_INTERIOR_PAGE ||
			this.pageType == PageType.TABLE_INTERIOR_ROOT || this.pageType == PageType.INDEX_INTERIOR_PAGE ||
			this.pageType == PageType.INDEX_INTERIOR_ROOT; }
	
	/**
	 * Check if the page is a root page
	 * @return true if a root page, false otherwise
	 */
	boolean isRoot() { return this.pageType == PageType.TABLE_LEAF_ROOT ||
			this.pageType == PageType.TABLE_INTERIOR_ROOT || this.pageType == PageType.INDEX_INTERIOR_ROOT ||
			this.pageType == PageType.INDEX_LEAF_ROOT; }
	
	/**
	 * Check if the page is a table page
	 * @return true if a table page, false otherwise
	 */
	boolean isTablePage() { return this.pageType == PageType.TABLE_LEAF_PAGE ||
			this.pageType == PageType.TABLE_LEAF_ROOT || this.pageType == PageType.TABLE_INTERIOR_PAGE ||
			this.pageType == PageType.TABLE_INTERIOR_ROOT;}
	
	/**
	 * Check if the page is a index page
	 * @return true if a index page, false otherwise
	 */
	boolean isIndexPage() { return this.pageType == PageType.INDEX_LEAF_PAGE ||
			this.pageType == PageType.INDEX_LEAF_ROOT || this.pageType == PageType.INDEX_INTERIOR_PAGE ||
			this.pageType == PageType.INDEX_INTERIOR_ROOT; }
			
	boolean hasTextColumns() {
		return this.tableConfig.hasTextColumns();
	}
	
	int getRecordSideNoText() {
		return this.tableConfig.getDataRecordSizeNoText();
	}
	
	boolean contains(DataCell dataCell) {
		for(DataCell d : dataCells) {
			if(d.getRowId() == dataCell.getRowId()) {
				return true;
			}
		}
		
		return this.dataCells.contains(dataCell);
	}
	
	/**
	 * A method to return the ArrayList containing DataRecord formatted in bytes to the specified format to
	 * store into the table/index file. <br>
	 *
	 * The format being: [pageHeader, dataCellOffsets, freeSpace, reversedDataCells]<br>
	 *
	 * Originally function does not add the freeSpace cells between the dataCellOffsets, but to simplify the decision
	 * was made to include the the NULL bytes in the center. Thus this method will output an ArrayList that is always
	 * side PAGE_SIZE. This can be done for error checking purposes
	 *
	 * TODO: if time, switch to array
	 * @return an ArrayList containing the bytes that make up the page
	 */
	public List<Byte> getBytes() {
		ArrayList<Byte> output = new ArrayList<>(PAGE_SIZE);
		
		// Placeholders for the dataCellOffsets and the dataCellBytes that need to be reversed
		ArrayList<Byte> dataCellOffsets = new ArrayList<>();
		ArrayList<Byte> dataCellBytes = new ArrayList<>();
		
		// Initialize the first offset to 0, this is also going to count the total bytes taken up by the data cells.
		// 2 birds 1 for loop
		short dataCellOffset = (short)ZERO;
		
		for(DataCell c: getDataCells()) {
			// Get the offset for this data cell
			for(byte offsetBytes : shortToBytes(dataCellOffset)) {
				dataCellOffsets.add(offsetBytes);
			}
			
			// Get the byte representation of this data cell
			ArrayList<Byte> bytes = (ArrayList<Byte>)c.getBytes();
			
			// Use size of array to determine the cell offset for the next data cell
			dataCellOffset += (short)bytes.size();
			
			// Add bytes to list of data cell bytes
			dataCellBytes.addAll(bytes);
		}
		
		// Save the startOfCellPointers for the writePage method to use
		this.startOfCellPointers = dataCellOffset;
		
		// Now that startOfCellPointers is current call getHeaderBytes which uses it's value.
		output.addAll(getHeaderBytes());
		
		// Append the 2*n offset array after the header
		output.addAll(dataCellOffsets);
		
		// Fill the free space with NULL values
		for(int i = output.size(); i < PAGE_SIZE - this.startOfCellPointers; i++) {
			output.add(NULL_BYTE);
		}
		
		// Reverse the data cell bytes and add them to the en of the array
		Collections.reverse(dataCellBytes);
		output.addAll(dataCellBytes);
		
		return output;
	}
	
	/**
	 * A method to write the update page data to the file, each subclass uses their own getBytes() method which contains
	 * class specific instructions on how to prepare the bytes for the file
	 *  ____ pageStartAddress = (PAGE_SIZE * pageNum)
	 * |    |
	 * |    |
	 * |    |
	 * |----|startOfFreeSpace = pageStartAddress + headerSize = pageStartAddress + 8 + 2*numOfCells
	 * |    |
	 * |    |
	 * |    |
	 * |    |
	 * |    |
	 * |    |
	 * |    |
	 * |----|startOfDataCells = startOfNextPage - startOfCellPointers
	 * |    |
	 * |    |
	 * |    |
	 * |____|
	 * |    |startOfNextPage = pageStartAddress + PAGE_SIZE
	 * |    |
	 * |....|
	 * @param treeFile the RandomAccessFile associated with the page's database file
	 * @throws IOException is thrown when an I/O operation fails
	 */
	void writePage(RandomAccessFile treeFile) throws IOException {
		// Get the bytes to write to file
		byte[] pageBytes = byteArrayListToArray(getBytes());
		if(this.startOfCellPointers == -ONE) {
			this.startOfCellPointers = getSizeOfDataCells();
		}
		
		// Pointers into the table file
		// The beginning of this page and the start the next and the
		// beginning of the data cell area at the end of the file
		long pageStartAddress = ((long)this.getPageNumber() * PAGE_SIZE);
		long startOfNextPage = pageStartAddress + PAGE_SIZE;
		
		// The remaining values are calculated for logging purposes
		// Sizes of the page segments
		// Header + data cell offsets, the free space in the middle of the page, and the size of the data cell area
		int headerSize = PAGE_HEADER_SIZE + (Short.BYTES * this.numOfCells);
		
		// Data Cell Area Size = this.startOfCellPointers
		int freeSpaceSize = PAGE_SIZE - this.startOfCellPointers - headerSize;
		
		long startOfFreeSpace = pageStartAddress + headerSize;
		long startOfDataCells = startOfNextPage - (long)this.startOfCellPointers;
		
		// Information about the writing operation that is about to take place
		LOGGER.log(Level.INFO, "Page number: {0}", this.pageNumber);
		LOGGER.log(Level.INFO, "RandomAccessFile.length(): {0}", treeFile.length());
		LOGGER.log(Level.INFO, "Page header/offset size: {0}", headerSize);
		LOGGER.log(Level.INFO, "Free space size: {0}", freeSpaceSize);
		LOGGER.log(Level.INFO, "Page start address: {0}", pageStartAddress);
		LOGGER.log(Level.INFO, "Free space start address: {0}", startOfFreeSpace);
		LOGGER.log(Level.INFO, "Start of data cells: {0}", startOfDataCells);
		
		
		// Expand the file if needed
		if(treeFile.length() < startOfNextPage) {
			LOGGER.log(Level.INFO, "Expanding file");
			treeFile.setLength(startOfNextPage);
		}
		
		treeFile.seek(pageStartAddress);
		treeFile.write(pageBytes);
	}
	
	/**
	 * *****************************
	 * *****************************
	 * *****************************
	 *       Abstract Methods
	 * *****************************
	 * *****************************
	 * *****************************
	 */
	/**
	 * Abstract method that all subclasses must implement which returns an 8-byte array to be stored at the beginning of
	 * each page and acts as a header containing only the most basic information associated with reconstructing it from
	 * raw bytes. <br>
	 *
	 *
	 * This was mainly implemented as a way to not have to have IndexLeafPages store a `nextPagePointer`
	 * @return an 8-byte array to be stored as a header at the beginning of this page on file
	 */
	abstract List<Byte> getHeaderBytes();
	
	/**
	 * Abstract method that all subclasses must implement that returns the number of bytes taken up by the data cells.
	 * This value should already be calculated when preparing the page to be written to the file.<br>
	 *
	 * This is method is mainly used as a last resort if that mistakenly not saved, not calculated, or not set for some
	 * other reason.<br>
	 *
	 * @return the number of bytes taken up by the DataCell storage area
	 */
	abstract short getSizeOfDataCells();
	
	/**
	 * Abstract class method that all subclasses must implement that contains instructions specific to each page type on
	 * how to log information about itself
	 */
	public abstract void printPage();
	
	/**
	 * *****************************
	 * *****************************
	 * *****************************
	 *        Static Methods
	 * *****************************
	 * *****************************
	 * *****************************
	 */
	/**
	 * A static helper method that accepts an 8 digit page header and if all bytes are null
	 * then it returns true, else it returns false
	 * @param data a page header (8 bytes)
	 * @return true if the page is not being used, false otherwise
	 */
	static boolean isFreePage(byte[] data) {
		for (byte b : data) {
			if (b != ZERO) {
				return false;
			}
		}
		return true;
	}
}