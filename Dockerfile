FROM openjdk:8-slim
COPY . /home/user/planning
RUN chown -R 1000:1000 /home/user && chmod 0700 /home/user
USER 1000
WORKDIR /home/user/planning
RUN ./gradlew --no-daemon -g /home/user/.gradle build

FROM openjdk:8-jre-slim
COPY --from=0 /home/user/.gradle /home/user/.gradle
COPY --from=0 /home/user/planning /home/user/planning
RUN chown -R 1000:1000 /home/user && chmod 0700 /home/user
EXPOSE 9080/tcp
USER 1000
WORKDIR /home/user/planning
CMD ["./gradlew", "--no-daemon", "-g", "/home/user/.gradle", "run"]
