"use client";

import { useEffect, useState } from 'react';
import { Bell } from 'lucide-react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { useAuth } from '@/context/Authcontext';
import api from '@/services/api';
import Link from 'next/link';

export default function NotificationBell() {
  const { user } = useAuth();
  const [notifications, setNotifications] = useState<any[]>([]);
  const [isOpen, setIsOpen] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    if (!user) return;

    // 1. Load thông báo cũ từ API
    fetchNotifications();

    // 2. Kết nối WebSocket
    const socket = new SockJS('http://localhost:8080/ws');
    const stompClient = new Client({
      webSocketFactory: () => socket,
      onConnect: () => {
        console.log('Connected to WS');
        // Subscribe vào kênh riêng của User
        stompClient.subscribe(`/user/${user.id}/queue/notifications`, (message) => {
          const newNotif = JSON.parse(message.body);
          setNotifications(prev => [newNotif, ...prev]);
          setUnreadCount(prev => prev + 1);
          
          // Có thể thêm Toast/Sound ở đây
          playNotificationSound(); 
        });
      },
      // Thêm token nếu backend yêu cầu Auth trên WebSocket
      connectHeaders: {
         Authorization: `Bearer ${localStorage.getItem('token')}` 
      }
    });

    stompClient.activate();

    return () => {
      stompClient.deactivate();
    };
  }, [user]);

  const fetchNotifications = async () => {
    try {
      const res = await api.get('/notifications');
      setNotifications(res.data);
      setUnreadCount(res.data.filter((n: any) => !n.read).length);
    } catch (e) {
      console.error(e);
    }
  };

  const handleRead = async (notif: any) => {
    if (!notif.read) {
        await api.put(`/notifications/${notif.id}/read`);
        notif.read = true;
        setUnreadCount(prev => Math.max(0, prev - 1));
    }
    setIsOpen(false);
  };
  
  const playNotificationSound = () => {
      const audio = new Audio('/notification.mp3'); // Bạn cần file mp3 trong public folder
      audio.play().catch(e => console.log("Audio play blocked"));
  };

  return (
    <div className="relative">
      <button onClick={() => setIsOpen(!isOpen)} className="relative p-2 hover:bg-gray-100 rounded-full">
        <Bell size={24} className="text-gray-600" />
        {unreadCount > 0 && (
          <span className="absolute top-0 right-0 inline-flex items-center justify-center px-2 py-1 text-xs font-bold leading-none text-red-100 transform translate-x-1/4 -translate-y-1/4 bg-red-600 rounded-full">
            {unreadCount}
          </span>
        )}
      </button>

      {isOpen && (
        <div className="absolute right-0 mt-2 w-80 bg-white rounded-md shadow-lg overflow-hidden z-50 border border-gray-200">
          <div className="py-2">
            <div className="px-4 py-2 border-b text-sm font-bold text-gray-700">Thông báo</div>
            <div className="max-h-96 overflow-y-auto">
              {notifications.length === 0 ? (
                <div className="px-4 py-3 text-sm text-gray-500 text-center">Không có thông báo mới</div>
              ) : (
                notifications.map((notif) => (
                  <Link 
                    key={notif.id} 
                    href={notif.link || '#'} 
                    onClick={() => handleRead(notif)}
                    className={`block px-4 py-3 hover:bg-gray-50 border-b last:border-0 ${!notif.read ? 'bg-blue-50' : ''}`}
                  >
                    <p className="text-sm font-semibold text-gray-800">{notif.title}</p>
                    <p className="text-xs text-gray-600 mt-1 line-clamp-2">{notif.message}</p>
                    <p className="text-[10px] text-gray-400 mt-1">{new Date(notif.createdAt).toLocaleString('vi-VN')}</p>
                  </Link>
                ))
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}