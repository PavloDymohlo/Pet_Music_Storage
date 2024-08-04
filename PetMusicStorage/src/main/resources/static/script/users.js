function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
}

document.addEventListener('click', function(event) {
    const target = event.target;
    const isClickInsideButton = target.closest('.button-nav-list, .button-nav-list-delete-users, .button-nav-list-main-menu');
    const isClickInsideForm = target.closest('.form-nav-list');
    const isClickInsideUserDetails = target.closest('#userDetails');
    const isClickInsideUserList = target.closest('#usersContainer');
    if (isClickInsideButton) {
        console.log('Button clicked:', isClickInsideButton);
        const allForms = document.querySelectorAll('.form-nav-list form');
        allForms.forEach(form => form.style.display = 'none');
        const onclickAttr = isClickInsideButton.getAttribute('onclick');
        console.log('Onclick attribute:', onclickAttr);
        if (onclickAttr) {
            const parts = onclickAttr.split('\'');
            if (parts.length > 1) {
                const formId = parts[1];
                console.log('Form ID:', formId);
                const formToShow = document.getElementById(formId);
                if (formToShow) {
                    formToShow.style.display = 'block';
                }
            }
        }
    }
    else if (!isClickInsideForm && !isClickInsideUserDetails && !isClickInsideUserList) {
        const allForms = document.querySelectorAll('.form-nav-list form');
        allForms.forEach(form => form.style.display = 'none');
        const detailsElement = document.getElementById('userDetails');
        if (detailsElement) {
            detailsElement.style.display = 'none';
        }
        const usersContainer = document.getElementById('usersContainer');
        if (usersContainer) {
            usersContainer.style.display = 'none';
        }
    }

});

let currentActiveFormId = null;
function showForm(formId, showUserDetails = false) {
    if (showUserDetails) {
        const forms = document.querySelectorAll('.form-submenu-personal-data, .form-find-by-phone-number, .form-find-by-bank-card, .form-find-by-subscription, .form-find-by-email, .form-find-by-id, .form-delete-all-users, .form-main-menu');
        forms.forEach(form => {
            form.style.display = 'none';
        });
        if (currentActiveFormId) {
            const activeForm = document.getElementById(currentActiveFormId);
            if (activeForm) {
                activeForm.style.display = 'none';
            }
        }
        currentActiveFormId = null;
    } else {
        hideAllMenus();
    }
    const form = document.getElementById(formId);
    if (form) {
        form.style.display = 'block';
        currentActiveFormId = formId;
    }
}

function hideAllMenus() {
    const usersContainer = document.getElementById('usersContainer');
    if (usersContainer) {
        usersContainer.style.display = 'none';
    }
    const usersInfo = document.getElementById('usersInfo');
    if (usersInfo) {
        usersInfo.innerHTML = '';
    }
    const detailsElement = document.getElementById('userDetails');
    if (detailsElement) {
        detailsElement.style.display = 'none';
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

const usersPerPage = 3;
let currentPage = 1;
let totalUsers = 0;
let users = [];
function updatePaginationControls() {
    const prevButton = document.getElementById('prev-page');
    const nextButton = document.getElementById('next-page');
    const pageInfo = document.getElementById('page-info');
    prevButton.disabled = currentPage === 1;
    nextButton.disabled = currentPage * usersPerPage >= totalUsers;
    pageInfo.textContent = `Page ${currentPage}`;
}

function changePage(direction) {
    const newPage = currentPage + direction;
    if (newPage >= 1 && newPage <= Math.ceil(totalUsers / usersPerPage)) {
        currentPage = newPage;
        displayUsers(users);
    }
}

let isUsersVisible = false;
let isUserDetailsVisible = false;
function submitFindAllUsers() {
    if (isUsersVisible || isUserDetailsVisible) {
        hideAllMenus();
        isUsersVisible = false;
        isUserDetailsVisible = false;
        return;
    }
    const jwtToken = getCookie('JWT_TOKEN');
    if (!jwtToken) {
        console.error('JWT token not found');
        return;
    }
    fetch('/users/all_users', {
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
        currentPage = 1;
        displayUsers(users);
        isUsersVisible = true;
    })
    .catch(error => {
        console.error('Failed to fetch user details: ', error);
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
    fetch(`/users/user_by_phone?phoneNumber=${phoneNumber}`, {
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

function displayUsers(usersList) {
    console.log('Displaying users:', usersList);
    const usersInfo = document.getElementById('usersInfo');
    const usersContainer = document.getElementById('usersContainer');
    if ( !usersContainer || !usersInfo) {
        console.error('Element not found');
        return;
    }
    usersInfo.style.display = 'block';
    usersContainer.style.display = 'block';
    usersInfo.innerHTML = '';
    totalUsers = usersList.length;
    const startIndex = (currentPage - 1) * usersPerPage;
    const endIndex = Math.min(startIndex + usersPerPage, totalUsers);
    const usersToDisplay = usersList.slice(startIndex, endIndex);
    usersToDisplay.forEach(user => {
        usersInfo.innerHTML += `
            <li class="submenu-all-users-item">
                <div class="user-info">
                    <p>Phone: ${user.phoneNumber}</p>
                    <button class="submenu-all-users-button" onclick="findUserByPhoneNumber(event, '${user.phoneNumber}')">Show details</button>
                </div>
            </li>
            <hr class="dashed-line">
        `;
    });
    updatePaginationControls();
}

function displayUserDetails(user) {
    const detailsElement = document.getElementById('userDetails');
    const currentPhoneNumberElement = document.getElementById('currentPhoneNumber');
    const currentEmailElement = document.getElementById('currentEmail');
    const currentBankCardElement = document.getElementById('currentBankCardNumber');
    const currentSubscription = document.getElementById('currentSubscription');
    const usersContainer = document.getElementById('usersContainer');
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
    const formattedEndTime = formatDate(date);
    detailsElement.style.display = 'block';
    detailsElement.innerHTML = `
<div>
  <div class="user-details-row">
    <p>Phone: ${user.phoneNumber}</p>
    <input id="newPhoneNumber" class="input-personal-data" placeholder="New phone number" type="number" required />
    <button class="submenu-all-users-button" onclick="updateUsersPhoneNumber(event, '${user.phoneNumber}')"> Update </button>
  </div>
  <div class="user-details-row">
     <p>Subscription: ${user.subscription.subscriptionName}</p>
     <button class="submenu-all-users-button" onclick="submitFindAlSubscriptions()" > Update </button>
  </div>
  <div class="user-details-row">
      <p>Auto-renew subscription: ${user.autoRenew}</p>
      <div class="auto-renew-options">
      <input name="Auto-renew" id="YES" type="radio" value="YES" onclick="updateUsersAutoRenewStatus(event, '${user.phoneNumber}')" checked />
      <label for="YES" class="users-personal-data-text">YES</label>
      <span class="radio-spacing"></span>
      <input name="Auto-renew" id="NO" type="radio" value="NO" onclick="updateUsersAutoRenewStatus(event, '${user.phoneNumber}')" />
      <label for="NO" class="users-personal-data-text">NO</label>
      </div>
  </div>
  <div class="user-details-row">
    <p>Password: hidden</p>
    <input id="newPassword" class="input-personal-data" placeholder="New password" type="text" required />
    <button class="submenu-all-users-button" onclick="updateUsersPassword(event, '${user.phoneNumber}')" > Update </button>
  </div>
  <div class="user-details-row">
     <p>Email: ${user.email}</p>
     <input id="newEmail" class="input-personal-data" placeholder="New email" type="text" required />
     <button class="submenu-all-users-button" onclick="updateUsersEmail(event, '${user.phoneNumber}')" > Update </button>
  </div>
   <div class="user-details-row">
       <p>Bank card number: ${user.userBankCard.cardNumber}</p>
       <div class="bank-card-group">
           <input id="newBankCardNumber" class="input-bank-personal-data" placeholder="Bank card number" type="number" required>
           <div class="bank-card-details">
               <input id="newBankCardExpirationDate" class="input-bank-card-expiration-data" placeholder="Expiration date" type="text" required>
               <input id="newBankCardCVV" class="input-bank-card-cvv" placeholder="CVV" type="number" required>
           </div>
       </div>
       <button class="submenu-all-users-button" onclick="updateUsersBankCard(event, '${user.phoneNumber}')">Update</button>
   </div>
  <div class="user-details-row">
     <p>Subscription end time: ${formattedEndTime}</p>
  </div>
  <div class="user-details-row">
     <p>Delete user: </p>
     <button class="submenu-all-users-button" onclick="deleteUserByPhoneNumber('${user.phoneNumber}')" > Delete </button>
  </div>
</div>
      `;
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
    function setRadioValue(name, value) {
        const radios = document.getElementsByName(name);
        radios.forEach(radio => {
            if (radio.value === value) {
                radio.checked = true;
            }
        });
    }
    setRadioValue('Auto-renew', user.autoRenew);
    if (currentSubscription) {
        currentSubscription.style.display = 'block';
        submitFindAlSubscriptions();
    }
    if (usersContainer) {
        usersContainer.style.display = 'none';
    }

    showForm(null, true);
     isUserDetailsVisible = true;
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
        displaySubscriptions();
    })
    .catch(error => {
        console.error('Failed to fetch subscriptions: ', error);
        displayErrorMessage(error.message);
    });
}

function displaySubscriptions() {
    const subscriptionsElement = document.getElementById('subscriptionInfo');
    if (!subscriptionsElement) return;
    subscriptionsElement.innerHTML = '';
    subscriptions.forEach(subscribe => {
        subscriptionsElement.innerHTML += `
            <li class="subscription-item">
                <p class="subscription-name">Subscription name: <strong>${subscribe.subscriptionName}</strong></p>
                <p class="subscription-price">Subscription price: ${subscribe.subscriptionPrice}</p>
                <p class="subscription-duration">Duration time: ${subscribe.subscriptionDurationTime}</p>
                <button class="subscribe-button" onclick="updateUsersSubscription('${subscribe.subscriptionName}')">Subscribe</button>
            </li>
            <hr class="dashed-line">
        `;
    });
}





function updateUsersPhoneNumber(event, currentPhoneNumber) {
    const newPhoneNumber = document.getElementById('newPhoneNumber').value;
    const jwtToken = getCookie('JWT_TOKEN');
    if (!jwtToken) {
        console.error('JWT token not found');
        return;
    }
    const payload = {
        currentPhoneNumber: currentPhoneNumber,
        newPhoneNumber: newPhoneNumber
    };
    fetch('/users/update_phone_number', {
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

function displayErrorMessageForPersonalDataUpdate(message) {
    const submenuPersonalData = document.getElementById('usersPersonalDataContainer');
    submenuPersonalData.classList.add('submenu-personal-data-error');
    submenuPersonalData.innerHTML = `
        <p class="display-error-message-for-personal-data-update">${message}</p>
    `;
}

function displayMessageForPersonalDataUpdate(message) {
    const submenuPersonalData = document.getElementById('usersPersonalDataContainer');
     submenuPersonalData.classList.add('submenu-personal-data-success');
    submenuPersonalData.innerHTML = `
        <p class="display-message-for-personal-data-update">${message}</p>
    `;
}

function updateUsersPassword(event, userPhoneNumber) {
    event.preventDefault();
    const newPassword = document.getElementById('newPassword').value;
    if (!newPassword) {
        alert('Please enter a new password.');
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
    fetch('/users/update_password', {
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

function updateUsersAutoRenewStatus(event, currentPhoneNumber) {
    const autoRenewStatus = String(event.target.value);
    const jwtToken = getCookie('JWT_TOKEN');
    if (!jwtToken) {
        console.error('JWT token not found');
        return;
    }
    const payload = {
        userPhoneNumber: currentPhoneNumber,
        autoRenewStatus: autoRenewStatus
    };
    fetch('/users/set_auto_renew', {
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
    })
    .catch(error => {
        console.error('Error updating auto-renew status:', error);
    });
}

function updateUsersEmail(event, userPhoneNumber) {
    event.preventDefault();
    const newEmail = document.getElementById('newEmail').value;
    const jwtToken = getCookie('JWT_TOKEN');
    if (!jwtToken) {
        console.error('JWT token not found');
        return;
    }
    const payload = {
        userPhoneNumber: userPhoneNumber,
        newEmail: newEmail
    };
    fetch('/users/update_email', {
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

function updateUsersBankCard(event, userPhoneNumber) {
    event.preventDefault();
    const newBankCardNumber = document.getElementById('newBankCardNumber').value;
    const newBankCardExpirationDate = document.getElementById('newBankCardExpirationDate').value;
    const newBankCardCVV = document.getElementById('newBankCardCVV').value;
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
    fetch('/users/update_bank_card', {
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

     fetch('/users/update_subscription', {
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
//
// function displayErrorMessage(message) {
//     const usersElement = document.getElementById('usersInfo');
//     if (!usersElement) return;
//     usersElement.innerHTML = `
//         <p class="display-error-message">${message}</p>
//     `;
// }

 function deleteUserByPhoneNumber(userPhoneNumber) {
     event.preventDefault();
     const jwtToken = getCookie('JWT_TOKEN');
     if (!jwtToken) {
         console.error('JWT token not found');
         return;
     }
     fetch(`/users/delete_user_by_phone_number?phoneNumber=${encodeURIComponent(userPhoneNumber)}`, {
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

 function submitManualFindUserByPhoneNumber(event) {
     event.preventDefault();
            const phoneNumber = document.getElementById('userPhoneNumber').value;
            const jwtToken = getCookie('JWT_TOKEN');
               if (!jwtToken) {
                   console.error('JWT token not found');
                   return;
               }
               fetch(`/users/user_by_phone?phoneNumber=${phoneNumber}`, {
                   method: 'GET',
                   headers: {
                       'Authorization': `Bearer ${jwtToken}`
                   },
               })
            .then(response => {
                if (!response.ok) {
                    return response.text().then(text => {
                        throw new Error(text);
                    });
                }
                return response.json();
            })
            .then(user => {
                    console.log('User details:', user);
                   displayUserDetails(user);
            })
            .catch(error => {
                console.error('Error finding user:', error);
                displayErrorMessageForPersonalDataUpdate(error.message);
            });
 }

 function submitFindUserByBankCard() {
 event.preventDefault();
 const userBankCardNumber = document.getElementById('userBankCardNumber').value;
     if (isUsersVisible || isUserDetailsVisible) {
         hideAllMenus();
         isUsersVisible = false;
         isUserDetailsVisible = false;
         return;
     }
     const jwtToken = getCookie('JWT_TOKEN');
     if (!jwtToken) {
         console.error('JWT token not found');
         return;
     }
     fetch(`/users/user_by_bank_card?bankCardNumber=${userBankCardNumber}`, {
                        method: 'GET',
                        headers: {
                            'Authorization': `Bearer ${jwtToken}`
                        },
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
         currentPage = 1;
         displayUsers(users);
         showForm(null, true);
         isUsersVisible = true;
     })
     .catch(error => {
                     console.error('Error finding user:', error);
                     displayErrorMessageForPersonalDataUpdate(error.message);
                 });
 }

 function submitFindSubscriptionsList() {
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
     console.log('Fetched subscriptions data:', data);
         subscriptions = data;
         displaySubscriptionsForFindUser();
     })
     .catch(error => {
         console.error('Failed to fetch subscriptions: ', error);
         displayErrorMessage(error.message);
     });
 }

function displaySubscriptionsForFindUser() {
    const subscriptionsElement = document.getElementById('subscriptionList');
    const findUsersBySubscriptionElement = document.getElementById('findUsersBySubscription');
    if (findUsersBySubscriptionElement) {
        findUsersBySubscriptionElement.style.display = 'block';
    }
    if (!subscriptionsElement) return;
    subscriptionsElement.innerHTML = '';
    subscriptions.forEach(subscription => {
        const subscriptionName = subscription.subscriptionName;
        subscriptionsElement.innerHTML += `
            <li>
                <button class="subscription-list-button" onclick="submitFindUsersBySubscription('${subscription.subscriptionName}')">
                    ${subscriptionName}
                </button>
            </li>
        `;
    });
}


function submitFindUsersBySubscription(subscriptionName) {
 event.preventDefault();
     if (isUsersVisible || isUserDetailsVisible) {
         hideAllMenus();
         isUsersVisible = false;
         isUserDetailsVisible = false;
         return;
     }
     const jwtToken = getCookie('JWT_TOKEN');
     if (!jwtToken) {
         console.error('JWT token not found');
         return;
     }
     fetch(`/users/user_by_subscription?subscription=${subscriptionName}`, {
                        method: 'GET',
                        headers: {
                            'Authorization': `Bearer ${jwtToken}`
                        },
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
         currentPage = 1;
         displayUsers(users);
         showForm(null, true);
         isUsersVisible = true;
     })
     .catch(error => {
                     console.error('Error finding user:', error);
                     displayErrorMessageForPersonalDataUpdate(error.message);
                 });
 }


 function submitFindUserByEmail(event) {
      event.preventDefault();
             const email = document.getElementById('userEmail').value;
             const jwtToken = getCookie('JWT_TOKEN');
                if (!jwtToken) {
                    console.error('JWT token not found');
                    return;
                }
                fetch(`/users/user_by_email?email=${email}`, {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${jwtToken}`
                    },
                })
             .then(response => {
                 if (!response.ok) {
                     return response.text().then(text => {
                         throw new Error(text);
                     });
                 }
                 return response.json();
             })
             .then(user => {
                     console.log('User details:', user);
                    displayUserDetails(user);
             })
             .catch(error => {
                 console.error('Error finding user:', error);
                 displayErrorMessageForPersonalDataUpdate(error.message);
             });
  }

  function submitFindUserById(event) {
        event.preventDefault();
               const userId = document.getElementById('userId').value;
               const jwtToken = getCookie('JWT_TOKEN');
                  if (!jwtToken) {
                      console.error('JWT token not found');
                      return;
                  }
                  fetch(`/users/user_by_id?id=${userId}`, {
                      method: 'GET',
                      headers: {
                          'Authorization': `Bearer ${jwtToken}`
                      },
                  })
               .then(response => {
                   if (!response.ok) {
                       return response.text().then(text => {
                           throw new Error(text);
                       });
                   }
                   return response.json();
               })
               .then(user => {
                       console.log('User details:', user);
                      displayUserDetails(user);
               })
               .catch(error => {
                   console.error('Error finding user:', error);
                   displayErrorMessageForPersonalDataUpdate(error.message);
               });
    }

  function submitDeleteAllUser(event) {
      const jwtToken = getCookie('JWT_TOKEN');
      if (!jwtToken) {
          console.error('JWT token not found');
          return;
      }
      fetch('/users/delete_all_users', {
          method: 'DELETE',
          headers: {
              'Authorization': `Bearer ${jwtToken}`,
              'Content-Type': 'application/json'
          },
      })
      .then(response => {
          if (!response.ok) {
              return response.text().then(text => {
                  throw new Error(text);
              });
          }
          return response.json();
      })
      .then(data => {
          console.log('Response data:', data);
          displayMessageForPersonalDataUpdate(data);
      })
      .catch(error => {
          console.error('Error:', error);
          displayErrorMessageForPersonalDataUpdate(error.message);
      });
  }

function returnMainMenu(event) {
    const jwtToken = getCookie('JWT_TOKEN');
    if (!jwtToken) {
        console.error('JWT token not found');
        return;
    }
    fetch('http://localhost:8080/admin_office', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${jwtToken}`
        }
    })
    .then(response => {
        if (response.ok) {
            window.location.href = 'http://localhost:8080/admin_office';
        } else {
            throw new Error('Invalid token or request failed');
        }
    })
    .catch(error => {
        console.error('Error:', error);
    });
}











