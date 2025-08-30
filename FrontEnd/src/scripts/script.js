// --- BẢNG MÀU VÀ TRẠNG THÁI ỨNG DỤNG ---
const colorPalette = [
    { cardClass: 'card-green', hex: '#93c47d' },
    { cardClass: 'card-cyan', hex: '#76a5af' },
    { cardClass: 'card-orange', hex: '#f6b26b' },
    { cardClass: 'card-red', hex: '#ef9a9a' },
    { cardClass: 'card-purple', hex: '#ce93d8' },
    { cardClass: 'card-yellow', hex: '#f9d15c' }
];
let appState = { categories: [] };
let nextColorIndex = 0;

// --- CÁC HÀM CHỨC NĂNG ---

async function fakeApiCall(data) {
    console.log("Đang gọi API với dữ liệu:", data);
    await new Promise(resolve => setTimeout(resolve, 500)); 
    console.log("API call thành công!");
    return { success: true, message: "Cập nhật thành công" };
}

function updateStatsFromCards() {
    const allCards = document.querySelectorAll('.summary-cards .card:not(:first-child):not(.add-card)');
    const expensesData = [];
    allCards.forEach(card => {
        const categoryName = card.querySelector('.card-header').textContent;
        const amountText = card.querySelector('.card-body').textContent;
        const amount = parseInt(amountText.replace(/[.,\s]|vnd/g, '')) || 0;
        if (categoryName) {
            expensesData.push({ category: categoryName, amount: amount });
        }
    });
    updateChartAndTable({ expenses: expensesData });
}

async function updateBalance() {
    document.getElementById('balance').textContent = "Đang tải...";
    try {
        await fakeApiCall({ action: 'getBalance' });
        const newBalance = Math.floor(Math.random() * 10000000);
        document.getElementById('balance').textContent = newBalance.toLocaleString('vi-VN') + ' vnd';
    } catch (error) {
        document.getElementById('balance').textContent = "Lỗi!";
    }
}

async function updateCategory(categoryId) {
    const element = document.getElementById(categoryId);
    if (!element) return;
    element.textContent = "Đang tải...";
    try {
        await fakeApiCall({ action: 'getCategory', id: categoryId });
        const newAmount = Math.floor(Math.random() * 2000000);
        element.textContent = newAmount.toLocaleString('vi-VN') + ' vnd';
        updateStatsFromCards();
    } catch (error) {
        element.textContent = "Lỗi!";
    }
}

function showCustomPrompt(title) {
    const overlay = document.getElementById('custom-prompt-overlay');
    const titleElement = document.getElementById('prompt-title');
    const inputElement = document.getElementById('prompt-input');
    const confirmBtn = document.getElementById('prompt-confirm-btn');
    const cancelBtn = document.getElementById('prompt-cancel-btn');
    titleElement.textContent = title;
    inputElement.value = '';
    overlay.classList.add('active');
    setTimeout(() => inputElement.focus(), 100);
    return new Promise((resolve) => {
        const closePrompt = (value) => {
            overlay.classList.remove('active');
            confirmBtn.onclick = null;
            cancelBtn.onclick = null;
            overlay.onclick = null;
            resolve(value);
        };
        confirmBtn.onclick = () => closePrompt(inputElement.value);
        cancelBtn.onclick = () => closePrompt(null);
        overlay.onclick = (event) => { if (event.target === overlay) closePrompt(null); };
    });
}

async function addCategory() {
    const categoryName = await showCustomPrompt("Nhập tên danh mục mới");
    if (categoryName && categoryName.trim() !== '') {
        const trimmedName = categoryName.trim();
        const newCardId = trimmedName.toLowerCase().replace(/\s+/g, '-');
        if (appState.categories.some(c => c.id === newCardId)) {
            alert("Danh mục này đã tồn tại!");
            return;
        }
        const color = colorPalette[nextColorIndex % colorPalette.length];
        nextColorIndex++;
        const newCategory = { id: newCardId, name: trimmedName, color: color };
        appState.categories.push(newCategory);
        const addCardButton = document.querySelector('.add-card');
        const newCard = document.createElement('div');
        newCard.classList.add('card', color.cardClass);
        newCard.innerHTML = `
            <div class="card-header">${trimmedName}</div>
            <button class="delete-btn" onclick="deleteCategory(this)">&times;</button>
            <div class="card-body" id="${newCardId}">0 vnd</div>
            <button class="card-button" onclick="updateCategory('${newCardId}')">Cập nhật</button>
        `;
        addCardButton.parentNode.insertBefore(newCard, addCardButton);
        updateStatsFromCards();
    }
}

function deleteCategory(buttonElement) {
    const cardToDelete = buttonElement.closest('.card');
    if (cardToDelete) {
        const categoryName = cardToDelete.querySelector('.card-header').textContent;
        const categoryId = cardToDelete.querySelector('.card-body').id;
        if (confirm(`Bạn có chắc chắn muốn xóa mục "${categoryName}" không?`)) {
            cardToDelete.remove();
            appState.categories = appState.categories.filter(c => c.id !== categoryId);
            updateStatsFromCards();
        }
    }
}

function logout() {
    console.log("Thực hiện đăng xuất...");
    // Chuyển hướng đến trang login.html trong cùng thư mục /public
    window.location.href = 'login.html';
}

function updateChartAndTable(statsData) {
    const tableBody = document.getElementById('expense-table-body');
    const legendContainer = document.getElementById('legend-container');
    const pieChart = document.querySelector('.pie-chart');
    tableBody.innerHTML = '';
    legendContainer.innerHTML = '';

    if (!statsData.expenses || statsData.expenses.length === 0 || statsData.expenses.every(e => e.amount === 0)) {
        tableBody.innerHTML = '<tr><td colspan="2">Không có dữ liệu chi tiêu.</td></tr>';
        pieChart.style.backgroundImage = 'conic-gradient(#e0e0e0 0% 100%)';
        legendContainer.innerHTML = `<div class="legend-item"><span class="legend-color" style="background-color: #e0e0e0;"></span>Chưa có dữ liệu</div>`;
        return;
    }
    statsData.expenses.forEach(item => {
        tableBody.innerHTML += `<tr><td>${item.category}</td><td>${item.amount.toLocaleString('vi-VN')} đ</td></tr>`;
    });

    const totalAmount = statsData.expenses.reduce((sum, item) => sum + item.amount, 0);
    let gradientString = 'conic-gradient(';
    let currentPercentage = 0;
    statsData.expenses.forEach((item) => {
        const categoryInfo = appState.categories.find(c => c.name === item.category);
        const colorHex = categoryInfo ? categoryInfo.color.hex : '#808080';
        if(item.amount > 0) {
            const percentage = (item.amount / totalAmount) * 100;
            gradientString += `${colorHex} ${currentPercentage}% ${currentPercentage + percentage}%, `;
            currentPercentage += percentage;
        }
        legendContainer.innerHTML += `<div class="legend-item"><span class="legend-color" style="background-color: ${colorHex};"></span> ${item.category}</div>`;
    });
    gradientString = gradientString.slice(0, -2) + ')';
    pieChart.style.backgroundImage = gradientString;
}

function setActiveFilter(clickedButton) {
    if (!clickedButton) return;
    document.querySelectorAll('.stats-filters .filter-button').forEach(button => {
        button.classList.remove('active');
    });
    clickedButton.classList.add('active');
}

// KHỞI TẠO TRẠNG THÁI KHI TẢI TRANG
document.addEventListener('DOMContentLoaded', () => {
    const existingCards = document.querySelectorAll('.summary-cards .card:not(:first-child):not(.add-card)');
    existingCards.forEach(card => {
        const name = card.querySelector('.card-header').textContent;
        const id = card.querySelector('.card-body').id;
        const color = colorPalette[nextColorIndex % colorPalette.length];
        nextColorIndex++;
        appState.categories.push({ id: id, name: name, color: color });
    });
    updateStatsFromCards();
});

// Các hàm thống kê mẫu
async function getDailyStats(clickedButton) {
    const fakeData = { expenses: [{ category: 'Ăn uống', amount: 120000 }, { category: 'Cafe & đi chơi', amount: 45000 }]};
    updateChartAndTable(fakeData);
    setActiveFilter(clickedButton);
}
async function getWeeklyStats(clickedButton) {
    const fakeData = { expenses: [{ category: 'Ăn uống', amount: 1850000 }, { category: 'Cafe & đi chơi', amount: 750000 }, { category: 'Xăng xe', amount: 200000 }]};
    updateChartAndTable(fakeData);
    setActiveFilter(clickedButton);
}
async function getMonthlyStats(clickedButton) {
     const fakeData = { expenses: [{ category: 'Ăn uống', amount: 3500000 }, { category: 'Xăng xe', amount: 800000 }]};
    updateChartAndTable(fakeData);
    setActiveFilter(clickedButton);
}