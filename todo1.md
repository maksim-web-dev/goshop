docker inspect --format='{{json .State.Health}}' localization-service

curl http://localhost:8083/actuator/health/readiness