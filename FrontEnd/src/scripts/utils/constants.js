let appState = {
    categories: [],
    user: null,
    currentFilter: {
        type: 'month',
        date: '2025-01',
        page: 0,
        size: 20
    }
};

// ============= UTILITY FUNCTIONS =============
function formatCurrency(amount) {
    return parseFloat(amount || 0).toLocaleString('vi-VN');
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
    });
}

function showLoading(show) {
    const container = document.getElementById('summary-cards');
    if (show) {
        container.classList.add('loading');
    } else {
        container.classList.remove('loading');
    }
}

function showCustomAlert(message) {
    const messageElement = document.getElementById('alert-message');
    const okBtn = document.getElementById('alert-ok-btn');

    messageElement.textContent = message;
    openCustomAlert('custom-alert-overlay');

    return new Promise((resolve) => {
        okBtn.onclick = () => {
            closeCustomAlert('custom-alert-overlay');
            resolve();
        };
    });
}
