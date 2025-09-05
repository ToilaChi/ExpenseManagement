// ============= CHART MANAGEMENT =============

let expenseChart = null;

// Initialize Chart.js pie chart
function initializeChart() {
  const ctx = document.getElementById('expense-pie-chart').getContext('2d');

  expenseChart = new Chart(ctx, {
    type: 'pie',
    data: {
      labels: [],
      datasets: [{
        data: [],
        backgroundColor: [],
        borderColor: '#ffffff',
        borderWidth: 2
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          display: false // Tắt legend mặc định, dùng custom legend
        },
        tooltip: {
          callbacks: {
            label: function (context) {
              const label = context.label || '';
              const value = formatCurrency(context.parsed);
              // Kiểm tra percentages có tồn tại không
              const percentage = context.dataset.percentages && context.dataset.percentages[context.dataIndex]
                ? context.dataset.percentages[context.dataIndex]
                : '0';
              return `${label}: ${value}₫ (${percentage}%)`;
            }
          }
        }
      },
      onClick: (event, activeElements) => {
        if (activeElements.length > 0) {
          const dataIndex = activeElements[0].index;
          const categoryName = expenseChart.data.labels[dataIndex];
          // Có thể thêm action khi click vào slice
          console.log(`Clicked on category: ${categoryName}`);
        }
      }
    }
  });
}

// Load chart data from API
async function loadChartData() {
  console.log('loadChartData called'); // Debug log

  try {
    const { type, date } = appState.currentFilter;
    console.log('Loading chart data with:', { type, date }); // Debug log

    showChartLoading(true);

    const response = await ExpenseAPI.getChartData(type, date);
    console.log('Chart API response:', response); // Debug log

    // Match với response format của bạn
    if (response.data && response.stats) {
      updateChart(response.data, response.stats);
      updateChartSummary(response.stats);
    } else {
      console.error('Invalid chart data format:', response);
      showEmptyChart();
    }

  } catch (error) {
    console.error('Load chart data error:', error);
    await showCustomAlert('Có lỗi xảy ra khi tải dữ liệu biểu đồ');
    showEmptyChart();
  } finally {
    showChartLoading(false);
  }
}

// Update chart with new data
function updateChart(categoryData, summaryStats) {
  if (!expenseChart) {
    initializeChart();
  }

  // Prepare data for Chart.js - match với field names của bạn
  const labels = categoryData.map(item => item.categoryName);
  const data = categoryData.map(item => parseFloat(item.totalSpent));  // totalSpent instead of totalAmount
  const colors = categoryData.map(item => item.colorHex);
  const percentages = categoryData.map(item => parseFloat(item.percentage));

  // Update chart data
  expenseChart.data.labels = labels;
  expenseChart.data.datasets[0].data = data;
  expenseChart.data.datasets[0].backgroundColor = colors;
  expenseChart.data.datasets[0].percentages = percentages;

  // Update chart
  expenseChart.update('active');

  // Update custom legend
  updateCustomLegend(categoryData);
}

// Update custom legend
function updateCustomLegend(categoryData) {
  const legendContainer = document.getElementById('chart-legend');
  legendContainer.innerHTML = '';

  categoryData.forEach((item, index) => {
    const legendItem = document.createElement('div');
    legendItem.className = 'legend-item';
    legendItem.innerHTML = `
            <div class="legend-color" style="background-color: ${item.colorHex}"></div>
            <div class="legend-text">
                <span class="legend-category">${item.categoryName}</span>
                <span class="legend-amount">${formatCurrency(item.totalSpent)}₫ (${item.percentage}%)</span>
                <span class="legend-count">${item.transactionCount} giao dịch</span>
            </div>
        `;

    // Add click event to highlight chart slice
    legendItem.addEventListener('click', () => {
      highlightChartSlice(index);
    });

    legendContainer.appendChild(legendItem);
  });
}

// Update chart summary info
function updateChartSummary(summaryStats) {
  console.log('Updating chart summary with:', summaryStats); // Debug log
  const totalElement = document.getElementById('total-expense-amount');
  if (totalElement) {
    totalElement.textContent = formatCurrency(summaryStats.grandTotal) + '₫';
  }
}

// Show empty chart when no data
function showEmptyChart() {
  if (!expenseChart) {
    initializeChart();
  }

  expenseChart.data.labels = ['Không có dữ liệu'];
  expenseChart.data.datasets[0].data = [1];
  expenseChart.data.datasets[0].backgroundColor = ['#e0e0e0'];
  expenseChart.update();

  document.getElementById('chart-legend').innerHTML =
    '<div class="empty-chart-message">Chưa có chi tiêu nào trong khoảng thời gian này</div>';

  document.getElementById('total-expense-amount').textContent = '0₫';
}

// Highlight specific chart slice
function highlightChartSlice(index) {
  if (expenseChart) {
    // Reset all elements
    expenseChart.setActiveElements([]);

    // Highlight selected element
    expenseChart.setActiveElements([{
      datasetIndex: 0,
      index: index
    }]);

    expenseChart.update('active');
  }
}

// Loading state for chart
function showChartLoading(show) {
  const chartContainer = document.querySelector('.chart-section');
  if (show) {
    chartContainer.classList.add('loading');
  } else {
    chartContainer.classList.remove('loading');
  }
}

// Updated filter function to include chart refresh
function setExpenseFilter(type, event) {
  // Kiểm tra event có tồn tại không
  if (!event) {
    console.warn('event missing in setExpenseFilter');
    // Tạo event giả để tránh crash
    event = { target: document.querySelector('.filter-button.active') };
  }

  console.log('setExpenseFilter called with:', type, event);

  // Remove active từ tất cả buttons
  document.querySelectorAll('.filter-button').forEach(btn => {
    btn.classList.remove('active');
  });
  document.querySelector(`[onclick="setExpenseFilter('${type}')"]`).classList.add('active');

  // Update state
  appState.currentFilter.type = type;
  appState.currentFilter.page = 0;

  // Set default date based on filter type
  const now = new Date();
  switch (type) {
    case 'day':
      appState.currentFilter.date = now.toISOString().slice(0, 10); // 2025-09-05
      break;
    case 'week':
      const monday = new Date(now);
      monday.setDate(now.getDate() - now.getDay() + 1);
      appState.currentFilter.date = monday.toISOString().slice(0, 10);
      break;
    case 'month':
      appState.currentFilter.date = now.toISOString().slice(0, 7); // 2025-09
      break;
  }

  console.log('Updated filter state:', appState.currentFilter);

  // QUAN TRỌNG: Reload cả expense list và chart data
  if (typeof loadExpenses === 'function') {
    loadExpenses();
  }
  loadChartData();
}
