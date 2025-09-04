document.addEventListener("DOMContentLoaded", () => {
  const loginTab = document.getElementById("login-tab");
  const signupTab = document.getElementById("signup-tab");
  const loginForm = document.getElementById("login-form");
  const signupForm = document.getElementById("signup-form");

  window.togglePassword = function (inputId, toggleButton) {
    const passwordInput = document.getElementById(inputId);
    const eyeIcon = toggleButton.querySelector('.eye-icon');

    if (passwordInput.type === 'password') {
      passwordInput.type = 'text';
      eyeIcon.textContent = '🙈'; // Hide icon
      toggleButton.setAttribute('aria-label', 'Ẩn mật khẩu');
    } else {
      passwordInput.type = 'password';
      eyeIcon.textContent = '👁'; // Show icon
      toggleButton.setAttribute('aria-label', 'Hiện mật khẩu');
    }
  };

  function showCustomAlert(message) {
    const overlay = document.getElementById('custom-alert-overlay');
    const messageElement = document.getElementById('alert-message');
    const okBtn = document.getElementById('alert-ok-btn');

    messageElement.textContent = message;
    overlay.classList.add('active');

    return new Promise((resolve) => {
      okBtn.onclick = () => {
        overlay.classList.remove('active');
        resolve();
      };
    });
  }

  function showLoading(show) {
    const submitButtons = document.querySelectorAll('button[type="submit"]');
    submitButtons.forEach(btn => {
      btn.disabled = show;
      btn.textContent = show ? 'Đang xử lý...' : (btn.id.includes('login') ? 'Đăng nhập' : 'Đăng ký');
    });
  }

  loginTab.addEventListener("click", () => {
    loginTab.classList.add("active");
    signupTab.classList.remove("active");
    loginForm.classList.add("active");
    signupForm.classList.remove("active");
  });

  signupTab.addEventListener("click", () => {
    signupTab.classList.add("active");
    loginTab.classList.remove("active");
    signupForm.classList.add("active");
    loginForm.classList.remove("active");
  });

  //Handle Login
  loginForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    showLoading(true);

    const phone = document.getElementById("login-phone").value;
    const password = document.getElementById("login-password").value;

    try {
      const response = await ApiService.login({ phone, password });

      if (response.data) {
        localStorage.setItem("accessToken", response.data.accessToken);
        localStorage.setItem("refreshToken", response.data.refreshToken);
        localStorage.setItem('userInfo', JSON.stringify(response.data.account));

        await showCustomAlert(response.message || `Chào mừng trở lại, ${response.data.account.fullName}!`);
        window.location.href = "index.html";
      } else {
        await showCustomAlert(response.message || "Đăng nhập thất bại. Vui lòng thử lại.");
      }
    } catch (error) {
      await showCustomAlert('Có lỗi xảy ra khi đăng nhập. Vui lòng thử lại!');
    } finally {
      showLoading(false);
    }
  });

  //Handle registration
  signupForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    showLoading(true);

    const fullName = document.getElementById("fullname").value;
    const phone = document.getElementById("phone").value;
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;
    const confirmPassword = document.getElementById("confirm-password").value;

    if (password !== confirmPassword) {
      await showCustomAlert("Mật khẩu không khớp!");
      showLoading(false);
      return;
    }

    try {
      const response = await ApiService.register({ fullName, phone, email, password });

      if (response.data) {
        await showCustomAlert(response.message || `Tài khoản cho ${fullName} đã được tạo!`);
        loginTab.click();
        signupForm.reset();
      } else {
        await showCustomAlert(response.message || "Đăng ký thất bại. Vui lòng thử lại.");
      }
    } catch (error) {
      await showCustomAlert('Có lỗi xảy ra khi đăng ký. Vui lòng thử lại!');
    } finally {
      showLoading(false);
    }
  });
});