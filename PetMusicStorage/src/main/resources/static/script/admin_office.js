function showForm(formId) {
    const forms = document.querySelectorAll('#formContainer .form-submenu-personal-data');
    forms.forEach(form => form.style.display = 'none');
    document.getElementById(formId).style.display = 'block';
    document.querySelectorAll('.submenu-subscription, .submenu-users').forEach(submenu => submenu.style.display = 'none');
    document.getElementById('users-container').style.display = 'none';
}

// Функція для перемикання підменю
function toggleSubMenu(submenuId) {
    const submenu = document.getElementById(submenuId);
    const allSubmenus = document.querySelectorAll('.submenu-subscription, .submenu-users');
    allSubmenus.forEach(submenu => submenu.style.display = 'none');
    if (submenu) {
        submenu.style.display = 'block';
    }
}

// Відкриття підменю при натисканні на кнопки
document.querySelector('.button-container:nth-child(1) .button')?.addEventListener('click', function() {
    toggleSubMenu('users');
});
document.querySelector('.button-container:nth-child(2) .button')?.addEventListener('click', function() {
    toggleSubMenu('subscribe');
});

// Закриття підменю при натисканні поза блоком
document.addEventListener('click', function(event) {
    const target = event.target;
    const isClickInsideButton = target.closest('.button-container .button');
    const isClickInsideSubmenu = target.closest('.submenu-subscription, .submenu-users');
    const isClickInsideForm = target.closest('#formContainer, #users-container');
    if (!isClickInsideButton && !isClickInsideSubmenu && !isClickInsideForm) {
        const allSubmenus = document.querySelectorAll(
            '.submenu-subscription, .submenu-users, .form-submenu-personal-data, .form-submenu-personal-data-email, #users-container '
        );
        allSubmenus.forEach(submenu => submenu.style.display = 'none');
    }
});

function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
}

const usersPerPage = 5;
let currentPage = 1;
let totalUsers = 0;
let users = [];

// Оновлення елементів пагінації
function updatePaginationControls() {
    const prevButton = document.getElementById('prev-page');
    const nextButton = document.getElementById('next-page');
    const pageInfo = document.getElementById('page-info');
    prevButton.disabled = currentPage === 1;
    nextButton.disabled = currentPage * usersPerPage >= totalUsers;
    pageInfo.textContent = `Page ${currentPage}`;
}

function displayUsers() {
    const usersElement = document.getElementById('usersInfo');
    if (!usersElement) return;
    usersElement.innerHTML = '';
    const start = (currentPage - 1) * usersPerPage;
    const end = Math.min(start + usersPerPage, totalUsers);
    for (let i = start; i < end; i++) {
        const user = users[i];
        usersElement.innerHTML += `
            <li class="submenu-all-users-item">
                <div class="user-info">
                    <p class="submenu-all-users-phone">Phone: ${user.phoneNumber}</p>
                    <p class="submenu-all-users-subscription">Subscription: ${user.subscription.subscriptionName}</p>
                    <p class="submenu-all-users-email">Email: ${user.email}</p>
                </div>
                <button class="submenu-all-users-button" onclick="findUserByPhoneNumber(event, '${user.phoneNumber}')">Show details</button>
            </li>
            <hr class="dashed-line">
        `;
    }
    updatePaginationControls();
}



function changePage(direction) {
    const newPage = currentPage + direction;
    if (newPage >= 1 && newPage <= Math.ceil(totalUsers / usersPerPage)) {
        currentPage = newPage;
        displayUsers();
    }
}


function submitFindAllUsers() {
    const jwtToken = getCookie('JWT_TOKEN');
    if (!jwtToken) {
        console.error('JWT token not found');
        return;
    }
    fetch('/admin_office/users', {
        headers: {
            'Authorization': `Bearer ${jwtToken}`
        }
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(errorMessage => {
                throw new Error(errorMessage);
            });
        }
        return response.json();
    })
    .then(data => {
        users = data;
        totalUsers = users.length;
        currentPage = 1;
        displayUsers();
        document.querySelector('.submenu-users').style.display = 'none';
        document.getElementById('users-container').style.display = 'block';
    })
    .catch(error => {
        console.error('Failed to fetch user details: ', error);
        displayErrorMessage(error.message);
    });
}

function findUserByPhoneNumber(event, phoneNumber) {
    event.preventDefault();
    const usersElement = document.getElementById('usersInfo');
        if (usersElement) {
            usersElement.style.display = 'none';
        }
    const jwtToken = getCookie('JWT_TOKEN');
    if (!jwtToken) {
        console.error('JWT token not found');
        return;
    }
    fetch(`/admin_office/user_by_phone?phoneNumber=${phoneNumber}`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${jwtToken}`
        },
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        return response.json();
    })
    .then(user => {
        console.log('User details:', user);
        displayUserDetails(user);
    })
    .catch(error => {
        console.error('Error fetching user details:', error);
        alert('Failed to fetch user details.');
    });
}


function displayUserDetails(user) {
    const detailsElement = document.getElementById('userDetails');
    const usersElement = document.getElementById('usersInfo');
    const paginationControls = document.querySelector('.pagination-controls');
    const currentPhoneNumberElement = document.getElementById('currentPhoneNumber');
    const currentEmailElement = document.getElementById('currentEmail');
    const currentBankCardElement = document.getElementById('currentBankCardNumber');
    const currentSubscription = document.getElementById('currentSubscription');
    if (!detailsElement) {
        console.error('Element with id "userDetails" not found');
        return;
    }
    function formatDate(date) {
        const options = {
            day: '2-digit',
            month: 'short',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        };
        return date.toLocaleString('en-GB', options);
    }
    const [year, month, day, hour, minute, second, millisecond] = user.endTime;
    const date = new Date(year, month - 1, day, hour, minute, second, millisecond);
    detailsElement.className = 'user-details';
    detailsElement.innerHTML = `
        <p>Phone Number: ${user.phoneNumber} <button class="change-button" onclick="toggleForm('changeUsersPhoneNumber')">Change</button></p>
        <p>Password: hidden <button class="change-button" onclick="toggleForm('changeUsersPassword')">Change</button></p>
        <p>Auto Renew: ${user.autoRenew} <button class="change-button" onclick="toggleForm('changeUsersAutoRenewStatus')">Change</button></p>
        <p>Email: ${user.email} <button class="change-button" onclick="toggleForm('changeUserEmail')">Change</button></p>
        <p>Card Number: ${user.userBankCard.cardNumber} <button class="change-button" onclick="toggleForm('changeBankCardDataForm')">Change</button></p>
        <p>Subscription: ${user.subscription.subscriptionName} <button class="change-button" onclick="toggleForm('changeUsersSubscription')">Change</button></p>
        <p>Delete user: <button class="change-button" onclick="toggleForm('deleteUserByPhoneNumber')">Delete</button></p>
    `;
    detailsElement.style.display = 'block';
    if (usersElement) {
        usersElement.style.display = 'none';
    }
    if (paginationControls) {
        paginationControls.style.display = 'none';
    }
    if (currentPhoneNumberElement) {
        currentPhoneNumberElement.textContent = user.phoneNumber;
    }
    if (currentEmailElement) {
        currentEmailElement.textContent = user.email;
    }
    if (currentBankCardElement) {
        currentBankCardElement.textContent = user.userBankCard.cardNumber;
    }
    if (currentSubscription) {
        currentSubscription.textContent = user.subscription.subscriptionName;
    }
    if (currentSubscription) {
        currentSubscription.style.display = 'block';
        submitFindAlSubscriptions();
    }

}


function toggleForm(formId) {
    const formContainer = document.getElementById('usersPersonalDataContainer');
    const forms = formContainer.getElementsByTagName('form');
    if (formContainer.style.display === 'none') {
        formContainer.style.display = 'block';
    }
    Array.from(forms).forEach(form => {
        if (form.id === formId) {
            form.style.display = form.style.display === 'none' ? 'block' : 'none';
        } else {
            form.style.display = 'none';
        }
    });
    if (formContainer.querySelector(`#${formId}`).style.display === 'none') {
        formContainer.style.display = 'none';
    }
}

function displayErrorMessage(message) {
    const usersElement = document.getElementById('usersInfo');
    if (!usersElement) return;
    usersElement.innerHTML = `
        <p class="display-error-message">${message}</p>
    `;
}

function updateUsersPhoneNumber(event) {
    event.preventDefault();
    const currentPhoneNumber = document.getElementById('currentPhoneNumber').textContent;
    const newPhoneNumber = document.getElementById('newPhoneNumber').value;
    if (!newPhoneNumber || !currentPhoneNumber) {
        alert('Please fill in all fields.');
        return;
    }
    const jwtToken = getCookie('JWT_TOKEN');
    if (!jwtToken) {
        console.error('JWT token not found');
        return;
    }
    const payload = {
        currentPhoneNumber: parseInt(currentPhoneNumber, 10),
        newPhoneNumber: parseInt(newPhoneNumber, 10)
    };
    fetch('/admin_office/update_phone_number', {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${jwtToken}`
        },
        body: JSON.stringify(payload)
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => {
                throw new Error(text);
            });
        }
        return response.text();
    })
    .then(data => {
        console.log('Server response:', data);
        displayMessageForPersonalDataUpdate(data, 'success');
    })
    .catch(error => {
        console.error('Error updating phone number:', error);
        displayErrorMessageForPersonalDataUpdate(error.message);
    });
}

function updateUsersPassword(event) {
    event.preventDefault();
    const newPassword = document.getElementById('newPassword').value;
    if (!newPassword) {
        alert('Please enter a new password.');
        return;
    }
    const userPhoneNumber = parseInt(document.getElementById('currentPhoneNumber').textContent, 10);
    if (isNaN(userPhoneNumber)) {
        alert('Invalid phone number.');
        return;
    }
    const jwtToken = getCookie('JWT_TOKEN');
    if (!jwtToken) {
        console.error('JWT token not found');
        return;
    }
    const payload = {
        userPhoneNumber: userPhoneNumber,
        newPassword: newPassword
    };
    fetch('/admin_office/update_password', {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${jwtToken}`
        },
        body: JSON.stringify(payload)
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => {
                throw new Error(text);
            });
        }
        return response.text();
    })
    .then(data => {
        console.log('Server response:', data);
        displayMessageForPersonalDataUpdate(data, 'success');
    })
    .catch(error => {
        console.error('Error updating password:', error);
        displayErrorMessageForPersonalDataUpdate(error.message);
    });
}

function updateUsersAutoRenewStatus(event) {
    const autoRenewStatus = event.target.value;
const userPhoneNumber = parseInt(document.getElementById('currentPhoneNumber').textContent, 10);
    if (isNaN(userPhoneNumber)) {
        alert('Invalid phone number.');
        return;
    }
    const jwtToken = getCookie('JWT_TOKEN');
    if (!jwtToken) {
        console.error('JWT token not found');
        return;
    }
    const payload = {
        userPhoneNumber: userPhoneNumber,
        autoRenewStatus: autoRenewStatus
    };
    fetch('/admin_office/set_auto_renew', {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${jwtToken}`
        },
        body: JSON.stringify(payload)
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => {
                throw new Error(text);
            });
        }
        return response.text();
    })
    .then(data => {
        console.log('Server response:', data);
        displayMessageForPersonalDataUpdate(data, 'success');
    })
    .catch(error => {
        console.error('Error updating auto-renew status:', error);
        displayErrorMessageForPersonalDataUpdate(error.message);
    });
}

function updateUsersEmail(event) {
    event.preventDefault();
    const newEmail = document.getElementById('newEmail').value;
    const userPhoneNumber = parseInt(document.getElementById('currentPhoneNumber').textContent, 10);
    if (isNaN(userPhoneNumber)) {
        alert('Invalid phone number.');
        return;
    }
    const jwtToken = getCookie('JWT_TOKEN');
    if (!jwtToken) {
        console.error('JWT token not found');
        return;
    }
    const payload = {
        userPhoneNumber: userPhoneNumber,
        newEmail: newEmail
    };
    fetch('/admin_office/update_email', {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${jwtToken}`
        },
        body: JSON.stringify(payload)
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => {
                throw new Error(text);
            });
        }
        return response.text();
    })
    .then(data => {
        console.log('Server response:', data);
        displayMessageForPersonalDataUpdate(data, 'success');
    })
    .catch(error => {
        console.error('Error updating password:', error);
        displayErrorMessageForPersonalDataUpdate(error.message);
    });
}

function updateUsersBankCard(event) {
    event.preventDefault();
    const newBankCardNumber = document.getElementById('newBankCardNumber').value;
    const newBankCardExpirationDate = document.getElementById('newBankCardExpirationDate').value;
    const newBankCardCVV = document.getElementById('newBankCardCVV').value;
    const userPhoneNumber = parseInt(document.getElementById('currentPhoneNumber').textContent, 10);
    if (isNaN(userPhoneNumber)) {
        alert('Invalid phone number.');
        return;
    }
    const jwtToken = getCookie('JWT_TOKEN');
    if (!jwtToken) {
        console.error('JWT token not found');
        return;
    }
     const payload = {
            userPhoneNumber: userPhoneNumber,
            newUserBankCard: {
                cardNumber: newBankCardNumber,
                cvv: newBankCardCVV,
                cardExpirationDate: newBankCardExpirationDate
            }
        };

    fetch('/admin_office/update_bank_card', {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${jwtToken}`
        },
        body: JSON.stringify(payload)
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => {
                throw new Error(text);
            });
        }
        return response.text();
    })
    .then(data => {
        console.log('Server response:', data);
        displayMessageForPersonalDataUpdate(data, 'success');
    })
    .catch(error => {
        console.error('Error updating bank card:', error);
        displayErrorMessageForPersonalDataUpdate(error.message);
    });
}


function displayErrorMessageForPersonalDataUpdate(message) {
    const submenuPersonalData = document.getElementById('usersPersonalDataContainer');
    submenuPersonalData.classList.add('submenu-personal-data-error');
    submenuPersonalData.innerHTML = `
        <p class="display-error-message-for-personal-data-update">${message}</p>
    `;
    submenuPersonalData.style.display = 'block';
}


function displayMessageForPersonalDataUpdate(message) {
    const submenuPersonalData = document.getElementById('usersPersonalDataContainer');
     submenuPersonalData.classList.add('submenu-personal-data-error');
    submenuPersonalData.innerHTML = `
        <p class="display-message-for-personal-data-update">${message}</p>
    `;

}

function submitFindAlSubscriptions() {
    const jwtToken = getCookie('JWT_TOKEN');
    if (!jwtToken) {
        console.error('JWT token not found');
        return;
    }
    fetch('/admin_office/subscriptions', {
        headers: {
            'Authorization': `Bearer ${jwtToken}`
        }
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(errorMessage => {
                throw new Error(errorMessage);
            });
        }
        return response.json();
    })
    .then(data => {
        subscriptions = data;
        totalSubscriptions = subscriptions.length;
        currentPage = 1;
        displaySubscriptions();
    })
    .catch(error => {
        console.error('Failed to fetch subscriptions: ', error);
        displayErrorMessage(error.message);
    });
}


const subscriptionsPerPage = 2;  // Кількість підписок на сторінку
let currentSubscriptionPage = 1;  // Поточна сторінка підписок
let totalSubscriptions = 0;  // Загальна кількість підписок
let subscriptions = [];  // Масив підписок

// Оновлення елементів пагінації
function updateSubscriptionPaginationControls() {
    const prevButton = document.getElementById('sub-prev-page');
    const nextButton = document.getElementById('sub-next-page');
    const pageInfo = document.getElementById('sub-page-info');
    prevButton.disabled = currentSubscriptionPage === 1;
    nextButton.disabled = currentSubscriptionPage * subscriptionsPerPage >= totalSubscriptions;
    pageInfo.textContent = `Page ${currentSubscriptionPage}`;
}

function displaySubscriptions() {
    const subscriptionsElement = document.getElementById('subscriptionInfo');
    if (!subscriptionsElement) return;
    subscriptionsElement.innerHTML = '';

    const start = (currentSubscriptionPage - 1) * subscriptionsPerPage;
    const end = Math.min(start + subscriptionsPerPage, totalSubscriptions);

    for (let i = start; i < end; i++) {
        const subscribe = subscriptions[i];
        subscriptionsElement.innerHTML += `
            <li class="subscription-item">
                <p class="subscription-name">Subscription name: <strong>${subscribe.subscriptionName}</strong></p>
                <p class="subscription-price">Subscription price: ${subscribe.subscriptionPrice}</p>
                <p class="subscription-duration">Duration time: ${subscribe.subscriptionDurationTime}</p>
                <button class="subscribe-button" onclick="updateUsersSubscription('${subscribe.subscriptionName}')">Subscribe</button>
            </li>
            <hr class="dashed-line">
        `;
    }

    updateSubscriptionPaginationControls();
}

function changeSubscriptionPage(direction) {
    const newPage = currentSubscriptionPage + direction;
    if (newPage >= 1 && newPage <= Math.ceil(totalSubscriptions / subscriptionsPerPage)) {
        currentSubscriptionPage = newPage;
        displaySubscriptions();
    }
}


 function updateUsersSubscription(event) {
     const userPhoneNumber = parseInt(document.getElementById('currentPhoneNumber').textContent, 10);
     if (isNaN(userPhoneNumber)) {
         alert('Invalid phone number.');
         return;
     }
     const jwtToken = getCookie('JWT_TOKEN');
     if (!jwtToken) {
         console.error('JWT token not found');
         return;
     }
      const payload = {
             userPhoneNumber: userPhoneNumber,
             newSubscription: {
                 subscriptionName: event,
             }
         };

     fetch('/admin_office/update_subscription', {
         method: 'PUT',
         headers: {
             'Content-Type': 'application/json',
             'Authorization': `Bearer ${jwtToken}`
         },
         body: JSON.stringify(payload)
     })
     .then(response => {
         if (!response.ok) {
             return response.text().then(text => {
                 throw new Error(text);
             });
         }
         return response.text();
     })
     .then(data => {
         console.log('Server response:', data);
         displayMessageForPersonalDataUpdate(data, 'success');
     })
     .catch(error => {
         console.error('Error updating bank card:', error);
         displayErrorMessageForPersonalDataUpdate(error.message);
     });
 }

function deleteUserByPhoneNumber(event) {
    event.preventDefault(); // Зупиняє стандартну поведінку форми

    const currentPhoneNumber = document.getElementById('currentPhoneNumber').textContent.trim();
    if (!currentPhoneNumber) {
        alert('Current phone number is missing.');
        return;
    }

    const jwtToken = getCookie('JWT_TOKEN');
    if (!jwtToken) {
        console.error('JWT token not found');
        return;
    }

    fetch(`/admin_office/delete_user_by_phone_number?phoneNumber=${encodeURIComponent(currentPhoneNumber)}`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${jwtToken}`
        }
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => {
                throw new Error(text);
            });
        }
        return response.text();
    })
    .then(data => {
        console.log('Server response:', data);
        displayMessageForPersonalDataUpdate(data, 'success');
    })
    .catch(error => {
        console.error('Error deleting user:', error);
        displayErrorMessageForPersonalDataUpdate(error.message);
    });
}

//function findUserByPhoneNumber(event, phoneNumber) {
//    event.preventDefault();
//    const usersElement = document.getElementById('usersInfo');
//        if (usersElement) {
//            usersElement.style.display = 'none';
//        }
//    const jwtToken = getCookie('JWT_TOKEN');
//    if (!jwtToken) {
//        console.error('JWT token not found');
//        return;
//    }
//    fetch(`/admin_office/user_by_phone?phoneNumber=${phoneNumber}`, {
//        method: 'GET',
//        headers: {
//            'Authorization': `Bearer ${jwtToken}`
//        },
//    })
//    .then(response => {
//        if (!response.ok) {
//            throw new Error(`HTTP error! Status: ${response.status}`);
//        }
//        return response.json();
//    })
//    .then(user => {
//        console.log('User details:', user);
//        displayUserDetails(user);
//    })
//    .catch(error => {
//        console.error('Error fetching user details:', error);
//        alert('Failed to fetch user details.');
//    });
//}
