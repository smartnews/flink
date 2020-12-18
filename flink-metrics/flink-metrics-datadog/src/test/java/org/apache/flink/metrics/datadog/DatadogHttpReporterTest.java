/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.metrics.datadog;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests for the {@link DatadogHttpReporter}.
 */
public class DatadogHttpReporterTest {
	@Test
	public void metricTagTransformTest() {
		DatadogHttpReporter reporter = new DatadogHttpReporter();
		String metricTagConfig = "(.*\\.)stream\\.(.*)\\.(.*),,$1$3,,|| (.*\\.)shardId\\.(.*)\\.(.*),,$1$3,,|| (.*\\.)topic\\.\n" +
			"(.*)\\.(.*),,$1$3,,|| (.*\\.)partition\\.(.*)\\.(.*),,$1$3,,|| (flink\\.operator)\\.(.*)(\\.rocksdb\\..*),,$1$3,,statename:$2";
		reporter.metricTagTranforms = DatadogHttpReporter.parseMetricTagTransform(metricTagConfig);

		String metric = "this.metric.will.not.match.any.pattern";
		List<String> tags = new ArrayList<>();
		String newmetric = reporter.processMetricAndTagSn(metric, tags);
		assertEquals(metric, newmetric);
		assertEquals(0, tags.size());

		tags.clear();
		metric = "flink.operator.shardId.001.consumelag";
		newmetric = reporter.processMetricAndTagSn(metric, tags);
		assertEquals("flink.operator.consumelag", newmetric);
		assertEquals(0, tags.size());

		tags.clear();
		metric = "flink.operator.mystate.rocksdb.block_cause_usage";
		newmetric = reporter.processMetricAndTagSn(metric, tags);
		assertEquals("flink.operator.rocksdb.block_cause_usage", newmetric);
		assertEquals(1, tags.size());
		assertEquals("statename:mystate", tags.get(0));
	}
}
