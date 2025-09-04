class CategoryAPI {
  static API_BASE_URL = 'http://localhost:8080';

  static async getCategories() {
    try {
      const accessToken = localStorage.getItem('accessToken');
      const response = await fetch(`${this.API_BASE_URL}/categories/list`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${accessToken}`,
          'Content-Type': 'application/json'
        }
      });
      if (!response.ok) {
        throw new Error('Failed to fetch categories');
      }
      const data = await response.json();
      return data;
    } catch (error) {
      console.error('Error fetching categories:', error);
      throw error;
    }
  }

  static async createCategory(categoryName, allocatedBudget) {
    try {
      const accessToken = localStorage.getItem('accessToken');
      const response = await fetch(`${this.API_BASE_URL}/categories/create`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${accessToken}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ categoryName, allocatedBudget: parseFloat(allocatedBudget) })
      });

      if (!response.ok) {
        throw new Error('Failed to create category');
      }

      const data = await response.json();
      return data;
    } catch (error) {
      console.error('Error creating category:', error);
      throw error;
    }
  }

  static async updateCategory(categoryId, categoryName, budget) {
    try {
      const accessToken = localStorage.getItem('accessToken');
      const response = await fetch(`${this.API_BASE_URL}/categories/update/${categoryId}`, {
        method: 'PATCH',
        headers: {
          'Authorization': `Bearer ${accessToken}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ categoryName, budget: budget ? parseFloat(budget) : null })
      });

      if (!response.ok) {
        throw new Error('Failed to update category');
      }

      const data = await response.json();
      return data;
    } catch (error) {
      console.error('Error updating category:', error);
      throw error;
    }
  }

  static async deleteCategory(categoryId) {
    try {
      const accessToken = localStorage.getItem('accessToken');
      const response = await fetch(`${this.API_BASE_URL}/categories/delete/${categoryId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${accessToken}`,
          'Content-Type': 'application/json'
        }
      });

      if (!response.ok) {
        throw new Error('Failed to delete category');
      }

      const data = await response.json();
      return data;
    } catch (error) {
      console.error('Error deleting category:', error);
      throw error;
    }
  }
}