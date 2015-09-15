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
        var domFormHiddenInputId = "attachmentName" + i;
        var domFormTextInputId = "attachmentTitle" + i;
        var domFormTextMimetypeInputId = "attachmentMimetype" + i;
        var domFormInputDiv = $("<div>");
        domFormInputDiv.attr("class", "col-sm-10");
        domFormInputDiv.append(createHiddenInput(domFormHiddenInputId, attachment));
        domFormInputDiv.append(createTextInput(domFormTextInputId, attachment.name, "Filenavn"));
        domFormInputDiv.append(createTextInput(domFormTextMimetypeInputId, attachment.type, "Mimetype"));
        var domFormGroup = $("<div>");
        domFormGroup.attr("class", "form-group");
        domFormGroup.append(createLabel(domFormTextInputId, attachment.name));
        domFormGroup.append(domFormInputDiv);
        domPanelBody.append(domFormGroup);
    }

    function createLabel(domFormTextInputId, name) {
        var domFormLabel = $("<label>");
        domFormLabel.attr("class", "col-sm-2 control-label");
        domFormLabel.attr("for", domFormTextInputId);
        domFormLabel.text(name);
        return domFormLabel;
    }

    function createHiddenInput(domFormHiddenInputId, attachment) {
        var domFormHiddenInput = $("<input>");
        domFormHiddenInput.attr("id", domFormHiddenInputId);
        domFormHiddenInput.attr("name", domFormHiddenInputId);
        domFormHiddenInput.attr("type", "hidden");
        domFormHiddenInput.val(attachment.name);
        return domFormHiddenInput;
    }
    function createTextInput(domFormTextInputId, value, title) {
        var domFormTextInput = $("<input>");
        domFormTextInput.attr("id", domFormTextInputId);
        domFormTextInput.attr("name", domFormTextInputId);
        domFormTextInput.attr("class", "form-control");
        domFormTextInput.attr("type", "text");
        domFormTextInput.attr("autocomplete", "off");
        domFormTextInput.attr("title", title);
        if(value !== '') {
            domFormTextInput.attr("placeholder", value);
        }else{
            domFormTextInput.attr("placeholder", title+" ukjent");

        }
        domFormTextInput.val(value);
        return domFormTextInput;
    }
};
