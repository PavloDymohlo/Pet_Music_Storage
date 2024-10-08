function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
}

function closeAllMenus() {
    const menus = ['findAllSubmenu', 'changePersonalData', 'logOut', 'manualFindUserByPhoneNumber'];
    menus.forEach(menuId => {
        const menu = document.getElementById(menuId);
        if (menu) {
            menu.style.display = 'none';
        }
    });
}

function toggleMenu(menuId) {
    const menu = document.getElementById(menuId);
    const isCurrentlyOpen = menu.style.display === 'block';
    closeAllMenus();
    if (!isCurrentlyOpen) {
        menu.style.display = 'block';
    }
}

document.getElementById('PersonalDataButton').addEventListener('click', function() {
    toggleMenu('changePersonalData');
});

document.getElementById('LogOutButton').addEventListener('click', function() {
    toggleMenu('logOut');
});

document.getElementById('SubscriptionButton').addEventListener('click', submitFindSubscriptionsList);

function showSubscriptionOnScreen() {
    const jwtToken = getCookie('JWT_TOKEN');
    if (!jwtToken) {
        console.error('JWT token not found');
        return;
    }
    fetch('/personal_office/subscription', {
        headers: {
            'Authorization': `Bearer ${jwtToken}`
        }
    })
        .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.json();
    })
        .then(data => {
        const subscriptionDetails = document.getElementById('subscriptionDetails');
        subscriptionDetails.innerText = data.subscriptionName;
    })
        .catch(error => {
        console.error('Failed to fetch subscription details: ', error);
    });
}

function showSubscriptionEndTimeOnScreen() {
    const jwtToken = getCookie('JWT_TOKEN');
    if (!jwtToken) {
        console.error('JWT token not found');
        return;
    }
    fetch('/personal_office/subscription_end_time', {
        headers: {
            'Authorization': `Bearer ${jwtToken}`
        }
    })
        .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.text();
    })
        .then(data => {
        const subscriptionExpiry = document.getElementById('subscriptionExpiry');
        const trimmedData = data.trim();
        if (trimmedData === '0') {
            subscriptionExpiry.innerText = 'infinity';
        } else {
            const expiryDate = new Date(trimmedData);
            const currentDate = new Date();
            if (isNaN(expiryDate.getTime())) {
                console.error('Invalid date format:', trimmedData);
                subscriptionExpiry.innerText = 'Invalid date';
                return;
            }
            if (expiryDate <= currentDate) {
                subscriptionExpiry.innerText = 'Infinity';
            } else {
                subscriptionExpiry.innerText = trimmedData;
            }
        }
    })
        .catch(error => {
        console.error('Failed to fetch subscription details: ', error);
    });
}

function startUpdatingSubscriptionInfo(intervalMinutes) {
    function updateSubscriptionInfo() {
        showSubscriptionOnScreen();
        showSubscriptionEndTimeOnScreen();
    }
    const intervalMilliseconds = intervalMinutes * 60 * 1000;
    setInterval(updateSubscriptionInfo, intervalMilliseconds);
}

function checkUsersAutoRenewStatus() {
    const jwtToken = getCookie('JWT_TOKEN');
    if (!jwtToken) {
        console.error('JWT token not found');
        return;
    }
    fetch('/personal_office/auto_renew_status', {
        headers: {
            'Authorization': `Bearer ${jwtToken}`
        }
    })
        .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.text();
    })
        .then(status => {
        if (status === "YES") {
            document.getElementById('YES').checked = true;
        } else {
            document.getElementById('NO').checked = true;
        }
    })
        .catch(error => {
        console.error('Failed to fetch auto-renew status: ', error);
    });
}

function changeUsersAutoRenewStatus(newStatus) {
    const jwtToken = getCookie('JWT_TOKEN');
    if (!jwtToken) {
        console.error('JWT token not found');
        return;
    }
    const requestData = {
        autoRenewStatus: newStatus
    };
    fetch('/personal_office/set_auto_renew', {
        method: 'PUT',
        headers: {
            'Authorization': `Bearer ${jwtToken}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestData)
    })
        .then(response => {
        if (!response.ok) {
            throw new Error('Failed to update auto-renew status');
        }
        return response.text();
    })
        .then(responseMessage => {
        console.log(responseMessage);
    })
        .catch(error => {
        console.error('Failed to update auto-renew status: ', error);
    });
}

function submitFindSubscriptionsList() {
    console.log("Function called");
    const findAllSubmenu = document.getElementById('findAllSubmenu');
    console.log("Current display style:", findAllSubmenu.style.display);
    if (findAllSubmenu.style.display === 'block') {
        console.log("Closing menu");
        findAllSubmenu.style.display = 'none';
    } else {
        console.log("Opening menu");
        closeAllMenus();
        const jwtToken = getCookie('JWT_TOKEN');
        if (!jwtToken) {
            console.error('JWT token not found');
            return;
        }
        fetch('/personal_office/subscriptions', {
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
            console.log('Fetched subscriptions data:', data);
            subscriptions = data;
            displayAllSubscription();
            findAllSubmenu.style.display = 'block';
            console.log("Menu should be visible now");
        })
            .catch(error => {
            console.error('Failed to fetch subscriptions: ', error);
            displayErrorMessage(error.message);
        });
    }
}

function displayAllSubscription() {
    findAllSubmenu.innerHTML = '<ul>';
    subscriptions.forEach(subscription => {
        const subscriptionName = subscription.subscriptionName;
        const subscriptionPrice = subscription.subscriptionPrice;
        const subscriptionDurationTime = subscription.subscriptionDurationTime;
        findAllSubmenu.innerHTML += `
            <li class="submenu-all-subscriptions-item">
                <p class="submenu-all-subscriptions-name">Name: ${subscriptionName}</p>
                <p class="submenu-all-subscriptions-name">Price: ${subscriptionPrice}</p>
                <p class="submenu-all-subscriptions-name">Duration time: ${subscriptionDurationTime}</p>
                <button class="submenu-all-subscriptions-button" onclick="updateSubscription('${subscriptionName}')">Subscribe</button>
            </li>
        `;
    });
    findAllSubmenu.innerHTML += '</ul>';
    findAllSubmenu.style.display = 'block';
}

function updateSubscription(subscriptionName) {
    if (event) {
        event.preventDefault();
    }
    console.log('updateSubscription called with:', subscriptionName);
    const jwtToken = getCookie('JWT_TOKEN');
    if (!jwtToken) {
        console.error('JWT token not found');
        return;
    }
    if (!subscriptionName) {
        console.error('Subscription name is required');
        return;
    }
    const requestData = { newSubscription: { subscriptionName: subscriptionName } };
    fetch('/personal_office/update_subscription', {
        method: 'PUT',
        headers: {
            'Authorization': `Bearer ${jwtToken}`,
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify(requestData)
    })
        .then(response => {
        if (!response.ok) {
            return response.text().then(text => {
                throw new Error(response.status + ': ' + text);
            });
        }
        return response.text();
    })
        .then(responseMessage => {
        displayMessage(responseMessage, 'success');
        showSubscriptionOnScreen();
        showSubscriptionEndTimeOnScreen();
    })
        .catch(error => {
        console.error('Failed to update subscription: ', error);
        displayMessage(error.message, 'error');
    });
}

function displayMessage(message, type) {
    const submenuSubscribe = document.getElementById('findAllSubmenu');
    submenuSubscribe.innerHTML = `<div class="display-message ${type}">${message}</div>`;
    submenuSubscribe.style.display = 'block';
    setTimeout(() => {
        submenuSubscribe.style.display = 'none';
    }, 5000);
}

function getUserPhoneNumber() {
    const jwtToken = getCookie('JWT_TOKEN');
    if (!jwtToken) {
        console.error('JWT token not found');
        return;
    }
    fetch('/personal_office/phone_number', {
        headers: {
            'Authorization': `Bearer ${jwtToken}`
        }
    })
        .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.json();
    })
        .then(data => {
        const currentPhoneNumber = document.getElementById('currentPhoneNumber');
        currentPhoneNumber.innerHTML = `Phone: ${data}`;
    })
        .catch(error => {
        console.error('Failed to fetch phone number: ', error);
    });
}

function updateUsersPhoneNumber(event) {
    event.preventDefault();
    const newPhoneNumber = document.getElementById('newPhoneNumber').value;
    if (!newPhoneNumber) {
        console.error('Phone number is required');
        return;
    }
    const jwtToken = getCookie('JWT_TOKEN');
    if (!jwtToken) {
        console.error('JWT token not found');
        return;
    }
    const requestData = {
        newPhoneNumber: newPhoneNumber
    };
    fetch('/personal_office/update_phone_number', {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${jwtToken}`
        },
        body: JSON.stringify(requestData)
    })
        .then(response => {
        if (response.ok) {
            return response.text();
        } else {
            return response.text().then(text => Promise.reject(text));
        }
    })
        .then(responseMessage => {
        getUserPhoneNumber();
    })
        .catch(error => {
        console.error('Failed to update phone number:', error);
        displayErrorMessageForCurrentPhoneNumber(error);
    });
}

function displayErrorMessageForCurrentPhoneNumber(message) {
    const submenuPersonalData = document.getElementById('currentPhoneNumber');
    const errorElement = submenuPersonalData.querySelector('.display-error-message-for-personal-data-update');
    if (errorElement) {
        errorElement.textContent = message;
    } else {
        const newErrorElement = document.createElement('p');
        newErrorElement.className = 'display-error-message-for-personal-data-update';
        newErrorElement.textContent = message;
        submenuPersonalData.appendChild(newErrorElement);
    }
    submenuPersonalData.style.display = 'block';
}

function updateUsersPassword(event) {
    event.preventDefault();
    const currentPassword = document.getElementById('currentPassword').value;
    const newPassword = document.getElementById('newPassword').value;
    const jwtToken = getCookie('JWT_TOKEN');
    if (!jwtToken) {
        console.error('JWT token not found');
        return;
    }
    const requestData = {
        currentPassword: currentPassword,
        newPassword: newPassword
    };
    fetch('/personal_office/update_password', {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${jwtToken}`
        },
        body: JSON.stringify(requestData)
    })
        .then(response => {
        if (response.ok) {
            return response.text();
        } else {
            return response.text().then(text => Promise.reject(text));
        }
    })
        .then(responseMessage => {
        displayMessageForUserPassword(responseMessage, 'success');
    })
        .catch(error => {
        console.error('Failed to update password:', error);
        displayMessageForUserPassword(error, 'error');
    });
}

function displayMessageForUserPassword(message, type) {
    const submenuPersonalData = document.getElementById('userPassword');
    const existingMessage = submenuPersonalData.querySelector('.display-message-for-personal-data-update');
    const existingError = submenuPersonalData.querySelector('.display-error-message-for-personal-data-update');
    if (existingMessage) {
        existingMessage.remove();
    }
    if (existingError) {
        existingError.remove();
    }
    const newMessageElement = document.createElement('p');
    if (type === 'success') {
        newMessageElement.className = 'display-message-for-personal-data-update';
    } else if (type === 'error') {
        newMessageElement.className = 'display-error-message-for-personal-data-update';
    }
    newMessageElement.textContent = message;
    submenuPersonalData.appendChild(newMessageElement);
    submenuPersonalData.style.display = 'block';
}

function getUserEmail() {
    const jwtToken = getCookie('JWT_TOKEN');
    if (!jwtToken) {
        console.error('JWT token not found');
        return;
    }
    fetch('/personal_office/email', {
        headers: {
            'Authorization': `Bearer ${jwtToken}`
        }
    })
        .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.text();
    })
        .then(data => {
        const currentEmail = document.getElementById('currentEmail');
        currentEmail.innerHTML = `Email: ${data}`;
    })
        .catch(error => {
        console.error('Failed to fetch phone number: ', error);
    });
}

function updateUsersEmail(event) {
    event.preventDefault();
    const newEmail = document.getElementById('newEmail').value;
    const jwtToken = getCookie('JWT_TOKEN');
    if (!jwtToken) {
        console.error('JWT token not found');
        return;
    }
    const requestData = {
        newEmail: newEmail
    };
    fetch('/personal_office/update_email', {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${jwtToken}`
        },
        body: JSON.stringify(requestData)
    })
        .then(response => {
        if (response.ok) {
            return response.text();
        } else {
            return response.text().then(text => Promise.reject(text));
        }
    })
        .then(responseMessage => {
        getUserEmail();
    })
        .catch(error => {
        console.error('Failed to update phone number:', error);
        displayErrorMessageForUserEmail(error);
    });
}

function displayErrorMessageForUserEmail(message) {
    const submenuPersonalData = document.getElementById('currentEmail');
    const errorElement = submenuPersonalData.querySelector('.display-error-message-for-personal-data-update');
    if (errorElement) {
        errorElement.textContent = message;
    } else {
        const newErrorElement = document.createElement('p');
        newErrorElement.className = 'display-error-message-for-personal-data-update';
        newErrorElement.textContent = message;
        submenuPersonalData.appendChild(newErrorElement);
    }
    submenuPersonalData.style.display = 'block';
}

function getUserBankCardNumber() {
    const jwtToken = getCookie('JWT_TOKEN');
    if (!jwtToken) {
        console.error('JWT token not found');
        return;
    }
    fetch('/personal_office/bank_card_number', {
        headers: {
            'Authorization': `Bearer ${jwtToken}`
        }
    })
        .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.json();
    })
        .then(data => {
        const currentBankCardNumber = document.getElementById('currentBankCardNumber');
        currentBankCardNumber.innerHTML = `Bank card number: ${data}`;
    })
        .catch(error => {
        console.error('Failed to fetch phone number: ', error);
    });
}

function updateUsersBankCard(event) {
    event.preventDefault();
    const newBankCardNumber = document.getElementById('newBankCardNumber').value;
    const newBankCardExpirationDate = document.getElementById('newBankCardExpirationDate').value;
    const newBankCardCVV = document.getElementById('newBankCardCVV').value;
    const jwtToken = getCookie('JWT_TOKEN');
    if (!jwtToken) {
        console.error('JWT token not found');
        return;
    }
    const requestData = {
        newUserBankCard: {
            cardNumber: newBankCardNumber,
            cvv: newBankCardCVV,
            cardExpirationDate: newBankCardExpirationDate
        }
    };
    fetch('/personal_office/update_bank_card', {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${jwtToken}`
        },
        body: JSON.stringify(requestData)
    })
        .then(response => {
        if (response.ok) {
            return response.text();
        } else {
            return response.text().then(text => Promise.reject(text));
        }
    })
        .then(responseMessage => {
        getUserBankCardNumber();
    })
        .catch(error => {
        console.error('Failed to update phone number:', error);
        displayErrorMessageForUserBankCard(error);
    });
}

function displayErrorMessageForUserBankCard(message) {
    const submenuPersonalData = document.getElementById('currentEmail');
    const errorElement = submenuPersonalData.querySelector('.display-error-message-for-personal-data-update');
    if (errorElement) {
        errorElement.textContent = message;
    } else {
        const newErrorElement = document.createElement('p');
        newErrorElement.className = 'display-error-message-for-personal-data-update';
        newErrorElement.textContent = message;
        submenuPersonalData.appendChild(newErrorElement);
    }
    submenuPersonalData.style.display = 'block';
}

function deleteUserAccount(event) {
    event.preventDefault();
    const password = document.getElementById('Password').value;
    const jwtToken = getCookie('JWT_TOKEN');

    if (!jwtToken) {
        console.error('JWT token not found');
        return;
    }
    const requestData = {
        password: password
    };
    fetch('/personal_office/delete_user_by_phone_number', {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${jwtToken}`
        },
        body: JSON.stringify(requestData)
    })
        .then(response => {
        if (response.redirected) {
            window.location.href = response.url;
        } else if (response.ok) {
            console.log('User account deleted successfully');
        } else {
            return response.text().then(text => Promise.reject(text));
        }
    })
        .catch(error => {
        console.error('Failed to delete account:', error);
        displayErrorMessageForDeleteAccount(error);
    });
}

function displayErrorMessageForDeleteAccount(message) {
    const submenuPersonalData = document.getElementById('deleteAccount');
    const errorElement = submenuPersonalData.querySelector('.display-error-message-for-personal-data-update');
    if (errorElement) {
        errorElement.textContent = message;
    } else {
        const newErrorElement = document.createElement('p');
        newErrorElement.className = 'display-error-message-for-personal-data-update';
        newErrorElement.textContent = message;
        submenuPersonalData.appendChild(newErrorElement);
    }
    submenuPersonalData.style.display = 'block';
}

function logOut(event) {
    event.preventDefault();
    document.cookie = "JWT_TOKEN=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    localStorage.clear();
    sessionStorage.clear();
    window.location.href = '/host_page';
    window.history.pushState(null, "", window.location.href);
    window.onpopstate = function() {
        window.history.pushState(null, "", window.location.href);
    };
}

function navigateToMusic() {
    const jwtToken = getCookie('JWT_TOKEN');
    if (!jwtToken) {
        console.error('JWT token not found. User might not be authenticated.');
        return;
    }
    fetch('/music/get_music_page', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${jwtToken}`,
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
    })
        .then(data => {
        if (data && data.url) {
            window.location.href = data.url;
        } else {
            console.error('Invalid response from server');
        }
    })
        .catch(error => {
        console.error('Error:', error);
    });
}

// Call the function to fetch and display the subscription details
showSubscriptionOnScreen();
showSubscriptionEndTimeOnScreen()
checkUsersAutoRenewStatus();
getUserPhoneNumber();
getUserEmail();
getUserBankCardNumber();
startUpdatingSubscriptionInfo(3);

