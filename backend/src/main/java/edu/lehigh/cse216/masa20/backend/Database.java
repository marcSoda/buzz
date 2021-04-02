package edu.lehigh.cse216.masa20.backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class Database {
    //State of the database connection. Should be null when not connected
    private Connection mConnection;

    //Postgresql statements to be called in subsequent functions.
    private PreparedStatement mSelectAllPosts;
    private PreparedStatement mSelectAllUsers;
    private PreparedStatement mSelectOnePost;
    private PreparedStatement mDeleteOnePost;
    private PreparedStatement mSelectUserUpvotes;
    private PreparedStatement mSelectUserDownvotes;
    private PreparedStatement mSelectPostComments;

    private PreparedStatement mInsertPost;
    private PreparedStatement mInsertUser;
    private PreparedStatement mInsertUpvote;
    private PreparedStatement mInsertDownvote;
    private PreparedStatement mUpvotePost;
    private PreparedStatement mDownvotePost;
    private PreparedStatement mInsertComment;

    private PreparedStatement mUpdateOnePost;

    private PreparedStatement mCreatePostTable;
    private PreparedStatement mDropPostTable;
    private PreparedStatement mCreateUserTable;
    private PreparedStatement mDropUserTable;
    private PreparedStatement mCreateUpvoteTable;
    private PreparedStatement mDropUpvoteTable;
    private PreparedStatement mCreateDownvoteTable;
    private PreparedStatement mDropDownvoteTable;
    private PreparedStatement mCreateCommentTable;
    private PreparedStatement mDropCommentTable;

    //Contains all fields of a single post.
    public static class PostData {
        int mPostId;
        String mSubject;
        String mMessage;
        String mTimestamp;
        int mUpvotes;
        int mDownvotes;

        public PostData(int postId, String subject, String message, String timestamp, int upvotes, int downvotes) {
            mPostId = postId;
            mSubject = subject;
            mMessage = message;
            mTimestamp = timestamp;
            mUpvotes = upvotes;
            mDownvotes = downvotes;
        }
    }

    //Contains all fields for a single user.
    public static class UserData {
        String mEmail;
        String mName;
        String mDescription;
        int mUid;

        public UserData(int uid, String email, String name, String description) {
            mUid = uid;
            mEmail=email;
            mName=name;
            mDescription=description;
        }
    }

    //Contains all fields for a single upvote.
    public static class UpvoteData {
        int mUid;
        int mPostId;

        public UpvoteData(int uid, int postId) {
            mUid = uid;
            mPostId = postId;
        }
    }

    //Contains all fields for a single downvote.
    public static class DownvoteData {
        int mUid;
        int mPostId;

        public DownvoteData(int uid, int postId) {
            mUid = uid;
            mPostId = postId;
        }
    }

    //Contains all fields for a single comment.
    public static class CommentData {
        int mUid;
        int mPostId;
        String mComment;

        public CommentData(int uid, int postId, String comment) {
            mUid = uid;
            mPostId = postId;
            mComment = comment;
        }
    }

    private Database() {
    }

    /**
     * Get a fully-configured connection to the database
     *
     * @param db_url the name of the database on heroku
     *
     * @return A Database object, or null if we cannot connect properly
     */
    static Database getDatabase(String db_url) {
        Database db = new Database();

        // Give the Database object a connection, fail if we cannot get one
        try {
            Class.forName("org.postgresql.Driver");
            URI dbUri = new URI(db_url);
            String username = dbUri.getUserInfo().split(":")[0];
            String password = dbUri.getUserInfo().split(":")[1];
            String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath() + "?sslmode=require";
            Connection conn = DriverManager.getConnection(dbUrl, username, password);
            if (conn == null) {
                System.err.println("Error: DriverManager.getConnection() returned a null object");
                return null;
            }
            db.mConnection = conn;
        } catch (SQLException e) {
            System.err.println("Error: DriverManager.getConnection() threw a SQLException");
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException cnfe) {
            System.out.println("Unable to find postgresql driver");
            return null;
        } catch (URISyntaxException s) {
            System.out.println("URI Syntax Error");
            return null;
        }

        // Attempt to create all of our prepared statements.  If any of these
        // fail, the whole getDatabase() call should fail
        try {
            // Note: no "IF NOT EXISTS" or "IF EXISTS" checks on table
            // creation/deletion, so multiple executions will cause an exception
            db.mCreatePostTable = db.mConnection.prepareStatement(
                                                                  "CREATE TABLE postData (" +
                                                                  "postId SERIAL PRIMARY KEY," +
                                                                  "uid VARCHAR(50)," +
                                                                  "subject VARCHAR(50) NOT NULL," +
                                                                  "message VARCHAR(500) NOT NULL," +
                                                                  "timestamp VARCHAR(50) NOT NULL," +
                                                                  "upvotes INTEGER DEFAULT 0," +
                                                                  "downvotes INTEGER DEFAULT 0)");
            db.mCreateUserTable = db.mConnection.prepareStatement(
                                                                  "CREATE TABLE userData (" +
                                                                  "uid VARCHAR(50) PRIMARY KEY," +
                                                                  "email VARCHAR(20) NOT NULL," +
                                                                  "firstName VARCHAR(50)," +
                                                                  "lastName VARCHAR(50) NOT NULL," +
                                                                  "description VARCHAR(500))");
            db.mCreateUpvoteTable = db.mConnection.prepareStatement(
                                                                    "CREATE TABLE upvotes (" +
                                                                    "uid VARCHAR(50)," +
                                                                    "postId SERIAL," +
                                                                    "PRIMARY KEY(uid, postId))");
            db.mCreateDownvoteTable = db.mConnection.prepareStatement(
                                                                      "CREATE TABLE downvotes (" +
                                                                      "uid VARCHAR(50)," +
                                                                      "postId SERIAL," +
                                                                      "PRIMARY KEY(uid, postId))");
            db.mCreateCommentTable = db.mConnection.prepareStatement(
                                                                     "CREATE TABLE comments (" +
                                                                     "uid VARCHAR(50)," +
                                                                     "postId SERIAL," +
                                                                     "comment VARCHAR(200)," +
                                                                     "PRIMARY KEY(uid, postId))");
            db.mDropPostTable = db.mConnection.prepareStatement(
                                                                "DROP TABLE postData");
            db.mDropUserTable = db.mConnection.prepareStatement(
                                                                "DROP TABLE userData");
            db.mDropUpvoteTable = db.mConnection.prepareStatement(
                                                                  "DROP TABLE upvotes");
            db.mDropDownvoteTable = db.mConnection.prepareStatement(
                                                                    "DROP TABLE downvotes");
            db.mDropCommentTable = db.mConnection.prepareStatement(
                                                                   "DROP TABLE comments");

            db.mDeleteOnePost = db.mConnection.prepareStatement(
                                                                "DELETE FROM postData" +
                                                                "WHERE postId = ?");
            db.mInsertPost = db.mConnection.prepareStatement(
                                                             "INSERT INTO postData" +
                                                             "VALUES (default, ?, ?, ?, NOW())", PreparedStatement.RETURN_GENERATED_KEYS); //RETURN_GENERATED_KEYS to be able to get the id in insertRow()
            db.mSelectAllPosts = db.mConnection.prepareStatement(
                                                                 "SELECT * FROM postData");
            db.mSelectAllUsers = db.mConnection.prepareStatement(
                                                                 "SELECT * FROM userData");
            db.mSelectUserUpvotes = db.mConnection.prepareStatement(
                                                                    "SELECT * FROM upvotes" +
                                                                    "WHERE uid = ?");
            db.mSelectUserDownvotes = db.mConnection.prepareStatement(
                                                                      "SELECT * FROM downvotes" +
                                                                      "WHERE uid = ?");
            db.mSelectPostComments = db.mConnection.prepareStatement(
                                                                     "SELECT * FROM comments" +
                                                                     "WHERE postId = ?");
            db.mSelectOnePost = db.mConnection.prepareStatement(
                                                                "SELECT * from postData" +
                                                                "WHERE postId=?");
            db.mUpdateOnePost = db.mConnection.prepareStatement(
                                                                "UPDATE postData" +
                                                                "SET subject = ?, message = ?" +
                                                                "WHERE postId = ?", PreparedStatement.RETURN_GENERATED_KEYS);
            db.mUpvotePost = db.mConnection.prepareStatement(
                                                             "UPDATE postData" +
                                                             "SET upvotes = upvotes + 1" +
                                                             "WHERE postId = ?");
            db.mDownvotePost = db.mConnection.prepareStatement(
                                                               "UPDATE postData" +
                                                               "SET downvotes = downvotes + 1" +
                                                               "WHERE postId = ?");
            db.mInsertUser = db.mConnection.prepareStatement(
                                                             "INSERT INTO userData (uid, email, firstName, lastName)" +
                                                             "VALUES (?, ?, ?, ?)");
            db.mInsertUpvote = db.mConnection.prepareStatement(
                                                               "INSERT INTO upvotes" +
                                                               "VALUES (?, ?)");
            db.mInsertDownvote = db.mConnection.prepareStatement(
                                                                 "INSERT INTO downvotes" +
                                                                 "VALUES (?, ?)");
            db.mInsertComment = db.mConnection.prepareStatement(
                                                                "INSERT INTO comments" +
                                                                "VALUES (?, ?, ?)");

        } catch (SQLException e) {
            System.err.println("Error creating prepared statement");
            e.printStackTrace();
            db.disconnect();
            return null;
        }
        return db;
    }

    /**
     * Close the current connection to the database, if one exists.
     *
     * NB: The connection will always be null after this call, even if an
     *     error occurred during the closing operation.
     *
     * @return True if the connection was cleanly closed, false otherwise
     */
    boolean disconnect() {
        if (mConnection == null) {
            System.err.println("Unable to close connection: Connection was null");
            return false;
        }
        try {
            mConnection.close();
        } catch (SQLException e) {
            System.err.println("Error: Connection.close() threw a SQLException");
            e.printStackTrace();
            mConnection = null;
            return false;
        }
        mConnection = null;
        return true;
    }

    //TODO: FORMAT ALL FUNCTIONS FOR JAVADOC

    /**
     * Insert a row into the database
     * @param uid The user ID associated with this post.
     * @param subject The heading of this post.
     * @param message The content of this post.
     * @return The postId of the inserted row.
     */
    int insertPost(String uid, String subject, String message) {
        int newPostId = -1;
        try {
            mInsertPost.setString(1, uid);
            mInsertPost.setString(2, subject);
            mInsertPost.setString(3, message);
            mInsertPost.executeUpdate();
            //get the postId
            ResultSet rs = mInsertPost.getGeneratedKeys();
            if (rs.next()) newPostId = rs.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return newPostId;
    }

    //Insert a user into the db
    boolean insertUser(String uid, String email, String firstName, String lastName) {
        try {
            mInsertUser.setString(1, uid);
            mInsertUser.setString(2, email);
            mInsertUser.setString(3, firstName);
            mInsertUser.setString(4, lastName);
            mInsertUser.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //Keeps track of who upvoted what post. Probably just return true or false.
    boolean insertUpvote(String subject, Integer up, Integer down) {
        return true;
    }

    //Keeps track of who downvoted what post. Probably just return true or false.
    boolean insertDovote(String subject, Integer up, Integer down) {
        return true;
    }

    //Insert a comment into the db. Prob return true or false.
    boolean insertComment(String email, String message) {
        return true;
    }

    //Get all posts from db.
    ArrayList<PostData> selectAllPosts() {
        ArrayList<PostData> res = new ArrayList<PostData>();
        try {
            ResultSet rs = mSelectAllPosts.executeQuery();
            while (rs.next()) {
                res.add(new PostData(rs.getInt("postId"),
                                     rs.getString("subject"),
                                     rs.getString("message"),
                                     rs.getString("timestamp"),
                                     rs.getInt("upvotes"),
                                     rs.getInt("downvotes")));
            }
            rs.close();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Get all users from db. (Not sure why we would ever need this... I don't even know if it works.)
    ArrayList<UserData> selectAllUsers() {
        ArrayList<UserData> res = new ArrayList<UserData>();
        try {
            ResultSet rs = mSelectAllUsers.executeQuery();
            while (rs.next()) {
                res.add(new UserData(rs.getInt("uid"),
                                     rs.getString("email"),
                                     rs.getString("name"),
                                     rs.getString("description")));
            }
            rs.close();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Get all upvotes for a certain user.
    ArrayList<UpvoteData> selectUserUpvotes(int uid) {
        ArrayList<UpvoteData> data = new ArrayList<UpvoteData>();
        return data;
    }

    //Get all downvotes for a certain user.
    ArrayList<DownvoteData> selectUserDownvotes(int uid) {
        ArrayList<DownvoteData> data = new ArrayList<DownvoteData>();
        return data;
    }

    //Get all comments associated with a single post.
    ArrayList<CommentData> selectPostComments(int postId) {
        ArrayList<CommentData> data = new ArrayList<CommentData>();
        return data;
    }

    //Get all post data for one post.
    PostData selectOnePost(int postId) {
        PostData res = null;
        try {
            mSelectOnePost.setInt(1, postId);
            ResultSet rs = mSelectOnePost.executeQuery();
            if (rs.next()) {
                res = new PostData(rs.getInt("postId"),
                                   rs.getString("subject"),
                                   rs.getString("message"),
                                   rs.getString("timestamp"),
                                   rs.getInt("upvotes"),
                                   rs.getInt("downvotes"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    //Delete a post
    boolean deleteOnePost(int postId) {
        int res = 0;
        try {
            mDeleteOnePost.setInt(1, postId);
            res = mDeleteOnePost.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //res is the number of altered rows. return false if the row was no deleted
        if (res > 0) return true;
        return false;
    }

    //PUT post
    PostData updateOnePost(int postId, String subject, String message) {
        PostData row = null;
        try {
            mUpdateOnePost.setString(1, subject);
            mUpdateOnePost.setString(2, message);
            mUpdateOnePost.setInt(3, postId);
            mUpdateOnePost.executeUpdate();
            ResultSet rs = mUpdateOnePost.getGeneratedKeys();
            if (rs.next()) {
                row = new PostData (rs.getInt("postId"),
                                    rs.getString("subject"),
                                    rs.getString("message"),
                                    rs.getString("timestamp"),
                                    rs.getInt("upvotes"),
                                    rs.getInt("downvotes"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return row;
    }

    //Increment upvote for a post.
    boolean upvote(int postId) {
        try {
            mUpvotePost.setInt(1, postId);
            mUpvotePost.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //Increment downvote for a post.
    boolean downvote(int postId) {
        try {
            mDownvotePost.setInt(1, postId);
            mDownvotePost.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    void createPostTable() {
        try {
            mCreatePostTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    void createUserTable() {
        try {
            mCreateUserTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    void createUpvoteTable() {
        try {
            mCreateUpvoteTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    void createDownvoteTable() {
        try {
            mCreateDownvoteTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    void createCommentTable() {
        try {
            mCreateCommentTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void dropPostTable() {
        try {
            mDropPostTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    void dropUserTable() {
        try {
            mDropUserTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    void dropUpvoteTable() {
        try {
            mDropUpvoteTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    void dropDownvoteTable() {
        try {
            mDropDownvoteTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    void dropCommentTable() {
        try {
            mDropCommentTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
