package edu.utdallas.cs6360.davisbase;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *  @author Chris Irwin Davis
 *  @version 1.0
 *  <b>
 *  <p>This is an example of how to create an interactive prompt</p>
 *  <p>There is also some guidance to get started wiht read/write of
 *     binary data files using RandomAccessFile class</p>
 *  </b>
 *
 */

public class DavisBase {
	
	public static final String COPYRIGHT = "Â©2016 Chris Irwin Davis";
	public static final String VERSION = "v1.0b(example)";
	private static boolean isExit = false;
	/*
	 * Page size for all files is 512 bytes by default.
	 * You may choose to make it user modifiable
	 */
	static long pageSize = 512; 

	/**
	 *  The Scanner class is used to collect user commands from the prompt
	 *  There are many ways to do this. This is just one.
	 *
	 *  Each time the semicolon (;) delimiter is entered, the userCommand 
	 *  String is re-populated.
	 */
	private static Scanner scanner = new Scanner(System.in, "UTF-8").useDelimiter(";");
	
	/** ***********************************************************************
	 *  Main method
	 */
    public static void main(String[] args) {

		/* Display the welcome screen */
		splashScreen();

		/* Variable to collect user input from the prompt */
		String userCommand = "";

		while(!isExit) {
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
	 *  Display the splash screen
	 */
	private static void splashScreen() {
		System.out.println(line("-",80));
		// Display the string.
        System.out.println("Welcome to DavisBaseLite");
		System.out.println("DavisBaseLite Version " + getVersion());
		System.out.println(getCopyright());
		System.out.println("\nType \"help;\" to display supported commands.");
		System.out.println(line("-",80));
	}
	
	/**
	 * @param s The String to be repeated
	 * @param num The number of time to repeat String s.
	 * @return String A String object, which is the String s appended to itself num times.
	 */
	private static String line(String s,int num) {
		StringBuilder a = new StringBuilder();
		for(int i=0;i<num;i++) {
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
	 *  Help: Display supported commands
	 */
	private static void help() {
		System.out.println(line("*",80));
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
		System.out.println(line("*",80));
	}

	/** return the DavisBase version */
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
		
	private static void parseUserCommand (String userCommand) {
		
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
				if(commandTokens.size() > 1)
				{
					if(commandTokens.get(1).equals("index") || commandTokens.get(1).equals("unique"))
					{ parseCreateIndex(userCommand); }
					else { parseCreateTable(userCommand); }
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
	 *  Stub method for showing all tables
	 *  @param showTableString is a String of the user input
	 */
	private static void showTable(String showTableString) {
		System.out.println("STUB: This is the showTable method.");
		
		if(checkShowTable(showTableString))
		{
			System.out.println("SUCCESS! This will print all tables");
		}
		
	}
	
	/**
	 *  Stub method for dropping tables
	 *  @param dropTableString is a String of the user input
	 */
	private static void dropTable(String dropTableString) {
		System.out.println("STUB: This is the dropTable method.");
		if(checkDropTable(dropTableString))
		{
			System.out.println("\tParsing the string:\"" + dropTableString + "\"");
		}
	}
	
	/**
	 *  Stub method for executing queries
	 *  @param queryString is a String of the user input
	 */
	private static void parseQuery(String queryString) {
		System.out.println("STUB: This is the parseQuery method");
		if(checkQuery(queryString))
		{
			System.out.println("\tParsing the string:\"" + queryString + "\"");
		}
	}

	/**
	 *  Stub method for updating records
	 *  @param updateString is a String of the user input
	 */
	private static void parseUpdate(String updateString) {
		System.out.println("STUB: This is the dropTable method");
		if(checkUpdate(updateString))
		{
			System.out.println("Parsing the string:\"" + updateString + "\"");
		}
	}
	
	/**
	 *  Stub method for dropping tables
	 *  @param deleteString is a String of the user input
	 */
	private static void parseDelete(String deleteString) {
		System.out.println("STUB: This is the parseDelete method.");
		if(checkDelete(deleteString))
		{
			System.out.println("\tParsing the string:\"" + deleteString + "\"");
		}
	}

	
	/**
	 *  Stub method for creating new tables
	 *  @param createTableString is a String of the user input
	 */
	private static void parseCreateTable(String createTableString) {
		
		System.out.println("STUB: Calling your method to create a table");
		System.out.println("Parsing the string:\"" + createTableString + "\"");
		
		if(!checkCreateTable(createTableString))
		{
			return;
		}
		else
		{
			//TEST: See a success message.
			System.out.println("SUCCESS! Creating table");
		}

//		/* Define table file name */
//		String tableFileName = createTableTokens.get(2) + ".tbl";
//
//		/* YOUR CODE GOES HERE */
//		
//		/*  Code to create a .tbl file to contain table data */
//		try {
//			/*  Create RandomAccessFile tableFile in read-write mode.
//			 *  Note that this doesn't create the table file in the correct directory structure
//			 */
//			RandomAccessFile tableFile = new RandomAccessFile(tableFileName, "rw");
//			tableFile.setLength(pageSize);
//			tableFile.seek(0);
//			tableFile.writeInt(63);
//		}
//		catch(Exception e) {
//			System.out.println(e);
//		}
		
		/*  Code to insert a row in the davisbase_tables table 
		 *  i.e. database catalog meta-data 
		 */
		
		/*  Code to insert rows in the davisbase_columns table  
		 *  for each column in the new table 
		 *  i.e. database catalog meta-data 
		 */
	}
	
	/**
	 *
	 * @param createIndexString string
	 */
	private static void parseCreateIndex(String createIndexString) {
		System.out.println("STUB: This is the createQuery method");
		if(checkCreateIndex(createIndexString))
		{
			System.out.println("\tParsing the string:\"" + createIndexString + "\"");
		}
	}
	
	/**
	 *  Stub method for inserting a row into an existing table.
	 *  @param insertString is a String of the user input
	 */
	private static void parseInsert(String insertString)
	{
		System.out.println("STUB: This is the dropTable method.");
		if(checkInsert(insertString))
		{
			System.out.println("\tParsing the string:\"" + insertString + "\"");
		}
		
	}
	
	/**
	 * This function checks if command is exactly "show table;" and returns true if so.
	 * Print error and return false if otherwise.
	 * @param showTableString string
	 * @return boolean
	 */
	private static boolean checkShowTable(String showTableString)
	{
		ArrayList<String> tokens =  cleanCommand(showTableString);
		if(tokens.size() == 2 && tokens.get(1).equals("tables"))
		{
			return true;
		}
		else
		{
			System.out.println("SYNTAX ERROR. Did you mean \"show tables\"?");
			return false;
		}
		
	}
	
	/**
	 * This function checks the syntax of select query
	 * @param queryString string
	 * @return true/false
	 */
	private static boolean checkQuery(String queryString)
	{
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
		
		if(tokens.size() < 4)
		{
			state = '0';
		}
		
		while(state != 'E')
		{
			switch(state)
			{
				case 'S':
					if(itr == 1 && !(nameCheck(tokens.get(itr)) || tokens.get(itr).equals("*")))
					{
						//System.out.println(itr);
						state = '0';
					}
					else if(tokens.get(itr).equals("from"))
					{
						//System.out.println(itr);
						state = 'F';
					}
					else if(itr > 1)
					{
						if(itr % 2 == 0)
						{
							if(!tokens.get(itr).equals(","))
							{
								//System.out.println(itr);
								state = '0';
							}
						}
						else
						{
							if(!nameCheck(tokens.get(itr)))
							{
								//System.out.println(itr);
								state = '0';
							}
						}
					}
					break;
				case 'F':
					if(!nameCheck(tokens.get(itr))) { state = '0'; }
					else { state = 'W'; }
					break;
				case 'W':
					if(itr >= tokens.size()) { state = 'E'; }
					else {
						if(checkCondition(tokens.subList(itr, tokens.size()))) {state = 'E'; }
						else { state = '0'; }
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
	 * @param dropTableString string
	 * @return true/false
	 */
	private static boolean checkDropTable(String dropTableString)
	{
		ArrayList<String> tokens =  cleanCommand(dropTableString);
		if(tokens.size() < 3)
		{
			System.out.println("SYNTAX ERROR. Drop statment is incomplete. Format is \"DROP TABLE 'table_name'\"");
			return false;
		}
		else if(!tokens.get(1).equals("table"))
		{
			System.out.println("SYNTAX ERROR. Drop statment is incorrect. Format is \"DROP TABLE 'table_name'\"");
			return false;
		} //Invalid table name.
		else if(!nameCheck(tokens.get(2)))
		{
			System.out.println("SYNTAX ERROR. Table name is invalid.");
			return false;
		} //Syntax is correct.
		else { return true; }
	}
	
	/**
	 * This function checks the syntax of update statement.
	 * @param updateString string
	 * @return true/false
	 */
	private static boolean checkUpdate(String updateString)
	{
		ArrayList<String> tokens = cleanCommand(updateString);
		
		//Check size
		if(tokens.size() < 6)
		{
			System.out.println("SYNTAX ERROR. Update statement is incorrect. "
					+ " Format is \"UPDATE 'table_name' SET 'column_name' = value [WHERE 'condition']\"");
			return false;
		}
		
		//Check keywords
		if(!(nameCheck(tokens.get(1)) && tokens.get(2).equals("set")))
		{
			System.out.println("SYNTAX ERROR. Update statement is incorrect. "
					+ " Format is \"UPDATE 'table_name' SET 'column_name' = value [WHERE 'condition']\"");
			return false;
		}
		
		//Check Set Portion
		if(!(nameCheck(tokens.get(3)) && relationalOp(tokens.get(4)).equals("=") && (nameCheck(tokens.get(5)) || tokens.get(5).chars().allMatch(Character::isDigit))))
		{
			System.out.println("SYNTAX ERROR. Update statement is incorrect. "
					+ " Format is \"UPDATE 'table_name' SET 'column_name' = value [WHERE 'condition']\"");
			return false;
		}
		
		if(tokens.size() > 6) { return checkCondition(tokens.subList(6, tokens.size())); }
		else { return true; }
	}
	
	/**
	 * This function checks the syntax of delete statement
	 * @param deleteString str
	 * @return true/false
	 */
	private static boolean checkDelete(String deleteString)
	{
		ArrayList<String> tokens = cleanCommand(deleteString);
		
		//Check size
		if(tokens.size() < 3)
		{
			System.out.println("SYNTAX ERROR. Delete statement is incorrect. "
					+ " Format is \"DELETE FROM 'table_name' [WHERE 'condition']\"");
			return false;
		}
		
		//Check keywords, and table name
		if(!(tokens.get(1).equals("from") && nameCheck(tokens.get(2))))
		{
			System.out.println("SYNTAX ERROR. Delete statement is incorrect. "
					+ " Format is \"DELETE FROM 'table_name' [WHERE 'condition']\"");
			return false;
		}
		
		//Check condition
		if(tokens.size() > 3)
		{
			return checkCondition(tokens.subList(3, tokens.size()));
		}
		
		return true;
	}
	
	/**
	 * This function checks the syntax of insert statement.
	 * @param insertString string
	 * @return true/false
	 */
	private static boolean checkInsert(String insertString)
	{
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
		
		for(int itr = 1; itr < tokens.size(); itr++)
		{
			//TEST: see iteration, string, and state.
			//System.out.println(itr + ": " + tokens.get(itr) + "; state: " + state);
			
			switch(state)
			{
				case 'I':
					if(itr == 1 && !tokens.get(itr).equals("into")) { state = '0'; }
					else if(itr == 2 && !nameCheck(tokens.get(itr))) {state = '0'; }
					else if(itr > 2 && !tokens.get(itr).equals("(")) { state = '0'; }
					else if (itr > 2) { state = 'C'; }
					break;
				case 'C':
					if(itr == 4 && !nameCheck(tokens.get(itr))) { state = '0'; }
					else
					{
						if(itr % 2 == 0)
						{
							if(!nameCheck(tokens.get(itr)))
							{
								state = '0';
							}
							args++;
						}
						else
						{
							if(!tokens.get(itr).equals(",") && !tokens.get(itr).equals(")"))
							{
								state = '0';
							}
							else if(tokens.get(itr).equals(")")) { state = 'V'; }
							
						}
					}
					break;
				case 'V':
					if(itr % 2 == 0 && !tokens.get(itr).equals("values")) { state = '0'; }
					else if(itr % 2 == 1 && !tokens.get(itr).equals("(")) { state = '0'; }
					else if(itr % 2 == 1) { state = 'A';}
					break;
				case 'A':
					if(itr % 2 == 0)
					{
						if(!(nameCheck(tokens.get(itr)) || tokens.get(itr).chars().allMatch(Character::isDigit)) || args < 0)
						{
							state = '0';
						}
						else { args--; }
					}
					else
					{
						if(!tokens.get(itr).equals(",") && !tokens.get(itr).equals(")"))
						{
							state = '0';
						}
						else if(tokens.get(itr).equals(")") && args != 0) { state = '0'; }
						else if(tokens.get(itr).equals(")")) { state = 'E'; }
						
					}
					break;
				default:
					System.out.println("SYNTAX ERROR. Insert statement is incorrect. "
							+ " Format is \"INSERT INTO 'columns list' VALUES 'values list'\"");
					return false;
			}
			
			
		}
		
		if(state != 'E')
		{
			System.out.println("SYNTAX ERROR. Insert statement is not structure properly. "
					+ " Format is \"INSERT INTO 'columns list' VALUES 'values list'\"");
			return false;
		}
		else { return true; }
	}
	
	/**
	 * This function checks the syntax of create table statement.
	 * @param createTableString string
	 * @return true/false
	 */
	private static boolean checkCreateTable(String createTableString)
	{
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
		
		for(int itr = 1; itr < tokens.size(); itr++)
		{
			//TEST: see iteration, string, and state.
			//System.out.println(itr + ": " + tokens.get(itr) + "; state: " + state);
			
			switch(state)
			{
				case 'B':
					if(itr == 1 && !(tokens.get(itr).equals("table")))
						state = '0';
					else if(itr == 2 && !nameCheck(tokens.get(itr)))
						state = '0';
					else if(itr == 3 && tokens.get(itr).equals("("))
						state = 'C';
					break;
				case 'C':
					if(firstColumn && (tokens.get(itr).equals("row_id") || tokens.get(itr).equals("rowid")))
						state = 'D';
					else if(!firstColumn && nameCheck(tokens.get(itr)))
						state = 'D';
					else
						state = '0';
					break;
				case 'D':
					if(firstColumn && dataType(tokens.get(itr)).equals("int"))
						state = 'N';
					else if(!firstColumn && !dataType(tokens.get(itr)).isEmpty())
						state = 'N';
					else
						state = '0';
					break;
				case 'N':
					if(firstColumn)
					{
						if(tokens.get(itr).equals("primary") && tokens.get(itr+1).equals("key"))
						{
							firstColumn = false;
							itr++;
						}
						else
						{
							state ='0';
						}
					}
					else if(tokens.get(itr).equals("not") && notState == 0)
						notState++;
					else if(tokens.get(itr).equals("null") && notState == 1)
						notState++;
					else if(tokens.get(itr).equals("null"))
						state = '0';
					else if(tokens.get(itr).equals(","))
					{
						state = 'C';
						notState = 0; //Reset not null state.
					}
					else if(tokens.get(itr).equals(")"))
						state = 'E';
					
					
					break;
				default:
					System.out.println("SYNTAX ERROR. Create Table statement is not structure properly. "
							+ " Format is \"CREATE TABLE ' table_name' (data data_type [NOT NULL], . . . , data data_type [NOT NULL])\"");
				return false;
			}
		}
		
		if(state != 'E')
		{
			System.out.println("SYNTAX ERROR. Create Table statement is not structure properly. "
					+ " Format is \"CREATE TABLE ' table_name' (data data_type [NOT NULL], . . . , data data_type [NOT NULL])\"");
			return false;
		}
		else { return true; }
	}
	
	/**
	 * This function checks the syntax of create index statement.
	 * Returns true if syntax is correct or false if incorrect.
	 * @param createIndexString string
	 * @return true/false
	 */
	private static boolean checkCreateIndex(String createIndexString)
	{
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
		if(tokens.get(1).equals("unique")) { unique = 1; }
		
		for(int itr = 1; itr < tokens.size(); itr++)
		{
			//TEST: see iteration, string, and state.
			System.out.println(itr + ": " + tokens.get(itr) + "; state: " + state);
			
			switch(state)
			{
				case 'C':
					if(itr == 1 && !(tokens.get(itr).equals("unique") || tokens.get(itr).equals("index"))) { state = '0'; }
					else if(tokens.get(itr).equals("index")) { state = 'I'; }
					break;
				case 'I':
					if(nameCheck(tokens.get(itr))) { state = 'T'; }
					else { state = '0'; }
					break;
				case 'T':
					if(tokens.get(itr).equals("on") && nameCheck(tokens.get(itr + 1)))
					{
						itr++;
					}
					else if(tokens.get(itr).equals("(")) { state = 'L'; }

					break;
				case 'L':
					if((itr-unique) % 2 == 0)
					{
						if(!nameCheck(tokens.get(itr)))
						{
							state = '0';
						}
					}
					else
					{
						if(!tokens.get(itr).equals(",") && !tokens.get(itr).equals(")"))
						{
							state = '0';
						}
						else if(tokens.get(itr).equals(")")) { state = 'E'; }
					}
					break;
				default:
					System.out.println("SYNTAX ERROR. Create Index statement is incorrect. "
							+ " Format is \"CREATE [UNIQUE] INDEX 'index_name' ON 'table_name' 'column list'\"");
					return false;
			}
		}
		
		//If state is not E, then the structure of the statement is incorrect.
		if(state != 'E')
		{
			System.out.println("SYNTAX ERROR. Create Index statement is not structure properly. "
					+ " Format is \"CREATE [UNIQUE] INDEX 'index_name' ON 'table_name' 'column list'\"");
			return false;
		}
		else { return true; }
	}
	
	/**
	 * This function accepts a List of tokens that is the sublist of the arrayList<String>
	 * The format of where condition is:
	 * 		WHERE 'attribute' 'relational' 'value'
	 * This function returns true if string fufills that structure.
	 * Returns false if not.
	 * @param tokens par
	 * @return true/false
	 */
	private static boolean checkCondition(List<String> tokens)
	{
		//TEST: See ArrayList.
//		for(int i = 0; i < tokens.size();i++)
//		{
//			System.out.println(tokens.get(i));
//		}
//		System.out.println(tokens.size());
		//return true;
		
		if(tokens.size() == 4 && tokens.get(0).equals("where") && nameCheck(tokens.get(1)) 
				&& !relationalOp(tokens.get(2)).isEmpty() 
				&& (nameCheck(tokens.get(3)) || tokens.get(3).chars().allMatch(Character::isDigit)))
		{
			return true;
		}
		else
		{
			System.out.println("SYNTAX ERROR. Condition is incorrect. "
					+ " Format is \"WHERE 'attribute' 'relational' 'value'\"");
			return false;
		}
	}
	
	/**
	 * This function accepts a string and returns true if string is an acceptable name.
	 * Returns false if not.
	 * @param name string
	 * @return true/false
	 */
	private static boolean nameCheck(String name)
	{
		//String must not start with the digit or a character that is not a letter except for _, @, and #.
		if(Character.isDigit(name.charAt(0)) || (!Character.isLetter(name.charAt(0)) 
				&& !(name.charAt(0) == '_' || name.charAt(0) == '@' || name.charAt(0) == '#')))
		{
			return false;
		}
		
		//String must not contain a period.
		for(int i = 0; i < name.length(); i++)
		{	
			if(!Character.isLetterOrDigit(name.charAt(i)) && name.charAt(i) == '.')
			{
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * This function accepts a string and returns the argument if argument is a data type.
	 * Returns empty if not
	 * @param type data type
	 * @return string
	 */
	private static String dataType(String type)
	{
		switch(type)
		{
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
	 * @param r string
	 * @return string
	 */
	private static String relationalOp(String r)
	{
		switch(r)
		{
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
	 * @param input string
	 * @return list
	 */
	private static ArrayList<String> cleanCommand(String input)
	{
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
	
}
