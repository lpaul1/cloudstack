// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package com.cloud.api.commands;

import org.apache.log4j.Logger;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseAsyncCreateCmd;
import com.cloud.api.BaseCmd;
import com.cloud.api.IdentityMapper;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.PlugService;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.VirtualRouterProviderResponse;
import com.cloud.event.EventTypes;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.network.VirtualRouterProvider;
import com.cloud.network.VirtualRouterProvider.VirtualRouterProviderType;
import com.cloud.network.element.VirtualRouterElementService;
import com.cloud.user.Account;
import com.cloud.user.UserContext;

@Implementation(responseObject=VirtualRouterProviderResponse.class, description="Create a virtual router element.")
public class CreateVirtualRouterElementCmd extends BaseAsyncCreateCmd {
	public static final Logger s_logger = Logger.getLogger(CreateVirtualRouterElementCmd.class.getName());
    private static final String s_name = "createvirtualrouterelementresponse";
    
    @PlugService
    private VirtualRouterElementService _service;

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name=ApiConstants.NETWORK_SERVICE_PROVIDER_ID, type=CommandType.LONG, required=true, description="the network service provider ID of the virtual router element")
    @IdentityMapper(entityTableName="physical_network_service_providers")
    private Long nspId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public void setNspId(Long nspId) {
        this.nspId = nspId;
    }

    @Override
    public String getEntityTable() {
        return "virtual_router_providers";
    }

    public Long getNspId() {
        return nspId;
    }
    
    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getCommandName() {
        return s_name;
    }
    
    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }
    
    @Override
    public void execute(){
        UserContext.current().setEventDetails("Virtual router element Id: "+getEntityId());
        VirtualRouterProvider result = _service.getCreatedElement(getEntityId());
        if (result != null) {
            VirtualRouterProviderResponse response = _responseGenerator.createVirtualRouterProviderResponse(result);
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        }else {
            throw new ServerApiException(BaseCmd.INTERNAL_ERROR, "Failed to add Virtual Router entity to physical network");
        }
    }

    @Override
    public void create() throws ResourceAllocationException {
        VirtualRouterProvider result = _service.addElement(getNspId(), VirtualRouterProviderType.VirtualRouter);
        if (result != null) {
            setEntityId(result.getId());
        } else {
            throw new ServerApiException(BaseCmd.INTERNAL_ERROR, "Failed to add Virtual Router entity to physical network");
        }        
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_SERVICE_PROVIDER_CREATE;
    }
    
    @Override
    public String getEventDescription() {
        return  "Adding physical network ServiceProvider Virtual Router: " + getEntityId();
    }
}
