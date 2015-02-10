$(document).ready(function() {
    $("#keyPairAlias").change(function() {
        var keyPairAlias = $("#keyPairAlias").val();
        var orgNumber = keyPairAlias.substring(0, 9);
        $("#senderOrgNumber").val(orgNumber);
    });
    $("#attachments").change(function() {
        refreshAttachmentsTitleDialog();
    });
    refreshAttachmentsTitleDialog();
});

var refreshAttachmentsTitleDialog = function() {
    var attachments = $("#attachments")[0].files;
    var domPanel = $("#attachmentTitles");
    if (attachments.length == 0) {
        domPanel.hide();
        return;
    }
    domPanel.show();
    var domPanelBody = $("#attachmentTitles .panel-body");
    domPanelBody.html("");
    for (var i = 0; i < attachments.length; i++) {
        var attachment = attachments[i];
        var domFormLabel = $("<label>");
        domFormLabel.attr("class", "col-sm-2 control-label");
        domFormLabel.attr("for", domFormTextInputId);
        domFormLabel.text(attachment.name);
        var domFormHiddenInputId = "attachmentName" + i;
        var domFormHiddenInput = $("<input>");
        domFormHiddenInput.attr("id", domFormHiddenInputId);
        domFormHiddenInput.attr("name", domFormHiddenInputId);
        domFormHiddenInput.attr("type", "hidden");
        domFormHiddenInput.val(attachment.name);
        var domFormTextInputId = "attachmentTitle" + i;
        var domFormTextInput = $("<input>");
        domFormTextInput.attr("id", domFormTextInputId);
        domFormTextInput.attr("name", domFormTextInputId);
        domFormTextInput.attr("class", "form-control");
        domFormTextInput.attr("type", "text");
        domFormTextInput.attr("autocomplete", "off");
        domFormTextInput.attr("placeholder", attachment.name);
        domFormTextInput.val(attachment.name);
        var domFormInputDiv = $("<div>");
        domFormInputDiv.attr("class", "col-sm-10");
        domFormInputDiv.append(domFormHiddenInput);
        domFormInputDiv.append(domFormTextInput);
        var domFormGroup = $("<div>");
        domFormGroup.attr("class", "form-group");
        domFormGroup.append(domFormLabel);
        domFormGroup.append(domFormInputDiv);
        domPanelBody.append(domFormGroup);
    }
};
