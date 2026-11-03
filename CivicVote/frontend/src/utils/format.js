export const formatDate = (value) => value ? new Date(value).toLocaleString() : '—'
export const errorMessage = (error) => error.response?.data?.message || error.response?.data?.detail || error.message || 'Something went wrong.'
