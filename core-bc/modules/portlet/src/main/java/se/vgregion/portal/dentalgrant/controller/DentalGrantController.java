package se.vgregion.portal.dentalgrant.controller;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.soap.SOAPFaultException;

import org.datacontract.schemas._2004._07.tears_proxy.Patient;
import org.datacontract.schemas._2004._07.tears_proxy.PatientCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

import se.vgregion.ldapservice.LdapUser;
import se.vgregion.portal.dentalgrant.service.CertificateService;
import se.vgregion.portal.dentalgrant.service.DentalLdapService;
import se.vgregion.portal.patient.event.PersonNummer;

import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.User;
import com.liferay.portal.theme.ThemeDisplay;

/**
 * @author Patrik Bergström
 */
@Controller
@RequestMapping("VIEW")
public class DentalGrantController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DentalGrantController.class);

    private DentalLdapService dentalLdapService;
    private CertificateService certificateService;

    @Autowired
    public DentalGrantController(DentalLdapService dentalLdapService, CertificateService certificateService) {
        this.dentalLdapService = dentalLdapService;
        this.certificateService = certificateService;
    }

    @RenderMapping
    public String showForm(RenderRequest request, Model model) {
        List<PatientCategory> patientCategoryList = certificateService.getPatientCategoryList();
        model.addAttribute("patientCategoryList", patientCategoryList);

        User user = getUser(request);

        List<LdapUser> ldapUsers = dentalLdapService.searchByUserIdQuery(user.getScreenName());

        if (ldapUsers.size() != 1) {
            LOGGER.error("An LDAP search for " + user.getScreenName() + "resulted in " + ldapUsers.size()
                    + " number " + "if hits. There should be exactly one hit so this needs to be looked into.");
        } else {
            LdapUser ldapUser = ldapUsers.get(0);

            String hsaPersonPrescriptionCode = ldapUser.getAttributeValue("hsaPersonPrescriptionCode");

            if (hsaPersonPrescriptionCode != null) {
                String givenName = ldapUser.getAttributeValue("givenName");
                String sn = ldapUser.getAttributeValue("sn");

                model.addAttribute("firstName", givenName);
                model.addAttribute("lastName", sn);
                model.addAttribute("hsaPersonPrescriptionCode", hsaPersonPrescriptionCode);
            }
        }
        return "grantForm";
    }

    // todo This needs good javadoc
    @ResourceMapping
    public void queryPrescriber(ResourceRequest requst, ResourceResponse response) {
        String vgrId = requst.getParameter("vgrId");

        System.out.println("vgrId " + vgrId);

        List<LdapUser> ldapUsers = dentalLdapService.searchByUserIdQuery(vgrId + "*");

        StringBuilder json;
        if (ldapUsers.size() > 0) {
            json = new StringBuilder("{");

            for (LdapUser ldapUser : ldapUsers) {
                String foundVgrId = ldapUser.getAttributeValue("uid");
                String givenName = ldapUser.getAttributeValue("givenName");
                String sn = ldapUser.getAttributeValue("sn");
                String hsaPersonPrescriptionCode = ldapUser.getAttributeValue("hsaPersonPrescriptionCode");
                String hsaTitle = ldapUser.getAttributeValue("hsaTitle");
                String o = ldapUser.getAttributeValue("o");
                String ou = ldapUser.getAttributeValue("ou");

                json.append(String.format("'%s':{'givenName':'%s','sn':'%s','hsaPersonPrescriptionCode':'%s',"
                        + "'hsaTitle':'%s','o':'%s','ou':'%s'},", foundVgrId, givenName, sn,
                        hsaPersonPrescriptionCode, hsaTitle, o, ou));
            }

            json.deleteCharAt(json.length() - 1);
            json.append("}");
        } else {
            json = new StringBuilder("{}");
        }

        OutputStream out = null;
        BufferedOutputStream bos = null;
        try {
            out = response.getPortletOutputStream();
            bos = new BufferedOutputStream(out);

            String result = json.toString();
            // String s = "{'" + vgrId + "':'" + sn + "'}";
            // String s = "{'kalle':2,'alfons':4}";
            System.out.println(result);
            bos.write(result.getBytes());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

    // todo Validera att det är en läkare, dvs hsaTitle=Läkare
    @ActionMapping
    public void submitForm(ActionRequest request, ActionResponse response, Model model) {
        String vgrId = request.getParameter("vgrId");
        model.addAttribute("vgrId", vgrId);

        if (request.getParameter("submitSearch") != null) {
            // It's a search request
            LdapUser ldapUser = dentalLdapService.getLdapUserByUid(vgrId);

            if (ldapUser != null) {
                addAttributesToModel(model, ldapUser);
                if (ldapUser.getAttributeValue("hsaPersonPrescriptionCode") == null) {
                    model.addAttribute("message",
                            "Denna person är ej förskrivare och kan därför inte användas för " + "intyg.");
                }
            } else {
                model.addAttribute("message",
                        "Ingen användare hittades. Observera att du måste söka på fullständigt"
                                + " VGR-ID om du inte har javascript aktiverat.");
            }
        } else if (request.getParameter("submitGrant") != null || request.getParameter("renewGrant") != null
                || request.getParameter("confirmed") != null) {
            List<String> errorMessages = new ArrayList<String>();
            model.addAttribute("errorMessages", errorMessages);

            // Validate prescriber
            LdapUser ldapUser = dentalLdapService.getLdapUserByUid(vgrId);
            String hsaPersonPrescriptionCode = ldapUser.getAttributeValue("hsaPersonPrescriptionCode");
            if (ldapUser == null) {
                errorMessages.add("Personen med angivet VGR-ID kan inte hittas.");
            } else {
                addAttributesToModel(model, ldapUser);

                if (hsaPersonPrescriptionCode == null) {
                    errorMessages.add("Detta är ingen giltig förskrivare. Personen saknar förskrivarkod.");
                }
            }

            // Validate patient personalNumber
            String personalNumber = request.getParameter("personalNumber");
            model.addAttribute("personalNumber", personalNumber);
            PersonNummer pn = PersonNummer.personummer(personalNumber);
            if (!(pn.isCheckNumberValid() && pn.isDayValid() && pn.isMonthValid())) {
                errorMessages.add("Patientens personnummer är inte giltigt.");
            } else {

                Patient patient = null;
                try {
                    patient = certificateService.getPatient(personalNumber);
                    if (patient.isAvliden()) {
                        errorMessages.add("Patienten är avliden.");
                    }
                    if ("UV".equals(patient.getStatus().getValue())) {
                        errorMessages.add("Patienten har utvandrat.");
                    }
                    if ("UF".equals(patient.getStatus().getValue())) {
                        errorMessages.add("Patienten är utflyttad.");
                    }
                    if (patient.getIntygsnummerFh() != null && patient.getIntygsnummerFh() > 0) {
                        // Patient already has a grant. Is this a renew request?
                        if (request.getParameter("renewGrant") != null
                                || request.getParameter("confirmed") != null) {
                            // Do nothing here. Just do not add to errorMessages. The call to insertCertificate
                            // call
                            // will renew it automatically.
                        } else {
                            errorMessages.add("Patienten har redan ett intyg, giltigt till "
                                    + formatDate(patient.getIntygGiltighetstidFh().getValue())
                                    + ". Vill du förnya?");
                            model.addAttribute("renewOption", true);
                        }
                    }

                } catch (SOAPFaultException e) {
                    errorMessages.add("Kontroll av patient misslyckades.");
                    LOGGER.error(e.getMessage(), e);
                }

                Integer patientCategory = Integer.valueOf(request.getParameter("patientCategory"));
                model.addAttribute("patientCategory", patientCategory);

                User user = getUser(request);

                if (errorMessages.size() == 0) {
                    try {
                        String patientNamn = patient.getFörnamn() + " " + patient.getEfternamn().getValue();
                        String patientAdress = patient.getAdress().getValue();
                        String patientPostadress = patient.getPostnummer().getValue() + ", "
                                + patient.getPostort().getValue();
                        String hsaIdentity = ldapUser.getAttributeValue("hsaIdentity");
                        String prescriberFullName = ldapUser.getAttributeValue("fullName");

                        if (request.getParameter("confirmed") != null) {
                            certificateService.insertCertificate(personalNumber, patientCategory, patientNamn,
                                    patientAdress, patientPostadress,
                                    // hsaIdentity,
                                    null, user.getScreenName() + ": " + user.getFullName(),
                                    hsaPersonPrescriptionCode, prescriberFullName);
                            model.addAttribute("confirmed", true);
                        }

                        if (!patient.isSekretessmarkering()) {
                            model.addAttribute("patientNamn", patientNamn);
                            model.addAttribute("patientAdress", patientAdress);
                            model.addAttribute("patientPostadress", patientPostadress);
                        } else {
                            String hiddenText = "Dold";
                            model.addAttribute("patientNamn", hiddenText);
                            model.addAttribute("patientAdress", hiddenText);
                            model.addAttribute("patientPostadress", hiddenText);
                        }

                        model.addAttribute("hsaPersonPrescriptionCode", hsaPersonPrescriptionCode);
                        model.addAttribute("prescriberFullName", prescriberFullName);

                        response.setRenderParameter("view", "confirmation");
                    } catch (SOAPFaultException e) {
                        LOGGER.error(e.getMessage(), e);
                        errorMessages.add("Anropet misslyckades.");
                    }
                }
            }
        } else {
            throw new RuntimeException("This should not happen. Fix it.");
        }
    }

    private String formatDate(XMLGregorianCalendar calendar) {
        if (calendar == null) {
            return "{inget datum}";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(calendar.toGregorianCalendar().getTime());
    }

    @RenderMapping(params = "view=confirmation")
    public String showConfirmation() {
        return "confirmation";
    }

    private void addAttributesToModel(Model model, LdapUser ldapUser) {
        if (ldapUser == null) {
            return;
        }
        String givenName = ldapUser.getAttributeValue("givenName");
        String sn = ldapUser.getAttributeValue("sn");
        String hsaPersonPrescriptionCode = ldapUser.getAttributeValue("hsaPersonPrescriptionCode");

        model.addAttribute("firstName", givenName);
        model.addAttribute("lastName", sn);

        if (hsaPersonPrescriptionCode == null) {
            model.addAttribute("hsaPersonPrescriptionCode", "Ej förskrivare");
        } else {
            model.addAttribute("hsaPersonPrescriptionCode", hsaPersonPrescriptionCode);
        }
    }

    protected User getUser(PortletRequest request) {
        return ((ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY)).getUser();
    }
}
