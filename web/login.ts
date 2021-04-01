/// This constant indicates the path to our backend server
const backendUrl = "https://runtime-tremor.herokuapp.com";

function onSignIn(googleUser: any) {
    var profile = googleUser.getBasicProfile();
    var id_token = googleUser.getAuthResponse().id_token;
    console.log("LOGGING IN ");
    $.ajax({
	type: "POST",
	url: backendUrl + "/auth",
	dataType: "json",
	data: JSON.stringify({ mId_token: id_token }),
	success: function(resp: any) {
	    localStorage.setItem("uid", resp.mData[0]);
	    localStorage.setItem("sessionKey", resp.mData[1]);
	    $(location).attr('href',"/");
	},
	error: function() {
	    alert("auth error");
	}
    });
}
