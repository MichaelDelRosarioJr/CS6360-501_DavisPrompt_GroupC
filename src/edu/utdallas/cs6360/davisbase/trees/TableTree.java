package edu.utdallas.cs6360.davisbase.trees;

import edu.utdallas.cs6360.davisbase.DatabaseType;
import edu.utdallas.cs6360.davisbase.utils.FileHandler;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static edu.utdallas.cs6360.davisbase.Config.*;

/**
 * B+Tree Class
 *
 * The root tree is always held in main memory so user operations never have to do a disk-read on the root before we can
 * access the rest of the tree.<br>
 *
 * All pages instantiated into memory have been entirely read from the file and reconstructed in main memory.<br>
 *
 * The below algorithms do the best to acomplish all of their goals in "one-pass"
 * TODO: Add counting of reads/writes to keep me honest,
 *  -creating new root: 1 reads and potentially 1 write if it does not exist.
 *
 * B+-Tree Algorithms and formulas were taken from the text book used in<i>CS 4349 Advanced Algorithm Design and
 * Analysis</i>. <i>Introduction to Algorithms 3rd Edition</i> a book so synonymous with CS Algorithms curriculum that
 * it is often referred to as the CLRS Algorithms book.
 * <br>
 *
 * - Notes that need to be formatted better
 *	   -All except root: All pages have degree t>=2, i.e. all pages but root must hold t - 1 DataCells
 *		   -min: t-1 data cells
 *		   -max: 2t - 1 data cells
 *	   -Root: For empty tree root can have 0 keys, for nonempty root must have at least 1 key
 *	   -Internal Nodes:
 *	   -full when the number of keys is (2t-1) keys
 *
 *	    -Maybe function to get height from number of nodes?
 *	   	h <= logt((n+1)/2) => n >= 2t^(h) - 1
 *
 * @author Charles Krol
 * @author Matthew Villarreal
 * @author Michael Del Rosario
 * @author Mithil Vijay
 */
public class TableTree {
	/**
	 * The operation mode for the RandomAccessFile
	 */
	private static final String READ_WRITE_MODE = "rw";
	private static final int ZERO = 0;
	
	
	/**
	 * A logger that logs things for logging purposes
	 */
	private static final Logger LOGGER = Logger.getLogger(TableTree.class.getName());
	
	/**
	 * The name of the database this tree supports
	 */
	private String databaseName;
	/**
	 * The fully qualified filename for the file.
	 * TODO: Might not need to store
	 */
	private String fileName;
	
	/**
	 * The RandomAccessFile for the database. Saved so we do not have the make writes immediately to file, they can wait
	 * until they are absolutely necessary
	 */
	private RandomAccessFile tableFile;
	
	/**
	 * An enumarator that can take on the values of USER to represent a database created by the user or CATALOG to
	 * represent a database created by the software to help manage the database
	 */
	private DatabaseType databaseType;
	
	/**
	 * 	Holds metadata about the tree's configuration that all must be easily transferred between pages as we traverse
	 * 	into the tree(#columns/types/etc) and calculates information about B-Trees such as as tree's degree degree.
	 */
	private TableConfig treeConfig;
	
	/**
	 * The logical root page of the tree which is also the first physical page in the file
	 */
	private Page root;
	
	/**
	 * A placeholder used for splitting a node
	 */
	private Page newLeftChild;
	
	/**
	 * The largest page number in the file. This does not have anything to do with the logical ordering of the keys on
	 * the page. Simply the last page within the tile
	 */
	private int largestPageNumber;
	/**
	 * Can used in conjunction with `largestPageNumber` to determine if a new page should be inserted at the end or if a
	 * better location should be searched within the file.<br>
	 *
	 * This can also be used to determine if the file can be shrunk to reclaim empty pages
	 */
	private int numOfPages;
	
	private int nextFreePageNum;
	/**
	 * TODO: Link to Metadata tables
	 */
	public int getRowIdCounter() {
		return rowIdCounter;
	}
	
	public TableConfig getTreeConfig()
	{
		return treeConfig;
	}
	
	/**
	 * TODO: Link to Metadata tables
	 */
	public void incrementRowIdCounter() {
		this.rowIdCounter++;
	}
	
	/**
	 * TODO: Link to Metadata
	 */
	public void decrementRowIdCounter() {
		this.rowIdCounter--;
	}
	
	/**
	 * To keep track of the largest rowId until IndexTables are implemented
	 * TODO: Replace with call to index tables maybe?
	 */
	private int rowIdCounter;
	
	/**
	 * Default constructor that sets fileName and databaseName to null and the DB type to USER
	 */
	public TableTree() {
		this.fileName = null;
		this.databaseName = null;
		this.databaseType = DatabaseType.USER;
	}
	
	/**
	 * Constructor that accepts the table name as an argument. This constructor assumes the
	 * table is a USER table and not a system catalog<br>
	 *
	 * The filename is created and it checks if the file already exists. If it does not exist
	 * the root page/node is created which is also a leaf node.
	 * @param databaseName the name of the user table to access
	 */
	public TableTree(String databaseName, ArrayList<DataType> colTypes) throws IOException {
		this.databaseName = databaseName;
		this.databaseType = DatabaseType.USER;
		this.fileName = FileHandler.getTableFileName(this.databaseName, this.databaseType);
		this.treeConfig = new TableConfig(colTypes);
		openTreeFile();
	}
	
	/**
	 * Constructor that accepts the table name and the database type as arguments. <br>
	 *
	 * To be able to do this insert in "one-pass" without having to re-traverse the tree or pass back information to the
	 * beginning of the traversal we will split any full nodes that cross our path on the way down. This way if we
	 * proceed to the next page and it needs to be split before we can insert we can be sure that the previous page
	 * will not be full because we would have just split it before visiting this page.
	 *
	 * This constructor should be used when attemping to access a system catalog table
	 * @param databaseName the name of the table to access
	 * @param type DatabaseType.CATALOG or DatabaseType.USER
	 * @throws IOException 
	 */
	public TableTree(String databaseName, DatabaseType type) throws IOException {
		this.databaseName = databaseName;
		this.databaseType = type;
		if(databaseName.equals("davisbase_tables"))
		{
			ArrayList<DataType> tableType = new ArrayList<DataType>();
			tableType.add(DataType.TEXT_TYPE_CODE);
			this.treeConfig = new TableConfig(tableType);
		}
		if(databaseName.equals("davisbase_columns"))
		{
			ArrayList <DataType> dt = new ArrayList<DataType>();
			dt.add(DataType.TEXT_TYPE_CODE);
			dt.add(DataType.TEXT_TYPE_CODE);
			dt.add(DataType.TEXT_TYPE_CODE);
			dt.add(DataType.TINY_INT_TYPE_CODE);
			dt.add(DataType.TEXT_TYPE_CODE);
			this.treeConfig = new TableConfig(dt);
		}
		
		this.fileName = FileHandler.getTableFileName(this.databaseName, this.databaseType);
//		System.out.println(fileName);
		openTreeFile();
	}
	
	private void splitChildPage(TableInteriorPage newParent) {
		// Assuming a parent Interior Page
		DataCell dataCell;
		Page rightChild;
		
		// If leaf Page, create a new leaf page
		if (this.newLeftChild.isLeaf()) {
			rightChild = newRightLeafPage(newParent);
			for(DataCell cell : this.newLeftChild.getDataCells()) {
				if (cell.getRowId() > this.newLeftChild.getMediaRowId()) {
					TableLeafCell moveToRight = (TableLeafCell)this.newLeftChild.removeCell(cell.getRowId());
					rightChild.addDataCell(moveToRight);
				}
			}
		}
		if (this.newLeftChild.isInterior()) {
			rightChild = newRightInteriorPage(newParent);
			int medianRowId = this.newLeftChild.getMediaRowId();
			for(DataCell cell : this.newLeftChild.getDataCells()) {
				if (cell.getRowId() > medianRowId) {
					// Move to right child,  Remove from left
					TableInteriorCell moveToRight = (TableInteriorCell)this.newLeftChild.removeCell(cell.getRowId());
					rightChild.addDataCell(moveToRight);
					// If == media row ID then remove as it is now a key in the new parent
				} else if (cell.getRowId() == medianRowId) {
					// If == media row ID then remove as it is now a key in the new parent
					this.newLeftChild.removeCell(cell.getRowId());
				}
			}
			try {
				// Write the new pages the disk
				this.newLeftChild.writePage(tableFile);
				rightChild.writePage(tableFile);
				this.root.writePage(tableFile);
			} catch (IOException e) {
			
			}
		}
		
		
	}
	
	private TableLeafPage newRightLeafPage(TableInteriorPage newParent) {
		// Cast to TableLeafPage so we can access the subclass' getters/setters
		TableLeafPage tmpLeafPage = (TableLeafPage) this.newLeftChild;
		// Create new right child leaf page and store leftChild's rightPointer as the rightChild's new right pointer
		TableLeafPage rightChild = new TableLeafPage(PageType.TABLE_LEAF_PAGE, getNewPageNumber(), tmpLeafPage.getNextPagePointer(), treeConfig);
		
		// Switch the left child's rightPointer to the right child's new page number
		tmpLeafPage.setNextPagePointer(rightChild.getPageNumber());
		return rightChild;
	}
	
	private TableInteriorPage newRightInteriorPage(TableInteriorPage newParent) {
		// Cast to TableInteriorPage so we can access the subclass' getters/setters
		TableInteriorPage tableInteriorPage = (TableInteriorPage) this.newLeftChild;
		
		// Create new right child interior page and store leftChild's rightPointer as the rightChild's new right pointer
		TableInteriorPage rightChild = new TableInteriorPage(PageType.TABLE_INTERIOR_PAGE, getNewPageNumber(), tableInteriorPage.getNextPagePointer(), treeConfig);
		
		// Switch the left child's rightPointer to the right child's new page number
		// Get the rowId that is being promoted and save it's pointer
		// as that is the one that will be removed during the splot
		int medianOfOldParent = this.newLeftChild.getMediaRowId();
		TableInteriorCell promoteded = ((TableInteriorCell)this.newLeftChild.getDataCells().get(medianOfOldParent));
		
		// Save the leftChildPointer of the newly promoted cell and assign it as the rightChildPointer of the page
		tableInteriorPage.setNextPagePointer(promoteded.getLeftChildPointer());
		return rightChild;
	}
	
	/**
	 * 1-of-2 methods that make up the recursive B+Tree traversal insert algorithm.
	 * This is insert method that other classes will call when the users or a system table need to make an insertion
	 * into the file. This one implicitly calls the root page as our starting page.<br>
	 *
	 * The datatypes and values are accepted to abstract away the the creation of the underlying DataCell classes that
	 * hold the DataRecords
	 * @param colTypes A List of byte type codes to represent the various data types that will be stored in this file
	 * @param colValues the actual values of each column for this new row.
	 * @throws IOException
	 */
	public void insert(ArrayList<DataType> colTypes, ArrayList<String> colValues){
		if (Optional.ofNullable(this.root).isPresent()) {
			throw new IllegalStateException("Tree can't have null root");
		}
		
		// Check if valid insert
		validInsert(colTypes, colValues);
		// Create new LeafCell so less to pass around
		insert(new TableLeafCell(getRowIdCounter(), new DataRecord(colTypes, colValues)));
		
		
		// TODO: Count insertions, add in Config class
	}
	
	private void insert(TableLeafCell newRecord) {
		// Check if root is is full and needs splitting
		if (this.root.isFull(this.treeConfig)) {
			// Save old root for the split function
			this.newLeftChild = this.root;
			
			// Get the rowId that is the median rowId of the page
			// Which is the rowId that should be promoted to the new root
			int rootMedianPointer = this.root.getMediaRowId();
			
			// Get the next free page number for its new position in the file
			this.newLeftChild.setPageNumber(getNewPageNumber());
			
			// Create new root and save it since there will be no way to save it later
			this.root = new TableInteriorPage(PageType.TABLE_INTERIOR_PAGE, 0, this.treeConfig);
			
			// Create a new data cell to store in the new root page that points to the left child
			TableInteriorCell newRootCell = new TableInteriorCell(rootMedianPointer, this.newLeftChild.getPageNumber());
			
			this.root.addDataCell(newRootCell);
			// Run split root
			splitChildPage((TableInteriorPage) this.root);
			//insertNonFull(, k)
		}
		
		// traverse down the either insert into the next page if leaf or continue traversing if interior page
		
	}
	/**
	 *
	 * @param currentPage the page following the page that called this method
	 * @param rowId a 4-byte integer rowId
	 */
	private void insertNonFull(Page currentPage, int rowId, TableLeafCell tableLeafCell) throws IOException {
		if(currentPage.isLeaf()) {
			// In Leaf, double check there isn't a duplicate and if there throw an exception, otherwise insert
			TableLeafPage leaf = (TableLeafPage)currentPage;
			
			if(leaf.contains(tableLeafCell)) {
				throw new IllegalStateException("Error, there is a duplicate entry");
			}
			currentPage.addDataCell(tableLeafCell);
		} else {
			// Get the next page number to traverse to and load it from the file
			int nextPageNum = currentPage.getNextPagePointerForRowId(tableLeafCell.getRowId());
			Page nextPage = ((TableInteriorPage)getPage(nextPageNum));
			int nextPagePtr;
			if (nextPage.isFull(treeConfig)) {
				this.newLeftChild = nextPage;
				splitChildPage((TableInteriorPage)currentPage);
				if (rowId > nextPage.getMaxRowId()) {
					nextPagePtr = ((TableInteriorPage) currentPage).getNextPagePointer();
				} else {
					nextPagePtr = ((TableInteriorPage) currentPage).getFirst().getPageOffset();
				}
				nextPage = getPage(nextPagePtr);
				insertNonFull(nextPage, rowId, tableLeafCell);
			}
		}
	}
	
	
	private void treeSearch(Page treePage, int rowId) {
		;
	}
	
	/**
	 * Checks to see if the given insert of colTypes and colValues are valid. Input validation should be done at the
	 * front end
	 * @param colTypes array of bytes that represent each column's data types
	 * @param colValues the actual values that will be stored in each column.
	 * @return true if this is a valid insert false otherwise
	 */
	boolean validInsert(ArrayList<DataType> colTypes, ArrayList<String> colValues) {
		
		if(!DataType.sameColTypes(colTypes, treeConfig.getColTypes())) {
			throw new IllegalArgumentException("Given column data types do not match this tree's column data types");
		}
		
		if(colTypes.size() != colValues.size()) {
			throw new IllegalArgumentException("The column type arrays and the column values array have different " +
					"sizes");
		}
		return true;
	}
	
	private void expandFile() throws IOException{
		this.tableFile.setLength(this.tableFile.length() + PAGE_SIZE);
	}
	
	private void shrinkFile() throws IOException {
		this.tableFile.setLength(this.tableFile.length() - PAGE_SIZE);
	}
	
	private int getNumOfPages() throws IOException{
		return this.numOfPages;
	}
	
	private void incrementPages() {
		this.numOfPages++;
	}
	
	private void decrementPages() {
		this.numOfPages--;
	}
	
	/**
	 * Checks to see if the Tree file exits if it does it sets the root page field of this tree instance if it does
	 * not exist it calls `createTreeFile` to create a new tree file.
	 * @throws IOException if it has a problem reading the file
	 */
	private void openTreeFile() throws IOException {
		if (!FileHandler.doesTableExist(this.fileName)) {
			// Need to create new file
			LOGGER.log(Level.INFO, "Creating new table file for: {0}", this.databaseName);
			createTreeFile();
		} else {
			this.tableFile = new RandomAccessFile(this.fileName, READ_WRITE_MODE);
			LOGGER.log(Level.INFO, "Table {0} exists", this.databaseName);
			getRootPage();
		}
	}
	
	/**
	 * Method to create a tree file and insert a new root page
	 * @throws IOException when it can't write
	 */
	private void createTreeFile() throws IOException{
		// Create file and set length equal to PAGE_SIZE
		System.out.println(fileName);
		FileHandler.createTable(this.fileName, this.databaseType);
		LOGGER.log(Level.INFO, "New table file for {0} created", this.databaseName);
		this.tableFile = new RandomAccessFile(this.fileName, READ_WRITE_MODE);
		this.root = createNewRootLeaf();
	}
	
	/**
	 * Used whenever the file is first created to insert a new root page that also happens to be a leaf because it's a
	 * new file.
	 * TODO: Move to Abstract Tree class
	 * @throws IOException when it can't write to file
	 * @return the new root leaf page just created so it can be saved with for later use
	 */
	private TableLeafPage createNewRootLeaf() throws IOException {
		TableLeafPage tableLeafPage = new TableLeafPage(PageType.TABLE_LEAF_ROOT, ZERO, ZERO, treeConfig);
		tableLeafPage.writePage(this.tableFile);
		LOGGER.log(Level.INFO, "New Root Leaf for: {0}: ", this.databaseName);
		nextFreePageNum++;
		return tableLeafPage;
	}
	
	/**
	 * Retrieves the root page from the beginning of the file and stores it for later use by the tree. It does this
	 * by passing the root page number of 0 to `get()` page
	 * @throws IOException when there was a problem reading from the file
	 */
	private void getRootPage() {
		// Prepare array and file to read in the root page data
		try {
			this.root = getPage(ROOT_PAGE_NUMBER);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Method that retrieves a specific page from the file depending on the page number and the PAGE_SIZE value set
	 * in the Config class.
	 *
	 * TODO: add in Index pages
	 * @see edu.utdallas.cs6360.davisbase.Config
	 * @param pageNumber a 4-byte integer representing the physical position of the desired page within the file
	 *                   relative to the beginning of the file
	 * @return depending on the type code stored in file a
	 * TableInteriorPage/TableLeafPage/IndexInteriorPage/IndexLeafPage
	 * @throws IOException when there was a problem reading from the file
	 */
	private Page getPage(int pageNumber) throws IOException{
		byte[] pageBytes = new byte[PAGE_SIZE];
		this.tableFile.seek(PAGE_SIZE * pageNumber);
		this.tableFile.read(pageBytes);
		
		// Return a different subclass depending on the PageType value
		return PageType.getEnum(pageBytes[ZERO]) == PageType.TABLE_LEAF_PAGE ?
				new TableLeafPage(pageBytes, pageNumber, treeConfig) :
				new TableInteriorPage(pageBytes, pageNumber, treeConfig);
	}
	
	/**
	 * This method searches pages sequentially until it finds a page header of all null values
	 * meaning we have found a free page. If a page is not found a new one is added at the end of the file
	 *
	 * TODO: Implement some search algorithm to find an empty space of 000s/NULLs to insert a new page
	 * 		 Something like binary search?
	 */
	private int getNewPageNumber() {
		if (this.largestPageNumber == this.numOfPages) {
			return this.largestPageNumber + ONE;
		}
		return -ONE;
	}
	
	/**
	 * A method that determines if a sequence of non-repeating numbers is free of gaps using only the minimum value and
	 * the sum of the values.
	 * @param sum
	 */
	static void isInSequence(int sum) {
	
	}
}