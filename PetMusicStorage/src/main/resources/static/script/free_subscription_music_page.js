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

function showMusicFreeSubscription() {
    const jwtToken = getCookie('JWT_TOKEN');
    if (!jwtToken) {
        console.error('JWT token not found');
        return;
    }
    fetch('/free_subscription/list_free_subscription?subscriptionName=FREE', {
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
        const audioElement = document.getElementById('audio');

        if (Array.isArray(data) && data.length > 0) {
            subscriptionDetails.innerHTML = ''; // Очистити список файлів, якщо вони вже є
            data.forEach(file => {
                const listItem = document.createElement('li');
                listItem.innerText = `Music File: ${file.musicFileName}`;
                listItem.onclick = function() {
                    audioElement.src = file.filePath;
                    audioElement.play();
                };
                subscriptionDetails.appendChild(listItem);
            });
        } else {
            subscriptionDetails.innerText = 'No music files found for the FREE subscription.';
        }
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






//const audio = new Audio('/mp3/free_subscription.mp3');
//
//// Функція для відтворення аудіо
//function playAudio() {
//    audio.play();
//}
//
//// Функція для зупинки аудіо
//function pauseAudio() {
//    audio.pause();
//}
//
//// Додаємо обробники подій для кнопок
//document.getElementById('playAudio').addEventListener('click', playAudio);
//document.getElementById('pauseAudio').addEventListener('click', pauseAudio);

  // Call the function to fetch and display the subscription details
showMusicFreeSubscription();