package vn.edu.eaut.ems.service;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.stereotype.Service;

@Service
public class MqttService {

    private IMqttClient mqttClient;

    public MqttService() {
        try {           
            String broker = "wss://5f6dd65ef73945c2832e7dd2d5f3f8c4.s1.eu.hivemq.cloud:8884/mqtt"; 
            String clientId = MqttClient.generateClientId();
            MemoryPersistence persistence = new MemoryPersistence(); // luu tai RAM
            mqttClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            
            options.setUserName("esp32s3");
            options.setPassword("Abc@@123".toCharArray());
            
            mqttClient.connect(options);
            System.out.println("Ket noi MQTT thanh cong!");

            mqttClient.subscribe("Uptime", (topic, msg) -> {
                String payload = new String(msg.getPayload());
                System.out.println("ESP32: " + payload);
            });

        } catch (Exception e) { 
            e.printStackTrace();
        }
    }

    public void sendCommandToESP32(String topic, String command) {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                MqttMessage message = new MqttMessage(command.getBytes());
                message.setQos(1);
                mqttClient.publish(topic, message);
                System.out.println(topic + " : " + message);
            } else {
                System.out.println("MQTT chưa ket noi");
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}