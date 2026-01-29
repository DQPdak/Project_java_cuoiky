'use client';

import React, { useEffect, useState } from 'react';

export default function MaintenancePage() {
  const [msg, setMsg] = useState('Hệ thống đang bảo trì, vui lòng thử lại sau.');

  useEffect(() => {
    const m = localStorage.getItem('maintenance_message');
    if (m) setMsg(m);
  }, []);

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 p-6">
      <div className="bg-white border rounded-2xl p-8 max-w-lg w-full text-center">
        <h1 className="text-2xl font-bold mb-2">Hệ thống đang bảo trì</h1>
        <p className="text-gray-600">{msg}</p>

        <button
          className="mt-6 px-4 py-2 rounded-lg bg-blue-600 text-white hover:bg-blue-700"
          onClick={() => window.location.reload()}
        >
          Thử lại
        </button>
      </div>
    </div>
  );
}
