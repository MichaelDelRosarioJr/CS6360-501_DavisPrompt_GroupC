package edu.utdallas.cs6360.davisbase;

import edu.utdallas.cs6360.davisbase.trees.DataType;
import edu.utdallas.cs6360.davisbase.trees.TableTree;
import edu.utdallas.cs6360.davisbase.utils.FileHandler;

import java.io.IOException;
import java.util.*;

import static edu.utdallas.cs6360.davisbase.Config.*;

/**
 * @author Charles Krol
 * @author Matthew Villarreal
 * @author Michael Del Rosario
 * @author Mithil Vijay
 * @version 1.0
 * <b>
 * <p>This is an example of how to create an interactive prompt</p>
 * <p>There is also some guidance to get started wiht read/write of
 * binary data files using RandomAccessFile class</p>
 * </b>
 */

public class DavisBase {
    private static boolean isExit = false;
    

    /**
     * The Scanner class is used to collect user commands from the prompt
     * There are many ways to do this. This is just one.
     * <p>
     * Each time the semicolon (;) delimiter is entered, the userCommand
     * String is re-populated.
     */
    private static Scanner scanner = new Scanner(System.in, CHARACTER_SET).useDelimiter(";");

    /**
     * **********************************************************************
     * Main method
     */
    public static void main(String[] args) {

        /* Display the welcome screen */
        splashScreen();

        /* Create data directory if it doesn't exits*/
        FileHandler.initializeDataStore();

        /* Variable to collect user input from the prompt */
        String userCommand = "";

        while (!isExit) {
            /* This can be changed to whatever you like */
            String prompt = "davisql> ";
            System.out.print(prompt);
            /* toLowerCase() renders command case insensitive */
            userCommand = scanner.next().replace("\n", " ").replace("\r", "").trim().toLowerCase();
            parseUserCommand(userCommand);
        }
        System.out.println("Exiting...");


    }

    /* ***********************************************************************
     *  Static method definitions
     */

    /**
     * Display the splash screen
     */
    private static void splashScreen() {
        System.out.println(line("-", 80));
        // Display the string.
        System.out.println("Welcome to DavisBaseLite");
        System.out.println("DavisBaseLite Version " + getVersion());
        System.out.println(getCopyright());
        System.out.println("\nType \"help;\" to display supported commands.");
        System.out.println(line("-", 80));
    }

    /**
     * @param s   The String to be repeated
     * @param num The number of time to repeat String s.
     * @return String A String object, which is the String s appended to itself num times.
     */
    private static String line(String s, int num) {
        StringBuilder a = new StringBuilder();
        for (int i = 0; i < num; i++) {
            a.append(s);
        }
        return a.toString();
    }

    public static void printCmd(String s) {
        System.out.println("\n\t" + s + "\n");
    }

    public static void printDef(String s) {
        System.out.println("\t\t" + s);
    }

    /**
     * Help: Display supported commands
     */
    private static void help() {
        System.out.println(line("*", 80));
        System.out.println("SUPPORTED COMMANDS\n");
        System.out.println("All commands below are case insensitive\n");
        System.out.println("SHOW TABLES;");
        System.out.println("\tDisplay the names of all tables.\n");
        //printCmd("SELECT * FROM <table_name>;");
        //printDef("Display all records in the table <table_name>.");
        System.out.println("SELECT <column_list> FROM <table_name> [WHERE <condition>];");
        System.out.println("\tDisplay table records whose optional <condition>");
        System.out.println("\tis <column_name> = <value>.\n");
        System.out.println("DROP TABLE <table_name>;");
        System.out.println("\tRemove table data (i.e. all records) and its schema.\n");
        System.out.println("UPDATE TABLE <table_name> SET <column_name> = <value> [WHERE <condition>];");
        System.out.println("\tModify records data whose optional <condition> is\n");
        System.out.println("VERSION;");
        System.out.println("\tDisplay the program version.\n");
        System.out.println("HELP;");
        System.out.println("\tDisplay this help information.\n");
        System.out.println("EXIT;");
        System.out.println("\tExit the program.\n");
        System.out.println(line("*", 80));
    }

    /**
     * return the DavisBase version
     */
    private static String getVersion() {
        return VERSION;
    }

    private static String getCopyright() {
        return COPYRIGHT;
    }

    private static void displayVersion() {
        System.out.println("DavisBaseLite Version " + getVersion());
        System.out.println(getCopyright());
    }

    private static void parseUserCommand(String userCommand) {

        /* commandTokens is an array of Strings that contains one token per array element
         * The first token can be used to determine the type of command
         * The other tokens can be used to pass relevant parameters to each command-specific
         * method inside each case statement
         * */
        ArrayList<String> commandTokens = new ArrayList<>(Arrays.asList(userCommand.split(" ")));

        /*
         *  This switch handles a very small list of hardcoded commands of known syntax.
         *  You will want to rewrite this method to interpret more complex commands.
         */
        switch (commandTokens.get(0)) {
            case "show":
                System.out.println("CASE: SHOW");
                showTable(userCommand);
                break;
            case "select":
                System.out.println("CASE: SELECT");
                parseQuery(userCommand);
                break;
            case "drop":
                System.out.println("CASE: DROP");
                dropTable(userCommand);
                break;
            case "create":
                System.out.println("CASE: CREATE");
                if (commandTokens.size() > 1) {
                    if (commandTokens.get(1).equals("index") || commandTokens.get(1).equals("unique")) {
                        parseCreateIndex(userCommand);
                    } else {
                        try {
							parseCreateTable(userCommand);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                    }
                } else {
                    System.out.println("I didn't understand the command: \"" + userCommand + "\"");
                }
                break;
            case "update":
                System.out.println("CASE: UPDATE");
                parseUpdate(userCommand);
                break;
            case "insert":
                System.out.println("CASE: INSERT");
                parseInsert(userCommand);
                break;
            case "delete":
                System.out.println("CASE: DELETE");
                parseDelete(userCommand);
                break;
            case "help":
                help();
                break;
            case "version":
                displayVersion();
                break;
            case "exit":
                isExit = true;
                break;
            case "quit":
                isExit = true;
                break;
            default:
                System.out.println("I didn't understand the command: \"" + userCommand + "\"");
                break;
        }
    }


    /**
     * Stub method for showing all tables
     *
     * @param showTableString is a String of the user input
     */
    private static void showTable(String showTableString) {
        //System.out.println("STUB: This is the showTable method.");

        if (checkShowTable(showTableString)) {
            //System.out.println("SUCCESS! This will print all tables");
        	
        	//TODO: Finish show all tables
        	try {
				TableTree metaDataTables = new TableTree("davisbase_tables", DatabaseType.CATALOG);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

        	
        }

    }

    /**
     * Stub method for dropping tables
     *
     * @param dropTableString is a String of the user input
     */
    private static void dropTable(String dropTableString) {
//        System.out.println("STUB: This is the dropTable method.");
        if (checkDropTable(dropTableString)) {
        	ArrayList<String> tokens = cleanCommand(dropTableString);
        	if(FileHandler.findTable(tokens.get(2)))
        	{
        		System.out.println("\tParsing the string:\"" + dropTableString + "\"");
        		
        		//TODO: Finish drop table implementation
        		
        	}
        }
    }

    /**
     * Stub method for executing queries
     *
     * @param queryString is a String of the user input
     */
    private static void parseQuery(String queryString) {
        //System.out.println("STUB: This is the parseQuery method");
    	
        if (checkQuery(queryString)) {
        	ArrayList<String> tokens = cleanCommand(queryString);
        	//System.out.println(tokens.get(3));
        	if(FileHandler.findTable(getTableNameFromSelect(tokens)))
        	{
        		//System.out.println("\tParsing the string:\"" + queryString + "\"");
        		
        		//TEST: Create a dummy select fields to test printTable
				String[] colName = {"name", "age"};
				String[][] data = {{"michael del rosario", "23"}, {"matt villarreal", "22"}, {"cory krol", "21"}, {"mithl", "23"}};

				//Temporary placement for getting columns and condition
				getColumnsFromSelect(tokens);
				getConditionStatement(tokens);
				
				//TODO: Finish Select method with tables
				TableTree tree = new TableTree();
				
									
				//TODO: create a function that will get all columns in the select statement.
				//Get column names
				ArrayList<String> colNames = getColumnsFromSelect(tokens);
			
				//TODO: create a function that will store the WHERE statement
				ArrayList<String> cond = getConditionStatement(tokens);
					
					
			
        		
        		//Print Table
				printTable(colName, data);
        	}
        }
    }

    /**
     * Stub method for updating records
     *
     * @param updateString is a String of the user input
     */
    private static void parseUpdate(String updateString) {
//        System.out.println("STUB: This is the dropTable method");
        if (checkUpdate(updateString)) {
        	ArrayList<String> tokens = cleanCommand(updateString);
        	if(FileHandler.findTable(tokens.get(1)))
        	{
        		System.out.println("\tParsing the string:\"" + updateString + "\"");
        		
        		//Get update column name and value and condition statment
        		ArrayList<String> updateArray = getUpdateColumns(tokens);
        		ArrayList<String> con = getConditionStatement(tokens);

        		//TEST:See column names and values
    			//System.out.println("Column: " + updateArray[0] + "\tValue: " + updateArray[2]);
        		
        		System.out.println("Updating columns" + "[" + updateArray.get(0) + " " + updateArray.get(1) + " " + updateArray.get(2) + "]...");
        		
    			System.out.println("with condition [" + con.get(0) + " " + con.get(1) + " " + con.get(2) + "]...");
        		
        		//TODO: Finish update implementation
        		
        	}
        }
    }

    /**
     * Stub method for dropping tables
     *
     * @param deleteString is a String of the user input
     */
    private static void parseDelete(String deleteString) {
//        System.out.println("STUB: This is the parseDelete method.");
        if (checkDelete(deleteString)) {
        	ArrayList<String> tokens = cleanCommand(deleteString);
        	if(FileHandler.findTable(tokens.get(2)))
        	{
        		System.out.println("\tParsing the string:\"" + deleteString + "\"");
        		ArrayList<String> con = getConditionStatement(tokens);
        		
        		//TODO: Finish delete implementation
        		if(con.size() == 0)
        		{
        			System.out.println("Deleting all records...");
        		}
        		else
        		{
        			System.out.println("Deleting records of condition [" + con.get(0) + " " + con.get(1) + " " + con.get(2) + "]...");
        		}
        	}
        }
    }


    /**
     * Stub method for creating new tables
     *
     * @param createTableString is a String of the user input
     * @throws IOException 
     */
    private static void parseCreateTable(String createTableString) throws IOException {

        //System.out.println("STUB: Calling your method to create a table");
        //System.out.println("Parsing the string:\"" + createTableString + "\"");

        if (!checkCreateTable(createTableString)) {
            return;
        } else {
            //TEST: See a success message. create table tablename ( rowid int  primary key , ab text);
            ArrayList<String> tokens = cleanCommand(createTableString);
            /** example create table table_name .....
             * so the token at second position is table name */
//            if (!FileHandler.createTable(tokens.get(2))) {
//                System.out.println("OOPS! Table " + tokens.get(2) + " already exists");
//            } else {
            	ArrayList<DataType> colTypes = getColTypes(tokens);
                String tablename = tokens.get(2);
                
                
                TableTree tableTree = new TableTree(tablename, colTypes);
                
                //Create table trees objects for the metadata
				TableTree metaDataTables = new TableTree("davisbase_tables", DatabaseType.CATALOG);
				TableTree metaDataColumns = new TableTree("davisbase_columns", DatabaseType.CATALOG);
				
				
				//TODO: Finish create table implementation.
				/*  Code to insert a row in the davisbase_tables table
				 *  i.e. database catalog meta-data
				 */
				
				//Insert table row into davisbase_tables.tbl
				ArrayList<DataType> tableType = new ArrayList<DataType>();
					tableType.add(DataType.TEXT_TYPE_CODE);
				ArrayList<String> tableName = new ArrayList<String>();
					tableName.add(tokens.get(2));
					//TODO: Getting a "java.lang.IllegalStateException: Tree can't have null root" exception here
//				metaDataTables.insert(tableType, tableName);
				
				
				

				/*  Code to insert rows in the davisbase_columns table
				 *  for each column in the new table
				 *  i.e. database catalog meta-data
				 */
				
              //Insert Column rows into davisbase_columns.tbl
               
				
				ArrayList<DataType> columnDataTypeArray = getColumnArray(colTypes);
				ArrayList<String> columnNames = getColumnNamesFromCreateQuery(tokens);
				ArrayList<String> ordinalPositionArray = getOrdinalPositionArray(colTypes);
				ArrayList<String> isColumnNullableArray = getIsColumnsNullableFromCreateQuery(tokens);
				
				for(int i = 0; i < columnNames.size(); i++)
				{
					ArrayList<String> columns = new ArrayList<String>();
					columns.add(tokens.get(2)); 
					columns.add(columnNames.get(i)); 
					columns.add(DataType.getDataTypeString(columnDataTypeArray.get(i))); 
					columns.add(ordinalPositionArray.toString());
					columns.add(isColumnNullableArray.get(i));
					ArrayList <DataType> dt = new ArrayList<DataType>();
					dt.add(DataType.TEXT_TYPE_CODE);
					dt.add(DataType.TEXT_TYPE_CODE);
					dt.add(DataType.TEXT_TYPE_CODE);
					dt.add(DataType.TINY_INT_TYPE_CODE);
					dt.add(DataType.TEXT_TYPE_CODE);
						
					//TODO: Getting a "java.lang.IllegalStateException: Tree can't have null root" exception here
//					metaDataColumns.insert(dt, columns);
					
				}
//                System.out.println(columnNameTypeMap);
                //System.out.println("SUCCESS! Creating table");
            //}
        }


        
    }

    /**
     * @param createIndexString string
     */
    private static void parseCreateIndex(String createIndexString) {
//        System.out.println("STUB: This is the createIndexQuery method");
        if (checkCreateIndex(createIndexString)) {
        	ArrayList<String> tokens = cleanCommand(createIndexString);
        	if(FileHandler.findTable(getTableNameFromCreateIndex(tokens)))
        	{
        		System.out.println("\tParsing the string:\"" + createIndexString + "\"");
        		
        		String indexName = getIndexNameFromCreateIndex(tokens);
        		String isUnique = isUniqueIndex(tokens);
        		ArrayList<String> columns = getColumnsFromCreateIndex(tokens);
        		
        		//TODO: Finish create index implementation
        		
        	}
        }
    }

    /**
     * Stub method for inserting a row into an existing table.
     *
     * @param insertString is a String of the user input
     */
    private static void parseInsert(String insertString) {
        //System.out.println("STUB: This is the dropTable method.");
        if (checkInsert(insertString)) {
        	ArrayList<String> tokens = cleanCommand(insertString);
        	if(FileHandler.findTable(getTableNameFromInsert(tokens)))
        	{
        		System.out.println("\tParsing the string:\"" + insertString + "\"");
        		
        		
        		//ArrayList<DataType> columns = getColumnsFromInsert(tokens);
        		ArrayList<String> values = getValuesFromInsert(tokens);
        		
        		//TODO: Finish insert implementation. Getting "java.lang.NullPointerException" here
//        		TableTree tb;
//				try {
//					tb = new TableTree(getTableNameFromInsert(tokens), DatabaseType.USER);
//					tb.insert(tb.getTreeConfig().getColTypes(), values);
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
        		
        		
        	}
        }

    }

    /**
     * This function checks if command is exactly "show table;" and returns true if so.
     * Print error and return false if otherwise.
     *
     * @param showTableString string
     * @return boolean
     */
    private static boolean checkShowTable(String showTableString) {
        ArrayList<String> tokens = cleanCommand(showTableString);
        if (tokens.size() == 2 && tokens.get(1).equals("tables")) {
            return true;
        } else {
            System.out.println("SYNTAX ERROR. Did you mean \"show tables\"?");
            return false;
        }

    }

    /**
     * This function checks the syntax of select query
     *
     * @param queryString string
     * @return true/false
     */
    private static boolean checkQuery(String queryString) {
        ArrayList<String> tokens = cleanCommand(queryString);
        /*
         * state determines the structure of the statement
         * states are:
         * S = select column list  portion
         * F = from table portion
         * W = where condition portion
         * E = end
         */
        char state = 'S';
        int itr = 1;

        if (tokens.size() < 4) {
            state = '0';
        }

        while (state != 'E') {
            switch (state) {
                case 'S':
                    if (itr == 1 && !(nameCheck(tokens.get(itr)) || tokens.get(itr).equals("*"))) {
                        //System.out.println(itr);
                        state = '0';
                    } else if (tokens.get(itr).equals("from")) {
                        //System.out.println(itr);
                        state = 'F';
                    } else if (itr > 1) {
                        if (itr % 2 == 0) {
                            if (!tokens.get(itr).equals(",")) {
                                //System.out.println(itr);
                                state = '0';
                            }
                        } else {
                            if (!nameCheck(tokens.get(itr))) {
                                //System.out.println(itr);
                                state = '0';
                            }
                        }
                    }
                    break;
                case 'F':
                    if (!nameCheck(tokens.get(itr))) {
                        state = '0';
                    } else {
                        state = 'W';
                    }
                    break;
                case 'W':
                    if (itr >= tokens.size()) {
                        state = 'E';
                    } else {
                        if (checkCondition(tokens.subList(itr, tokens.size()))) {
                            state = 'E';
                        } else {
                            state = '0';
                        }
                    }
                    break;
                default:
                    System.out.println("SYNTAX ERROR. Select Statement is incomplete. "
                            + " Format is \\\"SELECT 'column_name' FROM 'table_name' WHERE 'condition'\"");
                    return false;
            }

            itr++;
        }

        return true;
    }

    /**
     * This function checks the syntax of drop table statement
     *
     * @param dropTableString string
     * @return true/false
     */
    private static boolean checkDropTable(String dropTableString) {
        ArrayList<String> tokens = cleanCommand(dropTableString);
        if (tokens.size() < 3) {
            System.out.println("SYNTAX ERROR. Drop statment is incomplete. Format is \"DROP TABLE 'table_name'\"");
            return false;
        } else if (!tokens.get(1).equals("table")) {
            System.out.println("SYNTAX ERROR. Drop statment is incorrect. Format is \"DROP TABLE 'table_name'\"");
            return false;
        } //Invalid table name.
        else if (!nameCheck(tokens.get(2))) {
            System.out.println("SYNTAX ERROR. Table name is invalid.");
            return false;
        } //Syntax is correct.
        else {
            return true;
        }
    }

    /**
     * This function checks the syntax of update statement.
     *
     * @param updateString string
     * @return true/false
     */
    private static boolean checkUpdate(String updateString) {
        ArrayList<String> tokens = cleanCommand(updateString);

        //Check size
        if (tokens.size() < 6) {
            System.out.println("SYNTAX ERROR. Update statement is incorrect. "
                    + " Format is \"UPDATE 'table_name' SET 'column_name' = value [WHERE 'condition']\"");
            return false;
        }

        //Check keywords
        if (!(nameCheck(tokens.get(1)) && tokens.get(2).equals("set"))) {
            System.out.println("SYNTAX ERROR. Update statement is incorrect. "
                    + " Format is \"UPDATE 'table_name' SET 'column_name' = value [WHERE 'condition']\"");
            return false;
        }

        //Check Set Portion
        if (!(nameCheck(tokens.get(3)) && relationalOp(tokens.get(4)).equals("=") && (nameCheck(tokens.get(5)) || tokens.get(5).chars().allMatch(Character::isDigit)))) {
            System.out.println("SYNTAX ERROR. Update statement is incorrect. "
                    + " Format is \"UPDATE 'table_name' SET 'column_name' = value [WHERE 'condition']\"");
            return false;
        }

        if (tokens.size() > 6) {
            return checkCondition(tokens.subList(6, tokens.size()));
        } else {
            return true;
        }
    }

    /**
     * This function checks the syntax of delete statement
     *
     * @param deleteString str
     * @return true/false
     */
    private static boolean checkDelete(String deleteString) {
        ArrayList<String> tokens = cleanCommand(deleteString);

        //Check size
        if (tokens.size() < 3) {
            System.out.println("SYNTAX ERROR. Delete statement is incorrect. "
                    + " Format is \"DELETE FROM 'table_name' [WHERE 'condition']\"");
            return false;
        }

        //Check keywords, and table name
        if (!(tokens.get(1).equals("from") && nameCheck(tokens.get(2)))) {
            System.out.println("SYNTAX ERROR. Delete statement is incorrect. "
                    + " Format is \"DELETE FROM 'table_name' [WHERE 'condition']\"");
            return false;
        }

        //Check condition
        if (tokens.size() > 3) {
            return checkCondition(tokens.subList(3, tokens.size()));
        }

        return true;
    }

    /**
     * This function checks the syntax of insert statement.
     *
     * @param insertString string
     * @return true/false
     */
    private static boolean checkInsert(String insertString) {
        ArrayList<String> tokens = cleanCommand(insertString);
        /*
         * state determines the structure of the statement
         * states are:
         * I = insert into table_name portion
         * C = column list portion
         * V = values portion
         * A = value list portion
         * E = end
         */
        char state = 'I';
        int args = 0;

        for (int itr = 1; itr < tokens.size(); itr++) {
            //TEST: see iteration, string, and state.
            //System.out.println(itr + ": " + tokens.get(itr) + "; state: " + state);

            switch (state) {
                case 'I':
                    if (itr == 1 && !tokens.get(itr).equals("into")) {
                        state = '0';
                    } else if (itr == 2 && !nameCheck(tokens.get(itr))) {
                        state = '0';
                    } else if (itr > 2 && !tokens.get(itr).equals("(")) {
                        state = '0';
                    } else if (itr > 2) {
                        state = 'C';
                    }
                    break;
                case 'C':
                    if (itr == 4 && !nameCheck(tokens.get(itr))) {
                        state = '0';
                    } else {
                        if (itr % 2 == 0) {
                            if (!nameCheck(tokens.get(itr))) {
                                state = '0';
                            }
                            args++;
                        } else {
                            if (!tokens.get(itr).equals(",") && !tokens.get(itr).equals(")")) {
                                state = '0';
                            } else if (tokens.get(itr).equals(")")) {
                                state = 'V';
                            }

                        }
                    }
                    break;
                case 'V':
                    if (itr % 2 == 0 && !tokens.get(itr).equals("values")) {
                        state = '0';
                    } else if (itr % 2 == 1 && !tokens.get(itr).equals("(")) {
                        state = '0';
                    } else if (itr % 2 == 1) {
                        state = 'A';
                    }
                    break;
                case 'A':
                    if (itr % 2 == 0) {
                        if (!(nameCheck(tokens.get(itr)) || tokens.get(itr).chars().allMatch(Character::isDigit)) || args < 0) {
                            state = '0';
                        } else {
                            args--;
                        }
                    } else {
                        if (!tokens.get(itr).equals(",") && !tokens.get(itr).equals(")")) {
                            state = '0';
                        } else if (tokens.get(itr).equals(")") && args != 0) {
                            state = '0';
                        } else if (tokens.get(itr).equals(")")) {
                            state = 'E';
                        }

                    }
                    break;
                default:
                    System.out.println("SYNTAX ERROR. Insert statement is incorrect. "
                            + " Format is \"INSERT INTO 'columns list' VALUES 'values list'\"");
                    return false;
            }


        }

        if (state != 'E') {
            System.out.println("SYNTAX ERROR. Insert statement is not structure properly. "
                    + " Format is \"INSERT INTO 'columns list' VALUES 'values list'\"");
            return false;
        } else {
            return true;
        }
    }

    /**
     * This function checks the syntax of create table statement.
     *
     * @param createTableString string
     * @return true/false
     */
    private static boolean checkCreateTable(String createTableString) {
        //First column statement checks to if if the first column is the create primary
        boolean firstColumn = true;
        ArrayList<String> tokens = cleanCommand(createTableString);
        /*
         * state determines the structure of the statement
         * states are:
         * B = Create table name portion
         * C = column name portion
         * D = data type portion
         * N = not null portion
         * E = end
         */
        char state = 'B';
        /*
         * notState contains that not null state:
         * 0: not is absent.
         * 1: not is present but there is no null
         * 2: not null is present
         */
        int notState = 0;

        for (int itr = 1; itr < tokens.size(); itr++) {
            //TEST: see iteration, string, and state.
            //System.out.println(itr + ": " + tokens.get(itr) + "; state: " + state);

            switch (state) {
                case 'B':
                    if (itr == 1 && !(tokens.get(itr).equals("table")))
                        state = '0';
                    else if (itr == 2 && !nameCheck(tokens.get(itr)))
                        state = '0';
                    else if (itr == 3 && tokens.get(itr).equals("("))
                        state = 'C';
                    break;
                case 'C':
                    if (firstColumn && (tokens.get(itr).equals("row_id") || tokens.get(itr).equals("rowid")))
                        state = 'D';
                    else if (!firstColumn && nameCheck(tokens.get(itr)))
                        state = 'D';
                    else
                        state = '0';
                    break;
                case 'D':
                    if (firstColumn && dataType(tokens.get(itr)).equals("int"))
                        state = 'N';
                    else if (!firstColumn && !dataType(tokens.get(itr)).isEmpty())
                        state = 'N';
                    else
                        state = '0';
                    break;
                case 'N':
                    if (firstColumn) {
                        if (tokens.get(itr).equals("primary") && tokens.get(itr + 1).equals("key")) {
                            firstColumn = false;
                            itr++;
                        } else {
                            state = '0';
                        }
                    } else if (tokens.get(itr).equals("not") && notState == 0)
                        notState++;
                    else if (tokens.get(itr).equals("null") && notState == 1)
                        notState++;
                    else if (tokens.get(itr).equals("null"))
                        state = '0';
                    else if (tokens.get(itr).equals(",")) {
                        state = 'C';
                        notState = 0; //Reset not null state.
                    } else if (tokens.get(itr).equals(")"))
                        state = 'E';


                    break;
                default:
                    System.out.println("SYNTAX ERROR. Create Table statement is not structure properly. "
                            + " Format is \"CREATE TABLE ' table_name' (data data_type [NOT NULL], . . . , data data_type [NOT NULL])\"");
                    return false;
            }
        }

        if (state != 'E') {
            System.out.println("SYNTAX ERROR. Create Table statement is not structure properly. "
                    + " Format is \"CREATE TABLE ' table_name' (data data_type [NOT NULL], . . . , data data_type [NOT NULL])\"");
            return false;
        } else {
            return true;
        }
    }

    /**
     * This function checks the syntax of create index statement.
     * Returns true if syntax is correct or false if incorrect.
     *
     * @param createIndexString string
     * @return true/false
     */
    private static boolean checkCreateIndex(String createIndexString) {
        ArrayList<String> tokens = cleanCommand(createIndexString);
        /*
         * state determines the structure of the statement
         * states are:
         * C = Create portion
         * I = Index name portion
         * T = on table_name portion
         * L = Column list portion
         * E = end
         */
        char state = 'C';
        int unique = 0;
        if (tokens.get(1).equals("unique")) {
            unique = 1;
        }

        for (int itr = 1; itr < tokens.size(); itr++) {
            //TEST: see iteration, string, and state.
           // System.out.println(itr + ": " + tokens.get(itr) + "; state: " + state);

            switch (state) {
                case 'C':
                    if (itr == 1 && !(tokens.get(itr).equals("unique") || tokens.get(itr).equals("index"))) {
                        state = '0';
                    } else if (tokens.get(itr).equals("index")) {
                        state = 'I';
                    }
                    break;
                case 'I':
                    if (nameCheck(tokens.get(itr))) {
                        state = 'T';
                    } else {
                        state = '0';
                    }
                    break;
                case 'T':
                    if (tokens.get(itr).equals("on") && nameCheck(tokens.get(itr + 1))) {
                        itr++;
                    } else if (tokens.get(itr).equals("(")) {
                        state = 'L';
                    }

                    break;
                case 'L':
                    if ((itr - unique) % 2 == 0) {
                        if (!nameCheck(tokens.get(itr))) {
                            state = '0';
                        }
                    } else {
                        if (!tokens.get(itr).equals(",") && !tokens.get(itr).equals(")")) {
                            state = '0';
                        } else if (tokens.get(itr).equals(")")) {
                            state = 'E';
                        }
                    }
                    break;
                default:
                    System.out.println("SYNTAX ERROR. Create Index statement is incorrect. "
                            + " Format is \"CREATE [UNIQUE] INDEX 'index_name' ON 'table_name' 'column list'\"");
                    return false;
            }
        }

        //If state is not E, then the structure of the statement is incorrect.
        if (state != 'E') {
            System.out.println("SYNTAX ERROR. Create Index statement is not structure properly. "
                    + " Format is \"CREATE [UNIQUE] INDEX 'index_name' ON 'table_name' 'column list'\"");
            return false;
        } else {
            return true;
        }
    }

    /**
     * This function accepts a List of tokens that is the sublist of the arrayList<String>
     * The format of where condition is:
     * WHERE 'attribute' 'relational' 'value'
     * This function returns true if string fufills that structure.
     * Returns false if not.
     *
     * @param tokens par
     * @return true/false
     */
    private static boolean checkCondition(List<String> tokens) {
        //TEST: See ArrayList.
//		for(int i = 0; i < tokens.size();i++)
//		{
//			System.out.println(tokens.get(i));
//		}
//		System.out.println(tokens.size());
        //return true;

        if (tokens.size() == 4 && tokens.get(0).equals("where") && nameCheck(tokens.get(1))
                && !relationalOp(tokens.get(2)).isEmpty()
                && (nameCheck(tokens.get(3)) || tokens.get(3).chars().allMatch(Character::isDigit))) {
            return true;
        } else {
            System.out.println("SYNTAX ERROR. Condition is incorrect. "
                    + " Format is \"WHERE 'attribute' 'relational' 'value'\"");
            return false;
        }
    }

    /**
     * This function accepts a string and returns true if string is an acceptable name.
     * Returns false if not.
     *
     * @param name string
     * @return true/false
     */
    private static boolean nameCheck(String name) {
        //String must not start with the digit or a character that is not a letter except for _, @, and #.
        if (Character.isDigit(name.charAt(0)) || (!Character.isLetter(name.charAt(0))
                && !(name.charAt(0) == '_' || name.charAt(0) == '@' || name.charAt(0) == '#'))) {
            return false;
        }

        //String must not contain a period.
        for (int i = 0; i < name.length(); i++) {
            if (!Character.isLetterOrDigit(name.charAt(i)) && name.charAt(i) == '.') {
                return false;
            }
        }

        return true;
    }

    /**
     * This function accepts a string and returns the argument if argument is a data type.
     * Returns empty if not
     *
     * @param type data type
     * @return string
     */
    private static String dataType(String type) {
        switch (type) {
            case "int":
            case "tinyint":
            case "smallint":
            case "bigint":
            case "real":
            case "double":
            case "datetime":
            case "date":
            case "text":
                return type;
            default:
                return "";
        }
    }

    /**
     * This function accepts a string and returns the argument if argument is a relational operation.
     * Returns empty if not
     *
     * @param r string
     * @return string
     */
    private static String relationalOp(String r) {
        switch (r) {
            case "=":
            case ">":
            case ">=":
            case "<":
            case "<=":
            case "!=":
            case "true":
            case "false":
                return r;
            default:
                return "";
        }
    }


    /**
     * This function accepts a string input and parses the string into an ArrayList of strings, command.
     * This function will separate any relational operation (except for true/false), commas, and parenthesis
     * of any strings that contain those characters.
     * Function will return command.
     *
     * @param input string
     * @return list
     */
    private static ArrayList<String> cleanCommand(String input) {
        //Separate the string into strings slit by white-space
        ArrayList<String> temp = new ArrayList<>(Arrays.asList(input.split(" ")));
        //This is the return arrayList
        ArrayList<String> command = new ArrayList<>();

        //Parse each string in the temp arrayList
        for (String aTemp : temp) {
            //Stores the stored characters
            StringBuilder str = new StringBuilder();

            //Parse each character of the itertive string.
            for (int j = 0; j < aTemp.length(); j++) {
                //Stores the current and next character
                String inp = "";
                String inp2 = "";
                inp += aTemp.charAt(j);

                //If character is a relational op, store the relational into the command
                if (!relationalOp(inp).isEmpty()) {
                    //If characters are stored into the string, add the string first.
                    if (str.length() > 0) {
                        command.add(str.toString());
                        str = new StringBuilder();
                    }

                    //If the relational is two characters, store both characters else store one character
                    if (j + 1 < aTemp.length()) {
                        inp2 = inp;
                        inp2 += aTemp.charAt(j + 1);

                        if (!relationalOp(inp2).isEmpty())//Relational is two characters
                        {
                            command.add(inp2);
                            j++;//Move pointer since two characters are stored in one iteration
                        } else//Relational is one character
                        {
                            command.add(inp);
                        }
                    } else//Relational is one character
                    {
                        command.add(inp);
                    }
                } else if (aTemp.charAt(j) == ',')//Character is comma
                {
                    //If characters are stored into the string, add the string first.
                    if (str.length() > 0) {
                        command.add(str.toString());
                        str = new StringBuilder();
                    }

                    command.add(",");
                } else if (aTemp.charAt(j) == '(') {
                    //If characters are stored into the string, add the string first.
                    if (str.length() > 0) {
                        command.add(str.toString());
                        str = new StringBuilder();
                    }

                    command.add("(");
                } else if (aTemp.charAt(j) == ')') {
                    //If characters are stored into the string, add the string first.
                    if (str.length() > 0) {
                        command.add(str.toString());
                        str = new StringBuilder();
                    }

                    command.add(")");
                } else//Add the character into the string
                {
                    str.append(aTemp.charAt(j));
                }

            }

            //At the end of the outer loop, if characters are stored into the string, add the string to command.
            if (str.length() > 0) {
                command.add(str.toString());
                str = new StringBuilder();
            }
        }

        //TEST: See command
//		for(int i = 0; i < command.size();i++)
//		{
//			System.out.println(command.get(i));
//		}

        return command;
    }

    /**
     * Get column name to column type map from create query
     * @param tokens query tokens
     * @return map of column name to column type
     */
    private static HashMap<String, String> getColumnNameTypeMap(ArrayList<String> tokens){
        // using linked map to preserve order of column names
        LinkedHashMap<String, String> map = new LinkedHashMap<>();

        // start from 4 as column names start from index 4 in tokens list
        for(int i = 4; i < tokens.size() - 2; i++){
            // add token which is after comma as column name and after that it's column type
            if(tokens.get(i).contains(",")){
                map.put(tokens.get(i+1), tokens.get(i+2));
            }
            else if( i == 4){
                map.put(tokens.get(i), tokens.get(i+1));
            }
        }
        return map;
    }

    /**
     * Get column names create query
     * @param tokens query tokens
     * @return array of column names
     */
    private static ArrayList<String> getColumnNamesFromCreateQuery(ArrayList<String> tokens){
        ArrayList<String> columns = new ArrayList<>();
        // start from 4 as column names start from index 4 in tokens list
        for(int i = 4; i < tokens.size() - 2; i++){
            // add token which is after comma as column name and after that it's column type
            if(tokens.get(i).contains(",")){
                columns.add(tokens.get(i+1));
            }
            else if( i == 4){
                columns.add(tokens.get(i));
            }
        }
        //Test see Array
//        for(int i = 0; i < columns.size();i++)
//        {
//        	System.out.println(columns.get(i));
//        }
        return columns;
    }

    /**
     * Get column datatypes from create query
     * @param tokens query tokens
     * @return DataType array
     */
    private static ArrayList<DataType> getColTypes(ArrayList<String> tokens){

        ArrayList<DataType> dataTypeArrayList = new ArrayList<>();

        // start from 4 as column names start from index 4 in tokens list
        for(int i = 4; i < tokens.size() - 2; i++){
            // add token which is after comma as column name and after that it's column type
            if(tokens.get(i).contains(",")){
                dataTypeArrayList.add(DataType.getDataTypeCodeFromString(tokens.get(i+2)));
            }
            else if( i == 4){
                dataTypeArrayList.add(DataType.getDataTypeCodeFromString(tokens.get(i+1)));
            }
        }
        //Test see Array
//        for(int i = 0; i < dataTypeArrayList.size();i++)
//        {
//        	System.out.println(dataTypeArrayList.get(i));
//        }
        
        return dataTypeArrayList;
    }
    
    /**
     * Create an array of column datatypes for adding into davisbase_columns
     * @param dataType array
     * @return array of DataType
     */
    private static ArrayList<DataType> getColumnArray(ArrayList<DataType> colTypes){
    	ArrayList<DataType>columnArrayList = new ArrayList<>();
    	
    	for(int i = 0; i < colTypes.size(); i++)
    	{
    		columnArrayList.add(DataType.TEXT_TYPE_CODE);
    	}
    	
    	 return columnArrayList;
    }
    
    /**
     * Create an array of ordinal position for adding into davisbase_columns
     * @param dataType array
     * @return array of integers
     */
    private static ArrayList<String> getOrdinalPositionArray(ArrayList<DataType> data){
    	ArrayList<String> ordArray = new ArrayList<>();
    	
    	for(int i = 0; i < data.size(); i++)
    	{
    		ordArray.add(Integer.toString(i));
    		
    		//Test see Array;
    		//System.out.println(ordArray[i]);
    	}
    	
    	
    	
    	return ordArray;
    }
    
    /**
     * Method to get String array of columns which are nullable or not from create query
     * @param tokens
     * @return String[] of "true"/"false"
     */
    private static ArrayList<String> getIsColumnsNullableFromCreateQuery(ArrayList<String> tokens){
        ArrayList<String> columns = new ArrayList<>();
        // start from 4 as column names start from index 4 in tokens list
        StringBuilder s = new StringBuilder();
        for(int i = 4; i < tokens.size(); i++){
            if(tokens.get(i).equals(",") || tokens.get(i).equals(")")){
                if(s.toString().contains("not null") || s.toString().contains("primary key")){
                	//TEST: See what attribute is not nullable.
                	//System.out.println(s.toString() + " is not nullable.");
                    columns.add("false");
                }
                else{
                	
                    columns.add("true");
                }
                s.setLength(0);
            }
            else {
                s.append(tokens.get(i));
                s.append(" ");
            }

        }
        return columns;
    }

    /**
     * Method to Print a table using a string array for column names and a 2D string array data
     * @param colName, data
     */
    private static void printTable(String[] colName, String[][] data)
    {
    	//This int values determine how many lines needs to be printed for the dividing line
    	//TODO: Look at algorithm to fix printing
    	int colNameArraySize = colName.length;
    	int tabSize = 0;
    	int largestString = 0;
    	
    	for(int i = 0; i < colName.length; i++)
    	{
    		if(largestString < colName[i].length())
    			largestString = colName[i].length();
    	}
    	
    	for(int i = 0; i < data.length; i++)
    	{
    		for(int j = 0; j < data[i].length;j++)
    		{
    			if(largestString < data[i][j].length())
        			largestString = data[i][j].length();
    		}
    	}
    	
    	
    	
    	//Print out all Column Names
    	for(int i = 0; i < colName.length; i++)
    	{
    		String tabs = "";
    		tabSize = (int) Math.ceil((largestString-colName[i].length())/4)+1;
    		
    		for(int k = 0;k< tabSize;k++)
        		tabs += "\t";
    		
    		System.out.print(colName[i] + tabs);
    		colNameArraySize += largestString;//Add extra size for tab
    		
    		//Print a newLine at the last column
    		if(i + 1 == colName.length)
    			System.out.print("\n");
    	}
    	
    	//Print a dividing line
    	for(int i = colNameArraySize; i > 0; i--)
    		System.out.print("-");
    	System.out.print("\n");
    	
    	//Print data
    	for(int i = 0; i < data.length; i++)
    	{
    		for(int j = 0; j < data[i].length;j++)
    		{
    			String tabs = "";
        		tabSize = (int) Math.ceil((largestString-data[i][j].length())/4)+1;
        		
        		for(int k = 0;k< tabSize;k++)
            		tabs += "\t";
    			
    			System.out.print(data[i][j] + tabs);
    		}
    		System.out.print("\n");
    	}
    }
    
    /**
     * Get a table name from the select query
     * @param ArrayList<String> tokens
     * @return String
     */
    private static String getTableNameFromSelect(ArrayList<String> tokens)
    {
    	String tblName ="";
    	for(int i=0; i < tokens.size(); i++)
    	{
    		if(tokens.get(i).equals("from"))
    		{
    			tblName = tokens.get(i+1);
    			
    			//TEST: see return value
    			//System.out.println("Table name: " + tblName);
    			
    			return tblName;
    		}
    	}
    	
    	return tblName;
    }
    
    private static ArrayList<String> getColumnsFromSelect(ArrayList<String> tokens)
    {
    	ArrayList<String> colNames = new ArrayList<>();
    	int index = 0;
    	while(index<tokens.size() && !tokens.get(index).equals("from"))
    	{
    		
    		if(!tokens.get(index).equals("select") && !tokens.get(index).equals(","))
    		{
    			colNames.add(tokens.get(index));
    		}
    		
    		
    		
    		index++;
    	}
    	
    	//Return an empty string if there is a * and other column names within the select statement
    	if(colNames.contains("*") && colNames.size() > 1)
		{
			
			colNames.clear();
			return colNames;
		}
    	
    	//TEST: See return variable.
//    	for(int i=0;i<colNames.size();i++)
//    		System.out.println(colNames.get(i));
    	
    	return colNames;
    }
    
    /**
     * Create an array for storing the condition portion of the all SQL statements
     * @param ArrayList<String> tokens
     * @return String array
     */
    private static ArrayList<String> getConditionStatement(ArrayList<String> tokens)
    {
    	ArrayList<String> conTokens = new ArrayList<>();
    	int index = 0;
    	boolean whereReached = false;
    	
    	while(index<tokens.size())
    	{
    		if(whereReached)
    		{
    			conTokens.add(tokens.get(index));
    		}
    		
    		if(tokens.get(index).equals("where"))
    			whereReached = true;
    		
    		index++;
    	}
    	
    	//TEST: See return variable.
//    	for(int i=0;i<conTokens.size();i++)
//    		System.out.println(conTokens.get(i));
    	
    	 return conTokens;
    }
    
    /**
     * Create an array for storing the column names of the update statement
     * @param ArrayList<String> tokens
     * @return String array
     */
    private static ArrayList<String> getUpdateColumns(ArrayList<String> tokens)
    {
    	ArrayList<String> upTokens = new ArrayList<>();
    	
    	int index = 0;
    	boolean record = false;//This boolean determines if upTokens adds elements to the arrayList
    	
    	while(index<tokens.size())
    	{
    		
    		
    		//If where token is reach, stop adding elements
    		if(record && tokens.get(index).equals("where"))
    		{
    			record = false;
    		}
    		else if(record && !tokens.get(index).equals(","))//Add all elements except for commas
    		{
    			upTokens.add(tokens.get(index));
    		}
    		
    		if(tokens.get(index).equals("set"))//when set token is reached, start adding elements
    			record = true;
    		
    		index++;
    	}
    	
    	//TEST: See return variable.
//    	for(int i=0;i<upTokens.size();i++)
//    		System.out.println(upTokens.get(i));
    	
    	return upTokens;
    }
    
    /**
     * Get table name from an insert statement
     * @param ArrayList<String> tokens
     * @return String
     */
    private static String getTableNameFromInsert(ArrayList<String> tokens)
    {
    	String tblName ="";
    	for(int i=0; i < tokens.size(); i++)
    	{
    		if(tokens.get(i).equals("into"))
    		{
    			tblName = tokens.get(i+1);
    			
    			//TEST: see return value
    			//System.out.println("Table name: " + tblName);
    			
    			return tblName;
    		}
    	}
    	
    	return tblName;
    }
    
    /**
     * Create an array for storing the column names for insert statement
     * @param ArrayList<String> tokens
     * @return String array
     */
    private static ArrayList<String> getColumnsFromInsert(ArrayList<String> tokens)
    {
    	ArrayList<String> colNames = new ArrayList<>();
    	int index = 0;
    	boolean record = false;
    	
    	while(index<tokens.size() && !tokens.get(index).equals("values"))
    	{
    		
    		if(record && !tokens.get(index).equals("(") && !tokens.get(index).equals(",") && !tokens.get(index).equals(")"))
    		{
    			colNames.add(tokens.get(index));
    		}
    		
    		if(tokens.get(index).equals("("))//Start adding elements after table name token
    			record = true;
    		
    		index++;
    	}
    	
    	//TEST: See return variable.
//    	for(int i=0;i<colNames.size();i++)
//    		System.out.println(colNames.get(i));
    	
    	return colNames;
    }
    
    /**
     * Create an array for storing the values for insert statement
     * @param ArrayList<String> tokens
     * @return String array
     */
    private static ArrayList<String> getValuesFromInsert(ArrayList<String> tokens)
    {
    	ArrayList<String> values = new ArrayList<String>();
    	int index = 0;
    	boolean record = false;
    	
    	while(index<tokens.size())
    	{
    		
    		if(record && !tokens.get(index).equals("(") && !tokens.get(index).equals(",") && !tokens.get(index).equals(")"))
    		{
    			values.add((tokens.get(index)));
    		}
    		
    		if(tokens.get(index).equals("values"))//Start adding elements after values token
    			record = true;
    		
    		index++;
    	}
    	
    	//TEST: See return variable.
//    	for(int i=0;i<values.size();i++)
//    		System.out.println(values.get(i));
    	
    	return values;
    }
    
    /**
     * Finds an unique token in create index statement.
     * Returns String true if yes; otherwise returns String false
     * @param ArrayList<String> tokens
     * @return String
     */
    private static String isUniqueIndex(ArrayList<String> tokens)
    {
    	for(int i=0; i < tokens.size() && !tokens.get(i).equals("on"); i++)
    	{
    		if(tokens.get(i).equals("unique"))
    		{
    			//TEST: see return value
    			//System.out.println("Index is unique");
    			
    			return "true";
    		}
    	}
    	
    	//TEST: see return value
		//System.out.println("Index is not unique");
    	return "false";
    }
    
    /**
     * Get table name from an create index statement
     * @param ArrayList<String> tokens
     * @return String
     */
    private static String getTableNameFromCreateIndex(ArrayList<String> tokens)
    {
    	String tblName ="";
    	for(int i=0; i < tokens.size(); i++)
    	{
    		if(tokens.get(i).equals("on"))
    		{
    			tblName = tokens.get(i+1);
    			
    			//TEST: see return value
    			//System.out.println("Table name: " + tblName);
    			
    			return tblName;
    		}
    	}
    	
    	return tblName;
    }
    
    /**
     * Get index name from an create index statement
     * @param ArrayList<String> tokens
     * @return String
     */
    private static String getIndexNameFromCreateIndex(ArrayList<String> tokens)
    {
    	String indexName ="";
    	for(int i=0; i < tokens.size(); i++)
    	{
    		if(tokens.get(i).equals("index"))
    		{
    			indexName = tokens.get(i+1);
    			
    			//TEST: see return value
    			System.out.println("Table name: " + indexName);
    			
    			return indexName;
    		}
    	}
    	
    	return indexName;
    }
    
    /**
     * Create an array for storing the column names for create index statement
     * @param ArrayList<String> tokens
     * @return String array
     */
    private static ArrayList<String> getColumnsFromCreateIndex(ArrayList<String> tokens)
    {
    	ArrayList<String> colNames = new ArrayList<>();
    	int index = 0;
    	boolean record = false;
    	
    	while(index<tokens.size())
    	{
    		
    		if(record && !tokens.get(index).equals("(") && !tokens.get(index).equals(",") && !tokens.get(index).equals(")"))
    		{
    			colNames.add(tokens.get(index));
    		}
    		
    		if(tokens.get(index).equals("("))//Start adding elements after table name token
    			record = true;
    		
    		index++;
    	}
    	
    	//TEST: See return variable.
//    	for(int i=0;i<colNames.size();i++)
//    		System.out.println(colNames.get(i));
    	
    	return colNames;
    }
}
