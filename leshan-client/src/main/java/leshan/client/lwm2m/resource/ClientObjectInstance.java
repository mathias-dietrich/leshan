package leshan.client.lwm2m.resource;

import static ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.CONTENT;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import leshan.server.lwm2m.tlv.Tlv;
import leshan.server.lwm2m.tlv.TlvEncoder;
import ch.ethz.inf.vs.californium.server.resources.CoapExchange;
import ch.ethz.inf.vs.californium.server.resources.Resource;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;

class ClientObjectInstance extends ResourceBase {

	public ClientObjectInstance(final int instanceID, final Map<Integer, ClientResource> resources) {
		super(Integer.toString(instanceID));
		for(final Map.Entry<Integer, ClientResource> entry : resources.entrySet()){
			add(entry.getValue());
		}
	}

	public int getInstanceId() {
		return Integer.parseInt(getName());
	}

	public Tlv[] asTlvArray() {
		final List<Tlv> tlvs = new ArrayList<>();
		for (final Resource res : getChildren()) {
			final ClientResource resource = (ClientResource) res;
			if (resource.isReadable()) {
				tlvs.add(resource.asTlv());
			}
		}
		final Tlv[] tlvArray = tlvs.toArray(new Tlv[0]);
		return tlvArray;
	}

	@Override
	public void handleGET(final CoapExchange exchange) {
		final Tlv[] tlvArray = asTlvArray();
		exchange.respond(CONTENT, TlvEncoder.encode(tlvArray).array());
	}

}