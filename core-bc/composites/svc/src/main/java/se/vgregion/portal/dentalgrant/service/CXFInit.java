package se.vgregion.portal.dentalgrant.service;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBusFactory;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * @author Patrik Bergström
 */
public class CXFInit {

    /**
     * This method is called by means of Spring configuration. It Creates a default {@link Bus} for CXF.
     */
    public void init() throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException, KeyManagementException {
        SpringBusFactory bf = new SpringBusFactory();
        Bus bus = bf.createBus("cxf-conduit.xml");
        bf.setDefaultBus(bus);
    }

}
