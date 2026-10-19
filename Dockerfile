FROM eclipse-temurin:17-jre

WORKDIR /app

RUN groupadd --system --gid 10001 app \
    && useradd --system --uid 10001 --gid app --home-dir /app --shell /usr/sbin/nologin app \
    && mkdir -p /data \
    && chown app:app /app /data

COPY --chown=app:app target/crop-advisory.war /app/app.war

USER app:app

ENV APP_PORT=8080 \
    APP_ENV=docker \
    DB_PATH=/data/cropdb

VOLUME /data
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD ["curl", "--fail", "--silent", "--show-error", "--output", "/dev/null", "http://127.0.0.1:8080/actuator/health"]

ENTRYPOINT ["java", "-jar", "/app/app.war"]
