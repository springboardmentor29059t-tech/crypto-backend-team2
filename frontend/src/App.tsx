import { useState } from 'react';
import { Login } from './components/Login';
import Dashboard from './components/Dashboard'; 
import ExchangeConnections from './components/ExchangeConnections';

import { Portfolio } from './components/Portfolio';
import { Trades } from './components/Trades';
import { PricingCharts } from './components/PricingCharts';
import { RiskAlerts } from './components/RiskAlerts';
import { PLReports } from './components/PLReports';

import { 
  LayoutDashboard, 
  Wallet, 
  ArrowLeftRight, 
  LineChart, 
  ShieldAlert, 
  FileText, 
  Link2,
  LogOut
} from 'lucide-react';

type View = 'login' | 'dashboard' | 'portfolio' | 'trades' | 'pricing' | 'risk' | 'reports' | 'exchanges';

interface User {
  id: number;
  name: string;
  email: string;
  token?: string;
}

export default function App() {
  // 1. Lazy Initialization (Runs once on startup)
  const [user, setUser] = useState<User | null>(() => {
    try {
      const stored = localStorage.getItem('user');
      return stored ? JSON.parse(stored) : null;
    } catch {
      return null;
    }
  });

  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(() => !!user);
  const [currentView, setCurrentView] = useState<View>(() => user ? 'dashboard' : 'login');

  // 2. Handle Login
  const handleLogin = (userData: User) => {
    setIsAuthenticated(true);
    setUser(userData);
    setCurrentView('dashboard');
    localStorage.setItem('user', JSON.stringify(userData));
  };

  // 3. Handle Logout
  const handleLogout = () => {
    setIsAuthenticated(false);
    setUser(null);
    setCurrentView('login');
    localStorage.removeItem('user');
  };

  if (!isAuthenticated) {
    return <Login onLogin={handleLogin} />;
  }

  // Navigation Items
  const navItems = [
    { id: 'dashboard' as View, label: 'Dashboard', icon: LayoutDashboard },
    { id: 'portfolio' as View, label: 'Portfolio', icon: Wallet },
    { id: 'trades' as View, label: 'Trades', icon: ArrowLeftRight },
    { id: 'pricing' as View, label: 'Pricing & Charts', icon: LineChart },
    { id: 'risk' as View, label: 'Risk Alerts', icon: ShieldAlert },
    { id: 'reports' as View, label: 'P&L Reports', icon: FileText },
    { id: 'exchanges' as View, label: 'Exchange Connections', icon: Link2 },
  ];

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 text-slate-50">
      {/* Top Header */}
      <header className="bg-slate-800/50 border-b border-slate-700 backdrop-blur-sm sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-xl text-white font-bold bg-gradient-to-r from-blue-400 to-blue-600 bg-clip-text text-transparent">
                CryptoTracker
              </h1>
            </div>
            <div className="flex items-center gap-4">
              <div className="text-right hidden sm:block">
                <p className="text-sm text-white font-medium">{user?.name}</p>
                <p className="text-xs text-slate-400">{user?.email}</p>
              </div>
              <button
                onClick={handleLogout}
                className="p-2 text-slate-400 hover:text-red-400 hover:bg-slate-800 rounded-lg transition-colors"
                title="Sign Out"
              >
                <LogOut className="w-5 h-5" />
              </button>
            </div>
          </div>
        </div>
      </header>

      <div className="flex max-w-7xl mx-auto">
        {/* Sidebar */}
        <aside className="w-64 min-h-[calc(100vh-73px)] bg-slate-800/30 border-r border-slate-700 p-4 hidden md:block">
          <nav className="space-y-2 sticky top-24">
            {navItems.map((item) => {
              const Icon = item.icon;
              return (
                <button
                  key={item.id}
                  onClick={() => setCurrentView(item.id)}
                  className={`w-full flex items-center gap-3 px-4 py-3 rounded-lg transition-colors ${
                    currentView === item.id
                      ? 'bg-blue-600 text-white shadow-lg shadow-blue-900/20'
                      : 'text-slate-400 hover:bg-slate-800 hover:text-white'
                  }`}
                >
                  <Icon className="w-5 h-5" />
                  <span className="font-medium">{item.label}</span>
                </button>
              );
            })}
          </nav>
        </aside>

        {/* Main Content Area */}
        <main className="flex-1 p-4 md:p-8 overflow-x-hidden">
          {/* Dashboard needs the 'user' prop now */}
          {currentView === 'dashboard' && <Dashboard user={user} />}
          
          {currentView === 'portfolio' && <Portfolio />}
          {currentView === 'trades' && <Trades />}
          {currentView === 'pricing' && <PricingCharts />}
          {currentView === 'risk' && <RiskAlerts />}
          {currentView === 'reports' && <PLReports />}
          
          {/* Ensure user exists before rendering connections */}
          {currentView === 'exchanges' && user && (
            <ExchangeConnections userId={user.id} />
          )}
        </main>
      </div>
    </div>
  );
}