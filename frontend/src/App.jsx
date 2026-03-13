import { BrowserRouter, Routes, Route } from "react-router-dom";
import { AuthProvider } from "./contexts/AuthContext";
import ProtectedRoute from "./routes/ProtectedRoute";

import Home from "./pages/Home";

// Participant
import ParticipantLogin from "./pages/participant/Login";
import Register from "./pages/participant/Register";
import QrCode from "./pages/participant/QrCode";

// Organizer
import OrganizerLogin from "./pages/organizer/Login";
import Dashboard from "./pages/organizer/Dashboard";
import EventDetail from "./pages/organizer/EventDetail";
import Staff from "./pages/organizer/Staff";
import Group from "./pages/organizer/Group";
import ParticipantsPage from "./pages/organizer/ParticipantsPage";

// EventStaff
import StaffLogin from "./pages/staff/Login";
import Scan from "./pages/staff/Scan";

import About from "./pages/About";
import Terms from "./pages/Terms";

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Home />} />

          {/* Participant */}
          <Route path="/register" element={<Register />} />
          <Route path="/participant/login" element={<ParticipantLogin />} />
          <Route
            path="/participant/qrcode"
            element={
              <ProtectedRoute role="PARTICIPANT">
                <QrCode />
              </ProtectedRoute>
            }
          />

          {/* Organizer */}
          <Route path="/organizer/login" element={<OrganizerLogin />} />
          <Route
            path="/organizer/dashboard"
            element={
              <ProtectedRoute role="ORGANIZER">
                <Dashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/organizer/events/:id"
            element={
              <ProtectedRoute role="ORGANIZER">
                <EventDetail />
              </ProtectedRoute>
            }
          />
          <Route
            path="/organizer/staff"
            element={
              <ProtectedRoute role="ORGANIZER">
                <Staff />
              </ProtectedRoute>
            }
          />
          <Route
            path="/organizer/group"
            element={
              <ProtectedRoute role="ORGANIZER">
                <Group />
              </ProtectedRoute>
            }
          />
          <Route
            path="/organizer/participants"
            element={
              <ProtectedRoute role="ORGANIZER">
                <ParticipantsPage />
              </ProtectedRoute>
            }
          />

          {/* EventStaff */}
          <Route path="/staff/login" element={<StaffLogin />} />
          <Route
            path="/staff/scan"
            element={
              <ProtectedRoute role="EVENT_STAFF">
                <Scan />
              </ProtectedRoute>
            }
          />

          <Route path="/about" element={<About />} />
          <Route path="/terms" element={<Terms />} />

          <Route path="/sobre" element={<RedirectSobre />} />

          <Route path="*" element={<Home />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

function RedirectSobre() {
  window.location.href = "https://parusya-one.vercel.app/";
  return null;
}
