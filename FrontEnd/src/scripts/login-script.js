document.addEventListener("DOMContentLoaded", () => {
  const loginTab = document.getElementById("login-tab");
  const signupTab = document.getElementById("signup-tab");
  const loginForm = document.getElementById("login-form");
  const signupForm = document.getElementById("signup-form");

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

  loginForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    const username = document.getElementById("login-username").value;
    await showCustomAlert(`Chào mừng trở lại, ${username}!`);
    // Chuyển hướng đến trang index.html trong cùng thư mục /public
    window.location.href = 'index.html';
  });

  signupForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    const fullname = document.getElementById("fullname").value;
    const password = document.getElementById("password").value;
    const confirmPassword = document.getElementById("confirm-password").value;
    if (password !== confirmPassword) {
      await showCustomAlert("Mật khẩu không khớp!");
      return;
    }
    await showCustomAlert(`Tài khoản cho ${fullname} đã được tạo!`);
    loginTab.click();
  });
});