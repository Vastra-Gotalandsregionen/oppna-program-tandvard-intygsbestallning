package se.vgregion.portal.dentalgrant.service;

import org.datacontract.schemas._2004._07.tears_proxy.Patient;
import org.datacontract.schemas._2004._07.tears_proxy.PatientCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.tempuri.Certificate;
import org.tempuri.ITearsData;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author Patrik Bergström
 */
@Service
public class CertificateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateService.class);

    private ITearsData iTearsDataPort;
    private List<PatientCategory> patientCategories;

    public CertificateService() {
        Certificate c = new Certificate();
        iTearsDataPort = c.getPort(ITearsData.class);
    }

    @PostConstruct
    public void init() {
        updatePatientCategories();
    }

    // todo Göra med property?
    @Scheduled(fixedRate = 3600000)
    private void updatePatientCategories() {
        LOGGER.info("Updating patientCategories");
        patientCategories = iTearsDataPort.getPatientCategoryList(2).getPatientCategory();
    }

    public CertificateService(ITearsData iTearsDataPort) {
        this.iTearsDataPort = iTearsDataPort;
    }

    public Patient getPatient(String personalNumber) {
        Patient patient = iTearsDataPort.getPatient(personalNumber);

        return patient;
    }

    public List<PatientCategory> getPatientCategoryList() {
        if (patientCategories == null) {
            updatePatientCategories();
        }
        return patientCategories;
    }

    public void insertCertificate(String personNummer, Integer patientKategoriId, String namn, String adress,
                                  String postadress, String hsaId, String registreradAv, String förskrivarKod,
                                  String läkarensNamn) {

        iTearsDataPort.insertCertificate(personNummer, patientKategoriId, namn, adress, postadress, hsaId,
                registreradAv, förskrivarKod, läkarensNamn);
    }
}
