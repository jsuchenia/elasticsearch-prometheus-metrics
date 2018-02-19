ARG PROMETHEUS_VERSION
FROM prom/prometheus:$PROMETHEUS_VERSION
RUN rm -f /etc/prometheus/consoles /etc/prometheus/console_libraries
RUN mkdir /etc/prometheus/rules && chmod 755 /etc/prometheus/rules
COPY consoles /etc/prometheus/consoles
COPY console_libraries /etc/prometheus/console_libraries
COPY prometheus.yml /etc/prometheus/prometheus.yml
COPY elastic-rules.yml /etc/prometheus/rules/elastic-rules.yml
ENTRYPOINT [ "/bin/prometheus", "--web.console.libraries=/etc/prometheus/console_libraries", "--web.console.templates=/etc/prometheus/consoles" ]
CMD        [ "--config.file=/etc/prometheus/prometheus.yml", "--storage.tsdb.path=/prometheus" ]
