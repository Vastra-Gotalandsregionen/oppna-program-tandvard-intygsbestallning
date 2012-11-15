package se.vgregion.portal.dentalgrant.service;

import org.datacontract.schemas._2004._07.tears_proxy.Patient;
import org.datacontract.schemas._2004._07.tears_proxy.PatientCategory;
import org.springframework.stereotype.Service;
import org.tempuri.Certificate;
import org.tempuri.ITearsData;

import javax.xml.soap.SOAPException;
import java.util.List;

/**
 * @author Patrik Bergström
 */
@Service
public class CertificateService {

    private ITearsData iTearsDataPort;

    public CertificateService() {
        Certificate c = new Certificate();
        iTearsDataPort = c.getPort(ITearsData.class);
    }

    public CertificateService(ITearsData iTearsDataPort) {
        this.iTearsDataPort = iTearsDataPort;
    }

    public Patient getPatient(String personalNumber) throws SOAPException {
        Patient patient = iTearsDataPort.getPatient(personalNumber);

        return patient;
    }

    public List<PatientCategory> getPatientCategoryList(Integer groupId) {
        return iTearsDataPort.getPatientCategoryList(groupId).getPatientCategory();
    }

    public void insertCertificate(String personNummer, Integer patientKategoriId, String namn, String adress,
                                  String postadress, String hsaId, String registreradAv, String förskrivarKod,
                                  String läkarensNamn) {

        iTearsDataPort.insertCertificate(personNummer, patientKategoriId, namn, adress, postadress, hsaId,
                registreradAv, förskrivarKod, läkarensNamn);
    }
}
