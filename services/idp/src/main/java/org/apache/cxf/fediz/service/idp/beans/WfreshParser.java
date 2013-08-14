/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cxf.fediz.service.idp.beans;

import java.util.Date;

//import org.apache.cxf.fediz.service.idp.model.IDPConfig;
import org.apache.cxf.fediz.service.idp.util.WebUtils;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.webflow.execution.RequestContext;

/**
 * This class is responsible to parse 'wfresh' parameter 
 * @author T.Beucher 
 */

public class WfreshParser {

//    private static final String IDP_CONFIG = "idpConfig";
    private static final Logger LOG = LoggerFactory
            .getLogger(WfreshParser.class);

    public boolean authenticationRequired(String wfresh, String whr, RequestContext context)
        throws Exception {
        
        SecurityToken idpToken = 
            (SecurityToken) WebUtils.getAttributeFromExternalContext(context, whr);
//        if ("1".equals(wfresh)) {
        if (idpToken.isExpired()) {
            LOG.info("[IDP_TOKEN=" + idpToken.getId() + "] is expired.");
//            forceFurtherAuthentication(context, whr, idpToken);
            return true;
        }

        if (wfresh == null || wfresh.trim().isEmpty()) {
            return false;
        }

        long ttl;
        try {
            ttl = Long.parseLong(wfresh.trim());
        } catch (Exception e) {
            LOG.info("wfresh value '" + wfresh + "' is invalid.");
            return false;
        }
        if (ttl > 0) {

            Date createdDate = idpToken.getCreated();
            if (createdDate != null) {
                Date expiryDate = new Date();
                expiryDate.setTime(createdDate.getTime() + (ttl * 60L * 1000L));
                if (expiryDate.before(new Date())) {
                    LOG.info("[IDP_TOKEN="
                            + idpToken.getId()
                            + "] is valid but relying party requested new authentication caused by wfresh="
                            + wfresh + " outdated.");
//                    forceFurtherAuthentication(context, whr, idpToken);
                    return true;
                }
            } else {
                LOG.info("token creation date not set. Unable to check wfresh is outdated.");
            }
        } else {
            LOG.info("ttl value '" + ttl + "' is negative.");
        }
        return false;
    }

//    private void forceFurtherAuthentication(RequestContext context, String whr, SecurityToken idpToken) {
//        if (isThisRealm(context, whr)) {
//            SecurityContextHolder.clearContext();
//            LOG.info("Security context has been cleared");
//            WebUtils.removeAttributeFromExternalContext(context, whr);
//            LOG.info("[IDP_TOKEN=" + idpToken.getId() + "] has been uncached.");
//        }
//    }
//
//    private boolean isThisRealm(RequestContext context, String whr) {
//        IDPConfig idpConfig = (IDPConfig)WebUtils.getAttributeFromFlowScope(context, IDP_CONFIG);
//        if (idpConfig.getRealm().equals(whr)) {
//            return true;
//        }
//        return false;
//    }
}
