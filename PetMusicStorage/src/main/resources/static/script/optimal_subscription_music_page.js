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


function showMusicOptimalSubscription() {
    const jwtToken = getCookie('JWT_TOKEN');
    if (!jwtToken) {
        console.error('JWT token not found');
        return;
    }
    fetch('/free_subscription/list_optimal_subscription?subscriptionName=OPTIMAL', {
        headers: {
            'Authorization': `Bearer ${jwtToken}`
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.blob();
    })
    .then(blob => {
        const zip = new JSZip();
        return zip.loadAsync(blob).then(zip => {
            const container = document.getElementById('subscriptionDetails');
            container.innerHTML = '';
            zip.forEach((relativePath, zipEntry) => {
                zipEntry.async('blob').then(fileBlob => {
                    const audioElement = document.createElement('audio');
                    audioElement.controls = true;
                    audioElement.src = URL.createObjectURL(fileBlob);
                    const trackName = document.createElement('div');
                    trackName.textContent = zipEntry.name;
                    const trackContainer = document.createElement('div');
                    trackContainer.appendChild(trackName);
                    trackContainer.appendChild(audioElement);
                    container.appendChild(trackContainer);
                });
            });
        });
    })
    .catch(error => {
        console.error('Failed to fetch free subscription music files: ', error);
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

showMusicOptimalSubscription();