# spring-rabbitmq


docker run -d --name my-rmq -p 8080:15672 -p 5672:5672 rabbitmq:3-management

curl -X POST -H "Content-Type: application/json" -d "{\"content\":\"Testando JSON message\", \"sender\":\"Carlos\"}" http://localhost:8081/publish/json