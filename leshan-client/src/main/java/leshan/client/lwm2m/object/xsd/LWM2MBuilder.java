package leshan.client.lwm2m.object.xsd;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

public class LWM2MBuilder {
	private static final String JAXB_PATH = "leshan.client.lwm2m.object.xsd";
	public static final LWM2M EMPTY = new LWM2M();

	public static LWM2M create(final String lwm2mModelFile){
		if(lwm2mModelFile == null){
			throw new IllegalArgumentException("Must pass a non-null file path.");
		}
		
		final File modelFile = new File(lwm2mModelFile);
		
		if(!modelFile.exists()){
			throw new IllegalArgumentException("The file '" + lwm2mModelFile + "' does not exist.");
		}
		
		try{
			final JAXBContext context = JAXBContext.newInstance(JAXB_PATH);
			final Unmarshaller unmarshaller = context.createUnmarshaller();
			return (LWM2M) unmarshaller.unmarshal(modelFile);
		}
		catch(final Exception e){
			return EMPTY;
		}
	}

}