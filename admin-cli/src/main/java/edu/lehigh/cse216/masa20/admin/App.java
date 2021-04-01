package edu.lehigh.cse216.masa20.admin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Map;

/**
 * App is our basic admin app.  For now, it is a demonstration of the six key
 * operations on a database: connect, insert, update, query, delete, disconnect
 */
public class App {

    /**
     * Print the menu for our program
     */
    static void menu() {
        System.out.println("Main Menu");
        System.out.println("  [T] Create tblData");
        System.out.println("  [D] Drop tblData");
        System.out.println("  [1] Query for a specific row");
        System.out.println("  [*] Query for all rows");
        System.out.println("  [-] Delete a row");
        System.out.println("  [+] Insert a new row");
        System.out.println("  [~] Update a row");
        // New Commands added by Jake
        System.out.println("  [u] Increment the number of upvotes in a row");
        System.out.println("  [d] Decrement the number of downvotes in a row");
        // Commands from tutorial
        System.out.println("  [q] Quit Program");
        System.out.println("  [?] Help (this message)");
    }

    /**
     * Ask the user to enter a menu option; repeat until we get a valid option
     *
     * @param in A BufferedReader, for reading from the keyboard
     *
     * @return The character corresponding to the chosen menu option
     */
    static char prompt(BufferedReader in) {
        // The valid actions:
        String actions = "TD1*-+~qud?"; 
        // Jake added new commands u and d

        // We repeat until a valid single-character option is selected
        while (true) {
            System.out.print("[" + actions + "] :> ");
            String action;
            try {
                action = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            if (action.length() != 1)
                continue;
            if (actions.contains(action)) {
                return action.charAt(0);
            }
            System.out.println("Invalid Command");
        }
    }

    /**
     * Ask the user to enter a String message
     *
     * @param in A BufferedReader, for reading from the keyboard
     * @param message A message to display when asking for input
     *
     * @return The string that the user provided.  May be "".
     */
    static String getString(BufferedReader in, String message) {
        String s;
        try {
            System.out.print(message + " :> ");
            s = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return s;
    }

    /**
     * Ask the user to enter an integer
     *
     * @param in A BufferedReader, for reading from the keyboard
     * @param message A message to display when asking for input
     *
     * @return The integer that the user provided.  On error, it will be -1
     */
    static int getInt(BufferedReader in, String message) {
        int i = -1;
        try {
            System.out.print(message + " :> ");
            i = Integer.parseInt(in.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return i;
    }

    /**
     * The main routine runs a loop that gets a request from the user and
     * processes it
     *
     * @param argv Command-line options.  Ignored by this program.
     */
    public static void main(String[] argv) {
        // get the Postgres configuration from the environment
	    String databaseName = "postgres://mttuejjprtjezy:4c6467ad48c998116f9e2dda61d46b615d9cf6f236ba0281f212cbeb81ca384d@ec2-54-242-43-231.compute-1.amazonaws.com:5432/dacvh86oqpjgot";

        // Get a fully-configured connection to the database, or exit
        // immediately
        Database db = Database.getDatabase(databaseName);
        if (db == null)
            return;

        // Start our basic command-line interpreter:
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            // Get the user's request, and do it
            //
            // NB: for better testability, each action should be a separate
            //     function call
            char action = prompt(in);
            if (action == '?') {
                menu();
            } else if (action == 'q') {
                break;
            } else if (action == 'T') {
                db.createTable();
            } else if (action == 'D') {
                db.dropTable();
            } else if (action == '1') {
                int id = getInt(in, "Enter the row ID");
                if (id == -1)
                    continue;
                Database.RowData res = db.selectOne(id);
                if (res != null) {
                    System.out.println("  [" + res.mId + "] " + res.mSubject);
                    System.out.println("  --> " + res.mMessage);
                    // integration testing for selectOne
                    System.out.println("  --> " + res.mTimestamp);
                    System.out.println("  --> " + res.mUpvotes);
                    System.out.println("  --> " + res.mDownvotes);
                }
            } else if (action == '*') {
                ArrayList<Database.RowData> res = db.selectAll();
                if (res == null)
                    continue;
                System.out.println("  Current Database Contents");
                System.out.println("  -------------------------");
                for (Database.RowData rd : res) {
                    System.out.println("  [" + rd.mId + "] " + rd.mSubject);
                    // integration testing for selectAll
                    System.out.println("  --> " + rd.mMessage);
                    System.out.println("  --> " + rd.mTimestamp);
                    System.out.println("  --> " + rd.mUpvotes);
                    System.out.println("  --> " + rd.mDownvotes);
                }
            } else if (action == '-') {
                int id = getInt(in, "Enter the row ID");
                if (id == -1)
                    continue;
                int res = db.deleteRow(id);
                if (res == -1)
                    continue;
                System.out.println("  " + res + " rows deleted");
            } else if (action == '+') {
                String subject = getString(in, "Enter the subject");
                String message = getString(in, "Enter the message");
                if (subject.equals("") || message.equals("")) // if wrong format exit
                    continue;
                int res = db.insertRow(subject, message); // updated to select four parameters
                System.out.println(res + " rows added");
            } else if (action == '~') {
                int id = getInt(in, "Enter the row ID :> ");
                if (id == -1)
                    continue;
                String newSubject = getString(in, "Enter the new subject");
                String newMessage = getString(in, "Enter the new message");
                int res = db.updateOne(id, newSubject, newMessage);
                if (res == -1)
                    continue;
                System.out.println("  " + res + " rows updated");
            } else if (action == 'u') { // new test for updating upvote
                int id = getInt(in, "Enter the row ID :> ");
                if (id == -1)
                    continue;
                boolean res = db.upvote(id);
                if (!res)
                    continue;
                System.out.println("rows updated for upvote");
            } else if (action == 'd') { // new test for updating downvote
                int id = getInt(in, "Enter the row ID :> ");
                if (id == -1)
                    continue;
                boolean res = db.downvote(id);
                if (!res)
                    continue;
                System.out.println("rows updated for downvote");
            }
        }
        // Always remember to disconnect from the database when the program
        // exits
        db.disconnect();
    }
}
