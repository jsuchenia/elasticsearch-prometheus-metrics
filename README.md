# prometheus-metrics plugin for ElasticSearch [![Build Status](https://travis-ci.org/jsuchenia/elasticsearch-prometheus-metrics.svg?branch=master)](https://travis-ci.org/jsuchenia/elasticsearch-prometheus-metrics)

[Elasticsearch](https://www.elastic.co/products/elasticsearch) exporter plugin returns internal status data in a [Prometheus](https://prometheus.io) metrics format for monitoring and alerting purposes. *Comes together with rules for alerting*
It can deliver basic information about cluster, indices and JVM status in an asynchronous way. Just add it as a target endpoint and start collecting data from the internal status metrics of elasticsearch database.

## Installation
To install this plugin just add it into your version of ES. Example for *0.15.0* version for *ES 7.4.2*:
```
bin/elasticsearch-plugin install https://github.com/jsuchenia/elasticsearch-prometheus-metrics/releases/download/0.13.3/prometheus-metrics-0.15.0-7.4.2.zip
```

and register it in your prometheus & add *rules!!*. Example docker image can be run using a command:
```
  docker run -p 9090:9090 jsuchenia/prometheus-elasticsearch:v2.15.1-0.15.0
```
and visit [http://localhost:9200](http://localhost:9200)

## Features
Run of variety of Elasticsearch versions without any dependency - just pure asynchroneus java - so it's not blocking threads with sync calls.

After installation it will expose few HTTP endpoints:
* */_prometheus/node* - with details about node including:
    * JVM - most of metric aligned with [client_java](https://github.com/prometheus/client_java) code
    * OS - metrics about an operating system
* */_prometheus/cluster* - Cluster and indices status
* */_prometheus/cluster_settings* - Cluster wide settings to check global run-time values as metrics
* */_prometheus* - Overall status - includes all metrics from above endpoints

To use it just add target URL to your prometheus: `http://elasticsearch.domain.com:9200/_prometheus`

## Console
Together with plugin & rules you can find here a [console](https://prometheus.io/docs/visualization/consoles/) templates. When extracted into /etc/prometheus/ (two directories: console and consoles_lib)
you will find out additional button with few simple dashbards about cluster and nodes

## Rules
Simple rule to monitor cluster health (v1 version):
```
ALERT ElasticSearchClusterStatus
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
Few rules that we use are located in rules file:
* Prometheus 1.X version: [elastic-rules.rule](rules/elastic-rules.rule)
* Prometheus 2.X version: [elastic-rules.rule.yml](rules/elastic-rules.rule.yml)

### Rules assumptions
* Cluster can be usntable for 10 minutes - after that we will send *severity=warning* alert
* Cluster can be unavailable for at most 1m - after that we will send *severity=critical* alert

### Hints for rules transformation:
You can convert rules by executing a command:
```
docker run --rm -it -v "`pwd`:/prometheus" --entrypoint /bin/promtool prom/prometheus update rules elastic-rules.rule
```

And validate them via command:
```
docker run --rm -it -v "`pwd`:/prometheus" --entrypoint /bin/promtool prom/prometheus check rules elastic-rules.rule.yml
```

## Supported versions by latest releases (only 7.3.X and above)
* 7.5.1
* 7.4.2
* 7.4.1
* 7.4.0
* 7.3.2

## Supported prometheus Releases
* 2.11.1

## Supported versions in previous releases
* 7.3.0
* 7.2.1
* 7.2.0
* 7.1.1
* 7.1.0
* 7.0.1
* 7.0.0
* 6.8.1
* 6.8.0
* 6.7.2
* 6.7.1
* 6.7.0
* 6.6.2
* 6.6.1
* 6.6.0
* 6.5.4
* 6.5.3
* 6.5.2
* 6.5.1
* 6.5.0
* 6.4.2
* 6.4.1
* 6.4.0
* 6.3.2
* 6.3.1
* 6.3.0
* 6.2.4
* 6.2.3
* 6.2.2
* 6.2.1
* 6.2.0
* 6.1.4
* 6.1.3
* 6.1.2
* 6.1.1
* 6.1.0
* 6.0.1
* 6.0.0

Generated .zip plugins are available at Releases section of this project (in a future in a maven repository)

## Security
Currently this plugin do not provide any REST access rules, consider to use other plugins like [Search Guard](https://github.com/floragunncom/search-guard)
