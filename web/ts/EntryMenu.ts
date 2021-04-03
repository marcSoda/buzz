class EntryMenu {
    /**
     * Track if the Singleton has been initialized
     */
    private static isInit = false;

    /**
     * The name of the DOM entry associated with EntryMenu
     */
    private static readonly NAME = "EntryMenu";

    private static init() {
	//close entry menu if a click event happens outside the menu or the menu icon
	$(document).click(function() {
	    var entryMenu = $("#EntryMenu");
	    var menuDivs = $('.ElementList-menu-div');
	    if (!entryMeny.is(event.target) && !entryMeny.has(event.target).length) {
		if (!menuDivs.is(event.target) && !menuDivs.has(event.target).length) {
		    EntryMenu.close();
		}
	    }
	});

	EntryMenu.isInit = true;
    }

    public static spawn(id) {
	if (!EntryMenu.isInit) EntryMenu.init();
	EntryMenu.close();
	//change position of menu to be where the menu icon of the corresponding element
	let position = $("." + ElementList.NAME + "-menu-div[data-value=\"" + id + "\"]").position();
	//add EntryMenu template
	$("body").prepend(Handlebars.templates[EntryMenu.NAME + ".hb"]({id: id,
									left: position.left,
									top: position.top,
									position: "absolute"}));
	//Set click events
	$("#" + EntryMenu.NAME + "-delete").click(EntryMenu.clickDelete);
	$("#" + EntryMenu.NAME + "-edit").click(EntryMenu.clickEdit);

    }

    /**
    * clickDelete is the code we run in response to a click of a delete button
    */
    private static clickDelete() {
	// for now, just print the ID that goes along with the data in the row
	// whose "delete" button was clicked
	let id = $(this).data("value");
	$.ajax({
	    type: "DELETE",
	    url: backendUrl + "/messages/" + id,
	    dataType: "json",
	    // TODO: we should really have a function that looks at the return
	    //       value and possibly prints an error message.
	    success: function() {
		ElementList.refresh();
		EntryMenu.close();
	    }
	});
    }

    /**
    * clickEdit is the code we run in response to a click of a delete button
    */
    private static clickEdit() {
	let id = $(this).data("value");
	EditEntryForm.spawn(id);
    }


    public static close() {
	$("#" + EntryMenu.NAME).remove()
    }
}
