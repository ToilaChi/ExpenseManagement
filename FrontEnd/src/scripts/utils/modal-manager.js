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


