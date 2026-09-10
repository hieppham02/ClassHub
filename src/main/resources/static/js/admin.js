document.addEventListener("DOMContentLoaded", function () {

    const menuButton = document.querySelector(".mobile-menu");
    const sidebar = document.querySelector(".sidebar");

    if (menuButton) {
        menuButton.addEventListener("click", function () {
            sidebar.classList.toggle("show");
        });
    }

});