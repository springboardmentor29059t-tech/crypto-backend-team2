import React, { useEffect, useState, useCallback } from "react";
import axios from "axios";
import {
  TrendingUp,
  Wallet,
  DollarSign,
  AlertTriangle,
  Activity,
  ArrowUpRight,
  ArrowDownRight,
  RefreshCw
} from "lucide-react";
import {
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  AreaChart,
  Area
} from "recharts";

// --- Interfaces ---
interface User {
  id: number;
  name: string;
  email: string;
  token?: string;
}

interface AssetDto {
  id: number;
  symbol: string;
  name: string;
  price: number;
  balance: number;
  value: number;
  change24h: number; 
}

interface DashboardTotals {
  totalBalance: number;
  totalChange24h: number; 
  totalChangePercent: number;
}

interface ChartDataPoint {
  date: string;
  value: number;
}

const recentAlerts = [
  {
    asset: "ETH",
    type: "price_threshold",
    severity: "low",
    time: "5 hours ago",
  },
];

export default function Dashboard({ user }: { user: User | null }) {
  const [totals, setTotals] = useState<DashboardTotals | null>(null);
  const [topAssets, setTopAssets] = useState<AssetDto[]>([]);
  const [chartData, setChartData] = useState<ChartDataPoint[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isChartLoading, setIsChartLoading] = useState(true);

  const userId = user?.id;

  const fetchDashboardData = useCallback(async () => {
    if (!userId) return;
    try {
      // 1. Fetch Totals
      const totalsRes = await axios.get<DashboardTotals>(`http://localhost:5000/api/dashboard/${userId}`);
      setTotals(totalsRes.data);

      // 2. Fetch Assets (for Top Holdings list)
      const assetsRes = await axios.get<any[]>(`http://localhost:5000/api/portfolio/${userId}`);
      
      // Map Backend "Portfolio" format to Frontend "AssetDto" format
      const mappedAssets: AssetDto[] = assetsRes.data.map(item => ({
        id: item.id,
        symbol: item.symbol,
        name: item.name,
        price: item.currentPrice,
        balance: item.quantity,
        value: item.value,
        change24h: item.change24hPercent || 0
      }));
      
      // Sort by value (highest first) and take top 5
      setTopAssets(mappedAssets.sort((a, b) => b.value - a.value).slice(0, 5));

    } catch (error) {
      console.error("Failed to fetch dashboard data:", error);
    } finally {
      setIsLoading(false);
    }
  }, [userId]);

  const fetchChart = useCallback(async () => {
    if (!userId) return;
    setIsChartLoading(true);
    try {
      await new Promise(resolve => setTimeout(resolve, 500));
      const response = await axios.get<ChartDataPoint[]>(`http://localhost:5000/api/dashboard/chart/${userId}`);
      if(response.data && response.data.length > 0) {
          setChartData(response.data);
      }
    } catch (error) {
      console.error("Failed to fetch chart:", error);
    } finally {
      setIsChartLoading(false);
    }
  }, [userId]);

  useEffect(() => {
    const init = async () => {
        setIsLoading(true);
        await fetchDashboardData();
        fetchChart();
    };
    if (userId) init();
    else setIsLoading(false);

    // Auto-refresh: Dashboard fast (60s), Chart slow (5 mins)
    const intervalDash = setInterval(fetchDashboardData, 60000);
    const intervalChart = setInterval(fetchChart, 300000); 

    return () => {
        clearInterval(intervalDash);
        clearInterval(intervalChart);
    };
  }, [userId, fetchDashboardData, fetchChart]);

  if (isLoading) {
    return (
       <div className="flex justify-center items-center h-64 text-slate-400">
          <Activity className="animate-spin mr-2" /> Loading Dashboard...
       </div>
    );
  }

  // Safety Defaults
  const totalBalance = totals?.totalBalance || 0;
  const totalChange24h = totals?.totalChange24h || 0;
  const totalChangePercent = totals?.totalChangePercent || 0;
  const isPositive = totalChange24h >= 0;

  return (
    <div className="space-y-6 p-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl text-white mb-1">Portfolio Dashboard</h2>
          <p className="text-slate-400">Overview of your crypto holdings and performance</p>
        </div>
        <button 
          onClick={() => { setIsLoading(true); fetchDashboardData(); fetchChart(); }} 
          className="p-2 bg-slate-800 hover:bg-slate-700 rounded-lg text-slate-300 transition-colors"
        >
          <RefreshCw className="w-4 h-4" />
        </button>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Total Portfolio Value"
          value={`$${totalBalance.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`}
          change={`${Math.abs(totalChangePercent).toFixed(2)}%`}
          icon={DollarSign}
          positive={isPositive}
        />
        <StatCard
          title="Total Holdings"
          value={topAssets.length.toString()}
          subtitle="Top Assets Tracked"
          icon={Wallet}
        />
        <StatCard
          title="24h Change"
          value={`$${Math.abs(totalChange24h).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`}
          change="Today"
          icon={TrendingUp}
          positive={isPositive}
        />
        <StatCard
          title="Active Alerts"
          value={recentAlerts.length.toString()}
          subtitle="Requires attention"
          icon={AlertTriangle}
          warning
        />
      </div>

      {/* Portfolio Chart */}
      <div className="bg-slate-800/50 rounded-xl border border-slate-700 p-6">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h3 className="text-lg text-white mb-1">Portfolio Performance</h3>
            <p className="text-sm text-slate-400">Last 7 Days</p>
          </div>
          <Activity className="w-5 h-5 text-blue-400" />
        </div>
        
        <div className="h-[300px] w-full">
          {isChartLoading ? (
             <div className="h-full flex items-center justify-center text-slate-500">Loading Chart...</div>
          ) : chartData.length > 0 ? (
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={chartData}>
                <defs>
                  <linearGradient id="colorValue" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.3}/>
                    <stop offset="95%" stopColor="#3b82f6" stopOpacity={0}/>
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#334155" vertical={false} />
                <XAxis dataKey="date" stroke="#94a3b8" tick={{ fontSize: 12 }} tickMargin={10} />
                <YAxis stroke="#94a3b8" tick={{ fontSize: 12 }} tickFormatter={(val) => `$${val}`} width={60} />
                <Tooltip
                  contentStyle={{ backgroundColor: "#1e293b", border: "1px solid #334155", borderRadius: "8px", color: "#fff" }}
                  formatter={(value: number) => [`$${value.toLocaleString()}`, "Value"]}
                />
                <Area type="monotone" dataKey="value" stroke="#3b82f6" strokeWidth={2} fillOpacity={1} fill="url(#colorValue)" />
              </AreaChart>
            </ResponsiveContainer>
          ) : (
            <div className="h-full flex flex-col items-center justify-center text-slate-500">
               <p>Chart data unavailable (API Rate Limit).</p>
               <button onClick={fetchChart} className="mt-2 text-blue-400 underline text-sm">Try Again</button>
            </div>
          )}
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Top Holdings */}
        <div className="bg-slate-800/50 rounded-xl border border-slate-700 p-6">
          <h3 className="text-lg text-white mb-4">Top Holdings</h3>
          <div className="space-y-3">
            {topAssets.map((holding) => (
              <div key={holding.id} className="flex items-center justify-between p-3 bg-slate-900/50 rounded-lg">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 bg-blue-600 rounded-full flex items-center justify-center">
                    <span className="text-sm text-white font-bold">{holding.symbol.slice(0, 1)}</span>
                  </div>
                  <div>
                    <p className="text-white font-medium">{holding.symbol}</p>
                    <p className="text-xs text-slate-400">{holding.balance.toLocaleString()} {holding.symbol}</p>
                  </div>
                </div>
                <div className="text-right">
                  <p className="text-white font-medium">${holding.value.toLocaleString(undefined, { maximumFractionDigits: 2 })}</p>
                  <p className={`text-sm flex items-center justify-end gap-1 ${holding.change24h >= 0 ? "text-green-400" : "text-red-400"}`}>
                    {holding.change24h >= 0 ? <ArrowUpRight className="w-3 h-3"/> : <ArrowDownRight className="w-3 h-3"/>}
                    {Math.abs(holding.change24h).toFixed(2)}%
                  </p>
                </div>
              </div>
            ))}
            {topAssets.length === 0 && (
                <p className="text-slate-400 text-center py-4">No holdings yet. Sync an exchange to get started.</p>
            )}
          </div>
        </div>

        {/* Recent Alerts */}
        {/* <div className="bg-slate-800/50 rounded-xl border border-slate-700 p-6">
          <h3 className="text-lg text-white mb-4">Recent Alerts</h3>
          <div className="space-y-3">
            {recentAlerts.map((alert, idx) => (
              <div key={idx} className={`p-4 rounded-lg border ${alert.severity === "high" ? "bg-red-500/10 border-red-500/30" : "bg-yellow-500/10 border-yellow-500/30"}`}>
                <div className="flex items-start gap-3">
                  <AlertTriangle className={`w-5 h-5 mt-0.5 ${alert.severity === "high" ? "text-red-400" : "text-yellow-400"}`} />
                  <div className="flex-1">
                    <p className="text-white">{alert.asset}</p>
                    <p className="text-sm text-slate-400 mt-1">{alert.type.replace("_", " ")}</p>
                    <p className="text-xs text-slate-500 mt-1">{alert.time}</p>
                  </div>
                </div>
              </div>
            ))}
            {recentAlerts.length === 0 && <p className="text-slate-400 text-center py-8">No active alerts</p>}
          </div>
        </div> */}
      </div>
    </div>
  );
}

interface StatCardProps {
  title: string;
  value: string;
  change?: string;
  subtitle?: string;
  icon: React.ElementType;
  positive?: boolean;
  warning?: boolean;
}

function StatCard({
  title,
  value,
  change,
  subtitle,
  icon: Icon,
  positive,
  warning,
}: StatCardProps) {
  return (
    <div className="bg-slate-800/50 rounded-xl border border-slate-700 p-6">
      <div className="flex items-center justify-between mb-3">
        <p className="text-sm text-slate-400">{title}</p>
        <Icon className={`w-5 h-5 ${warning ? "text-yellow-400" : positive ? "text-green-400" : "text-red-400" }`} />
      </div>
      <p className="text-2xl text-white mb-1">{value}</p>
      {change && (
        <p className={`text-sm ${positive ? "text-green-400" : "text-red-400"}`}>
          {change}
        </p>
      )}
      {subtitle && (
        <p className="text-sm text-slate-400">{subtitle}</p>
      )}
    </div>
  );
}