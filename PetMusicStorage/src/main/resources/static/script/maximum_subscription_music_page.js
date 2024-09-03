function getCookie(name) {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
    }

    function closeAllMenus() {
        const menus = [, 'MeinMenu', 'logOut'];
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
document.getElementById('LogOutButton').addEventListener('click', function() {
    toggleMenu('logOut');
});

document.getElementById('MeinMenuButton').addEventListener('click', function() {
    toggleMenu('MeinMenu');
});

function showMusicMaximumSubscription() {
    const jwtToken = getCookie('JWT_TOKEN');
    if (!jwtToken) {
        console.error('JWT token not found');
        return;
    }
    const spinner = document.getElementById('spinner');
    const container = document.getElementById('subscriptionDetails');
    spinner.style.display = 'block';
    container.innerHTML = '';
    fetch('/maximum_subscription/list_maximum_subscription?subscriptionName=MAXIMUM', {
        headers: {
            'Authorization': `Bearer ${jwtToken}`
        }
    })
    .then(response => {
        if (response.status === 404) {
            const emptyMessage = document.createElement('div');
            emptyMessage.textContent = 'Список порожній!';
            emptyMessage.className = 'header-text';
            container.appendChild(emptyMessage);
            spinner.style.display = 'none';
            return;
        }
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.blob();
    })
    .then(blob => {
        if (blob) {
            const zip = new JSZip();
            return zip.loadAsync(blob).then(zip => {
                container.innerHTML = '';
                const audioElements = [];
                if (Object.keys(zip.files).length === 0) {
                    const emptyMessage = document.createElement('div');
                    emptyMessage.textContent = 'List is empty!';
                    emptyMessage.className = 'header-text';
                    container.appendChild(emptyMessage);
                    spinner.style.display = 'none';
                    return;
                }
                zip.forEach((relativePath, zipEntry) => {
                    zipEntry.async('blob').then(fileBlob => {
                        const audioElement = document.createElement('audio');
                        audioElement.controls = true;
                        audioElement.src = URL.createObjectURL(fileBlob);
                        audioElement.dataset.index = audioElements.length;
                        const trackName = document.createElement('div');
                        trackName.textContent = zipEntry.name;
                        trackName.className = 'header-text';
                        const trackContainer = document.createElement('div');
                        trackContainer.appendChild(trackName);
                        trackContainer.appendChild(audioElement);
                        container.appendChild(trackContainer);
                        audioElements.push(audioElement);
                        audioElement.addEventListener('ended', function() {
                            const nextIndex = (parseInt(audioElement.dataset.index) + 1) % audioElements.length;
                            audioElements[nextIndex].play();
                        });
                        audioElement.addEventListener('play', function() {
                            audioElements.forEach((el, index) => {
                                if (index !== parseInt(audioElement.dataset.index)) {
                                    el.pause();
                                    el.currentTime = 0;
                                }
                            });
                        });
                    });
                });
                spinner.style.display = 'none';
            });
        }
    })
    .catch(error => {
        console.error('Failed to fetch free subscription music files: ', error);
        spinner.style.display = 'none';
    });
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

function MeinMenu(event) {
    event.preventDefault();
    const jwtToken = getCookie('JWT_TOKEN');
    const payload = JSON.parse(atob(jwtToken.split('.')[1]));
    if (payload.role === 'admin') {
        window.location.href = '/admin_office';
    } else {
        window.location.href = '/personal_office';
    }
}

function updateJWTToken() {
    const jwtToken = getCookie('JWT_TOKEN');
   if (!jwtToken) {
       console.error('JWT token not found. User might not be authenticated.');
       return;
     }
    fetch('/update_cookie', {
        method: 'POST',
        headers: {
           'Authorization': `Bearer ${jwtToken}`
        },
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.text();
    })
    .then(responseMessage => {
        console.log(responseMessage);
        const newJwtToken = getCookie('JWT_TOKEN');
        if (newJwtToken) {
            console.log('JWT Token updated successfully');
        }
    })
    .catch(error => {
        console.error('There was a problem with the fetch operation:', error);
    });
}


function returnMeinMenu() {
    const jwtToken = getCookie('JWT_TOKEN');
    if (!jwtToken) {
        console.error('JWT Token not found');
        return;
    }
    try {
        const parts = jwtToken.split('.');
        if (parts.length !== 3) {
            console.error('JWT Token format is invalid');
            return;
        }
        const payload = JSON.parse(atob(parts[1]));
        if (payload.roles && payload.roles.includes('ROLE_MAXIMUM')) {
            console.log('User has OPTIMAL role');
        } else {
         console.log('time to go');
            window.location.href = '/personal_office';
        }
    } catch (error) {
        console.error('Error decoding JWT Token:', error);
    }
}

setInterval(returnMeinMenu, 60000);
setInterval(updateJWTToken, 60000);

showMusicMaximumSubscription();
