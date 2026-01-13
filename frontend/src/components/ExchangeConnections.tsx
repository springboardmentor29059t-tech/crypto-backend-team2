import React, { useState, useEffect, useCallback } from 'react';
import axios from 'axios';
import { Plus, Link2, Trash2, Eye, EyeOff, CheckCircle, XCircle, RefreshCw } from 'lucide-react';

// --- 1. STRICT INTERFACES ---

interface BackendExchange {
  id: number;
  name: string;
}

interface BackendApiKey {
  id: number;
  exchange: {
    id: number;
    name: string;
  };
  label: string;
  encryptedApiKey: string; 
  encryptedSecret: string;
  createdAt: string;
}

interface Exchange {
  id: number;
  name: string;
  baseUrl?: string;
  logo?: string;
  supported?: boolean;
}

interface ApiKey {
  id: number;
  exchangeId: number;
  exchangeName: string;
  label: string;
  apiKey: string;
  apiSecret: string;
  createdAt: string;
  status: 'active' | 'error';
  lastSync?: string;
}

interface ConnectionFormData {
  exchangeId: number;
  exchangeName: string;
  label: string;
  apiKey: string;
  apiSecret: string;
}

interface ExchangeConnectionsProps {
  userId: number;
}

export default function ExchangeConnections({ userId }: ExchangeConnectionsProps) {
  const [apiKeys, setApiKeys] = useState<ApiKey[]>([]);
  const [exchanges, setExchanges] = useState<Exchange[]>([]);
  const [showAddModal, setShowAddModal] = useState(false);
  const [revealedSecrets, setRevealedSecrets] = useState<Set<number>>(new Set());
  const [isLoading, setIsLoading] = useState(false);

  // --- 1. Fetch Real Data ---
  const fetchData = useCallback(async () => {
    try {
      
      const exResponse = await axios.get<BackendExchange[]>('http://localhost:5000/api/exchanges/list');
      
      const enhancedExchanges: Exchange[] = exResponse.data.map((ex) => ({
        id: ex.id,
        name: ex.name,
        
        baseUrl: ex.name.toLowerCase().includes('testnet') 
          ? 'https://testnet.binance.vision' 
          : 'https://api.binance.com',
        logo: '💱', 
        supported: true
      }));
      setExchanges(enhancedExchanges);

      // Fetch User Keys
      if (userId) {
        const userResponse = await axios.get<BackendApiKey[]>(`http://localhost:5000/api/exchanges/${userId}`);
        
        const mappedKeys: ApiKey[] = userResponse.data.map((k) => ({
          id: k.id,
          exchangeId: k.exchange.id,
          exchangeName: k.exchange.name,
          label: k.label,
          apiKey: k.encryptedApiKey,     
          apiSecret: k.encryptedSecret,  
          createdAt: k.createdAt,
          status: 'active',
          lastSync: new Date().toISOString()
        }));
        setApiKeys(mappedKeys);
      }
    } catch (error) {
      console.error("Error fetching data:", error);
    }
  }, [userId]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  // --- 2. Handlers ---
  const toggleSecretVisibility = (id: number) => {
    setRevealedSecrets((prev) => {
      const newSet = new Set(prev);
      if (newSet.has(id)) newSet.delete(id);
      else newSet.add(id);
      return newSet;
    });
  };

  const deleteApiKey = async (id: number) => {
    if (!window.confirm("Delete this connection? This action cannot be undone.")) return;

    // 1. Snapshot previous state (in case of error)
    const previousKeys = [...apiKeys];

    // 2. Optimistic Update: Remove visually immediately
    setApiKeys(apiKeys.filter((key) => key.id !== id));

    try {
      // 3. Network Request: Call the backend delete endpoint
      await axios.delete(`http://localhost:5000/api/exchanges/${id}`);
      console.log(`Exchange connection ${id} deleted successfully.`);
    } catch (error) {
      console.error("Failed to delete connection:", error);
      alert("Failed to delete connection from server. Please try again.");
      
      // 4. Rollback: If backend fails, restore the list
      setApiKeys(previousKeys);
    }
  };

  const syncExchange = (id: number) => {
    console.log("Syncing connection ID:", id);
    fetchData(); 
  };

  const handleAddConnection = async (connectionData: ConnectionFormData) => {
    setIsLoading(true);
    try {
      const payload = {
        userId: userId,
        exchange: connectionData.exchangeName,
        apiKey: connectionData.apiKey,
        apiSecret: connectionData.apiSecret,
        label: connectionData.label
      };

      await axios.post('http://localhost:5000/api/exchanges/connect', payload);
      alert("Connected successfully! Syncing data...");
      setShowAddModal(false);
      fetchData(); 
    } catch (error) {
      console.error("Connection failed:", error);
      alert("Failed to connect. Check backend logs.");
    } finally {
      setIsLoading(false);
    }
  };

  const formatDate = (dateString: string) => {
    if (!dateString) return 'Just now';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className="space-y-6 p-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl text-white mb-1">Exchange Connections</h2>
          <p className="text-slate-400">Manage your exchange API keys securely</p>
        </div>
        <button
          onClick={() => setShowAddModal(true)}
          className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg transition-colors"
        >
          <Plus className="w-4 h-4" />
          Add Connection
        </button>
      </div>

      {/* Security Notice */}
      <div className="bg-blue-500/10 border border-blue-500/30 rounded-xl p-4">
        <div className="flex gap-3">
          <Link2 className="w-5 h-5 text-blue-400 mt-0.5" />
          <div>
            <p className="text-blue-400 mb-1">Secure API Storage</p>
            <p className="text-sm text-slate-300">
              Your API keys are encrypted at rest (AES-256). We recommend
              using read-only API keys with withdrawal permissions disabled.
            </p>
          </div>
        </div>
      </div>

      {/* Connected Exchanges */}
      <div className="bg-slate-800/50 rounded-xl border border-slate-700 p-6">
        <h3 className="text-lg text-white mb-4">Connected Exchanges</h3>
        <div className="space-y-3">
          {apiKeys.map((apiKey) => (
            <div
              key={apiKey.id}
              className="bg-slate-900/50 rounded-lg p-4 border border-slate-700"
            >
              <div className="flex items-start justify-between mb-3">
                <div className="flex items-center gap-3">
                  <div className="text-3xl">
                    {exchanges.find((e) => e.id === apiKey.exchangeId)?.logo || '💱'}
                  </div>
                  <div>
                    <div className="flex items-center gap-2 mb-1">
                      <p className="text-white">{apiKey.exchangeName}</p>
                      {apiKey.status === 'active' ? (
                        <CheckCircle className="w-4 h-4 text-green-400" />
                      ) : (
                        <XCircle className="w-4 h-4 text-red-400" />
                      )}
                    </div>
                    <p className="text-sm text-slate-400">{apiKey.label}</p>
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  <button
                    onClick={() => syncExchange(apiKey.id)}
                    className="p-2 text-slate-400 hover:text-blue-400 transition-colors"
                    title="Sync now"
                  >
                    <RefreshCw className="w-4 h-4" />
                  </button>
                  <button
                    onClick={() => deleteApiKey(apiKey.id)}
                    className="p-2 text-slate-400 hover:text-red-400 transition-colors"
                    title="Delete"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>

              <div className="space-y-2">
                <div className="flex items-center gap-2">
                  <span className="text-xs text-slate-500 w-20">API Key:</span>
                  <code className="text-sm text-slate-300 bg-slate-800 px-2 py-1 rounded flex-1 overflow-hidden text-ellipsis whitespace-nowrap">
                    {apiKey.apiKey.substring(0, 20)}...
                  </code>
                </div>
                <div className="flex items-center gap-2">
                  <span className="text-xs text-slate-500 w-20">Secret:</span>
                  <code className="text-sm text-slate-300 bg-slate-800 px-2 py-1 rounded flex-1">
                    {revealedSecrets.has(apiKey.id)
                      ? apiKey.apiSecret 
                      : '••••••••••••••••••••'}
                  </code>
                  <button
                    onClick={() => toggleSecretVisibility(apiKey.id)}
                    className="p-1 text-slate-400 hover:text-white transition-colors"
                  >
                    {revealedSecrets.has(apiKey.id) ? (
                      <EyeOff className="w-4 h-4" />
                    ) : (
                      <Eye className="w-4 h-4" />
                    )}
                  </button>
                </div>
              </div>

              <div className="mt-3 pt-3 border-t border-slate-700 flex items-center justify-between text-xs text-slate-500">
                <span>Created: {formatDate(apiKey.createdAt)}</span>
              </div>
            </div>
          ))}
          {apiKeys.length === 0 && (
            <p className="text-slate-400 text-center py-8">
              No exchanges connected. Add your first connection to get started.
            </p>
          )}
        </div>
      </div>

      {/* Available Exchanges Grid */}
      <div className="bg-slate-800/50 rounded-xl border border-slate-700 p-6">
        <h3 className="text-lg text-white mb-4">Available Exchanges</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {exchanges.map((exchange) => (
            <div
              key={exchange.id}
              className={`bg-slate-900/50 rounded-lg p-4 border border-slate-700`}
            >
              <div className="flex items-center gap-3 mb-2">
                <span className="text-2xl">{exchange.logo}</span>
                <div className="flex-1">
                  <p className="text-white">{exchange.name}</p>
                  <p className="text-xs text-slate-500">{exchange.baseUrl}</p>
                </div>
              </div>
              <span className="inline-block px-2 py-1 bg-green-500/20 text-green-400 rounded text-xs">
                Supported
              </span>
            </div>
          ))}
        </div>
      </div>

      {showAddModal && (
        <AddConnectionModal
          exchanges={exchanges}
          onClose={() => setShowAddModal(false)}
          onAdd={handleAddConnection}
          isLoading={isLoading}
        />
      )}
    </div>
  );
}

// --- Helper Modal Component ---

interface AddConnectionModalProps {
  exchanges: Exchange[];
  onClose: () => void;
  onAdd: (connection: ConnectionFormData) => void; 
  isLoading: boolean;
}

function AddConnectionModal({ exchanges, onClose, onAdd, isLoading }: AddConnectionModalProps) {
  const [formData, setFormData] = useState({
    exchangeId: exchanges.length > 0 ? exchanges[0].id : 0,
    label: '',
    apiKey: '',
    apiSecret: '',
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const selectedExchange = exchanges.find(e => e.id === formData.exchangeId);
    
    onAdd({
      exchangeId: formData.exchangeId,
      exchangeName: selectedExchange?.name || 'Unknown',
      label: formData.label,
      apiKey: formData.apiKey,
      apiSecret: formData.apiSecret,
    });
  };

  return (
    <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4 z-50">
      <div className="bg-slate-800 rounded-xl border border-slate-700 p-6 max-w-md w-full">
        <h3 className="text-xl text-white mb-4">Add Exchange Connection</h3>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm text-slate-300 mb-2">Exchange</label>
            <select
              value={formData.exchangeId}
              onChange={(e) => setFormData({ ...formData, exchangeId: parseInt(e.target.value) })}
              className="w-full bg-slate-900/50 border border-slate-600 rounded-lg px-4 py-2 text-white focus:outline-none focus:border-blue-500"
            >
              {exchanges.map((exchange) => (
                  <option key={exchange.id} value={exchange.id}>
                    {exchange.logo} {exchange.name}
                  </option>
                ))}
            </select>
          </div>
          <div>
            <label className="block text-sm text-slate-300 mb-2">Connection Label</label>
            <input
              type="text"
              value={formData.label}
              onChange={(e) => setFormData({ ...formData, label: e.target.value })}
              className="w-full bg-slate-900/50 border border-slate-600 rounded-lg px-4 py-2 text-white focus:outline-none focus:border-blue-500"
              placeholder="e.g., Main Trading Account"
              required
            />
          </div>
          <div>
            <label className="block text-sm text-slate-300 mb-2">API Key</label>
            <input
              type="text"
              value={formData.apiKey}
              onChange={(e) => setFormData({ ...formData, apiKey: e.target.value })}
              className="w-full bg-slate-900/50 border border-slate-600 rounded-lg px-4 py-2 text-white focus:outline-none focus:border-blue-500"
              placeholder="Your API key"
              required
            />
          </div>
          <div>
            <label className="block text-sm text-slate-300 mb-2">API Secret</label>
            <input
              type="password"
              value={formData.apiSecret}
              onChange={(e) => setFormData({ ...formData, apiSecret: e.target.value })}
              className="w-full bg-slate-900/50 border border-slate-600 rounded-lg px-4 py-2 text-white focus:outline-none focus:border-blue-500"
              placeholder="Your API secret"
              required
            />
          </div>
          <div className="bg-yellow-500/10 border border-yellow-500/30 rounded-lg p-3">
            <p className="text-xs text-yellow-400">
              <strong>Security Tip:</strong> Use read-only API keys with withdrawal permissions
              disabled for maximum security.
            </p>
          </div>
          <div className="flex gap-3 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 bg-slate-700 hover:bg-slate-600 text-white py-2 rounded-lg transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isLoading}
              className="flex-1 bg-blue-600 hover:bg-blue-700 text-white py-2 rounded-lg transition-colors flex justify-center items-center"
            >
              {isLoading ? <RefreshCw className="w-4 h-4 animate-spin" /> : "Add Connection"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}