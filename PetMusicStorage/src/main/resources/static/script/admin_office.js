function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
}

document.getElementById('usersButton').addEventListener('click', function() {
    const token = getCookie('token');
    fetch('/users', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
        if (response.ok) {
            window.location.href = '/users';
        } else {
            throw new Error('Failed to load users page');
        }
    })
        .catch(error => {
        console.error('Error:', error);
    });
});









