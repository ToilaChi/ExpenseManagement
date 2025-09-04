// ============= FORM HANDLERS =============
async function handleCreateCategory(event) {
  event.preventDefault();

  const categoryName = document.getElementById('new-category-name').value.trim();
  const allocatedBudget = document.getElementById('new-category-budget').value;

  if (!categoryName || !allocatedBudget) {
    await showCustomAlert('Vui lòng điền đầy đủ thông tin');
    return;
  }

  try {
    showLoading(true);
    const response = await CategoryAPI.createCategory(categoryName, allocatedBudget);

    if (response.data) {
      await showCustomAlert(response.message || 'Tạo danh mục thành công');
      closeCustomAlert('create-category-overlay');
      await loadCategories();
    } else {
      await showCustomAlert(response.message || 'Không thể tạo danh mục');
    }
  } catch (error) {
    console.error('Create category error:', error);
    await showCustomAlert('Có lỗi xảy ra khi tạo danh mục');
  } finally {
    showLoading(false);
  }
}

async function handleUpdateCategory(event) {
  event.preventDefault();

  const categoryId = document.getElementById('update-category-id').value;
  const categoryName = document.getElementById('update-category-name').value.trim();
  const budget = document.getElementById('update-category-budget').value;

  if (!categoryName && !budget) {
    await showCustomAlert('Vui lòng nhập ít nhất một thông tin cần cập nhật');
    return;
  }

  try {
    showLoading(true);
    const response = await CategoryAPI.updateCategory(
      categoryId,
      categoryName || null,
      budget || null
    );

    if (response.data) {
      await showCustomAlert(response.message || 'Cập nhật thành công');
      closeCustomAlert('update-category-overlay');
      await loadCategories();
    } else {
      await showCustomAlert(response.message || 'Không thể cập nhật danh mục');
    }
  } catch (error) {
    console.error('Update category error:', error);
    await showCustomAlert('Có lỗi xảy ra khi cập nhật danh mục');
  } finally {
    showLoading(false);
  }
}

async function handleAddExpense(event) {
  event.preventDefault();

  const categoryId = document.getElementById('expense-category-id').value;
  const amount = document.getElementById('expense-amount').value;
  const description = document.getElementById('expense-description').value.trim();

  if (!amount || parseFloat(amount) <= 0) {
    await showCustomAlert('Vui lòng nhập số tiền hợp lệ');
    return;
  }

  // TODO: Implement expense API call
  await showCustomAlert('Chức năng thêm chi tiêu sẽ được hoàn thiện sau');
  closeCustomAlert('expense-overlay');
}

async function deleteCategory(categoryId, categoryName) {
  const confirmed = confirm(`Bạn có chắc muốn xóa danh mục "${categoryName}"?`);
  if (!confirmed) return;

  try {
    showLoading(true);
    const response = await CategoryAPI.deleteCategory(categoryId);

    await showCustomAlert(response.message || 'Xóa danh mục thành công');
    await loadCategories();
  } catch (error) {
    console.error('Delete category error:', error);
    await showCustomAlert('Có lỗi xảy ra khi xóa danh mục');
  } finally {
    showLoading(false);
  }
}
