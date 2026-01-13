import React, { useState, useEffect, useCallback, useRef, useMemo } from 'react';
import axios from 'axios';
import { Plus, Edit2, Trash2, Search, RefreshCw, ArrowUpCircle, ArrowDownCircle, X, Check, Loader2 } from 'lucide-react';

// --- Types ---
interface PortfolioItem {
  symbol: string;
  quantity: number;
  avgBuyPrice: number;
  currentPrice: number;
  value: number;
  pl: number;
  plPercent: number;
  change24hPercent: number;
  walletType?: string; 
  exchange?: string;   
}

interface Trade {
  id: number;
  symbol: string;
}

interface AssetPrice {
  symbol: string;
  price: number;
  name: string;
}

const SUPPORTED_COINS = [
  { symbol: 'BTC', name: 'Bitcoin' },
  { symbol: 'ETH', name: 'Ethereum' },
  { symbol: 'SOL', name: 'Solana' },
  { symbol: 'MATIC', name: 'Polygon' },
  { symbol: 'LINK', name: 'Chainlink' },
  { symbol: 'USDT', name: 'Tether' },
  { symbol: 'ADA', name: 'Cardano' },
  { symbol: 'DOGE', name: 'Dogecoin' },
  { symbol: 'XRP', name: 'Ripple' },
  { symbol: 'DOT', name: 'Polkadot' },
  { symbol: 'LTC', name: 'Litecoin' },
  { symbol: 'SHIB', name: 'Shiba Inu' },
  { symbol: 'AVAX', name: 'Avalanche' },
  { symbol: 'TRX', name: 'Tron' },
  { symbol: 'ATOM', name: 'Cosmos' },
  // Example for scam token
  { symbol: 'SCAM', name: 'Scam-token-id' },
];

const getUser = () => JSON.parse(localStorage.getItem('user') || '{}');

export function Portfolio() {
  const [holdings, setHoldings] = useState<PortfolioItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  
  const [showModal, setShowModal] = useState(false);
  const [modalType, setModalType] = useState<'ADD' | 'EDIT'>('ADD');
  const [selectedAsset, setSelectedAsset] = useState<PortfolioItem | null>(null);

  const user = getUser();

  const fetchPortfolio = useCallback(async () => {
    if (!user.id) return;
    setIsLoading(true);
    try {
      const response = await axios.get<PortfolioItem[]>(`http://localhost:5000/api/portfolio/${user.id}`);
      setHoldings(response.data);
    } catch (error) {
      console.error("Failed to fetch portfolio", error);
    } finally {
      setIsLoading(false);
    }
  }, [user.id]);

  useEffect(() => {
    fetchPortfolio();
  }, [fetchPortfolio]);

  const handleDelete = async (symbol: string) => {
    if (!window.confirm(`Are you sure you want to delete ALL history for ${symbol}?`)) return;
    try {
      const res = await axios.get<Trade[]>(`http://localhost:5000/api/trades/${user.id}`);
      const assetTrades = res.data.filter((t) => t.symbol === symbol);
      await Promise.all(assetTrades.map(t => axios.delete(`http://localhost:5000/api/trades/${t.id}`)));
      fetchPortfolio(); 
    } catch (error) {
       console.error("Delete failed", error);
       alert("Failed to delete asset.");
    }
  };

  const openAddModal = () => {
    setModalType('ADD');
    setSelectedAsset(null);
    setShowModal(true);
  };

  const openEditModal = (asset: PortfolioItem) => {
    setModalType('EDIT');
    setSelectedAsset(asset);
    setShowModal(true);
  };

  const filteredHoldings = holdings.filter((h) =>
    h.symbol.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const totalValue = holdings.reduce((acc, h) => acc + h.value, 0);
  const totalPL = holdings.reduce((acc, h) => acc + h.pl, 0);
  const totalCost = holdings.reduce((acc, h) => acc + (h.quantity * h.avgBuyPrice), 0);
  const totalPLPercent = totalCost > 0 ? ((totalPL / totalCost) * 100).toFixed(2) : '0.00';

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl text-white mb-1">Portfolio Overview</h2>
          <p className="text-slate-400">Aggregated view of all your assets</p>
        </div>
        <div className="flex gap-2">
            <button onClick={openAddModal} className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg transition-colors">
              <Plus className="w-4 h-4" /> Add Trade
            </button>
            <button onClick={fetchPortfolio} className="p-2 bg-slate-700 hover:bg-slate-600 text-white rounded-lg transition-colors">
              <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin' : ''}`} />
            </button>
        </div>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="bg-slate-800/50 rounded-xl border border-slate-700 p-6">
          <p className="text-sm text-slate-400 mb-2">Total Value</p>
          <p className="text-2xl text-white">${totalValue.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</p>
        </div>
        <div className="bg-slate-800/50 rounded-xl border border-slate-700 p-6">
          <p className="text-sm text-slate-400 mb-2">Total Cost Basis</p>
          <p className="text-2xl text-white">${totalCost.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</p>
        </div>
        <div className="bg-slate-800/50 rounded-xl border border-slate-700 p-6">
          <p className="text-sm text-slate-400 mb-2">Total Unrealized P&L</p>
          <p className={`text-2xl ${totalPL >= 0 ? 'text-green-400' : 'text-red-400'}`}>
            ${totalPL.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })} ({totalPL >= 0 ? '+' : ''}{totalPLPercent}%)
          </p>
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
            placeholder="Search by symbol..."
            className="w-full bg-slate-800/50 border border-slate-700 rounded-lg pl-10 pr-4 py-2 text-white placeholder-slate-500 focus:outline-none focus:border-blue-500"
          />
        </div>
      </div>

      {/* Holdings Table */}
      <div className="bg-slate-800/50 rounded-xl border border-slate-700 overflow-hidden">
        {isLoading ? (
            <div className="p-8 flex justify-center items-center text-slate-400">
                <RefreshCw className="animate-spin mr-2 w-5 h-5" /> Loading Portfolio...
            </div>
        ) : (
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-slate-700">
                <th className="text-left text-sm text-slate-400 px-6 py-4">Asset</th>
                <th className="text-right text-sm text-slate-400 px-6 py-4">Quantity</th>
                <th className="text-right text-sm text-slate-400 px-6 py-4">Avg Cost</th>
                <th className="text-right text-sm text-slate-400 px-6 py-4">Current Price</th>
                <th className="text-right text-sm text-slate-400 px-6 py-4">Value</th>
                <th className="text-right text-sm text-slate-400 px-6 py-4">24h</th>
                <th className="text-right text-sm text-slate-400 px-6 py-4">P&L</th>
                <th className="text-left text-sm text-slate-400 px-6 py-4">Location</th>
                <th className="text-right text-sm text-slate-400 px-6 py-4">Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredHoldings.map((holding, idx) => (
                  <tr key={idx} className="border-b border-slate-700/50 hover:bg-slate-700/30">
                    <td className="px-6 py-4">
                      <p className="text-white font-bold">{holding.symbol}</p>
                    </td>
                    <td className="px-6 py-4 text-right text-white">{holding.quantity.toLocaleString()}</td>
                    <td className="px-6 py-4 text-right text-slate-300">
                      ${holding.avgBuyPrice.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                    </td>
                    <td className="px-6 py-4 text-right text-white font-medium">
                      ${holding.currentPrice.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                    </td>
                    <td className="px-6 py-4 text-right text-white font-mono">
                      ${holding.value.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                    </td>
                    <td className="px-6 py-4 text-right">
                        <div className={`flex items-center justify-end gap-1 ${holding.change24hPercent >= 0 ? 'text-green-400' : 'text-red-400'}`}>
                            {holding.change24hPercent >= 0 ? <ArrowUpCircle className="w-3 h-3"/> : <ArrowDownCircle className="w-3 h-3"/>}
                            <span>{holding.change24hPercent.toFixed(2)}%</span>
                        </div>
                    </td>
                    <td className="px-6 py-4 text-right">
                      <div className={holding.pl >= 0 ? 'text-green-400' : 'text-red-400'}>
                        <p>${holding.pl.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</p>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                        <div>
                            <span 
                                className={`inline-block px-2 py-1 rounded text-xs ${
                                    (holding.walletType || 'exchange').toLowerCase() === 'wallet' 
                                    ? 'bg-purple-500/20 text-purple-400' 
                                    : (holding.walletType === 'mixed' ? 'bg-orange-500/20 text-orange-400' : 'bg-blue-500/20 text-blue-400')
                                }`}
                            >
                                {(holding.walletType || 'exchange').toUpperCase()}
                            </span>
                            <p className="text-sm text-slate-400 mt-1">
                                {holding.exchange || 'Unknown'}
                            </p>
                        </div>
                    </td>
                    <td className="px-6 py-4 text-right">
                      <div className="flex items-center justify-end gap-2">
                        <button onClick={() => openEditModal(holding)} className="p-2 text-slate-400 hover:text-blue-400 transition-colors">
                          <Edit2 className="w-4 h-4" />
                        </button>
                        <button onClick={() => handleDelete(holding.symbol)} className="p-2 text-slate-400 hover:text-red-400 transition-colors">
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                )
              )}
              {filteredHoldings.length === 0 && (
                 <tr>
                     <td colSpan={9} className="text-center py-8 text-slate-500 italic">No holdings found. Add trades to see data.</td>
                 </tr>
              )}
            </tbody>
          </table>
        </div>
        )}
      </div>

      {showModal && (
        <PortfolioModal 
            type={modalType}
            initialData={selectedAsset}
            onClose={() => setShowModal(false)}
            onSuccess={() => {
                setShowModal(false);
                fetchPortfolio();
            }}
        />
      )}
    </div>
  );
}

// --- MODAL COMPONENT ---
interface ModalProps {
    type: 'ADD' | 'EDIT';
    initialData: PortfolioItem | null;
    onClose: () => void;
    onSuccess: () => void;
}

function PortfolioModal({ type, initialData, onClose, onSuccess }: ModalProps) {
    const user = getUser();
    const [formData, setFormData] = useState({
        side: 'BUY', 
        symbol: initialData?.symbol || '',
        name: '', 
        quantity: initialData?.quantity ? initialData.quantity.toString() : '',
        price: initialData?.avgBuyPrice ? initialData.avgBuyPrice.toString() : '', 
        currentPrice: initialData?.currentPrice ? initialData.currentPrice.toString() : '',
        walletType: initialData?.walletType === 'wallet' ? 'wallet' : 'exchange', 
        exchangeName: (initialData?.exchange === 'Multiple Sources') ? '' : (initialData?.exchange || '')
    });

    const [isFetchingPrice, setIsFetchingPrice] = useState(false);
    const [showSuggestions, setShowSuggestions] = useState(false);
    const wrapperRef = useRef<HTMLDivElement>(null);

    const suggestions = useMemo(() => {
        if (!formData.symbol) return [];
        const lower = formData.symbol.toLowerCase();
        return SUPPORTED_COINS.filter(c => 
            c.symbol.toLowerCase().includes(lower) || c.name.toLowerCase().includes(lower)
        );
    }, [formData.symbol]);

    useEffect(() => {
        function handleClickOutside(event: MouseEvent) {
            if (wrapperRef.current && !wrapperRef.current.contains(event.target as Node)) {
                setShowSuggestions(false);
            }
        }
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, [wrapperRef]);

    const fetchLivePrice = async (symbol: string) => {
        if (!symbol) return;
        setIsFetchingPrice(true);
        try {
            const res = await axios.get<AssetPrice[]>('http://localhost:5000/api/pricing/assets');
            const asset = res.data.find(a => a.symbol.toUpperCase() === symbol.toUpperCase());
            if (asset) {
                setFormData(prev => ({ ...prev, currentPrice: asset.price.toString(), name: asset.name }));
            }
        } catch (error) {
            console.error("Failed to fetch price", error);
        } finally {
            setIsFetchingPrice(false);
        }
    };

    const selectCoin = (coin: {symbol: string, name: string}) => {
        setFormData(prev => ({ ...prev, symbol: coin.symbol, name: coin.name }));
        setShowSuggestions(false);
        fetchLivePrice(coin.symbol); 
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            
            if (type === 'EDIT' && initialData) {
                const res = await axios.get<Trade[]>(`http://localhost:5000/api/trades/${user.id}`);
                const assetTrades = res.data.filter(t => t.symbol === initialData.symbol);
                await Promise.all(assetTrades.map(t => axios.delete(`http://localhost:5000/api/trades/${t.id}`)));
            }

            // Post the new Trade
            await axios.post('http://localhost:5000/api/trades', {
                userId: user.id,
                symbol: formData.symbol.toUpperCase(),
                type: formData.side, 
                quantity: parseFloat(formData.quantity),
                price: parseFloat(formData.price), 
                fee: 0, 
                exchange: formData.exchangeName || 'Manual', 
                storageType: formData.walletType 
            });
            onSuccess();
        } catch (err) {
            console.error("Failed to save trade:", err);
            alert('Failed to save trade.');
        }
    };

    return (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4 z-50">
           <div className="bg-slate-800 rounded-xl border border-slate-700 p-6 max-w-md w-full relative">
              <button onClick={onClose} className="absolute top-4 right-4 text-slate-400 hover:text-white">
                  <X className="w-5 h-5" />
              </button>
              
              <h3 className="text-xl text-white mb-4">
                  {type === 'ADD' ? 'Log New Trade' : `Adjust ${formData.symbol}`}
              </h3>
              
              <form onSubmit={handleSubmit} className="space-y-4">
                 
                 {/* ---BUY / SELL TOGGLE --- */}
                 <div className="flex gap-4 p-1 bg-slate-900 rounded-lg">
                    <button 
                        type="button"
                        onClick={() => setFormData({...formData, side: 'BUY'})}
                        className={`flex-1 py-2 rounded-md font-bold transition-all ${
                            formData.side === 'BUY' 
                            ? 'bg-green-600 text-white shadow-lg' 
                            : 'text-slate-400 hover:text-white'
                        }`}
                    >
                        BUY
                    </button>
                    <button 
                        type="button"
                        onClick={() => setFormData({...formData, side: 'SELL'})}
                        className={`flex-1 py-2 rounded-md font-bold transition-all ${
                            formData.side === 'SELL' 
                            ? 'bg-red-600 text-white shadow-lg' 
                            : 'text-slate-400 hover:text-white'
                        }`}
                    >
                        SELL
                    </button>
                 </div>

                 <div className="grid grid-cols-2 gap-4 relative" ref={wrapperRef}>
                    <div>
                        <label className="block text-sm text-slate-300 mb-2">Symbol</label>
                        <input type="text" className="w-full bg-slate-900/50 border border-slate-600 rounded-lg px-4 py-2 text-white focus:outline-none focus:border-blue-500" required value={formData.symbol} onChange={e => {setFormData({...formData, symbol: e.target.value}); setShowSuggestions(true);}} onFocus={() => setShowSuggestions(true)} disabled={type === 'EDIT'} autoComplete="off" />
                        {showSuggestions && suggestions.length > 0 && (
                           <div className="absolute top-[70px] left-0 w-full bg-slate-800 border border-slate-600 rounded-lg shadow-xl z-10 max-h-48 overflow-y-auto">
                               {suggestions.map(coin => (
                                   <div key={coin.symbol} onClick={() => selectCoin(coin)} className="p-2 hover:bg-slate-700 cursor-pointer flex justify-between items-center border-b border-slate-700/50 last:border-0">
                                       <div><span className="text-white font-bold">{coin.symbol}</span><span className="text-slate-400 text-xs ml-2">{coin.name}</span></div>
                                       {formData.symbol === coin.symbol && <Check className="w-4 h-4 text-green-400"/>}
                                   </div>
                               ))}
                           </div>
                       )}
                    </div>
                    <div>
                        <label className="block text-sm text-slate-300 mb-2">Quantity</label>
                        <input type="number" step="any" className="w-full bg-slate-900/50 border border-slate-600 rounded-lg px-4 py-2 text-white focus:outline-none focus:border-blue-500" required value={formData.quantity} onChange={e => setFormData({...formData, quantity: e.target.value})} />
                    </div>
                 </div>

                 <div className="grid grid-cols-2 gap-4">
                    <div>
                        <label className="block text-sm text-slate-300 mb-2">{formData.side === 'BUY' ? 'Buy Price ($)' : 'Sell Price ($)'}</label>
                        <input type="number" step="any" className="w-full bg-slate-900/50 border border-slate-600 rounded-lg px-4 py-2 text-white focus:outline-none focus:border-blue-500" required value={formData.price} onChange={e => setFormData({...formData, price: e.target.value})} />
                    </div>
                    <div>
                        <label className="block text-sm text-slate-300 mb-2">Market Price ($)</label>
                        <div className="relative">
                            <input type="number" step="any" className="w-full bg-slate-900/50 border border-slate-600 rounded-lg px-4 py-2 text-white focus:outline-none focus:border-blue-500" value={formData.currentPrice} disabled />
                            {isFetchingPrice && <div className="absolute right-3 top-1/2 -translate-y-1/2"><Loader2 className="w-4 h-4 animate-spin text-blue-400" /></div>}
                        </div>
                    </div>
                 </div>

                 <div>
                    <label className="block text-sm text-slate-300 mb-2">Location Type</label>
                    <select className="w-full bg-slate-900/50 border border-slate-600 rounded-lg px-4 py-2 text-white focus:outline-none focus:border-blue-500" value={formData.walletType} onChange={e => setFormData({...formData, walletType: e.target.value})}>
                        <option value="exchange">Exchange</option>
                        <option value="wallet">Wallet</option>
                    </select>
                 </div>

                 <div>
                    <label className="block text-sm text-slate-300 mb-2">{formData.walletType === 'exchange' ? 'Exchange Name' : 'Wallet Address'}</label>
                    <input type="text" className="w-full bg-slate-900/50 border border-slate-600 rounded-lg px-4 py-2 text-white focus:outline-none focus:border-blue-500" value={formData.exchangeName} onChange={e => setFormData({...formData, exchangeName: e.target.value})} placeholder={formData.walletType === 'exchange' ? "e.g. Binance" : "e.g. 0x123..."} />
                 </div>

                 <div className="flex gap-3 pt-2">
                    <button type="button" onClick={onClose} className="flex-1 bg-slate-700 hover:bg-slate-600 text-white py-2 rounded-lg transition-colors">Cancel</button>
                    <button type="submit" className={`flex-1 py-2 rounded-lg transition-colors text-white ${formData.side === 'BUY' ? 'bg-green-600 hover:bg-green-700' : 'bg-red-600 hover:bg-red-700'}`}>
                        {type === 'ADD' ? (formData.side === 'BUY' ? 'Confirm Buy' : 'Confirm Sell') : 'Update Trade'}
                    </button>
                 </div>
              </form>
           </div>
        </div>
    );
}