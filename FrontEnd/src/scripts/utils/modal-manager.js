// ============= MODAL MANAGEMENT =============
function openCustomAlert(overlayId) {
  document.getElementById(overlayId).classList.add('active');
}

function closeCustomAlert(overlayId) {
  document.getElementById(overlayId).classList.remove('active');
}

function openUpdateCategoryModal(categoryId, currentName, currentBudget) {
  document.getElementById('update-category-id').value = categoryId;
  document.getElementById('update-category-name').value = '';
  document.getElementById('update-category-budget').value = currentBudget;
  openCustomAlert('update-category-overlay');
}

function openExpenseModal(categoryId, categoryName) {
  document.getElementById('expense-category-id').value = categoryId;
  document.getElementById('expense-amount').value = '';
  document.getElementById('expense-description').value = '';
  document.getElementById('expense-modal-title').textContent = `Chi tiêu - ${categoryName}`;
  openCustomAlert('expense-overlay');
}

function openUpdateExpenseModal(expenseId, categoryName, amount, description) {
  document.getElementById('update-expense-id').value = expenseId;
  document.getElementById('update-expense-category').value = categoryName;
  document.getElementById('update-expense-amount').value = amount;
  document.getElementById('update-expense-description').value = description;
  document.getElementById('update-expense-modal-title').textContent = `Cập nhật chi tiêu - ${categoryName}`;
  openCustomAlert('update-expense-overlay');
}

