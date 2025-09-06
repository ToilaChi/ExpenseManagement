// ============= CHART MANAGEMENT =============
let expenseChart = null;
let dayPicker = null;
let weekPicker = null;
let monthPicker = null;

function getStartOfWeek(date) {
  const d = new Date(date);
  const day = d.getDay();
  const diff = d.getDate() - day + (day === 0 ? -6 : 1);
  return new Date(d.setDate(diff));
}

function formatDateToString(date) {
  return date.toISOString().split('T')[0];
}

function formatDateForMonth(date) {
  const d = new Date(date);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
}

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
        legend: { display: false },
        tooltip: {
          callbacks: {
            label: function (context) {
              const label = context.label || '';
              const value = formatCurrency(context.parsed);
              const percentage = context.dataset.percentages &&
                context.dataset.percentages[context.dataIndex] ?
                context.dataset.percentages[context.dataIndex] : '0';
              return `${label}: ${value}₫ (${percentage}%)`;
            }
          }
        }
      }
    }
  });
}

function initializeDatePicker() {
  // Calendar cho NGÀY
  dayPicker = flatpickr("#day-picker", {
    dateFormat: "Y-m-d",
    inline: false,
    position: "below",
    onChange: function (selectedDates, dateStr) {
      if (selectedDates.length > 0) {
        appState.currentFilter.type = 'day';
        appState.currentFilter.date = dateStr;
        console.log('Selected day:', dateStr);

        updateActiveFilterButton('day');

        if (typeof loadExpenses === 'function') {
          loadExpenses();
        }
        loadChartData();
        this.close();
      }
    }
  });

  // Calendar cho TUẦN
  weekPicker = flatpickr("#week-picker", {
    dateFormat: "Y-m-d",
    inline: false,
    position: "below",
    onChange: function (selectedDates, dateStr) {
      if (selectedDates.length > 0) {
        const selectedDate = selectedDates[0];
        const startOfWeek = getStartOfWeek(selectedDate);
        const formattedDate = formatDateToString(startOfWeek);

        appState.currentFilter.type = 'week';
        appState.currentFilter.date = formattedDate;
        console.log('Selected week starting:', formattedDate);

        updateActiveFilterButton('week');

        if (typeof loadExpenses === 'function') {
          loadExpenses();
        }
        loadChartData();
        this.close();
      }
    }
  });

  // Calendar cho THÁNG - Đơn giản hóa không dùng plugin
  monthPicker = flatpickr("#month-picker", {
    dateFormat: "Y-m-d",
    inline: false,
    position: "below",
    onChange: function (selectedDates, dateStr) {
      if (selectedDates.length > 0) {
        const date = new Date(selectedDates[0]);
        const formattedDate = formatDateForMonth(date);

        appState.currentFilter.type = 'month';
        appState.currentFilter.date = formattedDate;
        console.log('Selected month:', formattedDate);

        updateActiveFilterButton('month');

        if (typeof loadExpenses === 'function') {
          loadExpenses();
        }
        loadChartData();
        this.close();
      }
    }
  });

  // Setup hover events và click events
  setupFilterEvents();
}

function setupFilterEvents() {
  const filterButtons = document.querySelectorAll('.filter-button');

  filterButtons.forEach(button => {
    const filterType = button.getAttribute('data-filter');
    let picker = null;

    // Xác định picker tương ứng
    switch (filterType) {
      case 'day':
        picker = dayPicker;
        break;
      case 'week':
        picker = weekPicker;
        break;
      case 'month':
        picker = monthPicker;
        break;
    }

    if (picker) {
      // Hover events
      button.addEventListener('mouseenter', () => {
        picker.open();
      });

      button.addEventListener('mouseleave', () => {
        setTimeout(() => {
          if (!picker.calendarContainer.matches(':hover')) {
            picker.close();
          }
        }, 200);
      });

      // Click events để set filter ngay lập tức
      button.addEventListener('click', () => {
        appState.currentFilter.type = filterType;

        // Set default date cho filter hiện tại
        const today = new Date();
        if (filterType === 'day') {
          appState.currentFilter.date = formatDateToString(today);
        } else if (filterType === 'week') {
          appState.currentFilter.date = formatDateToString(getStartOfWeek(today));
        } else if (filterType === 'month') {
          appState.currentFilter.date = formatDateForMonth(today);
        }

        updateActiveFilterButton(filterType);

        if (typeof loadExpenses === 'function') {
          loadExpenses();
        }
        loadChartData();
      });
    }
  });
}

function updateActiveFilterButton(activeType) {
  // Remove active class from all buttons
  document.querySelectorAll('.filter-button').forEach(btn => {
    btn.classList.remove('active');
  });

  // Add active class to selected button
  const activeBtn = document.querySelector(`[data-filter="${activeType}"]`);
  if (activeBtn) {
    activeBtn.classList.add('active');
  }
}

async function loadChartData() {
  try {
    const { type, date } = appState.currentFilter;
    console.log('Loading chart data with:', { type, date });

    showChartLoading(true);
    const response = await ExpenseAPI.getChartData(type, date);

    if (response.data && response.stats) {
      updateChart(response.data, response.stats);
      updateChartSummary(response.stats);
    } else {
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

function updateChart(categoryData, summaryStats) {
  if (!expenseChart) {
    initializeChart();
  }

  const labels = categoryData.map(item => item.categoryName);
  const data = categoryData.map(item => parseFloat(item.totalSpent));
  const colors = categoryData.map(item => item.colorHex);
  const percentages = categoryData.map(item => parseFloat(item.percentage));

  expenseChart.data.labels = labels;
  expenseChart.data.datasets[0].data = data;
  expenseChart.data.datasets[0].backgroundColor = colors;
  expenseChart.data.datasets[0].percentages = percentages;

  expenseChart.update('active');
  updateCustomLegend(categoryData);
}

function updateCustomLegend(categoryData) {
  const legendContainer = document.getElementById('chart-legend');
  if (!legendContainer) return;

  legendContainer.innerHTML = '';
  categoryData.forEach((item) => {
    const legendItem = document.createElement('div');
    legendItem.className = 'legend-item';
    legendItem.innerHTML = `
            <span class="legend-color" style="background-color: ${item.colorHex}"></span>
            <span class="legend-label">${item.categoryName}</span>
            <span class="legend-value">${formatCurrency(item.totalSpent)}₫</span>
            <span class="legend-percentage">(${item.percentage}%)</span>
        `;
    legendContainer.appendChild(legendItem);
  });
}

function showChartLoading(show) {
  const loading = document.getElementById('chart-loading');
  if (loading) {
    loading.style.display = show ? 'block' : 'none';
  }
}

function showEmptyChart() {
  if (expenseChart) {
    expenseChart.data.labels = [];
    expenseChart.data.datasets[0].data = [];
    expenseChart.data.datasets[0].backgroundColor = [];
    expenseChart.update();
  }

  const legendContainer = document.getElementById('chart-legend');
  if (legendContainer) {
    legendContainer.innerHTML = '<p>Không có dữ liệu để hiển thị</p>';
  }
}

function updateChartSummary(summaryStats) {
  console.log('Updating chart summary with:', summaryStats); // Debug log
  const totalElement = document.getElementById('total-expense-amount');
  if (totalElement) {
    totalElement.textContent = formatCurrency(summaryStats.grandTotal) + '₫';
  }
}
