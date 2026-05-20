import { Routes, Route } from 'react-router-dom';
import Header from './components/Header.jsx';
import BottomTabs from './components/BottomTabs.jsx';
import RequireAuth from './components/RequireAuth.jsx';
import RequireAdmin from './components/RequireAdmin.jsx';
import HomePage from './pages/HomePage.jsx';
import LevelSelectionPage from './pages/LevelSelectionPage.jsx';
import SubLevelPage from './pages/SubLevelPage.jsx';
import ExercisePage from './pages/ExercisePage.jsx';
import LoginPage from './pages/LoginPage.jsx';
import RegisterPage from './pages/RegisterPage.jsx';
import PracticePage from './pages/PracticePage.jsx';
import DictionaryPage from './pages/DictionaryPage.jsx';
import ProgressPage from './pages/ProgressPage.jsx';
import AdminPage from './pages/AdminPage.jsx';
import LeaderboardPage from './pages/LeaderboardPage.jsx';
import ProfilePage from './pages/ProfilePage.jsx';
import SettingsPage from './pages/SettingsPage.jsx';
import ForgotPasswordPage from './pages/ForgotPasswordPage.jsx';
import ResetPasswordPage from './pages/ResetPasswordPage.jsx';
import PricingPage from './pages/PricingPage.jsx';
import NotFoundPage from './pages/NotFoundPage.jsx';

function App() {
  return (
    <div className="app">
      <Header />
      <BottomTabs />
      <main className="app__main">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/registro" element={<RegisterPage />} />
          <Route path="/diccionario" element={<DictionaryPage />} />
          <Route path="/clasificacion" element={<RequireAuth><LeaderboardPage /></RequireAuth>} />
          <Route path="/admin" element={<RequireAdmin><AdminPage /></RequireAdmin>} />
          <Route
            path="/practica"
            element={
              <RequireAuth>
                <PracticePage />
              </RequireAuth>
            }
          />
          <Route
            path="/progreso"
            element={
              <RequireAuth>
                <ProgressPage />
              </RequireAuth>
            }
          />
          <Route path="/recuperar" element={<ForgotPasswordPage />} />
          <Route path="/restablecer" element={<ResetPasswordPage />} />
          <Route path="/perfil" element={<RequireAuth><ProfilePage /></RequireAuth>} />
          <Route path="/configuracion" element={<RequireAuth><SettingsPage /></RequireAuth>} />
          <Route path="/precios" element={<PricingPage />} />
          <Route path="/:categoryType" element={<LevelSelectionPage />} />
          <Route path="/:categoryType/:levelCode" element={<SubLevelPage />} />
          <Route path="/:categoryType/:levelCode/:exerciseId" element={<ExercisePage />} />
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </main>
    </div>
  );
}

export default App;