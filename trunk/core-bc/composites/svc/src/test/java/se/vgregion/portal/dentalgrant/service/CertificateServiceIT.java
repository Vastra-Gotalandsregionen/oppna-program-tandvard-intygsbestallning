package se.vgregion.portal.dentalgrant.service;

import org.datacontract.schemas._2004._07.tears_proxy.Patient;
import org.datacontract.schemas._2004._07.tears_proxy.PatientCategory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Patrik Bergström
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:services-context.xml"})
public class CertificateServiceIT {

    @Autowired
    private CertificateService certificateService;

    @Test
    public void testGetPatient() throws Exception {
        Patient patient = certificateService.getPatient("191311214827");

        // This may need to be changed if the service is changed
        assertEquals("Magna Josefina", patient.getFörnamn().getValue());
    }

    @Test
    public void testGetPatientCategoryList() {
        List<PatientCategory> patientCategoryList = certificateService.getPatientCategoryList();

        // This may need to be changed if the service is changed
        assertEquals("S01, missbildning", patientCategoryList.get(0).getPatientCategoryName().getValue());
    }
}
