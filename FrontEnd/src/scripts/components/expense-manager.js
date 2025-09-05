// ============= EXPENSE MANAGEMENT =============

async function loadExpenses() {
  try {
    showExpenseLoading(true);
    const response = await ExpenseAPI.getExpenseList(
      appState.currentFilter.type,
      appState.currentFilter.date,
      appState.currentFilter.page,
      appState.currentFilter.size
    );

    if (response.data) {
      appState.expenses = response.data;
      renderExpenseTable();
      updatePaginationInfo(response.pagination);
    } else {
      await showCustomAlert(response.message || 'Không thể tải danh sách chi tiêu');
    }
  } catch (error) {
    console.error('Load expenses error:', error);
    await showCustomAlert('Có lỗi xảy ra khi tải danh sách chi tiêu');
  } finally {
    showExpenseLoading(false);
  }
}

function renderExpenseTable() {
  const tbody = document.querySelector('#expense-table tbody');
  tbody.innerHTML = '';

  if (!appState.expenses || appState.expenses.length === 0) {
    tbody.innerHTML = `
            <tr>
                <td colspan="5" style="text-align: center; color: #666; padding: 20px;">
                    Chưa có chi tiêu nào trong khoảng thời gian này
                </td>
            </tr>
        `;
    return;
  }

  appState.expenses.forEach(expense => {
    const row = createExpenseRow(expense);
    tbody.appendChild(row);
  });
}

function createExpenseRow(expense) {
  const row = document.createElement('tr');
  row.innerHTML = `
        <td>
            <div style="display: flex; align-items: center;">
                <div style="width: 12px; height: 12px; border-radius: 50%; background-color: ${expense.colorHex}; margin-right: 8px;"></div>
                ${expense.categoryName}
            </div>
        </td>
        <td>${formatDate(expense.createdAt)}</td>
        <td style="font-weight: bold; color: #e74c3c;">${formatCurrency(expense.amount)}đ</td>
        <td>${expense.description || '-'}</td>
        <td>
            <button class="btn-action btn-update" onclick="openUpdateExpenseModal(${expense.id}, '${expense.categoryName}', ${expense.amount}, '${expense.description || ''}')">
                Cập nhật
            </button>
            <button class="btn-action btn-delete" onclick="deleteExpense(${expense.id}, '${expense.categoryName}')">
                Xóa
            </button>
        </td>
    `;
  return row;
}

function updatePaginationInfo(pagination) {
  if (!pagination) return;

  const info = document.getElementById('pagination-info');
  info.textContent = `Trang ${pagination.currentPage + 1} / ${pagination.totalPages} (${pagination.totalItems} chi tiêu)`;
}

function showExpenseLoading(show) {
  const table = document.getElementById('expense-table');
  if (show) {
    table.classList.add('loading');
  } else {
    table.classList.remove('loading');
  }
}

// Filter functions
function setExpenseFilter(type) {
  // Update UI
  document.querySelectorAll('.filter-button').forEach(btn => {
    btn.classList.remove('active');
  });
  document.querySelector(`[onclick="setExpenseFilter('${type}')"]`).classList.add('active');

  // Update state
  appState.currentFilter.type = type;
  appState.currentFilter.page = 0; // Reset to first page

  // Set default date based on filter type
  const now = new Date();
  switch (type) {
    case 'day':
      appState.currentFilter.date = now.toISOString().split('T')[0]; // YYYY-MM-DD
      break;
    case 'week':
      // Get Monday of current week
      const monday = new Date(now);
      monday.setDate(now.getDate() - now.getDay() + 1);
      appState.currentFilter.date = monday.toISOString().split('T')[0];
      break;
    case 'month':
      appState.currentFilter.date = now.toISOString().slice(0, 7); // YYYY-MM
      break;
  }

  loadExpenses();
}
