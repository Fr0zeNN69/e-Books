// Toggle pentru meniul lateral
const menuButton = document.getElementById('menu-button');
const sideMenu = document.getElementById('side-menu');
const container = document.querySelector('.container'); // Secțiunea cu cărțile

if (menuButton && sideMenu) {
    menuButton.addEventListener('click', function (e) {
        e.stopPropagation();
        console.log("Menu button clicked");
        sideMenu.classList.toggle('open');
        if (container) {
            container.classList.toggle('menu-open');
        } else {
            console.error("Container not found");
        }
    });

    // Închidere meniul când faci clic în afara meniului
    document.addEventListener('click', function (event) {
        if (!sideMenu.contains(event.target) && !menuButton.contains(event.target)) {
            console.log("Clicked outside, closing menu");
            sideMenu.classList.remove('open');
            if (container) {
                container.classList.remove('menu-open');
            }
        }
    });
} else {
    console.error("Menu button or side menu not found");
}

// Pentru meniul de utilizator (username)
const userButton = document.getElementById('userButton');
const userDropdown = document.getElementById('userDropdown');

if (userButton && userDropdown) {
    userButton.addEventListener('click', function (event) {
        event.stopPropagation();
        console.log("User menu clicked");
        userDropdown.classList.toggle('active');
    });

    // Ascundere dropdown când clickezi în afara butonului username
    document.addEventListener('click', function(event) {
        if (!userButton.contains(event.target) && !userDropdown.contains(event.target)) {
            userDropdown.classList.remove('active');
        }
    });
} else {
    console.error("User button or dropdown not found");
}

// Pentru lupa de căutare
const searchIcon = document.getElementById('searchIcon');
const searchDropdown = document.querySelector('.search-dropdown');

if (searchIcon && searchDropdown) {
    searchIcon.addEventListener('click', function (event) {
        event.stopPropagation(); // Stop propagarea pentru a nu închide meniul imediat
        console.log("Search icon clicked");
        searchDropdown.classList.toggle('active');
    });

    // Ascundere doar dacă se face click în afara căsuței de căutare și iconiței
    document.addEventListener('click', function (event) {
        if (!searchDropdown.contains(event.target) && !searchIcon.contains(event.target)) {
            searchDropdown.classList.remove('active');
        }
    });
} else {
    console.error("Search icon or dropdown not found");
}
