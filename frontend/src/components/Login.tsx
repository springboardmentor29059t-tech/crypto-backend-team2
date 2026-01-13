import React, { useState } from 'react';
import { Shield, Mail, Lock, User, AlertCircle } from 'lucide-react';

interface LoginProps {
  onLogin: (userData: { id: number; name: string; email: string; token: string }) => void;
}

export function Login({ onLogin }: LoginProps) {
  const [isSignUp, setIsSignUp] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({ name: '', email: '', password: '' });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    // Dynamic endpoint based on mode
    const endpoint = isSignUp ? '/register' : '/login';
    const url = `http://localhost:5000/api/auth${endpoint}`;

    try {
      const response = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData),
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || data.error || 'Authentication failed');
      }

      if (isSignUp) {
        setIsSignUp(false);
        setError('Account created! Please sign in.');
      } else {
        // Pass the user object back to App.tsx
        onLogin({
          id: data.user.id || data.id, 
          name: data.user.name || data.name,
          email: data.user.email || data.email,
          token: data.token
        });
      }
    } catch (err: unknown) { 
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError('An unexpected error occurred');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-blue-900 to-slate-900 flex items-center justify-center p-4">
      <div className="w-full max-w-md bg-slate-800/50 backdrop-blur-sm rounded-xl border border-slate-700 p-8">
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-blue-600 rounded-full mb-4">
            <Shield className="w-8 h-8 text-white" />
          </div>
          <h1 className="text-2xl text-white font-bold">CryptoTracker</h1>
        </div>

        <h2 className="text-xl text-white mb-6">{isSignUp ? 'Create Account' : 'Sign In'}</h2>

        {error && (
          <div className="mb-4 p-3 bg-red-500/20 border border-red-500/50 rounded-lg flex items-center gap-2 text-red-200 text-sm">
            <AlertCircle className="w-4 h-4" /> {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          {isSignUp && (
            <div>
              <label className="block text-sm text-slate-300 mb-2">Full Name</label>
              <div className="relative">
                <User className="absolute left-3 top-3 w-5 h-5 text-slate-400" />
                <input type="text" value={formData.name} onChange={(e) => setFormData({ ...formData, name: e.target.value })} className="w-full bg-slate-900/50 border border-slate-600 rounded-lg pl-10 pr-4 py-3 text-white" required />
              </div>
            </div>
          )}
          <div>
            <label className="block text-sm text-slate-300 mb-2">Email</label>
            <div className="relative">
              <Mail className="absolute left-3 top-3 w-5 h-5 text-slate-400" />
              <input type="email" value={formData.email} onChange={(e) => setFormData({ ...formData, email: e.target.value })} className="w-full bg-slate-900/50 border border-slate-600 rounded-lg pl-10 pr-4 py-3 text-white" required />
            </div>
          </div>
          <div>
            <label className="block text-sm text-slate-300 mb-2">Password</label>
            <div className="relative">
              <Lock className="absolute left-3 top-3 w-5 h-5 text-slate-400" />
              <input type="password" value={formData.password} onChange={(e) => setFormData({ ...formData, password: e.target.value })} className="w-full bg-slate-900/50 border border-slate-600 rounded-lg pl-10 pr-4 py-3 text-white" required />
            </div>
          </div>
          <button disabled={loading} type="submit" className="w-full bg-blue-600 hover:bg-blue-700 text-white py-3 rounded-lg transition-colors">
            {loading ? 'Processing...' : (isSignUp ? 'Sign Up' : 'Sign In')}
          </button>
        </form>
        
        <div className="mt-6 text-center">
          <button onClick={() => setIsSignUp(!isSignUp)} className="text-blue-400 hover:text-blue-300 text-sm">
            {isSignUp ? 'Already have an account? Sign In' : "Don't have an account? Sign Up"}
          </button>
        </div>
      </div>
    </div>
  );
}