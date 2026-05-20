import { useState, useEffect, useCallback } from 'react';
import { pushService } from '../services/pushService.js';

export function usePushNotifications() {
  const [isSupported, setIsSupported] = useState(false);
  const [permission, setPermission] = useState(typeof Notification !== 'undefined' ? Notification.permission : 'denied');
  const [isSubscribed, setIsSubscribed] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const supported = 'serviceWorker' in navigator && 'PushManager' in window;
    setIsSupported(supported);
    if (supported) {
      checkSubscription();
    } else {
      setLoading(false);
    }
  }, []);

  const checkSubscription = async () => {
    try {
      const registration = await navigator.serviceWorker.ready;
      if (registration && registration.pushManager) {
        const subscription = await registration.pushManager.getSubscription();
        setIsSubscribed(!!subscription);
      }
    } catch {
      setIsSubscribed(false);
    } finally {
      setLoading(false);
    }
  };

  const subscribe = useCallback(async () => {
    setError(null);
    try {
      if (!('Notification' in window)) {
        setError('Las notificaciones no están disponibles en este navegador.');
        return false;
      }
      if (permission !== 'granted') {
        const result = await Notification.requestPermission();
        setPermission(result);
        if (result !== 'granted') {
          setError('Permiso denegado. Activa las notificaciones desde la configuración del navegador.');
          return false;
        }
      }

      const registration = await navigator.serviceWorker.ready;
      const vapidKey = await pushService.getVapidPublicKey();
      const convertedKey = urlBase64ToUint8Array(vapidKey);

      const subscription = await registration.pushManager.subscribe({
        userVisibleOnly: true,
        applicationServerKey: convertedKey,
      });

      await pushService.subscribe(subscription);
      setIsSubscribed(true);
      return true;
    } catch (err) {
      setError(err.message || 'Error al activar notificaciones.');
      return false;
    }
  }, [permission]);

  const unsubscribe = useCallback(async () => {
    setError(null);
    try {
      const registration = await navigator.serviceWorker.ready;
      const subscription = await registration.pushManager.getSubscription();
      if (subscription) {
        await pushService.unsubscribe(subscription.endpoint);
        await subscription.unsubscribe();
      }
      setIsSubscribed(false);
    } catch (err) {
      setError(err.message || 'Error al desactivar notificaciones.');
    }
  }, []);

  const sendTest = useCallback(async () => {
    try {
      await pushService.sendTest();
    } catch (err) {
      setError(err.message || 'Error al enviar notificación de prueba.');
    }
  }, []);

  return { isSupported, permission, isSubscribed, loading, error, subscribe, unsubscribe, sendTest };
}

function urlBase64ToUint8Array(base64String) {
  const padding = '='.repeat((4 - (base64String.length % 4)) % 4);
  const base64 = (base64String + padding).replace(/-/g, '+').replace(/_/g, '/');
  const rawData = window.atob(base64);
  return Uint8Array.from([...rawData].map(ch => ch.charCodeAt(0)));
}
