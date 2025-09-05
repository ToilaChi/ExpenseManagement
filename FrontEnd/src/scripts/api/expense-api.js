class ExpenseAPI {
  static API_BASE_URL = 'http://localhost:8080';

  static async addExpense(categoryId, amount, description) {
    try {
      const accessToken = localStorage.getItem('accessToken');
      const response = await fetch(`${this.API_BASE_URL}/expenses/add`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${accessToken}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          categoryId,
          amount: parseFloat(amount),
          description
        })
      });

      if (!response.ok) {
        throw new Error('Failed to add expense');
      }

      const data = await response.json();
      return data;
    } catch (error) {
      console.error('Error adding expense:', error);
      throw error;
    }
  }

  static async getExpenseList(filterType = 'month', date = '2025-01', page = 0, size = 20) {
    try {
      const accessToken = localStorage.getItem('accessToken');
      const params = new URLSearchParams({
        filterType,
        date,
        page: page.toString(),
        size: size.toString()
      });

      const response = await fetch(`${this.API_BASE_URL}/expenses/list?${params}`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${accessToken}`,
          'Content-Type': 'application/json'
        }
      });

      if (!response.ok) {
        throw new Error('Failed to fetch expenses');
      }

      const data = await response.json();
      return data;
    } catch (error) {
      console.error('Error fetching expenses:', error);
      throw error;
    }
  }

  static async updateExpense(expenseId, newCategoryName, newAmount, newDescription) {
    try {
      const accessToken = localStorage.getItem('accessToken');
      const response = await fetch(`${this.API_BASE_URL}/expenses/update/${expenseId}`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${accessToken}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          newCategoryName,
          newAmount: newAmount ? parseFloat(newAmount) : null,
          newDescription
        })
      });

      if (!response.ok) {
        throw new Error('Failed to update expense');
      }

      const data = await response.json();
      return data;
    } catch (error) {
      console.error('Error updating expense:', error);
      throw error;
    }
  }

  static async deleteExpense(expenseId) {
    try {
      const accessToken = localStorage.getItem('accessToken');
      const response = await fetch(`${this.API_BASE_URL}/expenses/delete/${expenseId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${accessToken}`,
          'Content-Type': 'application/json'
        }
      });

      if (!response.ok) {
        throw new Error('Failed to delete expense');
      }

      const data = await response.json();
      return data;
    } catch (error) {
      console.error('Error deleting expense:', error);
      throw error;
    }
  }
}