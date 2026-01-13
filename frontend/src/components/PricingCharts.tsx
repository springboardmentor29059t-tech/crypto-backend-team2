import React, { useState, useEffect, useCallback } from 'react';
import axios from 'axios';
import { TrendingUp, TrendingDown, Clock, DollarSign, RefreshCw } from 'lucide-react';
import { 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  ResponsiveContainer, 
  Area, 
  AreaChart 
} from 'recharts';

// --- Types Matching Backend DTOs ---
interface Asset {
  symbol: string;
  name: string;
  price: number;
  change24h: number;
  marketCap: number;
  volume24h: number;
}

interface ChartPoint {
  date: string;
  price: number;
}

export function PricingCharts() {
  const [assets, setAssets] = useState<Asset[]>([]);
  const [selectedAsset, setSelectedAsset] = useState<Asset | null>(null);
  const [chartData, setChartData] = useState<ChartPoint[]>([]);
  const [timeRange, setTimeRange] = useState<'7d' | '30d' | '90d' | '1y'>('30d');
  const [isLoading, setIsLoading] = useState(true);

  // 1. Fetch Asset List
  const fetchAssets = useCallback(async () => {
    try {
      const res = await axios.get<Asset[]>('http://localhost:5000/api/pricing/assets');
      setAssets(res.data);
      
      if (!selectedAsset && res.data.length > 0) {
        setSelectedAsset(res.data[0]);
      } else if (selectedAsset) {
        
        const updated = res.data.find(a => a.symbol === selectedAsset.symbol);
        if (updated) setSelectedAsset(updated);
      }
    } catch (error) {
      console.error("Failed to fetch assets", error);
    } finally {
      setIsLoading(false);
    }
  }, [selectedAsset]);

  // 2. Fetch Chart History
  const fetchHistory = useCallback(async () => {
    if (!selectedAsset) return;
    
    let days = 30;
    if (timeRange === '7d') days = 7;
    if (timeRange === '90d') days = 90;
    if (timeRange === '1y') days = 365;

    try {
      const res = await axios.get<ChartPoint[]>(
        `http://localhost:5000/api/pricing/history/${selectedAsset.symbol}?days=${days}`
      );
      setChartData(res.data);
    } catch (error) {
      console.error("Failed to fetch history", error);
    }
  }, [selectedAsset, timeRange]);

  // Initial Load & Timer
  useEffect(() => {
    fetchAssets();
    const interval = setInterval(fetchAssets, 60000); // Live price update every 60s
    return () => clearInterval(interval);
  }, []); 

  useEffect(() => {
    fetchHistory();
  }, [fetchHistory]);

  if (isLoading && assets.length === 0) {
    return (
        <div className="flex justify-center items-center h-64 text-slate-400">
            <RefreshCw className="animate-spin mr-2" /> Loading Market Data...
        </div>
    );
  }

  if (!selectedAsset) return null;

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl text-white mb-1">Pricing & Charts</h2>
        <p className="text-slate-400">Real-time pricing with historical data</p>
      </div>

      {/* Asset Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {assets.slice(0, 4).map((asset) => (
          <button
            key={asset.symbol}
            onClick={() => setSelectedAsset(asset)}
            className={`text-left p-4 rounded-xl border transition-colors ${
              selectedAsset.symbol === asset.symbol
                ? 'bg-blue-600/20 border-blue-500'
                : 'bg-slate-800/50 border-slate-700 hover:border-slate-600'
            }`}
          >
            <div className="flex items-center justify-between mb-2">
              <div className="flex items-center gap-2">
                <div className="w-8 h-8 bg-blue-600 rounded-full flex items-center justify-center">
                  <span className="text-xs text-white">{asset.symbol.slice(0, 2)}</span>
                </div>
                <div>
                  <p className="text-white">{asset.symbol}</p>
                  <p className="text-xs text-slate-400">{asset.name}</p>
                </div>
              </div>
            </div>
            <p className="text-xl text-white mb-1">${asset.price.toLocaleString()}</p>
            <div className="flex items-center gap-1">
              {asset.change24h >= 0 ? (
                <TrendingUp className="w-4 h-4 text-green-400" />
              ) : (
                <TrendingDown className="w-4 h-4 text-red-400" />
              )}
              <span
                className={`text-sm ${
                  asset.change24h >= 0 ? 'text-green-400' : 'text-red-400'
                }`}
              >
                {asset.change24h >= 0 ? '+' : ''}
                {asset.change24h.toFixed(2)}%
              </span>
            </div>
          </button>
        ))}
      </div>

      {/* Selected Asset Details */}
      <div className="bg-slate-800/50 rounded-xl border border-slate-700 p-6">
        <div className="flex items-center justify-between mb-6">
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 bg-blue-600 rounded-full flex items-center justify-center">
              <span className="text-white">{selectedAsset.symbol.slice(0, 2)}</span>
            </div>
            <div>
              <h3 className="text-xl text-white">{selectedAsset.name}</h3>
              <p className="text-slate-400">{selectedAsset.symbol}</p>
            </div>
          </div>
          <div className="text-right">
            <p className="text-3xl text-white mb-1">${selectedAsset.price.toLocaleString()}</p>
            <div className="flex items-center gap-2 justify-end">
              {selectedAsset.change24h >= 0 ? (
                <TrendingUp className="w-5 h-5 text-green-400" />
              ) : (
                <TrendingDown className="w-5 h-5 text-red-400" />
              )}
              <span
                className={`text-lg ${
                  selectedAsset.change24h >= 0 ? 'text-green-400' : 'text-red-400'
                }`}
              >
                {selectedAsset.change24h >= 0 ? '+' : ''}
                {selectedAsset.change24h.toFixed(2)}%
              </span>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4 mb-6">
          <div className="bg-slate-900/50 rounded-lg p-4">
            <div className="flex items-center gap-2 mb-2">
              <DollarSign className="w-4 h-4 text-slate-400" />
              <p className="text-sm text-slate-400">Market Cap</p>
            </div>
            <p className="text-lg text-white">
              ${(selectedAsset.marketCap / 1000000000).toFixed(2)}B
            </p>
          </div>
          <div className="bg-slate-900/50 rounded-lg p-4">
            <div className="flex items-center gap-2 mb-2">
              <Clock className="w-4 h-4 text-slate-400" />
              <p className="text-sm text-slate-400">24h Volume</p>
            </div>
            <p className="text-lg text-white">
              ${(selectedAsset.volume24h / 1000000000).toFixed(2)}B
            </p>
          </div>
        </div>

        {/* Time Range Selector */}
        <div className="flex gap-2 mb-6">
          {(['7d', '30d', '90d', '1y'] as const).map((range) => (
            <button
              key={range}
              onClick={() => setTimeRange(range)}
              className={`px-4 py-2 rounded-lg transition-colors ${
                timeRange === range
                  ? 'bg-blue-600 text-white'
                  : 'bg-slate-700/50 text-slate-300 hover:bg-slate-700'
              }`}
            >
              {range}
            </button>
          ))}
        </div>

        {/* Price Chart */}
        <ResponsiveContainer width="100%" height={350}>
          <AreaChart data={chartData}>
            <defs>
              <linearGradient id="priceGradient" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.3} />
                <stop offset="95%" stopColor="#3b82f6" stopOpacity={0} />
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
            <XAxis dataKey="date" stroke="#94a3b8" />
            <YAxis stroke="#94a3b8" domain={['auto', 'auto']} />
            <Tooltip
              contentStyle={{
                backgroundColor: '#1e293b',
                border: '1px solid #334155',
                borderRadius: '8px',
                color: '#fff',
              }}
              formatter={(val: number) => [`$${val.toLocaleString()}`, "Price"]}
            />
            <Area
              type="monotone"
              dataKey="price"
              stroke="#3b82f6"
              strokeWidth={2}
              fill="url(#priceGradient)"
            />
          </AreaChart>
        </ResponsiveContainer>
      </div>

      {/* Price Snapshots Table */}
      <div className="bg-slate-800/50 rounded-xl border border-slate-700 p-6">
        <h3 className="text-lg text-white mb-4">Recent Price Snapshots</h3>
        <div className="space-y-2">
          {chartData.slice(-5).reverse().map((snapshot, idx) => (
            <div
              key={idx}
              className="flex items-center justify-between p-3 bg-slate-900/50 rounded-lg"
            >
              <div className="flex items-center gap-3">
                <Clock className="w-4 h-4 text-slate-400" />
                <span className="text-slate-300">{snapshot.date}</span>
              </div>
              <div className="flex items-center gap-4">
                <span className="text-white">${snapshot.price.toLocaleString()}</span>
                <span className="text-xs text-slate-500">via CoinGecko</span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}