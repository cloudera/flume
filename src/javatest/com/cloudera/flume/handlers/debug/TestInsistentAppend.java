/**
 * Licensed to Cloudera, Inc. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  Cloudera, Inc. licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cloudera.flume.handlers.debug;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.codehaus.jettison.json.JSONException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudera.flume.conf.FlumeBuilder;
import com.cloudera.flume.conf.FlumeSpecException;
import com.cloudera.flume.conf.ReportTestingContext;
import com.cloudera.flume.core.EventImpl;
import com.cloudera.flume.core.EventSink;
import com.cloudera.flume.core.InterruptSinks;
import com.cloudera.flume.reporter.ReportEvent;
import com.cloudera.flume.reporter.ReportTestUtils;
import com.cloudera.flume.reporter.ReportUtil;
import com.cloudera.util.BackoffPolicy;
import com.cloudera.util.CappedExponentialBackoff;

/**
 * This tests the insistent opener (it tries many times before giving up)
 */
public class TestInsistentAppend {
	public static final Logger LOG = LoggerFactory
			.getLogger(TestInsistentAppend.class);

	/**
	 * Test insistent append metrics
	 */
	@Test
	public void testInsistentAppendMetrics() throws JSONException,
			FlumeSpecException, IOException, InterruptedException {
		ReportTestUtils.setupSinkFactory();

		EventSink snk = FlumeBuilder.buildSink(new ReportTestingContext(),
				"insistentAppend one");
		ReportEvent rpt = ReportUtil.getFlattenedReport(snk);
		LOG.info(ReportUtil.toJSONObject(rpt).toString());
		assertNotNull(rpt.getLongMetric(InsistentAppendDecorator.A_ATTEMPTS));
		assertNotNull(rpt.getLongMetric(InsistentAppendDecorator.A_GIVEUPS));
		assertNotNull(rpt.getLongMetric(InsistentAppendDecorator.A_REQUESTS));
		assertNotNull(rpt.getLongMetric(InsistentAppendDecorator.A_RETRIES));
		assertNotNull(rpt.getLongMetric(InsistentAppendDecorator.A_SUCCESSES));
		assertNotNull(rpt
				.getStringMetric("backoffPolicy.CappedExpBackoff.name"));
		assertEquals("One", rpt.getStringMetric("One.name"));
	}

	/**
	 * Enforce the semantics of interruption exception handling.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void testAppendInterruption() throws IOException,
			InterruptedException {
		BackoffPolicy bop = new CappedExponentialBackoff(10, 5000);
		EventSink snk = new InsistentAppendDecorator<EventSink>(
				InterruptSinks.appendSink(), bop);
		snk.open();
		try {
			snk.append(new EventImpl("test".getBytes()));
		} catch (InterruptedException ie) {
			// if interrupted, it should be closed.
			return;
		}
		fail("expected interruption!");
	}
}
