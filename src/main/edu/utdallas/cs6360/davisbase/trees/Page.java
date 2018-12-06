package edu.utdallas.cs6360.davisbase.trees;

import edu.utdallas.cs6360.davisbase.utils.ByteHelpers;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static edu.utdallas.cs6360.davisbase.Config.*;
import static edu.utdallas.cs6360.davisbase.trees.TreeConstants.*;
import static edu.utdallas.cs6360.davisbase.trees.TreeConstants.LOGGER_PAGE_CAPACITY_2;
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
	 * Constructor recreates a page from the file using it's byte representation and page number
	 * @param data the raw bytes of the page in the file
	 * @param pageNumber the ordering of the page within the file
	 */
	Page(byte[] data, int pageNumber, TableConfig tableConfig) {
		this.pageNumber = pageNumber;
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
		
		// TODO look at removing this value as it can probably be calculated as needed
		this.startOfCellPointers = 0;
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
	 * *****************************
	 * *****************************
	 * *****************************
	 *      Getters and Setters
	 * *****************************
	 * *****************************
	 * *****************************
	 */
	
	/**
	 * Uses the byte representation of a DataCell to add a new cell to the data store array
	 * @param data an array of bytes representing a data cell
	 */
	private void addDataCell(byte[] data) {
		if(isLeaf()) {
			this.dataCells.add(new TableLeafCell(data));
		} else {
			this.dataCells.add(new TableInteriorCell(data));
		}
		Collections.sort(this.dataCells);
	}
	
	/**
	 * Adds a new new DataCell to the tree
	 * @param data a DataCell to store in the tree
	 */
	void addDataCell(DataCell data) {
		if(isLeaf()) {
			if (data instanceof TableLeafCell) {
				this.dataCells.add(data);
				Collections.sort(this.dataCells);
				this.startOfCellPointers += data.size();
			} else {
				throw new IllegalArgumentException("Error: Cannot add non-Leaf data cell to a Leaf page");
			}
		} else {
			if (data instanceof  TableInteriorCell) {
				this.dataCells.add(data);
				Collections.sort(this.dataCells);
				this.startOfCellPointers += TABLE_INTERIOR_CELL_SIZE;
			} else {
				throw new IllegalArgumentException("Error: Cannot add an Interior data cell to a Leaf page");
			}
		}
	}
	
	/**
	 * Retrieves and deletes a specific DataCell from the page
	 * @param rowId the rowId of the DataCell to delete
	 * @return the DataCell being deleted, if it can't be found then null
	 */
	DataCell removeCell(int rowId) {
		int i = 0;
		for(DataCell dataCell : this.dataCells) {
			if (dataCell.getRowId() == rowId) {
				return this.dataCells.remove(i);
			}
		}
		return null;
	}
	
	/**
	 * When given a rowId this will return the page ID of the next page that needs to be traversed into to reach
	 * the bucket/leaf page where this DataCell is either stored or about to be stored
	 * @param rowId the rowId we are inserting or looking for
	 * @return the page number for the next page on our path to the leaf page where this DataCell is stored/will be
	 * stored
	 */
	int getNextPageForRowId(int rowId) {
		Collections.sort(this.dataCells);
		int nextPage = getNextPagePointerForRowId(rowId);
		Collections.sort(this.dataCells);
		return nextPage;
	}
	
	public String toString() {
		return LOGGER_PAGE_TYPE + this.pageType.toString() + NEW_LINE +
				LOGGER_PAGE_NUMBER + this.pageNumber + NEW_LINE +
				LOGGER_PAGE_CAPACITY + this.dataCells.size() + LOGGER_PAGE_CAPACITY_2;
	}
	
	public String getDataCellStrings() {
		StringBuilder builder = new StringBuilder();
		sort();
		int i = ONE;
		for(DataCell dataCell : getDataCells()) {
			builder.append(i).append(COLON_SPACE).append(dataCell.toString());
			i++;
		}
		return builder.toString();
	}
	
	/**
	 * Called by getNextPageForRowId. If the rowId is larger than the maxRowId for the page then we need to use the
	 * the nextPagePointer of the Page or the pointer to the far right child. This is not applicable to IndexLeafPages.
	 * If this is the case we return -1 as it does not have a nextPagePointer field, that is implemented in the
	 * subclasses<br>
	 *
	 * TODO: Move this to subclasses if we have time after writing the Index tree code
	 *
	 * The order of the DataCells is reversed and we find the first rowId that ours is larger than and that cell has
	 * our next page number.
	 * @param rowId the rowId of the entry we are either inserting or looking for
	 * @return the page number for the next page on our path to the leaf page where this DataCell is stored/will be
	 * stored
	 */
	private int getNextPagePointerForRowId(int rowId) {
		if (rowId > getMaxRowId()) {
			return -ONE;
		}
		Collections.reverse(this.dataCells);
		for(DataCell dataCells: getDataCells()) {
			if (rowId > dataCells.getRowId()) {
				return ((TableInteriorCell)dataCells).getLeftChildPointer();
			}
		}
		return -ONE;
	}
	
	
	TableInteriorCell getDataCellForRowId(int rowId) {
		Collections.sort(this.dataCells);
		TableInteriorCell out = getDataCellFromParentForRowId(rowId);
		Collections.sort(this.dataCells);
		return out;
	}
	
	TableInteriorCell getDataCellFromParentForRowId(int rowId) {
		Collections.reverse(this.dataCells);
		for(DataCell dataCells: getDataCells()) {
			if (dataCells.getRowId() <= rowId) {
				return (TableInteriorCell)dataCells;
			}
		}
		return null;
	}
	
	/**
	 * Retrieves the TableInteriorCell that stores the pointer(pageNumber) for the given page. This method is used to
	 * quickly retrieve this cell during the split operation as the rowId to this page(leftChild in split) must now be
	 * updated with the medianRowId to reflect the split.
	 * @param pageNumber the page number being stored by a TableInteriorCell within a page
	 * @return the TableInteriorCell that stores the given pageNumber
	 */
	TableInteriorCell getDataCellFromPagePointer(int pageNumber) {
		for (DataCell dataCell: dataCells) {
			TableInteriorCell tableInteriorCell = (TableInteriorCell)dataCell;
			if (tableInteriorCell.getLeftChildPointer() == pageNumber) {
				return tableInteriorCell;
			}
		}
		TableInteriorCell NULL_CELL = null;
		return NULL_CELL;
	}
	
	/**
	 * This method will be called on the parent of a split. When given a specific rowId the Page will find the DataCell
	 * storing that rowId and retrieve it's neighbor immediately to it's right. This method is used when splitting a
	 * page and we the newRightChild is not a far right page and requires a dedicated TableInteriorCell pointer.<br>
	 *
	 * This method returns the given TableInteriorCell and then the Tree uses the pageNumber pointer to retrieve the
	 * page and that page's minRowId number will be the rowId used in the new TableInteriorCell that will point to our
	 * newRightChild
	 *
	 * @param rowId the rowId used in the current pointer to the newLeftChild from a split
	 * @return the TableInteriorCell of the pointer immediately after it.
	 */
	DataCell getCellRightNeighbor(int rowId) {
		Collections.sort(this.dataCells);
		int i = ZERO;
		for (DataCell dataCell: dataCells) {
			if(dataCell.getRowId() == rowId) {
				return this.dataCells.get(i + ONE);
			}
			i++;
		}
		return null;
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
		return (byte)this.dataCells.size();
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
	int getMedianRowId() {
		Collections.sort(this.dataCells);
		return this.dataCells.get((this.dataCells.size() + ONE) / TWO).getRowId();
	}
	
	/**
	 * Used by the Tree's split method to retrieve the maxRowId stored in this page
	 * @return the largest rowId stored in a DataCell on this page
	 */
	int getMaxRowId() {
		if(this.dataCells.size() == ZERO) {
			return ZERO;
		}
		if(this.dataCells.size() == ONE) {
			return this.dataCells.get(ZERO).getRowId();
		}
		sort();
		return this.dataCells.get(this.dataCells.size() - ONE).getRowId();
	}
	
	/**
	 * Used by the Tree's split method to retrieve the minRowId stored in this page
	 * @return the smallest rowId stored in a DataCell on this page
	 */
	int getMinRowId() {
		sort();
		return this.dataCells.get(ZERO).getRowId();
	}
	
	/**
	 * Retrieves the first dataCell within a tree, useful for descending down the left subtree
	 * @return the far left dataCell stored on this page
	 */
	DataCell getFirst() {
		sort();
		return this.dataCells.get(ZERO);
	}
	
	/**
	 * When given a rowId it returns a DataCell that is storing it. Useful for retrieving DataRecords from leaf pages
	 * and updating TableInteriorCells. <br>
	 *
	 * Binary Search is used to locate the index of the DataCell within the ArrayList with the given rowId. If the index
	 * is -1 then NULL is returned because the given DataCell is not stored on this page, else it returns the requested
	 * dataCell
	 * @param rowId the rowId of the entry we want
	 * @return the DataCell that holds this rowId, null if there is no matching entry
	 */
	DataCell getDataCellFromRowId(int rowId) {
		int index = binarySearch(rowId, ZERO, this.dataCells.size() - ONE);
		return (index != -ONE ? this.dataCells.get(index) : null);
	}
	
	/**
	 * Accepts an ArrayList of DataCells to be removed from the page, this is called when the tree is splitting/merging
	 * pages
	 * @param remove ArrayList of DataCells that need to be removed
	 */
	void removeList(ArrayList<DataCell> remove) {
		this.dataCells.removeAll(remove);
	}
	
	/**
	 * Accepts an ArrayList of DataCells to be added to the page, this is called when the tree is splitting/merging
	 * pages
	 * @param add ArrayList of DataCells that need to be added
	 */
	void addList(ArrayList<DataCell> add) {
		this.dataCells.addAll(add);
		sort();
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
			return this.dataCells.size() >= config.getMaxLeafPageRecords();
		} else {
			return this.dataCells.size() >= config.getMaxInteriorPageCells();
		}
	}
	
	boolean needMerge(TableConfig config) {
		if(isRoot()) { return this.dataCells.size() <= ONE; }
		else if (isLeaf()) {
			return config.getMinLeafPageRecords() >= this.dataCells.size();
		} else {
			return config.getMinInteriorPageCell() >= this.dataCells.size();
		}
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
	boolean isEmpty() { return this.dataCells.size() == ZERO; }
	
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
	
	/**
	 * A method to determine if this table is set up with text columns
 	 * @return true if set up with text columns, false otherwise
	 */
	boolean hasTextColumns() {
		return this.tableConfig.hasTextColumns();
	}
	
	/**
	 * Gets the size of a record for this table without text values.
	 * @return the size of an entry without text values
	 */
	int getRecordSizeNoText() {
		return this.tableConfig.getDataRecordSizeNoText();
	}
	
	/**
	 * A method to determine if the give DataCell is stored in the page
	 * @param dataCell the dataCell we are inquiring about
	 * @return true if the dataCell is stored here, false otherwise
	 */
	boolean contains(DataCell dataCell) {
		for(DataCell d : dataCells) {
			if(d.getRowId() == dataCell.getRowId()) {
				return true;
			}
		}
		return this.dataCells.contains(dataCell);
	}
	
	/**
	 * A recursive Binary Search algorithm for saving literally a minuscule amount of time on DataCell queries in pages.
	 * @param requestedRowId the rowId of the entry we are looking for
	 * @param low the low end of the range we are currently searching through
	 * @param high the high end of the range we are currently searching through
	 * @return the index of the dataCell in the ArrayList with the key requestedRowId
	 */
	private int binarySearch(int requestedRowId, int low, int high) {
		if ( high < low) {
			return -ONE;
		}
		int mid = (low + high)/TWO;
		if(this.dataCells.get(mid).getRowId() > requestedRowId) {
			return binarySearch(requestedRowId, low, mid-ONE);
		} else if (this.dataCells.get(mid).getRowId() < requestedRowId) {
			return binarySearch(requestedRowId, mid+ONE, high);
		} else {
			return mid;
		}
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
	void writePage(RandomAccessFile treeFile) {
		// Get the bytes to write to file
		byte[] pageBytes = byteArrayListToArray(getBytes());
		this.startOfCellPointers = getSizeOfDataCells();
		
		
		// Pointers into the table file
		// The beginning of this page and the start the next and the
		// beginning of the data cell area at the end of the file
		long pageStartAddress = (long)(this.getPageNumber() * PAGE_SIZE);
		long startOfNextPage = pageStartAddress + PAGE_SIZE;
		
		// The remaining values are calculated for logging purposes
		// Sizes of the page segments
		// Header + data cell offsets, the free space in the middle of the page, and the size of the data cell area
		int headerSize = PAGE_HEADER_SIZE + (Short.BYTES * this.dataCells.size());
		
		// Data Cell Area Size = this.startOfCellPointers
		int freeSpaceSize = PAGE_SIZE - this.startOfCellPointers - headerSize;
		
		long startOfFreeSpace = pageStartAddress + headerSize;
		long startOfDataCells = startOfNextPage - (long)this.startOfCellPointers;
		
		try {
			// Information about the writing operation that is about to take place
			LOGGER.log(Level.INFO, "Page number: {0}", this.pageNumber);
			LOGGER.log(Level.INFO, "RandomAccessFile.length(): {0}", treeFile.length());
			LOGGER.log(Level.INFO, "Page header/offset size: {0}", headerSize);
			LOGGER.log(Level.INFO, "Free space size: {0}", freeSpaceSize);
			LOGGER.log(Level.INFO, "Page start address: {0}", pageStartAddress);
			LOGGER.log(Level.INFO, "Free space start address: {0}", startOfFreeSpace);
			LOGGER.log(Level.INFO, "Start of data cells: {0}", startOfDataCells);
			
			long length = treeFile.length();
			// Expand the file if needed
			if(length < startOfNextPage) {
				LOGGER.log(Level.INFO, "Expanding file");
				treeFile.setLength(length + PAGE_SIZE);
			}
			
			treeFile.seek(pageStartAddress);
			treeFile.write(pageBytes);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Error writing Page");
		}
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
}