import { useCallback, useEffect, useRef, useState } from 'react'
import API from '../api/api'
import { confirmAction, notifyError } from '../utils/notify'

/**
 * Shared CRUD hook for story-scoped entities
 * (characters, locations, lore, …).
 *
 * @param {object} options
 * @param {string} options.endpoint   - e.g. '/api/characters'
 * @param {number|string|null} options.storyId
 * @param {object} [options.initialForm] - extra default fields besides storyId
 * @param {string} [options.idParam] - query param name for story filter (default storyId)
 */
export default function useEntityCrud({
  endpoint,
  storyId,
  initialForm = {},
  idParam = 'storyId',
}) {
  // Keep a stable snapshot of initialForm so clearForm does not change identity every render
  const initialFormRef = useRef(initialForm)
  initialFormRef.current = initialForm

  const buildEmptyForm = useCallback(() => {
    return { [idParam]: storyId, ...initialFormRef.current }
  }, [storyId, idParam])

  const [list, setList] = useState([])
  const [form, setForm] = useState(() => buildEmptyForm())
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [saving, setSaving] = useState(false)

  const clearForm = useCallback(() => {
    setForm(buildEmptyForm())
  }, [buildEmptyForm])

  const fetchList = useCallback(async () => {
    if (!storyId) {
      setList([])
      setLoading(false)
      return
    }
    try {
      setLoading(true)
      setError(null)
      const r = await API.get(`${endpoint}?${idParam}=${storyId}`)
      setList(r.data)
    } catch (err) {
      console.error(`Erreur chargement ${endpoint}:`, err)
      setError(err.friendlyMessage || 'Impossible de charger les données.')
    } finally {
      setLoading(false)
    }
  }, [endpoint, storyId, idParam])

  useEffect(() => {
    fetchList()
  }, [fetchList])

  // Reset form storyId when the selected story changes
  useEffect(() => {
    setForm((prev) => ({ ...prev, [idParam]: storyId }))
  }, [storyId, idParam])

  async function save() {
    if (!storyId) {
      notifyError('Veuillez d’abord sélectionner un roman.')
      return
    }
    try {
      setSaving(true)
      const payload = { ...form, [idParam]: storyId }
      if (form.id) {
        await API.put(`${endpoint}/${form.id}`, payload)
      } else {
        await API.post(endpoint, payload)
      }
      clearForm()
      await fetchList()
    } catch (err) {
      notifyError(err.friendlyMessage || 'Erreur lors de la sauvegarde.')
    } finally {
      setSaving(false)
    }
  }

  async function del(id) {
    if (!confirmAction('Supprimer ?')) return
    try {
      await API.delete(`${endpoint}/${id}`)
      if (form.id === id) clearForm()
      await fetchList()
    } catch (err) {
      notifyError(err.friendlyMessage || 'Erreur lors de la suppression.')
    }
  }

  function edit(item) {
    setForm({ ...item, [idParam]: storyId })
  }

  function updateField(name, value) {
    setForm((prev) => ({ ...prev, [name]: value }))
  }

  return {
    list,
    form,
    setForm,
    updateField,
    loading,
    error,
    saving,
    fetchList,
    save,
    del,
    edit,
    clearForm,
  }
}
