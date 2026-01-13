import { useState, useEffect, useCallback } from 'react';
import axios from 'axios';
import { ArrowDownCircle, ArrowUpCircle, Calendar, Search, Download, RefreshCw } from 'lucide-react';

// --- 1. Interface matching Java @JsonProperty ---
interface BackendTrade {
  id: number;
  symbol: string;       
  exchange: string;     
  side: 'BUY' | 'SELL'; 
  quantity: number;
  price: number;
  fee: number;
  executedAt: string;   
  storageType: string;
}

// --- 2. Interface for Frontend Display ---
interface Trade {
  id: number;
  symbol: string;
  side: 'BUY' | 'SELL';
  quantity: number;
  price: number;
  fee: number;
  exchange: string;
  executedAt: string;
  total: number;
}

export function Trades() {
  const [trades, setTrades] = useState<Trade[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterSide, setFilterSide] = useState<'all' | 'BUY' | 'SELL'>('all');

  const user = JSON.parse(localStorage.getItem('user') || '{}');

  const fetchTrades = useCallback(async () => {
    if (!user.id) return;
    setIsLoading(true);
    try {
      const response = await axios.get<BackendTrade[]>(`http://localhost:5000/api/trades/${user.id}`);
      
      const mappedTrades = response.data.map((t) => ({
        id: t.id,
        symbol: t.symbol, 
        side: (t.side || 'BUY').toUpperCase() as 'BUY' | 'SELL',
        quantity: t.quantity,
        price: t.price,
        fee: t.fee || 0,
        exchange: t.exchange || 'Manual', 
        executedAt: t.executedAt, 
        total: (t.quantity * t.price) 
      }));
      
      setTrades(mappedTrades);
    } catch (error) {
      console.error("Failed to load trades", error);
    } finally {
      setIsLoading(false);
    }
  }, [user.id]);

  useEffect(() => {
    fetchTrades();
  }, [fetchTrades]);

  const formatDate = (dateString: string) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const filteredTrades = trades.filter((trade) => {
    // Safe navigation (?.) to prevent crashes
    const sym = trade.symbol?.toLowerCase() || '';
    const exch = trade.exchange?.toLowerCase() || '';
    const search = searchTerm.toLowerCase();

    const matchesSearch = sym.includes(search) || exch.includes(search);
    const matchesSide = filterSide === 'all' || trade.side === filterSide;
    return matchesSearch && matchesSide;
  });

  const totalBuyVolume = trades
    .filter((t) => t.side === 'BUY')
    .reduce((acc, t) => acc + t.total, 0);

  const totalSellVolume = trades
    .filter((t) => t.side === 'SELL')
    .reduce((acc, t) => acc + t.total, 0);

  const totalFees = trades.reduce((acc, t) => acc + (t.fee || 0), 0);

  const exportTrades = () => {
    const csv = [
      ['Date', 'Symbol', 'Side', 'Quantity', 'Price', 'Fee', 'Total', 'Exchange'],
      ...trades.map((t) => [
        t.executedAt,
        t.symbol,
        t.side,
        t.quantity,
        t.price,
        t.fee,
        t.total,
        t.exchange,
      ]),
    ]
      .map((row) => row.join(','))
      .join('\n');

    const blob = new Blob([csv], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'trades.csv';
    a.click();
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl text-white mb-1">Trade History</h2>
          <p className="text-slate-400">View and analyze your trading activity</p>
        </div>
        <div className="flex gap-2">
            <button 
                onClick={fetchTrades} 
                className="flex items-center gap-2 bg-slate-700 hover:bg-slate-600 text-white px-4 py-2 rounded-lg transition-colors"
                title="Refresh Table"
            >
                <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin' : ''}`} />
            </button>

            <button
                onClick={exportTrades}
                className="flex items-center gap-2 bg-slate-700 hover:bg-slate-600 text-white px-4 py-2 rounded-lg transition-colors"
            >
                <Download className="w-4 h-4" />
                Export CSV
            </button>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="bg-slate-800/50 rounded-xl border border-slate-700 p-6">
          <div className="flex items-center gap-3 mb-2">
            <ArrowDownCircle className="w-5 h-5 text-green-400" />
            <p className="text-sm text-slate-400">Total Buy Volume</p>
          </div>
          <p className="text-2xl text-white">
            ${totalBuyVolume.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
          </p>
        </div>
        <div className="bg-slate-800/50 rounded-xl border border-slate-700 p-6">
          <div className="flex items-center gap-3 mb-2">
            <ArrowUpCircle className="w-5 h-5 text-red-400" />
            <p className="text-sm text-slate-400">Total Sell Volume</p>
          </div>
          <p className="text-2xl text-white">
            ${totalSellVolume.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
          </p>
        </div>
        <div className="bg-slate-800/50 rounded-xl border border-slate-700 p-6">
          <div className="flex items-center gap-3 mb-2">
            <Calendar className="w-5 h-5 text-blue-400" />
            <p className="text-sm text-slate-400">Total Fees Paid</p>
          </div>
          <p className="text-2xl text-white">
            ${totalFees.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
          </p>
        </div>
      </div>

      <div className="flex gap-4">
        <div className="flex-1 relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400" />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Search by symbol or exchange..."
            className="w-full bg-slate-800/50 border border-slate-700 rounded-lg pl-10 pr-4 py-2 text-white placeholder-slate-500 focus:outline-none focus:border-blue-500"
          />
        </div>
        <div className="flex gap-2">
          <button
            onClick={() => setFilterSide('all')}
            className={`px-4 py-2 rounded-lg transition-colors ${
              filterSide === 'all'
                ? 'bg-blue-600 text-white'
                : 'bg-slate-800/50 text-slate-300 hover:bg-slate-700/50'
            }`}
          >
            All
          </button>
          <button
            onClick={() => setFilterSide('BUY')}
            className={`px-4 py-2 rounded-lg transition-colors ${
              filterSide === 'BUY'
                ? 'bg-green-600 text-white'
                : 'bg-slate-800/50 text-slate-300 hover:bg-slate-700/50'
            }`}
          >
            Buy
          </button>
          <button
            onClick={() => setFilterSide('SELL')}
            className={`px-4 py-2 rounded-lg transition-colors ${
              filterSide === 'SELL'
                ? 'bg-red-600 text-white'
                : 'bg-slate-800/50 text-slate-300 hover:bg-slate-700/50'
            }`}
          >
            Sell
          </button>
        </div>
      </div>

      <div className="bg-slate-800/50 rounded-xl border border-slate-700 overflow-hidden">
        {isLoading ? (
            <div className="p-8 text-center text-slate-400">Loading trades...</div>
        ) : (
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-slate-700">
                <th className="text-left text-sm text-slate-400 px-6 py-4">Date & Time</th>
                <th className="text-left text-sm text-slate-400 px-6 py-4">Asset</th>
                <th className="text-left text-sm text-slate-400 px-6 py-4">Side</th>
                <th className="text-right text-sm text-slate-400 px-6 py-4">Quantity</th>
                <th className="text-right text-sm text-slate-400 px-6 py-4">Price</th>
                <th className="text-right text-sm text-slate-400 px-6 py-4">Fee</th>
                <th className="text-right text-sm text-slate-400 px-6 py-4">Total</th>
                <th className="text-left text-sm text-slate-400 px-6 py-4">Exchange</th>
              </tr>
            </thead>
            <tbody>
              {filteredTrades.map((trade) => (
                <tr key={trade.id} className="border-b border-slate-700/50 hover:bg-slate-700/30">
                  <td className="px-6 py-4 text-slate-300">{formatDate(trade.executedAt)}</td>
                  <td className="px-6 py-4 text-white font-bold">{trade.symbol}</td>
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-2">
                      {trade.side === 'BUY' ? (
                        <>
                          <ArrowDownCircle className="w-4 h-4 text-green-400" />
                          <span className="text-green-400">Buy</span>
                        </>
                      ) : (
                        <>
                          <ArrowUpCircle className="w-4 h-4 text-red-400" />
                          <span className="text-red-400">Sell</span>
                        </>
                      )}
                    </div>
                  </td>
                  <td className="px-6 py-4 text-right text-white">{trade.quantity}</td>
                  <td className="px-6 py-4 text-right text-white">${trade.price.toLocaleString()}</td>
                  <td className="px-6 py-4 text-right text-slate-300">${trade.fee.toFixed(2)}</td>
                  <td className="px-6 py-4 text-right text-white font-mono">
                    ${trade.total.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                  </td>
                  <td className="px-6 py-4">
                    <span className="inline-block px-2 py-1 bg-blue-500/20 text-blue-400 rounded text-xs">
                      {trade.exchange}
                    </span>
                  </td>
                </tr>
              ))}
              {filteredTrades.length === 0 && (
                  <tr><td colSpan={8} className="text-center py-8 text-slate-500 italic">No trades found.</td></tr>
              )}
            </tbody>
          </table>
        </div>
        )}
      </div>
    </div>
  );
}