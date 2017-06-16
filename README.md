# prometheus-metrics plugin for ElasticSearch [![Build Status](https://travis-ci.org/jsuchenia/elasticsearch-prometheus-metrics.svg?branch=master)](https://travis-ci.org/jsuchenia/elasticsearch-prometheus-metrics)

[Elasticsearch](https://www.elastic.co/products/elasticsearch) exporter plugin will return internal status data in [Prometheus](https://prometheus.io) format for monitoring purposes. It can deliver basic information about cluster, indices and JVM status. Just add it as a target endpoint and start collecting data from the internal status metrics of elasticsearch database.

## Installation
To install this plugin just add it into your version of ES. Example for *0.3.2* version for *ES 5.4.1*:
```
bin/elasticsearch-plugin install https://github.com/jsuchenia/elasticsearch-prometheus-metrics/releases/download/0.3.2/prometheus-metrics-0.3.2-5.4.1.zip"
```

## Features
Run of variety of Elasticsearch versions without any dependency - just pure asynchroneus java - so it's not blocking threads with sync calls.

After installation it will expose few HTTP endpoints:
* */_prometheus/jvm* - with details about JVM - most of metric aligned with [client_java](https://github.com/prometheus/client_java) code
* */_prometheus/indices* - details about Indices stats
* */_prometheus/cluster* - CLuster and indices status
* */_prometheus* - Overall status

To use it just add target URL to your prometheus: `http://elasticsearch.domain.com:9200/_prometheus`

## Rules
Simple rule to monitor cluster health:
```
ALERT EsClusterStatus
  IF es_status > 0
  FOR 1m
  LABELS {
    severity="critical"
  }
  ANNOTATIONS {
    description="{{$labels.instance}} reports non-healthy status of ElasticSearch cluster",
    link="http://prometheus.host:9090/alerts",
    summary="NonHealthy cluster on {{$labels.instance}}"
  }
```
Few rules that we use are located in `elasticsearch.rule` file.

## Supported versions
* 5.4.1
* 5.4.0
* 5.3.2
* 5.3.1
* 5.3.0

Generated .zip plugins are available at Releases section of this project
