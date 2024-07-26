    // Function to close all dropdowns except the one passed as argument
  function closeAllDropdowns(exceptDropdownId) {
      const dropdowns = document.querySelectorAll('.dropdown-content');
      dropdowns.forEach(dropdown => {
          if (dropdown.id !== exceptDropdownId) {
              dropdown.style.display = 'none';
          }
      });
  }
  // Login form visibility
  document.getElementById('loginButton').addEventListener('click', function() {
      const loginFormContainer = document.getElementById('loginFormContainer');
      const isVisible = loginFormContainer.style.display === 'block';
      closeAllDropdowns('loginFormContainer');
      loginFormContainer.style.display = isVisible ? 'none' : 'block';
  });
  // Registration form visibility
  document.getElementById('RegistrationButton').addEventListener('click', function() {
      const RegistrationFormContainer = document.getElementById('RegistrationFormContainer');
      const isVisible = RegistrationFormContainer.style.display === 'block';
      closeAllDropdowns('RegistrationFormContainer');
      RegistrationFormContainer.style.display = isVisible ? 'none' : 'block';
  });

  // Login form submission
  document.getElementById('loginForm').addEventListener('submit', function(event) {
      event.preventDefault();
      const phoneNumber = document.getElementById('phoneNumber').value;
      const password = document.getElementById('password').value;
      fetch('/login', {
          method: 'POST',
          headers: {
              'Content-Type': 'application/json'
          },
          body: JSON.stringify({ phoneNumber: phoneNumber, password: password })
      })
      .then(response => {
          if (response.redirected) {
              window.location.href = response.url;
          } else if (response.ok) {
              return response.json();
          } else {
              return response.json().then(error => {
                  throw new Error(error.message);
              });
          }
      })
      .catch(error => {
          const messageDiv = document.getElementById('message');
          messageDiv.innerText = 'Login failed: ' + error.message;
          messageDiv.style.color = 'red';
          messageDiv.style.display = 'block';
      });
  });

  // Registration form submission
  document.getElementById('registrationForm').addEventListener('submit', function(event) {
      event.preventDefault();
      const phoneNumber = document.getElementById('phoneNumberRegistration').value;
      const password = document.getElementById('passwordRegistration').value;
      const confirmPassword = document.getElementById('confirmPassword').value;
      const email = document.getElementById('email').value;
      const cardNumber = document.getElementById('cardNumber').value;
      const cardExpirationDate = document.getElementById('cardExpirationDate').value;
      const cvv = document.getElementById('cvv').value;
      const messageDiv = document.getElementById('message');
     if (password !== confirmPassword) {
      messageDiv.innerText = 'Passwords do not match!';
      messageDiv.style.color = 'red';
      messageDiv.style.display = 'block';
      return;
  }
      const registrationData = {
          phoneNumber: phoneNumber,
          password: password,
          email: email,
          userBankCard: {
              cardNumber: cardNumber,
              cardExpirationDate: cardExpirationDate,
              cvv: cvv
          }
      };
      fetch('/register', {
          method: 'POST',
          headers: {
              'Content-Type': 'application/json'
          },
          body: JSON.stringify(registrationData)
      })
      .then(response => {
          const messageDiv = document.getElementById('message');
          if (response.redirected) {
              window.location.href = response.url;
          } else if (response.ok) {
              return response.json();
          } else {
              return response.text().then(errorMessage => {
                  messageDiv.innerText = errorMessage;
                  messageDiv.style.color = 'red';
                  messageDiv.style.display = 'block';
                  throw new Error(errorMessage);
              });
          }
      })
      .catch(error => {
          console.error('Registration failed: ', error.message);
      });
  });