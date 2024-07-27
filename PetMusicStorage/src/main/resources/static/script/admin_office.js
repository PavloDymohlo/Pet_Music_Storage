function showForm(formId) {
    const forms = document.querySelectorAll('#formContainer .form-submenu-personal-data');
    forms.forEach(form => form.style.display = 'none');
    document.getElementById(formId).style.display = 'block';
    document.querySelectorAll('.submenu-subscription, .submenu-personal-data').forEach(submenu => submenu.style.display = 'none');
}

const subscribe = document.getElementById("subscribe");
const users = document.getElementById("users");

function toggleSubMenu(submenuId) {
    const submenu = document.getElementById(submenuId);
    const allSubmenus = document.querySelectorAll('.submenu-subscription, .submenu-users');
    allSubmenus.forEach(submenu => submenu.style.display = 'none');
    submenu.style.display = 'block';
}

// Open submenu
document.querySelector('.button-container:nth-child(1) .button').addEventListener('click', function(event) {
    toggleSubMenu('users');
});
document.querySelector('.button-container:nth-child(2) .button').addEventListener('click', function(event) {
    toggleSubMenu('subscribe');
});

// Close submenu when clicking outside the block
document.addEventListener('click', function(event) {
    const target = event.target;
    const isClickInsideButton = target.closest('.button-container .button');
    const isClickInsideSubmenu = target.closest('.submenu-subscription, .submenu-users');
    const isClickInsideForm = target.closest('#formContainer .submenu-users');
    if (!isClickInsideButton && !isClickInsideSubmenu && !isClickInsideForm) {
        const allSubmenus = document.querySelectorAll(
        '.submenu-subscription,.submenu-users,.form-submenu-personal-data,.form-submenu-personal-data-email');
        allSubmenus.forEach(submenu => submenu.style.display = 'none');
    }
});