#!/bin/bash

lein jar
cp lib/*.jar /Volumes/relevance/mdex/mdex-perf/jakarta-jmeter-2.4/lib/.
cp *.jar /Volumes/relevance/mdex/mdex-perf/jakarta-jmeter-2.4/lib/ext/.