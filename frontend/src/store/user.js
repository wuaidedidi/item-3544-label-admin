import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getProfile, getMenus } from '../api/profile'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  const menus = ref([])

  const isAdmin = computed(() => userInfo.value?.roleCode === 'ADMIN')
  const isLoggedIn = computed(() => !!token.value)
  const nickname = computed(() => userInfo.value?.nickname || userInfo.value?.username || '')

  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setUserInfo(info) {
    userInfo.value = { ...info }
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  function updateUserInfo(partial) {
    if (userInfo.value) {
      const updated = { ...userInfo.value, ...partial }
      userInfo.value = updated
      localStorage.setItem('userInfo', JSON.stringify(updated))
    }
  }

  function setMenus(newMenus) {
    menus.value = newMenus
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    menus.value = []
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  async function fetchUserInfo() {
    try {
      const res = await getProfile()
      if (res.code === 200) {
        setUserInfo(res.data)
      }
    } catch (e) {
      // handled by interceptor
    }
  }

  async function fetchMenus() {
    try {
      const res = await getMenus()
      if (res.code === 200) {
        setMenus(res.data)
      }
    } catch (e) {
      // handled by interceptor
    }
  }

  return {
    token, userInfo, menus, isAdmin, isLoggedIn, nickname,
    setToken, setUserInfo, updateUserInfo, setMenus, logout,
    fetchUserInfo, fetchMenus
  }
})
