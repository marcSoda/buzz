class ElementList {
    /**
     * The name of the DOM entry associated with ElementList
     */
    private static readonly NAME = "ElementList";

    /**
     * Track if the Singleton has been initialized
     */
    private static isInit = false;

    /**
    * Initialize the ElementList singleton.
    * This needs to be called from any public static method, to ensure that the
    * Singleton is initialized before use.
    */
    private static init() {
	if (!ElementList.isInit) {
	    ElementList.isInit = true;
	}
    }

    /**
    * update() is the private method used by refresh() to update the
    * ElementList
    */
    private static update(data: any) {
	// Remove the table of data, if it exists
	$("#" + ElementList.NAME).remove();
	// Use a template to re-generate the table, and then insert it
	$("body").append(Handlebars.templates[ElementList.NAME + ".hb"](data));
	$("." + ElementList.NAME + "-menu-div").click(ElementList.clickMenu);
	// Find all of the Upvote buttons, and set their behavior
	$("." + ElementList.NAME + "-upvote-div").click(ElementList.clickUpvote);
	// Find all of the Downvote buttons, and set their behavior
	$("." + ElementList.NAME + "-downvote-div").click(ElementList.clickDownvote);
	$("." + ElementList.NAME + "-comment-div").click(ElementList.clickComment);
	$("." + ElementList.NAME + "-userName").click(ElementList.clickUser);
    }

    /**
    * refresh() is the public method for updating the ElementList
    */
    public static refresh() {
        // Make sure the singleton is initialized
        ElementList.init();
        // Issue a GET, and then pass the result to update()
        $.ajax({
            type: "GET",
            url: backendUrl + "/messages",
            headers: {
                'custom_header': 'hello',
                'uid': localStorage.getItem('uid'),
                'sessionKey': localStorage.getItem('sessionKey')
            },
            dataType: "json",
            success: ElementList.update,
            error: function() { window.location = "/login"; },
        });
    }

    //entry menu spawns
    private static clickMenu() {
	let id = $(this).data("value");
	EntryMenu.spawn(id);
	return false;
    }

    //user page spawns
    private static clickUser() {
	let uid = $(this).data("value");
	UserPage.spawn(uid);
    }

    /**
    * clickUpvote is the code we run in response to a click of a like button
    */
    private static clickUpvote() {
	let id = $(this).data("value");
	let likes = $(this).children().last();
	$.ajax({
	    type: "POST",
	    url: backendUrl + "/messages/" + id + "/upvote",
	    dataType: "json",
	    //Increment the number of likes ONLY if a successful response is received
	    success: function(resp: any) {
		if (resp.mStatus === 'ok') {
		    likes.html(parseInt(likes.html()) + 1)
		} else { alert("upvote fail") }
	    },
	    error: function() {
		alert("upvote fail");
	    }
	});
    }

    /**
    * clickDownvote is the code we run in response to a click of a dislike button
    */
    private static clickDownvote() {
	let id = $(this).data("value");
	let dislikes = $(this).children().last();
	$.ajax({
	    type: "POST",
	    url: backendUrl + "/messages/" + id + "/downvote",
	    dataType: "json",
	    //Increment the number of dislikes ONLY if a successful response is received
	    success: function(resp: any) {
		if (resp.mStatus === 'ok') {
		    dislikes.html(parseInt(dislikes.html()) + 1)
		} else { alert("downvote fail") }
	    },
	    error: function() {
		alert("downvote fail");
	    }
	});
    }

    //comment page spawns
    private static clickComment() {
	let id = $(this).data("value");
	CommentList.spawn(id);
    }
}
