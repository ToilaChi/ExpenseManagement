// ============= CATEGORY MANAGEMENT =============
async function loadCategories() {
  try {
    showLoading(true);
    const response = await CategoryAPI.getCategories();

    if (response.data) {
      appState.categories = response.data;
      renderCategories();
    } else {
      await showCustomAlert(response.message || 'Không thể tải danh mục');
    }
  } catch (error) {
    console.error('Load categories error:', error);
    await showCustomAlert('Có lỗi xảy ra khi tải danh mục');
  } finally {
    showLoading(false);
  }
}

function renderCategories() {
  const container = document.getElementById('summary-cards');
  container.innerHTML = '';

  // Render categories theo thứ tự: INCOME trước, EXPENSE sau
  appState.categories.forEach(category => {
    const card = createCategoryCard(category);
    container.appendChild(card);
  });

  // Add "+" card cho EXPENSE categories
  const addCard = createAddCard();
  container.appendChild(addCard);
}

function createCategoryCard(category) {
  const card = document.createElement('div');
  const isIncome = category.expenseType === 'INCOME';

  // Áp dụng class CSS tương ứng với màu từ BE
  card.className = `card dynamic-color`;
  card.style.setProperty('--dynamic-color', category.colorHex);

  const formattedCurrent = formatCurrency(category.currentBudget);
  const formattedAllocated = formatCurrency(category.allocatedBudget);

  // Tính progress cho expense categories
  let progressHTML = '';
  if (!isIncome && category.allocatedBudget > 0) {
    const progressPercent = Math.min((category.allocatedBudget - category.currentBudget) / category.allocatedBudget * 100, 100);
    progressHTML = `
            <div class="progress-bar">
                <div class="progress-fill" style="width: ${progressPercent}%; background-color: ${category.colorHex};"></div>
            </div>
        `;
  }

  card.innerHTML = `
        <div class="card-header">${category.name}</div>
        <div class="card-body ${isIncome ? 'balance-display' : ''}">
            ${formattedCurrent} vnd
            ${!isIncome ? `<div class="budget-progress">/${formattedAllocated} vnd</div>` : ''}
            ${progressHTML}
        </div>
        ${!isIncome ? `
            <div class="card-actions">
                <button class="update-btn" onclick="openUpdateCategoryModal(${category.id}, '${category.name.replace(/'/g, "\\'")}', ${category.allocatedBudget})">
                    Cập nhật
                </button>
                <button class="expense-btn" onclick="openExpenseModal(${category.id}, '${category.name.replace(/'/g, "\\'")}')">
                    Chi
                </button>
                <button class="delete-btn" onclick="deleteCategory(${category.id}, '${category.name.replace(/'/g, "\\'")}')">
                    Xóa
                </button>
            </div>
        ` : `
            <div class="card-actions">
                <button class="update-btn" onclick="openUpdateCategoryModal(${category.id}, '${category.name.replace(/'/g, "\\'")}', ${category.currentBudget})" style="width: 100%;">
                    Cập nhật số dư
                </button>
            </div>
        `}
    `;

  return card;
}

function createAddCard() {
  const addCard = document.createElement('div');
  addCard.className = 'card add-card';
  addCard.onclick = () => openCustomAlert('create-category-overlay');

  addCard.innerHTML = `+`;

  return addCard;
}
