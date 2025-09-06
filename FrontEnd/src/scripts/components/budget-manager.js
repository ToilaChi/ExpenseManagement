// ============= BUDGET MANAGEMENT =============

let budgetUpdatePermissions = {}; // Cache permissions cho categories
let updateInfoMessage = '';

/**
 * Reset budgets của user hiện tại
 */
async function resetMyBudgets() {
  try {
    const confirmed = await showConfirmDialog(
      'Xác nhận reset ngân sách',
      'Bạn có chắc chắn muốn reset tất cả ngân sách của mình về giá trị ban đầu?'
    );

    if (!confirmed) return;

    showBudgetLoading(true);

    const response = await BudgetAPI.resetMyBudgets();

    await showCustomAlert(response.message || 'Reset ngân sách thành công');

    // Refresh data
    clearBudgetPermissionCache();
    await loadCategories();
    await loadUpdateInfo();

  } catch (error) {
    console.error('Error resetting budgets:', error);
    await showCustomAlert('Có lỗi xảy ra khi reset ngân sách');
  } finally {
    showBudgetLoading(false);
  }
}
