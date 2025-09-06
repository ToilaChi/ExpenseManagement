class BudgetAPI {
  static API_BASE_URL = 'http://localhost:8080';

  static async resetMyBudgets() {
    try {
      const accessToken = localStorage.getItem('accessToken');
      const response = await fetch(`${this.API_BASE_URL}/budget/reset-my-budgets`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${accessToken}`,
          'Content-Type': 'application/json'
        }
      });

      if (!response.ok) {
        throw new Error('Failed to reset budgets');
      }

      const data = await response.json();
      return data;
    } catch (error) {
      console.error('Error resetting budgets:', error);
      throw error;
    }
  }
}