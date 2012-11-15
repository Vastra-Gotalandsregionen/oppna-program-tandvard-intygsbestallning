package se.vgregion.portal.dentalgrant.service.cxf;

import org.apache.cxf.binding.soap.SoapHeader;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

/**
 * @author Patrik Bergström
 */
public class AddHeaderInterceptor extends AbstractSoapInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddHeaderInterceptor.class);

    public AddHeaderInterceptor() {
        super(Phase.PREPARE_SEND);
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {
        try {
            SoapHeader toHeader = new SoapHeader(new QName("http://www.w3.org/2005/08/addressing", "To"),
                    message.get(Message.ENDPOINT_ADDRESS), new JAXBDataBinding(String.class));

            String action = findAction(message);
            SoapHeader actionHeader = new SoapHeader(new QName("http://www.w3.org/2005/08/addressing", "Action"),
                    action, new JAXBDataBinding(String.class));

            message.getHeaders().add(toHeader);
            message.getHeaders().add(actionHeader);
        } catch (JAXBException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private String findAction(SoapMessage message) {
        String action = null;
        String[] contentType = ((String) message.get("Content-Type")).replaceAll(" ", "").split(";");
        for (String part : contentType) {
            if (part.startsWith("action")) {
                String[] split = part.replaceAll("\"", "").split("=");
                action = split[1];
            }
        }
        return action;
    }
}
