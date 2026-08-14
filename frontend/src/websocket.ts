import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client/dist/sockjs' // Important to import from dist for Vite compatibility

class WebSocketService {
    private client: Client | null = null;
    private subscriptions: any[] = [];

    connect(onMessageReceived: (message: any) => void) {
        const token = localStorage.getItem('jwt_token');
        if (!token) return;

        this.client = new Client({
            // Fallback for browsers without native WebSockets
            webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
            debug: (str) => console.log(str),
            reconnectDelay: 5000,
            onConnect: () => {
                console.log('Connected to WebSocket:');
                // Subscribe to the global topic
                const sub = this.client!.subscribe('/topic/tasks', (message) => {
                    const task = JSON.parse(message.body);
                    onMessageReceived(task);
                });
                this.subscriptions.push(sub);
            }
        });

        this.client.activate();
    }

    disconnect() {
        this.subscriptions.forEach(sub => sub.unsubscribe());
        this.subscriptions = [];
        if (this.client) {
            this.client.deactivate();
            this.client = null;
        }
    }
}

export const wsService = new WebSocketService();