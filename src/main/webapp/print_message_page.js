
(function ($) {
    var adressatId = '#adressat';
    var returadresseId = '#returAdresse';
    $(document).ready(function() {

        handleAdressatType($);
        handleReturadresseType($);

        $("#typeNorsk").click(function() {
            $(adressatId).show();
            visNorskFelt("");
        });

        $("#typeUtenlandsk").click(function() {
            $(adressatId).show();
            visUtenlandskFelt("");
        });

        $("#typeReturNorsk").click(function() {
            $(returadresseId).show();
            visNorskFelt("Retur");
        });

        $("#typeReturUtenlandsk").click(function() {
            $(returadresseId).show();
            visUtenlandskFelt("Retur");
        });

    });

    function handleAdressatType($) {
        if ($('#typeNorsk').is(':checked')) {
            visNorskFelt("");
        } else if ($('#typeUtenlandsk').is(':checked')) {
            visUtenlandskFelt("");
        } else {
            $(adressatId).hide();
        }
    }

    function handleReturadresseType($) {
        if ($('#typeReturNorsk').is(':checked')) {
            visNorskFelt("Retur");
        } else if ($('#typeReturUtenlandsk').is(':checked')) {
            visUtenlandskFelt("Retur");
        } else {
            $(returadresseId).hide();
        }
    }

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