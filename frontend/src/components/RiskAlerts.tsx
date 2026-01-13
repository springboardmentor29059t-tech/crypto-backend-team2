import { useState, useEffect } from 'react';
import axios from 'axios';
import { AlertTriangle, ShieldAlert, Shield, Bell, Search, X, Activity, RefreshCw } from 'lucide-react';

// --- Interfaces matching Backend ---
interface RiskAlert {
  id: number;
  symbol: string;
  alertType: string; 
  severity: 'low' | 'medium' | 'high';
  details: string;
  createdAt: string;
}

interface ScamToken {
  id: number;
  contractAddress: string;
  chain: string;
  riskLevel: 'low' | 'medium' | 'high';
  source: string;
  lastSeen: string;
}

interface RiskAlertsProps {
  userId?: number; 
}

type SeverityFilter = 'all' | 'low' | 'medium' | 'high';

export function RiskAlerts({ userId }: RiskAlertsProps) {
  const [alerts, setAlerts] = useState<RiskAlert[]>([]);
  const [scamTokens, setScamTokens] = useState<ScamToken[]>([]);
  const [loading, setLoading] = useState(true);
  
  const [searchTerm, setSearchTerm] = useState('');
  const [filterSeverity, setFilterSeverity] = useState<SeverityFilter>('all');

  const severityOptions: SeverityFilter[] = ['all', 'high', 'medium', 'low'];

  const activeUserId = userId || JSON.parse(localStorage.getItem('user') || '{}').id;

  // --- 1. Fetch Real Data from Backend ---
  useEffect(() => {
    if (!activeUserId) {
        setLoading(false);
        return;
    }

    const fetchData = async () => {
        setLoading(true);
        try {
            const [alertsRes, scamsRes] = await Promise.all([
                axios.get(`http://localhost:5000/api/risk/alerts/${activeUserId}`),
                axios.get<ScamToken[]>(`http://localhost:5000/api/risk/scam-tokens`)
            ]);

            const mappedAlerts = alertsRes.data.map((alert: any) => ({
                id: alert.id,
                symbol: alert.assetSymbol, 
                alertType: alert.alertType,
                severity: alert.severity,
                details: alert.details,
                createdAt: alert.createdAt
            }));

            setAlerts(mappedAlerts);
            setScamTokens(scamsRes.data);
        } catch (error) {
            console.error("Error fetching risk data:", error);
        } finally {
            setLoading(false);
        }
    };

    fetchData();
  }, [activeUserId]); 

  // --- 2. Force Scan Functionality ---
  const handleForceScan = async () => {
    if (!activeUserId) {
        alert("User not found. Please log in.");
        return;
    }

    setLoading(true);
    try {
      
      await axios.post(`http://localhost:5000/api/risk/scan/${activeUserId}`);
      
      const [alertsRes, scamsRes] = await Promise.all([
          axios.get(`http://localhost:5000/api/risk/alerts/${activeUserId}`),
          axios.get<ScamToken[]>(`http://localhost:5000/api/risk/scam-tokens`)
      ]);
      
      const mappedAlerts = alertsRes.data.map((alert: any) => ({
        id: alert.id,
        symbol: alert.assetSymbol, 
        alertType: alert.alertType,
        severity: alert.severity,
        details: alert.details,
        createdAt: alert.createdAt
      }));

      setAlerts(mappedAlerts);
      setScamTokens(scamsRes.data);
    } catch (error) {
      console.error("Scan failed:", error);
    } finally {
      setLoading(false);
    }
  };

  // --- 3. Filter Logic ---
  const filteredAlerts = alerts.filter((alert) => {
    const matchesSearch = alert.symbol.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesSeverity = filterSeverity === 'all' || alert.severity.toLowerCase() === filterSeverity;
    return matchesSearch && matchesSeverity;
  });

  const dismissAlert = (id: number) => {
    setAlerts(alerts.filter((alert) => alert.id !== id));
  };

  // --- Helpers ---
  const getSeverityColor = (severity: string) => {
    switch (severity.toLowerCase()) {
      case 'high': return 'text-red-400 bg-red-500/20 border-red-500/30';
      case 'medium': return 'text-yellow-400 bg-yellow-500/20 border-yellow-500/30';
      case 'low': return 'text-blue-400 bg-blue-500/20 border-blue-500/30';
      default: return 'text-slate-400 bg-slate-500/20 border-slate-500/30';
    }
  };

  const getAlertIcon = (type: string) => {
    switch (type) {
      case 'rugpull_warning': return <ShieldAlert className="w-5 h-5" />;
      case 'contract_risk': return <AlertTriangle className="w-5 h-5" />;
      default: return <Bell className="w-5 h-5" />;
    }
  };

  const formatDate = (dateString: string) => {
    if (!dateString) return '';
    return new Date(dateString).toLocaleDateString('en-US', {
      month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
    });
  };

  if (loading) {
    return (
        <div className="flex justify-center items-center h-64 text-slate-400">
           <Activity className="animate-spin mr-2" /> Scanning Portfolio Risks...
        </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
            <h2 className="text-2xl text-white mb-1">Risk Alerts & Scam Detection</h2>
            <p className="text-slate-400">Monitor threats and suspicious activity in your portfolio.</p>
        </div>
        
        <button 
            onClick={handleForceScan}
            className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg transition-colors"
        >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
            Run Risk Scan
        </button>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="bg-slate-800/50 rounded-xl border border-slate-700 p-6">
          <div className="flex items-center gap-3 mb-2">
            <AlertTriangle className="w-5 h-5 text-red-400" />
            <p className="text-sm text-slate-400">High Risk</p>
          </div>
          <p className="text-2xl text-white">
            {alerts.filter((a) => a.severity.toLowerCase() === 'high').length}
          </p>
        </div>
        <div className="bg-slate-800/50 rounded-xl border border-slate-700 p-6">
          <div className="flex items-center gap-3 mb-2">
            <AlertTriangle className="w-5 h-5 text-yellow-400" />
            <p className="text-sm text-slate-400">Medium Risk</p>
          </div>
          <p className="text-2xl text-white">
            {alerts.filter((a) => a.severity.toLowerCase() === 'medium').length}
          </p>
        </div>
        <div className="bg-slate-800/50 rounded-xl border border-slate-700 p-6">
          <div className="flex items-center gap-3 mb-2">
            <Bell className="w-5 h-5 text-blue-400" />
            <p className="text-sm text-slate-400">Low Risk</p>
          </div>
          <p className="text-2xl text-white">
            {alerts.filter((a) => a.severity.toLowerCase() === 'low').length}
          </p>
        </div>
        <div className="bg-slate-800/50 rounded-xl border border-slate-700 p-6">
          <div className="flex items-center gap-3 mb-2">
            <Shield className="w-5 h-5 text-green-400" />
            <p className="text-sm text-slate-400">Known Scams</p>
          </div>
          <p className="text-2xl text-white">{scamTokens.length}</p>
        </div>
      </div>

      {/* Filters */}
      <div className="flex gap-4">
        <div className="flex-1 relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400" />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Search by asset symbol..."
            className="w-full bg-slate-800/50 border border-slate-700 rounded-lg pl-10 pr-4 py-2 text-white placeholder-slate-500 focus:outline-none focus:border-blue-500"
          />
        </div>
        <div className="flex gap-2">
          {severityOptions.map((sev) => (
             <button
                key={sev}
                onClick={() => setFilterSeverity(sev)}
                className={`px-4 py-2 rounded-lg capitalize transition-colors ${
                  filterSeverity === sev
                    ? 'bg-blue-600 text-white'
                    : 'bg-slate-800/50 text-slate-300 hover:bg-slate-700/50'
                }`}
              >
                {sev}
              </button>
          ))}
        </div>
      </div>

      {/* Active Alerts */}
      <div className="bg-slate-800/50 rounded-xl border border-slate-700 p-6">
        <h3 className="text-lg text-white mb-4">Active Alerts</h3>
        <div className="space-y-3">
          {filteredAlerts.map((alert) => (
            <div key={alert.id} className={`p-4 rounded-lg border ${getSeverityColor(alert.severity)}`}>
              <div className="flex items-start justify-between gap-4">
                <div className="flex items-start gap-3 flex-1">
                  <div className={getSeverityColor(alert.severity).split(' ')[0]}>
                    {getAlertIcon(alert.alertType)}
                  </div>
                  <div className="flex-1">
                    <div className="flex items-center gap-3 mb-2">
                      <p className="text-white font-bold">{alert.symbol}</p>
                      <span className={`px-2 py-1 rounded text-xs border ${getSeverityColor(alert.severity)}`}>
                        {alert.severity.toUpperCase()}
                      </span>
                      <span className="text-xs text-slate-500 capitalize">
                        {alert.alertType.replace(/_/g, ' ')}
                      </span>
                    </div>
                    <p className="text-sm text-slate-300 mb-2">{alert.details}</p>
                    <p className="text-xs text-slate-500">{formatDate(alert.createdAt)}</p>
                  </div>
                </div>
                <button
                  onClick={() => dismissAlert(alert.id)}
                  className="p-1 text-slate-400 hover:text-white transition-colors"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>
            </div>
          ))}
          {filteredAlerts.length === 0 && (
            <p className="text-slate-400 text-center py-8">No alerts found. Your portfolio looks safe!</p>
          )}
        </div>
      </div>

      {/* Scam Token Database */}
      {/* <div className="bg-slate-800/50 rounded-xl border border-slate-700 p-6">
        <div className="flex items-center gap-3 mb-4">
          <ShieldAlert className="w-5 h-5 text-red-400" />
          <h3 className="text-lg text-white">Known Scam Database</h3>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-slate-700">
                <th className="text-left text-sm text-slate-400 px-4 py-3">Contract</th>
                <th className="text-left text-sm text-slate-400 px-4 py-3">Chain</th>
                <th className="text-left text-sm text-slate-400 px-4 py-3">Risk</th>
                <th className="text-left text-sm text-slate-400 px-4 py-3">Source</th>
                <th className="text-left text-sm text-slate-400 px-4 py-3">Last Seen</th>
              </tr>
            </thead>
            <tbody>
              {scamTokens.map((token) => (
                <tr key={token.id} className="border-b border-slate-700/50 hover:bg-slate-700/20">
                  <td className="px-4 py-3">
                    <code className="text-sm text-blue-400 bg-slate-900/50 px-2 py-1 rounded">
                      {token.contractAddress}
                    </code>
                  </td>
                  <td className="px-4 py-3 text-slate-300">{token.chain}</td>
                  <td className="px-4 py-3">
                    <span className={`inline-block px-2 py-1 rounded text-xs ${getSeverityColor(token.riskLevel)}`}>
                      {token.riskLevel.toUpperCase()}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-slate-300">{token.source}</td>
                  <td className="px-4 py-3 text-slate-400">{token.lastSeen}</td>
                </tr>
              ))}
              {scamTokens.length === 0 && (
                 <tr><td colSpan={5} className="text-center text-slate-500 py-4">Database Empty or Loading...</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div> */}
    </div>
  );
}