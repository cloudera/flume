package com.cloudera.flume.master;
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


import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.mortbay.log.Log;

import com.cloudera.flume.master.FlumeMaster;
import com.cloudera.flume.reporter.ReportUtil;

@Path("/master")
public class FlumeMasterResource {

  @Context
  UriInfo uriInfo;

  private final FlumeMaster master;

  public FlumeMasterResource(FlumeMaster master) {
    this.master = master;
  }

  /**
   * Must be public for jersey to instantiate and process.
   */
  public FlumeMasterResource() {
    this.master = FlumeMaster.getInstance();
  }

  public static Set<Class<?>> getResources() {
    Set<Class<?>> res = new HashSet<Class<?>>();
    res.add(FlumeMasterResource.class);
    res.add(StatusManagerResource.class);
    res.add(ConfigManagerResource.class);
    res.add(CommandManagerResource.class);
    res.add(MasterAckManagerResource.class);
    return res;
  }

  @GET
  @Produces("application/json")
  public JSONObject getMaster() {
    try {
      JSONObject o = ReportUtil.toJSONObject(master.getMetrics());
      o.put("sysInfo", ReportUtil.toJSONObject(master.getSystemInfo()
          .getMetrics()));
      o.put("vmInfo", ReportUtil.toJSONObject(master.getVMInfo().getMetrics()));
      o.put("statusLink", uriInfo.getAbsolutePathBuilder().path("../status")
          .build().toASCIIString());
      o.put("configLink", uriInfo.getAbsolutePathBuilder().path("../configs")
          .build().toASCIIString());
      o.put("commandLink", uriInfo.getAbsolutePathBuilder().path("../commands")
          .build().toASCIIString());
      o.put("acksLink", uriInfo.getAbsolutePathBuilder().path("../acks")
          .build().toASCIIString());
      return o;
    } catch (JSONException e) {
      Log.warn("Problem generating json ", e);
      return new JSONObject();
    }
  }
}
