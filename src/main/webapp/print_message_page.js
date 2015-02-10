(function ($) {
    $(document).ready(function() {
        $("#adressat").hide();
        $("#returAdresse").hide();

        $("#typeNorsk").click(function() {
            $("#adressat").show();
            visNorskFelt("");
        });

        $("#typeUtenlandsk").click(function() {
            $("#adressat").show();
            visUtenlandskFelt("");
        });

        $("#typeReturNorsk").click(function() {
            $("#returAdresse").show();
            visNorskFelt("Retur");
        });

        $("#typeReturUtenlandsk").click(function() {
            $("#returAdresse").show();
            visUtenlandskFelt("Retur");
        });

    });

    function visNorskFelt(retur) {
        $("#land" + retur + "Felt").hide();
        $("#landkode" + retur + "Felt").hide();
        $("#adresse4" + retur + "Felt").hide();
        $("#postnummer" + retur + "Felt").show();
        $("#poststed" + retur + "Felt").show();
    }
    function visUtenlandskFelt(retur) {
        $("#land" + retur + "Felt").show();
        $("#landkode" + retur + "Felt").show();
        $("#adresse4" + retur + "Felt").show();
        $("#postnummer" + retur + "Felt").hide();
        $("#poststed" + retur + "Felt").hide();
    }

}(jQuery));