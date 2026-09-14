package app.config;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service
public class NotificationService {
	@Async
	public void sendNotificationToDevice(NotificationRequestDTO request) {
        // Usuarios sem token registrado nao podem receber push. O retorno
        // evita que o Firebase rejeite toda a tentativa por token nulo/vazio.
        if (request.token() == null || request.token().isBlank()) return;
        
        Notification notification = Notification.builder()
                .setTitle(request.title())
                .setBody(request.body())
                .build();

        
        Message message = Message.builder()
                .setToken(request.token())
                .setNotification(notification)

                .putData("idPedido", request.id())
                .putData("status", request.status() != null ? request.status() : "")
                .putData("motivo", request.motivo() != null ? request.motivo() : "")
                .putData("tipo", request.tipo() != null ? request.tipo() : "")
                .build();

        try {
            // Envia a mensagem através do Firebase
//            String response = 
            		FirebaseMessaging.getInstance().send(message);
//            return "Sucesso! Mensagem enviada ID: " + response;
        } catch (Exception e) {
            e.printStackTrace();
//            return "Erro ao enviar notificação: " + e.getMessage();
        }
    }
}
