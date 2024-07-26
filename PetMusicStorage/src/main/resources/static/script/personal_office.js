function showForm(formId) {
    const forms = document.querySelectorAll('#formContainer .form-submenu-personal-data');
    forms.forEach(form => form.style.display = 'none');
    document.getElementById(formId).style.display = 'block';
    document.querySelectorAll('.submenu-subscription, .submenu-personal-data').forEach(submenu => submenu.style.display = 'none');
}

const submenuSubscribe = document.getElementById("submenuSubscribe");
const submenuPersonalData = document.getElementById("submenuPersonalData");

function toggleSubMenu(submenuId) {
    const submenu = document.getElementById(submenuId);
    const allSubmenus = document.querySelectorAll('.submenu-subscription, .submenu-personal-data');
    allSubmenus.forEach(submenu => submenu.style.display = 'none');
    submenu.style.display = 'block';
}

// Open submenu
document.querySelector('.button-container:nth-child(1) .button').addEventListener('click', function(event) {
    toggleSubMenu('submenuSubscribe');
});
document.querySelector('.button-container:nth-child(2) .button').addEventListener('click', function(event) {
    toggleSubMenu('submenuPersonalData');
});

// Close submenu when clicking outside the block
document.addEventListener('click', function(event) {
    const target = event.target;
    const isClickInsideButton = target.closest('.button-container .button');
    const isClickInsideSubmenu = target.closest('.submenu-subscription, .submenu-personal-data');
    const isClickInsideForm = target.closest('#formContainer .form-submenu-personal-data');
    if (!isClickInsideButton && !isClickInsideSubmenu && !isClickInsideForm) {
        const allSubmenus = document.querySelectorAll(
        '.submenu-subscription,.submenu-personal-data,.form-submenu-personal-data,.form-submenu-personal-data-email');
        allSubmenus.forEach(submenu => submenu.style.display = 'none');
    }
});

function getCookie(name) {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
    }

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

//find subscription by name
function submitSubscriptionForm(event, subscriptionName) {
    event.preventDefault();
    const jwtToken = getCookie('JWT_TOKEN');
    if (!jwtToken) {
        console.error('JWT token not found');
        return;
    }
    if (!subscriptionName) {
        const subscriptionNameInput = document.getElementById('subscriptionNameInput');
        if (subscriptionNameInput) {
            subscriptionName = subscriptionNameInput.value;
        }
    }
    if (!subscriptionName) {
        console.error('Subscription name is not provided');
        return;
    }
    fetch(`/personal_office/subscription_by_name?subscriptionName=${encodeURIComponent(subscriptionName)}`, {
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
        displaySubscriptionDetails(data);
    })
    .catch(error => {
        console.error('Failed to fetch subscription details: ', error);
        displayErrorMessage(error.message);
    });
}

//show subscription details
function displaySubscriptionDetails(subscription) {
    const submenuSubscribe = document.getElementById('submenuSubscribe');
    submenuSubscribe.innerHTML = `
        <p>Subscription Name: ${subscription.subscriptionName}</p>
        <p>Subscription Price: ${subscription.subscriptionPrice}</p>
        <p>Subscription Duration Time: ${subscription.subscriptionDurationTime}</p>
        <button class="submenu-subscription-button" onclick="updateSubscription('${subscription.subscriptionName}')">Subscribe</button>
    `;
    submenuSubscribe.classList.add('subscription-details');
    submenuSubscribe.style.display = 'block';
}

function displayErrorMessage(message) {
    const submenuSubscribe = document.getElementById('submenuSubscribe');
    submenuSubscribe.innerHTML = `
          <p class="display-error-message">${message}</p>
    `;
    submenuSubscribe.style.display = 'block';
}

//update subscription for user
function updateSubscription(subscriptionName) {
    const jwtToken = getCookie('JWT_TOKEN');
    if (!jwtToken) {
        console.error('JWT token not found');
        return;
    }
    const requestData = {
        newSubscription: {
            subscriptionName: subscriptionName
        }
    };
    fetch('/personal_office/update_subscription', {
        method: 'PUT',
        headers: {
            'Authorization': `Bearer ${jwtToken}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestData)
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => {
                throw new Error(text);
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
    const submenuSubscribe = document.getElementById('submenuSubscribe');
    submenuSubscribe.innerHTML = `
        <div class="display-message">${message}</div>
    `;
    submenuSubscribe.style.display = 'block';
}

function startUpdatingSubscriptionInfo(intervalMinutes) {
    function updateSubscriptionInfo() {
        showSubscriptionOnScreen();
        showSubscriptionEndTimeOnScreen();
    }
    const intervalMilliseconds = intervalMinutes * 60 * 1000;
    setInterval(updateSubscriptionInfo, intervalMilliseconds);
}

function submitAllSubscriptions() {
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
    .then(subscriptions => {
        const submenuSubscribe = document.getElementById('submenuSubscribe');
        submenuSubscribe.innerHTML = '<ul>';
        subscriptions.forEach(subscription => {
            submenuSubscribe.innerHTML += `
                <li class="submenu-all-subscriptions-item">
                    <p class="submenu-all-subscriptions-name">${subscription.subscriptionName}</p>
                    <button class="submenu-all-subscriptions-button" onclick="submitSubscriptionForm(event, '${subscription.subscriptionName}')">Show details</button>
                </li>
            `;
        });
        submenuSubscribe.innerHTML += '</ul>';
        submenuSubscribe.classList.add('allSubscription-details');
        submenuSubscribe.style.display = 'block';
    })
    .catch(error => {
        console.error('Failed to fetch subscription details: ', error);
        displayErrorMessage(error.message);
    });
}

function submitSubscriptionPriceForm(event) {
    event.preventDefault();
    const jwtToken = getCookie('JWT_TOKEN');
    if (!jwtToken) {
        console.error('JWT token not found');
        return;
    }
    const minPrice = document.getElementById('minSubscribePrice').value;
    const maxPrice = document.getElementById('maxSubscribePrice').value;
    if (minPrice === '' || maxPrice === '') {
        console.error('Both price fields are required');
        return;
    }
    fetch(`/personal_office/subscription_by_price?minPrice=${encodeURIComponent(minPrice)}&maxPrice=${encodeURIComponent(maxPrice)}`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${jwtToken}`
        }
    })
    .then(response => response.text())
    .then(text => {
        let data;
        try {
            data = JSON.parse(text);
        } catch (e) {
            data = text;
        }
        const submenuSubscribe = document.getElementById('submenuSubscribe');
        submenuSubscribe.innerHTML = '';
        if (typeof data === 'string') {
            submenuSubscribe.innerHTML = `<p>${data}</p>`;
        } else if (Array.isArray(data)) {
            submenuSubscribe.innerHTML = '<ul>';
            data.forEach(subscription => {
                submenuSubscribe.innerHTML += `
                    <li class="submenu-all-subscriptions-item">
                        <p class="submenu-all-subscriptions-name">${subscription.subscriptionName}</p>
                        <button class="submenu-all-subscriptions-button" onclick="submitSubscriptionForm(event, '${subscription.subscriptionName}')">Show details</button>
                    </li>
                `;
            });
            submenuSubscribe.innerHTML += '</ul>';
        } else {
            submenuSubscribe.innerHTML = `<p>Unexpected response format: ${text}</p>`;
        }
        submenuSubscribe.classList.add('allSubscription-details');
        submenuSubscribe.style.display = 'block';
    })
    .catch(error => {
        console.error('Failed to fetch subscriptions:', error);
    });
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
        currentPhoneNumber.innerText = data;
    })
    .catch(error => {
        console.error('Failed to fetch phone number: ', error);
    });
}

function updatePhoneNumber(event) {
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
        displayMessageForPersonalDataUpdate(responseMessage, 'success');
    })
    .catch(error => {
        console.error('Failed to update phone number:', error);
         displayErrorMessageForPersonalDataUpdate(error);
    });
}

function displayErrorMessageForPersonalDataUpdate(message) {
    const submenuPersonalData = document.getElementById('submenuPersonalData');
    submenuPersonalData.classList.add('submenu-personal-data-error');
    submenuPersonalData.innerHTML = `
        <p class="display-error-message-for-personal-data-update">${message}</p>
    `;
    submenuPersonalData.style.display = 'block';
      document.getElementById('updatePhoneNumberForm').style.display = 'none';
       document.getElementById('updatePasswordForm').style.display = 'none';
        document.getElementById('updateBankCardDataForm').style.display = 'none';
          document.getElementById('updateEmailForm').style.display = 'none';
}

function displayMessageForPersonalDataUpdate(message) {
    const submenuPersonalData = document.getElementById('submenuPersonalData');
     submenuPersonalData.classList.add('submenu-personal-data-error');
    submenuPersonalData.innerHTML = `
        <p class="display-message-for-personal-data-update">${message}</p>
    `;
    submenuPersonalData.style.display = 'block';
     document.getElementById('updatePhoneNumberForm').style.display = 'none';
      document.getElementById('updatePasswordForm').style.display = 'none';
       document.getElementById('updateBankCardDataForm').style.display = 'none';
       document.getElementById('updateEmailForm').style.display = 'none';
}

function updatePassword(event) {
     event.preventDefault();
    const newPassword = document.getElementById('newPassword').value;
    const jwtToken = getCookie('JWT_TOKEN');
    if (!jwtToken) {
        console.error('JWT token not found');
        return;
    }
    const requestData = {
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
        displayMessageForPersonalDataUpdate(responseMessage, 'success');
    })
    .catch(error => {
        console.error('Failed to update phone number:', error);
         displayErrorMessageForPersonalDataUpdate(error);
    });
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
        currentBankCardNumber.innerText = data;
    })
    .catch(error => {
        console.error('Failed to fetch phone number: ', error);
    });
}

function updateBankCard(event) {
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
        displayMessageForPersonalDataUpdate(responseMessage, 'success');
    })
    .catch(error => {
        console.error('Failed to update phone number:', error);
         displayErrorMessageForPersonalDataUpdate(error);
    });
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
        currentEmail.innerText = data;
    })
    .catch(error => {
        console.error('Failed to fetch phone number: ', error);
    });
}

function updateEmail(event) {
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
        displayMessageForPersonalDataUpdate(responseMessage, 'success');
    })
    .catch(error => {
        console.error('Failed to update phone number:', error);
         displayErrorMessageForPersonalDataUpdate(error);
    });
}

function deleteAccount(event) {
    event.preventDefault();
    const jwtToken = getCookie('JWT_TOKEN');
    if (!jwtToken) {
        console.error('JWT token not found');
        return;
    }
    fetch('/personal_office/delete_user_by_phone_number', {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${jwtToken}`
        }
    })
    .then(response => {
        if (response.redirected) {

            window.location.href = response.url;
        } else {
            return response.text().then(text => Promise.reject(text));
        }
    })
    .catch(error => {
        console.error('Failed to delete account:', error);
        displayErrorMessageForPersonalDataUpdate(error);
    });
}

function exit(event) {
    event.preventDefault();
    window.location.href = '/host_page';
}

    // Call the function to fetch and display the subscription details
    showSubscriptionOnScreen();
    showSubscriptionEndTimeOnScreen()
    checkUsersAutoRenewStatus();
    getUserPhoneNumber()
    getUserBankCardNumber()
    getUserEmail()
    startUpdatingSubscriptionInfo(3);