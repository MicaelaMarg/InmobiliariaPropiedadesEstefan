import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  // Sitio público
  {
    path: '/',
    component: () => import('../views/public/PublicLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('../views/public/HomePage.vue') },
      { path: 'catalogo', name: 'Catalog', component: () => import('../views/public/CatalogPage.vue') },
      { path: 'tasacion', name: 'Valuation', component: () => import('../views/public/ValuationPage.vue') },
      { path: 'propiedad/:slug', name: 'PropertyDetail', component: () => import('../views/public/PropertyDetailPage.vue') },
      { path: 'contacto', name: 'Contact', component: () => import('../views/public/ContactPage.vue') },
    ],
  },
  // Admin - login público
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('../views/admin/AdminLoginPage.vue'),
    meta: { requiresGuest: true },
  },
  {
    path: '/admin/forgot-password',
    name: 'AdminForgotPassword',
    component: () => import('../views/admin/AdminForgotPasswordPage.vue'),
    meta: { requiresGuest: true },
  },
  {
    path: '/admin/reset-password',
    name: 'AdminResetPassword',
    component: () => import('../views/admin/AdminResetPasswordPage.vue'),
    meta: { requiresGuest: true },
  },
  {
    path: '/admin/setup',
    name: 'AdminSetup',
    component: () => import('../views/admin/AdminSetupPage.vue'),
    meta: { requiresGuest: true },
  },
  // Admin - rutas protegidas
  {
    path: '/admin',
    component: () => import('../views/admin/AdminLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', name: 'AdminDashboard', component: () => import('../views/admin/AdminDashboardPage.vue') },
      { path: 'propiedades', name: 'AdminProperties', component: () => import('../views/admin/AdminPropertiesPage.vue') },
      { path: 'propiedades/nueva', name: 'AdminPropertyNew', component: () => import('../views/admin/AdminPropertyFormPage.vue') },
      { path: 'propiedades/:id/editar', name: 'AdminPropertyEdit', component: () => import('../views/admin/AdminPropertyFormPage.vue') },
      { path: 'usuarios', name: 'AdminUsers', component: () => import('../views/admin/AdminUsersPage.vue') },
      { path: 'configuracion', name: 'AdminSettings', component: () => import('../views/admin/AdminSettingsPage.vue') },
    ],
  },
  { path: '/:pathMatch(.*)*', name: 'NotFound', redirect: '/' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) return savedPosition
    if (to.hash) return { el: to.hash }
    return { top: 0 }
  },
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next({ name: 'AdminLogin', query: { redirect: to.fullPath } })
    return
  }

  if (to.meta.requiresGuest && authStore.isAuthenticated) {
    next({ name: 'AdminDashboard' })
    return
  }

  next()
})

export default router
