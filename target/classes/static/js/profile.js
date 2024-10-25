// fct pt a ascunde/;afisa sectiunile
function toggleSection(sectionId, buttonId) {
    const section = document.getElementById(sectionId);
    const button = document.getElementById(buttonId);
    if (section.style.display === "none") {
        section.style.display = "block";
        button.textContent = "Hide " + button.dataset.section;
    } else {
        section.style.display = "none";
        button.textContent = "Show " + button.dataset.section;
    }
}

// pt edit bio
function editBio() {
    const bioSection = document.getElementById('bioSection');
    const bioForm = document.getElementById('bioForm');
    bioSection.style.display = 'none';
    bioForm.style.display = 'block';
}

// confirmarea stergerii cont
function confirmDeleteAccount(event) {
    const confirmation = confirm("Are you sure you want to delete your account? This action cannot be undone.");
    if (!confirmation) {
        event.preventDefault();
    }
}
