package edu.utdallas.cs6360.davisbase;

import java.io.File;
import java.io.RandomAccessFile;

import static edu.utdallas.cs6360.davisbase.DavisBase.pageSize;

class FileHandler {
    private static String dataDir = "data";

    private static String catalogDir = dataDir + "/" +  "catalog";
    private static String userDataDir = dataDir + "/" +  "user_data";

    private static String davisbaseTablesTable = catalogDir + "/" + "davisbase_tables";
    private static String davisbaseColumnsTable = catalogDir + "/" + "davisbase_columns";

    /**
     * This static method creates the DavisBase data storage container
     * and then initializes two .tbl files to implement the two
     * system tables, davisbase_tables and davisbase_columns
     */
    static void initializeDataStore() {

        /** Create data directory at the current OS location to hold */
        createDatabaseDirectory(dataDir);
        createDatabaseDirectory(catalogDir);
        createDatabaseDirectory(userDataDir);

        /** Create davisbase_tables system catalog */
        createTableFile(davisbaseTablesTable);

        /** Create davisbase_columns system catalog */
        createTableFile(davisbaseColumnsTable);

    }


    /**
     * Creates directory if it doesn't exist
     *
     * @param name Directory name
     */
    private static void createDatabaseDirectory(String name) {
        try {
            File dataDir = new File(name);
            if (!dataDir.exists()) {
                while (true) {
                    if (dataDir.mkdir()) {
                        break;
                    }
                }
            }
        } catch (SecurityException se) {
            System.out.println("Unable to create " + name + " container directory");
            System.out.println(se);
        }
    }


    /**
     * @param tablename table name
     * @return boolean return false if table exists and returns true if table created
     */
    static boolean createTable(String tablename){
        return createTableFile(userDataDir + "/" + tablename);
    }

    private static boolean createTableFile(String tablename) {
        String tableFilename = tablename + ".tbl";
        try {
            File f = new File(tableFilename);
            if (!f.exists()) {
                RandomAccessFile file = new RandomAccessFile(tableFilename, "rw");
                file.setLength(pageSize);
                file.close();
                return true;
            }else{
                return false;
            }

        } catch (Exception e) {
            System.out.println("Unable to create the " + tablename + " file");
            System.out.println(e);
        }
        return false;
    }

}