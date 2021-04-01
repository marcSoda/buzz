class UserPage {

    private static readonly NAME = "UserPage";

    /**
     * Track if the Singleton has been initialized
     */
    private static isInit = false

    //prepare the userPage
    private static init(id) {
	UserPage.close();
	UserPage.getUserData(id);
	UserPage.isInit = true;
    }

    /**
     * Refresh() doesn't really have much meaning, but just like in sNavbar, we
     * have a refresh() method so that we don't have front-end code calling
     * init().
     */
    public static spawn(id) {
        UserPage.init(id);
    }

    //initialize handlebars template with user data from the database
    public static fill(data: any) {
	$("body").append(Handlebars.templates[UserPage.NAME + ".hb"]({id: data.id,
								      email: data.email,
								      bio: data.bio}));
	$("#" + UserPage.NAME + "-OK").click(UserPage.submitForm);
	$("#" + UserPage.NAME + "-Close").click(UserPage.close);
        $("#" + UserPage.NAME).modal("show");
    }

    //get user data from the database.
    public static getUserData(id) {
	$.ajax({
	    type: "GET",
            url: backendUrl + "user/" + id,
	    dataType: "json",
	    success: UserPage.fill
	});
    }

    //close the form
    public static close() {
	//remove the form from the dom. unlike NewEntryForm, it is removed and added when needed
	$('.modal-backdrop').remove();
	$('body').removeClass("modal-open");
	$("#" + UserPage.NAME).remove();
    }

    /**
     * Send data to submit the form only if the fields are both valid.
     * Immediately hide the form when we send data, so that the user knows that
     * their click was received.
     */
    private static submitForm() {
        // get the values of the two fields, force them to be strings, and check
        // that neither is empty
        let email = "" + $("#" + UserPage.NAME + "-email").val();
        let bio = "" + $("#" + UserPage.NAME + "-bio").val();
        if (email === "" || bio === "") {
            window.alert("Error: field(s) invalid");
            return;
        }
        UserPage.close();
        // set up an AJAX post.  When the server replies, the result will go to
        // onSubmitResponse
	let id = $(this).data("value");
        $.ajax({
            type: "PUT",
            url: backendUrl + "/user/" + id,
            dataType: "json",
            data: JSON.stringify({ mTitle: email, mMessage: bio }),
            success: UserPage.onSubmitResponse
        });
    }

    /**
     * onSubmitResponse runs when the AJAX call in submitForm() returns a
     * result.
     *
     * @param data The object returned by the server
     */
    private static onSubmitResponse(data: any) {
        // If we get an "ok" message, clear the form and refresh the main
        // listing of messages
        if (data.mStatus === "ok") {
            ElementList.refresh();
        }
        // Handle explicit errors with a detailed popup message
        else if (data.mStatus === "error") {
            window.alert("The server replied with an error:\n" + data.mMessage);
        }
        // Handle other errors with a less-detailed popup message
        else {
            window.alert("Unspecified error");
        }
    }
}
