package edu.lehigh.cse216.masa20.backend;

// Import the Spark package, so that we can make use of the "get" function to
// create an HTTP GET route
import spark.Spark;
import spark.Request;
import spark.Response;
import java.util.Map;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

//OAuth stuff
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;

// Import Google's JSON library
import com.google.gson.*;

public class App {
    //Session Table
    public static Hashtable<String, String> sessionTable = new Hashtable<String, String>();

    private static final HttpTransport transport = new NetHttpTransport();
    private static final JsonFactory jsonFactory = new JacksonFactory();

    public static void main(String[] args) {
        // Get the port on which to listen for requests
        Spark.port(getIntFromEnv("PORT", 4567));

        //get environment variables
        Map<String, String> env = System.getenv();

        // gson provides us with a way to turn JSON into objects, and objects
        // into JSON.
        //
        // NB: it must be final, so that it can be accessed from our lambdas
        //
        // NB: Gson is thread-safe.  See
        // https://stackoverflow.com/questions/10380835/is-it-ok-to-use-gson-instance-as-a-static-field-in-a-model-bean-reuse
        final Gson gson = new Gson();

        // db holds all of the data that has been provided via HTTP
        // requests

        // get the Postgres configuration from the environment
        String databaseName = "postgres://mttuejjprtjezy:4c6467ad48c998116f9e2dda61d46b615d9cf6f236ba0281f212cbeb81ca384d@ec2-54-242-43-231.compute-1.amazonaws.com:5432/dacvh86oqpjgot";

        // Get a fully-configured connection to the database, or exit
        Database db = Database.getDatabase(databaseName);
        if (db == null)
            return;
        // db.dropPostTable();
        // db.dropUserTable();
        // db.dropCommentTable();
        // db.dropUpvoteTable();
        // db.dropDownvoteTable();
        // db.createPostTable();
        // db.createUserTable();
        // db.createCommentTable();
        // db.createUpvoteTable();
        // db.createDownvoteTable();

        // Set up the location for serving static files.  If the STATIC_LOCATION
        // environment variable is set, we will serve from it.  Otherwise, serve
        // from "/web"
        String static_location_override = System.getenv("STATIC_LOCATION");
        if (static_location_override == null) {
            Spark.staticFileLocation("/web");
        } else {
            Spark.staticFiles.externalLocation(static_location_override);
        }

        Spark.before((request, response) -> {
	    String path = request.pathInfo();
	    if (path != null && !path.equals("/login") && !path.equals("/auth") && !isAuth(request)) {
                response.redirect("/login.html");
	    }
	});

        // Set up a route for serving the main page
        Spark.get("/", (req, res) -> {
            res.redirect("/main.html");
	    return "";
	});

        // Not used
        Spark.get("/login", (request, response) -> {
	    System.out.println("LOGIN ROUTE HIT");
	    response.redirect("/login.html");
	    return "";
	});

        //use cors if ENABLE_CORS = TRUE within heroku (which it should)
        String cors_enabled = env.get("ENABLE_CORS");
        if (cors_enabled.equals("TRUE")) {
            final String acceptCrossOriginRequestsFrom = "*";
            final String acceptedCrossOriginRoutes = "GET,PUT,POST,DELETE,OPTIONS";
            // final String supportedRequestHeaders = "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin";
            final String supportedRequestHeaders = "*";
            enableCORS(acceptCrossOriginRequestsFrom, acceptedCrossOriginRoutes, supportedRequestHeaders);
        }

        Spark.post("/auth", (request, response) -> {
	    SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
	    response.type("application/json");

	    String idTokenString = req.mId_token;
	    String clientId = env.get("CLIENT_ID");

	    GoogleIdToken idToken = null;
	    String uid = null;
	    String sessionKey = null;

	    GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
		.setAudience(Collections.singletonList(clientId))
		.build();
	    try {
		idToken = verifier.verify(idTokenString);
	    } catch (java.security.GeneralSecurityException eSecurity) {
		System.out.println("Token Verification Security Execption" + eSecurity);
	    } catch (java.io.IOException eIO) {
		System.out.println("Token Verification IO Execption" + eIO);
	    }
	    if (idToken != null) {
		Payload payload = idToken.getPayload();
		// Get profile information from payload
		uid = payload.getSubject();
		String email = payload.getEmail();
		boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
		String name = (String) payload.get("name");
		String familyName = (String) payload.get("family_name");
		String givenName = (String) payload.get("given_name");

		boolean isUserExists = db.checkUserExists(uid);
                if (!isUserExists) {
                    boolean succ = db.insertUser(uid, email, givenName, familyName);
                    if (!succ) {
                        System.out.println("ins usr fail");
                    } else {
                        System.out.println("succ");
                    }
                }

                // Auth is good
		sessionTable.put(request.session().id(), uid);
                request.session().attribute("uid", uid);
	    } else {
		System.out.println("Invalid ID token.");
	    }

	    response.status(200);
	    return gson.toJson(new StructuredResponse("OK", null , new String[]{uid, sessionKey}));
	});

        // GET route that returns all message titles and Ids.  All we do is get
        // the data, embed it in a StructuredResponse, turn it into JSON, and
        // return it.  If there's no data, we return "[]", so there's no need
        // for error handling.
        Spark.get("/messages", (request, response) -> {
                response.type("application/json");
                ArrayList<Database.PostData> data = db.selectAllPosts();
                if (data == null) {
                    response.status(500);
                    return gson.toJson(new StructuredResponse("error", "failed to get all messages", null));
                } else {
                    return gson.toJson(new StructuredResponse("ok", null, data));
                }
            });

        // GET route that returns everything for a single row in the db.
        // The ":id" suffix in the first parameter to get() becomes
        // request.params("id"), so that we can get the requested row ID.  If
        // ":id" isn't a number, Spark will reply with a status 500 Internal
        // Server Error.  Otherwise, we have an integer, and the only possible
        Spark.get("/messages/:id", (request, response) -> {
                int idx = Integer.parseInt(request.params("id"));
                response.type("application/json");
                Database.PostData data = db.selectOnePost(idx);
                if (data == null) {
                    response.status(500);
                    return gson.toJson(new StructuredResponse("error", idx + " not found", null));
                } else {
                    response.status(200);
                    return gson.toJson(new StructuredResponse("ok", null, data));
                }
            });

        // POST route for adding a new element to the db.  This will read
        // JSON from the body of the request, turn it into a SimpleRequest
        // object, extract the title and message, insert them, and return the
        // ID of the newly created row.
        Spark.post("/messages", (request, response) -> {
                // NB: if gson.Json fails, Spark will reply with status 500 Internal
                // Server Error
                SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
                response.type("application/json");
                int newId = db.insertPost(req.mUid, req.mTitle, req.mMessage);

                if (newId == -1) {
                    response.status(500);
                    return gson.toJson(new StructuredResponse("error", "error performing insertion", null));
                } else {
                    response.status(200);
                    return gson.toJson(new StructuredResponse("ok", "" + newId, null));
                }
            });

        // POST route for incrementing upvote
        Spark.post("/messages/:id/upvote", (request, response) -> {
                // NB: if gson.Json fails, Spark will reply with status 500 Internal
                // Server Error
                SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
                response.type("application/json");

                int idx = Integer.parseInt(request.params("id"));
                boolean succ = db.upvote(idx);

                if (!succ) {
                    response.status(500);
                    return gson.toJson(new StructuredResponse("error", "upvote error", null));
                } else {
                    response.status(200);
                    return gson.toJson(new StructuredResponse("ok", null, null));
                }
            });

        // POST route for incrementing downvote
        Spark.post("/messages/:id/downvote", (request, response) -> {
                // NB: if gson.Json fails, Spark will reply with status 500 Internal
                // Server Error
                SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
                response.type("application/json");

                int idx = Integer.parseInt(request.params("id"));
                boolean succ = db.downvote(idx);

                if (!succ) {
                    response.status(500);
                    return gson.toJson(new StructuredResponse("error", "downvote error", null));
                } else {
                    response.status(200);
                    return gson.toJson(new StructuredResponse("ok", null, null));
                }
            });

        // PUT route for updating a row in the db. This is almost
        // exactly the same as POST
        Spark.put("/messages/:id", (request, response) -> {
                // If we can't get an ID or can't parse the JSON, Spark will send
                // a status 500
                int idx = Integer.parseInt(request.params("id"));
                SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
                response.type("application/json");
                Database.PostData result = db.updateOnePost(idx, req.mTitle, req.mMessage);
                if (result == null) {
                    response.status(500);
                    return gson.toJson(new StructuredResponse("error", "unable to update row " + idx, null));
                } else {
                    response.status(200);
                    return gson.toJson(new StructuredResponse("ok", null, result));
                }
            });

        // DELETE route for removing a row from the db
        Spark.delete("/messages/:id", (request, response) -> {
                // If we can't get an ID, Spark will send a status 500
                int idx = Integer.parseInt(request.params("id"));
                response.type("application/json");
                // NB: we won't concern ourselves too much with the quality of the
                //     message sent on a successful delete
                boolean result = db.deleteOnePost(idx);
                if (!result) {
                    response.status(500);
                    return gson.toJson(new StructuredResponse("error", "unable to delete row " + idx, null));
                } else {
                    response.status(200);
                    return gson.toJson(new StructuredResponse("ok", null, null));
                }
            });

        // GET all comments for message `:id`
        Spark.get("/messages/:id/comments", (request, response) -> {
                response.type("application/json");

                int postId = Integer.parseInt(request.params("id"));
                ArrayList<Database.CommentData> data = db.selectPostComments(postId);
                if (data == null) {
                    response.status(500);
                    return gson.toJson(new StructuredResponse("error", "failed to get comments for " + postId, null));
                } else {
                    return gson.toJson(new StructuredResponse("ok", null, data));
                }
            });

        // POST new comment for message `:id`
        Spark.post("/messages/:id/comments", (request, response) -> {
                response.type("application/json");

                int postId = Integer.parseInt(request.params("id"));
                CommentRequest req = gson.fromJson(request.body(), CommentRequest.class);
                int newId = db.insertComment(postId, req.mUid, req.mComment);
                if (newId == -1) {
                    response.status(500);
                    return gson.toJson(new StructuredResponse("error", "error performing insertion", null));
                } else {
                    response.status(200);
                    return gson.toJson(new StructuredResponse("ok", "" + newId, null));
                }
            });

        // PUT for updating comment `:cid` on message `:id`
        // NB: `:id` is not used
        Spark.put("/messages/:id/comments/:cid", (request, response) -> {
                response.type("application/json");

                int commentId = Integer.parseInt(request.params("cid"));
                CommentRequest req = gson.fromJson(request.body(), CommentRequest.class);
                Database.CommentData result = db.updateComment(commentId, req.mComment);
                if (result == null) {
                    response.status(500);
                    return gson.toJson(new StructuredResponse("error", "unable to update comment " + commentId, null));
                } else {
                    response.status(200);
                    return gson.toJson(new StructuredResponse("ok", null, result));
                }
            });
    }

    /**
     * Get an integer environment varible if it exists, and otherwise return the
     * default value.
     *
     * @envar      The name of the environment variable to get.
     * @defaultVal The integer value to use as the default if envar isn't found
     *
     * @returns The best answer we could come up with for a value for envar
     */
    static int getIntFromEnv(String envar, int defaultVal) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get(envar) != null) {
            return Integer.parseInt(processBuilder.environment().get(envar));
        }
        return defaultVal;
    }

    private static boolean isAuth(Request request) {
        String uid = request.session(true).attribute("uid");
        String sessionKey = request.session().id();

        try {
            if (uid != null && sessionKey != null && sessionTable.get(sessionKey).equals(uid)) {
                return true;
            } else {
                System.out.println("Auth failed");
                return false;
            }
        } catch (Exception e) {
            System.out.println("Auth error: " + e.toString());
        }
        return false;
    }

    /**
     * SET up CORS headers for the OPTIONS verb, and for every response that the
     * server sends.  This only needs to be called once.
     *
     * @param origin The server that is allowed to send requests to this server
     * @param methods The allowed HTTP verbs from the above origin
     * @param headers The headers that can be sent with a request from the above
     *                origin
     */
    private static void enableCORS(String origin, String methods, String headers) {
        // Create an OPTIONS route that reports the allowed CORS headers and methods
        Spark.options("/*", (request, response) -> {
                String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
                if (accessControlRequestHeaders != null) {
                    response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
                }
                String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
                if (accessControlRequestMethod != null) {
                    response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
                }
                return "OK";
            });

        // 'before' is a decorator, which will run before any
        // get/post/put/delete.  In our case, it will put three extra CORS
        // headers into the response
        Spark.before((request, response) -> {
                response.header("Access-Control-Allow-Origin", origin);
                response.header("Access-Control-Request-Method", methods);
                response.header("Access-Control-Allow-Headers", headers);
            });
    }
}
