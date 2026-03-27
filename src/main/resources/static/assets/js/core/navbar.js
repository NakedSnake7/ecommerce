 document.addEventListener("scroll", () => {
    const navbar = document.querySelector(".custom-navbar");
    navbar.classList.toggle("affix", window.scrollY > 50);
});
