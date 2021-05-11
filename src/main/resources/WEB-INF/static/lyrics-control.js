$(document).ready(function () {
    $(".aml-lyrics-control").click(function () {
        let name = $(this).attr("data-alm-type");
        if ($(this).hasClass("active")) {
            $("." + name + "-text-line").slideUp();
        } else {
            $("." + name + "-text-line").slideDown({start: function () { $(this).css({display: "block"}) }},
                function() { $(this).css({display: "block"}); });
        }
        $(this).closest(".lyrics").children(".aml-lyrics-control-group")
            .children("[data-alm-type=\"" + name + "\"]").toggleClass("active");
    });
    $("[data-alm-position]").mouseenter(function () {
        let word_position = $(this).attr("data-alm-position");
        $("[data-alm-position=\"" + word_position + "\"]").addClass("hover-word");
    }).mouseleave(function () {
        let word_position = $(this).attr("data-alm-position");
        $("[data-alm-position=\"" + word_position + "\"]").removeClass("hover-word");
    });
});
