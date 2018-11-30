package edu.utdallas.cs6360.davisbase.trees;

import edu.utdallas.cs6360.davisbase.DatabaseType;
import edu.utdallas.cs6360.davisbase.utils.FileHandler;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import static edu.utdallas.cs6360.davisbase.Config.*;

/**
 * B+Tree Class
 * @author Charles Krol
 * @author Matthew Villarreal
 * @author Michael Del Rosario
 * @author Mithil Vijay
 */
public class TableTree {
	private static final Logger LOGGER = Logger.getLogger(TableTree.class.getName());
	
	private RandomAccessFile tableFile;
	private Page root;
	private Page tmp;
	private String fileName;
	private String tableName;
	private DatabaseType databaseType;
	private static final int ZERO = 0;
	private TableConfig treeConfig;
	
	// File access modes
	private static final String READ_WRITE_MODE = "rw";
	
	/**
	 * Default constructor that sets fileName and tableName to null and the DB type to USER
	 */
	public TableTree() {
		this.fileName = null;
		this.tableName = null;
		this.databaseType = DatabaseType.USER;
	}
	
	/**
	 * Constructor that accepts the table name as an argument. This constructor assumes the
	 * table is a USER table and not a system catalog<br>
	 *
	 * The filename is created and it checks if the file already exists. If it does not exist
	 * the root page/node is created which is also a leaf node.
	 * @param tableName the name of the user table to access
	 */
	public TableTree(String tableName, DataType[] colTypes) throws IOException {
		this.tableName = tableName;
		this.databaseType = DatabaseType.USER;
		this.fileName = FileHandler.getTableFileName(this.tableName, this.databaseType);
		this.treeConfig = new TableConfig(colTypes);
		openTreeFile();
	}
	
	/**
	 * Constructor that accepts the table name and the database type as arguments. <br>
	 *
	 * This constructor should be used when attemping to access a system catalog table
	 * @param tableName the name of the table to access
	 * @param type DatabaseType.CATALOG or DatabaseType.USER
	 */
	public TableTree(String tableName, DatabaseType type) {
		this.tableName = tableName;
		this.databaseType = type;
		this.fileName = FileHandler.getTableFileName(this.tableName, this.databaseType);
		
		if(!FileHandler.doesTableExist(this.fileName)) {
			try (RandomAccessFile file = new RandomAccessFile(this.fileName, "rw")) {
				file.setLength(PAGE_SIZE);
				TableLeafPage rootNode = new TableLeafPage(PageType.TABLE_LEAF_ROOT, ZERO, ZERO);
				//file.write(rootNode.getBytes());
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "Unable to create the " + this.tableName + " file");
				LOGGER.log(Level.SEVERE, e.toString());
			}
		}
	}
	
	public void insert(int rowId, DataType[] colTypes, String[] colValues) throws IOException {
		//TODO: Do we need to insert the rowid into the colTypes and colValues?
		
		// Check if root page is null
		byte[] header = getHeader(ZERO);
		if (Page.isFreePage(header)) {
			throw new IllegalStateException("Error can't insert into null root");
		}
		
		if (rowId < ZERO) {
			throw new IllegalArgumentException("Error null key");
		}
		
		if (this.root.isFull(this.treeConfig)) {
			// Save root
			this.tmp = this.root;
			TableInteriorPage tmp2 = new TableInteriorPage(PageType.TABLE_INTERIOR_ROOT, getNewPageNumber());
			TableInteriorCell cellTmp = new TableInteriorCell(rowId, this.tmp.getPageNumber());
			//tmp2.addOffsetToList(ZERO, new);
			// TODO: FINISH INSERT TREE
			// Split Cells and reasign pointers and write to file
		}
	}
	
	public byte[] getHeader(int pageNumber) throws IOException{
		this.tableFile.seek(pageNumber * PAGE_SIZE);
		byte[] header = new byte[8];
		this.tableFile.read(header, ZERO, header.length);
		
		return header;
	}
	
	private void splitTree(TableInteriorPage newParent, int key) throws IOException{
		int pageNumber;
		//TODO Split Tree
	}
	
	private void expandFile() throws IOException{
		this.tableFile.setLength(this.tableFile.length() + PAGE_SIZE);
	}
	
	private void shrinkFile() throws IOException {
		this.tableFile.setLength(this.tableFile.length() - PAGE_SIZE);
	}
	
	private int getNumOfPages() throws IOException{
		return (int)this.tableFile.length() / PAGE_SIZE;
	}
	
	/**
	 *
	 * @throws IOException
	 */
	private void openTreeFile() throws IOException {
		if (FileHandler.doesTableExist(this.fileName)) {
			this.tableFile = new RandomAccessFile(this.fileName, READ_WRITE_MODE);
			LOGGER.log(Level.INFO, "Table {0) exists", this.tableName);
			getRootPage();
		} else {
			// Need to create new file
			LOGGER.log(Level.INFO, "Creating new table file for: {0}", this.tableName);
			createTreeFile();
		}
	}
	
	private void getRootPage() throws IOException{
		this.tableFile.seek(ZERO);
		byte[] rootHeader = new byte[8];
		this.tableFile.read(rootHeader, ZERO, rootHeader.length);
		
		switch (PageType.getEnum(rootHeader[ZERO])) {
			case TABLE_INTERIOR_PAGE:
				root = new TableInteriorPage(rootHeader, ZERO);
				break;
			case TABLE_LEAF_PAGE:
				root = new TableLeafPage(rootHeader, ZERO);
				break;
			default:
				throw new IllegalStateException("Invalid page type code in file");
		}
	}
	
	/**
	 * Method to create a tree file and insert a root page
	 * @throws IOException
	 */
	private void createTreeFile() throws IOException{
		FileHandler.createTableFile(this.fileName);
		LOGGER.log(Level.INFO, "New table file for {0} created", this.tableName);
		this.tableFile = new RandomAccessFile(this.fileName, READ_WRITE_MODE);
		createNewRootLeaf();
	}
	
	private void createNewRootLeaf() throws IOException{
		TableLeafPage tableLeafPage = new TableLeafPage(PageType.TABLE_LEAF_ROOT, ZERO, ZERO);
		tableLeafPage.writePage(this.tableFile);
		LOGGER.log(Level.INFO, "New Root Leaf for: {0}: ", this.tableName);
	}
	
	/**
	 * This method searches pages sequentially until it finds a page header of all null values
	 * meaning we have found a free page. If a page is not found a new one is added at the end of the file
	 */
	private int getNewPageNumber() throws IOException{
		int numOfPagesInFile = getNumOfPages();
		try (RandomAccessFile file = new RandomAccessFile(this.fileName, READ_WRITE_MODE)) {
			int pageNum;
			
			byte[] header = new byte[8];
			for(pageNum = ZERO;pageNum < numOfPagesInFile; pageNum++) {
				// pageNumber = pageNum * PageSize
				file.seek((long) pageNum * PAGE_SIZE);
				// Read in 8 bytes from the file to the array and check if free page
				file.read(header, ZERO, header.length);
				if (Page.isFreePage(header)) {
					return pageNum;
				}
			}
			// No free pages, expand file to create new page and return it's number
			pageNum++;
			expandFile();
			return pageNum;
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.toString());
		}
		return -ONE;
	}
}