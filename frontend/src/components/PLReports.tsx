import { useState, useEffect } from 'react';
import { Download, TrendingUp, TrendingDown, FileText, DollarSign, Calendar } from 'lucide-react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, Legend } from 'recharts';

interface PLData {
  symbol: string;
  realized: number;
  unrealized: number;
  total: number;
}

const COLORS = ['#3b82f6', '#8b5cf6', '#ec4899', '#f59e0b', '#10b981'];

export function PLReports() {
  const [plData, setPlData] = useState<PLData[]>([]);
  
  const [taxSummary, setTaxSummary] = useState<any>(null);
  
  const [loading, setLoading] = useState(true);
  const [selectedYear, setSelectedYear] = useState('2025');

  useEffect(() => {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    if (user.id) {
        // 1. Fetch P&L Details
        fetch(`http://localhost:5000/api/analytics/pnl-details/${user.id}`)
            .then(res => res.json())
            .then(data => setPlData(data))
            .catch(console.error);

        // 2. Fetch Tax Estimates
        fetch(`http://localhost:5000/api/analytics/tax-summary/${user.id}`)
            .then(res => res.json())
            .then(data => {
                setTaxSummary(data);
                setLoading(false);
            })
            .catch(err => {
                console.error("Error fetching tax data:", err);
                setLoading(false);
            });
    }
  }, []);

  const totalRealized = plData.reduce((acc, item) => acc + item.realized, 0);
  const totalUnrealized = plData.reduce((acc, item) => acc + item.unrealized, 0);
  const totalPL = totalRealized + totalUnrealized;

  // Pie Chart Logic
  const sortedPieData = [...plData].sort((a, b) => Math.abs(b.total) - Math.abs(a.total)).slice(0, 5);
  const pieChartTotal = sortedPieData.reduce((sum, item) => sum + Math.abs(item.total), 0);
  
  const pieData = sortedPieData.map((item) => {
    const value = Math.abs(item.total);
    const percent = pieChartTotal > 0 ? (value / pieChartTotal) * 100 : 0;
    return {
      name: `${item.symbol} (${percent.toFixed(0)}%)`,
      value: value,
    };
  });

  const exportReport = (type: 'csv') => {
    if (type === 'csv') {
      const csv = [
        ['Asset', 'Realized P&L', 'Unrealized P&L', 'Total P&L'],
        ...plData.map((item) => [item.symbol, item.realized, item.unrealized, item.total]),
        [],
        ['Tax Estimates (Year: ' + selectedYear + ')'],
        ['Short Term Gains', taxSummary?.shortTerm || 0],
        ['Long Term Gains', taxSummary?.longTerm || 0],
        ['Total Fees', taxSummary?.fees || 0],
        ['Est. Taxable Income', taxSummary?.estimatedTaxable || 0]
      ]
        .map((row) => row.join(','))
        .join('\n');

      const blob = new Blob([csv], { type: 'text/csv' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `pl-report-${selectedYear}.csv`;
      a.click();
    }
  };

  if (loading) return <div className="p-8 text-center text-slate-400">Loading Report Data...</div>;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl text-white mb-1">P&L Reports</h2>
          <p className="text-slate-400">Profit & Loss analysis and tax reporting</p>
        </div>
        <div className="flex gap-3">
          <select
            value={selectedYear}
            onChange={(e) => setSelectedYear(e.target.value)}
            className="bg-slate-800/50 border border-slate-700 rounded-lg px-4 py-2 text-white focus:outline-none focus:border-blue-500"
          >
            <option value="2025">2025</option>
            <option value="2024">2024</option>
          </select>
          <button
            onClick={() => exportReport('csv')}
            className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg transition-colors"
          >
            <Download className="w-4 h-4" />
            Export CSV
          </button>
        </div>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="bg-slate-800/50 rounded-xl border border-slate-700 p-6">
          <div className="flex items-center gap-3 mb-2">
            <TrendingUp className="w-5 h-5 text-green-400" />
            <p className="text-sm text-slate-400">Realized P&L</p>
          </div>
          <p className={`text-2xl ${totalRealized >= 0 ? 'text-green-400' : 'text-red-400'}`}>
            ${totalRealized.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
          </p>
          <p className="text-xs text-slate-500 mt-1">From closed positions</p>
        </div>
        <div className="bg-slate-800/50 rounded-xl border border-slate-700 p-6">
          <div className="flex items-center gap-3 mb-2">
            <TrendingDown className="w-5 h-5 text-blue-400" />
            <p className="text-sm text-slate-400">Unrealized P&L</p>
          </div>
          <p className={`text-2xl ${totalUnrealized >= 0 ? 'text-green-400' : 'text-red-400'}`}>
            ${totalUnrealized.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
          </p>
          <p className="text-xs text-slate-500 mt-1">From open positions</p>
        </div>
        <div className="bg-slate-800/50 rounded-xl border border-slate-700 p-6">
          <div className="flex items-center gap-3 mb-2">
            <DollarSign className="w-5 h-5 text-yellow-400" />
            <p className="text-sm text-slate-400">Total P&L</p>
          </div>
          <p className={`text-2xl ${totalPL >= 0 ? 'text-green-400' : 'text-red-400'}`}>
            ${totalPL.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
          </p>
          <p className="text-xs text-slate-500 mt-1">Combined positions</p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* P&L Bar Chart */}
        <div className="bg-slate-800/50 rounded-xl border border-slate-700 p-6">
          <h3 className="text-lg text-white mb-4">P&L by Asset</h3>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={plData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
              <XAxis dataKey="symbol" stroke="#94a3b8" />
              <YAxis stroke="#94a3b8" />
              <Tooltip
                contentStyle={{
                  backgroundColor: '#1e293b',
                  border: '1px solid #334155',
                  borderRadius: '8px',
                  color: '#fff',
                }}
              />
              <Bar dataKey="realized" fill="#3b82f6" name="Realized" barSize={30} minPointSize={2} />
              <Bar dataKey="unrealized" fill="#8b5cf6" name="Unrealized" barSize={30} minPointSize={2} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        {/* Portfolio Distribution */}
        <div className="bg-slate-800/50 rounded-xl border border-slate-700 p-6">
          <h3 className="text-lg text-white mb-4">P&L Distribution (Top 5)</h3>
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={pieData}
                cx="50%"
                cy="50%"
                labelLine={false}
                label={false}
                outerRadius={80}
                fill="#8884d8"
                dataKey="value"
              >
                {pieData.map((_, index) => (
                  <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip
                contentStyle={{
                  backgroundColor: '#1e293b',
                  border: '1px solid #334155',
                  borderRadius: '8px',
                  color: '#fff',
                }}
              />
              <Legend verticalAlign="bottom" height={36} iconType="circle" />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Detailed P&L Table */}
      <div className="bg-slate-800/50 rounded-xl border border-slate-700 overflow-hidden">
        <div className="p-6 border-b border-slate-700">
          <h3 className="text-lg text-white">Detailed P&L Breakdown</h3>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-slate-700">
                <th className="text-left text-sm text-slate-400 px-6 py-4">Asset</th>
                <th className="text-right text-sm text-slate-400 px-6 py-4">Realized P&L</th>
                <th className="text-right text-sm text-slate-400 px-6 py-4">Unrealized P&L</th>
                <th className="text-right text-sm text-slate-400 px-6 py-4">Total P&L</th>
              </tr>
            </thead>
            <tbody>
              {plData.length === 0 ? (
                  <tr><td colSpan={4} className="text-center py-8 text-slate-500 italic">No P&L data available yet.</td></tr>
              ) : (
                  plData.map((item) => (
                    <tr key={item.symbol} className="border-b border-slate-700/50 hover:bg-slate-700/30">
                      <td className="px-6 py-4 text-white font-bold">{item.symbol}</td>
                      <td className={`px-6 py-4 text-right ${item.realized >= 0 ? 'text-green-400' : 'text-red-400'}`}>
                        ${item.realized.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                      </td>
                      <td className={`px-6 py-4 text-right ${item.unrealized >= 0 ? 'text-green-400' : 'text-red-400'}`}>
                        ${item.unrealized.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                      </td>
                      <td className={`px-6 py-4 text-right font-medium ${item.total >= 0 ? 'text-green-400' : 'text-red-400'}`}>
                        ${item.total.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                      </td>
                    </tr>
                  ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* DYNAMIC TAX SECTION --- */}
      <div className="bg-slate-800/50 rounded-xl border border-slate-700 p-6">
        <div className="flex items-center gap-3 mb-4">
          <FileText className="w-5 h-5 text-blue-400" />
          <h3 className="text-lg text-white">Tax Summary (Estimated)</h3>
        </div>
        <div className="bg-yellow-500/10 border border-yellow-500/30 rounded-lg p-4 mb-4">
          <p className="text-sm text-yellow-400">
            <strong>Important:</strong> This is an estimated summary. Consult a tax professional.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* Short Term */}
            <div className="bg-slate-900/50 rounded-lg p-4">
              <div className="flex justify-between mb-2">
                <span className="text-sm text-slate-400">Short-term Gains</span>
                <span className="text-xs bg-red-500/20 text-red-400 px-2 py-1 rounded">Taxable</span>
              </div>
              <p className="text-xl text-white">
                ${(taxSummary?.shortTerm || 0).toLocaleString(undefined, {minimumFractionDigits: 2})}
              </p>
            </div>

            {/* Long Term */}
            <div className="bg-slate-900/50 rounded-lg p-4">
              <div className="flex justify-between mb-2">
                <span className="text-sm text-slate-400">Long-term Gains</span>
                <span className="text-xs bg-red-500/20 text-red-400 px-2 py-1 rounded">Taxable</span>
              </div>
              <p className="text-xl text-white">
                ${(taxSummary?.longTerm || 0).toLocaleString(undefined, {minimumFractionDigits: 2})}
              </p>
            </div>

            {/* Fees */}
            <div className="bg-slate-900/50 rounded-lg p-4">
              <div className="flex justify-between mb-2">
                <span className="text-sm text-slate-400">Total Fees</span>
                <span className="text-xs bg-green-500/20 text-green-400 px-2 py-1 rounded">Deductible</span>
              </div>
              <p className="text-xl text-white">
                ${(taxSummary?.fees || 0).toLocaleString(undefined, {minimumFractionDigits: 2})}
              </p>
            </div>

            {/* Net Taxable */}
            <div className="bg-blue-500/10 border border-blue-500/30 rounded-lg p-4">
              <div className="flex justify-between mb-2">
                <span className="text-sm text-blue-200">Est. Taxable Income</span>
                <Calendar className="w-4 h-4 text-blue-400" />
              </div>
              <p className="text-2xl text-white font-bold">
                ${(taxSummary?.estimatedTaxable || 0).toLocaleString(undefined, {minimumFractionDigits: 2})}
              </p>
            </div>
        </div>
      </div>
    </div>
  );
}