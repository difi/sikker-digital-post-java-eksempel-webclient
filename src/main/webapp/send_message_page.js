$(document).ready(function() {
	var refreshContactDetailsDialog = function() {
		if ($("#retrieveContactDetails").is(':checked')) {
			$("#contactDetails").hide();
		} else {
			$("#contactDetails").show();
		}
	};

	$("#retrieveContactDetails").click(function() {
		refreshContactDetailsDialog();
	});

	// Initializes the send message page
	refreshContactDetailsDialog();
});