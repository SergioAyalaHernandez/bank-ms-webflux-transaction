package com.example.transactionalms.service;

import com.example.transactionalms.dto.MessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessagePublisherService {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.queue.name}")
    private String queueName;

    public void publishTransactionMessage(String transactionType, String accountId, String userId, boolean status, String mount, String finalBalance) {
        MessageDto message = new MessageDto();
        message.setIdEntidad(accountId);
        message.setFecha(LocalDateTime.now().toString());
        message.setMensaje("Se realizó una " + transactionType + " en la cuenta " + accountId + " por el usuario " + userId + " Monto de la transacción: " + mount + " monto final: " + finalBalance);
        message.setRecurso("transacción");
        message.setEstado(status);

        rabbitTemplate.convertAndSend(queueName, message);
    }

}
