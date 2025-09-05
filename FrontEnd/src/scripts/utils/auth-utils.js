// ============= AUTH & INITIALIZATION =============
function checkAuthStatus() {
  const accessToken = localStorage.getItem('accessToken');
  const userInfo = localStorage.getItem('userInfo');

  if (!accessToken || !userInfo) {
    window.location.href = 'login.html';
    return false;
  }

  try {
    appState.user = JSON.parse(userInfo);
    updateUserDisplay();
    return true;
  } catch (error) {
    console.error('Error parsing user info:', error);
    localStorage.clear();
    window.location.href = 'login.html';
    return false;
  }
}

function updateUserDisplay() {
  if (appState.user) {
    document.getElementById('user-name').textContent = appState.user.fullName;
  }
}

async function logout() {
  try {
    const accessToken = localStorage.getItem('accessToken');
    const refreshToken = localStorage.getItem('refreshToken');

    if (accessToken && refreshToken) {
      await ApiService.logout({ accessToken, refreshToken });
    }
  } catch (error) {
    console.error('Logout error:', error);
  } finally {
    localStorage.clear();
    window.location.href = 'login.html';
  }
}


// ============= EVENT LISTENERS =============
document.addEventListener('DOMContentLoaded', () => {
  // Check authentication
  if (!checkAuthStatus()) return;

  // Load initial data
  loadCategories();

  // Form event listeners
  document.getElementById('create-category-form').addEventListener('submit', handleCreateCategory);
  document.getElementById('update-category-form').addEventListener('submit', handleUpdateCategory);
  document.getElementById('expense-form').addEventListener('submit', handleAddExpense);
  document.getElementById('update-expense-form').addEventListener('submit', handleUpdateExpense);

  // Close modals when clicking overlay
  document.querySelectorAll('.custom-alert-overlay').forEach(overlay => {
    overlay.addEventListener('click', (e) => {
      if (e.target === overlay) {
        overlay.classList.remove('active');
      }
    });
  });
});
