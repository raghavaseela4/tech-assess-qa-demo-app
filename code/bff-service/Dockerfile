FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY target/*.jar app.jar
RUN chown -R appuser:appgroup /app
USER appuser
EXPOSE 8090
HEALTHCHECK --interval=10s --timeout=5s --retries=10 --start-period=60s \
  CMD wget -qO /dev/null http://localhost:8090/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
