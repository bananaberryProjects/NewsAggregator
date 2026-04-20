import { useState, useCallback } from 'react'
import { categoriesApi, type Category } from '../api/client'

export function useCategories() {
  const [categories, setCategories] = useState<Category[]>([])

  const loadCategories = useCallback(async () => {
    try {
      const categoriesData = await categoriesApi.getAll().catch(() => [])
      setCategories(categoriesData)
    } catch (err) {
      console.error('Error loading categories:', err)
    }
  }, [])

  const addCategory = async (name: string, color: string, icon?: string) => {
    await categoriesApi.create({ name: name.trim(), color, icon })
    await loadCategories()
  }

  const updateCategory = async (id: string, name: string, color: string, icon: string) => {
    await categoriesApi.update(id, { name: name.trim(), color, icon })
    await loadCategories()
  }

  const deleteCategory = async (id: string) => {
    await categoriesApi.delete(id)
    await loadCategories()
  }

  return {
    categories,
    loadCategories,
    addCategory,
    deleteCategory,
    updateCategory,
  }
}
