package leshan.server.integration.register.normal.register;

import static com.jayway.awaitility.Awaitility.await;
import static org.junit.Assert.*;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import leshan.client.lwm2m.response.OperationResponse;
import leshan.server.clienttest.TestUtils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;

import com.google.gson.Gson;

@RunWith(MockitoJUnitRunner.class)
public class UpdateTest extends AbstractRegisteringTest {
	
	@Test
	public void testRegisterUpdateAndDeregisterSync() throws UnknownHostException {
		final OperationResponse registerResponse = registerUplink.register(clientEndpoint, clientParameters, objectsAndInstances, TIMEOUT_MS);

		final String locationPath = new String(registerResponse.getLocation());

		final Long newLifetime = (long) 100001;
		clientParameters.put("lt", newLifetime.toString());
		
		final OperationResponse updateResponse = registerUplink.update(locationPath, clientParameters, objectsAndInstances, TIMEOUT_MS);
		
		validateUpdatedClientOnServer(newLifetime, 1);
		
		registerUplink.deregister(locationPath, TIMEOUT_MS);
		
		assertTrue(updateResponse.isSuccess());
		assertEquals(ResponseCode.CHANGED, updateResponse.getResponseCode());
		

	}

	@Test
	public void testRegisterUpdateAndDeregisterAsync() throws UnknownHostException {
		registerUplink.register(clientEndpoint, clientParameters, objectsAndInstances, callback);
		
		await().untilTrue(callback.isCalled());

		final String locationPath = new String(callback.getResponse().getLocation());

		final Long newLifetime = (long) 100002;
		clientParameters.put("lt", newLifetime.toString());

		callback.reset();
		registerUplink.update(locationPath, clientParameters, objectsAndInstances, callback);
		
		await().untilTrue(callback.isCalled());
		
		assertTrue(callback.isSuccess());
		assertEquals(ResponseCode.CHANGED, callback.getResponseCode());
		
		validateUpdatedClientOnServer(newLifetime, 1);

		callback.reset();
		registerUplink.deregister(locationPath, callback);
		
		await().untilTrue(callback.isCalled());
	}
	
	private void validateUpdatedClientOnServer(final Long newLifetime, final int expectedClients) {
		final Gson gson = new Gson();
		
		final String serverKnownClientsJson = TestUtils.getAPI("api/clients");
		List<Map<String, Object>> serverKnownClients = new ArrayList<>();
		serverKnownClients = gson.fromJson(serverKnownClientsJson, serverKnownClients.getClass());
		assertEquals(expectedClients, serverKnownClients.size());
		
		final Map<String, Object> clientParameters = serverKnownClients.get(0);
		assertEquals(newLifetime.doubleValue(), Double.parseDouble(clientParameters.get("lifetime").toString()), 0.001);
	}
}