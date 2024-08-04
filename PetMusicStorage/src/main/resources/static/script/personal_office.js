function getCookie(name) {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
    }
    function showForm(formId) {
        const forms = document.querySelectorAll('.form-nav-list form');
        forms.forEach(form => form.style.display = 'none');
        const formToShow = document.getElementById(formId);
        if (formToShow) {
            formToShow.style.display = 'block';
        }
    }


document.addEventListener('click', function(event) {
    const target = event.target;
    const isClickInsideButton = target.closest('.button-nav-list');
    const isClickInsideForm = target.closest('.form-nav-list');
    const isClickInsideSubmenu = target.closest('.subscription-list');
    if (!isClickInsideSubmenu && !isClickInsideButton && !isClickInsideForm) {
        document.querySelector('.subscription-list').style.display = 'none';
    }
    if (isClickInsideButton && target.textContent.trim() === 'Subscriptions') {
        const subscriptionList = document.querySelector('.subscription-list');
        const currentDisplay = getComputedStyle(subscriptionList).display;
        subscriptionList.style.display = (currentDisplay === 'none' || currentDisplay === '') ? 'block' : 'none';
    }
});


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
     })
     .catch(error => {
         console.error('Failed to fetch subscriptions: ', error);
         displayErrorMessage(error.message);
     });
 }

//show subscription details
function displayAllSubscription() {
    const findAllSubmenu = document.getElementById('findAllSubmenu');
    if (!findAllSubmenu) {
        console.error('Element with id "submenuSubscribe" not found.');
        return;
    }
    findAllSubmenu.innerHTML = '<ul>';
    subscriptions.forEach(subscription => {
        const subscriptionName = subscription.subscriptionName || 'Unknown';
        findAllSubmenu.innerHTML += `
            <li class="submenu-all-subscriptions-item">
                <p class="submenu-all-subscriptions-name">${subscriptionName}</p>
                <button class="submenu-all-subscriptions-button" onclick="submitSubscriptionForm(event, '${subscriptionName}')">Show details</button>
            </li>
        `;
    });
    findAllSubmenu.innerHTML += '</ul>';
    findAllSubmenu.style.display = 'block';
}








    // Call the function to fetch and display the subscription details
    showSubscriptionOnScreen();
    showSubscriptionEndTimeOnScreen()
     checkUsersAutoRenewStatus();
    startUpdatingSubscriptionInfo(3);