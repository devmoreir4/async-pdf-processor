# spring-rabbitmq-microservices


docker run -d --name my-rmq -p 8080:15672 -p 5672:5672 rabbitmq:3-management

curl -d "test message" -H "Content-Type: application/text" -X POST http://localhost:8081/publish/message