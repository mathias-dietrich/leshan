/*
 * Copyright (c) 2014, Sierra Wireless
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of {{ project }} nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package leshan.server.californium.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import leshan.ObserveSpec;
import leshan.core.node.LwM2mObjectInstance;
import leshan.core.node.LwM2mResource;
import leshan.core.node.Value;
import leshan.core.request.ContentFormat;
import leshan.server.client.Client;
import leshan.server.request.CreateRequest;
import leshan.server.request.DeleteRequest;
import leshan.server.request.DiscoverRequest;
import leshan.server.request.ExecuteRequest;
import leshan.server.request.ObserveRequest;
import leshan.server.request.ReadRequest;
import leshan.server.request.WriteAttributesRequest;
import leshan.server.request.WriteRequest;

import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.junit.Test;

/**
 * Unit tests for {@link CoapRequestBuilder}
 */
public class CoapRequestBuilderTest {

    private Client newClient() throws UnknownHostException {
        Client client = mock(Client.class);
        InetAddress address = Inet4Address.getByName("127.0.0.1");
        when(client.getAddress()).thenReturn(address);
        when(client.getPort()).thenReturn(12354);
        return client;
    }

    @Test
    public void build_read_request() throws Exception {
        Client client = newClient();

        // test
        CoapRequestBuilder builder = new CoapRequestBuilder();
        ReadRequest request = new ReadRequest(client, 3, 0);
        builder.visit(request);

        // verify
        Request coapRequest = builder.getRequest();
        assertEquals(CoAP.Code.GET, coapRequest.getCode());
        assertEquals("127.0.0.1", coapRequest.getDestination().getHostAddress());
        assertEquals(12354, coapRequest.getDestinationPort());
        assertEquals("coap://localhost/3/0", coapRequest.getURI());
    }

    @Test
    public void build_read_request_with_non_default_object_path() throws Exception {
        Client client = newClient();
        when(client.getRootPath()).thenReturn("/lwm2m");

        // test
        CoapRequestBuilder builder = new CoapRequestBuilder();
        ReadRequest request = new ReadRequest(client, 3, 0, 1);
        builder.visit(request);

        // verify
        Request coapRequest = builder.getRequest();
        assertEquals("coap://localhost/lwm2m/3/0/1", coapRequest.getURI());
    }

    @Test
    public void build_read_request_with_root_path() throws Exception {
        Client client = newClient();
        when(client.getRootPath()).thenReturn("/");

        // test
        CoapRequestBuilder builder = new CoapRequestBuilder();
        ReadRequest request = new ReadRequest(client, 3);
        builder.visit(request);

        // verify
        Request coapRequest = builder.getRequest();
        assertEquals("coap://localhost/3", coapRequest.getURI());
    }

    @Test
    public void build_discover_request() throws Exception {
        Client client = newClient();

        // test
        CoapRequestBuilder builder = new CoapRequestBuilder();
        DiscoverRequest request = new DiscoverRequest(client, 3, 0);
        builder.visit(request);

        // verify
        Request coapRequest = builder.getRequest();
        assertEquals(CoAP.Code.GET, coapRequest.getCode());
        assertEquals("127.0.0.1", coapRequest.getDestination().getHostAddress());
        assertEquals(12354, coapRequest.getDestinationPort());
        assertEquals(MediaTypeRegistry.APPLICATION_LINK_FORMAT, coapRequest.getOptions().getAccept());
        assertEquals("coap://localhost/3/0", coapRequest.getURI());
    }

    @Test
    public void build_write_request() throws Exception {
        Client client = newClient();

        // test
        CoapRequestBuilder builder = new CoapRequestBuilder();
        WriteRequest request = new WriteRequest(client, 3, 0, 14, new LwM2mResource(14, Value.newStringValue("value")),
                ContentFormat.TEXT, false);
        builder.visit(request);

        // verify
        Request coapRequest = builder.getRequest();
        assertEquals(CoAP.Code.POST, coapRequest.getCode());
        assertEquals("127.0.0.1", coapRequest.getDestination().getHostAddress());
        assertEquals(12354, coapRequest.getDestinationPort());
        assertEquals("value", coapRequest.getPayloadString());
        assertEquals("coap://localhost/3/0/14", coapRequest.getURI());
    }

    @Test
    public void build_write_request_replace() throws Exception {
        Client client = newClient();

        // test
        CoapRequestBuilder builder = new CoapRequestBuilder();
        WriteRequest request = new WriteRequest(client, 3, 0, 14, new LwM2mResource(14, Value.newStringValue("value")),
                ContentFormat.TEXT, true);
        builder.visit(request);

        // verify
        Request coapRequest = builder.getRequest();
        assertEquals(CoAP.Code.PUT, coapRequest.getCode());
    }

    @Test
    public void build_write_attribute_request() throws Exception {
        Client client = newClient();

        // test
        CoapRequestBuilder builder = new CoapRequestBuilder();
        WriteAttributesRequest request = new WriteAttributesRequest(client, 3, 0, 14, new ObserveSpec.Builder()
                .minPeriod(10).maxPeriod(100).build());
        builder.visit(request);

        // verify
        Request coapRequest = builder.getRequest();
        assertEquals(CoAP.Code.PUT, coapRequest.getCode());
        assertEquals("127.0.0.1", coapRequest.getDestination().getHostAddress());
        assertEquals(12354, coapRequest.getDestinationPort());
        assertEquals("coap://localhost/3/0/14?pmin=10&pmax=100", coapRequest.getURI());
    }

    @Test
    public void build_execute_request() throws Exception {
        Client client = newClient();

        // test
        CoapRequestBuilder builder = new CoapRequestBuilder();
        ExecuteRequest request = new ExecuteRequest(client, 3, 0, 12, "params".getBytes(), ContentFormat.TEXT);
        builder.visit(request);

        // verify
        Request coapRequest = builder.getRequest();
        assertEquals(CoAP.Code.POST, coapRequest.getCode());
        assertEquals("127.0.0.1", coapRequest.getDestination().getHostAddress());
        assertEquals(12354, coapRequest.getDestinationPort());
        assertEquals("coap://localhost/3/0/12", coapRequest.getURI());
        assertEquals("params", coapRequest.getPayloadString());
        assertEquals(ContentFormat.TEXT.getCode(), coapRequest.getOptions().getContentFormat());
    }

    @Test
    public void build_create_request() throws Exception {
        Client client = newClient();

        // test
        CoapRequestBuilder builder = new CoapRequestBuilder();
        CreateRequest request = new CreateRequest(client, 12, 0, new LwM2mObjectInstance(0,
                new LwM2mResource[] { new LwM2mResource(0, Value.newStringValue("value")) }), ContentFormat.TLV);
        builder.visit(request);

        // verify
        Request coapRequest = builder.getRequest();
        assertEquals(CoAP.Code.POST, coapRequest.getCode());
        assertEquals("127.0.0.1", coapRequest.getDestination().getHostAddress());
        assertEquals(12354, coapRequest.getDestinationPort());
        assertEquals("coap://localhost/12/0", coapRequest.getURI());
        assertNotNull(coapRequest.getPayload());
        assertEquals(ContentFormat.TLV.getCode(), coapRequest.getOptions().getContentFormat());
    }

    @Test
    public void build_delete_request() throws Exception {
        Client client = newClient();

        // test
        CoapRequestBuilder builder = new CoapRequestBuilder();
        DeleteRequest request = new DeleteRequest(client, 12, 0);
        builder.visit(request);

        // verify
        Request coapRequest = builder.getRequest();
        assertEquals(CoAP.Code.DELETE, coapRequest.getCode());
        assertEquals("127.0.0.1", coapRequest.getDestination().getHostAddress());
        assertEquals(12354, coapRequest.getDestinationPort());
        assertEquals("coap://localhost/12/0", coapRequest.getURI());
    }

    @Test
    public void build_observe_request() throws Exception {
        Client client = newClient();

        // test
        CoapRequestBuilder builder = new CoapRequestBuilder();
        ObserveRequest request = new ObserveRequest(client, 12, 0);
        builder.visit(request);

        // verify
        Request coapRequest = builder.getRequest();
        assertEquals(CoAP.Code.GET, coapRequest.getCode());
        assertEquals(0, coapRequest.getOptions().getObserve().intValue());
        assertEquals("127.0.0.1", coapRequest.getDestination().getHostAddress());
        assertEquals(12354, coapRequest.getDestinationPort());
        assertEquals("coap://localhost/12/0", coapRequest.getURI());
    }
}
