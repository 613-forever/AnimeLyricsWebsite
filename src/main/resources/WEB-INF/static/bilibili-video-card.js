$(document).ready(function() {
    $(".bilibili-toggle").click(function() {
        if ($(this).hasClass("active")) {
            $(this).removeClass("active").text("展开");
            $(this).closest(".bilibili-video-container").children(".video-frame")
                .slideUp(function () {
                    $(this).attr("src", $(this).attr("src"));
                });
        } else {
            $(this).addClass("active").text("收起");
            $(this).closest(".bilibili-video-container").children(".video-frame")
                .attr("src", unescape($(this).attr("data-alm-src")))
                .slideDown();
        }
    });
});