import { lazy, Suspense } from 'react'
import { Route, Routes } from 'react-router-dom'
import ProtectedRoute from './components/ProtectedRoute'
import AppLayout from './layouts/AppLayout'
import Loading from './components/Loading'
import { useAuth } from './context/AuthContext'
const LoginPage = lazy(() => import('./pages/LoginPage'))
const DashboardPage = lazy(() => import('./pages/DashboardPage'))
const AdminDashboardPage = lazy(() => import('./pages/AdminDashboardPage'))
const AdminElectionsPage = lazy(() => import('./pages/AdminElectionsPage'))
const ElectionsPage = lazy(() => import('./pages/ElectionsPage'))
const ElectionDetailsPage = lazy(() => import('./pages/ElectionDetailsPage'))
const CreateElectionPage = lazy(() => import('./pages/CreateElectionPage'))
const ResultsPage = lazy(() => import('./pages/ResultsPage'))
const StatusPage = lazy(() => import('./pages/StatusPage'))
const AccessDeniedPage = lazy(() => import('./pages/MessagePages').then((module) => ({ default: module.AccessDeniedPage })))
const NotFoundPage = lazy(() => import('./pages/MessagePages').then((module) => ({ default: module.NotFoundPage })))
function RoleDashboard() {
  const { user } = useAuth()
  return user?.role === 'ADMIN' ? <AdminDashboardPage /> : <DashboardPage />
}
export default function App() {
  return <Suspense fallback={<Loading />}><Routes><Route path="/login" element={<LoginPage />} /><Route path="/denied" element={<AccessDeniedPage />} /><Route element={<ProtectedRoute />}><Route element={<AppLayout />}><Route index element={<RoleDashboard />} /><Route path="/elections" element={<ElectionsPage />} /><Route path="/elections/:id" element={<ElectionDetailsPage />} /><Route path="/results" element={<ResultsPage />} /><Route element={<ProtectedRoute role="VOTER" />}><Route path="/vote" element={<ElectionsPage />} /><Route path="/status" element={<StatusPage />} /></Route><Route element={<ProtectedRoute role="ADMIN" />}><Route path="/admin" element={<AdminDashboardPage />} /><Route path="/admin/elections" element={<AdminElectionsPage />} /><Route path="/admin/elections/new" element={<CreateElectionPage />} /></Route></Route></Route><Route path="*" element={<NotFoundPage />} /></Routes></Suspense>
}
